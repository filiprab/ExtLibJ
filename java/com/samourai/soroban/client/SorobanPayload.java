package com.samourai.soroban.client;

public interface SorobanPayload extends SorobanReply {
    String toPayload();
    String getTypePayload();
    long getTimePayload();
}