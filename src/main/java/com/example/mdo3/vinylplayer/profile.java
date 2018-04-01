package com.example.mdo3.vinylplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class profile extends AppCompatActivity
{
    private Toolbar mTopToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //adding tool bar with back arrow to go back to activity
        //it goes to the activity listed in the android manifest
        mTopToolbar = (Toolbar) findViewById(R.id.profile_tool);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    //Callback method for the back arrow on the tool bar
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}
