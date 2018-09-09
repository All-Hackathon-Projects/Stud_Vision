package com.pennappsxviii.visualize;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LandingPageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
    }

    public void top(View view) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.bhai.bhaidekh");
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    public void bottom(View view) {
        Intent intent = new Intent(LandingPageActivity.this, MainActivity.class);
        LandingPageActivity.this.startActivity(intent);
    }
}
