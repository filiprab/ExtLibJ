package com.samourai.wallet.cahoots;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.cahoots.psbt.PSBT;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.util.RandomUtil;
import com.samourai.wallet.util.Z85;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;

// shared payload for 2x Cahoots: Stonewallx2 or Stowaway
public abstract class Cahoots2x extends Cahoots {
    private static final Logger log = LoggerFactory.getLogger(Cahoots2x.class);

    // used by Sparrow
    protected static final String BLOCK_HEIGHT_PROPERTY = "com.sparrowwallet.blockHeight";
    protected static final long SEQUENCE_RBF_ENABLED = 4294967293L;

    protected long ts = -1L;
    protected String strID = null;
    protected PSBT psbt = null;
    protected long spendAmount = 0L;
    protected long feeAmount = 0L;
    protected HashMap<String,Long> outpoints = null;
    protected String strDestination = null;
    protected String strPayNymCollab = null;
    protected String strPayNymInit = null;
    protected int account = 0;
    protected int cptyAccount = 0;
    protected byte[] fingerprint = null;
    protected byte[] fingerprintCollab = null;
    protected String strCollabChange = null;
    protected long verifiedSpendAmount = 0; // set for step >= 3

    public Cahoots2x()    {
        super();
        outpoints = new HashMap<String,Long>();
    }

    protected Cahoots2x(Cahoots2x c)    {
        super(c);
        this.ts = c.getTS();
        this.strID = c.getID();
        this.psbt = c.getPSBT();
        this.spendAmount = c.getSpendAmount();
        this.feeAmount = c.getFeeAmount();
        this.outpoints = c.getOutpoints();
        this.strDestination = c.strDestination;
        this.strPayNymCollab = c.strPayNymCollab;
        this.strPayNymInit = c.strPayNymInit;
        this.account = c.getAccount();
        this.cptyAccount = c.getCounterpartyAccount();
        this.fingerprint = c.getFingerprint();
        this.fingerprintCollab = c.getFingerprintCollab();
        this.strCollabChange = c.getCollabChange();
    }

    public Cahoots2x(int type, NetworkParameters params, long spendAmount, String strDestination, int account) {
        super(type, params);
        this.ts = System.currentTimeMillis() / 1000L;
        SecureRandom random = RandomUtil.getSecureRandom();
        this.strID = Hex.toHexString(Sha256Hash.hash(BigInteger.valueOf(random.nextLong()).toByteArray()));
        this.spendAmount = spendAmount;
        this.outpoints = new HashMap<String, Long>();
        this.strDestination = strDestination;
        this.account = account;
    }

    public long getTS() { return ts; }

    public String getID() {
        return strID;
    }

    public PSBT getPSBT() {
        return psbt;
    }

    public void setPSBT(PSBT psbt) {
        this.psbt = psbt;
    }

    public Transaction getTransaction() {
        if (psbt == null) {
            return null;
        }
        return psbt.getTransaction();
    }

    @Override
    public long getSpendAmount() {
        return spendAmount;
    }

    @Override
    public long getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(long fee)  {
        feeAmount = fee;
    }

    @Override
    public HashMap<String, Long> getOutpoints() {
        return outpoints;
    }

    public void setOutpoints(HashMap<String, Long> outpoints) {
        this.outpoints = outpoints;
    }

    @Override
    public String getDestination() {
        return strDestination;
    }

    public void setDestination(String strDestination) {
        this.strDestination = strDestination;
    }

    public String getPayNymCollab() {
        return strPayNymCollab;
    }

    public String getPayNymInit() {
        return strPayNymInit;
    }

    public int getAccount() {
        return account;
    }

    public void setCounterpartyAccount(int account) {
        this.cptyAccount = account;
    }

    public int getCounterpartyAccount() {
        return cptyAccount;
    }

    public byte[] getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(byte[] fingerprint) {
        this.fingerprint = fingerprint;
    }

    public byte[] getFingerprintCollab() {
        return fingerprintCollab;
    }

    public void setFingerprintCollab(byte[] fingerprint) {
        this.fingerprintCollab = fingerprint;
    }

    public String getCollabChange() {
        return strCollabChange;
    }

    public void setCollabChange(String strCollabChange) {
        this.strCollabChange = strCollabChange;
    }

    public long getVerifiedSpendAmount() {
        return verifiedSpendAmount;
    }

    public void setVerifiedSpendAmount(long verifiedSpendAmount) {
        this.verifiedSpendAmount = verifiedSpendAmount;
    }

