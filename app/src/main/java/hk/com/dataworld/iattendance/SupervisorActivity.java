package hk.com.dataworld.iattendance;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapDropDown;
import com.bumptech.glide.Glide;
import com.evrencoskun.tableview.TableView;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.multi.ByQuadrantReader;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.qrcode.QRCodeReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static hk.com.dataworld.iattendance.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.iattendance.Constants.PREF_TOKEN;
import static hk.com.dataworld.iattendance.Constants.SCAN_TIMEOUT_SECONDS;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_Address;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_AuthMethod;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_DateTime;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_Description;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_EmploymentNumber;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_InOut;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_Name;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_StationCode;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_Status;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_SyncTime;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_ZoneCode;
import static hk.com.dataworld.iattendance.Utility.extendBaseUrl;
import static hk.com.dataworld.iattendance.Utility.getDayOfWeekSuffixedString;
import static hk.com.dataworld.iattendance.Utility.getShortDayOfWeek;

public class SupervisorActivity extends BaseActivity {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_CODE_GPS_PERMISSION = 102;
    private static final int REQUEST_CODE_NFC_PERMISSION = 103;
    private static final int REQUEST_CODE_ENABLE_AUTOTIME = 104;
    private static final int REQUEST_CODE_ENABLE_NFC = 105;

    private BluetoothAdapter mBluetoothAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;

    private TableLayout mTableLayout;
    private CountDownTimer mCountdownTimer;

    private String mToken;
    private String mBaseURL;
    //    private String mBdName = null, mBdAddr = null;
    private RequestQueue mRequestQueue;

    private BootstrapButton mInButton;
    private BootstrapButton mOutButton;
    private LinearLayout mLayout;

    private boolean mInitialBluetoothOpen = true;

    private SQLiteHelper dbHelper;
    private Dialog mDialog;

    private boolean mIsFound = false;
    private int mInOut = -1;

    private BluetoothLeScannerCompat mScanner = BluetoothLeScannerCompat.getScanner();


    private static String[][] mTechList = new String[][]{
            new String[]{MifareClassic.class.getName()},
            new String[]{android.nfc.tech.MifareUltralight.class.getName()},
            new String[]{android.nfc.tech.Ndef.class.getName()},
            new String[]{android.nfc.tech.NfcA.class.getName()},
            new String[]{android.nfc.tech.NfcB.class.getName()},
            new String[]{android.nfc.tech.NfcF.class.getName()},
            new String[]{android.nfc.tech.NfcV.class.getName()}
    };
    private NfcAdapter mNfcAdapter;

    private void startAttendanceSyncService() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AttendanceSyncService.class.getName().equals(service.service.getClassName())) {
                Log.i("AttendanceSync", "Service already running.");
                return;
            }
        }
        Log.i("AttendanceSync", "Starting service...");
        Intent attendanceIntent = new Intent(this, AttendanceSyncService.class);
        startService(attendanceIntent);
    }

    private void openNetworkNotConnectedDialog() {
        //TODO: Not finished
        Dialog dialog = new Dialog(this) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.dialog_network_not_connected);

                BootstrapButton btnSync = findViewById(R.id.dialog_sync_button);
                btnSync.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sync();
                        dismiss();
                    }
                });


                BootstrapButton btnClose = findViewById(R.id.dialog_close_button);
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });
            }
        };
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_new);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mBaseURL = extendBaseUrl(prefs.getString(PREF_SERVER_ADDRESS, ""));
        mToken = prefs.getString(PREF_TOKEN, "");

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mIntentFilters = new IntentFilter[]{};

        // Used to be started on SelectionActivity
        startAttendanceSyncService();

        trySyncHistory();

        mInButton = findViewById(R.id.in_button);
        mOutButton = findViewById(R.id.out_button);
        mLayout = findViewById(R.id.bluetooth_container);
        int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission != PERMISSION_GRANTED) {

            // Needs GPS for autotime

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_GPS_PERMISSION);
        } else {

            // NFC (if enabled)

            hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.NFC);
            if (hasPermission != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.NFC},
                        REQUEST_CODE_NFC_PERMISSION);
            } else {
                dbHelper = new SQLiteHelper(this);
                tryRefreshReceptors();
            }
        }
    }

    private void tryRefreshReceptors() {
        mRequestQueue = Volley.newRequestQueue(this);
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", mToken);
            obj.put("program", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                String.format("%s%s", mBaseURL, "GetBluetoothReceiverID"), obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray arr = response.getJSONArray("d");
                    dbHelper.openDB();
                    dbHelper.clearReceptors();
                    for (int x = 0; x < arr.length(); x++) {
                        JSONObject obj = arr.getJSONObject(x);
                        Log.i("test", "test");
                        dbHelper.insertReceptor(obj.getString("BD_NAME"), obj.getString("Description"), obj.getString("BD_ADDR"), obj.getString("ZoneCode"), obj.getString("StationCode"));
                    }
                    dbHelper.closeDB();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                payload();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                payload();
            }
        });
        mRequestQueue.add(req);
    }

    private void payload() {
        getSupportActionBar().setTitle(R.string.btn_attendanceCamel);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        forceAutoTime();

        BootstrapButton findButton = findViewById(R.id.find_button);
        findButton.setText(R.string.bluetooth_bd_logo);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mCountdownTimer.pause();
                //mIsEnableRestartBehaviour = false;
                finish();
                Intent intent = new Intent(SupervisorActivity.this, BluetoothFindActivity.class);
                startActivity(intent);
            }
        });

        mTableLayout = findViewById(R.id.myRecords);
        updateTable();
