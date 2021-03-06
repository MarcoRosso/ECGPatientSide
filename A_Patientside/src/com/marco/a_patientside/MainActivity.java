package com.marco.a_patientside;





import java.io.File;
import java.util.List;

import com.marco.constant.PatientUser;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity {
    private EditText usernameEdittext;
    private EditText passwordEdittext;
    private Button loginbutton;
    private String roomnumber;
    private long exitTime = 0;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().hide();
        usernameEdittext=(EditText)findViewById(R.id.username);
        passwordEdittext=(EditText)findViewById(R.id.password);
        loginbutton=(Button)findViewById(R.id.login);
        usernameEdittext.setText("����");
        passwordEdittext.setText("test");
        Bmob.initialize(this, "540ce211a2e2d4de0350b0b92cef5ebf");
		 File file = null;
		    try {
		        file = new File("/sdcard/ECG/");
		        if (!file.exists()) {
		            file.mkdir();
		        }
		    } catch (Exception e) {
		        Log.i("error:", e+"");
		    }
        loginbutton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				final String usernames=usernameEdittext.getText().toString();
		    	String passwords=passwordEdittext.getText().toString();
		    	if(usernames.equals("")||passwords.equals("")){
		    		Toast.makeText(MainActivity.this, R.string.empty, Toast.LENGTH_SHORT).show();
		    		return;
		    	}
		    	pd=new ProgressDialog(MainActivity.this);
		    	pd.setTitle("���ڵ�½");
		    	pd.setMessage("���Ժ�......");
		    	pd.setCancelable(false);
		    	pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		    	pd.setIndeterminate(true);
		    	pd.show();
		    	final BmobUser user= new BmobUser();
		    	user.setUsername(usernames);
		    	user.setPassword(passwords);
		    	user.login(MainActivity.this, new SaveListener(){

					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub
						pd.dismiss();
						Toast.makeText(MainActivity.this, R.string.loginfailed, Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						BmobQuery<PatientUser> query = new BmobQuery<PatientUser>();
						query.addWhereEqualTo("username", usernames);
						query.findObjects(MainActivity.this, new FindListener<PatientUser>() {
						    @Override
						    public void onSuccess(List<PatientUser> object) {
                            	pd.dismiss();
                                for(PatientUser patientuser:object){
                                	boolean isuse=patientuser.isInUse();
                                	if(isuse){
                                	roomnumber=patientuser.getRoomnumber();
            						Intent intent= new Intent();
            						intent.putExtra("username", usernames);
            						intent.putExtra("roomnumber", roomnumber);
            						intent.setClass(MainActivity.this, ContactActivity.class);
            						startActivity(intent);
            		        		finish();
            		        		}else
            		        	   Toast.makeText(MainActivity.this, "�����˺��ѱ�ע��", Toast.LENGTH_SHORT).show();
                                }
						    }
						    @Override
						    public void onError(int code, String msg) {
						    	pd.dismiss();
						    	Toast.makeText(MainActivity.this, R.string.loginfailed, Toast.LENGTH_SHORT).show();
						    }
						});
 
					}	    		
		    	});
			
			}});
       /* DisplayMetrics metric = new DisplayMetrics(); 
        getWindowManager().getDefaultDisplay().getMetrics(metric); 
        int width = metric.widthPixels;  // ��Ļ���ȣ����أ� 
        System.out.println("width"+width);
        int height = metric.heightPixels;  // ��Ļ�߶ȣ����أ�
        System.out.println("height"+height);
        float density = metric.density;  // ��Ļ�ܶȣ�0.75 / 1.0 / 1.5�� 
        System.out.println("density"+density);
        float xdpi=metric.xdpi;
        System.out.println("xdpi"+xdpi);
        float ydpi=metric.ydpi;
        System.out.println("ydpi"+ydpi);
        float xcm = (float) (width/(metric.xdpi / 2.54));
        System.out.println("xcm"+xcm);
        float ycm=(float)(height/(metric.ydpi/2.54));
        System.out.println("ycm"+ycm);
        int densityDpi = metric.densityDpi;  // ��Ļ�ܶ�DPI��120 / 160 / 240�� 
        System.out.println("densityDpi"+densityDpi);
       double diagonalPixels = Math.sqrt(Math.pow(width, 2)+Math.pow(height, 2)) ;
       System.out.println("diagonalPixels"+diagonalPixels);
       double screenSize = diagonalPixels/(160*density) ;
       System.out.println("screenSize"+screenSize);*/
        loginbutton.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				    Intent intent= new Intent();
					intent.setClass(MainActivity.this, OffLineRead.class);
					startActivity(intent);
	        		finish();
				return false;
			}
        	
        });
    }
    

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

}
