package heartbeat.monitor.tablet;

import heartbeat.monitor.tablet.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
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

public class AddEditContact extends Activity 
{
   // EditTexts for contact information
   private EditText nameEditText;
   private EditText phoneEditText;
   private EditText smsphoneEditText1;
   private EditText smsphoneEditText2;
   private EditText smsphoneEditText3;
   private EditText emailEditText;
   private EditText addressEditText;
   private EditText noteEditText;
   
   private DBHelper DH = null;
   private int patient_wave_on_num = 0;
   private String select_id;
   private Boolean Add=true;	//判斷現在要新增或是編輯
   
   // called when the Activity is first started
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      
      // 隱藏標題列
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      
      setContentView(R.layout.add_contact);

      nameEditText = (EditText) findViewById(R.id.nameEditText);
      emailEditText = (EditText) findViewById(R.id.emailEditText);
      phoneEditText = (EditText) findViewById(R.id.phoneEditText);
      smsphoneEditText1 = (EditText) findViewById(R.id.smsphoneEditText1);
      smsphoneEditText2 = (EditText) findViewById(R.id.smsphoneEditText2);
      smsphoneEditText3 = (EditText) findViewById(R.id.smsphoneEditText3);
      addressEditText = (EditText) findViewById(R.id.addressEditText);
      noteEditText = (EditText) findViewById(R.id.noteEditText);
      
      Bundle extras = getIntent().getExtras(); // get Bundle of extras

      // if there are extras, use them to populate the EditTexts
      if (extras != null)
      {
    	  try{
    		  patient_wave_on_num = Integer.parseInt(extras.getString("patient_wave_on_num"));
    	  }catch(Exception e){
    		  Add=false;
    		  select_id = extras.getString(OptionList.select_id);
    	      nameEditText.setText(extras.getString("name"));
              nameEditText.setEnabled(false);
    	      emailEditText.setText(extras.getString("email"));  
    	      phoneEditText.setText(extras.getString("phone"));
    	      smsphoneEditText1.setText(extras.getString("sms_phone_1"));
    	      smsphoneEditText2.setText(extras.getString("sms_phone_2"));
    	      smsphoneEditText3.setText(extras.getString("sms_phone_3"));
    	      addressEditText.setText(extras.getString("address"));  
    	      noteEditText.setText(extras.getString("note"));
    	  }
      } // end if
      
      // set event listener for the Save Contact Button
      Button saveContactButton = 
         (Button) findViewById(R.id.saveContactButton);
      saveContactButton.setOnClickListener(saveContactButtonClicked);
   }

   // responds to event generated when user clicks the Done Button
   OnClickListener saveContactButtonClicked = new OnClickListener() 
   {
      @Override
      public void onClick(View v) 
      {
         if (nameEditText.getText().length() != 0)
         {
            AsyncTask<Object, Object, Object> saveContactTask = 
               new AsyncTask<Object, Object, Object>() 
               {
                  @Override
                  protected Object doInBackground(Object... params) 
                  {
                     saveContact(); // save contact to the database
                     return null;
                  } // end method doInBackground
      
                  @Override
                  protected void onPostExecute(Object result) 
                  {
                     finish(); // return to the previous Activity
                  } // end method onPostExecute
               }; // end AsyncTask
               
            // save the contact to the database using a separate thread
            saveContactTask.execute((Object[]) null); 
         } // end if
         else
         {
            // create a new AlertDialog Builder
            AlertDialog.Builder builder = 
               new AlertDialog.Builder(AddEditContact.this);
      
            // set dialog title & message, and provide Button to dismiss
            builder.setTitle(R.string.error_Title); 
            builder.setMessage(R.string.error_Message);
            builder.setPositiveButton(R.string.error_Button, null); 
            builder.show();
         }
      }
   };
   
   private void saveContact(){
	  DH = new DBHelper(this);
 	  SQLiteDatabase db = DH.getWritableDatabase();
 	  ContentValues values = new ContentValues();
 	 
	  values.put("_name", nameEditText.getText().toString());
	  values.put("_email", emailEditText.getText().toString());
	  values.put("_phone", phoneEditText.getText().toString());
	  values.put("_smsphone1", smsphoneEditText1.getText().toString());
	  values.put("_smsphone2", smsphoneEditText2.getText().toString());
	  values.put("_smsphone3", smsphoneEditText3.getText().toString());
	  values.put("_address", addressEditText.getText().toString());
	  values.put("_note", noteEditText.getText().toString());
	  if(Add==true){	//如果要新增，則加入這兩個參數
		  values.put("_wave", 1);
		  values.put("_order", patient_wave_on_num);
	  }

	  if(Add==true)		//如果要新增，則插入一筆資料
		  db.insert("HeartBeat", null, values);
	  else				//如果要修改，則更新指定資料
		  db.update("HeartBeat", values, "_id=" + select_id, null);

       String id = "";
       String name = nameEditText.getText().toString();
       String phone = phoneEditText.getText().toString();
       String phone1 = smsphoneEditText1.getText().toString();
       String phone2 = smsphoneEditText2.getText().toString();
       String phone3 = smsphoneEditText3.getText().toString();
       String e_mail = emailEditText.getText().toString();
       String address = addressEditText.getText().toString();
       String note = noteEditText.getText().toString();

       String login_url = "http://192.168.1.113/add_patient_information_tablet.php";
       try {
           URL url = new URL(login_url);
           HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
           httpURLConnection.setRequestMethod("POST");
           httpURLConnection.setDoOutput(true);
           httpURLConnection.setDoInput(true);
           OutputStream outputStream = httpURLConnection.getOutputStream();
           BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
           String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8")
                   + "&"
                   + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8")
                   + "&"
                   + URLEncoder.encode("phone", "UTF-8") + "=" + URLEncoder.encode(phone, "UTF-8")
                   + "&"
                   + URLEncoder.encode("phone1", "UTF-8") + "=" + URLEncoder.encode(phone1, "UTF-8")
                   + "&"
                   + URLEncoder.encode("phone2", "UTF-8") + "=" + URLEncoder.encode(phone2, "UTF-8")
                   + "&"
                   + URLEncoder.encode("phone3", "UTF-8") + "=" + URLEncoder.encode(phone3, "UTF-8")
                   + "&"
                   + URLEncoder.encode("e_mail", "UTF-8") + "=" + URLEncoder.encode(e_mail, "UTF-8")
                   + "&"
                   + URLEncoder.encode("address", "UTF-8") + "=" + URLEncoder.encode(address, "UTF-8")
                   + "&"
                   + URLEncoder.encode("note", "UTF-8") + "=" + URLEncoder.encode(note, "UTF-8");
           bufferedWriter.write(post_data);
           bufferedWriter.flush();
           bufferedWriter.close();
           outputStream.close();
           InputStream inputStream = httpURLConnection.getInputStream();
           BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
           String result = "", line;
           while((line = bufferedReader.readLine())!=null) result += line;
           bufferedReader.close();
           inputStream.close();
           httpURLConnection.disconnect();
       } catch (MalformedURLException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }

      db.close();
   }
}