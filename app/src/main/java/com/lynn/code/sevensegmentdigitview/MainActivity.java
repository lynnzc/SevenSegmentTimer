package com.lynn.code.sevensegmentdigitview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lynn.code.sevensegmenttimer.CountDownDigitTimer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final CountDownDigitTimer v = (CountDownDigitTimer) findViewById(R.id.digit_view);

        v.setCountDownTime(99, 59, 59);
    }
}
