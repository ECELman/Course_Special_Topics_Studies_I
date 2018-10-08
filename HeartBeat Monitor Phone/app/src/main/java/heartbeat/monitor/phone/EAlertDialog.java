package heartbeat.monitor.phone;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import heartbeat.monitor.phone.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EAlertDialog extends Activity{
	private Button alt_btn_ok;
	private TextView TV1;
	private TextView TV2;
	private TextView TV3;
    private SoundPool sPool;							//音效pool
	private HashMap<Integer, Integer> sPoolMap;			//音效資料hashmap
	private static final int Dialog_Openning= 0;
	private static final int Dialog_Closed = 1;
	private int Flag = 0;
	private String Warning_Type;
	private String Warning_Name;
	private String Warning_SmsPhone_1;
	private String Warning_SmsPhone_2;
	private String Warning_SmsPhone_3;
	public static String filename = "data.ini";
	public static String contactPhone_key = "contactPhone";
	public static String userAge_key = "userAge";
	private String contactPhone_value = "";
	private String userAge_value = "";
	private RelativeLayout RL1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.alertdialog);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        findView();
        setListener();
        checkfile();
        loadfiles();
        initsounds();
        CDtime();
	}
	
	private void checkfile(){
		boolean isExit = true;
		FileOutputStream fos = null;
		try{
			openFileInput(filename);
		}
		catch(FileNotFoundException e){
			isExit = false;
		}
		if(!isExit){
			try{
				fos = openFileOutput(filename, MODE_WORLD_WRITEABLE);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				String txt = contactPhone_key + "=" + contactPhone_value;
				bos.write(txt.getBytes());
				bos.write(new String("\n").getBytes());
				txt = userAge_key + "=" + userAge_value;
				bos.write(txt.getBytes());
				
				bos.close();
				fos.close();
			}
			catch(FileNotFoundException e){
				
			}
			catch(IOException e){
				
			}
		}
	}

	//讀檔區
	private void loadfiles(){
		Properties p = new Properties();
		try{
			p.load(openFileInput(filename));
			contactPhone_value = p.getProperty(contactPhone_key);
		}
		catch(FileNotFoundException e){
			
		}
		catch(IOException e){
			
		}
	}

	//音效初始區
    private void initsounds(){
    	sPool = new SoundPool(4,AudioManager.STREAM_MUSIC,100);
    	sPoolMap = new HashMap<Integer, Integer> ();
    	sPoolMap.put(1, sPool.load(this, R.raw.warning, 1));
    }
	//音效播放區
    private void WarningSounds(int sound,int loop,int  priority){
    	AudioManager Wmgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    	float streamVolumeCurrent = Wmgr.getStreamVolume(AudioManager.STREAM_MUSIC);
    	float streamVolumeMax = Wmgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    	float Volume = streamVolumeCurrent/streamVolumeMax;
    	sPool.play(sPoolMap.get(sound), Volume, Volume,  priority, loop, 1f);
    	}
	
	private void findView(){
		alt_btn_ok = (Button)findViewById(R.id.Alert_Btn_ok);
		TV1 = (TextView)findViewById(R.id.Alert_TextView1);
		TV2 = (TextView)findViewById(R.id.Alert_TextView2);
		TV3 = (TextView)findViewById(R.id.Alert_TextView3);
		RL1 = (RelativeLayout)findViewById(R.id.alert_relativeLayout);
		Intent intent = getIntent();
		Warning_Type = (String)intent.getExtras().get("WarnType");
		Warning_Name = (String)intent.getExtras().get("WarnName");
		Warning_SmsPhone_1 = (String)intent.getExtras().get("WarnSmsPhone1");
		Warning_SmsPhone_2 = (String)intent.getExtras().get("WarnSmsPhone2");
		Warning_SmsPhone_3 = (String)intent.getExtras().get("WarnSmsPhone3");
		
		 if(Warning_Type.equals("IhrHigh"))
			 TV1.setText("警告!! 病患  " + Warning_Name + "  " + getString(R.string.altdia_ihr_high));
		 else if(Warning_Type.equals("IhrLow"))
			 TV1.setText("警告!! 病患  " + Warning_Name + "  " + getString(R.string.altdia_ihr_low));
		 else if(Warning_Type.equals("BTHigh"))
			 TV1.setText("警告!! 病患  " + Warning_Name + "  " + getString(R.string.altdia_bt_high));
		 else if(Warning_Type.equals("LBBB"))
			 TV1.setText("警告!! 病患  " + Warning_Name + "  " + getString(R.string.altdia_LBBB));
		 else if(Warning_Type.equals("RBBB"))
			 TV1.setText("警告!! 病患  " + Warning_Name + "  " + getString(R.string.altdia_RBBB));
		 else if(Warning_Type.equals("APC"))
			 TV1.setText("警告!! 病患  " + Warning_Name + "  " + getString(R.string.altdia_APC));
		 else if(Warning_Type.equals("VPC"))
			 TV1.setText("警告!! 病患  " + Warning_Name + "  " + getString(R.string.altdia_VPC));
		 TV2.setText(getString(R.string.altdia_sendsms1));
	}
	
	private void setListener(){
		alt_btn_ok.setOnClickListener(btn_ok);
	}
	
	private OnClickListener btn_ok = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Flag = Dialog_Closed;
			sPool.autoPause();
			sPool.release();
			finish();
		}	
	};
	
	private void CDtime(){
		new Thread(){
			@Override
			public void run(){
				
					for(int i = 31;i > -1;i--){
						if(Flag == Dialog_Openning){
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							Message m = new Message();
							m.obj = (Integer)i;
							handler.sendMessage(m);
						}
						else{
							break;
						}
					}
			}
		}.start();
	}
	

	
	private Handler handler = new Handler(){
		 public void handleMessage(Message msg){
			 String CDT = msg.obj.toString();
			 int CDi = Integer.parseInt(CDT);
			 TV3.setText(getString(R.string.altdia_sendsms2) + " [" + CDT + "]");

			 if(CDi%2 == 1){
				 RL1.setBackgroundColor(Color.RED);
			 }
			 else{
				 RL1.setBackgroundColor(Color.DKGRAY);
			 }
			 
			 if(CDi == 0){
				 SmsManager smsManager = SmsManager.getDefault();
				 PendingIntent mPI = PendingIntent.getBroadcast(EAlertDialog.this, 0, new Intent(), 0);
				 if(Warning_Type.equals("IhrHigh")){
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_1, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_ihr_high) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){ 
					 }
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_2, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_ihr_high) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){
					 }
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_3, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_ihr_high) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){
					 }
				 }
				 else if(Warning_Type.equals("IhrLow")){
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_1, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_ihr_low) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_2, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_ihr_low) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_3, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_ihr_low) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
				 }
				 else if(Warning_Type.equals("BTHigh")){
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_1, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_bt_high) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_2, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_bt_high) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_3, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_bt_high) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
				 }
				 else if(Warning_Type.equals("LBBB")){
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_1, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_LBBB) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_2, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_LBBB) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_3, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_LBBB) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
				 }
				 else if(Warning_Type.equals("RBBB")){
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_1, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_RBBB) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_2, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_RBBB) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_3, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_RBBB) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
				 }
				 else if(Warning_Type.equals("VPC")){
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_1, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_VPC) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_2, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_VPC) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_3, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_VPC) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
				 }
				 else if(Warning_Type.equals("APC")){
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_1, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_APC) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_2, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_APC) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
					 
					 try{
						 smsManager.sendTextMessage(Warning_SmsPhone_3, null, getString(R.string.altdia_sms_message1) + "  " + Warning_Name + "  " + getString(R.string.altdia_APC) + "  " + getString(R.string.altdia_sms_message2), mPI, null);
					 }catch(Exception e){}
				 }
				 sPool.release();
				 finish();
			 }
			 
			 if(CDi%5 == 0){
				 WarningSounds(1,0,1);
			 }
			 
		 }
	};

}