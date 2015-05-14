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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ECGFileRead extends Activity{
	private String filepath;
	private TextView draw;
	private TextView ecgshow;
	private TextView drawt;
	private TextView drawnumber;
	private TextView previous;
	private TextView next;
	private TextView ecgname;
	private TextView pre;
	private TextView nex;
	private TextView bac;
	private TextView forw;
	private TextView back;
	private TextView forward;
	private TextView counttex;
	private TextView counttext;
	private TextView ecgperiod;
	private SurfaceView sfv;
    private SurfaceHolder sfh;
    private float frecount=0;
    private float smallfrecount=0;
    private int fre=0;
    private int frenumber=0;
    private int smallfrenumber=0;
    private int linecount;
    private int drawcount;
    private int drewcount=1;
    private int calculate=0;
    private int totalamount=0;
    private double readonetime;
    double[] data;
    float hnumber1=0.0f,hnumber2=0.0f,vnumber=0.0f;
    private int  centerY,hline=0,hline2=0,smallhline=0,smallhline2=0,volnumber=0,smallvolnumber=0,oldY,Y_axis[],y=0;
    private float vline=0;
    private float smallvline=0;
    private float onceplus=0;
    private float drawoldx=0;
    private float drawnextx=0;
    Handler mHandler= new Handler(){
    	public void handleMessage(Message msg){
            if(msg.what==0x112){
    	    	ecgshow.setText(msg.getData().getString("HR","XXX"));
    	    }
    	}
    };
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecgfileread_layout);
        getActionBar().hide();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        draw=(TextView)findViewById(R.id.drawgraph);
        ecgshow=(TextView)findViewById(R.id.show);
        drawt=(TextView)findViewById(R.id.drawgrapht);
        drawnumber=(TextView)findViewById(R.id.drawnumber);
        previous=(TextView)findViewById(R.id.pre);
        next=(TextView)findViewById(R.id.nex);
        ecgname=(TextView)findViewById(R.id.textView1);
        pre=(TextView)findViewById(R.id.previous);
        nex=(TextView)findViewById(R.id.next);
        bac=(TextView)findViewById(R.id.back);
        forw=(TextView)findViewById(R.id.forward);
        back=(TextView)findViewById(R.id.bac);
        forward=(TextView)findViewById(R.id.forw);
        counttext=(TextView)findViewById(R.id.counttex);
        counttex=(TextView)findViewById(R.id.counttext);
        ecgperiod=(TextView)findViewById(R.id.ecgperiod);
        
        sfv = (SurfaceView)findViewById(R.id.SurfaceView01);      
        sfh = sfv.getHolder();
        centerY = (getWindowManager().getDefaultDisplay().getHeight()-sfv.getTop()) / 2;
        previous.setVisibility(View.GONE);
        next.setVisibility(View.GONE);
        drawnumber.setVisibility(View.GONE);
        ecgshow.setVisibility(View.GONE);
        ecgname.setVisibility(View.GONE);
        pre.setVisibility(View.GONE);
        nex.setVisibility(View.GONE);
        bac.setVisibility(View.GONE);
        forw.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        forward.setVisibility(View.GONE);
        counttext.setVisibility(View.GONE);
        counttex.setVisibility(View.GONE);
        ecgperiod.setVisibility(View.GONE);
                
        Intent intent=getIntent();
        filepath=intent.getStringExtra("readpath");
        
        linecount=ReadTxtFileline(filepath);
        String readfile=ReadTxtFile(filepath,0,0);
        String tempfre[]=readfile.split("\n");
        fre=Integer.parseInt(tempfre[0]);
        
        DisplayMetrics metric = new DisplayMetrics(); 
        getWindowManager().getDefaultDisplay().getMetrics(metric); 
        float xcm = (float) (metric.xdpi / 2.54);
        double temp=getWindowManager().getDefaultDisplay().getWidth()/xcm/0.5;
        frenumber=(int)temp;
        double temp2=getWindowManager().getDefaultDisplay().getWidth()/xcm/0.1;
        smallfrenumber=(int)temp2;
        frecount=(float) (xcm*0.5);
        smallfrecount=(float)(xcm*0.1);
        volnumber=getWindowManager().getDefaultDisplay().getHeight()/50;
        smallvolnumber=getWindowManager().getDefaultDisplay().getHeight()/10;
        double temp3=getWindowManager().getDefaultDisplay().getWidth()/xcm/2.5*fre;
        double temp4=getWindowManager().getDefaultDisplay().getWidth()/temp3;
        onceplus=(float)temp4;
        readonetime=frenumber*(frecount/temp4);
    	int drawtemp=(int) ((linecount-1)/readonetime);
    	int afterdot=(int) ((linecount-1)%readonetime);
    	if(afterdot==0)
    	drawcount=(int)drawtemp;
    	else
    	drawcount=(int)drawtemp+1;
        
        draw.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
		    	ClearDraw();
		        String read=ReadTxtFile(filepath,1,(int)readonetime+3);
		        String tempdata[]=read.split("\n");
		        data = new double[tempdata.length];
		        Y_axis= new int[tempdata.length]; 
		        for(int i=0;i<tempdata.length;i++){
		        	data[i]=Double.parseDouble(tempdata[i]);
		        	Y_axis[i]=(int)(data[i]*100);
		        	Y_axis[i] = centerY-Y_axis[i];	           
		        }
		    	vline=0;
		    	smallvline=0;
		    	hline=centerY;
		    	hline2=centerY;
			    smallhline=centerY;
			    smallhline2=centerY;
		        drawoldx=0;
		        drawnextx=0;
		    	y=0;
		        oldY = centerY;
		    	hnumber1=0.0f;
		    	hnumber2=0.0f;
		    	vnumber=0.0f;
		    	drewcount=1;	    	
		        drawnumber.setText("共"+Integer.toString(drawcount)+"屏，现在是第"+
                        Integer.toString(drewcount)+"屏");
		        ecgshow.setText("XXX");
 		    	SimpleDraw(Y_axis.length);
 		    	draw.setVisibility(View.GONE);
 		    	drawt.setVisibility(View.GONE);
		        previous.setVisibility(View.VISIBLE);
		        next.setVisibility(View.VISIBLE);
		        pre.setVisibility(View.VISIBLE);
		        nex.setVisibility(View.VISIBLE);
		        drawnumber.setVisibility(View.VISIBLE);
		        ecgshow.setVisibility(View.VISIBLE);
		        ecgname.setVisibility(View.VISIBLE);
		        bac.setVisibility(View.VISIBLE);
		        forw.setVisibility(View.VISIBLE);
		        back.setVisibility(View.VISIBLE);
		        forward.setVisibility(View.VISIBLE);
		        counttext.setVisibility(View.VISIBLE);
		        counttex.setVisibility(View.VISIBLE);
		        ecgperiod.setVisibility(View.VISIBLE);
		        HRCal();
			}      	
        });
        next.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				drewcount++;
				if(drewcount==drawcount){
		    	   Toast.makeText(getApplicationContext(), "文件读取完毕", Toast.LENGTH_SHORT).show();
				  drewcount=drawcount;
				}
				if(drewcount>drawcount){
			    	   Toast.makeText(getApplicationContext(), "当前已是最后一屏!", Toast.LENGTH_SHORT).show();	
					   drewcount=drawcount;
				       return;
				}
		        drawnumber.setText("共"+Integer.toString(drawcount)+"屏，现在是第"+
                        Integer.toString(drewcount)+"屏");
		        ClearDraw();
		        String read=ReadTxtFile(filepath,(drewcount-1)*(int)readonetime,drewcount*(int)readonetime);
		        String tempdata[]=read.split("\n");
		        data = new double[tempdata.length];
		        Y_axis= new int[tempdata.length]; 
		        for(int i=0;i<tempdata.length;i++){
		        	data[i]=Double.parseDouble(tempdata[i]);
		        	Y_axis[i]=(int)(data[i]*100);
		        	Y_axis[i] = centerY-Y_axis[i];
		           
		        }
		    	vline=0;
		    	smallvline=0;
		    	hline=centerY;
		    	hline2=centerY;
		    	smallhline=centerY;
			    smallhline2=centerY;
		        drawoldx=0;
		        drawnextx=0;
		    	y=0;
		        oldY = centerY;
		    	hnumber1=0.0f;
		    	hnumber2=0.0f;
		    	vnumber=0.0f;
		    	SimpleDraw(Y_axis.length);
		   }			        	
        });
        previous.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				drewcount--;
				if(drewcount==drawcount)
		    	   Toast.makeText(getApplicationContext(), "文件读取完毕", Toast.LENGTH_SHORT).show();
				if(drewcount==0){
			    	   Toast.makeText(getApplicationContext(), "当前已是第一屏!", Toast.LENGTH_SHORT).show();
			    	   drewcount=1;
				       return;
				}
		        drawnumber.setText("共"+Integer.toString(drawcount)+"屏，现在是第"+
                        Integer.toString(drewcount)+"屏");
		        ClearDraw();
		        if(drewcount==1){
			        String read=ReadTxtFile(filepath,1,drewcount*(int)readonetime);
			        String tempdata[]=read.split("\n");
			        data = new double[tempdata.length];
			        Y_axis= new int[tempdata.length]; 
			        for(int i=0;i<tempdata.length;i++){
			        	data[i]=Double.parseDouble(tempdata[i]);
			        	Y_axis[i]=(int)(data[i]*100);
			        	Y_axis[i] = centerY-Y_axis[i];
			           
			        }
		        }else{
		        String read=ReadTxtFile(filepath,(drewcount-1)*(int)readonetime,drewcount*(int)readonetime);
		        String tempdata[]=read.split("\n");
		        data = new double[tempdata.length];
		        Y_axis= new int[tempdata.length]; 
		        for(int i=0;i<tempdata.length;i++){
		        	data[i]=Double.parseDouble(tempdata[i]);
		        	Y_axis[i]=(int)(data[i]*100);
		        	Y_axis[i] = centerY-Y_axis[i];	           
		        }}
		    	vline=0;
		    	smallvline=0;
		    	hline=centerY;
		    	hline2=centerY;
		    	smallhline=centerY;
			    smallhline2=centerY;
		        drawoldx=0;
		        drawnextx=0;
		    	y=0;
		        oldY = centerY;
		    	hnumber1=0.0f;
		    	hnumber2=0.0f;
		    	vnumber=0.0f;
		    	SimpleDraw(Y_axis.length);
		   }			        	
        });
        back.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(calculate>0){
					calculate=calculate-2;
					HRCal();
					if(calculate==0)
					ecgperiod.setText("1-"+Integer.toString(calculate+12)+"秒");
					else
					ecgperiod.setText(Integer.toString(calculate)+"-"+Integer.toString(calculate+12)+"秒");
				}else{
					Toast.makeText(ECGFileRead.this, "已到开头，无法前移！", Toast.LENGTH_SHORT).show();
					return;
				}
			}
        	
        });
        forward.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if((calculate+12)*fre<linecount-1){
					calculate=calculate+2;
					HRCal();
					ecgperiod.setText(Integer.toString(calculate)+"-"+Integer.toString(calculate+12)+"秒");
				}else{
					Toast.makeText(ECGFileRead.this, "已到末尾，无法后移！", Toast.LENGTH_SHORT).show();
					return;
				}
			}        	
        });
        
	}
	protected void onDestory(){
		super.onDestroy();
	}
	public void HRCal(){
        new Thread(){ 
           public void run(){ 
        	   double[] datacount=new double[fre*12];
           	if(calculate==0)
   	        {String readdata=ReadTxtFile(filepath,1,fre*12);
   	        String tempdatas[]=readdata.split("\n");
   	        for(int i=0;i<tempdatas.length;i++){
   	        	datacount[i]=Double.parseDouble(tempdatas[i]);	           
   	        }
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
       		double TR = RPeekAverage / fre;
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
   	        	if(fre*(calculate+12)>linecount-1){
                        return;
   		        }else{
   		        	String readdata=ReadTxtFile(filepath,fre*calculate+1,
   		        			fre*(12+calculate));
   			        String tempdatas[]=readdata.split("\n");
   			        for(int i=0;i<tempdatas.length;i++){
   			        	datacount[i]=Double.parseDouble(tempdatas[i]);
   		             }
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
       }.start(); 
	}
	
    void SimpleDraw(int length) {
        Canvas canvas = sfh.lockCanvas(new Rect(0, 0, getWindowManager().getDefaultDisplay().getWidth(),
                getWindowManager().getDefaultDisplay().getHeight()));
        canvas.drawColor(Color.BLACK);
        Paint mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(2);
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
        canvas.drawLine(0, centerY, getWindowManager().getDefaultDisplay().getWidth(), centerY, mPaint);
    	        mPaint.setColor(Color.GREEN);// 画笔为绿色
    	        mPaint.setStrokeWidth(2);// 设置画笔粗细
    	        int y;
    	        for (int i = 0; i < length; i++) {// 绘画
    	        y = Y_axis[i];
    	        canvas.drawLine(drawoldx, oldY, drawnextx, y, mPaint);
    	        drawoldx=drawnextx;
    	        drawnextx=drawoldx+onceplus;
    	        oldY = y;
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
