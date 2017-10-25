package com.example.daniel.sia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int OBJECT_LENGTH = 01;

    private Handler mRuntimePermissionsHandler = new Handler();

    private int fileReadPermissionCheck;
    private int fileWritePermissionCheck;
    private int cameraPermissionCheck;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;  // For our internal use
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101; // For our internal use
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 102; // For out internal use

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        mRuntimePermissionsHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getCameraPermissions();
                getFileWritePermissions();
                getFileReadPermissions();
            }
        }, 5);
    }

    public void getFileReadPermissions() {
        fileReadPermissionCheck = ContextCompat.checkSelfPermission(this.getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (fileReadPermissionCheck != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    public void getFileWritePermissions() {
        fileWritePermissionCheck = ContextCompat.checkSelfPermission(this.getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (fileWritePermissionCheck != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    public void getCameraPermissions() {
        cameraPermissionCheck = ContextCompat.checkSelfPermission(this.getBaseContext(), Manifest.permission.CAMERA);
        if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_CAPTURE_IMAGE_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
            case READ_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }

    public void cameraActivity(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("ImageNum", OBJECT_LENGTH);
        startActivity(intent);
        finish();
    }

    public void fullscreenActivity(View view) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