//        BluetoothDeviceAdapter adaptor = new BluetoothDeviceAdapter(this);
//        mTableView.setAdapter(adaptor);
    }

    private void forceAutoTime() {
        // Force user to turn on autotime
        String isAutoTimeSet = Settings.System.getString(this.getContentResolver(),
                Settings.Global.AUTO_TIME);
        // Verify if timezone is +8
        String isAutoTimeZoneSet = Settings.System.getString(this.getContentResolver(),
                Settings.Global.AUTO_TIME_ZONE);

        Log.i("autotime", isAutoTimeZoneSet);

        if (isAutoTimeSet.equals("0") || isAutoTimeZoneSet.equals("0")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean hasWriteSettingsAbility = Settings.System.canWrite(this);
                if (hasWriteSettingsAbility) {
                    Log.i("autotime", "has write ability");
                    Settings.System.putString(
                            this.getContentResolver(),
                            Settings.Global.AUTO_TIME, "1");
                } else {
                    Log.i("autotime", "has no write ability");
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
//                            .setMessage(R.string.attendance_function_requires_autotime)
//                            .setCancelable(true)
//                            .setPositiveButton(R.string.attendance_go_to_settings, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 104);
//                                }
//                            });
//                    builder.create().show();

                    mDialog = new Dialog(this) {
                        @Override
                        protected void onCreate(Bundle savedInstanceState) {
                            super.onCreate(savedInstanceState);
                            setContentView(R.layout.autotime_dialog);

                            ImageView iv = findViewById(R.id.instructionAnim);
                            Glide.with(SupervisorActivity.this).asGif().load(R.raw.force_autotime).into(iv);
                            TextView txt = findViewById(R.id.instructionTxt);
                            txt.setText(R.string.attendance_function_requires_autotime);
                            Button goToSettings = findViewById(R.id.go_to_settings_button);
                            goToSettings.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivityForResult(new Intent(Settings.ACTION_DATE_SETTINGS), 104);
                                }
                            });
                            setCancelable(false);
                        }
                    };
                    mDialog.show();
                }
            }
        } else {
            bluetoothContent();
        }
    }

    private void updateTable() {
        TableView tableView = findViewById(R.id.recordsTableView);
        List<CellModel> headings = new ArrayList<>();
        headings.add(new CellModel(""));
        headings.add(new CellModel(getString(R.string.bluetooth_record_date_time)));
        headings.add(new CellModel(getString(R.string.bluetooth_record_in_out)));
        headings.add(new CellModel(getString(R.string.bluetooth_description)));
        headings.add(new CellModel(getString(R.string.bluetooth_name)));
        headings.add(new CellModel(getString(R.string.bluetooth_record_status)));
        headings.add(new CellModel(getString(R.string.bluetooth_record_sync_time)));
        headings.add(new CellModel(getString(R.string.employment_number)));
        headings.add(new CellModel(getString(R.string.method)));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        tableView.setTranslationX(-160);
        tableView.getLayoutParams().width = displayMetrics.widthPixels + 200;

        BluetoothDeviceAdapter adapter = new BluetoothDeviceAdapter(this);

        List<List<CellModel>> cells = new ArrayList<>();

        dbHelper.openDB();
        mTableLayout.removeViews(1, mTableLayout.getChildCount() - 1);
        ArrayList<ContentValues> arr = dbHelper.getAllRecords();

        for (ContentValues c :
                arr) {
            List<CellModel> tmp = new ArrayList<>();

            TableRow row = new TableRow(SupervisorActivity.this);
            TextView t1 = new TextView(SupervisorActivity.this);
            String dateTime = c.getAsString(BT_DateTime);

            tmp.add(new CellModel(getShortDayOfWeek(this, dateTime.substring(0, 10))));
            tmp.add(new CellModel(dateTime.substring(5)));


            t1.setText(String.format("%s %s", getDayOfWeekSuffixedString(this, dateTime.substring(0, 10)), dateTime.substring(11)));
            TextView t2 = new TextView(SupervisorActivity.this);


            tmp.add(new CellModel(c.getAsInteger(BT_InOut) == 0 ? getString(R.string.bluetooth_in) : getString(R.string.bluetooth_out)));

            t2.setText(c.getAsInteger(BT_InOut) == 0 ? getString(R.string.bluetooth_in) : getString(R.string.bluetooth_out));
            t2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            TextView t3 = new TextView(SupervisorActivity.this);


            tmp.add(new CellModel(c.getAsString(BT_Description)));
            tmp.add(new CellModel(c.getAsString(BT_Name)));
            tmp.add(new CellModel(c.getAsInteger(BT_Status) == 0 ? getString(R.string.status_pending) : getString(R.string.bluetooth_success)));


            t3.setText(c.getAsInteger(BT_Status) == 0 ? getString(R.string.status_pending) : getString(R.string.bluetooth_success));
            t3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            TextView t4 = new TextView(SupervisorActivity.this);


            tmp.add(new CellModel(c.getAsString(BT_SyncTime) == null ? "" : c.getAsString(BT_SyncTime)));


            t4.setText(c.getAsString(BT_SyncTime));


            tmp.add(new CellModel(c.getAsString(BT_EmploymentNumber) == null ? "" : c.getAsString(BT_EmploymentNumber)));
            tmp.add(new CellModel(c.getAsString(BT_AuthMethod)));

            row.addView(t1);
            row.addView(t2);
            row.addView(t3);
            row.addView(t4);

            cells.add(tmp);

            mTableLayout.addView(row);
        }
        dbHelper.closeDB();


        List<CellModel> rows = new ArrayList<>();
        for (int z = 0; z < arr.size(); z++) {
            rows.add(new CellModel(String.valueOf(z + 1)));
        }

        if (arr.size() == 0) {
            List<CellModel> empty = new ArrayList<>();
            for (int cols = 0; cols < 9; cols++) {
                empty.add(new CellModel(""));
            }
            cells.add(empty);
        }
        tableView.setAdapter(adapter);
        adapter.setAllItems(headings, rows, cells);
    }

    private void bluetoothContent() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        androidx.appcompat.app.AlertDialog.Builder builder;
        if (mBluetoothAdapter == null) { //mBluetoothAdapter == null
            builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setMessage(R.string.bluetooth_function_unavailable);
            builder.setCancelable(false);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
//            if (!mBluetoothAdapter.isEnabled()) {
//                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivity(i);
//            }
            if (!mBluetoothAdapter.isEnabled()) {
//                mBluetoothAdapter.enable();
                mInitialBluetoothOpen = false;
                Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBtEnabled, 1);
            } else {

                mInButton.setEnabled(true);
                mOutButton.setEnabled(true);
//                    mLayout.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                mInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mInOut = 0;
                        realBluetoothContent();
                    }
                });
                mOutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mInOut = 1;
                        realBluetoothContent();
                    }
                });
            }
        }
    }


    private void realBluetoothContent() {
        mIsFound = false;

        dbHelper.openDB();
        final ArrayList<String> addresses = dbHelper.getReceptorAddresses();
        dbHelper.closeDB();

        mDialog = new Dialog(this) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.supervisor_dialog);

                BootstrapButton backButton1 = findViewById(R.id.back1);
                BootstrapButton backButton2 = findViewById(R.id.back2);
                BootstrapButton backButton3 = findViewById(R.id.back3);
                BootstrapButton nextButton1 = findViewById(R.id.next1);
                BootstrapButton doneButton2 = findViewById(R.id.done2);
                BootstrapButton newStaffButton2 = findViewById(R.id.newstaff2);
                BootstrapButton doneButton3 = findViewById(R.id.done3);
                final RelativeLayout relLayout1 = findViewById(R.id.page1);
                final RelativeLayout relLayout2 = findViewById(R.id.page2);
                final RelativeLayout relLayout3 = findViewById(R.id.page3);

                final BootstrapDropDown contractCodes = findViewById(R.id.ddl_contract_codes);
                final BootstrapDropDown zoneCodes = findViewById(R.id.ddl_zone_codes);

                BootstrapButton qrScanBtn = findViewById(R.id.qrcode_button);
                BootstrapButton barcodeScanBtn = findViewById(R.id.barcode_button);

                TableView tableView = findViewById(R.id.employmentTableView);

                List<CellModel> headings = new ArrayList<>();
                headings.add(new CellModel("Name"));
                headings.add(new CellModel("Staff number"));
                headings.add(new CellModel("Employment number"));

                //TODO
                dbHelper.openDB();
                ArrayList<String> contracts = dbHelper.getSupervisorMasterTableContract();
                dbHelper.closeDB();

                contractCodes.setDropdownData(contracts.toArray(new String[0]) == null ? new String[] {""} : contracts.toArray(new String[0]));

                contractCodes.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                    @Override
                    public void onItemClick(ViewGroup parent, View v, int id) {
                        contractCodes.setText(contractCodes.getDropdownData()[id]);
                        dbHelper.openDB();
                        ArrayList<String> zones = dbHelper.getSupervisorMasterTableZone(contractCodes.getText().toString());
                        zoneCodes.setDropdownData(zones.toArray(new String[0]) == null ? new String[] {""} : zones.toArray(new String[0]));
                        dbHelper.closeDB();
                    }
                });

                contractCodes.callOnClick();

                zoneCodes.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                    @Override
                    public void onItemClick(ViewGroup parent, View v, int id) {
                        zoneCodes.setText(zoneCodes.getDropdownData()[id]);
                    }
                });

                zoneCodes.callOnClick();

                backButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();

