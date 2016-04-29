package com.tokool.earphone.activity;

import java.util.ArrayList;
import java.util.List;

import com.tokool.earphone.R;
import com.tokool.earphone.custominterface.OnValueChangeListener;
import com.tokool.earphone.customview.NoScrollViewPager;
import com.tokool.earphone.customview.Ruler;
import com.tokool.earphone.service.BleService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class GuideActivity extends Activity {

	private NoScrollViewPager pager;
	private MyPagerAdapter pagerAdapter;
	private List<View> views;
	private LinearLayout previousAndNextlayout;
	private Button btnNextSpecial, btnNext, btnPrevious;
	private int currentPage=0;
	private boolean isInitDialog=false;
	
	private RadioGroup radioGroup;
	private RadioButton radioBtnMale, radioBtnFemale;
	private TextView tvMale, tvFemale;

	private Ruler rulerHeight, rulerWeight, rulerStepLength, rulerTargetSteps;
	private TextView tvHeight, tvWeight, tvStepLength, tvTargetSteps;
	private TextView tvHeightUnit, tvWeightUnit, tvStepLengthUnit;
	
	private int sex, height, weight, stepLength, targetSteps, unit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		boolean isFirstUse=getSharedPreferences("isFirstUse", Context.MODE_PRIVATE).getBoolean("isFirstUse", true);
		if(!isFirstUse){			
			startActivity(new Intent(this, FlashActivity.class));
			startService(new Intent(this, BleService.class));
			finish();
		}
		
		setContentView(R.layout.activity_guide);
		
		//初始化默认数据
		height=170;
		weight=60;
		stepLength=60;
		targetSteps=10000;
		unit=0;
		
		LayoutInflater inflater=LayoutInflater.from(this);
		views=new ArrayList<View>();
		View view1=inflater.inflate(R.layout.pager_item_sex, null);
		final View view2=inflater.inflate(R.layout.pager_item_height_and_weight, null);
		final View view3=inflater.inflate(R.layout.pager_item_step_length, null);
		View view4=inflater.inflate(R.layout.pager_item_target_steps, null);
		views.add(view1);
		views.add(view2);
		views.add(view3);
		views.add(view4);
		
		initView1(view1);
		initView2(view2);
		initView3(view3);
		initView4(view4);
		
		pagerAdapter=new MyPagerAdapter();
		pager=(NoScrollViewPager)findViewById(R.id.pager);
		pager.setNoScroll(true);		
		pager.setAdapter(pagerAdapter);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				if(position==0){
					previousAndNextlayout.setVisibility(View.GONE);
					btnNextSpecial.setVisibility(View.VISIBLE);
				}else if(position==1){
					if(!isInitDialog){
						View view=getLayoutInflater().inflate(R.layout.dialog_unit, null);
						final RadioGroup radioGroup=(RadioGroup)view.findViewById(R.id.radiogroup_unit);
						AlertDialog.Builder builder=new AlertDialog.Builder(GuideActivity.this);
						builder.setView(view);
						builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								if(radioGroup.getCheckedRadioButtonId()==R.id.unit_metric){
									unit=0;
								}else{
									unit=1;
									initView2(view2);
									initView3(view3);
								}
							}
						});
						builder.setCancelable(false).create().show();
						isInitDialog=true;
					}					
					previousAndNextlayout.setVisibility(View.VISIBLE);
					btnNextSpecial.setVisibility(View.GONE);
					btnNext.setText(getString(R.string.next));
					
				}else if(position==3){
					btnNext.setText(getString(R.string.done));										
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		btnNextSpecial=(Button)findViewById(R.id.btn_next_special);
		btnNextSpecial.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				currentPage++;
				pager.setCurrentItem(currentPage);
			}
		});
		
		btnNext=(Button)findViewById(R.id.btn_next);
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub				
				if(currentPage==3){
					//跳转页面,并保存数据
					
					Editor editor=getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit();
					editor.putInt("sex", sex);
					editor.putInt("height", height);
					editor.putInt("weight", weight);
					editor.putInt("stepLength", stepLength);
					editor.putInt("targetSteps", targetSteps);
					editor.putInt("unit", unit);
					editor.commit();
					//引导页只用一次
					getSharedPreferences("isFirstUse", Context.MODE_PRIVATE).edit().putBoolean("isFirstUse", false).commit();
					startActivity(new Intent(GuideActivity.this, MainActivity.class));
					startService(new Intent(GuideActivity.this, BleService.class));					
					finish();
					return;
				}
				currentPage++;
				pager.setCurrentItem(currentPage);
			}
		});
		
		btnPrevious=(Button)findViewById(R.id.btn_previous);
		btnPrevious.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				currentPage--;
				pager.setCurrentItem(currentPage);
			}
		});
		
		previousAndNextlayout=(LinearLayout)findViewById(R.id.previous_next_layout);
	}
	
	private void initView1(View v){
		radioBtnMale=(RadioButton)v.findViewById(R.id.radio_btn_male);
		radioBtnFemale=(RadioButton)v.findViewById(R.id.radio_btn_female);
		
		tvMale=(TextView)v.findViewById(R.id.tv_male);
		tvFemale=(TextView)v.findViewById(R.id.tv_female);
		
		radioGroup=(RadioGroup)v.findViewById(R.id.radiogroup);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkId) {
				// TODO Auto-generated method stub
				if(checkId==R.id.radio_btn_male){
					sex=0;
					radioBtnMale.setAlpha(1.0f);
					radioBtnFemale.setAlpha(0.5f);
					tvMale.setTextColor(0xffffffff);
					tvFemale.setTextColor(0x88ffffff);
				}else{
					sex=1;
					radioBtnMale.setAlpha(0.5f);
					radioBtnFemale.setAlpha(1.0f);
					tvMale.setTextColor(0x88ffffff);
					tvFemale.setTextColor(0xffffffff);
				}
			}
		});
		radioBtnMale.setChecked(true);
		
	}
	
	private void initView2(View v){
		
		if(unit==0){
			tvHeight=(TextView)v.findViewById(R.id.tv_height);
			tvHeight.setText(height+"");
			
			tvHeightUnit=(TextView)v.findViewById(R.id.tv_height_unit);
			tvHeightUnit.setText("cm");
			
			rulerHeight=(Ruler)v.findViewById(R.id.ruler_height);
			rulerHeight.setValue(height);
			rulerHeight.setOnValueChangeListener(new OnValueChangeListener() {
				
				@Override
				public void onValueChange(int currentPosition) {
					// TODO Auto-generated method stub
					tvHeight.setText(currentPosition+"");
					height=currentPosition;
				}
			});
			
			tvWeight=(TextView)v.findViewById(R.id.tv_weight);
			tvWeight.setText(weight+"");
			
			tvWeightUnit=(TextView)v.findViewById(R.id.tv_weight_unit);
			tvWeightUnit.setText("kg");
			
			rulerWeight=(Ruler)v.findViewById(R.id.ruler_weight);
			rulerWeight.setOnValueChangeListener(new OnValueChangeListener() {
				
				@Override
				public void onValueChange(int currentPosition) {
					// TODO Auto-generated method stub
					tvWeight.setText(currentPosition+"");
				}
			});
			
		}else{
			tvHeight=(TextView)v.findViewById(R.id.tv_height);
			tvHeight.setText(Math.round(height/2.54f)+"");
			
			tvHeightUnit=(TextView)v.findViewById(R.id.tv_height_unit);
			tvHeightUnit.setText("in");
			
			rulerHeight=(Ruler)v.findViewById(R.id.ruler_height);
			rulerHeight.setFromValue(50);
			rulerHeight.setToValue(90);
			rulerHeight.setValue(Math.round(height/2.54f));
			rulerHeight.setOnValueChangeListener(new OnValueChangeListener() {
				
				@Override
				public void onValueChange(int currentPosition) {
					// TODO Auto-generated method stub
					tvHeight.setText(currentPosition+"");
					height=Math.round(currentPosition*2.54f);
				}
			});
			
			tvWeight=(TextView)v.findViewById(R.id.tv_weight);
			tvWeight.setText(Math.round(weight/0.45359f)+"");
			
			tvWeightUnit=(TextView)v.findViewById(R.id.tv_weight_unit);
			tvWeightUnit.setText("lb");
			
			rulerWeight=(Ruler)v.findViewById(R.id.ruler_weight);
			rulerWeight.setFromValue(60);
			rulerWeight.setToValue(330);
			rulerWeight.setValue(Math.round(weight/0.45359f));
			rulerWeight.setOnValueChangeListener(new OnValueChangeListener() {
				
				@Override
				public void onValueChange(int currentPosition) {
					// TODO Auto-generated method stub
					tvWeight.setText(currentPosition+"");
					weight=Math.round(currentPosition*0.45359f);
				}
			});
		}
		

	}
	
	private void initView3(View v){
		
		if(unit==0){
			tvStepLength=(TextView)v.findViewById(R.id.tv_step_length);	
			tvStepLength.setText(stepLength+"");
			
			tvStepLengthUnit=(TextView)v.findViewById(R.id.tv_step_length_unit);
			tvStepLengthUnit.setText("cm");
			
			rulerStepLength=(Ruler)v.findViewById(R.id.ruler_step_length);
			rulerStepLength.setValue(stepLength);
			rulerStepLength.setOnValueChangeListener(new OnValueChangeListener() {
				
				@Override
				public void onValueChange(int currentPosition) {
					// TODO Auto-generated method stub
					tvStepLength.setText(currentPosition+"");
					
				}
			});
			
		}else{
			tvStepLength=(TextView)v.findViewById(R.id.tv_step_length);	
			tvStepLength.setText(Math.round(stepLength/2.54f)+"");
			
			tvStepLengthUnit=(TextView)v.findViewById(R.id.tv_step_length_unit);
			tvStepLengthUnit.setText("in");
			
			rulerStepLength=(Ruler)v.findViewById(R.id.ruler_step_length);
			rulerStepLength.setFromValue(0);
			rulerStepLength.setToValue(50);
			rulerStepLength.setValue(Math.round(stepLength/2.54f));
			rulerStepLength.setOnValueChangeListener(new OnValueChangeListener() {
				
				@Override
				public void onValueChange(int currentPosition) {
					// TODO Auto-generated method stub
					tvStepLength.setText(currentPosition+"");
					stepLength=Math.round(currentPosition/2.54f);
				}
			});
		}
		
		

	}
	
	private void initView4(View v){
		
		tvTargetSteps=(TextView)v.findViewById(R.id.tv_target_steps);
		tvTargetSteps.setText(targetSteps+"");
		
		rulerTargetSteps=(Ruler)v.findViewById(R.id.ruler_target_step);
		rulerTargetSteps.setValue(targetSteps);
		rulerTargetSteps.setOnValueChangeListener(new OnValueChangeListener() {
			
			@Override
			public void onValueChange(int currentPosition) {
				// TODO Auto-generated method stub
				tvTargetSteps.setText(currentPosition+"");
			}
		});
		
	}
	
	private class MyPagerAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0==arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			((ViewPager)container).removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			((ViewPager)container).addView(views.get(position));
			return views.get(position);
		}
		
		
		
	}
}
