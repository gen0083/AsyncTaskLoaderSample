package jp.gcreate.sample.asynctaskloadersample;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.OperationCanceledException;
import android.util.Log;

/**
 * jp.gcreate.sample.asynctaskloadersample
 */
public class MyAsyncTaskLoader extends AsyncTaskLoader<String> {
    private int mCount;
    private String mCachedResult;

    public MyAsyncTaskLoader(Context context) {
        this(context, 10);
    }

    public MyAsyncTaskLoader(Context context, int count){
        super(context);
        mCount = count;
        Log.d("test", this + " constructor called.");
    }

    @Override
    public String loadInBackground() {
        try {
            for (int i = 0; i < mCount; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("test", this + " loadInBackground count:" + i);
            }
            return Integer.toString(mCount);
        }catch(OperationCanceledException e){
            Log.d("test", this + " isLoadInBackgroundCanceled.");
            return "onCancel";
        }
    }

    @Override
    public void onCanceled(String data) {
        super.onCanceled(data);
        Log.d("test", this + " onCanceled :" + data);
        mCachedResult = data;
    }

    @Override
    public void deliverResult(String data) {
        // ローダーをリセット（再読み込み）するタイミング
        if(isReset()){
            Log.d("test", this + " deliverResult isReset");
            if(mCachedResult != null) mCachedResult = null;
            // 結果を通知しなくていいので処理を抜ける
            return;
        }
        // ローダーの結果をキャッシュさせる（終了時にコールバック先が死んでる場合に備えて）
        Log.d("test", this + " deliverResult result cached.");
        mCachedResult = data;
        // ローダーが読み込み処理を普通に終わらせたタイミング
        if(isStarted()) {
            Log.d("test", this + " deliverResult isStarted (normally finished) :" + data);
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        Log.d("test", getClass().getSimpleName() + " onStartLoading");
        //キャッシュがある場合（処理結果ができあがったけど、通知先が死んでたというルート）
        if(mCachedResult != null){
            Log.d("test", this + " onStartLoading has cached result:" + mCachedResult);
            deliverResult(mCachedResult);
            //すでに処理結果があるので、それを通知する＝非同期処理を始める必要が無いため処理を抜ける
            return;
        }
        boolean isChange = takeContentChanged();
        Log.d("test", this + " takeContentChanged:" + isChange
                + ", Cache:" + mCachedResult);
        isChange |= (mCachedResult == null);
        if(isChange){
            Log.d("test", this + " onStartLoading forceLoad.");
            forceLoad();
        }
    }

    @Override
    protected boolean onCancelLoad() {
        Log.d("test", this + " onCancelLoad");
        return super.onCancelLoad();
    }

    @Override
    public void cancelLoadInBackground() {
        Log.d("test", this + " cancelLoadInBackground");
        super.cancelLoadInBackground();
    }

    @Override
    protected void onStopLoading() {
        Log.d("test", this + " onStopLoading. cached data:" + mCachedResult);
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        Log.d("test", this + " onReset. cached data:" + mCachedResult);
        super.onReset();
    }
}