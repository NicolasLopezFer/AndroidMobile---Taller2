package com.example.taller2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    ImageButton btnContacts, btnCamera, btnMaps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inflate
        btnContacts = findViewById(R.id.btnContacts);
        btnCamera = findViewById(R.id.openCamera);
        btnMaps = findViewById(R.id.btnMaps);


        btnContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),ShowContactsActivity.class);
                startActivity(intent);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),ShowImageActivity.class);
                startActivity(intent);
            }
        });

        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}