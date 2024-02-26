package com.samourai.dex.config;

import java.util.Arrays;
import java.util.Collection;

public class SamouraiConfig {
    // extlibj: BackendServer
    private String backendServerMainnetClear = "https://api.samouraiwallet.com/v2";
    private String backendServerMainnetOnion = "http://d2oagweysnavqgcfsfawqwql2rwxend7xxpriq676lzsmtfwbt75qbqd.onion/v2";
    private String backendServerTestnetClear = "https://api.samouraiwallet.com/test/v2";
    private String backendServerTestnetOnion = "http://d2oagweysnavqgcfsfawqwql2rwxend7xxpriq676lzsmtfwbt75qbqd.onion/test/v2";

    // extlibj: SorobanServer
    private String sorobanServerTestnetClear = "https://soroban.samouraiwallet.com/test";
    private String sorobanServerTestnetOnion = "http://sorob4sg7yiopktgz4eom7hl5mcodr6quvhmdpljl5qqhmt6po7oebid.onion/test";
    private String sorobanServerMainnetClear = "https://soroban.samouraiwallet.com";
    private String sorobanServerMainnetOnion = "http://sorob4sg7yiopktgz4eom7hl5mcodr6quvhmdpljl5qqhmt6po7oebid.onion";

    // extlibj: SorobanServerDex
    private Collection<String> sorobanServerDexTestnetClear = Arrays.asList( // TODO test cluster
            "http://163.172.128.201:4242",
            "http://163.172.130.151:4242",
            "http://163.172.159.127:4242",
            "http://163.172.174.29:4242",
            "http://163.172.159.227:4242",
            "http://51.15.192.136:4242",
            "http://51.158.116.168:4242",
            "http://51.15.226.163:4242",
            "http://212.47.230.157:4242"
    );
    private Collection<String> sorobanServerDexTestnetOnion = Arrays.asList(
            "http://sorin6xws7lfvz2ikma3ceqzlbmmp4huyfwla3d5mzkcjnta2djgggid.onion",
            "http://sor2aduqon52pngz56b3pp2niq5vp4l7xstco654gfa37fcaoblle5yd.onion",
            "http://sor3xcg6i4tyt4uawt2t3i3ml4dzubh46wyloptl3g5jyshkgekuumyd.onion",
            "http://sor4ky4w6iywr3terpofu3xlnwrnawbb4aq2okobixojatswelfpsuad.onion",
            "http://sor5qv4q3uvg3cyfaay5dazkvuytukepexlqehgirxbq7ynszpqo4uad.onion",
            "http://sor6jatlc2pim7mg4paxy6kgzuw7qidajlxk7xy6ic6ytcpcy47lucyd.onion",
            "http://sor7qfbf24l3gdba5ed625pfwfebwctiao5po3zux3c6udlboowkucid.onion",
            "http://sorark2anb6q6oz6egxo4zo67cmlnfvjxz2h34v74ta6jozceqclw5yd.onion",
            "http://sorbutari3lpxmqhpzu3jojflteluekxnfyu2jgs4vqkgtsxrjyv4byd.onion"
    );
    private Collection<String> sorobanServerDexMainnetClear = Arrays.asList(
            "http://163.172.128.201:4242",
            "http://163.172.130.151:4242",
            "http://163.172.159.127:4242",
            "http://163.172.174.29:4242",
            "http://163.172.159.227:4242",
            "http://51.15.192.136:4242",
            "http://51.158.116.168:4242",
            "http://51.15.226.163:4242",
            "http://212.47.230.157:4242"
    );
    private Collection<String> sorobanServerDexMainnetOnion = Arrays.asList(
            "http://sorin6xws7lfvz2ikma3ceqzlbmmp4huyfwla3d5mzkcjnta2djgggid.onion",
            "http://sor2aduqon52pngz56b3pp2niq5vp4l7xstco654gfa37fcaoblle5yd.onion",
            "http://sor3xcg6i4tyt4uawt2t3i3ml4dzubh46wyloptl3g5jyshkgekuumyd.onion",
            "http://sor4ky4w6iywr3terpofu3xlnwrnawbb4aq2okobixojatswelfpsuad.onion",
            "http://sor5qv4q3uvg3cyfaay5dazkvuytukepexlqehgirxbq7ynszpqo4uad.onion",
            "http://sor6jatlc2pim7mg4paxy6kgzuw7qidajlxk7xy6ic6ytcpcy47lucyd.onion",
            "http://sor7qfbf24l3gdba5ed625pfwfebwctiao5po3zux3c6udlboowkucid.onion",
            "http://sorark2anb6q6oz6egxo4zo67cmlnfvjxz2h34v74ta6jozceqclw5yd.onion",
            "http://sorbutari3lpxmqhpzu3jojflteluekxnfyu2jgs4vqkgtsxrjyv4byd.onion",

            // onion only
            "http://g35mf2l5zzbzldqfq5sfdsw3fibajh5rytnppyb634gn6b5o3fhif6yd.onion",
            "http://3y3j4dgul5767ibxkll3siadcojmhqudeaz7f2i3grsgmklkpntrbbyd.onion",
            "http://jvdzspo3kkhztcyoddcgednw6643zuvfubha43bj4zh7jqre27lgvvad.onion",
            "http://so4cne7cfg5tbqe6d5c2q5ci2ezpg7ii4vm2w5cu7v37bvyjfv6g6fid.onion",
            "http://7m4eaaxxpbwnoy77qx6nfxqjgbe3ttbkb6f4lftvohzia2ph5qnx2vad.onion",
            "http://lyxiuvhkbvrp2krprwlj5z5oip52qlle6woryty6fhs4r7vqxrnkupad.onion",
            "http://thcywo6avdbjwamyt2vilfw7kb254a7adb42nud63qfvpysdetxzphqd.onion",
            "http://v5uevs57okmzsrz5bdald2qvs7k3m3ekotujjvpnaqiuwnuafbnhadqd.onion",
            "http://445hebzvpkm2gwohxxzwohdw5wnxwg5lth67nakmg7fmdqbcry4irvqd.onion"
    );

