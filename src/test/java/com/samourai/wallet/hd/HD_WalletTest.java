package com.samourai.wallet.hd;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HD_WalletTest {
    private HD_WalletFactoryGeneric hdWalletFactory = HD_WalletFactoryGeneric.getInstance();

    @Test
    public void testHdWalletTestnet() throws Exception {
        NetworkParameters params = TestNet3Params.get();

        HD_Wallet hdWallet1 = hdWalletFactory.getHD(44, "foo1".getBytes(), "test1", params);
        HD_Wallet hdWallet2 = hdWalletFactory.getHD(44, "foo1".getBytes(), "test2", params);

        HD_Wallet hdWallet3 = hdWalletFactory.getHD(44, "foo2".getBytes(), "test1", params);
        HD_Wallet hdWallet4 = hdWalletFactory.getHD(44, "foo2".getBytes(), "test2", params);

        HD_Wallet hdWallet1Copy = new HD_Wallet(44, hdWallet1);

        // verify
        verifyWallet1Testnet(hdWallet1);
        verifyWallet1Testnet(hdWallet1Copy);
        Assertions.assertArrayEquals(new String[]{"tpubDCjWUQzx4WG2igBV4MtRbYg8ZkgsSs3LBs4Y1rBmF8MbFeGCcvFFPmqLgbpr1bNy37fnwsfm9SCkgcUYBw4hgVFzLsfwxoCqWkBiWhbU9Ry"}, hdWallet2.getXPUBs());
        Assertions.assertArrayEquals(new String[]{"tpubDCCzbVg6SzMF9fYUTuRbqPdcUFu2erTn2iHHgTp1jxu7Ve9zxLSvPVdfxR6nRT6GE5ShqM35eTfKXMhncSHCkrZzTEdgbZ8tFs63ix6S1fC"}, hdWallet3.getXPUBs());
        Assertions.assertArrayEquals(new String[]{"tpubDDK5Q8DUwp9TvNZjf6QBAUuxk2QAgcmpVVpxSHVYc4fDBuCpwuyQnzDDAtimCBLDrQXrX3R8KKoyjWjVABZtfE41FWeYSVKndfKCvmXrhD6"}, hdWallet4.getXPUBs());

        Assertions.assertEquals("myrDKvdCUNAMEoxWT4r3116i21R93s5vUV", hdWallet2.getAccount(0).getChain(0).getAddressAt(0).getAddressString());
        Assertions.assertEquals("mpZYLcbacAdehXp3max1h2Lubk4GRnnpLj", hdWallet3.getAccount(0).getChain(0).getAddressAt(0).getAddressString());
        Assertions.assertEquals("mhAaH3UGm6NYeHHvHx3KRGGCddwYdBj3VH", hdWallet4.getAccount(0).getChain(0).getAddressAt(0).getAddressString());

        Assertions.assertEquals("mnTHShP3iqCBYnxDnnBswpaHW3gMnVQyYq", hdWallet2.getAccount(0).getChain(1).getAddressAt(0).getAddressString());
        Assertions.assertEquals("mq8cBiuaRRPUyue22TMbWerhruXrJmfGZY", hdWallet3.getAccount(0).getChain(1).getAddressAt(0).getAddressString());
        Assertions.assertEquals("mnEntMG6XAqwvH9C1UbHiKgwdWFKAEgHtu", hdWallet4.getAccount(0).getChain(1).getAddressAt(0).getAddressString());

        Assertions.assertEquals("n2HYGk5jk4YoRQrBeMNkE5RKHegRgVyU9M", hdWallet2.getAccount(0).getChain(1).getAddressAt(1).getAddressString());
        Assertions.assertEquals("mpZbQ8syt9MNRh9duiwVaEsVYwQE5E6r5p", hdWallet3.getAccount(0).getChain(1).getAddressAt(1).getAddressString());
        Assertions.assertEquals("mmiiiy2gW4KFdKXQJC93p3jpjS7FXpi8Lq", hdWallet4.getAccount(0).getChain(1).getAddressAt(1).getAddressString());

        // getAddressString
        Assertions.assertEquals("myrDKvdCUNAMEoxWT4r3116i21R93s5vUV", hdWallet2.getAddressAt(0, 0, 0).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("mpZYLcbacAdehXp3max1h2Lubk4GRnnpLj", hdWallet3.getAddressAt(0, 0, 0).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("mhAaH3UGm6NYeHHvHx3KRGGCddwYdBj3VH", hdWallet4.getAddressAt(0, 0, 0).getAddressString(AddressType.LEGACY));

        Assertions.assertEquals("mnTHShP3iqCBYnxDnnBswpaHW3gMnVQyYq", hdWallet2.getAddressAt(0, 1, 0).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("mq8cBiuaRRPUyue22TMbWerhruXrJmfGZY", hdWallet3.getAddressAt(0, 1, 0).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("mnEntMG6XAqwvH9C1UbHiKgwdWFKAEgHtu", hdWallet4.getAddressAt(0, 1, 0).getAddressString(AddressType.LEGACY));

        Assertions.assertEquals("n2HYGk5jk4YoRQrBeMNkE5RKHegRgVyU9M", hdWallet2.getAddressAt(0, 1, 1).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("mpZbQ8syt9MNRh9duiwVaEsVYwQE5E6r5p", hdWallet3.getAddressAt(0, 1, 1).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("mmiiiy2gW4KFdKXQJC93p3jpjS7FXpi8Lq", hdWallet4.getAddressAt(0, 1, 1).getAddressString(AddressType.LEGACY));

        Assertions.assertEquals("2NCe823Q5mGhyX3wW1w7AQYwRKPLapwMwcB", hdWallet2.getAddressAt(0, 0, 0).getAddressString(AddressType.SEGWIT_COMPAT));
        Assertions.assertEquals("tb1qeyttwdc6uv6flw93e56qvvfvwz6tlt02vsqc34", hdWallet2.getAddressAt(0, 0, 0).getAddressString(AddressType.SEGWIT_NATIVE));
    }

    private void verifyWallet1Testnet(HD_Wallet hdWallet1) {
        Assertions.assertArrayEquals(new String[]{"tpubDC8qnx32oEZVipETHVHJhybpkTrgadwLGQZqd2nz9VPVu63gH4R6BHKB1UB4DYwXNu37Dtw1JmADsZQ75upg4Dy8aBCBCR28mDuC86DKueS"}, hdWallet1.getXPUBs());
        Assertions.assertEquals("n3VABKp2wDB3mLHw6xH6SgVMWzFGU9u169", hdWallet1.getAccount(0).getChain(0).getAddressAt(0).getAddressString());
        Assertions.assertEquals("mt6uJ69jhbFGzAgrb6RnEqhwrd7tTKjxF7", hdWallet1.getAccount(0).getChain(1).getAddressAt(0).getAddressString());
        Assertions.assertEquals("muimRQFJKMJM1pTminJxiD5HrPgSu257tX", hdWallet1.getAccount(0).getChain(1).getAddressAt(1).getAddressString());
    }

    @Test
    public void testHdWalletMainnet() throws Exception {
        NetworkParameters params = MainNetParams.get();

        HD_Wallet hdWallet1 = hdWalletFactory.getHD(44, "foo1".getBytes(), "test1", params);
        HD_Wallet hdWallet2 = hdWalletFactory.getHD(44, "foo1".getBytes(), "test2", params);

        HD_Wallet hdWallet3 = hdWalletFactory.getHD(44, "foo2".getBytes(), "test1", params);
        HD_Wallet hdWallet4 = hdWalletFactory.getHD(44, "foo2".getBytes(), "test2", params);

        HD_Wallet hdWallet1Copy = new HD_Wallet(44, hdWallet1);

        // verify
        verifyWallet1Mainnet(hdWallet1);
        verifyWallet1Mainnet(hdWallet1Copy);
        Assertions.assertArrayEquals(new String[]{"xpub6C8aSUjB7fwH6CSpS5AjRh1sPwfmrZKNNrfye5rkijhFpSfiKeSNT2CpVLuDzQiipdYAmmyi4eLXritVhYjfBfeEWJPXUrUEEHrcgnEH7wX"}, hdWallet2.getXPUBs());
        Assertions.assertArrayEquals(new String[]{"xpub6DUQ2PuGdPGVK74fqMpFw7UxQa2wLcv8JcWEV7mjNgiuiv4NjgsxukpDfd6xaeuU87oEGx16k3w1XhCs4mmK8GybS6n9W5hvAvCtyxB9nLV"}, hdWallet3.getXPUBs());
        Assertions.assertArrayEquals(new String[]{"xpub6DQVth98Zm2fQnsjAo7djuzoxYVnXMkio5TMCTdCdMgergYKJMQjzAqGCLfciX5fs7gkAa3xnS8cnoHmQ9sQTqnTppQPULwN778KqdRemf5"}, hdWallet4.getXPUBs());

        Assertions.assertEquals("1HtHLCcbiF5QGTceLn75t5ob5UvUGH8VeF", hdWallet2.getAccount(0).getChain(0).getAddressAt(0).getAddressString());
        Assertions.assertEquals("1GwQVkN26sQzBT31SRL44jVCKwzXkguiWb", hdWallet3.getAccount(0).getChain(0).getAddressAt(0).getAddressString());
        Assertions.assertEquals("19PsVTqibwYznPwnippykj9DERnZz2h5Xd", hdWallet4.getAccount(0).getChain(0).getAddressAt(0).getAddressString());

        Assertions.assertEquals("13vwGM9oQxgtJfUnVazpecUnXqeF5gmf5o", hdWallet2.getAccount(0).getChain(1).getAddressAt(0).getAddressString());
        Assertions.assertEquals("1DURg2Jm2Pf1128L1ZB135yRyjdfskuS5R", hdWallet3.getAccount(0).getChain(1).getAddressAt(0).getAddressString());
        Assertions.assertEquals("1Nyq7DHGA562DCHhS46WqX6BPDzuDGTjYk", hdWallet4.getAccount(0).getChain(1).getAddressAt(0).getAddressString());

        Assertions.assertEquals("179vtkLefbwrNKg1U84Jj5m48qEavS55De", hdWallet2.getAccount(0).getChain(1).getAddressAt(1).getAddressString());
        Assertions.assertEquals("1ENJv6fC5aionx1HJwDkLeSh2dkRXtxtnQ", hdWallet3.getAccount(0).getChain(1).getAddressAt(1).getAddressString());
        Assertions.assertEquals("171nJXTUgRNtpMyivBRsHVeE1RZYeR8i2D", hdWallet4.getAccount(0).getChain(1).getAddressAt(1).getAddressString());

        // getAddressString
        Assertions.assertEquals("1HtHLCcbiF5QGTceLn75t5ob5UvUGH8VeF", hdWallet2.getAddressAt(0, 0, 0).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("1GwQVkN26sQzBT31SRL44jVCKwzXkguiWb", hdWallet3.getAddressAt(0, 0, 0).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("19PsVTqibwYznPwnippykj9DERnZz2h5Xd", hdWallet4.getAddressAt(0, 0, 0).getAddressString(AddressType.LEGACY));

        Assertions.assertEquals("13vwGM9oQxgtJfUnVazpecUnXqeF5gmf5o", hdWallet2.getAddressAt(0, 1, 0).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("1DURg2Jm2Pf1128L1ZB135yRyjdfskuS5R", hdWallet3.getAddressAt(0, 1, 0).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("1Nyq7DHGA562DCHhS46WqX6BPDzuDGTjYk", hdWallet4.getAddressAt(0, 1, 0).getAddressString(AddressType.LEGACY));

        Assertions.assertEquals("179vtkLefbwrNKg1U84Jj5m48qEavS55De", hdWallet2.getAddressAt(0, 1, 1).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("1ENJv6fC5aionx1HJwDkLeSh2dkRXtxtnQ", hdWallet3.getAddressAt(0, 1, 1).getAddressString(AddressType.LEGACY));
        Assertions.assertEquals("171nJXTUgRNtpMyivBRsHVeE1RZYeR8i2D", hdWallet4.getAddressAt(0, 1, 1).getAddressString(AddressType.LEGACY));

        Assertions.assertEquals("39KBoSBgfvKpDAJLzwXdJdNLqEJ8Lxno4Y", hdWallet2.getAddressAt(0, 0, 0).getAddressString(AddressType.SEGWIT_COMPAT));
        Assertions.assertEquals("bc1qhy6dh6c67q8uwshffrs2rjs85x4wyhx9k45rha", hdWallet2.getAddressAt(0, 0, 0).getAddressString(AddressType.SEGWIT_NATIVE));
    }

    @Test
    public void testHdWalletMainnetXpub() throws Exception {
        NetworkParameters params = MainNetParams.get();

        HD_Wallet hdWallet1 = new HD_Wallet(params, new String[]{"xpub6By39V6HgpxbtuBVMpGDWPDFaBpMqEewX1KV45eXUZkvoV5TVgr9dvi5MkxtRrdovbngSAJtHR3mau3a2b9hmnTR9G7zjXozwqDBaHFPT5j"});

        // verify
        verifyWallet1Mainnet(hdWallet1);
    }

    private void verifyWallet1Mainnet(HD_Wallet hdWallet1) {
        Assertions.assertArrayEquals(new String[]{"xpub6By39V6HgpxbtuBVMpGDWPDFaBpMqEewX1KV45eXUZkvoV5TVgr9dvi5MkxtRrdovbngSAJtHR3mau3a2b9hmnTR9G7zjXozwqDBaHFPT5j"}, hdWallet1.getXPUBs());
        Assertions.assertEquals("xpub6By39V6HgpxbtuBVMpGDWPDFaBpMqEewX1KV45eXUZkvoV5TVgr9dvi5MkxtRrdovbngSAJtHR3mau3a2b9hmnTR9G7zjXozwqDBaHFPT5j", hdWallet1.getAccount(0).xpubstr());
        Assertions.assertEquals("1C36vErfBHdZPnrB5vMh6fRxnZ3RfRr8eW", hdWallet1.getAccount(0).getChain(0).getAddressAt(0).getAddressString());
        Assertions.assertEquals("19pAMZjGAy3C4uVREZKK959jhRynUJ6hhD", hdWallet1.getAccount(0).getChain(1).getAddressAt(0).getAddressString());
        Assertions.assertEquals("1b1C6KHtjXb5Ln2UFMwxNpuZbuQsmdrGv", hdWallet1.getAccount(0).getChain(1).getAddressAt(1).getAddressString());
    }

    @Test
    public void testAddressGetPathString() throws Exception {
        NetworkParameters params = TestNet3Params.get();

        HD_Wallet hdw44 = hdWalletFactory.getHD(44, "foo1".getBytes(), "test1", params);
        Assertions.assertEquals("m/44'/0'/0'/0/0", hdw44.getAddressAt(0, 0, 0).getPathFull(AddressType.LEGACY));
        Assertions.assertEquals("m/44'/0'/0'/1/0", hdw44.getAddressAt(0, 1, 0).getPathFull(AddressType.LEGACY));
        Assertions.assertEquals("m/44'/0'/0'/1/1", hdw44.getAddressAt(0, 1, 1).getPathFull(AddressType.LEGACY));
        Assertions.assertEquals("m/44'/0'/3'/1/1", hdw44.getAddressAt(3, 1, 1).getPathFull(AddressType.LEGACY));
        Assertions.assertEquals("muimRQFJKMJM1pTminJxiD5HrPgSu257tX", hdw44.getAddressAt(0, 1, 1).getAddressString(AddressType.LEGACY));

        HD_Wallet hdw49 = hdWalletFactory.getHD(49, "foo1".getBytes(), "test1", params);
        Assertions.assertEquals("m/49'/0'/0'/0/0", hdw49.getAddressAt(0, 0, 0).getPathFull(AddressType.SEGWIT_COMPAT));
        Assertions.assertEquals("m/49'/0'/0'/1/0", hdw49.getAddressAt(0, 1, 0).getPathFull(AddressType.SEGWIT_COMPAT));
        Assertions.assertEquals("m/49'/0'/0'/1/1", hdw49.getAddressAt(0, 1, 1).getPathFull(AddressType.SEGWIT_COMPAT));
        Assertions.assertEquals("m/49'/0'/3'/1/1", hdw49.getAddressAt(3, 1, 1).getPathFull(AddressType.SEGWIT_COMPAT));
        Assertions.assertEquals("2MuEXFSbU4xNM8kc5xBQ5BtrAGbpoWf45Yq", hdw49.getAddressAt(0, 1, 1).getAddressString(AddressType.SEGWIT_COMPAT));

        HD_Wallet hdw84 = hdWalletFactory.getHD(84, "foo1".getBytes(), "test1", params);
        Assertions.assertEquals("m/84'/0'/0'/0/0", hdw84.getAddressAt(0, 0, 0).getPathFull(AddressType.SEGWIT_NATIVE));
        Assertions.assertEquals("m/84'/0'/0'/1/0", hdw84.getAddressAt(0, 1, 0).getPathFull(AddressType.SEGWIT_NATIVE));
        Assertions.assertEquals("m/84'/0'/0'/1/1", hdw84.getAddressAt(0, 1, 1).getPathFull(AddressType.SEGWIT_NATIVE));
        Assertions.assertEquals("m/84'/0'/3'/1/1", hdw84.getAddressAt(3, 1, 1).getPathFull(AddressType.SEGWIT_NATIVE));
        Assertions.assertEquals("tb1qx8tg2ayl2hkutcmc3d2uw2zrc6wh6wa6sqf4v9", hdw84.getAddressAt(0, 1, 1).getAddressString(AddressType.SEGWIT_NATIVE));
    }

}
