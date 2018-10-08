package heartbeat.monitor.tablet;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private final static int _DBVersion = 1;
	private final static String _DBName = "SampleList.db"; 
	private final static String _TableName = "HeartBeat";
	public DBHelper(Context context) {
		super(context, _DBName, null, _DBVersion);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		final String SQL = "CREATE TABLE IF NOT EXISTS " + _TableName + "( " +
		"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
		"_name VARCHAR(32), " +
		"_email VARCHAR(64), " +
		"_phone VARCHAR(32), " +
		"_smsphone1 VARCHAR(32), " +
		"_smsphone2 VARCHAR(32), " +
		"_smsphone3 VARCHAR(32), " +
		"_address VARCHAR(128), " +
		"_note VARCHAR(256), " +
		"_wave INTEGER, " +
		"_order INTEGER" +
		");";
		
		db.execSQL(SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		final String SQL = "DROP TABLE " + _TableName;
		db.execSQL(SQL);       
	}
}
