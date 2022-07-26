package com.samourai.wallet.cahoots.stowaway;

import com.samourai.soroban.cahoots.CahootsContext;
import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.cahoots.AbstractCahoots2xService;
import com.samourai.wallet.cahoots.CahootsType;
import com.samourai.wallet.cahoots.CahootsUtxo;
import com.samourai.wallet.cahoots.CahootsWallet;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.util.FeeUtil;
import org.apache.commons.lang3.tuple.Triple;
import org.bitcoinj.core.*;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StowawayService extends AbstractCahoots2xService<Stowaway> {
    private static final Logger log = LoggerFactory.getLogger(StowawayService.class);

    public StowawayService(BipFormatSupplier bipFormatSupplier, NetworkParameters params) {
        super(CahootsType.STOWAWAY, bipFormatSupplier, params);
    }

    @Override
    public Stowaway startInitiator(CahootsWallet cahootsWallet, CahootsContext cahootsContext) throws Exception {
        return startInitiator(cahootsWallet, cahootsContext.getAmount(), cahootsContext.getAccount());
    }

    protected Stowaway startInitiator(CahootsWallet cahootsWallet, long amount, int account) throws Exception {
        if (amount <= 0) {
            throw new Exception("Invalid amount");
        }
        byte[] fingerprint = cahootsWallet.getFingerprint();
        Stowaway stowaway0 = doStowaway0(amount, account, fingerprint);
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway INITIATOR => step="+stowaway0.getStep());
        }
        return stowaway0;
    }

    @Override
    public Stowaway startCollaborator(CahootsWallet cahootsWallet, CahootsContext cahootsContext, Stowaway stowaway0) throws Exception {
        Stowaway stowaway1 = doStowaway1(stowaway0, cahootsWallet, cahootsContext);
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway COUNTERPARTY => step="+stowaway1.getStep());
        }
        return stowaway1;
    }

    @Override
    public Stowaway reply(CahootsWallet cahootsWallet, CahootsContext cahootsContext, Stowaway stowaway) throws Exception {
        int step = stowaway.getStep();
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway <= step="+step);
        }
        Stowaway payload;
        switch (step) {
            case 1:
                payload = doStowaway2(stowaway, cahootsWallet, cahootsContext);
                break;
            case 2:
                payload = doStep3(stowaway, cahootsWallet, cahootsContext);
                break;
            case 3:
                payload = doStep4(stowaway, cahootsWallet, cahootsContext);
                break;
            default:
                throw new Exception("Unrecognized #Cahoots step");
        }
        if (payload == null) {
            throw new Exception("Cannot compose #Cahoots");
        }
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway => step="+payload.getStep());
        }
        return payload;
    }

    //
    // sender
    //
    public Stowaway doStowaway0(long spendAmount, int account, byte[] fingerprint) {
        //
        //
        // step0: B sends spend amount to A,  creates step0
        //
        //
        Stowaway stowaway0 = new Stowaway(spendAmount, params, account, fingerprint);
        return stowaway0;
    }

    //
    // receiver
    //
    public Stowaway doStowaway1(Stowaway stowaway0, CahootsWallet cahootsWallet, CahootsContext cahootsContext) throws Exception {
        byte[] fingerprint = cahootsWallet.getFingerprint();
        stowaway0.setFingerprintCollab(fingerprint);

        int account = cahootsContext.getAccount();
        List<CahootsUtxo> utxos = cahootsWallet.getUtxosByAccount(account);
        // sort in descending order by value
        Collections.sort(utxos, new UTXO.UTXOComparator());
        if (log.isDebugEnabled()) {
            log.debug("utxos:" + utxos.size());
        }

        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        long totalContributedAmount = 0L;
        List<CahootsUtxo> highUTXO = new ArrayList<CahootsUtxo>();
        for (CahootsUtxo utxo : utxos) {
            if (utxo.getValue() > stowaway0.getSpendAmount() + SamouraiWalletConst.bDust.longValue()) {
                highUTXO.add(utxo);
            }
        }
        if(highUTXO.size() > 0)    {
            CahootsUtxo utxo = highUTXO.get(getRandNextInt(highUTXO.size()));
            if (log.isDebugEnabled()) {
                log.debug("selected random utxo: " + utxo);
            }
            selectedUTXO.add(utxo);
            totalContributedAmount = utxo.getValue();
        }
        if (selectedUTXO.size() == 0) {
            for (CahootsUtxo utxo : utxos) {
                selectedUTXO.add(utxo);
                totalContributedAmount += utxo.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("selected utxo: " + utxo);
                }
                if (stowaway0.isContributedAmountSufficient(totalContributedAmount)) {
                    break;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalContributedAmount+", requiredAmount="+stowaway0.computeRequiredAmount());
        }
        if (!stowaway0.isContributedAmountSufficient(totalContributedAmount)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }

        //
        //
        // step1: A utxos -> B (take largest that cover amount)
        //
        //

        List<TransactionInput> inputsA = new LinkedList<>();

        for (CahootsUtxo utxo : selectedUTXO) {
            TransactionInput input = utxo.getOutpoint().computeSpendInput();
            inputsA.add(input);
        }

        // destination output
        BipAddress receiveAddress = cahootsWallet.fetchAddressReceive(account, true, BIP_FORMAT.SEGWIT_NATIVE);
        if (log.isDebugEnabled()) {
            log.debug("+output (CounterParty receive) = "+receiveAddress);
        }
        List<TransactionOutput> outputsA = new LinkedList<>();
        TransactionOutput output_A0 = computeTxOutput(receiveAddress, stowaway0.getSpendAmount(), cahootsContext);
        outputsA.add(output_A0);

        stowaway0.setDestination(receiveAddress.getAddressString());
        stowaway0.setCounterpartyAccount(account);

        Stowaway stowaway1 = stowaway0.copy();
        stowaway1.doStep1(inputsA, outputsA);

        return stowaway1;
    }

    //
    // sender
    //
    public Stowaway doStowaway2(Stowaway stowaway1, CahootsWallet cahootsWallet, CahootsContext cahootsContext) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("sender account (2):" + stowaway1.getAccount());
        }

        Transaction transaction = stowaway1.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("step2 tx:" + Hex.toHexString(transaction.bitcoinSerialize()));
        }
        int nbIncomingInputs = transaction.getInputs().size();

        List<CahootsUtxo> utxos = cahootsWallet.getUtxosByAccount(stowaway1.getAccount());
        // sort in ascending order by value
        Collections.sort(utxos, new UTXO.UTXOComparator());
        Collections.reverse(utxos);

        if (log.isDebugEnabled()) {
            log.debug("utxos:" + utxos.size());
        }

        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        int nbTotalSelectedOutPoints = 0;
        long totalSelectedAmount = 0L;
        List<CahootsUtxo> lowUTXO = new ArrayList<CahootsUtxo>();
        for (CahootsUtxo utxo : utxos) {
            if(utxo.getValue() < stowaway1.getSpendAmount())    {
                lowUTXO.add(utxo);
            }
        }

        long feePerB = cahootsWallet.fetchFeePerB();

        List<List<CahootsUtxo>> listOfLists = new ArrayList<List<CahootsUtxo>>();
        shuffleUtxos(lowUTXO);
        listOfLists.add(lowUTXO);
        listOfLists.add(utxos);
        for(List<CahootsUtxo> list : listOfLists)   {

            selectedUTXO.clear();
            totalSelectedAmount = 0L;
            nbTotalSelectedOutPoints = 0;

            for (CahootsUtxo utxo : list) {
                selectedUTXO.add(utxo);
                totalSelectedAmount += utxo.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("selected utxo: " + utxo);
                }
                nbTotalSelectedOutPoints ++;
                if (stowaway1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(selectedUTXO, nbIncomingInputs, feePerB))) {

                    // discard "extra" utxo, if any
                    List<CahootsUtxo> _selectedUTXO = new ArrayList<CahootsUtxo>();
                    Collections.reverse(selectedUTXO);
                    int _nbTotalSelectedOutPoints = 0;
                    long _totalSelectedAmount = 0L;
                    for (CahootsUtxo utxoSel : selectedUTXO) {
                        _selectedUTXO.add(utxoSel);
                        _totalSelectedAmount += utxoSel.getValue();
                        if (log.isDebugEnabled()) {
                            log.debug("post selected utxo: " + utxoSel);
                        }
                        _nbTotalSelectedOutPoints ++;
                        if (stowaway1.isContributedAmountSufficient(_totalSelectedAmount, estimatedFee(_selectedUTXO, nbIncomingInputs, feePerB))) {
                            selectedUTXO.clear();
                            selectedUTXO.addAll(_selectedUTXO);
                            totalSelectedAmount = _totalSelectedAmount;
                            nbTotalSelectedOutPoints = _nbTotalSelectedOutPoints;
                            break;
                        }
                    }

                    break;
                }
            }
            if (stowaway1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(selectedUTXO, nbIncomingInputs, feePerB))) {
                break;
            }
        }

        long estimatedFee = estimatedFee(selectedUTXO, nbIncomingInputs, feePerB);
        if (log.isDebugEnabled()) {
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalSelectedAmount+", requiredAmount="+stowaway1.computeRequiredAmount(estimatedFee));
        }
        if (!stowaway1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }

        long fee = estimatedFee(selectedUTXO, nbIncomingInputs, feePerB);
        if (log.isDebugEnabled()) {
            log.debug("fee:" + fee);
        }

        //
        //
        // step2: B verif, utxos -> A (take smallest that cover amount)
        //
        //

        List<TransactionInput> inputsB = new LinkedList<>();
        for (CahootsUtxo utxo : selectedUTXO) {
            TransactionInput input = utxo.getOutpoint().computeSpendInput();
            inputsB.add(input);
        }

        List<TransactionOutput> outputsB = new LinkedList<>();

        // tx: modify spend output
        long contributedAmount = 0L;
        for(Long value : stowaway1.getOutpoints().values())   {
            contributedAmount += value;
        }
        long outputAmount = transaction.getOutput(0).getValue().longValue();
        TransactionOutput spendOutput = transaction.getOutput(0);
        spendOutput.setValue(Coin.valueOf(outputAmount + contributedAmount));
        outputsB.add(spendOutput);
        stowaway1.getTransaction().clearOutputs(); // replace spend output by the new one

        // change output
        BipAddress changeAddress = cahootsWallet.fetchAddressChange(stowaway1.getAccount(), true);
        if (log.isDebugEnabled()) {
            log.debug("+output (sender change) = "+changeAddress);
        }
        TransactionOutput output_B0 = computeTxOutput(changeAddress, (totalSelectedAmount - stowaway1.getSpendAmount()) - fee, cahootsContext);
        outputsB.add(output_B0);

        Stowaway stowaway2 = stowaway1.copy();
        stowaway2.doStep2(inputsB, outputsB);
        stowaway2.setFeeAmount(fee);

        return stowaway2;
    }

    private long estimatedFee(int nbTotalSelectedOutPoints, int nbIncomingInputs, long feePerB) {
        return FeeUtil.getInstance().estimatedFeeSegwit(0, 0, nbTotalSelectedOutPoints + nbIncomingInputs, 2, 0, feePerB);
    }

    private long estimatedFee(List<CahootsUtxo> utxos, int nbIncomingInputs, long feePerB) {
        Vector<MyTransactionOutPoint> outpoints = new Vector<>();
        for(CahootsUtxo utxo : utxos) {
            outpoints.add(utxo.getOutpoint());
        }
        Triple<Integer, Integer, Integer> outpointCounts = FeeUtil.getInstance().getOutpointCount(outpoints, params);
        return FeeUtil.getInstance().estimatedFeeSegwit(outpointCounts.getLeft() + nbIncomingInputs, outpointCounts.getMiddle(), outpointCounts.getRight(), 4, 0, feePerB);
    }

    @Override
    protected long computeMaxSpendAmount(long minerFee, CahootsContext cahootsContext) throws Exception {
        long maxSpendAmount;
        switch (cahootsContext.getTypeUser()) {
            case SENDER:
                // spends amount + minerFee
                maxSpendAmount = cahootsContext.getAmount()+minerFee;
                break;
            case COUNTERPARTY:
                // receives money (<0)
                maxSpendAmount = 0;
                break;
            default:
                throw new Exception("Unknown typeUser");
        }
        return maxSpendAmount;
    }
}
