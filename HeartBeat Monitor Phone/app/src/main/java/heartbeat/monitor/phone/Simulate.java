package heartbeat.monitor.phone;

import heartbeat.monitor.phone.R;

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

            if(Monitor.Button_internet.getText().toString().equals("斷開連線")) Monitor.Button_synch.setEnabled(true);
            else Monitor.Button_synch.setEnabled(false);

            String info = ((TextView) v).getText().toString();

            if(info=="模擬NORM"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "0");
                setResult(Activity.RESULT_OK, intent);
                refresh("NORM");
                finish();
            }
            else if(info=="模擬LBBB"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "1");
                setResult(Activity.RESULT_OK, intent);
                refresh("LBBB");
                finish();
            }
            else if(info=="模擬RBBB"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "2");
                setResult(Activity.RESULT_OK, intent);
                refresh("RBBB");
                finish();
            }
            else if(info=="模擬VPC"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "3");
                setResult(Activity.RESULT_OK, intent);
                refresh("VPC");
                finish();
            }
            else if(info=="模擬APC"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "4");
                setResult(Activity.RESULT_OK, intent);
                refresh("APC");
                finish();
            }
            else if(info=="關閉模擬"){
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, "-1");
                setResult(Activity.RESULT_OK, intent);
                Monitor.Button_synch.setEnabled(false);
                refresh("--");
                finish();
            }
        }
    };

    public void restart_simulate(String info)
    {
        if(info=="NORM"){
            Intent intent = new Intent();
            intent.putExtra(EXTRA_STATE, "0");
            setResult(Activity.RESULT_OK, intent);
            refresh("NORM");
            finish();
        }
        else if(info=="LBBB"){
            Intent intent = new Intent();
            intent.putExtra(EXTRA_STATE, "1");
            setResult(Activity.RESULT_OK, intent);
            refresh("LBBB");
            finish();
        }
        else if(info=="RBBB"){
            Intent intent = new Intent();
            intent.putExtra(EXTRA_STATE, "2");
            setResult(Activity.RESULT_OK, intent);
            refresh("RBBB");
            finish();
        }
        else if(info=="VPC"){
            Intent intent = new Intent();
            intent.putExtra(EXTRA_STATE, "3");
            setResult(Activity.RESULT_OK, intent);
            refresh("VPC");
            finish();
        }
        else if(info=="APC"){
            Intent intent = new Intent();
            intent.putExtra(EXTRA_STATE, "4");
            setResult(Activity.RESULT_OK, intent);
            refresh("APC");
            finish();
        }
        else if(info=="--"){
            Intent intent = new Intent();
            intent.putExtra(EXTRA_STATE, "-1");
            setResult(Activity.RESULT_OK, intent);
            Monitor.Button_synch.setEnabled(false);
            refresh("--");
            finish();
        }
    }

    protected void refresh(String status)
    {
        String user_name = "111";
        String user_password = "000";

        String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
        String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
        String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
        String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
        String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
        String heart = status.equals("--") ? "0" : "60";
        String temp = status.equals("--") ? "0.0" : "36.0";

        String login_url = "http://192.168.43.190/ECG/add_patient_status.php";
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
                    + URLEncoder.encode("user_password", "UTF-8") + "=" + URLEncoder.encode(user_password, "UTF-8")
                    + "&"
                    + URLEncoder.encode("patient_name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8")
                    + "&"
                    + URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(status, "UTF-8")
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
    }
}