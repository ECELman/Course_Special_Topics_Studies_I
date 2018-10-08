// AddEditContact.java
// Activity for adding a new entry to or  
// editing an existing entry in the address book.
package heartbeat.monitor.phone;

import heartbeat.monitor.phone.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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

public class CreateAccount extends Activity{
   private long rowID;

   private EditText UsernameEditText;
   private EditText PinEditText;
   private EditText PasswordEditText;

   public static String USERNAME = "username";
   public static String PASSWORD = "password";
   public static String PIN = "pin";
   
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
      setContentView(R.layout.create_account);
      setTitle(R.string.create_account);

      UsernameEditText = (EditText) findViewById(R.id.UsernameEditText);
      PasswordEditText = (EditText) findViewById(R.id.PasswordEditText);
      
      Bundle extras = getIntent().getExtras();

      if(extras != null){
         rowID = extras.getLong("row_id");  
      }

      Button CreateButton = (Button) findViewById(R.id.CreateButton);
      CreateButton.setOnClickListener(CreateButtonClicked);
   }

   OnClickListener CreateButtonClicked = new OnClickListener() {
      @Override
      public void onClick(View v) {
         if(UsernameEditText.getText().length() != 0 && PasswordEditText.getText().length() != 0){
        	 /*Intent intent = new Intent();
             intent.putExtra(USERNAME, UsernameEditText.getText().toString());
             intent.putExtra(PASSWORD, PasswordEditText.getText().toString());
             intent.putExtra(PIN, PinEditText.getText().toString());
             // Set result and finish this Activity
             setResult(Activity.RESULT_OK, intent);*/

            String user_name = UsernameEditText.getText().toString();
            String user_password = PasswordEditText.getText().toString();

            /*BackgroundWorker background_worker = new BackgroundWorker();
            background_worker.execute(type, database_username, database_pin);*/

            String login_url = "http://192.168.43.190/ECG/add_account.php";
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
                          + URLEncoder.encode("user_password", "UTF-8") + "=" + URLEncoder.encode(user_password, "UTF-8");
                  bufferedWriter.write(post_data);
                  bufferedWriter.flush();
                  bufferedWriter.close();
                  outputStream.close();
                  InputStream inputStream = httpURLConnection.getInputStream();
                  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                  //String line;
                  while((line = bufferedReader.readLine())!=null) result += line;
                  bufferedReader.close();
                  inputStream.close();
                  httpURLConnection.disconnect();
               } catch (MalformedURLException e) {
                  e.printStackTrace();
               } catch (IOException e) {
                  e.printStackTrace();
               }

            if(result.equals("the account has been created"))
            {
               AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);

               builder.setTitle("ERROR");
               builder.setMessage("此帳號已有人註冊，請更換成別的帳號進行註冊");
               builder.setPositiveButton(R.string.errorButton, null);
               builder.show();
            }
            else finish();
         }
         else{
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);

            builder.setTitle(R.string.errorTitle2); 
            builder.setMessage(R.string.errorMessage4);
            builder.setPositiveButton(R.string.errorButton, null); 
            builder.show();
         }
      }
   };
}