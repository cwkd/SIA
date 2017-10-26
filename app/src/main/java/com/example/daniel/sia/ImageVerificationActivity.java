package com.example.daniel.sia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class ImageVerificationActivity extends AppCompatActivity {

    public Uri pictureUri;
    public Bitmap bitmap;

    int currentImageCount;
    int filePermissionCheck;

    ImageView imageView;

    private static final String TAG = "ImageVerActivity";
    private static final int OBJECT_LENGTH = 01;
    private static final int OBJECT_BASE = 02;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_verification);
        Intent intent = getIntent();
        pictureUri = intent.getParcelableExtra("pictureUri");
        currentImageCount = intent.getIntExtra("ImageNum", OBJECT_LENGTH);
        if (currentImageCount == OBJECT_LENGTH) {
            Toast.makeText(getBaseContext(), "Object length taken", Toast.LENGTH_SHORT).show();
        } else if (currentImageCount == OBJECT_BASE) {
            Toast.makeText(getBaseContext(), "Object base taken", Toast.LENGTH_SHORT).show();
        }

        imageView = (ImageView) findViewById(R.id.image_preview_view);
        bitmap = getBitmap(pictureUri);
        imageView.setImageBitmap(bitmap);
        imageView.setRotation(90);
    }

    public void getFileWritePermissions() {
        filePermissionCheck = ContextCompat.checkSelfPermission(this.getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (filePermissionCheck != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
        }
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
        if (currentImageCount == OBJECT_LENGTH) {
            intent.putExtra("ImageNum", OBJECT_LENGTH);
        } else if (currentImageCount == OBJECT_BASE) {
            intent.putExtra("ImageNum", OBJECT_BASE);
        }
        /*File deleteFile = new File(String.valueOf(pictureUri));
        getFileWritePermissions();
        if (deleteFile.exists()) {
            getBaseContext().deleteFile(deleteFile.getAbsolutePath());
            Log.d(TAG, deleteFile.getAbsolutePath());
        }*/
        intent.putExtra("deletedFile", pictureUri);
        intent.putExtra("deletePrevious", true);
        startActivity(intent);
        finish();
    }

    public void acceptImage(View view) {
        if (currentImageCount == OBJECT_LENGTH){
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("ImageNum", OBJECT_BASE);
            startActivity(intent);
            finish();
        } else if (currentImageCount == OBJECT_BASE) {
            Intent intent = new Intent(this, VolumeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        finish();
    }
}


