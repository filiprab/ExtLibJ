package com.samourai.wallet.send.spend;

import com.google.common.base.Preconditions;
import com.samourai.wallet.bip340.BIP340Util;
import com.samourai.wallet.bip340.Point;
import com.samourai.wallet.bip340.Schnorr;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.send.SendFactoryGeneric;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class SendFactoryGenericTest {
    private static final SendFactoryGeneric sendFactory = SendFactoryGeneric.getInstance();
    private static final NetworkParameters params = TestNet3Params.get();
    private ECKey inputKey = ECKey.fromPrivate(new BigInteger("45292090369707310635285627500870691371399357286012942906204494584441273561412"));
    private ECKey outputKey = ECKey.fromPrivate(new BigInteger("77292090369707310635285627500870691371399357286012942906204494584441273561412"));
    private BipFormatSupplier bipFormatSupplier = BIP_FORMAT.PROVIDER;

    private Transaction computeTxCoinbase(long value, Script outputScript) {
        Transaction tx = new Transaction(params);

        // add output
        tx.addOutput(Coin.valueOf(value), outputScript);

        // add input: coinbase
        int txCounter = 1;
        TransactionInput input =
                new TransactionInput(
                        params, tx, new byte[] {(byte) txCounter, (byte) (txCounter++ >> 8)});
        tx.addInput(input);

        tx.verify();
        return tx;
    }

    private Transaction computeSpendTxUnsigned(TransactionOutput txOutput) {
        // spend coinbase
        TransactionOutPoint inputOutPoint = txOutput.getOutPointFor();
        inputOutPoint.setValue(txOutput.getValue());

        Transaction tx = new Transaction(params);
        SegwitAddress outputAddress = new SegwitAddress(outputKey, params);
        TransactionOutput transactionOutput = new TransactionOutput(params, null, inputOutPoint.getValue(),
                outputAddress.getAddress());
        tx.addOutput(transactionOutput);

        // add input
        TransactionInput txInput = new TransactionInput(params, null, new byte[0], inputOutPoint, inputOutPoint.getValue());
        tx.addInput(txInput);
        return tx;
    }

    @Test
    public void signTransactionP2WPKH() throws Exception {
        // spend coinbase -> P2WPKH
        Script outputScript = ScriptBuilder.createP2WPKHOutputScript(inputKey);
        Transaction txCoinbase = computeTxCoinbase(999999, outputScript);

        // spend tx
        TransactionOutput txOutput = txCoinbase.getOutput(0);
        Transaction tx = computeSpendTxUnsigned(txOutput);
        Assertions.assertEquals("876e9d14e51a6992377435b51cfbe506ed33743667a292039424cef30e1d633f", tx.getHashAsString());

        // sign tx
        Map<String,ECKey> keyBag = new LinkedHashMap<>();
        keyBag.put(tx.getInput(0).getOutpoint().toString(), inputKey);
        sendFactory.signTransaction(tx, keyBag, bipFormatSupplier);

        tx.verify();

        Assertions.assertEquals("876e9d14e51a6992377435b51cfbe506ed33743667a292039424cef30e1d633f", tx.getHashAsString());
    }

    @Test
    public void signTransactionP2PKH() throws Exception {
        // spend coinbase -> P2PKH
        Address inputAddressP2PKH = new Address(params, inputKey.getPubKeyHash());
        Script outputScript = ScriptBuilder.createOutputScript(inputAddressP2PKH);
        Transaction txCoinbase = computeTxCoinbase(999999, outputScript);

        // spend tx
        TransactionOutput txOutput = txCoinbase.getOutput(0);
        Transaction tx = computeSpendTxUnsigned(txOutput);
        Assertions.assertEquals("7a359d7a01ae9eb38019ddafecd32b7a5831bbb75919bbed3e1198444f811d2c", tx.getHashAsString());

        // sign tx
        Map<String,ECKey> keyBag = new LinkedHashMap<String, ECKey>();
        keyBag.put(tx.getInput(0).getOutpoint().toString(), inputKey);
        sendFactory.signTransaction(tx, keyBag, bipFormatSupplier);

        tx.verify();

        Assertions.assertEquals("42a5498a1414fec290dc38b0545bf5f6346a6c9339b5055462af0fb31f8ebc84", tx.getHashAsString());
    }

    @Test
    public void signTransactionP2SHP2WPKH() throws Exception {
        // spend coinbase -> P2SHP2WPKH
        Script ouputScriptP2WPKH = ScriptBuilder.createP2WPKHOutputScript(inputKey);
        Script outputScript = ScriptBuilder.createP2SHOutputScript(ouputScriptP2WPKH);
        Transaction txCoinbase = computeTxCoinbase(999999, outputScript);

        // spend tx
        TransactionOutput txOutput = txCoinbase.getOutput(0);
        Transaction tx = computeSpendTxUnsigned(txOutput);
        Assertions.assertEquals("2c525345160a4820fb49d184349e1f88b8aaf94f05bf4dba6a9cfdd751efdf48", tx.getHashAsString());

        // sign tx
        Map<String,ECKey> keyBag = new LinkedHashMap<String, ECKey>();
        keyBag.put(tx.getInput(0).getOutpoint().toString(), inputKey);
        sendFactory.signTransaction(tx, keyBag, bipFormatSupplier);

        tx.verify();

        Assertions.assertEquals("34f4a406306e36aabccaa2dad7677665998b1ebb0bc5e89ad8887c1a5c32379d", tx.getHashAsString());
    }

    @Test
    public void signTransactionP2TR() throws Exception {
        // spend coinbase -> P2TR
        ECKey tweakedKey = BIP340Util.getTweakedPrivKey(inputKey, null);
        byte[] tweakedPubKeyBytes = BIP340Util.getInternalPubkey(tweakedKey).toBytes();
        Script outputScript = createP2TROutputScript(tweakedPubKeyBytes);
        Transaction txCoinbase = computeTxCoinbase(999999, outputScript);

        // spend tx
        TransactionOutput txOutput = txCoinbase.getOutput(0);
        Transaction tx = computeSpendTxUnsigned(txOutput);
        Assertions.assertEquals("598cbf9f11ab9a1a5e788dbd11a7cf970089cec43e04fc073eb91c0a5717fd0e", tx.getHashAsString());

        // sign tx
        Map<String,ECKey> keyBag = new LinkedHashMap<>();
        keyBag.put(tx.getInput(0).getOutpoint().toString(), tweakedKey);
        byte[] sigHash = Hex.decode("46983f14cecf854c913cd31ed17402facd7b540e3071e9f0cd6cec3a912f13b8"); // The Taproot sighash for SigHash.ALL (0x01) for the above spend tx.
        sendFactory.signTransaction(tx, keyBag, bipFormatSupplier);

        tx.verify();

        byte[] encodedSig = tx.getWitness(0).getPush(0);
        byte[] sig = null;
        if(encodedSig != null) {
            sig = Arrays.copyOfRange(encodedSig, 0, 64);
        }

        assert(sig != null);
        assert(sig.length == 64);
        boolean validSig = Schnorr.verify(sigHash, tweakedPubKeyBytes, sig);
        assert(validSig);

        Assertions.assertEquals("598cbf9f11ab9a1a5e788dbd11a7cf970089cec43e04fc073eb91c0a5717fd0e", tx.getHashAsString());
    }

    private Script createP2TROutputScript(byte[] hash) {
        Preconditions.checkArgument(hash.length == 32);
        return (new ScriptBuilder()).smallNum(1).data(hash).build();
    }
}
