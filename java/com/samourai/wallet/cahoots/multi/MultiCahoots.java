package com.samourai.wallet.cahoots.multi;

import com.samourai.wallet.bip69.BIP69InputComparator;
import com.samourai.wallet.bip69.BIP69OutputComparator;
import com.samourai.wallet.cahoots.Cahoots;
import com.samourai.wallet.cahoots.CahootsType;
import com.samourai.wallet.cahoots._TransactionOutput;
import com.samourai.wallet.cahoots.psbt.PSBT;
import com.samourai.wallet.cahoots.psbt.PSBTEntry;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.segwit.bech32.Bech32Segwit;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.wallet.util.JavaUtil;
import com.samourai.wallet.util.RandomUtil;
import com.samourai.wallet.util.Z85;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MultiCahoots extends Cahoots {
    private static final Logger log = LoggerFactory.getLogger(MultiCahoots.class);
    private long stowawayFee = 0;
    private long stonewallAmount = 0;
    private String stonewallDestination = "";
    private Transaction stowawayTransaction = null;
    private Transaction stonewallTransaction = null;

    private MultiCahoots()    { ; }

    public MultiCahoots(MultiCahoots multiCahoots)    {
        super(multiCahoots);
        this.stonewallAmount = multiCahoots.stonewallAmount;
        this.stowawayFee = multiCahoots.stowawayFee;
        this.stonewallDestination = multiCahoots.stonewallDestination;
        this.stowawayTransaction = multiCahoots.stowawayTransaction;
        this.stonewallTransaction = multiCahoots.stonewallTransaction;
    }

    public MultiCahoots(JSONObject obj)    {
        this.fromJSON(obj);
    }

    // Stowaway
    public MultiCahoots(String address, long spendAmount, NetworkParameters params, int account)    {
        this.ts = System.currentTimeMillis() / 1000L;
        SecureRandom random = RandomUtil.getSecureRandom();
        this.strID = Hex.toHexString(Sha256Hash.hash(BigInteger.valueOf(random.nextLong()).toByteArray()));
        this.type = CahootsType.MULTI.getValue();
        this.step = 0;
        this.spendAmount = spendAmount;
        this.stonewallDestination = address;
        this.outpoints = new HashMap<String, Long>();
        this.params = params;
        this.account = account;
    }

    // Stowaway
    public MultiCahoots(long spendAmount, NetworkParameters params, String strPayNymInit, String strPayNymCollab, int account)    {
        this.ts = System.currentTimeMillis() / 1000L;
        SecureRandom random = RandomUtil.getSecureRandom();
        this.strID = Hex.toHexString(Sha256Hash.hash(BigInteger.valueOf(random.nextLong()).toByteArray()));
        this.type = CahootsType.MULTI.getValue();
        this.step = 0;
        this.spendAmount = spendAmount;
        this.outpoints = new HashMap<String, Long>();
        this.strPayNymInit = strPayNymInit;
        this.strPayNymCollab = strPayNymCollab;
        this.params = params;
        this.account = account;
    }

    public MultiCahoots doStep0_Stowaway_StartInitiator(String destination, long spendAmount, int account, byte[] fingerprint) {
        //
        //
        // step0: B sends spend amount to A,  creates step0
        //
        //
        if (log.isDebugEnabled()) {
            log.debug("sender account (0):" + account);
        }
        long stowawayFee = (long)(spendAmount * 0.01d);
        MultiCahoots stowaway0 = new MultiCahoots(destination, stowawayFee, params, account);
        stowaway0.setFingerprint(fingerprint);
        stowaway0.setStonewallAmount(spendAmount);
        stowaway0.setStowawayFee(stowawayFee);
        return stowaway0;
    }

    //
    // receiver
    //
    public void doStep1_Stowaway_StartCollaborator(HashMap<MyTransactionOutPoint,Triple<byte[],byte[],String>> inputs, HashMap<_TransactionOutput,Triple<byte[],byte[],String>> outputs) throws Exception    {

        if(this.getStep() != 0 || this.getSpendAmount() == 0L)   {
            throw new Exception("Invalid step/amount");
        }
        if(outputs == null)    {
            throw new Exception("Invalid outputs");
        }

        Transaction transaction = new Transaction(params);
        transaction.setVersion(2);
        for(MyTransactionOutPoint outpoint : inputs.keySet())   {
            TransactionInput input = outpoint.computeSpendInput();
            input.setSequenceNumber(SEQUENCE_RBF_ENABLED);
            if (log.isDebugEnabled()) {
                log.debug("input value:" + input.getValue().longValue());
            }
            transaction.addInput(input);
            outpoints.put(outpoint.getHash().toString() + "-" + outpoint.getIndex(), outpoint.getValue().longValue());
        }
        for(_TransactionOutput output : outputs.keySet())   {
            transaction.addOutput(output);
        }

        // used by Sparrow
        String strBlockHeight = System.getProperty(BLOCK_HEIGHT_PROPERTY);
        if(strBlockHeight != null) {
            transaction.setLockTime(Long.parseLong(strBlockHeight));
        }

        PSBT psbt = new PSBT(transaction);
        for(MyTransactionOutPoint outpoint : inputs.keySet())   {
            Triple triple = inputs.get(outpoint);
            // input type 1
            SegwitAddress segwitAddress = new SegwitAddress((byte[])triple.getLeft(), params);
            psbt.addInput(PSBT.PSBT_IN_WITNESS_UTXO, null, PSBT.writeSegwitInputUTXO(outpoint.getValue().longValue(), segwitAddress.segWitRedeemScript().getProgram()));
            // input type 6
            String[] s = ((String)triple.getRight()).split("/");
            psbt.addInput(PSBT.PSBT_IN_BIP32_DERIVATION, (byte[])triple.getLeft(), PSBT.writeBIP32Derivation((byte[])triple.getMiddle(), 84, params instanceof TestNet3Params ? 1 : 0, account, Integer.valueOf(s[1]), Integer.valueOf(s[2])));
        }
        for(_TransactionOutput output : outputs.keySet())   {
            Triple triple = outputs.get(output);
            // output type 2
            String[] s = ((String)triple.getRight()).split("/");
            psbt.addOutput(PSBT.PSBT_OUT_BIP32_DERIVATION, (byte[])triple.getLeft(), PSBT.writeBIP32Derivation((byte[])triple.getMiddle(), 84, params instanceof TestNet3Params ? 1 : 0, account, Integer.valueOf(s[1]), Integer.valueOf(s[2])));
        }

        //
        //
        //
//        this.psbt = psbt;
        this.psbt = new PSBT(transaction);

        if (log.isDebugEnabled()) {
            log.debug("input value:" + psbt.getTransaction().getInputs().get(0).getValue().longValue());
        }

        this.setStep(1);
    }

    //
    // sender
    //
    public void doStep2_Stowaway(HashMap<MyTransactionOutPoint,Triple<byte[],byte[],String>> inputs, HashMap<_TransactionOutput,Triple<byte[],byte[],String>> outputs) throws Exception    {

        Transaction transaction = psbt.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("step2 tx:" + transaction.toString());
            log.debug("step2 tx:" + Hex.toHexString(transaction.bitcoinSerialize()));
        }

        // tx: modify spend output
        long contributedAmount = 0L;
        /*
        for(TransactionInput input : transaction.getInputs())   {
//            Log.d("Stowaway", input.getOutpoint().toString());
            contributedAmount += input.getOutpoint().getValue().longValue();
        }
        */
        for(String outpoint : outpoints.keySet())   {
            contributedAmount += outpoints.get(outpoint);
        }
        long outputAmount = transaction.getOutput(0).getValue().longValue();
        TransactionOutput spendOutput = transaction.getOutput(0);
        spendOutput.setValue(Coin.valueOf(outputAmount + contributedAmount));
        transaction.clearOutputs();
        transaction.addOutput(spendOutput);

        for(MyTransactionOutPoint outpoint : inputs.keySet())   {
            if (log.isDebugEnabled()) {
                log.debug("outpoint value:" + outpoint.getValue().longValue());
            }
            TransactionInput input = outpoint.computeSpendInput();
            input.setSequenceNumber(SEQUENCE_RBF_ENABLED);
            transaction.addInput(input);
            outpoints.put(outpoint.getHash().toString() + "-" + outpoint.getIndex(), outpoint.getValue().longValue());
        }
        for(_TransactionOutput output : outputs.keySet())   {
            transaction.addOutput(output);
        }

        // psbt: modify spend output
        List<PSBTEntry> updatedEntries = new ArrayList<PSBTEntry>();
        for(PSBTEntry entry : psbt.getPsbtInputs())   {
            if(entry.getKeyType()[0] == PSBT.PSBT_IN_WITNESS_UTXO)    {
                byte[] data = entry.getData();
                byte[] scriptPubKey = new byte[data.length - JavaUtil.LONG_BYTES];
                System.arraycopy(data, JavaUtil.LONG_BYTES, scriptPubKey, 0, scriptPubKey.length);
                entry.setData(PSBT.writeSegwitInputUTXO(outputAmount + contributedAmount, scriptPubKey));
            }
            updatedEntries.add(entry);
        }
        psbt.setPsbtInputs(updatedEntries);

        for(MyTransactionOutPoint outpoint : inputs.keySet())   {
            Triple triple = inputs.get(outpoint);
            // input type 1
            SegwitAddress segwitAddress = new SegwitAddress((byte[])triple.getLeft(), params);
            psbt.addInput(PSBT.PSBT_IN_WITNESS_UTXO, null, PSBT.writeSegwitInputUTXO(outpoint.getValue().longValue(), segwitAddress.segWitRedeemScript().getProgram()));
            // input type 6
            String[] s = ((String)triple.getRight()).split("/");
            psbt.addInput(PSBT.PSBT_IN_BIP32_DERIVATION, (byte[])triple.getLeft(), PSBT.writeBIP32Derivation((byte[])triple.getMiddle(), 84, params instanceof TestNet3Params ? 1 : 0, account, Integer.valueOf(s[1]), Integer.valueOf(s[2])));
        }
        for(_TransactionOutput output : outputs.keySet())   {
            Triple triple = outputs.get(output);
            // output type 2
            String[] s = ((String)triple.getRight()).split("/");
            psbt.addOutput(PSBT.PSBT_OUT_BIP32_DERIVATION, (byte[])triple.getLeft(), PSBT.writeBIP32Derivation((byte[])triple.getMiddle(), 84, params instanceof TestNet3Params ? 1 : 0, account, Integer.valueOf(s[1]), Integer.valueOf(s[2])));
        }

        //
        //
        //
//        psbt.setTransaction(transaction);
        psbt = new PSBT(transaction);

        this.setStep(2);
    }

    //
    // receiver
    //
    public void doStep3_Stowaway(HashMap<String,ECKey> keyBag)    {

        Transaction transaction = this.getTransaction();
        List<TransactionInput> inputs = new ArrayList<TransactionInput>();
        inputs.addAll(transaction.getInputs());
        Collections.sort(inputs, new BIP69InputComparator());
        transaction.clearInputs();
        for(TransactionInput input : inputs)    {
            transaction.addInput(input);
        }
        List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
        outputs.addAll(transaction.getOutputs());
        Collections.sort(outputs, new BIP69OutputComparator());
        transaction.clearOutputs();
        for(TransactionOutput output : outputs)    {
            transaction.addOutput(output);
        }

        //
        //
        //
//        psbt.setTransaction(transaction);
        psbt = new PSBT(transaction);

        signTx(keyBag);

        this.setStep(3);
    }

    //
    // sender
    //
    public void doStep4_Stowaway(HashMap<String,ECKey> keyBag)    {

        signTx(keyBag);

        this.setStep(4);
    }

    private MultiCahoots doStep5_Stonewallx2_StartInitiator(long spendAmount, String address, int account, byte[] fingerprint) {
        MultiCahoots stonewall0 = new MultiCahoots(address, spendAmount, params, account);
        stonewall0.setFingerprint(fingerprint);
        stonewall0.setStep(5);

        return stonewall0;
    }

    //
    // counterparty
    //
    protected void doStep6_Stonewallx2_StartCollaborator(HashMap<MyTransactionOutPoint,Triple<byte[],byte[],String>> inputs, HashMap<_TransactionOutput,Triple<byte[],byte[],String>> outputs) throws Exception    {

        if(this.getStep() != 5 || this.getSpendAmount() == 0L)   {
            throw new Exception("Invalid step/amount");
        }
        if(outputs == null)    {
            throw new Exception("Invalid outputs");
        }

        Transaction transaction = new Transaction(params);
        transaction.setVersion(2);
        for(MyTransactionOutPoint outpoint : inputs.keySet())   {
            TransactionInput input = outpoint.computeSpendInput();
            input.setSequenceNumber(SEQUENCE_RBF_ENABLED);
            transaction.addInput(input);
            outpoints.put(outpoint.getHash().toString() + "-" + outpoint.getIndex(), outpoint.getValue().longValue());
        }
        for(_TransactionOutput output : outputs.keySet())   {
            transaction.addOutput(output);
        }

        // used by Sparrow
        String strBlockHeight = System.getProperty(BLOCK_HEIGHT_PROPERTY);
        if(strBlockHeight != null) {
            transaction.setLockTime(Long.parseLong(strBlockHeight));
        }

        PSBT psbt = new PSBT(transaction);
        for(MyTransactionOutPoint outpoint : inputs.keySet())   {
            Triple triple = inputs.get(outpoint);
            // input type 1
            SegwitAddress segwitAddress = new SegwitAddress((byte[])triple.getLeft(), params);
            psbt.addInput(PSBT.PSBT_IN_WITNESS_UTXO, null, PSBT.writeSegwitInputUTXO(outpoint.getValue().longValue(), segwitAddress.segWitRedeemScript().getProgram()));
            // input type 6
            String[] s = ((String)triple.getRight()).split("/");
            psbt.addInput(PSBT.PSBT_IN_BIP32_DERIVATION, (byte[])triple.getLeft(), PSBT.writeBIP32Derivation((byte[])triple.getMiddle(), 84, params instanceof TestNet3Params ? 1 : 0, cptyAccount, Integer.valueOf(s[1]), Integer.valueOf(s[2])));
        }
        for(_TransactionOutput output : outputs.keySet())   {
            Triple triple = outputs.get(output);
            // output type 2
            String[] s = ((String)triple.getRight()).split("/");
            psbt.addOutput(PSBT.PSBT_OUT_BIP32_DERIVATION, (byte[])triple.getLeft(), PSBT.writeBIP32Derivation((byte[])triple.getMiddle(), 84, params instanceof TestNet3Params ? 1 : 0, cptyAccount, Integer.valueOf(s[1]), Integer.valueOf(s[2])));
        }

        //
        //
        //
//        this.psbt = psbt;
        this.psbt = new PSBT(transaction);

        this.setStep(6);
    }

    //
    // sender
    //
    protected void doStep7_Stonewallx2(HashMap<MyTransactionOutPoint,Triple<byte[],byte[],String>> inputs, HashMap<_TransactionOutput,Triple<byte[],byte[],String>> outputs) throws Exception    {

        Transaction transaction = psbt.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("step2 tx:" + transaction.toString());
            log.debug("step2 tx:" + Hex.toHexString(transaction.bitcoinSerialize()));
        }

        for(MyTransactionOutPoint outpoint : inputs.keySet())   {
            if (log.isDebugEnabled()) {
                log.debug("outpoint value:" + outpoint.getValue().longValue());
            }
            TransactionInput input = outpoint.computeSpendInput();
            input.setSequenceNumber(SEQUENCE_RBF_ENABLED);
            transaction.addInput(input);
            outpoints.put(outpoint.getHash().toString() + "-" + outpoint.getIndex(), outpoint.getValue().longValue());
        }
        for(_TransactionOutput output : outputs.keySet())   {
            transaction.addOutput(output);
        }

        TransactionOutput _output = null;
        if(!FormatsUtilGeneric.getInstance().isValidBitcoinAddress(stonewallDestination, params)) {
            throw new Exception("Invalid destination address: " + stonewallDestination);
        }
        if(FormatsUtilGeneric.getInstance().isValidBech32(stonewallDestination))    {
            Pair<Byte, byte[]> pair = Bech32Segwit.decode(params instanceof TestNet3Params ? "tb" : "bc", stonewallDestination);
            byte[] scriptPubKey = Bech32Segwit.getScriptPubkey(pair.getLeft(), pair.getRight());
            _output = new TransactionOutput(params, null, Coin.valueOf(spendAmount), scriptPubKey);
        }
        else    {
            Script toOutputScript = ScriptBuilder.createOutputScript(Address.fromBase58(params, stonewallDestination));
            _output = new TransactionOutput(params, null, Coin.valueOf(spendAmount), toOutputScript.getProgram());
        }
        transaction.addOutput(_output);

        for(MyTransactionOutPoint outpoint : inputs.keySet())   {
            Triple triple = inputs.get(outpoint);
            // input type 1
            SegwitAddress segwitAddress = new SegwitAddress((byte[])triple.getLeft(), params);
            psbt.addInput(PSBT.PSBT_IN_WITNESS_UTXO, null, PSBT.writeSegwitInputUTXO(outpoint.getValue().longValue(), segwitAddress.segWitRedeemScript().getProgram()));
            // input type 6
            String[] s = ((String)triple.getRight()).split("/");
            psbt.addInput(PSBT.PSBT_IN_BIP32_DERIVATION, (byte[])triple.getLeft(), PSBT.writeBIP32Derivation((byte[])triple.getMiddle(), 84, params instanceof TestNet3Params ? 1 : 0, account, Integer.valueOf(s[1]), Integer.valueOf(s[2])));
        }
        for(_TransactionOutput output : outputs.keySet())   {
            Triple triple = outputs.get(output);
            // output type 2
            String[] s = ((String)triple.getRight()).split("/");
            psbt.addOutput(PSBT.PSBT_OUT_BIP32_DERIVATION, (byte[])triple.getLeft(), PSBT.writeBIP32Derivation((byte[])triple.getMiddle(), 84, params instanceof TestNet3Params ? 1 : 0, account, Integer.valueOf(s[1]), Integer.valueOf(s[2])));
        }

        //
        //
        //
