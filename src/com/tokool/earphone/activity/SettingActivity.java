package com.tokool.earphone.activity;

import com.tokool.earphone.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingActivity extends Activity {
	
	private ImageView btnBack;
	private LinearLayout btnUnit, btnVersion, btnReset;
	private TextView tvUnit;
	private int unit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);
		
		unit=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("unit", 0);
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		tvUnit=(TextView)findViewById(R.id.tv_unit);	
		if(unit==0){
			tvUnit.setText(getString(R.string.meter));
		}else{
			tvUnit.setText(getString(R.string.inch));
		}
		
		btnUnit=(LinearLayout)findViewById(R.id.btn_unit);
		btnUnit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_unit, null);
				final RadioGroup radioGroup=(RadioGroup)dialog.findViewById(R.id.radiogroup_unit);
				RadioButton btnMetric=(RadioButton)dialog.findViewById(R.id.unit_metric);
				RadioButton btnInch=(RadioButton)dialog.findViewById(R.id.unit_inch);
				if(unit==0){
					btnMetric.setChecked(true);
				}else{
					btnInch.setChecked(true);
				}
				new AlertDialog.Builder(SettingActivity.this)
					.setView(dialog)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub							
							if(radioGroup.getCheckedRadioButtonId()==R.id.unit_metric){
								tvUnit.setText(getString(R.string.meter));
								getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("unit", 0).commit();
							}else{
								tvUnit.setText(getString(R.string.inch));
								getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("unit", 1).commit();
							}
							sendBroadcast(new Intent("update_unit"));
						}
					})
					.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							
						}
					})
					.create()
					.show();
			}
		});
		
		btnVersion=(LinearLayout)findViewById(R.id.btn_version);
		btnVersion.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		 
		btnReset=(LinearLayout)findViewById(R.id.btn_reset);
		btnReset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_tip, null);
				((TextView)dialog.findViewById(R.id.tv_title)).setText(getString(R.string.restart_device));
				((TextView)dialog.findViewById(R.id.tv_tip)).setText(getString(R.string.sure_to_restart_device));
				new AlertDialog.Builder(SettingActivity.this)
					.setView(dialog)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							
						}
					})
					.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							
						}
					})
					.create()
					.show();
			}
		});
	}
	
}
