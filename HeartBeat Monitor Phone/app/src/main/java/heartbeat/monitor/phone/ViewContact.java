// ViewContact.java
// Activity for viewing a single contact.
package heartbeat.monitor.phone;

import heartbeat.monitor.phone.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class ViewContact extends Activity 
{
   private long rowID; // selected contact's name
   private TextView nameTextView; // displays contact's name 
   private TextView phoneTextView; // displays contact's phone
   private TextView smsphoneTextView1;
   private TextView smsphoneTextView2;
   private TextView smsphoneTextView3;
   private TextView emailTextView; // displays contact's email
   private TextView addressTextView; // displays contact's street
   private TextView noteTextView; // displays contact's city/state/zip

   private Button Button_modify_patient_contact; // 修改病患的資料
   private Button Button_delete_patient_contact; // 刪除病患的資料

   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.view_contact);

      // get the EditTexts
      nameTextView = (TextView) findViewById(R.id.nameTextView);
      phoneTextView = (TextView) findViewById(R.id.phoneTextView);
      smsphoneTextView1 = (TextView) findViewById(R.id.smsphoneTextView1);
      smsphoneTextView2 = (TextView) findViewById(R.id.smsphoneTextView2);
      smsphoneTextView3 = (TextView) findViewById(R.id.smsphoneTextView3);
      emailTextView = (TextView) findViewById(R.id.emailTextView);
      addressTextView = (TextView) findViewById(R.id.addressTextView);
      noteTextView = (TextView) findViewById(R.id.noteTextView);

      // 修改病患資料按鈕，以及按鈕觸發
      Button_modify_patient_contact= (Button) findViewById(R.id.modify_patient_contact);
      Button_modify_patient_contact.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            // create an Intent to launch the AddEditContact Activity
            Intent addEditContact = new Intent(ViewContact.this, AddEditContact.class);

            // pass the selected contact's data as extras with the Intent
            addEditContact.putExtra(Monitor.ROW_ID, rowID);
            addEditContact.putExtra("name", nameTextView.getText());
            addEditContact.putExtra("phone", phoneTextView.getText());
            addEditContact.putExtra("smsphone1", smsphoneTextView1.getText());
            addEditContact.putExtra("smsphone2", smsphoneTextView2.getText());
            addEditContact.putExtra("smsphone3", smsphoneTextView3.getText());
            addEditContact.putExtra("email", emailTextView.getText());
            addEditContact.putExtra("address", addressTextView.getText());
            addEditContact.putExtra("note", noteTextView.getText());
            startActivity(addEditContact); // start the Activity
         }
      });

      // 刪除病患資料按鈕，以及按鈕觸發
      Button_delete_patient_contact = (Button) findViewById(R.id.delete_patient_contact);
      Button_delete_patient_contact.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            deleteContact();
         }
      });
      
      // get the selected contact's row ID
      Bundle extras = getIntent().getExtras();
      rowID = extras.getLong(Monitor.ROW_ID);
   } // end method onCreate

   // called when the activity is first created
   @Override
   protected void onResume()
   {
      super.onResume();
      
      // create new LoadContactTask and execute it 
      new LoadContactTask().execute(rowID);
   } // end method onResume
   
   // performs database query outside GUI thread
   private class LoadContactTask extends AsyncTask<Long, Object, Cursor> 
   {
      DatabaseConnector databaseConnector = 
         new DatabaseConnector(ViewContact.this);

      // perform the database access
      @Override
      protected Cursor doInBackground(Long... params)
      {
         databaseConnector.open();
         
         // get a cursor containing all data on given entry
         return databaseConnector.getOneContact(params[0]);
      } // end method doInBackground

      // use the Cursor returned from the doInBackground method
      @Override
      protected void onPostExecute(Cursor result)
      {
         super.onPostExecute(result);
   
         result.moveToFirst(); // move to the first item 
   
         // get the column index for each data item
         int nameIndex = result.getColumnIndex("name");
         int phoneIndex = result.getColumnIndex("phone");
         int smsphoneIndex1 = result.getColumnIndex("smsphone1");
         int smsphoneIndex2 = result.getColumnIndex("smsphone2");
         int smsphoneIndex3 = result.getColumnIndex("smsphone3");
         int emailIndex = result.getColumnIndex("email");
         int addressIndex = result.getColumnIndex("address");
         int noteIndex = result.getColumnIndex("note");
   
         // fill TextViews with the retrieved data
         nameTextView.setText(result.getString(nameIndex));
         phoneTextView.setText(result.getString(phoneIndex));
         smsphoneTextView1.setText(result.getString(smsphoneIndex1));
         smsphoneTextView2.setText(result.getString(smsphoneIndex2));
         smsphoneTextView3.setText(result.getString(smsphoneIndex3));
         emailTextView.setText(result.getString(emailIndex));
         addressTextView.setText(result.getString(addressIndex));
         noteTextView.setText(result.getString(noteIndex));
   
         result.close(); // close the result cursor
         databaseConnector.close(); // close database connection
      } // end method onPostExecute
   } // end class LoadContactTask
      
   // create the Activity's menu from a menu resource XML file
   @Override
   public boolean onCreateOptionsMenu(Menu menu) 
   {
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.view_contact_menu, menu);
      return true;
   } // end method onCreateOptionsMenu
   
   // handle choice from options menu
   @Override
   public boolean onOptionsItemSelected(MenuItem item) 
   {
       switch (item.getItemId()) // switch based on selected MenuItem's ID
      {
         case R.id.editItem:
            // create an Intent to launch the AddEditContact Activity
            Intent addEditContact =
               new Intent(this, AddEditContact.class);
            
            // pass the selected contact's data as extras with the Intent
            addEditContact.putExtra(Monitor.ROW_ID, rowID);
            addEditContact.putExtra("name", nameTextView.getText());
            addEditContact.putExtra("phone", phoneTextView.getText());
            addEditContact.putExtra("smsphone1", smsphoneTextView1.getText());
            addEditContact.putExtra("smsphone2", smsphoneTextView2.getText());
            addEditContact.putExtra("smsphone3", smsphoneTextView3.getText());
            addEditContact.putExtra("email", emailTextView.getText());
            addEditContact.putExtra("address", addressTextView.getText());
            addEditContact.putExtra("note", noteTextView.getText());
            startActivity(addEditContact); // start the Activity
            return true;
         case R.id.deleteItem:
            deleteContact(); // delete the displayed contact
            return true;
         default:
            return super.onOptionsItemSelected(item);
      } // end switch 
   } // end method onOptionsItemSelected

   // delete a contact
   private void deleteContact()
   {

      // create a new AlertDialog Builder
      AlertDialog.Builder builder = 
         new AlertDialog.Builder(ViewContact.this);

      builder.setTitle(R.string.confirmTitle); // title bar string
      builder.setMessage(R.string.confirmMessage); // message to display

      // provide an OK button that simply dismisses the dialog
      builder.setPositiveButton(R.string.button_delete,
         new DialogInterface.OnClickListener()
         {
            @Override
            public void onClick(DialogInterface dialog, int button)
            {
               final DatabaseConnector databaseConnector = 
                  new DatabaseConnector(ViewContact.this);

               // create an AsyncTask that deletes the contact in another 
               // thread, then calls finish after the deletion
               AsyncTask<Long, Object, Object> deleteTask =
                  new AsyncTask<Long, Object, Object>()
                  {
                     @Override
                     protected Object doInBackground(Long... params)
                     {
                        databaseConnector.deleteContact(params[0]); 
                        return null;
                     } // end method doInBackground

                     @Override
                     protected void onPostExecute(Object result)
                     {
                        finish(); // return to the AddressBook Activity
                     } // end method onPostExecute
                  }; // end new AsyncTask

               // execute the AsyncTask to delete contact at rowID
               deleteTask.execute(new Long[] { rowID });

               // 當刪除病患資料，將其詳細資料的按鈕設為False (還須修改)
               Monitor.Button_inform.setEnabled(false);

               String name = Monitor.textView.getText().toString();

               String login_url = "http://192.168.43.190/ECG/delete_patient_information.php";
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

               login_url = "http://192.168.43.190/ECG/delete_patient_status.php";
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

               login_url = "http://192.168.43.190/ECG/delete_patient_id.php";
               try {
                  URL url = new URL(login_url);
                  HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                  httpURLConnection.setRequestMethod("POST");
                  httpURLConnection.setDoOutput(true);
                  httpURLConnection.setDoInput(true);
                  OutputStream outputStream = httpURLConnection.getOutputStream();
                  BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                  String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8");
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

               login_url = "http://192.168.43.190/ECG/delete_bth_ECG.php";
               try {
                  URL url = new URL(login_url);
                  HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                  httpURLConnection.setRequestMethod("POST");
                  httpURLConnection.setDoOutput(true);
                  httpURLConnection.setDoInput(true);
                  OutputStream outputStream = httpURLConnection.getOutputStream();
                  BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                  String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8");
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

               login_url = "http://192.168.43.190/ECG/delete_bth_heart.php";
               try {
                  URL url = new URL(login_url);
                  HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                  httpURLConnection.setRequestMethod("POST");
                  httpURLConnection.setDoOutput(true);
                  httpURLConnection.setDoInput(true);
                  OutputStream outputStream = httpURLConnection.getOutputStream();
                  BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                  String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8");
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

               login_url = "http://192.168.43.190/ECG/delete_bth_temp.php";
               try {
                  URL url = new URL(login_url);
                  HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                  httpURLConnection.setRequestMethod("POST");
                  httpURLConnection.setDoOutput(true);
                  httpURLConnection.setDoInput(true);
                  OutputStream outputStream = httpURLConnection.getOutputStream();
                  BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                  String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8");
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

               Monitor.textView.setText("請選擇病患");
               Monitor.textView3.setText("尚未連結");
               Monitor.accountView.setText("尚未同步");
               Monitor.Button_simulate.setEnabled(false);
               Monitor.Button_bth.setText("連結裝置");
               Monitor.Button_bth.setEnabled(false);
               Monitor.Button_internet.setText("連結網路");
               Monitor.Button_internet.setEnabled(false);
               Monitor.Button_synch.setText("線上同步");
               Monitor.Button_synch.setEnabled(false);
               Monitor.IHRText.setText("0");
               Monitor.TEText.setText("0.0");
               Monitor.StateText.setText("--");
               Monitor.StateText.setTextColor(Color.parseColor("#000088"));
               Monitor.mChartView.ClearChart();

            } // end method onClick
         } // end anonymous inner class
      ); // end call to method setPositiveButton

      builder.setNegativeButton(R.string.button_cancel, null);
      builder.show(); // display the Dialog
   } // end method deleteContact
} // end class ViewContact


/**************************************************************************
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 **************************************************************************/