    // whrilpool-client: WhrilpoolServer
    private String whirlpoolServerTestnetClear = "https://pool.whirl.mx:8081";
    private String whirlpoolServerTestnetOnion = "http://y5qvjlxvbohc73slq4j4qldoegyukvpp74mbsrjosnrsgg7w5fon6nyd.onion";
    private String whirlpoolServerMainnetClear = "https://pool.whirl.mx:8080";
    private String whirlpoolServerMainnetOnion = "http://udkmfc5j6zvv3ysavbrwzhwji4hpyfe3apqa6yst7c7l32mygf65g4ad.onion";
    private String whirlpoolServerIntegrationClear = "https://pool.whirl.mx:8082";
    private String whirlpoolServerIntegrationOnion = "http://yuvewbfkftftcbzn54lfx3i5s4jxr4sfgpsbkvcflgzcvumyxrkopmyd.onion";

    public String getWhirlpoolServerIntegrationClear() {
        return whirlpoolServerIntegrationClear;
    }

    public void setWhirlpoolServerIntegrationClear(String whirlpoolServerIntegrationClear) {
        this.whirlpoolServerIntegrationClear = whirlpoolServerIntegrationClear;
    }

    public String getWhirlpoolServerIntegrationOnion() {
        return whirlpoolServerIntegrationOnion;
    }

    public void setWhirlpoolServerIntegrationOnion(String whirlpoolServerIntegrationOnion) {
        this.whirlpoolServerIntegrationOnion = whirlpoolServerIntegrationOnion;
    }
    public String getWhirlpoolServerTestnetClear() {
        return whirlpoolServerTestnetClear;
    }

    public void setWhirlpoolServerTestnetClear(String whirlpoolServerTestnetClear) {
        this.whirlpoolServerTestnetClear = whirlpoolServerTestnetClear;
    }

    public String getWhirlpoolServerTestnetOnion() {
        return whirlpoolServerTestnetOnion;
    }

    public void setWhirlpoolServerTestnetOnion(String whirlpoolServerTestnetOnion) {
        this.whirlpoolServerTestnetOnion = whirlpoolServerTestnetOnion;
    }

