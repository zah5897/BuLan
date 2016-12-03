package com.mingmay.bulan.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PointView extends View {
	public static final int SIZE=3;
    private float mWidth ,mHeight;
    private float centerPoint;
    private int currentSelectedIndex=0;
    private Paint paint;
    private int centerY;
    private int R=6;
	public PointView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint=new Paint();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);
		centerPoint=mWidth/2;
		centerY=(int) (mHeight/2);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if(currentSelectedIndex==0){
			paint.setColor(Color.WHITE);
			canvas.drawCircle(centerPoint-20, centerY, R, paint);
		}else{
			paint.setColor(Color.GRAY);
			canvas.drawCircle(centerPoint-20, centerY, R, paint);
		}
		if(currentSelectedIndex==1){
			paint.setColor(Color.WHITE);
			canvas.drawCircle(centerPoint+20, centerY, R, paint);
		}else{
			paint.setColor(Color.GRAY);
			canvas.drawCircle(centerPoint+20, centerY, R, paint);
		}
		 
	}
	public void setCurrentPosition(int position){
		if(position>=2){
			position=0;
		}
		currentSelectedIndex=position;
		this.invalidate();
	}
}
