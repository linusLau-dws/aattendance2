package hk.com.dataworld.iattendance;

/**
 * Created by Terence on 2018/1/22.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {
    // Offline Users
    static final String USR_USERNAME = "username";
    static final String USR_HASH = "hash";

    // Actual Record
    static final String BT_DateTime = "datetime";
    static final String BT_Address = "address";
    static final String BT_ZoneCode = "zonecode";
    static final String BT_StationCode = "stationcode";
    static final String BT_InOut = "inout";
    static final String BT_Status = "status";
    static final String BT_Description = "description";
    static final String BT_Name = "name";
    static final String BT_SyncTime = "sync_time";
    // Added Sep 2019
    static final String BT_EmploymentNumber = "employment_number";
    static final String BT_AuthMethod = "auth_method";

    // Receivers
    static final String BD_Name = "name";
    static final String BD_Description = "description";
    static final String BD_Address = "address";
    static final String BD_ZoneCode = "zonecode";
    static final String BD_StationCode = "stationcode";

    // Supervisor Mode
    static final String SV_EmploymentNumber = "employmentnumber";
    static final String SV_HKID = "hkid";
    static final String SV_Name = "name";
    static final String SV_ContractCode = "contractcode";
    static final String SV_StationCode = "stationcode";
    static final String SV_ZoneCode = "zonecode";
    static final String SV_DefaultIn = "defaultin";
    static final String SV_DefaultOut = "defaultout";

    private static final String TAG = SQLiteHelper.class.getSimpleName();
    private static final int VERSION = 1;

    private static final String TABLE_OFFLINE_USERS = "OfflineUsers";
    private static final String TABLE_BLUETOOTH_ATTENDANCE = "BluetoothAttendance";
    private static final String TABLE_BLUETOOTH_RECEPTORS = "BluetoothReceptors";
    private static final String TABLE_SUPERVISOR_INFO = "SupervisorInfo";
//    private static final String TABLE_NOTIFICATIONS = "Notifications";

    private static String DATABASE_NAME = "HRMSDataBase";
    SQLiteDatabase myDB;


    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        try {
            String CREATE_TABLE6 = "CREATE TABLE " + TABLE_OFFLINE_USERS + " ("
                    + USR_USERNAME + " VARCHAR PRIMARY KEY, "
                    + USR_HASH + " VARCHAR)";
            database.execSQL(CREATE_TABLE6);

            String CREATE_TABLE7 = "CREATE TABLE " + TABLE_BLUETOOTH_ATTENDANCE + " ("
                    + BT_DateTime + " VARCHAR PRIMARY KEY, "
                    + BT_Address + " VARCHAR, "
                    + BT_ZoneCode + " VARCHAR, "
                    + BT_StationCode + " VARCHAR, "
                    + BT_InOut + " INT, "
                    + BT_Status + " INT, "
                    + BT_Description + " VARCHAR, "
                    + BT_Name + " VARCHAR, "
                    + BT_SyncTime + " VARCHAR, "
                    + BT_EmploymentNumber + " VARCHAR, "
                    + BT_AuthMethod + " VARCHAR)";
            database.execSQL(CREATE_TABLE7);

            String CREATE_TABLE8 = "CREATE TABLE " + TABLE_BLUETOOTH_RECEPTORS + " ("
                    + BD_Name + " VARCHAR, "
                    + BD_Description + " VARCHAR, "
                    + BD_Address + " VARCHAR PRIMARY KEY, "
                    + BD_ZoneCode + " VARCHAR, "
                    + BD_StationCode + " VARCHAR)";
            database.execSQL(CREATE_TABLE8);

            String CREATE_TABLE9 = "CREATE TABLE " + TABLE_SUPERVISOR_INFO + " ("
                    + SV_EmploymentNumber + " VARCHAR PRIMARY KEY, "
                    + SV_HKID + " VARCHAR, "
                    + SV_Name + " VARCHAR, "
                    + SV_ContractCode + " VARCHAR, "
                    + SV_StationCode + " VARCHAR, "
                    + SV_ZoneCode + " VARCHAR, "
                    + SV_DefaultIn + " VARCHAR, "
                    + SV_DefaultOut + " VARCHAR)";
            database.execSQL(CREATE_TABLE9);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Perform DB onUpgrade");
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_OFFLINE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLUETOOTH_ATTENDANCE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLUETOOTH_RECEPTORS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPERVISOR_INFO);
            onCreate(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void openDB() {
        Log.i(TAG, "openDB");
        try {
            myDB = getWritableDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void closeDB() {
        Log.i(TAG, "closeDB");
        if (myDB != null && myDB.isOpen()) {
            try {
                myDB.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    // region 2019.01.11 Bluetooth Attendance

    String getUsrHash(String usrname) {
        Cursor cursor = myDB.rawQuery("SELECT " + USR_HASH + " FROM " + TABLE_OFFLINE_USERS + " WHERE " + USR_USERNAME + " = ?", new String[]{usrname});
        cursor.moveToFirst();
        String h = cursor.getString(0);
        cursor.close();
        return h;
    }

    String findZoneCodeByAddress(String address) {
        Cursor cursor = myDB.rawQuery("SELECT " + BD_ZoneCode + " FROM " + TABLE_BLUETOOTH_RECEPTORS + " WHERE " + BD_Address + " = ?", new String[]{address});
        cursor.moveToFirst();
        String zoneCode = cursor.getString(0);
        cursor.close();
        return zoneCode;
    }

    String findStationCodeByAddress(String address) {
        Cursor cursor = myDB.rawQuery("SELECT " + BD_StationCode + " FROM " + TABLE_BLUETOOTH_RECEPTORS + " WHERE " + BD_Address + " = ?", new String[]{address});
        cursor.moveToFirst();
        String stationCode = cursor.getString(0);
        cursor.close();
        return stationCode;
    }

    String findDescriptionByAddress(String address) {
        Cursor cursor = myDB.rawQuery("SELECT " + BD_Description + " FROM " + TABLE_BLUETOOTH_RECEPTORS + " WHERE " + BD_Address + " = ?", new String[]{address});
        cursor.moveToFirst();
        String description = cursor.getString(0);
        cursor.close();
        return description;
    }

    String findAddressByZoneAndStation(String zonecode, String stationcode) {
        Cursor cursor = myDB.rawQuery("SELECT " + BD_Address + " FROM " + TABLE_BLUETOOTH_RECEPTORS + " WHERE " + BD_ZoneCode + " = ? AND " + BD_StationCode + " = ?", new String[]{zonecode, stationcode});
        cursor.moveToFirst();
        String address = cursor.getString(0);
        cursor.close();
        return address;
    }

    String findDescriptionByZoneAndStation(String zonecode, String stationcode) {
        Cursor cursor = myDB.rawQuery("SELECT " + BD_Description + " FROM " + TABLE_BLUETOOTH_RECEPTORS + " WHERE " + BD_ZoneCode + " = ? AND " + BD_StationCode + " = ?", new String[]{zonecode, stationcode});
        cursor.moveToFirst();
        String description = cursor.getString(0);
        cursor.close();
        return description;
    }

    String findNameByZoneAndStation(String zonecode, String stationcode) {
        Cursor cursor = myDB.rawQuery("SELECT " + BD_Name + " FROM " + TABLE_BLUETOOTH_RECEPTORS + " WHERE " + BD_ZoneCode + " = ? AND " + BD_StationCode + " = ?", new String[]{zonecode, stationcode});
        cursor.moveToFirst();
        String name = cursor.getString(0);
        cursor.close();
        return name;
    }

    void insertOfflineUser(String u, String h) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO " + TABLE_OFFLINE_USERS + " (" + USR_USERNAME + ", " + USR_HASH + ") " + " values (?,?)";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        Log.i(TAG, "Begin insertLeaveList Transaction");
        try {
            statement.clearBindings();
            statement.bindString(1, u);
            statement.bindString(2, h);
            statement.execute();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i(TAG, "Finish insertLeaveList Transaction");
        }
    }

    long insertLocalAttendance(String datetime, int inout, String address, String zonecode, String stationcode, String description, String name, String employmentnumber, String authmethod) {
        ContentValues values = new ContentValues();
        values.put(BT_DateTime, datetime);
        values.put(BT_InOut, inout);
        values.put(BT_Address, address);
        values.put(BT_ZoneCode, zonecode);              // TODO
        values.put(BT_StationCode, stationcode);        // TODO
        values.put(BT_Status, 0);
        values.put(BT_Description, description);
        values.put(BT_Name, name);
        values.put(BT_EmploymentNumber, employmentnumber);
        values.put(BT_AuthMethod, authmethod);
        return myDB.insert(TABLE_BLUETOOTH_ATTENDANCE, null, values);
    }

    void clearReceptors() {
        Cursor cursor = myDB.rawQuery("DELETE FROM " + TABLE_BLUETOOTH_RECEPTORS, null);
        cursor.moveToFirst();
    }

    long insertReceptor(String name, String description, String address, String zonecode, String stationcode) {
        ContentValues values = new ContentValues();
        values.put(BD_Name, name);
        values.put(BD_Description, description);
        values.put(BD_Address, address);
        values.put(BD_ZoneCode, zonecode);
        values.put(BD_StationCode, stationcode);
        return myDB.insert(TABLE_BLUETOOTH_RECEPTORS, null, values);
    }

    ArrayList<ContentValues> getReceptors() {
        ArrayList<ContentValues> receptors = new ArrayList<>();
        Cursor cur = myDB.rawQuery("SELECT * FROM " + TABLE_BLUETOOTH_RECEPTORS, null);

        for (int x = 0; x < cur.getCount(); x++) {
            cur.moveToPosition(x);
            ContentValues tmp = new ContentValues();
            tmp.put(BD_Name, cur.getString(0));
            tmp.put(BD_Description, cur.getString(1));
            tmp.put(BD_Address, cur.getString(2));
            tmp.put(BD_ZoneCode, cur.getString(3));
            tmp.put(BD_StationCode, cur.getString(4));
            receptors.add(tmp);
        }
        cur.close();
        return receptors;
    }

    ArrayList<String> getReceptorAddresses() {
        ArrayList<String> receptors = new ArrayList<>();
        Cursor cur = myDB.rawQuery("SELECT " + BT_Address + " FROM " + TABLE_BLUETOOTH_RECEPTORS, null);

        for (int x = 0; x < cur.getCount(); x++) {
            cur.moveToPosition(x);
            receptors.add(cur.getString(0));
        }
        cur.close();
        return receptors;
    }

    ArrayList<ContentValues> getAllRecords() {
        ArrayList<ContentValues> attendanceRecordContents = new ArrayList<>();
        Cursor cur = myDB.rawQuery("SELECT * FROM " + TABLE_BLUETOOTH_ATTENDANCE + " ORDER BY " + BT_DateTime + " DESC", null);

        for (int x = 0; x < cur.getCount(); x++) {
            cur.moveToPosition(x);
            ContentValues tmp = new ContentValues();
            tmp.put(BT_DateTime, cur.getString(0));
            tmp.put(BT_Address, cur.getString(1));
            tmp.put(BT_ZoneCode, cur.getString(2));
            tmp.put(BT_StationCode, cur.getString(3));
            tmp.put(BT_InOut, cur.getInt(4));
            tmp.put(BT_Status, cur.getInt(5));
            tmp.put(BT_Description, cur.getString(6));
            tmp.put(BT_Name, cur.getString(7));
            tmp.put(BT_SyncTime, cur.getString(8));
            tmp.put(BT_EmploymentNumber, cur.getString(9));
            tmp.put(BT_AuthMethod, cur.getString(10));
            attendanceRecordContents.add(tmp);
        }
        cur.close();
        return attendanceRecordContents;
    }

    ArrayList<ContentValues> getUnsyncedRecords() {
        ArrayList<ContentValues> attendanceRecordContents = new ArrayList<>();
        Cursor cur = myDB.rawQuery("SELECT * FROM " + TABLE_BLUETOOTH_ATTENDANCE + " WHERE Status = ?", new String[]{"0"});

        for (int x = 0; x < cur.getCount(); x++) {
            cur.moveToPosition(x);
            ContentValues tmp = new ContentValues();
            tmp.put(BT_DateTime, cur.getString(0));
            tmp.put(BT_Address, cur.getString(1));
            tmp.put(BT_ZoneCode, cur.getString(2));
            tmp.put(BT_StationCode, cur.getString(3));
            tmp.put(BT_InOut, cur.getInt(4));
            tmp.put(BT_Status, cur.getInt(5));
            tmp.put(BT_Description, cur.getString(6));
            tmp.put(BT_Name, cur.getString(7));
            // No sync time: 8
            tmp.put(BT_EmploymentNumber, cur.getString(9));
            tmp.put(BT_EmploymentNumber, cur.getString(10));
            attendanceRecordContents.add(tmp);
        }
        cur.close();
        return attendanceRecordContents;
    }

    void setSynced(String datetime, String synctime) {
        Cursor cur = myDB.rawQuery("UPDATE " + TABLE_BLUETOOTH_ATTENDANCE + " SET " + BT_Status + " = ?, " + BT_SyncTime + " = ? WHERE " + BT_DateTime + " = ?", new String[]{"1", synctime, datetime});
        cur.moveToFirst();
        cur.close();
    }


    void replaceOrInsertUser() {
        // TODO: Replace or Insert User
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO " + TABLE_SUPERVISOR_INFO + " (" + SV_EmploymentNumber + ", " + SV_HKID + ", " + SV_Name + ", " + SV_ContractCode + ", " + SV_StationCode + ", " + SV_ZoneCode + ", " + SV_DefaultIn + ", " + SV_DefaultOut + ") " + " values (?,?,?,?,?,?,?,?)";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        Log.i(TAG, "Begin insertLeaveList Transaction");
        try {
            statement.clearBindings();
            statement.bindString(1, "");
            statement.bindString(2, "");
            statement.bindString(3, "");
            statement.bindString(4, "");
            statement.bindString(5, "");
            statement.bindString(6, "");
            statement.bindString(7, "");
            statement.bindString(8, "");
            statement.execute();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i(TAG, "Finish insertLeaveList Transaction");
        }
    }
    // endregion 2019.01.11 Bluetooth Attendance

}