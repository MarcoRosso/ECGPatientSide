package com.marco.a_patientside;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import com.marco.dataprocess.FilterProcess;
import com.marco.dataprocess.QRSProcess;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ECGShow extends Activity{
	private String filepath;
	private TextView draw;
	private TextView ecgshow;
	private TextView drawt;
	private TextView drawnumber;
	private SurfaceView sfv;
    private SurfaceHolder sfh;
    private Timer mTimer;
    private Timer HRTimer;
    private MyTimerTask mTimerTask;
    private HRTimerTask mHRTimerTask;
    private int screencount=0;
    private int frecount=0;
    private int fre=0;
    private int frenumber=0;
    private int volnumber=0;
    private int linecount;
    private int drawcount;
    private int drewcount=1;
    private int calculate=0;
    private double drawtime;
    double[] data;
    float hnumber1=0.0f,hnumber2=0.0f,vnumber=0.0f;
    int Y_axis[];
    int  centerY,//中心线
    oldX=0,oldY=0,//上一个XY 点
    currentX,i=1,y=0,
    vline=0,hline=0,hline2=0;//当前绘制到的X 轴上的点
    PowerManager.WakeLock mWakeLock;
    Handler mHandler= new Handler(){
    	public void handleMessage(Message msg){
    		if(msg.what==0x111)
    	    {
    	    if(drewcount==drawcount){
    	    Toast.makeText(getApplicationContext(), "文件读取完毕", Toast.LENGTH_SHORT).show();
            draw.setEnabled(true);
            draw.setText("点此开始绘图");
            drawt.setText("点此开始绘图");
            HRTimer.cancel();
            mHRTimerTask.cancel();
            mWakeLock.release();
    	    }else{
    	    	 String read=ReadTxtFile(filepath,
    	    			 getWindowManager().getDefaultDisplay().getWidth()*drewcount,
    	    			 getWindowManager().getDefaultDisplay().getWidth()*(drewcount+1));
    	         String tempdata[]=read.split("\n");
    	         data = new double[tempdata.length];
    	         Y_axis= new int[tempdata.length]; 
    	         for(int i=0;i<tempdata.length;i++){
    	         	data[i]=Float.parseFloat(tempdata[i]);
    	         	Y_axis[i]=(int)(data[i]*100);
    	         	Y_axis[i] = centerY-Y_axis[i];   	            
    	         }
    	         vline=0;
 		    	hline=centerY;
 		    	hline2=centerY;
 		    	oldX=0;
 		    	i=1;
 		    	y=0;
 		        oldY = centerY;
 		        currentX = 0;
 		        screencount=0;
		    	vnumber=0.0f;
 		    	hnumber1=0.0f;
 		    	hnumber2=0.0f;
 		    	calculate=0;
 		    	DrawGrid();
 		      mTimer = new Timer();
 		      mTimerTask = new MyTimerTask();
 		      mTimer.schedule(mTimerTask, 0, 1);
 	    	    drewcount++;
 	            drawnumber.setText("共"+Integer.toString(drawcount)+"屏，现在是第"+
 	                    Integer.toString(drewcount)+"屏");
    	    } 
    	    }else if(msg.what==0x112){
    	    	ecgshow.setText(msg.getData().getString("HR","XXX"));
    	    }
    	}
    };
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecgfileread_layout);
        getActionBar().hide();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag"); 
        draw=(TextView)findViewById(R.id.drawgraph);
        ecgshow=(TextView)findViewById(R.id.show);
        drawt=(TextView)findViewById(R.id.drawgrapht);
        drawnumber=(TextView)findViewById(R.id.drawnumber);
        sfv = (SurfaceView)findViewById(R.id.SurfaceView01);
        
        sfh = sfv.getHolder();
        centerY = (getWindowManager().getDefaultDisplay().getHeight()-sfv.getTop()) / 2;

        Intent intent=getIntent();
        filepath=intent.getStringExtra("readpath");
        linecount=ReadTxtFileline(filepath);
        System.out.println("linecount"+linecount);
        drawcount=linecount/getWindowManager().getDefaultDisplay().getWidth()+1;
        drawnumber.setText("共"+Integer.toString(drawcount)+"屏，现在是第"+
                                   Integer.toString(drewcount)+"屏");
        String readfile=ReadTxtFile(filepath,0,0);
        String tempfre[]=readfile.split("\n");
        fre=Integer.parseInt(tempfre[0]);
        drawtime=(double)fre/250*4400;
        System.out.println(drawtime);
        double temp=fre;
        double temp1=0.2/(1/temp);
        frecount=(int)(temp1);
        frenumber=getWindowManager().getDefaultDisplay().getWidth()/frecount;
        volnumber=getWindowManager().getDefaultDisplay().getHeight()/50;
        draw.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
		    	ClearDraw();
		        String read=ReadTxtFile(filepath,1,getWindowManager().getDefaultDisplay().getWidth());
		        String tempdata[]=read.split("\n");
		        data = new double[tempdata.length];
		        Y_axis= new int[tempdata.length]; 
		        for(int i=0;i<tempdata.length;i++){
		        	data[i]=Double.parseDouble(tempdata[i]);
		        	Y_axis[i]=(int)(data[i]*100);
		        	Y_axis[i] = centerY-Y_axis[i];
		           
		        }
		    	vline=0;
		    	hline=centerY;
		    	hline2=centerY;
		    	oldX=0;
		    	i=1;
		    	y=0;
		        oldY = centerY;
		        currentX = 0;
		        screencount=0;
		    	hnumber1=0.0f;
		    	hnumber2=0.0f;
		    	vnumber=0.0f;
		    	drewcount=1;
		        drawnumber.setText("共"+Integer.toString(drawcount)+"屏，现在是第"+
                        Integer.toString(drewcount)+"屏");
		        ecgshow.setText("XXX");
 		    	DrawGrid();
		       mTimer = new Timer();
		       mTimerTask = new MyTimerTask();
		       mTimer.schedule(mTimerTask, 0, 1);
	 		   HRTimer=new Timer();
	 		   mHRTimerTask=new HRTimerTask();
	 		   HRTimer.schedule(mHRTimerTask,(int)drawtime*12,(int)drawtime*2);
		       draw.setEnabled(false);
		       draw.setText("正在绘图.....");
		       drawt.setText("正在绘图.....");
			}      	
        });
	}
	protected void onDestory(){
		super.onDestroy();
		mTimer.cancel();
        mTimerTask.cancel();
        HRTimer.cancel();
        mHRTimerTask.cancel();
        mWakeLock.release(); 
	}
	class HRTimerTask extends TimerTask{
		public void run() {
        	double[] datacount=new double[fre*12];
        	if(calculate==0)
	        {String readdata=ReadTxtFile(filepath,1,fre*12);
	        String tempdatas[]=readdata.split("\n");
	        for(int i=0;i<tempdatas.length;i++){
	        	datacount[i]=Double.parseDouble(tempdatas[i]);	           
	        }
        	calculate=calculate+2;
        	System.out.println(calculate);
       		double Y[]=new double[datacount.length + 2];
    		FilterProcess.filtering(datacount.length, datacount, Y, fre);
    		int Rnum;
    		int[] RIndex = new int[datacount.length+1];
    		Rnum = QRSProcess.RPeekDetect(Y, datacount.length, fre, RIndex);
    		for (int i = 0; i < Rnum; i++)
    		{
    			RIndex[i] = RIndex[i + 1];
    		}
    		System.out.println("Rnum"+Rnum);
    		int[] QIndex = new int[datacount.length+1];
    		int Qnum;
    		Qnum = QRSProcess.QPeekDetect(Y, datacount.length, fre, QIndex, Rnum, RIndex);
    		System.out.println("Qnum"+Qnum);
    		int[] SIndex = new int[datacount.length+1];
    		int Snum;
    		Snum = QRSProcess.SPeekDetect(Y, datacount.length, fre, SIndex, Rnum, RIndex);
    		System.out.println("Snum"+Snum);
    		int[] RPeek = new int[Rnum-1];
    		for (int i = 0; i < Rnum - 1; i++)//这里不知道怎么要减二本来减一
    		{
    			RPeek[i] = RIndex[i + 1] - RIndex[i];
    		}
    		double RPeekAverage = QRSProcess.sumint(RPeek,Rnum - 1) / (Rnum - 1);
    		double TR = RPeekAverage / 250.0;
    		double HR = 60.0 / TR;
    		System.out.println("HR:"+HR);
    		int x=Integer.parseInt(new java.text.DecimalFormat("0").format(HR));
    		Message msg = new Message();  
            msg.what = 0x112;  
            Bundle bundle = new Bundle();    
            bundle.putString("HR",Integer.toString(x));  //往Bundle中存放数据       
            msg.setData(bundle);//mes利用Bundle传递数据   
            mHandler.sendMessage(msg);
    		}else{
	        	if(fre*(calculate+12)>linecount){
		            HRTimer.cancel();
		            mHRTimerTask.cancel();
		        }else{
		        	String readdata=ReadTxtFile(filepath,fre*calculate+1,
		        			fre*(12+calculate));
			        String tempdatas[]=readdata.split("\n");
			        for(int i=0;i<tempdatas.length;i++){
			        	datacount[i]=Double.parseDouble(tempdatas[i]);
		             }
		        	calculate=calculate+2;
		        	System.out.println(calculate);
		       		double Y[]=new double[datacount.length + 2];
		    		FilterProcess.filtering(datacount.length, datacount, Y, fre);
		    		int Rnum;
		    		int[] RIndex = new int[datacount.length+1];
		    		Rnum = QRSProcess.RPeekDetect(Y, datacount.length, fre, RIndex);
		    		for (int i = 0; i < Rnum; i++)
		    		{
		    			RIndex[i] = RIndex[i + 1];
		    		}
		    		System.out.println("Rnum"+Rnum);
		    		int[] QIndex = new int[datacount.length+1];
		    		int Qnum;
		    		Qnum = QRSProcess.QPeekDetect(Y, datacount.length, fre, QIndex, Rnum, RIndex);
		    		System.out.println("Qnum"+Qnum);
		    		int[] SIndex = new int[datacount.length+1];
		    		int Snum;
		    		Snum = QRSProcess.SPeekDetect(Y, datacount.length, fre, SIndex, Rnum, RIndex);
		    		System.out.println("Snum"+Snum);
		    		int[] RPeek = new int[Rnum-1];
		    		for (int i = 0; i < Rnum - 1; i++)//这里不知道怎么要减二本来减一
		    		{
		    			RPeek[i] = RIndex[i + 1] - RIndex[i];
		    		}
		    		double RPeekAverage = QRSProcess.sumint(RPeek,Rnum - 1) / (Rnum - 1);
		    		double TR = RPeekAverage / 250.0;
		    		double HR = 60.0 / TR;
		    		System.out.println("HR:"+HR);
		    		int x=Integer.parseInt(new java.text.DecimalFormat("0").format(HR));
		    		Message msg = new Message();  
		            msg.what = 0x112;  
		            Bundle bundle = new Bundle();    
		            bundle.putString("HR",Integer.toString(x));  //往Bundle中存放数据       
		            msg.setData(bundle);//mes利用Bundle传递数据   
		            mHandler.sendMessage(msg);
		        }
	        }
		}	
	}
	
    class MyTimerTask extends TimerTask {
      @Override
       public void run() {  	
       SimpleDraw(currentX);
       currentX++;
       if (currentX == getWindowManager().getDefaultDisplay().getWidth()) {//如果到了终点，则清屏重来
         ClearDraw();
         currentX = 0;
         oldY = centerY;
         screencount++;
	    	vline=0;
	    	hline=centerY;
	    	hline2=centerY;
	    	hnumber1=0.0f;
	    	hnumber2=0.0f;
	    	vnumber=0.0f;
	    	DrawGrid();
          }
	    mWakeLock.acquire(); 
       }
    }
    void SimpleDraw(int length) {

         if (length == 0)
         oldX = 0;
         Canvas canvas = sfh.lockCanvas(new Rect(oldX, 0, oldX + length,
         getWindowManager().getDefaultDisplay().getHeight()));
         //Log.i("Canvas:",String.valueOf(oldX) + "," + String.valueOf(oldY));
         Paint mPaint = new Paint();
         mPaint.setColor(Color.GREEN);
         mPaint.setStrokeWidth(2);
         if (i<Y_axis.length)
         {
         y = Y_axis[i-1];
         canvas.drawLine(oldX, oldY, i-getWindowManager().getDefaultDisplay().getWidth()*screencount, y, mPaint);
         oldX++;
         oldY = y;
         i++;
         }else{
            mHandler.sendEmptyMessage(0x111);
            mTimer.cancel();
            mTimerTask.cancel();
         }
         sfh.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
    }
    void ClearDraw() {
         Canvas canvas = sfh.lockCanvas(null);
         canvas.drawColor(Color.BLACK);// 清除画布
         sfh.unlockCanvasAndPost(canvas);
    }
    void DrawGrid(){
        Canvas canvas = sfh.lockCanvas(new Rect(0, 0, getWindowManager().getDefaultDisplay().getWidth(),
                getWindowManager().getDefaultDisplay().getHeight()));
        canvas.drawColor(Color.BLACK);
        Paint mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(1);
        for(int j=0;j<=frenumber;j++){
        	canvas.drawLine(vline, 0, vline, getWindowManager().getDefaultDisplay().getHeight(), mPaint);
        	DecimalFormat decimalFormat=new DecimalFormat("0.0");
        	String p=decimalFormat.format(vnumber);
        	canvas.drawText(p+"s", vline+3, centerY+13, mPaint);
        	vnumber=vnumber+000.2f;
        	vline=vline+frecount;
        }
        for(int k=0;k<=volnumber/2;k++){
        	canvas.drawLine(0, hline, getWindowManager().getDefaultDisplay().getWidth(),hline, mPaint);
        	canvas.drawText(Float.toString(hnumber1)+"mV", 2, hline, mPaint);
        	hnumber1=hnumber1-0.5f;
        	hline=hline+50;
        }
        for(int k=0;k<=volnumber/2;k++){
        	canvas.drawLine(0, hline2, getWindowManager().getDefaultDisplay().getWidth(),hline2, mPaint);
        	canvas.drawText(Float.toString(hnumber2)+"mV", 2, hline2, mPaint);
        	hnumber2=hnumber2+0.5f;
        	hline2=hline2-50;
        }
        mPaint.setStrokeWidth(4);
        canvas.drawLine(0, centerY, getWindowManager().getDefaultDisplay().getWidth(), centerY, mPaint);
        sfh.unlockCanvasAndPost(canvas);
    }

	public static String ReadTxtFile(String strFilePath, int startline, int endline)
    {
        String path = strFilePath;
        String content = ""; 
        int linecount=0;//文件内容字符串
           //打开文件
           File file = new File(path);
            //如果path是传递过来的参数，可以做一个非目录的判断
           if (file.isDirectory())
            {
                Log.d("TestFile", "The File doesn't not exist.");
            }
            else
            {
                try {
                    InputStream instream = new FileInputStream(file); 
                    if (instream != null) 
                    {
                        InputStreamReader inputreader = new InputStreamReader(instream);
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line;
                        //分行读取
                       while (( line = buffreader.readLine()) != null) {
                           if (linecount>=startline&&linecount<=endline)
                           {
                    	   content += line + "\n";
                           }  
                    	   linecount++;
                        }              
                        instream.close();
                    }
                }
                catch (java.io.FileNotFoundException e) 
                {
                    Log.d("TestFile", "The File doesn't not exist.");
                } 
                catch (IOException e) 
                {
                     Log.d("TestFile", e.getMessage());
                }
            }
            return content;
    }
	public static int ReadTxtFileline(String strFilePath)
    {
        String path = strFilePath;
        int linecount = 0;  //打开文件
           File file = new File(path);
            //如果path是传递过来的参数，可以做一个非目录的判断
           if (file.isDirectory())
            {
                Log.d("TestFile", "The File doesn't not exist.");
            }
            else
            {
                try {
                    InputStream instream = new FileInputStream(file); 
                    if (instream != null) 
                    {
                        InputStreamReader inputreader = new InputStreamReader(instream);
                        BufferedReader buffreader = new BufferedReader(inputreader);
                       
                        //分行读取
                       while (buffreader.readLine()!= null) {
                            linecount++;
                        }                
                        instream.close();
                    }
                }
                catch (java.io.FileNotFoundException e) 
                {
                    Log.d("TestFile", "The File doesn't not exist.");
                } 
                catch (IOException e) 
                {
                     Log.d("TestFile", e.getMessage());
                }
            }
            return linecount;
    }
}
