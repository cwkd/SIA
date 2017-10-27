package com.example.daniel.sia;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class VolumeActivity extends AppCompatActivity {

    private String volumeMessage;
    private double volumeActual;
    private double[] dimensionsActual;

    private TextView textView;

    private int batchNum;
    private boolean isStackable;
    private boolean isTiltable;
    private int numOfCargo;
    private int cargoRemaining;
    private String cargoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume);

        Intent intent = getIntent();
        isStackable = intent.getBooleanExtra("isStackable", false);
        isTiltable = intent.getBooleanExtra("isTiltable", false);
        numOfCargo = intent.getIntExtra("numOfCargo", 0);
        cargoRemaining = intent.getIntExtra("cargoRemaining", 0);
        batchNum = intent.getIntExtra("batchNum", 123456);


        cargoId = String.valueOf(batchNum) + "-" + String.valueOf(numOfCargo) + "-" + String.valueOf(numOfCargo - cargoRemaining);
        volumeActual = intent.getDoubleExtra("volumeActual", 10.70);
        dimensionsActual = intent.getDoubleArrayExtra("dimensionsActual");
        volumeMessage = String.format("The volume of object %s is %f", cargoId ,volumeActual);
        textView = (TextView)findViewById(R.id.volume_text_view);
        textView.setText(volumeMessage);
    }

    public void backToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void continueButton(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("batchNum", batchNum);
        intent.putExtra("isStackable", isStackable);
        intent.putExtra("isTiltable", isTiltable);
        intent.putExtra( "numOfCargo", numOfCargo);
        cargoRemaining--;
        intent.putExtra("cargoRemaining", cargoRemaining);
        startActivity(intent);
    }
}
