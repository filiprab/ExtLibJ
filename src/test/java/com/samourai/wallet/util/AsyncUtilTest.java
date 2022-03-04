package com.samourai.wallet.util;

import io.reactivex.Completable;
import io.reactivex.Observable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

public class AsyncUtilTest {
  private static final AsyncUtil asyncUtil = AsyncUtil.getInstance();
  private int counter;

  @BeforeEach
  public void setUp() {
    this.counter = 0;
  }

  @Test
  public void blockingSingle() throws Exception {
    try {
      asyncUtil.blockingSingle(Observable.error(new IllegalArgumentException("test")));
      Assertions.assertTrue(false);
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("test",e.getMessage());
    }
  }

  @Test
  public void blockingAwait() throws Exception {
    try {
      asyncUtil.blockingAwait(Completable.fromCallable(() -> {
        throw new IllegalArgumentException("test");
      }));
      Assertions.assertTrue(false);
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("test",e.getMessage());
    }
  }

  @Test
  public void runIOAsync_success() throws Exception {
      String result = asyncUtil.blockingSingle(asyncUtil.runIOAsync(() -> "ok"));
      Assertions.assertEquals("ok", result);
  }

  @Test
  public void runIOAsync_error() throws Exception {
    try {
      asyncUtil.blockingSingle(asyncUtil.runIOAsync(() -> {
        throw new IllegalArgumentException("test");
      }));
      Assertions.assertTrue(false);
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("test",e.getMessage());
    }
  }

  @Test
  public void runIOAsync() throws Exception {
    this.counter = 0;
    Callable callable = () -> counter++;

    // simple run
    asyncUtil.blockingSingle(asyncUtil.runIOAsync(callable));
    Assertions.assertEquals(1, counter);

    // with doOnComplete
    asyncUtil.blockingSingle(asyncUtil.runIOAsync(callable)
            .doOnComplete(
                    () -> {
                      Assertions.assertEquals(2, counter);
                      counter++;
                    })
            .doOnComplete(
                    () -> {
                      Assertions.assertEquals(3, counter);
                      counter++;
                    })
    );
    Assertions.assertEquals(4, counter);
  }

  @Test
  public void runIO_success() throws Exception {
    String result = asyncUtil.runIO(() -> "ok");
    Assertions.assertEquals("ok", result);
  }

  @Test
  public void runIO_error() throws Exception {
    try {
      asyncUtil.runIO(() -> {
        throw new IllegalArgumentException("test");
      });
      Assertions.assertTrue(false);
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("test",e.getMessage());
    }
  }
}