package com.marco.a_patientside;








import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.marco.constant.PatientIn;





import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ContactActivity extends Activity  {
     private TextView welcome_name;
     private TextView welcome_roomnumber;
     private String name;
 	 private String contact1;
 	 private String contact2;
 	 private String contact3;
 	 private int idlecount=0;
 	private boolean servicesetting;
 	SharedPreferences preferences;
	 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.contact_layout);
	        getActionBar().hide();
	        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);  
	        telephonyManager.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);   
	        welcome_name=(TextView)findViewById(R.id.welcome_name);
	        welcome_roomnumber=(TextView)findViewById(R.id.welcome_roomnumber);
	        preferences = getSharedPreferences("setting", MODE_PRIVATE);
			contact1=preferences.getString("contact1", "");
			contact2=preferences.getString("contact2", "");
			contact3=preferences.getString("contact3", "");
			servicesetting = preferences.getBoolean("alerswitch",true);
	        
	        Intent intent=getIntent();
	        name=intent.getStringExtra("username");
	        String roomnumber=intent.getStringExtra("roomnumber");
	        welcome_name.setText(name+",欢迎使用心电监测与报警系统");
	        welcome_roomnumber.setText("你的病床号是："+roomnumber);
	        
	        
    		if(!isMyServiceRunning()&&servicesetting)
	        {Intent intent1 = new Intent(ContactActivity.this,FallDownService.class); 
	        startService(intent1);} 
    		BmobQuery<PatientIn> query = new BmobQuery<PatientIn>();
			 query.addWhereEqualTo("PatientName", name);
			 query.findObjects(ContactActivity.this, new FindListener<PatientIn>() {
				@Override
				public void onError(int arg0, String arg1) {
					// TODO Auto-generated method stub
					Toast.makeText(ContactActivity.this, "获取用户信息错误"+arg0+arg1, Toast.LENGTH_SHORT).show();	
				}
				@Override
				public void onSuccess(
						List<PatientIn> arg0) {
					// TODO Auto-generated method stub
					String id = null;
					for(PatientIn patientin:arg0){
						id=patientin.getObjectId();
					}
					PatientIn p2 = new PatientIn();
					p2.setOnline(0);
					p2.update(ContactActivity.this, id, new UpdateListener() {
					    @Override
					    public void onSuccess() {
					        // TODO Auto-generated method stub
					    	//Toast.makeText(ContactActivity.this, "下线成功", Toast.LENGTH_SHORT).show();
					    }
					    @Override
					    public void onFailure(int code, String msg) {
					        // TODO Auto-generated method stub
					    	Toast.makeText(ContactActivity.this, "远程心电实时监测下线失败"+code+msg, Toast.LENGTH_SHORT).show();	
					    }
					});
				}             					 
			 });
	 }
	 protected void onResume(){
		 super.onResume();
	        preferences = getSharedPreferences("setting", MODE_PRIVATE);
			contact1=preferences.getString("contact1", "");
			contact2=preferences.getString("contact2", "");
			contact3=preferences.getString("contact3", "");
	 }

	 private class PhoneListener extends PhoneStateListener {  
	        @Override  
	        public void onCallStateChanged(int state, String incomingNumber) {  
	            super.onCallStateChanged(state, incomingNumber);  
	            switch (state) {  
	            case TelephonyManager.CALL_STATE_RINGING://来电状态  
	                break;  
	            case TelephonyManager.CALL_STATE_OFFHOOK://接听状态  
	                break;            
	            case TelephonyManager.CALL_STATE_IDLE://挂断后回到空闲状态  
	            	if(idlecount!=0)
	            		readcallrecord();
	            	idlecount=idlecount+1;
	                break;  
	                  
	            default:  
	                break;  
	            }  
	        }  
	          
	    }  
     public void readcallrecord(){
    	 Cursor cursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI,                            
    		        null, null, null, null);                                                                                                 
    		cursor.moveToLast();                                                                                                                                                                                                                                                              
    		        String number = cursor.getString(cursor.getColumnIndex(Calls.NUMBER));                                                                         
    		        String type;                                                                                     
    		        switch (Integer.parseInt(cursor.getString(cursor.getColumnIndex(Calls.TYPE)))) {                 
    		        case Calls.INCOMING_TYPE:                                                                        
    		            type = "呼入";                                                                                 
    		            break;                                                                                       
    		        case Calls.OUTGOING_TYPE:                                                                        
    		            type = "呼出";                                                                                 
    		            break;                                                                                       
    		        case Calls.MISSED_TYPE:                                                                          
    		            type = "未接";                                                                                 
    		            break;                                                                                       
    		        default:                                                                                         
    		            type = "挂断";                                      
    		            break; 
    		        }                                                         
    		        String duration = cursor.getString(cursor.getColumnIndexOrThrow(Calls.DURATION));                
    		     
		           if(duration.equals("0")&&type.equals("呼出")){
 		           if(number.equals(contact1)){
 		        	   if(!contact2.equals("")){
 		  	            Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact2));  
 			            startActivity(intent);}else if(!contact3.equals("")){
 	 		  	         Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact3));  
 	 			         startActivity(intent);}else{
 	 		  	         Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact1));  
 	 			         startActivity(intent);
 			            }
 		           }else if(number.equals(contact2)){
 		        	   if(!contact3.equals("")){
 	 	 		  	     Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact3));  
 	 	 			     startActivity(intent);}else if(!contact1.equals("")){
 	 	 		  	     Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact1));  
 	 	 			     startActivity(intent);}else{
 	 	 	 		  	 Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact2));  
 	 	 	 			 startActivity(intent);
 	 	 			     }
 		           }else if(number.equals(contact3)){
 		        	   if(!contact1.equals("")){
 		 		  	      Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact1));  
 		 			      startActivity(intent);}else if(!contact2.equals("")){
 	 	 	 	 		  Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact2));  
 	 	 	 	 	      startActivity(intent);}else{
 	 	 	 		  	  Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact3));  
 	 	 	 			  startActivity(intent);  
 	 	 	 	 	      }
 		           }
 		        }    		                                                                                                          
    		}
     


	 public void menuone(View view){
		    Intent intent= new Intent();
			intent.putExtra("username", name);
			intent.setClass(ContactActivity.this, OffLineRead.class);
			startActivity(intent);
	 }
	 public void menuthree(View view){
		    Intent intent= new Intent();
			intent.putExtra("username", name);
			intent.setClass(ContactActivity.this, OtherFunction.class);
			startActivity(intent);
	 }
	 public void menutwo(View view){
		    Intent intent= new Intent();
		    intent.putExtra("username", name);
		    intent.setClass(ContactActivity.this, OnLineRead.class);
		    startActivity(intent);
	 }
	 public void menufour(View view){
		 if(!contact1.equals("")){
	         Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact1));  
	         startActivity(intent);  
		 }else if(!contact2.equals("")){
	         Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact2));  
	         startActivity(intent);  
		 }else if(!contact3.equals("")){
	         Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contact3));  
	         startActivity(intent);  
		 }else
			 Toast.makeText(ContactActivity.this, "没有有效号码，请在设置中填写", Toast.LENGTH_SHORT).show();

	 }
		private boolean isMyServiceRunning() {
		    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		        if ("com.marco.a_patientside.FallDownService".equals(service.service.getClassName())) {
		            return true;
		        }
		    }
		    return false;
		}

	 
	

}
