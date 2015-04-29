package jp.gcreate.sample.asynctaskloadersample;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

/**
 * キャッシュ機構を実装。
 *
 * 処理結果を返すdeliverResultをオーバーライドし、処理結果をキャッシュに保存する。
 *
 * onStartLoadingでキャッシュがあればdeliverResultでキャッシュを返すようにする。
 */
public class MyAsyncTaskLoader extends AsyncTaskLoader<String> {
    private static final String TAG = "MyAsyncTaskLoader";
    private int mCount;
    private String mCachedResult;

    public MyAsyncTaskLoader(Context context) {
        this(context, 10);
    }

    public MyAsyncTaskLoader(Context context, int count){
        super(context);
        mCount = count;
        Log.d(TAG, this + " constructor called.");
    }

    @Override
    public String loadInBackground() {
        for (int i = 0; i < mCount; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, this + " loadInBackground count:" + i);
        }
        return Integer.toString(mCount);
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, this + " onStartLoading.");
        if(mCachedResult != null){
            Log.d(TAG, this + " has cached result:" + mCachedResult);
            //キャッシュデータがある場合はキャッシュデータを返す
            deliverResult(mCachedResult);
            //非同期処理をする必要が無いため処理を抜ける
            return;
        }
        Log.d(TAG, this + " forceLoad.");
        forceLoad();
    }

    @Override
    public void deliverResult(String data) {
        Log.d(TAG, this + " deliverResult :" + data);
        //Loaderがお役御免になっている場合（resetされたLoaderは2度と使われない・・・のだと思う）
        //LoaderManagerがLoaderをresetした後にここに来る可能性はある（非同期処理があまりにも長い場合とか？）
        if(isReset()){
            destroy();
            return;
        }
        //処理結果をキャッシュに入れる
        mCachedResult = data;

        //Loaderが開始状態なら処理結果を通知する
        if(isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onReset() {
        Log.d(TAG, this + " onReset. This loader will never used.");
        destroy();
    }

    private void destroy(){
        mCachedResult = null;
    }
}