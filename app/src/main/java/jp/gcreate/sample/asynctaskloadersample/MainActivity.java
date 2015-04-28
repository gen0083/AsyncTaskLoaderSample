package jp.gcreate.sample.asynctaskloadersample;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private static final String TAG = "MainActivity";
    private static final String KEY_BUNDLE = "randomInt";
    private TextView mTextView;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text);
        mButton = (Button) findViewById(R.id.button);

        LoaderManager.enableDebugLogging(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextToloadingNow();
                //10-30の数字をランダムで設定
                int random = (int) (Math.random() * 10) + 1;
                Bundle args = new Bundle();
                args.putInt(KEY_BUNDLE, random);
                getLoaderManager().restartLoader(0, args, MainActivity.this);
                Log.d(TAG, getClass().getSimpleName() + " loader restart.");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mButton.setOnClickListener(null);
    }

    private void setTextToloadingNow(){
        mTextView.setText("now loading.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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