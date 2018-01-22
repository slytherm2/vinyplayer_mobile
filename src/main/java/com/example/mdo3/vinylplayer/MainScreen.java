package com.example.mdo3.vinylplayer;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //get the intent of the previous activity
        Intent intent = getIntent();

        //get the user email from the previous activity (login/signup)
        String user = null;
        if(Login.LOGIN_USER == null)
            user = intent.getStringExtra(SignUp.SIGNUP_USER);
        else
            user = intent.getStringExtra(Login.LOGIN_USER);

        //creates a new title with "welcome" and "user email before the @"
        String tempStr = user.substring(0,user.indexOf('@'));
        String newTitle = getString(R.string.label_Welcome) + tempStr;

        //update title to reflect user "welcome ...user"
        TextView title = (TextView) findViewById(R.id.main_title);
        title.setText(newTitle);

        Button btn = findViewById(R.id.main_stateBTN);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
    }
}
