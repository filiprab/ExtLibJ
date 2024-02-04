package com.samourai.wallet.util;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class AsyncUtil {
    private static AsyncUtil instance;
    private static final ThreadUtil threadUtil = ThreadUtil.getInstance();

    public static AsyncUtil getInstance() {
        if(instance == null) {
            instance = new AsyncUtil();
        }
        return instance;
    }

    public <T> T unwrapException(Callable<T> c) throws Exception {
        try {
            return c.call();
        }
        catch (RuntimeException e) {
            // blockingXXX wraps errors with RuntimeException, unwrap it
            throw unwrapException(e);
        }
    }

    public Exception unwrapException(Exception e) throws Exception {
        if (e.getCause() != null && e instanceof Exception) {
            throw (Exception)e.getCause();
        }
        throw e;
    }

    public <T> T blockingGet(Single<T> o) throws Exception {
        try {
            return unwrapException(() -> o.blockingGet());
        } catch (ExecutionException e) {
            // blockingGet(threadUtil.runWithTimeoutAndRetry()) wraps InterruptedException("exit (done)")
            // with ExecutionException, unwrap it
            throw unwrapException(e);
        }
    }

    public <T> T blockingGet(Single<T> o, long timeoutMs) throws Exception {
        Callable<T> callable = () -> blockingGet(o);
        return blockingGet(threadUtil.runWithTimeout(callable, timeoutMs));
    }

    public <T> T blockingLast(Observable<T> o) throws Exception {
        return unwrapException(() -> o.blockingLast());
    }

    public void blockingAwait(Completable o) throws Exception {
        Callable<Optional> callable = () -> {
            o.blockingAwait();
            return Optional.empty();
        };
        unwrapException(callable);
    }

    public void blockingAwait(Completable o, long timeoutMs) throws Exception {
        Callable<Optional> callable = () -> {
            o.blockingAwait();
            return Optional.empty();
        };
        blockingGet(threadUtil.runWithTimeout(callable, timeoutMs));
    }

    public <T> Single<T> timeout(Single<T> o, long timeoutMs) {
        try {
            return Single.just(blockingGet(o, timeoutMs));
        } catch (Exception e) {
            return Single.error(e);
        }
    }

    public Completable timeout(Completable o, long timeoutMs) {
        try {
            return Completable.fromCallable(() -> {
                blockingAwait(o, timeoutMs);
                return Optional.empty();
            });
        } catch (Exception e) {
            return Completable.error(e);
        }
    }

    public <T> Single<T> runIOAsync(final Callable<T> callable) {
        return Single.fromCallable(() -> callable.call()).subscribeOn(Schedulers.io());
    }

    public Completable runIOAsyncCompletable(final Action action) {
        return Completable.fromAction(() -> action.run()).subscribeOn(Schedulers.io());
    }

    public <T> T runIO(final Callable<T> callable) throws Exception {
        return blockingGet(runIOAsync(callable));
    }

    public void runIO(final Action action) throws Exception {
        blockingAwait(runIOAsyncCompletable(action));
    }

    public Future<?> runAsync(Runnable runnable) {
        return threadUtil.getExecutorService().submit(runnable);
    }

    public <T> Future<T> runAsync(Callable<T> runnable) {
        return threadUtil.getExecutorService().submit(runnable);
    }

    /**
     * Run loop (without timeout) every <loopFrequencyMs>
     */
    public <T> Single<T> loopUntilSuccess(
            Callable<Optional<T>> doLoop, long retryFrequencyMs, Supplier<Boolean> isDoneOrNull) {
        return Single.fromCallable(() -> {
            while (true) {
                if (isDoneOrNull != null && isDoneOrNull.get()) {
                    throw new InterruptedException("exit (done)");
                }
                long loopStartTime = System.currentTimeMillis();
                try {
                    // run loop (without timeout)
                    Optional<T> opt = doLoop.call();
                    if (opt.isPresent()) {
                        // value found
                        return opt.get();
                    }
                    // if no value, continue looping
                } catch (TimeoutException e) {
                    // continue looping
                }

                // wait delay before next loop
                long loopSpentTime = System.currentTimeMillis() - loopStartTime;
                long waitTime = retryFrequencyMs - loopSpentTime;
                //if (log.isDebugEnabled()) {
                //    log.debug("runWithTimeoutFrequency(): loop timed out, loopSpentTime=" + loopSpentTime + ", waitTime=" + waitTime);
                //}
                if (waitTime > 0) {
                    synchronized (this) {
                        try {
                            wait(waitTime);
                        } catch (InterruptedException ee) {
                        }
                    }
                }
            }
        });
    }

    public <T> Single<T> loopUntilSuccess(Callable<Optional<T>> doLoop, long retryFrequencyMs) {
        return loopUntilSuccess(doLoop, retryFrequencyMs, null);
    }
}
