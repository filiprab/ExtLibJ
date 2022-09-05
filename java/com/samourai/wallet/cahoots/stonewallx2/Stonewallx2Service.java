package com.samourai.wallet.cahoots.stonewallx2;

import com.samourai.soroban.cahoots.Stonewallx2Context;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.cahoots.AbstractCahoots2xService;
import com.samourai.wallet.cahoots.CahootsType;
import com.samourai.wallet.cahoots.CahootsUtxo;
import com.samourai.wallet.cahoots.CahootsWallet;
import com.samourai.wallet.cahoots.multi.Stonewallx2InputData;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.xmanager.client.XManagerClient;
import com.samourai.xmanager.protocol.XManagerService;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Stonewallx2Service extends AbstractCahoots2xService<STONEWALLx2, Stonewallx2Context> {
    private static final Logger log = LoggerFactory.getLogger(Stonewallx2Service.class);

    public Stonewallx2Service(BipFormatSupplier bipFormatSupplier, NetworkParameters params) {
        super(CahootsType.STONEWALLX2, bipFormatSupplier, params);
    }

    @Override
    public void verifyResponse(Stonewallx2Context cahootsContext, STONEWALLx2 cahoots, STONEWALLx2 request) throws Exception {
        super.verifyResponse(cahootsContext, cahoots, request);

        if (request != null) {
            // properties should never change once set
            if (!StringUtils.equals(cahoots.paynymDestination, request.paynymDestination)) {
                throw new Exception("Invalid altered Cahoots strPayNymDestination");
            }
        }
    }

    @Override
    public STONEWALLx2 startInitiator(CahootsWallet cahootsWallet, Stonewallx2Context cahootsContext) throws Exception {
        return startInitiator(cahootsWallet, cahootsContext.getAmount(), cahootsContext.getAccount(), cahootsContext.getAddress(), cahootsContext.getPaynymDestination());
    }

    protected STONEWALLx2 startInitiator(CahootsWallet cahootsWallet, long amount, int account, String address, String paynymDestination) throws Exception {
        if (amount <= 0) {
            throw new Exception("Invalid amount");
        }
        if (StringUtils.isEmpty(address)) {
            throw new Exception("Invalid address");
        }
        byte[] fingerprint = cahootsWallet.getFingerprint();
        STONEWALLx2 stonewall0 = doSTONEWALLx2_0(amount, address, paynymDestination, account, fingerprint);
        if (log.isDebugEnabled()) {
            log.debug("# STONEWALLx2 INITIATOR => step="+stonewall0.getStep());
        }
        return stonewall0;
    }

    @Override
    public STONEWALLx2 startCollaborator(CahootsWallet cahootsWallet, Stonewallx2Context cahootsContext, STONEWALLx2 stonewall0) throws Exception {
        STONEWALLx2 stonewall1 = doSTONEWALLx2_1(stonewall0, cahootsWallet, cahootsContext);
        if (log.isDebugEnabled()) {
            log.debug("# STONEWALLx2 COUNTERPARTY => step="+stonewall1.getStep());
        }
        return stonewall1;
    }

    @Override
    public STONEWALLx2 reply(CahootsWallet cahootsWallet, Stonewallx2Context cahootsContext, STONEWALLx2 stonewall) throws Exception {
        int step = stonewall.getStep();
        if (log.isDebugEnabled()) {
            log.debug("# STONEWALLx2 "+cahootsContext.getTypeUser()+" <= step="+step);
        }
        STONEWALLx2 payload;
        switch (step) {
            case 1:
                payload = doSTONEWALLx2_2(stonewall, cahootsWallet, cahootsContext);
                break;
            case 2:
                payload = doStep3(stonewall, cahootsWallet, cahootsContext);
                break;
            case 3:
                payload = doStep4(stonewall, cahootsWallet, cahootsContext);
                break;
            default:
                throw new Exception("Unrecognized #Cahoots step");
        }
        if (payload == null) {
            throw new Exception("Cannot compose #Cahoots");
        }
        if (log.isDebugEnabled()) {
            log.debug("# STONEWALLx2 "+cahootsContext.getTypeUser()+" => step="+payload.getStep());
        }
        return payload;
    }

    //
    // sender
    //
    private STONEWALLx2 doSTONEWALLx2_0(long spendAmount, String address, String paynymDestination, int account, byte[] fingerprint) {
        //
        //
        // step0: B sends spend amount to A,  creates step0
        //
        //
        STONEWALLx2 stonewall0 = new STONEWALLx2(spendAmount, address, paynymDestination, params, account, fingerprint);
        return stonewall0;
    }



    //
    // counterparty
    //
    private Stonewallx2InputData getInputData(CahootsWallet cahootsWallet, Stonewallx2Context cahootsContext, STONEWALLx2 stonewall0, int account, List<String> seenTxs) throws Exception {
        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(account);
        shuffleUtxos(utxos);

        if (log.isDebugEnabled()) {
            log.debug("BIP84 utxos:" + utxos.size());
        }

        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        long totalContributedAmount = 0L;
        for (int step = 0; step < 3; step++) {

            if (stonewall0.getCounterpartyAccount() == 0) {
                step = 2;
            }

            selectedUTXO = new ArrayList<CahootsUtxo>();
            totalContributedAmount = 0L;
            for (CahootsUtxo utxo : utxos) {

                switch (step) {
                    case 0:
                        if (utxo.getPath() != null && utxo.getPath().length() > 3 && utxo.getPath().charAt(2) != '0') {
                            continue;
                        }
                        break;
                    case 1:
                        if (utxo.getPath() != null && utxo.getPath().length() > 3 && utxo.getPath().charAt(2) != '1') {
                            continue;
                        }
                        break;
                    default:
                        break;
                }

                MyTransactionOutPoint outpoint = utxo.getOutpoint();
                if (!seenTxs.contains(outpoint.getHash().toString())) {
                    seenTxs.add(outpoint.getHash().toString());

                    selectedUTXO.add(utxo);
                    totalContributedAmount += utxo.getValue();
                    if (log.isDebugEnabled()) {
                        log.debug("BIP84 selected utxo: " + utxo);
                    }
                }

                if (stonewall0.isContributedAmountSufficient(totalContributedAmount)) {
                    break;
                }
            }
            if (stonewall0.isContributedAmountSufficient(totalContributedAmount)) {
                break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalContributedAmount+", requiredAmount="+stonewall0.computeRequiredAmount());
        }
        if (!stonewall0.isContributedAmountSufficient(totalContributedAmount)) {
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
            cahootsContext.addInput(utxo);
        }

        return new Stonewallx2InputData(totalContributedAmount, utxos, inputsA);
    }
    private BipAddress getContributorMixAddress(CahootsWallet cahootsWallet, STONEWALLx2 stonewall0, boolean increment, BipFormat bipFormat) throws Exception {
        BipAddress receiveAddress = cahootsWallet.fetchAddressChange(stonewall0.getCounterpartyAccount(), increment, bipFormat);
        if (receiveAddress.getAddressString().equalsIgnoreCase(stonewall0.getDestination())) {
            receiveAddress = cahootsWallet.fetchAddressChange(stonewall0.getCounterpartyAccount(), increment, bipFormat);
        }
        return receiveAddress;
    }

    public STONEWALLx2 doSTONEWALLx2_1_Multi(STONEWALLx2 stonewall0, CahootsWallet cahootsWallet, Stonewallx2Context cahootsContext, List<String> seenTxs, XManagerClient xManagerClient) throws Exception {
        int account = cahootsContext.getAccount();
        Coin balance = CahootsUtxo.sumValue(cahootsWallet.getUtxosWpkhByAccount(account));
        boolean isBech32Destination = FormatsUtilGeneric.getInstance().isValidBech32(stonewall0.getDestination());
        if(balance.isGreaterThan(THRESHOLD) && isBech32Destination) {
            // mix to external
            String xmAddress = xManagerClient.getAddressOrDefault(XManagerService.STONEWALL, 3);
            if(!xmAddress.equals(XManagerService.STONEWALL.getDefaultAddress(params == TestNet3Params.get()))) {
                log.info("EXTRACTING FUNDS TO EXTERNAL WALLET > " + xmAddress);
                TransactionOutput mixOutput = computeTxOutput(xmAddress, stonewall0.getSpendAmount(), cahootsContext);
                if (log.isDebugEnabled()) {
                    log.debug("+output (CounterParty Mix) = " + xmAddress);
                }
                return doSTONEWALLx2_1(mixOutput, cahootsWallet, stonewall0, cahootsContext, seenTxs);
            }
        }

        // regular STONEWALLx2
        return doSTONEWALLx2_1(stonewall0, cahootsWallet, cahootsContext);
    }

    private STONEWALLx2 doSTONEWALLx2_1(STONEWALLx2 stonewall0, CahootsWallet cahootsWallet, Stonewallx2Context cahootsContext) throws Exception {
        stonewall0.setCounterpartyAccount(cahootsContext.getAccount());
        byte[] fingerprint = cahootsWallet.getFingerprint();
        stonewall0.setFingerprintCollab(fingerprint);

        List<String> seenTxs = new ArrayList<>();
        // contributor mix output: like-typed with destination
        BipFormat bipFormatDestination = getBipFormatSupplier().findByAddress(stonewall0.getDestination(), params);
        log.debug("BIP FORMAT:: " + bipFormatDestination.getId());
        BipAddress receiveAddress = getContributorMixAddress(cahootsWallet, stonewall0, true, bipFormatDestination);
        TransactionOutput mixOutput = computeTxOutput(receiveAddress, stonewall0.getSpendAmount(), cahootsContext);
        if (log.isDebugEnabled()) {
            log.debug("+output (CounterParty Mix) = " + receiveAddress.getAddressString());
        }
        return doSTONEWALLx2_1(mixOutput, cahootsWallet, stonewall0, cahootsContext, seenTxs);
    }

    private STONEWALLx2 doSTONEWALLx2_1(TransactionOutput mixOutput, CahootsWallet cahootsWallet, STONEWALLx2 stonewall0, Stonewallx2Context cahootsContext, List<String> seenTxs) throws Exception {
        debug("BEGIN doSTONEWALLx2_1", stonewall0, cahootsContext);

        int account = cahootsContext.getAccount();
        Stonewallx2InputData inputData = getInputData(cahootsWallet, cahootsContext, stonewall0, account, seenTxs);

        List<TransactionInput> inputsA = inputData.getInputs();

        List<TransactionOutput> outputsA = new LinkedList<>();
        outputsA.add(mixOutput);

        // contributor change output
        BipAddress changeAddress = cahootsWallet.fetchAddressChange(stonewall0.getCounterpartyAccount(), true, BIP_FORMAT.SEGWIT_NATIVE);
        if (log.isDebugEnabled()) {
            log.debug("+output (CounterParty change) = " + changeAddress);
        }
        TransactionOutput output_A1 = computeTxOutput(changeAddress, inputData.getContributedAmount() - stonewall0.getSpendAmount(), cahootsContext);
        outputsA.add(output_A1);
        stonewall0.setCollabChange(changeAddress.getAddressString());

        STONEWALLx2 stonewall1 = stonewall0.copy();
        stonewall1.doStep1(inputsA, outputsA);

        debug("END doSTONEWALLx2_1", stonewall1, cahootsContext);
        return stonewall1;
    }

    //
    // sender
    //
    private STONEWALLx2 doSTONEWALLx2_2(STONEWALLx2 stonewall1, CahootsWallet cahootsWallet, Stonewallx2Context cahootsContext) throws Exception {
        return doSTONEWALLx2_2(stonewall1, cahootsWallet, cahootsContext, new ArrayList<>());
    }
    public STONEWALLx2 doSTONEWALLx2_2(STONEWALLx2 stonewall1, CahootsWallet cahootsWallet, Stonewallx2Context cahootsContext, List<String> seenTxs) throws Exception {
        debug("BEGIN doSTONEWALLx2_2", stonewall1, cahootsContext);

        Transaction transaction = stonewall1.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("step2 tx:" + Hex.toHexString(transaction.bitcoinSerialize()));
            log.debug("step2 tx:" + transaction);
        }
        int nbIncomingInputs = transaction.getInputs().size();

        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(stonewall1.getAccount());
        shuffleUtxos(utxos);

        if (log.isDebugEnabled()) {
            log.debug("BIP84 utxos:" + utxos.size());
        }

        for (TransactionInput input : transaction.getInputs()) {
            if (!seenTxs.contains(input.getOutpoint().getHash().toString())) {
                seenTxs.add(input.getOutpoint().getHash().toString());
            }
        }

        long feePerB = cahootsContext.getFeePerB();
        String destination = stonewall1.getDestination();

        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        long totalSelectedAmount = 0L;
        int nbTotalSelectedOutPoints = 0;
        for (int step = 0; step < 3; step++) {

            if (stonewall1.getCounterpartyAccount() == 0) {
                step = 2;
            }

            List<String> _seenTxs = seenTxs;
            selectedUTXO = new ArrayList<CahootsUtxo>();
            totalSelectedAmount = 0;
            nbTotalSelectedOutPoints = 0;
            for (CahootsUtxo utxo : utxos) {

                switch (step) {
                    case 0:
                        if (utxo.getPath() != null && utxo.getPath().length() > 3 && utxo.getPath().charAt(2) != '0') {
                            continue;
                        }
                        break;
                    case 1:
                        if (utxo.getPath() != null && utxo.getPath().length() > 3 && utxo.getPath().charAt(2) != '1') {
                            continue;
                        }
                        break;
                    default:
                        break;
                }

                if (!_seenTxs.contains(utxo.getOutpoint().getHash().toString())) {
                    _seenTxs.add(utxo.getOutpoint().getHash().toString());

                    selectedUTXO.add(utxo);
                    totalSelectedAmount += utxo.getValue();
                    nbTotalSelectedOutPoints ++;
                    if (log.isDebugEnabled()) {
                        log.debug("BIP84 selected utxo: " + utxo);
                    }
                }

                if (stonewall1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, destination, feePerB))) {
                    break;
                }
            }
            if (stonewall1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, destination, feePerB))) {
                break;
            }
        }
        long fee = estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, destination, feePerB);
        if (log.isDebugEnabled()) {
            log.debug("fee:" + fee);
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalSelectedAmount+", requiredAmount="+stonewall1.computeRequiredAmount(fee));
        }
        if (!stonewall1.isContributedAmountSufficient(totalSelectedAmount, fee)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }

        if (fee % 2L != 0) {
            fee++;
        }
        if (log.isDebugEnabled()) {
            log.debug("fee pair:" + fee);
        }
        stonewall1.setFeeAmount(fee);

        if (log.isDebugEnabled()) {
            log.debug("destination:" + stonewall1.getDestination());
        }

        if (transaction.getOutputs() == null || transaction.getOutputs().size() != 2) {
            log.error("outputs: " + transaction.getOutputs().size());
            log.error("tx:" + transaction.toString());
            throw new Exception("Cannot compose #Cahoots: invalid tx outputs count");
        }

        // find counterparty change output index
        TransactionOutput counterpartyChangeOutput = null;
        for (TransactionOutput transactionOutput : transaction.getOutputs()) {
            String toAddress = getBipFormatSupplier().getToAddress(transactionOutput);
            if(toAddress.equalsIgnoreCase(stonewall1.getCollabChange())) {
                counterpartyChangeOutput = transactionOutput;
                break;
            }
        }
        if (counterpartyChangeOutput == null) {
            throw new Exception("Cannot compose #Cahoots: invalid tx outputs");
        }

        // counterparty pays half of fees
        Coin _value = Coin.valueOf(counterpartyChangeOutput.getValue().longValue() - (fee / 2L));
        if (log.isDebugEnabled()) {
            log.debug("output value post fee:" + _value);
        }
        counterpartyChangeOutput.setValue(_value);
        stonewall1.getPSBT().setTransaction(transaction);

        //
        //
        // step2: B verif, utxos -> A (take smallest that cover amount)
        //
        //

        List<TransactionInput> inputsB = new LinkedList<>();
        for (CahootsUtxo utxo : selectedUTXO) {
            TransactionInput input = utxo.getOutpoint().computeSpendInput();
            inputsB.add(input);
            cahootsContext.addInput(utxo);
        }

        // spender change output
        List<TransactionOutput> outputsB = new LinkedList<>();
        BipAddress changeAddress = cahootsWallet.fetchAddressChange(stonewall1.getAccount(), true, BIP_FORMAT.SEGWIT_NATIVE);
        if (log.isDebugEnabled()) {
            log.debug("+output (Spender change) = " + changeAddress);
        }
        TransactionOutput output_B0 = computeTxOutput(changeAddress, (totalSelectedAmount - stonewall1.getSpendAmount()) - (fee / 2L), cahootsContext);
        outputsB.add(output_B0);

        // destination output
        TransactionOutput destOutput = getBipFormatSupplier().getTransactionOutput(stonewall1.getDestination(), stonewall1.getSpendAmount(), params);
        transaction.addOutput(destOutput);

        STONEWALLx2 stonewall2 = stonewall1.copy();
        stonewall2.doStep2(inputsB, outputsB);

        debug("END doSTONEWALLx2_2",stonewall2, cahootsContext);
        return stonewall2;
    }

    private long estimatedFee(int nbTotalSelectedOutPoints, int nbIncomingInputs, String destination, long feePerB) {
        int outputsNonP2TR = 4;
        int outputsP2TR = 0;
        if (FormatsUtilGeneric.getInstance().isValidP2TR(destination)) {
            // destination is P2TR, contributor mix output is SEGWIT_NATIVE (no like-typed output for P2TR)
            outputsNonP2TR--;
            outputsP2TR++;
        }
        return FeeUtil.getInstance().estimatedFeeSegwit(0, 0, nbTotalSelectedOutPoints + nbIncomingInputs, outputsNonP2TR, outputsP2TR, 0, feePerB);
    }

    @Override
    protected long computeMaxSpendAmount(long minerFee, Stonewallx2Context cahootsContext) throws Exception {
        // shares minerFee
        long maxSpendAmount;
        long sharedMinerFee = minerFee / 2;
        String prefix = "["+cahootsContext.getCahootsType()+"/"+cahootsContext.getTypeUser()+"] ";
        switch (cahootsContext.getTypeUser()) {
            case SENDER:
                // spends amount + sharedMinerFee
                maxSpendAmount = cahootsContext.getAmount()+sharedMinerFee;
                if (log.isDebugEnabled()) {
                    log.debug(prefix+"maxSpendAmount = "+maxSpendAmount+": amount="+cahootsContext.getAmount()+" + sharedMinerFee="+sharedMinerFee);
                }
                break;
            case COUNTERPARTY:
                // shares minerFee
                maxSpendAmount = sharedMinerFee;
                if (log.isDebugEnabled()) {
                    log.debug(prefix+"maxSpendAmount = "+maxSpendAmount+": sharedMinerFee="+sharedMinerFee);
                }
                break;
            default:
                throw new Exception("Unknown typeUser");
        }
        return maxSpendAmount;
    }

    public static final Coin THRESHOLD = Coin.valueOf(200000000);
}
