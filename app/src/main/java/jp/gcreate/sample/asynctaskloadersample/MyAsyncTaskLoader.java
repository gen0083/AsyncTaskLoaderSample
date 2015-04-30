package jp.gcreate.sample.asynctaskloadersample;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

/**
 * 非同期処理をキャンセルできるようにする。
 * 問題点：restartLoaderを呼ぶと、以前の非同期処理が終わらないとrestartLoaderした非同期処理が始まらない。
 * （restartLoaderを2回連続で呼び出す（rerun asynctaskloaderボタンを連続で押す）とよく分かる）
 * restartLoaderしたらすぐに新しい非同期処理が始まって欲しい。
 * （使わない処理結果が出るまで待つのは、時間とリソースのムダ使いである）
 *
 * そこでまずどんなメソッドがどういう風に呼ばれているかを確認する。
 * 関係しそうなメソッドをオーバーライドして、ログを吐くようにする。
 *
 * ログの結果とソースコードを読んで分かったこと
 * + onStopLoadingはActivityがonStopになったときに呼ばれる（画面回転は除く）
 * + キャンセルの処理(restartLoader実行時)は、まずonCancelLoadが呼ばれる
 * + AsyncTaskLoader.onCancelLoadでLoaderの状態に合わせてキャンセル処理を行う
 * + 実際のキャンセル処理はcancelLoadInBackgroundメソッドで行われる
 * + しかしAsyncTaskLoader.cancelLoadInBackgroundでは何もしていない
 * + すなわち実際にloadInBackgroundの処理を止めるのは自分で実装しなければならない
 * + AsyncTaskLoader.onCancelLoadを経ていれば、loadInBackgroundの処理結果は最終的にonCanceledに通知される
 * + LoaderManagerがうまいこと管理してくれているので、restartLoader呼んだ数だけ非同期処理が乱立するわけではない
 * （同時に何個か動きはするけど（おそらく同時に実行される非同期処理は4個までかな？））
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
        Log.d(TAG, this + " constructor called." + dumpState());
    }

    @Override
    public String loadInBackground() {
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

    @Override
    protected void onStartLoading() {
        Log.d(TAG, this + " onStartLoading." + dumpState());
        if(mCachedResult != null){
            Log.d(TAG, this + " has cached result:" + mCachedResult + dumpState());
            //キャッシュデータがある場合はキャッシュデータを返す
            deliverResult(mCachedResult);
            //非同期処理をする必要が無いため処理を抜ける
            return;
        }
        Log.d(TAG, this + " forceLoad." + dumpState());
        forceLoad();
    }

    @Override
    public void deliverResult(String data) {
        Log.d(TAG, this + " deliverResult :" + data + dumpState());
        //Loaderがお役御免になっている場合（resetされたLoaderは2度と使われない・・・のだと思う）
        //LoaderManagerがLoaderをresetした後にここに来る可能性はある（非同期処理があまりにも長い場合とか？）
        if(isReset()){
            destroy();
            return;
        }
        //処理結果をキャッシュに入れる
        Log.d(TAG, this + " saved results to cache." + dumpState());
        mCachedResult = data;

        //Loaderが開始状態なら処理結果を通知する
        if(isStarted()) {
            Log.d(TAG, this + " delivers results." + dumpState());
            super.deliverResult(data);
        }
    }

    @Override
    protected void onReset() {
        //LoaderManager.restartLoaderを呼んで、新しいアクティブなLoaderの処理が終わったら、古いLoaderの
        //これが呼ばれている。
        //再利用されるのかされないのかがいまいちはっきりしなくて気持ち悪い。
        Log.d(TAG, this + " onReset. This loader will never used." + dumpState());
        destroy();
    }

    @Override
    public void reset() {
        Log.d(TAG, this + " reset before to call super.reset()" + dumpState());
        super.reset();
        Log.d(TAG, this + " reset after super.reset()" + dumpState());
    }

    private void destroy(){
        mCachedResult = null;
    }

    @Override
    protected void onAbandon() {
        //LoaderManager.restartLoaderを呼んだら、古いLoaderのこれが呼ばれる
        Log.d(TAG, this + " onAbandon." + dumpState());
    }

    @Override
    protected void onStopLoading() {
        //メソッドをオーバーライドしただけでは呼ばれない
        //その場合はActivityがonStop()になったときには呼ばれている（画面回転は除く）
        Log.d(TAG, this + " onStopLoading()." + dumpState());
    }

    @Override
    protected boolean onCancelLoad() {
        //Loaderの非同期処理が実行されている時で、次のような場合に呼ばれる
        //+ Loaderが非同期処理実行中の間に、Activity等でinitLoader().forceLoad()をしたとき
        //+ Activity等でrestartLoaderを呼んだ時（非同期処理が実行中かは問わない）
        Log.d(TAG, this + " onCancelLoad()." + dumpState());
        //AsyncTaskLoader.onCancelLoad()でキャンセル関連の処理が行われているためsuper.onCancelLoad()を呼ぶ
        //ただしやってるのはLoaderの管理情報の更新するだけ
        //具体的には、、現在実行中の非同期処理があるか確認（mTask != null）
        //→ある場合：キャンセル処理中の非同期処理があるか確認(mCancellingTask != null)
        //　→ある場合：onCancelLoadが呼び出されたLoaderが次に実行する予定のタスクなら破棄する(mTask.waiting == true)
        //　　（実行待ちのLoaderをキャンセルするということで、実行待ちのタスクはまだ開始されてないから破棄するだけでいい）
        //　→ない場合：onCancelLoadが呼び出されたLoaderが次に実行予定のタスクか確認
        //　　→mTask.waiting == true：Loaderを破棄
        //　　→false：現在実行中の非同期処理をキャンセル処理中のタスクへ移動(mCancellingTask = mTask)
        //　　その上でcancelLoadInBackgroundを呼び出す
        //→ない場合：キャンセル対象がないので何もしない
        //実際のキャンセル処理はcancelLoadInBackgroundで実装する
        return super.onCancelLoad();
    }

    @Override
    public void cancelLoadInBackground() {
        //onCancelLoad()が呼ばれた後、Loaderが今実行中の非同期処理であればこれが呼ばれる
        //ここでloadInBackgroundで動いている処理を実際に止める処理を行えばよいはず
        Log.d(TAG, this + " cancelLoadInBackground()." + dumpState());
        //super.cancelLoadInBackground() は何も実装されていないので呼ぶ必要なし
    }

    @Override
    public void onCanceled(String data) {
        //onCancelLoad()が呼ばれたタスクが終了し、deliverResultが呼ばれた時にここにくる
        //引数は非同期処理が終了した結果（途中経過ではない）
        //Activityへはこの結果は通知されない
        //非同期処理の結果がcloseが必要だったりするのであれば、ここでリソースを解放する
        Log.d(TAG, this + " onCanceled with result:" + data + dumpState());
        //super.onCanceled(data) は何もしていないため呼ぶ必要なし
    }

    private String dumpState() {
        return " [state = isStarted:"
                + isStarted()
                + ", isReset:"
                + isReset()
                + ", isAbandoned:"
                + isAbandoned() + " ]";
    }
}