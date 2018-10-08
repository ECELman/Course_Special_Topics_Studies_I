// AddEditContact.java
// Activity for adding a new entry to or  
// editing an existing entry in the address book.
package heartbeat.monitor.phone;

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

import heartbeat.monitor.phone.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;

public class SynchLogin extends Activity{
   private long rowID;

   private EditText usernameEditText;
   private EditText passwordEditText;

   public static String USERNAME = "username";
   public static String PASSWORD = "password";
   public static String PIN = "pin";
   public static String LOGIN = "login";
   
   private static final int REQUEST_CREATE_ACCOUNT = 1;
   
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
      setContentView(R.layout.synch_login);
      setTitle(R.string.synch_login);

      usernameEditText = (EditText) findViewById(R.id.usernameEditText);
      passwordEditText = (EditText) findViewById(R.id.passwordEditText);
      
      /*Bundle extras = getIntent().getExtras();

      if(extras != null){
         rowID = extras.getLong("row_id");  
      }*/

      Button LoginButton = (Button) findViewById(R.id.LoginButton);
      Button NewButton = (Button) findViewById(R.id.NewButton);
      LoginButton.setOnClickListener(LoginButtonClicked);
      NewButton.setOnClickListener(NewButtonClicked);
   }

   OnClickListener LoginButtonClicked = new OnClickListener() {
      @Override
      public void onClick(View v) {
         if(usernameEditText.getText().length() != 0 && passwordEditText.getText().length() != 0){
        	 /*Intent intent = new Intent();
             intent.putExtra(USERNAME, usernameEditText.getText().toString());
             intent.putExtra(PASSWORD, passwordEditText.getText().toString());
             intent.putExtra(LOGIN, "1");
             // Set result and finish this Activity
             setResult(Activity.RESULT_OK, intent);*/

             String user_name = usernameEditText.getText().toString();
             String user_password = passwordEditText.getText().toString();
             String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
             String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
             String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
             String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
             String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
             String heart = Monitor.StateText.getText().equals("--") ? "0" : "60";
             String temp = Monitor.StateText.getText().equals("--") ? "0.0" : "36.0";

             String login_url = "http://192.168.43.190/ECG/add_patient_status.php";
             String result = "", line;
             try {
                 URL url = new URL(login_url);
                 HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                 httpURLConnection.setRequestMethod("POST");
                 httpURLConnection.setDoOutput(true);
                 httpURLConnection.setDoInput(true);
                 OutputStream outputStream = httpURLConnection.getOutputStream();
                 BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                 String post_data = URLEncoder.encode("user_name", "UTF-8") + "=" + URLEncoder.encode(user_name, "UTF-8")
                         + "&"
                         + URLEncoder.encode("user_password", "UTF-8") + "=" + URLEncoder.encode(user_password, "UTF-8")
                         + "&"
                         + URLEncoder.encode("patient_name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8")
                         + "&"
                         + URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(Monitor.StateText.getText().toString(), "UTF-8")
                         + "&"
                         + URLEncoder.encode("patient_heartbeat_number", "UTF-8") + "=" + URLEncoder.encode(heart, "UTF-8")
                         + "&"
                         + URLEncoder.encode("patient_temperature", "UTF-8") + "=" + URLEncoder.encode(temp, "UTF-8")
                         + "&"
                         + URLEncoder.encode("internet_button", "UTF-8") + "=" + URLEncoder.encode(internet_button, "UTF-8")
                         + "&"
                         + URLEncoder.encode("internet_status", "UTF-8") + "=" + URLEncoder.encode(internet_status, "UTF-8")
                         + "&"
                         + URLEncoder.encode("syn_button", "UTF-8") + "=" + URLEncoder.encode(syn_button, "UTF-8")
                         + "&"
                         + URLEncoder.encode("syn_status", "UTF-8") + "=" + URLEncoder.encode(syn_status, "UTF-8")
                         + "&"
                         + URLEncoder.encode("bth_button", "UTF-8") + "=" + URLEncoder.encode(bth_button, "UTF-8");
                 bufferedWriter.write(post_data);
                 bufferedWriter.flush();
                 bufferedWriter.close();
                 outputStream.close();
                 InputStream inputStream = httpURLConnection.getInputStream();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                 while((line = bufferedReader.readLine())!=null) result += line;
                 bufferedReader.close();
                 inputStream.close();
                 httpURLConnection.disconnect();
             } catch (MalformedURLException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }

             if(result.equals("not find the account"))
             {
                 AlertDialog.Builder builder = new AlertDialog.Builder(SynchLogin.this);

                 builder.setTitle("ERROR");
                 builder.setMessage("輸入的帳號或密碼有錯誤，請重新輸入");
                 builder.setPositiveButton(R.string.errorButton, null);
                 builder.show();
             }
             else
             {
                 Monitor.Button_synch.setText("解除同步");
                 Monitor.accountView.setText("已同步");

                 result = "";
                 user_name = usernameEditText.getText().toString();
                 user_password = passwordEditText.getText().toString();
                 internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
                 internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
                 syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
                 syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
                 bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
                 heart = Monitor.StateText.getText().toString().equals("--") ? "0" : "60";
                 temp = Monitor.StateText.getText().toString().equals("--") ? "0.0" : "36.0";

                 try {
                     URL url = new URL(login_url);
                     HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                     httpURLConnection.setRequestMethod("POST");
                     httpURLConnection.setDoOutput(true);
                     httpURLConnection.setDoInput(true);
                     OutputStream outputStream = httpURLConnection.getOutputStream();
                     BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                     String post_data = URLEncoder.encode("user_name", "UTF-8") + "=" + URLEncoder.encode(user_name, "UTF-8")
                             + "&"
                             + URLEncoder.encode("user_password", "UTF-8") + "=" + URLEncoder.encode(user_password, "UTF-8")
                             + "&"
                             + URLEncoder.encode("patient_name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8")
                             + "&"
                             + URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(Monitor.StateText.getText().toString(), "UTF-8")
                             + "&"
                             + URLEncoder.encode("patient_heartbeat_number", "UTF-8") + "=" + URLEncoder.encode(heart, "UTF-8")
                             + "&"
                             + URLEncoder.encode("patient_temperature", "UTF-8") + "=" + URLEncoder.encode(temp, "UTF-8")
                             + "&"
                             + URLEncoder.encode("internet_button", "UTF-8") + "=" + URLEncoder.encode(internet_button, "UTF-8")
                             + "&"
                             + URLEncoder.encode("internet_status", "UTF-8") + "=" + URLEncoder.encode(internet_status, "UTF-8")
                             + "&"
                             + URLEncoder.encode("syn_button", "UTF-8") + "=" + URLEncoder.encode(syn_button, "UTF-8")
                             + "&"
                             + URLEncoder.encode("syn_status", "UTF-8") + "=" + URLEncoder.encode(syn_status, "UTF-8")
                             + "&"
                             + URLEncoder.encode("bth_button", "UTF-8") + "=" + URLEncoder.encode(bth_button, "UTF-8");
                     bufferedWriter.write(post_data);
                     bufferedWriter.flush();
                     bufferedWriter.close();
                     outputStream.close();
                     InputStream inputStream = httpURLConnection.getInputStream();
                     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                     while((line = bufferedReader.readLine())!=null) result += line;
                     bufferedReader.close();
                     inputStream.close();
                     httpURLConnection.disconnect();
                 } catch (MalformedURLException e) {
                     e.printStackTrace();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 finish();
             }
         }
         else{
            AlertDialog.Builder builder = new AlertDialog.Builder(SynchLogin.this);

            builder.setTitle(R.string.errorTitle2); 
            builder.setMessage(R.string.errorMessage2);
            builder.setPositiveButton(R.string.errorButton, null); 
            builder.show();
         }
      }
   };
   
   OnClickListener NewButtonClicked = new OnClickListener() {
	      @Override
	      public void onClick(View v) {
              Intent create = new Intent(SynchLogin.this, CreateAccount.class);
              startActivityForResult(create, REQUEST_CREATE_ACCOUNT);
	      }
	};
	
	/*public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
        case REQUEST_CREATE_ACCOUNT:
            if (resultCode == Activity.RESULT_OK) {

                String username = data.getExtras().getString(CreateAccount.USERNAME);
                String password = data.getExtras().getString(CreateAccount.PASSWORD);
                String pin = data.getExtras().getString(CreateAccount.PIN);
                
	           	 Intent intent = new Intent();
	             intent.putExtra(USERNAME, username);
	             intent.putExtra(PASSWORD, password);
	             intent.putExtra(PIN, pin);
	             intent.putExtra(LOGIN, "0");

	             setResult(Activity.RESULT_OK, intent);
             finish();
            }
            break;
        }
    }*/
}