package com.megaspawn.quickshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.megaspawn.quickshare.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent contentIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(contentIntent);
    }
}
