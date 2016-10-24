package de.btcdev.eliteanimesapp.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

public class UserDataHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "user";
	private static final String TABLE_DATA = "data";
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_USERID = "userid";
	private static final String KEY_TOKEN = "token";

	private static final String[] COLUMNS = { KEY_ID, KEY_NAME, KEY_USERID,
			KEY_TOKEN };

	public UserDataHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_DATA_TABLE = "CREATE TABLE data ( "
				+ "id INTEGER PRIMARY KEY, " + "name TEXT, "
				+ "userid INTEGER, " + "token TEXT)";
		db.execSQL(CREATE_DATA_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS data");
		this.onCreate(db);
	}

	public void updateData(String name, int userId, String token) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ID, 1);
		values.put(KEY_NAME, name);
		values.put(KEY_USERID, userId);
		values.put(KEY_TOKEN, token);

		db.replace(TABLE_DATA, null, values);
		// 4. close
		db.close();
	}

	public Bundle getData() {
		Bundle bundle = new Bundle();

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_DATA, COLUMNS, " id = 1", null, null,
				null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		bundle.putString("name", cursor.getString(1));
		bundle.putInt("userid", cursor.getInt(2));
		bundle.putString("token", cursor.getString(3));
		db.close();
		return bundle;
	}
}
