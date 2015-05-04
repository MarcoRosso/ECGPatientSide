package com.marco.a_patientside;










import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.marco.dataprocess.BluetoothChatService;
import com.marco.dataprocess.FilterProcess;
import com.marco.dataprocess.QRSProcess;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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

public class OffLineRead extends Activity{
	private TextView mTitle;
	private TextView ecgshow;
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;
	private Button button_search;
    PowerManager.WakeLock mWakeLock;
	private SurfaceView sfv;
    private SurfaceHolder sfh;
    private int frenumber=0;
    private int frecount=0;
    private int fre=250;
    private int readamount=0;
    private int calamount=0;
    private int calchange=0;
    private int linepos=0;
	private int writeamount=0;
    private int  centerY,vline=0,hline=0,hline2=0,volnumber=0,oldX,oldY,Y_axis[],y=0;
    private int maxhr;
    private int minhr;
    private double readdata[];
    private double ecgcal[];
    private boolean creatfile=false;
    private boolean begincal=true;
    private Boolean hralertswitchcondition;
    private String filename;
    SharedPreferences preferences;
	private String latitude="正在获取.....";
	private String longitude="正在获取.....";
	private String address="打开数据或WiFi连接互联网获取";
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
    	    	if(hralertswitchcondition){
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
        ecgcal=new double[fre*12];
        double temp=fre;
        double temp1=0.2/(1/temp);
        frecount=(int)(temp1);
        preferences = getSharedPreferences("setting", MODE_PRIVATE);
		maxhr=Integer.parseInt(preferences.getString("hralertmax", "150"));
		minhr=Integer.parseInt(preferences.getString("hralertmin", "45"));
		hralertswitchcondition=preferences.getBoolean("hralertswitch", true);
		
		locationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);        //是否打开GPS
        option.setCoorType("gcj02");       //设置返回值的坐标类型。
        option.setLocationMode(LocationMode.Battery_Saving);  //设置定位优先级
        option.setProdName("LocationDemo"); //设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
        option.setScanSpan(UPDATE_TIME);    //设置定时定位的时间间隔。单位毫秒
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        
        sfv = (SurfaceView)findViewById(R.id.SurfaceView01);       
        sfh = sfv.getHolder();
        sfh.addCallback(new Callback()  {

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// TODO Auto-generated method stub
		        frenumber=sfv.getWidth()/frecount;
		        volnumber=sfv.getHeight()/50;
		        centerY = sfv.getHeight()/2; 
			    hline=centerY;
			    hline2=centerY;
		        DrawGrid();
		        readdata=new double[sfv.getWidth()];
				mWakeLock.acquire(); 
			}
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {				
			}
			public void surfaceDestroyed(SurfaceHolder holder) {
			}        	
        });
        
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
           
		// Get local Bluetooth adapter
        ecgshow=(TextView)findViewById(R.id.show);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "蓝牙不可用！程序退出",
					Toast.LENGTH_LONG).show();
			finish();
			return;
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
     	intent.setClass(OffLineRead.this,AlertDialog.class);
   	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
     	startActivity(intent);
   }
	protected void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		mWakeLock.release(); 
	}
	  void DrawGrid(){
	        Canvas canvas = sfh.lockCanvas(new Rect(0, 0, getWindowManager().getDefaultDisplay().getWidth(),
	                getWindowManager().getDefaultDisplay().getHeight()));
	        canvas.drawColor(Color.BLACK);
	        Paint mPaint = new Paint();
	        mPaint.setColor(Color.GRAY);
	        mPaint.setStrokeWidth(1);
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
	        mPaint.setStrokeWidth(4);
	        canvas.drawLine(0, centerY, sfv.getWidth(), centerY, mPaint);
	        mPaint.setColor(Color.GREEN);
	        mPaint.setStrokeWidth(2);
	        double H = 8; // 箭头高度   
	        double L = 3.5; // 底边的一半   
	        int x3 = 0;
	        int y3 = 0;
	        int x4 = 0;
	        int y4 = 0;
	        int ex=frecount,sx=frecount,ey=sfv.getHeight()-85,sy=sfv.getHeight()-35;
	        canvas.drawText("0.5mV", frecount+2, sfv.getHeight()-86, mPaint);
	        double awrad = Math.atan(L / H); // 箭头角度   
	        double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度   
	        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
	        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
	        double x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点   
	        double y_3 = ey - arrXY_1[1];
	        double x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点   
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
	        awrad = Math.atan(L / H); // 箭头角度   
	        arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度   
	        arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
	        arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
	        x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点   
	        y_3 = ey - arrXY_1[1];
	        x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点   
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
	        mPaint.setStrokeWidth(1);
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
	        mPaint.setStrokeWidth(4);
	        canvas.drawLine(0, centerY, sfv.getWidth(), centerY, mPaint);
	    	 if (length == 0)
	    	        oldX = 0;
	    	        mPaint.setColor(Color.GREEN);// 画笔为绿色
	    	        mPaint.setStrokeWidth(2);// 设置画笔粗细
	    	        canvas.drawLine(linepos, 0, linepos, sfv.getHeight(), mPaint);
	    	        int y;
	    	        for (int i = oldX + 1; i < length; i++) {// 绘画
	    	        y = Y_axis[i-1];
	    	        canvas.drawLine(oldX, oldY, i, y, mPaint);
	    	        oldX = i;
	    	        oldY = y;
	    	        }
	    	  double H = 8; // 箭头高度   
	    	  double L = 3.5; // 底边的一半   
	    	  int x3 = 0;
	    	  int y3 = 0;
	    	  int x4 = 0;
	    	  int y4 = 0;
	    	  int ex=frecount,sx=frecount,ey=sfv.getHeight()-85,sy=sfv.getHeight()-35;
	    	  canvas.drawText("0.5mV", frecount+2, sfv.getHeight()-86, mPaint);
	    	  double awrad = Math.atan(L / H); // 箭头角度   
	    	  double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度   
	    	  double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
	    	  double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
	    	  double x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点   
	    	  double y_3 = ey - arrXY_1[1];
	    	  double x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点   
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
	    	  awrad = Math.atan(L / H); // 箭头角度   
	    	  arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度   
	    	  arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
	    	  arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
	    	  x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点   
	    	  y_3 = ey - arrXY_1[1];
	    	  x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点   
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
	       sfh.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
	  }
      void ClearDraw() {
      Canvas canvas = sfh.lockCanvas(null);
      canvas.drawColor(Color.BLACK);// 清除画布
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
				Intent serverIntent = new Intent(OffLineRead.this,
						DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

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
			Toast.makeText(this,"未连接到任何设备", Toast.LENGTH_SHORT)
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
					mTitle.setText("已连接");
					mTitle.append(mConnectedDeviceName);
					mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText("正在连接....");
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText("未连接");
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
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				mConversationArrayAdapter.add(readMessage);
				if(!creatfile){
					filename=gettime();
					startfile(Integer.toString(fre),filename);
					creatfile=true;
				}
				writefile(filename,readMessage);
				String[] temp1=readMessage.split("\n");
				int leftthing=temp1.length%10;
				int readamountonce=temp1.length-leftthing;				
				if(calamount<fre*12){
				   for(int i=calamount;i<readamountonce+calamount;i++){
					   if(isDouble(temp1[i-calamount]))
					   ecgcal[i]=Double.parseDouble(temp1[i-calamount]);
				   }
				   calamount=calamount+readamountonce;
				   writeamount=writeamount+readamountonce;
				}else{
					  int readtemp=0;
				      for(int i=readamountonce;i<fre*12;i++){
						   double trans=ecgcal[i];
						   ecgcal[i-readamountonce]=trans;
						   }
				      for(int i=fre*12-readamountonce;i<fre*12;i++){
				    	   if(isDouble(temp1[readtemp])){
							   ecgcal[i]=Double.parseDouble(temp1[readtemp]);
							   readtemp++;
				    	   }else 
                               readtemp++;
						   }
				      if(begincal) {
				    	  HRCal();
				    	  begincal=false;
				      }
				      calchange=calchange+readamountonce;
				      if(calchange>=fre*2){
					      HRCal();
					      calchange=0;
				      }
				      writeamount=writeamount+readamountonce;
				}
				
				if(writeamount==(60*fre)){
					creatfile=false;
					writeamount=0;
				}
			    if (readamountonce+readamount>sfv.getWidth()) 
			    	readamount=0;			    
			    	  //从左往右滚动式
			    	 /*int readtemp=0;
				      for(int i=readamountonce;i<sfv.getWidth();i++){
						   double trans=readdata[i];
						   readdata[i-readamountonce]=trans;
						   }
				      for(int i=sfv.getWidth()-readamountonce;i<sfv.getWidth();i++){
						   readdata[i]=Double.parseDouble(temp1[readtemp]);
						   readtemp++;
						   }*/
			    	  //从左往右扫描式
				for(int i=readamount;i<readamountonce+readamount;i++){
				   if(isDouble(temp1[i-readamount])){
					  readdata[i]=Double.parseDouble(temp1[i-readamount]);
					  linepos=i;
				       }
				 }
			    readamount=readamountonce+readamount;
						   
				Y_axis=new int[readdata.length];
		        for(int i=0;i<readdata.length;i++){
		        	Y_axis[i]=(int)(readdata[i]*100);
		        	Y_axis[i] = centerY-Y_axis[i];
		        }
		        oldX=0;
		    	y=0;
		        oldY = centerY;
		        vline=0;
			    hline=centerY;
			    hline2=centerY;
		        SimpleDraw(Y_axis.length-1);
			/*	try{  //处理板子上三位电阻值 
			for(int i=0;i<temp1.length;i++){
					String process1=temp1[i];
					char a=process1.charAt(0);
					char b=process1.charAt(1);
					char c=process1.charAt(2);
					char[] cha={a,b,c};
					String n = String.valueOf(cha);
					int process3=Integer.parseInt(n,16);
					System.out.println(process3);
				}
				}catch(Exception e){
					
				}*/
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"连接到： " + mConnectedDeviceName,
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
	   writer.close();//记得关闭
	   outStream.close();
	  } 
	  catch (Exception e)
	  {
	   Toast.makeText(OffLineRead.this, "文件创建错误", Toast.LENGTH_SHORT).show();
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
				Toast.makeText(OffLineRead.this, "文件写入错误", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}			
		} catch (FileNotFoundException e) {
			Toast.makeText(OffLineRead.this, "文件写入错误", Toast.LENGTH_SHORT).show();
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
    public double[] rotateVec(int px, int py, double ang, boolean isChLen, double newLen)
    {
        double mathstr[] = new double[2];
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度   
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
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
       		for (int i = 0; i < Rnum - 1; i++)//这里不知道怎么要减二本来减一
       		{
       			RPeek[i] = RIndex[i + 1] - RIndex[i];
       		}
       		double RPeekAverage = QRSProcess.sumint(RPeek,Rnum - 1) / (Rnum - 1);
       		double TR = RPeekAverage / 250.0;
       		double HR = 60.0 / TR;
       		//System.out.println("HR:"+HR);
       		int x=Integer.parseInt(new java.text.DecimalFormat("0").format(HR));
       		Message msg = new Message();  
               msg.what = 0x112;  
               Bundle bundle = new Bundle();    
               bundle.putString("HR",Integer.toString(x));  //往Bundle中存放数据       
               msg.setData(bundle);//mes利用Bundle传递数据   
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
				Toast.makeText(this, "蓝牙未开启!即将退出",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
}
