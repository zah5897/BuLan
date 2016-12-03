package com.mingmay.bulan.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mingmay.bulan.R;

public class AboutServiceTipPage extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_about_service);
		TextView tx = (TextView) findViewById(R.id.info);
		String info = getInfo();
		tx.setText(info != null ? info : "");
		findViewById(R.id.agree).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	private String getInfo() {
		try {
			InputStream in = getAssets().open("service_info.txt");

			InputStreamReader ir = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(ir);
			StringBuffer sb = new StringBuffer();
			String line = null;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (i > 0) {
					sb.append("\n");
				}
				i = 1;
				sb.append(line);
			}
			br.close();
			ir.close();
			in.close();
			return sb.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void back(View v) {
		finish();
	}

}
