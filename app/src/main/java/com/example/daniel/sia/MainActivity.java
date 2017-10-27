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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int OBJECT_LENGTH = 01;

    private Handler mRuntimePermissionsHandler = new Handler();

    private EditText batchNumEditText;
    private EditText cargoNumEditText;
    private CheckBox stackableCheckBox;
    private CheckBox tiltableCheckBox;

    private boolean isStackable;
    private boolean isTiltable;
    private int numOfCargo;
    private int cargoRemaining;
    private int batchNum;

    private int fileReadPermissionCheck;
    private int fileWritePermissionCheck;
    private int cameraPermissionCheck;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;  // For our internal use
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101; // For our internal use
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 102; // For out internal use


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        batchNumEditText = (EditText)findViewById(R.id.batch_num_edit_text);
        cargoNumEditText = (EditText)findViewById(R.id.cargo_num_edit_text);
        stackableCheckBox = (CheckBox)findViewById(R.id.stackable_check_box);
        tiltableCheckBox = (CheckBox)findViewById(R.id.tiltable_check_box);
        /*batchNumEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null) {
                    batchNum = Integer.valueOf(String.valueOf(editable));
                }
            }
        });
        cargoNumEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null) {
                    numOfCargo = Integer.getInteger(String.valueOf(editable));
                }
            }
        });*/

        mRuntimePermissionsHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getCameraPermissions();
                getFileWritePermissions();
                getFileReadPermissions();
            }
        }, 100);
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
        batchNumEditText = (EditText)findViewById(R.id.batch_num_edit_text);
        cargoNumEditText = (EditText)findViewById(R.id.cargo_num_edit_text);
        Editable temp = batchNumEditText.getText();
        batchNum = Integer.valueOf(String.valueOf(temp));
        intent.putExtra("batchNum", batchNum);
        intent.putExtra("ImageNum", OBJECT_LENGTH);
        temp = cargoNumEditText.getText();
        numOfCargo = Integer.valueOf(String.valueOf(temp));
        intent.putExtra("numOfCargo", numOfCargo);
        intent.putExtra("cargoRemaining", numOfCargo);
        intent.putExtra("isTiltable", tiltableCheckBox.isChecked());
        intent.putExtra("isStackable", stackableCheckBox.isChecked());
        startActivity(intent);
        finish();
    }

    public void fullscreenActivity(View view) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        startActivity(intent);
        finish();
    }
}
