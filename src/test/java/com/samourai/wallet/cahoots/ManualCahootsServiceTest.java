package com.samourai.wallet.cahoots;

import com.samourai.soroban.cahoots.*;
import com.samourai.soroban.client.SorobanInteraction;
import com.samourai.wallet.bipWallet.WalletSupplierImpl;
import com.samourai.wallet.cahoots.multi.MultiCahoots;
import com.samourai.wallet.cahoots.multi.MultiCahootsContext;
import com.samourai.wallet.cahoots.stonewallx2.STONEWALLx2;
import com.samourai.wallet.cahoots.stowaway.Stowaway;
import com.samourai.wallet.client.indexHandler.MemoryIndexHandlerSupplier;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.util.TestUtil;
import org.bitcoinj.core.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class ManualCahootsServiceTest extends AbstractCahootsTest {
    private static final Logger log = LoggerFactory.getLogger(ManualCahootsServiceTest.class);

    private static final String SEED_WORDS = "all all all all all all all all all all all all";
    private static final String SEED_PASSPHRASE_INITIATOR = "initiator";
    private static final String SEED_PASSPHRASE_COUNTERPARTY = "counterparty";

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void Stowaway() throws Exception {
        int account = 0;
        final String[] EXPECTED_PAYLOADS = {
                "{\"cahoots\":{\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[],\"type\":1,\"params\":\"testnet\",\"dest\":\"\",\"version\":2,\"fee_amount\":0,\"fingerprint\":\"eed8a1cd\",\"step\":0,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}",
                "{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":1,\"params\":\"testnet\",\"dest\":\"tb1q9z5slgl572zlc6yl8zg32vndh7tfzltzz3pw8w\",\"version\":2,\"fee_amount\":0,\"fingerprint\":\"eed8a1cd\",\"step\":1,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}",
                "{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"14cf9c6be92efcfe628aabd32b02c85e763615ddd430861bc18f6d366e4c4fd5-1\"},{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":1,\"params\":\"testnet\",\"dest\":\"tb1q9z5slgl572zlc6yl8zg32vndh7tfzltzz3pw8w\",\"version\":2,\"fee_amount\":216,\"fingerprint\":\"eed8a1cd\",\"step\":2,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}",
                "{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"14cf9c6be92efcfe628aabd32b02c85e763615ddd430861bc18f6d366e4c4fd5-1\"},{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":1,\"params\":\"testnet\",\"dest\":\"tb1q9z5slgl572zlc6yl8zg32vndh7tfzltzz3pw8w\",\"version\":2,\"fee_amount\":216,\"fingerprint\":\"eed8a1cd\",\"step\":3,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}",
                "{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"14cf9c6be92efcfe628aabd32b02c85e763615ddd430861bc18f6d366e4c4fd5-1\"},{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":1,\"params\":\"testnet\",\"dest\":\"tb1q9z5slgl572zlc6yl8zg32vndh7tfzltzz3pw8w\",\"version\":2,\"fee_amount\":216,\"fingerprint\":\"eed8a1cd\",\"step\":4,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}"
        };

        // mock sender wallet
        cahootsWalletSender.addUtxo(account, "senderTx1", 1, 10000, "senderAddress1");

        // mock receiver wallet
        cahootsWalletCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "counterpartyAddress1");

        // instanciate sender
        ManualCahootsService cahootsSender = new ManualCahootsService(cahootsWalletSender, stowawayService, stonewallx2Service, multiCahootsService);

        // instanciate receiver
        ManualCahootsService cahootsReceiver = new ManualCahootsService(cahootsWalletCounterparty, stowawayService, stonewallx2Service, multiCahootsService);

        // sender => start Stowaway
        long spendAmount = 5000;
        StowawayContext contextSender = StowawayContext.newInitiator(account, FEE_PER_B, spendAmount);
        ManualCahootsMessage payload0 = cahootsSender.initiate(contextSender);
        verify(EXPECTED_PAYLOADS[0], payload0, false, CahootsType.STOWAWAY, CahootsTypeUser.SENDER);

        // receiver => doStowaway1
        StowawayContext contextReceiver = StowawayContext.newCounterparty(account);
        ManualCahootsMessage payload1 = (ManualCahootsMessage)cahootsReceiver.reply(contextReceiver, payload0);
        verify(EXPECTED_PAYLOADS[1], payload1, false, CahootsType.STOWAWAY, CahootsTypeUser.COUNTERPARTY);

        // sender => doStowaway2
        ManualCahootsMessage payload2 = (ManualCahootsMessage)cahootsSender.reply(contextSender, payload1);
        verify(EXPECTED_PAYLOADS[2], payload2, false, CahootsType.STOWAWAY, CahootsTypeUser.SENDER);

        // receiver => doStowaway3
        ManualCahootsMessage payload3 = (ManualCahootsMessage)cahootsReceiver.reply(contextReceiver, payload2);
        verify(EXPECTED_PAYLOADS[3], payload3, false, CahootsType.STOWAWAY, CahootsTypeUser.COUNTERPARTY);

        // sender => interaction TX_BROADCAST
        TxBroadcastInteraction txBroadcastInteraction = (TxBroadcastInteraction)cahootsSender.reply(contextSender, payload3);
        Assertions.assertEquals(TypeInteraction.TX_BROADCAST, txBroadcastInteraction.getTypeInteraction());

        // sender => doStowaway4
        ManualCahootsMessage payload4 = (ManualCahootsMessage)txBroadcastInteraction.getReplyAccept();
        verify(EXPECTED_PAYLOADS[4], payload4, true, CahootsType.STOWAWAY, CahootsTypeUser.SENDER);
        Cahoots cahoots = payload4.getCahoots();

        // verify TX
        String txid = "5ee43707672e0fdf27c87e398c6f547f33dadef43fa510c2b7e22ab5fc271b85";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff02b0120000000000001600144e4fed51986dbaf322d2b36e690b8638fa0f0204983a00000000000016001428a90fa3f4f285fc689f389115326dbf96917d6202483045022100e1507e5b457b5052488d41c1685bab3f2eb5ef51fc515168d3bbb14c30adf9cf02203c463ceb0394d68a3e4b037fcff91eec0e04e9b7357eb3479d07fa3a6e93ed7e012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f20247304402203ab7cba03ee40b4f4fceb02c5d2735be560dab25eb264e6850b307be3350383d022046a4a23ed073d13a7792a4dbfa22a46ad123b7cabb31fa8e9aa0cdcf1318f6e1012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f200000000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(COUNTERPARTY_RECEIVE_84[0], 15000L);
        outputs.put(SENDER_CHANGE_84[0], 4784L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
    }

    @Test
    public void Stonewallx2() throws Exception {
        int account = 0;
        final String[] EXPECTED_PAYLOADS = {
                "{\"cahoots\":{\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":0,\"fingerprint\":\"eed8a1cd\",\"step\":0,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}",
                "{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":0,\"fingerprint\":\"eed8a1cd\",\"step\":1,\"collabChange\":\"tb1qgzzjhah2q3pqfwpx5xpdrd649qmyl59a6m6cp4\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}",
                "{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"14cf9c6be92efcfe628aabd32b02c85e763615ddd430861bc18f6d366e4c4fd5-1\"},{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":284,\"fingerprint\":\"eed8a1cd\",\"step\":2,\"collabChange\":\"tb1qgzzjhah2q3pqfwpx5xpdrd649qmyl59a6m6cp4\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}",
                "{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"14cf9c6be92efcfe628aabd32b02c85e763615ddd430861bc18f6d366e4c4fd5-1\"},{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":284,\"fingerprint\":\"eed8a1cd\",\"step\":3,\"collabChange\":\"tb1qgzzjhah2q3pqfwpx5xpdrd649qmyl59a6m6cp4\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}",
                "{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"14cf9c6be92efcfe628aabd32b02c85e763615ddd430861bc18f6d366e4c4fd5-1\"},{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":284,\"fingerprint\":\"eed8a1cd\",\"step\":4,\"collabChange\":\"tb1qgzzjhah2q3pqfwpx5xpdrd649qmyl59a6m6cp4\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}"
        };

        // mock sender wallet
        cahootsWalletSender.addUtxo(account, "senderTx1", 1, 10000, "senderAddress1");

        // mock counterparty wallet
        cahootsWalletCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "counterpartyAddress1");

        // instanciate sender
        ManualCahootsService cahootsSender = new ManualCahootsService(cahootsWalletSender, stowawayService, stonewallx2Service, multiCahootsService);

        // instanciate counterparty
        ManualCahootsService cahootsCounterparty = new ManualCahootsService(cahootsWalletCounterparty, stowawayService, stonewallx2Service, multiCahootsService);

        // sender => start Stonewallx2
        long spendAmount = 5000;
        String address = ADDRESS_BIP84;
        Stonewallx2Context contextSender = Stonewallx2Context.newInitiator(account, FEE_PER_B, spendAmount, address, null);
        ManualCahootsMessage payload0 = cahootsSender.initiate(contextSender);
        verify(EXPECTED_PAYLOADS[0], payload0, false, CahootsType.STONEWALLX2, CahootsTypeUser.SENDER);

        // counterparty => doSTONEWALLx2_1
        Stonewallx2Context contextCounterparty = Stonewallx2Context.newCounterparty(account);
        ManualCahootsMessage payload1 = (ManualCahootsMessage)cahootsCounterparty.reply(contextCounterparty, payload0);
        verify(EXPECTED_PAYLOADS[1], payload1, false, CahootsType.STONEWALLX2, CahootsTypeUser.COUNTERPARTY);

        // sender => doSTONEWALLx2_2
        ManualCahootsMessage payload2 = (ManualCahootsMessage)cahootsSender.reply(contextSender, payload1);
        verify(EXPECTED_PAYLOADS[2], payload2, false, CahootsType.STONEWALLX2, CahootsTypeUser.SENDER);

        // counterparty => doSTONEWALLx2_3
        ManualCahootsMessage payload3 = (ManualCahootsMessage)cahootsCounterparty.reply(contextCounterparty, payload2);
        verify(EXPECTED_PAYLOADS[3], payload3, false, CahootsType.STONEWALLX2, CahootsTypeUser.COUNTERPARTY);

        // sender => interaction TX_BROADCAST
        SorobanInteraction payload4Interaction = (SorobanInteraction)cahootsSender.reply(contextSender, payload3);

        // sender => doSTONEWALLx2_4
        ManualCahootsMessage payload4 = (ManualCahootsMessage)payload4Interaction.getReplyAccept();
        verify(EXPECTED_PAYLOADS[4], payload4, true, CahootsType.STONEWALLX2, CahootsTypeUser.SENDER);
        Cahoots cahoots = payload4.getCahoots();

        // verify TX
        String txid = "44ad31cb9cd68283bc746ccf8b88e02a2a617fb8f45aba0e0f35f4eaa5c5686b";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff04fa1200000000000016001440852bf6ea044204b826a182d1b75528364fd0bdfa120000000000001600144e4fed51986dbaf322d2b36e690b8638fa0f020488130000000000001600142ecf8c3e5697f0513999ed3aa8ea9cd04d1273558813000000000000160014657b6afdeef6809fdabce7face295632fbd94feb024730440220041c9f5e3810d77aee65d6fab99d325b67a8f8435548eed48d2809779b5da1ea02204819c0fc77ae2e4bbb091effbccd7e7987adf4feac04a9ab5751d0f72260cea5012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f202483045022100df91fdcffa6dd5e7139a319b8ac5beec98e32ad1625073596654acc9d2ea6cad02202c607842ff05652814e963627a47d2e3d819c8f8772f362d2b6bda4a84a5aeab012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f200000000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(address, spendAmount);
        outputs.put(COUNTERPARTY_CHANGE_84[0], spendAmount); // counterparty mix
        outputs.put(COUNTERPARTY_CHANGE_84[1], 4858L);
        outputs.put(SENDER_CHANGE_84[0], 4858L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
    }

    @Test
    public void multiCahoots() throws Exception {
        int account = 0;
        final String[] EXPECTED_PAYLOADS = {
                "{\"cahoots\":{\"step\":0,\"type\":2,\"params\":\"testnet\",\"version\":2},\"stonewallx2\":{\"cahoots\":{\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":0,\"fingerprint\":\"eed8a1cd\",\"step\":0,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}},\"stowaway\":{\"cahoots\":{\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":-1,\"outpoints\":[],\"type\":1,\"params\":\"testnet\",\"dest\":\"\",\"version\":2,\"fee_amount\":0,\"fingerprint\":\"eed8a1cd\",\"step\":0,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}}",
                "{\"cahoots\":{\"step\":1,\"type\":2,\"params\":\"testnet\",\"version\":2},\"stonewallx2\":{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":0,\"fingerprint\":\"eed8a1cd\",\"step\":1,\"collabChange\":\"tb1qgzzjhah2q3pqfwpx5xpdrd649qmyl59a6m6cp4\",\"id\":\"testID\",\"account\":0,\"ts\":123456}},\"stowaway\":{\"cahoots\":{\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":-1,\"outpoints\":[],\"type\":1,\"params\":\"testnet\",\"dest\":\"\",\"version\":2,\"fee_amount\":0,\"fingerprint\":\"eed8a1cd\",\"step\":0,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}}",
                "{\"cahoots\":{\"step\":2,\"type\":2,\"params\":\"testnet\",\"version\":2},\"stonewallx2\":{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"14cf9c6be92efcfe628aabd32b02c85e763615ddd430861bc18f6d366e4c4fd5-1\"},{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":284,\"fingerprint\":\"eed8a1cd\",\"step\":2,\"collabChange\":\"tb1qgzzjhah2q3pqfwpx5xpdrd649qmyl59a6m6cp4\",\"id\":\"testID\",\"account\":0,\"ts\":123456}},\"stowaway\":{\"cahoots\":{\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":-1,\"outpoints\":[],\"type\":1,\"params\":\"testnet\",\"dest\":\"\",\"version\":2,\"fee_amount\":0,\"fingerprint\":\"eed8a1cd\",\"step\":0,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}}",
                "{\"cahoots\":{\"step\":3,\"type\":2,\"params\":\"testnet\",\"version\":2},\"stonewallx2\":{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"14cf9c6be92efcfe628aabd32b02c85e763615ddd430861bc18f6d366e4c4fd5-1\"},{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":284,\"fingerprint\":\"eed8a1cd\",\"step\":3,\"collabChange\":\"tb1qgzzjhah2q3pqfwpx5xpdrd649qmyl59a6m6cp4\",\"id\":\"testID\",\"account\":0,\"ts\":123456}},\"stowaway\":{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":317,\"outpoints\":[{\"value\":9000,\"outpoint\":\"b4b65c3246da72537c93591919f8479f7a2b232f4dafec55b9e01d34e47fde26-1\"}],\"type\":1,\"params\":\"testnet\",\"dest\":\"tb1q9z5slgl572zlc6yl8zg32vndh7tfzltzz3pw8w\",\"version\":2,\"fee_amount\":0,\"fingerprint\":\"eed8a1cd\",\"step\":1,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}}",
                "{\"cahoots\":{\"step\":4,\"type\":2,\"params\":\"testnet\",\"version\":2},\"stonewallx2\":{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"14cf9c6be92efcfe628aabd32b02c85e763615ddd430861bc18f6d366e4c4fd5-1\"},{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":284,\"fingerprint\":\"eed8a1cd\",\"step\":4,\"collabChange\":\"tb1qgzzjhah2q3pqfwpx5xpdrd649qmyl59a6m6cp4\",\"id\":\"testID\",\"account\":0,\"ts\":123456}},\"stowaway\":{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":317,\"outpoints\":[{\"value\":9000,\"outpoint\":\"b4b65c3246da72537c93591919f8479f7a2b232f4dafec55b9e01d34e47fde26-1\"},{\"value\":8000,\"outpoint\":\"7a1a2c7732f7a4e4dcb077286b61b36be99baf97168fd93ef862283c77acb03e-1\"}],\"type\":1,\"params\":\"testnet\",\"dest\":\"tb1q9z5slgl572zlc6yl8zg32vndh7tfzltzz3pw8w\",\"version\":2,\"fee_amount\":216,\"fingerprint\":\"eed8a1cd\",\"step\":2,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}}",
                "{\"cahoots\":{\"step\":5,\"type\":2,\"params\":\"testnet\",\"version\":2},\"stonewallx2\":{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"destPaynym\":\"\",\"cpty_account\":0,\"spend_amount\":5000,\"outpoints\":[{\"value\":10000,\"outpoint\":\"14cf9c6be92efcfe628aabd32b02c85e763615ddd430861bc18f6d366e4c4fd5-1\"},{\"value\":10000,\"outpoint\":\"9407b31fd0159dc4dd3f5377e3b18e4b4aafef2977a52e76b95c3f899cbb05ad-1\"}],\"type\":0,\"params\":\"testnet\",\"dest\":\"tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4\",\"version\":2,\"fee_amount\":284,\"fingerprint\":\"eed8a1cd\",\"step\":0,\"collabChange\":\"tb1qgzzjhah2q3pqfwpx5xpdrd649qmyl59a6m6cp4\",\"id\":\"testID\",\"account\":0,\"ts\":123456}},\"stowaway\":{\"cahoots\":{\"fingerprint_collab\":\"f0d70870\",\"psbt\":\"\",\"cpty_account\":0,\"spend_amount\":317,\"outpoints\":[{\"value\":9000,\"outpoint\":\"b4b65c3246da72537c93591919f8479f7a2b232f4dafec55b9e01d34e47fde26-1\"},{\"value\":8000,\"outpoint\":\"7a1a2c7732f7a4e4dcb077286b61b36be99baf97168fd93ef862283c77acb03e-1\"}],\"type\":1,\"params\":\"testnet\",\"dest\":\"tb1q9z5slgl572zlc6yl8zg32vndh7tfzltzz3pw8w\",\"version\":2,\"fee_amount\":216,\"fingerprint\":\"eed8a1cd\",\"step\":3,\"collabChange\":\"\",\"id\":\"testID\",\"account\":0,\"ts\":123456}}}",
        };

        // mock sender wallet
        final HD_Wallet bip84WalletSender = TestUtil.computeBip84wallet(SEED_WORDS, SEED_PASSPHRASE_INITIATOR);
        TestCahootsWallet cahootsWalletSender = new TestCahootsWallet(new WalletSupplierImpl(new MemoryIndexHandlerSupplier(), bip84WalletSender), bipFormatSupplier, params);
        cahootsWalletSender.addUtxo(account, "senderTx1", 1, 10000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");
        cahootsWalletSender.addUtxo(account, "senderTx2", 1, 8000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");

        // mock receiver wallet
        final HD_Wallet bip84WalletCounterparty = TestUtil.computeBip84wallet(SEED_WORDS, SEED_PASSPHRASE_COUNTERPARTY);
        TestCahootsWallet cahootsWalletCounterparty = new TestCahootsWallet(new WalletSupplierImpl(new MemoryIndexHandlerSupplier(), bip84WalletCounterparty), bipFormatSupplier, params);
        cahootsWalletCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");
        cahootsWalletCounterparty.addUtxo(account, "counterpartyTx2", 1, 9000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");

        // instanciate sender
        ManualCahootsService cahootsSender = new ManualCahootsService(cahootsWalletSender, stowawayService, stonewallx2Service, multiCahootsService);

        // instanciate receiver
        ManualCahootsService cahootsReceiver = new ManualCahootsService(cahootsWalletCounterparty, stowawayService, stonewallx2Service, multiCahootsService);

        // sender => start multiCahoots
        long spendAmount = 5000;
        String address = ADDRESS_BIP84;
        MultiCahootsContext contextSender = MultiCahootsContext.newInitiator(account, FEE_PER_B, spendAmount, address, null);
        ManualCahootsMessage payload0 = cahootsSender.initiate(contextSender);
        verify(EXPECTED_PAYLOADS[0], payload0, false, CahootsType.MULTI, CahootsTypeUser.SENDER);

        // counterparty
        MultiCahootsContext contextReceiver = MultiCahootsContext.newCounterparty(account);
        ManualCahootsMessage payload1 = (ManualCahootsMessage)cahootsReceiver.reply(contextReceiver, payload0);
        verify(EXPECTED_PAYLOADS[1], payload1, false, CahootsType.MULTI, CahootsTypeUser.COUNTERPARTY);

        // sender
        ManualCahootsMessage payload2 = (ManualCahootsMessage)cahootsSender.reply(contextSender, payload1);
        verify(EXPECTED_PAYLOADS[2], payload2, false, CahootsType.MULTI, CahootsTypeUser.SENDER);

        // counterparty
        ManualCahootsMessage payload3 = (ManualCahootsMessage)cahootsReceiver.reply(contextReceiver, payload2);
        verify(EXPECTED_PAYLOADS[3], payload3, false, CahootsType.MULTI, CahootsTypeUser.COUNTERPARTY);

        // sender
        ManualCahootsMessage payload4 = (ManualCahootsMessage)cahootsSender.reply(contextSender, payload3);
        verify(EXPECTED_PAYLOADS[4], payload4, false, CahootsType.MULTI, CahootsTypeUser.SENDER);

        // counterparty
        ManualCahootsMessage payload5 = (ManualCahootsMessage)cahootsReceiver.reply(contextReceiver, payload4);
        verify(EXPECTED_PAYLOADS[5], payload5, false, CahootsType.MULTI, CahootsTypeUser.COUNTERPARTY);

        // sender => interaction TX_BROADCAST
        TxBroadcastInteraction txBroadcastInteraction = (TxBroadcastInteraction)cahootsSender.reply(contextSender, payload5);
        Assertions.assertEquals(TypeInteraction.TX_BROADCAST_MULTI, txBroadcastInteraction.getTypeInteraction());

        Cahoots cahoots = payload5.getCahoots();

        // verify stonewallx2
        {
            Transaction tx = ((MultiCahoots)cahoots).getStonewallTransaction();
            String txid = "44ad31cb9cd68283bc746ccf8b88e02a2a617fb8f45aba0e0f35f4eaa5c5686b";
            String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff04fa1200000000000016001440852bf6ea044204b826a182d1b75528364fd0bdfa120000000000001600144e4fed51986dbaf322d2b36e690b8638fa0f020488130000000000001600142ecf8c3e5697f0513999ed3aa8ea9cd04d1273558813000000000000160014657b6afdeef6809fdabce7face295632fbd94feb024730440220041c9f5e3810d77aee65d6fab99d325b67a8f8435548eed48d2809779b5da1ea02204819c0fc77ae2e4bbb091effbccd7e7987adf4feac04a9ab5751d0f72260cea5012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f202483045022100df91fdcffa6dd5e7139a319b8ac5beec98e32ad1625073596654acc9d2ea6cad02202c607842ff05652814e963627a47d2e3d819c8f8772f362d2b6bda4a84a5aeab012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f200000000";
            Map<String,Long> outputs = new LinkedHashMap<>();
            outputs.put(address, spendAmount);
            outputs.put(COUNTERPARTY_CHANGE_84[0], spendAmount); // counterparty mix
            outputs.put(COUNTERPARTY_CHANGE_84[1], 4858L);
            outputs.put(SENDER_CHANGE_84[0], 4858L);
            verifyTx(tx, txid, raw, outputs);
        }

        // verify stowaway
        {
            Transaction tx = ((MultiCahoots)cahoots).getStowawayTransaction();
            String txid = "fd918da2c2fb00b2aa071fa0e12afd8f9daad4427babe261d96ed35946f3c237";
            String raw = "020000000001023eb0ac773c2862f83ed98f1697af9be96bb3616b2877b0dce4a4f732772c1a7a0100000000fdffffff26de7fe4341de0b955ecaf4d2f232b7a9f47f8191959937c5372da46325cb6b40100000000fdffffff022b1d00000000000016001485963b79fea38b84ce818e5f29a5a115bd4c8229652400000000000016001428a90fa3f4f285fc689f389115326dbf96917d620247304402201f4ccc9f8ae4b6202d18fe9bf1f1f7847528b9d618c6daceba87d6400c09716c0220012b04f2a2860c4e58e1928c890f5ad3658fb10694b18265cfb1f6fcc42c9270012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f20247304402205612ca04146d2b10e6184c4a72bb20c645529d2e2cf88ab546516ecda8aa25eb02203dce007a19c3d93b7339331666ca683e5d7eab097360ed8f9bf2d8de70d31e6e012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f200000000";
            Map<String,Long> outputs = new LinkedHashMap<>();
            outputs.put(COUNTERPARTY_RECEIVE_84[0], 9317L);
            outputs.put(SENDER_CHANGE_84[1], 7467L);
            verifyTx(tx, txid, raw, outputs);
        }
    }
}
