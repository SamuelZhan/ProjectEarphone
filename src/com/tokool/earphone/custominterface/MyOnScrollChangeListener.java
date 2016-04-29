package com.tokool.earphone.custominterface;

import com.tokool.earphone.customview.ObservableHorizontalScrollView;
import com.tokool.earphone.customview.ObservableScrollView;

public interface MyOnScrollChangeListener {
	void onScrollChange(ObservableScrollView observableScrollView, int x, int y, int oldx, int oldy);
	void onScrollChange(ObservableHorizontalScrollView observableHorizontalScrollView, int x, int y, int oldx, int oldy);
}
