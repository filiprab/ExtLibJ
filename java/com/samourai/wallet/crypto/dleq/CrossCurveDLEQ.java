package com.samourai.wallet.crypto.dleq;

import com.samourai.wallet.util.Pair;
import org.bouncycastle.math.ec.ECPoint;

import java.util.List;

public class CrossCurveDLEQ {
    private final ECPoint HP;
    private final ECPoint HQ;
    private final List<Pair<ECPoint, ECPoint>> powersOfTwo;

    public CrossCurveDLEQ(ECPoint HP, ECPoint HQ, List<Pair<ECPoint, ECPoint>> powersOfTwo) {
        this.HP = HP;
        this.HQ = HQ;
        this.powersOfTwo = powersOfTwo;
    }

    public List<Pair<ECPoint, ECPoint>> getPowersOfTwo() {
        return powersOfTwo;
    }
}
