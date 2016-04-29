package com.tokool.earphone.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.tokool.earphone.R;
import com.tokool.earphone.service.BleService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ClockActivity extends Activity {

	private ImageView btnBack, btnAdd, btnDelete;
	private ListView lvClocks;
	private ArrayList<HashMap<String, Object>> clocks;
	private MyBaseAdapter adapter;
	private boolean isDeleting=false;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_clock);
		
		clocks=new ArrayList<HashMap<String, Object>>();
		
		String s=getSharedPreferences("clocks", Context.MODE_PRIVATE).getString("clocks", null);
		if(s!=null){
			try {
				clocks=(ArrayList<HashMap<String, Object>>)string2List(s);
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		adapter=new MyBaseAdapter();
		lvClocks=(ListView)findViewById(R.id.lv_clocks);
		lvClocks.setAdapter(adapter);
		lvClocks.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				// TODO Auto-generated method stub
				if(isDeleting){
					CheckBox cb=(CheckBox)v.findViewById(R.id.cb_delete);
					cb.toggle();
					clocks.get(position).put("delete", cb.isChecked());					
				}else{
					Intent intent=new Intent(ClockActivity.this,  AddClockActivity.class);
					intent.putExtra("day", (boolean[])clocks.get(position).get("day"));
					intent.putExtra("hour", (Integer)clocks.get(position).get("hour"));
					intent.putExtra("minute", (Integer)clocks.get(position).get("minute"));
					intent.putExtra("isSet", true);
					startActivityForResult(intent, position);
				}				
			}
		});
		
		lvClocks.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
				// TODO Auto-generated method stub
				if(!isDeleting){
					CheckBox cb=(CheckBox)v.findViewById(R.id.cb_delete);				
					for(int i=0; i<clocks.size(); i++){
						clocks.get(i).put("delete", false);
					}	
					clocks.get(position).put("delete", true);
					btnDelete.setVisibility(View.VISIBLE);
					btnAdd.setVisibility(View.GONE);
					isDeleting=true;
					adapter.notifyDataSetChanged();
				}
				return true;
			}
		});
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isDeleting){
					btnDelete.setVisibility(View.GONE);
					btnAdd.setVisibility(View.VISIBLE);
					isDeleting=false;
					for(int i=0; i<clocks.size(); i++){
						clocks.get(i).put("delete", false);
					}				
					adapter.notifyDataSetChanged();					
				}else{
					finish();
				}				
			}
		});
		
		btnDelete=(ImageView)findViewById(R.id.btn_delete);
		btnDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int count=0;
				for(int i=0; i<clocks.size(); i++){
					if((Boolean)clocks.get(i).get("delete")){
						break;
					};
					count++;
				}	
				if(count==clocks.size()){
					Toast.makeText(ClockActivity.this, getString(R.string.select_the_reminder_you_need_to_delete), Toast.LENGTH_SHORT).show();
					return;
				}
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_tip, null);
				((TextView)dialog.findViewById(R.id.tv_title)).setText(getString(R.string.delete_reminder));
				((TextView)dialog.findViewById(R.id.tv_tip)).setText(getString(R.string.sure_to_delete_the_reminder));
				new AlertDialog.Builder(ClockActivity.this)
					.setView(dialog)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							btnDelete.setVisibility(View.GONE);
							btnAdd.setVisibility(View.VISIBLE);
							isDeleting=false;
							Iterator<HashMap<String, Object>> clocksIter=clocks.iterator();
							while(clocksIter.hasNext()){
								HashMap<String, Object> clock=clocksIter.next();
								if((Boolean) clock.get("delete")){
									clocksIter.remove();
								}
							}
							try {
								getSharedPreferences("clocks", Context.MODE_PRIVATE).edit().putString("clocks", list2String(clocks)).commit();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							adapter.notifyDataSetChanged();
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
		
		btnAdd=(ImageView)findViewById(R.id.btn_add);
		btnAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(clocks.size()==10){
					Toast.makeText(ClockActivity.this, getString(R.string.can_not_create_more), Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent=new Intent(ClockActivity.this, AddClockActivity.class);
				intent.putExtra("isSet", false);
				startActivityForResult(intent, 100);
			}
		});
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(isDeleting){
				btnDelete.setVisibility(View.GONE);
				btnAdd.setVisibility(View.VISIBLE);
				isDeleting=false;
				for(int i=0; i<clocks.size(); i++){
					clocks.get(i).put("delete", false);
				}				
				adapter.notifyDataSetChanged();
				return false;
			}
		}
		finish();
		return super.onKeyUp(keyCode, event);
	}



	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode==1){
			if(!BleService.isConnected){
				Toast.makeText(ClockActivity.this, getString(R.string.can_not_change_when_disconnected), Toast.LENGTH_SHORT).show();
				return;
			}
			if(requestCode==100){
				HashMap<String, Object> clock=new HashMap<String, Object>();
				clock.put("hour", data.getIntExtra("hour", new Date().getHours()));
				clock.put("minute", data.getIntExtra("minute", new Date().getMinutes()));
				clock.put("day", data.getBooleanArrayExtra("day"));
				clock.put("isOpen", true);
				clock.put("delete", false);
				clocks.add(clock);
				adapter.notifyDataSetChanged();
				try {
					getSharedPreferences("clocks", Context.MODE_PRIVATE).edit().putString("clocks", list2String(clocks)).commit();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Intent intent=new Intent("set_clocks");
				intent.putExtra("clocks", (Serializable)clocks);
				sendBroadcast(intent);
			}else{
				HashMap<String, Object> clock=clocks.get(requestCode);
				clock.put("hour", data.getIntExtra("hour", new Date().getHours()));
				clock.put("minute", data.getIntExtra("minute", new Date().getMinutes()));
				clock.put("day", data.getBooleanArrayExtra("day"));
				clock.put("isOpen", true);
				clock.put("delete", false);
				adapter.notifyDataSetChanged();
				try {
					getSharedPreferences("clocks", Context.MODE_PRIVATE).edit().putString("clocks", list2String(clocks)).commit();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Intent intent=new Intent("set_clocks");
				intent.putExtra("clocks", (Serializable)clocks);
				sendBroadcast(intent);
			}
		}
	}
	
	private class MyBaseAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return clocks.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return clocks.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}
		
		private class ViewHolder{
			private TextView tvTime;
			private TextView tvCycle;
			private CheckBox cbToggle;
			private CheckBox cbDelete;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView!=null){
				holder=(ViewHolder) convertView.getTag();
			}else{
				holder=new ViewHolder();
				convertView=getLayoutInflater().inflate(R.layout.list_item_clocks, null);
				holder.tvTime=(TextView) convertView.findViewById(R.id.tv_time);
				holder.tvCycle=(TextView)convertView.findViewById(R.id.tv_cycle);
				holder.cbToggle=(CheckBox)convertView.findViewById(R.id.cb_toggle);
				holder.cbDelete=(CheckBox)convertView.findViewById(R.id.cb_delete);
				convertView.setTag(holder);
			}
			
			HashMap<String, Object> clock=clocks.get(position);
			DecimalFormat format=new DecimalFormat("00");
			holder.tvTime.setText(format.format(clock.get("hour"))+":"+format.format(clock.get("minute")));
			boolean[] day=(boolean[]) clock.get("day");
			holder.tvCycle.setText((day[0]?getString(R.string.monday)+"  ":"")+(day[1]?getString(R.string.tuesday)+"  ":"")
					+(day[2]?getString(R.string.wednesday)+"  ":"")+(day[3]?getString(R.string.thursday)+"  ":"")
					+(day[4]?getString(R.string.friday)+"  ":"")+(day[5]?getString(R.string.saturday)+"  ":"")
					+(day[6]?getString(R.string.sunday)+"  ":""));
			holder.cbToggle.setChecked((Boolean)clock.get("isOpen"));
			holder.cbToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
					// TODO Auto-generated method stub
					clocks.get(position).put("isOpen", isChecked);
					try {
						getSharedPreferences("clocks", Context.MODE_PRIVATE).edit().putString("clocks", list2String(clocks)).commit();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Intent intent=new Intent("set_clocks");
					intent.putExtra("clocks", (Serializable)clocks);
					sendBroadcast(intent);
				}
			});
			holder.cbDelete.setChecked((Boolean)clock.get("delete"));
			holder.cbDelete.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
					// TODO Auto-generated method stub
					clocks.get(position).put("delete", isChecked);	
					Intent intent=new Intent("set_clocks");
					intent.putExtra("clocks", (Serializable)clocks);
					sendBroadcast(intent);
				}
			});
			if(isDeleting){
				holder.cbToggle.setVisibility(View.GONE);
				holder.cbDelete.setVisibility(View.VISIBLE);
			}else{
				holder.cbToggle.setVisibility(View.VISIBLE);
				holder.cbDelete.setVisibility(View.GONE);
			}
			return convertView;
		}
		
	}
	
	//list转String
	public static String list2String(List<?> list) throws IOException{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(baos);
		oos.writeObject(list);
		String s=new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
		oos.close();
		return s;	
	}
	
	//String转list
	public static List<?> string2List(String s) throws StreamCorruptedException, IOException, ClassNotFoundException{
		byte[] bytes=Base64.decode(s.getBytes(), Base64.DEFAULT);
		ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
		ObjectInputStream ois=new ObjectInputStream(bais);
		List<?> list=(List<?>) ois.readObject();
		ois.close();
		return list;
	}

}
