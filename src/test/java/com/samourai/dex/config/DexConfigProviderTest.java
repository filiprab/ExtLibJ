package com.samourai.dex.config;

import com.samourai.http.client.IHttpClient;
import com.samourai.http.client.JettyHttpClient;
import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class DexConfigProviderTest extends AbstractTest {

  private DexConfigProvider dexConfigProvider = DexConfigProvider.getInstance();
  protected IHttpClient httpClient;


  @BeforeEach
  public void setUp() throws Exception{
    super.setUp();
  }

  @Test
  public void test() {
    Assertions.assertEquals("https://api.samouraiwallet.com/v2", dexConfigProvider.getSamouraiConfig().getBackendServerMainnetClear());
  }

  @Test
  public void load() throws Exception {
    httpClient = new JettyHttpClient(10000, Optional.empty(), "test");
    dexConfigProvider.load(httpClient, params);
    Assertions.assertEquals("https://soroban.samouraiwallet.com/test?whirlpoolServer=true", dexConfigProvider.getSamouraiConfig().getSorobanServerTestnetClear());
  }
}
