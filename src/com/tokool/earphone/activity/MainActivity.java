package com.tokool.earphone.activity;

import com.tokool.earphone.R;
import com.tokool.earphone.fragment.DetailFragment;
import com.tokool.earphone.fragment.HomeFragment;
import com.tokool.earphone.fragment.MineFragment;
import com.tokool.earphone.service.BleService;
import com.tokool.earphone.fragment.DeviceFragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends Activity {

	private RadioGroup tabs;
	private HomeFragment homeFragment;
	private DetailFragment detailFragment;
	private DeviceFragment settingFragment;
	private MineFragment mineFragment;
	private Fragment from;
	private long backFirstTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		homeFragment=new HomeFragment();
		detailFragment=new DetailFragment();
		settingFragment=new DeviceFragment();
		mineFragment=new MineFragment();
		from=homeFragment;
		
		getFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment, "home").commit();
		
		tabs=(RadioGroup)findViewById(R.id.tabs);
		tabs.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup arg0, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.tab_home:
					changeFragment(homeFragment, "home");
					sendBroadcast(new Intent("turn_back_to_today"));
					break;

				case R.id.tab_detail:
					changeFragment(detailFragment, "detail");
					break;
					
				case R.id.tab_setting:
					changeFragment(settingFragment, "device");
					break;
	
				case R.id.tab_mine:
					changeFragment(mineFragment, "mine");
					break;
				}
			}
		});		
		
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
						
		if(keyCode==KeyEvent.KEYCODE_BACK){
			long backSecondTime=System.currentTimeMillis();
			if(backSecondTime-backFirstTime>800){
				Toast.makeText(this, getString(R.string.one_more_click_exit), Toast.LENGTH_SHORT).show();
				backFirstTime=backSecondTime;
				return true;
			}else{
				finish();
			}
		}
		return false;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();		
		stopService(new Intent(this, BleService.class));
	}
	
	private void changeFragment(Fragment to, String tag){
		FragmentTransaction transaction=getFragmentManager().beginTransaction();
		if(to.isAdded()){
			transaction.hide(from).show(to).commit();
		}else{
			transaction.hide(from).add(R.id.fragment_container, to, tag).commit();
		}
		from=to;
	}

}
