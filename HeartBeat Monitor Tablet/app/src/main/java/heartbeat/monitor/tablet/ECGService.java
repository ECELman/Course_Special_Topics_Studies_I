package heartbeat.monitor.tablet;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ECGService 
{
    // Debugging
    private static final String TAG = "ECGService";
    private static final boolean D = false;
    
    // Constants that indicate the current KY-Module state
    public static final int STATE_NONE = 0;       	// doing nothing
    public static final int STATE_WAVEON = 1;     	// detecting with Raw Data
    public static final int STATE_WAVEOFF = 2; 		// detecting without Raw Data
    public static final int STATE_SIM_WAVEON = 3;  	// simulation signal with Raw Data
    public static final int STATE_SIM_WAVEOFF = 4;	// simulation signal without Raw Data
    
    public static final int NORM = 0;
    public static final int LBBB = 1;
    public static final int RBBB = 2;
    public static final int VPC = 3;
    public static final int APC = 4;
    public static final int UNKNOWN = 5;
    
	private static final int RawSize32 = 32;
	private static final int RawBufferSize =64;		//為32的倍數
	private final Handler mHandler;
	
	private int[] iRawData = new int[1024];			//讀入RawData計數
	private int[] iRawBuffer = new int[1024];			//RawBuffer計數
	private int mState;
	
	//儲存當前顯示心電圖的病患id
	private int[] wave_on_id = new int[4];
	
	private int Q;
	private int R;
	private int R_prime;
	private int S;
	private int T_prime;
	
	private float[] H_QR = new float[1024];
	private float[] H_RS = new float[1024];
	private float[] QRS_dur = new float[1024];
	private float[] QTP_int = new float[1024];
	private float[] Ratio_RR = new float[1024];
	private float[] Slope_QR = new float[1024];
	private float[] Slope_RS = new float[1024];
	private float[] Area_QRS = new float[1024];
	private float[] Area_RST = new float[1024];

	private float[][] A_state = new float[5][5];
	private float[][] B_code = new float[5][50];
	private float[] PI_first = new float[5];
	private float[][] CodeBook_4 = new float[50][4];
	private float[][] CodeBook_8 = new float[50][8];
		
	private int[] previous_vector_4 = new int[1024];
	private int[] previous_vector_8 = new int[1024];
	private int[] previous_state = new int[1024];
	
	private int[] first = new int[1024];
	private int[] Ratio_RR_count = new int[1024];
	private float[] average_R = new float[1024];
	private float[] average_RR_count = new float[1024];
	
	private byte[] Info_Buffer ;		//存放完整的資訊
	private byte[][] Raw_Buffer = new byte[1024][RawBufferSize];		//存放32bytes Raw Data
	
	private int[][] State_num = new int[1024][5];
	private int[][] State_num_2 = new int[1024][5];
	
	public ECGService(Context context, Handler handler) 
	{
		mHandler = handler;
		for(int i=0; i<1024; i++){
			iRawData[i] = RawSize32;		//Raw Data 計數初始值設為全滿
			iRawBuffer[i] = 0;
			first[i]=2;
			previous_vector_4[i]=0;
			previous_vector_8[i]=0;
			previous_state[i]=0;
			average_R[i]=130;
			for(int j=0; j<5; j++){
				State_num[i][j]=0;
				State_num_2[i][j]=0;
			}
		}
		
		A_state[0][0] = (float) 0.916678;
		A_state[0][1] = (float) 0;
		A_state[0][2] = (float) 0.00039362;
		A_state[0][3] = (float) 0.0746304;
		A_state[0][4] = (float) 0.00829751;
		A_state[1][0] = (float) 0;
		A_state[1][1] = (float) 0.957186;
		A_state[1][2] = (float) 0;
		A_state[1][3] = (float) 0.0428139;
		A_state[1][4] = (float) 0;
		A_state[2][0] = (float) 0.00362824;
		A_state[2][1] = (float) 0;
		A_state[2][2] = (float) 0.939715;
		A_state[2][3] = (float) 0.00474463;
		A_state[2][4] = (float) 0.0519118;
		A_state[3][0] = (float) 0.828586;
		A_state[3][1] = (float) 0.0462704;
		A_state[3][2] = (float) 0.00461057;
		A_state[3][3] = (float) 0.118722;
		A_state[3][4] = (float) 0.0018113;
		A_state[4][0] = (float) 0.223684;
		A_state[4][1] = (float) 0;
		A_state[4][2] = (float) 0.15747;
		A_state[4][3] = (float) 0.00339559;
		A_state[4][4] = (float) 0.61545;
		
		B_code[0][0] = (float) 0.0257949;
		B_code[0][1] = (float) 0.000170622;
		B_code[0][2] = (float) 0.0419885;
		B_code[0][3] = (float) 0.0233442;
		B_code[0][4] = (float) 0.037118;
		B_code[0][5] = (float) 0.0200714;
		B_code[0][6] = (float) 0.0194199;
		B_code[0][7] = (float) 0.0196526;
		B_code[0][8] = (float) 0.00705755;
		B_code[0][9] = (float) 0.000201644;
		B_code[0][10] = (float) 0.00888785;
		B_code[0][11] = (float) 0.044734;
		B_code[0][12] = (float) 0.0276563;
		B_code[0][13] = (float) 0.0255778;
		B_code[0][14] = (float) 0.0207538;
		B_code[0][15] = (float) 0.0143167;
		B_code[0][16] = (float) 0.0534978;
		B_code[0][17] = (float) 0.013293;
		B_code[0][18] = (float) 0.000325733;
		B_code[0][19] = (float) 0.0429037;
		B_code[0][20] = (float) 0.018179;
		B_code[0][21] = (float) 0.0263068;
		B_code[0][22] = (float) 0.0278734;
		B_code[0][23] = (float) 9.30665e-005;
		B_code[0][24] = (float) 0.0131689;
		B_code[0][25] = (float) 0.0302932;
		B_code[0][26] = (float) 0.00566155;
		B_code[0][27] = (float) 0.0667597;
		B_code[0][28] = (float) 0.00133395;
		B_code[0][29] = (float) 0.0203971;
		B_code[0][30] = (float) 0.00907399;
		B_code[0][31] = (float) 0.0329766;
		B_code[0][32] = (float) 0.0171087;
		B_code[0][33] = (float) 0.0311152;
		B_code[0][34] = (float) 0.0207849;
		B_code[0][35] = (float) 0.021002;
		B_code[0][36] = (float) 0.00947728;
		B_code[0][37] = (float) 0.0079727;
		B_code[0][38] = (float) 0.0545215;
		B_code[0][39] = (float) 0.0177757;
		B_code[0][40] = (float) 0.00913603;
		B_code[0][41] = (float) 0.0276097;
		B_code[0][42] = (float) 0.0134171;
		B_code[0][43] = (float) 0.0138824;
		B_code[0][44] = (float) 4.65333e-005;
		B_code[0][45] = (float) 0.00383124;
		B_code[0][46] = (float) 0.00812781;
		B_code[0][47] = (float) 0.032806;
		B_code[0][48] = (float) 0.000310222;
		B_code[0][49] = (float) 0.0121917;
		B_code[1][0] = (float) 0.000151172;
		B_code[1][1] = (float) 0.115797;
		B_code[1][2] = (float) 0.0154195;
		B_code[1][3] = (float) 0;
		B_code[1][4] = (float) 0.0128496;
		B_code[1][5] = (float) 0;
		B_code[1][6] = (float) 0.0258503;
		B_code[1][7] = (float) 0.0241875;
		B_code[1][8] = (float) 0.00151172;
		B_code[1][9] = (float) 0.0645503;
		B_code[1][10] = (float) 0;
		B_code[1][11] = (float) 0.0154195;
		B_code[1][12] = (float) 0.000302343;
		B_code[1][13] = (float) 0;
		B_code[1][14] = (float) 0;
		B_code[1][15] = (float) 0;
		B_code[1][16] = (float) 0.0243386;
		B_code[1][17] = (float) 0;
		B_code[1][18] = (float) 0;
		B_code[1][19] = (float) 0.0021164;
		B_code[1][20] = (float) 0.0238851;
		B_code[1][21] = (float) 0.0021164;
		B_code[1][22] = (float) 0.00120937;
		B_code[1][23] = (float) 0;
		B_code[1][24] = (float) 0;
		B_code[1][25] = (float) 0;
		B_code[1][26] = (float) 0.000151172;
		B_code[1][27] = (float) 0.000907029;
		B_code[1][28] = (float) 0.166893;
		B_code[1][29] = (float) 0;
		B_code[1][30] = (float) 0;
		B_code[1][31] = (float) 0;
		B_code[1][32] = (float) 0;
		B_code[1][33] = (float) 0;
		B_code[1][34] = (float) 0.0985639;
		B_code[1][35] = (float) 0.00559335;
		B_code[1][36] = (float) 0;
		B_code[1][37] = (float) 0.114588;
		B_code[1][38] = (float) 0.00362812;
		B_code[1][39] = (float) 0.0143613;
		B_code[1][40] = (float) 0.000907029;
		B_code[1][41] = (float) 0;
		B_code[1][42] = (float) 0.207407;
		B_code[1][43] = (float) 0.00453515;
		B_code[1][44] = (float) 0.0329554;
		B_code[1][45] = (float) 0;
		B_code[1][46] = (float) 0;
		B_code[1][47] = (float) 0.00982615;
		B_code[1][48] = (float) 0.00997732;
		B_code[1][49] = (float) 0;
		B_code[2][0] = (float) 0.0107392;
		B_code[2][1] = (float) 0.0882845;
		B_code[2][2] = (float) 0.0264993;
		B_code[2][3] = (float) 0;
		B_code[2][4] = (float) 0.0055788;
		B_code[2][5] = (float) 0;
		B_code[2][6] = (float) 0.00111576;
		B_code[2][7] = (float) 0.0221757;
		B_code[2][8] = (float) 0.101953;
		B_code[2][9] = (float) 0.140865;
		B_code[2][10] = (float) 0.00083682;
		B_code[2][11] = (float) 0.0338912;
		B_code[2][12] = (float) 0.00669456;
		B_code[2][13] = (float) 0.00348675;
		B_code[2][14] = (float) 0.0185495;
		B_code[2][15] = (float) 0.105579;
		B_code[2][16] = (float) 0.00460251;
		B_code[2][17] = (float) 0.0131102;
		B_code[2][18] = (float) 0.12901;
		B_code[2][19] = (float) 0.00013947;
		B_code[2][20] = (float) 0.00097629;
		B_code[2][21] = (float) 0.0230126;
		B_code[2][22] = (float) 0.00027894;
		B_code[2][23] = (float) 0;
		B_code[2][24] = (float) 0;
		B_code[2][25] = (float) 0.00027894;
		B_code[2][26] = (float) 0.00195258;
		B_code[2][27] = (float) 0.00488145;
		B_code[2][28] = (float) 0.00027894;
		B_code[2][29] = (float) 0.00599721;
		B_code[2][30] = (float) 0.00055788;
		B_code[2][31] = (float) 0.00083682;
		B_code[2][32] = (float) 0.00027894;
		B_code[2][33] = (float) 0.0013947;
		B_code[2][34] = (float) 0.00125523;
		B_code[2][35] = (float) 0.00041841;
		B_code[2][36] = (float) 0.00195258;
		B_code[2][37] = (float) 0.0157601;
		B_code[2][38] = (float) 0.0298466;
		B_code[2][39] = (float) 0.00027894;
		B_code[2][40] = (float) 0.00069735;
		B_code[2][41] = (float) 0.00181311;
		B_code[2][42] = (float) 0.00111576;
		B_code[2][43] = (float) 0.00041841;
		B_code[2][44] = (float) 0;
		B_code[2][45] = (float) 0.12106;
		B_code[2][46] = (float) 0;
		B_code[2][47] = (float) 0.0119944;
		B_code[2][48] = (float) 0.0595537;
		B_code[2][49] = (float) 0;
		B_code[3][0] = (float) 0.0244262;
		B_code[3][1] = (float) 0.0419672;
		B_code[3][2] = (float) 0.0614754;
		B_code[3][3] = (float) 0.0183607;
		B_code[3][4] = (float) 0.0413115;
		B_code[3][5] = (float) 0.0486885;
		B_code[3][6] = (float) 0;
		B_code[3][7] = (float) 0.034918;
		B_code[3][8] = (float) 0.00622951;
		B_code[3][9] = (float) 0.0129508;
		B_code[3][10] = (float) 0.0439344;
		B_code[3][11] = (float) 0.0147541;
		B_code[3][12] = (float) 0.000983607;
		B_code[3][13] = (float) 0.03;
		B_code[3][14] = (float) 0.0204918;
		B_code[3][15] = (float) 0.0213115;
		B_code[3][16] = (float) 0.0047541;
		B_code[3][17] = (float) 0.0178689;
		B_code[3][18] = (float) 0.03;
		B_code[3][19] = (float) 0.0104918;
		B_code[3][20] = (float) 0.0201639;
		B_code[3][21] = (float) 0.0940984;
		B_code[3][22] = (float) 0.000163934;
		B_code[3][23] = (float) 0.0290164;
		B_code[3][24] = (float) 0.0178689;
		B_code[3][25] = (float) 0.000655738;
		B_code[3][26] = (float) 0.0260656;
		B_code[3][27] = (float) 0.00885246;
		B_code[3][28] = (float) 0.0139344;
		B_code[3][29] = (float) 0.0236066;
		B_code[3][30] = (float) 0.00409836;
		B_code[3][31] = (float) 0.0114754;
		B_code[3][32] = (float) 0.0219672;
		B_code[3][33] = (float) 0.0163934;
		B_code[3][34] = (float) 0.0154098;
		B_code[3][35] = (float) 0.00704918;
		B_code[3][36] = (float) 0.0106557;
		B_code[3][37] = (float) 0.0291803;
		B_code[3][38] = (float) 0.012459;
		B_code[3][39] = (float) 0.0177049;
		B_code[3][40] = (float) 0.0172131;
		B_code[3][41] = (float) 0.00147541;
		B_code[3][42] = (float) 0.0442623;
		B_code[3][43] = (float) 0.0129508;
		B_code[3][44] = (float) 0.00606557;
		B_code[3][45] = (float) 0.00229508;
		B_code[3][46] = (float) 0.017377;
		B_code[3][47] = (float) 0.00196721;
		B_code[3][48] = (float) 0.000491803;
		B_code[3][49] = (float) 0.0301639;
		B_code[4][0] = (float) 0.00742881;
		B_code[4][1] = (float) 0.000412712;
		B_code[4][2] = (float) 0.00123813;
		B_code[4][3] = (float) 0.000412712;
		B_code[4][4] = (float) 0.000825423;
		B_code[4][5] = (float) 0;
		B_code[4][6] = (float) 0.021461;
		B_code[4][7] = (float) 0.00165085;
		B_code[4][8] = (float) 0.0656211;
		B_code[4][9] = (float) 0;
		B_code[4][10] = (float) 0.00123813;
		B_code[4][11] = (float) 0.015683;
		B_code[4][12] = (float) 0.0330169;
		B_code[4][13] = (float) 0.00495254;
		B_code[4][14] = (float) 0.000825423;
		B_code[4][15] = (float) 0.0169212;
		B_code[4][16] = (float) 0.0953364;
		B_code[4][17] = (float) 0.000412712;
		B_code[4][18] = (float) 0;
		B_code[4][19] = (float) 0.00206356;
		B_code[4][20] = (float) 0.00288898;
		B_code[4][21] = (float) 0;
		B_code[4][22] = (float) 0.10648;
		B_code[4][23] = (float) 0.00123813;
		B_code[4][24] = (float) 0;
		B_code[4][25] = (float) 0.091622;
		B_code[4][26] = (float) 0.0317788;
		B_code[4][27] = (float) 0.0231118;
		B_code[4][28] = (float) 0.00495254;
		B_code[4][29] = (float) 0.0169212;
		B_code[4][30] = (float) 0;
		B_code[4][31] = (float) 0.0193974;
		B_code[4][32] = (float) 0.000825423;
		B_code[4][33] = (float) 0;
		B_code[4][34] = (float) 0.0152703;
		B_code[4][35] = (float) 0;
		B_code[4][36] = (float) 0.00206356;
		B_code[4][37] = (float) 0;
		B_code[4][38] = (float) 0.113496;
		B_code[4][39] = (float) 0.00206356;
		B_code[4][40] = (float) 0;
		B_code[4][41] = (float) 0.193974;
		B_code[4][42] = (float) 0;
		B_code[4][43] = (float) 0.00577796;
		B_code[4][44] = (float) 0;
		B_code[4][45] = (float) 0.058605;
		B_code[4][46] = (float) 0.00165085;
		B_code[4][47] = (float) 0;
		B_code[4][48] = (float) 0.0379695;
		B_code[4][49] = (float) 0.000412712;

		PI_first[0] = (float) 0.710526;
		PI_first[1] = (float) 0.0789474;
		PI_first[2] = (float) 0.131579;
		PI_first[3] = (float) 0.0526316;
		PI_first[4] = (float) 0;
		
		CodeBook_4[0][0] = (float) 73.004;
		CodeBook_4[0][1] = (float) 116.267;
		CodeBook_4[0][2] = (float) 102.962;
		CodeBook_4[0][3] = (float) 109.951;
		CodeBook_4[1][0] = (float) 107.793;
		CodeBook_4[1][1] = (float) 117.077;
		CodeBook_4[1][2] = (float) 168.648;
		CodeBook_4[1][3] = (float) 122.483;
		CodeBook_4[2][0] = (float) 69.1873;
		CodeBook_4[2][1] = (float) 83.0688;
		CodeBook_4[2][2] = (float) 32.5722;
		CodeBook_4[2][3] = (float) 8.39982;
		CodeBook_4[3][0] = (float) 75.2458;
		CodeBook_4[3][1] = (float) 84.6362;
		CodeBook_4[3][2] = (float) 139.207;
		CodeBook_4[3][3] = (float) 84.3499;
		CodeBook_4[4][0] = (float) 17.7493;
		CodeBook_4[4][1] = (float) 56.6929;
		CodeBook_4[4][2] = (float) 4.9769;
		CodeBook_4[4][3] = (float) 10.5018;
		CodeBook_4[5][0] = (float) 118.537;
		CodeBook_4[5][1] = (float) 127.993;
		CodeBook_4[5][2] = (float) 185.114;
		CodeBook_4[5][3] = (float) 139.081;
		CodeBook_4[6][0] = (float) 42.3077;
		CodeBook_4[6][1] = (float) 60.9508;
		CodeBook_4[6][2] = (float) 70.0378;
		CodeBook_4[6][3] = (float) 62.567;
		CodeBook_4[7][0] = (float) 117.498;
		CodeBook_4[7][1] = (float) 127.112;
		CodeBook_4[7][2] = (float) 110.333;
		CodeBook_4[7][3] = (float) 83.17;
		CodeBook_4[8][0] = (float) 59.5601;
		CodeBook_4[8][1] = (float) 76.6638;
		CodeBook_4[8][2] = (float) 62.7682;
		CodeBook_4[8][3] = (float) 41.4008;
		CodeBook_4[9][0] = (float) 109.599;
		CodeBook_4[9][1] = (float) 128.938;
		CodeBook_4[9][2] = (float) 198.138;
		CodeBook_4[9][3] = (float) 161.826;
		CodeBook_4[10][0] = (float) 75.8917;
		CodeBook_4[10][1] = (float) 85.5986;
		CodeBook_4[10][2] = (float) 40.4641;
		CodeBook_4[10][3] = (float) 11.8119;
		CodeBook_4[11][0] = (float) 74.3707;
		CodeBook_4[11][1] = (float) 90.2344;
		CodeBook_4[11][2] = (float) 107.439;
		CodeBook_4[11][3] = (float) 76.1912;
		CodeBook_4[12][0] = (float) 35.4974;
		CodeBook_4[12][1] = (float) 48.4496;
		CodeBook_4[12][2] = (float) 16.4785;
		CodeBook_4[12][3] = (float) 12.6573;
		CodeBook_4[13][0] = (float) 117.621;
		CodeBook_4[13][1] = (float) 127.365;
		CodeBook_4[13][2] = (float) 124.371;
		CodeBook_4[13][3] = (float) 93.5343;
		CodeBook_4[14][0] = (float) 80.9492;
		CodeBook_4[14][1] = (float) 175.044;
		CodeBook_4[14][2] = (float) 114.121;
		CodeBook_4[14][3] = (float) 191.271;
		CodeBook_4[15][0] = (float) 97.4335;
		CodeBook_4[15][1] = (float) 112.898;
		CodeBook_4[15][2] = (float) 54.4729;
		CodeBook_4[15][3] = (float) 42.3054;
		CodeBook_4[16][0] = (float) 62.0407;
		CodeBook_4[16][1] = (float) 78.8567;
		CodeBook_4[16][2] = (float) 42.189;
		CodeBook_4[16][3] = (float) 28.1068;
		CodeBook_4[17][0] = (float) 13.7879;
		CodeBook_4[17][1] = (float) 29.9747;
		CodeBook_4[17][2] = (float) 2.7733;
		CodeBook_4[17][3] = (float) 3.53037;
		CodeBook_4[18][0] = (float) 74.2258;
		CodeBook_4[18][1] = (float) 147.829;
		CodeBook_4[18][2] = (float) 57.1391;
		CodeBook_4[18][3] = (float) 88.0871;
		CodeBook_4[19][0] = (float) 69.8873;
		CodeBook_4[19][1] = (float) 115.58;
		CodeBook_4[19][2] = (float) 54.0684;
		CodeBook_4[19][3] = (float) 62.4332;
		CodeBook_4[20][0] = (float) 118.632;
		CodeBook_4[20][1] = (float) 127.564;
		CodeBook_4[20][2] = (float) 81.7007;
		CodeBook_4[20][3] = (float) 61.1146;
		CodeBook_4[21][0] = (float) 19.5025;
		CodeBook_4[21][1] = (float) 108.42;
		CodeBook_4[21][2] = (float) 4.81482;
		CodeBook_4[21][3] = (float) 25.6563;
		CodeBook_4[22][0] = (float) 54.912;
		CodeBook_4[22][1] = (float) 71.6968;
		CodeBook_4[22][2] = (float) 34.0452;
		CodeBook_4[22][3] = (float) 23.7916;
		CodeBook_4[23][0] = (float) 113.765;
		CodeBook_4[23][1] = (float) 124.537;
		CodeBook_4[23][2] = (float) 144.853;
		CodeBook_4[23][3] = (float) 109.043;
		CodeBook_4[24][0] = (float) 16.947;
		CodeBook_4[24][1] = (float) 210.143;
		CodeBook_4[24][2] = (float) 3.85207;
		CodeBook_4[24][3] = (float) 42.6158;
		CodeBook_4[25][0] = (float) 46.0669;
		CodeBook_4[25][1] = (float) 68.3751;
		CodeBook_4[25][2] = (float) 46.4334;
		CodeBook_4[25][3] = (float) 41.2629;
		CodeBook_4[26][0] = (float) 68.6911;
		CodeBook_4[26][1] = (float) 81.9915;
		CodeBook_4[26][2] = (float) 88.1304;
		CodeBook_4[26][3] = (float) 55.8666;
		CodeBook_4[27][0] = (float) 63.8889;
		CodeBook_4[27][1] = (float) 100.076;
		CodeBook_4[27][2] = (float) 32.3053;
		CodeBook_4[27][3] = (float) 31.9304;
		CodeBook_4[28][0] = (float) 80.3551;
		CodeBook_4[28][1] = (float) 96.8213;
		CodeBook_4[28][2] = (float) 67.1427;
		CodeBook_4[28][3] = (float) 51.1234;
		CodeBook_4[29][0] = (float) 73.033;
		CodeBook_4[29][1] = (float) 93.4685;
		CodeBook_4[29][2] = (float) 50.1191;
		CodeBook_4[29][3] = (float) 40.233;
		CodeBook_4[30][0] = (float) 585.111;
		CodeBook_4[30][1] = (float) 594.556;
		CodeBook_4[30][2] = (float) 1081.14;
		CodeBook_4[30][3] = (float) 114.748;
		CodeBook_4[31][0] = (float) 80.793;
		CodeBook_4[31][1] = (float) 138.414;
		CodeBook_4[31][2] = (float) 113.684;
		CodeBook_4[31][3] = (float) 139.109;
		CodeBook_4[32][0] = (float) 117.769;
		CodeBook_4[32][1] = (float) 127.86;
		CodeBook_4[32][2] = (float) 94.2267;
		CodeBook_4[32][3] = (float) 71.2531;
		CodeBook_4[33][0] = (float) 117.722;
		CodeBook_4[33][1] = (float) 127.5;
		CodeBook_4[33][2] = (float) 233.331;
		CodeBook_4[33][3] = (float) 175.665;
		CodeBook_4[34][0] = (float) 64.313;
		CodeBook_4[34][1] = (float) 85.1145;
		CodeBook_4[34][2] = (float) 29.8499;
		CodeBook_4[34][3] = (float) 21.8583;
		CodeBook_4[35][0] = (float) 101.098;
		CodeBook_4[35][1] = (float) 225.129;
		CodeBook_4[35][2] = (float) 170.389;
		CodeBook_4[35][3] = (float) 308.731;
		CodeBook_4[36][0] = (float) 58.2143;
		CodeBook_4[36][1] = (float) 72.6587;
		CodeBook_4[36][2] = (float) 25.6634;
		CodeBook_4[36][3] = (float) 5.05873;
		CodeBook_4[37][0] = (float) 116.604;
		CodeBook_4[37][1] = (float) 128.823;
		CodeBook_4[37][2] = (float) 161.383;
		CodeBook_4[37][3] = (float) 124.473;
		CodeBook_4[38][0] = (float) 58.6895;
		CodeBook_4[38][1] = (float) 76.2266;
		CodeBook_4[38][2] = (float) 27.8532;
		CodeBook_4[38][3] = (float) 18.5963;
		CodeBook_4[39][0] = (float) 83.8987;
		CodeBook_4[39][1] = (float) 109.071;
		CodeBook_4[39][2] = (float) 84.5912;
		CodeBook_4[39][3] = (float) 76.3715;
		CodeBook_4[40][0] = (float) 20.0265;
		CodeBook_4[40][1] = (float) 123.122;
		CodeBook_4[40][2] = (float) 3.92348;
		CodeBook_4[40][3] = (float) -11.9938;
		CodeBook_4[41][0] = (float) 59.7601;
		CodeBook_4[41][1] = (float) 77.592;
		CodeBook_4[41][2] = (float) 35.2073;
		CodeBook_4[41][3] = (float) 23.1937;
		CodeBook_4[42][0] = (float) 15.4321;
		CodeBook_4[42][1] = (float) 146.389;
		CodeBook_4[42][2] = (float) 3.31042;
		CodeBook_4[42][3] = (float) 28.7806;
		CodeBook_4[43][0] = (float) 117.234;
		CodeBook_4[43][1] = (float) 128.107;
		CodeBook_4[43][2] = (float) 68.0624;
		CodeBook_4[43][3] = (float) 51.71;
		CodeBook_4[44][0] = (float) 103.303;
		CodeBook_4[44][1] = (float) 317.192;
		CodeBook_4[44][2] = (float) 176.117;
		CodeBook_4[44][3] = (float) 475.133;
		CodeBook_4[45][0] = (float) 52.5641;
		CodeBook_4[45][1] = (float) 68.3048;
		CodeBook_4[45][2] = (float) 24.1648;
		CodeBook_4[45][3] = (float) 16.6969;
		CodeBook_4[46][0] = (float) 90.9848;
		CodeBook_4[46][1] = (float) 108.939;
		CodeBook_4[46][2] = (float) 125.725;
		CodeBook_4[46][3] = (float) 110.07;
		CodeBook_4[47][0] = (float) 63.1945;
		CodeBook_4[47][1] = (float) 73.1481;
		CodeBook_4[47][2] = (float) 72.3391;
		CodeBook_4[47][3] = (float) 6.37442;
		CodeBook_4[48][0] = (float) 44.386;
		CodeBook_4[48][1] = (float) 59.7953;
		CodeBook_4[48][2] = (float) 32.9131;
		CodeBook_4[48][3] = (float) 24.893;
		CodeBook_4[49][0] = (float) 17.1922;
		CodeBook_4[49][1] = (float) 83.2883;
		CodeBook_4[49][2] = (float) 4.01175;
		CodeBook_4[49][3] = (float) 14.4612;
		
		CodeBook_8[0][0] = (float) 2.15564;
		CodeBook_8[0][1] = (float) 2.61724;
		CodeBook_8[0][2] = (float) 62.426;
		CodeBook_8[0][3] = (float) 77.1782;
		CodeBook_8[0][4] = (float) 0.0581117;
		CodeBook_8[0][5] = (float) 0.106946;
		CodeBook_8[0][6] = (float) 81.5617;
		CodeBook_8[0][7] = (float) 51.9387;
		CodeBook_8[1][0] = (float) 1.51075;
		CodeBook_8[1][1] = (float) 2.61535;
		CodeBook_8[1][2] = (float) 89.9992;
		CodeBook_8[1][3] = (float) 151.708;
		CodeBook_8[1][4] = (float) 0.0389875;
		CodeBook_8[1][5] = (float) 0.0541533;
		CodeBook_8[1][6] = (float) 116.631;
		CodeBook_8[1][7] = (float) 146.765;
		CodeBook_8[2][0] = (float) 1.07866;
		CodeBook_8[2][1] = (float) 1.21763;
		CodeBook_8[2][2] = (float) 65.1308;
		CodeBook_8[2][3] = (float) 76.9522;
		CodeBook_8[2][4] = (float) 0.0290238;
		CodeBook_8[2][5] = (float) 0.045134;
		CodeBook_8[2][6] = (float) 40.3192;
		CodeBook_8[2][7] = (float) 23.9531;
		CodeBook_8[3][0] = (float) 1.35091;
		CodeBook_8[3][1] = (float) 1.90148;
		CodeBook_8[3][2] = (float) 50.1827;
		CodeBook_8[3][3] = (float) 87.975;
		CodeBook_8[3][4] = (float) 0.0486897;
		CodeBook_8[3][5] = (float) 0.0885411;
		CodeBook_8[3][6] = (float) 47.4215;
		CodeBook_8[3][7] = (float) 56.5332;
		CodeBook_8[4][0] = (float) 1.69951;
		CodeBook_8[4][1] = (float) 1.81565;
		CodeBook_8[4][2] = (float) 97.7748;
		CodeBook_8[4][3] = (float) 108.58;
		CodeBook_8[4][4] = (float) 0.0580073;
		CodeBook_8[4][5] = (float) 0.0276685;
		CodeBook_8[4][6] = (float) 89.5497;
		CodeBook_8[4][7] = (float) 70.2236;
		CodeBook_8[5][0] = (float) 2.41167;
		CodeBook_8[5][1] = (float) 2.62939;
		CodeBook_8[5][2] = (float) 43.2986;
		CodeBook_8[5][3] = (float) 51.9855;
		CodeBook_8[5][4] = (float) 0.0923764;
		CodeBook_8[5][5] = (float) 0.155518;
		CodeBook_8[5][6] = (float) 56.8588;
		CodeBook_8[5][7] = (float) 33.7655;
		CodeBook_8[6][0] = (float) 1.0578;
		CodeBook_8[6][1] = (float) 1.39414;
		CodeBook_8[6][2] = (float) 67.0919;
		CodeBook_8[6][3] = (float) 86.6778;
		CodeBook_8[6][4] = (float) 0.0291514;
		CodeBook_8[6][5] = (float) 0.0478742;
		CodeBook_8[6][6] = (float) 46.7909;
		CodeBook_8[6][7] = (float) 34.7517;
		CodeBook_8[7][0] = (float) 0.814782;
		CodeBook_8[7][1] = (float) 0.854889;
		CodeBook_8[7][2] = (float) 62.9429;
		CodeBook_8[7][3] = (float) 75.354;
		CodeBook_8[7][4] = (float) 0.0243754;
		CodeBook_8[7][5] = (float) 0.0320818;
		CodeBook_8[7][6] = (float) 28.5162;
		CodeBook_8[7][7] = (float) 17.2738;
		CodeBook_8[8][0] = (float) 1.57384;
		CodeBook_8[8][1] = (float) 1.84768;
		CodeBook_8[8][2] = (float) 81.0765;
		CodeBook_8[8][3] = (float) 92.9232;
		CodeBook_8[8][4] = (float) 0.0459955;
		CodeBook_8[8][5] = (float) 0.0425282;
		CodeBook_8[8][6] = (float) 74.8981;
		CodeBook_8[8][7] = (float) 52.4777;
		CodeBook_8[9][0] = (float) 1.47942;
		CodeBook_8[9][1] = (float) 2.47325;
		CodeBook_8[9][2] = (float) 87.0009;
		CodeBook_8[9][3] = (float) 137.313;
		CodeBook_8[9][4] = (float) 0.0387308;
		CodeBook_8[9][5] = (float) 0.054148;
		CodeBook_8[9][6] = (float) 106.41;
		CodeBook_8[9][7] = (float) 121.119;
		CodeBook_8[10][0] = (float) 3.64488;
		CodeBook_8[10][1] = (float) 4.16314;
		CodeBook_8[10][2] = (float) 62.8813;
		CodeBook_8[10][3] = (float) 75.734;
		CodeBook_8[10][4] = (float) 0.0943132;
		CodeBook_8[10][5] = (float) 0.173376;
		CodeBook_8[10][6] = (float) 130.784;
		CodeBook_8[10][7] = (float) 77.0715;
		CodeBook_8[11][0] = (float) 1.89361;
		CodeBook_8[11][1] = (float) 1.78905;
		CodeBook_8[11][2] = (float) 70.5006;
		CodeBook_8[11][3] = (float) 81.3076;
		CodeBook_8[11][4] = (float) 0.0493344;
		CodeBook_8[11][5] = (float) 0.0563746;
		CodeBook_8[11][6] = (float) 68.7187;
		CodeBook_8[11][7] = (float) 38.3307;
		CodeBook_8[12][0] = (float) 2.01722;
		CodeBook_8[12][1] = (float) 2.35137;
		CodeBook_8[12][2] = (float) 61.2943;
		CodeBook_8[12][3] = (float) 75.7368;
		CodeBook_8[12][4] = (float) 0.0567177;
		CodeBook_8[12][5] = (float) 0.0954791;
		CodeBook_8[12][6] = (float) 72.0317;
		CodeBook_8[12][7] = (float) 46.5337;
		CodeBook_8[13][0] = (float) 2.30876;
		CodeBook_8[13][1] = (float) 3.23166;
		CodeBook_8[13][2] = (float) 59.4818;
		CodeBook_8[13][3] = (float) 77.3499;
		CodeBook_8[13][4] = (float) 0.0615794;
		CodeBook_8[13][5] = (float) 0.152332;
		CodeBook_8[13][6] = (float) 95.9856;
		CodeBook_8[13][7] = (float) 63.7661;
		CodeBook_8[14][0] = (float) 1.66456;
		CodeBook_8[14][1] = (float) 2.66564;
		CodeBook_8[14][2] = (float) 64.3592;
		CodeBook_8[14][3] = (float) 88.6319;
		CodeBook_8[14][4] = (float) 0.0445686;
		CodeBook_8[14][5] = (float) 0.103043;
		CodeBook_8[14][6] = (float) 85.5524;
		CodeBook_8[14][7] = (float) 67.5694;
		CodeBook_8[15][0] = (float) 2.04378;
		CodeBook_8[15][1] = (float) 2.67578;
		CodeBook_8[15][2] = (float) 63.2981;
		CodeBook_8[15][3] = (float) 99.9111;
		CodeBook_8[15][4] = (float) 0.0638385;
		CodeBook_8[15][5] = (float) 0.0990286;
		CodeBook_8[15][6] = (float) 84.1279;
		CodeBook_8[15][7] = (float) 87.8955;
		CodeBook_8[16][0] = (float) 1.70095;
		CodeBook_8[16][1] = (float) 1.79372;
		CodeBook_8[16][2] = (float) 63.6534;
		CodeBook_8[16][3] = (float) 75.3208;
		CodeBook_8[16][4] = (float) 0.0465947;
		CodeBook_8[16][5] = (float) 0.0684357;
		CodeBook_8[16][6] = (float) 57.5003;
		CodeBook_8[16][7] = (float) 34.4477;
		CodeBook_8[17][0] = (float) 1.63495;
		CodeBook_8[17][1] = (float) 2.21287;
		CodeBook_8[17][2] = (float) 65.4139;
		CodeBook_8[17][3] = (float) 87.662;
		CodeBook_8[17][4] = (float) 0.0438974;
		CodeBook_8[17][5] = (float) 0.0828669;
		CodeBook_8[17][6] = (float) 72.2295;
		CodeBook_8[17][7] = (float) 55.072;
		CodeBook_8[18][0] = (float) 1.96665;
		CodeBook_8[18][1] = (float) 2.37599;
		CodeBook_8[18][2] = (float) 61.8654;
		CodeBook_8[18][3] = (float) 120.871;
		CodeBook_8[18][4] = (float) 0.0863176;
		CodeBook_8[18][5] = (float) 0.061712;
		CodeBook_8[18][6] = (float) 73.4933;
		CodeBook_8[18][7] = (float) 116.027;
		CodeBook_8[19][0] = (float) 1.32939;
		CodeBook_8[19][1] = (float) 1.85807;
		CodeBook_8[19][2] = (float) 48.6849;
		CodeBook_8[19][3] = (float) 75.9477;
		CodeBook_8[19][4] = (float) 0.0485929;
		CodeBook_8[19][5] = (float) 0.0900081;
		CodeBook_8[19][6] = (float) 45.0957;
		CodeBook_8[19][7] = (float) 44.7989;
		CodeBook_8[20][0] = (float) 1.09803;
		CodeBook_8[20][1] = (float) 1.65004;
		CodeBook_8[20][2] = (float) 62.2101;
		CodeBook_8[20][3] = (float) 92.2524;
		CodeBook_8[20][4] = (float) 0.0318639;
		CodeBook_8[20][5] = (float) 0.0625904;
		CodeBook_8[20][6] = (float) 51.2145;
		CodeBook_8[20][7] = (float) 47.2537;
		CodeBook_8[21][0] = (float) 1.54927;
		CodeBook_8[21][1] = (float) 1.68898;
		CodeBook_8[21][2] = (float) 49.7736;
		CodeBook_8[21][3] = (float) 64.0314;
		CodeBook_8[21][4] = (float) 0.0532422;
		CodeBook_8[21][5] = (float) 0.0855383;
		CodeBook_8[21][6] = (float) 42.5116;
		CodeBook_8[21][7] = (float) 29.0717;
		CodeBook_8[22][0] = (float) 0.877242;
		CodeBook_8[22][1] = (float) 0.822319;
		CodeBook_8[22][2] = (float) 77.2402;
		CodeBook_8[22][3] = (float) 92.0246;
		CodeBook_8[22][4] = (float) 0.0228026;
		CodeBook_8[22][5] = (float) 0.0214242;
		CodeBook_8[22][6] = (float) 35.0335;
		CodeBook_8[22][7] = (float) 21.9862;
		CodeBook_8[23][0] = (float) 4.06417;
		CodeBook_8[23][1] = (float) 4.06417;
		CodeBook_8[23][2] = (float) 0;
		CodeBook_8[23][3] = (float) 17.1297;
		CodeBook_8[23][4] = (float) 0.00456651;
		CodeBook_8[23][5] = (float) -0.00456651;
		CodeBook_8[23][6] = (float) 0;
		CodeBook_8[23][7] = (float) -2061.96;
		CodeBook_8[24][0] = (float) 2.49013;
		CodeBook_8[24][1] = (float) 2.70057;
		CodeBook_8[24][2] = (float) 47.5235;
		CodeBook_8[24][3] = (float) 56.5988;
		CodeBook_8[24][4] = (float) 0.0900969;
		CodeBook_8[24][5] = (float) 0.138725;
		CodeBook_8[24][6] = (float) 64.2173;
		CodeBook_8[24][7] = (float) 38.7862;
		CodeBook_8[25][0] = (float) 2.2298;
		CodeBook_8[25][1] = (float) 2.31568;
		CodeBook_8[25][2] = (float) 51.1272;
		CodeBook_8[25][3] = (float) 64.6948;
		CodeBook_8[25][4] = (float) 0.0738142;
		CodeBook_8[25][5] = (float) 0.112789;
		CodeBook_8[25][6] = (float) 59.3714;
		CodeBook_8[25][7] = (float) 39.7248;
		CodeBook_8[26][0] = (float) 1.13426;
		CodeBook_8[26][1] = (float) 1.69833;
		CodeBook_8[26][2] = (float) 60.7236;
		CodeBook_8[26][3] = (float) 109.02;
		CodeBook_8[26][4] = (float) 0.035009;
		CodeBook_8[26][5] = (float) 0.0644013;
		CodeBook_8[26][6] = (float) 51.4668;
		CodeBook_8[26][7] = (float) 63.9143;
		CodeBook_8[27][0] = (float) 1.36251;
		CodeBook_8[27][1] = (float) 1.34364;
		CodeBook_8[27][2] = (float) 45.7856;
		CodeBook_8[27][3] = (float) 58.96;
		CodeBook_8[27][4] = (float) 0.0504911;
		CodeBook_8[27][5] = (float) 0.0740543;
		CodeBook_8[27][6] = (float) 31.5861;
		CodeBook_8[27][7] = (float) 21.1767;
		CodeBook_8[28][0] = (float) 2.05288;
		CodeBook_8[28][1] = (float) 2.51613;
		CodeBook_8[28][2] = (float) 117.632;
		CodeBook_8[28][3] = (float) 128;
		CodeBook_8[28][4] = (float) 0.0528518;
		CodeBook_8[28][5] = (float) 0.0324799;
		CodeBook_8[28][6] = (float) 147.28;
		CodeBook_8[28][7] = (float) 111.312;
		CodeBook_8[29][0] = (float) 2.57679;
		CodeBook_8[29][1] = (float) 3.3655;
		CodeBook_8[29][2] = (float) 64.3943;
		CodeBook_8[29][3] = (float) 84.2515;
		CodeBook_8[29][4] = (float) 0.0792934;
		CodeBook_8[29][5] = (float) 0.11024;
		CodeBook_8[29][6] = (float) 108.218;
		CodeBook_8[29][7] = (float) 85.9055;
		CodeBook_8[30][0] = (float) 2.75013;
		CodeBook_8[30][1] = (float) 3.59693;
		CodeBook_8[30][2] = (float) 59.4229;
		CodeBook_8[30][3] = (float) 74.651;
		CodeBook_8[30][4] = (float) 0.0712257;
		CodeBook_8[30][5] = (float) 0.174546;
		CodeBook_8[30][6] = (float) 106.8;
		CodeBook_8[30][7] = (float) 64.6637;
		CodeBook_8[31][0] = (float) 2.26076;
		CodeBook_8[31][1] = (float) 2.37419;
		CodeBook_8[31][2] = (float) 55.1571;
		CodeBook_8[31][3] = (float) 68.5073;
		CodeBook_8[31][4] = (float) 0.0697602;
		CodeBook_8[31][5] = (float) 0.106717;
		CodeBook_8[31][6] = (float) 65.7218;
		CodeBook_8[31][7] = (float) 42.5147;
		CodeBook_8[32][0] = (float) 0.870068;
		CodeBook_8[32][1] = (float) 0.91214;
		CodeBook_8[32][2] = (float) 89.8519;
		CodeBook_8[32][3] = (float) 106.435;
		CodeBook_8[32][4] = (float) 0.0228426;
		CodeBook_8[32][5] = (float) 0.0181048;
		CodeBook_8[32][6] = (float) 41.5791;
		CodeBook_8[32][7] = (float) 30.905;
		CodeBook_8[33][0] = (float) 2.41034;
		CodeBook_8[33][1] = (float) 3.20513;
		CodeBook_8[33][2] = (float) 62.6447;
		CodeBook_8[33][3] = (float) 81.3231;
		CodeBook_8[33][4] = (float) 0.0746447;
		CodeBook_8[33][5] = (float) 0.110706;
		CodeBook_8[33][6] = (float) 100.289;
		CodeBook_8[33][7] = (float) 76.9856;
		CodeBook_8[34][0] = (float) 1.83918;
		CodeBook_8[34][1] = (float) 2.11319;
		CodeBook_8[34][2] = (float) 117.134;
		CodeBook_8[34][3] = (float) 129.227;
		CodeBook_8[34][4] = (float) 0.0476907;
		CodeBook_8[34][5] = (float) 0.0269935;
		CodeBook_8[34][6] = (float) 123.748;
		CodeBook_8[34][7] = (float) 95.6449;
		CodeBook_8[35][0] = (float) 0.812733;
		CodeBook_8[35][1] = (float) 0.844602;
		CodeBook_8[35][2] = (float) 35.567;
		CodeBook_8[35][3] = (float) 48.5814;
		CodeBook_8[35][4] = (float) 0.0424527;
		CodeBook_8[35][5] = (float) 0.0539729;
		CodeBook_8[35][6] = (float) 15.4828;
		CodeBook_8[35][7] = (float) 11.7857;
		CodeBook_8[36][0] = (float) 2.13872;
		CodeBook_8[36][1] = (float) 2.22066;
		CodeBook_8[36][2] = (float) 41.6622;
		CodeBook_8[36][3] = (float) 51.7153;
		CodeBook_8[36][4] = (float) 0.0897085;
		CodeBook_8[36][5] = (float) 0.131591;
		CodeBook_8[36][6] = (float) 47.0388;
		CodeBook_8[36][7] = (float) 30.1301;
		CodeBook_8[37][0] = (float) 1.72712;
		CodeBook_8[37][1] = (float) 2.27061;
		CodeBook_8[37][2] = (float) 97.2901;
		CodeBook_8[37][3] = (float) 109.056;
		CodeBook_8[37][4] = (float) 0.0525582;
		CodeBook_8[37][5] = (float) 0.0375827;
		CodeBook_8[37][6] = (float) 111.048;
		CodeBook_8[37][7] = (float) 83.2591;
		CodeBook_8[38][0] = (float) 1.7109;
		CodeBook_8[38][1] = (float) 1.78765;
		CodeBook_8[38][2] = (float) 58.1808;
		CodeBook_8[38][3] = (float) 69.4103;
		CodeBook_8[38][4] = (float) 0.0507609;
		CodeBook_8[38][5] = (float) 0.0767657;
		CodeBook_8[38][6] = (float) 52.1491;
		CodeBook_8[38][7] = (float) 31.3797;
		CodeBook_8[39][0] = (float) 0.798556;
		CodeBook_8[39][1] = (float) 1.24796;
		CodeBook_8[39][2] = (float) 65.6569;
		CodeBook_8[39][3] = (float) 105.701;
		CodeBook_8[39][4] = (float) 0.0226275;
		CodeBook_8[39][5] = (float) 0.0426983;
		CodeBook_8[39][6] = (float) 40.9908;
		CodeBook_8[39][7] = (float) 43.4299;
		CodeBook_8[40][0] = (float) 0.945809;
		CodeBook_8[40][1] = (float) 1.04531;
		CodeBook_8[40][2] = (float) 110.19;
		CodeBook_8[40][3] = (float) 123.329;
		CodeBook_8[40][4] = (float) 0.0255751;
		CodeBook_8[40][5] = (float) 0.0145366;
		CodeBook_8[40][6] = (float) 57.871;
		CodeBook_8[40][7] = (float) 44.5904;
		CodeBook_8[41][0] = (float) 2.2431;
		CodeBook_8[41][1] = (float) 3.11989;
		CodeBook_8[41][2] = (float) 57.8036;
		CodeBook_8[41][3] = (float) 74.7829;
		CodeBook_8[41][4] = (float) 0.0599911;
		CodeBook_8[41][5] = (float) 0.15606;
		CodeBook_8[41][6] = (float) 90.0452;
		CodeBook_8[41][7] = (float) 58.0021;
		CodeBook_8[42][0] = (float) 0.743938;
		CodeBook_8[42][1] = (float) 1.14536;
		CodeBook_8[42][2] = (float) 60.6359;
		CodeBook_8[42][3] = (float) 89.7513;
		CodeBook_8[42][4] = (float) 0.0288483;
		CodeBook_8[42][5] = (float) 0.035044;
		CodeBook_8[42][6] = (float) 34.6966;
		CodeBook_8[42][7] = (float) 36.0291;
		CodeBook_8[43][0] = (float) 1.33401;
		CodeBook_8[43][1] = (float) 1.50981;
		CodeBook_8[43][2] = (float) 110.327;
		CodeBook_8[43][3] = (float) 121.638;
		CodeBook_8[43][4] = (float) 0.0358846;
		CodeBook_8[43][5] = (float) 0.0209235;
		CodeBook_8[43][6] = (float) 83.3642;
		CodeBook_8[43][7] = (float) 63.2583;
		CodeBook_8[44][0] = (float) 2.50615;
		CodeBook_8[44][1] = (float) 3.79149;
		CodeBook_8[44][2] = (float) 99.9875;
		CodeBook_8[44][3] = (float) 151.986;
		CodeBook_8[44][4] = (float) 0.0644901;
		CodeBook_8[44][5] = (float) 0.0624302;
		CodeBook_8[44][6] = (float) 189.444;
		CodeBook_8[44][7] = (float) 214.503;
		CodeBook_8[45][0] = (float) 2.15983;
		CodeBook_8[45][1] = (float) 2.69125;
		CodeBook_8[45][2] = (float) 72.1102;
		CodeBook_8[45][3] = (float) 109.235;
		CodeBook_8[45][4] = (float) 0.0614905;
		CodeBook_8[45][5] = (float) 0.0800944;
		CodeBook_8[45][6] = (float) 96.181;
		CodeBook_8[45][7] = (float) 98.1696;
		CodeBook_8[46][0] = (float) 2.25678;
		CodeBook_8[46][1] = (float) 3.0416;
		CodeBook_8[46][2] = (float) 51.5212;
		CodeBook_8[46][3] = (float) 70.7329;
		CodeBook_8[46][4] = (float) 0.0722343;
		CodeBook_8[46][5] = (float) 0.155;
		CodeBook_8[46][6] = (float) 78.3248;
		CodeBook_8[46][7] = (float) 59.1661;
		CodeBook_8[47][0] = (float) 1.62297;
		CodeBook_8[47][1] = (float) 1.81436;
		CodeBook_8[47][2] = (float) 110.979;
		CodeBook_8[47][3] = (float) 124.254;
		CodeBook_8[47][4] = (float) 0.0460714;
		CodeBook_8[47][5] = (float) 0.02442;
		CodeBook_8[47][6] = (float) 100.813;
		CodeBook_8[47][7] = (float) 79.79;
		CodeBook_8[48][0] = (float) 1.55981;
		CodeBook_8[48][1] = (float) 2.63916;
		CodeBook_8[48][2] = (float) 78.8443;
		CodeBook_8[48][3] = (float) 89.425;
		CodeBook_8[48][4] = (float) 0.0409194;
		CodeBook_8[48][5] = (float) 0.0667636;
		CodeBook_8[48][6] = (float) 103.862;
		CodeBook_8[48][7] = (float) 66.8319;
		CodeBook_8[49][0] = (float) 3.44972;
		CodeBook_8[49][1] = (float) 3.98071;
		CodeBook_8[49][2] = (float) 60.9588;
		CodeBook_8[49][3] = (float) 74.0247;
		CodeBook_8[49][4] = (float) 0.0893116;
		CodeBook_8[49][5] = (float) 0.17878;
		CodeBook_8[49][6] = (float) 121.308;
		CodeBook_8[49][7] = (float) 70.3658;
		
		Info_Buffer = new byte [0];
		setState(STATE_WAVEON);
	}
	
	public void reset()
	{
		/*for(int i=0; i<1024; i++){	
			iRawData[i] = RawSize32;			
			iRawBuffer[i] = 0;
		}
		setState(STATE_NONE);*/	
	}
	
    public synchronized void setState(int state){
        mState = state;
    }

    public synchronized int getState() {
        return mState;
    }

    //設定當前顯示心電圖的病患id
    public void setWaveOnId(int a, int b, int c, int d){
    	wave_on_id[0]=a;
    	wave_on_id[1]=b;
    	wave_on_id[2]=c;
    	wave_on_id[3]=d;
    }
    
	public void DataHandler (byte[] Data, int patinet_data_id)
	{
//		if (D) Log.d(TAG, "DataHandler");
		
		int iDataEnd = -1;			//存放資訊結尾('\n')的指標在data中的位置
		
		for(int i=0; i < Data.length ; i++ )
		{
			//若資訊為Raw=32,後面接到的32bytes 全為RawData
			if (iRawData[patinet_data_id] < RawSize32)
			{
				Raw_Buffer[patinet_data_id][iRawBuffer[patinet_data_id]] = Data[i];
				iRawData[patinet_data_id]++;
				iRawBuffer[patinet_data_id]++;
				iDataEnd = i;
				
				if(iRawBuffer[patinet_data_id]==RawBufferSize)
				{			
					// Notice (A)					
					byte [] rawData = new byte[RawBufferSize];				                        
					System.arraycopy(Raw_Buffer[patinet_data_id], 0, rawData, 0, RawBufferSize);
					
					// Send the obtained bytes to the UI Activity
		            // arg1-> length, arg2-> -1, obj->buffer
					int state = stateCheck(rawData, patinet_data_id, first[patinet_data_id]);
					
					if(state<5){
						first[patinet_data_id]--;
						if(first[patinet_data_id]<0)
							first[patinet_data_id]=0;
					}
					
					Message msg = mHandler.obtainMessage(Main.MESSAGE_STATE);
					Bundle bundle = new Bundle();
				    bundle.putString(Main.WARN_TYPE, String.valueOf(state));
				    bundle.putString(Main.PATIENT_ID, Integer.toString(patinet_data_id));
				    msg.setData(bundle);
				    mHandler.sendMessage(msg);

					if(patinet_data_id==wave_on_id[0]){
						mHandler.obtainMessage(Main.MESSAGE_RAW_1, RawBufferSize, -1, rawData)
			                    .sendToTarget();
					}
					if(patinet_data_id==wave_on_id[1]){
						mHandler.obtainMessage(Main.MESSAGE_RAW_2, RawBufferSize, -1, rawData)
			                    .sendToTarget();
					}
					if(patinet_data_id==wave_on_id[2]){
						mHandler.obtainMessage(Main.MESSAGE_RAW_3, RawBufferSize, -1, rawData)
			                    .sendToTarget();
					}
					if(patinet_data_id==wave_on_id[3]){
						mHandler.obtainMessage(Main.MESSAGE_RAW_4, RawBufferSize, -1, rawData)
			                    .sendToTarget();
					}
					
					msg = mHandler.obtainMessage(Main.MESSAGE_UPDATE);
					bundle = new Bundle();
				    bundle.putString(Main.UPDATE_STRING, patinet_data_id + "," + "ECG" + " " + BytesToString(rawData));
				    msg.setData(bundle);
				    mHandler.sendMessage(msg);
					
					iRawBuffer[patinet_data_id] = 0;
				}
			
			}
			//若字元為資訊結尾字元(0x0D)('\n')
			else if(Data[i] == 0x0D) 
			{				
				//若Info_Buffer 為空
				if (Info_Buffer.length == 0)
				{	
					Info_Buffer = new byte[i-iDataEnd];				
					System.arraycopy(Data, iDataEnd+1, Info_Buffer, 0, i-iDataEnd);
				}else
				{
					byte[] Temp = new byte[i-iDataEnd];
					System.arraycopy(Data, iDataEnd+1, Temp, 0, Temp.length);
					Info_Buffer = Combine(Info_Buffer,Temp);
				}
								
				iDataEnd = i;//指向資訊結尾的指標指在('\n')的位置
				
				//將完整的資訊傳給InfoHandler處理
				InfoHandler(Info_Buffer, patinet_data_id);
				
			}//End of [if(Data[i] == 0x0D)]
			else if(i==Data.length-1)//將作0X0D判斷後不完整的資訊存入Info_Buffer
			{	
				byte[] Temp = new byte[i - iDataEnd];

				System.arraycopy(Data, iDataEnd+1, Temp, 0, Temp.length);				
				Info_Buffer = Combine(Info_Buffer,Temp);
			}
									
		}
		
	}
	
	public String BytesToString(byte[] bytes){
	    String s = "";

	    for(int i = 0; i < bytes.length; i++){
	        s = s + Integer.toString(bytes[i] & 0xFF) + " ";
	    }

	    return s;    
	}
	
	public int stateCheck(byte[] Data, int patinet_data_id, int first){
		int[] value = new int [64];
		int plus_number_count = 0;
		int plus_number_amount = 0;
		int threshold;
		int max_number = 0;
		int min_number = 0;
		int max_point = 0;
		int min_point = 0;
		int check=0;
		int now_state;
		float min=10000000, a=0;
		int vector_4;
		int vector_8;
		int vector_value=0;
		float t1 = (float) 0.88;
		double[][] probability = new double [5][5];
		double max_value=0;
		int max_i=0, max_j=0;
		
		Q = -1;
		R = -1;
		R_prime = -1;
		S = -1;
		T_prime = -1;

		if(Ratio_RR_count[patinet_data_id]<=0){
			Ratio_RR_count[patinet_data_id]=-1;
			average_RR_count[patinet_data_id]=100;
		}
		
		for(int i=0; i<Data.length; i++){
			value[i]= Data[i] & 0xFF;
		}
		int[] diff = new int [64];
		for(int i=1; i<Data.length; i++){
			diff[i]= value[i] - value[i-1];
		}
		for(int i=1; i<Data.length; i++){
			if(diff[i]>0){
				plus_number_count++;
				plus_number_amount = plus_number_amount + diff[i];
			}
		}
		if(plus_number_count==0)
			plus_number_count = 1;
		threshold = plus_number_amount * 2 / plus_number_count;
		
		for(int i=1; i<Data.length; i++){
			if(diff[i]<threshold){
				diff[i] = 0;
			}
		}
		for(int i=1; i<Data.length; i++){
			if(diff[i]>max_number){
				max_number = diff[i];
				max_point = i;
			}
		}
		
		if(max_point!=0 && max_point+8<Data.length){
			max_number=0;
		    
		    int temp = max_point;
			for(int i=max_point; i<temp+8; i++){
				if(value[i]>max_number ){
					max_number = value[i];
					max_point = i;
				}
			}
			if(max_number>130)
				average_R[patinet_data_id] = (float)(average_R[patinet_data_id]*0.75 + max_number*0.25);
			if(max_number>average_R[patinet_data_id]-10){
				R = max_point;
				
				if(Ratio_RR_count[patinet_data_id]>0){
					Ratio_RR_count[patinet_data_id] = Ratio_RR_count[patinet_data_id]+max_point;
					if(Ratio_RR_count[patinet_data_id]<280){
						Ratio_RR[patinet_data_id] = (float)Ratio_RR_count[patinet_data_id]/average_RR_count[patinet_data_id];
						average_RR_count[patinet_data_id] = (float) (average_RR_count[patinet_data_id] * 0.75 + (float)Ratio_RR_count[patinet_data_id] * 0.25);
					}
				}
				Ratio_RR_count[patinet_data_id] = 64-max_point;
				
				if(R-12>=0){
					min_number=255;
					for(int i=R-1; i>=R-12; i--){
						if(value[i]<min_number ){
							min_number = value[i];
							min_point = i;
						}
					}
					Q = min_point;
				}
				if(R+12<Data.length){
					min_number=255;
					for(int i=R+1; i<R+12; i++){
						if(value[i]<min_number){
							min_number = value[i];
							min_point = i;
						}
					}
					S = min_point;
					if(S+3<Data.length){
						T_prime = S+3;
						check=0;
						for(int i=S+3; i<Data.length; i++){
							if(diff[i]<=0 && value[i]>value[S]){
								check=1;
								T_prime=i-1;
								break;
							}
						}
						/*if(check==1){
							min_number=128;
							int d;
							for(int i=R+1; i<S; i++){
								d=value[T_prime]-value[i];
								if(d<0)
									d=d*(-1);
								if(d<min_number){
									min_number=d;
									min_point = i;
								}
							}
							R_prime = min_point;
						}*/
					}
				}				
			}
			else{
				if(Ratio_RR_count[patinet_data_id]>0)
					Ratio_RR_count[patinet_data_id] = Ratio_RR_count[patinet_data_id]+64;				
			}
		}
		else{
			if(Ratio_RR_count[patinet_data_id]>0)
				Ratio_RR_count[patinet_data_id] = Ratio_RR_count[patinet_data_id]+64;
		}
		
		if(Q>0 && R>0){
			H_QR[patinet_data_id] = ((float)value[R] - (float)value[Q])*6/256;
			Slope_QR[patinet_data_id] = H_QR[patinet_data_id]/(((float)R - (float)Q)*4);
		}
		if(R>0 && S>0){
			H_RS[patinet_data_id] = ((float)value[R] - (float)value[S])*6/256;
			Slope_RS[patinet_data_id] = H_RS[patinet_data_id]/(((float)S - (float)R)*4);
		}
		if(Q>0 && S>0)
			QRS_dur[patinet_data_id] = ((float)S - (float)Q)*4;
		if(Q>0 && R>0 && S>0){
			if(H_QR[patinet_data_id]>Slope_RS[patinet_data_id])
				Area_QRS[patinet_data_id] = H_QR[patinet_data_id]*QRS_dur[patinet_data_id]/2;
			else
				Area_QRS[patinet_data_id] = H_RS[patinet_data_id]*QRS_dur[patinet_data_id]/2;
		}
		if(Q>0 && T_prime>0)
			QTP_int[patinet_data_id] = ((float)T_prime - (float)Q)*4;
		if(R>0 && S>0 && T_prime>0){
			Area_RST[patinet_data_id] = (((float)value[R] - (float)value[S])*6/256)*(((float)T_prime - (float)R)*4)/2;
		}
	    
	    now_state = UNKNOWN;
    	if(H_QR[patinet_data_id]>0 && H_RS[patinet_data_id]>0 && QRS_dur[patinet_data_id]>0 && QTP_int[patinet_data_id]>0 && Ratio_RR[patinet_data_id]>0 && Slope_QR[patinet_data_id]>0 && Slope_RS[patinet_data_id]>0 && Area_QRS[patinet_data_id]>0 && Area_RST[patinet_data_id]>0){
			
    		for(int i=0; i<50; i++){
				a = (QRS_dur[patinet_data_id]-CodeBook_4[i][0])*(QRS_dur[patinet_data_id]-CodeBook_4[i][0])
						+ (QTP_int[patinet_data_id]-CodeBook_4[i][1])*(QTP_int[patinet_data_id]-CodeBook_4[i][1])
						+ (Area_QRS[patinet_data_id]-CodeBook_4[i][2])*(Area_QRS[patinet_data_id]-CodeBook_4[i][2])
						+ (Area_RST[patinet_data_id]-CodeBook_4[i][3])*(Area_RST[patinet_data_id]-CodeBook_4[i][3]);
				if(a<min){
					min = a;
					vector_value = i;
				}
				a = 0;
			}
			vector_4 = vector_value;
			
			min = 10000000;	
			for(int i=0; i<50; i++){
				a = (H_QR[patinet_data_id]-CodeBook_8[i][0])*(H_QR[patinet_data_id]-CodeBook_8[i][0])
						+ (H_RS[patinet_data_id]-CodeBook_8[i][1])*(H_RS[patinet_data_id]-CodeBook_8[i][1])
						+ (QRS_dur[patinet_data_id]-CodeBook_8[i][2])*(QRS_dur[patinet_data_id]-CodeBook_8[i][2])
						+ (QTP_int[patinet_data_id]-CodeBook_8[i][3])*(QTP_int[patinet_data_id]-CodeBook_8[i][3])
						+ (Slope_QR[patinet_data_id]-CodeBook_8[i][4])*(Slope_QR[patinet_data_id]-CodeBook_8[i][4])
						+ (Slope_RS[patinet_data_id]-CodeBook_8[i][5])*(Slope_RS[patinet_data_id]-CodeBook_8[i][5])
						+ (Area_QRS[patinet_data_id]-CodeBook_8[i][6])*(Area_QRS[patinet_data_id]-CodeBook_8[i][6])
						+ (Area_RST[patinet_data_id]-CodeBook_8[i][7])*(Area_RST[patinet_data_id]-CodeBook_8[i][7]);
				if(a<min){
					min = a;
					vector_value = i;
				}
				a = 0;
			}
			vector_8 = vector_value;
					
    		if(first==1){
    			if(Ratio_RR[patinet_data_id]>0.88){
    				for(int i=0; i<=2; i++){
    					for(int j=0; j<=4; j++){
    						if(j<=2){
    							probability[i][j]=(double)PI_first[i]*(double)B_code[i][previous_vector_8[patinet_data_id]]*(double)A_state[i][j]*(double)B_code[j][vector_8];
    						}
    						else{
    							probability[i][j]=(double)PI_first[i]*(double)B_code[i][previous_vector_8[patinet_data_id]]*(double)A_state[i][j]*(double)B_code[j][vector_4];
    						}
    					}
    				}
    				max_value=0;
    				for(int i=0; i<=2; i++){
    					for(int j=0; j<=4; j++){
    						if(probability[i][j]>max_value){
    							max_i=i;
    							max_j=j;
    							max_value=probability[i][j];
    						}
    					}
    				}
    				now_state=max_i;	
    			}
    			else{
    				for(int i=3; i<=4; i++){
    					for(int j=0; j<=4; j++){
    						if(j<=2){
    							probability[i][j]=(double)PI_first[i]*(double)B_code[i][previous_vector_4[patinet_data_id]]*(double)A_state[i][j]*(double)B_code[j][vector_8];
    						}
    						else{
    							probability[i][j]=(double)PI_first[i]*(double)B_code[i][previous_vector_4[patinet_data_id]]*(double)A_state[i][j]*(double)B_code[j][vector_4];
    						}
    					}
    				}
    				max_value=0;
    				for(int i=3; i<=4; i++){
    					for(int j=0; j<=4; j++){
    						if(probability[i][j]>max_value){
    							max_i=i;
    							max_j=j;
    							max_value=probability[i][j];
    						}
    					}
    				}
    				now_state=max_i;    				
    			}
    	    }
    	    else{
    			if(Ratio_RR[patinet_data_id]>0.88){
    				for(int i=0; i<=2; i++){
    					for(int j=0; j<=4; j++){
    						if(j<=2){
    							probability[i][j]=(double)A_state[previous_state[patinet_data_id]][i]*(double)B_code[i][previous_vector_8[patinet_data_id]]*(double)A_state[i][j]*(double)B_code[j][vector_8];
    						}
    						else{
    							probability[i][j]=(double)A_state[previous_state[patinet_data_id]][i]*(double)B_code[i][previous_vector_8[patinet_data_id]]*(double)A_state[i][j]*(double)B_code[j][vector_4];
    						}
    					}
    				}
    				max_value=0;
    				for(int i=0; i<=2; i++){
    					for(int j=0; j<=4; j++){
    						if(probability[i][j]>max_value){
    							max_i=i;
    							max_j=j;
    							max_value=probability[i][j];
    						}
    					}
    				}
    				now_state=max_i;	
    			}
    			else{
    				for(int i=3; i<=4; i++){
    					for(int j=0; j<=4; j++){
    						if(j<=2){
    							probability[i][j]=(double)A_state[previous_state[patinet_data_id]][i]*(double)B_code[i][previous_vector_4[patinet_data_id]]*(double)A_state[i][j]*(double)B_code[j][vector_8];
    						}
    						else{
    							probability[i][j]=(double)A_state[previous_state[patinet_data_id]][i]*(double)B_code[i][previous_vector_4[patinet_data_id]]*(double)A_state[i][j]*(double)B_code[j][vector_4];
    						}
    					}
    				}
    				max_value=0;
    				for(int i=3; i<=4; i++){
    					for(int j=0; j<=4; j++){
    						if(probability[i][j]>max_value){
    							max_i=i;
    							max_j=j;
    							max_value=probability[i][j];
    						}
    					}
    				}
    				now_state=max_i;    				
    			}    	    	
    	    }
    		
    		previous_vector_4[patinet_data_id] = vector_4;
    		previous_vector_8[patinet_data_id] = vector_8;
    		previous_state[patinet_data_id] = now_state;
    	}

	    if(now_state<5){
    	
	    	State_num[patinet_data_id][now_state]++;
	    	String A = Integer.toString(State_num[patinet_data_id][0]) + " " + Integer.toString(State_num[patinet_data_id][1]) + " " + Integer.toString(State_num[patinet_data_id][2]) + " " + Integer.toString(State_num[patinet_data_id][3]) + " " + Integer.toString(State_num[patinet_data_id][4]) + "    ";
	    	
	    	//傳送狀態
	    }
		return now_state;
	}
	
	public void InfoHandler (byte[] Info, int patinet_data_id)
	{	
		String InfoStr = new String(Info,0,Info.length-1);	//去掉結尾0x0D
		
		if (D) Log.d(TAG, "InfoHandler Info:"+ InfoStr);
		
		if(InfoStr.equals("RAW=32")){
			iRawData[patinet_data_id] = 0;	
		}
		else{
	        Message msg = mHandler.obtainMessage(Main.MESSAGE_INFO);
	        Bundle bundle = new Bundle();
	        bundle.putString(Main.KY_INFO, InfoStr);
	        bundle.putString(Main.PATIENT_ID, Integer.toString(patinet_data_id));
	        msg.setData(bundle);
	        mHandler.sendMessage(msg);	
		}
		
		Info_Buffer = new byte [0];	
	}
	
	private byte[] Combine (byte[] A1,byte[] A2){	
		byte[] R = new byte[A1.length + A2.length];
		System.arraycopy(A1, 0, R, 0, A1.length);
		System.arraycopy(A2, 0, R, A1.length, A2.length);
		
		return R;
	}
}
