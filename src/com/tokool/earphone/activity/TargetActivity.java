package com.tokool.earphone.activity;

import com.tokool.earphone.R;
import com.tokool.earphone.custominterface.MyOnScrollChangeListener;
import com.tokool.earphone.custominterface.OnValueChangeListener;
import com.tokool.earphone.customview.ObservableHorizontalScrollView;
import com.tokool.earphone.customview.ObservableScrollView;
import com.tokool.earphone.customview.Ruler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TargetActivity extends Activity {
	
	private Ruler rulerTargetSteps;
	private TextView tvTargetSteps;
	private ImageView btnBack;
	private Button btnEnsure;
	private int targetSteps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_target);
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnEnsure=(Button)findViewById(R.id.btn_ensure);
		btnEnsure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("targetSteps", targetSteps).commit();
				sendBroadcast(new Intent("update_target_steps"));
				finish();
			}
		});
		
		targetSteps=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("targetSteps", 10000);
		
		tvTargetSteps=(TextView)findViewById(R.id.tv_target_steps);
		tvTargetSteps.setText(""+targetSteps);
		
		rulerTargetSteps=(Ruler)findViewById(R.id.ruler_target_step);
		rulerTargetSteps.setValue(targetSteps);
		rulerTargetSteps.setOnValueChangeListener(new OnValueChangeListener() {
			
			@Override
			public void onValueChange(int value) {
				// TODO Auto-generated method stub
				targetSteps=value;
				tvTargetSteps.setText(targetSteps+"");
			}
		});
	}

}
