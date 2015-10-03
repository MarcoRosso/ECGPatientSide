package com.marco.a_patientside;










import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.bmob.BTPFileResponse;
import com.bmob.BmobProFile;
import com.bmob.btp.callback.UploadListener;
import com.marco.constant.Filename;
import com.marco.constant.PatientIn;
import com.marco.constant.PatientUser;
import com.marco.constant.RealTimeData;
import com.marco.dataprocess.BluetoothChatService;
import com.marco.dataprocess.FilterProcess;
import com.marco.dataprocess.QRSProcess;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OnLineRead extends Activity{
	private TextView mTitle;
	private TextView ecgshow;
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;
	private Button button_search;
	private Timer mTimer;
	private MyTimerTask mTimerTask;
    PowerManager.WakeLock mWakeLock;
	private SurfaceView sfv;
    private SurfaceHolder sfh;
    private int frenumber=0;
    private int smallfrenumber=0;
    private float frecount=0;
    private float smallfrecount=0;
    private int fre=250;
    private int readamount=0;
    private int totalamount=0;
    private int calamount=0;
    private int calchange=0;
    private float linepos=0;
	private int writeamount=0;
	private int drawamount=0;
    private int  centerY,hline=0,hline2=0,smallhline=0,smallhline2=0,volnumber=0,smallvolnumber=0,oldY,Y_axis[],y=0;
    private float vline=0;
    private float smallvline=0;
    private float onceplus=0;
    private float drawoldx=0;
    private float drawnextx=0;
    private int maxhr;
    private int minhr;
    private int onlineway=0;
    private double readdata[];
    private double ecgcal[];
    private double buffer[];
    private boolean creatfile=false;
    private boolean begincal=false;
    private boolean calenough=false;
    private boolean readenough=false;
    private boolean timerrun=false;
    private Boolean hralertswitchcondition;
    PatientUser user= new PatientUser();
    private String filename;
    private String username;
    private String onlinesend;
    SharedPreferences preferences;
	private String latitude="���ڻ�ȡ.....";
	private String longitude="���ڻ�ȡ.....";
	private String address="�����ݻ�WiFi���ӻ�������ȡ";
	private LocationClient locationClient = null;
	private static final int UPDATE_TIME = 5000;
	private static int LOCATION_COUTNS = 0;
    
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_READDATA = 6;
	public static final int MESSAGE_WRITEDATA = 7;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int buffersecond=5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	Handler mHandler2= new Handler(){
    	public void handleMessage(Message msg){
            if(msg.what==0x112){
    	    	ecgshow.setText(msg.getData().getString("HR","XXX"));
    	    	int hr=Integer.parseInt(msg.getData().getString("HR","0"));
    	    	if(hralertswitchcondition&&onlineway==0){
        	    	if(!(hr==0)){
        	    		if(hr<minhr)
        	    			alert(0);
        	    		else if(hr>maxhr)
        	    			alert(1);
        	    	}
    	    	}
    	    }
    	}
    };
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offlineread_layout);
        getActionBar().hide();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        Intent intent=getIntent();
        username=intent.getStringExtra("username");
		BmobQuery<PatientUser> query = new BmobQuery<PatientUser>();
    	query.addWhereEqualTo("username", username);
    	query.findObjects(this, new FindListener<PatientUser>() {
    	    @Override
    	    public void onError(int code, String msg) {
    	        // TODO Auto-generated method stub
    	    	Toast.makeText(OnLineRead.this, R.string.usergetfailed+code+msg, Toast.LENGTH_SHORT).show();
    	    	finish();
    	    }
			@Override
			public void onSuccess(List<PatientUser> arg0) {
				// TODO Auto-generated method stub
                for(PatientUser patientuser:arg0){
                	String objectid=patientuser.getObjectId();
                user.setObjectId(objectid);
			}}
    	});	
        ecgcal=new double[fre*12];
        buffer=new double[fre*10];
        double temp=fre;
        double temp1=0.2/(1/temp);
        frecount=(int)(temp1);

        preferences = getSharedPreferences("setting", MODE_PRIVATE);
		maxhr=Integer.parseInt(preferences.getString("hralertmax", "150"));
		minhr=Integer.parseInt(preferences.getString("hralertmin", "45"));
		hralertswitchcondition=preferences.getBoolean("hralertswitch", true);
		
		locationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);        //�Ƿ��GPS
        option.setCoorType("gcj02");       //���÷���ֵ���������͡�
        option.setLocationMode(LocationMode.Battery_Saving);  //���ö�λ���ȼ�
        option.setProdName("LocationDemo"); //���ò�Ʒ�����ơ�ǿ�ҽ�����ʹ���Զ���Ĳ�Ʒ�����ƣ����������Ժ�Ϊ���ṩ����Ч׼ȷ�Ķ�λ����
        option.setScanSpan(UPDATE_TIME);    //���ö�ʱ��λ��ʱ��������λ����
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        
        sfv = (SurfaceView)findViewById(R.id.SurfaceView01);       
        sfh = sfv.getHolder();
        sfh.addCallback(new Callback()  {

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// TODO Auto-generated method stub
		        DisplayMetrics metric = new DisplayMetrics(); 
		        getWindowManager().getDefaultDisplay().getMetrics(metric); 
		        float xcm = (float) (metric.xdpi / 2.54);
		        double temp=sfv.getWidth()/xcm/0.5;
		        frenumber=(int)temp;
		        double temp2=sfv.getWidth()/xcm/0.1;
		        smallfrenumber=(int)temp2;
		        frecount=(float) (xcm*0.5);
		        smallfrecount=(float)(xcm*0.1);
		        volnumber=sfv.getHeight()/50;
		        smallvolnumber=sfv.getHeight()/10;
		        centerY = sfv.getHeight()/2; 
			    hline=centerY;
			    hline2=centerY;
			    smallhline=centerY;
			    smallhline2=centerY;
		        DrawGrid();
		        double temp3=sfv.getWidth()/xcm/2.5*fre;
		        totalamount=(int)temp3;
		        System.out.println("temp3 "+temp3);
		        System.out.println("totalamount"+totalamount);
		        readdata=new double[totalamount];
		        double temp4=sfv.getWidth()/temp3;
		        onceplus=(float)temp4;
				mWakeLock.acquire(); 
			}
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {				
			}
			public void surfaceDestroyed(SurfaceHolder holder) {
			}        	
        });
        if(hralertswitchcondition&onlineway==0){
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
                    sb.append("\n���λ�ø��´�����");
                    sb.append(String.valueOf(LOCATION_COUTNS));
                }                    
            });
               locationClient.start(); 
               locationClient.requestLocation();
        }

           
		// Get local Bluetooth adapter
        ecgshow=(TextView)findViewById(R.id.show);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "���������ã������˳�",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}
    class MyTimerTask extends TimerTask {
        @Override
         public void run() {  	
               Double temp2[]=new Double[50];
		       for(int i=0;i<50;i++){
			          temp2[i]=buffer[i];
		       }
			    if (50+drawamount>sfv.getWidth()){
			    	for(int i=drawamount;i<sfv.getWidth();i++){
						readdata[i]=temp2[i-drawamount];
						linepos=i;
						    
			    	}
			    	int alreadyread=sfv.getWidth()-drawamount;
			    	int leftread=50+drawamount-sfv.getWidth();
					for(int i=0;i<leftread;i++){
							  readdata[i]=temp2[alreadyread+i];
							  linepos=i;
						 }
					drawamount=leftread;
			    }else{
					for(int i=drawamount;i<50+drawamount;i++){
							  readdata[i]=temp2[i-drawamount];
							  linepos=i;
						 }
					drawamount=50+drawamount;
			    }
			    
				Y_axis=new int[readdata.length];
		        for(int i=0;i<readdata.length;i++){
		        	Y_axis[i]=(int)(readdata[i]*100);
		        	Y_axis[i] = centerY-Y_axis[i];
		        }
		    	y=0;
		        oldY = centerY;
		        vline=0;
			    hline=centerY;
			    hline2=centerY;
		        SimpleDraw(Y_axis.length-1);	
		  for(int i=50;i<fre*5;i++){
		       double trans=buffer[i];
		       buffer[i-50]=trans;
			}    
         }
      }
	protected void onStart() {
		super.onStart();
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}
	public synchronized void onResume() {
		super.onResume();
		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}
    public void alert (int typein){
     	Intent intent  = new Intent();
     	intent.putExtra("type", typein);
     	intent.putExtra("latitude", latitude);
     	intent.putExtra("longitude", longitude);
     	intent.putExtra("address", address);
     	intent.setClass(OnLineRead.this,AlertDialog.class);
   	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
     	startActivity(intent);
   }

	  void DrawGrid(){
	        Canvas canvas = sfh.lockCanvas(new Rect(0, 0, getWindowManager().getDefaultDisplay().getWidth(),
	                getWindowManager().getDefaultDisplay().getHeight()));
	        canvas.drawColor(Color.BLACK);
	        Paint mPaint = new Paint();
	        mPaint.setColor(Color.GRAY);
	        mPaint.setStrokeWidth(2);
	        for(int j=0;j<=frenumber;j++){
	        	canvas.drawLine(vline, 0, vline, sfv.getHeight(), mPaint);
	        	vline=vline+frecount;
	        }
	        for(int k=0;k<=volnumber/2;k++){
	        	canvas.drawLine(0, hline, sfv.getWidth(),hline, mPaint);
	        	hline=hline+50;
	        }
	        for(int k=0;k<=volnumber/2;k++){
	        	canvas.drawLine(0, hline2, sfv.getWidth(),hline2, mPaint);
	        	hline2=hline2-50;
	        }
	        mPaint.setStrokeWidth(1);
	        for(int j=0;j<=smallfrenumber;j++){
	        	canvas.drawLine(smallvline, 0, smallvline, sfv.getHeight(), mPaint);
	        	smallvline=smallvline+smallfrecount;
	        }
	        for(int k=0;k<=smallvolnumber/2;k++){
	        	canvas.drawLine(0, smallhline, sfv.getWidth(),smallhline, mPaint);
	        	smallhline=smallhline+10;
	        }
	        for(int l=0;l<=smallvolnumber/2;l++){
	        	canvas.drawLine(0, smallhline2, sfv.getWidth(),smallhline2, mPaint);
	        	smallhline2=smallhline2-10;
	        }
	        mPaint.setStrokeWidth(4);
	        canvas.drawLine(0, centerY, sfv.getWidth(), centerY, mPaint);
	        mPaint.setColor(Color.GREEN);
	        mPaint.setStrokeWidth(2);
	        double H = 8; // ��ͷ�߶�   
	        double L = 3.5; // �ױߵ�һ��   
	        int x3 = 0;
	        int y3 = 0;
	        int x4 = 0;
	        int y4 = 0;
	        float ex=frecount,sx=frecount;
	        int ey=sfv.getHeight()-85,sy=sfv.getHeight()-35;
	        canvas.drawText("0.5mV", frecount+2, sfv.getHeight()-86, mPaint);
	        double awrad = Math.atan(L / H); // ��ͷ�Ƕ�   
	        double arraow_len = Math.sqrt(L * L + H * H); // ��ͷ�ĳ���   
	        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
	        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
	        double x_3 = ex - arrXY_1[0]; // (x3,y3)�ǵ�һ�˵�   
	        double y_3 = ey - arrXY_1[1];
	        double x_4 = ex - arrXY_2[0]; // (x4,y4)�ǵڶ��˵�   
	        double y_4 = ey - arrXY_2[1];
	        Double X3 = new Double(x_3);
	        x3 = X3.intValue();
	        Double Y3 = new Double(y_3);
	        y3 = Y3.intValue();
	        Double X4 = new Double(x_4);
	        x4 = X4.intValue();
	        Double Y4 = new Double(y_4);
	        y4 = Y4.intValue();
	        canvas.drawLine(sx, sy, ex, ey,mPaint);
	        Path triangle = new Path();
	        triangle.moveTo(ex, ey);
	        triangle.lineTo(x3, y3);  
	        triangle.lineTo(x4, y4); 
	        triangle.close();
	        canvas.drawPath(triangle,mPaint);
	        x3 = 0;y3 = 0;x4 = 0;y4 = 0;
	        ex=frecount*2;sx=frecount;ey=sfv.getHeight()-36;sy=sfv.getHeight()-36;
	        canvas.drawText("0.2s", frecount*2, sfv.getHeight()-38, mPaint);
	        awrad = Math.atan(L / H); // ��ͷ�Ƕ�   
	        arraow_len = Math.sqrt(L * L + H * H); // ��ͷ�ĳ���   
	        arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
	        arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
	        x_3 = ex - arrXY_1[0]; // (x3,y3)�ǵ�һ�˵�   
	        y_3 = ey - arrXY_1[1];
	        x_4 = ex - arrXY_2[0]; // (x4,y4)�ǵڶ��˵�   
	        y_4 = ey - arrXY_2[1];
	        X3 = new Double(x_3);
	        x3 = X3.intValue();
	        Y3 = new Double(y_3);
	        y3 = Y3.intValue();
	        X4 = new Double(x_4);
	        x4 = X4.intValue();
	        Y4 = new Double(y_4);
	        y4 = Y4.intValue();
	        canvas.drawLine(sx, sy, ex, ey,mPaint);
	        Path triangle2 = new Path();
	        triangle2.moveTo(ex, ey);
	        triangle2.lineTo(x3, y3);  
	        triangle2.lineTo(x4, y4); 
	        triangle2.close();
	        canvas.drawPath(triangle2,mPaint);
	        sfh.unlockCanvasAndPost(canvas);
	    }
	  void SimpleDraw(int length) {
		  Canvas canvas = sfh.lockCanvas(new Rect(0, 0, getWindowManager().getDefaultDisplay().getWidth(),
	                getWindowManager().getDefaultDisplay().getHeight()));
	        canvas.drawColor(Color.BLACK);
	        Paint mPaint = new Paint();
	        mPaint.setColor(Color.GRAY);
	        mPaint.setStrokeWidth(2);
	        for(int j=0;j<=frenumber;j++){
	        	canvas.drawLine(vline, 0, vline, sfv.getHeight(), mPaint);
	        	vline=vline+frecount;
	        }
	        for(int k=0;k<=volnumber/2;k++){
	        	canvas.drawLine(0, hline, sfv.getWidth(),hline, mPaint);
	        	hline=hline+50;
	        }
	        for(int k=0;k<=volnumber/2;k++){
	        	canvas.drawLine(0, hline2, sfv.getWidth(),hline2, mPaint);
	        	hline2=hline2-50;
	        }
	        mPaint.setStrokeWidth(1);
	        for(int i=0;i<=smallfrenumber;i++){
	        	canvas.drawLine(smallvline, 0, smallvline, sfv.getHeight(), mPaint);
	        	smallvline=smallvline+smallfrecount;
	        }
	        for(int k=0;k<=smallvolnumber/2;k++){
	        	canvas.drawLine(0, smallhline, sfv.getWidth(),smallhline, mPaint);
	        	smallhline=smallhline+10;
	        }
	        for(int l=0;l<=smallvolnumber/2;l++){
	        	canvas.drawLine(0, smallhline2, sfv.getWidth(),smallhline2, mPaint);
	        	smallhline2=smallhline2-10;
	        }
	        mPaint.setStrokeWidth(4);
	        canvas.drawLine(0, centerY, sfv.getWidth(), centerY, mPaint);
	    	        mPaint.setColor(Color.GREEN);// ����Ϊ��ɫ
	    	        mPaint.setStrokeWidth(2);// ���û��ʴ�ϸ
	    	        canvas.drawLine(linepos, 0, linepos, sfv.getHeight(), mPaint);
	    	        int y;
	    	        for (int i = 0; i < length; i++) {// �滭
	    	        y = Y_axis[i];
	    	        canvas.drawLine(drawoldx, oldY, drawnextx, y, mPaint);
	    	        drawoldx=drawnextx;
	    	        drawnextx=drawoldx+onceplus;
	    	        oldY = y;
	    	        }
	    	  double H = 8; // ��ͷ�߶�   
	    	  double L = 3.5; // �ױߵ�һ��   
	    	  int x3 = 0;
	    	  int y3 = 0;
	    	  int x4 = 0;
	    	  int y4 = 0;
	    	  float ex=frecount,sx=frecount;
		       int ey=sfv.getHeight()-85,sy=sfv.getHeight()-35;
	    	  canvas.drawText("0.5mV", frecount+2, sfv.getHeight()-86, mPaint);
	    	  double awrad = Math.atan(L / H); // ��ͷ�Ƕ�   
	    	  double arraow_len = Math.sqrt(L * L + H * H); // ��ͷ�ĳ���   
	    	  double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
	    	  double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
	    	  double x_3 = ex - arrXY_1[0]; // (x3,y3)�ǵ�һ�˵�   
	    	  double y_3 = ey - arrXY_1[1];
	    	  double x_4 = ex - arrXY_2[0]; // (x4,y4)�ǵڶ��˵�   
	    	  double y_4 = ey - arrXY_2[1];
	    	  Double X3 = new Double(x_3);
	    	  x3 = X3.intValue();
	    	  Double Y3 = new Double(y_3);
	    	  y3 = Y3.intValue();
	    	  Double X4 = new Double(x_4);
	    	  x4 = X4.intValue();
	    	  Double Y4 = new Double(y_4);
	    	  y4 = Y4.intValue();
	    	  canvas.drawLine(sx, sy, ex, ey,mPaint);
	    	  Path triangle = new Path();
	    	  triangle.moveTo(ex, ey);
	    	  triangle.lineTo(x3, y3);  
	    	  triangle.lineTo(x4, y4); 
	    	  triangle.close();
	    	  canvas.drawPath(triangle,mPaint);
	    	  x3 = 0;y3 = 0;x4 = 0;y4 = 0;
	    	  ex=frecount*2;sx=frecount;ey=sfv.getHeight()-36;sy=sfv.getHeight()-36;
	    	  canvas.drawText("0.2s", frecount*2, sfv.getHeight()-38, mPaint);
	    	  awrad = Math.atan(L / H); // ��ͷ�Ƕ�   
	    	  arraow_len = Math.sqrt(L * L + H * H); // ��ͷ�ĳ���   
	    	  arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
	    	  arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
	    	  x_3 = ex - arrXY_1[0]; // (x3,y3)�ǵ�һ�˵�   
	    	  y_3 = ey - arrXY_1[1];
	    	  x_4 = ex - arrXY_2[0]; // (x4,y4)�ǵڶ��˵�   
	    	  y_4 = ey - arrXY_2[1];
	    	  X3 = new Double(x_3);
	    	  x3 = X3.intValue();
	    	  Y3 = new Double(y_3);
	    	  y3 = Y3.intValue();
	    	  X4 = new Double(x_4);
	    	  x4 = X4.intValue();
	    	  Y4 = new Double(y_4);
	    	  y4 = Y4.intValue();
	    	  canvas.drawLine(sx, sy, ex, ey,mPaint);
	    	  Path triangle2 = new Path();
	    	  triangle2.moveTo(ex, ey);
	    	  triangle2.lineTo(x3, y3);  
	    	  triangle2.lineTo(x4, y4); 
	    	  triangle2.close();
	    	  canvas.drawPath(triangle2,mPaint);
	       sfh.unlockCanvasAndPost(canvas);// �����������ύ���õ�ͼ��
	  }
      void ClearDraw() {
      Canvas canvas = sfh.lockCanvas(null);
      canvas.drawColor(Color.BLACK);// �������
      sfh.unlockCanvasAndPost(canvas);
      }
	private void setupChat() {

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		mOutEditText =(EditText)findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();
				sendMessage(message);
							
			}
		});


		button_search = (Button) findViewById(R.id.button3);
		button_search.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final String[] onlineways={"ÿ�����Զ������ĵ��ļ����ϴ�","ʵʱ�����ĵ�����"};
	            Dialog alertDialog = new android.app.AlertDialog.Builder(OnLineRead.this). 
                	    setTitle("��ѡ�����߷�ʽ"). 
                	    setIcon(R.drawable.ic_launcher) 
                	    .setItems(onlineways, new DialogInterface.OnClickListener() { 
                	     @Override 
                	     public void onClick(DialogInterface dialog, int which) {
                	    	onlineway=which;
                	    	if(onlineway==1){
               				 BmobQuery<PatientIn> query = new BmobQuery<PatientIn>();
            				 query.addWhereEqualTo("PatientName", username);
            				 query.findObjects(OnLineRead.this, new FindListener<PatientIn>() {
            					@Override
            					public void onError(int arg0, String arg1) {
            						// TODO Auto-generated method stub
            						Toast.makeText(OnLineRead.this, "��ȡ�û���Ϣ����"+arg0+arg1, Toast.LENGTH_SHORT).show();	
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
            						p2.setOnline(1);
            						p2.update(OnLineRead.this, id, new UpdateListener() {
            						    @Override
            						    public void onSuccess() {
            						        // TODO Auto-generated method stub
            						    	Toast.makeText(OnLineRead.this, "ѡ��ɹ�", Toast.LENGTH_SHORT).show();
            	             				Intent serverIntent = new Intent(OnLineRead.this,
            	            						DeviceListActivity.class);
            	            				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            						    }
            						    @Override
            						    public void onFailure(int code, String msg) {
            						        // TODO Auto-generated method stub
            						    	Toast.makeText(OnLineRead.this, "ѡ��ʧ��"+code+msg, Toast.LENGTH_SHORT).show();	
            						    }
            						});
            					}            					 
            				 });
                	    	}else{
                	    	 BmobQuery<PatientIn> query = new BmobQuery<PatientIn>();
               				 query.addWhereEqualTo("PatientName", username);
               				 query.findObjects(OnLineRead.this, new FindListener<PatientIn>() {
               					@Override
               					public void onError(int arg0, String arg1) {
               						// TODO Auto-generated method stub
               						Toast.makeText(OnLineRead.this, "��ȡ�û���Ϣ����"+arg0+arg1, Toast.LENGTH_SHORT).show();	
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
               						p2.update(OnLineRead.this, id, new UpdateListener() {
               						    @Override
               						    public void onSuccess() {
               						        // TODO Auto-generated method stub
               						    	Toast.makeText(OnLineRead.this, "ѡ��ɹ�", Toast.LENGTH_SHORT).show();
               	             				Intent serverIntent = new Intent(OnLineRead.this,
               	            						DeviceListActivity.class);
               	            				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
               						    }
               						    @Override
               						    public void onFailure(int code, String msg) {
               						        // TODO Auto-generated method stub
               						    	Toast.makeText(OnLineRead.this, "ѡ��ʧ��"+code+msg, Toast.LENGTH_SHORT).show();	
               						    }
               						});
               					}             					 
               				 });
                	    	}
                	       } 
                	    }). 
                	    setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
                	     @Override 
                	     public void onClick(DialogInterface dialog, int which) { 
                	      // TODO Auto-generated method stub
                	    	dialog.dismiss();
                	     } 
                	    }). 
                	    create(); 
                alertDialog.show();


			}
		});
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this,"δ���ӵ��κ��豸", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			mOutEditText.setText(mOutStringBuffer);
		}
	}
	
	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);
			}
			return true;
		}
	};
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText("������");
					mTitle.append(mConnectedDeviceName);
					mConversationArrayAdapter.clear();
					if(timerrun){
						mTimer.cancel();
						mTimerTask.cancel();
						timerrun=false;
					}
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText("��������....");
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText("δ����");
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				if(!creatfile){
					filename=gettime();
					startfile(Integer.toString(fre),filename);
					creatfile=true;
				}
				String readMessage="";
				for(int i=0;i<msg.arg1;i++){
			            String hex = Integer.toHexString(readBuf[i] & 0xFF);
			            if (hex.length() == 1)
			            {
			                hex = '0' + hex;
			            }
						readMessage=readMessage+"a"+hex.toUpperCase();
				}
				mConversationArrayAdapter.add(readMessage);
				String[] temp1=readMessage.split("a");
				readMessage="";
				int readBuf2[]=new int[temp1.length-1];
				double temp2[]=new double[temp1.length-1];
				for(int i=1;i<temp1.length;i++){
		            readBuf2[i-1]=Integer.parseInt(temp1[i], 16);
		            temp2[i-1]=readBuf2[i-1]*3.3/256*1.61-1.95;
		            if(onlineway==1){
		            	DecimalFormat df = new DecimalFormat( "######.000000");  
						onlinesend=onlinesend+df.format(temp2[i-1])+"\n";
		            }
					if(writeamount==fre*60){
						if(onlineway==0){
							uploadfile();
						}
						filename=gettime();
						startfile(Integer.toString(fre),filename);
						creatfile=true;
						writeamount=0;
					}
					writefile(filename,temp2[i-1]+"\n");
					writeamount=writeamount+1;
			    }

				/*String readMessage = new String(readBuf, 0, msg.arg1);
				mConversationArrayAdapter.add(readMessage);
				String[] temp1=readMessage.split("\n");
				double temp2[]=new double[temp1.length];
				for(int i=0;i<temp1.length;i++){
		            temp2[i]=Double.parseDouble(temp1[i]);
		             if(onlineway==1){
						onlinesend=onlinesend+temp2[i]+"\n";
		             }
					if(writeamount==fre*60){
						if(onlineway==0){
							uploadfile();
						}
						filename=gettime();
						startfile(Integer.toString(fre),filename);
						creatfile=true;
						writeamount=0;
					}
					writefile(filename,temp2[i]+"\n");
					writeamount=writeamount+1;
			    }*/
				
				if(onlineway==1){
					sendMsgbywifi(username,onlinesend);
					onlinesend="";
				}
				 int readamountonce=temp2.length;
				if(calamount+readamountonce<fre*12)
			    	calenough=false;
			    else
			    	calenough=true;
			    if(!calenough){
				       for(int i=calamount;i<readamountonce+calamount;i++){
					          ecgcal[i]=temp2[i-calamount];
				       } 
				   calamount=calamount+readamountonce;
				}else{
					  if(begincal){
						  int readtemp=0;
					      for(int i=readamountonce;i<fre*12;i++){
							   double trans=ecgcal[i];
							   ecgcal[i-readamountonce]=trans;
							   }
					      for(int i=fre*12-readamountonce;i<fre*12;i++){
								   ecgcal[i]=temp2[readtemp]; 
								   readtemp++;
							   }
					      calchange=calchange+readamountonce;
					      if(calchange>=fre*2){
						      HRCal();
						      calchange=0;
					      }
					  }else{
						  int readtemp=0;
					      for(int i=readamountonce;i<fre*12;i++){
							   double trans=ecgcal[i];
							   ecgcal[i-readamountonce]=trans;
							   }
					      for(int i=fre*12-readamountonce;i<fre*12;i++){
								   ecgcal[i]=temp2[readtemp];
								   readtemp++;
							   }
					      HRCal();
					      begincal=true;
					  }			      
				}
			    
				 if (readamountonce+readamount>totalamount){
				    	for(int i=readamount;i<totalamount;i++){
									  readdata[i]=temp2[i-readamount];
									  linepos=i*onceplus;
				    	}
				    	int alreadyread=totalamount-readamount;
				    	int leftread=readamountonce+readamount-totalamount;
						for(int i=0;i<leftread;i++){
								  readdata[i]=temp2[alreadyread+i];
								  linepos=i*onceplus;
							 }
				    	readamount=leftread;
				    }else{
						for(int i=readamount;i<readamountonce+readamount;i++){
								  readdata[i]=temp2[i-readamount];
								  linepos=i*onceplus;
							 }
						readamount=readamountonce+readamount;
				    }
				    
					Y_axis=new int[readdata.length];
			        for(int i=0;i<readdata.length;i++){
			        	Y_axis[i]=(int)(readdata[i]*100);
			        	Y_axis[i] = centerY-Y_axis[i];
			        }
			        drawoldx=0;
			        drawnextx=0;
			    	y=0;
			        oldY = centerY;
			        vline=0;
			        smallvline=0;
				    hline=centerY;
				    hline2=centerY;
				    smallhline=centerY;
				    smallhline2=centerY;
			        SimpleDraw(Y_axis.length);						        
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"���ӵ��� " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
	private void sendMsgbywifi(String name, final String msg){
		RealTimeData chat = new RealTimeData(name, msg);
		chat.save(this, new SaveListener() {		
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
            //Toast.makeText(OnLineRead.this, "���ͳɹ���", Toast.LENGTH_SHORT).show();
            //mConversationArrayAdapter.add(msg);
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
	            Toast.makeText(OnLineRead.this, "����ʧ�ܣ�"+arg0+arg1, Toast.LENGTH_SHORT).show();	
			}
		});
		
	}
	private void uploadfile(){
		String uploadpath="/sdcard/ECG/"+filename+".txt";
		BTPFileResponse response = BmobProFile.getInstance(OnLineRead.this).upload(uploadpath, 
				new UploadListener() {

            @Override
            public void onSuccess(String fileName,String url) {
                // TODO Auto-generated method stub
    			Toast.makeText(OnLineRead.this, R.string.fileuploadsuccess, Toast.LENGTH_SHORT).show();
    			saveFileNameInfo(fileName);
            }

            @Override
            public void onProgress(int ratio) {
                // TODO Auto-generated method stub

            }
            @Override
            public void onError(int statuscode, String errormsg) {
                // TODO Auto-generated method stub
            	Toast.makeText(OnLineRead.this, R.string.fileuploadfailed+statuscode+errormsg, Toast.LENGTH_SHORT).show();
            }
        });
	}
	private void saveFileNameInfo(String name){
		String realname=filename+".txt";
	    if(TextUtils.isEmpty(user.getObjectId())){
	    	Toast.makeText(OnLineRead.this, R.string.usernameempty, Toast.LENGTH_SHORT).show();
	        return;
	    }
	    final Filename filename = new Filename();
	    filename.setFilename(name);        // ������������ 
	    filename.setUser(user);
        filename.setRealname(realname);// �������п�����
	    filename.save(this, new SaveListener() {
	        @Override
	        public void onSuccess() {
	            // TODO Auto-generated method stub
		    	Toast.makeText(OnLineRead.this, R.string.fileinfosuccess, Toast.LENGTH_SHORT).show();
	            addFileToUser(filename);
	        }

	        @Override
	        public void onFailure(int arg0, String arg1) {
	            // TODO Auto-generated method stub
		    	Toast.makeText(OnLineRead.this, R.string.fileinfofailed+arg0+arg1, Toast.LENGTH_SHORT).show();
	        }
	    });
	}
	private void addFileToUser(Filename filename){
	    if(TextUtils.isEmpty(user.getObjectId()) || 
	            TextUtils.isEmpty(filename.getObjectId())){
	    	Toast.makeText(OnLineRead.this, R.string.usernameempty, Toast.LENGTH_SHORT).show();
	        return;
	    }

	    BmobRelation filenames = new BmobRelation();
	    filenames.add(filename);
	    user.setInUse(true);
	    user.setFilename(filenames);
	    user.update(this, new UpdateListener() {

	        @Override
	        public void onSuccess() {
	            // TODO Auto-generated method stub
	        	Toast.makeText(OnLineRead.this, R.string.attachusersuccess, Toast.LENGTH_SHORT).show();
	        }

	        @Override
	        public void onFailure(int arg0, String arg1) {
	            // TODO Auto-generated method stub
	        	Toast.makeText(OnLineRead.this, R.string.attachuserfailed+arg0+arg1, Toast.LENGTH_SHORT).show();
	        }
	    });
	}
	public String gettime()
	{
	  Date date=new Date();
	  DateFormat format=new SimpleDateFormat("yyyyMMdd-HHmmss");
	  String time=format.format(date);
	  return time;
	}
	 public void startfile(String s,String name)
    {
	  try 
	  {
	   FileOutputStream outStream = new FileOutputStream("/sdcard/ECG/"+name+".txt",true);
	   OutputStreamWriter writer = new OutputStreamWriter(outStream,"UTF-8");
	   writer.write(s);
	   writer.write("\n");
	   writer.flush();
	   writer.close();//�ǵùر�
	   outStream.close();
	  } 
	  catch (Exception e)
	  {
	   Toast.makeText(OnLineRead.this, "�ļ���������", Toast.LENGTH_SHORT).show();
	  } 
   }
	 public void writefile(String name,String content){
		 File targetFile=new File("/sdcard/ECG/"+name+".txt");
		 try {
			RandomAccessFile raf=new RandomAccessFile(targetFile,"rw");
			try {
				raf.seek(targetFile.length());
				raf.write(content.getBytes());
				raf.close();
			} catch (IOException e) {
				Toast.makeText(OnLineRead.this, "�ļ�д�����", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}			
		} catch (FileNotFoundException e) {
			Toast.makeText(OnLineRead.this, "�ļ�д�����", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		 
	 }
	public boolean isDouble(String str)
	{
		   try
		   {
		      Double.parseDouble(str);
		      return true;
		   }
		   catch(NumberFormatException ex){}
		   return false;
	}
    public double[] rotateVec(float f, int py, double ang, boolean isChLen, double newLen)
    {
        double mathstr[] = new double[2];
        // ʸ����ת��������������ֱ���x������y��������ת�ǡ��Ƿ�ı䳤�ȡ��³���   
        double vx = f * Math.cos(ang) - py * Math.sin(ang);
        double vy = f * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathstr[0] = vx;
            mathstr[1] = vy;
        }
        return mathstr;
    }
	public void HRCal(){
        new Thread(){ 
           public void run(){ 
            double Y[]=new double[ecgcal.length + 2];
       		FilterProcess.filtering(ecgcal.length, ecgcal, Y, fre);
       		int Rnum;
       		int[] RIndex = new int[ecgcal.length+1];
       		Rnum = QRSProcess.RPeekDetect(Y, ecgcal.length, fre, RIndex);
       		for (int i = 0; i < Rnum; i++)
       		{
       			RIndex[i] = RIndex[i + 1];
       		}
       		//System.out.println("Rnum"+Rnum);
       		int[] QIndex = new int[ecgcal.length+1];
       		int Qnum;
       		Qnum = QRSProcess.QPeekDetect(Y, ecgcal.length, fre, QIndex, Rnum, RIndex);
       		//System.out.println("Qnum"+Qnum);
       		int[] SIndex = new int[ecgcal.length+1];
       		int Snum;
       		Snum = QRSProcess.SPeekDetect(Y, ecgcal.length, fre, SIndex, Rnum, RIndex);
       		//System.out.println("Snum"+Snum);
       		int[] RPeek = new int[Rnum-1];
       		for (int i = 0; i < Rnum - 1; i++)//���ﲻ֪����ôҪ����������һ
       		{
       			RPeek[i] = RIndex[i + 1] - RIndex[i];
       		}
       		double RPeekAverage = QRSProcess.sumint(RPeek,Rnum - 1) / (Rnum - 1);
       		double TR = RPeekAverage / fre;
       		double HR = 60.0 / TR;
       		//System.out.println("HR:"+HR);
       		int x=Integer.parseInt(new java.text.DecimalFormat("0").format(HR));
       		Message msg = new Message();  
               msg.what = 0x112;  
               Bundle bundle = new Bundle();    
               bundle.putString("HR",Integer.toString(x));  //��Bundle�д������       
               msg.setData(bundle);//mes����Bundle��������   
               mHandler2.sendMessage(msg);   		        
           } 
       }.start(); 
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, "����δ����!�����˳�",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
    		if (mChatService != null)
    			mChatService.stop();
    		mWakeLock.release(); 
    		if (locationClient.isStarted())
    			locationClient.stop();
    		if(timerrun){
    			mTimer.cancel();
    			mTimerTask.cancel();
    		}
    		if (mChatService != null)
    			mChatService.stop();
    		mWakeLock.release(); 
    		if (locationClient.isStarted())
    			locationClient.stop();
    		if(timerrun){
    			mTimer.cancel();
    			mTimerTask.cancel();
    		}
    		finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
