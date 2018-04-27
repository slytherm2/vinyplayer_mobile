package com.example.mdo3.vinylplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * A login screen that offers login via email/password.
 * Default location of shared preferences
 * View -> tool windows -> Device File Explorer
 *data/data/YOUR_PACKAGE_NAME/shared_prefs/YOUR_PACKAGE_NAME_preferences.xml
 */

public class Login extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AsyncTaskFactory factory = null;
    private AsyncTask loginTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    public static final String LOGIN_USER = "John Doe";
    private String sessionId = null;
    private String userId = null;
    private static SharedPreferences preferences;

    private final String COOKIEFLAG = "cookie";
    private final String NOCOOKIEFLAG = "noCookie";
    private final boolean DEBUG = true;
    private String httpURL;

    private Boolean result;
    private  ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.log_email);
        mPasswordView = (EditText) findViewById(R.id.log_passwd);
        mLoginFormView = findViewById(R.id.login_form);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        factory = new AsyncTaskFactory();
        httpURL = getResources().getString(R.string.https_url_login);
        ApplicationContext appContext = ApplicationContext.getInstance();
        appContext.setAppContext(this);

        //if user already has valid cookie
        //automatically sign user into application
//        pb = (ProgressBar) findViewById(R.id.login_progress);
        isLoggedIn();
        //startNextActivity();
    }

    /*
    Attempts to sign in the user with the credentials
     */
    private void attemptLogin()
    {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (password.isEmpty() && !isPasswordValid(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email))
        {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
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
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (email != null && password != null)
            {
                startBackgroundTask(email, password);
            }
        }
    }

    private void startBackgroundTask(String email, String password)
    {
        if(authenticateUser(NOCOOKIEFLAG, email, password))
        {
            //save user email after successful login
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(this.getResources().getString(R.string.label_email), email);
            if(editor.commit())
            {
                startNextActivity();
            }
            else
            {
//                pb.setVisibility(View.INVISIBLE);
                return;
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

    private boolean isPasswordValid(String password)
    {

        boolean hasCap = false;
        boolean hasNum = false;
        boolean hasSize = false;

        if (password.length() > 4 && password.length() < 15)
            hasSize = true;

        for (int i=0; i < password.length(); i++)
        {
            int temp = (int) password.charAt(i);

            if (temp >= 65 && temp <= 90)
                hasCap = true;
            if (temp >= 48 && temp <= 57)
                hasNum = true;
        }
        return (hasCap && hasNum && hasSize);
    }

    /** sign in button on the log in page */
    public void signIn(View view) throws InterruptedException
    {
        //pb.setVisibility(View.VISIBLE);
        attemptLogin();
    }

    /** Sign up button on the log in page */
    public void signUp(View view)
    {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
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

    private void startNextActivity()
    {
       // pb.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }

    private boolean authenticateUser(String flag, String userIdEmail, String userSessionPass)
    {
        try
        {
            String[] params = {flag, userIdEmail, userSessionPass, httpURL};
            loginTask = factory.generateAsyncTask("Login", this);
            result = (boolean) loginTask.execute(params).get();
            System.out.println(DEBUG?"DEBUG: Authenticate user " + result:"");
        }
        catch (InterruptedException e)
        {
            System.out.println(DEBUG?"DEBUG: " + e : "");
            return false;
        }
        catch(ExecutionException e)
        {
            System.out.println(DEBUG?"DEBUG: " + e : "");
            return false;
        }
        return result;
    }

    private void showError()
    {
        Context context = ApplicationContext.getInstance().getAppContext();
        Toast toast = Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void isLoggedIn()
    {
        //Toast.makeText(this, R.string.loading_msg, Toast.LENGTH_LONG).show();
        if(Utils.hasCookies(this))
        {
            sessionId = preferences.getString(getResources().getString(R.string.session_id), null);
            userId = preferences.getString(getResources().getString(R.string.user_id), null);

            System.out.println(DEBUG?"DEBUG: Creating Task":"");
            loginTask = factory.generateAsyncTask("Login", this);
            if(loginTask != null)
            {

                System.out.println(DEBUG?"DEBUG: Task successfully created":"");
                if (sessionId != null && userId != null && !sessionId.isEmpty() && !userId.isEmpty())
                {
                    System.out.println(DEBUG?"DEBUG: Authenticating cookies":"");
                    if(authenticateUser(COOKIEFLAG, userId, sessionId))
                    {
                        System.out.println(DEBUG?"DEBUG: User Authenticated":"");
                        startNextActivity();
                    }
                    else
                    {
                        return;
                    }
                }
            }
        }
        return;
    }
}
