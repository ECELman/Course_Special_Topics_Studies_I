// AddEditContact.java
// Activity for adding a new entry to or  
// editing an existing entry in the address book.
package heartbeat.monitor.phone;

import heartbeat.monitor.phone.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.EditText;

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

public class ConnectInternet extends Activity{
   private long rowID;

   private EditText UsernameEditText;
   private EditText PinEditText;

   public static String USERNAME = "username";
   public static String PIN = "pin";
   
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);

      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
      setContentView(R.layout.connect_internet);
      setTitle(R.string.connect_internet);

      UsernameEditText = (EditText) findViewById(R.id.UsernameEditText);
      PinEditText = (EditText) findViewById(R.id.PinEditText);
      
      /*Bundle extras = getIntent().getExtras();

      if(extras != null){
         rowID = extras.getLong("row_id");  
      }*/

      Button SendButton = (Button) findViewById(R.id.LoginButton);
      SendButton.setOnClickListener(SendButtonClicked);
   }

   OnClickListener SendButtonClicked = new OnClickListener() {
      @Override
      public void onClick(View v) {
         if(UsernameEditText.getText().length() != 0 && PinEditText.getText().length() != 0){
        	 /*Intent intent = new Intent();
             intent.putExtra(USERNAME, UsernameEditText.getText().toString());
             intent.putExtra(PIN, PinEditText.getText().toString());
             // Set result and finish this Activity
             setResult(Activity.RESULT_OK, intent);*/

            String database_username = UsernameEditText.getText().toString();
            String database_pin = PinEditText.getText().toString();
            String type = "login";

            /*BackgroundWorker background_worker = new BackgroundWorker();
            background_worker.execute(type, database_username, database_pin);*/

            String login_url = "http://192.168.43.190/ECG/login.php";
            String result = "", line;
            if(type.equals("login"))
            {
               try {
                  URL url = new URL(login_url);
                  HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                  httpURLConnection.setRequestMethod("POST");
                  httpURLConnection.setDoOutput(true);
                  httpURLConnection.setDoInput(true);
                  OutputStream outputStream = httpURLConnection.getOutputStream();
                  BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                  String post_data = URLEncoder.encode("user_name", "UTF-8") + "=" + URLEncoder.encode(database_username, "UTF-8")
                          + "&"
                          + URLEncoder.encode("user_password", "UTF-8") + "=" + URLEncoder.encode(database_pin, "UTF-8");
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
            }

            if(result.equals("login successed"))
            {
               Monitor.Button_internet.setText("斷開連線");
               Monitor.Button_internet.setEnabled(true);

               if(Monitor.Button_bth.getText().toString().equals("解除裝置")) Monitor.textView3.setText("已連結到 8Z027");
                else Monitor.textView3.setText(result);

               if(!Monitor.StateText.getText().toString().equals("--")) Monitor.Button_synch.setEnabled(true);

                result = "";
                String user_name = "111";
                String user_password = "000";
                String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
                String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
                String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
                String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
                String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
                String heart = Monitor.StateText.getText().toString().equals("--") ? "0" : "60";
                String temp = Monitor.StateText.getText().toString().equals("--") ? "0.0" : "36.0";

                login_url = "http://192.168.43.190/ECG/add_patient_status.php";
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
            else
            {
               AlertDialog.Builder builder = new AlertDialog.Builder(ConnectInternet.this);

               builder.setTitle("ERROR");
               builder.setMessage("帳號或密碼有錯誤");
               builder.setPositiveButton(R.string.errorButton, null);
               builder.show();
            }
         }
         else{
            AlertDialog.Builder builder = new AlertDialog.Builder(ConnectInternet.this);

            builder.setTitle(R.string.errorTitle2); 
            builder.setMessage(R.string.errorMessage3);
            builder.setPositiveButton(R.string.errorButton, null); 
            builder.show();
         }

         /*String database_username = UsernameEditText.getText().toString();
         String database_pin = PinEditText.getText().toString();
         String type = "login";

         BackgroundWorker background_worker = new BackgroundWorker();
         background_worker.execute(type, database_username, database_pin);*/
      }
   };
}