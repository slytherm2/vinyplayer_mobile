package com.example.mdo3.vinylplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class SignUp extends AppCompatActivity
{
    private EditText inputEmail = null;
    private EditText inputPassword = null;
    private EditText inputPassword2 = null;
    private EditText name = null;
    private EditText phone = null;
    private EditText address = null;
    private EditText city = null;
    private EditText state = null;

    private View mLoginFormView;
    private Toolbar mTopToolbar;
    private String httpURL = null;
    private Boolean result;

    private String email;
    private String password;
    private String password2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        inputEmail = (EditText) findViewById(R.id.signup_email);
        inputPassword = (EditText) findViewById(R.id.signup_pass);
        inputPassword2 = (EditText) findViewById(R.id.signup_pass2);
        name = (EditText) findViewById(R.id.signup_name);
        phone = (EditText) findViewById(R.id.signup_phone);
        address = (EditText) findViewById(R.id.signup_address);
        city = (EditText) findViewById(R.id.signup_city);
        state = (EditText) findViewById(R.id.signup_state);
        httpURL = this.getResources().getString(R.string.https_url_createuser);

        mLoginFormView = findViewById(R.id.signup_form);
        ApplicationContext contextInst = ApplicationContext.getInstance();
        contextInst.setAppContext(this);

        //adding tool bar with back arrow to go back to activity
        //it goes to the activity listed in the android manifest
        mTopToolbar = (Toolbar) findViewById(R.id.signup_toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /*
    Attempts to sign up the user with the credentials
     */
    private void attemptLogin()
    {
        // Reset errors.
        inputEmail.setError(null);
        inputPassword.setError(null);

        // Store values at the time of the login attempt.
        email = inputEmail.getText().toString();
        password = inputPassword.getText().toString();
        password2 = inputPassword2.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password,password2))
        {
            inputPassword.setError("Password must have 1 Capital Letter, 1 number, more than 4 characters and less than 15");
            focusView = inputPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            inputEmail.setError(getString(R.string.error_field_required));
            focusView = inputEmail;
            cancel = true;
        } else if (!isEmailValid(email))
        {
            inputEmail.setError(getString(R.string.error_invalid_email));
            focusView = inputEmail;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            final CountDownLatch latch = new CountDownLatch(1);
            result = false;
            try {
                Thread thread1 = new HandlerThread("SignUp")
                {
                    AsyncTaskFactory factory = new AsyncTaskFactory();
                    AsyncTask signUpTask = factory.generateAsyncTask("SignUp");
                    String[] params = {email, password, httpURL};

                    @Override
                    public void run()
                    {
                        try
                        {
                            result = (Boolean) signUpTask.execute(params).get();
                            latch.countDown();
                        } catch (ExecutionException ex) {
                            Log.d("Exception", ex.getMessage());
                            result = false;
                            latch.countDown();
                        } catch (InterruptedException ex) {
                            Log.d("Exception", ex.getMessage());
                            result = false;
                            latch.countDown();
                        }
                    }
                };
                thread1.start();
                latch.await();
            }
            catch(InterruptedException ex)
            {
                Log.d("Exception", ex.getMessage());
            }

            if(result)
            {
                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                Resources rsrc = this.getResources();

                String userTag = email;
                editor.putString(rsrc.getString(R.string.signup_email), email);
                editor.putString(userTag + rsrc.getString(R.string.signup_address),
                        address.getText().toString());
                editor.putString(userTag + rsrc.getString(R.string.signup_name),
                        name.getText().toString());
                editor.putString(userTag + rsrc.getString(R.string.signup_city),
                        city.getText().toString());
                editor.putString(userTag + rsrc.getString(R.string.signup_state),
                        state.getText().toString());
                editor.putString(userTag + rsrc.getString(R.string.signup_phone),
                        phone.getText().toString());
                editor.commit();

                Intent intent = new Intent(this, MainScreen.class);
                startActivity(intent);
            }
            else
            {
                Toast toast = Toast.makeText(this, R.string.signup_failed, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private boolean isEmailValid(String email)
    {
        boolean endingValid = false;
        boolean hasSymbol = false;

        endingValid = CheckDomains(email);
        hasSymbol = email.contains("@");

        return (endingValid && hasSymbol);
    }

    private boolean isPasswordValid(String password, String password2) {

        boolean hasCap = false;
        boolean hasNum = false;
        boolean hasSize = false;
        boolean samePass = false;

        if (password.length() > 4 && password.length() < 15)
            hasSize = true;

        if(password.equals(password2))
            samePass = true;

        for (int i=0; i < password.length(); i++)
        {
            int temp = (int) password.charAt(i);

            if (temp >= 65 && temp <= 90)
                hasCap = true;
            if (temp >= 48 && temp <= 57)
                hasNum = true;
        }

        return (hasCap && hasNum && hasSize && samePass);
    }

    public boolean CheckDomains(String temp)
    {
        String[] domains = {
                "aol.com", "att.net", "comcast.net", "facebook.com", "gmail.com", "gmx.com", "googlemail.com",
                "google.com", "hotmail.com", "hotmail.co.uk", "mac.com", "me.com", "mail.com", "msn.com",
                "live.com", "sbcglobal.net", "verizon.net", "yahoo.com", "yahoo.co.uk",
                "email.com", "fastmail.fm", "games.com", "gmx.net", "hush.com", "hushmail.com", "icloud.com",
                "iname.com", "inbox.com", "lavabit.com", "love.com", "outlook.com", "pobox.com", "protonmail.com",
                "rocketmail.com", "safe-mail.net", "wow.com", "ygm.com" /* AOL */,
                "ymail.com", "zoho.com", "yandex.com",
                "bellsouth.net", "charter.net", "cox.net", "earthlink.net", "juno.com",
                "btinternet.com", "virginmedia.com", "blueyonder.co.uk", "freeserve.co.uk", "live.co.uk",
                "ntlworld.com", "o2.co.uk", "orange.net", "sky.com", "talktalk.co.uk", "tiscali.co.uk",
                "virgin.net", "wanadoo.co.uk", "bt.com",
                "sina.com", "qq.com", "naver.com", "hanmail.net", "daum.net", "nate.com", "yahoo.co.jp", "yahoo.co.kr", "yahoo.co.id", "yahoo.co.in", "yahoo.com.sg", "yahoo.com.ph",
                "hotmail.fr", "live.fr", "laposte.net", "yahoo.fr", "wanadoo.fr", "orange.fr", "gmx.fr", "sfr.fr", "neuf.fr", "free.fr",
                "gmx.de", "hotmail.de", "live.de", "online.de", "t-online.de" /* T-Mobile */, "web.de", "yahoo.de",
                "libero.it", "virgilio.it", "hotmail.it", "aol.it", "tiscali.it", "alice.it", "live.it", "yahoo.it", "email.it", "tin.it", "poste.it", "teletu.it",
                "mail.ru", "rambler.ru", "yandex.ru", "ya.ru", "list.ru",
                "hotmail.be", "live.be", "skynet.be", "voo.be", "tvcablenet.be", "telenet.be",
                "hotmail.com.ar", "live.com.ar", "yahoo.com.ar", "fibertel.com.ar", "speedy.com.ar", "arnet.com.ar",
                "yahoo.com.mx", "live.com.mx", "hotmail.es", "hotmail.com.mx", "prodigy.net.mx",
                "yahoo.com.br", "hotmail.com.br", "outlook.com.br", "uol.com.br", "bol.com.br", "terra.com.br", "ig.com.br", "itelefonica.com.br", "r7.com", "zipmail.com.br", "globo.com", "globomail.com", "oi.com.br",
                ".edu"
        };

        for (String str : domains)
        {
            if(temp.endsWith(str))
            {
                return true;
            }
        }
        return false;
    }

    //Sign up button located on the signup activity
    public void btn_signup(View view)
    {
        attemptLogin();
    }

    //Cancel button located on the signup activity
    public void btn_cancel(View view)
    {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    /*
    private void getPostResponse(URLConnection conn)
    {
        String inputLine = null;
        BufferedReader in = null;
        StringBuffer response = null;

        try
        {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch(IOException e)
        {
            System.err.print("IO Exception w/ Post Response " + e);
        }

        System.out.println("Response: " + response.toString());
    }
    */
}