//                        mCountdownTimer.pause();
//                        mScanner.stopScan(new ScanCallback() {
//                            @Override
//                            public void onScanResult(int callbackType, @NonNull ScanResult result) {
//                                super.onScanResult(callbackType, result);
//                            }
//
//                            @Override
//                            public void onBatchScanResults(@NonNull List<ScanResult> results) {
//                                super.onBatchScanResults(results);
//                            }
//
//                            @Override
//                            public void onScanFailed(int errorCode) {
//                                super.onScanFailed(errorCode);
//                            }
//                        });
//                        finish();
//                        Intent retMenu = new Intent(SupervisorActivity.this, SupervisorActivity.class);  // TODO: Formerly Selection.class
//                        startActivity(retMenu);
                    }
                });
                backButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        relLayout2.setVisibility(View.GONE);
                        relLayout1.setVisibility(View.VISIBLE);
                    }
                });
                backButton3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        relLayout3.setVisibility(View.GONE);
                        relLayout2.setVisibility(View.VISIBLE);
//                        mCountdownTimer.pause();
//                        mScanner.stopScan(new ScanCallback() {
//                            @Override
//                            public void onScanResult(int callbackType, @NonNull ScanResult result) {
//                                super.onScanResult(callbackType, result);
//                            }
//
//                            @Override
//                            public void onBatchScanResults(@NonNull List<ScanResult> results) {
//                                super.onBatchScanResults(results);
//                            }
//
//                            @Override
//                            public void onScanFailed(int errorCode) {
//                                super.onScanFailed(errorCode);
//                            }
//                        });
//                        finish();
//                        Intent retMenu = new Intent(SupervisorActivity.this, SupervisorActivity.class);  // TODO: Formerly Selection.class
//                        startActivity(retMenu);
                    }
                });

                nextButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        relLayout1.setVisibility(View.GONE);
                        relLayout2.setVisibility(View.VISIBLE);
                    }
                });

                newStaffButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        relLayout2.setVisibility(View.GONE);
                        relLayout3.setVisibility(View.VISIBLE);
                    }
                });

                doneButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("token", mToken);
                            obj.put("program", 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.i("Why500", obj.toString());

                        mRequestQueue = Volley.newRequestQueue(SupervisorActivity.this);
                        JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                                String.format("%s%s", mBaseURL, "BluetoothSyncHistory"),
                                obj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        dismiss();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Ignore
                            }
                        });
                        mRequestQueue.add(req);
                    }
                });

                doneButton3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("token", mToken);
                            obj.put("program", 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.i("Why500", obj.toString());

                        mRequestQueue = Volley.newRequestQueue(SupervisorActivity.this);
                        JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                                String.format("%s%s", mBaseURL, "BluetoothAddNewStaffRecord"),
                                obj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        dismiss();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Ignore
                            }
                        });
                        mRequestQueue.add(req);
                    }
                });

                qrScanBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent qrIntent = new Intent(SupervisorActivity.this, ZxingViewActivity.class);
                        startActivity(qrIntent);
                    }
                });
