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

public class CreateAccount extends Activity{
	public static String select_id = "select_id";

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
      setTitle(R.string.title_create_account);

      UsernameEditText = (EditText) findViewById(R.id.UsernameEditText);
      PasswordEditText = (EditText) findViewById(R.id.PasswordEditText);
      PinEditText = (EditText) findViewById(R.id.PinEditText);
      
      Bundle extras = getIntent().getExtras();
      select_id = extras.getString(OptionList.select_id);

      Button CreateButton = (Button) findViewById(R.id.CreateButton);
      CreateButton.setOnClickListener(CreateButtonClicked);
   }

   OnClickListener CreateButtonClicked = new OnClickListener() {
      @Override
      public void onClick(View v) {
         if(UsernameEditText.getText().length() != 0 && PasswordEditText.getText().length() != 0 && PinEditText.getText().length() != 0){
        	 Intent intent = new Intent();
             intent.putExtra(USERNAME, UsernameEditText.getText().toString());
             intent.putExtra(PASSWORD, PasswordEditText.getText().toString());
             intent.putExtra(PIN, PinEditText.getText().toString());
             // Set result and finish this Activity
             setResult(Activity.RESULT_OK, intent);
             finish();
         }
         else{
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);

            builder.setTitle(R.string.error_Title2); 
            builder.setMessage(R.string.error_Message4);
            builder.setPositiveButton(R.string.error_Button, null); 
            builder.show();
         }
      }
   };
}