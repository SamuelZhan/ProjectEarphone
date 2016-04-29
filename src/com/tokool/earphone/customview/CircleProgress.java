package com.tokool.earphone.customview;

import com.tokool.earphone.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class CircleProgress extends View{
	
	//小圆的个数
	private int numOfCircles;
	//最大圆半径
	private int maxRadius;
	//最小圆半径
	private int minRadius;
	//旋转速度
	private int rotateSpeedInMillis;
	//顺时针旋转
	private boolean isClockwise;
	//小圆的颜色
	private int circleColor;
	
	private Paint paint;
	
	private float rotateDegrees;
	private int numOfRotate;

	public CircleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		TypedArray array=context.obtainStyledAttributes(attrs, R.styleable.CircleProgress);
		
		numOfCircles=array.getInt(R.styleable.CircleProgress_numOfCircles, 10);
		maxRadius=array.getDimensionPixelSize(R.styleable.CircleProgress_maxRadius, dp2px(8));
		minRadius=array.getDimensionPixelSize(R.styleable.CircleProgress_minRadius, dp2px(2));
		rotateSpeedInMillis=array.getInt(R.styleable.CircleProgress_rotateSpeedInMillis, 200);
		isClockwise=array.getBoolean(R.styleable.CircleProgress_isClockwise, true);
		circleColor=array.getColor(R.styleable.CircleProgress_circleColor, Color.BLACK);
		
		array.recycle();
	
		paint=new Paint();
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		paint.setColor(circleColor);
		
		rotateDegrees=360/numOfCircles;
		numOfRotate=0;

	}

	public CircleProgress(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public CircleProgress(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	
	private int dp2px(int dp){
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		//按一定角度旋转画布canvas
		if(numOfRotate==numOfCircles){
			numOfRotate=0;
		}
		if(isClockwise){
			canvas.rotate(rotateDegrees*numOfRotate, getWidth()/2, getHeight()/2);
		}else{
			canvas.rotate(-rotateDegrees*numOfRotate, getWidth()/2, getHeight()/2);
		}
		
		numOfRotate++;
		
		//取View最短边，并减去最大圆的半径，得到所有圆所在的圆路径的半径
		int radius=(getWidth()>getHeight()?getHeight():getWidth())/2-maxRadius;
		//每个小圆的半径增量
		float radiusIncrement=(float)(maxRadius-minRadius)/numOfCircles;
		//每隔多少度绘制一个小圆,弧度制		
		double angle=2*Math.PI/numOfCircles;
		
		if(isClockwise){
			for(int i=0; i<numOfCircles; i++){
				float x=(float) (getWidth()/2+Math.cos(i*angle)*radius);
				float y=(float) (getHeight()/2-Math.sin(i*angle)*radius);
				canvas.drawCircle(x, y, maxRadius-radiusIncrement*i, paint);				
			}
		}else{
			for(int i=0; i<numOfCircles; i++){
				float x=(float) (getWidth()/2+Math.cos(i*angle)*radius);
				float y=(float) (getHeight()/2-Math.sin(i*angle)*radius);
				canvas.drawCircle(x, y, minRadius+radiusIncrement*i, paint);				
			}
		}
		
		
		//旋转间隔
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				invalidate(); 
			}
		}, rotateSpeedInMillis);
	}


}
