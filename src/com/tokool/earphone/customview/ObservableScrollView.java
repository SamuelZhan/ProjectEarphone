package com.tokool.earphone.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;
import com.tokool.earphone.custominterface.*;

public class ObservableScrollView extends ScrollView {
	
	private MyOnScrollChangeListener listener;
	
	public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ObservableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ObservableScrollView(Context context) {
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
