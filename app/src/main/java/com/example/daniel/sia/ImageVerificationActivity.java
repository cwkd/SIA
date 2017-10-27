package com.example.daniel.sia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageVerificationActivity extends AppCompatActivity {

    public Uri pictureUri;

    private int batchNum;
    private boolean isStackable;
    private boolean isTiltable;
    private int numOfCargo;
    private int cargoRemaining;
    private int currentImageCount;

    private static final String TAG = "ImageVerActivity";
    private static final int OBJECT_LENGTH = 01;
    private static final int OBJECT_BASE = 02;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_verification);
        Intent intent = getIntent();
        isStackable = intent.getBooleanExtra("isStackable", false);
        isTiltable = intent.getBooleanExtra("isTiltable", false);
        numOfCargo = intent.getIntExtra("numOfCargo", 0);
        cargoRemaining = intent.getIntExtra("cargoRemaining", 0);
        batchNum = intent.getIntExtra("batchNum", 123456);

        pictureUri = intent.getParcelableExtra("pictureUri");
        currentImageCount = intent.getIntExtra("ImageNum", OBJECT_LENGTH);
        if (currentImageCount == OBJECT_LENGTH) {
            Toast.makeText(getBaseContext(), "Object length taken", Toast.LENGTH_SHORT).show();
        } else if (currentImageCount == OBJECT_BASE) {
            Toast.makeText(getBaseContext(), "Object base taken", Toast.LENGTH_SHORT).show();
        }

        ImageView imageView = (ImageView) findViewById(R.id.image_preview_view);
        Bitmap bitmap = getBitmap(pictureUri);
        imageView.setImageBitmap(bitmap);
        imageView.setRotation(90);
    }

    protected Bitmap getBitmap(Uri uri) throws NullPointerException {
        Bitmap bitmap = null;
        try {
            File imgFile = new File(uri.getPath());
            if (imgFile.exists()) {
                bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            } else {
                throw new NullPointerException("Image file does not exist");
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
        if (bitmap != null) {
            return bitmap;
        } else {
            throw new NullPointerException("Bitmap is null");
        }
    }

    public void tryAgain(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("batchNum", batchNum);
        intent.putExtra("isStackable", isStackable);
        intent.putExtra("isTiltable", isTiltable);
        intent.putExtra( "numOfCargo", numOfCargo);
        intent.putExtra("cargoRemaining", cargoRemaining);
        if (currentImageCount == OBJECT_LENGTH) {
            intent.putExtra("ImageNum", OBJECT_LENGTH);
        } else if (currentImageCount == OBJECT_BASE) {
            intent.putExtra("ImageNum", OBJECT_BASE);
        }
        intent.putExtra("deleteFile", pictureUri);
        intent.putExtra("deletePrevious", true);
        startActivity(intent);
        finish();
    }

    public void acceptImage(View view) {
//        saveCannyImage(pictureUri);
        new SaveCannyImageTask().execute(pictureUri);
        new SaveBoxImageTask().execute(pictureUri);
        if (currentImageCount == OBJECT_LENGTH){
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("batchNum", batchNum);
            intent.putExtra("isStackable", isStackable);
            intent.putExtra("isTiltable", isTiltable);
            intent.putExtra( "numOfCargo", numOfCargo);
            intent.putExtra("cargoRemaining", cargoRemaining);
            intent.putExtra("ImageNum", OBJECT_BASE);
            startActivity(intent);
            finish();
        } else if (currentImageCount == OBJECT_BASE) {
            Intent intent = new Intent(this, VolumeActivity.class);
            intent.putExtra("batchNum", batchNum);
            intent.putExtra("isStackable", isStackable);
            intent.putExtra("isTiltable", isTiltable);
            intent.putExtra( "numOfCargo", numOfCargo);
            intent.putExtra("cargoRemaining", cargoRemaining);
            startActivity(intent);
            finish();
        }
    }

/*    *//** Create a file Uri for saving an image or video *//*
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }*/

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveCannyImage(Uri uri) {
        Bitmap bitmap = ImageProcessor.getBitmapFromFile(uri);
        try {
            Utils.matToBitmap(ImageProcessor.getCannyImage(bitmap), bitmap);
        } catch (Exception e) {
            return;
        }
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        } else {
            try {
                OutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    }

    private void saveBoxImage(Uri uri){
        Bitmap bitmap = ImageProcessor.getBitmapFromFile(uri);
        Utils.matToBitmap(ImageProcessor.getContoursImage(bitmap), bitmap);
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        } else {
            try {
                OutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    }

    private double[] getDimensions(Uri uri) {
        Bitmap bitmap = ImageProcessor.getBitmapFromFile(uri);
        try {
            return ImageProcessor.getDimensions(bitmap);
        } catch (Exception e) {
            return null;
        }
    }

    private class SaveBoxImageTask extends AsyncTask<Uri, Void, Void>{

        @Override
        protected Void doInBackground(Uri... uris) {
            saveBoxImage(uris[0]);
            return null;
        }
    }

    private class SaveCannyImageTask extends AsyncTask<Uri, Void, Void>{

        @Override
        protected Void doInBackground(Uri... uris) {
            saveCannyImage(uris[0]);
            return null;
        }
    }
}


