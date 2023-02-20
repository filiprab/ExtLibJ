package com.samourai.wallet.cahoots;

import com.samourai.soroban.client.RpcWallet;
import com.samourai.soroban.client.RpcWalletImpl;
import com.samourai.wallet.bip47.rpc.BIP47Wallet;
import com.samourai.wallet.bip47.rpc.PaymentCode;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.bipWallet.WalletSupplier;
import com.samourai.wallet.chain.ChainSupplier;
import com.samourai.wallet.hd.BIP_WALLET;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.send.provider.CahootsUtxoProvider;
import com.samourai.wallet.whirlpool.WhirlpoolConst;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;

import java.util.List;

public class CahootsWallet {
    private WalletSupplier walletSupplier;
    private BipFormatSupplier bipFormatSupplier;
    private ChainSupplier chainSupplier;
    private CahootsUtxoProvider utxoProvider;

    private HD_Wallet hdWallet;
    private BIP47Wallet bip47Wallet;
    private RpcWallet rpcWallet;

    public CahootsWallet(WalletSupplier walletSupplier, ChainSupplier chainSupplier, BipFormatSupplier bipFormatSupplier, CahootsUtxoProvider utxoProvider) {
        this.walletSupplier = walletSupplier;
        this.chainSupplier = chainSupplier;
        this.bipFormatSupplier = bipFormatSupplier;
        this.utxoProvider = utxoProvider;

        this.hdWallet = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP84).getHdWallet();
        this.bip47Wallet = new BIP47Wallet(hdWallet);
        this.rpcWallet = new RpcWalletImpl(bip47Wallet);
    }

    public BipWallet getReceiveWallet(int account, BipFormat bipFormat) throws Exception {
        if (bipFormat == BIP_FORMAT.TAPROOT) {
            // like-typed output is not implemented for TAPROOT => handle TAPROOT mix output as SEGWIT_NATIVE
            bipFormat = BIP_FORMAT.SEGWIT_NATIVE;
        }
        switch(account) {
            case WhirlpoolConst.WHIRLPOOL_POSTMIX_ACCOUNT:
                return walletSupplier.getWallet(WhirlpoolAccount.POSTMIX, bipFormat);
            case 0:
                return walletSupplier.getWallet(WhirlpoolAccount.DEPOSIT, bipFormat);
        }
        throw new Exception("Invalid account: "+account);
    }

    public BipAddress fetchAddressReceive(int account, boolean increment, BipFormat bipFormat) throws Exception {
        return getReceiveWallet(account, bipFormat).getNextAddress(increment);
    }

    public BipAddress fetchAddressChange(int account, boolean increment, BipFormat bipFormat) throws Exception {
        return getReceiveWallet(account, bipFormat).getNextChangeAddress(increment);
    }

    public BipFormatSupplier getBipFormatSupplier() {
        return bipFormatSupplier;
    }

    public ChainSupplier getChainSupplier() {
        return chainSupplier;
    }

    public RpcWallet getRpcWallet() {
        return rpcWallet;
    }

    public PaymentCode getPaymentCode() {
        return rpcWallet.getPaymentCode();
    }

    public byte[] getFingerprint() {
        return hdWallet.getFingerprint();
    }

    public List<CahootsUtxo> getUtxosWpkhByAccount(int account) {
        return utxoProvider.getUtxosWpkhByAccount(account);
    }
}
