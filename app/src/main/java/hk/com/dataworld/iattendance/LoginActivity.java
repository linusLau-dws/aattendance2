package hk.com.dataworld.iattendance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import static hk.com.dataworld.iattendance.Constants.DEBUG_FALLBACK_URL;
import static hk.com.dataworld.iattendance.Constants.MAGIC_WORD;
import static hk.com.dataworld.iattendance.Constants.PREF_FIRST_RUN;
import static hk.com.dataworld.iattendance.Constants.PREF_HASH;
import static hk.com.dataworld.iattendance.Constants.PREF_LOCALE;
import static hk.com.dataworld.iattendance.Constants.PREF_REFRESH_TOKEN;
import static hk.com.dataworld.iattendance.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.iattendance.Constants.PREF_TOKEN;
import static hk.com.dataworld.iattendance.Constants.PREF_UNAME;
import static hk.com.dataworld.iattendance.Utility.extendBaseUrl;
import static hk.com.dataworld.iattendance.Utility.getGenericErrorListener;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.MD5;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_512;

public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    protected static String userName, userPassword;
    //    private static boolean connectToServer;
//    private static Pattern mLowercasePattern = Pattern.compile("[a-z]"),
//            mUppercasePattern = Pattern.compile("[A-Z]"),
//            mDigitPattern = Pattern.compile("[0-9]");
    //region New login methods
    private static SecureRandom rnd = new SecureRandom();
    protected String baseUrl, deviceId;
    private RequestQueue mRequestQueue;
    private EditText mUsernameEdit, mPasswordEdit;
    private Button mLoginButton;
    private TextView mVersionNameEdit;
    private CheckBox mRememberMe;
    //Set server timeout in seconds
//    private int serverTimeOut = 3;
    private SQLiteDatabase SQLITEDATABASE;
    private SQLiteHelper dbHelper;
    private boolean mUseHashAsPwd = false, mIsFirstRun = true;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mPrefsEditor;
    private ProgressDialog pd;
    private Response.ErrorListener networkIssueListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            pd.dismiss();
//            openNetworkNotConnectedDialog();

//            changeUrlDialog();
        }
    };

