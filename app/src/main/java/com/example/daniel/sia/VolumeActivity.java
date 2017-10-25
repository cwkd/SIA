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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume);

        Intent intent = getIntent();
        volumeActual = intent.getDoubleExtra("volumeActual", 10.70);
        dimensionsActual = intent.getDoubleArrayExtra("dimensionsActual");
        volumeMessage = String.format("The volume of the object is %f.6", volumeActual);
        textView = (TextView)findViewById(R.id.volume_text_view);
        textView.setText(volumeMessage);
    }

    public void backToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
