package hk.com.dataworld.iattendance;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import androidx.annotation.Nullable;

import static hk.com.dataworld.iattendance.Constants.DEBUG_FALLBACK_URL;
import static hk.com.dataworld.iattendance.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.iattendance.Constants.PREF_TOKEN;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_Address;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_DateTime;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_InOut;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_StationCode;
import static hk.com.dataworld.iattendance.SQLiteHelper.BT_ZoneCode;
import static hk.com.dataworld.iattendance.Utility.extendBaseUrl;

public class AttendanceSyncWifiService extends Service {
    public AttendanceSyncWifiService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    Log.i("TestStates", "Available");
                    // Sync here
                    sync();
                }

                @Override
                public void onLosing(Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    Log.i("TestStates", "Losing");
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    Log.i("TestStates", "Lost");
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                }

                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                }
            });
        }
    }

    private void sync() {
        SharedPreferences lPref = PreferenceManager.getDefaultSharedPreferences(this);
        String token = lPref.getString(PREF_TOKEN, "");
        if (token.isEmpty()) {
            return;
        }
        try {
            RequestQueue lQueue = Volley.newRequestQueue(this);
            JSONObject obj = new JSONObject();
            final SQLiteHelper dbHelper = new SQLiteHelper(this);
            dbHelper.openDB();
            ArrayList<ContentValues> unsynced = dbHelper.getUnsyncedRecords();
            //get all "SELECT * FROM " +  + " WHERE Status = 0"
            dbHelper.closeDB();

            obj.put("token", token);
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
                array.put(innerObj);
            }
            obj.put("Data", array.toString());
            Log.i("reqContent", obj.toString());
            Log.i("url", String.format("%s%s", extendBaseUrl(lPref.getString(PREF_SERVER_ADDRESS, DEBUG_FALLBACK_URL)), "BluetoothSyncAttendance"));

            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", extendBaseUrl(lPref.getString(PREF_SERVER_ADDRESS, DEBUG_FALLBACK_URL)), "BluetoothSyncAttendance"),
                    obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("Success", "sync");
                            // Return successful dateTime
                            try {
                                JSONObject obj = response.getJSONObject("d");
                                JSONArray datetime = obj.getJSONArray("timestamps");
                                String synctime = obj.getString("synctime");
                                dbHelper.openDB();
                                for (int y = 0; y < datetime.length(); y++) {
                                    dbHelper.setSynced(datetime.getString(y), synctime);
                                }
                                dbHelper.closeDB();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // Set successful dateTime to 1
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError) {
                        Log.i("sync", "Timeout");
                    } else if (error instanceof NoConnectionError) {
                        Log.i("sync", "NoConnection");
                    } else if (error instanceof AuthFailureError) {
                        Log.i("sync", "AuthFailureError");
                    } else if (error instanceof ServerError) {
                        Log.i("sync", "ServerError");
                    } else if (error instanceof NetworkError) {
                        Log.i("sync", "NetworkError");
                    } else if (error instanceof ParseError) {
                        Log.i("sync", "ParseError");
                    }
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            lQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(fd, writer, args);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
    @Override
    public IBinder peekService(Context myContext, Intent service) {
        return super.peekService(myContext, service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        boolean isWifiEnabled = wifiManager.isWifiEnabled();
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        boolean isNetworkEnabled = cm.isDefaultNetworkActive();
//
//        if (isWifiEnabled || isNetworkEnabled) {

        Log.i("classRunning", "broadcastReceiver");

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    Log.i("TestStates", "Available");
                }

                @Override
                public void onLosing(Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    Log.i("TestStates", "Losing");
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    Log.i("TestStates", "Lost");
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                }

                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                }
            });
        }

        try {
            RequestQueue lQueue = Volley.newRequestQueue(context);
            SharedPreferences lPref = PreferenceManager.getDefaultSharedPreferences(context);
            JSONObject obj = new JSONObject();
            final SQLiteHelper dbHelper = new SQLiteHelper(context);
            dbHelper.openDB();
            ArrayList<ContentValues> unsynced = dbHelper.getUnsyncedRecords();
            //get all "SELECT * FROM " +  + " WHERE Status = 0"
            dbHelper.closeDB();

            obj.put("token", lPref.getString(PREF_TOKEN, ""));
            JSONArray array = new JSONArray();
            for (ContentValues c :
                    unsynced) {
                JSONObject innerObj = new JSONObject();
                innerObj.put("useragent", System.getProperty("http.agent"));
                innerObj.put("datetime", c.get(BT_DateTime));
                innerObj.put("address", c.get(BT_Address));
                innerObj.put("inout", c.get(BT_InOut));
                array.put(innerObj);
            }
            obj.put("Data", array.toString());

            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", extendBaseUrl(lPref.getString(PREF_SERVER_ADDRESS, DEBUG_FALLBACK_URL)), "BluetoothSyncAttendance"),
                    obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Return successful dateTime
                            try {
                                dbHelper.openDB();
                                JSONArray datetime = response.getJSONArray("d");
                                for (int y = 0; y < datetime.length(); y++) {
                                    dbHelper.setSynced(datetime.getString(y));
                                }
                                dbHelper.closeDB();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // Set successful dateTime to 1
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Maybe not this network
                }
            });
            lQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
//        else {
//            //stop service
//        }
//    }
//    }
    */
}
