package com.tokool.earphone.receiver;

import com.tokool.earphone.service.CallingRemindService;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallingReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context c, Intent intent) {
		// TODO Auto-generated method stub
		
		
		//若Bleservice不运行，则启动CallingRemindService连接设备并发送命令给硬件
		boolean isRunning=false;
		ActivityManager manager=(ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);		
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	         if ("com.tokool.earphone.service.BleService".equals(service.service.getClassName())) {
	             isRunning=true;
	         }
	    }
		if(!isRunning){
			if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
				TelephonyManager telephonyManager=(TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
				int state=telephonyManager.getCallState();
				if(state==TelephonyManager.CALL_STATE_RINGING){
					boolean isCalledOpen=c.getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).getBoolean("isCalledOpen", false);
					if(isCalledOpen){
						c.startService(new Intent(c, CallingRemindService.class));
					}
				}				
			}
		}
	}

}
