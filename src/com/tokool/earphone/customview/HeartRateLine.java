package com.tokool.earphone.customview;

import com.tokool.earphone.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class HeartRateLine extends View {
	
	private int numOfPoints;
	private Paint paint;
	private int[] y;
	private int repeat;
	private boolean isInit;

	public HeartRateLine(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		
		TypedArray array=context.obtainStyledAttributes(attrs, R.styleable.HeartRateLine);
		
		numOfPoints=array.getInt(R.styleable.HeartRateLine_numOfPoints, 100);
		
		array.recycle();
		
		paint=new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(2f);
		paint.setStyle(Style.STROKE);
		
		y=new int[numOfPoints+10+1];
		isInit=false;
		
	}

	public HeartRateLine(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public HeartRateLine(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		int interval=getWidth()*(numOfPoints+10)/numOfPoints/numOfPoints;
		
		if(!isInit){
			for(int i=0; i<y.length; i++){
				y[i]=getHeight()/2;
			}
			isInit=true;
		}

		Path path=new Path();
		path.moveTo(0, getHeight()/2);
		for(int i=0; i<=numOfPoints; i++){
			path.lineTo(i*interval, y[i]);
		}
		canvas.drawPath(path, paint);
		
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for(int i=0; i<numOfPoints+5; i++){
					y[i]=y[i+1]; 
				}
				if(repeat==0){
					int height=(int) (Math.random()*getHeight()/2);
					y[numOfPoints]=getHeight()/2;
					y[numOfPoints+1]=getHeight()/2;
					y[numOfPoints+2]=getHeight()/2-height;
					y[numOfPoints+3]=getHeight()/2+height;
					y[numOfPoints+4]=getHeight()/2;
					y[numOfPoints+5]=getHeight()/2;
					y[numOfPoints+6]=getHeight()/2;
					y[numOfPoints+7]=getHeight()/2;
					y[numOfPoints+8]=getHeight()/2;
					y[numOfPoints+9]=getHeight()/2;
					repeat=10;
				}
				repeat--;
				
				invalidate();
			}
		}, 100);
	}
	
	
}
