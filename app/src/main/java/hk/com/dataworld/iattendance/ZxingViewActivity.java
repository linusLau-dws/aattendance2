package hk.com.dataworld.iattendance;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.klimek.scanner.OnCameraErrorCallback;
import de.klimek.scanner.OnDecodedCallback;
import de.klimek.scanner.ScannerView;

public class ZxingViewActivity extends BaseActivity implements OnDecodedCallback, OnCameraErrorCallback {
    private static final int CAMERA_PERMISSION_REQUEST = 0xabc;
    private ScannerView mScanner;
    private boolean mPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zxing_view);

        mScanner = findViewById(R.id.zxing_view);
        mScanner.setOnDecodedCallback(this);
        mScanner.setOnCameraErrorCallback(this);

        // get permission
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        mPermissionGranted = cameraPermission == PackageManager.PERMISSION_GRANTED;
        if (!mPermissionGranted) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.length > 0) {
            mPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionGranted) {
            mScanner.startScanning();
            Log.i("Zxing", "start");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Zxing", "paused");
        mScanner.stopScanning();
    }

    @Override
    public void onDecoded(String decodedData) {
        Log.i("Zxing", decodedData);
        Toast.makeText(this, decodedData, Toast.LENGTH_SHORT).show();
//        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("Is the employment ID correct?")
        mScanner.stopScanning();
        Intent returnIntent=new Intent();
        returnIntent.putExtra("employmentID", decodedData);
        setResult(RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onCameraError(Exception error) {
        Log.i("Err", "erred");
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
    }
}
