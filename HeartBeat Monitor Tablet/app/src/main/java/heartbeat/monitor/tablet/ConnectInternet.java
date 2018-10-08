// AddEditContact.java
// Activity for adding a new entry to or  
// editing an existing entry in the address book.
package heartbeat.monitor.tablet;

import heartbeat.monitor.tablet.R;

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
      setTitle(R.string.title_connect_internet);

      UsernameEditText = (EditText) findViewById(R.id.UsernameEditText);
      PinEditText = (EditText) findViewById(R.id.PinEditText);
      
      Bundle extras = getIntent().getExtras();

      if(extras != null){
         rowID = extras.getLong("row_id");  
      }

      Button SendButton = (Button) findViewById(R.id.LoginButton);
      SendButton.setOnClickListener(SendButtonClicked);
   }

   OnClickListener SendButtonClicked = new OnClickListener() {
      @Override
      public void onClick(View v) {
         if(UsernameEditText.getText().length() != 0 && PinEditText.getText().length() != 0){
        	 Intent intent = new Intent();
             intent.putExtra(USERNAME, UsernameEditText.getText().toString());
             intent.putExtra(PIN, PinEditText.getText().toString());
             // Set result and finish this Activity
             setResult(Activity.RESULT_OK, intent);
             finish();
         }
         else{
            AlertDialog.Builder builder = new AlertDialog.Builder(ConnectInternet.this);

            builder.setTitle(R.string.error_Title2); 
            builder.setMessage(R.string.error_Message3);
            builder.setPositiveButton(R.string.error_Button, null); 
            builder.show();
         }
      }
   };
}