package com.GhostlyRunes;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


public class StarPatternActivity extends PatternActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PATTERNID=0;

        setContentView(R.layout.activity_star_pattern);

        splat = (ImageView) findViewById(R.id.splat);
        splat.setAlpha(0.0f);

        int[] pattern = new int[5];
        pattern[0]=R.id.Point1;
        pattern[1]=R.id.Point2;
        pattern[2]=R.id.Point3;
        pattern[3]=R.id.Point4;
        pattern[4]=R.id.Point5;

        View view = findViewById(R.id.activity_star_pattern);

        touch= new TouchHandler(this, TOUCHID);

        view.setOnTouchListener(touch);

        touch.setPattern(pattern);

    }


}
