package com.easemob.livedemo.ui.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by wei on 2016/5/30.
 */
public class BaseActivity extends Activity {


    protected void showToast(final String toastContent){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(BaseActivity.this, toastContent, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void showLongToast(final String toastContent){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(BaseActivity.this, toastContent, Toast.LENGTH_LONG).show();
            }
        });
    }
}
