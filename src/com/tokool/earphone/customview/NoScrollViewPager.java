package com.tokool.earphone.customview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoScrollViewPager extends ViewPager {
	
	private boolean noScroll=false;

	public NoScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public NoScrollViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void setNoScroll(boolean noScroll){
		this.noScroll=noScroll;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(noScroll){
			return false;
		}else{
			return super.onTouchEvent(event);
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event){
		if(noScroll){
			return false;
		}else{
			return super.onInterceptTouchEvent(event);
		}
	}

}
