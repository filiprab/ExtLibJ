package com.samourai.wallet.cahoots;

import java.util.List;

public interface CahootsInputListener {
    void addInProgressInput(String outpoint);
    List<String> getInProgressInputs();
}
