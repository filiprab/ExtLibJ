package com.samourai.wallet.crypto.dleq;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.LazyECPoint;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

public class PedersenCommitment {
    private BigInteger r;
    private ECPoint commitment;

    public PedersenCommitment(ECPoint commitment, BigInteger r) {
        this.r = r;
        this.commitment = commitment;
    }
    private static ECPoint G_PRIME = new LazyECPoint(ECKey.CURVE.getCurve(), Hex.decode("0250929b74c1a04954b78b4b6035e97a5e078a5a0f28ec96d547bfee9ace803ac0")).get();
    public static PedersenCommitment commit(BigInteger x) {
        BigInteger r = new ECKey().getPrivKey();
        ECPoint commitment = (ECKey.CURVE.getG().multiply(x)).add(G_PRIME.multiply(r));
        assert !r.equals(BigInteger.ZERO);
        return new PedersenCommitment(commitment, r);
    }
    // TODO unsure if the above is needed

    // TODO blinding and shit


    public ECPoint getCommitment() {
        return commitment;
    }
}
