package com.samourai.wallet.bip47.auth47;

import com.google.common.base.Optional;

// protocol for Auth47 callbacks
public enum Auth47Protocol {
  HTTPS("https"),
  HTTP("http"),
  SRBN("srbn"),
  SRBNS("srbns");

  private String value;

  Auth47Protocol(String value) {
    this.value = value;
  }

  public static Optional<Auth47Protocol> find(String value) {
    for (Auth47Protocol item : Auth47Protocol.values()) {
      if (item.value.equals(value)) {
        return Optional.of(item);
      }
    }
    return Optional.absent();
  }

  public String getValue() {
    return value;
  }
}
