package jp.gcreate.sample.asynctaskloadersample;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private static final String TAG = "MainActivity";
    private static final String KEY_BUNDLE = "randomInt";
    private static final String KEY_COUNT = "count";

    private int mCount;
    private TextView mCountTextView;
    private TextView mTextView;
    private Button mButton;
    private Button mForceLoadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCount = 10;
        mCountTextView = (TextView) findViewById(R.id.count);
        mTextView = (TextView) findViewById(R.id.text);
        mButton = (Button) findViewById(R.id.button);
        mForceLoadButton = (Button) findViewById(R.id.forceButton);

        setTextToCount();

        LoaderManager.enableDebugLogging(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                ++mCount;
                setTextToCount();
                setTextToloadingNow();
                Bundle args = new Bundle();
                args.putInt(KEY_BUNDLE, mCount);
                Log.d(TAG, getClass().getSimpleName() + " LoaderManager.restartLoader().");
                getLoaderManager().restartLoader(0, args, MainActivity.this);
            }
        });
        mForceLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                getLoaderManager().initLoader(0, null, MainActivity.this).forceLoad();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        mButton.setOnClickListener(null);
        mForceLoadButton.setOnClickListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    //EventをMainThreadで受け取り処理するため、onEventMainThreadを実装する
    public void onEventMainThread(String event){
        mTextView.setText(event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_COUNT, mCount);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCount = savedInstanceState.getInt(KEY_COUNT);
        setTextToCount();
    }

    private void setTextToloadingNow(){
        mTextView.setText("now loading.");
    }

    private void setTextToCount(){
        mCountTextView.setText(getString(R.string.text_count,mCount));
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, getClass().getSimpleName() + " onCreateLoader,id=" + id + ", args=" + args);
        if(args != null){
            return new MyAsyncTaskLoader(this, args.getInt(KEY_BUNDLE));
        }else {
            return new MyAsyncTaskLoader(this);
        }
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        Log.d(TAG, getClass().getSimpleName() + " onLoadFinished, loader=" + loader + ", data=" + data);
        mTextView.setText(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(TAG, getClass().getSimpleName() + " onLoaderReset, loader=" + loader);
    }
}