//    private void openNetworkNotConnectedDialog() {
//        Dialog dialog = new Dialog(this) {
//            @Override
//            protected void onCreate(Bundle savedInstanceState) {
//                super.onCreate(savedInstanceState);
//                setContentView(R.layout.dialog_network_not_connected);
//
//                BootstrapButton btnSync = findViewById(R.id.dialog_sync_button);
//                btnSync.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        sync(dial);
//                        dismiss();
//                    }
//                });
//
//
//                BootstrapButton btnClose = findViewById(R.id.dialog_close_button);
//                btnClose.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dismiss();
//                    }
//                });
//            }
//
//            private void sync() {
//                try {
//                    final ProgressDialog pDialog = new ProgressDialog(LoginActivity.this);
//                    pDialog.setMessage(getString(R.string.loading));
//                    pDialog.setIndeterminate(false);
//                    pDialog.setCancelable(true);
//                    pDialog.show();
//                    RequestQueue lQueue = Volley.newRequestQueue(LoginActivity.this);
//                    JSONObject obj = new JSONObject();
//                    final SQLiteHelper dbHelper = new SQLiteHelper(LoginActivity.this);
//                    dbHelper.openDB();
//                    ArrayList<ContentValues> unsynced = dbHelper.getUnsyncedRecords();
//                    //get all "SELECT * FROM " +  + " WHERE Status = 0"
//                    dbHelper.closeDB();
//
//                    obj.put("token", mToken);
//                    JSONArray array = new JSONArray();
//                    for (ContentValues c :
//                            unsynced) {
//                        JSONObject innerObj = new JSONObject();
//                        innerObj.put("useragent", System.getProperty("http.agent"));
//                        innerObj.put("datetime", c.get(BT_DateTime));
//                        innerObj.put("address", c.get(BT_Address));
//                        innerObj.put("zonecode", c.get(BT_ZoneCode));
//                        innerObj.put("stationcode", c.get(BT_StationCode));
//                        innerObj.put("inout", c.get(BT_InOut));
//                        array.put(innerObj);
//                    }
//                    obj.put("Data", array.toString());
//                    Log.i("reqContent", obj.toString());
//
//                    JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
//                            String.format("%s%s", mBaseURL, "BluetoothSyncAttendance"),
//                            obj,
//                            new Response.Listener<JSONObject>() {
//                                @Override
//                                public void onResponse(JSONObject response) {
//                                    pDialog.dismiss();
//                                    // Return successful dateTime
//                                    try {
//                                        JSONObject obj = response.getJSONObject("d");
//                                        JSONArray datetime = obj.getJSONArray("timestamps");
//                                        String synctime = obj.getString("synctime");
//                                        dbHelper.openDB();
//                                        for (int y = 0; y < datetime.length(); y++) {
//                                            Log.i("setsync", "test");
//                                            dbHelper.setSynced(datetime.getString(y), synctime);
//                                        }
//                                        dbHelper.closeDB();
////                                        snackbar(R.string.bluetooth_success);
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            // Maybe not this network
//                            Log.i("Sync", "Not this network");
//                            pDialog.dismiss();
//                        }
//                    });
//                    lQueue.add(req);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        };
//        dialog.show();
//    }


    private int mPolicy_low, mPolicy_num, mPolicy_sym, mPolicy_upper, mPolicy_len;

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        menu.removeItem(R.id.action_logout);
        MenuItem item = menu.findItem(R.id.action_quit);
        item.setVisible(true);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                finish();
                return false;
            }
        });
        return true;
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefsEditor = mSharedPreferences.edit();
        mPrefsEditor.putString(PREF_TOKEN, "");
        if (mSharedPreferences.getString(PREF_LOCALE, "").isEmpty()) {
            String firstStartLocale = Locale.getDefault().getLanguage();
            if (!firstStartLocale.equals("en") && !firstStartLocale.equals("zh")) {
                firstStartLocale = "en";
            }
            mPrefsEditor.putString(PREF_LOCALE, firstStartLocale);
            setLocale(firstStartLocale);
        }
        mPrefsEditor.apply();
        mIsFirstRun = mSharedPreferences.getBoolean(PREF_FIRST_RUN, true);

        setContentView(R.layout.activity_login);

        mUsernameEdit = findViewById(R.id.etName);
        mPasswordEdit = findViewById(R.id.etPassword);
        mLoginButton = findViewById(R.id.btnLogin);
        mVersionNameEdit = findViewById(R.id.tvVersionName);
        mRememberMe = findViewById(R.id.cbRememberMe);


        if (mIsFirstRun) {
            // Show Server URL Change Dialog
            changeUrlDialog();
        } else {
            String uname = mSharedPreferences.getString(PREF_UNAME, "");
            if (!uname.equals("")) {
//                String md5 = mSharedPreferences.getString(PREF_HASH, "");
                mUseHashAsPwd = true;
                mUsernameEdit.setText(uname);
                mPasswordEdit.setText(MAGIC_WORD);
                mRememberMe.setChecked(true);
            }
        }

        dbHelper = new SQLiteHelper(this);

        String default_language = mSharedPreferences.getString(PREF_LOCALE, "");
        Log.i(TAG, "default_language is: " + default_language);
        if (default_language.equals("-1")) {
            Log.i(TAG, "pref language not set yet " + default_language);

        } else {
            Log.i(TAG, "pref language been set to: " + default_language);
        }

        String vName = BuildConfig.VERSION_NAME;
        mVersionNameEdit.setText(vName);

        String server_addr = extendBaseUrl(mSharedPreferences.getString(PREF_SERVER_ADDRESS, DEBUG_FALLBACK_URL));
        if (!server_addr.equals("")) {
            baseUrl = server_addr;
        }

        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deviceId = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getImei();
            } else {
                deviceId = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            }
        }


        mLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                userName = mUsernameEdit.getText().toString();
                userPassword = mPasswordEdit.getText().toString();
                validate(userName, userPassword);
            }
        });
    }

    private void changeUrlDialog() {
        Dialog mChangeUrlDialog = new Dialog(this) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_login_urldialog);
                Button login = findViewById(R.id.url_btnSubmit);
                // TODO: Debug
                EditText urlEdit = findViewById(R.id.server_url);
                urlEdit.setText(DEBUG_FALLBACK_URL);
                // TODO: Debug
                login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText urlEdit = findViewById(R.id.server_url);
                        String url = urlEdit.getText().toString();
                        TextView urlErr = findViewById(R.id.url_err);
                        if (url.equals("")) {
                            urlErr.setText(getString(R.string.msg_errorField));
                        } else {
//                            if (url.charAt(url.length() - 1) != '/') {
//                                url += "/";
//                            }
//                            url += "BLL/MobileSvc.asmx/";
                            mPrefsEditor.putString(PREF_SERVER_ADDRESS, url);
                            mPrefsEditor.apply();
                            baseUrl = extendBaseUrl(url);
                            dismiss();
                        }
                    }
                });
            }
        };

        mChangeUrlDialog.show();
    }

    private void forcePwChangeDialog() {
        Dialog forcePwChangeDialog = new Dialog(this) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_demand_change);
                Button login = findViewById(R.id.force_btnSubmit);
                login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText pwEdit = findViewById(R.id.force_password);
                        final String pw = pwEdit.getText().toString();
                        EditText pwRetypeEdit = findViewById(R.id.force_retype_password);
                        String pwRetype = pwRetypeEdit.getText().toString();
                        boolean isValid = true;
                        StringBuilder errMsg = new StringBuilder();
                        TextView errText = findViewById(R.id.force_err);
                        if (pw.equals("") || pwRetype.equals("")) {
                            errMsg.append(getString(R.string.msg_errorField));
                            isValid = false;
                        } else if (!pw.equals(pwRetype)) {
                            errMsg.append(getString(R.string.msg_errorPasswordMismatch));
                            isValid = false;
                        } else {
                            if (pw.replaceAll("[^a-z]", "").length() < mPolicy_low) {
                                errMsg.append(getString(R.string.msg_errorPasswordLower, mPolicy_low));
                                isValid = false;
                            }
                            if (pw.replaceAll("\\D", "").length() < mPolicy_num) {
                                if (!isValid) errMsg.append("\n");
                                errMsg.append(getString(R.string.msg_errorPasswordNumber, mPolicy_num));
                                isValid = false;
                            }
                            if (pw.replaceAll("[^A-Za-z0-9]", "").length() < mPolicy_sym) {
                                if (!isValid) errMsg.append("\n");
                                errMsg.append(getString(R.string.msg_errorPasswordSymbol, mPolicy_sym));
                                isValid = false;
                            }
                            if (pw.replaceAll("[^A-Z]", "").length() < mPolicy_upper) {
                                if (!isValid) errMsg.append("\n");
                                errMsg.append(getString(R.string.msg_errorPasswordUpper, mPolicy_upper));
                                isValid = false;
                            }
                            if (pw.length() < mPolicy_len) {
                                if (!isValid) errMsg.append("\n");
                                errMsg.append(getString(R.string.msg_errorPasswordTooShort, mPolicy_len));
                                isValid = false;
                            }
//                            if (!mLowercasePattern.matcher(pw).find() ||
//                                    !mUppercasePattern.matcher(pw).find() ||
//                                    !mDigitPattern.matcher(pw).find()) {
//                                if (!isValid) errMsg.append("\n");
//                                errMsg.append(getString(R.string.msg_errorPasswordAgainstPolicy));
//                                isValid = false;
//                            }
                        }
                        if (isValid) {
                            JSONObject changePwObj = new JSONObject();
                            try {
                                changePwObj.put("token", mSharedPreferences.getString(PREF_TOKEN, ""));
                                changePwObj.put("m", md5(pw));
                                changePwObj.put("program", 1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Call script method
                            JsonObjectRequest changePwReq = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                                    String.format("%s%s", baseUrl, "_ChangePassword"), changePwObj, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    mPrefsEditor.putString(PREF_HASH, md5(pw));
                                    mPrefsEditor.putBoolean(PREF_FIRST_RUN, false);
                                    mPrefsEditor.apply();

                                    dismiss();
                                    // Intent to menu Activity
                                    Intent intent = new Intent(LoginActivity.this, BluetoothNewActivity.class);
                                    finish();
                                    startActivity(intent);
                                }
                            }, networkIssueListener);
                            mRequestQueue.add(changePwReq);
                        } else {
                            errText.setText(errMsg.toString());
                        }
                    }
                });
            }
        };

        // Show Force Password Change Dialog
        forcePwChangeDialog.setCancelable(false);
        forcePwChangeDialog.show();
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.i("TAG", "Permission is granted");
                return true;
            } else {

                Log.i("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.i("TAG", "Permission is granted");
            return true;
        }
    }

    //TODO: TBD - Start
