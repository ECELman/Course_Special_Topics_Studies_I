// AddEditContact.java
// Activity for adding a new entry to or  
// editing an existing entry in the address book.
package heartbeat.monitor.tablet;

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

import heartbeat.monitor.tablet.R;

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
import android.widget.Toast;

public class SynchLogin extends Activity{
	
	public static String select_id = "select_id";

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
		setTitle(R.string.title_synch_login);

		usernameEditText = (EditText) findViewById(R.id.usernameEditText);
		passwordEditText = (EditText) findViewById(R.id.passwordEditText);

		Bundle extras = getIntent().getExtras();
		select_id = extras.getString(OptionList.select_id);

		Button LoginButton = (Button) findViewById(R.id.LoginButton);
		Button NewButton = (Button) findViewById(R.id.NewButton);
		LoginButton.setOnClickListener(LoginButtonClicked);
		NewButton.setOnClickListener(NewButtonClicked);
	}

	OnClickListener LoginButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(usernameEditText.getText().length() != 0 && passwordEditText.getText().length() != 0){
				Intent intent = new Intent();
				intent.putExtra(USERNAME, usernameEditText.getText().toString());
	            intent.putExtra(PASSWORD, passwordEditText.getText().toString());
	            intent.putExtra(LOGIN, "1");
	            // Set result and finish this Activity
	            setResult(Activity.RESULT_OK, intent);

				finish();
	        }
	        else{
	            AlertDialog.Builder builder = new AlertDialog.Builder(SynchLogin.this);
	
	            builder.setTitle(R.string.error_Title2); 
	            builder.setMessage(R.string.error_Message2);
	            builder.setPositiveButton(R.string.error_Button, null); 
	            builder.show();
         	}
		}
	};
   
	OnClickListener NewButtonClicked = new OnClickListener() {
		@Override
	    public void onClick(View v) {
			Intent create = new Intent(SynchLogin.this, CreateAccount.class);
			create.putExtra(select_id, select_id);
            startActivityForResult(create, REQUEST_CREATE_ACCOUNT);
	    }
	};
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
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
	             intent.putExtra(LOGIN, "2");

	             setResult(Activity.RESULT_OK, intent);
             finish();
            }
            break;
        }
    }
}