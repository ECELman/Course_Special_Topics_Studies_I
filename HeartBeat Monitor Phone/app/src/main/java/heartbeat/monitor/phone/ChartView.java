package heartbeat.monitor.phone;

/*
 *  KY LAB program source for development
 *
 *	Ver: EM202.1.0
 *	Data : 2010/06/01
 *	Designer : Weiting Lin
 *	
 *	function description:
 *	RAW Data chart draw.
 */
import heartbeat.monitor.phone.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

public class ChartView extends View
{
    // Debugging
    private static final String TAG = "ChartView";
    private static final boolean D = true;

    private String record = "";
    private int count = 0;
	
	protected static int mTileSize;
	final float PROPORTION = 1f;
	
    private Bitmap  mBitmap;
    private Paint   mPaint = new Paint();
    private Paint	textPaint = new Paint();
    private Canvas  mCanvas = new Canvas();
        
    private Resources res = getResources();
                
    private int X_Axis;   //
    private int Y_Axis;
    private float   mLastX;
    private float	mNextX;
    private float	mLastY;
    private float	mNextY;        
    private float   mSpeed;
    private float   mSpeed_orign;
    private int   mMaxX;    //
    
    private String mIHR ;

    private ShapeDrawable Mask = new ShapeDrawable(new RectShape());
        
    public ChartView(Context context) 
    {	// TODO Auto-generated constructor stub
		super(context);           
	}

    //從外部使用此class 則必需使用此初始函式
	public ChartView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		if (D) Log.d(TAG, "ChartView Initial");
						
		//get argument in value/dimens
		X_Axis = (int) res.getDimension(R.dimen.Max_X_Axis) ;	//Default 100px	//改成float
		Y_Axis = (int) res.getDimension(R.dimen.Max_Y_Axis) ;	//Default 400px
			
		mLastX = 0;
		mNextX = 0;
		mLastY = 0;
        mSpeed = 0.75f*1.5f; //0.5f
        mSpeed_orign = 1.0f*1.5f;
        mMaxX = X_Axis; //X_Axis

        //Set mPaint 各項參數
		Mask.getPaint().setColor(Color.BLACK);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle( Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setTextSize(50);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
                   
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.GREEN);
        textPaint.setStyle( Paint.Style.STROKE );
        textPaint.setStrokeWidth(2);
        textPaint.setTextSize(5); 
        
        //in pixel unit
        mBitmap = Bitmap.createBitmap(X_Axis, Y_Axis, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(Color.BLACK);
	}

    //將各變數初始化
	public void ClearChart()
	{
		if (mBitmap != null) 
        {
            final Canvas canvas = mCanvas;
            
         	Mask.setBounds(0,0 , X_Axis , Y_Axis);
           	Mask.draw(canvas);                                     
            invalidate();//invalidate()觸發 onDraw 事件
        }
		mLastX = 0;
		mLastY = 0;
	}
	
	public void setIHR(String IHR)
	{
            mIHR = IHR;
	}		
	
