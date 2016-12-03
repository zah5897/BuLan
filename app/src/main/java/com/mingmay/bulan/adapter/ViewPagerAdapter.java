package com.mingmay.bulan.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.util.GuideImageCache;

/**
 *	功能描述：ViewPager适配器，用来绑定数据和view
 */
public class ViewPagerAdapter extends PagerAdapter {
	private static final int[] pics = { R.drawable.frist, R.drawable.second,
		R.drawable.thrid, R.drawable.four, R.drawable.five,R.drawable.guide_transparent };
	//界面列表
    private ArrayList<ImageView> views;
    Context ctx;
    public ViewPagerAdapter (Context ctx,ArrayList<ImageView> views){
        this.views = views;
        this.ctx=ctx;
    }
       
	/**
	 * 获得当前界面数
	 */
	@Override
	public int getCount() {
		 if (views != null) {
             return views.size();
         }      
         return 0;
	}

	/**
	 * 初始化position位置的界面
	 */
    @Override
    public Object instantiateItem(View view, int position) {
       
        ((ViewPager) view).addView(views.get(position), 0);
        
        ImageView imgView=views.get(position);
        Bitmap bmp= GuideImageCache.getInstance().getMemoryCache(""+position);
        if(bmp!=null){
        	imgView.setImageBitmap(bmp);
        }else{
        	if(position==5){
        		imgView.setImageResource(pics[position]);
        	}else{
        	bmp= BitmapFactory.decodeResource(ctx.getResources(), pics[position]);
        	imgView.setImageBitmap(bmp);
        	GuideImageCache.getInstance().addBitmapToMemoryCache(""+position, bmp);
        	}
        }
        return imgView;
    }
    
    /**
	 * 判断是否由对象生成界面
	 */
	@Override
	public boolean isViewFromObject(View view, Object arg1) {
		return (view == arg1);
	}

	/**
	 * 销毁position位置的界面
	 */
    @Override
    public void destroyItem(View view, int position, Object arg2) {
        ((ViewPager) view).removeView(views.get(position));       
    }

	@Override
	public void finishUpdate(View arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
