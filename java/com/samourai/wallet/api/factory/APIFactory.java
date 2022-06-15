package com.samourai.wallet.api.factory;

import com.samourai.wallet.api.Tx;
import com.samourai.wallet.bip47.BIP47Meta;
import com.samourai.wallet.send.UTXO;
import io.reactivex.subjects.BehaviorSubject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class APIFactory {
    private static String APP_TOKEN = null;         // API app token
    private static String ACCESS_TOKEN = null;      // API access token
    private static long ACCESS_TOKEN_REFRESH = 300L;  // in seconds

    private static long xpub_balance = 0L;
    private static long xpub_premix_balance = 0L;
    private static long xpub_postmix_balance = 0L;
    private static long xpub_badbank_balance = 0L;
    private static HashMap<String, Long> xpub_amounts = null;
    private static HashMap<String, List<Tx>> xpub_txs = null;
    private static HashMap<String,List<Tx>> premix_txs = null;
    private static HashMap<String,List<Tx>> postmix_txs = null;
    private static HashMap<String,List<Tx>> badbank_txs = null;
    private static HashMap<String,Integer> unspentAccounts = null;
    private static HashMap<String,Integer> unspentBIP49 = null;
    private static HashMap<String,Integer> unspentBIP84 = null;
    private static HashMap<String,Integer> unspentBIP84PreMix = null;
    private static HashMap<String,Integer> unspentBIP84PostMix = null;
    private static HashMap<String,Integer> unspentBIP84BadBank = null;
    private static HashMap<String,String> unspentPaths = null;
    private static HashMap<String, UTXO> utxos = null;
    private static HashMap<String,UTXO> utxosPreMix = null;
    private static HashMap<String,UTXO> utxosPostMix = null;
    private static HashMap<String,UTXO> utxosBadBank = null;

    private static HashMap<String, Long> bip47_amounts = null;
    public boolean walletInit = false;

    //Broadcast balance changes to the application, this will be a timestamp,
    //Balance will be recalculated when the change is broadcasted
    public BehaviorSubject<Long> walletBalanceObserver = BehaviorSubject.create();
    private static long latest_block_height = -1L;
    private static String latest_block_hash = null;
    private static long latest_block_time = -1L;

    private int lastRicochetReceiveIdx = 0;

    private static int XPUB_PREMIX = 1;
    private static int XPUB_POSTMIX = 2;
    private static int XPUB_BADBANK = 3;

    private static APIFactory instance = null;


    private synchronized boolean parseXPUB(JSONObject jsonObject) throws JSONException {

        if(jsonObject != null)  {

            HashMap<String,Integer> pubkeys = new HashMap<String,Integer>();

            if(jsonObject.has("wallet"))  {
                JSONObject walletObj = (JSONObject)jsonObject.get("wallet");
                if(walletObj.has("final_balance"))  {
                    xpub_balance = walletObj.getLong("final_balance");
                    debug("APIFactory", "xpub_balance:" + xpub_balance);
                }
            }

            if(jsonObject.has("info"))  {
                JSONObject infoObj = (JSONObject)jsonObject.get("info");
                if(infoObj.has("latest_block"))  {
                    JSONObject blockObj = (JSONObject)infoObj.get("latest_block");
                    if(blockObj.has("height"))  {
                        latest_block_height = blockObj.getLong("height");
                    }
                    if(blockObj.has("hash"))  {
                        latest_block_hash = blockObj.getString("hash");
                    }
                    if(blockObj.has("time"))  {
                        latest_block_time = blockObj.getLong("time");
                    }
                }
            }

            if(jsonObject.has("addresses"))  {

                JSONArray addressesArray = (JSONArray)jsonObject.get("addresses");
                JSONObject addrObj = null;
                for(int i = 0; i < addressesArray.length(); i++)  {
                    addrObj = (JSONObject)addressesArray.get(i);
                    if(addrObj != null && addrObj.has("final_balance") && addrObj.has("address"))  {
                        if(FormatsUtil.getInstance().isValidXpub((String)addrObj.get("address")))    {
                            xpub_amounts.put((String)addrObj.get("address"), addrObj.getLong("final_balance"));

                            WALLET_INDEX walletIndexReceive = null;
                            WALLET_INDEX walletIndexChange = null;
                            if(addrObj.getString("address").equals(BIP84Util.getInstance(context).getWallet().getAccount(0).xpubstr()) ||
                                    addrObj.getString("address").equals(BIP84Util.getInstance(context).getWallet().getAccount(0).zpubstr()))    {
                                walletIndexReceive = WALLET_INDEX.BIP84_RECEIVE;
                                walletIndexChange = WALLET_INDEX.BIP84_CHANGE;
                            }
                            else if(addrObj.getString("address").equals(BIP49Util.getInstance(context).getWallet().getAccount(0).xpubstr()) ||
                                    addrObj.getString("address").equals(BIP49Util.getInstance(context).getWallet().getAccount(0).ypubstr()))    {
                                walletIndexReceive = WALLET_INDEX.BIP49_RECEIVE;
                                walletIndexChange = WALLET_INDEX.BIP49_CHANGE;
                            }
                            else if(AddressFactory.getInstance().xpub2account().get((String) addrObj.get("address")) != null)    {
                                walletIndexReceive = WALLET_INDEX.BIP44_RECEIVE;
                                walletIndexChange = WALLET_INDEX.BIP44_CHANGE;
                            }
                            else    {
                                ;
                            }
                            if (walletIndexReceive != null) {
                                AddressFactory.getInstance().setHighestIdx(walletIndexReceive, addrObj.has("account_index") ? addrObj.getInt("account_index") : 0);
                            }
                            if (walletIndexChange != null) {
                                AddressFactory.getInstance().setHighestIdx(walletIndexChange, addrObj.has("change_index") ? addrObj.getInt("change_index") : 0);
                            }
                        }
                        else    {
                            long amount = 0L;
                            String addr = null;
                            addr = (String)addrObj.get("address");
                            amount = addrObj.getLong("final_balance");
                            String pcode = BIP47Meta.getInstance().getPCode4Addr(addr);

                            if(addrObj.has("pubkey"))    {
                                bip47Lookahead(pcode, addrObj.getString("pubkey"));
                            }

                            if(addr != null && addr.length() > 0 && pcode != null && pcode.length() > 0 && BIP47Meta.getInstance().getIdx4Addr(addr) != null)    {
                                int idx = BIP47Meta.getInstance().getIdx4Addr(addr);
                                if(amount > 0L)    {
                                    BIP47Meta.getInstance().addUnspent(pcode, idx);
                                    if(idx > BIP47Meta.getInstance().getIncomingIdx(pcode))    {
                                        BIP47Meta.getInstance().setIncomingIdx(pcode, idx);
                                    }
                                }
                                else    {
                                    if(addrObj.has("pubkey"))    {
                                        String pubkey = addrObj.getString("pubkey");
                                        if(pubkeys.containsKey(pubkey))    {
                                            int count = pubkeys.get(pubkey);
                                            count++;
                                            if(count == BIP47Meta.INCOMING_LOOKAHEAD)    {
                                                BIP47Meta.getInstance().removeUnspent(pcode, Integer.valueOf(idx));
                                            }
                                            else    {
                                                pubkeys.put(pubkey, count + 1);
                                            }
                                        }
                                        else    {
                                            pubkeys.put(pubkey, 1);
                                        }
                                    }
                                    else    {
                                        BIP47Meta.getInstance().removeUnspent(pcode, Integer.valueOf(idx));
                                    }
                                }
                                if(addr != null)  {
                                    bip47_amounts.put(addr, amount);
                                }
                            }

                        }
                    }
                }
            }

            if(jsonObject.has("txs"))  {
                xpub_txs.clear();
                List<String> seenHashes = new ArrayList<String>();

                JSONArray txArray = (JSONArray)jsonObject.get("txs");
                JSONObject txObj = null;
                for(int i = 0; i < txArray.length(); i++)  {

                    txObj = (JSONObject)txArray.get(i);
                    long height = 0L;
                    long amount = 0L;
                    long ts = 0L;
                    String hash = null;
                    String addr = null;
                    String _addr = null;

                    if(txObj.has("block_height"))  {
                        height = txObj.getLong("block_height");
                    }
                    else  {
                        height = -1L;  // 0 confirmations
                    }
                    if(txObj.has("hash"))  {
                        hash = (String)txObj.get("hash");
                    }
                    if(txObj.has("result"))  {
                        amount = txObj.getLong("result");
                    }
                    if(txObj.has("time"))  {
                        ts = txObj.getLong("time");
                    }

                    if(!seenHashes.contains(hash))  {
                        seenHashes.add(hash);
                    }

                    if(txObj.has("inputs"))  {
                        JSONArray inputArray = (JSONArray)txObj.get("inputs");
                        JSONObject inputObj = null;
                        for(int j = 0; j < inputArray.length(); j++)  {
                            inputObj = (JSONObject)inputArray.get(j);
                            if(inputObj.has("prev_out"))  {
                                JSONObject prevOutObj = (JSONObject)inputObj.get("prev_out");
                                if(prevOutObj.has("xpub"))  {
                                    JSONObject xpubObj = (JSONObject)prevOutObj.get("xpub");
                                    addr = (String)xpubObj.get("m");
                                }
                                else if(prevOutObj.has("addr") && BIP47Meta.getInstance().getPCode4Addr((String)prevOutObj.get("addr")) != null)  {
                                    _addr = (String)prevOutObj.get("addr");
                                }
                                else  {
                                    _addr = (String)prevOutObj.get("addr");
                                }
                            }
                        }
                    }

                    if(txObj.has("out"))  {
                        JSONArray outArray = (JSONArray)txObj.get("out");
                        JSONObject outObj = null;
                        for(int j = 0; j < outArray.length(); j++)  {
                            outObj = (JSONObject)outArray.get(j);
                            if(outObj.has("xpub"))  {
                                JSONObject xpubObj = (JSONObject)outObj.get("xpub");
                                addr = (String)xpubObj.get("m");
                            }
                            else  {
                                _addr = (String)outObj.get("addr");
                            }
                        }
                    }

                    if(addr != null || _addr != null)  {

                        if(addr == null)    {
                            addr = _addr;
                        }

                        Tx tx = new Tx(hash, addr, amount, ts, (latest_block_height > 0L && height > 0L) ? (latest_block_height - height) + 1 : 0);
                        if(SentToFromBIP47Util.getInstance().getByHash(hash) != null)    {
                            tx.setPaymentCode(SentToFromBIP47Util.getInstance().getByHash(hash));
                        }
                        if(BIP47Meta.getInstance().getPCode4Addr(addr) != null)    {
                            tx.setPaymentCode(BIP47Meta.getInstance().getPCode4Addr(addr));
                        }
                        if(!xpub_txs.containsKey(addr))  {
                            xpub_txs.put(addr, new ArrayList<Tx>());
                        }
                        if(FormatsUtil.getInstance().isValidXpub(addr))    {
                            xpub_txs.get(addr).add(tx);
                        }
                        else    {
                            if(!xpub_txs.containsKey(AddressFactory.getInstance().account2xpub().get(0)))    {
                                xpub_txs.put(AddressFactory.getInstance().account2xpub().get(0), new ArrayList<Tx>());

                            }
                            xpub_txs.get(AddressFactory.getInstance().account2xpub().get(0)).add(tx);
                        }

                        if(height > 0L)    {
                            RBFUtil.getInstance().remove(hash);
                        }

                    }
                }

                List<String> hashesSentToViaBIP47 = SentToFromBIP47Util.getInstance().getAllHashes();
                if(hashesSentToViaBIP47.size() > 0)    {
                    for(String s : hashesSentToViaBIP47)    {
                        if(!seenHashes.contains(s)) {
                            SentToFromBIP47Util.getInstance().removeHash(s);
                        }
                    }
                }

            }

            if(isWellFormedMultiAddr(jsonObject))    {
                try {
                    PayloadUtil.getInstance(context).serializeMultiAddr(jsonObject);
                }
                catch(Exception e) {
                    ;
                }
            }

            return true;

        }

        return false;

    }


    private synchronized JSONObject getXPUB(String[] xpubs, boolean parse) {

        String _url = WebUtil.getAPIUrl(true);

        JSONObject jsonObject  = null;

        try {

            String response = null;

            if(AppUtil.getInstance(context).isOfflineMode())    {
                response = PayloadUtil.getInstance(context).deserializeMultiAddr().toString();
            }
            else if(!TorManager.INSTANCE.isRequired())    {
                // use POST
                StringBuilder args = new StringBuilder();
                args.append("active=");
                args.append(StringUtils.join(xpubs, URLEncoder.encode("|", "UTF-8")));
                info("APIFactory", "XPUB:" + args.toString());
                args.append("&at=");
                args.append(getAccessToken());
                response = WebUtil.getInstance(context).postURL(_url + "wallet?", args.toString());
                info("APIFactory", "XPUB response:" + response);
            }
            else    {
                HashMap<String,String> args = new HashMap<String,String>();
                args.put("active", StringUtils.join(xpubs, "|"));
                info("APIFactory", "XPUB:" + args.toString());
                args.put("at", getAccessToken());
                info("APIFactory", "XPUB access token:" + getAccessToken());
                response = WebUtil.getInstance(context).tor_postURL(_url + "wallet", args);
                info("APIFactory", "XPUB response:" + response);
            }

            try {
                jsonObject = new JSONObject(response);
                if(!parse)    {
                    return jsonObject;
                }
                xpub_txs.put(xpubs[0], new ArrayList<Tx>());
                parseXPUB(jsonObject);
                parseDynamicFees_bitcoind(jsonObject);
                xpub_amounts.put(HD_WalletFactory.getInstance(context).get().getAccount(0).xpubstr(), xpub_balance - BlockedUTXO.getInstance().getTotalValueBlocked0());
                walletBalanceObserver.onNext( System.currentTimeMillis());
            }
            catch(JSONException je) {
                je.printStackTrace();
                jsonObject = null;
            }
        }
        catch(Exception e) {
            jsonObject = null;
            e.printStackTrace();
        }

        return jsonObject;
    }


    public synchronized int syncBIP47Incoming(String[] addresses) {

        JSONObject jsonObject = getXPUB(addresses, false);
        //debug("APIFactory", "sync BIP47 incoming:" + jsonObject.toString());
        int ret = 0;

        try {

            if(jsonObject != null && jsonObject.has("addresses"))  {

                HashMap<String,Integer> pubkeys = new HashMap<String,Integer>();

                JSONArray addressArray = (JSONArray)jsonObject.get("addresses");
                JSONObject addrObj = null;
                for(int i = 0; i < addressArray.length(); i++)  {
                    addrObj = (JSONObject)addressArray.get(i);
                    long amount = 0L;
                    int nbTx = 0;
                    String addr = null;
                    String pcode = null;
                    int idx = -1;
                    if(addrObj.has("address"))  {

                        if(addrObj.has("pubkey"))    {
                            addr = (String)addrObj.get("pubkey");
                            pcode = BIP47Meta.getInstance().getPCode4Addr(addr);
                            idx = BIP47Meta.getInstance().getIdx4Addr(addr);

                            BIP47Meta.getInstance().getIdx4AddrLookup().put(addrObj.getString("address"), idx);
                            BIP47Meta.getInstance().getPCode4AddrLookup().put(addrObj.getString("address"), pcode);
                        }
                        else    {
                            addr = (String)addrObj.get("address");
                            pcode = BIP47Meta.getInstance().getPCode4Addr(addr);
                            idx = BIP47Meta.getInstance().getIdx4Addr(addr);
                        }

                        if(addrObj.has("final_balance"))  {
                            amount = addrObj.getLong("final_balance");
                            if(amount > 0L)    {
                                BIP47Meta.getInstance().addUnspent(pcode, idx);
                                //info("APIFactory", "BIP47 incoming amount:" + idx + ", " + addr + ", " + amount);
                            }
                            else    {
                                if(addrObj.has("pubkey"))    {
                                    String pubkey = addrObj.getString("pubkey");
                                    if(pubkeys.containsKey(pubkey))    {
                                        int count = pubkeys.get(pubkey);
                                        count++;
                                        if(count == 3)    {
                                            BIP47Meta.getInstance().removeUnspent(pcode, Integer.valueOf(idx));
                                            //info("APIFactory", "BIP47 remove unspent:" + pcode + ":" + idx);
                                        }
                                        else    {
                                            pubkeys.put(pubkey, count + 1);
                                        }
                                    }
                                    else    {
                                        pubkeys.put(pubkey, 1);
                                    }
                                }
                                else    {
                                    BIP47Meta.getInstance().removeUnspent(pcode, Integer.valueOf(idx));
                                }
                            }
                        }
                        if(addrObj.has("n_tx"))  {
                            nbTx = addrObj.getInt("n_tx");
                            if(nbTx > 0)    {
                                if(idx > BIP47Meta.getInstance().getIncomingIdx(pcode))    {
                                    BIP47Meta.getInstance().setIncomingIdx(pcode, idx);
                                }
                                //info("APIFactory", "sync receive idx:" + idx + ", " + addr);
                                ret++;
                            }
                        }

                    }
                }

            }

        }
        catch(Exception e) {
            jsonObject = null;
            e.printStackTrace();
        }

        return ret;
    }

}
