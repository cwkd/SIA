package com.example.daniel.sia;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        /*if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }*/
    }

   /* private Camera mCamera;
    private CamPreview mPreview;

    private FrameLayout previewFrame;
    private Handler mHandler = new Handler();


    int permissionCheck;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        /*mHandler.postDelayed(new Runnable() {
            public void run() {
                createCamPreview();
            }
        }, 1000);*/
    }

    public void cameraActivity(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);

    }

    public void fullscreenActivity(View view) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        startActivity(intent);
    }

    /*public void createCamPreview() {
        if (checkCameraHardware(this)) {
            permissionCheck = ContextCompat.checkSelfPermission(this.getBaseContext(), Manifest.permission.CAMERA);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED
                    && Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }
            mCamera = getCameraInstance();
            if (mCamera != null) {
                mPreview = new CamPreview(this, mCamera);
                previewFrame = (FrameLayout) findViewById(R.id.camera_preview);
                previewFrame.addView(mPreview);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            Toast.makeText(this, "Camera found.", Toast.LENGTH_LONG).show();
            return true;
        } else {
            // no camera on this device
            Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public void captureButton(View view) {
        try {
            mCamera.takePicture(null, null, captureCallback);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    Camera.PictureCallback captureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

        }
    };*/

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
