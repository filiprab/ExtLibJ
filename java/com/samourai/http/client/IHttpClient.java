package com.samourai.http.client;

import com.samourai.wallet.api.backend.IBackendClient;
import io.reactivex.Single;

import java.util.Map;
import java.util.Optional;

public interface IHttpClient extends IBackendClient {
  void connect() throws Exception;

  <T> Single<Optional<T>> postJson(
          String url, Class<T> responseType, Map<String, String> headers, Object body);
}