//        psbt.setTransaction(transaction);
        psbt = new PSBT(transaction);

        this.setStep(7);
    }

    //
    // counterparty
    //
    protected void doStep8_Stonewallx2(HashMap<String,ECKey> keyBag)    {

        Transaction transaction = this.getTransaction();
        List<TransactionInput> inputs = new ArrayList<TransactionInput>();
        inputs.addAll(transaction.getInputs());
        Collections.sort(inputs, new BIP69InputComparator());
        transaction.clearInputs();
        for(TransactionInput input : inputs)    {
            transaction.addInput(input);
        }
        List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
        outputs.addAll(transaction.getOutputs());
        Collections.sort(outputs, new BIP69OutputComparator());
        transaction.clearOutputs();
        for(TransactionOutput output : outputs)    {
            transaction.addOutput(output);
        }

        //
        //
        //
//        psbt.setTransaction(transaction);
        psbt = new PSBT(transaction);

        signTx(keyBag);

        this.setStep(8);
    }

    //
    // sender
    //
    protected void doStep9_Stonewallx2(HashMap<String,ECKey> keyBag)    {

        signTx(keyBag);

        this.setStep(9);
    }

    private void setStonewallAmount(long amount) {
        this.stonewallAmount = amount;
    }

    public long getStonewallAmount() {
        return stonewallAmount;
    }

    private void setStowawayFee(long amount) {
        this.stowawayFee = amount;
    }

    public long getStowawayFee() {
        return stowawayFee;
    }

    public String getStonewallDestination() {
        return stonewallDestination;
    }

    private void setStonewallDestination(String destination) {
        this.stonewallDestination = destination;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();
        jsonObject.put("stonewall_amount", stonewallAmount);
        jsonObject.put("stowaway_fee", stowawayFee);
        jsonObject.put("stonewall_destination", stonewallDestination);
        jsonObject.put("stowaway_tx", stowawayTransaction == null ? "" : Z85.getInstance().encode(stowawayTransaction.bitcoinSerialize()));
        jsonObject.put("stonewall_tx", stonewallTransaction == null ? "" : Z85.getInstance().encode(stonewallTransaction.bitcoinSerialize()));
        return jsonObject;
    }

    @Override
    public void fromJSON(JSONObject cObj) {
        super.fromJSON(cObj);
        this.stonewallAmount = cObj.getLong("stonewall_amount");
        this.stowawayFee = cObj.getLong("stowaway_fee");
        this.stonewallDestination = cObj.getString("stonewall_destination");
        this.stowawayTransaction = cObj.getString("stowaway_tx").equals("") ? null : new Transaction(params, Z85.getInstance().decode(cObj.getString("stowaway_tx")));
        this.stonewallTransaction = cObj.getString("stonewall_tx").equals("") ? null : new Transaction(params, Z85.getInstance().decode(cObj.getString("stonewall_tx")));
    }

    @Override
    protected void signTx(HashMap<String,ECKey> keyBag) {

        Transaction transaction = psbt.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("signTx:" + transaction.toString());
        }

        for(int i = 0; i < transaction.getInputs().size(); i++)   {

            TransactionInput input = transaction.getInput(i);
            TransactionOutPoint outpoint = input.getOutpoint();
            if(keyBag.containsKey(outpoint.toString())) {

                if (log.isDebugEnabled()) {
                    log.debug("signTx outpoint:" + outpoint.toString());
                }

                ECKey key = keyBag.get(outpoint.toString());
                SegwitAddress segwitAddress = new SegwitAddress(key.getPubKey(), params);

                if (log.isDebugEnabled()) {
                    log.debug("signTx bech32:" + segwitAddress.getBech32AsString());
                }

                final Script redeemScript = segwitAddress.segWitRedeemScript();
                final Script scriptCode = redeemScript.scriptCode();

                long value = outpoints.get(outpoint.getHash().toString() + "-" + outpoint.getIndex());
                if (log.isDebugEnabled()) {
                    log.debug("signTx value:" + value);
                }

                TransactionSignature sig = transaction.calculateWitnessSignature(i, key, scriptCode, Coin.valueOf(value), Transaction.SigHash.ALL, false);
                final TransactionWitness witness = new TransactionWitness(2);
                witness.setPush(0, sig.encodeToBitcoin());
                witness.setPush(1, key.getPubKey());
                transaction.setWitness(i, witness);

            }

        }

        if(getStep() > 3) {
            stonewallTransaction = transaction;
        } else {
            stowawayTransaction = transaction;
        }
    }

    public void setStowawayTransaction(Transaction transaction) {
        this.stowawayTransaction = transaction;
    }

    public Transaction getStowawayTransaction() {
        return this.stowawayTransaction;
    }

    public void setStonewallTransaction(Transaction transaction) {
        this.stonewallTransaction = transaction;
    }

    public Transaction getStonewallTransaction() {
        return this.stonewallTransaction;
    }
}