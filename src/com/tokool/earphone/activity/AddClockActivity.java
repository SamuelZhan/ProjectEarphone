package com.tokool.earphone.activity;

import java.util.ArrayList;
import java.util.Date;

import com.tokool.earphone.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;

public class AddClockActivity extends Activity {
	
	private ImageView btnBack;
	private Button btnEnsure;
	private RelativeLayout btnCycle;
	private TextView tvCycle;
	private NumberPicker npHour, npMinute;
	private int hour, minute;
	private boolean[] day;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_clock);
		
		Date date=new Date();		
		hour=date.getHours();
		minute=date.getMinutes();		
		day=new boolean[7];
		for(int i=0; i<day.length; i++){
			if((date.getDay()-1)==i){
				day[i]=true;
			}else{
				day[i]=false;
			}
		}
		
		Intent intent=getIntent();
		if(intent.getBooleanExtra("isSet", false)){
			hour=intent.getIntExtra("hour", new Date().getHours());
			minute=intent.getIntExtra("minute", new Date().getMinutes());
			day=intent.getBooleanArrayExtra("day");
		}
		
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
				Intent intent=new Intent();
				intent.putExtra("day", day);
				intent.putExtra("hour", hour);
				intent.putExtra("minute", minute);
				setResult(1, intent);
				finish();
			}
		});
		
		tvCycle=(TextView)findViewById(R.id.tv_cycle);
		tvCycle.setText((day[0]?getString(R.string.monday)+"  ":"")+(day[1]?getString(R.string.tuesday)+"  ":"")
				+(day[2]?getString(R.string.wednesday)+"  ":"")+(day[3]?getString(R.string.thursday)+"  ":"")
				+(day[4]?getString(R.string.friday)+"  ":"")+(day[5]?getString(R.string.saturday)+"  ":"")
				+(day[6]?getString(R.string.sunday)+"  ":""));
		
		btnCycle=(RelativeLayout)findViewById(R.id.btn_cycle);
		btnCycle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_clock_cycle, null);
				final ArrayList<CheckBox> cbs=new ArrayList<CheckBox>();
				CheckBox cb1=(CheckBox)dialog.findViewById(R.id.cb1);
				cbs.add(cb1);
				CheckBox cb2=(CheckBox)dialog.findViewById(R.id.cb2);
				cbs.add(cb2);
				CheckBox cb3=(CheckBox)dialog.findViewById(R.id.cb3);
				cbs.add(cb3);
				CheckBox cb4=(CheckBox)dialog.findViewById(R.id.cb4);
				cbs.add(cb4);
				CheckBox cb5=(CheckBox)dialog.findViewById(R.id.cb5);
				cbs.add(cb5);
				CheckBox cb6=(CheckBox)dialog.findViewById(R.id.cb6);
				cbs.add(cb6);
				CheckBox cb7=(CheckBox)dialog.findViewById(R.id.cb7);
				cbs.add(cb7);
				
				for(int i=0; i<day.length; i++){
					cbs.get(i).setChecked(day[i]);
				}
				
				AlertDialog alertDialog=new AlertDialog.Builder(AddClockActivity.this, R.style.MyDialogTheme)
				.setView(dialog)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						int numOfSelected=0;
						for(int i=0; i<cbs.size(); i++){
							if(cbs.get(i).isChecked()){
								numOfSelected++;
							}
						}
						if(numOfSelected==0) return;
						
						for(int i=0; i<day.length; i++){
							day[i]=cbs.get(i).isChecked();
						}
						tvCycle.setText((day[0]?getString(R.string.monday)+"  ":"")+(day[1]?getString(R.string.tuesday)+"  ":"")
								+(day[2]?getString(R.string.wednesday)+"  ":"")+(day[3]?getString(R.string.thursday)+"  ":"")
								+(day[4]?getString(R.string.friday)+"  ":"")+(day[5]?getString(R.string.saturday)+"  ":"")
								+(day[6]?getString(R.string.sunday)+"  ":""));
					}
				})
				.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create();
				alertDialog.show();
			}
		});
		
		npHour=(NumberPicker)findViewById(R.id.number_picker_hour);
		npHour.setMinValue(0);
		npHour.setMaxValue(23);
		npHour.setValue(hour);
		npHour.setLable(getString(R.string.hour));
		npHour.setMiddleTextColor(0xff00aced);
		npHour.setTopAndBottomTextColor(0xffffffff);
		npHour.setOnValueChangedListener(new OnValueChangeListener() {
			
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				// TODO Auto-generated method stub
				hour=newVal;
			}
		});
		
		npMinute=(NumberPicker)findViewById(R.id.number_picker_minute);
		npMinute.setMinValue(0);
		npMinute.setMaxValue(59);
		npMinute.setValue(minute);
		npMinute.setLable(getString(R.string.minute));
		npMinute.setMiddleTextColor(0xff00aced);
		npMinute.setTopAndBottomTextColor(0xffffffff);
		npMinute.setOnValueChangedListener(new OnValueChangeListener() {
			
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				// TODO Auto-generated method stub
				minute=newVal;
			}
		});
		
	}

}
