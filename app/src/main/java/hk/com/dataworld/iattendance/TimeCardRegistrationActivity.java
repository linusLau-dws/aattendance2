package hk.com.dataworld.iattendance;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.listener.ITableViewListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class TimeCardRegistrationActivity extends BaseActivity {
    private String mToken;
    private String mBaseURL;
    private RequestQueue mRequestQueue;
    private EmploymentDropdown mDropdown;
    private String mCardId;
    private TextView mStatusTxtView;
    private LinearLayout mTableLayout;
    private BootstrapButton mRegister;
    private BootstrapButton mReassign;
    private BootstrapButton mRemoveLinkage;
    private TextView mStatus;
    private TextView mInstructions;
    private TableView mTableView;
    private List<CellModel> mHeadings;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private static String[][] mTechList = new String[][]{
            new String[]{MifareClassic.class.getName()},
            new String[]{android.nfc.tech.MifareUltralight.class.getName()},
            new String[]{android.nfc.tech.Ndef.class.getName()},
            new String[]{android.nfc.tech.NfcA.class.getName()},
            new String[]{android.nfc.tech.NfcB.class.getName()},
            new String[]{android.nfc.tech.NfcF.class.getName()},
            new String[]{android.nfc.tech.NfcV.class.getName()}
    };
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timecard_registration);

        getSupportActionBar().setTitle(R.string.supervisor_edition_plain);

        mInstructions = findViewById(R.id.instructions);
        mInstructions.setVisibility(View.VISIBLE);

        mTableLayout = findViewById(R.id.reg_form_group);
        mTableLayout.setVisibility(View.GONE);

        mDropdown = findViewById(R.id.employee_dropdown);
        mStatusTxtView = findViewById(R.id.foundDevice);

        mRegister = findViewById(R.id.register);
        mRegister.setVisibility(View.GONE);
        mReassign = findViewById(R.id.reassign);
        mReassign.setVisibility(View.GONE);
        mRemoveLinkage = findViewById(R.id.remove);

        mStatus = findViewById(R.id.status_txt);

        mTableView = findViewById(R.id.employmentTableView);

        mHeadings = new ArrayList<>();
        mHeadings.add(new CellModel(getString(R.string.header1)));
        mHeadings.add(new CellModel(getString(R.string.header2)));
        mHeadings.add(new CellModel(getString(R.string.header3)));
        mHeadings.add(new CellModel(getString(R.string.header4)));

