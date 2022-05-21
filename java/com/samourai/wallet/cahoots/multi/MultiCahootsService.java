package com.samourai.wallet.cahoots.multi;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.cahoots.*;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.wallet.util.RandomUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MultiCahootsService extends AbstractCahootsService<MultiCahoots> {
    private ArrayList<String> activeUtxos = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(MultiCahootsService.class);

    public MultiCahootsService(BipFormatSupplier bipFormatSupplier, NetworkParameters params) {
        super(bipFormatSupplier, params);
    }

    public MultiCahoots startInitiator(CahootsWallet cahootsWallet, String address, long amount, int account) throws Exception {
        if (amount <= 0) {
            throw new Exception("Invalid amount");
        }
        byte[] fingerprint = cahootsWallet.getBip84Wallet().getFingerprint();
        MultiCahoots multiCahoots0 = doMultiCahoots0_Stowaway0(address, amount, account, fingerprint);
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway INITIATOR => step="+multiCahoots0.getStep());
        }
        return multiCahoots0;
    }

    @Override
    public MultiCahoots startCollaborator(CahootsWallet cahootsWallet, int account, MultiCahoots multiCahoots0) throws Exception {
        MultiCahoots multiCahoots1 = doMultiCahoots1_Stowaway1(multiCahoots0, cahootsWallet, account);
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway COUNTERPARTY => step="+multiCahoots1.getStep());
        }
        return multiCahoots1;
    }

    @Override
    public MultiCahoots reply(CahootsWallet cahootsWallet, MultiCahoots multiCahoots) throws Exception {
        int step = multiCahoots.getStep();
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway <= step="+step);
        }
        MultiCahoots payload;
        switch (step) {
            case 1:
                // sender
                payload = doMultiCahoots2_Stowaway2(multiCahoots, cahootsWallet);
                break;
            case 2:
                // counterparty
                payload = doMultiCahoots3_Stowaway3(multiCahoots, cahootsWallet);
                break;
            case 3:
                // sender
                payload = doMultiCahoots4_Stowaway4(multiCahoots, cahootsWallet);
                payload = doMultiCahoots4_Stonewallx20_StartInitiator(payload);
                break;
            case 4:
                // counterparty
                payload = doMultiCahoots5_Stonewallx21_StartCollaborator(multiCahoots, cahootsWallet, multiCahoots.getAccount());
                break;
            case 5:
                // sender
                payload = doMultiCahoots6_Stonewallx22(multiCahoots, cahootsWallet);
                break;
            case 6:
                // counterparty
                payload = doMultiCahoots7_Stonewallx23(multiCahoots, cahootsWallet);
                break;
            case 7:
                // sender
                payload = doMultiCahoots8_Stonewallx24(multiCahoots, cahootsWallet);
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
    private MultiCahoots doMultiCahoots0_Stowaway0(String address, long spendAmount, int account, byte[] fingerprint) {
        //
        //
        // step0: B sends spend amount to A,  creates step0
        //
        //
        if (log.isDebugEnabled()) {
            log.debug("sender account (0):" + account);
        }
        MultiCahoots multiCahoots0 = new MultiCahoots(address, spendAmount, params, account);
        multiCahoots0 = multiCahoots0.doStep0_Stowaway_StartInitiator(address, spendAmount, account, fingerprint);
        return multiCahoots0;
    }

    //
    // counterparty
    //
    private MultiCahoots doMultiCahoots1_Stowaway1(MultiCahoots multiCahoots0, CahootsWallet cahootsWallet, int account) throws Exception {
        byte[] fingerprint = cahootsWallet.getBip84Wallet().getFingerprint();
        multiCahoots0.setFingerprintCollab(fingerprint);

        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(account);
        // No need to filter out UTXOs that have been used in a previous part of the Cahoots, as this is the first time we select UTXOs.
        // sort in descending order by value
        utxos.sort(new UTXO.UTXOComparator());
        if (log.isDebugEnabled()) {
            log.debug("BIP84 utxos:" + utxos.size());
        }

        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        long totalContributedAmount = 0L;
        List<CahootsUtxo> highUTXO = new ArrayList<CahootsUtxo>();
        for (CahootsUtxo utxo : utxos) {
            if (utxo.getValue() > multiCahoots0.getSpendAmount() + SamouraiWalletConst.bDust.longValue()) {
                highUTXO.add(utxo);
            }
        }
        if(highUTXO.size() > 0)    {
            SecureRandom random = RandomUtil.getSecureRandom();
            CahootsUtxo utxo = highUTXO.get(random.nextInt(highUTXO.size()));
            if (log.isDebugEnabled()) {
                log.debug("BIP84 selected random utxo:" + utxo.getValue());
            }

            String outpoint = utxo.getOutpoint().toString();
            if(!activeUtxos.contains(outpoint)) {
                activeUtxos.add(outpoint);
                selectedUTXO.add(utxo);
                totalContributedAmount = utxo.getValue();
            }
        }
        if (selectedUTXO.size() == 0) {
            for (CahootsUtxo utxo : utxos) {
                String outpoint = utxo.getOutpoint().toString();
                if(!activeUtxos.contains(outpoint)) {
                    activeUtxos.add(outpoint);
                    selectedUTXO.add(utxo);
                    totalContributedAmount += utxo.getValue();
                    if (log.isDebugEnabled()) {
                        log.debug("BIP84 selected utxo:" + utxo.getValue());
                    }
                }
                if (multiCahoots0.isContributedAmountSufficient(totalContributedAmount)) {
                    break;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalContributedAmount+", requiredAmount="+multiCahoots0.computeRequiredAmount());
        }
        if (!multiCahoots0.isContributedAmountSufficient(totalContributedAmount)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }

        //
        //
        // step1: A utxos -> B (take largest that cover amount)
        //
        //

        HashMap<MyTransactionOutPoint, Triple<byte[], byte[], String>> inputsA = new HashMap<MyTransactionOutPoint, Triple<byte[], byte[], String>>();

        for (CahootsUtxo utxo : selectedUTXO) {
            MyTransactionOutPoint _outpoint = utxo.getOutpoint();
            ECKey eckey = utxo.getKey();
            String path = utxo.getPath();
            inputsA.put(_outpoint, Triple.of(eckey.getPubKey(), multiCahoots0.getFingerprintCollab(), path));
        }

        // destination output
        BipAddress receiveAddress = cahootsWallet.fetchAddressReceive(account, true);
        if (log.isDebugEnabled()) {
            log.debug("+output (CounterParty receive) = "+receiveAddress);
        }
        HashMap<_TransactionOutput, Triple<byte[], byte[], String>> outputsA = new HashMap<_TransactionOutput, Triple<byte[], byte[], String>>();
        _TransactionOutput output_A0 = computeTxOutput(receiveAddress, multiCahoots0.getSpendAmount());
        outputsA.put(output_A0, computeOutput(receiveAddress, multiCahoots0.getFingerprintCollab()));

        multiCahoots0.setDestination(receiveAddress.getAddressString());
        multiCahoots0.setCounterpartyAccount(account);

        MultiCahoots multiCahoots1 = new MultiCahoots(multiCahoots0);
        multiCahoots1.doStep1_Stowaway_StartCollaborator(inputsA, outputsA);

        return multiCahoots1;
    }

    //
    // sender
    //
    private MultiCahoots doMultiCahoots2_Stowaway2(MultiCahoots multiCahoots1, CahootsWallet cahootsWallet) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("sender account (2):" + multiCahoots1.getAccount());
        }

        Transaction transaction = multiCahoots1.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("step2 tx:" + Hex.toHexString(transaction.bitcoinSerialize()));
        }
        int nbIncomingInputs = transaction.getInputs().size();

        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(multiCahoots1.getAccount());
        // This is the first time we fetch UTXOs on initiator side. No need to filter yet.
        // sort in ascending order by value
        utxos.sort(new UTXO.UTXOComparator());
        Collections.reverse(utxos);

        if (log.isDebugEnabled()) {
            log.debug("BIP84 utxos:" + utxos.size());
        }

        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        int nbTotalSelectedOutPoints = 0;
        long totalSelectedAmount = 0L;
        List<CahootsUtxo> lowUTXO = new ArrayList<CahootsUtxo>();
        for (CahootsUtxo utxo : utxos) {
            if(utxo.getValue() < multiCahoots1.getSpendAmount())    {
                lowUTXO.add(utxo);
            }
        }

        long feePerB = cahootsWallet.fetchFeePerB();

        List<List<CahootsUtxo>> listOfLists = new ArrayList<List<CahootsUtxo>>();
        Collections.shuffle(lowUTXO);
        listOfLists.add(lowUTXO);
        listOfLists.add(utxos);
        int OUTPUTS_STOWAWAY = 2;
        for(List<CahootsUtxo> list : listOfLists)   {

            selectedUTXO.clear();
            totalSelectedAmount = 0L;
            nbTotalSelectedOutPoints = 0;

            for (CahootsUtxo utxo : list) {
                selectedUTXO.add(utxo);
                totalSelectedAmount += utxo.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("BIP84 selected utxo:" + utxo.getValue());
                }
                nbTotalSelectedOutPoints ++;
                if (multiCahoots1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB, OUTPUTS_STOWAWAY))) {

                    // discard "extra" utxo, if any
                    List<CahootsUtxo> _selectedUTXO = new ArrayList<CahootsUtxo>();
                    Collections.reverse(selectedUTXO);
                    int _nbTotalSelectedOutPoints = 0;
                    long _totalSelectedAmount = 0L;
                    for (CahootsUtxo utxoSel : selectedUTXO) {
                        _selectedUTXO.add(utxoSel);
                        _totalSelectedAmount += utxoSel.getValue();
                        if (log.isDebugEnabled()) {
                            log.debug("BIP84 post selected utxo:" + utxoSel.getValue());
                        }
                        _nbTotalSelectedOutPoints ++;
                        if (multiCahoots1.isContributedAmountSufficient(_totalSelectedAmount, estimatedFee(_nbTotalSelectedOutPoints, nbIncomingInputs, feePerB, OUTPUTS_STOWAWAY))) {
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
            if (multiCahoots1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB, OUTPUTS_STOWAWAY))) {
                break;
            }
        }

        long estimatedFee = estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB, OUTPUTS_STOWAWAY);
        if (log.isDebugEnabled()) {
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalSelectedAmount+", requiredAmount="+multiCahoots1.computeRequiredAmount(estimatedFee));
        }
        if (!multiCahoots1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }

        long fee = estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB, OUTPUTS_STOWAWAY);
        if (log.isDebugEnabled()) {
            log.debug("fee:" + fee);
        }

        NetworkParameters params = multiCahoots1.getParams();

        //
        //
        // step2: B verif, utxos -> A (take smallest that cover amount)
        //
        //

        HashMap<MyTransactionOutPoint, Triple<byte[], byte[], String>> inputsB = new HashMap<MyTransactionOutPoint, Triple<byte[], byte[], String>>();

        for (CahootsUtxo utxo : selectedUTXO) {
            MyTransactionOutPoint _outpoint = utxo.getOutpoint();
            ECKey eckey = utxo.getKey();
            String path = utxo.getPath();
            inputsB.put(_outpoint, Triple.of(eckey.getPubKey(), multiCahoots1.getFingerprint(), path));
        }

        if (log.isDebugEnabled()) {
            log.debug("inputsB:" + inputsB.size());
        }

        // change output
        BipAddress changeAddress = cahootsWallet.fetchAddressChange(multiCahoots1.getAccount(), true);
        if (log.isDebugEnabled()) {
            log.debug("+output (sender change) = "+changeAddress);
        }
        HashMap<_TransactionOutput, Triple<byte[], byte[], String>> outputsB = new HashMap<_TransactionOutput, Triple<byte[], byte[], String>>();
        _TransactionOutput output_B0 = computeTxOutput(changeAddress, (totalSelectedAmount - multiCahoots1.getSpendAmount()) - fee);
        outputsB.put(output_B0, computeOutput(changeAddress, multiCahoots1.getFingerprint()));

        if (log.isDebugEnabled()) {
            log.debug("outputsB:" + outputsB.size());
        }

        MultiCahoots multiCahoots2 = new MultiCahoots(multiCahoots1);
        multiCahoots2.doStep2_Stowaway(inputsB, outputsB);
        multiCahoots2.setFeeAmount(fee);

        return multiCahoots2;
    }

    //
    // counterparty
    //
    private MultiCahoots doMultiCahoots3_Stowaway3(MultiCahoots multiCahoots2, CahootsWallet cahootsWallet) throws Exception {
        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(multiCahoots2.getCounterpartyAccount());
        HashMap<String, ECKey> keyBag_A = computeKeyBag(multiCahoots2, utxos);

        MultiCahoots multiCahoots3 = new MultiCahoots(multiCahoots2);
        multiCahoots3.doStep3_Stowaway(keyBag_A);

        // compute verifiedSpendAmount
        long verifiedSpendAmount = computeSpendAmount(keyBag_A, cahootsWallet, multiCahoots3, CahootsTypeUser.COUNTERPARTY);
        multiCahoots3.setVerifiedSpendAmount(verifiedSpendAmount);
        return multiCahoots3;
    }

    //
    // sender
    //
    private MultiCahoots doMultiCahoots4_Stowaway4(MultiCahoots multiCahoots3, CahootsWallet cahootsWallet) throws Exception {
        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(multiCahoots3.getAccount());
        HashMap<String, ECKey> keyBag_B = computeKeyBag(multiCahoots3, utxos);

        MultiCahoots multiCahoots4 = new MultiCahoots(multiCahoots3);
        multiCahoots4.doStep4_Stowaway(keyBag_B);

        // compute verifiedSpendAmount
        long verifiedSpendAmount = computeSpendAmount(keyBag_B, cahootsWallet, multiCahoots4, CahootsTypeUser.SENDER);
        multiCahoots4.setVerifiedSpendAmount(verifiedSpendAmount);
        return multiCahoots4;
    }

    //
    // sender
    //
    public MultiCahoots doMultiCahoots4_Stonewallx20_StartInitiator(MultiCahoots multiCahoots4) throws Exception {
        if (multiCahoots4.getStonewallAmount() <= 0) {
            throw new Exception("Invalid amount");
        }
        if (StringUtils.isEmpty(multiCahoots4.getStonewallDestination())) {
            throw new Exception("Invalid address");
        }

        MultiCahoots multiCahoots4_0 = new MultiCahoots(multiCahoots4.getStonewallDestination(), multiCahoots4.getStonewallAmount(), multiCahoots4.getParams(), multiCahoots4.getAccount());
        multiCahoots4_0.setStep(4);
        // Testing this out, might need to "fake" the initiation so the fingerprints don't change from prior step.
        multiCahoots4_0.setFingerprint(multiCahoots4.getFingerprint());
        multiCahoots4_0.setFingerprintCollab(multiCahoots4.getFingerprintCollab());
        multiCahoots4_0.setCounterpartyAccount(multiCahoots4.getCounterpartyAccount());
        multiCahoots4_0.setDestination(multiCahoots4.getStonewallDestination());

        multiCahoots4_0.setStowawayTransaction(multiCahoots4.getStowawayTransaction());
        if (log.isDebugEnabled()) {
            log.debug("# STONEWALLx2 INITIATOR => step="+multiCahoots4_0.getStep());
        }
        return multiCahoots4_0;
    }

    //
    // counterparty
    //
    private MultiCahoots doMultiCahoots5_Stonewallx21_StartCollaborator(MultiCahoots multiCahoots4, CahootsWallet cahootsWallet, int account) throws Exception {

        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(multiCahoots4.getCounterpartyAccount());

        if (log.isDebugEnabled()) {
            log.debug("BIP84 utxos:" + utxos.size());
        }

        List<String> seenTxs = new ArrayList<String>();
        for(TransactionInput input : multiCahoots4.getStowawayTransaction().getInputs()) {
            seenTxs.add(input.getOutpoint().getHash().toString());
        }

        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        long totalContributedAmount = 0L;
        for (int step = 0; step < 3; step++) {

            if (multiCahoots4.getCounterpartyAccount() == 0) {
                step = 2;
            }

            for(CahootsUtxo utxo : selectedUTXO) {
                activeUtxos.remove(utxo.getOutpoint().toString());
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
                    String outpointString = outpoint.toString();
                    if(!activeUtxos.contains(outpointString)) {
                        activeUtxos.add(outpointString);
                        selectedUTXO.add(utxo);
                        totalContributedAmount += utxo.getValue();
                        if (log.isDebugEnabled()) {
                            log.debug("BIP84 selected utxo:" + utxo.getValue());
                        }
                    }
                }

                if (multiCahoots4.isContributedAmountSufficient(totalContributedAmount)) {
                    break;
                }
            }
            if (multiCahoots4.isContributedAmountSufficient(totalContributedAmount)) {
                break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalContributedAmount+", requiredAmount="+multiCahoots4.computeRequiredAmount());
        }
        if (!multiCahoots4.isContributedAmountSufficient(totalContributedAmount)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }

        //
        //
        // step1: A utxos -> B (take largest that cover amount)
        //
        //

        HashMap<MyTransactionOutPoint, Triple<byte[], byte[], String>> inputsA = new HashMap<MyTransactionOutPoint, Triple<byte[], byte[], String>>();

        for (CahootsUtxo utxo : selectedUTXO) {
            MyTransactionOutPoint _outpoint = utxo.getOutpoint();
            ECKey eckey = utxo.getKey();
            String path = utxo.getPath();
            inputsA.put(_outpoint, Triple.of(eckey.getPubKey(), multiCahoots4.getFingerprintCollab(), path));
        }

        HashMap<_TransactionOutput, Triple<byte[], byte[], String>> outputsA = new HashMap<_TransactionOutput, Triple<byte[], byte[], String>>();
        // contributor mix output
        BipAddress receiveAddress = cahootsWallet.fetchAddressReceive(multiCahoots4.getCounterpartyAccount(), true);
        if (log.isDebugEnabled()) {
            log.debug("+output (CounterParty mix) = "+receiveAddress);
        }
        _TransactionOutput output_A0 = computeTxOutput(receiveAddress, multiCahoots4.getSpendAmount());
        outputsA.put(output_A0, computeOutput(receiveAddress, multiCahoots4.getFingerprintCollab()));
        // sender change output
        BipAddress changeAddress = cahootsWallet.fetchAddressChange(multiCahoots4.getCounterpartyAccount(), true);
        if (log.isDebugEnabled()) {
            log.debug("+output (CounterParty change) = " + changeAddress);
        }
        _TransactionOutput output_A1 = computeTxOutput(changeAddress, totalContributedAmount - multiCahoots4.getSpendAmount());
        outputsA.put(output_A1, computeOutput(changeAddress, multiCahoots4.getFingerprintCollab()));
        multiCahoots4.setCollabChange(changeAddress.getAddressString());

        MultiCahoots multiCahoots5 = new MultiCahoots(multiCahoots4);
        multiCahoots5.doStep5_Stonewallx2_StartCollaborator(inputsA, outputsA);

        return multiCahoots5;
    }

    //
    // sender
    //
    private MultiCahoots doMultiCahoots6_Stonewallx22(MultiCahoots multiCahoots5, CahootsWallet cahootsWallet) throws Exception {

        Transaction transaction = multiCahoots5.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("step2 tx:" + Hex.toHexString(transaction.bitcoinSerialize()));
            log.debug("step2 tx:" + transaction);
        }
        int nbIncomingInputs = transaction.getInputs().size();

        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(multiCahoots5.getAccount());

        if (log.isDebugEnabled()) {
            log.debug("BIP84 utxos:" + utxos.size());
        }

        List<String> seenTxs = new ArrayList<String>();
        for (TransactionInput input : transaction.getInputs()) {
            if (!seenTxs.contains(input.getOutpoint().getHash().toString())) {
                seenTxs.add(input.getOutpoint().getHash().toString());
            }
        }

        for(TransactionInput input : multiCahoots5.getStowawayTransaction().getInputs()) {
            seenTxs.add(input.getOutpoint().getHash().toString());
        }

        long feePerB = cahootsWallet.fetchFeePerB();

        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        long totalSelectedAmount = 0L;
        int nbTotalSelectedOutPoints = 0;
        int OUTPUTS_STONEWALL = 4;
        for (int step = 0; step < 3; step++) {

            if (multiCahoots5.getCounterpartyAccount() == 0) {
                step = 2;
            }

            List<String> _seenTxs = seenTxs;
            selectedUTXO = new ArrayList<CahootsUtxo>();
            totalSelectedAmount = 0L;
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
                        log.debug("BIP84 selected utxo:" + utxo.getValue());
                    }
                }

                if (multiCahoots5.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB, OUTPUTS_STONEWALL))) {
                    break;
                }
            }
            if (multiCahoots5.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB, OUTPUTS_STONEWALL))) {
                break;
            }
        }
        long estimatedFee = estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB, OUTPUTS_STONEWALL);
        if (log.isDebugEnabled()) {
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalSelectedAmount+", requiredAmount="+multiCahoots5.computeRequiredAmount(estimatedFee));
        }
        if (!multiCahoots5.isContributedAmountSufficient(totalSelectedAmount, estimatedFee)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }

        long fee = estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB, OUTPUTS_STONEWALL);
        if (log.isDebugEnabled()) {
            log.debug("fee:" + fee);
        }
        if (fee % 2L != 0) {
            fee++;
        }
        if (log.isDebugEnabled()) {
            log.debug("fee pair:" + fee);
        }
        multiCahoots5.setFeeAmount(fee);

        if (log.isDebugEnabled()) {
            log.debug("destination:" + multiCahoots5.getDestination());
        }

        if (transaction.getOutputs() != null && transaction.getOutputs().size() == 2) {

            int idx = -1;
            for (int i = 0; i < 2; i++) {
                byte[] buf = transaction.getOutputs().get(i).getScriptBytes();
                byte[] script = new byte[buf.length];
                script[0] = 0x00;
                System.arraycopy(buf, 1, script, 1, script.length - 1);
                if (log.isDebugEnabled()) {
                    log.debug("script:" + new Script(script).toString());
                    log.debug("script hex:" + Hex.toHexString(script));
                    log.debug("address from script:" + getBipFormatSupplier().getToAddress(script, params));
                }
                if(getBipFormatSupplier().getToAddress(script, params).equalsIgnoreCase(multiCahoots5.getCollabChange())) {
                    idx = i;
                    break;
                }
            }

            if(idx == 0 || idx == 1) {
                Coin value = transaction.getOutputs().get(idx).getValue();
                Coin _value = Coin.valueOf(value.longValue() - (fee / 2L));
                if (log.isDebugEnabled()) {
                    log.debug("output value post fee:" + _value);
                }
                transaction.getOutputs().get(idx).setValue(_value);
                multiCahoots5.getPSBT().setTransaction(transaction);
            }
            else {
                throw new Exception("Cannot compose #Cahoots: invalid tx outputs");
            }

        }
        else {
            log.error("outputs: "+transaction.getOutputs().size());
            log.error("tx:"+transaction.toString());
            throw new Exception("Cannot compose #Cahoots: invalid tx outputs count");
        }

        //
        //
        // step2: B verif, utxos -> A (take smallest that cover amount)
        //
        //

        String zpub = cahootsWallet.getBip84Wallet().getAccount(multiCahoots5.getAccount()).zpubstr();
        HashMap<MyTransactionOutPoint, Triple<byte[], byte[], String>> inputsB = new HashMap<MyTransactionOutPoint, Triple<byte[], byte[], String>>();

        for (CahootsUtxo utxo : selectedUTXO) {
            MyTransactionOutPoint _outpoint = utxo.getOutpoint();
            ECKey eckey = utxo.getKey();
            String path = utxo.getPath();
            inputsB.put(_outpoint, Triple.of(eckey.getPubKey(), FormatsUtilGeneric.getInstance().getFingerprintFromXPUB(zpub), path));
        }

        // spender change output
        HashMap<_TransactionOutput, Triple<byte[], byte[], String>> outputsB = new HashMap<_TransactionOutput, Triple<byte[], byte[], String>>();

        BipAddress changeAddress = cahootsWallet.fetchAddressChange(multiCahoots5.getAccount(), true);
        if (log.isDebugEnabled()) {
            log.debug("+output (Spender change) = " + changeAddress);
        }

        long amount = (totalSelectedAmount - multiCahoots5.getSpendAmount()) - (fee / 2L);
        _TransactionOutput output_B0 = computeTxOutput(changeAddress, amount);
        outputsB.put(output_B0, computeOutput(changeAddress, multiCahoots5.getFingerprint()));

        MultiCahoots multiCahoots6 = new MultiCahoots(multiCahoots5);
        multiCahoots6.doStep6_Stonewallx2(inputsB, outputsB);

        return multiCahoots6;
    }

    //
    // counterparty
    //
    private MultiCahoots doMultiCahoots7_Stonewallx23(MultiCahoots multiCahoots6, CahootsWallet cahootsWallet) throws Exception {
        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(multiCahoots6.getCounterpartyAccount());
        HashMap<String, ECKey> keyBag_A = computeKeyBag(multiCahoots6, utxos);

        MultiCahoots multiCahoots7 = new MultiCahoots(multiCahoots6);
        boolean noExtraFee = checkForNoExtraFee(multiCahoots7, utxos);
        if(noExtraFee) {
            multiCahoots7.doStep7_Stonewallx2(keyBag_A);
        } else {
            throw new Exception("Cannot compose #Cahoots: extra fee is being taken from us");
        }

        // compute verifiedSpendAmount
        long verifiedSpendAmount = computeSpendAmount(keyBag_A, cahootsWallet, multiCahoots7, CahootsTypeUser.COUNTERPARTY);
        multiCahoots7.setVerifiedSpendAmount(verifiedSpendAmount);
        return multiCahoots7;
    }

    private boolean checkForNoExtraFee(MultiCahoots multiCahoots, List<CahootsUtxo> utxos) {
        long inputSum = 0;
        long outputSum = 0;

        for(int i = 0; i < multiCahoots.getTransaction().getInputs().size(); i++) {
            TransactionInput input = multiCahoots.getTransaction().getInput(i);
            for(CahootsUtxo cahootsUtxo : utxos) {
                int outpointIndex = cahootsUtxo.getOutpoint().getTxOutputN();
                Sha256Hash outpointHash = cahootsUtxo.getOutpoint().getTxHash();
                if(input != null && input.getOutpoint().getHash().equals(outpointHash) && input.getOutpoint().getIndex() == outpointIndex) {
                    long amount = cahootsUtxo.getValue();
                    inputSum += amount;
                    break;
                }
            }
        }
        for(int i = 0; i < multiCahoots.getTransaction().getOutputs().size(); i++) {
            TransactionOutput utxo = multiCahoots.getTransaction().getOutput(i);
            long amount = utxo.getValue().value;
            String address = null;
            try {
                address = getBipFormatSupplier().getToAddress(utxo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(address != null && address.equals(multiCahoots.getCollabChange())) {
                outputSum += amount;
            } else if(address != null && amount == multiCahoots.getSpendAmount() && !address.equals(multiCahoots.getDestination())) {
                outputSum += amount;
            }
        }

        return (inputSum - outputSum) == (multiCahoots.getFeeAmount()/2L) && inputSum != 0 && outputSum != 0;
    }

    //
    // sender
    //
    private MultiCahoots doMultiCahoots8_Stonewallx24(MultiCahoots multiCahoots7, CahootsWallet cahootsWallet) throws Exception {
        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(multiCahoots7.getAccount());
        HashMap<String, ECKey> keyBag_B = computeKeyBag(multiCahoots7, utxos);

        MultiCahoots multiCahoots8 = new MultiCahoots(multiCahoots7);
        multiCahoots8.doStep8_Stonewallx2(keyBag_B);

        // compute verifiedSpendAmount
        long verifiedSpendAmount = computeSpendAmount(keyBag_B, cahootsWallet, multiCahoots8, CahootsTypeUser.SENDER);
        multiCahoots8.setVerifiedSpendAmount(verifiedSpendAmount);
        return multiCahoots8;
    }

    private long estimatedFee(int nbTotalSelectedOutPoints, int nbIncomingInputs, long feePerB, int outputsNonOpReturn) {
        return FeeUtil.getInstance().estimatedFeeSegwit(0, 0, nbTotalSelectedOutPoints + nbIncomingInputs, outputsNonOpReturn, 0, feePerB);
    }
}
