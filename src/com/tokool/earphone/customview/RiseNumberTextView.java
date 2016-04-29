package com.tokool.earphone.customview;

import java.text.DecimalFormat;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class RiseNumberTextView extends TextView {
	
	private float toNumberF;
	private float fromNumberF;
	private int toNumber;
	private int fromNumber;
	
	public interface EndListener{
		public void onEnd();
	}

	public RiseNumberTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public RiseNumberTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public RiseNumberTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	private void startRisingF(){
		
		final DecimalFormat df=new DecimalFormat("0.0");
		
		ValueAnimator valueAnimator=ValueAnimator.ofFloat(fromNumberF, toNumberF);		
		valueAnimator.setDuration(800);		
		valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				// TODO Auto-generated method stub
				
				setText(df.format(Float.parseFloat(valueAnimator.getAnimatedValue().toString())));
			}
		});
		
		valueAnimator.start();
	}
	
	private void startRising(){
		ValueAnimator valueAnimator=ValueAnimator.ofInt(fromNumber, toNumber);		
		valueAnimator.setDuration(800);		
		valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				// TODO Auto-generated method stub
				
				setText(valueAnimator.getAnimatedValue().toString());
			}
		});
		
		valueAnimator.start();
	}
	
	public void setNumber(float number){
		toNumberF=number;
		fromNumberF=number/2;
		startRisingF();
	}
	
	public void setNumber(int number){
		toNumber=number;
		fromNumber=number/2;
		startRising();
	}

}
