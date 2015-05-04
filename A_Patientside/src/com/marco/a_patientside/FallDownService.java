package com.marco.a_patientside;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class FallDownService extends Service {
	private SensorManager sensorManager;
	private NotificationManager manager;
	WakeLock mWakeLock = null;
	private float[] gravity={0,0,0};
	private float accvalues[]=new float[3];
	private int acccounter=0;
	private int type=2;
	private boolean accavoidshake=true;
	private boolean accable=true;
	private float maxacc=12.0f;
	private String latitude="正在获取.....";
	private String longitude="正在获取.....";
	private String address="打开数据或WiFi连接互联网获取";
	private static final float ALPHA = 0.8f;
	SharedPreferences preferences;
	private LocationClient locationClient = null;
	private static final int UPDATE_TIME = 5000;
	private static int LOCATION_COUTNS = 0;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public void onCreate(){
		super.onCreate();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        locationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);        //是否打开GPS
        option.setCoorType("gcj02");       //设置返回值的坐标类型。
        option.setLocationMode(LocationMode.Battery_Saving);  //设置定位优先级
        option.setProdName("LocationDemo"); //设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
        option.setScanSpan(UPDATE_TIME);    //设置定时定位的时间间隔。单位毫秒
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        
        preferences = getSharedPreferences("setting", MODE_PRIVATE);
        accable=preferences.getBoolean("alerswitch", true);
        
        Timer updateTimer2 = new Timer("updateaccelerator");
        updateTimer2.scheduleAtFixedRate(new TimerTask() {
          public void run() {  	  
        	updateaccelerator();
          }
        }, 0, 200);
        Sensor AcceleratorSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(accSensorEventListener, 
        		AcceleratorSensor,SensorManager.SENSOR_DELAY_NORMAL );
        locationClient.registerLocationListener(new BDLocationListener() {
            
            @Override
            public void onReceiveLocation(BDLocation location) {
                // TODO Auto-generated method stub
                if (location == null) {
                    return;
                }
                StringBuffer sb = new StringBuffer(256);
                sb.append("Time : ");
                sb.append(location.getTime());
                sb.append("\nError code : ");
                sb.append(location.getLocType());
                sb.append("\nLatitude : ");
                latitude=String.valueOf(location.getLatitude());
                sb.append(location.getLatitude());
                sb.append("\nLontitude : ");
                longitude=String.valueOf(location.getLongitude());
                sb.append(location.getLongitude());
                sb.append("\nRadius : ");
                sb.append(location.getRadius());
                if (location.getLocType() == BDLocation.TypeGpsLocation){
                    sb.append("\nSpeed : ");
                    sb.append(location.getSpeed());
                    sb.append("\nSatellite : ");
                    sb.append(location.getSatelliteNumber());
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                    sb.append("\nAddress : ");
                    address=location.getAddrStr();
                    sb.append(location.getAddrStr());
                }
                LOCATION_COUTNS ++;
                sb.append("\n检查位置更新次数：");
                sb.append(String.valueOf(LOCATION_COUTNS));

            }                    
        });
           locationClient.start(); 
           locationClient.requestLocation();
         
        
	}
	public int onStartCommand(Intent intent, int flags,int startId){
        super.onStartCommand(intent, flags, startId);
		 String ns = Context.NOTIFICATION_SERVICE;
        manager = (NotificationManager) getSystemService(ns);
        Notification notification = new Notification();
        notification.icon=R.drawable.ic_launcher;
        notification.tickerText=getText(R.string.app_name);
        notification.when=System.currentTimeMillis();
        CharSequence contentTitle = getText(R.string.app_name);
        CharSequence contentText =  "跌倒检测正在后台运行";
        Intent intent1 = new Intent(FallDownService.this, OtherFunction.class);
        PendingIntent contentIntent = PendingIntent.getActivity(FallDownService.this, 0, intent1, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        notification.setLatestEventInfo(FallDownService.this, contentTitle, contentText, contentIntent);
        notification.flags=Notification.FLAG_NO_CLEAR;
        manager.notify(0, notification);
        accavoidshake=true;
        acccounter=0;
        acquireWakeLock();
		return startId;
	 }

	   private final SensorEventListener accSensorEventListener =new SensorEventListener(){
			public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		   public void onSensorChanged(SensorEvent event) {   
			   accvalues =event.values.clone();		   
		   }	   
	   };
	   public void updateaccelerator(){
	       	 float[] values=accvalues;
	       	 values=highPass(values[0],values[1],values[2]);
	       	 double sumOfSquares=(values[0]*values[0])+(values[1]*values[1]
	       			 +values[2]*values[2]);
	       	 double acceleration = Math.sqrt(sumOfSquares);
	       	 DecimalFormat df = new DecimalFormat("########.0000");
	       	 acceleration = Double.parseDouble(df.format(acceleration));
	       	 if (acceleration>=maxacc&&!accavoidshake&&accable)
	       	 {
	       		 type=2;
	       		 alert(type);
	       	 }
	       	acccounter++;
	   	     if (acccounter>3)
	   	      accavoidshake=false;
	   }
	     public void alert (int typein){
	     	Intent intent  = new Intent();
	     	intent.putExtra("type", typein);
	     	intent.putExtra("latitude", latitude);
	     	intent.putExtra("longitude", longitude);
	     	intent.putExtra("address", address);
	     	intent.setClass(FallDownService.this,AlertDialog.class);
	   	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	     	startActivity(intent);
	   }
	   private float[] highPass(float x, float y, float z)
	   {
	       float[] filteredValues = new float[3];
	       
	       gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
	       gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
	       gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

	       filteredValues[0] = x - gravity[0];
	       filteredValues[1] = y - gravity[1];
	       filteredValues[2] = z - gravity[2];
	       
	       return filteredValues;
	   }
	   public void onDestroy() {
	         sensorManager.unregisterListener(accSensorEventListener);
	         manager.cancel(0);
	         releaseWakeLock();
	         if (locationClient != null && locationClient.isStarted()) {
	             locationClient.stop();
	             locationClient = null;}
	         super.onDestroy();
	     }
	   private void acquireWakeLock()
	   {
	      
		if (null == mWakeLock)
	       {
	           PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
	           mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE,"");
	           if (null != mWakeLock)
	           {
	               mWakeLock.acquire();
	           }
	       }
	    }
	   private void releaseWakeLock()
	   {
	       if (null != mWakeLock)
	       {
	           mWakeLock.release();
	           mWakeLock = null;
	       }
	   }

}
