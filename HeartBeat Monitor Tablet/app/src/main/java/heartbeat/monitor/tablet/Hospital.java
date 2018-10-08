package heartbeat.monitor.tablet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import heartbeat.monitor.tablet.R;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Hospital extends Activity implements LocationListener{  
   private static final String MAP_URL = "file:///android_asset/googleMap.html";
   private WebView webView;
   private TextView LatText, LogText;
   private Button submit;
   private boolean webviewReady = false;
   private Location mostRecentLocation = null;
   private float now_lat, now_log;
   private float hospital_lat = -1, hospital_log = -1;
   private Spinner spinner;
   
   private GeoPoint MyGP;
	private double HosDis[] = new double[36];
	private GeoPoint HOS[] = new GeoPoint[36];
	private String HosName[] = new String[36];
  
	private Boolean first = true;
	
   private void getLocation() {//取得裝置的GPS位置資料
      LocationManager locationManager =
        (LocationManager)getSystemService(Context.LOCATION_SERVICE);
      Criteria criteria = new Criteria();
      criteria.setAccuracy(Criteria.ACCURACY_FINE);
      String provider = locationManager.getBestProvider(criteria,true);
      //In order to make sure the device is getting the location, request updates.
      locationManager.requestLocationUpdates(provider, 1, 0, this);
      mostRecentLocation = locationManager.getLastKnownLocation(provider);
    }
  
   @Override
   /** Called when the activity is first created. */
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     //鎖定螢幕為橫向顯示
     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
     
     //隱藏標題列
     requestWindowFeature(Window.FEATURE_NO_TITLE);
     
     setContentView(R.layout.hospital); 
     LatText = (TextView) findViewById(R.id.latitude);
     LogText = (TextView) findViewById(R.id.longitude);
     spinner = (Spinner)findViewById(R.id.hospital_spinner);
     
     setHospitalLocation();
     getLocation();//取得定位位置
     setupWebView();//設定webview    
     if (mostRecentLocation!=null){
    	 now_lat = Float.parseFloat(String.valueOf(mostRecentLocation.getLatitude()));
    	 now_log = Float.parseFloat(String.valueOf(mostRecentLocation.getLongitude()));
       LatText.setText(String.valueOf(now_lat));
       LogText.setText(String.valueOf(now_log));
   
       //將畫面移至定位點的位置
       final String centerURL = "javascript:centerAt(" +
         mostRecentLocation.getLatitude() + "," +
         mostRecentLocation.getLongitude()+ ")";
       if (webviewReady) webView.loadUrl(centerURL);
     }
    
   }
 
    
   /** Sets up the WebView object and loads the URL of the page **/
   private void setupWebView(){
 
     webView = (WebView) findViewById(R.id.webview);
     webView.getSettings().setJavaScriptEnabled(true);
     //Wait for the page to load then send the location information
     webView.setWebViewClient(new WebViewClient(){ 
       @Override 
       public void onPageFinished(WebView view, String url) 
       {
         //webView.loadUrl(centerURL);
        webviewReady = true;//webview已經載入完畢
       }

     });
     webView.loadUrl(MAP_URL); 
   }

 @Override
 public void onLocationChanged(Location location) {//定位位置改變時會執行的方法
  // TODO Auto-generated method stub
     if (location !=null){
    	 now_lat = Float.parseFloat(String.valueOf(location.getLatitude()));
    	 now_log = Float.parseFloat(String.valueOf(location.getLongitude()));
       LatText.setText(String.valueOf(now_lat));
       LogText.setText(String.valueOf(now_log));  
        //將畫面移至定位點的位置，呼叫在googlemaps.html中的centerAt函式
        final String centerURL = "javascript:centerAt(" +
        		now_lat + "," +
        		now_log + ")";
        if(webviewReady)
        	webView.loadUrl(centerURL);
        final String removeURL = "javascript:remove_mark()"; 
           webView.loadUrl(removeURL);
        final String markURL = "javascript:mark(" +
        		now_lat + "," +
        		now_log + ")";
        if(webviewReady)
           webView.loadUrl(markURL);
        if(hospital_lat!=-1 && hospital_log!=-1){
        	final String markURL2 = "javascript:mark2(" +
        			hospital_lat + "," +
        			hospital_log + ")";
            if(webviewReady)
               webView.loadUrl(markURL2);
        }
        
        if(first==true){
           MyGP = new GeoPoint((int) (location.getLatitude()*1E6) ,(int) (location.getLongitude()*1E6));
         setHospitalDistance(MyGP);
           sortDis();
           
			String[] temp = new String[]{
						"醫院地點資料已載入...",
						HosName[0] + " 距離: " + String.valueOf(HosDis[0]).substring(0,4) + "km",
						HosName[1] + " 距離: " + String.valueOf(HosDis[1]).substring(0,4) + "km",
						HosName[2] + " 距離: " + String.valueOf(HosDis[2]).substring(0,4) + "km",
						HosName[3] + " 距離: " + String.valueOf(HosDis[3]).substring(0,4) + "km",
						HosName[4] + " 距離: " + String.valueOf(HosDis[4]).substring(0,4) + "km",
						HosName[5] + " 距離: " + String.valueOf(HosDis[5]).substring(0,4) + "km",
						HosName[6] + " 距離: " + String.valueOf(HosDis[6]).substring(0,4) + "km",
						HosName[7] + " 距離: " + String.valueOf(HosDis[7]).substring(0,4) + "km",
						HosName[8] + " 距離: " + String.valueOf(HosDis[8]).substring(0,4) + "km",
						HosName[9] + " 距離: " + String.valueOf(HosDis[9]).substring(0,4) + "km"
					};
        setSpinner(temp);
        first=false;
        }
      }
 }

	private void setHospitalDistance(GeoPoint mylocat){
		for(int i = 0;i < 36;i++){
			HosDis[i] = CalculationByDistance(mylocat,HOS[i]);
		}
	}
 
	public double CalculationByDistance(GeoPoint StartP, GeoPoint EndP) {
		double Radius = 6371;
	      double lat1 = StartP.getLatitudeE6()/1E6;
	      double lat2 = EndP.getLatitudeE6()/1E6;
	      double lon1 = StartP.getLongitudeE6()/1E6;
	      double lon2 = EndP.getLongitudeE6()/1E6;
	      double dLat = Math.toRadians(lat2-lat1);
	      double dLon = Math.toRadians(lon2-lon1);
	      double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
	      Math.sin(dLon/2) * Math.sin(dLon/2); 
	      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	      return Radius * c;
	   }
	
	private void setHospitalLocation(){
		HOS[0] = new GeoPoint((int) 22645611 ,(int)120309144);
		HosName[0] = "高雄醫學大學";
		HOS[1] = new GeoPoint((int) 22628262 ,(int)120362971);
		HosName[1] = "高雄市立鳳山醫院";
		HOS[2] = new GeoPoint((int) 22649453 ,(int)120356319);
		HosName[2] = "高雄長庚醫院";
		HOS[3] = new GeoPoint((int) 22615744 ,(int)120297394);
		HosName[3] = "阮綜合醫院";
		HOS[4] = new GeoPoint((int) 22680272 ,(int)120322902);
		HosName[4] = "高雄榮民總醫院";
		HOS[5] = new GeoPoint((int) 22627155 ,(int)120296937);
		HosName[5] = "高雄市立大同醫院";
		
		HOS[6] = new GeoPoint((int) 22723861 ,(int)120329259);
		HosName[6] = "健仁醫院";
		HOS[7] = new GeoPoint((int) 22504073 ,(int)120386723);
		HosName[7] = "建佑醫院";
		HOS[8] = new GeoPoint((int) 22567335 ,(int)120363098);
		HosName[8] = "高雄市立小港醫院";
		HOS[9] = new GeoPoint((int) 22766220 ,(int)120364257);
		HosName[9] = "義大醫院";
		HOS[10] = new GeoPoint((int) 22796870 ,(int)120294563);
		HosName[10] = "高雄市立岡山醫院";
		HOS[11] = new GeoPoint((int) 22702108 ,(int)120290931);
		HosName[11] = "國軍左營總醫院";
		HOS[12] = new GeoPoint((int) 22625709 ,(int)120341953);
		HosName[12] = "國軍高雄總醫院";
		HOS[13] = new GeoPoint((int) 22655007 ,(int)120291194);
		HosName[13] = "高雄市立聯合醫院";
		HOS[14] = new GeoPoint((int) 22626732 ,(int)120323579);
		HosName[14] = "高雄市立民生醫院";
		
		HOS[15] = new GeoPoint((int) 25026898 ,(int)121563632);
		HosName[15] = "臺北醫學大學附設醫院";
		HOS[16] = new GeoPoint((int) 24999954 ,(int)121558377);
		HosName[16] = "台北市立萬芳醫院";
		HOS[17] = new GeoPoint((int) 25055930 ,(int)121550443);
		HosName[17] = "長庚醫療財團法人台北長庚紀念醫院";
		HOS[18] = new GeoPoint((int) 25048072 ,(int)121547423);
		HosName[18] = "基督復臨安息日會醫療財團法人臺安醫院";
		HOS[19] = new GeoPoint((int) 25036820 ,(int)121553678);
		HosName[19] = "國泰醫療財團法人國泰綜合醫院";
		HOS[20] = new GeoPoint((int) 25058571 ,(int)121522256);
		HosName[20] = "財團法人臺灣基督長老教會馬偕紀念社會事業基金會馬偕紀念醫院";
		HOS[21] = new GeoPoint((int) 25096421 ,(int)121520910);
		HosName[21] = "新光醫療財團法人新光吳火獅紀念醫院";
		HOS[22] = new GeoPoint((int) 25119132 ,(int)121519252);
		HosName[22] = "行政院國軍退除役官兵輔導委員會台北榮民總醫院";
		HOS[23] = new GeoPoint((int) 25071533 ,(int)121590000);
		HosName[23] = "三軍總醫院附設民眾診療服務處";
		HOS[24] = new GeoPoint((int) 25040699 ,(int)121518555);
		HosName[24] = "國立台灣大學醫學院附設醫院";
		
		HOS[25] = new GeoPoint((int) 25081911 ,(int)121590757);
		HosName[25] = "中國醫藥大學附設醫院臺北分院";
		HOS[26] = new GeoPoint((int) 25117524 ,(int)121522532);
		HosName[26] = "振興醫療財團法人振興醫院";
		HOS[27] = new GeoPoint((int) 25128394 ,(int)121472407);
		HosName[27] = "醫療財團法人辜公亮基金會和信治癌中心醫院";
		HOS[28] = new GeoPoint((int) 25036466 ,(int)121549883);
		HosName[28] = "中山醫療社團法人中山醫院";
		HOS[29] = new GeoPoint((int) 25120715 ,(int)121465975);
		HosName[29] = "台北市立關渡醫院";
		HOS[30] = new GeoPoint((int) 25054360 ,(int)121557664);
		HosName[30] = "國軍松山總醫院";
		HOS[31] = new GeoPoint((int) 25037513 ,(int)121545234);
		HosName[31] = "臺北市立聯合醫院仁愛院區";
		HOS[32] = new GeoPoint((int) 25050919 ,(int)121509190);
		HosName[32] = "臺北市立聯合醫院中興院區";
		HOS[33] = new GeoPoint((int) 25046242 ,(int)121586087);
		HosName[33] = "臺北市立聯合醫院忠孝院區";
		HOS[34] = new GeoPoint((int) 25105148 ,(int)121531681);
		HosName[34] = "臺北市立聯合醫院陽明院區";
		HOS[35] = new GeoPoint((int) 25035805 ,(int)121506965);
		HosName[35] = "臺北市立聯合醫院和平婦幼院區";
	}
 
	private void sortDis(){
		GeoPoint tempGeo;
		double tempdis;
		String tempName;
		for(int i =0;i<35;i++){
			for(int j =i;j<36;j++){
				if(HosDis[j] < HosDis[i]){
					tempdis = HosDis[j];
					tempGeo = HOS[j];
					tempName = HosName[j];
					
					HosDis[j] = HosDis[i];
					HOS[j] = HOS[i];
					HosName[j] = HosName[i];
					
					HosDis[i] = tempdis;
					HOS[i] = tempGeo;
					HosName[i] = tempName;
				}
			}
		}
	}
 
 private void setSpinner(String[] mDaysList){
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
             android.R.layout.simple_spinner_item, mDaysList);
     adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     spinner.setAdapter(adapter);
     spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> arg0, View arg1,
                 int arg2, long arg3) {
             // TODO Auto-generated method stub
         	if(arg2==0){
                final String centerURL = "javascript:centerAt(" +
                		now_lat + "," +
                		now_log + ")";
                if(webviewReady)
                	webView.loadUrl(centerURL);
                final String removeURL = "javascript:remove_mark()"; 
                   webView.loadUrl(removeURL);
                final String markURL = "javascript:mark(" +
                		now_lat + "," +
                		now_log + ")";
                if(webviewReady)
                   webView.loadUrl(markURL);
                hospital_lat = -1;
                hospital_log = -1;
         	}
         	else{
                final String centerURL = "javascript:centerAt(" +
                		now_lat + "," +
                		now_log + ")";
                if(webviewReady)
                	webView.loadUrl(centerURL);
                final String removeURL = "javascript:remove_mark()"; 
                   webView.loadUrl(removeURL);
                final String markURL = "javascript:mark(" +
                		now_lat + "," +
                		now_log + ")";
                if(webviewReady)
                   webView.loadUrl(markURL);
                hospital_lat = (float) (HOS[arg2-1].getLatitudeE6()/1E6);
                hospital_log = (float) (HOS[arg2-1].getLongitudeE6()/1E6);
            	final String markURL2 = "javascript:mark2(" +
            			hospital_lat + "," +
            			hospital_log + ")";
                if(webviewReady)
                   webView.loadUrl(markURL2);
         	}
         }

         @Override
         public void onNothingSelected(AdapterView<?> arg0) {
             // TODO Auto-generated method stub

         }

     });
	}

 @Override
 public void onProviderDisabled(String provider) {
  // TODO Auto-generated method stub
  
 }

 @Override
 public void onProviderEnabled(String provider) {
  // TODO Auto-generated method stub
  
 }

 @Override
 public void onStatusChanged(String provider, int status, Bundle extras) {
  // TODO Auto-generated method stub
  
 }
}