package com.samourai.wallet.bip47.auth47;

import com.samourai.wallet.bip47.rpc.PaymentCode;
import com.samourai.wallet.util.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.NetworkParameters;

import javax.management.Notification;

public class Auth47Proof {
    private static final String AUTH47_RESPONSE = "1.0";
    private String auth47_response;     // '1.0'
    private String challenge;           // 'auth47://aerezerzerze23131d?r=https://samourai.io/auth',
    private String address; // either address or nym
    private String nym; // 'PM8TJTLJbPRGxSbc8EJ...TzFcwQRya4GA',
    private String signature;           // 'signature': 'Hyn9En/w5I2LHR...ct8mbFD86o

    public Auth47Proof() {}

    public Auth47Proof(String challenge, String address, String nym, String signature) {
        this.auth47_response = AUTH47_RESPONSE;
        this.challenge = challenge;
        this.address = address;
        this.nym = nym;
        this.signature = signature;
    }

    public static Auth47Proof parse(String proof) throws Exception {
        return JSONUtils.getInstance().getObjectMapper().readValue(proof, Auth47Proof.class);
    }

    public String computeDestination(NetworkParameters params) {
        if (StringUtils.isEmpty(nym) && StringUtils.isEmpty(address)) {
            throw new IllegalArgumentException("Invalid proof: you must specify either proof.address or proof.nym");
        }
        if (!StringUtils.isEmpty(nym)) {
            PaymentCode paymentCode = new PaymentCode(nym);
            return paymentCode.notificationAddress(params).getAddressString();
        }
        return address;
    }

    public Auth47Challenge getChallengeAuth47() throws Exception {
        return Auth47Challenge.parse(challenge);
    }

    public String getAuth47_response() {
        return auth47_response;
    }

    public void setAuth47_response(String auth47_response) {
        this.auth47_response = auth47_response;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNym() {
        return nym;
    }

    public void setNym(String nym) {
        this.nym = nym;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
