// AddEditContact.java
// Activity for adding a new entry to or  
// editing an existing entry in the address book.
package heartbeat.monitor.phone;

import heartbeat.monitor.phone.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.AlertDialog;
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
   private long rowID; // id of contact being edited, if any
   
   // EditTexts for contact information
   private EditText nameEditText;
   private EditText phoneEditText;
   private EditText smsphoneEditText1;
   private EditText smsphoneEditText2;
   private EditText smsphoneEditText3;
   private EditText emailEditText;
   private EditText addressEditText;
   private EditText noteEditText;
   
   // called when the Activity is first started
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState); // call super's onCreate
      setContentView(R.layout.add_contact); // inflate the UI

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
         rowID = extras.getLong("row_id");
         nameEditText.setText(extras.getString("name"));
         nameEditText.setEnabled(false);
         emailEditText.setText(extras.getString("email"));  
         phoneEditText.setText(extras.getString("phone"));
         smsphoneEditText1.setText(extras.getString("smsphone1"));
         smsphoneEditText2.setText(extras.getString("smsphone2"));
         smsphoneEditText3.setText(extras.getString("smsphone3"));
         addressEditText.setText(extras.getString("address"));  
         noteEditText.setText(extras.getString("note"));  
      } // end if
      
      // set event listener for the Save Contact Button
      Button saveContactButton = (Button) findViewById(R.id.saveContactButton);
      saveContactButton.setOnClickListener(saveContactButtonClicked);
   } // end method onCreate

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
            builder.setTitle(R.string.errorTitle); 
            builder.setMessage(R.string.errorMessage);
            builder.setPositiveButton(R.string.errorButton, null); 
            builder.show(); // display the Dialog
         } // end else
      } // end method onClick
   }; // end OnClickListener saveContactButtonClicked

   // saves contact information to the database
   private void saveContact() 
   {
      // get DatabaseConnector to interact with the SQLite database
      DatabaseConnector databaseConnector = new DatabaseConnector(this);

      if (getIntent().getExtras() == null)
      {
         // insert the contact information into the database
         databaseConnector.insertContact(
            nameEditText.getText().toString(),
            emailEditText.getText().toString(), 
            phoneEditText.getText().toString(),
            smsphoneEditText1.getText().toString(),
            smsphoneEditText2.getText().toString(),
            smsphoneEditText3.getText().toString(),
            addressEditText.getText().toString(),
            noteEditText.getText().toString());

         String name = nameEditText.getText().toString();
         String phone = phoneEditText.getText().toString();
         String phone1 = smsphoneEditText1.getText().toString();
         String phone2 = smsphoneEditText2.getText().toString();
         String phone3 = smsphoneEditText3.getText().toString();
         String e_mail = emailEditText.getText().toString();
         String address = addressEditText.getText().toString();
         String note = noteEditText.getText().toString();

         String login_url = "http://192.168.43.190/ECG/add_patient_information.php";
         try {
            URL url = new URL(login_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8")
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
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
      } // end if
      else
      {
         databaseConnector.updateContact(rowID,
            nameEditText.getText().toString(),
            emailEditText.getText().toString(), 
            phoneEditText.getText().toString(), 
            smsphoneEditText1.getText().toString(),
            smsphoneEditText2.getText().toString(), 
            smsphoneEditText3.getText().toString(), 
            addressEditText.getText().toString(),
            noteEditText.getText().toString());




         String name = nameEditText.getText().toString();
         String phone = phoneEditText.getText().toString();
         String phone1 = smsphoneEditText1.getText().toString();
         String phone2 = smsphoneEditText2.getText().toString();
         String phone3 = smsphoneEditText3.getText().toString();
         String e_mail = emailEditText.getText().toString();
         String address = addressEditText.getText().toString();
         String note = noteEditText.getText().toString();

         String login_url = "http://192.168.43.190/ECG/add_patient_information.php";
         try {
            URL url = new URL(login_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8")
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
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
      } // end else
   } // end class saveContact
} // end class AddEditContact


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
