package com.marco.a_patientside;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.bmob.BTPFileResponse;
import com.bmob.BmobConfiguration;
import com.bmob.BmobPro;
import com.bmob.BmobProFile;
import com.bmob.btp.callback.DownloadListener;
import com.bmob.btp.callback.UploadListener;
import com.marco.constant.Filename;
import com.marco.constant.PatientUser;
import com.marco.getfilepath.CallbackBundle;
import com.marco.getfilepath.OpenFileDialog;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OtherFunction extends Activity{
	static private int openfileDialogId = 0; 
	private Button contactnumberconfirm;
	private Button contactnumber1search;
	private Button contactnumber2search;
	private Button contactnumber3search;
	private Button alertswitch;
	private EditText contactnumber1;
	private EditText contactnumber2;
	private EditText contactnumber3;
	private TextView uploadname;
	private TextView readname;
	private TextView downloadname;
	private String uploadpath="";
	private String readpath="";
	private String username;
	private String contact1;
	private String contact2;
	private String contact3;
	private String realfilename;
	private String downfilename;
	private int searchbuttonnumber;
	private int buttonnumber=1;
	private Boolean switchcondition;
	ProgressDialog pd;
	PatientUser user= new PatientUser();
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	private static final int PICK_CONTACT_SUBACTIVITY = 2;
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_layout);
        getActionBar().hide();
        uploadname=(TextView)findViewById(R.id.upload_name);
        readname=(TextView)findViewById(R.id.read_name);
        downloadname=(TextView)findViewById(R.id.download_name);
        contactnumberconfirm=(Button)findViewById(R.id.contactconfirm);
        contactnumber1search=(Button)findViewById(R.id.searchcontact1);
        contactnumber2search=(Button)findViewById(R.id.searchcontact2);
        contactnumber3search=(Button)findViewById(R.id.searchcontact3);
        alertswitch=(Button)findViewById(R.id.alertswitch);
        contactnumber1=(EditText)findViewById(R.id.contactnumber1);
        contactnumber2=(EditText)findViewById(R.id.contactnumber2);
        contactnumber3=(EditText)findViewById(R.id.contactnumber3);
        
        preferences = getSharedPreferences("setting", MODE_PRIVATE);
		contact1=preferences.getString("contact1", "");
		contact2=preferences.getString("contact2", "");
		contact3=preferences.getString("contact3", "");
		switchcondition=preferences.getBoolean("alerswitch", true);
		contactnumber1.setText(contact1);
		contactnumber2.setText(contact2);
		contactnumber3.setText(contact3);
		
		if(switchcondition) alertswitch.setBackgroundResource(R.drawable.switch_on);
		else alertswitch.setBackgroundResource(R.drawable.switch_off);
		 contactnumber1search.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
			        Uri uri = Uri.parse("content://contacts/people"); 
			        Intent intent = new Intent(Intent.ACTION_PICK, uri);		        
			        startActivityForResult(intent, PICK_CONTACT_SUBACTIVITY);
			        searchbuttonnumber=1;
				}        	
	        });
	        contactnumber2search.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
			        Uri uri = Uri.parse("content://contacts/people"); 
			        Intent intent = new Intent(Intent.ACTION_PICK, uri);		        
			        startActivityForResult(intent, PICK_CONTACT_SUBACTIVITY);
			        searchbuttonnumber=2;
				}        	
	        });
	        contactnumber3search.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
			        Uri uri = Uri.parse("content://contacts/people"); 
			        Intent intent = new Intent(Intent.ACTION_PICK, uri);		        
			        startActivityForResult(intent, PICK_CONTACT_SUBACTIVITY);
			        searchbuttonnumber=3;
				}        	
	        });
	        contactnumberconfirm.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					contact1=contactnumber1.getText().toString();
					contact2=contactnumber2.getText().toString();
					contact3=contactnumber3.getText().toString();
					int count=0;
		    		editor = preferences.edit();
					editor.putString("contact1", contact1);
					editor.putString("contact2", contact2);
					editor.putString("contact3", contact3);
					editor.commit();
					if(!contact1.equals("")) count++;
					if(!contact2.equals("")) count++;
					if(!contact3.equals("")) count++;
					  Toast.makeText(OtherFunction.this,   
							  "您共保存了"+ count +"个有效号码",   
	  	                        Toast.LENGTH_SHORT).show(); 			
				}      	
	        });
	        alertswitch.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					if(switchcondition) {
						alertswitch.setBackgroundResource(R.drawable.switch_off);
						switchcondition=false;
						if (isMyServiceRunning())
				        {Intent intent1 = new Intent(OtherFunction.this,FallDownService.class); 
				        stopService(intent1);} 
					}
					else {
						alertswitch.setBackgroundResource(R.drawable.switch_on);
						switchcondition=true;
						if (!isMyServiceRunning())
				        {Intent intent1 = new Intent(OtherFunction.this,FallDownService.class); 
				        startService(intent1);} 
					}
					editor = preferences.edit();
					editor.putBoolean("alerswitch", switchcondition);
					editor.commit();					
				}
	        	
	        });
	        BmobConfiguration config = new BmobConfiguration.Builder(getApplicationContext()).customExternalCacheDir("ECGDownLoad").build();
	        BmobPro.getInstance(getApplicationContext()).initConfig(config);
        Intent intent=getIntent();
        username=intent.getStringExtra("username");
		BmobQuery<PatientUser> query = new BmobQuery<PatientUser>();
    	query.addWhereEqualTo("username", username);
    	query.findObjects(this, new FindListener<PatientUser>() {
    	    @Override
    	    public void onError(int code, String msg) {
    	        // TODO Auto-generated method stub
    	    	Toast.makeText(OtherFunction.this, R.string.usergetfailed, Toast.LENGTH_SHORT).show();
    	    	finish();
    	    }
			@Override
			public void onSuccess(List<PatientUser> arg0) {
				// TODO Auto-generated method stub
                for(PatientUser patientuser:arg0){
                	String objectid=patientuser.getObjectId();
            //   Toast.makeText(OtherFunction.this, R.string.usergetsuccess, Toast.LENGTH_SHORT).show();
                user.setObjectId(objectid);
			}}
    	});	
    	
	}
	protected void onActivityResult 
	  (int requestCode, int resultCode, Intent data) 
	  { 
       if(data!=null){
	    switch (requestCode) 
	    {  
	      case PICK_CONTACT_SUBACTIVITY: 
	        final Uri uriRet = data.getData(); 
	        if(uriRet != null) 
	        { 
	          try 
	          {
	           @SuppressWarnings("deprecation")
			Cursor c = managedQuery(uriRet, null, null, null, null);
	           c.moveToFirst();
	           String strPhone = "";
	           int contactId = c.
	             getInt(c.getColumnIndex(ContactsContract.Contacts._ID));
	           Cursor curContacts = getContentResolver().
	             query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
	                 null,ContactsContract.CommonDataKinds.Phone.
	                   CONTACT_ID +" = "+ contactId,null, null);
	           if(curContacts.getCount()>0)
	           {
	             curContacts.moveToFirst();
	             strPhone = curContacts.getString(curContacts.
	                 getColumnIndex(ContactsContract.CommonDataKinds.
	                                  Phone.NUMBER));
	           }
	           else
	           {
	           }
	           switch(searchbuttonnumber){
	           case 1:contactnumber1.setText(strPhone);break;
	           case 2:contactnumber2.setText(strPhone);break;
	           case 3:contactnumber3.setText(strPhone);break;
	           }
	          } 
	          catch(Exception e) 
	          {             
	            e.printStackTrace(); 
	          } 
	        } 
	        break; 
	    } }
	    super.onActivityResult(requestCode, resultCode, data);    
	  } 
	public void choosefile(View view){
		showDialog(openfileDialogId);
		buttonnumber=1;
	}
	public void choosefile_read(View view){
		showDialog(openfileDialogId);
		buttonnumber=2;
	}
	public void downloadfile_choose(View view){		
            downloadchoose();		
	}
	public void downloadfile_confirm(View view){
		if(realfilename.equals("")||downfilename.equals("")){
			Toast.makeText(OtherFunction.this, "未选择下载文件", Toast.LENGTH_SHORT).show();
			return;
		}
		downloadfile(downfilename,realfilename);
	}
	public void readconfirm(View view){
		if(readpath.equals("")){
			Toast.makeText(OtherFunction.this, R.string.emptypath, Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent= new Intent();
		intent.putExtra("readpath", readpath);
		intent.setClass(OtherFunction.this, ECGFileRead.class);
		startActivity(intent);	
	}
	public void uploadconfirm(View view){
		if(uploadpath.equals("")){
			Toast.makeText(OtherFunction.this, R.string.emptypath, Toast.LENGTH_SHORT).show();
			return;
		}
		uploadfile();
	
	}
	private void downloadchoose(){
		BmobQuery<Filename> cards = new BmobQuery<Filename>();
	    cards.addWhereRelatedTo("filename", new BmobPointer(user));
	    cards.findObjects(this, new FindListener<Filename>() {

	        @Override
	        public void onSuccess(List<Filename> arg0) {
	            // TODO Auto-generated method stub
            	final String[] realnamearray= new String[arg0.size()];
            	final String[] filenamearray= new String[arg0.size()];
            	int i=0;
	            for (Filename filename : arg0) {
	            	realnamearray[i]=filename.getRealname();
	            	filenamearray[i]=filename.getFilename();
	            	i++;
	                Log.d("bmob", "objectId:"+filename.getObjectId()+",下载名称："+filename.getFilename()+",真实名称："+filename.getRealname());
	            }
                Dialog alertDialog = new android.app.AlertDialog.Builder(OtherFunction.this). 
                	    setTitle("需要下载哪个文件？"). 
                	    setIcon(R.drawable.ic_launcher) 
                	    .setItems(realnamearray, new DialogInterface.OnClickListener() { 
                	     @Override 
                	     public void onClick(DialogInterface dialog, int which) { 
                	    	 downloadname.setText(realnamearray[which]);
                	    	 realfilename=realnamearray[which];
                	    	 downfilename=filenamearray[which];
                	     } 
                	    }). 
                	    setNegativeButton("取消", new DialogInterface.OnClickListener() {

                	     @Override 
                	     public void onClick(DialogInterface dialog, int which) { 
                	      // TODO Auto-generated method stub
                	    	dialog.dismiss();
                	     } 
                	    }). 
                	    create(); 
                alertDialog.show();
	        }

	        @Override
	        public void onError(int arg0, String arg1) {
	            // TODO Auto-generated method stub
	        	Toast.makeText(OtherFunction.this,R.string.downloadqueryfailed,Toast.LENGTH_SHORT).show();
	        }
	    });
		
	}
	private void downloadfile(String downloadpath,final String filerealname){
		pd = new ProgressDialog(OtherFunction.this);
		pd.setTitle("正在下载文件");
		pd.setCancelable(false);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setIndeterminate(false);
		pd.show();
		BmobProFile.getInstance(OtherFunction.this).download(downloadpath, new DownloadListener() {

            @Override
            public void onSuccess(String fullPath) {
            	pd.dismiss();
                // TODO Auto-generated method stub
                Toast.makeText(OtherFunction.this, "下载成功!", Toast.LENGTH_SHORT).show();
                System.out.println("fullpath:"+fullPath);
                copyandchangname(fullPath,filerealname);
            }

            @Override
            public void onProgress(String localPath, int percent) {
                // TODO Auto-generated method stub
            	pd.setProgress(percent);
            	System.out.println("localpath:"+localPath);
            }

            @Override
            public void onError(int statuscode, String errormsg) {
                // TODO Auto-generated method stub
                pd.dismiss();
                Toast.makeText(OtherFunction.this,"下载出错："+statuscode +"--"+errormsg, Toast.LENGTH_SHORT).show();
            }
        });
	}
	private void copyandchangname(String oldPath,String filerealname){
		 File file = null;
		    try {
		        file = new File("/sdcard/ECG/Download/");
		        if (!file.exists()) {
		            file.mkdir();
		        }
		    } catch (Exception e) {
		        Log.i("error:", e+"");
		    }

         try {   
             int bytesum = 0;   
             int byteread = 0;   
             File oldfile = new File(oldPath);   
             if (oldfile.exists()) { //文件存在时   
                 InputStream inStream = new FileInputStream(oldPath); //读入原文件   
                 FileOutputStream fs = new FileOutputStream("/sdcard/ECG/Download/"+filerealname);   
                 byte[] buffer = new byte[1444];   
                 while ( (byteread = inStream.read(buffer)) != -1) {   
                     bytesum += byteread; //字节数 文件大小   
                     System.out.println(bytesum);   
                     fs.write(buffer, 0, byteread);   
                 }   
                 inStream.close();   
             }   
         }   
         catch (Exception e) {   
             System.out.println("复制单个文件操作出错");   
             e.printStackTrace();    
         } 
         Intent intent= new Intent();
         intent.setClass(OtherFunction.this, OtherFunction.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
         startActivity(intent);
         finish();
         overridePendingTransition(0, 0);
	}
	private void uploadfile(){
		pd = new ProgressDialog(OtherFunction.this);
		pd.setTitle("正在上传文件");
		pd.setCancelable(false);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setIndeterminate(false);
		pd.show();
		
		BTPFileResponse response = BmobProFile.getInstance(OtherFunction.this).upload(uploadpath, 
				new UploadListener() {

            @Override
            public void onSuccess(String fileName,String url) {
                // TODO Auto-generated method stub
    			Toast.makeText(OtherFunction.this, R.string.fileuploadsuccess, Toast.LENGTH_SHORT).show();
    			pd.dismiss();
    			saveFileNameInfo(fileName);
            }

            @Override
            public void onProgress(int ratio) {
                // TODO Auto-generated method stub
            	pd.setProgress(ratio);

            }
            @Override
            public void onError(int statuscode, String errormsg) {
                // TODO Auto-generated method stub
            	Toast.makeText(OtherFunction.this, R.string.fileuploadfailed, Toast.LENGTH_SHORT).show();
            	pd.dismiss();
            }
        });
	}
	private void saveFileNameInfo(String name){
	    if(TextUtils.isEmpty(user.getObjectId())){
	    	Toast.makeText(OtherFunction.this, R.string.usernameempty, Toast.LENGTH_SHORT).show();
	        return;
	    }
	    final Filename filename = new Filename();
	    filename.setFilename(name);        // 设置银行名称 
	    filename.setUser(user);
        filename.setRealname(uploadname.getText().toString());// 设置银行卡户主
	    filename.save(this, new SaveListener() {
	        @Override
	        public void onSuccess() {
	            // TODO Auto-generated method stub
		    	Toast.makeText(OtherFunction.this, R.string.fileinfosuccess, Toast.LENGTH_SHORT).show();
	            addFileToUser(filename);
	        }

	        @Override
	        public void onFailure(int arg0, String arg1) {
	        	System.out.println("fileinfo:"+arg0);
	            // TODO Auto-generated method stub
		    	Toast.makeText(OtherFunction.this, R.string.fileinfofailed, Toast.LENGTH_SHORT).show();
	        }
	    });
	}
	private void addFileToUser(Filename filename){
	    if(TextUtils.isEmpty(user.getObjectId()) || 
	            TextUtils.isEmpty(filename.getObjectId())){
	    	Toast.makeText(OtherFunction.this, R.string.usernameempty, Toast.LENGTH_SHORT).show();
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
	        	Toast.makeText(OtherFunction.this, R.string.attachusersuccess, Toast.LENGTH_SHORT).show();
	        }

	        @Override
	        public void onFailure(int arg0, String arg1) {
	            // TODO Auto-generated method stub
	        	Toast.makeText(OtherFunction.this, R.string.attachuserfailed, Toast.LENGTH_SHORT).show();
	        }
	    });
	}
	protected Dialog onCreateDialog(int id) {  
        if(id==openfileDialogId){   
            Map<String, Integer> images = new HashMap<String, Integer>();  
            // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹  
            images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);   // 根目录图标  
            images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);    //返回上一层的图标  
            images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);   //文件夹图标  
            images.put("txt", R.drawable.filedialog_txtfile);   //文件图标  
            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);  
            Dialog dialog = OpenFileDialog.createDialog(id, this, "打开文件", new CallbackBundle() {  
                public void callback(Bundle bundle) {  
                    String filepath = bundle.getString("path");
                    setTitle(filepath); // 把文件路径显示在标题上  
                    String filename[]=filepath.split("/");
                    switch(buttonnumber){
                    case 1:uploadname.setText(filename[filename.length-1]);uploadpath=filepath;break;
                    case 2:readname.setText(filename[filename.length-1]);readpath=filepath;break;
                    }
                    
                }  
            },   
            ".txt;",  
            images);  
            return dialog;  
        }  
        return null;  
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
