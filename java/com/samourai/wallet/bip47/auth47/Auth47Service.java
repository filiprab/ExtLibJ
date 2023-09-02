package com.samourai.wallet.bip47.auth47;

import com.samourai.wallet.bip47.rpc.BIP47Account;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.util.MessageSignUtilGeneric;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Auth47Service {
    private static final Logger log = LoggerFactory.getLogger(Auth47Service.class);
    private static final MessageSignUtilGeneric messageSignUtil = MessageSignUtilGeneric.getInstance();

    public Auth47Service() {
    }

    public boolean verify(Auth47Proof proof, NetworkParameters params) throws Exception {
        // verify challenge
        Auth47Challenge.parse(proof.getChallenge());

        String address = proof.computeDestination(params);
        return messageSignUtil.verifySignedMessage(address, proof.getChallenge(), proof.getSignature(), params);
    }

    public Auth47Proof create(Auth47Challenge auth47Challenge, HD_Address hdAddress) throws Exception {
        return create(auth47Challenge, hdAddress.getAddressString(), hdAddress.getECKey());
    }

    public Auth47Proof create(Auth47Challenge challenge, String address, ECKey ecKey) throws Exception {
        String challengeStr = challenge.toString();
        String signature = messageSignUtil.signMessage(ecKey, challengeStr);
        return new Auth47Proof(challengeStr, address, null, signature);
    }

    public Auth47Proof create(Auth47Challenge challenge, BIP47Account bip47Account) throws Exception {
        HD_Address hdAddress = bip47Account.getNotificationAddress();
        String nym = bip47Account.getPaymentCode();

        // create proof
        String challengeStr = challenge.toString();
        String signature = messageSignUtil.signMessage(hdAddress.getECKey(), challengeStr);
        return new Auth47Proof(challengeStr, null, nym, signature);
    }
}
