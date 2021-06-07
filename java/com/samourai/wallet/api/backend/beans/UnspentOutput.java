package com.samourai.wallet.api.backend.beans;

import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.send.MyTransactionOutPoint;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

public class UnspentOutput {
    private static final String PATH_SEPARATOR = "/";
    public String tx_hash;
    public int tx_output_n;
    public int tx_version;
    public long tx_locktime;
    public long value;
    public String script;
    public String addr;
    public String pubkey;
    public int confirmations;
    public Xpub xpub;

    public UnspentOutput() {
    }

    public UnspentOutput(UnspentOutput copy) {
        this.tx_hash = copy.tx_hash;
        this.tx_output_n = copy.tx_output_n;
        this.tx_version = copy.tx_version;
        this.tx_locktime = copy.tx_locktime;
        this.value = copy.value;
        this.script = copy.script;
        this.addr = copy.addr;
        this.pubkey = copy.pubkey;
        this.confirmations = copy.confirmations;
        this.xpub = copy.xpub;
    }

    public int computePathChainIndex() {
      return Integer.parseInt(xpub.path.split(PATH_SEPARATOR)[1]);
    }

    public int computePathAddressIndex() {
      return Integer.parseInt(xpub.path.split(PATH_SEPARATOR)[2]);
    }

    public String getPath() {
        if (xpub == null) {
            return null;
        }
      return xpub.path;
    }

    public String getPathFull(int purpose, int accountIndex) {
        return HD_Address.getPathFull(purpose, 0, accountIndex, computePathChainIndex(), computePathAddressIndex());
    }

    public MyTransactionOutPoint computeOutpoint(NetworkParameters params) {
        Sha256Hash sha256Hash = Sha256Hash.wrap(Hex.decode(tx_hash));
        // use MyTransactionOutPoint to forward scriptBytes + address
        return new MyTransactionOutPoint(params, sha256Hash, tx_output_n, BigInteger.valueOf(value), getScriptBytes(), addr);
    }

    public TransactionInput computeSpendInput(NetworkParameters params) {
        return new TransactionInput(
                        params, null, new byte[] {}, computeOutpoint(params), Coin.valueOf(value));

    }

    public byte[] getScriptBytes() {
        return script != null ? Hex.decode(script) : null;
    }

    public Script computeScript() {
        return new Script(getScriptBytes());
    }

    public static class Xpub {
      public String m;
      public String path;
    }

    @Override
    public String toString() {
      return tx_hash
          + ":"
          + tx_output_n
          + " ("
          + value
          + " sats, "
          + confirmations
          + " confirmations, path="
          + xpub.path
          + ", address="
          + addr
          + ")";
    }
  }