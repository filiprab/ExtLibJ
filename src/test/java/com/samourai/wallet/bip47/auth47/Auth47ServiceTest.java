package com.samourai.wallet.bip47.auth47;

import com.samourai.wallet.bip47.rpc.BIP47Account;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Auth47ServiceTest extends AbstractTest {
    protected static final Logger log = LoggerFactory.getLogger(Auth47ServiceTest.class);
    private static final String CALLBACK_URI = "srbn://123aef4567890aef@samourai.onion";
    private Auth47Service auth47Service;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        auth47Service = new Auth47Service();
    }

    @Test
    public void verify() throws Exception {
        Auth47Proof proof = new Auth47Proof(
                "auth47://aerezerzerze23131d?r=srbn",
                null,
                "PM8TJTLJbPRGxSbc8EJi42Wrr6QbNSaSSVJ5Y3E4pbCYiTHUskHg13935Ubb7q8tx9GVbh2UuRnBc3WSyJHhUrw8KhprKnn9eDznYGieTzFcwQRya4GA",
                "Hyn9En/w5I2LHRNE1iuV+r3pFnSdBj9XZHtXuqZjcAjXdh3IsdUR9c5rTnQibGRb6aowfXY21G+Nyct8mbFD86o=");

        Assertions.assertTrue(auth47Service.verify(proof, params));
    }

    @Test
    public void create_verify_parse_address() throws Exception {
        // create proof
        Auth47Challenge challenge = new Auth47Challenge(CALLBACK_URI, "foo", 1234L);
        HD_Address hdAddress = bip44w.getAddressAt(0,0,0);
        Auth47Proof proof = auth47Service.create(challenge, hdAddress);
        String proofStr = jsonUtils.getObjectMapper().writeValueAsString(proof);
        Assertions.assertEquals("{\"auth47_response\":\"1.0\",\"challenge\":\"auth47://000000000000000000000000?c=srbn://123aef4567890aef@samourai.onion&r=foo&e=1234\",\"address\":\"mhJALds5qzDgZrpnAS9xk8utUcBEWmFdaU\",\"nym\":null,\"signature\":\"ILZ9d/PWHKjxCo4rMrxmrQqQfovS7R6dS/4JzR3MCMOvHErAEQxmorm2Kut6hDnLhIN978u32laWTvNtp1yntb4=\",\"challengeAuth47\":{\"nonce\":\"000000000000000000000000\",\"callback\":\"srbn://123aef4567890aef@samourai.onion\",\"resource\":\"foo\",\"expiry\":1234}}", proofStr);

        // verify
        Assertions.assertTrue(auth47Service.verify(proof, params));

        // parse
        Auth47Proof p = Auth47Proof.parse(proofStr);
        Auth47Challenge c = p.getChallengeAuth47();
        Assertions.assertEquals(CALLBACK_URI, c.getCallback());
        Assertions.assertEquals("foo", c.getResource());
        Assertions.assertEquals(1234L, c.getExpiry());
        Assertions.assertEquals("mhJALds5qzDgZrpnAS9xk8utUcBEWmFdaU", p.getAddress());
        Assertions.assertEquals("ILZ9d/PWHKjxCo4rMrxmrQqQfovS7R6dS/4JzR3MCMOvHErAEQxmorm2Kut6hDnLhIN978u32laWTvNtp1yntb4=", p.getSignature());
        Assertions.assertNull(p.getNym());
    }

    @Test
    public void create_verify_parse_nym() throws Exception {
        // create proof
        Auth47Challenge challenge = new Auth47Challenge(CALLBACK_URI, "foo", 1234L);
        BIP47Account bip47Account = bip47Wallet.getAccount(0);
        Auth47Proof proof = auth47Service.create(challenge, bip47Account);
        String proofStr = jsonUtils.getObjectMapper().writeValueAsString(proof);
        Assertions.assertEquals("{\"auth47_response\":\"1.0\",\"challenge\":\"auth47://000000000000000000000000?c=srbn://123aef4567890aef@samourai.onion&r=foo&e=1234\",\"address\":null,\"nym\":\"PM8TJXp19gCE6hQzqRi719FGJzF6AreRwvoQKLRnQ7dpgaakakFns22jHUqhtPQWmfevPQRCyfFbdDrKvrfw9oZv5PjaCerQMa3BKkPyUf9yN1CDR3w6\",\"signature\":\"IOVdsbThB801MWyUY447VtX1DxxZzMAQNvn7NYI34bMZagKfUfExlcV+6gMZ/k1SIHYDuJQGCf20PHoK8m0fKxw=\",\"challengeAuth47\":{\"nonce\":\"000000000000000000000000\",\"callback\":\"srbn://123aef4567890aef@samourai.onion\",\"resource\":\"foo\",\"expiry\":1234}}", proofStr);

        // verify
        Assertions.assertTrue(auth47Service.verify(proof, params));

        // parse
        Auth47Proof p = Auth47Proof.parse(proofStr);
        Auth47Challenge c = p.getChallengeAuth47();
        Assertions.assertEquals(CALLBACK_URI, c.getCallback());
        Assertions.assertEquals("foo", c.getResource());
        Assertions.assertEquals(1234L, c.getExpiry());
        Assertions.assertNull(p.getAddress());
        Assertions.assertEquals("IOVdsbThB801MWyUY447VtX1DxxZzMAQNvn7NYI34bMZagKfUfExlcV+6gMZ/k1SIHYDuJQGCf20PHoK8m0fKxw=", p.getSignature());
        Assertions.assertEquals("PM8TJXp19gCE6hQzqRi719FGJzF6AreRwvoQKLRnQ7dpgaakakFns22jHUqhtPQWmfevPQRCyfFbdDrKvrfw9oZv5PjaCerQMa3BKkPyUf9yN1CDR3w6", p.getNym());
    }
};