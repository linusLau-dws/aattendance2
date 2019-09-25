package hk.com.dataworld.iattendance;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.bumptech.glide.Glide;
import com.evrencoskun.tableview.TableView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static hk.com.dataworld.iattendance.Constants.DEBUG_FALLBACK_URL;
import static hk.com.dataworld.iattendance.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.iattendance.Constants.PREF_TOKEN;
import static hk.com.dataworld.iattendance.Utility.extendBaseUrl;

public class BluetoothFindActivity extends BaseActivity {

    private String mToken;
    private String mBaseURL;
    private BluetoothAdapter mBluetoothAdapter;

    private List<String> mAddr = new ArrayList<>();
    private TableLayout mTable;

    private TableView mTableView;
    private BluetoothDeviceAdapter mTableViewAdapter;

    private SQLiteHelper dbHelper;

    private BluetoothLeScannerCompat mScanner = BluetoothLeScannerCompat.getScanner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = pref.getString(PREF_TOKEN, "");
        mBaseURL = extendBaseUrl(pref.getString(PREF_SERVER_ADDRESS, DEBUG_FALLBACK_URL));

        getSupportActionBar().setTitle(R.string.btn_attendanceCamel);
        dbHelper = new SQLiteHelper(this);

        int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},
                103);
        setContentView(R.layout.activity_bluetooth_find);

        TextView mHeader = findViewById(R.id.bluetooth_not_found_info);
        if (getIntent().hasExtra("NOT_FOUND")) {
            mHeader.setText(getString(R.string.not_found));
            mHeader.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        mTable = findViewById(R.id.devices_table);

        mTableView = findViewById(R.id.findTableView);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mTableView.setTranslationX(-130);
        mTableView.getLayoutParams().width = displayMetrics.widthPixels + 130;

        mTableViewAdapter = new BluetoothDeviceAdapter(this);
        List<CellModel> arrayList = new ArrayList<>();
        arrayList.add(new CellModel(getString(R.string.bluetooth_name)));
        arrayList.add(new CellModel(getString(R.string.bluetooth_address)));
        arrayList.add(new CellModel(getString(R.string.bluetooth_dbm)));
        mTableViewAdapter.setAllItems(arrayList, new ArrayList<CellModel>(), new ArrayList<List<CellModel>>());
        mTableView.setAdapter(mTableViewAdapter);

        bluetoothContent();

        BootstrapButton backBtn = findViewById(R.id.bluetooth_back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(BluetoothFindActivity.this, PunchActivity.class); // TODO: Formerly Selection.class
                startActivity(intent);
            }
        });
    }

    private void checkPermission() {

    }

//    private void findCheckedBoxes() {
//        dbHelper.openDB();
//        for (int x = 1; x < mTable.getChildCount(); x++) {
//            TableRow row = (TableRow) mTable.getChildAt(x);
//            CheckBox cb = (CheckBox) row.getChildAt(0);
//            if (cb.isChecked()) {
//                TextView txt = (TextView) row.getChildAt(1);
//                //dbHelper.insertReceptor(cb.getText().toString(), txt.getText().toString());
//            }
//        }
//        dbHelper.closeDB();
//    }

    private void bluetoothContent() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        AlertDialog.Builder builder;
        if (mBluetoothAdapter == null) {
            builder = new AlertDialog.Builder(this);
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
            if (!mBluetoothAdapter.isEnabled()) {
                Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBtEnabled, 1);
            } else {
                realBluetoothContent();
            }
        }
    }

    private void realBluetoothContent() {
        ImageView btAnim = findViewById(R.id.bt_anim);
        Glide.with(this).asGif().load(R.raw.bluetooth_searching).into(btAnim);

        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0)
                .setUseHardwareBatchingIfSupported(true).build();

//            List<ScanFilter> filters = new ArrayList<>();
//        filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());

        mScanner.startScan(null, settings, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull final ScanResult result) {
                super.onScanResult(callbackType, result);
//                Log.i("List", result.getDevice().getAddress() + " " + result.getDevice().getName());

                String currentAddr = result.getDevice().getAddress();
                int rssi = result.getRssi();

                if (!mAddr.contains(currentAddr)) {
                    mAddr.add(0, currentAddr);

                    List<CellModel> list = new ArrayList<>();
                    list.add(new CellModel(result.getDevice().getName() == null ? "" : result.getDevice().getName()));
                    list.add(new CellModel(result.getDevice().getAddress()));
                    list.add(new CellModel(String.valueOf(rssi)));

                    mTableViewAdapter.addRow(0,
                            new CellModel(""),
                            list);
                }
            }

            @Override
            public void onBatchScanResults(@NonNull List<ScanResult> results) {
                super.onBatchScanResults(results);
                for (final ScanResult result :
                        results) {
//                    Log.i("Lista", result.getDevice().getAddress() + " " + result.getDevice().getName());

                    String currentAddr = result.getDevice().getAddress();
                    int rssi = result.getRssi();

                    if (!mAddr.contains(currentAddr)) {
                        mAddr.add(0, currentAddr);

                        List<CellModel> list = new ArrayList<>();
                        list.add(new CellModel(result.getDevice().getName() == null ? "" : result.getDevice().getName()));
                        list.add(new CellModel(result.getDevice().getAddress()));
                        list.add(new CellModel(String.valueOf(rssi)));

                        mTableViewAdapter.addRow(0,
                                new CellModel(""),
                                list);
                    } else {

                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.i("Scan", "failed " + errorCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBtEnabled, 1);
            } else {
                realBluetoothContent();
            }
        }
    }

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

        // Disable Bluetooth
//        if (mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.disable();
//        }
    }
}
