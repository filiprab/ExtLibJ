package com.samourai.wallet.constants;

import com.samourai.wallet.bip47.rpc.PaymentCode;
import com.samourai.wallet.util.FormatsUtilGeneric;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

import java.util.Optional;

public enum SamouraiNetwork {
  TESTNET(
          TestNet3Params.get(),
          "mi42XN9J3eLdZae4tjQnJnVkCcNDRuAtz4"), // TODO
  INTEGRATION(
          TestNet3Params.get(),
          "mi42XN9J3eLdZae4tjQnJnVkCcNDRuAtz4"), // TODO
  MAINNET(
          MainNetParams.get(),
          "1NwVafYT1s6SF5Atusv7A8MASzCvGruGXq"), // TODO
  LOCAL_TESTNET(TestNet3Params.get(),
          "mi42XN9J3eLdZae4tjQnJnVkCcNDRuAtz4"), // TODO
  LOCAL_REGTEST(
          RegTestParams.get(),
          "Malo by to byt jedno co sem dam");

  private NetworkParameters params;
  private String signingAddress;

  SamouraiNetwork(
          NetworkParameters params,
          String signingAddress) {
    this.params = params;
    this.signingAddress = signingAddress;
  }

  public NetworkParameters getParams() {
    return params;
  }

  public static Optional<SamouraiNetwork> find(String value) {
    try {
      return Optional.of(valueOf(value));
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
