package hk.com.dataworld.iattendance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static hk.com.dataworld.iattendance.Constants.DEBUG_FALLBACK_URL;
import static hk.com.dataworld.iattendance.Constants.PREF_LOCALE;
import static hk.com.dataworld.iattendance.Constants.PREF_REFRESH_TOKEN;
import static hk.com.dataworld.iattendance.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.iattendance.Constants.PREF_TOKEN;
import static hk.com.dataworld.iattendance.Utility.extendBaseUrl;
import static hk.com.dataworld.iattendance.Utility.getGenericErrorListener;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    // 10 min = 30 * 60 * 1000 ms
    public static final long DISCONNECT_TIMEOUT = 600000;
    private static final String TAG = BaseActivity.class.getSimpleName();
    private static String baseUrl;
    protected String deviceId;
    protected boolean mIsEnableRestartBehaviour = true;
    SQLiteHelper dbHelper;
    private Handler disconnectHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };
    private Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {
            // Going back to Login page on disconnect
            Log.i(TAG, "disconnectCallback");
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext())
                    .setCancelable(false)
                    .setMessage(R.string.msg_infoTimeoutLogin)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finishAffinity();
                            startActivity(new Intent(BaseActivity.this, LoginActivity.class));
                            finish();
                        }
                    });
            // builder.create().show();
        }
    };
    private ProgressDialog pDialog;

    //region KeyboardRelated
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        dbHelper = new SQLiteHelper(BaseActivity.this);

        setLocale(PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_LOCALE, "en"));

        String server_addr = extendBaseUrl(PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_SERVER_ADDRESS, DEBUG_FALLBACK_URL));
        if (!server_addr.equals("")) {
            baseUrl = server_addr;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        AlertDialog.Builder builder;
        switch (id) {
            case R.id.action_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
            }
            break;
            case R.id.action_logout:
                builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.msg_infoLogoutConfirm);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

//                        dbHelper.openDB();
//                        long resultDeleteUser = dbHelper.deleteUser();
//                        if (resultDeleteUser == 0) {
//                            Log.i(TAG, "No User record found");
//                        } else {
//                            Log.i(TAG, "User record successfully deleted");
//                        }
//                        long resultDeleteLeaveBalance = dbHelper.deleteLeaveBalance();
//                        if (resultDeleteLeaveBalance == 0) {
//                            Log.i(TAG, "No Leave Balance record found");
//                        } else {
//                            Log.i(TAG, "Leave Balance record successfully deleted");
//                        }
//                        long resultDeleteApprovalHistory = dbHelper.deleteApprovalHistory();
//                        if (resultDeleteApprovalHistory == 0) {
//                            Log.i(TAG, "No Approval History record found");
//                        } else {
//                            Log.i(TAG, "Approval History record successfully deleted");
//                        }
//                        dbHelper.closeDB();
//                        finishAffinity();
//                        startActivity(new Intent(BaseActivity.this, LoginActivity.class));

                        logOut();
                    }
                });

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                builder.create().show();
                break;
            case R.id.action_about:
                builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.msg_infoCopyright).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
        if (mIsEnableRestartBehaviour) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    }
                }, 1);
            } else {
                setLocale(PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_LOCALE, "en"));
                recreate();
            }
        }
    }

    public void resetDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
        //disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT);
    }

