package com.samourai.wallet.api.paynym;

import com.samourai.wallet.api.paynym.beans.*;
import com.samourai.wallet.bip47.rpc.BIP47Wallet;
import com.samourai.wallet.bip47.rpc.java.Bip47UtilJava;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import com.samourai.wallet.test.AbstractTest;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PaynymApiTest extends AbstractTest {
  private static final String PCODE="PM8TJXp19gCE6hQzqRi719FGJzF6AreRwvoQKLRnQ7dpgaakakFns22jHUqhtPQWmfevPQRCyfFbdDrKvrfw9oZv5PjaCerQMa3BKkPyUf9yN1CDR3w6";
  private static final String PCODE2 = "PM8TJfP8GCovEuu715SgTzzhRFY6Lki9E9T9JJR4JRyqEBXcFmMmfSrz58cY5MhaDEfd1BuWUBXPwjk1vRm4aTHcBM2vQyVvQhcdTGRQGNCnGeqbWW4B";
  private static final NetworkParameters params = TestNet3Params.get();

  private PaynymApi paynymApi;
  private BIP47Wallet bip47w;
  private String paymentCode;

  public PaynymApiTest() throws Exception {
    super();

    Bip47UtilJava bip47Util = Bip47UtilJava.getInstance();
    paynymApi = new PaynymApi(httpClient, PaynymServer.get().getUrl(), bip47Util);

    HD_Wallet bip44w = HD_WalletFactoryGeneric.getInstance().restoreWallet(SEED_WORDS, SEED_PASSPHRASE, params);
    bip47w = new BIP47Wallet(bip44w);
    paymentCode = bip47Util.getPaymentCode(bip47w).toString();
    Assertions.assertEquals(PCODE, paymentCode);
  }

  @Test
  public void createPaynym() throws Exception {
    CreatePaynymResponse response = paynymApi.createPaynym(paymentCode).blockingSingle();

    Assertions.assertEquals("/"+PCODE+"/avatar", response.nymAvatar);
    Assertions.assertEquals("+stillmud69f", response.nymName);
    Assertions.assertEquals("nymmFABjPvpR2uxmAUKfD53mj", response.nymID);
    Assertions.assertEquals(true, response.claimed);
  }

  @Test
  public void addPaynym() throws Exception {
    String token = paynymApi.getToken(paymentCode).blockingSingle();
    AddPaynymResponse response = paynymApi.addPaynym(token, bip47w).blockingSingle();

    Assertions.assertEquals("/"+PCODE+"/avatar", response.nymAvatar);
    Assertions.assertEquals("+stillmud69f", response.nymName);
    Assertions.assertEquals("nymmFABjPvpR2uxmAUKfD53mj", response.nymID);
    Assertions.assertEquals(true, response.claimed);
  }

  @Test
  public void claim() throws Exception {
    String token = paynymApi.getToken(paymentCode).blockingSingle();
    ClaimPaynymResponse claim = paynymApi.claim(token, bip47w).blockingSingle();

    Assertions.assertEquals(paymentCode, claim.claimed);
  }

  @Test
  public void followUnfollow() throws Exception {

    // follow
    String token = paynymApi.getToken(paymentCode).blockingSingle();
    paynymApi.follow(token, bip47w, PCODE2).blockingSingle();

    // verify
    GetNymInfoResponse getNymInfo = paynymApi.getNymInfo(paymentCode).blockingSingle();
    PaynymContact paynymContact = getNymInfo.following.iterator().next();
    Assertions.assertEquals(PCODE2, paynymContact.getCode());
    Assertions.assertEquals("nymHc99UYDRYd6EdPYxbLCSLC", paynymContact.getNymId());
    Assertions.assertEquals("+boldboat533", paynymContact.getNymName());

    // unfollow
    paynymApi.unfollow(token, bip47w, PCODE2).blockingSingle();

    // verify
    getNymInfo = paynymApi.getNymInfo(paymentCode).blockingSingle();
    Assertions.assertFalse(getNymInfo.following.contains(PCODE2));
  }

  @Test
  public void getNym() throws Exception {
    GetNymInfoResponse getNymInfo = paynymApi.getNymInfo(paymentCode).blockingSingle();
    Assertions.assertEquals("/"+PCODE+"/avatar", getNymInfo.nymAvatar);
    Assertions.assertEquals("+stillmud69f", getNymInfo.nymName);
    Assertions.assertEquals("nymmFABjPvpR2uxmAUKfD53mj", getNymInfo.nymID);
    Assertions.assertEquals(true, getNymInfo.segwit);

    Assertions.assertTrue(getNymInfo.following.isEmpty());
    Assertions.assertTrue(getNymInfo.followers.isEmpty());
  }
}
