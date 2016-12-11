package com.easemob.livedemo.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.easemob.livedemo.R;

/**
 * Created by wei on 2016/7/27.
 */
public class ScreenshotDialog extends Dialog {

    ImageView imageView;

    private Bitmap bitmap;

    public ScreenshotDialog(Context context, Bitmap bitmap) {
        super(context);
        this.bitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_screenshot);
        imageView = (ImageView) findViewById(R.id.imageview);
        setCanceledOnTouchOutside(false);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
       findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               onCancel();
           }
       });

    }



    void onCancel() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        dismiss();
    }
}
