package heartbeat.monitor.tablet;

import heartbeat.monitor.tablet.ConnectInternet;
import heartbeat.monitor.tablet.SynchLogin;
import heartbeat.monitor.tablet.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

//病患功能選單
public class OptionList extends Activity {

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_CONNECT_BTH = "connect_bth";
    public static String EXTRA_CONNECT_INTERNET = "connect_internet";
    public static String EXTRA_SYNCH_USERNAME = "synch_username";
    public static String EXTRA_SYNCH_PASSWORD = "synch_password";
    public static String EXTRA_SYNCH_PIN = "synch_pin";
    public static String EXTRA_SYNCH_LOGIN = "synch_login";
    public static String EXTRA_CONNECT_USERNAME = "connect_username";
    public static String EXTRA_CONNECT_PIN = "connect_pin";
    public static String EXTRA_SIMULATE_STATE = "simulate_state";
    public static String EXTRA_STATE = "state";

    // Member fields
    private ArrayAdapter<String> mOption;

    public static String select_id = "select_id";						//欲使用功能的病患ID
	public static String select_name = "select_name";
    public static String select_bth_address = "select_bth_address";
    public static String select_server_id = "select_server_id";
    public static String select_connect_name = "select_connect_name";
    public static String select_simulate_state = "select_simulate_state";
    private DBHelper DH = null;
    private int patient_num;
    
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_SYNCH = 2;
    private static final int REQUEST_CONNECT_INTERNET = 3;
    private static final int REQUEST_SIMULATE_STATE = 4;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定此view 的window 外觀
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.option_list);
        // 預設回傳結果為RESULT_CANCELED，若使用者輸入回上層，則表示並無作任何動作
        setResult(Activity.RESULT_CANCELED);

        Bundle extras = getIntent().getExtras();
        select_id = extras.getString(Main.select_id);
		select_name = extras.getString(Main.select_name);
        select_bth_address = extras.getString(Main.select_bth_address);
        select_server_id = extras.getString(Main.select_server_id);
        select_connect_name = extras.getString(Main.select_connect_name);
        select_simulate_state = extras.getString(Main.select_simulate_state);
        
        int simulate_state = Integer.parseInt(select_simulate_state);
        int server_id = Integer.parseInt(select_server_id);
        
        mOption = new ArrayAdapter<String>(this, R.layout.option_name);

        ListView optionListView = (ListView) findViewById(R.id.options);
        optionListView.setAdapter(mOption);
        optionListView.setOnItemClickListener(mOptionClickListener);

		String name = select_name;

		String login_url = "http://192.168.1.113/find_patient.php";
		String result = "", line;
		try {
			URL url = new URL(login_url);
			HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			OutputStream outputStream = httpURLConnection.getOutputStream();
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
			String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
			bufferedWriter.write(post_data);
			bufferedWriter.flush();
			bufferedWriter.close();
			outputStream.close();
			InputStream inputStream = httpURLConnection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			while((line = bufferedReader.readLine())!=null) result += line;
			bufferedReader.close();
			inputStream.close();
			httpURLConnection.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(result.equals("exist"))
		{
			mOption.add("刪除病患");
		}
		else
		{
			if(select_bth_address==null && select_connect_name==null)
				mOption.add("藍牙連線");
			if(select_bth_address!=null)
				mOption.add("中斷藍牙連線");
			if(server_id==-1 && (simulate_state!=-1 || select_bth_address!=null))
				mOption.add("線上同步");
			if(server_id>=0)
				mOption.add("解除同步");
			if(select_connect_name==null && select_bth_address==null)
				mOption.add("網路連線");
			if(select_connect_name!=null)
				mOption.add("中斷網路連線");
			mOption.add("模擬訊號");
			mOption.add("詳細資料");
			mOption.add("編輯資料");
			mOption.add("刪除病患");
		}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    //監視點擊選項
    private OnItemClickListener mOptionClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	
			String info = ((TextView) v).getText().toString();
			    
			if(info=="藍牙連線"){
	            Intent BthConnect = new Intent(OptionList.this, DeviceListActivity.class);
	            BthConnect.putExtra(select_id, select_id);
                startActivityForResult(BthConnect, REQUEST_CONNECT_DEVICE);
			}
			else if(info=="中斷藍牙連線"){
                Intent BthConnect = new Intent();
                BthConnect.putExtra(EXTRA_DEVICE_ADDRESS, select_bth_address);
                BthConnect.putExtra(EXTRA_CONNECT_BTH, "0");
                BthConnect.putExtra(EXTRA_SYNCH_LOGIN, "-1");
                BthConnect.putExtra(EXTRA_CONNECT_INTERNET, "-1");
                BthConnect.putExtra(EXTRA_SIMULATE_STATE, "-1");
                setResult(Activity.RESULT_OK, BthConnect);
                finish();				
			}
			else if(info=="線上同步"){
	            Intent Synch = new Intent(OptionList.this, SynchLogin.class);
	            Synch.putExtra(select_id, select_id);
                startActivityForResult(Synch, REQUEST_SYNCH);				
			}
			else if(info=="解除同步"){
                Intent Synch = new Intent();
                Synch.putExtra(EXTRA_CONNECT_BTH, "-1");
                Synch.putExtra(EXTRA_SYNCH_LOGIN, "0");
                Synch.putExtra(EXTRA_CONNECT_INTERNET, "-1");
                Synch.putExtra(EXTRA_SIMULATE_STATE, "-1");
                setResult(Activity.RESULT_OK, Synch);
                finish();
			}
			else if(info=="網路連線"){
	            Intent Synch = new Intent(OptionList.this, ConnectInternet.class);
	            Synch.putExtra(select_id, select_id);
                startActivityForResult(Synch, REQUEST_CONNECT_INTERNET);				
			}
			else if(info=="中斷網路連線"){
	        	Intent InternetConnect = new Intent();
	        	InternetConnect.putExtra(EXTRA_SYNCH_LOGIN, "-1");
	        	InternetConnect.putExtra(EXTRA_CONNECT_INTERNET, "0");
	        	InternetConnect.putExtra(EXTRA_CONNECT_BTH, "-1");
	        	InternetConnect.putExtra(EXTRA_SIMULATE_STATE, "-1");
                setResult(Activity.RESULT_OK, InternetConnect);
                finish();
			}
			else if(info=="模擬訊號"){
	            Intent Simulate = new Intent(OptionList.this, Simulate.class);
                startActivityForResult(Simulate, REQUEST_SIMULATE_STATE);
			}
			else{
				if(info=="詳細資料"){
					Intent viewContact = new Intent(OptionList.this, ViewContact.class);
					viewContact.putExtra(select_id, select_id);
					startActivity(viewContact);
				}
				else if(info=="編輯資料"){
					
					Intent addEditContact = new Intent(OptionList.this, AddEditContact.class);
					addEditContact.putExtra(select_id, select_id);
					//搜尋資料庫
		            Cursor cursor;
		            DH = new DBHelper(OptionList.this);
		            SQLiteDatabase db = DH.getWritableDatabase();
		            cursor = db.query("HeartBeat", null, null, null, null, null, null);
		            patient_num = cursor.getCount();
		            
		      		if(patient_num != 0) {
		      			cursor.moveToFirst();
		      			for(int i=0; i<patient_num; i++) {	
		      				if(Integer.parseInt(select_id)==cursor.getInt(0)){
		      					addEditContact.putExtra("name", cursor.getString(1));
		      					addEditContact.putExtra("email", cursor.getString(2));
		      			        addEditContact.putExtra("phone", cursor.getString(3));
		      			        addEditContact.putExtra("sms_phone_1", cursor.getString(4));
			      			    addEditContact.putExtra("sms_phone_2", cursor.getString(5));
			      			    addEditContact.putExtra("sms_phone_3", cursor.getString(6));
		      			        addEditContact.putExtra("address", cursor.getString(7));
		      			        addEditContact.putExtra("note", cursor.getString(8));
		      				    break;
		      				}
		      				cursor.moveToNext();
		      			}
		      		}
		      		
		      		cursor.close();
		      		db.close();
	
		            startActivity(addEditContact); 
				}
				else if(info=="刪除病患"){
		            DH = new DBHelper(OptionList.this);
		            SQLiteDatabase db = DH.getWritableDatabase();
		            
		      		db.delete("HeartBeat", "_id=" + select_id, null);
		      		db.close();

					String name = select_name;

					String login_url = "http://192.168.1.113/delete_information_tablet.php";
					String result = "", line;
					try {
						URL url = new URL(login_url);
						HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
						httpURLConnection.setRequestMethod("POST");
						httpURLConnection.setDoOutput(true);
						httpURLConnection.setDoInput(true);
						OutputStream outputStream = httpURLConnection.getOutputStream();
						BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
						String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
						bufferedWriter.write(post_data);
						bufferedWriter.flush();
						bufferedWriter.close();
						outputStream.close();
						InputStream inputStream = httpURLConnection.getInputStream();
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						while((line = bufferedReader.readLine())!=null) result += line;
						bufferedReader.close();
						inputStream.close();
						httpURLConnection.disconnect();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					login_url = "http://192.168.1.113/delete_patient_status_tablet.php";
					result = "";
					try {
						URL url = new URL(login_url);
						HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
						httpURLConnection.setRequestMethod("POST");
						httpURLConnection.setDoOutput(true);
						httpURLConnection.setDoInput(true);
						OutputStream outputStream = httpURLConnection.getOutputStream();
						BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
						String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
						bufferedWriter.write(post_data);
						bufferedWriter.flush();
						bufferedWriter.close();
						outputStream.close();
						InputStream inputStream = httpURLConnection.getInputStream();
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						while((line = bufferedReader.readLine())!=null) result += line;
						bufferedReader.close();
						inputStream.close();
						httpURLConnection.disconnect();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
                Intent intent = new Intent();
                /*intent.putExtra(select_id, select_id);
                intent.putExtra(select_name, select_name);*/
                setResult(Activity.RESULT_CANCELED, intent);
				finish();
			}
        }
    };
    
    //接收startActivityForResult的回傳資料
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            if (resultCode == Activity.RESULT_OK){
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                Intent BthConnect = new Intent();
                BthConnect.putExtra(EXTRA_DEVICE_ADDRESS, address);
                BthConnect.putExtra(EXTRA_CONNECT_BTH, "1");
                BthConnect.putExtra(EXTRA_SYNCH_LOGIN, "-1");
                BthConnect.putExtra(EXTRA_CONNECT_INTERNET, "-1");
                BthConnect.putExtra(EXTRA_SIMULATE_STATE, "-1");
                setResult(Activity.RESULT_OK, BthConnect);
                finish();
            }
            break;
        
        case REQUEST_SYNCH:
            if (resultCode == Activity.RESULT_OK){
            	String login = data.getExtras().getString(SynchLogin.LOGIN);
            	if(login.equals("1")){
            		String username = data.getExtras().getString(SynchLogin.USERNAME);
		        	String password = data.getExtras().getString(SynchLogin.PASSWORD);
		        	Intent Synch = new Intent();
		        	Synch.putExtra(EXTRA_SYNCH_USERNAME, username);
		        	Synch.putExtra(EXTRA_SYNCH_PASSWORD, password);
		        	Synch.putExtra(EXTRA_SYNCH_LOGIN, login);
		        	Synch.putExtra(EXTRA_CONNECT_INTERNET, "-1");
		        	Synch.putExtra(EXTRA_CONNECT_BTH, "-1");
		        	Synch.putExtra(EXTRA_SIMULATE_STATE, "-1");
	                setResult(Activity.RESULT_OK, Synch);
	                finish();
	            }
            	else{
            		String username = data.getExtras().getString(SynchLogin.USERNAME);
		        	String password = data.getExtras().getString(SynchLogin.PASSWORD);
		        	String pin = data.getExtras().getString(SynchLogin.PIN);
		        	Intent Synch = new Intent();
		        	Synch.putExtra(EXTRA_SYNCH_USERNAME, username);
		        	Synch.putExtra(EXTRA_SYNCH_PASSWORD, password);
		        	Synch.putExtra(EXTRA_SYNCH_PIN, pin);
		        	Synch.putExtra(EXTRA_SYNCH_LOGIN, login);
		        	Synch.putExtra(EXTRA_CONNECT_INTERNET, "-1");
		        	Synch.putExtra(EXTRA_CONNECT_BTH, "-1");
		        	Synch.putExtra(EXTRA_SIMULATE_STATE, "-1");
	                setResult(Activity.RESULT_OK, Synch);
	                finish();
            	}
            }        	
        	break;
        case REQUEST_CONNECT_INTERNET:
            if (resultCode == Activity.RESULT_OK){
	        	String username = data.getExtras().getString(ConnectInternet.USERNAME);
	        	String pin = data.getExtras().getString(ConnectInternet.PIN);
	        	Intent InternetConnect = new Intent();
	        	InternetConnect.putExtra(EXTRA_CONNECT_USERNAME, username);
	        	InternetConnect.putExtra(EXTRA_CONNECT_PIN, pin);
	        	InternetConnect.putExtra(EXTRA_SYNCH_LOGIN, "-1");
	        	InternetConnect.putExtra(EXTRA_CONNECT_INTERNET, "1");
	        	InternetConnect.putExtra(EXTRA_CONNECT_BTH, "-1");
	        	InternetConnect.putExtra(EXTRA_SIMULATE_STATE, "-1");
                setResult(Activity.RESULT_OK, InternetConnect);
                finish();
            }        	
        	break;
    	case REQUEST_SIMULATE_STATE:
	        if (resultCode == Activity.RESULT_OK){
	        	String state = data.getExtras().getString(Simulate.EXTRA_STATE);
	        	Intent SimulateState = new Intent();
	        	SimulateState.putExtra(EXTRA_STATE, state);
	        	SimulateState.putExtra(EXTRA_SYNCH_LOGIN, "-1");
	        	SimulateState.putExtra(EXTRA_CONNECT_INTERNET, "-1");
	        	SimulateState.putExtra(EXTRA_CONNECT_BTH, "-1");
	        	SimulateState.putExtra(EXTRA_SIMULATE_STATE, "1");
	            setResult(Activity.RESULT_OK, SimulateState);
	            finish();
	        }        	
	    	break;
        }
    }
}
