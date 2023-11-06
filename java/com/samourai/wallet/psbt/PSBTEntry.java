package com.samourai.wallet.psbt;

import java.nio.ByteBuffer;

import static com.samourai.wallet.psbt.PSBT.readCompactInt;

public class PSBTEntry {

    public PSBTEntry() { ; }

    PSBTEntry(ByteBuffer psbtByteBuffer) throws Exception {
        int keyLen = PSBT.readCompactInt(psbtByteBuffer);

        if (keyLen == 0x00) {
            key = null;
            keyType = ByteBuffer.allocate(4).putInt(0x00).array();;
            keyData = null;
            data = null;
        } else {
            byte[] key = new byte[keyLen];
            psbtByteBuffer.get(key);

            byte keyType = key[0];

            byte[] keyData = null;
            if (key.length > 1) {
                keyData = new byte[key.length - 1];
                System.arraycopy(key, 1, keyData, 0, keyData.length);
            }

            int dataLen = readCompactInt(psbtByteBuffer);
            byte[] data = new byte[dataLen];
            psbtByteBuffer.get(data);

            this.key = key;
            this.keyType = ByteBuffer.allocate(4).putInt(keyType).array();;
            this.keyData = keyData;
            this.data = data;
        }
    }

    private byte[] key = null;
    private byte[] keyType = null;
    private byte[] keyData = null;
    private byte[] data = null;

    private int state = -1;

    public void checkOneByteKey() throws Exception {
        if(this.getKey().length != 1) {
            throw new Exception("PSBT key type must be one byte");
        }
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getKeyType() {
        return keyType;
    }

    public void setKeyType(byte[] keyType) {
        this.keyType = keyType;
    }

    public byte[] getKeyData() {
        return keyData;
    }

    public void setKeyData(byte[] keyData) {
        this.keyData = keyData;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
