package com.samourai.wallet.crypto.dleq;

import com.samourai.wallet.util.Pair;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.LazyECPoint;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

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

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("curve25519");
    private static final ECDomainParameters CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    public static void main(String args[])
    {
        BigInteger secretKey = generateCurve25519Key();
        DLEQProof dleqProof = prove(secretKey);
    }

//! 1. For i=0..252 we show the ith commitment is either to 0 or 2^i
//! 2. That the commiments are the same value for both sets.
//! 3. The sum of the commitments equals to the claimed public keys on each curve.

    public static DLEQProof prove(BigInteger privateKey) {
        System.out.println(privateKey);
        ECKey secpKey = ECKey.fromPrivate(privateKey);
        ECPoint secpPoint = secpKey.getPubKeyPoint();

        ECPoint edPoint = publicPointFromPrivate(privateKey);
        Pair<ECPoint, ECPoint> claim = Pair.of(secpPoint.normalize(), edPoint);
        System.out.println(Hex.toHexString(edPoint.getEncoded(false)));
        System.out.println(Hex.toHexString(secpPoint.getEncoded(false)));
        ArrayList<byte[]> pedersenBlindings = new ArrayList<>();
        for(int i = 0; i < 252; i++) {

        }
        return null;
    }

    public static ECPoint publicPointFromPrivate(BigInteger privKey) {
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }

        return (new FixedPointCombMultiplier()).multiply(CURVE.getG(), privKey);
    }

    public static BigInteger generateCurve25519Key() {
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, new SecureRandom());
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters)keypair.getPrivate();
        return privParams.getD();
    }
}
