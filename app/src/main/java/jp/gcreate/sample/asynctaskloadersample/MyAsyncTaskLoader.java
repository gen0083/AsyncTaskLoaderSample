package jp.gcreate.sample.asynctaskloadersample;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

/**
 * onStartLoadingをオーバーライド
 * 起動したとき（MainActivityが最初にinitLoaderを呼んだとき）にだけonStartLoadingが呼ばれる。
 * LoaderManagerはAsyncTaskLoaderを作成すると同時に、その状態を「開始状態」として管理している。
 * 非同期処理が動いているであろうから、LoaderManagerはその処理が終わるのを待っているだけ。
 * 相変わらずloadInBackgroundの処理は始まらないが、非同期処理を開始するにはforceLoad()を呼ぶ必要がある。
 */
public class MyAsyncTaskLoader extends AsyncTaskLoader<String> {
    private static final String TAG = "MyAsyncTaskLoader";
    private int mCount;

    public MyAsyncTaskLoader(Context context) {
        this(context, 10);
    }

    public MyAsyncTaskLoader(Context context, int count){
        super(context);
        mCount = count;
        Log.d(TAG, this + " constructor called." + dumpState());
    }

    @Override
    public String loadInBackground() {
        Log.d(TAG, this + " loadInBackground start." + dumpState());
        for (int i = 0; i < mCount; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, this + " loadInBackground count:" + i + dumpState());
        }
        return Integer.toString(mCount);
    }

    private String dumpState() {
        return " [state = isStarted:"
                + isStarted()
                + ", isReset:"
                + isReset()
                + ", isAbandoned:"
                + isAbandoned() + " ]";
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, this + " onStartLoading." + dumpState());
        super.onStartLoading();
    }
}