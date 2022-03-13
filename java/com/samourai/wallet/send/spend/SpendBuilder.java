package com.samourai.wallet.send.spend;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.beans.SpendError;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.exceptions.SpendException;
import com.samourai.wallet.send.provider.UtxoProvider;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SpendBuilder {
    private static final Logger log = LoggerFactory.getLogger(SpendBuilder.class);

    private NetworkParameters params;
    private UtxoProvider utxoProvider;
    private Runnable restoreChangeIndexes; // may be null

    public SpendBuilder(NetworkParameters params, UtxoProvider utxoProvider, Runnable restoreChangeIndexes) {
        this.params = params;
        this.utxoProvider = utxoProvider;
        this.restoreChangeIndexes = restoreChangeIndexes;
    }

    // forcedChangeType may be null
    public SpendTx preview(WhirlpoolAccount account, String address, long amount, boolean boltzmann, boolean rbfOptIn, BigInteger feePerKb, BipFormat forcedChangeFormat, List<MyTransactionOutPoint> preselectedInputs, long blockHeight) throws Exception {
        BipFormat addressFormat = computeAddressFormat(forcedChangeFormat, address, utxoProvider.getBipFormatSupplier(), params);

        // if possible, get UTXO by input 'type': p2pkh, p2sh-p2wpkh or p2wpkh, else get all UTXO
        long neededAmount = computeNeededAmount(account, amount, addressFormat, feePerKb);

        // get all UTXO
        Collection<UTXO> utxos = findUtxos(neededAmount, account, addressFormat, preselectedInputs);

        SpendSelection spendSelection = computeUtxoSelection(account, address, boltzmann, amount, neededAmount, utxos, addressFormat, params, feePerKb, forcedChangeFormat);
        SpendTx spendTx = spendSelection.spendTx(amount, address, addressFormat, account, rbfOptIn, params, feePerKb, restoreChangeIndexes, utxoProvider, blockHeight);
        if (spendTx != null) {
            if (log.isDebugEnabled()) {
                log.debug("spend type:" + spendSelection.getSpendType());
                log.debug("amount:" + amount);
                log.debug("total value selected:" + spendSelection.getTotalValueSelected());
                log.debug("fee:" + spendTx.getFee());
                log.debug("nb inputs:" + spendTx.getSpendFrom().size());
            }
        }
        return spendTx;
    }

    private Collection<UTXO> findUtxos(long neededAmount, WhirlpoolAccount account, BipFormat addressFormat, Collection<MyTransactionOutPoint> preselectedInputs) {
        // spend from preselected inputs
        if (preselectedInputs != null && preselectedInputs.size() > 0) {
            Collection<UTXO> utxos = new ArrayList<>();
            // sort in descending order by value
            for (MyTransactionOutPoint outPoint : preselectedInputs) {
                UTXO u = new UTXO();
                List<MyTransactionOutPoint> outs = new ArrayList<>();
                outs.add(outPoint);
                u.setOutpoints(outs);
                utxos.add(u);
            }
            return UTXO.sumValue(utxos) >= neededAmount ? utxos : new LinkedList<>();
        }

        Collection<UTXO> utxos = utxoProvider.getUtxos(account, addressFormat);

        // TODO filter-out do-not-spends
        /*
        //Filtering out do not spends
        for (String key : postmix.keySet()) {
            UTXO u = new UTXO();
            for (MyTransactionOutPoint out : postmix.get(key).getOutpoints()) {
                if (!BlockedUTXO.getInstance().contains(out.getTxHash().toString(), out.getTxOutputN())) {
                    u.getOutpoints().add(out);
                    u.setPath(postmix.get(key).getPath());
                }
            }
            if (u.getOutpoints().size() > 0) {
                utxos.add(u);
            }
        }*/

        // spend by addressType
        if (UTXO.sumValue(utxos) >= neededAmount) {
            return utxos;
        }

        // do not mix AddressTypes for postmix
        if (account == WhirlpoolAccount.POSTMIX) {
            return new LinkedList<>(); // not enough postmix
        }

        // fallback by mixed type
        utxos = utxoProvider.getUtxos(account);
        return UTXO.sumValue(utxos) >= neededAmount ? utxos : new LinkedList<>();
    }

    private SpendSelection computeUtxoSelection(WhirlpoolAccount account, String address, boolean boltzmann, long amount, long neededAmount, Collection<UTXO> utxos, BipFormat addressFormat, NetworkParameters params, BigInteger feePerKb, BipFormat forcedChangeFormat) throws SpendException {
        long balance = UTXO.sumValue(utxos);

        // insufficient balance
        if (amount > balance) {
            log.warn("InsufficientFundsException: amount="+amount+", balance="+balance);
            throw new SpendException(SpendError.INSUFFICIENT_FUNDS);
        }

        // entire balance (can only be simple spend)
        if (amount == balance) {
            // take all utxos, deduct fee
            if (log.isDebugEnabled()) {
                log.debug("SIMPLE spending all utxos");
            }
            return new SpendSelectionSimple(utxoProvider.getBipFormatSupplier(), utxos);
        }

        // boltzmann spend
        if (boltzmann) {
            SpendSelection spendSelection = SpendSelectionBoltzmann.compute(neededAmount, utxoProvider, addressFormat, amount, address, account, forcedChangeFormat, params, feePerKb, restoreChangeIndexes);
            if (spendSelection != null) {
                return spendSelection;
            }
        }

        // simple spend (less than balance)
        BipFormatSupplier bipFormatSupplier = utxoProvider.getBipFormatSupplier();

        // get smallest 1 UTXO > than spend + fee + dust
        SpendSelection spendSelection = SpendSelectionSimple.computeSpendSingle(utxos, amount, bipFormatSupplier, params, feePerKb);
        if (spendSelection != null) {
            if (log.isDebugEnabled()) {
                log.debug("SIMPLE spending smallest possible utxo");
            }
            return spendSelection;
        }

        // get largest UTXOs > than spend + fee + dust
        spendSelection = SpendSelectionSimple.computeSpendMultiple(utxos, amount, bipFormatSupplier, params, feePerKb);
        if (spendSelection != null) {
            if (log.isDebugEnabled()) {
                log.debug("SIMPLE spending multiple utxos");
            }
            return spendSelection;
        }

        // no selection found
        throw new SpendException(SpendError.MAKING);
    }

    private long computeNeededAmount(WhirlpoolAccount account, long amount, BipFormat changeFormat, BigInteger feePerKb) {
        long neededAmount = 0L;
        if (changeFormat == BIP_FORMAT.SEGWIT_NATIVE) {
            neededAmount += FeeUtil.getInstance().estimatedFeeSegwit(0, 0, UTXO.countOutpoints(utxoProvider.getUtxos(account, BIP_FORMAT.SEGWIT_NATIVE)), 4, 0, feePerKb).longValue();
//                    Log.d("segwit:" + neededAmount);
        } else if (changeFormat == BIP_FORMAT.SEGWIT_COMPAT) {
            neededAmount += FeeUtil.getInstance().estimatedFeeSegwit(0, UTXO.countOutpoints(utxoProvider.getUtxos(account, BIP_FORMAT.SEGWIT_COMPAT)), 0, 4, 0, feePerKb).longValue();
//                    Log.d("segwit:" + neededAmount);
        } else {
            neededAmount += FeeUtil.getInstance().estimatedFeeSegwit(UTXO.countOutpoints(utxoProvider.getUtxos(account, BIP_FORMAT.LEGACY)), 0, 0, 4, 0, feePerKb).longValue();
//                    Log.d("p2pkh:" + neededAmount);
        }
        neededAmount += amount;
        neededAmount += SamouraiWalletConst.bDust.longValue();
        return neededAmount;
    }

    public static BipFormat computeAddressFormat(BipFormat forcedChangeFormat, String address, BipFormatSupplier bipFormatSupplier, NetworkParameters params) {
        if (forcedChangeFormat != null) {
            return forcedChangeFormat;
        }
        return bipFormatSupplier.findByAddress(address, params);
    }
}