//                BootstrapLabel enabledMethodsLbl = findViewById(R.id.bluetooth_enabled);
//                enabledMethodsLbl.setBootstrapText(new BootstrapText.Builder(SupervisorActivity.this)
//                        .addMaterialIcon(MaterialIcons.MD_BLUETOOTH).addText(getString(R.string.bluetooth)).build());
//
//                BootstrapLabel enabledMethodsLbl2 = findViewById(R.id.nfc_enabled);
//                enabledMethodsLbl2.setBootstrapText(new BootstrapText.Builder(SupervisorActivity.this)
//                        .addMaterialIcon(MaterialIcons.MD_NFC).addText(getString(R.string.nfc)).build());
//
//                BootstrapButton qrcode_btn = findViewById(R.id.qrcode_button);
//                qrcode_btn.setBootstrapText(new BootstrapText.Builder(SupervisorActivity.this)
//                .addFontAwesomeIcon(FontAwesome.FA_QRCODE).addText(getString(R.string.qrcode)).build());
//
//                qrcode_btn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent zxing = new Intent(SupervisorActivity.this, ZxingViewActivity.class);
//                        startActivity(zxing);
//                    }
//                });
//
//                BootstrapButton barcode_btn = findViewById(R.id.barcode_button);
//                barcode_btn.setBootstrapText(new BootstrapText.Builder(SupervisorActivity.this)
//                        .addFontAwesomeIcon(FontAwesome.FA_BARCODE).addText(getString(R.string.barcode)).build());
//                barcode_btn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent zxing = new Intent(SupervisorActivity.this, ZxingViewActivity.class);
//                        startActivity(zxing);
//                    }
//                });
//
//                ImageView anim = findViewById(R.id.searching_anim);
//                Glide.with(SupervisorActivity.this).asGif().load(R.raw.bluetooth_searching).into(anim);
//
//                BluetoothDeviceAdapter adapter = new BluetoothDeviceAdapter(SupervisorActivity.this);
//
//                List<CellModel> headings = new ArrayList<>();
//                headings.add(new CellModel(getString(R.string.bluetooth_name)));
//                headings.add(new CellModel(getString(R.string.bluetooth_description)));
//
//
//                List<CellModel> rowHeadings = new ArrayList<>();
//                rowHeadings.add(new CellModel("1"));
//                rowHeadings.add(new CellModel("2"));
//
//
//                List<List<CellModel>> cells = new ArrayList<>();
//                dbHelper.openDB();
//                ArrayList<ContentValues> contentValues = dbHelper.getReceptors();
//                Log.i("Receptors", contentValues.toString());
//                for (ContentValues c :
//                        contentValues) {
//                    List<CellModel> tmp = new ArrayList<>();
//                    tmp.add(new CellModel(c.getAsString(BD_Name)));
//                    tmp.add(new CellModel(c.getAsString(BD_Description)));
////            tmp.add(new CellModel(c.getAsString(BD_Address)));
//                    cells.add(tmp);
//                }
//                dbHelper.closeDB();
//
//                adapter.setAllItems(headings, rowHeadings, cells);
//
//                final TableView tableView = findViewById(R.id.testTableView);
//                tableView.setAdapter(adapter);
//                DisplayMetrics displayMetrics = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//                tableView.setTranslationX(-100);
//                tableView.getLayoutParams().width = displayMetrics.widthPixels + 140;
//
//                final BootstrapButton showHideButton = findViewById(R.id.showHideTable);
//                showHideButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (tableView.getVisibility() == View.GONE) {
//                            tableView.setVisibility(View.VISIBLE);
//                            showHideButton.setText(getString(R.string.bluetooth_hide_searching_device));
//                        } else {
//                            tableView.setVisibility(View.GONE);
//                            showHideButton.setText(getString(R.string.bluetooth_show_searching_device));
//                        }
//                    }
//                });
            }
        };
        mDialog.setCancelable(false);
        mDialog.show();

        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0)
                .setUseHardwareBatchingIfSupported(true).build();