    @Override
    protected JSONObject toJSONObjectCahoots() throws Exception {
        JSONObject obj = super.toJSONObjectCahoots();
        obj.put("ts", ts);
        obj.put("id", strID);
        obj.put("spend_amount", spendAmount);
        obj.put("fee_amount", feeAmount);
        JSONArray _outpoints = new JSONArray();
        for(String outpoint : outpoints.keySet())   {
            JSONObject entry = new JSONObject();
            entry.put("outpoint", outpoint);
            entry.put("value", outpoints.get(outpoint));
            _outpoints.put(entry);
        }
        obj.put("outpoints", _outpoints);
        obj.put("dest", strDestination == null ? "" : strDestination);
        obj.put("account", account);
        obj.put("cpty_account", cptyAccount);
        if(fingerprint != null)    {
            obj.put("fingerprint", Hex.toHexString(fingerprint));
        }
        if(fingerprintCollab != null)    {
            obj.put("fingerprint_collab", Hex.toHexString(fingerprintCollab));
        }
        obj.put("psbt", psbt == null ? "" : Z85.getInstance().encode(psbt.toGZIP()));
        obj.put("collabChange", strCollabChange == null ? "" : strCollabChange);
        return obj;
    }

    @Override
    protected void fromJSONObjectCahoots(JSONObject obj) throws Exception {
        super.fromJSONObjectCahoots(obj);

        if(obj.has("psbt") && obj.has("ts") && obj.has("id") && obj.has("spend_amount"))    {
            this.ts = obj.getLong("ts");
            this.strID = obj.getString("id");
            this.spendAmount = obj.getLong("spend_amount");
            this.feeAmount = obj.getLong("fee_amount");
            JSONArray _outpoints = obj.getJSONArray("outpoints");
            for(int i = 0; i < _outpoints.length(); i++)   {
                JSONObject entry = _outpoints.getJSONObject(i);
                outpoints.put(entry.getString("outpoint"), entry.getLong("value"));
            }
            this.strDestination = obj.getString("dest");
            if(obj.has("collabChange")) {
                this.strCollabChange = obj.getString("collabChange");
            }
            else    {
                this.strCollabChange = "";
            }
            if(obj.has("account"))    {
                this.account = obj.getInt("account");
            }
            else    {
                this.account = 0;
            }
            if(obj.has("cpty_account"))    {
                this.cptyAccount = obj.getInt("cpty_account");
            }
            else    {
                this.cptyAccount = 0;
            }
            if(obj.has("fingerprint"))    {
                fingerprint = Hex.decode(obj.getString("fingerprint"));
            }
            if(obj.has("fingerprint_collab"))    {
                fingerprintCollab = Hex.decode(obj.getString("fingerprint_collab"));
            }
            this.psbt = obj.getString("psbt").equals("") ? null : PSBT.fromBytes(Z85.getInstance().decode(obj.getString("psbt")), getParams());
            this.verifiedSpendAmount = 0; // skip verifiedSpendAmount
        }
    }

    @Override
    public void signTx(HashMap<String,ECKey> keyBag) {

        Transaction transaction = psbt.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("signTx:" + transaction.toString());
        }

        for(int i = 0; i < transaction.getInputs().size(); i++)   {

            TransactionInput input = transaction.getInput(i);
            TransactionOutPoint outpoint = input.getOutpoint();
            if(keyBag.containsKey(outpoint.toString())) {

                if (log.isDebugEnabled()) {
                    log.debug("signTx outpoint:" + outpoint.toString());
                }

                ECKey key = keyBag.get(outpoint.toString());
                SegwitAddress segwitAddress = new SegwitAddress(key.getPubKey(), getParams());

                if (log.isDebugEnabled()) {
                    log.debug("signTx bech32:" + segwitAddress.getBech32AsString());
                }

                final Script redeemScript = segwitAddress.segWitRedeemScript();
                final Script scriptCode = redeemScript.scriptCode();

                long value = outpoints.get(outpoint.getHash().toString() + "-" + outpoint.getIndex());
                if (log.isDebugEnabled()) {
                    log.debug("signTx value:" + value);
                }

                TransactionSignature sig = transaction.calculateWitnessSignature(i, key, scriptCode, Coin.valueOf(value), Transaction.SigHash.ALL, false);
                final TransactionWitness witness = new TransactionWitness(2);
                witness.setPush(0, sig.encodeToBitcoin());
                witness.setPush(1, key.getPubKey());
                transaction.setWitness(i, witness);

            }

        }

        psbt.setTransaction(transaction);

    }

    public boolean isContributedAmountSufficient(long totalContributedAmount) {
        return isContributedAmountSufficient(totalContributedAmount, null);
    }

    public boolean isContributedAmountSufficient(long totalContributedAmount, Long estimatedFee) {
        return totalContributedAmount >= computeRequiredAmount(estimatedFee);
    }

    public long computeRequiredAmount() {
        return computeRequiredAmount(null);
    }

    public long computeRequiredAmount(Long estimatedFee) {
        long requiredAmount = getSpendAmount() + SamouraiWalletConst.bDust.longValue();
        if (estimatedFee != null) {
            requiredAmount += estimatedFee;
        }
        return requiredAmount;
    }
}
