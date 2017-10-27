package com.example.daniel.sia;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CameraActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */

    private Camera mCamera;
    private CamPreview mPreview;
    private File pictureFile;
    private Uri pictureUri;

    private FrameLayout previewFrame;

    private boolean isStackable;
    private boolean isTiltable;
    private int numOfCargo;
    private int cargoRemaining;
    private int currentImageCount;
    private int batchNum;

    private boolean deletePreviousImage;

    private static final String TAG = "CameraActivity";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final int OBJECT_LENGTH = 01;
    private static final int OBJECT_BASE = 02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Intent intent = getIntent();

        isStackable = intent.getBooleanExtra("isStackable", false);
        isTiltable = intent.getBooleanExtra("isTiltable", false);
        numOfCargo = intent.getIntExtra("numOfCargo", 0);
        cargoRemaining = intent.getIntExtra("cargoRemaining", 0);
        batchNum = intent.getIntExtra("batchNum", 123456);

        currentImageCount = intent.getIntExtra("ImageNum", OBJECT_LENGTH);
        deletePreviousImage = intent.getBooleanExtra("deletePrevious", false);
        if (deletePreviousImage) {
            pictureUri = intent.getParcelableExtra("deleteFile");
            new DeleteTask().execute(pictureUri);
        }
        createCamPreview();
    }

    public void createCamPreview() {
        if (checkCameraHardware(this)) {
            mCamera = getCameraInstance();
            if (mCamera != null) {
                mPreview = new CamPreview(this, mCamera);
                previewFrame = (FrameLayout) findViewById(R.id.fullscreen_content);
                previewFrame.addView(mPreview);
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

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // attempt to get a back-facing Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        if (c != null) {
            setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, c);
        }
        return c; // returns null if camera is unavailable
    }

    public void captureButton(View view) {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setRotation(0);
            mCamera.setParameters(parameters);
            mCamera.takePicture(null, null, captureCallback);
            Toast.makeText(this, "Image Captured", Toast.LENGTH_SHORT).show();
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCamera = getCameraInstance();
        if (mCamera != null) {
            mPreview = new CamPreview(this, mCamera);
            previewFrame = (FrameLayout) findViewById(R.id.fullscreen_content);
            previewFrame.addView(mPreview);
        }
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    Camera.PictureCallback captureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            pictureUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            if (pictureFile == null || pictureUri == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                OutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bytes);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            verifyImage();
        }
    };

    public void verifyImage() {
        Intent intent = new Intent(getBaseContext(), ImageVerificationActivity.class);
        intent.putExtra("batchNum", batchNum);
        intent.putExtra("isStackable", isStackable);
        intent.putExtra("isTiltable", isTiltable);
        intent.putExtra( "numOfCargo", numOfCargo);
        intent.putExtra("cargoRemaining", cargoRemaining);
        intent.putExtra("pictureUri", pictureUri);
        if (currentImageCount == OBJECT_LENGTH) {
            intent.putExtra("ImageNum", OBJECT_LENGTH);
        } else if (currentImageCount == OBJECT_BASE)
            intent.putExtra("ImageNum", OBJECT_BASE);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private class DeleteTask extends AsyncTask<Uri, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Uri... uris) {
            File deleteCandidate = new File(uris[0].getPath());
            if (deleteCandidate.exists()){
                if (deleteCandidate.delete()){
                    Log.d(TAG, "File Deleted");
                    return true;
                } else {
                    Log.d(TAG, "Delete Failed");
                }
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getBaseContext(), "File Delete Successful", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(getBaseContext(), "File Delete Failed", Toast.LENGTH_SHORT);
            }
        }
    }

    private Bitmap checkRotation(String filePath, Bitmap scaledBitmap) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scaledBitmap;
    }
}
