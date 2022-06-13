package com.samourai.wallet.api.paynym;

import com.samourai.http.client.IHttpClient;
import com.samourai.wallet.api.backend.IBackendClient;
import com.samourai.wallet.api.backend.beans.HttpException;
import com.samourai.wallet.api.paynym.beans.*;
import com.samourai.wallet.bip47.BIP47Meta;
import com.samourai.wallet.bip47.BIP47UtilGeneric;
import com.samourai.wallet.bip47.rpc.BIP47Wallet;
import com.samourai.wallet.bip47.rpc.PaymentAddress;
import com.samourai.wallet.bip47.rpc.PaymentCode;
import com.samourai.wallet.bip47.rpc.java.Bip47UtilJava;
import com.samourai.wallet.util.JSONUtils;
import com.samourai.wallet.util.MessageSignUtilGeneric;
import io.reactivex.Observable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaynymApi {
  private Logger log = LoggerFactory.getLogger(PaynymApi.class);

  private static final String URL_TOKEN = "/token";
  private static final String URL_CREATE = "/create";
  private static final String URL_CLAIM = "/claim";
  private static final String URL_ADD = "/nym/add";
  private static final String URL_NYM = "/nym";
  private static final String URL_FOLLOW = "/follow";
  private static final String URL_UNFOLLOW = "/unfollow";

  private IHttpClient httpClient;
  private String urlServer;
  private BIP47UtilGeneric bip47Util;

  public PaynymApi(IHttpClient httpClient, String urlServer, BIP47UtilGeneric bip47Util) {
    this.httpClient = httpClient;
    this.urlServer = urlServer;
    this.bip47Util = bip47Util;

    if (log.isDebugEnabled()) {
      log.debug("urlServer=" + urlServer);
    }
  }

  public Observable<String> getToken(String paynymCode) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("getToken");
    }
    Map<String,String> headers = computeHeaders(null);
    String url = urlServer+URL_TOKEN;
    GetTokenRequest request = new GetTokenRequest(paynymCode);
    return httpClient.postJson(url, GetTokenResponse.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Observable.error(responseError(throwable));
            })
            .map(
            responseOpt -> {
              GetTokenResponse response = responseOpt.get();
              if (StringUtils.isEmpty(response.token)) {
                throw new Exception("Invalid getToken response");
              }
              return response.token;
            }
    );
  }

  public Observable<CreatePaynymResponse> createPaynym(String paynymCode) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("createPaynym");
    }
    Map<String,String> headers = computeHeaders(null);
    String url = urlServer+URL_CREATE;
    CreatePaynymRequest request = new CreatePaynymRequest(paynymCode);
    return httpClient.postJson(url, CreatePaynymResponse.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Observable.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public Observable<ClaimPaynymResponse> claim(String paynymToken, BIP47Wallet bip47Wallet) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("claim");
    }
    Map<String,String> headers = computeHeaders(paynymToken);
    String url = urlServer+URL_CLAIM;
    String signature = computeSignature(bip47Wallet, paynymToken);
    ClaimPaynymRequest request = new ClaimPaynymRequest(signature);
    return httpClient.postJson(url, ClaimPaynymResponse.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Observable.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public Observable<AddPaynymResponse> addPaynym(String paynymToken, BIP47Wallet bip47Wallet) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("addPaynym");
    }
    Map<String,String> headers = computeHeaders(paynymToken);
    String url = urlServer+URL_ADD;
    String nym = bip47Wallet.getAccount(0).getPaymentCode();
    String code = bip47Util.getFeaturePaymentCode(bip47Wallet).toString();
    String signature = computeSignature(bip47Wallet, paynymToken);
    AddPaynymRequest request = new AddPaynymRequest(nym, code, signature);
    return httpClient.postJson(url, AddPaynymResponse.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Observable.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public Observable<GetNymInfoResponse> getNymInfo(String paynymCode) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("getNym");
    }
    Map<String,String> headers = computeHeaders(null);
    String url = urlServer+URL_NYM;
    GetNymInfoRequest request = new GetNymInfoRequest(paynymCode);
    return httpClient.postJson(url, GetNymInfoResponse.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Observable.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public Observable follow(String paynymToken, BIP47Wallet bip47Wallet, String paymentCodeTarget) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("follow");
    }
    Map<String,String> headers = computeHeaders(paynymToken);
    String url = urlServer+URL_FOLLOW;
    String signature = computeSignature(bip47Wallet, paynymToken);
    FollowPaynymRequest request = new FollowPaynymRequest(paymentCodeTarget, signature);
    return httpClient.postJson(url, Object.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Observable.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public Observable unfollow(String paynymToken, BIP47Wallet bip47Wallet, String paymentCodeTarget) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("unfollow");
    }
    Map<String,String> headers = computeHeaders(paynymToken);
    String url = urlServer+URL_UNFOLLOW;
    String signature = computeSignature(bip47Wallet, paynymToken);
    UnfollowPaynymRequest request = new UnfollowPaynymRequest(paymentCodeTarget, signature);
    return httpClient.postJson(url, Object.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Observable.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public void syncPcode(BIP47Wallet bip47Wallet, String pCode) {
    try {
      PaymentCode paymentCode = new PaymentCode(pCode);
      int idx = 0;
      boolean loop = true;
      ArrayList<String> addrs = new ArrayList<>();
      while (loop) {
        addrs.clear();
        for (int i = 0; i < idx + 20; i++) {
          PaymentAddress sendAddress = Bip47UtilJava.getInstance().getSendAddress(bip47Wallet, paymentCode, i, bip47Wallet.getParams());
          BIP47Meta.getInstance().getIdx4AddrLookup().put(Bip47UtilJava.getInstance().getSendPubKey(bip47Wallet, paymentCode, i, bip47Wallet.getParams()), i);
          BIP47Meta.getInstance().getPCode4AddrLookup().put(Bip47UtilJava.getInstance().getSendPubKey(bip47Wallet, paymentCode, i, bip47Wallet.getParams()), paymentCode.toString());
          addrs.add(Bip47UtilJava.getInstance().getSendPubKey(bip47Wallet, paymentCode, i, bip47Wallet.getParams()));
        }

        int nb = 0;
        //val nb = APIFactory.getInstance().syncBIP47Outgoing(s)
        //                        Log.i("BIP47Activity", "sync send idx:" + idx + ", nb == " + nb);
        if (nb == 0) {
          loop = false;
        }
        idx += 20;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
/*
  public fun syncPcode(pcode: String) {
    try {
      val payment_code = PaymentCode(pcode)
      var idx = 0
      var loop = true
      val addrs = ArrayList<String>()
      while (loop) {
        addrs.clear()
        for (i in idx until idx + 20) {
//                            Log.i("BIP47Activity", "sync receive from " + i + ":" + BIP47Util.getInstance(BIP47Activity.this).getReceivePubKey(payment_code, i));
          BIP47Meta.getInstance().idx4AddrLookup[BIP47Util.getInstance(context).getReceivePubKey(payment_code, i)] = i
          BIP47Meta.getInstance().pCode4AddrLookup[BIP47Util.getInstance(context).getReceivePubKey(payment_code, i)] = payment_code.toString()
          addrs.add(BIP47Util.getInstance(context).getReceivePubKey(payment_code, i))
          //                            Log.i("BIP47Activity", "p2pkh " + i + ":" + BIP47Util.getInstance(BIP47Activity.this).getReceiveAddress(payment_code, i).getReceiveECKey().toAddress(SamouraiWallet.getInstance().getCurrentNetworkParams()).toString());
        }
        val s = addrs.toTypedArray()
        val nb = APIFactory.getInstance(context).syncBIP47Incoming(s)
        //                        Log.i("BIP47Activity", "sync receive idx:" + idx + ", nb == " + nb);
        if (nb == 0) {
          loop = false
        }
        idx += 20
      }
      idx = 0
      loop = true
      BIP47Meta.getInstance().setOutgoingIdx(pcode, 0)
      while (loop) {
        addrs.clear()
        for (i in idx until idx + 20) {
          val sendAddress = BIP47Util.getInstance(context).getSendAddress(payment_code, i)
          //                            Log.i("BIP47Activity", "sync send to " + i + ":" + sendAddress.getSendECKey().toAddress(SamouraiWallet.getInstance().getCurrentNetworkParams()).toString());
//                            BIP47Meta.getInstance().setOutgoingIdx(payment_code.toString(), i);
          BIP47Meta.getInstance().idx4AddrLookup[BIP47Util.getInstance(context).getSendPubKey(payment_code, i)] = i
          BIP47Meta.getInstance().pCode4AddrLookup[BIP47Util.getInstance(context).getSendPubKey(payment_code, i)] = payment_code.toString()
          addrs.add(BIP47Util.getInstance(context).getSendPubKey(payment_code, i))
        }
        val s = addrs.toTypedArray()
        val nb = APIFactory.getInstance(context).syncBIP47Outgoing(s)
        //                        Log.i("BIP47Activity", "sync send idx:" + idx + ", nb == " + nb);
        if (nb == 0) {
          loop = false
        }
        idx += 20
      }
      BIP47Meta.getInstance().pruneIncoming()
      PayloadUtil.getInstance(context.applicationContext).saveWalletToJSON(CharSequenceX(AccessFactory.getInstance(context.applicationContext).guid + AccessFactory.getInstance(context.applicationContext).pin))
    } catch (ioe: IOException) {
    } catch (je: JSONException) {
    } catch (de: DecryptionException) {
    } catch (nse: NotSecp256k1Exception) {
    } catch (ikse: InvalidKeySpecException) {
    } catch (ike: InvalidKeyException) {
    } catch (nsae: NoSuchAlgorithmException) {
    } catch (nspe: NoSuchProviderException) {
    } catch (mle: MnemonicException.MnemonicLengthException) {
    } catch (ex: Exception) {
    }
  }

*/
  protected String computeSignature(BIP47Wallet bip47Wallet, String payNymToken) {
    return MessageSignUtilGeneric.getInstance().signMessage(bip47Util.getNotificationAddress(bip47Wallet).getECKey(), payNymToken);
  }

  protected Throwable responseError(Throwable throwable) {
    if (throwable instanceof HttpException) {
      // parse PaynymErrorResponse.message
      String responseBody = ((HttpException) throwable).getResponseBody();
      try {
        PaynymErrorResponse paynymErrorResponse = JSONUtils.getInstance().getObjectMapper().readValue(responseBody, PaynymErrorResponse.class);
        return new Exception(paynymErrorResponse.message);
      } catch (Exception e) {
        // unexpected response
      }
    }
    return throwable;
  }

  protected Map<String,String> computeHeaders(String paynymToken) throws Exception {
    Map<String,String> headers = new HashMap<String, String>();
    if (paynymToken != null) {
      headers.put("auth-token", paynymToken);
    }
    return headers;
  }

  protected IBackendClient getHttpClient() {
    return httpClient;
  }

  public String getUrlServer() {
    return urlServer;
  }

}
