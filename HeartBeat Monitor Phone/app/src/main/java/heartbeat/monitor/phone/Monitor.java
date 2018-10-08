package heartbeat.monitor.phone;
/*
 *  KY LAB program source for development
 *
 *	Ver: KY202.1.0
 *	Data : 2010/06/01
 *	Designer : Weiting Lin
 *	
 *	function description:
 *	Main activity handle everything.
 *	show chart,ECG Info and control button.
 */


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import heartbeat.monitor.phone.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import android.app.ListActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class Monitor extends ListActivity
{
	private int test = 0;

	private int delay=0;
	private int offset=0;

	private static final String SERVER_IP = "192.168.43.190/ECG05";
	private Boolean SERVER_CONNECTED = true;

	// Debugging
	private static final String TAG = "KY202Monitor";
	private static final boolean D = true;

	// Message types sent from the BluetoothService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_LOST = 6;
	public static final int MESSAGE_TEST = 7;
	public static final int MESSAGE_DISCONNECT = 8;
	public static final int MESSAGE_PATIENT = 9;
	public static final int MESSAGE_STATE = 10;

	// Message types sent from ECGService Handler
	public static final int MESSAGE_RAW = 1;
	public static final int MESSAGE_INFO = 2;
	public static final int MESSAGE_KY_STATE = 3;
	public static final int MESSAGE_WARN = 4;
	public static final int MESSAGE_UPDATE = 5;

	public static final int MESSAGE_NEW = 1;
	public static final int MESSAGE_LOGIN = 2;

	// Key names received from the ECGService Handler
	public static final String KY_INFO = "KY_Info";

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String Patient_Id = "patient_id";
	public static final String TOAST = "toast";
	public static final String Test_String = "test_string";
	public static final String DEVICE_ADDRESS = "device_address";
	public static final String PATIENT_DATA = "patient_data";
	public static final String WARN_TYPE = "warn_type";
	public static final String VALUE = "value";
	public static final String VALUE2 = "value2";
	public static final String STATE = "state";
	public static final String UPDATE_STRING = "update_string";

	public static final int NORM = 0;
	public static final int LBBB = 1;
	public static final int RBBB = 2;
	public static final int VPC = 3;
	public static final int APC = 4;


	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_LOG_IN = 3;
	private static final int REQUEST_CONNECT_INTERNET = 4;
	private static final int REQUEST_SIMULATE = 5;

	//警告狀態
	private static final int Enable_Warning = 0;
	private static final int Disable_Warning = 1;

	//警告旗標
	private int[] WarnFlag = new int[1024];

	//警告類型
	public static final String S_IhrTooHigh = "IhrHigh";
	public static final String S_IhrTooLow = "IhrLow";
	public static final String S_BodyTempTooHigh = "BTHigh";
	public static final String S_LBBB = "LBBB";
	public static final String S_RBBB = "RBBB";
	public static final String S_VPC = "VPC";
	public static final String S_APC = "APC";

	// Layout Views
	private TextView mTitle;
	public static ChartView mChartView; 						//圖形View
	public static Button Button_inform;						//病患資訊按鈕，需修改，需修改
	public static Button Button_bth;							//藍芽連線按鈕，需修改
	public static Button Button_synch;						//線上同步按鈕
	public static Button Button_internet;						//線上連線按鈕，需修改
	public static Button Button_simulate;						//模擬訊號按鈕，需修改
	private Button Button_add_patient;                  //新增病患按鈕
	private Button Button_hospital_map;                 //醫院地圖按鈕
	public static TextView IHRText;							//IHR 顯示欄位，需修改
	public static TextView TEText;							//TEP 顯示欄位，需修改
	public static TextView StateText;							//State 顯示欄位

	//儲存上次和這次的警告時間
	private long [] TBefore = new long[1024];
	private long [] TAfter = new long[1024];

	private String part1;								//溫度顯示使用之暫存參數z
	private String tempHF = "";
	private String tempLF = "";
	public static String filename = "data.ini";
	public static String contactPhone_key = "contactPhone";
	public static String userAge_key = "userAge";
	private String Server_key = "ServerIP";
	private String contactPhone_value = "";
	private String userAge_value = "";
	private String Server_value = "1.172.115.36";

	//狀態計數器，累積一定次數後發出警告
	private int [] IhrHighStateCount = new int[1024];
	private int [] IhrLowStateCount = new int[1024];
	private int [] TeHighStateCount = new int[1024];
	private int [] LBBBStateCount = new int[1024];
	private int [] RBBBStateCount = new int[1024];
	private int [] VPCStateCount = new int[1024];
	private int [] APCStateCount = new int[1024];

	private String data2 = "\r\n";
	private String sentbuf = "";
	private int count=0;
	final float PROPORTION = 1.0f;

	public static String SERVER_STRING = null;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	//
	private String mConnectedPatientId = null;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothService mBTService = null;

	//KY Serires Service Object
	private ECGService mECGService = null;

	public static String ROW_ID = "row_id"; // Intent extra key
	private ListView contactListView; // the ListActivity's ListView
	private CursorAdapter contactAdapter; // adapter for ListView

	public static TextView textView; // 需修改
	public static TextView textView3; // 需修改
	public static TextView accountView; // 需修改
	private CharSequence mText = "請選擇病患";
	private CharSequence mText2 = "尚未連結";
	private CharSequence mText3;
	private CharSequence mText4 = "尚未同步";

	private long choose = -1;

	private int connected_num = 0;
	private long [] connected_id = new long[1024];
	private String [] device_name = new String[1024];
	private String [] device_address = new String[1024];

	private String [] account_name = new String[1024];
	private int [] server_id = new int[1024];
	private String [] connect_name = new String[1024];
	private int [] simulate_state = new int[1024];

	private long now_patinet_id_data;

	private static String tmp;				// 暫存文字訊息
	private static Socket clientSocket;	// 客戶端socket

	private String[][][] simulate_ECG = { { {"121", "121", "121", "121", "121", "121", "121", "121", "122", "122", "121", "121", "121", "121", "121", "120", "120", "120", "120", "121", "120", "120", "119", "120", "121", "122", "121", "119", "119", "117", "118", "117", "117", "116", "116", "116", "116", "116", "117", "116", "116", "115", "115", "115", "115", "115", "115", "114", "115", "116", "116", "116", "115", "114", "115", "115", "116", "115", "115", "114", "114", "113", "112", "111"},
			{"111", "110", "108", "107", "107", "109", "113", "118", "125", "133", "144", "154", "161", "163", "160", "150", "135", "120", "112", "109", "109", "112", "113", "114", "113", "113", "113", "114", "114", "114", "114", "113", "113", "114", "113", "113", "113", "113", "113", "114", "113", "113", "113", "112", "113", "114", "114", "114", "114", "113", "113", "113", "113", "113", "112", "112", "113", "113", "113", "113", "113", "113", "114", "114"},
			{"113", "113", "113", "113", "113", "113", "113", "113", "112", "113", "113", "113", "114", "113", "113", "113", "113", "113", "113", "113", "113", "112", "113", "113", "113", "113", "113", "113", "113", "113", "113", "113", "113", "112", "112", "113", "113", "113", "113", "112", "112", "112", "112", "112", "112", "111", "112", "112", "112", "112", "112", "112", "112", "112", "112", "112", "112", "112", "112", "113", "114", "114", "113", "113"},
			{"114", "115", "115", "115", "115", "115", "115", "116", "117", "117", "117", "116", "117", "117", "118", "117", "117", "117", "117", "117", "117", "117", "117", "117", "117", "117", "116", "116", "116", "116", "116", "116", "116", "116", "116", "115", "116", "116", "116", "116", "116", "115", "116", "116", "116", "116", "115", "115", "116", "116", "115", "116", "115", "115", "114", "115", "116", "116", "116", "117", "118", "120", "120", "121"},
			{"121", "121", "121", "121", "121", "121", "121", "121", "122", "122", "121", "121", "121", "121", "121", "120", "120", "120", "120", "121", "120", "120", "119", "120", "121", "122", "121", "119", "119", "117", "118", "117", "117", "116", "116", "116", "116", "116", "117", "116", "116", "115", "115", "115", "115", "115", "115", "114", "115", "116", "116", "116", "115", "114", "115", "115", "116", "115", "115", "114", "114", "113", "112", "111"},
			{"111", "110", "108", "107", "107", "109", "113", "118", "125", "133", "144", "154", "161", "163", "160", "150", "135", "120", "112", "109", "109", "112", "113", "114", "113", "113", "113", "114", "114", "114", "114", "113", "113", "114", "113", "113", "113", "113", "113", "114", "113", "113", "113", "112", "113", "114", "114", "114", "114", "113", "113", "113", "113", "113", "112", "112", "113", "113", "113", "113", "113", "113", "114", "114"},
			{"113", "113", "113", "113", "113", "113", "113", "113", "112", "113", "113", "113", "114", "113", "113", "113", "113", "113", "113", "113", "113", "112", "113", "113", "113", "113", "113", "113", "113", "113", "113", "113", "113", "112", "112", "113", "113", "113", "113", "112", "112", "112", "112", "112", "112", "111", "112", "112", "112", "112", "112", "112", "112", "112", "112", "112", "112", "112", "112", "113", "114", "114", "113", "113"},
			{"114", "115", "115", "115", "115", "115", "115", "116", "117", "117", "117", "116", "117", "117", "118", "117", "117", "117", "117", "117", "117", "117", "117", "117", "117", "117", "116", "116", "116", "116", "116", "116", "116", "116", "116", "115", "116", "116", "116", "116", "116", "115", "116", "116", "116", "116", "115", "115", "116", "116", "115", "116", "115", "115", "114", "115", "116", "116", "116", "117", "118", "120", "120", "121"} },

			{ {"116", "117", "116", "115", "116", "117", "119", "118", "118", "118", "117", "118", "119", "119", "120", "120", "119", "120", "121", "121", "120", "120", "120", "120", "120", "120", "120", "120", "120", "121", "122", "122", "122", "121", "120", "119", "121", "121", "121", "120", "119", "119", "120", "120", "118", "117", "117", "119", "121", "121", "120", "119", "119", "119", "120", "120", "119", "118", "118", "119", "120", "119", "119", "117"},
					{"116", "116", "115", "114", "112", "112", "110", "109", "109", "108", "109", "109", "109", "109", "110", "109", "109", "108", "107", "107", "108", "108", "108", "107", "107", "107", "108", "108", "108", "108", "108", "109", "109", "108", "107", "107", "107", "109", "109", "107", "107", "106", "107", "109", "109", "109", "108", "107", "108", "108", "109", "108", "107", "107", "106", "107", "106", "106", "107", "110", "114", "119", "122", "124"},
					{"125", "126", "129", "133", "138", "141", "143", "144", "145", "144", "144", "143", "142", "140", "137", "136", "135", "132", "128", "125", "120", "114", "110", "104", "99", "96", "93", "91", "90", "89", "89", "89", "88", "89", "90", "91", "92", "92", "92", "93", "95", "95", "96", "96", "97", "97", "98", "98", "98", "98", "98", "98", "98", "98", "97", "97", "97", "97", "97", "97", "97", "96", "96", "97"},
					{"97", "97", "96", "96", "95", "96", "97", "96", "96", "96", "96", "96", "98", "97", "97", "97", "97", "98", "99", "99", "100", "99", "99", "100", "101", "102", "102", "102", "102", "102", "104", "105", "105", "105", "105", "106", "107", "108", "108", "108", "108", "110", "110", "110", "111", "111", "111", "111", "112", "112", "112", "112", "112", "113", "114", "114", "114", "113", "113", "113", "114", "114", "115", "115"},
					{"116", "117", "116", "115", "116", "117", "119", "118", "118", "118", "117", "118", "119", "119", "120", "120", "119", "120", "121", "121", "120", "120", "120", "120", "120", "120", "120", "120", "120", "121", "122", "122", "122", "121", "120", "119", "121", "121", "121", "120", "119", "119", "120", "120", "118", "117", "117", "119", "121", "121", "120", "119", "119", "119", "120", "120", "119", "118", "118", "119", "120", "119", "119", "117"},
					{"116", "116", "115", "114", "112", "112", "110", "109", "109", "108", "109", "109", "109", "109", "110", "109", "109", "108", "107", "107", "108", "108", "108", "107", "107", "107", "108", "108", "108", "108", "108", "109", "109", "108", "107", "107", "107", "109", "109", "107", "107", "106", "107", "109", "109", "109", "108", "107", "108", "108", "109", "108", "107", "107", "106", "107", "106", "106", "107", "110", "114", "119", "122", "124"},
					{"125", "126", "129", "133", "138", "141", "143", "144", "145", "144", "144", "143", "142", "140", "137", "136", "135", "132", "128", "125", "120", "114", "110", "104", "99", "96", "93", "91", "90", "89", "89", "89", "88", "89", "90", "91", "92", "92", "92", "93", "95", "95", "96", "96", "97", "97", "98", "98", "98", "98", "98", "98", "98", "98", "97", "97", "97", "97", "97", "97", "97", "96", "96", "97"},
					{"97", "97", "96", "96", "95", "96", "97", "96", "96", "96", "96", "96", "98", "97", "97", "97", "97", "98", "99", "99", "100", "99", "99", "100", "101", "102", "102", "102", "102", "102", "104", "105", "105", "105", "105", "106", "107", "108", "108", "108", "108", "110", "110", "110", "111", "111", "111", "111", "112", "112", "112", "112", "112", "113", "114", "114", "114", "113", "113", "113", "114", "114", "115", "115"} },

			{ {"83", "84", "86", "88", "89", "88", "87", "87", "87", "86", "86", "86", "85", "85", "86", "87", "88", "90", "91", "90", "90", "88", "87", "86", "86", "87", "89", "89", "88", "87", "88", "91", "93", "92", "90", "90", "91", "93", "95", "96", "98", "98", "98", "96", "94", "92", "92", "92", "93", "93", "92", "91", "90", "93", "97", "99", "100", "99", "96", "93", "89", "88", "89", "90"},
					{"92", "91", "90", "89", "87", "84", "83", "82", "81", "81", "82", "83", "85", "87", "88", "88", "87", "86", "84", "84", "85", "84", "85", "85", "84", "85", "86", "87", "89", "90", "91", "89", "86", "83", "82", "85", "89", "91", "91", "89", "87", "84", "85", "89", "94", "100", "107", "110", "112", "112", "112", "114", "120", "129", "139", "146", "145", "136", "120", "100", "82", "71", "63", "58"},
					{"55", "50", "44", "41", "37", "36", "36", "36", "38", "38", "38", "39", "40", "42", "45", "48", "49", "50", "55", "60", "66", "72", "76", "79", "80", "80", "80", "79", "78", "78", "80", "84", "86", "86", "85", "83", "81", "81", "81", "82", "83", "84", "85", "85", "84", "84", "82", "82", "82", "82", "83", "83", "82", "80", "79", "78", "78", "77", "76", "77", "78", "78", "78", "78"},
					{"78", "78", "77", "74", "72", "72", "72", "75", "77", "78", "78", "75", "71", "69", "70", "74", "76", "78", "77", "75", "74", "73", "72", "74", "76", "76", "76", "76", "77", "77", "78", "80", "82", "84", "86", "86", "85", "83", "83", "84", "86", "86", "87", "87", "87", "89", "90", "90", "88", "88", "89", "91", "92", "92", "91", "89", "88", "87", "87", "87", "88", "89", "88", "86"},
					{"83", "84", "86", "88", "89", "88", "87", "87", "87", "86", "86", "86", "85", "85", "86", "87", "88", "90", "91", "90", "90", "88", "87", "86", "86", "87", "89", "89", "88", "87", "88", "91", "93", "92", "90", "90", "91", "93", "95", "96", "98", "98", "98", "96", "94", "92", "92", "92", "93", "93", "92", "91", "90", "93", "97", "99", "100", "99", "96", "93", "89", "88", "89", "90"},
					{"92", "91", "90", "89", "87", "84", "83", "82", "81", "81", "82", "83", "85", "87", "88", "88", "87", "86", "84", "84", "85", "84", "85", "85", "84", "85", "86", "87", "89", "90", "91", "89", "86", "83", "82", "85", "89", "91", "91", "89", "87", "84", "85", "89", "94", "100", "107", "110", "112", "112", "112", "114", "120", "129", "139", "146", "145", "136", "120", "100", "82", "71", "63", "58"},
					{"55", "50", "44", "41", "37", "36", "36", "36", "38", "38", "38", "39", "40", "42", "45", "48", "49", "50", "55", "60", "66", "72", "76", "79", "80", "80", "80", "79", "78", "78", "80", "84", "86", "86", "85", "83", "81", "81", "81", "82", "83", "84", "85", "85", "84", "84", "82", "82", "82", "82", "83", "83", "82", "80", "79", "78", "78", "77", "76", "77", "78", "78", "78", "78"},
					{"78", "78", "77", "74", "72", "72", "72", "75", "77", "78", "78", "75", "71", "69", "70", "74", "76", "78", "77", "75", "74", "73", "72", "74", "76", "76", "76", "76", "77", "77", "78", "80", "82", "84", "86", "86", "85", "83", "83", "84", "86", "86", "87", "87", "87", "89", "90", "90", "88", "88", "89", "91", "92", "92", "91", "89", "88", "87", "87", "87", "88", "89", "88", "86"} },

			{ {"87", "87", "88", "88", "90", "92", "93", "92", "91", "91", "90", "90", "91", "92", "91", "89", "87", "86", "87", "89", "91", "90", "88", "88", "87", "88", "87", "85", "85", "85", "87", "88", "89", "90", "91", "90", "89", "89", "89", "88", "87", "86", "87", "89", "90", "90", "88", "87", "87", "87", "89", "89", "88", "87", "87", "87", "90", "90", "92", "91", "91", "90", "88", "88"},
					{"89", "91", "92", "91", "91", "91", "91", "91", "89", "89", "89", "90", "91", "92", "91", "89", "88", "87", "88", "89", "90", "91", "90", "91", "91", "91", "91", "92", "93", "94", "94", "95", "95", "96", "97", "97", "97", "97", "98", "97", "97", "96", "95", "93", "93", "94", "97", "99", "101", "100", "98", "97", "97", "98", "96", "93", "89", "88", "89", "90", "91", "92", "91", "90"},
					{"89", "89", "88", "88", "86", "86", "86", "85", "85", "86", "88", "89", "89", "88", "87", "85", "84", "84", "85", "86", "86", "85", "85", "86", "88", "87", "88", "87", "85", "84", "84", "85", "84", "83", "84", "88", "95", "101", "108", "113", "114", "112", "110", "112", "118", "127", "138", "145", "146", "139", "123", "105", "86", "71", "59", "52", "48", "46", "45", "45", "43", "40", "39", "37"},
					{"37", "39", "38", "39", "39", "41", "43", "46", "49", "53", "57", "62", "68", "73", "77", "80", "80", "80", "79", "78", "80", "80", "82", "84", "84", "85", "86", "86", "83", "80", "77", "76", "79", "81", "84", "85", "87", "87", "86", "85", "81", "78", "77", "77", "78", "79", "78", "78", "78", "78", "76", "75", "73", "72", "72", "72", "73", "75", "75", "76", "77", "75", "74", "73"},
					{"71", "70", "69", "69", "70", "72", "75", "76", "76", "76", "75", "74", "72", "71", "71", "71", "72", "73", "74", "76", "78", "78", "80", "80", "81", "82", "82", "83", "84", "84", "82", "79", "78", "82", "86", "90", "92", "91", "91", "88", "87", "87", "88", "88", "87", "86", "86", "87", "88", "88", "87", "86", "87", "86", "86", "86", "87", "85", "84", "81", "84", "87", "91", "93"},
					{"93", "92", "89", "87", "84", "83", "82", "84", "85", "85", "85", "84", "83", "83", "84", "87", "88", "88", "88", "87", "86", "84", "84", "85", "87", "89", "89", "88", "87", "87", "86", "86", "85", "85", "84", "83", "84", "85", "87", "87", "88", "88", "89", "89", "88", "87", "88", "88", "86", "85", "85", "87", "89", "92", "94", "94", "94", "91", "89", "87", "86", "87", "88", "89"},
					{"88", "88", "87", "87", "87", "88", "89", "89", "91", "92", "91", "90", "91", "91", "93", "94", "94", "95", "96", "96", "96", "95", "95", "96", "97", "97", "96", "95", "93", "92", "90", "91", "93", "96", "98", "99", "101", "100", "98", "94", "89", "87", "87", "88", "89", "91", "91", "90", "89", "90", "88", "86", "84", "81", "82", "83", "84", "85", "86", "86", "87", "87", "87", "88"},
					{"90", "91", "91", "90", "89", "89", "89", "89", "87", "88", "88", "88", "88", "86", "84", "84", "83", "83", "84", "87", "92", "97", "102", "108", "114", "118", "120", "119", "118", "119", "122", "128", "135", "140", "140", "133", "120", "102", "83", "67", "56", "49", "45", "43", "42", "41", "42", "41", "40", "38", "37", "36", "36", "36", "37", "38", "43", "48", "52", "58", "65", "70", "75", "80"} },

			{ {"106", "107", "109", "110", "110", "110", "109", "108", "107", "106", "106", "107", "108", "110", "113", "113", "114", "114", "112", "111", "110", "110", "110", "112", "112", "112", "114", "115", "116", "118", "120", "120", "120", "120", "119", "118", "118", "118", "118", "119", "119", "119", "119", "119", "119", "120", "119", "119", "118", "118", "119", "120", "120", "120", "121", "120", "119", "119", "120", "121", "123", "123", "123", "121"},
					{"119", "118", "119", "119", "120", "122", "122", "122", "122", "121", "121", "122", "122", "122", "122", "123", "123", "123", "123", "124", "124", "124", "124", "123", "124", "125", "126", "127", "127", "128", "127", "127", "126", "126", "126", "128", "129", "130", "130", "130", "130", "128", "128", "127", "127", "127", "128", "128", "129", "130", "130", "130", "129", "128", "126", "126", "126", "126", "126", "127", "126", "125", "125", "123"},
					{"121", "121", "122", "122", "122", "123", "122", "122", "122", "121", "120", "121", "121", "121", "122", "122", "122", "122", "123", "123", "123", "123", "123", "123", "121", "120", "120", "120", "122", "123", "123", "122", "121", "121", "122", "123", "123", "123", "124", "124", "126", "128", "132", "138", "143", "148", "149", "146", "139", "128", "119", "112", "109", "109", "110", "111", "113", "114", "114", "114", "112", "111", "110", "111"},
					{"113", "116", "117", "118", "120", "122", "121", "121", "120", "120", "120", "121", "121", "121", "121", "120", "119", "118", "118", "118", "119", "119", "120", "120", "120", "120", "120", "120", "119", "119", "120", "120", "121", "121", "122", "122", "122", "123", "123", "123", "123", "122", "122", "121", "121", "121", "122", "122", "122", "122", "122", "122", "121", "120", "120", "121", "122", "124", "125", "125", "123", "122", "122", "121"},
					{"118", "116", "117", "117", "116", "114", "111", "110", "110", "111", "113", "115", "115", "116", "116", "117", "117", "118", "120", "121", "121", "121", "121", "120", "121", "122", "122", "123", "123", "123", "123", "123", "122", "121", "120", "121", "121", "122", "122", "122", "123", "122", "121", "121", "122", "122", "122", "122", "122", "122", "122", "122", "123", "124", "124", "123", "123", "123", "123", "124", "125", "126", "126", "126"},
					{"127", "127", "126", "127", "127", "127", "128", "128", "129", "130", "131", "132", "132", "133", "133", "132", "131", "130", "130", "130", "132", "133", "133", "133", "133", "133", "133", "133", "133", "133", "132", "131", "130", "129", "129", "129", "129", "129", "128", "127", "126", "126", "126", "125", "123", "122", "122", "123", "124", "124", "123", "122", "121", "121", "121", "121", "121", "122", "122", "122", "122", "121", "121", "120"},
					{"120", "119", "120", "120", "120", "120", "121", "121", "122", "122", "121", "121", "120", "120", "121", "121", "121", "122", "123", "122", "122", "120", "119", "119", "119", "119", "119", "120", "120", "120", "122", "122", "122", "123", "123", "122", "121", "120", "119", "119", "118", "118", "119", "120", "121", "122", "123", "124", "123", "123", "122", "122", "122", "123", "123", "123", "122", "121", "120", "119", "119", "118", "118", "118"},
					{"118", "119", "119", "120", "121", "121", "121", "121", "120", "120", "120", "121", "121", "122", "122", "122", "122", "122", "122", "122", "122", "122", "122", "122", "121", "121", "122", "122", "123", "121", "120", "120", "120", "120", "121", "121", "122", "122", "122", "122", "122", "121", "121", "121", "122", "123", "125", "127", "129", "132", "136", "144", "148", "151", "151", "146", "138", "126", "115", "108", "104", "103", "104", "106"} } };

	private TextView msg;
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		public void run() {
			this.update();
			handler.postDelayed(this, 1);
		}
		void update() {
			/*if(SERVER_STRING!=null){
				ServerString(SERVER_STRING);
				SERVER_STRING=null;
			}*/
			delay = (delay + 1)%1000;
			if(delay==0 || delay==500){
				byte []Cmd = new byte []{0x0D};
				sendCmd(Cmd);
				Cmd = new byte []{'W','+',0x0D};
				sendCmd(Cmd);

				if(choose>0){
	        		/*DatabaseConnector databaseConnector = new DatabaseConnector(Monitor.this);
		            databaseConnector.open();
		            Cursor result = databaseConnector.getOneContact(choose);
		            result.moveToFirst();
		            int nameIndex = result.getColumnIndex("name");
		            try{
			            if(mText.toString().equals(result.getString(nameIndex))==false){
			                updatechoose( "請選擇病患", "尚未連結裝置", "連結裝置裝置");
			                choose = -1;
			            	Button_inform.setEnabled(false);
			            	Button_bth.setEnabled(false);
			            }
		            }catch(Exception e){
		                updatechoose( "請選擇病患", "尚未連結裝置", "連結裝置裝置");
		                choose = -1;
		            	Button_inform.setEnabled(false);
		            	Button_bth.setEnabled(false);
		            }
		            result.close(); // close the result cursor
		            databaseConnector.close();*/
					/*int i;
					int check=0;
					for(i=0; i<connected_num; i++){
						if(connected_id[i]==choose){
							textView3.setText("已連結到 " + device_name[i]);
							Button_bth.setText("解除裝置");
							Button_internet.setText("連結網路");
							Button_internet.setEnabled(false);
							if(account_name[(int) choose]!=null){
								accountView.setText("已同步到" + account_name[(int) choose]);
								Button_synch.setText("解除同步");
								Button_synch.setEnabled(true);
							}
							else{
								accountView.setText("尚未同步");
								Button_synch.setText("線上同步");
								Button_synch.setEnabled(true);
							}
							check=1;

							break;
						}
					}*/
					/*if(check==0){
						//textView3.setText("尚未連結");
						Button_bth.setText("連結裝置");
						Button_bth.setEnabled(true);

						//藍芽中斷時切斷同步
						Button_synch.setText("線上同步");*/
						/*if(simulate_state[(int) choose]>=0)
							Button_synch.setEnabled(true);
						else
							Button_synch.setEnabled(false);*/
						/*accountView.setText("尚未同步");

						Button_internet.setEnabled(true);
						if(connect_name[(int) choose]!=null){
							Button_internet.setText("解除網路");
							Button_bth.setText("連結裝置");
							Button_bth.setEnabled(false);
						}
						//else
							//Button_internet.setText("連結網路");
					}*/
					/*if(account_name[(int) choose]!=null){
						accountView.setText("已同步到 " + account_name[(int) choose]);
						Button_synch.setText("解除同步");
						Button_synch.setEnabled(true);
					}
					if(connect_name[(int) choose]!=null){
						textView3.setText("已連結到 " + connect_name[(int) choose]);
						Button_bth.setText("連結裝置");
						Button_bth.setEnabled(false);
						Button_internet.setText("解除網路");
					}*/
				}
			}

			if(textView.getText().toString().equals("請選擇病患") && choose!=-1)
			{
				for(int i = 0; i < 1024; i++)
				{
					if(choose == i)
					{
						simulate_state[i] = -1;
						break;
					}
				}
			}

			if(delay==0 || delay==250 || delay==500 || delay==750){
				for(int i=0; i<1024; i++){
					if(simulate_state[i]>=0){
						if(account_name[i]!=null){
							String s = "";
							for(int j=0; j<simulate_ECG[simulate_state[i]][(delay+offset)/250].length; j++){
								s = s + simulate_ECG[simulate_state[i]][(delay+offset)/250][j] + " ";
							}
							writeData("data" + " "  + server_id[i] + " " + "ECG" + " " + s + " " + "\n");
						}
						if(choose==(int) i){
							byte[] b = new byte[simulate_ECG[simulate_state[i]][(delay+offset)/250].length];
							for(int j=0; j<simulate_ECG[simulate_state[i]][(delay+offset)/250].length; j++){
								b[j] = (byte)(Integer.parseInt(simulate_ECG[simulate_state[i]][(delay+offset)/250][j])); //y
							}
							mChartView.Wave_Draw(b);
						}

						String temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3;

						DatabaseConnector databaseConnector = new DatabaseConnector(Monitor.this);
						databaseConnector.open();
						Cursor result = databaseConnector.getOneContact(i);
						result.moveToFirst();
						int nameIndex = result.getColumnIndex("name");
						int smsphoneIndex1 = result.getColumnIndex("smsphone1");
						int smsphoneIndex2 = result.getColumnIndex("smsphone2");
						int smsphoneIndex3 = result.getColumnIndex("smsphone3");
						temp_name=result.getString(nameIndex);
						temp_smsphone_1=result.getString(smsphoneIndex1);
						temp_smsphone_2=result.getString(smsphoneIndex2);
						temp_smsphone_3=result.getString(smsphoneIndex3);
						databaseConnector.close();

						String[] info = new String[2];

						TAfter[i] = System.currentTimeMillis();
						long Tpass = (TAfter[i] - TBefore[i])/1000;

						info[0] = "IHR";
						info[1] = "060";
						InfoMessage(Tpass, i, info, temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);

						info[0] = "TE";
						info[1] = "40000";
						InfoMessage(Tpass, i, info, temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);

						WarnMessage(simulate_state[i], i, temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);
					}
				}
				if(delay==750)
					offset=(offset+1000)%2000;
			}
		}
	};


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//鎖定畫面為垂直顯示
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		//設title bar為自訂的textview
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.monitor);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);	//左側顯示app name

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null)
		{
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		contactListView = getListView(); // get the built-in ListView
		contactListView.setOnItemClickListener(viewContactListener);

		// map each contact's name to a TextView in the ListView layout
		String[] from = new String[] { "name" };
		int[] to = new int[] { R.id.contactTextView };
		contactAdapter = new SimpleCursorAdapter(
				Monitor.this, R.layout.contact_list_item, null, from, to);
		setListAdapter(contactAdapter); // set contactView's adapter

		textView = (TextView) findViewById(R.id.textView1);
		textView3 = (TextView) findViewById(R.id.textView3);

		handler.postDelayed(runnable, 1);

		for(int i=0; i<1024; i++){
			IhrHighStateCount[i]=0;
			IhrLowStateCount[i]=0;
			TeHighStateCount[i]=0;
			LBBBStateCount[i]=0;
			RBBBStateCount[i]=0;
			VPCStateCount[i]=0;
			APCStateCount[i]=0;
			TBefore[i]=0;
			WarnFlag[i]=Enable_Warning;
			account_name[i]=null;
			server_id[i]=-1;
			connect_name[i]=null;
			simulate_state[i]=-1;
		}

		// 以新的執行緒來讀取資料
		Thread t = new Thread(readData);

		// 啟動執行緒
		t.start();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
	}

	private void checkfile(){
		boolean isExit = true;
		FileOutputStream fos = null;
		try{
			openFileInput(filename);
		}
		catch(FileNotFoundException e){
			isExit = false;
		}
		if(!isExit){
			try{
				fos = openFileOutput(filename, MODE_WORLD_WRITEABLE);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				String txt = contactPhone_key + "=" + contactPhone_value;
				bos.write(txt.getBytes());
				bos.write(new String("\r\n").getBytes());
				txt = userAge_key + "=" + userAge_value;
				bos.write(txt.getBytes());
				bos.write(new String("\r\n").getBytes());
				txt = Server_key + "=" + Server_value;
				bos.write(txt.getBytes());

				bos.close();
				fos.close();
			}
			catch(FileNotFoundException e){

			}
			catch(IOException e){

			}
		}
	}

	private void loadfiles(){
		Properties p = new Properties();
		try{
			p.load(openFileInput(filename));
			userAge_value = p.getProperty(userAge_key);
		}
		catch(FileNotFoundException e){

		}
		catch(IOException e){

		}
	}

	private void findViews()
	{
		mChartView =  (ChartView) findViewById(R.id.Chart);
		//setup ChartView's wid
		mChartView.setX_Axis(getWindowManager().getDefaultDisplay().getWidth());
		Button_inform = (Button) findViewById(R.id.inform);
		Button_bth = (Button) findViewById(R.id.bth);
		Button_synch = (Button) findViewById(R.id.synch);
		Button_internet = (Button) findViewById(R.id.internet);
		Button_simulate = (Button) findViewById(R.id.simulate);
		Button_add_patient = (Button) findViewById(R.id.add_patient);
		Button_hospital_map = (Button) findViewById(R.id.hospital_map);
		if(choose==-1){
			Button_inform.setEnabled(false);
			Button_bth.setEnabled(false);
			Button_synch.setEnabled(false);
			Button_internet.setEnabled(false);
			Button_simulate.setEnabled(false);
		}
		TEText =(TextView) findViewById(R.id.TE_Text);
		IHRText =(TextView) findViewById(R.id.IHR_Text);
		StateText =(TextView) findViewById(R.id.state);
		accountView = (TextView) findViewById(R.id.account);
	}

	// Initialize the send button with a listener that for click events
	private void setListensers()
	{

		Button_bth.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(Button_bth.getText().toString().equals("連結裝置"))
				{
					if(test == 0)
					{
						int i, j;
						for(i=0; i<connected_num; i++){
							if(connected_id[i]==choose){
								mBTService.disconnect(device_address[i]);
								break;
							}
						}
						if(i==connected_num){
							Intent serverIntent = new Intent(Monitor.this, DeviceListActivity.class);
							serverIntent.putExtra(ROW_ID, choose);
							startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
						}
					}
					else
					{
						Intent serverIntent = new Intent(Monitor.this, DeviceListActivity.class);
						serverIntent.putExtra(ROW_ID, choose);
						startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
					}

					String user_name = "111";
					String user_password = "000";

					String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
					String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
					String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
					String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
					String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
					String heart = Monitor.StateText.getText().toString().equals("--") ? "0" : "60";
					String temp = Monitor.StateText.getText().toString().equals("--") ? "0.0" : "36.0";

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
								+ URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(Monitor.StateText.getText().toString(), "UTF-8")
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
				else
				{
					test = 0;

					Button_bth.setText("連結裝置");
					if(Button_internet.getText().toString().equals("連結網路")) textView3.setText("尚未連結");
					else textView3.setText("login successed");

					String user_name = "111";
					String user_password = "000";

					String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
					String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
					String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
					String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
					String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
					String heart = Monitor.StateText.getText().toString().equals("--") ? "0" : "60";
					String temp = Monitor.StateText.getText().toString().equals("--") ? "0.0" : "36.0";

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
								+ URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(Monitor.StateText.getText().toString(), "UTF-8")
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

					int i, j;
					for(i=0; i<connected_num; i++){
						if(connected_id[i]==choose){
							mBTService.disconnect(device_address[i]);
							break;
						}
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(Monitor.this);

					builder.setTitle("注意");
					builder.setMessage("已斷開藍芽接收器，若欲重新連結，請再重新點按連結裝置的按鈕");
					builder.setPositiveButton(R.string.errorButton, null);
					builder.show();
				}
			}
		});

		Button_inform.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent viewContact = new Intent(Monitor.this, ViewContact.class);
				viewContact.putExtra(ROW_ID, choose);
				startActivity(viewContact);
			}
		});

		Button_synch.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(Button_synch.getText().toString().equals("解除同步"))
				{
					accountView.setText("尚未同步");
					Button_synch.setText("線上同步");

					String user_name = "111";
					String user_password = "000";

					String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
					String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
					String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
					String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
					String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
					String heart = Monitor.StateText.getText().toString().equals("--") ? "0" : "60";
					String temp = Monitor.StateText.getText().toString().equals("--") ? "0.0" : "36.0";

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
								+ URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(Monitor.StateText.getText().toString(), "UTF-8")
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

					AlertDialog.Builder builder = new AlertDialog.Builder(Monitor.this);

					builder.setTitle("注意");
					builder.setMessage("已斷開同步，若欲重新同步，請再重新點按線上同步的按鈕");
					builder.setPositiveButton(R.string.errorButton, null);
					builder.show();
				}
				else
				{
					if(account_name[(int) choose]==null){
						Intent Login = new Intent(Monitor.this, SynchLogin.class);
						Login.putExtra(ROW_ID, choose);
						startActivityForResult(Login, REQUEST_LOG_IN);
					}
				}

				/*else{
					writeData("logout " + server_id[(int) choose] + " " + "\n");
				}*/
			}
		});

		Button_internet.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				String check = Monitor.Button_internet.getText().toString();
				if(check.equals("斷開連線"))
				{
					Monitor.Button_internet.setText("連結網路");
					if(Button_bth.getText().toString().equals("解除裝置")) Monitor.textView3.setText("已連結到 8Z027");
					else Monitor.textView3.setText("尚未連結");
					Button_synch.setEnabled(false);
					Button_synch.setText("線上同步");
					accountView.setText("尚未同步");

					String user_name = "111";
					String user_password = "000";

					String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
					String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "false";
					String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
					String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
					String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
					String heart = Monitor.StateText.getText().toString().equals("--") ? "0" : "60";
					String temp = Monitor.StateText.getText().toString().equals("--") ? "0.0" : "36.0";

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
								+ URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(Monitor.StateText.getText().toString(), "UTF-8")
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

					/*login_url = "http://192.168.43.190/ECG/get_patient_status.php";
					String result1 = "";
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
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						//StringBuilder stringBuilder = new StringBuilder();
						while((line = bufferedReader.readLine())!=null) result1 += line;
						bufferedReader.close();
						inputStream.close();
						httpURLConnection.disconnect();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					String get[] = result1.split(" ");

					Simulate restart = new Simulate();
					restart.restart_simulate(get[1]);
					Button_inform.setText(get[1]);

					AlertDialog.Builder builder = new AlertDialog.Builder(Monitor.this);

					builder.setTitle("注意");
					builder.setMessage("已與伺服器斷開連線，若欲重新連結，請再重新點按連結網路的按鈕");
					builder.setPositiveButton(R.string.errorButton, null);
					builder.show();*/
				}
				else
				{
					if(connect_name[(int) choose]==null){
						Intent Login = new Intent(Monitor.this, ConnectInternet.class);
						Login.putExtra(ROW_ID, choose);
						startActivityForResult(Login, REQUEST_CONNECT_INTERNET);
					}
					/*else{
						writeData("disconnect " + choose + " " + connect_name[(int) choose] + " " + "\n");
					}*/
				}

			}
		});

		Button_simulate.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent Simulate = new Intent(Monitor.this, Simulate.class);
				startActivityForResult(Simulate, REQUEST_SIMULATE);
			}
		});

		// 新增病患按鈕
		Button_add_patient.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				/*Intent addEditContact = new Intent(Monitor.this, AddEditContact.class);
				addEditContact.putExtra(ROW_ID, choose);
				startActivity(addEditContact);*/
				// 新增病患
				Intent addNewContact = new Intent().setClass(Monitor.this, AddEditContact.class);
				Monitor.this.startActivity(addNewContact);
			}
		});

		// 醫院地圖按鈕
		Button_hospital_map.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent Hospital;
				Hospital = new Intent().setClass(Monitor.this, Hospital.class);
				Monitor.this.startActivity(Hospital);
			}
		});
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(D) Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled())
		{
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

			// public void startActivityForResult (Intent intent, int requestCode)
			// onActivityResult() will be called when activity return the request code
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the ky202 Monitor session
		} else {
			if (mBTService == null) setupMonitor();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if(D) Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		new GetContactsTask().execute((Object[]) null);
		if (mBTService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't started already
			if (mBTService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mBTService.start();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mBTService != null) mBTService.stop();
		if(D) Log.e(TAG, "--- ON DESTROY ---");
		handler.removeCallbacks(runnable);
		super.onDestroy();
	}


	@Override
	protected void onStop()
	{
		Cursor cursor = contactAdapter.getCursor(); // get current Cursor

		if (cursor != null)
			cursor.deactivate(); // deactivate it

		contactAdapter.changeCursor(null); // adapted now has no Cursor
		super.onStop();
	} // end method onStop


	private void setupMonitor()
	{
		Log.d(TAG, "setupMonitor()");

		findViews();
		setListensers();
		checkfile();
		loadfiles();
		//if(!userAge_value.equals("")){
		//	MAX_IHR = (220.0-Double.parseDouble(userAge_value))*0.85;
		//}
		//else{
		//	Toast.makeText(Monitor.this, "您尚未輸入使用者資訊", Toast.LENGTH_SHORT).show();
		//	MAX_IHR = 150.0;
		//}

		// Initialize the BluetoothChatService to perform bluetooth connections
		mBTService = new BluetoothService(this, mHandler);
		mECGService = new ECGService(this,mECGHandler);
		//mECGService.setState(ECGService.STATE_WAVEON);
	}

	private void resetECGService()
	{
		byte [] cmd = new byte[]{'R','S',0x0D};
		sendCmd(cmd);

		if (mECGService!=null)
			mECGService.reset();

		if (mChartView != null)
			mChartView.ClearChart();
	}

	/**
	 * Sends a message.
	 * param message  A string of text to send.
	 */
	private void sendCmd(byte[] Cmd) {
		// Check that we're actually connected before trying anything
		if (mBTService.getState() != BluetoothService.STATE_CONNECTED) {
			//Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (Cmd.length > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			mBTService.write(Cmd);

		}
	}


	// The Handler that gets information back from the BluetoothChatService
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				textView.setText(mText);
				textView3.setText(mText2);
				Button_bth.setText(mText3);
				accountView.setText(mText4);
			}
			else{
				switch (msg.what) {
					case MESSAGE_STATE_CHANGE:
						if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
						switch (msg.arg1) {
							case BluetoothService.STATE_CONNECTED:
								//resetECGService();
								break;
							case BluetoothService.STATE_CONNECTING:
								break;
							case BluetoothService.STATE_LISTEN:
							case BluetoothService.STATE_NONE:
								break;
						}
						break;
					case MESSAGE_WRITE:

						break;
					case MESSAGE_READ:

						byte[] readBuf = (byte[]) msg.obj;

						//Debug~
						//                String readMessage = new String(readBuf);
						//                if(D) Log.i(TAG, "Recieve: " + readMessage);
						//~Debug

						mECGService.DataHandler(readBuf, (int)now_patinet_id_data, (int)choose);

						break;
					case MESSAGE_DEVICE_NAME:
						// save the connected device's name
						mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
						mConnectedPatientId = msg.getData().getString(Patient_Id);
						connected_id[connected_num]=Long.parseLong(mConnectedPatientId);
						device_name[connected_num]=mConnectedDeviceName;
						device_address[connected_num]=msg.getData().getString(DEVICE_ADDRESS);
						connected_num++;
						if(choose==Long.parseLong(mConnectedPatientId)){
							test = 1;

							textView3.setText("已連結到 "+mConnectedDeviceName);

							Monitor.Button_bth.setText("解除裝置");

							int i;
							for(i = 0; i < 1024; i++)
							{
								if(choose == i)
								{
									simulate_state[i] = -1;
									break;
								}
							}

							Monitor.IHRText.setText("0");
							Monitor.IHRText.setTextColor(Color.parseColor("#4A7D6B"));
							Monitor.TEText.setText("0.0");
							Monitor.TEText.setTextColor(Color.parseColor("#FF8800"));
							Monitor.StateText.setText("--");
							Monitor.StateText.setTextColor(Color.parseColor("#000088"));
							Monitor.mChartView.ClearChart();

							String user_name = "111";
							String user_password = "000";

							String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
							String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
							String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
							String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
							String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
							String heart = Monitor.StateText.getText().toString().equals("--") ? "0" : "60";
							String temp = Monitor.StateText.getText().toString().equals("--") ? "0.0" : "36.0";

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
										+ URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(Monitor.StateText.getText().toString(), "UTF-8")
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
						Toast.makeText(getApplicationContext(), "已連線到  " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();


						break;
					case MESSAGE_TOAST:
						Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
								Toast.LENGTH_SHORT).show();

						AlertDialog.Builder builder = new AlertDialog.Builder(Monitor.this);

						builder.setTitle("ERROR");
						builder.setMessage("無法連線到裝置，有其他使用者正在使用藍芽接收器");
						builder.setPositiveButton(R.string.errorButton, null);
						builder.show();

						break;
					case MESSAGE_LOST:
						String temp = msg.getData().getString(DEVICE_ADDRESS);
						long id = 0;
						for(int i=0; i<connected_num; i++){
							if(device_address[i]==temp){
								id = connected_id[i];
								for(int j=i+1; j<connected_num; j++){
									connected_id[j-1]=connected_id[j];
									device_name[j-1]=device_name[j];
									device_address[j-1]=device_address[j];
								}
								break;
							}
						}
						if(id == choose){
							mChartView.ClearChart();
							IHRText.setText("0");
							IHRText.setTextColor(Color.parseColor("#4A7D6B"));
							TEText.setText("0.0");
							TEText.setTextColor(Color.parseColor("#FF8800"));
							StateText.setText("--");
							StateText.setTextColor(Color.parseColor("#000088"));
						}
						DatabaseConnector databaseConnector = new DatabaseConnector(Monitor.this);
						databaseConnector.open();
						Cursor result = databaseConnector.getOneContact(id);
						result.moveToFirst();
						int nameIndex = result.getColumnIndex("name");
						String name = result.getString(nameIndex);
						result.close();
						databaseConnector.close();


						connected_num--;
						Toast.makeText(getApplicationContext(), "裝置 " + msg.getData().getString(DEVICE_ADDRESS) + " 已經失去連線", Toast.LENGTH_SHORT).show();

						if(server_id[(int) id]!=-1 && simulate_state[(int) id]==-1){
							//終止同步
							writeData("logout " + server_id[(int) id] + " " + "\n");
						}
						break;
					case MESSAGE_PATIENT:
						String temp3 = msg.getData().getString(PATIENT_DATA);
						for(int i=0; i<connected_num; i++){
							if(device_address[i]==temp3){
								now_patinet_id_data=connected_id[i];
								break;
							}
						}
						break;
				}
			}
		}
	};//end of [private final Handler mHandler = new Handler() ]

	public String ByteToString(byte[] bytes)
	{
		String s = "";

		for(int i = 0; i < bytes.length; i++)
		{
			s = s + (char)bytes[i];
		}

		return s;
	}

	private int total = 0;
	private String record3 = "";
	private final Handler mECGHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what)
			{
				case MESSAGE_RAW:

					byte[] RawBuf = (byte[]) msg.obj;
					if(Monitor.Button_bth.getText().toString().equals("解除裝置")) {
						if (total != 8192) {
							for (int i = 0; i < RawBuf.length; i++)
								record3 += Byte.toString(RawBuf[i]) + " ";
							total += RawBuf.length;
						} else {
							String login_url = "http://192.168.43.190/ECG/add_bth_ECG.php";
							String result = "", line;
							try {
								URL url = new URL(login_url);
								HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
								httpURLConnection.setRequestMethod("POST");
								httpURLConnection.setDoOutput(true);
								httpURLConnection.setDoInput(true);
								OutputStream outputStream = httpURLConnection.getOutputStream();
								BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
								String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8")
										+ "&"
										+ URLEncoder.encode("ECG", "UTF-8") + "=" + URLEncoder.encode(record3, "UTF-8");
								bufferedWriter.write(post_data);
								bufferedWriter.flush();
								bufferedWriter.close();
								outputStream.close();
								InputStream inputStream = httpURLConnection.getInputStream();
								BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
								while ((line = bufferedReader.readLine()) != null) result += line;
								bufferedReader.close();
								inputStream.close();
								httpURLConnection.disconnect();
							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}

							record3 = "";
							total = 0;
						}
					}

					if(simulate_state[(int) choose]==-1)
						mChartView.Wave_Draw(RawBuf);

					break;

				case MESSAGE_UPDATE:
					String s = msg.getData().getString(UPDATE_STRING);
					String[] AfterSplit = s.split(",");
					if(account_name[Integer.parseInt(AfterSplit[0])]!=null && simulate_state[Integer.parseInt(AfterSplit[0])]==-1){
						writeData("data " + server_id[Integer.parseInt(AfterSplit[0])] + " " + AfterSplit[1] + " " + "\n");
					}
					break;
				case MESSAGE_INFO:
					//將資訊字串切割為"="前後，Ex:IHR=60分割為 info[0]=IHR,info[1]=60
					String [] info = msg.getData().getString(KY_INFO).split("=");
					int now_patient_id = Integer.parseInt(msg.getData().getString(Patient_Id));
					TAfter[now_patient_id] = System.currentTimeMillis();
					long Tpass = (TAfter[now_patient_id] - TBefore[now_patient_id])/1000;


					String temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3;

					DatabaseConnector databaseConnector = new DatabaseConnector(Monitor.this);
					databaseConnector.open();
					Cursor result = databaseConnector.getOneContact(now_patient_id);
					result.moveToFirst();
					int nameIndex = result.getColumnIndex("name");
					int smsphoneIndex1 = result.getColumnIndex("smsphone1");
					int smsphoneIndex2 = result.getColumnIndex("smsphone2");
					int smsphoneIndex3 = result.getColumnIndex("smsphone3");
					temp_name=result.getString(nameIndex);
					temp_smsphone_1=result.getString(smsphoneIndex1);
					temp_smsphone_2=result.getString(smsphoneIndex2);
					temp_smsphone_3=result.getString(smsphoneIndex3);
					databaseConnector.close();

					if(simulate_state[now_patient_id]==-1)
						InfoMessage(Tpass, now_patient_id, info, temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);

					break;

				case MESSAGE_KY_STATE:

					break;

				case MESSAGE_WARN:
					int warntype = Integer.parseInt(msg.getData().getString(WARN_TYPE));
					now_patient_id = Integer.parseInt(msg.getData().getString(Patient_Id));

					databaseConnector = new DatabaseConnector(Monitor.this);
					databaseConnector.open();
					result = databaseConnector.getOneContact(now_patient_id);
					result.moveToFirst();
					nameIndex = result.getColumnIndex("name");
					smsphoneIndex1 = result.getColumnIndex("smsphone1");
					smsphoneIndex2 = result.getColumnIndex("smsphone2");
					smsphoneIndex3 = result.getColumnIndex("smsphone3");
					temp_name=result.getString(nameIndex);
					temp_smsphone_1=result.getString(smsphoneIndex1);
					temp_smsphone_2=result.getString(smsphoneIndex2);
					temp_smsphone_3=result.getString(smsphoneIndex3);
					databaseConnector.close();

					if(simulate_state[now_patient_id]==-1)
						WarnMessage(warntype, now_patient_id, temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);
					break;
			}
		}
	};

	private String record = "";
	private int count_num = 0;
	private String record2 = "";
	private int count_num2 = 0;
	private void InfoMessage(long Tpass, int now_patient_id, String[] info, String temp_name, String temp_smsphone_1, String temp_smsphone_2, String temp_smsphone_3){
		if(Tpass >= 60){
			WarnFlag[now_patient_id] = Enable_Warning;
		}

		if(info[0].equals("IHR"))
		{
			double Ihr = Double.parseDouble(info[1]);
			int Ihr2 = Integer.parseInt(info[1]);
			//Log.d(TAG, "IHR_Data_In");
			if(choose==now_patient_id)
			{
				IHRText.setText(String.valueOf(Ihr2));

				if(Button_bth.getText().toString().equals("解除裝置"))
				{
					if(count_num!=10)
					{
						record += String.valueOf(Ihr2) + " ";
						count_num++;
					}
					else
					{
						String login_url = "http://192.168.43.190/ECG/add_bth_heart.php";
						String result = "", line;
						try {
							URL url = new URL(login_url);
							HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
							httpURLConnection.setRequestMethod("POST");
							httpURLConnection.setDoOutput(true);
							httpURLConnection.setDoInput(true);
							OutputStream outputStream = httpURLConnection.getOutputStream();
							BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8")
									+ "&"
									+ URLEncoder.encode("heart", "UTF-8") + "=" + URLEncoder.encode(record, "UTF-8");
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

						record = "";
						count_num = 0;
					}

					String user_name = "111";
					String user_password = "000";

					String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
					String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
					String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
					String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
					String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
					String heart = Monitor.IHRText.getText().toString();
					String temp = Monitor.TEText.getText().toString();

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
								+ URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(Monitor.StateText.getText().toString(), "UTF-8")
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

			if(account_name[now_patient_id]!=null){
				writeData("data " + server_id[now_patient_id] + " " + "IHR" + " " + (int)Ihr + " " + "\n");
			}
			try{
				if(Ihr > 130.0){
					if(choose==now_patient_id)
						IHRText.setTextColor(Color.RED);
					IhrHighStateCount[now_patient_id]++;
					if(WarnFlag[now_patient_id] == Enable_Warning){
						if(IhrHighStateCount[now_patient_id] >= 10 && temp_name!=""){
							TBefore[now_patient_id] = System.currentTimeMillis();
							Intent IhrHigh = new Intent();
							IhrHigh.setClass(Monitor.this, EAlertDialog.class);
							IhrHigh.putExtra("WarnType", S_IhrTooHigh);
							IhrHigh.putExtra("WarnName", temp_name);
							IhrHigh.putExtra("WarnSmsPhone1", temp_smsphone_1);
							IhrHigh.putExtra("WarnSmsPhone2", temp_smsphone_2);
							IhrHigh.putExtra("WarnSmsPhone3", temp_smsphone_3);
							startActivity(IhrHigh);
							WarnFlag[now_patient_id] = Disable_Warning;
							IhrHighStateCount[now_patient_id] = 0;
						}
					}
					else{
						IhrHighStateCount[now_patient_id] = 0;
						WarnFlag[now_patient_id] = Disable_Warning;
					}
				}
				else if(Ihr < 50.0){
					if(choose==now_patient_id)
						IHRText.setTextColor(Color.RED);
					IhrLowStateCount[now_patient_id]++;
					if(WarnFlag[now_patient_id] == Enable_Warning){
						if(IhrLowStateCount[now_patient_id] >= 10 && temp_name!=""){
							TBefore[now_patient_id] = System.currentTimeMillis();
							Intent IhrLow = new Intent();
							IhrLow.setClass(Monitor.this, EAlertDialog.class);
							IhrLow.putExtra("WarnType", S_IhrTooLow);
							IhrLow.putExtra("WarnName", temp_name);
							IhrLow.putExtra("WarnSmsPhone1", temp_smsphone_1);
							IhrLow.putExtra("WarnSmsPhone2", temp_smsphone_2);
							IhrLow.putExtra("WarnSmsPhone3", temp_smsphone_3);
							startActivity(IhrLow);
							WarnFlag[now_patient_id] = Disable_Warning;
							IhrLowStateCount[now_patient_id] = 0;
						}
					}
					else{
						IhrLowStateCount[now_patient_id] = 0;
						WarnFlag[now_patient_id] = Disable_Warning;
					}
				}
				else{
					if(choose==now_patient_id)
						IHRText.setTextColor(Color.parseColor("#4A7D6B"));
					IhrHighStateCount[now_patient_id] = 0;
					IhrLowStateCount[now_patient_id] = 0;
				}
			}catch(Exception e){

			}
		}
		else if (info[0].equals("TE"))
		{
			//Log.d(TAG, "TE_Data_In");
			part1 = info[1].substring(0,3);
			try{
				double tmp1 = (Double.parseDouble(part1)/10)-4.0;
				//textView4.setText(Double.toString(tmp1));

				if(Button_bth.getText().toString().equals("解除裝置"))
				{
					if(count_num2!=10)
					{
						record2 += tmp1 + " ";
						count_num2++;
					}
					else
					{
						String login_url = "http://192.168.43.190/ECG/add_bth_temp.php";
						String result = "", line;
						try {
							URL url = new URL(login_url);
							HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
							httpURLConnection.setRequestMethod("POST");
							httpURLConnection.setDoOutput(true);
							httpURLConnection.setDoInput(true);
							OutputStream outputStream = httpURLConnection.getOutputStream();
							BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8")
									+ "&"
									+ URLEncoder.encode("temp", "UTF-8") + "=" + URLEncoder.encode(record2, "UTF-8");
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

						record2 = "";
						count_num2 = 0;
					}
				}

				//Toast.makeText(getApplicationContext(), String.valueOf(tmp1), Toast.LENGTH_SHORT).show();

				String[] c = String.valueOf(tmp1).split("00");
				c = c[0].split("99");

				//Toast.makeText(getApplicationContext(), String.valueOf(tmp1) + " " + c, Toast.LENGTH_SHORT).show();

				if(choose==now_patient_id && tmp1<0)
					TEText.setText("--");
				if(choose==now_patient_id && tmp1>=0)
					TEText.setText(c[0]);
				if(account_name[now_patient_id]!=null)
					writeData("data " + server_id[now_patient_id] + " " + "TE" + " " + info[1] + " " + "\n");
				if(tmp1 >37.5){
					TeHighStateCount[now_patient_id]++;
					if(choose==now_patient_id)
						TEText.setTextColor(Color.RED);
					if(WarnFlag[now_patient_id] == Enable_Warning){
						if(TeHighStateCount[now_patient_id]>=10 && temp_name!=""){
							TBefore[now_patient_id] = System.currentTimeMillis();
							Intent TeHigh = new Intent();
							TeHigh.setClass(Monitor.this, EAlertDialog.class);
							TeHigh.putExtra("WarnType", S_BodyTempTooHigh);
							TeHigh.putExtra("WarnName", temp_name);
							TeHigh.putExtra("WarnSmsPhone1", temp_smsphone_1);
							TeHigh.putExtra("WarnSmsPhone2", temp_smsphone_2);
							TeHigh.putExtra("WarnSmsPhone3", temp_smsphone_3);
							startActivity(TeHigh);
							WarnFlag[now_patient_id] = Disable_Warning;
							TeHighStateCount[now_patient_id]=0;
						}
					}
				}
				else{
					TeHighStateCount[now_patient_id]=0;
					if(choose==now_patient_id)
						TEText.setTextColor(Color.parseColor("#FF8800"));
				}
			}catch(Exception e){

			}
		}
	}

	private void WarnMessage(int warntype, int now_patient_id, String temp_name, String temp_smsphone_1, String temp_smsphone_2, String temp_smsphone_3){
		if(account_name[now_patient_id]!=null){
			writeData("data " + server_id[now_patient_id] + " " + "STATE" + " " + warntype + " " + "\n");
		}
		if(warntype==NORM){
			if(now_patient_id == (int) choose){
				StateText.setText("NORM");
				StateText.setTextColor(Color.parseColor("#000088"));
			}

			LBBBStateCount[now_patient_id]--;
			if(LBBBStateCount[now_patient_id]<0)
				LBBBStateCount[now_patient_id]=0;

			RBBBStateCount[now_patient_id]--;
			if(RBBBStateCount[now_patient_id]<0)
				RBBBStateCount[now_patient_id]=0;

			VPCStateCount[now_patient_id]--;
			if(VPCStateCount[now_patient_id]<0)
				VPCStateCount[now_patient_id]=0;

			APCStateCount[now_patient_id]--;
			if(APCStateCount[now_patient_id]<0)
				APCStateCount[now_patient_id]=0;
		}
		else if(warntype==LBBB){
			LBBBStateCount[now_patient_id]++;
			if(LBBBStateCount[now_patient_id]>=3 && now_patient_id == (int) choose){
				StateText.setText("LBBB");
				StateText.setTextColor(Color.RED);
			}

			if(WarnFlag[now_patient_id] == Enable_Warning){
				if(LBBBStateCount[now_patient_id] >= 10 && temp_name!=""){
					TBefore[now_patient_id] = System.currentTimeMillis();
					Intent LBBB_WARN = new Intent();
					LBBB_WARN.setClass(Monitor.this, EAlertDialog.class);
					LBBB_WARN.putExtra("WarnType", S_LBBB);
					LBBB_WARN.putExtra("WarnName", temp_name);
					LBBB_WARN.putExtra("WarnSmsPhone1", temp_smsphone_1);
					LBBB_WARN.putExtra("WarnSmsPhone2", temp_smsphone_2);
					LBBB_WARN.putExtra("WarnSmsPhone3", temp_smsphone_3);
					startActivity(LBBB_WARN);

					WarnFlag[now_patient_id] = Disable_Warning;
					LBBBStateCount[now_patient_id] = 0;
				}
			}
			else{
				LBBBStateCount[now_patient_id] = 0;
				WarnFlag[now_patient_id] = Disable_Warning;
			}
		}
		else if(warntype==RBBB){
			RBBBStateCount[now_patient_id]++;
			if(RBBBStateCount[now_patient_id]>=3 && now_patient_id == (int) choose){
				StateText.setText("RBBB");
				StateText.setTextColor(Color.RED);
			}

			if(WarnFlag[now_patient_id] == Enable_Warning){
				if(RBBBStateCount[now_patient_id] >= 10 && temp_name!=""){
					TBefore[now_patient_id] = System.currentTimeMillis();
					Intent RBBB_WARN = new Intent();
					RBBB_WARN.setClass(Monitor.this, EAlertDialog.class);
					RBBB_WARN.putExtra("WarnType", S_RBBB);
					RBBB_WARN.putExtra("WarnName", temp_name);
					RBBB_WARN.putExtra("WarnSmsPhone1", temp_smsphone_1);
					RBBB_WARN.putExtra("WarnSmsPhone2", temp_smsphone_2);
					RBBB_WARN.putExtra("WarnSmsPhone3", temp_smsphone_3);
					startActivity(RBBB_WARN);

					WarnFlag[now_patient_id] = Disable_Warning;
					RBBBStateCount[now_patient_id] = 0;
				}
			}
			else{
				RBBBStateCount[now_patient_id] = 0;
				WarnFlag[now_patient_id] = Disable_Warning;
			}
		}
		else if(warntype==VPC){
			VPCStateCount[now_patient_id]++;
			if(VPCStateCount[now_patient_id]>=3 && now_patient_id == (int) choose){
				StateText.setText("VPC");
				StateText.setTextColor(Color.RED);
			}

			if(WarnFlag[now_patient_id] == Enable_Warning && temp_name!=""){
				if(VPCStateCount[now_patient_id] >= 10){
					TBefore[now_patient_id] = System.currentTimeMillis();
					Intent VPC_WARN = new Intent();
					VPC_WARN.setClass(Monitor.this, EAlertDialog.class);
					VPC_WARN.putExtra("WarnType", S_VPC);
					VPC_WARN.putExtra("WarnName", temp_name);
					VPC_WARN.putExtra("WarnSmsPhone1", temp_smsphone_1);
					VPC_WARN.putExtra("WarnSmsPhone2", temp_smsphone_2);
					VPC_WARN.putExtra("WarnSmsPhone3", temp_smsphone_3);
					startActivity(VPC_WARN);

					WarnFlag[now_patient_id] = Disable_Warning;
					VPCStateCount[now_patient_id] = 0;
				}
			}
			else{
				VPCStateCount[now_patient_id] = 0;
				WarnFlag[now_patient_id] = Disable_Warning;
			}
		}
		else if(warntype==APC){
			APCStateCount[now_patient_id]++;
			if(APCStateCount[now_patient_id]>=3 && now_patient_id == (int) choose){
				StateText.setText("APC");
				StateText.setTextColor(Color.RED);
			}

			if(WarnFlag[now_patient_id] == Enable_Warning && temp_name!=""){
				if(APCStateCount[now_patient_id] >= 10){
					TBefore[now_patient_id] = System.currentTimeMillis();
					Intent APC_WARN = new Intent();
					APC_WARN.setClass(Monitor.this, EAlertDialog.class);
					APC_WARN.putExtra("WarnType", S_APC);
					APC_WARN.putExtra("WarnName", temp_name);
					APC_WARN.putExtra("WarnSmsPhone1", temp_smsphone_1);
					APC_WARN.putExtra("WarnSmsPhone2", temp_smsphone_2);
					APC_WARN.putExtra("WarnSmsPhone3", temp_smsphone_3);
					startActivity(APC_WARN);

					WarnFlag[now_patient_id] = Disable_Warning;
					APCStateCount[now_patient_id] = 0;
				}
			}
			else{
				APCStateCount[now_patient_id] = 0;
				WarnFlag[now_patient_id] = Disable_Warning;
			}
		}
	}

	// startActivityForResult 取得activity 的resultCode後會進入此函式
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(D) Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
			case REQUEST_CONNECT_DEVICE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK) {
					// Get the device MAC address
					String address = data.getExtras()
							.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					// Get the BLuetoothDevice object
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
					// Attempt to connect to the device
					mBTService.setPatientId(choose);
					mBTService.connect(device);
				}
				break;
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					setupMonitor();
				} else {
					// User did not enable Bluetooth or an error occured
					Log.d(TAG, "BT not enabled");
					Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
					finish();
				}
				break;

			case REQUEST_LOG_IN:
				if (resultCode == Activity.RESULT_OK) {
					String login = data.getExtras().getString(SynchLogin.LOGIN);
					if(login.equals("1")){
						String username = data.getExtras().getString(SynchLogin.USERNAME);
						String password = data.getExtras().getString(SynchLogin.PASSWORD);
						writeData("login " + choose + " " + username + " " + password + " " + "\n");
					}
					else{
						String username = data.getExtras().getString(SynchLogin.USERNAME);
						String password = data.getExtras().getString(SynchLogin.PASSWORD);
						String pin = data.getExtras().getString(SynchLogin.PIN);
						writeData("create " + username + " " + password + " " + pin + " " + "\n");
					}
				}
				break;
			case REQUEST_CONNECT_INTERNET:
				if (resultCode == Activity.RESULT_OK) {
					String username = data.getExtras().getString(ConnectInternet.USERNAME);
					String pin = data.getExtras().getString(ConnectInternet.PIN);
					writeData("connect " + choose + " " + username + " " + pin + " " + "\n");
				}
				break;
			case REQUEST_SIMULATE:
				if (resultCode == Activity.RESULT_OK) {
					String state = data.getExtras().getString(Simulate.EXTRA_STATE);
					//Toast.makeText(getApplicationContext(), id + "&" + state, Toast.LENGTH_SHORT).show();
					mChartView.ClearChart();
					IHRText.setText("0");
					IHRText.setTextColor(Color.parseColor("#4A7D6B"));
					TEText.setText("0.0");
					TEText.setTextColor(Color.parseColor("#FF8800"));
					StateText.setText("--");
					StateText.setTextColor(Color.parseColor("#000088"));
					WarnFlag[(int) choose] = Enable_Warning;
					TBefore[(int) choose] = 0;

					//當關閉模擬訊號時，檢查是否有藍芽連線，沒有則再檢查是否同步，如果有則取消同步
					Boolean check = false;
					if(simulate_state[(int) choose]>=0 && Integer.parseInt(state)==-1){
						simulate_state[(int) choose] = Integer.parseInt(state);
						for(int i=0; i<connected_num; i++){
							if(connected_id[i]==(int) choose){
								check = true;
								break;
							}
						}
						if(check == false){
							if(account_name[(int) choose]!=null){
								writeData("logout " + server_id[(int) choose] + " " + "\n");
							}
						}
					}
					else{
						simulate_state[(int) choose] = Integer.parseInt(state);
					}
				}
				break;
		}
	}

	private void writeData(String s){
		try {
			if(clientSocket.isConnected()){
				BufferedWriter bw;
				try {
					// 取得網路輸出串流
					bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream()));

					// 寫入訊息
					bw.write(s);

					// 立即發送
					bw.flush();
				} catch (IOException e) {

				}

			}
			else{
				if(SERVER_CONNECTED==true){
					Toast.makeText(getApplicationContext(), "無法連線到Server，請稍後重新啟動本程式重試", Toast.LENGTH_SHORT).show();
					for(int i=0; i<1024; i++){
						account_name[i]=null;
						server_id[i]=-1;
						connect_name[i]=null;
					}
					SERVER_CONNECTED=false;
				}
			}
		} catch (Exception e) {
			if(SERVER_CONNECTED==true){
				Toast.makeText(getApplicationContext(), "無法連線到Server，請稍後重新啟動本程式重試", Toast.LENGTH_SHORT).show();
				for(int i=0; i<1024; i++){
					account_name[i]=null;
					server_id[i]=-1;
					connect_name[i]=null;
				}
				SERVER_CONNECTED=false;
			}
		}
	}

	// 取得網路資料
	private Runnable readData = new Runnable() {
		public void run() {
			// server端的IP
			InetAddress serverIp;

			try {
				// 以內定(本機電腦端)IP為Server端
				serverIp = InetAddress.getByName(SERVER_IP);
				int serverPort = 5050;
				clientSocket = new Socket(serverIp, serverPort);

				// 取得網路輸入串流
				BufferedReader br = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));

				// 當連線後
				while (clientSocket.isConnected()) {
					// 取得網路訊息
					tmp = br.readLine();

					// 如果不是空訊息則讀取資料
					if(tmp!=null){
						while(SERVER_STRING!=null){

						}
						SERVER_STRING = tmp;
						tmp = null;
					}
				}

			} catch (IOException e) {

			}
		}
	};

	private void ServerString(String s){
		//Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
		String[] S = s.split(" ");
		if(S[0].equals("create")){
			if(S[1].equals("ok"))
				Toast.makeText(this, "新增帳號成功", Toast.LENGTH_SHORT).show();
			else if(S[1].equals("same"))
				Toast.makeText(this, "帳號名稱已經有人使用，請換另一個名稱", Toast.LENGTH_SHORT).show();
		}
		if(S[0].equals("login")){
			if(S[3].equals("ok")){
				Toast.makeText(this, "登入成功", Toast.LENGTH_SHORT).show();
				account_name[Integer.parseInt(S[1])] = S[2];
				server_id[Integer.parseInt(S[1])] = Integer.parseInt(S[4]);
			}
			else if(S[3].equals("error"))
				Toast.makeText(this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
			else if(S[3].equals("loggedin")){
				Toast.makeText(this, "登入成功 (此帳號已經在他處同步，對方將解除同步)", Toast.LENGTH_SHORT).show();
				account_name[Integer.parseInt(S[1])] = S[2];
				server_id[Integer.parseInt(S[1])] = Integer.parseInt(S[4]);
			}
		}
		if(S[0].equals("data")){
			//Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
			if(S[2].equals("ECG")){
				if(choose==Long.parseLong(S[1])){
					byte[] RawBuf = new byte[S.length-3];
					for(int i=3; i<S.length; i++){
						RawBuf[i-3] = (byte) Integer.parseInt(S[i]);
					}
					if(simulate_state[(int) choose]==-1)
						mChartView.Wave_Draw(RawBuf);
				}
			}
			else if(S[2].equals("IHR")){
				String[] info = new String[2];
				info[0]=S[2];
				info[1]=S[3];

				long Tpass = (TAfter[Integer.parseInt(S[1])] - TBefore[Integer.parseInt(S[1])])/1000;

				DatabaseConnector databaseConnector = new DatabaseConnector(Monitor.this);
				databaseConnector.open();
				Cursor result = databaseConnector.getOneContact(Long.parseLong(S[1]));
				result.moveToFirst();
				int nameIndex = result.getColumnIndex("name");
				int smsphoneIndex1 = result.getColumnIndex("smsphone1");
				int smsphoneIndex2 = result.getColumnIndex("smsphone2");
				int smsphoneIndex3 = result.getColumnIndex("smsphone3");
				String temp_name=result.getString(nameIndex);
				String temp_smsphone_1=result.getString(smsphoneIndex1);
				String temp_smsphone_2=result.getString(smsphoneIndex2);
				String temp_smsphone_3=result.getString(smsphoneIndex3);
				databaseConnector.close();

				InfoMessage(Tpass, Integer.parseInt(S[1]), info, temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);
			}
			else if(S[2].equals("TE")){
				String[] info = new String[2];
				info[0]=S[2];
				info[1]=S[3];

				long Tpass = (TAfter[Integer.parseInt(S[1])] - TBefore[Integer.parseInt(S[1])])/1000;

				DatabaseConnector databaseConnector = new DatabaseConnector(Monitor.this);
				databaseConnector.open();
				Cursor result = databaseConnector.getOneContact(Long.parseLong(S[1]));
				result.moveToFirst();
				int nameIndex = result.getColumnIndex("name");
				int smsphoneIndex1 = result.getColumnIndex("smsphone1");
				int smsphoneIndex2 = result.getColumnIndex("smsphone2");
				int smsphoneIndex3 = result.getColumnIndex("smsphone3");
				String temp_name=result.getString(nameIndex);
				String temp_smsphone_1=result.getString(smsphoneIndex1);
				String temp_smsphone_2=result.getString(smsphoneIndex2);
				String temp_smsphone_3=result.getString(smsphoneIndex3);
				databaseConnector.close();

				InfoMessage(Tpass, Integer.parseInt(S[1]), info, temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);
			}
			else if(S[2].equals("STATE")){

				DatabaseConnector databaseConnector = new DatabaseConnector(Monitor.this);
				databaseConnector.open();
				Cursor result = databaseConnector.getOneContact(Long.parseLong(S[1]));
				result.moveToFirst();
				int nameIndex = result.getColumnIndex("name");
				int smsphoneIndex1 = result.getColumnIndex("smsphone1");
				int smsphoneIndex2 = result.getColumnIndex("smsphone2");
				int smsphoneIndex3 = result.getColumnIndex("smsphone3");
				String temp_name=result.getString(nameIndex);
				String temp_smsphone_1=result.getString(smsphoneIndex1);
				String temp_smsphone_2=result.getString(smsphoneIndex2);
				String temp_smsphone_3=result.getString(smsphoneIndex3);
				databaseConnector.close();

				WarnMessage(Integer.parseInt(S[3]), Integer.parseInt(S[1]), temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);
			}
		}
		if(S[0].equals("logout")){
			long id=0;
			for(int i=0; i<1024; i++){
				if(server_id[i]==Integer.parseInt(S[1])){
					server_id[i]=-1;
					account_name[i]=null;
					id=(long) i;
					break;
				}
			}
			if(id == (int) choose){
				mChartView.ClearChart();
				IHRText.setText("0");
				IHRText.setTextColor(Color.parseColor("#4A7D6B"));
				TEText.setText("0.0");
				TEText.setTextColor(Color.parseColor("#FF8800"));
				StateText.setText("--");
				StateText.setTextColor(Color.parseColor("#000088"));
			}
			DatabaseConnector databaseConnector = new DatabaseConnector(Monitor.this);
			databaseConnector.open();
			Cursor result = databaseConnector.getOneContact(id);
			result.moveToFirst();
			int nameIndex = result.getColumnIndex("name");
			String name = result.getString(nameIndex);
			result.close();
			databaseConnector.close();

			Toast.makeText(getApplicationContext(), name + " 已經解除同步", Toast.LENGTH_SHORT).show();
		}
		if(S[0].equals("connect")){
			if(S[3].equals("ok")){
				Toast.makeText(this, "連結成功", Toast.LENGTH_SHORT).show();
				connect_name[Integer.parseInt(S[1])] = S[2];
			}
			else if(S[3].equals("error"))
				Toast.makeText(this, "連結失敗", Toast.LENGTH_SHORT).show();
		}
		if(S[0].equals("disconnect")){
			connect_name[Integer.parseInt(S[1])]=null;

			DatabaseConnector databaseConnector = new DatabaseConnector(Monitor.this);
			databaseConnector.open();
			Cursor result = databaseConnector.getOneContact(Long.parseLong(S[1]));
			result.moveToFirst();
			int nameIndex = result.getColumnIndex("name");
			String name = result.getString(nameIndex);
			result.close();
			databaseConnector.close();

			if(Long.parseLong(S[1]) == choose){
				mChartView.ClearChart();
				IHRText.setText("0");
				IHRText.setTextColor(Color.parseColor("#4A7D6B"));
				TEText.setText("0.0");
				TEText.setTextColor(Color.parseColor("#FF8800"));
				StateText.setText("--");
				StateText.setTextColor(Color.parseColor("#000088"));
			}

			Toast.makeText(getApplicationContext(), name + " 已經解除連結網路", Toast.LENGTH_SHORT).show();
		}
	}

	private void openAboutDlg()
	{
		new AlertDialog.Builder(this)
				.setTitle(R.string.about_title)
				.setMessage(R.string.about_msg)
				.setPositiveButton(R.string.ok_label,
						new DialogInterface.OnClickListener(){public void onClick(
								DialogInterface dialoginterface,int i){}})
				.show();

	}

	private class GetContactsTask extends AsyncTask<Object, Object, Cursor>
	{
		DatabaseConnector databaseConnector =
				new DatabaseConnector(Monitor.this);

		// perform the database access
		@Override
		protected Cursor doInBackground(Object... params)
		{
			databaseConnector.open();

			// get a cursor containing call contacts
			return databaseConnector.getAllContacts();
		} // end method doInBackground

		// use the Cursor returned from the doInBackground method
		@Override
		protected void onPostExecute(Cursor result)
		{
			contactAdapter.changeCursor(result); // set the adapter's Cursor
			databaseConnector.close();
		} // end method onPostExecute
	} // end class GetContactsTask


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add:
				Intent addNewContact = new Intent().setClass(this, AddEditContact.class);
				Monitor.this.startActivity(addNewContact);
				return true; // call super's method
			case R.id.discoverable:
				// Ensure this device is discoverable by others
				//ensureDiscoverable();
				openAboutDlg();
				return true;
			case R.id.hospital:
				Intent Hospital;
				Hospital = new Intent().setClass(this, Hospital.class);
				Monitor.this.startActivity(Hospital);
				return true;
		}
		return false;
	}

	public void updatechoose(CharSequence text, CharSequence text2, CharSequence text3, CharSequence text4) {
		mText = text;
		mText2 = text2;
		mText3 = text3;
		mText4 = text4;
		mHandler.sendEmptyMessage(0);
	}

	private class LoadContactTask extends AsyncTask<Long, Object, Cursor>
	{
		DatabaseConnector databaseConnector =
				new DatabaseConnector(Monitor.this);

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
		protected void onPostExecute(Cursor result)	//切換病患
		{
			super.onPostExecute(result);

			result.moveToFirst(); // move to the first item

			// get the column index for each data item
			int nameIndex = result.getColumnIndex("name");
			/*int i;
			for(i=0; i<connected_num; i++){
				if(connected_id[i]==choose){
					if(account_name[(int) choose]!=null)
						updatechoose(result.getString(nameIndex), "已連結到 "+device_name[i], "解除裝置", "已同步到" + account_name[(int) choose]);
					else
						updatechoose(result.getString(nameIndex), "已連結到 "+device_name[i], "解除裝置", "尚未同步");
					break;
				}
			}
			if(i==connected_num){
				if(connect_name[(int) choose]!=null)
					updatechoose(result.getString(nameIndex), "已連結到 " + connect_name[(int) choose], "連結裝置", "尚未同步");
				else
					updatechoose(result.getString(nameIndex), "尚未連結", "連結裝置", "尚未同步");
			}*/

			textView.setText(result.getString(nameIndex));

			String name = textView.getText().toString();
			String result1 = "", line;
			String login_url = "http://192.168.43.190/ECG/find_patient.php";
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
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				while((line = bufferedReader.readLine())!=null) result1 += line;
				bufferedReader.close();
				inputStream.close();
				httpURLConnection.disconnect();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(result1.equals("not exist"))
			{
				Button_inform.setEnabled(true);
				Button_bth.setEnabled(true);
				Button_synch.setEnabled(false);
				Button_internet.setEnabled(true);
				Button_simulate.setEnabled(true);
				IHRText.setText("0");
				IHRText.setTextColor(Color.parseColor("#4A7D6B"));
				TEText.setText("0.0");
				TEText.setTextColor(Color.parseColor("#FF8800"));
				StateText.setText("--");
				StateText.setTextColor(Color.parseColor("#000088"));

				login_url = "http://192.168.43.190/ECG/add_patient_id.php";
				result1 = "";
				try {
					URL url = new URL(login_url);
					HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
					httpURLConnection.setRequestMethod("POST");
					httpURLConnection.setDoOutput(true);
					httpURLConnection.setDoInput(true);
					OutputStream outputStream = httpURLConnection.getOutputStream();
					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					String post_data = URLEncoder.encode("patient_name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8")
							+ "&"
							+ URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(Long.toString(choose), "UTF-8");
					bufferedWriter.write(post_data);
					bufferedWriter.flush();
					bufferedWriter.close();
					outputStream.close();
					InputStream inputStream = httpURLConnection.getInputStream();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
					while((line = bufferedReader.readLine())!=null) result1 += line;
					bufferedReader.close();
					inputStream.close();
					httpURLConnection.disconnect();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String user_name = "111";
				String user_password = "000";
				String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
				String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
				String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
				String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
				String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";

				login_url = "http://192.168.43.190/ECG/add_patient_status.php";
				result1 = "";
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
							+ URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(Monitor.StateText.getText().toString(), "UTF-8")
							+ "&"
							+ URLEncoder.encode("patient_heartbeat_number", "UTF-8") + "=" + URLEncoder.encode("0", "UTF-8")
							+ "&"
							+ URLEncoder.encode("patient_temperature", "UTF-8") + "=" + URLEncoder.encode("0.0", "UTF-8")
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
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
					while((line = bufferedReader.readLine())!=null) result1 += line;
					bufferedReader.close();
					inputStream.close();
					httpURLConnection.disconnect();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else
			{
				Button_inform.setEnabled(true);
				Button_simulate.setEnabled(true);

				login_url = "http://192.168.43.190/ECG/add_patient_id.php";
				result1 = "";
				try {
					URL url = new URL(login_url);
					HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
					httpURLConnection.setRequestMethod("POST");
					httpURLConnection.setDoOutput(true);
					httpURLConnection.setDoInput(true);
					OutputStream outputStream = httpURLConnection.getOutputStream();
					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					String post_data = URLEncoder.encode("patient_name", "UTF-8") + "=" + URLEncoder.encode(Monitor.textView.getText().toString(), "UTF-8")
							+ "&"
							+ URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(Long.toString(choose), "UTF-8");
					bufferedWriter.write(post_data);
					bufferedWriter.flush();
					bufferedWriter.close();
					outputStream.close();
					InputStream inputStream = httpURLConnection.getInputStream();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
					while((line = bufferedReader.readLine())!=null) result1 += line;
					bufferedReader.close();
					inputStream.close();
					httpURLConnection.disconnect();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				login_url = "http://192.168.43.190/ECG/get_patient_status.php";
				result1 = "";
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
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
					//StringBuilder stringBuilder = new StringBuilder();
					while((line = bufferedReader.readLine())!=null) result1 += line;
					bufferedReader.close();
					inputStream.close();
					httpURLConnection.disconnect();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String get[] = result1.split(" ");

				if(get[1].equals("NORM") || get[1].equals("--"))
				{
					StateText.setText(get[1]);
					StateText.setTextColor(Color.parseColor("#000088"));
				}
				else
				{
					StateText.setText(get[1]);
					StateText.setTextColor(Color.RED);
				}

				if(!get[1].equals("--"))
				{
					int status = -1;

					if(get[1].equals("NORM")) status = 0;
					else if(get[1].equals("LBBB")) status = 1;
					else if(get[1].equals("RBBB")) status = 2;
					else if(get[1].equals("VPC")) status = 3;
					else if(get[1].equals("APC")) status = 4;

					update(choose, status);
				}

				if(get[3].equals("36.0")) IHRText.setText("60");
				else IHRText.setText(get[2]);
				TEText.setText(get[3]);
				if(get[4].equals("false"))
				{
					Button_internet.setText("連結網路");
					Button_internet.setEnabled(true);
					textView3.setText("尚未連結");
				}
				else
				{
					Button_internet.setText("斷開連線");
					Button_internet.setEnabled(true);
					textView3.setText("login successed");
				}
				if(get[6].equals("false"))
				{
					if(Button_internet.getText().toString().equals("斷開連線") && !StateText.getText().toString().equals("--"))
					{
						Button_synch.setText("線上同步");
						Button_synch.setEnabled(true);
						accountView.setText("尚未同步");
					}
					else
					{
						Button_synch.setText("線上同步");
						Button_synch.setEnabled(false);
						accountView.setText("尚未同步");
					}
				}
				else
				{
					if(Button_internet.getText().toString().equals("斷開連線"))
					{
						Button_synch.setText("解除同步");
						Button_synch.setEnabled(true);
						accountView.setText("已同步");
					}
					else
					{
						Button_synch.setText("線上同步");
						Button_synch.setEnabled(false);
						accountView.setText("尚未同步");
					}
				}

				if(get[8].equals("false"))
				{
					Button_bth.setText("連結裝置");
					Button_bth.setEnabled(true);
					if(!Monitor.Button_internet.getText().toString().equals("斷開連線")) textView3.setText("尚未連結");
					else textView3.setText("login successed");
				}
				else
				{
					Button_bth.setEnabled(true);

					int i, j;
					for(i=0; i<connected_num; i++){
						if(connected_id[i]==choose){
							textView3.setText("已連結到 8Z027");
							Button_bth.setText("解除裝置");
							break;
						}
					}
					if(i==connected_num){
						Intent serverIntent = new Intent(Monitor.this, DeviceListActivity.class);
						serverIntent.putExtra(ROW_ID, choose);
						startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
					}
				}

				String user_name = "111";
				String user_password = "000";

				String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
				String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
				String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
				String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
				String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
				String heart = Monitor.StateText.getText().toString().equals("--") ? "0" : "60";
				String temp = Monitor.StateText.getText().toString().equals("--") ? "0.0" : "36.0";

				login_url = "http://192.168.43.190/ECG/add_patient_status.php";
				result1 = "";
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
							+ URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(Monitor.StateText.getText().toString(), "UTF-8")
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
					while((line = bufferedReader.readLine())!=null) result1 += line;
					bufferedReader.close();
					inputStream.close();
					httpURLConnection.disconnect();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// fill TextViews with the retrieved data

			result.close(); // close the result cursor
			databaseConnector.close(); // close database connection
		} // end method onPostExecute
	} // end class LoadContactTask


	OnItemClickListener viewContactListener = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
								long arg3)
		{
			choose = arg3;
			mChartView.ClearChart();
			/*Button_inform.setEnabled(true);
			Button_bth.setEnabled(true);
			Button_synch.setEnabled(false);
			Button_internet.setEnabled(true);
			Button_simulate.setEnabled(true);
			IHRText.setText("0");
			IHRText.setTextColor(Color.parseColor("#4A7D6B"));
			TEText.setText("0.0");
			TEText.setTextColor(Color.parseColor("#FF8800"));
			StateText.setText("--");
			StateText.setTextColor(Color.parseColor("#000088"));*/
			new LoadContactTask().execute(arg3);
		} // end method onItemClick
	};

	public void update(long choose_temp, int status_temp)
	{
		delay = (delay + 1)%1000;
		if(delay==0 || delay==500){
			byte []Cmd = new byte []{0x0D};
			sendCmd(Cmd);
			Cmd = new byte []{'W','+',0x0D};
			sendCmd(Cmd);
		}

		for(int i = 0; i < 1024; i++)
		{
			if(choose_temp == i)
			{
				simulate_state[i] = status_temp;
				break;
			}
		}

		if(delay==0 || delay==250 || delay==500 || delay==750){
			for(int i=0; i<1024; i++){
				if(simulate_state[i]>=0){
					if(account_name[i]!=null){
						String s = "";
						for(int j=0; j<simulate_ECG[simulate_state[i]][(delay+offset)/250].length; j++){
							s = s + simulate_ECG[simulate_state[i]][(delay+offset)/250][j] + " ";
						}
						writeData("data" + " "  + server_id[i] + " " + "ECG" + " " + s + " " + "\n");
					}
					if(choose==(int) i){
						byte[] b = new byte[simulate_ECG[simulate_state[i]][(delay+offset)/250].length];
						for(int j=0; j<simulate_ECG[simulate_state[i]][(delay+offset)/250].length; j++){
							b[j] = (byte)Integer.parseInt(simulate_ECG[simulate_state[i]][(delay+offset)/250][j]);
						}
						mChartView.Wave_Draw(b);
					}

					String temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3;

					DatabaseConnector databaseConnector = new DatabaseConnector(Monitor.this);
					databaseConnector.open();
					Cursor result = databaseConnector.getOneContact(i);
					result.moveToFirst();
					int nameIndex = result.getColumnIndex("name");
					int smsphoneIndex1 = result.getColumnIndex("smsphone1");
					int smsphoneIndex2 = result.getColumnIndex("smsphone2");
					int smsphoneIndex3 = result.getColumnIndex("smsphone3");
					temp_name=result.getString(nameIndex);
					temp_smsphone_1=result.getString(smsphoneIndex1);
					temp_smsphone_2=result.getString(smsphoneIndex2);
					temp_smsphone_3=result.getString(smsphoneIndex3);
					databaseConnector.close();

					String[] info = new String[2];

					TAfter[i] = System.currentTimeMillis();
					long Tpass = (TAfter[i] - TBefore[i])/1000;

					info[0] = "IHR";
					info[1] = "060";
					InfoMessage(Tpass, i, info, temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);

					info[0] = "TE";
					info[1] = "40000";
					InfoMessage(Tpass, i, info, temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);

					WarnMessage(simulate_state[i], i, temp_name, temp_smsphone_1, temp_smsphone_2, temp_smsphone_3);
				}
			}
			if(delay==750)
				offset=(offset+1000)%2000;
		}
	}
}

