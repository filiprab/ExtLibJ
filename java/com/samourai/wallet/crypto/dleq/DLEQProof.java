package com.samourai.wallet.crypto.dleq;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class DLEQProof {
    private final ECPoint g1;
    private final ECPoint g2;

    private final ECPoint h1;
    private final ECPoint h2;

    private final ECCurve curve;

    private final BigInteger x;

    // For proving
    private final BigInteger r;
    private final ECPoint a1;
    private final ECPoint a2;

    public DLEQProof(final ECPoint g1, final ECPoint g2, final ECCurve curve, final BigInteger secret) {
        this.x = secret;

        this.g1 = g1;
        this.g2 = g2;

        this.curve = curve;

        this.h1 = curve.getMultiplier().multiply(g1, secret);
        this.h2 = curve.getMultiplier().multiply(g2, secret);

        this.r = new ECKey().getPrivKey();
        this.a1 = curve.getMultiplier().multiply(g1, r);
        this.a2 = curve.getMultiplier().multiply(g2, r);
    }

    final ECPoint getH1()
    {
        return this.h1;
    }

    final ECPoint getH2()
    {
        return this.h2;
    }

    final ECPoint getA1()
    {
        return this.a1;
    }

    final ECPoint getA2()
    {
        return this.a2;
    }

    final BigInteger getS(final BigInteger challenge)
    {
        return (this.r.subtract(challenge.multiply(x).mod(curve.getOrder()))).mod(curve.getOrder());
    }

    public static void main(String args[])
    {
        // this will fail for now TODO: copy over the point hasher shit from https://github.com/jasonkresch/protect/blob/bfb3ffb83869910859ea5e74111741e85925d43c/pross-common/src/main/java/com/ibm/pross/common/util/crypto/ecc/PointHasher.java
        final ECCurve curve = ECKey.CURVE.getCurve();
        final ECPoint G1 = ECKey.CURVE.getG();
        final ECPoint G2 = ECKey.CURVE.getG().multiply(new BigInteger(Sha256Hash.hash("nothing up my sleeve".getBytes())));

        final BigInteger secret = new ECKey().getPrivKey();
        final DLEQProof dleq = new DLEQProof(G1, G2, curve, secret);

        // Verify result
        final ECPoint H1 = dleq.getH1();
        final ECPoint H2 = dleq.getH2();
        final ECPoint a1 = dleq.getA1();
        final ECPoint a2 = dleq.getA2();
        final BigInteger challenge = new ECKey().getPrivKey();
        final BigInteger s = dleq.getS(challenge);

        // Check first point
        final ECPoint G1s = curve.getMultiplier().multiply(G1, s);
        final ECPoint H1c = curve.getMultiplier().multiply(H1, challenge);
        final ECPoint G1sH1c = G1s.add(H1c);
        if (!G1sH1c.equals(a1))
        {
            System.err.println("Proof failed!");
            return;
        }

        // Check second point
        final ECPoint G2s = curve.getMultiplier().multiply(G2, s);
        final ECPoint H2c = curve.getMultiplier().multiply(H2, challenge);
        final ECPoint G2sH2c = G2s.add(H2c);
        if (!G2sH2c.equals(a2))
        {
            System.err.println("Proof failed!");
            return;
        }

        System.out.println("Proof passed!");
    }
}
