package com.samourai.wallet.cahoots.psbt;


import com.fasterxml.jackson.databind.ser.Serializers;
import com.samourai.wallet.cahoots.Cahoots;
import com.samourai.wallet.cahoots.psbt.PSBTEntry;
import com.samourai.wallet.cahoots.psbt.PSBT;
import com.samourai.wallet.segwit.SegwitAddress;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bouncycastle.util.encoders.Hex;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class PSBTTest {

    private static String strPSBT = "70736274 FF01009A 02000000 0232A139 2B4777AA AE109030 4246D564 E99D68EB E3D35F12 A77FBAE7 281D0A93 E1010000 0000FDFF FFFF67D3 8F936062 1FF49887 38FEB31D DEE2E618 2CFCDD4A B68721F4 D49CADBA 82EE0100 000000FD FFFFFF02 C222480A 00000000 16001498 79AF465E D7CF0054 276217D2 E9130767 F6369100 4B26A039 00000016 00149032 58C94687 540B468C 3763F348 941FAB40 7AF9A777 19000001 011FC049 6E0A0000 00001600 1407AF7C FEC74560 0DA9BFFE BC6A1DB2 9D42EA9A CE220602 F6017FE8 A221D141 CE5FE7FB D3636F59 45434246 B5A02CC4 CFC4B58B B3B2C3FC 180D8C85 AB540000 80010000 80000000 80000000 00080000 00000101 1FD32400 A0390000 00160014 C583F828 737798EE 6AB85B1D 919339BE BEC95CD0 22060215 5F483277 2ADB0D18 CFFBE94A 8A97BCB1 D952C68A B1992E94 808DA7AE 1A8CB018 0D8C85AB 54000080 01000080 00000080 01000000 04000000 00220202 F45169DB 34C54A85 F36BB454 BF0782D6 D0B0E8EC 6B8E2C4B 973AB6D5 8553A202 180D8C85 AB540000 80010000 80000000 80010000 00050000 000000";
    private static String strTx = "020000000232a1392b4777aaae1090304246d564e99d68ebe3d35f12a77fbae7281d0a93e10100000000fdffffff67d38f9360621ff4988738feb31ddee2e6182cfcdd4ab68721f4d49cadba82ee0100000000fdffffff02c222480a000000001600149879af465ed7cf0054276217d2e9130767f63691004b26a039000000160014903258c94687540b468c3763f348941fab407af9a7771900";

    private static String psbtSegwit = "70736274ff0100520200000001969240debf27b952ec4fc4b57633d33e259137aca55074bc67dfe9b7094839551b00000000fdffffff0134fe0f0000000000160014da4b15b9f909907f40ff63ea8e138077df4eba67a46722004f01043587cf03f5de08c280000000ea045c994bf39951b514fbbca2dc028cf11b97c5e2ef63a8f7ccc6b54a69c6cd0230b09ad5f51525c2809429727b0730aaee7e540f6b0d567debc677619f3de5711000000000540000800000008000000080000100fdc3030100000001dd164d8e361c0dacffc7d58d4d2cb19d1573646021a16af7e8c8f08c5567f09f0100000000ffffffff1c0000000000000000426a40f4542d0a644f5fd2198bef7bb3335cf3cbf0b8ad6a92a7b3e64f84cdb4c0c5ecec3b5bf34d47a35b49ef755cba4b656aa3570e09f032c9b0e1bb21ed77892e0388130000000000001600149cc9e8ca509efd15cf7f0e526b577c86a55d1970ce870100000000001600140ff1f43735f013abd4df16bb886dce1d7191ae61ce870100000000001600141987d2e5f4568e48e1a6cc2b981644e260e51fa9ce8701000000000016001420312683c856b02df4aa34a9d6336e55eab6b310ce87010000000000160014234d003d3e0551825a2de5aa6c6c40ec4580a158ce8701000000000016001423530475c41076a467d97c8586bf1ddbbef254ccce87010000000000160014351f639ca3a42e7d79f41ea3b45996f3db89106ece87010000000000160014440c857aa3623ebee26b4b831b87aa2b67e8d638ce870100000000001600144856a0afbf6f032a7d89de6b52a7ef2a065469cbce870100000000001600144bb7618b164e4691c69b63498275ba49fa0008efce87010000000000160014520669a0a104a7225221da6ea56592d0c59d9964ce870100000000001600145b2fce2b4af38d0103c6939602f2ab1ee18b0818ce870100000000001600145b77cc3e42e5d3a02302a54f8a150ad5fc8dcb04ce870100000000001600146773baf6b26ef3ce8db08e395ebe1ea873f6309cce870100000000001600146c7309b580160ab40ebce0d3f7cdab5ec6c8378ece870100000000001600147b7aa2e2bc814213200b5a83c41f25fd0e885c1ece870100000000001600148024283716b1a47490ae4b3e0def9f007d2bf3f9ce870100000000001600148d2c89a4dec79f8bfdbfddc03dbcef97f93b6375ce87010000000000160014975dcf4e2e0bf90dbdf4c637713d0b1a7aa9d2d7ce87010000000000160014af6c98a61a9ae4b969ffb2a774778dedf7114e27ce87010000000000160014b0b8ade4dc34ee76c1348ae7418cecc96ff23ab4ce87010000000000160014c8daa39ce3f7ace57889c764dbdb142ce55cd7a7ce87010000000000160014d45fada6b99e4b0408f22b8e0a9db99a88abb600ce87010000000000160014e37f7ed3944eeea1414504e31eccd6214f5188d3ce87010000000000160014ec1d18827a6fba381ebf5e494c8290725a0f8dfece87010000000000160014ecfd4cf837fde6eaa545cf941e059c27f3436b25a2fe0f000000000016001419412efe1be24aff0374a1da7a51c74ea293d31c0000000001011fa2fe0f000000000016001419412efe1be24aff0374a1da7a51c74ea293d31c01030401000000220603f82721000c1f104a8a41052ee3c15a8c17a17c6c29011aa6756d50c78deb68a918000000005400008000000080000000800100000020000000002202034fdb1fcc5396d683398345421247464d97b178141a2e9a51037aaca5d806dbcb1800000000540000800000008000000080000000002400000000";
    private static String txHexSegwit = "0200000001969240debf27b952ec4fc4b57633d33e259137aca55074bc67dfe9b7094839551b00000000fdffffff0134fe0f0000000000160014da4b15b9f909907f40ff63ea8e138077df4eba67a4672200";

    private static String psbtLegacy = "70736274ff0100550200000001bcb6bdf6b8ddfbc0f47e445588fc5be1a4d3252c153917161dfc7159b537121b0000000000fdffffff0115c20000000000001976a914eb8a0e37824a4d3ce32a3795eceb7811dfbc856c88ac304d22004f01043587cf035b3e59ba80000000e1eb03a68ea5d8c51d5807c64f867848e77fc187485b7decd2dd1c44f38a1e8602130a2ea8a1411d00725444e482258703aae3a445362ca4862317f90998bc4b2610000000002c00008000000080000000000001006c0200000001fb6ccee6bdc232ebf955cf322712d158fee1dba7c91b164400b64cc194a6e7610000000017160014535146a98ec54db5d9751415e41c135164f66206ffffffff0116c20000000000001976a9140d67958e3d6404d7a5bdea1c3c5f88c54e85996a88ac0000000001030401000000220602414469f58cda44a285963ac5c387d907c27bc56621cf1184bfc35c74b58e626c18000000002c0000800000008000000000000000000400000000220202073b1f1714bcdaf3da6518c5563fbc5889d676a5112b7cba5bd5cf10d71acdc518000000002c0000800000008000000000000000000500000000";
    private static String txHexLegacy = "0200000001bcb6bdf6b8ddfbc0f47e445588fc5be1a4d3252c153917161dfc7159b537121b0000000000fdffffff0115c20000000000001976a914eb8a0e37824a4d3ce32a3795eceb7811dfbc856c88ac304d2200";

    private static String psbtTaproot = "70736274ff01005e0200000001146935b57b36d17cdbbdb3c412f72244ac25614b2a7b9dc370b61088f5a811c501000000000000000001308601000000000022512080e2d9c811ad041953e25524bf7eb3cd6541b7bc3804b83eeb44ac49da3aef15000000004f01043587cf03be997f388000000051dd0cc3bdc3ab2f5f0397b6e7ea2df9c2b8cfaade859ae431d795b33db00f9c03ed10791895a6ecc155dd969b78b212b9f31e42a11eca37a167edf54638360ef9103927daee5600008001000080000000800001007d0200000001056e30a45ffd7b0e5ec724ebd4f67583eb1a38688b32b2b31cbb00430fc2556a0000000000feffffff0244366a6e000000001600145791c4c80ec1cdc287643b2b683d4493f35a96cba0860100000000002251206b5cbd16adef9c4ea9b0ae02d75e12cce463509ac1af48d17958995f1a0082e3a767220001012ba0860100000000002251206b5cbd16adef9c4ea9b0ae02d75e12cce463509ac1af48d17958995f1a0082e3010304000000002206036b5cbd16adef9c4ea9b0ae02d75e12cce463509ac1af48d17958995f1a0082e3183927daee560000800100008000000080000000000000000021165e6861ac2a08ee1e7b7e57379478caef2b4227175359e8bd3df090118e1619c019003927daee56000080010000800000008000000000000000000117205e6861ac2a08ee1e7b7e57379478caef2b4227175359e8bd3df090118e1619c0002202023762943fa4da61f1c291331e9f6f71e5246b2fa0f0f55e2be69452b0601c97c2183927daee560000800100008000000080000000000100000000";
    private static String txHexTaproot = "0200000001146935b57b36d17cdbbdb3c412f72244ac25614b2a7b9dc370b61088f5a811c501000000000000000001308601000000000022512080e2d9c811ad041953e25524bf7eb3cd6541b7bc3804b83eeb44ac49da3aef1500000000";

    private static String psbtCompat = "70736274ff01007c0200000002a4373954617572ff7e2dd93397df266156f14f82cb55f054c7df73f753f10e730000000000fdffffffa4373954617572ff7e2dd93397df266156f14f82cb55f054c7df73f753f10e730100000000fdffffff01e7af02000000000017a914d4134e65e4aa0600493e99163e270e2047ea130a87da6522004f01043587cf039f4d230c80000000183901ebd4ad87386cba6bb6fefab51d3621965d2578eb05b72aa391fb94e85f032e034599e5610dad1f43fd8ad34956c689da0bf7d5982f1eb976a601e8a56924101fe613363100008001000080000000800001008a0200000001046353dcb7b58eccdd5fef1753ceb5656a20682224342302994f4d022dc2f41a0000000017160014b1e865611857028bd723cedba5cedd0a3e22cc4afdffffff02803801000000000017a9147d3928df049a17ef11eaf0a6c7172c137b154ca887487801000000000017a914a130db58c5cfde47f1bdc61b82a43a0c9b0381278739f82100010120803801000000000017a9147d3928df049a17ef11eaf0a6c7172c137b154ca887010304010000000104160014ece2429798b4ab644df5c7b971066cc9a11b31c8220603af8c2da87a89c94f7aa732ec5f26cd0f161b82ab33f6fc641ee93ca0341f7f84181fe6133631000080010000800000008000000000010000000001008a0200000001046353dcb7b58eccdd5fef1753ceb5656a20682224342302994f4d022dc2f41a0000000017160014b1e865611857028bd723cedba5cedd0a3e22cc4afdffffff02803801000000000017a9147d3928df049a17ef11eaf0a6c7172c137b154ca887487801000000000017a914a130db58c5cfde47f1bdc61b82a43a0c9b0381278739f82100010120487801000000000017a914a130db58c5cfde47f1bdc61b82a43a0c9b03812787010304010000000104160014b58186f855e680b2f54c7d493e18e95f0b40b2442206031896542bebf66ccb38b841988dfb2e0ab640c87ab0f2ae9a40dc675b4b286cd8181fe613363100008001000080000000800100000000000000000100160014982ed864d74e3c6fb03c188d99875129bb253dfd220203fe2f409cc4536f6c79d6d61e4a7135858fea122d99e71ded0e913d68722739d2181fe61336310000800100008000000080000000000200000000";
    private static String txHexCompat = "0200000002a4373954617572ff7e2dd93397df266156f14f82cb55f054c7df73f753f10e730000000000fdffffffa4373954617572ff7e2dd93397df266156f14f82cb55f054c7df73f753f10e730100000000fdffffff01e7af02000000000017a914d4134e65e4aa0600493e99163e270e2047ea130a87da652200";
    @Test
    public void testParse() {

        try {

            PSBT.setDebug(true);
            PSBT psbtIn = PSBT.fromBytes(Hex.decode(strPSBT.replaceAll(" ", "")), TestNet3Params.get());
            PSBT.setDebug(false);

            Assertions.assertTrue(psbtIn.isParseOK());
        }
        catch(Exception e) {
            Assertions.assertTrue(false);
        }

    }

    @Test
    public void testParseAndInput() {

        try {

            PSBT psbtIn = PSBT.fromBytes(Hex.decode(strPSBT.replaceAll(" ", "")), TestNet3Params.get());

            Transaction tx = psbtIn.getTransaction();

            PSBT psbtOut = new PSBT(tx);
            psbtOut.setPsbtInputs(psbtIn.getPsbtInputs());
            psbtOut.setPsbtOutputs(psbtIn.getPsbtOutputs());
            byte[] psbtOutBuf = psbtOut.toBytes();

            System.out.println(Hex.toHexString(psbtOutBuf));
            System.out.println(strPSBT.replaceAll(" ", ""));
            Assertions.assertTrue(org.bouncycastle.util.encoders.Hex.toHexString(psbtOutBuf).toLowerCase().equals(strPSBT.replaceAll(" ", "").toLowerCase()));

        }
        catch(Exception e) {
            Assertions.assertTrue(false);
        }

    }

    @Test
    public void testInputsAndOutputsCounts() {

        try {

            PSBT psbtIn = PSBT.fromBytes(Hex.decode(strPSBT.replaceAll(" ", "")), TestNet3Params.get());

            Assertions.assertTrue(psbtIn.getTransaction().getInputs().size() == 2);
            Assertions.assertTrue(psbtIn.getTransaction().getOutputs().size() == 2);

            Assertions.assertTrue(psbtIn.getInputCount() == 2);
            Assertions.assertTrue(psbtIn.getOutputCount() == 2);

        }
        catch(Exception e) {
            Assertions.assertTrue(false);
        }

    }

    @Test
    public void testInputs() {

        try {

            PSBT psbtIn = PSBT.fromBytes(Hex.decode(strPSBT.replaceAll(" ", "")), TestNet3Params.get());

            PSBTEntry testEntry = null;
            for(PSBTEntry entry : psbtIn.getPsbtInputs()) {

                if(entry.getKey() == null) {
                    continue;
                }

                if(org.bouncycastle.util.encoders.Hex.toHexString(entry.getKeyType()).equals("01")) {
                    byte[] data = entry.getData();
                    byte[] amount = new byte[8];
                    byte[] scriptpubkey = new byte[data.length - 8];
                    System.arraycopy(data, 0, amount, 0, 8);
                    System.arraycopy(data, 8, scriptpubkey, 0, data.length - 8);
                    ByteBuffer bb = ByteBuffer.wrap(amount);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    Assertions.assertTrue(175000000L == bb.getLong());
                    Assertions.assertTrue("16001407af7cfec745600da9bffebc6a1db29d42ea9ace".equalsIgnoreCase(org.bouncycastle.util.encoders.Hex.toHexString(scriptpubkey)));
                }
                else if(org.bouncycastle.util.encoders.Hex.toHexString(entry.getKeyType()).equals("06")) {
                    byte[] keydata = entry.getKeyData();
                    Assertions.assertTrue("tb1qq7hhelk8g4sqm2dll67x58djn4pw4xkwx040qg".equals(new SegwitAddress(keydata, TestNet3Params.get()).getBech32AsString()));
                    break;
                }

            }

        }
        catch(Exception e) {
            Assertions.assertTrue(false);
        }

    }

    @Test
    public void testOutputs() {

        try {

            PSBT psbtIn = PSBT.fromBytes(Hex.decode(strPSBT.replaceAll(" ", "")), TestNet3Params.get());

            PSBTEntry testEntry = null;
            for(PSBTEntry entry : psbtIn.getPsbtOutputs()) {

                if(entry.getKey() == null) {
                    continue;
                }

                if(org.bouncycastle.util.encoders.Hex.toHexString(entry.getKeyType()).equals("02")) {
                    testEntry = entry;
                    break;
                }

            }

            if(testEntry != null) {
                PSBTEntry entry = testEntry;

                byte[] data = entry.getData();
                byte[] fp = new byte[4];
                System.arraycopy(data, 0, fp, 0, 4);
                // fingerprint
                Assertions.assertTrue("0d8c85ab".equalsIgnoreCase(Hex.toHexString(fp)));
                Assertions.assertTrue(data.length == 24);
                int nb = data.length / 4;
                for(int i = 1; i < nb; i++ ) {
                    byte[] segment = new byte[4];
                    System.arraycopy(data, i * 4, segment, 0, 4);
                    switch(i) {
                        case 1:
                            Assertions.assertTrue("54000080".equals(org.bouncycastle.util.encoders.Hex.toHexString(segment)));
                            break;
                        case 2:
                            Assertions.assertTrue("01000080".equals(org.bouncycastle.util.encoders.Hex.toHexString(segment)));
                            break;
                        case 3:
                            Assertions.assertTrue("00000080".equals(org.bouncycastle.util.encoders.Hex.toHexString(segment)));
                            break;
                        case 4:
                            Assertions.assertTrue("01000000".equals(org.bouncycastle.util.encoders.Hex.toHexString(segment)));
                            break;
                        case 5:
                            Assertions.assertTrue("05000000".equals(org.bouncycastle.util.encoders.Hex.toHexString(segment)));
                            break;
                        default:
                            Assertions.assertTrue(false);
                            break;
                    }
                }
                byte[] keydata = entry.getKeyData();
                Assertions.assertTrue("tb1qnpu673j76l8sq4p8vgta96gnqanlvd53vhsk5j".equals(new SegwitAddress(keydata, TestNet3Params.get()).getBech32AsString()));
            }
            else {
                Assertions.assertTrue(false);
            }

        }
        catch(Exception e) {
            Assertions.assertTrue(false);
        }

    }

    @Test
    public void testRawWrite() {

        try {

            Transaction tx = new Transaction(TestNet3Params.get(), Hex.decode(strTx));

            PSBT psbt = new PSBT(tx);

            psbt.addInput((byte)0x01, null, Hex.decode("c0496e0a0000000016001407af7cfec745600da9bffebc6a1db29d42ea9ace"));
            psbt.addInput((byte)0x06, Hex.decode("02f6017fe8a221d141ce5fe7fbd3636f5945434246b5a02cc4cfc4b58bb3b2c3fc"), Hex.decode("0d8c85ab5400008001000080000000800000000008000000"));
            psbt.addInputSeparator();

            psbt.addInput((byte)0x01, null, Hex.decode("d32400a039000000160014c583f828737798ee6ab85b1d919339bebec95cd0"));
            psbt.addInput((byte)0x06, Hex.decode("02155f4832772adb0d18cffbe94a8a97bcb1d952c68ab1992e94808da7ae1a8cb0"), Hex.decode("0d8c85ab5400008001000080000000800100000004000000"));
            psbt.addInputSeparator();

            psbt.addOutput((byte)0x02, Hex.decode("02f45169db34c54a85f36bb454bf0782d6d0b0e8ec6b8e2c4b973ab6d58553a202"), Hex.decode("0d8c85ab5400008001000080000000800100000005000000"));
            psbt.addOutputSeparator();
            psbt.addOutputSeparator();

            byte[] psbtOutBuf = psbt.toBytes();
            Assertions.assertTrue(org.bouncycastle.util.encoders.Hex.toHexString(psbtOutBuf).toLowerCase().equals(strPSBT.replaceAll(" ", "").toLowerCase()));

        }
        catch(Exception e) {
            Assertions.assertTrue(false);
        }

    }

    @Test
    public void testWalletWrite() {

        ECKey eckeyInput0 = ECKey.fromPublicOnly(Hex.decode("02f6017fe8a221d141ce5fe7fbd3636f5945434246b5a02cc4cfc4b58bb3b2c3fc"));
        ECKey eckeyInput1 = ECKey.fromPublicOnly(Hex.decode("02155f4832772adb0d18cffbe94a8a97bcb1d952c68ab1992e94808da7ae1a8cb0"));
        ECKey eckeyOutput0 = ECKey.fromPublicOnly(Hex.decode("02f45169db34c54a85f36bb454bf0782d6d0b0e8ec6b8e2c4b973ab6d58553a202"));

        try {

            Transaction tx = new Transaction(TestNet3Params.get(), Hex.decode(strTx));

            PSBT psbt = new PSBT(tx);

            psbt.addInput(TestNet3Params.get(), Hex.decode("0d8c85ab"), eckeyInput0, 175000000L, 84, 1, 0, 0, 8);
            psbt.addInput(TestNet3Params.get(), Hex.decode("0d8c85ab"), eckeyInput1, 247497499859L, 84, 1, 0, 1, 4);

            psbt.addOutput(TestNet3Params.get(), Hex.decode("0d8c85ab"), eckeyOutput0, 84, 1, 0, 1, 5);
            // add trailing separator
            psbt.addOutputSeparator();

            byte[] psbtOutBuf = psbt.toBytes();

            System.out.println(org.bouncycastle.util.encoders.Hex.toHexString(psbtOutBuf));
            System.out.println(strPSBT.replaceAll(" ", "").toLowerCase());

            Assertions.assertTrue(org.bouncycastle.util.encoders.Hex.toHexString(psbtOutBuf).toLowerCase().equals(strPSBT.replaceAll(" ", "").toLowerCase()));

        }
        catch(Exception e) {
            Assertions.assertTrue(false);
        }

    }

    @Test
    public void testWalletWriteCompatibility() {
        ECKey eckeyInput0 = ECKey.fromPublicOnly(Hex.decode("03af8c2da87a89c94f7aa732ec5f26cd0f161b82ab33f6fc641ee93ca0341f7f84"));
        ECKey eckeyInput1 = ECKey.fromPublicOnly(Hex.decode("031896542bebf66ccb38b841988dfb2e0ab640c87ab0f2ae9a40dc675b4b286cd8"));
        ECKey eckeyOutput0 = ECKey.fromPublicOnly(Hex.decode("03fe2f409cc4536f6c79d6d61e4a7135858fea122d99e71ded0e913d68722739d2"));

        try {

            Transaction tx = new Transaction(TestNet3Params.get(), Hex.decode(txHexCompat));

            PSBT psbt = new PSBT(tx);

            String xpub = "tpubDDCJxLAjwkcaW1GpTQdkguVWxmVz1Y5xbq3PhhCowB3YWXk9Jx4ZQSapMedHcVuhP7HBPGDe96HCiRVpWzQMn6FbM7bjd2jAJwBFBUPW9Mc";
            String txHex = "0200000002a4373954617572ff7e2dd93397df266156f14f82cb55f054c7df73f753f10e730000000000fdffffffa4373954617572ff7e2dd93397df266156f14f82cb55f054c7df73f753f10e730100000000fdffffff01e7af02000000000017a914d4134e65e4aa0600493e99163e270e2047ea130a87da652200";

            psbt.addGlobalUnsignedTx(txHex);
            psbt.addGlobalXpubRecord(PSBT.deserializeXPUB(xpub), Hex.decode("1fe61336"), 49, 1, 0);
            psbt.addGlobalSeparator();

            psbt.addInputCompatibility(TestNet3Params.get(), Hex.decode("1fe61336"), eckeyInput0, 80000L, 49, 1, 0, 0, 1, "0200000001046353dcb7b58eccdd5fef1753ceb5656a20682224342302994f4d022dc2f41a0000000017160014b1e865611857028bd723cedba5cedd0a3e22cc4afdffffff02803801000000000017a9147d3928df049a17ef11eaf0a6c7172c137b154ca887487801000000000017a914a130db58c5cfde47f1bdc61b82a43a0c9b0381278739f82100", 0);
            psbt.addInputCompatibility(TestNet3Params.get(), Hex.decode("1fe61336"), eckeyInput1, 96328L, 49, 1, 0, 1, 0, "0200000001046353dcb7b58eccdd5fef1753ceb5656a20682224342302994f4d022dc2f41a0000000017160014b1e865611857028bd723cedba5cedd0a3e22cc4afdffffff02803801000000000017a9147d3928df049a17ef11eaf0a6c7172c137b154ca887487801000000000017a914a130db58c5cfde47f1bdc61b82a43a0c9b0381278739f82100", 1);
            psbt.addOutput(TestNet3Params.get(), Hex.decode("1fe61336"), eckeyOutput0, 49, 1, 0, 0, 2);

            byte[] psbtOutBuf = psbt.toBytes();
            System.out.println(org.bouncycastle.util.encoders.Hex.toHexString(psbtOutBuf));
            System.out.println(psbtCompat.replaceAll(" ", "").toLowerCase());
            Assertions.assertTrue(org.bouncycastle.util.encoders.Hex.toHexString(psbtOutBuf)
                    .equals(psbtCompat.replaceAll(" ", "").toLowerCase()));
        }
        catch(Exception e) {
            System.out.println( e);
        }
    }

    @Test
    public void testWalletWriteLegacy() {

        ECKey eckeyInput0 = ECKey.fromPublicOnly(Hex.decode("031e6983331d3777000185b07d37745cb18ff571141ed942d565a128f1823dea57"));
        ECKey eckeyInput1 = ECKey.fromPublicOnly(Hex.decode("036bc6addd138156097c49797e22f366b893a62c989ed8dbb09c4c668805f2e9ec\n"));
        ECKey eckeyOutput0 = ECKey.fromPublicOnly(Hex.decode("02872597373fd4611df430e690508c49632c33024100ec26b5736d897a4e98ec9e"));

        try {

            Transaction tx = new Transaction(TestNet3Params.get(), Hex.decode(txHexLegacy));

            PSBT psbt = new PSBT(tx);

            String xpub = "tpubDChJA7nEJPdNPEZYLL4CutuuKsoPAyGn1FyMye6mqpomGd8FFgeWyGdWLp84gP7aCNp9mJJhHx8RxCHPNM74dUuqLx2U1SbHpvqTkjJ3fct";
            String txHex = "0200000002b4e5956775b0aa83b9a53c875265492d7021c5e4b58b50a50f2df67a2334a0220200000000fdffffffb4e5956775b0aa83b9a53c875265492d7021c5e4b58b50a50f2df67a2334a0220000000000fdffffff01f1c51000000000001976a914f5e595c6099aae6ec36fbf33336d47e2c369e96788ac77d92300";

            psbt.addGlobalUnsignedTx(txHex);
            psbt.addGlobalXpubRecord(PSBT.deserializeXPUB(xpub), Hex.decode("00000000"), 49, 1, 0);
            psbt.addGlobalSeparator();

            psbt.addInputLegacy(Hex.decode("00000000"), eckeyInput0,  1700L, 44, 0, 0, 1, 3, "0200000000010250b653909435a904f9aa6612b608488f06d191be360e1cf33fee44966cf3b10f000000006a47304402204217b19eef0eceeda932924ded2ca13aab6a51b972d40330edd4ae67f8bd76300220626ff117abb82659e9be7e331a6430d6b3c20cca0128688f1749734ca02607ac012102ac9e09694517f748cd7dccddfe0c004f481f581c6273ac8c181f4fbe3ef3c42dffffffff3999d9e12e034ff9b748dcd54bc2c229ded4524f98ec3a8650d138abac680b550000000000ffffffff04a4060000000000001976a914055c6a5ad3b43db026323736353271bb92808c5588aca4060000000000001976a91441848a8274d30b9147f70b51f0c4ccc95adb059f88aca3c01000000000001976a91409c437d0dd31885b118605c1593dfba59c06450d88ac60b1170000000000160014d0c52d76ebd8107d106f11e9f4c4a29131a243090002473044022016eb66d9196aa14a3a4c50bf5feae107aaa55220f56c124310a4e0d7b14411890220667d1160064aaf324d5eabbcfb1e4423f6c806d0d7950a21e902e0e4a706506a012102fd4672b2b4396febccb58692d529003ca89a93a5dbecf97250eb4ac25c006f6f00000000");
            psbt.addInputLegacy(Hex.decode("00000000"), eckeyInput1,  1097891L, 44, 0, 0, 1, 4, "0200000000010250b653909435a904f9aa6612b608488f06d191be360e1cf33fee44966cf3b10f000000006a47304402204217b19eef0eceeda932924ded2ca13aab6a51b972d40330edd4ae67f8bd76300220626ff117abb82659e9be7e331a6430d6b3c20cca0128688f1749734ca02607ac012102ac9e09694517f748cd7dccddfe0c004f481f581c6273ac8c181f4fbe3ef3c42dffffffff3999d9e12e034ff9b748dcd54bc2c229ded4524f98ec3a8650d138abac680b550000000000ffffffff04a4060000000000001976a914055c6a5ad3b43db026323736353271bb92808c5588aca4060000000000001976a91441848a8274d30b9147f70b51f0c4ccc95adb059f88aca3c01000000000001976a91409c437d0dd31885b118605c1593dfba59c06450d88ac60b1170000000000160014d0c52d76ebd8107d106f11e9f4c4a29131a243090002473044022016eb66d9196aa14a3a4c50bf5feae107aaa55220f56c124310a4e0d7b14411890220667d1160064aaf324d5eabbcfb1e4423f6c806d0d7950a21e902e0e4a706506a012102fd4672b2b4396febccb58692d529003ca89a93a5dbecf97250eb4ac25c006f6f00000000");
            psbt.addInputSeparator();
            psbt.addOutput(TestNet3Params.get(), Hex.decode("00000000"), eckeyOutput0, 44, 0, 0, 0, 17);

            byte[] psbtOutBuf = psbt.toBytes();

            System.out.println(org.bouncycastle.util.encoders.Hex.toHexString(psbtOutBuf));

            System.out.println(psbtLegacy.replaceAll(" ", "").toLowerCase());
            Assertions.assertTrue(org.bouncycastle.util.encoders.Hex.toHexString(psbtOutBuf)
                    .equals(psbtLegacy.replaceAll(" ", "").toLowerCase()));
        }
        catch(Exception e) {
            Assertions.assertTrue(false);
        }
    }


    @Test
    public void testWalletTaproot() {

        ECKey eckeyInput0 = ECKey.fromPublicOnly(Hex.decode("025e6861ac2a08ee1e7b7e57379478caef2b4227175359e8bd3df090118e1619c0"));
        ECKey eckeyOutput0 = ECKey.fromPublicOnly(Hex.decode("023762943fa4da61f1c291331e9f6f71e5246b2fa0f0f55e2be69452b0601c97c2"));

        try {

            Transaction tx = new Transaction(TestNet3Params.get(), Hex.decode(txHexTaproot));

            PSBT psbt = new PSBT(tx);

            String xpub = "tpubDDReqTrWFRHcuRA5TXenbjJV3cYZHCwyXiDV4yEw4N5q2JFaWfUFJGzurLmPP2R5FAkCdb3VjohGV8RqXJ1zXd4kWAMWJBkxZBnRn6m4UQ1";
            String txHex = "0200000001146935b57b36d17cdbbdb3c412f72244ac25614b2a7b9dc370b61088f5a811c501000000000000000001308601000000000022512080e2d9c811ad041953e25524bf7eb3cd6541b7bc3804b83eeb44ac49da3aef1500000000";

            psbt.addGlobalUnsignedTx(txHex);
            psbt.addGlobalXpubRecord(PSBT.deserializeXPUB(xpub), Hex.decode("3927daee"), 49, 1, 0);
            psbt.addGlobalSeparator();


            psbt.addInputTaproot(TestNet3Params.get(), Hex.decode("3927daee"), eckeyInput0, 100000L,86, 1, 0, 0, 0, "0200000001056e30a45ffd7b0e5ec724ebd4f67583eb1a38688b32b2b31cbb00430fc2556a0000000000feffffff0244366a6e000000001600145791c4c80ec1cdc287643b2b683d4493f35a96cba0860100000000002251206b5cbd16adef9c4ea9b0ae02d75e12cce463509ac1af48d17958995f1a0082e3a7672200");

            psbt.addInputSeparator();
            psbt.addOutput(TestNet3Params.get(), Hex.decode("3927daee"), eckeyOutput0, 86, 0, 0, 0, 1);
            // add trailing separator

            byte[] psbtOutBuf = psbt.toBytes();
            System.out.println(org.bouncycastle.util.encoders.Hex.toHexString(psbtOutBuf));
            System.out.println(psbtTaproot.replaceAll(" ", "").toLowerCase());
            Assertions.assertTrue(org.bouncycastle.util.encoders.Hex.toHexString(psbtOutBuf)
                    .equals(psbtTaproot.replaceAll(" ", "").toLowerCase()));
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}
