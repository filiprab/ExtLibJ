package com.samourai.wallet.send;

import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.send.beans.SweepPreview;
import com.samourai.wallet.test.AbstractTest;
import com.samourai.wallet.util.PrivKeyReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

public class SweepUtilGenericTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(SweepUtilGenericTest.class);

    private SweepUtilGeneric sweepUtil = SweepUtilGeneric.getInstance();

    @Test
    public void sweepPreview_hex() throws Exception {
        // hex
        String pk = "064f8f0bebfa2f65db003b56bc911535614f2764799bc89091398c1aed82e884";

        BackendApi backendApi = computeBackendApi(params);
        PrivKeyReader privKeyReader = new PrivKeyReader(pk, params);
        Collection<SweepPreview> sweepPreviews = sweepUtil.sweepPreviews(privKeyReader, 1, backendApi);

        Assertions.assertEquals(3, sweepPreviews.size());
        Iterator<SweepPreview> it = sweepPreviews.iterator();

        SweepPreview sweepPreview = it.next();
        assertSweepPreview(sweepPreview, 9798, "mwdHSyBBHMbcy8rogvzssvDrGyffRo3amQ", BIP_FORMAT.LEGACY, 202, 1, new BigInteger("2854445280755403823944422649848886010716442579975080723501674454330739189892"));

        sweepPreview = it.next();
        assertSweepPreview(sweepPreview, 1048, "2Mu3RYBxGdkz8rwCaYZWhi24A4aR1cSWJFb", BIP_FORMAT.SEGWIT_COMPAT,152, 1, new BigInteger("2854445280755403823944422649848886010716442579975080723501674454330739189892"));

        sweepPreview = it.next();
        assertSweepPreview(sweepPreview, 2871, "tb1qkz6870gwrtp4unx9yw4qvz27tagd5wykynufyq", BIP_FORMAT.SEGWIT_NATIVE, 129, 1, new BigInteger("2854445280755403823944422649848886010716442579975080723501674454330739189892"));
    }

    @Test
    public void sweepPreview_WIF_uncompressed() throws Exception {
        // WIF, uncompressed
        String pk = "91dhN38UTmqGtd3zG1GnDdnyivAP5LnWJQyyj7V7pqthirHAj4X";

        PrivKeyReader privKeyReader = new PrivKeyReader(pk, params);
        BackendApi backendApi = computeBackendApi(params);
        Collection<SweepPreview> sweepPreviews = sweepUtil.sweepPreviews(privKeyReader, 1, backendApi);

        Assertions.assertEquals(3, sweepPreviews.size());
        Iterator<SweepPreview> it = sweepPreviews.iterator();

        SweepPreview sweepPreview = it.next();
        assertSweepPreview(sweepPreview, 9798, "mwdHSyBBHMbcy8rogvzssvDrGyffRo3amQ", BIP_FORMAT.LEGACY, 202, 1, new BigInteger("2854445280755403823944422649848886010716442579975080723501674454330739189892"));

        sweepPreview = it.next();
        assertSweepPreview(sweepPreview, 1048, "2Mu3RYBxGdkz8rwCaYZWhi24A4aR1cSWJFb", BIP_FORMAT.SEGWIT_COMPAT,152, 1, new BigInteger("2854445280755403823944422649848886010716442579975080723501674454330739189892"));

        sweepPreview = it.next();
        assertSweepPreview(sweepPreview, 2871, "tb1qkz6870gwrtp4unx9yw4qvz27tagd5wykynufyq", BIP_FORMAT.SEGWIT_NATIVE, 129, 1, new BigInteger("2854445280755403823944422649848886010716442579975080723501674454330739189892"));
    }

    /*@Test
    public void sweepPreview_WIF_compressed() throws Exception {
        // WIF, compressed
        String pk = "cMny9rPzDAt58r8BjECeamPwN1eQSAKrKrrVNsd78AoCjcWxuVym";

        BackendApi backendApi = computeBackendApi(params);
        PrivKeyReader privKeyReader = new PrivKeyReader(pk, params);
        Collection<SweepPreview> sweepPreviews = sweepUtil.sweepPreviews(privKeyReader, 1, backendApi);

        Assertions.assertEquals(0, sweepPreviews.size());
    }*/

    protected void assertSweepPreview(SweepPreview sweepPreview, long amount, String address, BipFormat bipFormat, long fee, int outpoints, BigInteger privKey) {
        Assertions.assertEquals(amount, sweepPreview.getAmount());
        Assertions.assertEquals(privKey, sweepPreview.getPrivKey().getPrivKey());
        Assertions.assertEquals(address, sweepPreview.getAddress());
        Assertions.assertEquals(bipFormat, sweepPreview.getBipFormat());
        Assertions.assertEquals(fee, sweepPreview.getFee());
        Assertions.assertEquals(amount+fee, UnspentOutput.sumValue(sweepPreview.getUtxos()));
        Assertions.assertEquals(outpoints, sweepPreview.getUtxos().size());
    }
}