//        BootstrapDropDown mDropdown = findViewById(R.id.employee_dropdown);
//        mDropdown.setDropdownData(new String[]{});

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mBaseURL = Utility.extendBaseUrl(prefs.getString(Constants.PREF_SERVER_ADDRESS, ""));
        mToken = prefs.getString(Constants.PREF_TOKEN, "");

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
        try {
            JSONObject obj = new JSONObject();
            obj.put("token", mToken);
            obj.put("program", 1);


            mRequestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseURL, "SupervisorEmploymentList"),
                    obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //TODO: Should be the other way round, just for testing.
                            try {
                                if (pDialog != null) {
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                }

                                DisplayMetrics displayMetrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                mTableView.setTranslationX(-160);
                                mTableView.getLayoutParams().width = displayMetrics.widthPixels + 200;

                                final BluetoothDeviceAdapter adapter = new BluetoothDeviceAdapter(TimeCardRegistrationActivity.this);

                                List<List<CellModel>> cells = new ArrayList<>();

                                JSONObject arr = response.getJSONObject("d");
                                JSONArray regd = arr.getJSONArray("regd");
                                JSONArray not_regd = arr.getJSONArray("not_regd");

//                                if (regd.length() != 0) {
                                    mDropdown.setJSONArray(not_regd, regd);

                                    for (int i = 0; i < regd.length(); i++) {
                                        JSONObject obj = regd.getJSONObject(i);
                                        List<CellModel> tmp = new ArrayList<>();
                                        Log.i("proof", obj.getString("English Name"));
                                        tmp.add(new CellModel(obj.getString("English Name")));
                                        tmp.add(new CellModel(obj.getString("Chinese Name")));
                                        tmp.add(new CellModel(obj.getString("Employment Number")));
                                        tmp.add(new CellModel(obj.getString("Serial")));
                                        cells.add(tmp);
                                    }

                                    List<CellModel> rows = new ArrayList<>();
                                    for (int z = 0; z < cells.size(); z++) {
                                        rows.add(new CellModel(String.valueOf(z + 1)));
                                    }

                                    //TODO: Test if the list cells is empty
//                                    if (cells.size() == 0) {
//                                        List<CellModel> empty = new ArrayList<>();
//                                        empty.add(new CellModel(""));
//                                        empty.add(new CellModel(""));
//                                        empty.add(new CellModel(""));
//                                        empty.add(new CellModel(""));
//                                        cells.add(empty);
//                                    }
                                    Log.i("trynottodie","1");
                                    mTableView.setAdapter(adapter);
                                Log.i("trynottodie","2");

                                adapter.setAllItems(mHeadings, rows, cells);
                                Log.i("trynottodie","3");

                                mTableView.setTableViewListener(new ITableViewListener() {
                                        @Override
                                        public void onCellClicked(@NonNull RecyclerView.ViewHolder viewHolder, final int i, final int i1) {
                                            mTableView.setSelectedRow(i1);

                                            mRemoveLinkage.setEnabled(true);
                                            mRemoveLinkage.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(TimeCardRegistrationActivity.this);
//                                                    builder.setMessage(i+" "+i1).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                                                        }
//                                                    }).create().show();
                                                    //TODO: mDropdown.movedToUnassigned();
//                                                    adapter.removeRow(i1);
                                                    //TODO: Ought to be put into RemoveLinkage callback
                                                    removeLinkage(i1);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCellLongPressed(@NonNull RecyclerView.ViewHolder viewHolder, int i, int i1) {

                                        }

                                        @Override
                                        public void onColumnHeaderClicked(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                                            mTableView.getSelectionHandler().clearSelection();
                                            mRemoveLinkage.setEnabled(false);
                                        }

                                        @Override
                                        public void onColumnHeaderLongPressed(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                                        }

                                        @Override
                                        public void onRowHeaderClicked(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
                                            mTableView.setSelectedRow(i);

                                            mRemoveLinkage.setEnabled(true);
                                            mRemoveLinkage.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(TimeCardRegistrationActivity.this);
//                                                    builder.setMessage(i+" "+i1).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                                                        }
//                                                    }).create().show();
                                                    //TODO: mDropdown.movedToUnassigned();
//                                                    adapter.removeRow(i1);
                                                    //TODO: Ought to be put into RemoveLinkage callback
                                                    removeLinkage(i);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onRowHeaderLongPressed(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                                        }
                                    });
//                                }
                                Log.i("trynottodie","4");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e) {

                            }

                        }
                    }, Utility.getGenericErrorListener(this, pDialog));
            mRequestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mIntentFilters = new IntentFilter[]{};

        androidx.appcompat.app.AlertDialog.Builder builder;
        if (mNfcAdapter == null) { //mNfcAdapter == null
            builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setMessage(R.string.nfc_function_unavailable);
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
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mTechList);
        }
    }

    private void register() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("token", mToken);
            obj.put("program", 1);
            obj.put("EmploymentNumber", mDropdown.getmSelectedEmployment());
            obj.put("Serial", mCardId);
            Log.i("asdggg", obj.toString());

            mRequestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseURL, "SupervisorRegisterCard"),
                    obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getInt("d") == 1) {
                                    //Success
                                    JSONObject details = mDropdown.movedToAssigned(mCardId);

                                    List<CellModel> cells = new ArrayList<>();
                                    cells.add(new CellModel(details.getString("English Name")));
                                    cells.add(new CellModel(details.getString("Chinese Name")));
                                    cells.add(new CellModel(details.getString("Employment Number")));
                                    cells.add(new CellModel(mCardId));

                                    mTableView.getAdapter().addRow(0,new CellModel(""),cells);
                                    mStatusTxtView.setText(getString(R.string.succ_registered, mCardId));

                                    mInstructions.setVisibility(View.VISIBLE);

                                    mTableLayout.setVisibility(View.GONE);

                                    refreshAssignReassign();
//                                    mTableView.getSelectionHandler().clearSelection();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, Utility.getGenericErrorListener(this, null));
            mRequestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void reassign() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("token", mToken);
            obj.put("EmploymentNumber", mDropdown.getmSelectedEmployment());
            obj.put("Serial", mCardId);
            obj.put("program", 1);

            mRequestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseURL, "SupervisorReassignCard"),
                    obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getInt("d") == 1) {
                                    //Success
                                    mStatusTxtView.setText(getString(R.string.succ_reassigned, mCardId));

                                    mInstructions.setVisibility(View.GONE);

                                    mTableLayout.setVisibility(View.VISIBLE);

                                    JSONObject detail = mDropdown.updateAssigned(mCardId, mDropdown.getmSelectedEmployment());
                                    for (int x=0; x < mDropdown.getRegdNumberOfRows(); x++) {
                                        Log.i("Debugcount",""+x);
                                        String serial = ((CellModel) mTableView.getAdapter().getCellItem(3, x)).getData();
                                        if (serial.equals(mCardId)) {
                                            Log.i("Detail", detail.toString());
                                            mTableView.getAdapter().changeCellItem(0, x, new CellModel(mDropdown.getmSelectedEnglishName()));
                                            mTableView.getAdapter().changeCellItem(1, x, new CellModel(mDropdown.getmSelectedChineseName()));
                                            mTableView.getAdapter().changeCellItem(2, x, new CellModel(mDropdown.getmSelectedEmployment()));
                                            break;
                                        }
                                    }

                                    refreshAssignReassign();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, Utility.getGenericErrorListener(this, null));
            mRequestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void removeLinkage(final int i1) {
        //i1: row number
        final String serial = ((CellModel) mTableView.getAdapter().getCellItem(3,i1)).getData();
        Log.i("Cthulu", serial);

        mTableView.getSelectionHandler().clearSelection();
        try {
            JSONObject obj = new JSONObject();
            obj.put("token", mToken);
            obj.put("Serial", serial);
            obj.put("program", 1);

            mRequestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseURL, "SupervisorRemoveLinkage"),
                    obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getInt("d") == 1) {
                                    mTableView.getAdapter().removeRow(i1);
                                    mStatusTxtView.setText(getString(R.string.succ_unregd, serial));
                                    mRemoveLinkage.setEnabled(false);
                                    //TODO:
                                    mDropdown.movedToUnassigned(serial);

                                    refreshAssignReassign();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, Utility.getGenericErrorListener(this, null));
            mRequestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
    }

    void refreshAssignReassign() {
        mDropdown.debug();

        // TODO: isRegd ?
        String employeeDetails = mDropdown.isSerialRegistered(mCardId);
        if (!employeeDetails.equals("")) {
//                mDropdown.switchMode(true);

            mRegister.setVisibility(View.GONE);
            mReassign.setVisibility(View.VISIBLE);

            mReassign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reassign();
                }
            });

            mStatus.setText(getString(R.string.assigned, employeeDetails));

        } else {
//                mDropdown.switchMode(false);

            mRegister.setVisibility(View.VISIBLE);
            mReassign.setVisibility(View.GONE);

            mRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    register();
                }
            });

            mStatus.setText(getString(R.string.unassigned));
        }
    }

    void resolveIntent(Intent intent) {
        String intentAction = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentAction)) {
            if (!mDropdown.isDataSet()) {
                return;
            }
            mCardId = Utility.byteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
            Log.i("cardI56d", mCardId);

            mInstructions.setVisibility(View.GONE);
            mTableLayout.setVisibility(View.VISIBLE);

            TextView serial = findViewById(R.id.serial);
            serial.setText(mCardId);
//            if (addresses.contains(cardId)) {
//
//            }
            refreshAssignReassign();
        }
    }
}
