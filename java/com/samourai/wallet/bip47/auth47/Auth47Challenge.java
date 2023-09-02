package com.samourai.wallet.bip47.auth47;

import com.google.common.base.Splitter;
import com.samourai.wallet.util.RandomUtil;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;

public class Auth47Challenge {
    public static final String SCHEME = "auth47";

    private String nonce;
    private String callback; // may be null
    private String resource; // may be null
    private Long expiry; // may be null

    public Auth47Challenge() {}

    public Auth47Challenge(String callback, String resourceOrNull, Long expiryOrNull) throws Exception {
        this(generateNonce(), callback, resourceOrNull, expiryOrNull);
    }

    public Auth47Challenge(String nonce, String callback, String resourceOrNull, Long expiryOrNull) throws Exception {
        // validate callback
        if (!StringUtils.isEmpty(callback)) {
            String protocol = callback.split("://")[0];
            if (!Auth47Protocol.find(protocol).isPresent()) {
                throw new MalformedURLException("Invalid protocol for callback URI");
            }
        }

        this.nonce = nonce;
        this.callback = callback;
        this.resource = resourceOrNull;
        this.expiry = expiryOrNull;
    }

    private static String generateNonce() {
        return Hex.toHexString(RandomUtil.getInstance().nextBytes(12)); // random nonce
    }

    public static Auth47Challenge parse(String challenge) throws Exception {
        URI uri = new URI(challenge);
        if (!uri.getScheme().equals(SCHEME)) {
            throw new Exception("Invalid auth challenge: unknown scheme");
        }
        String nonce = uri.getHost();
        Map<String, String> args = Splitter.on('&').trimResults().withKeyValueSeparator('=').split(uri.getQuery());
        String callback = args.containsKey("c") ? URLDecoder.decode(args.get("c"), "UTF-8") : null;
        String resource = args.containsKey("r") ? URLDecoder.decode(args.get("r"), "UTF-8") : null;
        String expiryStr = args.containsKey("e") ? URLDecoder.decode(args.get("e"), "UTF-8") : null;
        Long expiry = !StringUtils.isEmpty(expiryStr) ? Long.parseLong(expiryStr) : null;
        return new Auth47Challenge(nonce, callback, resource, expiry);
    }

    @Override
    public String toString() {
        String url = SCHEME+"://"+nonce+"?";
        if (!StringUtils.isEmpty(callback)) {
            url += "&c="+callback;
        }
        if (!StringUtils.isEmpty(resource)) {
            url += "&r="+resource;
        }
        if (expiry != null) {
            url += "&e="+expiry;
        }
        url = url.replace("?&", "?");
        return url;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }
}
