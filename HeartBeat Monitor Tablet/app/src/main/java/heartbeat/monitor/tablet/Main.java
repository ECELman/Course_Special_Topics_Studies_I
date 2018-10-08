package heartbeat.monitor.tablet;

import heartbeat.monitor.tablet.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.util.List;
import android.util.Log;


import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {

    private static List<String> list = null;
    private DragListAdapter adapter = null;
    
    public static List<String> groupKey= new ArrayList<String>();
    private List<String> WaveOnList = new ArrayList<String>();
    private List<String> WaveOffList = new ArrayList<String>();
    
    private DBHelper DH = null;
    
    //偵測事件的頻率有不同，則設定計數器，讓頻率比較低的可以每隔幾次才做一次
    private static int delay_count = 0;
    
    //模擬訊號的計數器，計算顯示到第幾組
    private static int offset = 0;
    
    //Server IP
    private static final String SERVER_IP = "140.127.218.96";
    
    //Server連線狀態
	private Boolean SERVER_CONNECTED = true;
    
    //客戶端socket
    private static Socket clientSocket;
    
    //收到的字串暫存
    private static String tmp;
    
    //收到的字串
    public static String SERVER_STRING = null;
    
    //錯誤訊息
    private static final boolean D = true;
    private static final String TAG = "HeartBeat Monitor";
    
    //藍芽連線狀態
    private static final int OPTION_LIST = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    //藍芽連線控制
    private BluetoothAdapter mBluetoothAdapter = null;

    //溝通藍芽的object
    private BluetoothService mBTService = null;
    
    //藍芽連線訊息分類
    public static final int MESSAGE_DEVICE_CONNECTED = 1;
    public static final int MESSAGE_DEVICE_DISCONNECTED = 2;
    public static final int MESSAGE_ID= 3;
    public static final int MESSAGE_READ = 4;
    public static final int MESSAGE_WRITE = 5;
    public static final int MESSAGE_TOAST = 6;
    
    //傳遞資料
    public static final String DEVICE_NAME = "device_name";
    public static final String PATIENT_ID = "patient_id";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String PATIENT_DATA = "patient_data";
    public static final String TOAST = "toast";
    public static final String WARN_TYPE = "warn_type";
    public static final String KY_INFO = "KY_Info";
    public static final String DRAW_ID = "draw_id";
    public static final String UPDATE_STRING = "update_string";
    
    //儲存現在接收的資料是屬於哪個病人的ID
    private int patinet_data_id;
    
    //處理心電圖訊息分類
    public static final int MESSAGE_RAW_1 = 1;
    public static final int MESSAGE_RAW_2 = 2;
    public static final int MESSAGE_RAW_3 = 3;
    public static final int MESSAGE_RAW_4 = 4;
    public static final int MESSAGE_INFO = 5;
    public static final int MESSAGE_STATE = 6;
    public static final int MESSAGE_UPDATE = 7;

    public static final int REQUEST_SYN = 10;
    
    //心電圖訊號處理
    private ECGService mECGService = null;
    
    //右側四格TextView
    private TextView p1_no;			//第1格編號
    private TextView p1_name;		//第1格姓名
    private TextView p1_connect;	//第1格連線狀態
    private TextView p1_synch;		//第1格同步狀態
    private TextView p1_Ihr;		//第1格心跳
    private TextView p1_Te;			//第1格體溫
    private TextView p1_state;		//第1格狀態
    private TextView p1_state_word;	//第1格狀態標誌
    
    private TextView p2_no;			//第2格編號
    private TextView p2_name;		//第2格姓名
    private TextView p2_connect;	//第2格連線狀態
    private TextView p2_synch;		//第2格同步狀態
    private TextView p2_Ihr;		//第2格心跳
    private TextView p2_Te;			//第2格體溫
    private TextView p2_state;		//第2格狀態
    private TextView p2_state_word;	//第2格狀態標誌
    
    private TextView p3_no;			//第3格編號
    private TextView p3_name;		//第3格姓名
    private TextView p3_connect;	//第3格連線狀態
    private TextView p3_synch;		//第3格同步狀態
    private TextView p3_Ihr;		//第3格心跳
    private TextView p3_Te;			//第3格體溫
    private TextView p3_state;		//第3格狀態
    private TextView p3_state_word;	//第3格狀態標誌
    
    private TextView p4_no;			//第4格編號
    private TextView p4_name;		//第4格姓名
    private TextView p4_connect;	//第4格連線狀態
    private TextView p4_synch;		//第4格同步狀態
    private TextView p4_Ihr;		//第4格心跳
    private TextView p4_Te;			//第4格體溫
    private TextView p4_state;		//第4格狀態
    private TextView p4_state_word;	//第4格狀態標誌
    
    //右側四格畫心電圖
    private ChartView1 mChartView1;
    private ChartView2 mChartView2;
    private ChartView3 mChartView3;
    private ChartView4 mChartView4;
    
    //右下方頁數
    private TextView page;			//顯示頁數
    private Button back;			//上一頁
    private Button next;			//下一頁
    private Button syn_patient;     //同步病患
    
    //病患資料表
    private int patient_num;	//patient個數
    private int patient_wave_on_num;	//patient的心電圖是on個數
    private int patient_wave_off_num;	//patient的心電圖是off個數
    private int [] patient_id = new int[1024];
    private String [] patient_name = new String[1024];
    private String [] patient_email = new String[1024];
    private String [] patient_phone = new String[1024];
    private String [] patient_sms_phone_1 = new String[1024];
    private String [] patient_sms_phone_2 = new String[1024];
    private String [] patient_sms_phone_3 = new String[1024];
    private String [] patient_address = new String[1024];
    private String [] patient_note = new String[1024];
    private int [] patient_wave = new int[1024];	//每個patient的心電圖是on還是off，on=1, off=0
    private int [] patient_order = new int[1024];	//每個patient的心電圖在on/off的排序
    
    //連線狀態
    private int connected_num;
    private Boolean [] bth_connected = new Boolean[1024];		//儲存是否連線藍芽裝置，以patient_id對應
    private String [] bth_name = new String[1024];				//儲存連線藍芽裝置名稱，以patient_id對應
    private String [] bth_address = new String[1024];			//儲存連線藍芽裝置MAC，以patient_id對應
    private String [] synch_username = new String[1024];	//儲存同步的帳號名稱，以patient_id對應
    private int [] synch_server_id = new int[1024];				//儲存同步的帳號在Server上的ID，以patient_id對應
    private String [] connect_username = new String[1024];	//儲存連線的帳號名稱，以patient_id對應
    private int [] simulate_state = new int[1024];				//儲存模擬的心跳種類，以patient_id對應
    
    //右側病患id
    private int [] patient_wave_id = new int[4];
    
	//狀態計數器，累積一定次數後發出警告
    private int [] IhrHighStateCount = new int[1024];
    private int [] IhrLowStateCount = new int[1024];
    private int [] TeHighStateCount = new int[1024];
    private int [] LBBBStateCount = new int[1024];
    private int [] RBBBStateCount = new int[1024];
    private int [] VPCStateCount = new int[1024];
    private int [] APCStateCount = new int[1024]; 
    
    //儲存上次和這次的警告時間
    private long [] TBefore = new long[1024];
    private long [] TAfter = new long[1024];
    
    //判斷是否跳出警告視窗
    private int [] WarnFlag = new int[1024];
    private static final int Enable_Warning = 0;
    private static final int Disable_Warning = 1;
    
    //警告類型
    public static final String S_IhrHigh = "IhrHigh";
    public static final String S_IhrLow = "IhrLow";
    public static final String S_TeHigh = "BTHigh";
    public static final String S_LBBB = "LBBB";
    public static final String S_RBBB = "RBBB";
    public static final String S_VPC = "VPC";
    public static final String S_APC = "APC";
    
    //心跳狀態
    public static final int NORM = 0;
    public static final int LBBB = 1;
    public static final int RBBB = 2;
    public static final int VPC = 3;
    public static final int APC = 4;
    
    //頁數資訊
    private int total_page;
    private int now_page = 1;
    
    //OptionList訊息傳遞
    public static String select_id = "select_id";
    public static String select_name = "select_name";
    public static String select_bth_address = "bth_address";
    public static String select_server_id = "server_id";
    public static String select_connect_name = "connect_name";
    public static String select_simulate_state = "simulate_state";
    
    //防錯檢查變數
    Boolean OptionOpened = false;		//檢查Option List是否已經開啟，避免同時開很多個
    
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

    /*public void state_check(int now_patient_id, int Ihr, int tmp1, String temp_name)
    {
        if(Ihr > 130.0){		//心跳過快
            if(now_patient_id==patient_wave_id[0])
                p1_Ihr.setTextColor(Color.RED);
            if(now_patient_id==patient_wave_id[1])
                p2_Ihr.setTextColor(Color.RED);
            if(now_patient_id==patient_wave_id[2])
                p3_Ihr.setTextColor(Color.RED);
            if(now_patient_id==patient_wave_id[3])
                p4_Ihr.setTextColor(Color.RED);

            IhrHighStateCount[now_patient_id]++;

            //判斷是否發出警告
            if(WarnFlag[now_patient_id] == Enable_Warning){
                if(IhrHighStateCount[now_patient_id] >= 10 && temp_name!=""){
                    TBefore[now_patient_id] = System.currentTimeMillis();
                    Intent IhrHigh = new Intent();
                    IhrHigh.setClass(Main.this, EAlertDialog.class);
                    IhrHigh.putExtra("WarnType", S_IhrHigh);
                    IhrHigh.putExtra("WarnName", temp_name);
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
        else if(Ihr < 60.0){
            if(now_patient_id==patient_wave_id[0])
                p1_Ihr.setTextColor(Color.RED);
            if(now_patient_id==patient_wave_id[1])
                p2_Ihr.setTextColor(Color.RED);
            if(now_patient_id==patient_wave_id[2])
                p3_Ihr.setTextColor(Color.RED);
            if(now_patient_id==patient_wave_id[3])
                p4_Ihr.setTextColor(Color.RED);

            IhrLowStateCount[now_patient_id]++;

            //判斷是否發出警告
            if(WarnFlag[now_patient_id] == Enable_Warning){
                if(IhrLowStateCount[now_patient_id] >= 10 && temp_name!=""){
                    TBefore[now_patient_id] = System.currentTimeMillis();
                    Intent IhrLow = new Intent();
                    IhrLow.setClass(Main.this, EAlertDialog.class);
                    IhrLow.putExtra("WarnType", S_IhrLow);
                    IhrLow.putExtra("WarnName", temp_name);
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
            if(now_patient_id==patient_wave_id[0])
                p1_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
            if(now_patient_id==patient_wave_id[1])
                p2_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
            if(now_patient_id==patient_wave_id[2])
                p3_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
            if(now_patient_id==patient_wave_id[3])
                p4_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
            IhrHighStateCount[now_patient_id] = 0;
            IhrLowStateCount[now_patient_id] = 0;
        }
    }*/

    private int index1 = 0;
    private int index2 = 0;
    private int count_index1 = 3;
    private int count_index2 = 5;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            this.update();
            handler.postDelayed(this, 1);
        }
        void update() {
        	
        	//處理收到的字串，每秒做100次
        	if(SERVER_STRING!=null){
        		ServerString(SERVER_STRING);
        		SERVER_STRING=null;
        	}
        	
        	delay_count = (delay_count + 1)%1000;
        	
        	//更新畫面，每秒做2次
        	if(delay_count==0 || delay_count==500){
	        	OptionOpened=false;
	        	
	        	//心電圖顯示保持開啟
	        	byte []Cmd = new byte []{0x0D};
	        	sendCmd(Cmd);
	        	Cmd = new byte []{'W','+',0x0D};               	
	        	sendCmd(Cmd);
	        	
	        	RenewList();
        	}
        	
        	//偵測斷線時取消同步，每秒做2次
        	if(delay_count==0 || delay_count==500){
        		for(int i=0; i<patient_num; i++){
        			if(synch_username[patient_id[i]]!=null && bth_name[patient_id[i]]==null && simulate_state[patient_id[i]]==-1){
        				writeData("logout " + synch_server_id[patient_id[i]] + " " + "\n");
        			}
        		}
        	}

        	//模擬訊號，每秒做四次
        	if(delay_count==0 || delay_count==250 || delay_count==500 || delay_count==750){
        		for(int i=0; i<patient_num; i++){
        			if(simulate_state[patient_id[i]]>=0){

                        if(simulate_state[patient_id[i]] == 5)
                        {
                            String login_url = "http://192.168.1.113/get_bth_heart.php";
                            String result = "", line;
                            try {
                                URL url = new URL(login_url);
                                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                                httpURLConnection.setRequestMethod("POST");
                                httpURLConnection.setDoOutput(true);
                                httpURLConnection.setDoInput(true);
                                OutputStream outputStream = httpURLConnection.getOutputStream();
                                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                                String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                                bufferedWriter.write(post_data);
                                bufferedWriter.flush();
                                bufferedWriter.close();
                                outputStream.close();
                                InputStream inputStream = httpURLConnection.getInputStream();
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                                while((line = bufferedReader.readLine())!=null) result += line;
                                bufferedReader.close();
                                inputStream.close();
                                httpURLConnection.disconnect();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String heart[] = result.split(" ");

                            login_url = "http://192.168.1.113/get_bth_temp.php";
                            result = "";
                            try {
                                URL url = new URL(login_url);
                                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                                httpURLConnection.setRequestMethod("POST");
                                httpURLConnection.setDoOutput(true);
                                httpURLConnection.setDoInput(true);
                                OutputStream outputStream = httpURLConnection.getOutputStream();
                                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                                String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                                bufferedWriter.write(post_data);
                                bufferedWriter.flush();
                                bufferedWriter.close();
                                outputStream.close();
                                InputStream inputStream = httpURLConnection.getInputStream();
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                                while((line = bufferedReader.readLine())!=null) result += line;
                                bufferedReader.close();
                                inputStream.close();
                                httpURLConnection.disconnect();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String temp[] = result.split(" ");

                            login_url = "http://192.168.1.113/get_bth_ECG.php";
                            result = "";
                            try {
                                URL url = new URL(login_url);
                                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                                httpURLConnection.setRequestMethod("POST");
                                httpURLConnection.setDoOutput(true);
                                httpURLConnection.setDoInput(true);
                                OutputStream outputStream = httpURLConnection.getOutputStream();
                                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                                String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                                bufferedWriter.write(post_data);
                                bufferedWriter.flush();
                                bufferedWriter.close();
                                outputStream.close();
                                InputStream inputStream = httpURLConnection.getInputStream();
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                                while((line = bufferedReader.readLine())!=null) result += line;
                                bufferedReader.close();
                                inputStream.close();
                                httpURLConnection.disconnect();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String ecg[] = result.split(" ");

                            byte[] c = new byte[ecg.length];
                            for(int j = 0; j < ecg.length; j++)
                            {
                                c[j] = (byte)Integer.parseInt(ecg[j]);
                            }

                            if(patient_wave_id[0]==patient_id[i] ||
                                    patient_wave_id[1]==patient_id[i] ||
                                    patient_wave_id[2]==patient_id[i] ||
                                    patient_wave_id[3]==patient_id[i]){

                                if(patient_wave_id[0]==patient_id[i]){
                                    mChartView1.Wave_Draw(c);
                                    if(count_index1 == 3) p1_Ihr.setText(heart[index1++]);
                                    if(count_index2 == 5) p1_Te.setText(temp[index2++]);
                                }
                                if(patient_wave_id[1]==patient_id[i]){
                                    mChartView2.Wave_Draw(c);
                                    if(count_index1 == 3) p2_Ihr.setText(heart[index1++]);
                                    if(count_index2 == 5) p2_Te.setText(temp[index2++]);
                                }
                                if(patient_wave_id[2]==patient_id[i]){
                                    mChartView3.Wave_Draw(c);
                                    if(count_index1 == 3) p3_Ihr.setText(heart[index1++]);
                                    if(count_index2 == 5) p3_Te.setText(temp[index2++]);
                                }
                                if(patient_wave_id[3]==patient_id[i]){
                                    mChartView4.Wave_Draw(c);
                                    if(count_index1 == 3) p4_Ihr.setText(heart[index1++]);
                                    if(count_index2 == 5) p4_Te.setText(temp[index2++]);
                                }
                            }

                            if(index1 == 10) index1 = 0;
                            if(index2 == 10) index2 = 0;
                            if(count_index1++ == 3) count_index1 = 0;
                            if(count_index2++ == 5) count_index2 = 0;
                        }
                        else
                        {
                            if(synch_username[patient_id[i]]!=null){
                                String s = "";
                                for(int j=0; j<simulate_ECG[simulate_state[patient_id[i]]][(delay_count+offset)/250].length; j++){
                                    s = s + simulate_ECG[simulate_state[patient_id[i]]][(delay_count+offset)/250][j] + " ";
                                }
                                writeData("data" + " "  + synch_server_id[patient_id[i]] + " " + "ECG" + " " + s + " " + "\n");
                            }
                            if(patient_wave_id[0]==patient_id[i] ||
                                    patient_wave_id[1]==patient_id[i] ||
                                    patient_wave_id[2]==patient_id[i] ||
                                    patient_wave_id[3]==patient_id[i]){
                                byte[] b = new byte[simulate_ECG[simulate_state[patient_id[i]]][(delay_count+offset)/250].length];
                                for(int j=0; j<simulate_ECG[simulate_state[patient_id[i]]][(delay_count+offset)/250].length; j++){
                                    b[j] = (byte)Integer.parseInt(simulate_ECG[simulate_state[patient_id[i]]][(delay_count+offset)/250][j]);
                                }



                                if(patient_wave_id[0]==patient_id[i]){
                                    mChartView1.Wave_Draw(b);
                                }
                                if(patient_wave_id[1]==patient_id[i]){
                                    mChartView2.Wave_Draw(b);
                                }
                                if(patient_wave_id[2]==patient_id[i]){
                                    mChartView3.Wave_Draw(b);
                                }
                                if(patient_wave_id[3]==patient_id[i]){
                                    mChartView4.Wave_Draw(b);
                                }
                            }

                            String[] info = new String[2];

                            TAfter[patient_id[i]] = System.currentTimeMillis();
                            long Tpass = (TAfter[patient_id[i]] - TBefore[patient_id[i]])/1000;

                            info[0] = "IHR";
                            info[1] = "60";
                            InfoMessage(Tpass, patient_id[i], info, patient_name[i]);

                            info[0] = "TE";
                            info[1] = "40000";
                            InfoMessage(Tpass, patient_id[i], info, patient_name[i]);

                            StateMessage(simulate_state[patient_id[i]], patient_id[i], patient_name[i]);
                        }
        			}
        		}
        		if(delay_count==750)
        			offset=(offset+1000)%2000;
        	}
        }
    };

    public void refresh()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //鎖定螢幕為橫向顯示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        //隱藏標題列
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //設定layout
        setContentView(R.layout.main);
        
        //連結layout
        findViews();

        //初始化數據
        Initiation();
        
        //初始化mBTService和mECGService
        setupMonitor();
        
        //更新List
        list = new ArrayList<String>();
        RenewList();
        
        //設定List adapter
        DragListView dragListView = (DragListView)findViewById(R.id.drag_list);
        adapter = new DragListAdapter(this, list);
        dragListView.setAdapter(adapter);
        
        //設定bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){
            Toast.makeText(this, R.string.bth_unavailable, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        //監視操作
        setListensers();
        
        //以新的執行緒來讀取資料
     	Thread t = new Thread(readData);
	
     	//啟動執行緒
     	t.start();
        
        //每1毫秒刷新一次
        handler.postDelayed(runnable, 1);
    }
    
    private void findViews(){
    	//右側四格TextView
    	p1_no = (TextView) findViewById(R.id.textView1_no);
    	p1_name = (TextView) findViewById(R.id.textView1_name);
    	p1_connect = (TextView) findViewById(R.id.textView1_connect);
    	p1_synch = (TextView) findViewById(R.id.textView1_synch);
        p1_Ihr = (TextView) findViewById(R.id.textView1_Ihr);
        p1_Te = (TextView) findViewById(R.id.textView1_Te);
        p1_state = (TextView) findViewById(R.id.textView1_state);
        p1_state_word = (TextView) findViewById(R.id.textView1_state_word);
        
    	p2_no = (TextView) findViewById(R.id.textView2_no);
    	p2_name = (TextView) findViewById(R.id.textView2_name);
    	p2_connect = (TextView) findViewById(R.id.textView2_connect);
    	p2_synch = (TextView) findViewById(R.id.textView2_synch);
        p2_Ihr = (TextView) findViewById(R.id.textView2_Ihr);
        p2_Te = (TextView) findViewById(R.id.textView2_Te);
        p2_state = (TextView) findViewById(R.id.textView2_state);
        p2_state_word = (TextView) findViewById(R.id.textView2_state_word);
        
    	p3_no = (TextView) findViewById(R.id.textView3_no);
    	p3_name = (TextView) findViewById(R.id.textView3_name);
    	p3_connect = (TextView) findViewById(R.id.textView3_connect);
    	p3_synch = (TextView) findViewById(R.id.textView3_synch);
        p3_Ihr = (TextView) findViewById(R.id.textView3_Ihr);
        p3_Te = (TextView) findViewById(R.id.textView3_Te);
        p3_state = (TextView) findViewById(R.id.textView3_state);
        p3_state_word = (TextView) findViewById(R.id.textView3_state_word);
        
    	p4_no = (TextView) findViewById(R.id.textView4_no);
    	p4_name = (TextView) findViewById(R.id.textView4_name);
    	p4_connect = (TextView) findViewById(R.id.textView4_connect);
    	p4_synch = (TextView) findViewById(R.id.textView4_synch);
        p4_Ihr = (TextView) findViewById(R.id.textView4_Ihr);
        p4_Te = (TextView) findViewById(R.id.textView4_Te);
        p4_state = (TextView) findViewById(R.id.textView4_state);
        p4_state_word = (TextView) findViewById(R.id.textView4_state_word);
        
    	//右側四格畫心電圖
		mChartView1 =  (ChartView1) findViewById(R.id.Chart1);	
        mChartView1.setX_Axis((getWindowManager().getDefaultDisplay().getWidth()*2/5)-20);
        
		mChartView2 =  (ChartView2) findViewById(R.id.Chart2);	
        mChartView2.setX_Axis((getWindowManager().getDefaultDisplay().getWidth()*2/5)-20);
        
		mChartView3 =  (ChartView3) findViewById(R.id.Chart3);	
        mChartView3.setX_Axis((getWindowManager().getDefaultDisplay().getWidth()*2/5)-20);
        
		mChartView4 =  (ChartView4) findViewById(R.id.Chart4);	
        mChartView4.setX_Axis((getWindowManager().getDefaultDisplay().getWidth()*2/5)-20);
    	
    	//右下方頁數
    	page = (TextView) findViewById(R.id.page);
    	back = (Button) findViewById(R.id.back);
    	next = (Button) findViewById(R.id.next);
        syn_patient = (Button) findViewById(R.id.syn);
    }

    //初始化數據  
    public void Initiation(){
    	//預設為沒有連線
    	connected_num = 0;
    	for(int i=0; i<1024; i++){
    		bth_connected[i] = false;
    	}
    	
    	//清空連線藍芽裝置名稱和MAC
    	for(int i=0; i<1024; i++){
    		bth_name[i] = null;
    		bth_address[i] = null;
    	}
    	
    	//清空同步的帳號名稱和Server的ID
    	for(int i=0; i<1024; i++){
    		synch_username[i] = null;
    		synch_server_id[i] = -1;
    	}
    	
    	//清空連線的帳號名稱
    	for(int i=0; i<1024; i++){
    		connect_username[i] = null;
    	}   	
    	
    	//清空模擬訊號選項
    	for(int i=0; i<1024; i++){
    		simulate_state[i] = -1;
    	}   	
    	
    	//清空右側心電圖
    	for(int i=0; i<4; i++){
    		patient_wave_id[i]=-1;
    	}
    	
    	//清空狀態計數
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
        }
    }
    
    //更新List
    public void RenewList(){
    	
    	int count, order;
    	
    	//暫時資料，和原來做比對
    	Boolean same = true;
    	Boolean find_id;
    	int temp_patient_num;
        int temp_patient_wave_on_num;
        int temp_patient_wave_off_num;
        int [] temp_patient_id = new int[1024];
        String [] temp_patient_name = new String[1024];
        String [] temp_patient_email = new String[1024];
        String [] temp_patient_phone = new String[1024];
        String [] temp_patient_sms_phone_1 = new String[1024];
        String [] temp_patient_sms_phone_2 = new String[1024];
        String [] temp_patient_sms_phone_3 = new String[1024];
        String [] temp_patient_address = new String[1024];
        String [] temp_patient_note = new String[1024];
        int [] temp_patient_wave = new int[1024];
        int [] temp_patient_order = new int[1024];
    	
        temp_patient_wave_on_num = 0;
        temp_patient_wave_off_num = 0;
    	
    	//讀取資料庫
        Cursor cursor;
        DH = new DBHelper(this);
        SQLiteDatabase db = DH.getWritableDatabase();
        cursor = db.query("HeartBeat", null, null, null, null, null, null);
        temp_patient_num = cursor.getCount();
        
        //存放病人到list
		cursor.moveToFirst();			//將指標移至第一筆資料
		for(int i=0; i<temp_patient_num; i++) {
			temp_patient_id[i] = cursor.getInt(0);	//取得第0欄的資料，根據欄位type使用適當語法
			temp_patient_name[i] = cursor.getString(1);
			temp_patient_email[i] = cursor.getString(2);
			temp_patient_phone[i] = cursor.getString(3);
			temp_patient_sms_phone_1[i] = cursor.getString(4);
			temp_patient_sms_phone_2[i] = cursor.getString(5);
			temp_patient_sms_phone_3[i] = cursor.getString(6);
			temp_patient_address[i] = cursor.getString(7);
			temp_patient_note[i] = cursor.getString(8);
			temp_patient_wave[i] = cursor.getInt(9);
			temp_patient_order[i] = cursor.getInt(10);

            String id = Integer.toString(temp_patient_id[i]);
            String name = temp_patient_name[i];
            String phone = temp_patient_phone[i];
            String phone1 = temp_patient_sms_phone_1[i];
            String phone2 = temp_patient_sms_phone_2[i];
            String phone3 = temp_patient_sms_phone_3[i];
            String e_mail = temp_patient_email[i];
            String address = temp_patient_address[i];
            String note = temp_patient_note[i];

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

			if(temp_patient_wave[i]==1)
				temp_patient_wave_on_num++;
			else
				temp_patient_wave_off_num++;
			cursor.moveToNext();		//將指標移至下一筆資料
		}
		cursor.close();
		db.close();

		//比較新舊資料
		if(temp_patient_num!=patient_num || 
				temp_patient_wave_on_num!=patient_wave_on_num || 
				temp_patient_wave_off_num!=patient_wave_off_num){
			same = false;
		}
		else{
			for(int i=0; i<patient_num; i++) {
				find_id = false;
				for(int j=0; j<patient_num; j++) {
					if(temp_patient_id[i]==patient_id[j]){
						find_id = true;
						if(temp_patient_name[i].equals(patient_name[j]) && 
								temp_patient_email[i].equals(patient_email[j]) && 
								temp_patient_phone[i].equals(patient_phone[j]) &&
								temp_patient_sms_phone_1[i].equals(patient_sms_phone_1[j]) &&
								temp_patient_sms_phone_2[i].equals(patient_sms_phone_2[j]) &&
								temp_patient_sms_phone_3[i].equals(patient_sms_phone_3[j]) &&
								temp_patient_address[i].equals(patient_address[j]) && 
								temp_patient_note[i].equals(patient_note[j]) && 
								temp_patient_wave[i]==patient_wave[j] && 
								temp_patient_order[i]==patient_order[j]){
							//數據一樣，沒有動作
						}
						else{
							same = false;
						}
						break;
					}
				}
				if(find_id==false)
					same = false;
				if(same == false)
					break;
			}		
		}

		//如果不一樣則進行更新
		if(same == false){
			
			//將temp資料寫回
			patient_num = temp_patient_num;
			patient_wave_on_num = temp_patient_wave_on_num;
			patient_wave_off_num = temp_patient_wave_off_num;			

			for(int i=0; i<patient_num; i++) {
				patient_id[i] = temp_patient_id[i];
				patient_name[i] = temp_patient_name[i];
				patient_email[i] = temp_patient_email[i];
				patient_phone[i] = temp_patient_phone[i];
				patient_sms_phone_1[i] = temp_patient_sms_phone_1[i];
				patient_sms_phone_2[i] = temp_patient_sms_phone_2[i];
				patient_sms_phone_3[i] = temp_patient_sms_phone_3[i];
				patient_address[i] = temp_patient_address[i];
				patient_note[i] = temp_patient_note[i];
				patient_wave[i] = temp_patient_wave[i];
				patient_order[i] = temp_patient_order[i];

                String id = Integer.toString(temp_patient_id[i]);
                String name = temp_patient_name[i];
                String phone = temp_patient_phone[i];
                String phone1 = temp_patient_sms_phone_1[i];
                String phone2 = temp_patient_sms_phone_2[i];
                String phone3 = temp_patient_sms_phone_3[i];
                String e_mail = temp_patient_email[i];
                String address = temp_patient_address[i];
                String note = temp_patient_note[i];

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
			}			
			
			//清空之前的list
	    	list.clear();
	    	WaveOnList.clear();
	    	WaveOffList.clear();
	    	
	        //groupKey存放分組名稱
	        groupKey.add("顯示心電圖病患列表");
	        groupKey.add("不顯示心電圖病患列表");
	
	        //將顯示心電圖病患放入List
	        count=0;
	        order=0;
	        while(count<patient_wave_on_num && order<1024){		//order大於1024時，視為已經找不到了
	        	for(int j=0; j<patient_num; j++){
	        		if(patient_wave[j]==1 && patient_order[j]==order){
	        			WaveOnList.add(patient_name[j] + "\n" + "識別碼：" + patient_id[j]);
	        			count++;
	        		}
	        	}
	        	order++;
	        }
	
	        //將不顯示心電圖病患放入List
	        count=0;
	        order=0;
	        while(count<patient_wave_off_num && order<1024){	//order大於1024時，視為已經找不到了
	        	for(int j=0; j<patient_num; j++){
	        		if(patient_wave[j]==0 && patient_order[j]==order){
	        			WaveOffList.add(patient_name[j] + "\n" + "識別碼：" + patient_id[j]);
	        			count++;
	        		}
	        	}
	        	order++;
	        }
	        
	        //將分組名稱和病患依序放入List
	        list.add("顯示心電圖病患列表");
	        list.addAll(WaveOnList);
	
	        list.add("不顯示心電圖病患列表");
	        list.addAll(WaveOffList);
	
	        DragListView dragListView = (DragListView)findViewById(R.id.drag_list);
	        adapter = new DragListAdapter(this, list);
	        dragListView.setAdapter(adapter);
	
	        //更新右側
	        RenewRightSide();
		}
		
		//0個病人時加入分組名稱
		if(temp_patient_num == 0 && patient_num==0){
			//清空之前的list
	    	list.clear();
	    	
	        //groupKey存放分組名稱
	        groupKey.add("顯示心電圖病患列表");
	        groupKey.add("不顯示心電圖病患列表");
	        
	        //將分組名稱和病患依序放入List
	        list.add("顯示心電圖病患列表");
	        list.add("不顯示心電圖病患列表");
	
	        DragListView dragListView = (DragListView)findViewById(R.id.drag_list);
	        adapter = new DragListAdapter(this, list);
	        dragListView.setAdapter(adapter);
	
	        //更新右側
	        RenewRightSide();
		}
    }

    private int test1 = 0;
    private int test2 = 0;
    private int test3 = 0;
    private int test4 = 0;
    public String search(int i)
    {
        String name = patient_name[i];

        String login_url = "http://192.168.1.113/find_patient.php";
        String result = "", line;
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
            while((line = bufferedReader.readLine())!=null) result += line;
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(result.equals("exist"))
        {
            login_url = "http://192.168.1.113/get_patient_status.php";
            result = "";
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
                while((line = bufferedReader.readLine())!=null) result += line;
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }
        else return "not exist";
    }
    //更新右側
    public void RenewRightSide(){
    	//計算頁數
    	if(patient_wave_on_num%4==0)
    		total_page = patient_wave_on_num/4;
    	else
    		total_page = (patient_wave_on_num/4)+1;
    	if(total_page==0)			//最少一頁
    		total_page=1;
    	if(now_page>total_page)		//當前頁數不合法則進行修正
    		now_page=total_page;
    	
    	//顯示頁數狀態
    	page.setText("第 " + now_page + " 頁 / 共 " + total_page + " 頁");
    	
    	//設定上一頁和下一頁是否可用
    	if(now_page<=1)
    		back.setEnabled(false);
    	else
    		back.setEnabled(true);
    	
    	if(now_page>=total_page)
    		next.setEnabled(false);
    	else
    		next.setEnabled(true);
    	
    	//更新編號
    	p1_no.setText(Integer.toString((now_page-1)*4+1) + ".");
    	p2_no.setText(Integer.toString((now_page-1)*4+2) + ".");
    	p3_no.setText(Integer.toString((now_page-1)*4+3) + ".");
    	p4_no.setText(Integer.toString((now_page-1)*4+4) + ".");
    	
    	//檢查顯示心電圖狀態
    	Boolean [] check_wave = new Boolean[4];
    	for(int i=0; i<4; i++){
    		check_wave[i]=false;
    	}
    	
    	//更新病人名稱和連線狀態
    	int [] temp_id = new int[4];
    	
    	for(int i=0; i<4; i++)
    		temp_id[i]=-1;

        String check = "";
    	String t;
    	for(int i=0; i<patient_num; i++){
    		if(patient_wave[i]==1 && patient_order[i]==(now_page-1)*4){
    			p1_name.setText(patient_name[i]);
    			p1_state_word.setText("狀態：");

                check = search(i);

                String login_url = "http://192.168.1.113/find_patient_tablet.php";
                String result = "", line;
                try {
                    URL url = new URL(login_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    while((line = bufferedReader.readLine())!=null) result += line;
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(result.equals("not exist") && check.equals("not exists"))
                {
                    p1_synch.setText("尚未同步");
                    p1_connect.setText("尚未連結");
                    p1_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                    p1_Ihr.setText("0");
                    p1_Te.setTextColor(Color.parseColor("#FF8800"));
                    p1_Te.setText("0.0");
                    p1_state.setTextColor(Color.parseColor("#000088"));
                    p1_state.setText("--");

                    String user_name = "111";
                    String user_password = "000";

                    String internet_button = connect_username[patient_id[i]]!=null ? "true" : "false";
                    String internet_status = connect_username[patient_id[i]]!=null ? "true" : "false";
                    String syn_button = synch_username[patient_id[i]]!=null ? "true" : "false";
                    String syn_status = synch_username[patient_id[i]]!=null ? "true" : "false";
                    String bth_button = bth_name[patient_id[i]]!=null ? "true" : "false";
                    String heart = simulate_state[patient_id[i]]>=0 ? "60" : "0";
                    String temp = simulate_state[patient_id[i]]>=0 ? "36.0" : "0.0";

                    String ECG_status = "--";
                    if(simulate_state[patient_id[i]] < 0) ECG_status = "--";
                    else
                    {
                        if(simulate_state[patient_id[i]] == 0) ECG_status = "NORM";
                        else if(simulate_state[patient_id[i]] == 1) ECG_status = "LBBB";
                        else if(simulate_state[patient_id[i]] == 2) ECG_status = "RBBB";
                        else if(simulate_state[patient_id[i]] == 3) ECG_status = "VPC";
                        else if(simulate_state[patient_id[i]] == 4) ECG_status = "APC";
                    }

                    login_url = "http://192.168.1.113/add_patient_status_tablet.php";
                    result = "";
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
                                + URLEncoder.encode("patient_name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8")
                                + "&"
                                + URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(ECG_status, "UTF-8")
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
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
                    String get[] = check.split(" ");
                    if(!check.equals("not exist"))
                    {
                        login_url = "http://192.168.1.113/find_bth.php";
                        result = "";
                        try {
                            URL url = new URL(login_url);
                            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                            httpURLConnection.setRequestMethod("POST");
                            httpURLConnection.setDoOutput(true);
                            httpURLConnection.setDoInput(true);
                            OutputStream outputStream = httpURLConnection.getOutputStream();
                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                            String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                            bufferedWriter.write(post_data);
                            bufferedWriter.flush();
                            bufferedWriter.close();
                            outputStream.close();
                            InputStream inputStream = httpURLConnection.getInputStream();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                            while((line = bufferedReader.readLine())!=null) result += line;
                            bufferedReader.close();
                            inputStream.close();
                            httpURLConnection.disconnect();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(result.equals("not exist"))
                        {
                            if(get[1].equals("--")) simulate_state[patient_id[i]] = -1;
                            else if(get[1].equals("NORM")) simulate_state[patient_id[i]] = 0;
                            else if(get[1].equals("LBBB")) simulate_state[patient_id[i]] = 1;
                            else if(get[1].equals("RBBB")) simulate_state[patient_id[i]] = 2;
                            else if(get[1].equals("VPC")) simulate_state[patient_id[i]] = 3;
                            else if(get[1].equals("APC")) simulate_state[patient_id[i]] = 4;

                            if (simulate_state[patient_id[i]] >= 0) {

                                if(simulate_state[patient_id[i]] == 0)
                                {
                                    p1_state.setText("NORM");
                                    p1_state.setTextColor(Color.parseColor("#000088"));
                                }
                                else
                                {
                                    if(simulate_state[patient_id[i]] == 1) p1_state.setText("LBBB");
                                    else if(simulate_state[patient_id[i]] == 2) p1_state.setText("RBBB");
                                    else if(simulate_state[patient_id[i]] == 3) p1_state.setText("VPC");
                                    else if(simulate_state[patient_id[i]] == 4) p1_state.setText("APC");
                                    p1_state.setTextColor(Color.RED);
                                }

                                p1_connect.setText("已連結");
                                p1_synch.setText("已同步");
                                p1_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p1_Ihr.setText("60");
                                p1_Te.setTextColor(Color.parseColor("#FF8800"));
                                p1_Te.setText("36.0");
                                p1_state.setText(get[1]);
                            }
                            else
                            {
                                p1_connect.setText("尚未連結");
                                p1_synch.setText("尚未同步");
                                p1_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p1_Ihr.setText("0");
                                p1_Te.setTextColor(Color.parseColor("#FF8800"));
                                p1_Te.setText("0.0");
                                p1_state.setTextColor(Color.parseColor("#000088"));
                                p1_state.setText("--");
                            }
                        }
                        else
                        {
                            simulate_state[patient_id[i]] = 5;
                            p1_synch.setText("已同步");
                            p1_connect.setText("已連結");
                            p1_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                            p1_Te.setTextColor(Color.parseColor("#FF8800"));

                            if(get[1].equals("NORM") || get[1].equals("--")) p1_state.setTextColor(Color.parseColor("#000088"));
                            else p1_state.setTextColor(Color.RED);

                            p1_state.setText(get[1]);
                        }
                    }
                    else
                    {
                        if (simulate_state[patient_id[i]] >= 0) {

                            if(simulate_state[patient_id[i]] == 0)
                            {
                                p1_state.setText("NORM");
                                p1_state.setTextColor(Color.parseColor("#000088"));
                            }
                            else
                            {
                                if(simulate_state[patient_id[i]] == 1) p1_state.setText("LBBB");
                                else if(simulate_state[patient_id[i]] == 2) p1_state.setText("RBBB");
                                else if(simulate_state[patient_id[i]] == 3) p1_state.setText("VPC");
                                else if(simulate_state[patient_id[i]] == 4) p1_state.setText("APC");
                                p1_state.setTextColor(Color.RED);
                            }

                            if (bth_name[patient_id[i]] == null && connect_username[patient_id[i]] == null)
                                p1_connect.setText("尚未連結");
                            else if (bth_name[patient_id[i]] != null)
                                p1_connect.setText("已連結");
                            else if (connect_username[patient_id[i]] != null)
                                p1_connect.setText("已連結");

                            t = (String) p1_Ihr.getText();
                            if (t.equals("")) {
                                p1_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p1_Ihr.setText("0");
                                p1_Te.setTextColor(Color.parseColor("#FF8800"));
                                p1_Te.setText("0.0");
                                p1_state.setTextColor(Color.parseColor("#000088"));
                                p1_state.setText("--");
                            }
                        } else if (bth_name[patient_id[i]] == null && connect_username[patient_id[i]] == null) {
                            p1_connect.setText("尚未連結");
                            p1_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                            p1_Ihr.setText("0");
                            p1_Te.setTextColor(Color.parseColor("#FF8800"));
                            p1_Te.setText("0.0");
                            p1_state.setTextColor(Color.parseColor("#000088"));
                            p1_state.setText("--");
                            mChartView1.ClearChart();
                        } else {
                            if (bth_name[patient_id[i]] != null)
                                p1_connect.setText("已連結");
                            else if (connect_username[patient_id[i]] != null)
                                p1_connect.setText("已連結");
                            t = (String) p1_Ihr.getText();
                            if (t.equals("")) {
                                p1_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p1_Ihr.setText("0");
                                p1_Te.setTextColor(Color.parseColor("#FF8800"));
                                p1_Te.setText("0.0");
                                p1_state.setTextColor(Color.parseColor("#000088"));
                                p1_state.setText("--");
                            }
                        }

                        //設定同步狀態
                        if (synch_username[patient_id[i]] != null)
                            p1_synch.setText("已同步");
                        else
                            p1_synch.setText("尚未同步");
                    }

                    check_wave[0] = true;
                    temp_id[0] = patient_id[i];
                }
    		}
    		if(patient_wave[i]==1 && patient_order[i]==(now_page-1)*4+1){
    			p2_name.setText(patient_name[i]);
    			p2_state_word.setText("狀態：");

                check = search(i);

                String login_url = "http://192.168.1.113/find_patient_tablet.php";
                String result = "", line;
                try {
                    URL url = new URL(login_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    while((line = bufferedReader.readLine())!=null) result += line;
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(result.equals("not exist") && check.equals("not exists"))
                {
                    p2_synch.setText("尚未同步");
                    p2_connect.setText("尚未連結");
                    p2_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                    p2_Ihr.setText("0");
                    p2_Te.setTextColor(Color.parseColor("#FF8800"));
                    p2_Te.setText("0.0");
                    p2_state.setTextColor(Color.parseColor("#000088"));
                    p2_state.setText("--");

                    String user_name = "111";
                    String user_password = "000";

                    String internet_button = connect_username[patient_id[i]]!=null ? "true" : "false";
                    String internet_status = connect_username[patient_id[i]]!=null ? "true" : "false";
                    String syn_button = synch_username[patient_id[i]]!=null ? "true" : "false";
                    String syn_status = synch_username[patient_id[i]]!=null ? "true" : "false";
                    String bth_button = bth_name[patient_id[i]]!=null ? "true" : "false";
                    String heart = simulate_state[patient_id[i]]>=0 ? "60" : "0";
                    String temp = simulate_state[patient_id[i]]>=0 ? "36.0" : "0.0";

                    String ECG_status = "--";
                    if(simulate_state[patient_id[i]] < 0) ECG_status = "--";
                    else
                    {
                        if(simulate_state[patient_id[i]] == 0) ECG_status = "NORM";
                        else if(simulate_state[patient_id[i]] == 1) ECG_status = "LBBB";
                        else if(simulate_state[patient_id[i]] == 2) ECG_status = "RBBB";
                        else if(simulate_state[patient_id[i]] == 3) ECG_status = "VPC";
                        else if(simulate_state[patient_id[i]] == 4) ECG_status = "APC";
                    }

                    login_url = "http://192.168.1.113/add_patient_status_tablet.php";
                    result = "";
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
                                + URLEncoder.encode("patient_name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8")
                                + "&"
                                + URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(ECG_status, "UTF-8")
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
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
                    String get[] = check.split(" ");
                    if(!check.equals("not exist"))
                    {
                        login_url = "http://192.168.1.113/find_bth.php";
                        result = "";
                        try {
                            URL url = new URL(login_url);
                            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                            httpURLConnection.setRequestMethod("POST");
                            httpURLConnection.setDoOutput(true);
                            httpURLConnection.setDoInput(true);
                            OutputStream outputStream = httpURLConnection.getOutputStream();
                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                            String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                            bufferedWriter.write(post_data);
                            bufferedWriter.flush();
                            bufferedWriter.close();
                            outputStream.close();
                            InputStream inputStream = httpURLConnection.getInputStream();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                            while((line = bufferedReader.readLine())!=null) result += line;
                            bufferedReader.close();
                            inputStream.close();
                            httpURLConnection.disconnect();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(result.equals("not exist"))
                        {
                            if(get[1].equals("--")) simulate_state[patient_id[i]] = -1;
                            else if(get[1].equals("NORM")) simulate_state[patient_id[i]] = 0;
                            else if(get[1].equals("LBBB")) simulate_state[patient_id[i]] = 1;
                            else if(get[1].equals("RBBB")) simulate_state[patient_id[i]] = 2;
                            else if(get[1].equals("VPC")) simulate_state[patient_id[i]] = 3;
                            else if(get[1].equals("APC")) simulate_state[patient_id[i]] = 4;

                            if (simulate_state[patient_id[i]] >= 0) {

                                if(simulate_state[patient_id[i]] == 0)
                                {
                                    p2_state.setText("NORM");
                                    p2_state.setTextColor(Color.parseColor("#000088"));
                                }
                                else
                                {
                                    if(simulate_state[patient_id[i]] == 1) p2_state.setText("LBBB");
                                    else if(simulate_state[patient_id[i]] == 2) p2_state.setText("RBBB");
                                    else if(simulate_state[patient_id[i]] == 3) p2_state.setText("VPC");
                                    else if(simulate_state[patient_id[i]] == 4) p2_state.setText("APC");
                                    p2_state.setTextColor(Color.RED);
                                }

                                p2_connect.setText("已連結");
                                p2_synch.setText("已同步");
                                p2_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p2_Ihr.setText("60");
                                p2_Te.setTextColor(Color.parseColor("#FF8800"));
                                p2_Te.setText("36.0");
                                p2_state.setText(get[1]);
                            }
                            else
                            {
                                p2_connect.setText("尚未連結");
                                p2_synch.setText("尚未同步");
                                p2_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p2_Ihr.setText("0");
                                p2_Te.setTextColor(Color.parseColor("#FF8800"));
                                p2_Te.setText("0.0");
                                p2_state.setTextColor(Color.parseColor("#000088"));
                                p2_state.setText("--");
                            }
                        }
                        else
                        {
                            simulate_state[patient_id[i]] = 5;
                            p2_synch.setText("已同步");
                            p2_connect.setText("已連結");
                            p2_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                            p2_Te.setTextColor(Color.parseColor("#FF8800"));

                            if(get[1].equals("NORM") || get[1].equals("--")) p2_state.setTextColor(Color.parseColor("#000088"));
                            else p2_state.setTextColor(Color.RED);

                            p2_state.setText(get[1]);
                        }
                    }
                    else
                    {
                        if (simulate_state[patient_id[i]] >= 0) {

                            if(simulate_state[patient_id[i]] == 0)
                            {
                                p2_state.setText("NORM");
                                p2_state.setTextColor(Color.parseColor("#000088"));
                            }
                            else
                            {
                                if(simulate_state[patient_id[i]] == 1) p2_state.setText("LBBB");
                                else if(simulate_state[patient_id[i]] == 2) p2_state.setText("RBBB");
                                else if(simulate_state[patient_id[i]] == 3) p2_state.setText("VPC");
                                else if(simulate_state[patient_id[i]] == 4) p2_state.setText("APC");
                                p2_state.setTextColor(Color.RED);
                            }

                            if (bth_name[patient_id[i]] == null && connect_username[patient_id[i]] == null)
                                p2_connect.setText("尚未連結");
                            else if (bth_name[patient_id[i]] != null)
                                p2_connect.setText("已連結");
                            else if (connect_username[patient_id[i]] != null)
                                p2_connect.setText("已連結");

                            t = (String) p1_Ihr.getText();
                            if (t.equals("")) {
                                p2_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p2_Ihr.setText("0");
                                p2_Te.setTextColor(Color.parseColor("#FF8800"));
                                p2_Te.setText("0.0");
                                p2_state.setTextColor(Color.parseColor("#000088"));
                                p2_state.setText("--");
                            }
                        } else if (bth_name[patient_id[i]] == null && connect_username[patient_id[i]] == null) {
                            p2_connect.setText("尚未連結");
                            p2_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                            p2_Ihr.setText("0");
                            p2_Te.setTextColor(Color.parseColor("#FF8800"));
                            p2_Te.setText("0.0");
                            p2_state.setTextColor(Color.parseColor("#000088"));
                            p2_state.setText("--");
                            mChartView2.ClearChart();
                        } else {
                            if (bth_name[patient_id[i]] != null)
                                p2_connect.setText("已連結");
                            else if (connect_username[patient_id[i]] != null)
                                p2_connect.setText("已連結");
                            t = (String) p2_Ihr.getText();
                            if (t.equals("")) {
                                p2_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p2_Ihr.setText("0");
                                p2_Te.setTextColor(Color.parseColor("#FF8800"));
                                p2_Te.setText("0.0");
                                p2_state.setTextColor(Color.parseColor("#000088"));
                                p2_state.setText("--");
                            }
                        }

                        //設定同步狀態
                        if (synch_username[patient_id[i]] != null)
                            p2_synch.setText("已同步");
                        else
                            p2_synch.setText("尚未同步");
                    }

                    check_wave[1] = true;
                    temp_id[1] = patient_id[i];
                }
    		}
    		if(patient_wave[i]==1 && patient_order[i]==(now_page-1)*4+2){
    			p3_name.setText(patient_name[i]);
    			p3_state_word.setText("狀態：");

                check = search(i);

                String login_url = "http://192.168.1.113/find_patient_tablet.php";
                String result = "", line;
                try {
                    URL url = new URL(login_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    while((line = bufferedReader.readLine())!=null) result += line;
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(result.equals("not exist") && check.equals("not exists"))
                {
                    p3_synch.setText("尚未同步");
                    p3_connect.setText("尚未連結");
                    p3_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                    p3_Ihr.setText("0");
                    p3_Te.setTextColor(Color.parseColor("#FF8800"));
                    p3_Te.setText("0.0");
                    p3_state.setTextColor(Color.parseColor("#000088"));
                    p3_state.setText("--");

                    String user_name = "111";
                    String user_password = "000";

                    String internet_button = connect_username[patient_id[i]]!=null ? "true" : "false";
                    String internet_status = connect_username[patient_id[i]]!=null ? "true" : "false";
                    String syn_button = synch_username[patient_id[i]]!=null ? "true" : "false";
                    String syn_status = synch_username[patient_id[i]]!=null ? "true" : "false";
                    String bth_button = bth_name[patient_id[i]]!=null ? "true" : "false";
                    String heart = simulate_state[patient_id[i]]>=0 ? "60" : "0";
                    String temp = simulate_state[patient_id[i]]>=0 ? "36.0" : "0.0";

                    String ECG_status = "--";
                    if(simulate_state[patient_id[i]] < 0) ECG_status = "--";
                    else
                    {
                        if(simulate_state[patient_id[i]] == 0) ECG_status = "NORM";
                        else if(simulate_state[patient_id[i]] == 1) ECG_status = "LBBB";
                        else if(simulate_state[patient_id[i]] == 2) ECG_status = "RBBB";
                        else if(simulate_state[patient_id[i]] == 3) ECG_status = "VPC";
                        else if(simulate_state[patient_id[i]] == 4) ECG_status = "APC";
                    }

                    login_url = "http://192.168.1.113/add_patient_status_tablet.php";
                    result = "";
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
                                + URLEncoder.encode("patient_name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8")
                                + "&"
                                + URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(ECG_status, "UTF-8")
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
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
                    String get[] = check.split(" ");
                    if(!check.equals("not exist"))
                    {
                        login_url = "http://192.168.1.113/find_bth.php";
                        result = "";
                        try {
                            URL url = new URL(login_url);
                            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                            httpURLConnection.setRequestMethod("POST");
                            httpURLConnection.setDoOutput(true);
                            httpURLConnection.setDoInput(true);
                            OutputStream outputStream = httpURLConnection.getOutputStream();
                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                            String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                            bufferedWriter.write(post_data);
                            bufferedWriter.flush();
                            bufferedWriter.close();
                            outputStream.close();
                            InputStream inputStream = httpURLConnection.getInputStream();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                            while((line = bufferedReader.readLine())!=null) result += line;
                            bufferedReader.close();
                            inputStream.close();
                            httpURLConnection.disconnect();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(result.equals("not exist"))
                        {
                            if(get[1].equals("--")) simulate_state[patient_id[i]] = -1;
                            else if(get[1].equals("NORM")) simulate_state[patient_id[i]] = 0;
                            else if(get[1].equals("LBBB")) simulate_state[patient_id[i]] = 1;
                            else if(get[1].equals("RBBB")) simulate_state[patient_id[i]] = 2;
                            else if(get[1].equals("VPC")) simulate_state[patient_id[i]] = 3;
                            else if(get[1].equals("APC")) simulate_state[patient_id[i]] = 4;

                            if (simulate_state[patient_id[i]] >= 0) {

                                if(simulate_state[patient_id[i]] == 0)
                                {
                                    p3_state.setText("NORM");
                                    p3_state.setTextColor(Color.parseColor("#000088"));
                                }
                                else
                                {
                                    if(simulate_state[patient_id[i]] == 1) p3_state.setText("LBBB");
                                    else if(simulate_state[patient_id[i]] == 2) p3_state.setText("RBBB");
                                    else if(simulate_state[patient_id[i]] == 3) p3_state.setText("VPC");
                                    else if(simulate_state[patient_id[i]] == 4) p3_state.setText("APC");
                                    p3_state.setTextColor(Color.RED);
                                }

                                p3_connect.setText("已連結");
                                p3_synch.setText("已同步");
                                p3_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p3_Ihr.setText("60");
                                p3_Te.setTextColor(Color.parseColor("#FF8800"));
                                p3_Te.setText("36.0");
                                p3_state.setText(get[1]);
                            }
                            else
                            {
                                p3_connect.setText("尚未連結");
                                p3_synch.setText("尚未同步");
                                p3_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p3_Ihr.setText("0");
                                p3_Te.setTextColor(Color.parseColor("#FF8800"));
                                p3_Te.setText("0.0");
                                p3_state.setTextColor(Color.parseColor("#000088"));
                                p3_state.setText("--");
                            }
                        }
                        else
                        {
                            simulate_state[patient_id[i]] = 5;
                            p3_synch.setText("已同步");
                            p3_connect.setText("已連結");
                            p3_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                            p3_Te.setTextColor(Color.parseColor("#FF8800"));

                            if(get[1].equals("NORM") || get[1].equals("--")) p3_state.setTextColor(Color.parseColor("#000088"));
                            else p3_state.setTextColor(Color.RED);

                            p3_state.setText(get[1]);
                        }
                    }
                    else
                    {
                        if (simulate_state[patient_id[i]] >= 0) {

                            if(simulate_state[patient_id[i]] == 0)
                            {
                                p3_state.setText("NORM");
                                p3_state.setTextColor(Color.parseColor("#000088"));
                            }
                            else
                            {
                                if(simulate_state[patient_id[i]] == 1) p3_state.setText("LBBB");
                                else if(simulate_state[patient_id[i]] == 2) p3_state.setText("RBBB");
                                else if(simulate_state[patient_id[i]] == 3) p3_state.setText("VPC");
                                else if(simulate_state[patient_id[i]] == 4) p3_state.setText("APC");
                                p3_state.setTextColor(Color.RED);
                            }

                            if (bth_name[patient_id[i]] == null && connect_username[patient_id[i]] == null)
                                p3_connect.setText("尚未連結");
                            else if (bth_name[patient_id[i]] != null)
                                p3_connect.setText("已連結");
                            else if (connect_username[patient_id[i]] != null)
                                p3_connect.setText("已連結");

                            t = (String) p1_Ihr.getText();
                            if (t.equals("")) {
                                p3_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p3_Ihr.setText("0");
                                p3_Te.setTextColor(Color.parseColor("#FF8800"));
                                p3_Te.setText("0.0");
                                p3_state.setTextColor(Color.parseColor("#000088"));
                                p3_state.setText("--");
                            }
                        } else if (bth_name[patient_id[i]] == null && connect_username[patient_id[i]] == null) {
                            p3_connect.setText("尚未連結");
                            p3_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                            p3_Ihr.setText("0");
                            p3_Te.setTextColor(Color.parseColor("#FF8800"));
                            p3_Te.setText("0.0");
                            p3_state.setTextColor(Color.parseColor("#000088"));
                            p3_state.setText("--");
                            mChartView3.ClearChart();
                        } else {
                            if (bth_name[patient_id[i]] != null)
                                p3_connect.setText("已連結");
                            else if (connect_username[patient_id[i]] != null)
                                p3_connect.setText("已連結");
                            t = (String) p3_Ihr.getText();
                            if (t.equals("")) {
                                p3_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p3_Ihr.setText("0");
                                p3_Te.setTextColor(Color.parseColor("#FF8800"));
                                p3_Te.setText("0.0");
                                p3_state.setTextColor(Color.parseColor("#000088"));
                                p3_state.setText("--");
                            }
                        }

                        //設定同步狀態
                        if (synch_username[patient_id[i]] != null)
                            p3_synch.setText("已同步");
                        else
                            p3_synch.setText("尚未同步");
                    }

                    check_wave[2] = true;
                    temp_id[2] = patient_id[i];
                }
    		}
    		if(patient_wave[i]==1 && patient_order[i]==(now_page-1)*4+3){
    			p4_name.setText(patient_name[i]);
    			p4_state_word.setText("狀態：");

                check = search(i);

                String login_url = "http://192.168.1.113/find_patient_tablet.php";
                String result = "", line;
                try {
                    URL url = new URL(login_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    while((line = bufferedReader.readLine())!=null) result += line;
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(result.equals("not exist") && check.equals("not exists"))
                {
                    p4_synch.setText("尚未同步");
                    p4_connect.setText("尚未連結");
                    p4_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                    p4_Ihr.setText("0");
                    p4_Te.setTextColor(Color.parseColor("#FF8800"));
                    p4_Te.setText("0.0");
                    p4_state.setTextColor(Color.parseColor("#000088"));
                    p4_state.setText("--");

                    String user_name = "111";
                    String user_password = "000";

                    String internet_button = connect_username[patient_id[i]]!=null ? "true" : "false";
                    String internet_status = connect_username[patient_id[i]]!=null ? "true" : "false";
                    String syn_button = synch_username[patient_id[i]]!=null ? "true" : "false";
                    String syn_status = synch_username[patient_id[i]]!=null ? "true" : "false";
                    String bth_button = bth_name[patient_id[i]]!=null ? "true" : "false";
                    String heart = simulate_state[patient_id[i]]>=0 ? "60" : "0";
                    String temp = simulate_state[patient_id[i]]>=0 ? "36.0" : "0.0";

                    String ECG_status = "--";
                    if(simulate_state[patient_id[i]] < 0) ECG_status = "--";
                    else
                    {
                        if(simulate_state[patient_id[i]] == 0) ECG_status = "NORM";
                        else if(simulate_state[patient_id[i]] == 1) ECG_status = "LBBB";
                        else if(simulate_state[patient_id[i]] == 2) ECG_status = "RBBB";
                        else if(simulate_state[patient_id[i]] == 3) ECG_status = "VPC";
                        else if(simulate_state[patient_id[i]] == 4) ECG_status = "APC";
                    }

                    login_url = "http://192.168.1.113/add_patient_status_tablet.php";
                    result = "";
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
                                + URLEncoder.encode("patient_name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8")
                                + "&"
                                + URLEncoder.encode("patient_ECGstatus", "UTF-8") + "=" + URLEncoder.encode(ECG_status, "UTF-8")
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
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
                    String get[] = check.split(" ");
                    if(!check.equals("not exist"))
                    {
                        login_url = "http://192.168.1.113/find_bth.php";
                        result = "";
                        try {
                            URL url = new URL(login_url);
                            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                            httpURLConnection.setRequestMethod("POST");
                            httpURLConnection.setDoOutput(true);
                            httpURLConnection.setDoInput(true);
                            OutputStream outputStream = httpURLConnection.getOutputStream();
                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                            String post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(patient_name[i], "UTF-8");
                            bufferedWriter.write(post_data);
                            bufferedWriter.flush();
                            bufferedWriter.close();
                            outputStream.close();
                            InputStream inputStream = httpURLConnection.getInputStream();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                            while((line = bufferedReader.readLine())!=null) result += line;
                            bufferedReader.close();
                            inputStream.close();
                            httpURLConnection.disconnect();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(result.equals("not exist"))
                        {
                            if(get[1].equals("--")) simulate_state[patient_id[i]] = -1;
                            else if(get[1].equals("NORM")) simulate_state[patient_id[i]] = 0;
                            else if(get[1].equals("LBBB")) simulate_state[patient_id[i]] = 1;
                            else if(get[1].equals("RBBB")) simulate_state[patient_id[i]] = 2;
                            else if(get[1].equals("VPC")) simulate_state[patient_id[i]] = 3;
                            else if(get[1].equals("APC")) simulate_state[patient_id[i]] = 4;

                            if (simulate_state[patient_id[i]] >= 0) {

                                if(simulate_state[patient_id[i]] == 0)
                                {
                                    p4_state.setText("NORM");
                                    p4_state.setTextColor(Color.parseColor("#000088"));
                                }
                                else
                                {
                                    if(simulate_state[patient_id[i]] == 1) p4_state.setText("LBBB");
                                    else if(simulate_state[patient_id[i]] == 2) p4_state.setText("RBBB");
                                    else if(simulate_state[patient_id[i]] == 3) p4_state.setText("VPC");
                                    else if(simulate_state[patient_id[i]] == 4) p4_state.setText("APC");
                                    p4_state.setTextColor(Color.RED);
                                }

                                p4_connect.setText("已連結");
                                p4_synch.setText("已同步");
                                p4_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p4_Ihr.setText("60");
                                p4_Te.setTextColor(Color.parseColor("#FF8800"));
                                p4_Te.setText("36.0");
                                p4_state.setText(get[1]);
                            }
                            else
                            {
                                p4_connect.setText("尚未連結");
                                p4_synch.setText("尚未同步");
                                p4_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p4_Ihr.setText("0");
                                p4_Te.setTextColor(Color.parseColor("#FF8800"));
                                p4_Te.setText("0.0");
                                p4_state.setTextColor(Color.parseColor("#000088"));
                                p4_state.setText("--");
                            }
                        }
                        else
                        {
                            simulate_state[patient_id[i]] = 5;
                            p4_synch.setText("已同步");
                            p4_connect.setText("已連結");
                            p4_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                            p4_Te.setTextColor(Color.parseColor("#FF8800"));

                            if(get[1].equals("NORM") || get[1].equals("--")) p4_state.setTextColor(Color.parseColor("#000088"));
                            else p4_state.setTextColor(Color.RED);

                            p4_state.setText(get[1]);
                        }
                    }
                    else
                    {
                        if (simulate_state[patient_id[i]] >= 0) {

                            if(simulate_state[patient_id[i]] == 0)
                            {
                                p4_state.setText("NORM");
                                p4_state.setTextColor(Color.parseColor("#000088"));
                            }
                            else
                            {
                                if(simulate_state[patient_id[i]] == 1) p4_state.setText("LBBB");
                                else if(simulate_state[patient_id[i]] == 2) p4_state.setText("RBBB");
                                else if(simulate_state[patient_id[i]] == 3) p4_state.setText("VPC");
                                else if(simulate_state[patient_id[i]] == 4) p4_state.setText("APC");
                                p4_state.setTextColor(Color.RED);
                            }

                            if (bth_name[patient_id[i]] == null && connect_username[patient_id[i]] == null)
                                p4_connect.setText("尚未連結");
                            else if (bth_name[patient_id[i]] != null)
                                p4_connect.setText("已連結");
                            else if (connect_username[patient_id[i]] != null)
                                p4_connect.setText("已連結");

                            t = (String) p1_Ihr.getText();
                            if (t.equals("")) {
                                p4_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p4_Ihr.setText("0");
                                p4_Te.setTextColor(Color.parseColor("#FF8800"));
                                p4_Te.setText("0.0");
                                p4_state.setTextColor(Color.parseColor("#000088"));
                                p4_state.setText("--");
                            }
                        } else if (bth_name[patient_id[i]] == null && connect_username[patient_id[i]] == null) {
                            p4_connect.setText("尚未連結");
                            p4_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                            p4_Ihr.setText("0");
                            p4_Te.setTextColor(Color.parseColor("#FF8800"));
                            p4_Te.setText("0.0");
                            p4_state.setTextColor(Color.parseColor("#000088"));
                            p4_state.setText("--");
                            mChartView4.ClearChart();
                        } else {
                            if (bth_name[patient_id[i]] != null)
                                p4_connect.setText("已連結");
                            else if (connect_username[patient_id[i]] != null)
                                p4_connect.setText("已連結");
                            t = (String) p4_Ihr.getText();
                            if (t.equals("")) {
                                p4_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                                p4_Ihr.setText("0");
                                p4_Te.setTextColor(Color.parseColor("#FF8800"));
                                p4_Te.setText("0.0");
                                p4_state.setTextColor(Color.parseColor("#000088"));
                                p4_state.setText("--");
                            }
                        }

                        //設定同步狀態
                        if (synch_username[patient_id[i]] != null)
                            p4_synch.setText("已同步");
                        else
                            p4_synch.setText("尚未同步");
                    }

                    check_wave[3] = true;
                    temp_id[3] = patient_id[i];
                }
    		}
    	}
    	
    	//如果又側病患改變或沒有連線，清空心電圖
    	if(temp_id[0]!=patient_wave_id[0] || temp_id[0]==-1)
    		mChartView1.ClearChart();
    	if(temp_id[1]!=patient_wave_id[1] || temp_id[1]==-1)
    		mChartView2.ClearChart();
    	if(temp_id[2]!=patient_wave_id[2] || temp_id[2]==-1)
    		mChartView3.ClearChart();
    	if(temp_id[3]!=patient_wave_id[3] || temp_id[3]==-1)
    		mChartView4.ClearChart();
    	
    	patient_wave_id[0] = temp_id[0];
    	patient_wave_id[1] = temp_id[1];
    	patient_wave_id[2] = temp_id[2];
    	patient_wave_id[3] = temp_id[3];
    	
    	mECGService.setWaveOnId(temp_id[0], temp_id[1], temp_id[2], temp_id[3]);

    	//將不顯示心電圖的清空
    	if(check_wave[0]==false){
    		p1_no.setText("");
    		p1_name.setText("");
    		p1_connect.setText("");
    		p1_synch.setText("");
    		p1_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
    		p1_Ihr.setText("");
    		p1_Te.setTextColor(Color.parseColor("#FF8800"));
    		p1_Te.setText("");
    		p1_state.setTextColor(Color.parseColor("#000088"));
    		p1_state.setText("");
    		p1_state_word.setText("");
    		mChartView1.ClearChart();
    	}
    	if(check_wave[1]==false){
    		p2_no.setText("");
    		p2_name.setText("");
    		p2_connect.setText("");
    		p2_synch.setText("");
    		p2_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
    		p2_Ihr.setText("");
    		p2_Te.setTextColor(Color.parseColor("#FF8800"));
    		p2_Te.setText("");
    		p2_state.setTextColor(Color.parseColor("#000088"));
    		p2_state.setText("");
    		p2_state_word.setText("");
    		mChartView2.ClearChart();
    	}
    	if(check_wave[2]==false){
    		p3_no.setText("");
    		p3_name.setText("");
    		p3_connect.setText("");
    		p3_synch.setText("");
    		p3_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
    		p3_Ihr.setText("");
    		p3_Te.setTextColor(Color.parseColor("#FF8800"));
    		p3_Te.setText("");
    		p3_state.setTextColor(Color.parseColor("#000088"));
    		p3_state.setText("");
    		p3_state_word.setText("");
    		mChartView3.ClearChart();
    	}
    	if(check_wave[3]==false){
    		p4_no.setText("");
    		p4_name.setText("");
    		p4_connect.setText("");
    		p4_synch.setText("");
    		p4_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
    		p4_Ihr.setText("");
    		p4_Te.setTextColor(Color.parseColor("#FF8800"));
    		p4_Te.setText("");
    		p4_state.setTextColor(Color.parseColor("#000088"));
    		p4_state.setText("");
    		p4_state_word.setText("");
    		mChartView4.ClearChart();
    	}
    }
    
	//更新DataBase
    public void RenewDataBase(){
        String [] l = list.toArray(new String[0]);
        
        DH = new DBHelper(this);
  	  	SQLiteDatabase db = DH.getWritableDatabase();
   	    ContentValues values = new ContentValues();
   	  
   	    //更新顯示心電圖的病患
        int i=1;
        while(l[i]!="不顯示心電圖病患列表"){
        	for(int j=0; j<patient_num; j++){
        		String[] AfterSplit = l[i].split("\n識別碼：");
        		if(patient_id[j]==Integer.parseInt(AfterSplit[1])){
        			patient_wave[j]=1;
        			patient_order[j]=i-1;
      			  values.put("_wave", 1);
    			  values.put("_order", i-1);
    			  db.update("HeartBeat", values, "_ID=" + patient_id[j], null);
    			  values.clear();     			
        			break;
        		}
        	}
        	i++;
        }
        
        //更新不顯示心電圖的病患
        int j=i;
        if(j+1<l.length){
        	while(j+1<l.length){
            	for(int k=0; k<patient_num; k++){
            		String[] AfterSplit = l[j+1].split("\n識別碼：");
            		if(patient_id[k]==Integer.parseInt(AfterSplit[1])){
            			patient_wave[k]=0;
            			patient_order[k]=j-i;
          			  values.put("_wave", 0);
        			  values.put("_order", j-i);
        			  db.update("HeartBeat", values, "_ID=" + patient_id[k], null);
        			  values.clear();
            			break;
            		}
            	}
            	j++;        		
        	}
        }
	  
        db.close();

        //更新右側
        RenewRightSide();
    }

    //監視操作
	private void setListensers(){
		//按下"上一頁"時的動作
		back.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                now_page=now_page-1;
                RenewRightSide();
                RenewRightSide();
            }
		});
		//按下"下一頁"時的動作
		next.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                now_page=now_page+1;
                RenewRightSide();
                RenewRightSide();
            }
		});

        syn_patient.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = "root";
                String password = "0000";

                String login_url = "http://192.168.1.113/find_patient_num.php";
                String result = "", line;
                try {
                    URL url = new URL(login_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("user_name", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8")
                            + "&"
                            + URLEncoder.encode("user_password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    while((line = bufferedReader.readLine())!=null) result += line;
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(result.equals("0"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

                    builder.setTitle("ERROR");
                    builder.setMessage("沒有任何病患可以進行同步的動作");
                    builder.setPositiveButton(R.string.error_Button, null);
                    builder.show();
                }
                else
                {
                    String get[] = result.split(" ");
                    String add = "";
                    for(int i = 1; i <= Integer.parseInt(get[0]); i++)
                    {
                        get_status(get[i]);
                        add += "病患 " + get[i] + " 已被同步" + "\n";
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

                    builder.setTitle("同步成功");
                    builder.setMessage(add);
                    builder.setPositiveButton(R.string.error_Button, null);
                    builder.show();

                    check = 1;

                    RenewRightSide();
                }
            }
        });
    }

    private int check = 0;
    public void get_status(String name)
    {
        if(check == 1)
        {
            for(int i = 0; i < 1024; i++)
                if(patient_name[i].equals(name)) return;
        }

        SQLiteDatabase db = DH.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("_name", name);
        values.put("_email", "");
        values.put("_phone", "");
        values.put("_smsphone1", "");
        values.put("_smsphone2", "");
        values.put("_smsphone3", "");
        values.put("_address", "");
        values.put("_note", "");

        Boolean Add = true;

        if(Add==true){	//如果要新增，則加入這兩個參數
            values.put("_wave", 0);
            values.put("_order", patient_wave_on_num);
        }

        if(Add==true)		//如果要新增，則插入一筆資料
            db.insert("HeartBeat", null, values);
    }
    
	//程式開始執行的動作
	@Override
    public void onStart() {
        super.onStart();

        if(D)
        	Log.e(TAG, "ON START");
        
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);                     
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        }else{
            if(mBTService == null)
            	setupMonitor();
        }
    }

	//程式恢復執行的動作
    @Override
    public synchronized void onResume(){
        super.onResume();
        
        if(D)
        	Log.e(TAG, "ON RESUME");

        if(mBTService != null){
            if(mBTService.getState() == BluetoothService.STATE_NONE) {
            	mBTService.start();
            }
        }
    }
    
    //程式結束執行的動作
    @Override
    public void onDestroy(){
        super.onDestroy();
        
        if(mBTService != null)
        	mBTService.stop();
        if(D)
        	Log.e(TAG, "ON DESTROY");
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    //程式停止執行的動作
    @Override
    protected void onStop(){
       super.onStop();
    }  
    	
    private void setupMonitor(){
        Log.d(TAG, "setupMonitor");

        mBTService = new BluetoothService(this, mHandler);
        mECGService = new ECGService(this, mECGHandler);
               
    }
    
    //接收startActivityForResult的回傳資料
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case OPTION_LIST:
            if(resultCode == Activity.RESULT_OK){		
            	
            	//藍芽連線部分
                String connect_bth = data.getExtras().getString(OptionList.EXTRA_CONNECT_BTH);
                
                if(connect_bth.equals("1")){				//連線裝置
	                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
	                mBTService.setPatientId(Integer.parseInt(select_id));
	                mBTService.connect(device);
                }
                else if(connect_bth.equals("0")){			//中斷裝置連線
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    mBTService.disconnect(address);
                }
                
                //線上同步部分
                String synch = data.getExtras().getString(OptionList.EXTRA_SYNCH_LOGIN);
                
                if(synch.equals("1")){						//登入帳號
                	String username = data.getExtras().getString(OptionList.EXTRA_SYNCH_USERNAME);
                	String password = data.getExtras().getString(OptionList.EXTRA_SYNCH_PASSWORD);
                	//writeData("login " + select_id + " " + username + " " + password + " " + "\n");

                    String login_url = "http://192.168.1.113/login.php";
                    String result = "", line;
                    try {
                        URL url = new URL(login_url);
                        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        String post_data = URLEncoder.encode("user_name", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8")
                                + "&"
                                + URLEncoder.encode("user_password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
                        bufferedWriter.write(post_data);
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        outputStream.close();
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        while((line = bufferedReader.readLine())!=null) result += line;
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(result.equals("login failed")) ServerString("login " + select_id + " " + username + " error " + select_id);
                    else ServerString("login " + select_id + " " + username + " ok " + select_id);
                }
                else if(synch.equals("2")){					//新增帳號
                	String username = data.getExtras().getString(OptionList.EXTRA_SYNCH_USERNAME);
                	String password = data.getExtras().getString(OptionList.EXTRA_SYNCH_PASSWORD);
                	String pin = data.getExtras().getString(OptionList.EXTRA_SYNCH_PIN);
                	writeData("create " + username + " " + password + " " + pin + " " + "\n");
                }
                else if(synch.equals("0")){					//解除同步
                	//writeData("logout " + synch_server_id[Integer.parseInt(select_id)] + " " + "\n");
                    ServerString("logout " + select_id);
                }
                
                //線上連結部分
                String connect_internet = data.getExtras().getString(OptionList.EXTRA_CONNECT_INTERNET);
                
                if(connect_internet.equals("1")){			//連結網路
                	String username = data.getExtras().getString(OptionList.EXTRA_CONNECT_USERNAME);
                	String pin = data.getExtras().getString(OptionList.EXTRA_CONNECT_PIN);
                	//writeData("connect " + select_id + " " + username + " " + pin + " " + "\n");

                    String login_url = "http://192.168.1.113/login.php";
                    String result = "", line;
                    try {
                        URL url = new URL(login_url);
                        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        String post_data = URLEncoder.encode("user_name", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8")
                                + "&"
                                + URLEncoder.encode("user_password", "UTF-8") + "=" + URLEncoder.encode(pin, "UTF-8");
                        bufferedWriter.write(post_data);
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        outputStream.close();
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        while((line = bufferedReader.readLine())!=null) result += line;
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(result.equals("login failed"))
                    {
                        /*AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

                        builder.setTitle("ERROR");
                        builder.setMessage("輸入的帳號或密碼有錯誤，請重新輸入");
                        builder.setPositiveButton(R.string.error_Button, null);
                        builder.show();*/
                        ServerString("connect " + select_id + " " + username + " error " + result);
                    }
                    else
                    {
                        /*AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

                        builder.setTitle(result);
                        builder.setMessage("已與伺服器連結成功");
                        builder.setPositiveButton(R.string.error_Button, null);
                        builder.show();*/
                        ServerString("connect " + select_id + " " + username + " ok " + result);
                    }
                }
                else if(connect_internet.equals("0")){		//中斷連結網路
                	//writeData("disconnect " + select_id + " " + connect_username[Integer.parseInt(select_id)] + " "  + "\n");

                    /*AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

                    builder.setTitle("注意");
                    builder.setMessage("已與伺服器斷開連線，若欲重新連結，請再重新點按選單內的連結網路");
                    builder.setPositiveButton(R.string.error_Button, null);
                    builder.show();*/

                    ServerString("disconnect " + select_id + " " + connect_username[Integer.parseInt(select_id)] + " ok");
                }
                
                //模擬訊號部分
                String simulate = data.getExtras().getString(OptionList.EXTRA_SIMULATE_STATE);
                if(simulate.equals("1")){
                	String state = data.getExtras().getString(OptionList.EXTRA_STATE);
                	int state_back = simulate_state[Integer.parseInt(select_id)];
                	simulate_state[Integer.parseInt(select_id)] = Integer.parseInt(state);
                	if(Integer.parseInt(state) != state_back){
                		WarnFlag[Integer.parseInt(select_id)] = Enable_Warning;
        	        	TBefore[Integer.parseInt(select_id)] = 0;
                		/*if(Integer.parseInt(state)==-1){
                			if(patient_wave_id[0]==Integer.parseInt(select_id))
                				mChartView1.ClearChart();
                			if(patient_wave_id[1]==Integer.parseInt(select_id))
                				mChartView2.ClearChart();
                			if(patient_wave_id[2]==Integer.parseInt(select_id))
                				mChartView3.ClearChart();
                			if(patient_wave_id[3]==Integer.parseInt(select_id))
                				mChartView4.ClearChart();
                		}*/
                		RenewRightSide();
                	}
                }
            }
            /*else if(resultCode == Activity.RESULT_CANCELED)
            {
                String temp_id = data.getExtras().getString(OptionList.select_id);
                String temp_name = data.getExtras().getString(OptionList.select_name);
                for(int i = 0; i < 1024; i++)
                {
                    if(patient_id[i] == Integer.parseInt(temp_id))
                    {
                        patient_name[i] = null;
                    }
                }
            }*/
            break;
        }
    }
    
    //送資料給藍芽心電圖機
    private void sendCmd(byte[] Cmd){
        if(mBTService.getState() != BluetoothService.STATE_CONNECTED)
            return;

        if(Cmd.length > 0)
            mBTService.write(Cmd);
    }
    
    //處理藍芽連線
    private Handler mHandler = new Handler(){
    	
        @Override
        public void handleMessage(Message msg){
	 		switch(msg.what){
	 		
	 		//當連線成功時，接收病患id、裝置名稱和裝置MAC
            case MESSAGE_DEVICE_CONNECTED:
            	
            	int id = Integer.parseInt(msg.getData().getString(PATIENT_ID));
            	bth_connected[id] = true;
            	bth_name[id] = msg.getData().getString(DEVICE_NAME);
            	bth_address[id] = msg.getData().getString(DEVICE_ADDRESS);
                connected_num++;
                Toast.makeText(getApplicationContext(), "已連接到  " + msg.getData().getString(DEVICE_NAME), Toast.LENGTH_SHORT).show();
                RenewRightSide();
                break;
            
            //當中斷連線時，清除相關資訊
            case MESSAGE_DEVICE_DISCONNECTED:
            	String temp = msg.getData().getString(DEVICE_ADDRESS);
            	String temp2 = "";
    	      	for(int i=0; i<patient_num; i++){
    	    		if(bth_address[patient_id[i]]==temp){
    	            	bth_connected[patient_id[i]] = false;
    	            	temp2 = bth_name[patient_id[i]];
    	            	bth_name[patient_id[i]] = null;
    	            	bth_address[patient_id[i]] = null;
    	    			break;
    	    		}
    	    	}    	   
    	      	connected_num--;
                Toast.makeText(getApplicationContext(), "裝置 " + temp2 + " (" + temp + ") 已經失去連線", Toast.LENGTH_SHORT).show();
                RenewRightSide();
                break;
            
            //接收心電圖資料的來源病患ID
            case MESSAGE_ID:
            	String temp3 = msg.getData().getString(PATIENT_DATA);
    	      	for(int i=0; i<patient_num; i++){
    	    		if(bth_address[patient_id[i]]==temp3){
    	    			patinet_data_id=patient_id[i];
    	    			break;
    	    		}
    	    	}
                break;
            
            //接收心電圖資料
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                mECGService.DataHandler(readBuf, (int)patinet_data_id);
                //Toast.makeText(getBaseContext(), "message read", Toast.LENGTH_SHORT).show();
            	break;
            	
            case MESSAGE_WRITE:
                break;	
            
            //測試TOAST訊息
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                break;
	 		}
        }
    };
    
    //處理收到的資料
    private final Handler mECGHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
            	//收到畫心電圖1的電壓資料
	            case MESSAGE_RAW_1:
	            	if(simulate_state[patient_wave_id[0]]==-1){
		            	byte[] RawBuf_1 = (byte[]) msg.obj;
		            	mChartView1.Wave_Draw(RawBuf_1);
	            	}
	                break;
	                
	            //收到畫心電圖2的電壓資料    
	            case MESSAGE_RAW_2:
	            	if(simulate_state[patient_wave_id[1]]==-1){
		            	byte[] RawBuf_2 = (byte[]) msg.obj;
		            	mChartView2.Wave_Draw(RawBuf_2);
	            	}
	                break;
	            
	            //收到畫心電圖3的電壓資料
	            case MESSAGE_RAW_3:
	            	if(simulate_state[patient_wave_id[2]]==-1){
		            	byte[] RawBuf_3 = (byte[]) msg.obj;
		            	mChartView3.Wave_Draw(RawBuf_3);
	            	}
	                break;
	            
	            //收到畫心電圖4的電壓資料    
	            case MESSAGE_RAW_4:
	            	if(simulate_state[patient_wave_id[3]]==-1){
		            	byte[] RawBuf_4 = (byte[]) msg.obj;
		            	mChartView4.Wave_Draw(RawBuf_4);
	            	}
	                break;
	            
	            //收到心跳速度和體溫  
	            case MESSAGE_INFO:
	            	
	            	//將資訊字串切割為"="前後，Ex:IHR=60分割為 info[0]=IHR,info[1]=60
	            	String [] info = msg.getData().getString(KY_INFO).split("=");
	            	
	            	//讀取id和姓名
	            	int temp_id = Integer.valueOf(msg.getData().getString(PATIENT_ID));
	            	if(simulate_state[temp_id]==-1){
		            	String temp_name = "";
		            	for(int i=0; i<1024; i++){
		            		if(patient_id[i]==temp_id){
		            			temp_name=patient_name[i];
		            		}
		            	}
	
		            	//計算時間，同一病患警告間最少隔60秒
		            	TAfter[temp_id] = System.currentTimeMillis();
		            	long Tpass = (TAfter[temp_id] - TBefore[temp_id])/1000;
		               
		            	InfoMessage(Tpass, temp_id, info, temp_name);
	            	}
	                break;
	            
	            //收到心跳狀態(NORM、LBBB、RBBB、VPC、APC)    
            	case MESSAGE_STATE:
            	
            		//讀取狀態
			    	int warntype = Integer.parseInt(msg.getData().getString(WARN_TYPE));
			    	
			    	//讀取id和姓名
			    	temp_id = Integer.parseInt(msg.getData().getString(PATIENT_ID));
			    	if(simulate_state[temp_id]==-1){
		            	String temp_name = "";
		            	for(int i=0; i<1024; i++){
		            		if(patient_id[i]==temp_id){
		            			temp_name=patient_name[i];
		            		}
		            	}
		            	
		            	if(synch_username[temp_id]!=null && simulate_state[temp_id]==-1){
		            		writeData("data " + synch_server_id[temp_id] + " " + "STATE" + " " + warntype + " " + "\n");
		            	}
		            	
		            	StateMessage(warntype, temp_id, temp_name);
			    	}
			    	break;
            	case MESSAGE_UPDATE:
            		String s = msg.getData().getString(UPDATE_STRING);
                	String[] AfterSplit = s.split(",");
                	if(synch_username[Integer.parseInt(AfterSplit[0])]!=null && simulate_state[Integer.parseInt(AfterSplit[0])]==-1){
        				writeData("data " + synch_server_id[Integer.parseInt(AfterSplit[0])] + " " + AfterSplit[1] + " " + "\n");
                	}
            		break;
            }
        }
    };
    
    private void InfoMessage(long Tpass, int now_patient_id, String[] info, String temp_name){
    	
    	if(Tpass >= 60){
    		WarnFlag[now_patient_id] = Enable_Warning;
    	}
    	
    	//當收到IHR資料時
        if(info[0].equals("IHR")){
        	double Ihr = Double.parseDouble(info[1]);
        	int Ihr2 = Integer.parseInt(info[1]);
        	if(synch_username[now_patient_id]!=null){
        		writeData("data " + synch_server_id[now_patient_id] + " " + "IHR" + " " + (int)Ihr + " " + "\n");
        	}
        	
        	if(now_patient_id==patient_wave_id[0])
        		p1_Ihr.setText(String.valueOf(Ihr2));
        	if(now_patient_id==patient_wave_id[1])
        		p2_Ihr.setText(String.valueOf(Ihr2));
        	if(now_patient_id==patient_wave_id[2])
        		p3_Ihr.setText(String.valueOf(Ihr2));
        	if(now_patient_id==patient_wave_id[3])
        		p4_Ihr.setText(String.valueOf(Ihr2));
        	
        	try{
                
                if(Ihr > 130.0){		//心跳過快
                	if(now_patient_id==patient_wave_id[0])
                		p1_Ihr.setTextColor(Color.RED);
                	if(now_patient_id==patient_wave_id[1])
                		p2_Ihr.setTextColor(Color.RED);
                	if(now_patient_id==patient_wave_id[2])
                		p3_Ihr.setTextColor(Color.RED);
                	if(now_patient_id==patient_wave_id[3])
                		p4_Ihr.setTextColor(Color.RED);
                	
                	IhrHighStateCount[now_patient_id]++;
                	
                	//判斷是否發出警告
                	if(WarnFlag[now_patient_id] == Enable_Warning){  
                		if(IhrHighStateCount[now_patient_id] >= 10 && temp_name!=""){
                			TBefore[now_patient_id] = System.currentTimeMillis();
                			Intent IhrHigh = new Intent();
                			IhrHigh.setClass(Main.this, EAlertDialog.class);
                			IhrHigh.putExtra("WarnType", S_IhrHigh);
                			IhrHigh.putExtra("WarnName", temp_name);
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
                else if(Ihr < 60.0){
                	if(now_patient_id==patient_wave_id[0])
                		p1_Ihr.setTextColor(Color.RED);
                	if(now_patient_id==patient_wave_id[1])
                		p2_Ihr.setTextColor(Color.RED);
                	if(now_patient_id==patient_wave_id[2])
                		p3_Ihr.setTextColor(Color.RED);
                	if(now_patient_id==patient_wave_id[3])
                		p4_Ihr.setTextColor(Color.RED);
                	
                	IhrLowStateCount[now_patient_id]++;
                	
                	//判斷是否發出警告
                	if(WarnFlag[now_patient_id] == Enable_Warning){ 
                		if(IhrLowStateCount[now_patient_id] >= 10 && temp_name!=""){
                			TBefore[now_patient_id] = System.currentTimeMillis();
                			Intent IhrLow = new Intent();
                			IhrLow.setClass(Main.this, EAlertDialog.class);
                			IhrLow.putExtra("WarnType", S_IhrLow);
                			IhrLow.putExtra("WarnName", temp_name);
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
                	if(now_patient_id==patient_wave_id[0])
                		p1_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                	if(now_patient_id==patient_wave_id[1])
                		p2_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                	if(now_patient_id==patient_wave_id[2])
                		p3_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                	if(now_patient_id==patient_wave_id[3])
                		p4_Ihr.setTextColor(Color.parseColor("#4A7D6B"));
                	IhrHighStateCount[now_patient_id] = 0;
                	IhrLowStateCount[now_patient_id] = 0;
                }
        	}catch(Exception e){
        		
        	}
        }
        else if (info[0].equals("TE"))
        {
        	String part1 = info[1].substring(0,3);
        	try{
            	double tmp1 = (Double.parseDouble(part1)/10)-4.0;
            	String[] c = String.valueOf(tmp1).split("00");
            	c = c[0].split("99");
            	
            	if(synch_username[now_patient_id]!=null){
            		writeData("data " + synch_server_id[now_patient_id] + " " + "TE" + " " + info[1] + " " + "\n");
            	}
            	
            	if(tmp1>=0){
                	if(now_patient_id==patient_wave_id[0])
                		p1_Te.setText(c[0]);
                	if(now_patient_id==patient_wave_id[1])
                		p2_Te.setText(c[0]);
                	if(now_patient_id==patient_wave_id[2])
                		p3_Te.setText(c[0]);
                	if(now_patient_id==patient_wave_id[3])
                		p4_Te.setText(c[0]);
                	
                	if(tmp1 >37.5){
	                	if(now_patient_id==patient_wave_id[0])
	                		p1_Te.setTextColor(Color.RED);
	                	if(now_patient_id==patient_wave_id[1])
	                		p2_Te.setTextColor(Color.RED);
	                	if(now_patient_id==patient_wave_id[2])
	                		p3_Te.setTextColor(Color.RED);
	                	if(now_patient_id==patient_wave_id[3])
	                		p4_Te.setTextColor(Color.RED);
	                	
	                	TeHighStateCount[now_patient_id]++;

	                	//判斷是否發出警告
                		if(WarnFlag[now_patient_id] == Enable_Warning){
                			if(TeHighStateCount[now_patient_id] >= 10 && temp_name!=""){
	                			TBefore[now_patient_id] = System.currentTimeMillis();
	                			Intent TeHigh = new Intent();
	                			TeHigh.setClass(Main.this, EAlertDialog.class);
	                			TeHigh.putExtra("WarnType", S_TeHigh);
	                			TeHigh.putExtra("WarnName", temp_name);
	                			startActivity(TeHigh);
	                			WarnFlag[now_patient_id] = Disable_Warning;
	                			TeHighStateCount[now_patient_id] = 0;
                			}
                		}
                    	else{
                    		TeHighStateCount[now_patient_id] = 0;
                    		WarnFlag[now_patient_id] = Disable_Warning;
                    	}
                	}
                	else{
	                	if(now_patient_id==patient_wave_id[0])
	                		p1_Te.setTextColor(Color.parseColor("#FF8800"));
	                	if(now_patient_id==patient_wave_id[1])
	                		p2_Te.setTextColor(Color.parseColor("#FF8800"));
	                	if(now_patient_id==patient_wave_id[2])
	                		p3_Te.setTextColor(Color.parseColor("#FF8800"));
	                	if(now_patient_id==patient_wave_id[3])
	                		p4_Te.setTextColor(Color.parseColor("#FF8800"));
                	}
            	}
            	else{
                	if(now_patient_id==patient_wave_id[0]){
                		p1_Te.setTextColor(Color.parseColor("#FF8800"));
                		p1_Te.setText("--");
                	}
                	if(now_patient_id==patient_wave_id[1]){
                		p2_Te.setTextColor(Color.parseColor("#FF8800"));
                		p2_Te.setText("--");
                	}
                	if(now_patient_id==patient_wave_id[2]){
                		p3_Te.setTextColor(Color.parseColor("#FF8800"));
                		p3_Te.setText("--");
                	}
                	if(now_patient_id==patient_wave_id[3]){
                		p4_Te.setTextColor(Color.parseColor("#FF8800"));
                		p4_Te.setText("--");
                	}
            	}
        	}catch(Exception e){
        		
        	}
        }
    }
    
    private void StateMessage(int warntype, int now_patient_id, String temp_name){
    	if(synch_username[now_patient_id]!=null)
    		writeData("data " + synch_server_id[now_patient_id] + " " + "STATE" + " " + warntype + " " + "\n");
    	if(warntype==NORM){
        	if(now_patient_id==patient_wave_id[0]){
        		p1_state.setTextColor(Color.parseColor("#000088"));
        		p1_state.setText("NORM");
        	}
        	if(now_patient_id==patient_wave_id[1]){
        		p2_state.setTextColor(Color.parseColor("#000088"));
        		p2_state.setText("NORM");
        	}
        	if(now_patient_id==patient_wave_id[2]){
        		p3_state.setTextColor(Color.parseColor("#000088"));
        		p3_state.setText("NORM");
        	}
        	if(now_patient_id==patient_wave_id[3]){
        		p4_state.setTextColor(Color.parseColor("#000088"));
        		p4_state.setText("NORM");
        	}

        	//遇到正常，則所有病症的count減1，目的是減緩因雜訊而判斷到有心律不整
        	//唯有短時間內頻繁出現的心律不整才視為真實訊號
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
    		
    		if(LBBBStateCount[now_patient_id]>=3){
            	if(now_patient_id==patient_wave_id[0]){
            		p1_state.setTextColor(Color.RED);
            		p1_state.setText("LBBB");
            	}
            	if(now_patient_id==patient_wave_id[1]){
            		p2_state.setTextColor(Color.RED);
            		p2_state.setText("LBBB");
            	}
            	if(now_patient_id==patient_wave_id[2]){
            		p3_state.setTextColor(Color.RED);
            		p3_state.setText("LBBB");
            	}
            	if(now_patient_id==patient_wave_id[3]){
            		p4_state.setTextColor(Color.RED);
            		p4_state.setText("LBBB");
            	}
    		}
    		
				//判斷是否發出警告
				if(WarnFlag[now_patient_id] == Enable_Warning){  
        		if(LBBBStateCount[now_patient_id] >= 10 && temp_name!=""){
        			TBefore[now_patient_id] = System.currentTimeMillis();
        			Intent LBBB_WARN = new Intent();
        			LBBB_WARN.setClass(Main.this, EAlertDialog.class);
        			LBBB_WARN.putExtra("WarnType", S_LBBB);
        			LBBB_WARN.putExtra("WarnName", temp_name);
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
    		
    		if(RBBBStateCount[now_patient_id]>=3){
            	if(now_patient_id==patient_wave_id[0]){
            		p1_state.setTextColor(Color.RED);
            		p1_state.setText("RBBB");
            	}
            	if(now_patient_id==patient_wave_id[1]){
            		p2_state.setTextColor(Color.RED);
            		p2_state.setText("RBBB");
            	}
            	if(now_patient_id==patient_wave_id[2]){
            		p3_state.setTextColor(Color.RED);
            		p3_state.setText("RBBB");
            	}
            	if(now_patient_id==patient_wave_id[3]){
            		p4_state.setTextColor(Color.RED);
            		p4_state.setText("RBBB");
            	}
    		}
        	
        	
        	//判斷是否發出警告
        	if(WarnFlag[now_patient_id] == Enable_Warning){  
        		if(RBBBStateCount[now_patient_id] >= 10 && temp_name!=""){
        			TBefore[now_patient_id] = System.currentTimeMillis();
        			Intent RBBB_WARN = new Intent();
        			RBBB_WARN.setClass(Main.this, EAlertDialog.class);
        			RBBB_WARN.putExtra("WarnType", S_RBBB);
        			RBBB_WARN.putExtra("WarnName", temp_name);
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
    		
    		if(VPCStateCount[now_patient_id]>=3){
            	if(now_patient_id==patient_wave_id[0]){
            		p1_state.setTextColor(Color.RED);
            		p1_state.setText("VPC");
            	}
            	if(now_patient_id==patient_wave_id[1]){
            		p2_state.setTextColor(Color.RED);
            		p2_state.setText("VPC");
            	}
            	if(now_patient_id==patient_wave_id[2]){
            		p3_state.setTextColor(Color.RED);
            		p3_state.setText("VPC");
            	}
            	if(now_patient_id==patient_wave_id[3]){
            		p4_state.setTextColor(Color.RED);
            		p4_state.setText("VPC");
            	}
    		}
        	
        	//判斷是否發出警告
				if(WarnFlag[now_patient_id] == Enable_Warning && temp_name!=""){  
        		if(VPCStateCount[now_patient_id] >= 10){
        			TBefore[now_patient_id] = System.currentTimeMillis();
        			Intent VPC_WARN = new Intent();
        			VPC_WARN.setClass(Main.this, EAlertDialog.class);
        			VPC_WARN.putExtra("WarnType", S_VPC);
        			VPC_WARN.putExtra("WarnName", temp_name);
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
    		
    		if(APCStateCount[now_patient_id]>=3){
            	if(now_patient_id==patient_wave_id[0]){
            		p1_state.setTextColor(Color.RED);
            		p1_state.setText("APC");
            	}
            	if(now_patient_id==patient_wave_id[1]){
            		p2_state.setTextColor(Color.RED);
            		p2_state.setText("APC");
            	}
            	if(now_patient_id==patient_wave_id[2]){
            		p3_state.setTextColor(Color.RED);
            		p3_state.setText("APC");
            	}
            	if(now_patient_id==patient_wave_id[3]){
            		p4_state.setTextColor(Color.RED);
            		p4_state.setText("APC");
            	}
    		}

        	//判斷是否發出警告
				if(WarnFlag[now_patient_id] == Enable_Warning && temp_name!=""){  
        		if(APCStateCount[now_patient_id] >= 10){
        			TBefore[now_patient_id] = System.currentTimeMillis();
        			Intent APC_WARN = new Intent();
        			APC_WARN.setClass(Main.this, EAlertDialog.class);
        			APC_WARN.putExtra("WarnType", S_APC);
        			APC_WARN.putExtra("WarnName", temp_name);
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

    
    private void writeData(String s){
		/*try {
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
			        	synch_username[i]=null;
			        	synch_server_id[i]=-1;
			        	connect_username[i]=null;
			        }
			        SERVER_CONNECTED=false;
				}
			}
		} catch (Exception e) {
			if(SERVER_CONNECTED==true){
				Toast.makeText(getApplicationContext(), "無法連線到Server，請稍後重新啟動本程式重試", Toast.LENGTH_SHORT).show();
		        for(int i=0; i<1024; i++){
		        	synch_username[i]=null;
		        	synch_server_id[i]=-1;
		        	connect_username[i]=null;
		        }
		        SERVER_CONNECTED=false;
			}
		}*/
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

            String name = "";
            for(int i=0; i<patient_num; i++){
                if(patient_id[i]==Integer.parseInt(S[1])){
                    name = patient_name[i];
                }
            }

			if(S[3].equals("ok")){
                AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

                builder.setTitle("login successed");
                builder.setMessage(name + " 已與伺服器進行同步");
                builder.setPositiveButton(R.string.error_Button, null);
                builder.show();

				//Toast.makeText(this, "登入成功", Toast.LENGTH_SHORT).show();
				synch_username[Integer.parseInt(S[1])] = S[2];
				synch_server_id[Integer.parseInt(S[1])] = Integer.parseInt(S[4]);
			}
			else if(S[3].equals("error"))
            {
                //Toast.makeText(this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

                builder.setTitle("login failed");
                builder.setMessage(name + " 與伺服器進行同步失敗 (帳號或密碼輸入錯誤)");
                builder.setPositiveButton(R.string.error_Button, null);
                builder.show();
            }
			else if(S[3].equals("loggedin")){
				Toast.makeText(this, "登入成功 (此帳號已經在他處同步，對方將解除同步)", Toast.LENGTH_SHORT).show();
				synch_username[Integer.parseInt(S[1])] = S[2];
				synch_server_id[Integer.parseInt(S[1])] = Integer.parseInt(S[4]);
			}
		}
		if(S[0].equals("data")){
			//Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
			if(S[2].equals("ECG")){
				if(patient_wave_id[0]==Integer.parseInt(S[1]) || 
						patient_wave_id[1]==Integer.parseInt(S[1]) || 
						patient_wave_id[2]==Integer.parseInt(S[1]) || 
						patient_wave_id[3]==Integer.parseInt(S[1])){
					byte[] RawBuf = new byte[S.length-3];
					String temp = "";
					for(int i=3; i<S.length; i++){
						RawBuf[i-3] = (byte) Integer.parseInt(S[i]);
					}
					if(patient_wave_id[0]==Integer.parseInt(S[1]) && simulate_state[patient_wave_id[0]]==-1)
						mChartView1.Wave_Draw(RawBuf);
					if(patient_wave_id[1]==Integer.parseInt(S[1]) && simulate_state[patient_wave_id[1]]==-1)
						mChartView2.Wave_Draw(RawBuf);
					if(patient_wave_id[2]==Integer.parseInt(S[1]) && simulate_state[patient_wave_id[2]]==-1)
						mChartView3.Wave_Draw(RawBuf);
					if(patient_wave_id[3]==Integer.parseInt(S[1]) && simulate_state[patient_wave_id[3]]==-1)
						mChartView4.Wave_Draw(RawBuf);
				}
			}
			else if(S[2].equals("IHR")){
				String[] info = new String[2];
				info[0]=S[2];
				info[1]=S[3];
				
				long Tpass = (TAfter[Integer.parseInt(S[1])] - TBefore[Integer.parseInt(S[1])])/1000;
				
				String temp_name = "";
				for(int i=0; i<patient_num; i++){
					if(patient_id[i]==Integer.parseInt(S[1])){
						temp_name = patient_name[i];
					}
				}
	            
				InfoMessage(Tpass, Integer.parseInt(S[1]), info, temp_name);
			}
			else if(S[2].equals("TE")){
				String[] info = new String[2];
				info[0]=S[2];
				info[1]=S[3];
				
				long Tpass = (TAfter[Integer.parseInt(S[1])] - TBefore[Integer.parseInt(S[1])])/1000;
				
				String temp_name = "";
				for(int i=0; i<patient_num; i++){
					if(patient_id[i]==Integer.parseInt(S[1])){
						temp_name = patient_name[i];
					}
				}
	            
				InfoMessage(Tpass, Integer.parseInt(S[1]), info, temp_name);
			}
			else if(S[2].equals("STATE")){

				String temp_name = "";
				for(int i=0; i<patient_num; i++){
					if(patient_id[i]==Integer.parseInt(S[1])){
						temp_name = patient_name[i];
					}
				}
				
				StateMessage(Integer.parseInt(S[3]), Integer.parseInt(S[1]), temp_name);
			}
		}
		if(S[0].equals("logout")){
			String name = "";
			for(int i=0; i<patient_num; i++){
				if(synch_server_id[patient_id[i]]==Integer.parseInt(S[1])){
					synch_server_id[patient_id[i]]=-1;
					synch_username[patient_id[i]]=null;
					name=patient_name[i];
					break;
				}
			}

            AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

            builder.setTitle("注意");
            builder.setMessage(name + " 已與伺服器斷開同步，若欲重新同步，請再重新點按選單內的線上同步");
            builder.setPositiveButton(R.string.error_Button, null);
            builder.show();

            //Toast.makeText(getApplicationContext(), name + " 已經解除同步", Toast.LENGTH_SHORT).show();
		}	
		if(S[0].equals("connect")){

            String name = "";
            for(int i=0; i<patient_num; i++){
                if(patient_id[i]==Integer.parseInt(S[1])){
                    name = patient_name[i];
                }
            }

			if(S[3].equals("ok")){

                AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

                builder.setTitle(S[4] + " " + S[5]);
                builder.setMessage(name + " 已與伺服器連結成功");
                builder.setPositiveButton(R.string.error_Button, null);
                builder.show();

				//Toast.makeText(this, name + " 已與伺服器連結成功", Toast.LENGTH_SHORT).show();
				connect_username[Integer.parseInt(S[1])] = S[2];
			}
			else if(S[3].equals("error"))
            {
                //Toast.makeText(this, name + " 與伺服器連結失敗", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

                builder.setTitle(S[4] + " " + S[5]);
                builder.setMessage(name + " 與伺服器連結失敗 (帳號或密碼輸入錯誤)");
                builder.setPositiveButton(R.string.error_Button, null);
                builder.show();
            }

		}
		if(S[0].equals("disconnect")){
			connect_username[Integer.parseInt(S[1])]=null;
			
			String name = "";
			for(int i=0; i<patient_num; i++){
				if(patient_id[i]==Integer.parseInt(S[1])){
					name = patient_name[i];
				}
			}

            AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

            builder.setTitle("注意");
            builder.setMessage(name + " 已與伺服器斷開連線，若欲重新連結，請再重新點按選單內的連結網路");
            builder.setPositiveButton(R.string.error_Button, null);
            builder.show();
            
            //Toast.makeText(getApplicationContext(), name + " 已與伺服器斷開連線，若欲重新連結，請再重新點按選單內的連結網路", Toast.LENGTH_SHORT).show();
		}
		RenewRightSide();
    }
	
    public class DragListAdapter extends ArrayAdapter<String>{

        public DragListAdapter(Context context, List<String> objects){
            super(context, 0, objects);
        }
        
        public List<String> getList(){	//取得list
            return list;
        }
        
        public void UpdateDataBase(){	//更新DataBase
        	RenewDataBase();
        }
        
        public Boolean CheckOption(){	//檢查Option List是否已經開啟
        	return OptionOpened;
        }
        
        public void ClickItem(String A){ //點擊病患
            String name = "";

            for(int i=0; i<patient_num; i++){
                String[] AfterSplit = A.split("\n識別碼：");
                if(patient_id[i]==Integer.parseInt(AfterSplit[1])){
                    name = patient_name[i];
                    break;
                }
            }

            for(int i=0; i<patient_num; i++){
                String[] AfterSplit = A.split("\n識別碼：");
                if(patient_id[i]==Integer.parseInt(AfterSplit[1]) && OptionOpened==false){
                    OptionOpened=true;
                    select_id=Integer.toString(patient_id[i]);

                    Intent OptionIntent = new Intent(Main.this, OptionList.class);
                    OptionIntent.putExtra(select_id, select_id);
                    OptionIntent.putExtra(select_name, name);
                    OptionIntent.putExtra(select_bth_address, bth_address[patient_id[i]]);
                    OptionIntent.putExtra(select_server_id, String.valueOf(synch_server_id[patient_id[i]]));
                    OptionIntent.putExtra(select_connect_name, connect_username[patient_id[i]]);
                    OptionIntent.putExtra(select_simulate_state, String.valueOf(simulate_state[patient_id[i]]));
                    startActivityForResult(OptionIntent, OPTION_LIST);
                    break;
                }
            }
        }        
        
        @Override
        public boolean isEnabled(int position) {
            if(groupKey.contains(getItem(position))){
                //如果點選分組名稱，回傳false，不能點擊
                return false;
            }
            return super.isEnabled(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            
            View view = convertView;
            if(groupKey.contains(getItem(position))){
                //如果是分組名稱，使用分組名稱的xml
                view = LayoutInflater.from(getContext()).inflate(R.layout.drag_list_item_tag, null);
            }
            else{
                //如果是一般選項，使用一般選項的xml
                view = LayoutInflater.from(getContext()).inflate(R.layout.drag_list_item, null);
            }

            TextView textView = (TextView)view.findViewById(R.id.drag_list_item_text);
            textView.setText(getItem(position));
            
            return view;
        }
    }
    
    //關於本程式的說明
    private void openAboutDlg(){
    	new AlertDialog.Builder(this)
    		.setTitle(R.string.about_title)
    		.setMessage(R.string.about_msg)
    		.setPositiveButton(R.string.ok_label,
    				new DialogInterface.OnClickListener(){public void onClick(
    						DialogInterface dialoginterface,int i){}})
    		.show();
    	
    }
    
    //指定menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    //設定menu選項的執行事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.add:
        	Intent addNewContact = new Intent().setClass(this, AddEditContact.class);
        	addNewContact.putExtra("patient_wave_on_num", Integer.toString(patient_wave_on_num));
    		Main.this.startActivity(addNewContact);
			return true;
        case R.id.hospital:
    		Intent Hospital = new Intent().setClass(this, Hospital.class);
    		Main.this.startActivity(Hospital);
    		return true;	
        case R.id.inform:
        	openAboutDlg();
        	return true;
        }
        return false;
    }
}
