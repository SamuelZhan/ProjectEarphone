package com.tokool.earphone.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import com.tokool.earphone.custominterface.*;

public class ObservableHorizontalScrollView extends HorizontalScrollView {
	
	private MyOnScrollChangeListener listener;

	public ObservableHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public ObservableHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ObservableHorizontalScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void setOnScrollChangeListener(MyOnScrollChangeListener onScrollChangeListener){
		this.listener=onScrollChangeListener;
	}
	
	@Override  
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {  
		super.onScrollChanged(x, y, oldx, oldy);  
		if (listener!= null) {  
			listener.onScrollChange(this, x, y, oldx, oldy);  
		}  
	} 

}