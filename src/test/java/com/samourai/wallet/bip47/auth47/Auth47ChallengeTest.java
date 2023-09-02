package com.samourai.wallet.bip47.auth47;

import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Auth47ChallengeTest extends AbstractTest {
    protected static final Logger log = LoggerFactory.getLogger(Auth47ChallengeTest.class);
    private static final String CALLBACK_URI = "srbn://123aef4567890aef@samourai.onion";

    @Test
    public void challenge_toString() throws Exception {
        Assertions.assertEquals("auth47://000000000000000000000000?c=srbn://123aef4567890aef@samourai.onion", new Auth47Challenge(CALLBACK_URI, null, null).toString());
        Assertions.assertEquals("auth47://000000000000000000000000?c=srbn://123aef4567890aef@samourai.onion&r=foo&e=1234", new Auth47Challenge(CALLBACK_URI, "foo", 1234L).toString());
    }

    @Test
    public void parse_full() throws Exception {
        Auth47Challenge challenge = Auth47Challenge.parse("auth47://000000000000000000000000?c=srbn://123aef4567890aef@samourai.onion&r=foo&e=1234");
        Assertions.assertEquals(CALLBACK_URI, challenge.getCallback());
        Assertions.assertEquals("000000000000000000000000", challenge.getNonce());
        Assertions.assertEquals("foo", challenge.getResource());
        Assertions.assertEquals(1234L, challenge.getExpiry());
    }

    @Test
    public void parse_minimal() throws Exception {
        Auth47Challenge challenge = Auth47Challenge.parse("auth47://000000000000000000000000?c=srbn://123aef4567890aef@samourai.onion");
        Assertions.assertEquals(CALLBACK_URI, challenge.getCallback());
        Assertions.assertEquals("000000000000000000000000", challenge.getNonce());
        Assertions.assertNull(challenge.getResource());
        Assertions.assertNull(challenge.getExpiry());
    }
};