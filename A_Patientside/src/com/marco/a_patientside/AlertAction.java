package com.marco.a_patientside;



import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class AlertAction extends Activity {
	Camera m_Camera;
	Camera.Parameters mParameters;
	private int[] bgcolor={
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),
			Color.rgb(255,0,0), 
			
			Color.rgb(0,0,0),
			
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),			
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),
			Color.rgb(255,0,0),
			
			Color.rgb(0,0,0),
			
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),
			Color.rgb(255,0,0),
			
			Color.rgb(0,0,0)
			
			};
	private int[] bgflashtime = new int[] {
			300,
			300,
			300,	// ...	S
			300,
			300,
			//
			900,
			//
			900,
			300,
			900,	// ---	O
			300,
			900,
			//
			900,
			//
			300,
			300,
			300,	// ...	S
			300,
			300,
			//
			2100
	};
	private TextView warmingtv;
	private Handler show_handler;
	private Runnable show_runnable;
	private boolean servicesetting;
	private boolean firsttime=true;
	private String contact1;
	private String contact2;
	private String contact3;
	private String msg;
	private int warmingcounter=-1;
	private long exitTime = 0;
	private TextView titletv;
	private MediaPlayer mMediaPlayer;
	SharedPreferences preferences;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.fun4);
		warmingtv= (TextView) findViewById(R.id.warmingtv);
		titletv= (TextView) findViewById(R.id.titletv);
		
        Intent intent = getIntent(); 
        int type = intent.getIntExtra("type", 2);
        String latitude = intent.getStringExtra("latitude");
        String longitude=intent.getStringExtra("longitude");
		String address=intent.getStringExtra("address");
		preferences = getSharedPreferences("setting", MODE_PRIVATE);

		contact1=preferences.getString("contact1", "");
		contact2=preferences.getString("contact2", "");
		contact3=preferences.getString("contact3", "");
		servicesetting = preferences.getBoolean("alerswitch",true);



		titletv.setText("报警功能正在工作！");
		switch(type){
		case 0: 
			    msg="我可能遇到火灾，请迅速联系我！以下是我的位置信息："+"\n经度："+latitude+"\n纬度："+longitude+"\n可能的地址："+address;
		case 1: 
			    msg="我可能急速下坠，请迅速联系我！以下是我的位置信息："+"\n经度："+latitude+"\n纬度："+longitude+"\n可能的地址："+address;
		case 2: 
			    msg="我可能遭到撞击，请迅速联系我！以下是我的位置信息："+"\n经度："+latitude+"\n纬度："+longitude+"\n可能的地址："+address;
		}

			int count=0;
			if(contact1!=null&&!contact1.equals("")) {
				count++;
				sendmessage(contact1,msg);
			}
			if(contact2!=null&&!contact2.equals("")) {
				count++;
				sendmessage(contact2,msg);
			}
			if(contact3!=null&&!contact3.equals("")) {
				count++;
				sendmessage(contact3,msg);
			}
			if(count==0)
		    Toast.makeText(AlertAction.this, "您未填写有效号码，报警短息无法发送！" , Toast.LENGTH_SHORT).show();
			else
			Toast.makeText(AlertAction.this, count+"条报警信息已发送!" , Toast.LENGTH_SHORT).show();
		
		
	        mMediaPlayer=new MediaPlayer();
	        mMediaPlayer=MediaPlayer.create(this, R.raw.alertbeep);
	        if (!mMediaPlayer.isPlaying())
	        {
	    	try {
				mMediaPlayer.prepare();
			} catch (IllegalStateException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	        mMediaPlayer.setLooping(true);
	        mMediaPlayer.start();
	        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	        int max = am.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
	        am.setStreamVolume(AudioManager.STREAM_MUSIC, max,      
	                AudioManager.FLAG_PLAY_SOUND); 
	        }
		
		

		m_Camera = Camera.open();
		mParameters = m_Camera.getParameters();
		warmingtv.setBackgroundColor(bgcolor[1]);
		setBrightness((float) 1.0);
		show_handler = new Handler();
		show_runnable = new Runnable() {
			public void run() {
				warmingcounter++;
				warmingtv.setBackgroundColor(bgcolor[warmingcounter%18]);				
				if(warmingcounter%2==0)
				{
					mParameters = m_Camera.getParameters();
					mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					m_Camera.setParameters(mParameters);
				}
				else
				{
					mParameters = m_Camera.getParameters(); 
					mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					m_Camera.setParameters(mParameters);
				}
				show_handler.postDelayed(this,bgflashtime[warmingcounter%18]);
			}
		};
		



	}
	

    public void onDestroy() 
	{
        super.onDestroy();

    	show_handler.removeCallbacks(show_runnable);
    	m_Camera.release();
        
  	     if (mMediaPlayer.isPlaying())
 		     mMediaPlayer.release();

    }
    protected void onResume() {
        super.onResume();
        if(firsttime)
        {
        	firsttime=false;
        	show_handler.postDelayed(show_runnable,50);
        }
        Intent intent= new Intent();
        intent.setClass(AlertAction.this, FallDownService.class);
    	stopService(intent);
	}
	public void setBrightness(float f){
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = f;   
		getWindow().setAttributes(lp);
    } 
    public void sendmessage(String contactnumber, String alertmsg){
		SmsManager smsManager = SmsManager.getDefault(); 
  		if (contactnumber==null||contactnumber.equals("")){
  			Toast.makeText(this, "联系人号码为空!" , Toast.LENGTH_SHORT).show();
  		}else{
  			String SENT_SMS_ACTION = "SENT_SMS_ACTION";  
  			Intent sentIntent = new Intent(SENT_SMS_ACTION);  
  			PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, 0);  
  			// register the Broadcast Receivers  
  			getApplicationContext().registerReceiver(new BroadcastReceiver() {  
  			    @Override  
  			    public void onReceive(Context _context, Intent _intent) {  
  			        switch (getResultCode()) {  
  			              case Activity.RESULT_OK:  
  			            	Log.i("====>", "Activity.RESULT_OK");
  			        break;  
  			          case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
  			        	Log.i("====>", "RESULT_ERROR_GENERIC_FAILURE");
  			        break;  
  			          case SmsManager.RESULT_ERROR_RADIO_OFF: 
  			        	Log.i("====>", "RESULT_ERROR_RADIO_OFF");
  			        break;  
  			          case SmsManager.RESULT_ERROR_NULL_PDU: 
  			        	Log.i("====>", "RESULT_ERROR_NULL_PDU");
  			        break;
  			          case SmsManager.RESULT_ERROR_NO_SERVICE:
  			        Log.i("====>", "RESULT_ERROR_NO_SERVICE");
  			        }  
  			    }  
  			}, new IntentFilter(SENT_SMS_ACTION));
          ArrayList<String> messageArray=smsManager.divideMessage(alertmsg);
  		  ArrayList<PendingIntent> sentIntents=new ArrayList<PendingIntent>();
  		  for (int i=0; i<messageArray.size();i++)
  			  sentIntents.add(sentPI);
  			 smsManager.sendMultipartTextMessage(contactnumber, null, messageArray, sentIntents, null);
  			 
  			ContentValues values = new ContentValues();  
  			values.put("date", System.currentTimeMillis());   
  			values.put("read", 0);  
  			values.put("type", 2);  
  			values.put("address", contactnumber);  
  			values.put("body", alertmsg);  
  			getContentResolver().insert(Uri.parse("content://sms"),values);
  		} }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
                Toast.makeText(getApplicationContext(), "再按一次退出报警", Toast.LENGTH_SHORT).show();                                
                exitTime = System.currentTimeMillis();   
            } else {
        		if(!isMyServiceRunning()&&servicesetting)
        	    	startService(new Intent(this, FallDownService.class));	
               finish();
            }
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.marco.FallDownService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}
