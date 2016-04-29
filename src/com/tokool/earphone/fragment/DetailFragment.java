package com.tokool.earphone.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.tokool.earphone.R;
import com.tokool.earphone.custominterface.ChartViewOnTouchListener;
import com.tokool.earphone.customview.ChartView;
import com.tokool.earphone.customview.ChartView.ChartViewSettings;
import com.tokool.earphone.customview.RiseNumberTextView;
import com.tokool.earphone.database.DatabaseHelper;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DetailFragment extends Fragment {
	
	private TextView tvPeriod, tvTotalStepsTitle, tvTotalDistanceTitle, tvTotalCalorieTitle, tvNoData;
	private TextView tvTotalUnitFt, tvAverageUnitFt;
	private RiseNumberTextView tvTotalSteps, tvTotalDistance, tvTotalCalorie, tvAverageSteps, tvAverageDistance, tvAverageCalorie;
	private RadioGroup rgPeriod, rgType;
	private RadioButton btnStep, btnHeartRate;
	private ListView lvHeartRates;
	private MyBaseAdapter adapter;
	private ArrayList<HashMap<String, Object>> heartRates;
	private RelativeLayout chartLayout;
	private LinearLayout stepLayout, heartRateLayout;
	
	private MyBroadcastReceiver receiver;
	private SQLiteDatabase db;
	private long time;	
	
	private int period=0;
	private int type=0;
	private int unit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		unit=getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("unit", 0);
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter("update_unit");
		getActivity().registerReceiver(receiver, filter);
		
		View rootView=inflater.inflate(R.layout.fragment_detail, container, false);
		
		time=System.currentTimeMillis();
		
		DatabaseHelper dbHelper=new DatabaseHelper(getActivity(), "sportData", null, 1);		
		db=dbHelper.getWritableDatabase();
		
		tvPeriod=(TextView)rootView.findViewById(R.id.tv_period);
		tvTotalStepsTitle=(TextView)rootView.findViewById(R.id.tv_total_steps_title);
		tvTotalDistanceTitle=(TextView)rootView.findViewById(R.id.tv_total_distance_title);
		tvTotalCalorieTitle=(TextView)rootView.findViewById(R.id.tv_total_calorie_title);
		
		tvTotalSteps=(RiseNumberTextView)rootView.findViewById(R.id.tv_total_steps);
		tvTotalDistance=(RiseNumberTextView)rootView.findViewById(R.id.tv_total_distance);
		tvTotalCalorie=(RiseNumberTextView)rootView.findViewById(R.id.tv_total_calorie);
		tvAverageSteps=(RiseNumberTextView)rootView.findViewById(R.id.tv_average_steps);
		tvAverageDistance=(RiseNumberTextView)rootView.findViewById(R.id.tv_average_distance);
		tvAverageCalorie=(RiseNumberTextView)rootView.findViewById(R.id.tv_average_calorie);
		
		tvAverageUnitFt=(TextView)rootView.findViewById(R.id.tv_average_distance_unit);
		tvTotalUnitFt=(TextView)rootView.findViewById(R.id.tv_total_distance_unit);
		
		chartLayout=(RelativeLayout)rootView.findViewById(R.id.chart_layout);
		chartLayout.addView(getGraphicalView(null, 0), 0);
		
		stepLayout=(LinearLayout)rootView.findViewById(R.id.layout_step);
		
		heartRateLayout=(LinearLayout)rootView.findViewById(R.id.layout_heartRate);
		
		btnStep=(RadioButton)rootView.findViewById(R.id.btn_step);
		btnHeartRate=(RadioButton)rootView.findViewById(R.id.btn_heartRate);
		
		rgPeriod=(RadioGroup)rootView.findViewById(R.id.radiogroup_period);
		rgPeriod.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup arg0, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId==R.id.btn_week){
					period=0;
					changeShowDetail(period, type);
				}else if(checkedId==R.id.btn_month){
					period=1;
					changeShowDetail(period, type);
				}else if(checkedId==R.id.btn_year){
					period=2;
					changeShowDetail(period, type);					
				}
			}
		});
		((RadioButton)rootView.findViewById(R.id.btn_week)).setChecked(true);
		
		rgType=(RadioGroup)rootView.findViewById(R.id.radiogroup_type);
		rgType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup arg0, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId==R.id.btn_step){
					type=0;
					changeShowDetail(period, type);
				}else if(checkedId==R.id.btn_heartRate){
					type=1;
					changeShowDetail(period, type);
				}
			}
		});
		
		heartRates=new ArrayList<HashMap<String, Object>>();
		lvHeartRates=(ListView)rootView.findViewById(R.id.lv_heartRate);
		adapter=new MyBaseAdapter();
		lvHeartRates.setAdapter(adapter);
		
		tvNoData=(TextView)rootView.findViewById(R.id.tv_no_data);
	
		return rootView;
	}
	
	private void changeShowDetail(int period, int type){
		if(type==0){
			btnStep.setAlpha(0.3f);
			btnHeartRate.setAlpha(1f);
			stepLayout.setVisibility(View.VISIBLE);
			heartRateLayout.setVisibility(View.GONE);
			if(period==0){
				int[] stepsOfWeek=new int[7];
				int totalSteps=0;
				int totalDistance=0;
				float totalCalorie=0f;
				SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
				Calendar calendar=Calendar.getInstance();
				int dayOfWeek=calendar.get(Calendar.DAY_OF_WEEK);
				for(int i=0; i<dayOfWeek; i++){
					long timeTemp=time-(dayOfWeek-1-i)*24*60*60*1000L;
					Date date=new Date(timeTemp);
					df=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());		
					String dateString=df.format(date);
					Cursor c=db.rawQuery("SELECT * FROM sportData WHERE date=?", new String[]{dateString});
					if(c.moveToNext()){
						int steps=c.getInt(c.getColumnIndex("steps"));
						float calorie=c.getFloat(c.getColumnIndex("calorie"));
						int distance=c.getInt(c.getColumnIndex("distance"));
						stepsOfWeek[i]=steps;
						totalSteps+=steps;
						totalDistance+=distance;
						totalCalorie+=calorie;
					}
				}
				
				tvTotalStepsTitle.setText(getString(R.string.total_steps)); 
				tvTotalDistanceTitle.setText(getString(R.string.total_distance));
				tvTotalCalorieTitle.setText(getString(R.string.total_calorie));
				
				tvTotalSteps.setNumber(totalSteps);		
				tvTotalCalorie.setNumber(totalCalorie);
				if(unit==0){
					tvTotalDistance.setNumber(totalDistance);
					tvTotalUnitFt.setText("m");
				}else{
					tvTotalDistance.setNumber(Math.round(totalDistance/0.3048f));
					tvTotalUnitFt.setText("ft");
				}
				
				tvAverageSteps.setNumber(totalSteps/7);								
				tvAverageCalorie.setNumber(totalCalorie/7);
				if(unit==0){
					tvAverageDistance.setNumber(totalDistance/7);
					tvAverageUnitFt.setText("m");
				}else{
					tvAverageDistance.setNumber(Math.round(totalDistance/7/0.3048f));
					tvAverageUnitFt.setText("ft");
				}
						
				df=new SimpleDateFormat("yyyy/MM/dd");
				calendar.add(Calendar.DAY_OF_WEEK, 7-dayOfWeek);
				String endDateString=df.format(calendar.getTime());
				calendar.add(Calendar.DAY_OF_WEEK, -6);
				String startDateString=df.format(calendar.getTime());
				tvPeriod.setText(startDateString+"-"+endDateString);
				
				chartLayout.removeViewAt(0);
				chartLayout.addView(getGraphicalView(stepsOfWeek, 0), 0);
			}else if(period==1){
				int[] stepsOfMonth=new int[Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)];
				int totalSteps=0;
				int totalDistance=0;
				float totalCalorie=0f;
				Calendar calendar=Calendar.getInstance();
				int dayOfMonth=calendar.get(Calendar.DATE);
				for(int i=0; i<dayOfMonth; i++){
					long timeTemp=time-(dayOfMonth-1-i)*24*60*60*1000L;
					Date date=new Date(timeTemp);
					SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());		
					String dateString=df.format(date);
					Cursor c=db.rawQuery("SELECT * FROM sportData WHERE date=?", new String[]{dateString});
					if(c.moveToNext()){
						int steps=c.getInt(c.getColumnIndex("steps"));
						float calorie=c.getFloat(c.getColumnIndex("calorie"));
						int distance=c.getInt(c.getColumnIndex("distance"));
						stepsOfMonth[i]=steps;
						totalSteps+=steps;
						totalDistance+=distance;
						totalCalorie+=calorie;
					}
				}
				
				tvTotalStepsTitle.setText(getString(R.string.total_steps));
				tvTotalDistanceTitle.setText(getString(R.string.total_distance));
				tvTotalCalorieTitle.setText(getString(R.string.total_calorie));
				
				tvTotalSteps.setNumber(totalSteps);
				tvTotalCalorie.setNumber(totalCalorie);
				if(unit==0){
					tvTotalDistance.setNumber(totalDistance);
					tvTotalUnitFt.setText("m");
				}else{
					tvTotalDistance.setNumber(Math.round(totalDistance/0.3048f));
					tvTotalUnitFt.setText("ft");
				}
				
				tvAverageSteps.setNumber(totalSteps/calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				tvAverageCalorie.setNumber(totalCalorie/calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				if(unit==0){
					tvAverageDistance.setNumber(totalDistance/calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
					tvAverageUnitFt.setText("m");
				}else{
					tvAverageDistance.setNumber(Math.round(totalDistance/calendar.getActualMaximum(Calendar.DAY_OF_MONTH)/0.3048f));
					tvAverageUnitFt.setText("ft");
				}
				
				SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
				calendar.add(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)-dayOfMonth);
				String endDateString=df.format(calendar.getTime());
				calendar.add(Calendar.DAY_OF_MONTH, -calendar.getActualMaximum(Calendar.DAY_OF_MONTH)+1);
				String startDateString=df.format(calendar.getTime());
				tvPeriod.setText(startDateString+"-"+endDateString);
				
				chartLayout.removeViewAt(0);
				chartLayout.addView(getGraphicalView(stepsOfMonth, 1), 0);
			}else if(period==2){
				int[] stepsOfYear=new int[12];
				int totalSteps=0;
				int totalDistance=0;
				float totalCalorie=0f;
				Calendar calendar=Calendar.getInstance();
				int monthOfYear=calendar.get(Calendar.MONTH);
				for(int i=monthOfYear; i>0; i--){
					SimpleDateFormat df=new SimpleDateFormat("yyyy-MM", Locale.getDefault());
					String monthString=df.format(calendar.getTime());
					Cursor c=db.rawQuery("SELECT * FROM sportDataOfYear WHERE month=?", new String[]{monthString});
					if(c.moveToNext()){
						int steps=c.getInt(c.getColumnIndex("totalSteps"));
						float calorie=c.getFloat(c.getColumnIndex("totalCalorie"));
						int distance=c.getInt(c.getColumnIndex("totalDistance"));
						stepsOfYear[i]=steps;
						totalSteps+=steps;
						totalDistance+=distance;
						totalCalorie+=calorie;
					}
					calendar.add(Calendar.MONTH, -1);
				}
				
				tvTotalStepsTitle.setText(getString(R.string.total_steps));
				tvTotalDistanceTitle.setText(getString(R.string.total_distance));
				tvTotalCalorieTitle.setText(getString(R.string.total_calorie));
				
				tvTotalSteps.setNumber(totalSteps);
				tvTotalCalorie.setNumber(totalCalorie);
				if(unit==0){
					tvTotalDistance.setNumber(totalDistance);
					tvTotalUnitFt.setText("m");
				}else{
					tvTotalDistance.setNumber(Math.round(totalDistance/0.3048f));
					tvTotalUnitFt.setText("ft");
				}
				
				tvAverageSteps.setNumber(totalSteps/calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
				tvAverageCalorie.setNumber(totalCalorie/calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
				if(unit==0){
					tvAverageDistance.setNumber(totalDistance/calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
					tvAverageUnitFt.setText("m");
				}else{
					tvAverageDistance.setNumber(Math.round(totalDistance/calendar.getActualMaximum(Calendar.DAY_OF_YEAR)/0.3048f));
					tvAverageUnitFt.setText("ft");
				}
				
				SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
				calendar.add(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR)-calendar.get(Calendar.DAY_OF_YEAR));
				String endDateString=df.format(calendar.getTime());
				calendar.add(Calendar.DAY_OF_YEAR, -calendar.getActualMaximum(Calendar.DAY_OF_YEAR)+1);
				String startDateString=df.format(calendar.getTime());
				tvPeriod.setText(startDateString+"-"+endDateString);
				
				chartLayout.removeViewAt(0);
				chartLayout.addView(getGraphicalView(stepsOfYear, 2), 0);
			}
		}else{
			btnStep.setAlpha(1f);
			btnHeartRate.setAlpha(0.3f);
			stepLayout.setVisibility(View.GONE);
			heartRateLayout.setVisibility(View.VISIBLE);
			if(period==0){
				int[] heartRateOfWeek=new int[7];
				SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
				Calendar calendar=Calendar.getInstance();
				int dayOfWeek=calendar.get(Calendar.DAY_OF_WEEK);
				loadDataOnListViewWeek(dayOfWeek-1);
				for(int i=0; i<dayOfWeek; i++){
					long timeTemp=time-(dayOfWeek-1-i)*24*60*60*1000L;
					Date date=new Date(timeTemp);	
					String dateString=df.format(date);
					Cursor c=db.rawQuery("SELECT AVG(heartRate) AS averageHeartRate FROM heartRate WHERE date=?", new String[]{dateString});
					if(c.moveToNext()){
						heartRateOfWeek[i]=c.getInt(c.getColumnIndex("averageHeartRate"));
					}
				}
				
				df=new SimpleDateFormat("yyyy/MM/dd");
				calendar.add(Calendar.DAY_OF_WEEK, 7-dayOfWeek);
				String endDateString=df.format(calendar.getTime());
				calendar.add(Calendar.DAY_OF_WEEK, -6);
				String startDateString=df.format(calendar.getTime());
				tvPeriod.setText(startDateString+"-"+endDateString);
				
				chartLayout.removeViewAt(0);
				ChartView.ChartViewSettings settings=new ChartViewSettings();
				settings.setData(heartRateOfWeek);
				settings.setLabels(new String[]{"Sun", "Mon", "Tues", "Wed", "Thur", "Fri", "Sat"});
				settings.setSelectedData(dayOfWeek-1);
				ChartView chart=new ChartView(getActivity(), settings);
				chart.setOnItemTouchListener(new ChartViewOnTouchListener() {
					
					@Override
					public void onItemTouch(int position) {
						// TODO Auto-generated method stub
						loadDataOnListViewWeek(position);
						
					}
				});
				chartLayout.addView(chart, 0);
			}else if(period==1){
				int[] heartRateOfMonth=new int[Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)];
				Calendar calendar=Calendar.getInstance();
				SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());	
				int dayOfMonth=calendar.get(Calendar.DATE);
				loadDataOnListViewMonth(dayOfMonth-1);
				for(int i=0; i<dayOfMonth; i++){
					long timeTemp=time-(dayOfMonth-1-i)*24*60*60*1000L;
					Date date=new Date(timeTemp);						
					String dateString=df.format(date);
					Cursor c=db.rawQuery("SELECT AVG(heartRate) AS averageHeartRate FROM heartRate WHERE date=?", new String[]{dateString});
					if(c.moveToNext()){
						heartRateOfMonth[i]=c.getInt(c.getColumnIndex("averageHeartRate"));						
					}
				}
				
				df=new SimpleDateFormat("yyyy/MM/dd");
				calendar.add(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)-dayOfMonth);
				String endDateString=df.format(calendar.getTime());
				calendar.add(Calendar.DAY_OF_MONTH, -calendar.getActualMaximum(Calendar.DAY_OF_MONTH)+1);
				String startDateString=df.format(calendar.getTime());
				tvPeriod.setText(startDateString+"-"+endDateString);
				
				chartLayout.removeViewAt(0);
				ChartView.ChartViewSettings settings=new ChartViewSettings();
				settings.setData(heartRateOfMonth);
				settings.setSelectedData(dayOfMonth-1);				
				String[] labels=new String[heartRateOfMonth.length];
				for(int i=0; i<labels.length; i++){
					if(i%5==0){
						labels[i]=Integer.toString(i+1);
					}else{
						labels[i]="";
					}
				}
				settings.setLabels(labels);
				ChartView chart=new ChartView(getActivity(), settings);
				chart.setOnItemTouchListener(new ChartViewOnTouchListener() {
					
					@Override
					public void onItemTouch(int position) {
						// TODO Auto-generated method stub
						loadDataOnListViewMonth(position);
					}
				});
				chartLayout.addView(chart, 0);
			}else if(period==2){
				int[] heartRateOfYear=new int[12];
				Calendar calendar=Calendar.getInstance();
				int monthOfYear=calendar.get(Calendar.MONTH);
				loadDataOnListViewYear(monthOfYear);
				for(int i=monthOfYear; i>0; i--){
					SimpleDateFormat df=new SimpleDateFormat("yyyy-MM", Locale.getDefault());
					String monthString=df.format(calendar.getTime());
					Cursor c=db.rawQuery("SELECT AVG(heartRate) AS averageHeartRate FROM heartRate WHERE month=?", new String[]{monthString});
					if(c.moveToNext()){
						heartRateOfYear[i]=c.getInt(c.getColumnIndex("averageHeartRate"));
					}
					calendar.add(Calendar.MONTH, -1);
				}
				
				SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
				calendar.add(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR)-calendar.get(Calendar.DAY_OF_YEAR));
				String endDateString=df.format(calendar.getTime());
				calendar.add(Calendar.DAY_OF_YEAR, -calendar.getActualMaximum(Calendar.DAY_OF_YEAR)+1);
				String startDateString=df.format(calendar.getTime());
				tvPeriod.setText(startDateString+"-"+endDateString);

				chartLayout.removeViewAt(0);
				ChartView.ChartViewSettings settings=new ChartViewSettings();
				settings.setData(heartRateOfYear);
				settings.setLabels(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"});
				settings.setSelectedData(monthOfYear);
				ChartView chart=new ChartView(getActivity(), settings);
				chart.setOnItemTouchListener(new ChartViewOnTouchListener() {
					
					@Override
					public void onItemTouch(int position) {
						// TODO Auto-generated method stub
						loadDataOnListViewYear(position);
					}
				});
				chartLayout.addView(chart, 0);
			}
		}
	}
	
	//加载数据到周表
	private void loadDataOnListViewWeek(int position){
		Calendar calendar=Calendar.getInstance();
		int dayOfWeek=calendar.get(Calendar.DAY_OF_WEEK);
		calendar.add(Calendar.DATE, position-dayOfWeek+1);
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		String dateString=df.format(calendar.getTime());
		heartRates.clear();
		Cursor c=db.rawQuery("SELECT heartRate, time FROM heartRate WHERE date=?", new String[]{dateString});
		while(c.moveToNext()){
			HashMap<String, Object> heartRate=new HashMap<String, Object>();
			heartRate.put("heartRate", c.getInt(c.getColumnIndex("heartRate")));
			heartRate.put("time", c.getString(c.getColumnIndex("time")));
			heartRates.add(heartRate);
		}
		if(heartRates.size()==0){
			tvNoData.setVisibility(View.VISIBLE);
			lvHeartRates.setVisibility(View.GONE);
		}else{
			tvNoData.setVisibility(View.GONE);
			lvHeartRates.setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		}
	}
	
	//加载数据到月表
	private void loadDataOnListViewMonth(int position){
		Calendar calendar=Calendar.getInstance();
		int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
		calendar.add(Calendar.DATE, position-dayOfMonth+1);
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		String dateString=df.format(calendar.getTime());
		heartRates.clear();
		Cursor c=db.rawQuery("SELECT heartRate, time FROM heartRate WHERE date=?", new String[]{dateString});
		while(c.moveToNext()){
			HashMap<String, Object> heartRate=new HashMap<String, Object>();
			heartRate.put("heartRate", c.getInt(c.getColumnIndex("heartRate")));
			heartRate.put("time", c.getString(c.getColumnIndex("time")));
			heartRates.add(heartRate);
		}
		if(heartRates.size()==0){
			tvNoData.setVisibility(View.VISIBLE);
			lvHeartRates.setVisibility(View.GONE);
		}else{
			tvNoData.setVisibility(View.GONE);
			lvHeartRates.setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		}
	}
	
	//加载数据到年表
	private void loadDataOnListViewYear(int position){
		Calendar calendar=Calendar.getInstance();
		int monthOfYear=calendar.get(Calendar.MONTH);
		calendar.add(Calendar.DATE, position-monthOfYear+1);
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM", Locale.getDefault());
		String monthString=df.format(calendar.getTime());
		heartRates.clear();
		Cursor c=db.rawQuery("SELECT heartRate, time FROM heartRate WHERE month=?", new String[]{monthString});
		while(c.moveToNext()){
			HashMap<String, Object> heartRate=new HashMap<String, Object>();
			heartRate.put("heartRate", c.getInt(c.getColumnIndex("heartRate")));
			heartRate.put("time", c.getString(c.getColumnIndex("time")));
			heartRates.add(heartRate);
		}
		if(heartRates.size()==0){
			tvNoData.setVisibility(View.VISIBLE);
			lvHeartRates.setVisibility(View.GONE);
		}else{
			tvNoData.setVisibility(View.GONE);
			lvHeartRates.setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		}
	}
		
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(db!=null){
			db.close();
		}
		getActivity().unregisterReceiver(receiver);
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("update_unit")){
				unit=getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("unit", 0);
				changeShowDetail(period, type);
			}
		}
		
	}

	//获取折线图
	private GraphicalView getGraphicalView(int[] data, int period){
		//X轴标签
		String[] labels=null;
		XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset();
		XYSeries series=new XYSeries("步数");
		int maxHeight=0;
		if(period==0){
			labels=new String[]{"Sun", "Mon", "Tues", "Wed", "Thur", "Fri", "Sat"};
			if(data==null){
				for(int i=0; i<labels.length; i++){
					series.add(i, 0);
				}	
			}else{
				for(int i=0; i<data.length; i++){	
					
					series.add(i, data[i]);
					if(maxHeight<data[i]){
						maxHeight=data[i];
					}
				}
			}					
		}else if(period==1){
			labels=new String[data.length];
			for(int i=0; i<labels.length; i++){
				if(i%5==0){
					labels[i]=Integer.toString(i+1);
				}else{
					labels[i]="";
				}
				
			}
			for(int i=0; i<data.length; i++){				
				series.add(i, data[i]);
				if(maxHeight<data[i]){
					maxHeight=data[i];
				}
			}
		}else if(period==2){
			labels=new String[data.length];
			for(int i=0; i<labels.length; i++){
				labels[i]=Integer.toString(i+1);
			}
			for(int i=0; i<data.length; i++){				
				series.add(i, data[i]);	
				if(maxHeight<data[i]){
					maxHeight=data[i];
				}
			}
		}
		dataset.addSeries(series);
		
		//轴渲染器
		XYMultipleSeriesRenderer renderer=new XYMultipleSeriesRenderer();
		//X、Y的最大最小值
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(maxHeight+0.25f*maxHeight);//加上0.25倍的空间以显示数字，避免出界
		renderer.setXAxisMin(0);
		renderer.setXAxisMax(labels.length-1);
		//不显示标示
		renderer.setShowLegend(false);
		//设置边距，参数上，左，下，右
		renderer.setMargins(new int[]{dp2px(35), dp2px(20), dp2px(3), dp2px(20)});
		//设置边距颜色为透明
		renderer.setMarginsColor(0x00ffffff);
		//设置XY轴是否可以延伸
		renderer.setPanEnabled(false, false);
		//设置点的大小
		renderer.setPointSize(dp2px(3));
		//设置XY轴显示
		renderer.setShowAxes(false);		
		//设置折线图是否可以伸缩
		renderer.setZoomEnabled(false, false);
		//设置自定义X轴标签
		for(int i=0; i<labels.length; i++){
			renderer.addXTextLabel(i, labels[i]);
		}
		//标签文字大小颜色
		renderer.setXLabelsColor(0xffffffff);
		renderer.setLabelsTextSize(sp2px(12));
		renderer.setXLabels(0);
		renderer.setYLabels(0);
		renderer.setXLabelsAlign(Align.CENTER);
		
		//折线渲染器
		XYSeriesRenderer r=new XYSeriesRenderer();
		//折线颜色
		r.setColor(0xffffffff);
		//折线宽度
		r.setLineWidth(dp2px(1));	
		//实心点
		r.setFillPoints(true);
		//点的风格
		r.setPointStyle(PointStyle.CIRCLE);
		//显示值
		r.setDisplayChartValues(true);
		//设置这个是为了避免数字缺失
		r.setDisplayChartValuesDistance(dp2px(10));
		//值的文本大小
		r.setChartValuesTextSize(sp2px(12));
		//值与点的距离
		r.setChartValuesSpacing(dp2px(8));
		
		renderer.addSeriesRenderer(r);
		
		return ChartFactory.getLineChartView(getActivity(), dataset, renderer);
	}
		
	private int dp2px(int dp){

		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
		
	}
	
	private int sp2px(int sp){
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
	}
	
	private class MyBaseAdapter extends BaseAdapter{

		private class ViewHolder{
			private TextView tvHeartRate;
			private TextView tvTime;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return heartRates.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return heartRates.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView!=null){
				holder=(ViewHolder)convertView.getTag();
			}else{
				holder=new ViewHolder();
				convertView=getActivity().getLayoutInflater().inflate(R.layout.list_item_heartrate, null);
				holder.tvTime=(TextView)convertView.findViewById(R.id.tv_time);
				holder.tvHeartRate=(TextView)convertView.findViewById(R.id.tv_heartRate);
				convertView.setTag(holder);
			}
			holder.tvTime.setText((String)heartRates.get(position).get("time"));
			holder.tvHeartRate.setText(""+heartRates.get(position).get("heartRate"));
			
			return convertView;
		}
		
	}
}
