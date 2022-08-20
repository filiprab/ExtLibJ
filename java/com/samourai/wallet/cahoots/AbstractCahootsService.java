package com.samourai.wallet.cahoots;

import com.samourai.soroban.cahoots.CahootsContext;
import com.samourai.soroban.cahoots.ManualCahootsMessage;
import com.samourai.soroban.cahoots.TxBroadcastInteraction;
import com.samourai.soroban.cahoots.TypeInteraction;
import com.samourai.soroban.client.SorobanInteraction;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.util.RandomUtil;
import org.bitcoinj.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractCahootsService<T extends Cahoots, C extends CahootsContext> {
    private static final Logger log = LoggerFactory.getLogger(AbstractCahootsService.class);

    private CahootsType cahootsType;
    private BipFormatSupplier bipFormatSupplier;
    protected NetworkParameters params;
    private TypeInteraction typeInteractionBroadcast;

    public AbstractCahootsService(CahootsType cahootsType, BipFormatSupplier bipFormatSupplier, NetworkParameters params, TypeInteraction typeInteractionBroadcast) {
        this.cahootsType = cahootsType;
        this.bipFormatSupplier = bipFormatSupplier;
        this.params = params;
        this.typeInteractionBroadcast = typeInteractionBroadcast;
    }

    public abstract T startInitiator(C cahootsContext) throws Exception;

    public abstract T startCollaborator(C cahootsContext, T payload0) throws Exception;

    public abstract T reply(C cahootsContext, T payload) throws Exception;

    public void verifyResponse(C cahootsContext, T response, T request) throws Exception {
        if (!cahootsContext.getCahootsType().equals(cahootsType)) {
            throw new Exception("Invalid cahootsContext.type: "+cahootsContext.getCahootsType()+" vs "+cahootsType);
        }
        if (request != null) {
            // properties should never change
            if (response.getType() != request.getType()) {
                throw new Exception("Invalid altered Cahoots type");
            }
            if (response.getVersion() != request.getVersion()) {
                throw new Exception("Invalid altered Cahoots version");
            }
            if (!response.getParams().equals(request.getParams())) {
                throw new Exception("Invalid altered Cahoots params");
            }

            // step should increment
            if (response.getStep() != request.getStep() + 1) {
                throw new Exception("Invalid response step");
            }
        }
    }

    public SorobanInteraction checkInteraction(ManualCahootsMessage request, Cahoots cahootsResponse) {
        // broadcast by SENDER
        if (request.getTypeUser().getPartner().equals(typeInteractionBroadcast.getTypeUser()) && (request.getStep()+1) == typeInteractionBroadcast.getStep()) {
            return new TxBroadcastInteraction(typeInteractionBroadcast, cahootsResponse);
        }
        return null;
    }

    protected HashMap<String, ECKey> computeKeyBag(Cahoots2x cahoots, List<CahootsUtxo> utxos) {
        // utxos by hash
        HashMap<String, CahootsUtxo> utxosByHash = new HashMap<String, CahootsUtxo>();
        for (CahootsUtxo utxo : utxos) {
            MyTransactionOutPoint outpoint = utxo.getOutpoint();
            utxosByHash.put(outpoint.getHash().toString() + "-" + outpoint.getIndex(), utxo);
        }

        Transaction transaction = cahoots.getTransaction();
        HashMap<String, ECKey> keyBag = new HashMap<String, ECKey>();
        for (TransactionInput input : transaction.getInputs()) {
            TransactionOutPoint outpoint = input.getOutpoint();
            String key = outpoint.getHash().toString() + "-" + outpoint.getIndex();
            if (utxosByHash.containsKey(key)) {
                CahootsUtxo utxo = utxosByHash.get(key);
                ECKey eckey = ECKey.fromPrivate(utxo.getKey());
                keyBag.put(outpoint.toString(), eckey);
            }
        }
        return keyBag;
    }

    // verify

    protected long computeSpendAmount(HashMap<String,ECKey> keyBag, Cahoots2x cahoots, CahootsContext cahootsContext) throws Exception {
        long spendAmount = 0;

        if (log.isDebugEnabled()) {
            log.debug("computeSpendAmount: keyBag="+keyBag.keySet());
        }
        Transaction transaction = cahoots.getTransaction();
        for(TransactionInput input : transaction.getInputs()) {
            TransactionOutPoint outpoint = input.getOutpoint();
            if (keyBag.containsKey(outpoint.toString())) {
                Long inputValue = cahoots.getOutpoints().get(outpoint.getHash().toString() + "-" + outpoint.getIndex());
                if (inputValue != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("computeSpendAmount: +input "+inputValue + " "+outpoint.toString());
                    }
                    spendAmount += inputValue;
                }
            }
        }

        for(TransactionOutput output : transaction.getOutputs()) {
            String outputAddress = bipFormatSupplier.getToAddress(output);
            if (outputAddress != null && cahootsContext.getOutputAddresses().contains(outputAddress)) {
                if (output.getValue() != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("computeSpendAmount: -output " + output.getValue().longValue()+" "+outputAddress);
                    }
                    spendAmount -= output.getValue().longValue();
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("computeSpendAmount = " + spendAmount);
        }
        return spendAmount;
    }

    protected  TransactionOutput computeTxOutput(BipAddress bipAddress, long amount, CahootsContext cahootsContext) throws Exception{
        return computeTxOutput(bipAddress.getAddressString(), amount, cahootsContext);
    }

    protected  TransactionOutput computeTxOutput(String receiveAddressString, long amount, CahootsContext cahootsContext) throws Exception{
        cahootsContext.addOutputAddress(receiveAddressString); // save output address for computeSpendAmount()
        return bipFormatSupplier.getTransactionOutput(receiveAddressString, amount, params);
    }

    public BipFormatSupplier getBipFormatSupplier() {
        return bipFormatSupplier;
    }

    // overridable for tests
    protected int getRandNextInt(int bound) {
        SecureRandom random = RandomUtil.getSecureRandom();
        return random.nextInt(bound);
    }

    // overridable for tests
    protected void shuffleUtxos(List<CahootsUtxo> utxos) {
        Collections.shuffle(utxos);
    }
}
