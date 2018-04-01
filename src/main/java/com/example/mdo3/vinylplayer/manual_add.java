package com.example.mdo3.vinylplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class manual_add extends AppCompatActivity
{
    private Toolbar mTopToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_add);

        //adding tool bar with back arrow to go back to activity
        //it goes to the activity listed in the android manifest
        mTopToolbar = (Toolbar) findViewById(R.id.manual_add_toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