//    public void setLocale() {
//        String prefer_language = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_LOCALE, "");
//
//        if (prefer_language.equals("-1") || (prefer_language.equals(""))) {
//            prefer_language = Locale.getDefault().getLanguage();
//        } else if (prefer_language.equals("zh_TW")) {
//            prefer_language = "zh";
//        } else {
//            prefer_language = "en";
//        }
//
//        Locale locale = new Locale(prefer_language);
//        Locale.setDefault(locale);
//        Configuration config = getBaseContext().getResources().getConfiguration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config,
//                getBaseContext().getResources().getDisplayMetrics());
//    }

    public void stopDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
    }

    public void setLocale(String locale) {
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        Locale lc;
        if (locale.length() > 2) {
            lc = new Locale(locale.substring(0, 2), locale.substring(3, 5));
        } else {
            lc = new Locale(locale);
        }
        if (!configuration.locale.equals(lc)) {
            configuration.setLocale(lc);
            resources.updateConfiguration(configuration, null);
        }
    }

    @Override
    public void onUserInteraction() {
        Log.i(TAG, "onUserInteraction");
        // 2018.12.03 No need to call timeout on LoginActivity
        if (!getClass().getSimpleName().equals("LoginActivity")) {
            resetDisconnectTimer();
        }
    }

    public boolean isConnectedToServer(String url, int timeout) {
        try {
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void logOut() {
        try {
            pDialog = new ProgressDialog(BaseActivity.this);
            pDialog.setMessage(getString(R.string.logout_msg_progress));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            JSONObject obj = new JSONObject();
            obj.put("token", PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_TOKEN, ""));
            obj.put("program", 1);

            RequestQueue lRequestQueue = Volley.newRequestQueue(this);

            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format("%s%s", baseUrl, "_Logout"),
                    obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (pDialog != null) {
                        if (pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }

                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(PREF_TOKEN, "");
                    editor.putString(PREF_REFRESH_TOKEN, "");
                    editor.apply();

                    finishAffinity();
                    startActivity(new Intent(BaseActivity.this, LoginActivity.class));
                }
            }, getGenericErrorListener(this, pDialog));

            lRequestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private class LogoutTask extends AsyncTask<String, Void, Integer> {
//        private ProgressDialog pDialog = null;
//
//        LogoutTask() {
//        }
//
//        @Override
//        protected void onPreExecute() {
//            Log.i(TAG, "LogoutTask");
//            super.onPreExecute();
//            pDialog = new ProgressDialog(BaseActivity.this);
//            pDialog.setMessage(getString(R.string.logout_msg_progress));
//            pDialog.setIndeterminate(false);
//            pDialog.setCancelable(true);
//            pDialog.show();
//        }
//
//        @Override
//        protected Integer doInBackground(String... params) {
//            int result = -1;
//            try {
//                // Prepare parameter
//                ArrayList<NameValuePair> nvpList = new ArrayList<>();
//                NameValuePair nvp;
//
//                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
//
//                nvp = new NameValuePair();
//                nvp.setName("token");
//                nvp.setValue(sp.getString(PREF_TOKEN, ""));
//                Log.i("tok", sp.getString(PREF_TOKEN, ""));
//                nvpList.add(nvp);
//
//                JSONParser jParser = new JSONParser();
//                JSONObject json = jParser.getJSONFromUrl(baseUrl + "_Logout", nvpList);
//                result = json.getInt("d");
//
//                if (result == -1) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
//                    Log.i(TAG, "General error from server");
//                    builder.setMessage(getString(R.string.msg_errorLoginFail));
//                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
//                        }
//                    });
//                    builder.create().show();
//                }
//
//            } catch (Exception ex) {
//                Log.e(TAG, ex.toString());
//            }
//            return result;
//        }
//
//        protected void onPostExecute(Integer result) {
//            if (result == 1) {
//                if (pDialog != null) {
//                    if (pDialog.isShowing()) {
//                        pDialog.dismiss();
//                    }
//                }
//
//                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
//                SharedPreferences.Editor editor = sp.edit();
//                editor.putString(PREF_TOKEN, "");
//                editor.putString(PREF_REFRESH_TOKEN, "");
//                editor.apply();
//                dbHelper.openDB();
//
//                long resultDeleteUser = dbHelper.deleteUser();
//                if (resultDeleteUser == 0) {
//                    Log.i(TAG, "No User record found");
//                } else {
//                    Log.i(TAG, "User record successfully deleted");
//                }
//
//                long resultDeleteLeaveBalance = dbHelper.deleteLeaveBalance();
//                if (resultDeleteLeaveBalance == 0) {
//                    Log.i(TAG, "No Leave Balance record found");
//                } else {
//                    Log.i(TAG, "Leave Balance successfully deleted");
//                }
//
//                dbHelper.closeDB();
//                finishAffinity();
//                startActivity(new Intent(BaseActivity.this, LoginActivity.class));
//            }

    //region TBD
//            AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);

//            if (logoutReceivedList != null) {
//                if (!ErrorCode.isEmpty()) {
//                    Log.i(TAG, ErrorCode);
//                    builder.setMessage(ErrorCode);
//                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
//                        }
//                    });
//                    AlertDialog dlg = builder.create();
//                    dlg.show();
//                } else {
//                    Log.i(TAG, "Error Code is Empty");
//                    Log.i(TAG, "LogoutResult is not null");
//                    Log.i(TAG, "Logout success!");
//                    dbHelper.openDB();
//                    long resultDeleteUser = dbHelper.deleteUser();
//                    if (resultDeleteUser == 0) {
//                        Log.i(TAG, "No User record found");
//                    } else {
//                        Log.i(TAG, "User record successfully deleted");
//                    }
//
//                    long resultDeleteLeaveBalance = dbHelper.deleteLeaveBalance();
//                    if (resultDeleteLeaveBalance == 0) {
//                        Log.i(TAG, "No Leave Balance record found");
//                    } else {
//                        Log.i(TAG, "Leave Balance successfully deleted");
//                    }
//
//                    dbHelper.closeDB();
//                    finishAffinity();
//                    startActivity(new Intent(BaseActivity.this, LoginActivity.class));
//                }
//            } else {
//                Log.i("LogoutActivity: ", "Logout Failed");
//                builder.setMessage(getString(R.string.msg_errorLoginFail));
//                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog dlg = builder.create();
//                dlg.show();
//            }
    //endregion
//        }
//    }
}
