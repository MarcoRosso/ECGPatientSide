package com.marco.a_patientside;








import java.sql.Date;
import java.text.SimpleDateFormat;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;





import cn.bmob.v3.BmobUser;
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
	        welcome_name.setText(name+",��ӭʹ���ĵ����뱨��ϵͳ");
	        welcome_roomnumber.setText("��Ĳ������ǣ�"+roomnumber);
	        
	        
    		if(!isMyServiceRunning()&&servicesetting)
	        {Intent intent1 = new Intent(ContactActivity.this,FallDownService.class); 
	        startService(intent1);} 
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
	            case TelephonyManager.CALL_STATE_RINGING://����״̬  
	                break;  
	            case TelephonyManager.CALL_STATE_OFFHOOK://����״̬  
	                break;            
	            case TelephonyManager.CALL_STATE_IDLE://�ҶϺ�ص�����״̬  
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
    		            type = "����";                                                                                 
    		            break;                                                                                       
    		        case Calls.OUTGOING_TYPE:                                                                        
    		            type = "����";                                                                                 
    		            break;                                                                                       
    		        case Calls.MISSED_TYPE:                                                                          
    		            type = "δ��";                                                                                 
    		            break;                                                                                       
    		        default:                                                                                         
    		            type = "�Ҷ�";                                      
    		            break; 
    		        }                                                         
    		        String duration = cursor.getString(cursor.getColumnIndexOrThrow(Calls.DURATION));                
    		     
		           if(duration.equals("0")&&type.equals("����")){
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
		    intent.putExtra("readpath", "/sdcard/ECG/QRS.txt");
		    intent.setClass(ContactActivity.this, ECGShow.class);
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
			 Toast.makeText(ContactActivity.this, "û����Ч���룬������������д", Toast.LENGTH_SHORT).show();

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