//    private class LoginTask extends AsyncTask<String, Void, ArrayList<LoginResultData>> {
//        private ProgressDialog pDialog = null;
//
//        public LoginTask() {
//        }
//
//        @Override
//        protected void onPreExecute() {
//            Log.i("LoginActivity", "LoginTask");
//            super.onPreExecute();
//            pDialog = new ProgressDialog(LoginActivity.this);
//            pDialog.setMessage(getString(R.string.login_msg_progress));
//            pDialog.setIndeterminate(false);
//            pDialog.setCancelable(true);
//            pDialog.show();
//        }
//
//        @Override
//        protected ArrayList<LoginResultData> doInBackground(String... params) {
//            ArrayList<LoginResultData> loginReceivedList = new ArrayList<LoginResultData>();
//            ArrayList<LeaveBalanceContent> leaveBalanceList = new ArrayList<LeaveBalanceContent>();
//            LeaveBalanceContent leavebalance;
//            JSONObject itemUserInfo;
//            JSONObject itemSystemParam;
//            LoginResultData loginResultData;
//            String encrypt = "";//encryptIt(userPassword);
//
//            // Check server connection
//            try {
//
//                connectToServer = isConnectedToServer(baseUrl, serverTimeOut * 1000);
//                if (connectToServer) {
//                    Log.i("connectToServer", "TRUE");
//                } else {
//                    Log.i("connectToServer", "FALSE");
//                    loginReceivedList = null;
//                    Log.i(TAG, "loginReceivedList" + "[" + loginReceivedList + "]");
//                    return loginReceivedList;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            try {
//                // Prepare parameter
//                ArrayList<NameValuePair> nvpList = new ArrayList<>();
//                NameValuePair nvp;
//
//                Gson gson = new GsonBuilder().create();
//
//                JsonObject obj = new JsonObject();
//                obj.add("Username", gson.toJsonTree(userName));
//                obj.add("UserPassword", gson.toJsonTree(encrypt));
//                obj.add("LoginToken", gson.toJsonTree(LoginToken));
//                String jsonString = gson.toJson(obj);
//
//                Log.i(TAG, "mPasswordEdit is: " + "[" + encrypt + "]");
//
//                nvp = new NameValuePair();
//                nvp.setName("Data");
//                nvp.setValue("[" + jsonString + "]");
//                nvpList.add(nvp);
//
//                nvp = new NameValuePair();
//                nvp.setName("DeviceID");
//                nvp.setValue(deviceId);
//                nvpList.add(nvp);
//
//                Log.i("LoginActivity nvpList", nvpList.get(0).getName());
//                Log.i("LoginActivity nvpList", nvpList.get(0).getValue().toString());
//                Log.i("LoginActivity nvpList", nvpList.get(1).getName());
//                Log.i("LoginActivity nvpList", nvpList.get(1).getValue().toString());
//
//
//                JSONParser jParser = new JSONParser();
//                JSONObject json = jParser.getJSONFromUrl(baseUrl + "LoginActivity", nvpList);
//                JSONArray jArray = json.getJSONArray("d");
//                Log.i(TAG, "jArray content is: " + jArray.getString(0));
//                Log.i(TAG, "jArray size is: " + jArray.length());
//                for (int i = 0; i < jArray.length(); i++) {
//                    itemUserInfo = jArray.getJSONObject(i);
//                    loginResultData = new LoginResultData();
//                    loginResultData.setUsername(itemUserInfo.getString("Username"));
//                    loginResultData.setUserId(itemUserInfo.getInt("UserID"));
//                    loginResultData.setNickname(itemUserInfo.getString("Nickname"));
//                    loginResultData.setChineseName(itemUserInfo.getString("ChineseName"));
//                    loginResultData.setEnglishName(itemUserInfo.getString("EnglishName"));
//                    loginResultData.setStaffNumber(itemUserInfo.getString("StaffNumber"));
//                    loginResultData.setEmploymentNumber(itemUserInfo.getString("EmploymentNumber"));
//                    loginResultData.setErrorCode(itemUserInfo.getString("ErrorCode"));
//                    loginReceivedList.add(loginResultData);
//
//                    Log.i(TAG, "Username is: " + loginResultData.getUsername());
//                    Log.i(TAG, "UserID is: " + loginResultData.getUserId());
//                    Log.i(TAG, "Nickname is: " + loginResultData.getNickname());
//                    Log.i(TAG, "ChineseName is: " + loginResultData.getChineseName());
//                    Log.i(TAG, "EnglishName is: " + loginResultData.getEnglishName());
//                    Log.i(TAG, "StaffNumber is: " + loginResultData.getStaffNumber());
//                    Log.i(TAG, "EmploymentNumber is: " + loginResultData.getEmploymentNumber());
//                    Log.i(TAG, "ErrorCode is: " + loginResultData.getErrorCode());
//
//                    UserName = loginResultData.getUsername();
//                    UserId = loginResultData.getUserId();
//                    NickName = loginResultData.getNickname();
//                    EnglishName = loginResultData.getEnglishName();
//                    ChineseName = loginResultData.getChineseName();
//                    StaffNumber = loginResultData.getStaffNumber();
//                    EmploymentNumber = loginResultData.getEmploymentNumber();
//                    ErrorCode = loginResultData.getErrorCode();
//
//                    if (!ErrorCode.isEmpty()) {
//                        Log.i(TAG, "Error Code is not empty");
//                        return loginReceivedList;
//                    } else {
//                        loginResultData.setSystemParameterInfo(itemUserInfo.getJSONObject("SystemParameterInfo"));
//                        Log.i(TAG, " SystemParameterInfo is: " + loginResultData.getSystemParameterInfo());
//                        itemSystemParam = loginResultData.getSystemParameterInfo();
//                        IsAllow3Sections = itemSystemParam.getBoolean("IsLeaveAllow3Sections");
//                        IsAllowHalfDay = itemSystemParam.getBoolean("IsLeaveAllowHalfDay");
//                        IsAllowHourly = itemSystemParam.getBoolean("IsLeaveAllowHourly");
//                        Log.i(TAG, "IsAllow3Sections is: " + IsAllow3Sections);
//                        Log.i(TAG, "IsAllowHalfDay is: " + IsAllowHalfDay);
//                        Log.i(TAG, "IsAllowHourly is: " + IsAllowHourly);
//
//                        loginResultData.setLeaveBalance(itemUserInfo.getJSONArray("LeaveBalance"));
//                        Log.i(TAG, "LeaveBalance content is: " + loginResultData.getLeaveBalance());
//                        LeaveBalance = loginResultData.getLeaveBalance();
//                        Log.i(TAG, "LeaveBalance array length is: " + LeaveBalance.length());
//
//                        if (LeaveBalance.length() > 0) {
//                            dbHelper.openDB();
//                            long resultDeleteLeaveBalance = dbHelper.deleteLeaveBalance();
//                            if (resultDeleteLeaveBalance == 0) {
//                                Log.i(TAG, "No Leave Balance record found");
//                            } else {
//                                Log.i(TAG, "Leave Balance successfully deleted");
//                            }
//                            // Insert default LeaveBalance records as the first record display onto the spinner
//                            long resultInsertBalance = dbHelper.insertLeaveBalance(-1, EmploymentNumber, "OTHER", "Other (其它)... ", null, "");
//
//                            if (resultInsertBalance == -1) {
//                                Log.i(TAG, "Insert Leave Balance record failed");
//                            } else {
//                                Log.i(TAG, "Insert Leave Balance success");
//                            }
//                            dbHelper.closeDB();
//                        }
//
//                        for (int j = 0; j < LeaveBalance.length(); ++j) {
//                            JSONObject subMenuObject = LeaveBalance.getJSONObject(j);
//                            tmpEmploymentNumber = loginResultData.getEmploymentNumber();
//                            LeaveCode = subMenuObject.getString("LeaveCode");
//                            LeaveDescription = subMenuObject.getString("LeaveDescription");
//                            Balance = subMenuObject.getString("Balance");
//                            BalanceAsOfDate = subMenuObject.getString("AsOfDate");
//
//                            leavebalance = new LeaveBalanceContent(tmpEmploymentNumber, LeaveCode, LeaveDescription, Balance, BalanceAsOfDate, 0);
//                            leaveBalanceList.add(leavebalance);
//                        }
//                        if (!(leaveBalanceList.isEmpty())) {
//                            dbHelper.openDB();
//                            dbHelper.insertLeaveBalanceList(leaveBalanceList);
//                            dbHelper.closeDB();
//                        }
//                    }
//                }
//            } catch (Exception ex) {
//                Log.i(TAG, ex.toString());
//            }
//            return loginReceivedList;
//        }
//
//        protected void onPostExecute(ArrayList<LoginResultData> loginReceivedList) {
//            if (pDialog != null) {
//                if (pDialog.isShowing()) {
//                    pDialog.dismiss();
//                }
//            }
//            Log.i(TAG, "onPostExecute");
//            Log.i(TAG, "loginReceivedList" + "[" + loginReceivedList + "]");
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//
//            if (loginReceivedList != null) {
//                if (!ErrorCode.isEmpty()) {
//                    if (ErrorCode.equals("ERROR_INVALID_USERNAMEORPASSWORD")) {
//                        Log.i(TAG, "ERROR USER/PWD");
//                        builder.setMessage(getString(R.string.msg_errorUserPwd));
//                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.dismiss();
//                            }
//                        });
//                        AlertDialog dlg = builder.create();
//                        dlg.show();
//                    } else if (ErrorCode.equals("ERROR_DATA_INCOMPLETE")) {
//                        Log.i(TAG, "ERROR USER/PWD");
//                        builder.setMessage(getString(R.string.msg_errorLoginFail));
//                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.dismiss();
//                            }
//                        });
//                        AlertDialog dlg = builder.create();
//                        dlg.show();
//                    } else if (ErrorCode.equals("ERROR_VALID_EMPLOYMENT_NOTFOUND")) {
//                        Log.i(TAG, "ERROR NO EMPLOYMENT");
//                        builder.setMessage(getString(R.string.msg_errorEmployment));
//                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.dismiss();
//                            }
//                        });
//                        AlertDialog dlg = builder.create();
//                        dlg.show();
//                    } else {
//                        Log.i(TAG, ErrorCode);
//                        builder.setMessage(ErrorCode);
//                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.dismiss();
//                            }
//                        });
//                        AlertDialog dlg = builder.create();
//                        dlg.show();
//                    }
//                } else {
//                    DBCreate();
//                    SubmitData2SQLiteDB();
//
//                    Intent intent = new Intent(LoginActivity.this, SelectionActivity.class);
//                    startActivity(intent);
//                }
//            } else {
//                builder.setMessage(getString(R.string.msg_errorLoginFail));
//                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog dlg = builder.create();
//                dlg.show();
//            }
//        }
//    }
    //TODO: TBD - End

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "Permission granted");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        deviceId = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getImei();
                    } else {
                        deviceId = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    }
                    //do ur specific task after read phone state granted
                } else {
                    Log.i("TAG", "Permission denied");
                    finish();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void nonce() {
        // IP address AlertDialog


        // You have to start it all over (get a new nonce) if login fails
        mRequestQueue = Volley.newRequestQueue(this);
        JSONObject nonceReqBody = new JSONObject();
        try {
            nonceReqBody.put("deviceID", deviceId);
            nonceReqBody.put("program", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage(getString(R.string.loading));
        pd.setCancelable(false);
        pd.show();
        Log.i("nonace", "nonce");
        //Log.d("IMEI", deviceId);
        JsonObjectRequest nonceRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                String.format("%s%s", baseUrl, "_GenerateNonce"), nonceReqBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i("nonce", "nonce");
                    String nonce = response.getJSONObject("d").getString("n");

                    if (nonce != null) {
                        mRequestQueue = Volley.newRequestQueue(LoginActivity.this);
                        String cnonce = cnonce();

                        final String md5 = md5(userPassword);
                        String hash = sha512(nonce + cnonce + md5);
                        // TODO: May change to padWith0(nonce) ^ padWith0(cnonce) ^ padWith0(md5(password))
                        JSONObject tokenReqBody = new JSONObject();
                        tokenReqBody.put("deviceID", deviceId);
                        tokenReqBody.put("user", userName);
                        tokenReqBody.put("c", cnonce);
                        tokenReqBody.put("h", hash);
                        tokenReqBody.put("program", 1);
                        JsonObjectRequest tokenRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                                String.format("%s%s", baseUrl, "_Login"), tokenReqBody, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (response.has("d")) {
                                    try {
                                        if (!response.isNull("d")) {
                                            JSONObject tokContainer = response.getJSONObject("d");
                                            String tok = tokContainer.getString("t");
                                            String rtok = tokContainer.getString("r");

                                            mPolicy_low = tokContainer.getInt("policy_low");
                                            mPolicy_num = tokContainer.getInt("policy_num");
                                            mPolicy_sym = tokContainer.getInt("policy_sym");
                                            mPolicy_upper = tokContainer.getInt("policy_upper");
                                            mPolicy_len = tokContainer.getInt("policy_len");
                                            Log.i("Success", tok);
                                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
                                            // Store hashed password (DON'T SAVE THE REAL ONE!!!)
                                            if (mRememberMe.isChecked()) {
                                                editor.putString(PREF_UNAME, userName);
                                                editor.putString(PREF_HASH, md5);
                                            } else {
                                                // Clear saved username and password.
                                                editor.putString(PREF_UNAME, "");
                                                editor.putString(PREF_HASH, "");
                                            }

                                            // Store session token
                                            editor.putString(PREF_TOKEN, tok);
                                            editor.putString(PREF_REFRESH_TOKEN, rtok);
                                            editor.apply();

                                            // TODO: Leave Object
                                            mRequestQueue = Volley.newRequestQueue(LoginActivity.this);

                                            JSONObject leaveObj = new JSONObject();
                                            leaveObj.put("token", tok);
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("en", "gb"));
                                            leaveObj.put("fromDate", simpleDateFormat.format(Calendar.getInstance().getTime()));
                                            leaveObj.put("program", 1);

                                            JsonObjectRequest leaveReq = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                                                    String.format("%s%s", baseUrl, "_GetLeaveInfo"), leaveObj, new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    if (mIsFirstRun) {
                                                        DBCreate();
                                                        forcePwChangeDialog();
                                                    } else {
                                                        mPrefsEditor.putBoolean(PREF_FIRST_RUN, false);
                                                        mPrefsEditor.apply();
                                                        DBCreate();
                                                        pd.dismiss();
                                                        Intent intent = new Intent(LoginActivity.this, BluetoothNewActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            }, networkIssueListener);
                                            leaveReq.setRetryPolicy(new DefaultRetryPolicy(
                                                    60000,
                                                    0,
                                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                            mRequestQueue.add(leaveReq);
//                                            JsonObjectRequest leaveReq = new JsonObjectRequest(JsonObjectRequest.Method.POST,
//                                                    String.format("%s%s", baseUrl, "_GetLeaveInfo"), leaveObj, new Response.Listener<JSONObject>() {
//                                                @Override
//                                                public void onResponse(JSONObject response) {
//
//                                                    JSONObject employment = null;
//                                                    try {
//                                                        JSONArray array = response.getJSONArray("d");
//                                                        for (int foo = 0; foo < array.length(); foo++) {
//                                                            employment = array.getJSONObject(foo);
//                                                            String err = employment.getString("ErrorCode");
//                                                            Log.i("LoginBlocked", err);
//                                                            if (err.equals(ERR_VALID_EMPLOYMENT_NOT_FOUND)) {
//                                                                pd.dismiss();
//                                                                AlertDialog.Builder bd = new AlertDialog.Builder(LoginActivity.this);
//                                                                bd.setMessage(R.string.no_active_employments);
//                                                                bd.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                                                    @Override
//                                                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                                                        dialogInterface.dismiss();
//                                                                    }
//                                                                });
//                                                                bd.create().show();
//                                                                return;
//                                                            }
//
//                                                            if (employment.getString("EmploymentNumber") != null) {
//                                                                Intent intent = new Intent(LoginActivity.this, BluetoothNewActivity.class);
//                                                                startActivity(intent);
//                                                            }
//
//                                                            DBCreate();
//                                                        }
//                                                    } catch (JSONException e) {
//                                                        e.printStackTrace();
//                                                    }
//                                                    // TODO: Leave Object
//
//                                                    if (mIsFirstRun) {
//                                                        forcePwChangeDialog();
//                                                    } else {
//                                                        // Intent to menu Activity
//                                                        Intent intent = new Intent(LoginActivity.this, BluetoothNewActivity.class); // TODO: Formerly Selection.class
//                                                        //finish();
//                                                        startActivity(intent);
//                                                    }
//                                                }
//                                            }, networkIssueListener);
//                                            leaveReq.setRetryPolicy(new DefaultRetryPolicy(
//                                                    60000,
//                                                    0,
//                                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                                            mRequestQueue.add(leaveReq);
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                            builder.setMessage(getString(R.string.msg_errorUserPwd))
                                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.cancel();
                                                            pd.dismiss();
                                                        }
                                                    })
                                                    .create().show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, networkIssueListener);
                        tokenRequest.setRetryPolicy(new DefaultRetryPolicy(
                                30000,
                                0,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        mRequestQueue.add(tokenRequest);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Cannot reach server, consult SQLite table OfflineUsers instead.
                DBCreate();
                dbHelper.openDB();
                String mHash = dbHelper.getUsrHash(mUsernameEdit.getText().toString());
                dbHelper.closeDB();
                if (mHash.equals(md5(mPasswordEdit.getText().toString()))) {
                    Intent intent = new Intent(LoginActivity.this, BluetoothNewActivity.class);
                    startActivity(intent);
                }
            }
        });   //getGenericErrorListener(this, pd)
        mRequestQueue.add(nonceRequest);
    }

    private String md5(String password) {
        String md5 = null;
        if (mUseHashAsPwd && (password.equals(MAGIC_WORD))) {
            return mSharedPreferences.getString(PREF_HASH, "");
        } else {
            try {
                MessageDigest digest = MessageDigest
                        .getInstance(MD5);
                digest.update(password.getBytes());
                byte messageDigest[] = digest.digest();
                md5 = new String(Hex.encodeHex(messageDigest)).toUpperCase(); // toUpperCase is important!
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return md5;
    }

    private String cnonce() {
        return randomString(rnd.nextInt(10) + 10);
    }

    private String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append((char) rnd.nextInt(127));
        return sb.toString();
    }

    private String sha512(String mixture) {
        String sha512 = null;
        try {
            MessageDigest digest = MessageDigest
                    .getInstance(SHA_512);
            digest.update(mixture.getBytes());
            byte messageDigest[] = digest.digest();
            sha512 = new String(Hex.encodeHex(messageDigest));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha512;
    }
    //endregion

    public void DBCreate() {

        dbHelper.openDB();

        SQLITEDATABASE = openOrCreateDatabase("HRMSDataBase", Context.MODE_PRIVATE, null);

        SQLITEDATABASE.execSQL("CREATE TABLE IF NOT EXISTS User(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name VARCHAR, userid INTEGER, nickName VARCHAR, englishName VARCHAR, chineseName VARCHAR, staffNumber VARCHAR, employmentNumber VARCHAR, isAllow3Sections BOOLEAN, isAllowHalfDay BOOLEAN, isAllowHourly BOOLEAN);");

        SQLITEDATABASE.execSQL("CREATE TABLE IF NOT EXISTS LeaveBalance(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, empNo VARCHAR, leaveType VARCHAR, leaveDescription VARCHAR, leaveBalance VARCHAR, leaveBalanceAsOfDate VARCHAR);");

        dbHelper.closeDB();
    }

    // Check for blank fields and password policy
    private void validate(String userName, String userPassword) {
        boolean isValid = true;
        StringBuilder errMsg = new StringBuilder();

        if ((userName.equals("")) || (userPassword.equals(""))) {
            errMsg.append(getString(R.string.msg_errorField));
            isValid = false;
        }
//        if (userPassword.length() < 8) {
//            if (!isValid) errMsg.append("\n");
//            errMsg.append(getString(R.string.msg_errorPasswordTooShort));
//            isValid = false;
//        }
//        if (!mLowercasePattern.matcher(userPassword).find() ||
//                !mUppercasePattern.matcher(userPassword).find() ||
//                !mDigitPattern.matcher(userPassword).find()) {
//            if (!isValid) errMsg.append("\n");
//            errMsg.append(getString(R.string.msg_errorPasswordAgainstPolicy));
//            isValid = false;
//        }

        if (isValid) {
            nonce();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage(errMsg.toString());
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dlg = builder.create();
            dlg.show();
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.i(TAG, "onResume");
//        //resetDisconnectTimer();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        Log.i(TAG, "onStop");
//        //stopDisconnectTimer();
//    }

//    //add delay to prevent app hang for older android version
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Log.i(TAG, "onRestart");
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    finish();
//                    overridePendingTransition(0, 0);
//                    startActivity(getIntent());
//                    overridePendingTransition(0, 0);
//                }
//            }, 1);
//        } else {
//            setLocale(mSharedPreferences.getString(PREF_LOCALE, "en"));
//            recreate();
//        }
//    }
}