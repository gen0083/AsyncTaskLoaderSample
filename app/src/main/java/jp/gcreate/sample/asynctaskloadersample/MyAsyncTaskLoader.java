package jp.gcreate.sample.asynctaskloadersample;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

/**
 * 必要最小限のメソッドのみ実装した状態
 * MyAsyncTaskLoaderのインスタンスは作成されるが、非同期処理（loadInBackground）は実行されない。
 */
public class MyAsyncTaskLoader extends AsyncTaskLoader<String> {
    private int mCount;

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
        for (int i = 0; i < mCount; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("test", this + " loadInBackground count:" + i);
        }
        return Integer.toString(mCount);
    }
}