//               List<ScanFilter> filters = new ArrayList<>();
//               filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());

        mScanner.startScan(null, settings, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull final ScanResult result) {
                super.onScanResult(callbackType, result);
//                Log.i("Resultat", "Resultat");
//                Log.i("Resultat", result.getDevice().getAddress() + " " + result.getDevice().getName());

                if (!mIsFound && addresses.contains(result.getDevice().getAddress())) {
                    mScanner.stopScan(new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, @NonNull ScanResult result) {
                            super.onScanResult(callbackType, result);
                        }

                        @Override
                        public void onBatchScanResults(@NonNull List<ScanResult> results) {
                            super.onBatchScanResults(results);
                        }

                        @Override
                        public void onScanFailed(int errorCode) {
                            super.onScanFailed(errorCode);
                        }
                    });

//                    // Disable Bluetooth
//                    if (!mInitialBluetoothOpen) {
//                        if (mBluetoothAdapter.isEnabled()) {
//                            mBluetoothAdapter.disable();
//                        }
//                    }

                    // Close dialog
                    if (!isFinishing() && mDialog != null) {
                        mDialog.dismiss();
                    }

                    TextView textView = findViewById(R.id.foundDevice);
                    textView.setText(result.getDevice().getName());

                    mIsFound = true;

                    // Log into SQLite
                    dbHelper.openDB();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                    String zonecode = dbHelper.findZoneCodeByAddress(result.getDevice().getAddress());
                    String stationcode = dbHelper.findStationCodeByAddress(result.getDevice().getAddress());
                    String description = dbHelper.findDescriptionByAddress(result.getDevice().getAddress());
                    dbHelper.insertLocalAttendance(simpleDateFormat.format(Calendar.getInstance().getTime()), mInOut, result.getDevice().getAddress(), zonecode, stationcode, description, result.getDevice().getName(), "", "Bluetooth");
                    dbHelper.closeDB();

                    //Try sync
                    sync();

                    updateTable();

                    snackbar(R.string.bluetooth_success);

                    mInButton.setEnabled(false);
                    mOutButton.setEnabled(false);


