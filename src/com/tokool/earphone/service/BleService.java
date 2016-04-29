package com.tokool.earphone.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class BleService extends Service {
	
	private UUID serviceUUID=UUID.fromString("00001523-1212-efde-1523-785feabcd123");
	private UUID characteristicWriteUUID=UUID.fromString("00001524-1212-efde-1523-785feabcd123");
	private UUID characteristicReadUUID=UUID.fromString("00001525-1212-efde-1523-785feabcd123");
	private UUID descriptorUUID=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	
	private BluetoothGatt btGatt;
	private BluetoothGattCharacteristic characteristic;
	private String address;
	
	private MyBroadcastReceiver receiver;
	private Handler handler;
	private Timer timer;
	
	public static boolean isConnected=false;
	public static int electricity=0;
	public static boolean isCalledOpen=false;
	public static boolean isSitOpen=false;
	public static int steps=0;
	public static int sportTime=0;
	public static float calorie=0;
	public static int distance=0;
	private boolean isNoDataComing=true;
	private boolean hasInitLastStep=false;//连上设备第一次查询实时步数时，用该变量控制在接收数据函数中只初始化一次
	private int lastDataCameTime;//记录上次计步数据到来的时间，并用现在时间减去它，若大于20秒则停止运动计时
	private int lastSteps;//5秒之前的步数
	private int previousSteps;//每次获取步数的上一个步数
	private int day;
	private StringBuffer stepString;
	private SportTimeTask sportTimeTask;
	private boolean abandon=false;//该值用于舍去一个心率值，硬件老饼不想改，要软件去弥补，呵呵
	

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		//设置一个第二天的闹钟，使APP运行期间跨过0点时能重置数据，避免叠加
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.DATE, 1);
		Intent intent=new Intent("time_to_24_hour");
		PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager am=(AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000L, pendingIntent);
		
		
		SharedPreferences preferences=getSharedPreferences("stepInfo", Context.MODE_PRIVATE);
		steps=preferences.getInt("steps", 0);
		sportTime=preferences.getInt("sportTime", 0);
		calorie=preferences.getFloat("calorie", 0f);
		distance=preferences.getInt("distance", 0);
		day=preferences.getInt("day", 0);
		
		//这里判断是否到了第二天，若到了第二天，重置todaySteps和time,并把昨天的步数录入本地数据库
		if(new Date().getDay()!=day){
			steps=0;
			sportTime=0;
			calorie=0f;
			distance=0;
			day=new Date().getDay();
			
		}
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("request_connect");
		filter.addAction("request_disconnect");				
		filter.addAction("start_count");
		filter.addAction("get_history_steps");
		filter.addAction("set_clocks");
		filter.addAction("synchronize_time");
		filter.addAction("query_electricity");
		filter.addAction("time_to_24_hour");
		filter.addAction("open_long_sit_remind");
		filter.addAction("close_long_sit_remind");
		filter.addAction("query_long_sit_remind");
		filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		registerReceiver(receiver, filter);
		
		handler=new Handler();
		
		timer=new Timer();
		timer.schedule(new ConnectionDetector(), 0, 10000);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();		
		if(btGatt!=null){			
			btGatt.disconnect();
			btGatt.close();
			btGatt=null;
		}		
		isConnected=false;
		
		SharedPreferences.Editor editor=getSharedPreferences("stepInfo", Context.MODE_PRIVATE).edit();
		editor.putInt("steps", steps);
		editor.putInt("sportTime", sportTime);
		editor.putFloat("calorie", calorie);
		editor.putInt("distance", distance);
		editor.putInt("day", new Date().getDay());
		editor.commit();
		
		handler.removeCallbacksAndMessages(null);
		
		if(timer!=null){
			timer.cancel();
		}
		
		unregisterReceiver(receiver);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		address=getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).getString("address", "unbound");
		if(!address.equals("未绑定")){
			sendBroadcast(new Intent("request_connect"));
		}	
		return super.onStartCommand(intent, flags, startId);
		
		
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("request_connect")){
				if(!address.equals("unbound")){
					BluetoothDevice device=((BluetoothManager)getSystemService(BLUETOOTH_SERVICE))
							.getAdapter().getRemoteDevice(address);
					
					if(device.getName()!=null){
						if(btGatt!=null){
							btGatt.disconnect();
							btGatt.close();
							btGatt=null;
						}
						btGatt=device.connectGatt(BleService.this, true, mBluetoothGattCallback);			
					}else{
						Toast.makeText(BleService.this, "设备不在范围内", Toast.LENGTH_SHORT).show();
					}
				}
				
				
			}
			if(action.equals("request_disconnect")){
				
			}
			//查询电量
			if(action.equals("query_electricity")){				
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x13;
				bytes[2]=(byte)0x00;
				bytes[3]=(byte)0x00;
				writeCommand(bytes);
				
			}
			//获取实时计步
			if(action.equals("start_count")){	
				
				//开始计时
				if(sportTimeTask!=null){
					sportTimeTask.cancel();
					sportTimeTask=null;
				}
				sportTimeTask=new SportTimeTask();
				timer.schedule(sportTimeTask, 0, 1000);
				
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x51;
				bytes[2]=(byte)0x01;
				bytes[3]=(byte)0x00;
				writeCommand(bytes);				
			}
			//获取历史计步
			if(action.equals("get_history_steps")){
				if(stepString==null){
					stepString=new StringBuffer();
				}else{
					stepString.delete(0, stepString.length());
				}
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x52;
				bytes[2]=(byte)0x01;
				bytes[3]=(byte)0x00;
				writeCommand(bytes);
			}
			//设置闹钟，需传10条
			if(action.equals("set_clocks")){		
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, Object>> clocks=(ArrayList<HashMap<String, Object>>)intent.getSerializableExtra("clocks");
				//遍历闹钟，除去关闭状态的闹钟
				Iterator<HashMap<String, Object>> iterator=clocks.iterator();
				while (iterator.hasNext()) {
					HashMap<String, Object> clock = (HashMap<String, Object>) iterator.next();
					if(!(Boolean)clock.get("isOpen")){
						iterator.remove();
					}
				}
				//设置开启的闹钟
				for(int i=0; i<clocks.size(); i++){
					boolean[] day=(boolean[])clocks.get(i).get("day");
					byte dayBit=0x00;//初始星期位，8个bit为“0”表示周一到周日都为false状态
					byte bit=0x01;//用于位运算，通过对day的遍历，左移后作 与运算 达到初始化dayBit的效果;
					for(int j=0; j<day.length; j++){
						if(day[j]){
							dayBit=(byte)(dayBit|(bit<<j));
						}
					}
					byte[] bytes=new byte[9];
					bytes[0]=(byte)0x00;
					bytes[1]=(byte)0x98;
					bytes[2]=(byte)0x00;
					bytes[3]=(byte)0x05;
					bytes[4]=(byte)i;   //闹钟编号
					bytes[5]=(byte)(Integer.parseInt(clocks.get(i).get("hour").toString()));//小时
					bytes[6]=(byte)(Integer.parseInt(clocks.get(i).get("minute").toString())+1);//分钟
					bytes[7]=(byte)0x1e;//震动30秒
					bytes[8]=(byte)dayBit;//重复的星期几
					writeCommand(bytes);
				}
				//因为要发10条，但list里并没有10个item，所以补上数据位置0的闹钟
				for(int i=clocks.size(); i<10; i++){
					byte[] bytes=new byte[9];
					bytes[0]=(byte)0x00;
					bytes[1]=(byte)0x98;
					bytes[2]=(byte)0x00;
					bytes[3]=(byte)0x05;
					bytes[4]=(byte)i;   //闹钟编号
					bytes[5]=(byte)0x00;//小时
					bytes[6]=(byte)0x00;//分钟
					bytes[7]=(byte)0x00;//无震动
					bytes[8]=(byte)0x00;//无周期
					writeCommand(bytes);
				}
			}
			//将手机时间同步到设备上，以便设置闹钟
			if(action.equals("synchronize_time")){				
				SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
				long timeBefore=0;
				try {
					timeBefore=format.parse("2000-1-1").getTime();	//1970-1-1到2000-1-1的毫秒数				
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int time=(int)((new Date().getTime()-timeBefore)/1000); //2000-1-1到现在的秒数
				byte[] bytes=new byte[8];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x21;
				bytes[2]=(byte)0x00;
				bytes[3]=(byte)0x04;
				bytes[4]=(byte)((time>>24)&0xff);
				bytes[5]=(byte)((time>>16)&0xff);
				bytes[6]=(byte)((time>>8)&0xff);
				bytes[7]=(byte)(time&0xff);
				writeCommand(bytes);
				
			}
			//到第二天0点重置
			if(action.equals("time_to_24_hour")){
				BleService.steps=0;
				BleService.calorie=0f;
				BleService.distance=0;
				BleService.sportTime=0;
			}
			//监听来电
			if(action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
				TelephonyManager telephonyManager=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				int state=telephonyManager.getCallState();
				if(state==TelephonyManager.CALL_STATE_RINGING){
					if(isCalledOpen){
						byte[] bytes=new byte[4];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x97;
						bytes[2]=(byte)0x00;
						bytes[3]=(byte)0x14;
						writeCommand(bytes);
					}
				}	
				abortBroadcast();
				
			}
			//开启久坐提醒(30min)
			if(action.equals("open_long_sit_remind")){
				byte[] bytes=new byte[9];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x99;
				bytes[2]=(byte)0x00;
				bytes[3]=(byte)0x05;
				bytes[4]=(byte)0x30;//间隔30分钟
				bytes[5]=(byte)0x00;
				bytes[6]=(byte)0x01;//起始00:01
				bytes[7]=(byte)0x17;
				bytes[8]=(byte)0x3b;//结束23:59
						
				writeCommand(bytes);
			}
			//关闭久坐提醒
			if(action.equals("close_long_sit_remind")){
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x99;
				bytes[2]=(byte)0x01;
				bytes[3]=(byte)0x00;
				writeCommand(bytes);
			}
			//查询久坐提醒
			if(action.equals("query_long_sit_remind")){
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x99;
				bytes[2]=(byte)0x02;
				bytes[3]=(byte)0x00;
				writeCommand(bytes);
			}
		}
		
	}
	
	//往设备写命令
	private void writeCommand(byte[] bytes){		
		if(!isConnected){
			Toast.makeText(BleService.this, "没有连接设备", Toast.LENGTH_SHORT).show();
			return;
		}
		if(btGatt!=null && characteristic!=null){							
			characteristic.setValue(bytes);					
			btGatt.writeCharacteristic(characteristic);
		}			
	}
	
	private BluetoothGattCallback mBluetoothGattCallback=new BluetoothGattCallback(){

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			// TODO Auto-generated method stub
			if(newState==BluetoothGatt.STATE_CONNECTED){
				
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(BleService.this, "已连上设备", Toast.LENGTH_SHORT).show();
					}
				});
				String name=gatt.getDevice().getName();
				address=gatt.getDevice().getAddress();
				Intent intent=new Intent();
				intent.setAction("com.tokool.earphone.action.connected");
				intent.putExtra("name", name);
				sendBroadcast(intent);
				isConnected=true;
				hasInitLastStep=false;			
				gatt.discoverServices();
			}else if(newState==BluetoothGatt.STATE_DISCONNECTED){
				isConnected=false;
				electricity=0;
				hasInitLastStep=false;			
				sendBroadcast(new Intent("com.tokool.earphone.action.disconnected"));				
				
			}
		}
		
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			if(status==BluetoothGatt.GATT_SUCCESS){
				BluetoothGattService gattService=gatt.getService(serviceUUID);							
				characteristic=gattService.getCharacteristic(characteristicWriteUUID);
				gatt.setCharacteristicNotification(characteristic, true);
											
				BluetoothGattCharacteristic gattCharacteristicRead=gattService.getCharacteristic(characteristicReadUUID);
				gatt.setCharacteristicNotification(gattCharacteristicRead, true);
				BluetoothGattDescriptor gattDescriptor=gattCharacteristicRead.getDescriptor(descriptorUUID);
				gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				gatt.writeDescriptor(gattDescriptor);
				
				sendBroadcast(new Intent("synchronize_time"));
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						sendBroadcast(new Intent().setAction("start_count"));
						sendBroadcast(new Intent("query_electricity"));
						sendBroadcast(new Intent("get_history_steps"));
						sendBroadcast(new Intent("query_long_sit_remind"));
					}
				}, 500);				
				
			}
		}
		
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			byte[] bytes=characteristic.getValue();
			String response=bytes2HexString(bytes);
			if(response.substring(0, 8).equals("b0510204")){
				
				//只初始化一次
				if(!hasInitLastStep){
					SharedPreferences preferences=getSharedPreferences("userInfo", Context.MODE_PRIVATE);
					int stepLength=preferences.getInt("stepLength", 65);
					int weight=preferences.getInt("weight", 65);					
					
					int stepTemp=Integer.valueOf(response.substring(8), 16);
					//若硬件上的步数大于APP，则表示硬件没断电，取其新增步数；反之，硬件断电，把期间的数据全加
					if(stepTemp>=steps){
						distance+=stepLength*(stepTemp-steps)/100;
						steps+=stepTemp-steps;						
						calorie+=weight*distance/1000f*1.036f;
					}
					SharedPreferences.Editor editor=preferences.edit();
					editor.putInt("steps", steps);
					editor.putFloat("calorie", calorie);
					editor.putInt("sportTime", sportTime);
					editor.putInt("distance", distance);
					editor.putInt("day", new Date().getDay());
					editor.commit();
					//记录硬件的上次步数，用于下次相减后得到叠加数据
					previousSteps=Integer.valueOf(response.substring(8), 16);
					lastSteps=steps;
					hasInitLastStep=true;
					sendBroadcast(new Intent("update_sportTime_calorie_distance"));
					sendBroadcast(new Intent("update_steps"));
					return;
				}
				
				lastDataCameTime=sportTime;
				//使继续计时
				isNoDataComing=false;
				
				//从硬件获取步数，采用叠加的方式加到steps上
				int currentSteps=Integer.valueOf(response.substring(8), 16);	
				if(currentSteps-previousSteps>0){
					steps+=currentSteps-previousSteps;
					//记录硬件的上次步数，用于下次相减后得到叠加数据
					previousSteps=currentSteps;
					sendBroadcast(new Intent("update_steps"));
				}
				
			}
			//获取计步历史数据
			if(response.substring(0, 4).equals("b052")){
				Log.d("zz", "history:"+response);
				stepString.append(response.substring(8));									
			}
			//计步历史数据传输完毕（同步成功）
			if(response.substring(0, 8).equals("b0530200")){
				Log.d("zz", "同步完毕:"+response);
				String[] historyData=new String[stepString.length()/48];
				for(int i=0; i<stepString.length()/48; i++){
					historyData[i]=stepString.substring(i*48, (i+1)*48);
				}
				Intent intent=new Intent("history_steps_data");
				intent.putExtra("history_steps", historyData);
				
				SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
				String dateString=df.format(Calendar.getInstance().getTime());				
				getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().putString("sychronize_time", dateString).commit();
				
				sendBroadcast(intent);
			}			
			//电量
			if(response.substring(0, 8).equals("b0130101")){
				electricity=Integer.parseInt(response.substring(8, 10), 16);
				sendBroadcast(new Intent().setAction("update_electricity"));
			}
			//没久坐
			if(response.substring(0, 8).equals("b0990100")){
				isSitOpen=false;
				sendBroadcast(new Intent("update_long_sit_toggle"));
				getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().putBoolean("isSitOpen", false).commit();
			}
			//有久坐
			if(response.substring(0, 8).equals("b0990005")){
				isSitOpen=true;
				sendBroadcast(new Intent("update_long_sit_toggle"));
				getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().putBoolean("isSitOpen", true).commit();
			}
			if(response.substring(0, 4).equals("b054")){
				if(abandon){
					abandon=false;
					return;
				}
				int heartRate=Integer.valueOf(response.substring(4, 6), 16);				
				sendBroadcast(new Intent("update_heart_rate").putExtra("heartRate", heartRate));
				abandon=true;
			}
		}		
		
	};
	
	//byte转16进制字符串
	public String bytes2HexString(byte[] bytes){
		String hexString="";
		String temp="";
		for(int i=0; i<bytes.length; i++){
			temp=(Integer.toHexString(bytes[i] & 0xFF));
			if(temp.length() == 1){
				hexString=hexString+"0"+temp;
			}else{
				hexString=hexString+temp;
			}
		}	
		return hexString.toLowerCase();
	}
	
	public class SportTimeTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub			
			if(!isNoDataComing && isConnected){
				sportTime++;	
				//每5秒处理一次数据，并保存，以免硬件断电重置数据
				if(sportTime%5==0){
					SharedPreferences preferences=getSharedPreferences("userInfo", Context.MODE_PRIVATE);
					int stepLength=preferences.getInt("stepLength", 65);
					int weight=preferences.getInt("weight", 65);
					
					int alphaSteps=steps-lastSteps;
					if(alphaSteps<0) alphaSteps=0;
					//每5秒的速度
					float speed=(float)(alphaSteps*stepLength*0.01/5);
					//跑400米所需时间（分钟）
					float timeNeed=400/speed/60;
					//卡路里=体重(kg)*时间(h)*K(指数K=30/(跑400米所需的分钟数))
					calorie+=(float)weight*(5f/3600)*(30/timeNeed);
					
					distance+=stepLength*alphaSteps/100;
					
					lastSteps=steps;
					
					SharedPreferences.Editor editor=preferences.edit();
					editor.putInt("steps", steps);
					editor.putFloat("calorie", calorie);
					editor.putInt("sportTime", sportTime);
					editor.putInt("distance", distance);
					editor.putInt("day", new Date().getDay());
					editor.commit();
					
				}
				//若上次数据到现在超过5秒则不再计时
				if(sportTime-lastDataCameTime>4){
					isNoDataComing=true;
				}
				sendBroadcast(new Intent("update_sportTime_calorie_distance"));
			}
		}
		
	}
	
	//用于检测蓝牙的实时连接状态，防止异常断开又没有触发onConnectionStateChange回调造成UI没有及时更新的情况
	public class ConnectionDetector extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			BluetoothManager btManager=(BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
			if(btGatt!=null){
				BluetoothDevice device=btGatt.getDevice();
				if(device!=null){
					int state=btManager.getConnectionState(device, BluetoothGatt.GATT);
					if(state==BluetoothGatt.STATE_DISCONNECTED){
						isConnected=false;
						electricity=0;
						sendBroadcast(new Intent("com.tokool.earphone.action.disconnected"));
					}
				}
			}
		}
		
	}

}
