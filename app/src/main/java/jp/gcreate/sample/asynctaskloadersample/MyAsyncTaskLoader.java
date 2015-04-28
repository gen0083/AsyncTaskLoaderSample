package jp.gcreate.sample.asynctaskloadersample;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

/**
 * onStartLoadingでforceLoad()を実行。
 * ようやく非同期処理が開始される。
 * ActivityでgetLoaderManager().initLoad().forceLoad()とすることでも非同期処理は実行されるが、Activityは
 * Loaderの状態を知り得ないので、毎回強制的に非同期処理を開始することになって効率が良くない。
 * （すでに非同期処理が終わっていたとしても、また始めから非同期処理を開始してしまう）
 *
 * 現状の実装で、画面を回転した場合は既に処理済みの結果がActivityのonLoadFinishedに直接通知される。
 * しかしホーム画面に一度戻った後で帰ってくると、再度非同期処理が開始される（MyAsyncTaskLoaderのインスタンスは同一にもかかわらず）。
 * これはLoaderManagerが画面回転の時は前の処理結果をそのまま返すようになっているためである。
 * (LoaderManagerは画面の回転のみ特別に扱っている。FragmentのsetRetainInstance(true)に対応するため？）
 *
 * ホーム画面から再度アプリに戻った際に非同期処理が再度走るが、その走った結果はActivityのonLoadFinishedに通知されない。
 * つまり非同期処理を走らせるだけムダである。
 * LoaderManagerはAsyncTaskLoaderの状態を、AsyncTaskLoaderの実際に動きとは別に管理している。（LoaderInfo）
 * 画面回転時はLoaderManagerが持っている処理結果をActivityのonLoadFinishedに直接渡している。
 *
 * ホーム画面から戻ってきた際はAsyncTaskLoaderの状態によって処理を行う。
 * 非同期処理の最中であればそのまま非同期処理の終了を待つが、Activityが後ろに回っている状態でAsyncTaskLoaderの処理が
 * 終わると、単にAsyncTaskLoaderの状態を終了状態にするだけである。（処理結果については何もしない）
 * そのためActivityがinitLoaderを呼ぶと、既に作成済みのAsyncTaskLoaderがある→停止状態である→じゃあ処理始めろとなり、
 * MyAsyncTaskLoaderのonStartLoadingを呼ぶ。
 * MyAsyncTaskLoaderはonStartLoadingが呼ばれるとforceLoadを使って非同期処理を強制的に開始するため、ムダに非同期処理が
 * 走ってしまうのである。
 * これにはAsyncTaskLoaderにキャッシュデータを持たせて、キャッシュがあるときは非同期処理を開始せず、キャッシュをそのまま
 * 返すようにすればよい。
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
        forceLoad();
    }
}