//                    CountDownTimer countDownTimer = new CountDownTimer(1000, 10000) {
//                        @Override
//                        public void onTick(long millisUntilFinished) {
//
//                        }
//
//                        @Override
//                        public void onFinish() {
//                            mIsFound = false;
//                        }
//                    };
//                    countDownTimer.start();
                }
            }

            @Override
            public void onBatchScanResults(@NonNull List<ScanResult> results) {
                super.onBatchScanResults(results);
                for (final ScanResult result :
                        results) {
//                    Log.i("Resultata", "Resultata");
//                    Log.i("Resultata", result.getDevice().getAddress() + " " + result.getDevice().getName());

                    if (!mIsFound && addresses.contains(result.getDevice().getAddress())) {
//                        // Disable Bluetooth
//                        if (mBluetoothAdapter.isEnabled()) {
//                            mBluetoothAdapter.disable();
//                        }

                        // Close dialog
                        if (!isFinishing() && mDialog != null) {
                            mDialog.dismiss();
                        }

                        TextView textView = findViewById(R.id.foundDevice);
                        textView.setText(result.getDevice().getName());
                        Log.e("matched", result.getDevice().getName());

                        mIsFound = true;

                        // Log into SQLite
                        dbHelper.openDB();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                        String zonecode = dbHelper.findZoneCodeByAddress(result.getDevice().getAddress());
                        String stationcode = dbHelper.findStationCodeByAddress(result.getDevice().getAddress());
                        String description = dbHelper.findDescriptionByAddress(result.getDevice().getAddress());
                        dbHelper.insertLocalAttendance(simpleDateFormat.format(Calendar.getInstance().getTime()), mInOut, result.getDevice().getAddress(), zonecode, stationcode, description, result.getDevice().getName(), "", "Bluetooth");
                        dbHelper.closeDB();

                        //Try sync
                        sync();

                        updateTable();

                        snackbar(R.string.bluetooth_success);

                        mInButton.setEnabled(false);
                        mOutButton.setEnabled(false);

//                        mInButton.setEnabled(true);
//                        mOutButton.setEnabled(true);
//                        mLayout.setBackgroundColor(getResources().getColor(R.color.colorGreen));
//                        mInButton.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                //bluetoothContent();
//
//                                // Log into SQLite
//                                dbHelper.openDB();
//                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//                                String description = dbHelper.findDescriptionByAddress(result.getDevice().getAddress());
//                                dbHelper.insertLocalAttendance(simpleDateFormat.format(Calendar.getInstance().getTime()), 0, result.getDevice().getAddress(), description);
//                                dbHelper.closeDB();
//
//                                //Try sync
//                                sync();
//
//                                updateTable();
//                            }
//                        });
//                        mOutButton.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                //bluetoothContent();
//
//                                // Log into SQLite
//                                dbHelper.openDB();
//                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//                                String description = dbHelper.findDescriptionByAddress(result.getDevice().getAddress());
//                                dbHelper.insertLocalAttendance(simpleDateFormat.format(Calendar.getInstance().getTime()), 1, result.getDevice().getAddress(), description);
//                                dbHelper.closeDB();
//
//                                //Try sync
//                                sync();
//
//                                updateTable();
//                            }
//                        });
//                        CountDownTimer countDownTimer = new CountDownTimer(1000, 10000) {
//                            @Override
//                            public void onTick(long millisUntilFinished) {
//
//                            }
//
//                            @Override
//                            public void onFinish() {
//                                mIsFound = false;
//                            }
//                        };
//                        countDownTimer.start();
                        return;
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.i("Scan", "failed " + errorCode);
            }
        });

        mCountdownTimer = new CountDownTimer(1000 * 30, 1000 * SCAN_TIMEOUT_SECONDS) {
            @Override
            public void onTick(long l) {
                //mBluetoothAdapter.startDiscovery();
                Log.i("tick", String.valueOf(l));
            }

            @Override
            public void onFinish() {
                if (!mIsFound) {
                    finish();
                    Intent intent = new Intent(SupervisorActivity.this, BluetoothFindActivity.class);
                    intent.putExtra("NOT_FOUND", true);
                    startActivity(intent);
                }
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mCountdownTimer.start();
            }
        });
        thread.start();
    }

    private void snackbar(int res) {
        Snackbar.make(findViewById(R.id.scrollView), res, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void readQrcode() {
        QRCodeReader qrCodeReader = new QRCodeReader();
        try {
            qrCodeReader.decode(null);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
    }

    private void readBarcode() {
        MultiFormatReader multiReader = new MultiFormatReader();
        GenericMultipleBarcodeReader byquadReader = new GenericMultipleBarcodeReader(new ByQuadrantReader(multiReader));
//        Dictionary<DecodeHintType, object> hints = new Dictionary<DecodeHintType, object>();
//        hints.Add(DecodeHintType.TRY_HARDER, true);
//        List<BarcodeFormat> formats = new List<BarcodeFormat>();
//        formats.Add(BarcodeFormat.All_1D);
//        formats.Add(BarcodeFormat.QR_CODE);
//        hints.Add(DecodeHintType.POSSIBLE_FORMATS, formats);
//        byquadresults = byquadReader.decodeMultiple(binaryBitmap, hints);
//        GenericMultipleBarcodeReader qrCodeReader = new GenericMultipleBarcodeReader();


//        try {
//            qrCodeReader.decode(null);
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//        } catch (ChecksumException e) {
//            e.printStackTrace();
//        } catch (FormatException e) {
//            e.printStackTrace();
//        }
    }

    private void trySyncHistory() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("token", mToken);
            obj.put("program", 1);

            Log.i("Why500", obj.toString());

            mRequestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseURL, "BluetoothSyncHistory"),
                    obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.i("trySyncHistory", response.toString());
                                JSONArray arr = response.getJSONArray("d");

                                dbHelper.openDB();
                                for (int y = 0; y < arr.length(); y++) {
                                    Log.i("trySyncHistory", String.valueOf(y));
                                    // Add records if not exists
                                    JSONObject obj = arr.getJSONObject(y);
                                    dbHelper.syncHistory(obj.getString("Time")
                                            , obj.getBoolean("InOut") ? 1 : 0
                                            , ""
//                                            , dbHelper.findAddressByZoneAndStation(obj.getString("ZoneCode"), obj.getString("StationCode"))
                                            , obj.getString("ZoneCode")
                                            , obj.getString("StationCode")
                                            , ""
                                            , ""
                                            , ""
                                            , "Bluetooth"
                                            , obj.getString("CreateDate")
//                                            , dbHelper.findDescriptionByZoneAndStation(obj.getString("ZoneCode"), obj.getString("StationCode"))
//                                            , dbHelper.findNameByZoneAndStation(obj.getString("ZoneCode"), obj.getString("StationCode"))
                                    );
                                }
                                dbHelper.closeDB();

//                                snackbar(R.string.nfc_success);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            updateTable();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Ignore
                }
            });
            mRequestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sync() {
        try {
            final ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(getString(R.string.loading));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            mRequestQueue = Volley.newRequestQueue(this);
            JSONObject obj = new JSONObject();
            final SQLiteHelper dbHelper = new SQLiteHelper(this);
            dbHelper.openDB();
            ArrayList<ContentValues> unsynced = dbHelper.getUnsyncedRecords();
            //get all "SELECT * FROM " +  + " WHERE Status = 0"
            dbHelper.closeDB();

            obj.put("token", mToken);
            JSONArray array = new JSONArray();
            for (ContentValues c :
                    unsynced) {
                JSONObject innerObj = new JSONObject();
                innerObj.put("useragent", System.getProperty("http.agent"));
                innerObj.put("datetime", c.get(BT_DateTime));
                innerObj.put("address", c.get(BT_Address));
                innerObj.put("zonecode", c.get(BT_ZoneCode));
                innerObj.put("stationcode", c.get(BT_StationCode));
                innerObj.put("inout", c.get(BT_InOut));
                innerObj.put("authmethod", c.get(BT_AuthMethod));

                array.put(innerObj);
            }
            obj.put("Data", array.toString());
            Log.i("reqContent", obj.toString());

            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseURL, "BluetoothSyncAttendance"),
                    obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            pDialog.dismiss();
                            // Return successful dateTime
                            try {
                                JSONObject obj = response.getJSONObject("d");
                                JSONArray datetime = obj.getJSONArray("timestamps");
                                String synctime = obj.getString("synctime");
                                dbHelper.openDB();
                                for (int y = 0; y < datetime.length(); y++) {
                                    Log.i("setsync", "test");
                                    dbHelper.setSynced(datetime.getString(y), synctime);
                                }
                                dbHelper.closeDB();
                                snackbar(R.string.bluetooth_success);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            updateTable();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Maybe not this network
                    Log.i("Sync", "Not this network");
                    pDialog.dismiss();

                    openNetworkNotConnectedDialog();
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ENABLE_AUTOTIME) {
            forceAutoTime();
        } else if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (!mBluetoothAdapter.isEnabled()) {
//                mBluetoothAdapter.enable();
                Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBtEnabled, 1);
            } else {
                bluetoothContent();
            }
        } else if (requestCode == REQUEST_CODE_ENABLE_NFC) {
            if (!mNfcAdapter.isEnabled()) {
                //Dialog to NFC settings
                AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setMessage(R.string.nfc_not_enabled);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.nfc_go_to_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), REQUEST_CODE_ENABLE_NFC);
                    }
                });
                builder.create().show();
            } else {
                bluetoothContent();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_GPS_PERMISSION) {
            int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasPermission != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_GPS_PERMISSION);
            } else {
                hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.NFC);
                if (hasPermission != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    android.Manifest.permission.NFC},
                            REQUEST_CODE_NFC_PERMISSION);
                } else {
                    dbHelper = new SQLiteHelper(this);
                    tryRefreshReceptors();
                    bluetoothContent();
                }
            }
        } else if (requestCode == REQUEST_CODE_NFC_PERMISSION) {
            int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.NFC);
            if (hasPermission != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.NFC},
                        REQUEST_CODE_NFC_PERMISSION);
            } else {
                dbHelper = new SQLiteHelper(this);
                tryRefreshReceptors();
                bluetoothContent();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
//        }
        return super.onOptionsItemSelected(item);
    }
//    private syncSelected() {
//        JsonObjectRequest = new JsonObjectRequest();
//        mQueue.add();
//    }


    @Override
    protected void onStop() {
        super.onStop();
        mScanner.stopScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                super.onScanResult(callbackType, result);
            }

            @Override
            public void onBatchScanResults(@NonNull List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        });
        if (mCountdownTimer != null) {
            mCountdownTimer.pause();
        }
    }
}