    public String getWhirlpoolServerMainnetClear() {
        return whirlpoolServerMainnetClear;
    }

    public void setWhirlpoolServerMainnetClear(String whirlpoolServerMainnetClear) {
        this.whirlpoolServerMainnetClear = whirlpoolServerMainnetClear;
    }

    public String getWhirlpoolServerMainnetOnion() {
        return whirlpoolServerMainnetOnion;
    }

    public void setWhirlpoolServerMainnetOnion(String whirlpoolServerMainnetOnion) {
        this.whirlpoolServerMainnetOnion = whirlpoolServerMainnetOnion;
    }

    public SamouraiConfig() {
    }

    public String getBackendServerMainnetClear() {
        return backendServerMainnetClear;
    }

    public void setBackendServerMainnetClear(String backendServerMainnetClear) {
        this.backendServerMainnetClear = backendServerMainnetClear;
    }

    public String getBackendServerMainnetOnion() {
        return backendServerMainnetOnion;
    }

    public void setBackendServerMainnetOnion(String backendServerMainnetOnion) {
        this.backendServerMainnetOnion = backendServerMainnetOnion;
    }

    public String getBackendServerTestnetClear() {
        return backendServerTestnetClear;
    }

    public void setBackendServerTestnetClear(String backendServerTestnetClear) {
        this.backendServerTestnetClear = backendServerTestnetClear;
    }

    public String getBackendServerTestnetOnion() {
        return backendServerTestnetOnion;
    }

    public void setBackendServerTestnetOnion(String backendServerTestnetOnion) {
        this.backendServerTestnetOnion = backendServerTestnetOnion;
    }

    //

    public String getSorobanServerTestnetClear() {
        return sorobanServerTestnetClear;
    }

    public void setSorobanServerTestnetClear(String sorobanServerTestnetClear) {
        this.sorobanServerTestnetClear = sorobanServerTestnetClear;
    }

    public String getSorobanServerTestnetOnion() {
        return sorobanServerTestnetOnion;
    }

    public void setSorobanServerTestnetOnion(String sorobanServerTestnetOnion) {
        this.sorobanServerTestnetOnion = sorobanServerTestnetOnion;
    }

    public String getSorobanServerMainnetClear() {
        return sorobanServerMainnetClear;
    }

    public void setSorobanServerMainnetClear(String sorobanServerMainnetClear) {
        this.sorobanServerMainnetClear = sorobanServerMainnetClear;
    }

    public String getSorobanServerMainnetOnion() {
        return sorobanServerMainnetOnion;
    }

    public void setSorobanServerMainnetOnion(String sorobanServerMainnetOnion) {
        this.sorobanServerMainnetOnion = sorobanServerMainnetOnion;
    }

    //


    public Collection<String> getSorobanServerDexTestnetClear() {
        return sorobanServerDexTestnetClear;
    }

    public void setSorobanServerDexTestnetClear(Collection<String> sorobanServerDexTestnetClear) {
        this.sorobanServerDexTestnetClear = sorobanServerDexTestnetClear;
    }

    public Collection<String> getSorobanServerDexTestnetOnion() {
        return sorobanServerDexTestnetOnion;
    }

    public void setSorobanServerDexTestnetOnion(Collection<String> sorobanServerDexTestnetOnion) {
        this.sorobanServerDexTestnetOnion = sorobanServerDexTestnetOnion;
    }

    public Collection<String> getSorobanServerDexMainnetClear() {
        return sorobanServerDexMainnetClear;
    }

    public void setSorobanServerDexMainnetClear(Collection<String> sorobanServerDexMainnetClear) {
        this.sorobanServerDexMainnetClear = sorobanServerDexMainnetClear;
    }

    public Collection<String> getSorobanServerDexMainnetOnion() {
        return sorobanServerDexMainnetOnion;
    }

    public void setSorobanServerDexMainnetOnion(Collection<String> sorobanServerDexMainnetOnion) {
        this.sorobanServerDexMainnetOnion = sorobanServerDexMainnetOnion;
    }
}