	public void setX_Axis(int xValue)
	{	
		X_Axis = xValue;
		
        //in pixel unit
        mBitmap = Bitmap.createBitmap(X_Axis, Y_Axis, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(Color.BLACK);
        
        mMaxX = X_Axis;
	}
	
	protected void onDraw(Canvas canvas) 
	{           			
         synchronized (this)
         {
        	 if (mBitmap != null) 
             {
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        }//end of [synchronized (this)]
	}//end of [protected void onDraw(Canvas canvas)] 
		
	public void Wave_Draw(byte[] Raw_Data)
	{
		synchronized (this)
		{
            if (mBitmap != null) 
            {
                final Canvas canvas = mCanvas;
                final Paint paint = mPaint;
                int Mask_Start;
                int Mask_End;
                
                
                
                Mask_Start = (int)mLastX;
                if(Monitor.Button_bth.getText().toString().equals("連結裝置")) Mask_End = (int)(mLastX + (Raw_Data.length * mSpeed));
                else Mask_End = (int)(mLastX + (Raw_Data.length * mSpeed_orign));

                if ( Mask_End < mMaxX)
                {
                 	Mask.setBounds(Mask_Start,0 , Mask_End , Y_Axis);
                   	Mask.draw(canvas);
                }else
                {
                	Mask.setBounds(Mask_Start,0 , mMaxX , Y_Axis);
                   	Mask.draw(canvas);
                   	Mask_End = Mask_End - mMaxX ;
                   	Mask.setBounds(0,0 ,Mask_End , Y_Axis);
                   	Mask.draw(canvas);
                }
                
                //Moving mark line
                canvas.drawLine(Mask_End+1, 0,Mask_End+1 , Y_Axis, paint);
               	
                for(int i = 0; i < Raw_Data.length; i++)
                {
                    if(Monitor.Button_bth.getText().toString().equals("連結裝置")) mNextX = mLastX + mSpeed;
                    else mNextX = mLastX + mSpeed_orign;
                    if( mNextX >= mMaxX ){mNextX = 0;}///跳過一個點，無損於ECG data 判讀 //==
                    else
                    {
                    	//Java not support unsigned value, so byte's value is -128 ~ 127
                    	mNextY =  Y_Axis -((Raw_Data[i] & 0xFF)* PROPORTION);

                        if (count != 1024) {
                            record += Float.toString(mNextY) + " ";
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
                                        + URLEncoder.encode("ECG", "UTF-8") + "=" + URLEncoder.encode(record, "UTF-8");
                                bufferedWriter.write(post_data);
                                bufferedWriter.flush();
                                bufferedWriter.close();
                                outputStream.close();
                                InputStream inputStream = httpURLConnection.getInputStream();
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                                while ((line = bufferedReader.readLine()) != null) result += line;
                                bufferedReader.close();
                                inputStream.close();
                                httpURLConnection.disconnect();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            record = "";
                            count = 0;
                        }

                    	if(Monitor.Button_bth.getText().toString().equals("連結裝置")) mNextY=mNextY*2.25f-700;
                    	else mNextY=mNextY*7.0f-2700;

                       	canvas.drawLine(mLastX, mLastY, mNextX, mNextY, paint);
                    }

                    mLastX = mNextX;
                    mLastY = mNextY;
                }

                count += Raw_Data.length;

                if(Monitor.Button_bth.getText().toString().equals("解除裝置")) {

                    String user_name = "111";
                    String user_password = "000";

                    String internet_button = Monitor.Button_internet.getText().toString().equals("連結網路") ? "false" : "true";
                    String internet_status = Monitor.textView3.getText().toString().equals("尚未連結") ? "false" : "true";
                    String syn_button = Monitor.Button_synch.getText().toString().equals("線上同步") ? "false" : "true";
                    String syn_status = Monitor.accountView.getText().toString().equals("尚未同步") ? "false" : "true";
                    String bth_button = Monitor.Button_bth.getText().toString().equals("連結裝置") ? "false" : "true";
                    String heart = Monitor.IHRText.getText().toString();
                    String temp = Monitor.TEText.getText().toString();
                    String status = Monitor.StateText.getText().toString();

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

                    /*if (count != 1024) {
                        for (int i = 0; i < Raw_Data.length; i++)
                            record += Byte.toString(Raw_Data[i]) + " ";
                        count += Raw_Data.length;
                    } else {
                        login_url = "http://192.168.43.190/ECG/add_bth_ECG.php";
                        result = "";
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
                                    + URLEncoder.encode("ECG", "UTF-8") + "=" + URLEncoder.encode(record, "UTF-8");
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

                        record = "";
                        count = 0;
                    }*/
                }

               invalidate();//invalidate()觸發 onDraw 事件
            }//end of [if (mBitmap != null) ]
        }//end of [synchronized (this) ]
	}//end of [public void Wave_Draw(String Data)]
	
		
}//end of [private class GraphView extends View]	

/**********************************
 Notice Notes
 *********************************/
/*(A)
 * Synchronized使用時，需指定一個物件，系統會Lock此物件，當
 * 程式進入Synchrnoized區塊或Method時，該物件會被Lock，直到
 * 離開Synchronized時才會被釋放。在Lock期間，鎖定同一物件的
 * 其他Synchronized區塊，會因為無法取得物件的Lock而等待。
 * 待物件Release Lock後，其他的Synchronized區塊會有一個取得
 * 該物件的Lock而可以執行。
 * */
/*(B)
 * 一般在剛開始開發android時，會犯一個錯誤，即在View的構造函數
 * 中獲取getWidth()和getHeight()，當一個view對象創建時，android
 * 並不知道其大小，所以getWidth()和getHeight( )返回的結果是0，真
 * 正大小是在計算佈局時才會計算，所以會發現一個有趣的事，即在
 * onDraw( ) 卻能取得長寬的原因。
 */

