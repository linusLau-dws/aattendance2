package hk.com.dataworld.iattendance;

/**
 * Created by Terence on 2018/1/25.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import static hk.com.dataworld.iattendance.Constants.STATUS_APPROVED;
import static hk.com.dataworld.iattendance.Constants.STATUS_CANCELLED;
import static hk.com.dataworld.iattendance.Constants.STATUS_CONFIRMED_CANCELLED;
import static hk.com.dataworld.iattendance.Constants.STATUS_PENDING_CANCEL;
import static hk.com.dataworld.iattendance.Constants.STATUS_REJECTED;
import static hk.com.dataworld.iattendance.Constants.STATUS_WAITING;

//import java.io.IOException;
//import java.io.InputStream;
//import android.util.Log;

public class Utility {

    private static final String TAG = Utility.class.getSimpleName();

    // convert from bitmap to byte array
    static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 50, stream);
        return stream.toByteArray();
    }

    // resize bitmap
    static Bitmap resizeBitmap(Bitmap bitmap, int wi, int hi) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) wi) / width;
        float scaleHeight = ((float) hi) / height;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();

        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    // convert from byte array to bitmap
    public static Bitmap getPhoto(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    // 2018.12.11 Begin
    static String getDayOfWeekSuffixedString(Context context, String dateInString) {
        if (dateInString == null) return "";
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd", new Locale("en", "gb")).parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dateOfWeek = c.get(Calendar.DAY_OF_WEEK);


        String dateOfWeek_str = null;
        switch (dateOfWeek) {
            case Calendar.SUNDAY:
                dateOfWeek_str = context.getString(R.string.date_sun, dateInString);
                break;
            case Calendar.MONDAY:
                dateOfWeek_str = context.getString(R.string.date_mon, dateInString);
                break;
            case Calendar.TUESDAY:
                dateOfWeek_str = context.getString(R.string.date_tue, dateInString);
                break;
            case Calendar.WEDNESDAY:
                dateOfWeek_str = context.getString(R.string.date_wed, dateInString);
                break;
            case Calendar.THURSDAY:
                dateOfWeek_str = context.getString(R.string.date_thu, dateInString);
                break;
            case Calendar.FRIDAY:
                dateOfWeek_str = context.getString(R.string.date_fri, dateInString);
                break;
            case Calendar.SATURDAY:
                dateOfWeek_str = context.getString(R.string.date_sat, dateInString);
                break;
        }
        return dateOfWeek_str;
    }

    static String getShortDayOfWeek(Context context, String dateInString) {
        if (dateInString == null) return "";
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd", new Locale("en", "gb")).parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dateOfWeek = c.get(Calendar.DAY_OF_WEEK);


        String dateOfWeek_str = null;
        switch (dateOfWeek) {
            case Calendar.SUNDAY:
                dateOfWeek_str = context.getString(R.string.sun_short);
                break;
            case Calendar.MONDAY:
                dateOfWeek_str = context.getString(R.string.mon_short);
                break;
            case Calendar.TUESDAY:
                dateOfWeek_str = context.getString(R.string.tue_short);
                break;
            case Calendar.WEDNESDAY:
                dateOfWeek_str = context.getString(R.string.wed_short);
                break;
            case Calendar.THURSDAY:
                dateOfWeek_str = context.getString(R.string.thu_short);
                break;
            case Calendar.FRIDAY:
                dateOfWeek_str = context.getString(R.string.fri_short);
                break;
            case Calendar.SATURDAY:
                dateOfWeek_str = context.getString(R.string.sat_short);
                break;
        }
        return dateOfWeek_str;
    }

    static double roundTo2Dp(double balance) {
        return Math.round(balance * 100.0) / 100.0;
    }

    static String extendBaseUrl(String baseUrl) {
        if (baseUrl.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        if (baseUrl.charAt(0) != 'h') {
            sb.append("http://");
        }
        sb.append(baseUrl);
        if (baseUrl.charAt(baseUrl.length() - 1) != '/') {
            sb.append("/");
        }
        sb.append("DW-iHR/BLL/MobileSvc.asmx/");
        return sb.toString();
    }

    static Response.ErrorListener getGenericErrorListener(final Context context, @Nullable final ProgressDialog pDialog) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (pDialog != null) {
                        if (pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }
                } catch (Exception e) {

                }

                if (error instanceof TimeoutError) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.msg_infoTimeout)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create().show();
                    return;
                } else if (error instanceof NoConnectionError) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.msg_errorNoAvailableNetworks)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create().show();
                    return;
                } else if (error instanceof NetworkError) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.msg_errorLoginFail)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create().show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                switch (error.networkResponse.statusCode) {
                    case 403:
                        builder.setMessage(R.string.msg_errorSessionExpired)
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(context, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        context.startActivity(intent);
                                    }
                                })
                                .create().show();
                        break;
                    case 500:
                        builder.setMessage(String.format("%s:\n%s", context.getString(R.string.msg_errorPleaseContact), error.getMessage()))
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(context, BluetoothNewActivity.class);  // TODO: Formerly Selection.class
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        context.startActivity(intent);
                                    }
                                })
                                .create().show();
                }
            }
        };
    }
    // 2018.12.11 End
    // Converting byte[] to hex string:

    static String byteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }
}
