package com.example.mdo3.vinylplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SignUp extends AppCompatActivity {

    private View logPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    //Sign up button located on the signup activity
    public void btn_signup(View view)
    {
        System.out.println("signup activity: signup button has been pressed");
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }

    //Cancel button located on the signup activity
    public void btn_cancel(View view)
    {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}
