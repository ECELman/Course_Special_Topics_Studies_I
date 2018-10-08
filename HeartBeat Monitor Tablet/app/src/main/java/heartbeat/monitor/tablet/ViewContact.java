// ViewContact.java
// Activity for viewing a single contact.
package heartbeat.monitor.tablet;

import heartbeat.monitor.tablet.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class ViewContact extends Activity 
{
   private String select_id;
   private TextView nameTextView;
   private TextView phoneTextView;
   private TextView smsphoneTextView1;
   private TextView smsphoneTextView2;
   private TextView smsphoneTextView3;
   private TextView emailTextView;
   private TextView addressTextView;
   private TextView noteTextView;

   private DBHelper DH = null;
   private int patient_num;
   
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      
      // 隱藏標題列
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      
      setContentView(R.layout.view_contact);

      //取得Text名稱
      nameTextView = (TextView) findViewById(R.id.nameTextView);
      phoneTextView = (TextView) findViewById(R.id.phoneTextView);
      smsphoneTextView1 = (TextView) findViewById(R.id.smsphoneTextView1);
      smsphoneTextView2 = (TextView) findViewById(R.id.smsphoneTextView2);
      smsphoneTextView3 = (TextView) findViewById(R.id.smsphoneTextView3);
      emailTextView = (TextView) findViewById(R.id.emailTextView);
      addressTextView = (TextView) findViewById(R.id.addressTextView);
      noteTextView = (TextView) findViewById(R.id.noteTextView);
      
      //取得修改病人名稱
      Bundle extras = getIntent().getExtras();
      select_id = extras.getString(OptionList.select_id);
      
      Cursor cursor;
      DH = new DBHelper(this);
      SQLiteDatabase db = DH.getWritableDatabase();
      cursor = db.query("HeartBeat", null, null, null, null, null, null);
      patient_num = cursor.getCount();
      
		if(patient_num != 0) {
			cursor.moveToFirst();
			for(int i=0; i<patient_num; i++) {	
				if(Integer.parseInt(select_id)==cursor.getInt(0)){
				      nameTextView.setText(cursor.getString(1));
				      phoneTextView.setText(cursor.getString(3));
				      smsphoneTextView1.setText(cursor.getString(4));
				      smsphoneTextView2.setText(cursor.getString(5));
				      smsphoneTextView3.setText(cursor.getString(6));
				      emailTextView.setText(cursor.getString(2));
				      addressTextView.setText(cursor.getString(7));
				      noteTextView.setText(cursor.getString(8));
				      break;
				}
				cursor.moveToNext();
			}
		}
		
		cursor.close();
		db.close();
   }
}