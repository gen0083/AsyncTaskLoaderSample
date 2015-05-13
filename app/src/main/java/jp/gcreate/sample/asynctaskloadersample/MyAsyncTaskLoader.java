package jp.gcreate.sample.asynctaskloadersample;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.os.OperationCanceledException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * AsyncTaskLoaderで途中経過の通知を行う
 *
 * LocalBroadcastを利用するバージョン（アプリ外で利用することはないので、Context#sendBroadcast()使うより適切）
 * MyAsyncTaskLoaderはbroadcastで途中のデータを配信しているだけなので、呼び出し元の状態に依存することはない。
 */
public class MyAsyncTaskLoader extends AsyncTaskLoader<String> {
    private static final String TAG = "MyAsyncTaskLoader";
    private int mCount;
    private String mCachedResult;
    private LocalBroadcastManager mLocalBroadcastManager;

    public MyAsyncTaskLoader(Context context) {
        this(context, 10);
    }

    public MyAsyncTaskLoader(Context context, int count){
        super(context);
        mCount = count;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        Log.d(TAG, this + " constructor called." + dumpState());
    }

    @Override
    public String loadInBackground() {
        for (int i = 0; i < mCount; i++) {
            if(isLoadInBackgroundCanceled()){
                Log.d(TAG, this + " loadInBackground canceled and throw OperationCanceledException.");
                throw new OperationCanceledException();
            }

            //ProgressをLocalBroadcastManagerを通じて通知する
            Intent intent = new Intent(MainActivity.ACTION_PROGRESS)
                    .putExtra(MainActivity.BROADCAST_PROGRESS, "Progress:" + Integer.toString(i));
            mLocalBroadcastManager.sendBroadcast(intent);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, this + " loadInBackground count:" + i + dumpState());
        }
        return Integer.toString(mCount);
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, this + " onStartLoading." + dumpState());
        if(mCachedResult != null){
            Log.d(TAG, this + " has cached result:" + mCachedResult + dumpState());
            deliverResult(mCachedResult);
            return;
        }
        Log.d(TAG, this + " forceLoad." + dumpState());
        forceLoad();
    }

    @Override
    public void deliverResult(String data) {
        Log.d(TAG, this + " deliverResult :" + data + dumpState());
        if(isReset()){
            destroy();
            return;
        }

        Log.d(TAG, this + " saved results to cache." + dumpState());
        mCachedResult = data;

        if(isStarted()) {
            Log.d(TAG, this + " delivers results." + dumpState());
            super.deliverResult(data);
        }
    }

    private void destroy(){
        mCachedResult = null;
    }

    private String dumpState() {
        return " [state = isStarted:"
                + isStarted()
                + ", isReset:"
                + isReset()
                + ", isAbandoned:"
                + isAbandoned()
                + ", isLoad~Canceled:"
                + isLoadInBackgroundCanceled() +" ]";
    }
}