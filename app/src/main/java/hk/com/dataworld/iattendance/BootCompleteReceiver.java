package hk.com.dataworld.iattendance;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static hk.com.dataworld.iattendance.Constants.PREF_REFRESH_TOKEN;
import static hk.com.dataworld.iattendance.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.iattendance.Constants.PREF_TOKEN;
import static hk.com.dataworld.iattendance.Utility.extendBaseUrl;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG_BOOT_BROADCAST_RECEIVER = "BOOT_BROADCAST_RECEIVER";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        String message = "BootDeviceReceiver onReceive, action is " + action;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        Log.d(TAG_BOOT_BROADCAST_RECEIVER, action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            //
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String token = sp.getString(PREF_TOKEN, "");
            String refrToken = sp.getString(PREF_REFRESH_TOKEN, "");
            String baseUrl = extendBaseUrl(sp.getString(PREF_SERVER_ADDRESS, ""));
            if (!token.isEmpty()) {
                RequestQueue queue = Volley.newRequestQueue(context);
                JSONObject obj = new JSONObject();
                try {
                    obj.put("oldToken", token);
                    obj.put("refreshToken", refrToken);
                    obj.put("program", 1);
                    JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                            String.format("%s%s", baseUrl, "_RefreshToken"), obj,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    if (!response.isNull("d")) {
                                        try {
                                            JSONObject toks = response.getJSONObject("d");
                                            String newToken = toks.getString("t");
                                            String newRefr = toks.getString("r");

                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString(PREF_TOKEN, newToken);
                                            editor.putString(PREF_REFRESH_TOKEN, newRefr);
                                            editor.apply();

                                            // Start Bluetooth Service on Boot
                                            Intent startBtServiceIntent = new Intent(context, AttendanceSyncService.class);
                                            context.startService(startBtServiceIntent);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
                    queue.add(req);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

//        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_NAME, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
//            notificationChannel.enableLights(true);
//            notificationChannel.enableVibration(true);
//            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400, 300, 200});
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
    }
}