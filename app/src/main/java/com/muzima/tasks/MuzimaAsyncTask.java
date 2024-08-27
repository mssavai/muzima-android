/*
 * Copyright (c) The Trustees of Indiana University, Moi University
 * and Vanderbilt University Medical Center. All Rights Reserved.
 *
 * This version of the code is licensed under the MPL 2.0 Open Source license
 * with additional health care disclaimer.
 * If the user is an entity intending to commercialize any application that uses
 * this code in a for-profit venture, please contact the copyright holder.
 */

package com.muzima.tasks;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class MuzimaAsyncTask<INPUT, PROGRESS, OUTPUT> {
    private boolean cancelled = false;
    private Future<OUTPUT> mFuture;
    private OnProgressListener<PROGRESS> onProgressListener;

    /* @see #execute(Object) */
    public MuzimaAsyncTask<INPUT, PROGRESS, OUTPUT> execute() {
        return execute(null);
    }

    /**
     * Starts all
     * @param input Data you want to work with in the background
     */
    public MuzimaAsyncTask<INPUT, PROGRESS, OUTPUT> execute(final INPUT input) {
        onPreExecute();

        ExecutorService executorService = AsyncWorker.getInstance().getExecutorService();
        mFuture = executorService.submit(new Callable<OUTPUT>() {
            @Override
            public OUTPUT call() {
                OUTPUT output = null;
                try {
                    output = doInBackground(input);
                    OUTPUT finalOutput = output;
                    if (!isCancelled())
                        AsyncWorker.getInstance().getHandler().post(() -> onPostExecute(finalOutput));
                } catch (final Exception e) {
                    Log.e(getClass().getSimpleName(), "Encountered an exception", e);
                    AsyncWorker.getInstance().getHandler().post(() -> onBackgroundError(e));
                }
                return output;
            }
        });

        return this;
    }

    public OUTPUT get() {
        try {
            if (mFuture == null)
                throw new RuntimeException("Task not executed before calling get()");
            else
                return mFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public OUTPUT get(long timeout, TimeUnit timeUnit) throws Exception {
        if (mFuture == null)
            throw new Exception("Task not executed before calling get()");
        else
            return mFuture.get(timeout, timeUnit);
    }

    /**
     * Call to publish progress from background
     * @param progress  Progress made
     */
    protected void publishProgress(final PROGRESS progress) {
        AsyncWorker.getInstance().getHandler().post(() -> {
            onProgress(progress);
            if (onProgressListener != null)
                onProgressListener.onProgress(progress);
        });
    }

    protected void onProgress(final PROGRESS progress) {
    }

    /**
     * Call to cancel background work
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     *
     * @return Returns true if the background work should be cancelled
     */
    protected boolean isCancelled() {
        return cancelled;
    }

    /**
     * Call this method after cancelling background work
     */
    protected void onCancelled() {
        AsyncWorker.getInstance().getHandler().post(() -> {
            if (onCancelledListener != null)
                onCancelledListener.onCancelled();
        });
    }

    /**
     * Work which you want to be done on UI thread before {@link #doInBackground(Object)}
     */
    protected abstract void onPreExecute();

    /**
     * Work on background
     * @param input Input data
     * @return Output data
     * @throws Exception Any uncaught exception which occurred while working in background. If
     * any occurs, {@link #onBackgroundError(Exception)} will be executed (on the UI thread)
     */
    protected abstract OUTPUT doInBackground(INPUT input) throws Exception;

    /**
     * Work which you want to be done on UI thread after {@link #doInBackground(Object)}
     * @param output Output data from {@link #doInBackground(Object)}
     */
    protected abstract void onPostExecute(OUTPUT output);

    /**
     * Triggered on UI thread if any uncaught exception occurred while working in background
     * @param e Exception
     * @see #doInBackground(Object)
     */
    protected abstract void onBackgroundError(Exception e);

    public void setOnProgressListener(OnProgressListener<PROGRESS> onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    private OnCancelledListener onCancelledListener;

    public void setOnCancelledListener(OnCancelledListener onCancelledListener) {
        this.onCancelledListener = onCancelledListener;
    }

    public interface OnProgressListener<PROGRESS> {
        void onProgress(PROGRESS progress);
    }

    public interface OnCancelledListener {
        void onCancelled();
    }
}
