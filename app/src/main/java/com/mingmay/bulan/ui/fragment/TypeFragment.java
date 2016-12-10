package com.mingmay.bulan.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.model.Tag;
import com.mingmay.bulan.ui.SearchPage;
import com.mingmay.bulan.ui.SelectedTagPage;
import com.mingmay.bulan.ui.fragment.type.HotZhanLanPage;
import com.mingmay.bulan.ui.fragment.type.LanMuFragment;
import com.mingmay.bulan.ui.fragment.type.ZhuanLanFragment;

public class TypeFragment extends Fragment implements OnClickListener {
	private TextView lanmu, zhuanlan;

	FragmentManager childFragmengManager;
	private int tab = 0;
	private ImageView toTopView;

	
	private int  current_tag_id=-1;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater
				.inflate(R.layout.layout_type_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getView().findViewById(R.id.add_tag).setOnClickListener(this);
		getView().findViewById(R.id.search).setOnClickListener(this);
		lanmu = (TextView) getView().findViewById(R.id.lanmu);
		zhuanlan = (TextView) getView().findViewById(R.id.zhuanlan);
		lanmu.setOnClickListener(this);
		zhuanlan.setOnClickListener(this);
		childFragmengManager = getChildFragmentManager();
		Fragment f = childFragmengManager.findFragmentByTag("lanmu");
		FragmentTransaction transaction = childFragmengManager
				.beginTransaction();
		if (f != null) {
			transaction.show(f);
		} else {
			transaction.add(R.id.container, new LanMuFragment().setHandler(handler), "lanmu");
		}
		transaction.commit();
		lanmu.setText(Html.fromHtml("<u>" + "栏目" + "</u>"));
		toTopView = (ImageView) getView().findViewById(R.id.to_top);
		toTopView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toTopView.setVisibility(View.GONE);
				
				if(tab==0){
					Fragment f = childFragmengManager.findFragmentByTag("lanmu");
					if(f!=null){
						LanMuFragment lm=(LanMuFragment) f;
						lm.scrollToTop();
					}
				}else{
					Fragment f = childFragmengManager.findFragmentByTag("zhuanlan");
					if(f!=null){
						ZhuanLanFragment zl=(ZhuanLanFragment) f;
						zl.scrollToTop();
					}
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (tab == 0) {
			if (data != null) {
				Tag t = (Tag) data.getSerializableExtra("tag");
				if (t != null) {
					Fragment f = childFragmengManager
							.findFragmentByTag("lanmu");
					if (f != null) {
						((LanMuFragment) f).addTag(t);
					}
				}
			}
		} else {
			if (resultCode > 0) {
				Fragment f = childFragmengManager.findFragmentByTag("zhuanlan");
				if (f != null) {
					((ZhuanLanFragment) f).refresh();
				}
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_tag:
			if (tab == 1) {
				startActivity(new Intent(getActivity(), HotZhanLanPage.class));
			} else {
				Intent i = new Intent(getActivity(), SelectedTagPage.class);
				getActivity().startActivityForResult(i, 0);
				getActivity().overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
			}
			break;
		case R.id.search:
			Intent toSearchPage = new Intent(getActivity(), SearchPage.class);
			toSearchPage.putExtra("tag_id", current_tag_id);
			startActivity(toSearchPage);
			break;
		case R.id.lanmu:

			if (tab == 0) {
				return;
			}
			FragmentTransaction transaction = childFragmengManager
					.beginTransaction();
			Fragment f = childFragmengManager.findFragmentByTag("lanmu");
			if (f != null) {
				transaction.show(f);
			} else {
				transaction.add(R.id.container, new LanMuFragment().setHandler(handler),
						"lanmu");
			}

			// hide
			f = childFragmengManager.findFragmentByTag("zhuanlan");
			if (f != null) {
				transaction.hide(f);
			}

			transaction.commit();
			zhuanlan.setTextColor(getResources().getColor(R.color.black));
			zhuanlan.setText("专栏");

			lanmu.setTextColor(getResources().getColor(R.color.orange));
			lanmu.setText(Html.fromHtml("<u>" + "栏目" + "</u>"));
			tab = 0;
			break;
		case R.id.zhuanlan:
			if (tab == 1) {
				return;
			}
			tab = 1;
			transaction = childFragmengManager.beginTransaction();
			f = childFragmengManager.findFragmentByTag("zhuanlan");
			if (f != null) {
				transaction.show(f);
			} else {
				transaction.add(R.id.container, new ZhuanLanFragment().setHandler(handler),
						"zhuanlan");
			}

			f = childFragmengManager.findFragmentByTag("lanmu");
			if (f != null) {
				transaction.hide(f);
			}

			transaction.commit();

			lanmu.setTextColor(getResources().getColor(R.color.black));
			lanmu.setText("栏目");

			zhuanlan.setTextColor(getResources().getColor(R.color.orange));
			zhuanlan.setText(Html.fromHtml("<u>" + "专栏" + "</u>"));
			break;
		default:
			break;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				animateHide();
				break;
			case 1:
				animateBack();
				break;

			case 2: //选择tag
				Bundle data=msg.getData();
				current_tag_id=data.getInt("tag_id");
				break;
			case 3: //专栏id
				current_tag_id=-1;
				break;
			default:
				break;
			}
		}
	};
	private Runnable dismiss = new Runnable() {
		@Override
		public void run() {
			toTopView.setVisibility(View.GONE);
		}
	};

	private void animateBack() {
		toTopView.setVisibility(View.VISIBLE);
		toTopView.removeCallbacks(dismiss);
		toTopView.postDelayed(dismiss, 2000);
	}

	private void animateHide() {
		toTopView.setVisibility(View.GONE);
	}
}
