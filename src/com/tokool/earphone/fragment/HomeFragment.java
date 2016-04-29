package com.tokool.earphone.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.tokool.earphone.R;
import com.tokool.earphone.customview.RiseNumberTextView;
import com.tokool.earphone.customview.RoundProgressBar;
import com.tokool.earphone.database.DatabaseHelper;
import com.tokool.earphone.service.BleService;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment {
	
	private ViewPager pager;
	private ArrayList<View> views;
	private View viewStep, viewHeartRate;
	private MyPagerAdapter pagerAdapter;
	private RelativeLayout chartLayout;
	private LinearLayout switchLayout;
	private RelativeLayout heartRateLayout;
	private RiseNumberTextView tvSteps;
	private TextView tvTitle, tvDate, tvTargetSteps, tvPercent, tvCalorie, tvTime, tvDistance, tvAverageHeartRate, tvNoData;
	private ImageView btnPrevious1, btnNext1, btnPrevious2, btnNext2, ivConnection, btnShare;
	private RoundProgressBar progressBarSteps, progressBarHeartRate;
	private ListView lvHeartRates;
	private MyBaseAdapter lvAdapter;
	private MyBroadcastReceiver receiver;		
	private SQLiteDatabase db;
	private MyOnClickListener listener;
	private LocationClient locationClient;
	private BDLocationListener locationListener;
	private Handler handler;
	
	private int unit;
	private int targetSteps;
	private long showTime;
	private long currentTime;
	private String todayDateString;
	private String showDateString;
	private String yearString;
	
	private ArrayList<HashMap<String, Object>> heartRates;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		targetSteps=getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("targetSteps", 10000);
		unit=getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("unit", 0);
		
		DatabaseHelper dbHelper=new DatabaseHelper(getActivity(), "sportData", null, 1);		
		db=dbHelper.getWritableDatabase();
		
		currentTime=System.currentTimeMillis();
		showTime=currentTime;
		Date date=new Date(currentTime);
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());		
		todayDateString=df.format(date);
		showDateString=todayDateString;
		Cursor c=db.rawQuery("SELECT * FROM sportData WHERE date=?", new String[]{todayDateString});
		if(!c.moveToNext()){
			db.execSQL("INSERT INTO sportData VALUES(null, ?, ?, ?, ?, ?, ?, ?)", new Object[]{BleService.steps, targetSteps,
					BleService.calorie, BleService.distance, BleService.sportTime, null, todayDateString});
		}
		df=new SimpleDateFormat("yyyy-MM", Locale.getDefault());		
		yearString=df.format(date);	
		c=db.rawQuery("SELECT * FROM sportDataOfYear WHERE month=? ", new String[]{yearString});
		if(!c.moveToNext()){
			db.execSQL("INSERT INTO sportDataOfYear VALUES(null, ?, ?, ?, ?)", new Object[]{0, 0, 0, yearString});
		}
		
		handler=new Handler();
		initLocationSDK();
				
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("com.tokool.earphone.action.connected");
		filter.addAction("com.tokool.earphone.action.disconnected");
		filter.addAction("update_target_steps");
		filter.addAction("update_unit");
		filter.addAction("update_steps");
		filter.addAction("update_sportTime_calorie_distance");
		filter.addAction("history_steps_data");
		filter.addAction("time_to_24_hour");
		filter.addAction("update_heart_rate");
		filter.addAction("turn_back_to_today");
		getActivity().registerReceiver(receiver, filter);
		
		listener=new MyOnClickListener();
		
		View rootView=inflater.inflate(R.layout.fragment_home, container, false);
		
		tvTitle=(TextView)rootView.findViewById(R.id.tv_title);
		
		btnShare=(ImageView)rootView.findViewById(R.id.btn_share);
		btnShare.setOnClickListener(listener);
		
		tvDate=(TextView)rootView.findViewById(R.id.tv_date);
		tvDate.setText(todayDateString);
		
		ivConnection=(ImageView)rootView.findViewById(R.id.iv_connection);
		if(BleService.isConnected){
			ivConnection.setImageResource(R.drawable.bluetooth_connected);
		}else{
			ivConnection.setImageResource(R.drawable.bluetooth_disconnected);
		}
		
		views=new ArrayList<View>();
		viewStep=getActivity().getLayoutInflater().inflate(R.layout.pager_item_step, null);
		viewHeartRate=getActivity().getLayoutInflater().inflate(R.layout.pager_item_heart_rate, null);
		viewHeartRate.setScaleX(0.8f);
		viewHeartRate.setScaleY(0.8f);
		views.add(viewStep);
		views.add(viewHeartRate);
		
		initViewStep(viewStep);
		initViewHeartRate(viewHeartRate);
		
		pager=(ViewPager)rootView.findViewById(R.id.pager);
		pagerAdapter=new MyPagerAdapter();
		pager.setAdapter(pagerAdapter);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				if(position==0){
					tvTitle.setText(getString(R.string.passometer));
				}else{
					tvTitle.setText(getString(R.string.heart_rate));
				}
			}
			
			@Override
			public void onPageScrolled(int position, float offset, int arg2) {
				// TODO Auto-generated method stub
				if(position==1 && offset==0)return;
				chartLayout.setAlpha(1-offset);
				if((1-offset)>0.8f){
					viewStep.setScaleX(1-offset);
					viewStep.setScaleY(1-offset);
					
				}
				if(offset>0.8f){
					viewHeartRate.setScaleX(offset);
					viewHeartRate.setScaleY(offset);
				}
				heartRateLayout.setAlpha(offset);
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		switchLayout=(LinearLayout)rootView.findViewById(R.id.layout);
		
		chartLayout=(RelativeLayout)rootView.findViewById(R.id.chart_layout);
		c=db.rawQuery("SELECT stepString FROM sportData WHERE date=?", new String[]{todayDateString});
		if(c.moveToNext()){			
			chartLayout.addView(getGraphicalView(c.getString(c.getColumnIndex("stepString"))), 0);
		}else{
			chartLayout.addView(getGraphicalView(null), 0);
		}		

		heartRates=new ArrayList<HashMap<String, Object>>();
		c=db.rawQuery("SELECT heartRate, time FROM heartRate WHERE date=?", new String[]{todayDateString});
		while(c.moveToNext()){
			HashMap<String, Object> heartRate=new HashMap<String, Object>();
			heartRate.put("heartRate", c.getInt(c.getColumnIndex("heartRate")));
			heartRate.put("time", c.getString(c.getColumnIndex("time")));
			heartRates.add(heartRate);
		}
		heartRateLayout=(RelativeLayout)rootView.findViewById(R.id.heart_rate_layout);
		
		lvHeartRates=(ListView)rootView.findViewById(R.id.lv_heartRate);
		lvAdapter=new MyBaseAdapter();
		lvHeartRates.setAdapter(lvAdapter);
		
		tvNoData=(TextView)rootView.findViewById(R.id.tv_no_data);
		if(heartRates.size()>0){
			tvNoData.setVisibility(View.GONE);
		}else{
			tvNoData.setVisibility(View.VISIBLE);
		}
		
		return rootView;
	}
	
	private void initLocationSDK(){
		
		//若是夜晚则直接退出，不用再定位，获取天气
		Calendar calendar=Calendar.getInstance();
		if(calendar.get(Calendar.HOUR_OF_DAY)>18 || calendar.get(Calendar.HOUR_OF_DAY)<6){
			switchLayout.setBackgroundResource(R.drawable.night);
			return;
		}
				
		locationClient=new LocationClient(getActivity().getApplicationContext());
		locationListener=new LocationListener();
		locationClient.registerLocationListener(locationListener);
		
		LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死  
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        locationClient.setLocOption(option);
		
		locationClient.start();
	}
	
	private void initViewStep(View v){
	
		tvTargetSteps=(TextView)v.findViewById(R.id.tv_target_steps);		
		tvTargetSteps.setText(getString(R.string.target)+targetSteps+getString(R.string.step));
	
		tvSteps=(RiseNumberTextView)v.findViewById(R.id.tv_steps);
		tvSteps.setNumber(BleService.steps);
		
		progressBarSteps=(RoundProgressBar)v.findViewById(R.id.round_progress1);
		progressBarSteps.setCurrentCount((int)(100*(float)BleService.steps/targetSteps));
		progressBarSteps.setOnClickListener(listener);
		
		tvPercent=(TextView)v.findViewById(R.id.tv_percent);
		DecimalFormat df=new DecimalFormat("0.0");
		tvPercent.setText(getString(R.string.complete)+df.format((float)BleService.steps/targetSteps*100)+"%");
		
		tvCalorie=(TextView)v.findViewById(R.id.tv_calorie);
		tvCalorie.setText(df.format(BleService.calorie)+"k cal");
		
		tvDistance=(TextView)v.findViewById(R.id.tv_distance);
		if(unit==0){
			tvDistance.setText(BleService.distance+"m");
		}else{
			tvDistance.setText(Math.round(BleService.distance/0.3048f)+"ft");
		}
		
		
		tvTime=(TextView)v.findViewById(R.id.tv_time);
		int hour=BleService.sportTime/3600;
		int minute=(BleService.sportTime-hour*3600)/60;
		int second=BleService.sportTime%60;
		df=new DecimalFormat("00");
		tvTime.setText(df.format(hour)+":"+df.format(minute)+":"+df.format(second));
		
		btnPrevious1=(ImageView)v.findViewById(R.id.btn_previous);
		btnPrevious1.setOnClickListener(listener);
		
		btnNext1=(ImageView)v.findViewById(R.id.btn_next);
		btnNext1.setOnClickListener(listener);
	}
	
	private void initViewHeartRate(View v){
		
		progressBarHeartRate=(RoundProgressBar)v.findViewById(R.id.round_progress2);
		progressBarHeartRate.setOnClickListener(listener);
	
		tvAverageHeartRate=(TextView)v.findViewById(R.id.tv_average);
		
		Cursor c=db.rawQuery("SELECT heartRate FROM heartRate WHERE date=?", new String[]{todayDateString});
		if(!c.moveToNext()){			
			tvAverageHeartRate.setText("0");
		}else{	
			int averageTemp = 0;
			c=db.rawQuery("SELECT AVG(heartRate) AS averageOfHeartRate FROM heartRate WHERE date=?", new String[]{todayDateString});					
			if(c.moveToNext()){
				averageTemp=c.getInt(c.getColumnIndex("averageOfHeartRate"));
			}						
			tvAverageHeartRate.setText(""+averageTemp);
		}
		
		btnPrevious2=(ImageView)v.findViewById(R.id.btn_previous);
		btnPrevious2.setOnClickListener(listener);
		
		btnNext2=(ImageView)v.findViewById(R.id.btn_next);
		btnNext2.setOnClickListener(listener);
		
	}
	
	//获取折线图
	private GraphicalView getGraphicalView(String data){
		
		//X轴标签
		String[] labels=new String[]{"0","2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24"};
		//数据		

		XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset();
		XYSeries series=new XYSeries("步数");
				
		//数据, 只能递加添加数据，递减添加数据会报错，原因不知，应该是源码bug
		int maxHeight=0;
		if(data==null){
			for(int i=0; i<labels.length; i++){
				series.add(i, 0);
			}	
		}else{
			series.add(0, 0);
			for(int i=0; i<data.length()/4; i++){
				int x=Integer.valueOf(data.substring(i*4, (i+1)*4), 16);
				if(x>maxHeight){
					maxHeight=x;
				}
				series.add(i+1, x);
			}
		}
		
		dataset.addSeries(series);
		
		//轴渲染器
		XYMultipleSeriesRenderer renderer=new XYMultipleSeriesRenderer();
		//X、Y的最大最小值
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(maxHeight+0.25f*maxHeight);//加上0.25倍的空间以显示数字，避免出界
		renderer.setXAxisMin(0);
		renderer.setXAxisMax(12);
		//不显示标示
		renderer.setShowLegend(false);
		//设置边距，参数上，左，下，右
		renderer.setMargins(new int[]{dp2px(35), dp2px(20), dp2px(3), dp2px(20)});
		//设置边距颜色为透明
		renderer.setMarginsColor(0x00ffffff);
		//设置XY轴是否可以延伸
		renderer.setPanEnabled(false, false);
		//设置点的大小
		renderer.setPointSize(dp2px(4));
		//设置XY轴显示
		renderer.setShowAxes(false);		
		//设置折线图是否可以伸缩
		renderer.setZoomEnabled(false, false);
		//设置自定义X轴标签
		for(int i=0; i<labels.length; i++){
			renderer.addXTextLabel(i, labels[i]);
		}
		//标签文字大小颜色
		renderer.setXLabelsColor(0xff000000);
		renderer.setLabelsTextSize(sp2px(12));
		renderer.setXLabels(0);
		renderer.setYLabels(0);
		renderer.setXLabelsAlign(Align.CENTER);
		
		//折线渲染器
		XYSeriesRenderer r=new XYSeriesRenderer();
		//折线颜色
		r.setColor(0xff00aced);
		//折线宽度
		r.setLineWidth(dp2px(1));	
		//点的边宽
		r.setPointStrokeWidth(dp2px(2));
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
	
	private class LocationListener implements BDLocationListener{

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub

			final String requestWeather="http://api.map.baidu.com/telematics/v3/weather?location="
			+location.getCity().substring(0, 2)
			+"&output=json&ak=Gn9fRxfNZ5ilTXOgrFbqAq0b";
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						HttpPost request=new HttpPost(requestWeather);
						request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
						request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
						HttpResponse response=new DefaultHttpClient().execute(request);
						JSONObject jsonObject=new JSONObject(EntityUtils.toString(response.getEntity()));
						JSONArray jsonArray=jsonObject.getJSONArray("results");
						JSONObject jsonObject2=jsonArray.getJSONObject(0);
						JSONArray jsonArray2=jsonObject2.getJSONArray("weather_data");
						JSONObject jsonObject3=jsonArray2.getJSONObject(0);
						String weather=jsonObject3.getString("weather");
						Log.d("zz", "weather:"+weather);
						int rainy=weather.indexOf("雨");
						int cloudy=weather.indexOf("多云");
						int foggy=weather.indexOf("雾");
						int snowy=weather.indexOf("雪");
						if(snowy!=-1){
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									switchLayout.setBackgroundResource(R.drawable.snowy);
								}
							});							
							return;
						}
						if(rainy!=-1){
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									switchLayout.setBackgroundResource(R.drawable.rainy);
								}
							});								
							return;
						}else{
							if(cloudy!=-1){
								handler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										switchLayout.setBackgroundResource(R.drawable.cloudy);
									}
								});
							}else if(foggy!=-1){
								handler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										switchLayout.setBackgroundResource(R.drawable.foggy);
									}
								});
							}
						}
						
					} catch (Exception e) {
						// TODO: handle exception
						Log.d("zz", e.toString());
					}
					
				}
			}).start();
			locationClient.stop();
		}
		
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
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getActivity().unregisterReceiver(receiver);
		if(db!=null){
			db.close();
			
		}
		handler.removeCallbacksAndMessages(null);
	}
	
	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_previous:
				showTime-=24*60*60*1000L;
				changeDate();
				break;

			case R.id.btn_next:
				showTime+=24*60*60*1000L;
				changeDate();
				break;
			
			case R.id.btn_share:
				int width=switchLayout.getWidth();
				int height=switchLayout.getHeight();
				Bitmap bitmap=Bitmap.createBitmap(width, height, Config.ARGB_8888);
				switchLayout.setDrawingCacheEnabled(true);
				switchLayout.buildDrawingCache();
				bitmap=switchLayout.getDrawingCache();
				
				File f=null;
				try{
					File folder=new File(Environment.getExternalStorageDirectory()+"/earphone");
					if(!folder.exists()){
						folder.mkdirs();
					}
					f=new File(Environment.getExternalStorageDirectory()+"/earphone", "share.png");
				
					
					FileOutputStream fos=new FileOutputStream(f);
					bitmap.compress(CompressFormat.PNG, 80, fos);
					fos.flush();
					fos.close();
					Toast.makeText(getActivity(), getString(R.string.screenshot_successfully), Toast.LENGTH_SHORT).show();
				}catch(Exception e){						
					Log.d("zz", e.toString());
					return;
				}
				
				Intent intent=new Intent(Intent.ACTION_SEND);
				
				File file=new File(Environment.getExternalStorageDirectory()+"/earphone/share.png");

				Uri uri=Uri.fromFile(file);
				if(uri!=null){
					intent.setType("image/*");
					intent.putExtra(Intent.EXTRA_STREAM, uri);
				}else{
					intent.setType("text/plain");
				}								
				intent.putExtra(Intent.EXTRA_SUBJECT, "share");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(Intent.createChooser(intent, getString(R.string.share) ));
				switchLayout.setDrawingCacheEnabled(false);
				switchLayout.destroyDrawingCache();
				break;
				
			case R.id.round_progress1:
				pager.setCurrentItem(1);
				break;
				
			case R.id.round_progress2:
				pager.setCurrentItem(0);
				break;

			}
		
		}
		
	}
	
	//更换日期和UI
	private void changeDate(){
		showDateString=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(showTime));
		tvDate.setText(showDateString);
		//更新计步的UI
		if(showDateString.equals(todayDateString)){
			//若是显示今天，则直接用今天的数据刷新UI
			tvSteps.setNumber(BleService.steps);
			
			tvTargetSteps.setText(getString(R.string.target)+targetSteps+getString(R.string.step));
			
			int percent=(int)(100*(float)BleService.steps/targetSteps);
			if(percent>=100){
				progressBarSteps.setCurrentCount(100);
				tvPercent.setText(getString(R.string.target)+"100.0%");
			}else{
				progressBarSteps.setCurrentCount((int)(100*(float)BleService.steps/targetSteps));
				DecimalFormat df=new DecimalFormat("0.0");
				tvPercent.setText(getString(R.string.complete)+df.format((float)BleService.steps/targetSteps*100)+"%");
			}
			
			int hour=BleService.sportTime/3600;
			int minute=(BleService.sportTime-hour*3600)/60;
			int second=BleService.sportTime%60;
			DecimalFormat df=new DecimalFormat("00");
			tvTime.setText(df.format(hour)+":"+df.format(minute)+":"+df.format(second));
			
			df=new DecimalFormat("0.0");
			tvCalorie.setText(df.format(BleService.calorie)+"k cal");
			
			if(unit==0){
				tvDistance.setText(BleService.distance+"m");
			}else{
				tvDistance.setText(Math.round(BleService.distance/0.3048f)+"ft");
			}
			
			Cursor c=db.rawQuery("SELECT stepString FROM sportData WHERE date=?", new String[]{todayDateString});
			if(c.moveToNext()){
				chartLayout.removeViewAt(0);
				chartLayout.addView(getGraphicalView(c.getString(c.getColumnIndex("stepString"))), 0);
			}
			if(c!=null){
				c.close();
			}
		}else{
			
			Cursor c=db.rawQuery("SELECT * FROM sportData WHERE date=?", new String[]{showDateString});
			if(c.moveToNext()){
				int stepsTemp=c.getInt(c.getColumnIndex("steps"));
				tvSteps.setNumber(c.getInt(c.getColumnIndex("steps")));

				int targetStepsTemp=c.getInt(c.getColumnIndex("targetSteps"));
				tvTargetSteps.setText(getString(R.string.target)+targetStepsTemp+getString(R.string.step));
				
				int percent=(int)(100*(float)stepsTemp/targetStepsTemp);
				if(percent>=100){
					progressBarSteps.setCurrentCount(100);
					tvPercent.setText(getString(R.string.complete)+"100.0%");
				}else{
					progressBarSteps.setCurrentCount((int)(100*(float)stepsTemp/targetStepsTemp));
					DecimalFormat df=new DecimalFormat("0.0");
					tvPercent.setText(getString(R.string.complete)+df.format((float)stepsTemp/targetStepsTemp*100)+"%");
				}
				
				DecimalFormat df=new DecimalFormat("0.0");
				tvCalorie.setText(df.format(c.getFloat(c.getColumnIndex("calorie")))+"k cal");
				
				if(unit==0){
					tvDistance.setText(c.getInt(c.getColumnIndex("distance"))+"m");
				}else{
					tvDistance.setText(Math.round(c.getInt(c.getColumnIndex("distance"))/0.3048f)+"ft");
				}
				
				int sportTime=c.getInt(c.getColumnIndex("sportTime"));
				int hour=sportTime/3600;
				int minute=(sportTime-hour*3600)/60;
				int second=sportTime%60;
				df=new DecimalFormat("00");
				tvTime.setText(df.format(hour)+":"+df.format(minute)+":"+df.format(second));
				
				chartLayout.removeViewAt(0);
				chartLayout.addView(getGraphicalView(c.getString(c.getColumnIndex("stepString"))), 0);
			}else{
				tvSteps.setNumber(0);
				tvTargetSteps.setText(getString(R.string.target)+"10000"+getString(R.string.step));
				progressBarSteps.setCurrentCount(0);
				tvPercent.setText(getString(R.string.complete)+"0.0%");
				tvCalorie.setText("0.0k cal");
				if(unit==0){
					tvDistance.setText("0m");
				}else{
					tvDistance.setText("0ft");
				}
				tvTime.setText("00:00:00");
				chartLayout.removeViewAt(0);
				chartLayout.addView(getGraphicalView(null), 0);
				Toast.makeText(getActivity(), getString(R.string.no_data), Toast.LENGTH_SHORT).show();
			}
			if(c!=null){
				c.close();
			}
			
			
		}
		//更新心率UI
		Cursor c=db.rawQuery("SELECT heartRate, time FROM heartRate WHERE date=?", new String[]{showDateString});
		heartRates.clear();
		while(c.moveToNext()){
			HashMap<String, Object> heartRate=new HashMap<String, Object>();
			heartRate.put("heartRate", c.getInt(c.getColumnIndex("heartRate")));
			heartRate.put("time", c.getString(c.getColumnIndex("time")));
			heartRates.add(heartRate);
		}
		lvAdapter.notifyDataSetChanged();
		
		if(heartRates.size()>0){
			tvNoData.setVisibility(View.GONE);
		}else{
			tvNoData.setVisibility(View.VISIBLE);
		}
		
		c=db.rawQuery("SELECT heartRate FROM heartRate WHERE date=?", new String[]{todayDateString});
		if(!c.moveToNext()){			
			tvAverageHeartRate.setText("0");
		}else{	
			int averageTemp = 0;
			c=db.rawQuery("SELECT AVG(heartRate) AS averageOfHeartRate FROM heartRate WHERE date=?", new String[]{showDateString});					
			if(c.moveToNext()){
				averageTemp=c.getInt(c.getColumnIndex("averageOfHeartRate"));
			}						
			tvAverageHeartRate.setText(""+averageTemp);
		}
		if(c!=null){
			c.close();
		}
	}
	

	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("com.tokool.earphone.action.connected")){
				ivConnection.setImageResource(R.drawable.bluetooth_connected);
			}
			if(action.equals("com.tokool.earphone.action.disconnected")){
				ivConnection.setImageResource(R.drawable.bluetooth_disconnected);
			}
			if(action.equals("update_steps")){
				if(!showDateString.equals(todayDateString)) return;
				
				tvSteps.setText(""+BleService.steps);
			
				int percent=(int)(100*(float)BleService.steps/targetSteps);
				if(percent>=100){
					progressBarSteps.setCurrentCount(100);
					tvPercent.setText(getString(R.string.complete)+"100.0%");
				}else{
					progressBarSteps.setCurrentCount((int)(100*(float)BleService.steps/targetSteps));
					DecimalFormat df=new DecimalFormat("0.0");
					tvPercent.setText(getString(R.string.complete)+df.format((float)BleService.steps/targetSteps*100)+"%");
				}
				
			}
			//更新修改目标步数时的UI实时更新(fragment已存在的情况下)
			if(action.equals("update_target_steps")){
				
				targetSteps=getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("targetSteps", 10000);
				
				if(!showDateString.equals(todayDateString)) return;
								
				tvTargetSteps.setText(getString(R.string.target)+targetSteps+getString(R.string.step));
				
				int percent=(int)(100*(float)BleService.steps/targetSteps);
				if(percent>=100){
					progressBarSteps.setCurrentCount(100);
					tvPercent.setText(getString(R.string.complete)+"100.0%");
				}else{
					progressBarSteps.setCurrentCount((int)(100*(float)BleService.steps/targetSteps));
					DecimalFormat df=new DecimalFormat("0.0");
					tvPercent.setText(getString(R.string.complete)+df.format((float)BleService.steps/targetSteps*100)+"%");
				}
				
			}
			//更新长度单位
			if(action.equals("update_unit")){
				unit=getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("unit", 0);
				changeDate();
				
			}
			//更新运动时间、卡路里、距离
			if(action.equals("update_sportTime_calorie_distance")){
				
				if(!showDateString.equals(todayDateString)) return;
				
				int hour=BleService.sportTime/3600;
				int minute=(BleService.sportTime-hour*3600)/60;
				int second=BleService.sportTime%60;
				DecimalFormat df=new DecimalFormat("00");
				tvTime.setText(df.format(hour)+":"+df.format(minute)+":"+df.format(second));
				df=new DecimalFormat("0.0");
				tvCalorie.setText(df.format(BleService.calorie)+"k cal");
				if(unit==0){
					tvDistance.setText(BleService.distance+"m");
				}else{
					tvDistance.setText(Math.round(BleService.distance/0.3048f)+"ft");
				}
				
				db.execSQL("UPDATE sportData SET steps=?, targetSteps=?, calorie=?, distance=?, sportTime=? WHERE date=?",
						new Object[]{BleService.steps, targetSteps, BleService.calorie, 
								BleService.distance, BleService.sportTime, todayDateString});
				
				//更新数据的同时，对月数据进行统计并保存
				int totalSteps=0;
				int totalDistance=0;
				float totalCalorie=0f;
				int dateOfMonth=Calendar.getInstance().get(Calendar.DATE);
				for(int i=0; i<dateOfMonth; i++){
					long timeTemp=currentTime-i*24*60*60*1000L;
					Date date=new Date(timeTemp);
					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());		
					String dateString=sdf.format(date);
					Cursor c=db.rawQuery("SELECT * FROM sportData WHERE date=?", new String[]{dateString});
					if(c.moveToNext()){
						int steps=c.getInt(c.getColumnIndex("steps"));
						float calorie=c.getFloat(c.getColumnIndex("calorie"));
						int distance=c.getInt(c.getColumnIndex("distance"));
						totalSteps+=steps;
						totalDistance+=distance;
						totalCalorie+=calorie;
					}
					c.close();
				}
				db.execSQL("UPDATE sportDataOfYear SET totalSteps=?, totalCalorie=?, totalDistance=? WHERE month=?",
						new Object[]{totalSteps, totalCalorie, totalDistance, yearString});
				
				if(showDateString.equals(todayDateString)){
					Cursor c=db.rawQuery("SELECT stepString FROM sportData WHERE date=?", new String[]{todayDateString});
					if(c.moveToNext()){
						chartLayout.removeViewAt(0);
						chartLayout.addView(getGraphicalView(c.getString(c.getColumnIndex("stepString"))), 0);
					}
					if(c!=null){
						c.close();
					}
				}
				
			}
			if(action.equals("history_steps_data")){
				SharedPreferences preferences=getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
				int stepLength=preferences.getInt("stepLength", 65);
				int weight=preferences.getInt("weight", 65);
				
				String[] historyData=intent.getStringArrayExtra("history_steps");
				long time=System.currentTimeMillis();
				for(int i=historyData.length-1; i>=0; i--){				
					int totalSteps=0;
					for(int j=0; j<historyData[i].length()/4; j++){
						totalSteps+=Integer.valueOf(historyData[i].substring(j*4, (j+1)*4), 16);
					}
					int distance=stepLength*totalSteps/100;
					float calorie=weight*distance/1000f*1.036f;
					
					Date date=new Date(time-(historyData.length-i-1)*24*3600*1000L);
					SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
					String dateString=dateFormat.format(date);
					
					Cursor c=db.rawQuery("SELECT * FROM sportData WHERE date=?", new String[]{dateString});
					if(c.moveToNext()){
						db.execSQL("UPDATE sportData SET stepString=? WHERE date=?", new Object[]{historyData[i], dateString});
					}else{						
						db.execSQL("INSERT INTO sportData VALUES(null, ?, ?, ?, ?, ?, ?, ?)", new Object[]{totalSteps, targetSteps,
								calorie, distance, 0, historyData[i], dateString});
					}
					if(c!=null){
						c.close();
					}
					
				}
				if(showDateString.equals(todayDateString)){
					Cursor c=db.rawQuery("SELECT stepString FROM sportData WHERE date=?", new String[]{todayDateString});
					if(c.moveToNext()){
						chartLayout.removeViewAt(0);
						chartLayout.addView(getGraphicalView(c.getString(c.getColumnIndex("stepString"))), 0);
					}
					if(c!=null){
						c.close();
					}
				}
			}
			//第二天0点，重置日期字符串，插入新数据
			if(action.equals("time_to_24_hour")){
				currentTime=System.currentTimeMillis();
				Date date=new Date(currentTime);
				SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());		
				todayDateString=df.format(date);
				showDateString=todayDateString;
				Cursor c=db.rawQuery("SELECT * FROM sportData WHERE date=?", new String[]{todayDateString});
				if(!c.moveToNext()){
					db.execSQL("INSERT INTO sportData VALUES(null, ?, ?, ?, ?, ?, ?, ?)", new Object[]{BleService.steps, targetSteps,
							BleService.calorie, BleService.distance, BleService.sportTime, null, todayDateString});
				}
				df=new SimpleDateFormat("yyyy-MM", Locale.getDefault());		
				yearString=df.format(date);	
				c=db.rawQuery("SELECT * FROM sportDataOfYear WHERE month=? ", new String[]{yearString});
				if(!c.moveToNext()){
					db.execSQL("INSERT INTO sportDataOfYear VALUES(null, ?, ?, ?, ?)", new Object[]{0, 0, 0, yearString});
				}
				if(c!=null){
					c.close();
				}
			}
			if(action.equals("update_heart_rate")){
				int heartRate=intent.getIntExtra("heartRate", (int) (Math.random()*50+50));
				SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd hh:mm");
				String timeString=df.format(Calendar.getInstance().getTime());
				HashMap<String, Object> item=new HashMap<String, Object>();
				item.put("time", timeString);
				item.put("heartRate", heartRate);
				heartRates.add(item);
				lvAdapter.notifyDataSetChanged();
				
				df=new SimpleDateFormat("yyyy-MM-dd");
				String dateString=df.format(Calendar.getInstance().getTime());
			
				db.execSQL("INSERT INTO heartRate VALUES(null, ?, ?, ?, ?)", new Object[]{heartRate, timeString, dateString, dateString.substring(0, 7)});
				
				int averageTemp=0;									
				Cursor c=db.rawQuery("SELECT AVG(heartRate) AS averageHeartRate FROM heartRate WHERE date=?", new String[]{dateString});					
				if(c.moveToNext()){
					averageTemp=c.getInt(c.getColumnIndex("averageHeartRate"));
				}
				if(c!=null){
					c.close();
				}
													
				tvAverageHeartRate.setText(averageTemp+"");
				
				
			}
			if(action.equals("turn_back_to_today")){
				currentTime=System.currentTimeMillis();
				showTime=currentTime;
				Date date=new Date(currentTime);
				SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());		
				todayDateString=df.format(date);
				showDateString=todayDateString;
				changeDate();
			}
			
		}
		
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
