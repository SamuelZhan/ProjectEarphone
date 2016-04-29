package com.tokool.earphone.customview; 

import com.tokool.earphone.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class RoundProgressBar extends View {
	
	private int progressColor;
	private int secondProgressColor;
	private int progressWidth;
	private int count;
	private int currentCount;
	private int splitAngle;
	private int startAngle;
	private int spaceAngle;
	private int outsideCircleWidth;
	private Paint paint;
	
	public RoundProgressBar(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar, defStyleAttr, 0);
		int n=a.getIndexCount();
		for(int i=0; i<n; i++){
			int attr=a.getIndex(i);
			switch (attr) {
			case R.styleable.RoundProgressBar_count:
				count=a.getInt(attr, 10);
				break;

			case R.styleable.RoundProgressBar_currentCount:
				currentCount=a.getInt(attr, 4);
				break;
				
			case R.styleable.RoundProgressBar_progressColor:
				progressColor=a.getColor(attr, Color.GRAY);
				break;
				
			case R.styleable.RoundProgressBar_progressWidth:
				progressWidth=a.getDimensionPixelSize(attr, 
						(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
								getResources().getDisplayMetrics()));
				break;
				
			case R.styleable.RoundProgressBar_secondProgressColor:
				secondProgressColor=a.getColor(attr, Color.RED);
				break;
				
			case R.styleable.RoundProgressBar_splitAngle:
				splitAngle=a.getInt(attr, 20);
				break;
				
			case R.styleable.RoundProgressBar_startAngle:
				startAngle=a.getInt(attr, 0);
				break;
				
			case R.styleable.RoundProgressBar_spaceAngle:
				spaceAngle=a.getInt(attr, 0);
				break;
				
			case R.styleable.RoundProgressBar_outsideCircleWidth:
				outsideCircleWidth=a.getDimensionPixelSize(attr, 
						(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
								getResources().getDisplayMetrics()));
				break;

			}
		}
		
		a.recycle();
		
		paint=new Paint();
	}

	public RoundProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public RoundProgressBar(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas){
		paint.setAntiAlias(true);
		paint.setStrokeWidth(progressWidth);
		paint.setColor(progressColor);
		paint.setStyle(Style.STROKE);
		
		int centre=getWidth()/2;
		int radius=centre-progressWidth/2-(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
				getResources().getDisplayMetrics());
		
		float itemAngle=(360*1.0f-splitAngle*count)/count;
		RectF oval=new RectF(centre-radius, centre-radius, centre+radius, centre+radius);
		for(int i=0; i<count-count*spaceAngle/360; i++){
			canvas.drawArc(oval, i*(itemAngle+splitAngle)+startAngle, itemAngle, false, paint);
		}
		
		paint.setColor(secondProgressColor);
		for(int i=0; i<currentCount-currentCount*spaceAngle/360; i++){			
			canvas.drawArc(oval, i*(itemAngle+splitAngle)+startAngle, itemAngle, false, paint);
		}	
		
		paint.setColor(0xffffffff);
		paint.setStrokeWidth(progressWidth+(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
				getResources().getDisplayMetrics()));
		canvas.drawArc(oval, startAngle, itemAngle, false, paint);
		canvas.drawArc(oval, (currentCount-currentCount*spaceAngle/360)*(itemAngle+splitAngle)+startAngle, itemAngle, false, paint);
				
		radius=centre-progressWidth/2-outsideCircleWidth/2;
		paint.setColor(0x55ffffff);
		paint.setStrokeWidth(outsideCircleWidth);
		canvas.drawCircle(centre, centre, radius, paint);
		
	}
	
	//进度条从哪个角度开始
	public void setProgressStartAngle(int angle){
		startAngle=angle;
		invalidate();
	}
	
	//设置count个数,相当于max
	public void setCount(int count){
		this.count=count;
		invalidate();
	}
	
	//设置currentCount,相当progress
	public void setCurrentCount(int currentCount){
		this.currentCount=currentCount;
		invalidate();
	}
	
	//设置第一条进度条的颜色
	public void setProgressColor(int color){
		progressColor=color;
		invalidate();
	}
	
	//设置第二条进度条的颜色
	public void setSecondProgressColor(int color){
		secondProgressColor=color;
		invalidate();
	}
	
	//设置分隔距离
	public void setSplitAngle(int splitAngle) {
		this.splitAngle = splitAngle;
		invalidate();
	}
	
	//设置进度条宽度
	public void setProgressWidth(int progressWidth) {
		this.progressWidth = progressWidth;
		invalidate();
	}
	
	//设置挖空进度条的角度
	public void setSpaceAngle(int spaceAngle) {
		this.spaceAngle = spaceAngle;
		invalidate();
	}
	
	//设置进度开始位置
	public void setStartAngle(int startAngle) {
		this.startAngle = startAngle;
		invalidate();
	}
	
	public int getSpaceAngle() {
		return spaceAngle;
	}
	
	public int getStartAngle() {
		return startAngle;
	}

	public int getProgressWidth() {
		return progressWidth;
	}	

	public int getSplitAngle() {
		return splitAngle;
	}	

	public int getProgressColor() {
		return progressColor;
	}

	public int getSecondProgressColor() {
		return secondProgressColor;
	}

	public int getCount() {
		return count;
	}

	public int getCurrentCount() {
		return currentCount;
	}
	
	

}
