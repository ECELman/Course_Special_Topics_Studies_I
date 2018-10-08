package heartbeat.monitor.tablet;

import heartbeat.monitor.tablet.R;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

//病患功能選單
public class Simulate extends Activity {

    // Return Intent extra
    public static String EXTRA_STATE = "extra_state";

    // Member fields
    private ArrayAdapter<String> mOption;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.simulate_list);

        setResult(Activity.RESULT_CANCELED);
        
        mOption = new ArrayAdapter<String>(this, R.layout.simulate_name);

        ListView optionListView = (ListView) findViewById(R.id.options);
        optionListView.setAdapter(mOption);
        optionListView.setOnItemClickListener(mOptionClickListener);

        mOption.add("模擬NORM");
        mOption.add("模擬LBBB");
        mOption.add("模擬RBBB");
        mOption.add("模擬VPC");
        mOption.add("模擬APC");
        mOption.add("關閉模擬");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private OnItemClickListener mOptionClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	
			String info = ((TextView) v).getText().toString();
			    
			if(info=="模擬NORM"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "0");
                setResult(Activity.RESULT_OK, intent);
                finish();	
			}
			else if(info=="模擬LBBB"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "1");
                setResult(Activity.RESULT_OK, intent);
                finish();	
			}
			else if(info=="模擬RBBB"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "2");
                setResult(Activity.RESULT_OK, intent);
                finish();	
			}
			else if(info=="模擬VPC"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "3");
                setResult(Activity.RESULT_OK, intent);
                finish();	
			}
			else if(info=="模擬APC"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "4");
                setResult(Activity.RESULT_OK, intent);
                finish();	
			}
			else if(info=="關閉模擬"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "-1");
                setResult(Activity.RESULT_OK, intent);
                finish();	
			}
        }
    };
}
