package com.example.mdo3.vinylplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

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
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class SignUp extends AppCompatActivity
{

    //TODO: save cookie information when user signs in

    private EditText inputEmail = null;
    private EditText inputPassword = null;
    private EditText inputPassword2 = null;
    private EditText name = null;
    private EditText phone = null;
    private EditText address = null;
    private EditText city = null;
    private EditText state = null;
    private UserLoginTask mAuthTask = null;

    private View mProgressView;
    private View mLoginFormView;

    public static final String SIGNUP_USER = "Vinyl User";

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

        mLoginFormView = findViewById(R.id.signup_form);
        mProgressView = findViewById(R.id.signup_progress);
    }

    /*
    Attempts to sign up the user with the credentials
     */
    private void attemptLogin()
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        inputEmail.setError(null);
        inputPassword.setError(null);

        // Store values at the time of the login attempt.
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String password2 = inputPassword2.getText().toString();

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
        if (TextUtils.isEmpty(email)) {
            inputEmail.setError(getString(R.string.error_field_required));
            focusView = inputEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            inputEmail.setError(getString(R.string.error_invalid_email));
            focusView = inputEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password, this);
            mAuthTask.execute((Void) null);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
    {
        private final String mEmail;
        private final String mPassword;
        private Context context;

        public UserLoginTask(String email, String password, Context context) {
            mEmail = email;
            mPassword = password;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean urlResponse = false;

            try {

                StringBuilder str = new StringBuilder();
                str.append(URLEncoder.encode("email", "UTF-8"));
                str.append("=");
                str.append(URLEncoder.encode(mEmail, "UTF-8"));
                str.append("&");
                str.append(URLEncoder.encode("password", "UTF-8"));
                str.append("=");
                str.append(URLEncoder.encode(mPassword, "UTF-8"));
                String postParams = str.toString();

                URL url = new URL("https://vinyl-player-server.herokuapp.com/createUser");
                HttpsURLConnection urlConnection =  (HttpsURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Content-length", String.valueOf(postParams.length()));
                urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");

                OutputStream outputPost = new BufferedOutputStream((urlConnection.getOutputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));
                writer.write(postParams);
                writer.flush();
                writer.close();
                outputPost.close();
                urlConnection.connect();
                Thread.sleep(2000);

                if (urlConnection.getResponseCode() == urlConnection.HTTP_OK)
                    urlResponse = true;
                else
                    urlResponse = false;

                System.out.println("POST code " + urlConnection.getResponseCode());
                System.out.println(urlConnection.getResponseMessage());
                getPostResponse(urlConnection);

            } catch(MalformedURLException error) {
                System.err.println("Malformed Problem: " + error);
                return false;
            } catch(SocketTimeoutException error) {
                System.err.println("Socket Problem: " + error);
                return false;
            } catch (IOException error) {
                System.err.println("IO Problem: " + error);
                return false;
            } catch (InterruptedException e) {
                System.err.print("Interrupted Problem: " + e);
                return false;
            }catch(Exception e) {
                System.err.print("General Problem: " + e);
                return false;
            }

            return urlResponse;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success)
            {
                finish();
                Intent intent = new Intent(context, MainScreen.class);
                intent.putExtra(SIGNUP_USER, inputEmail.getText().toString());

                startActivity(intent);
            }
            else
            {
                inputPassword.setError(getString(R.string.error_incorrect_password));
                inputPassword.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    private boolean isEmailValid(String email) {
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

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    //Sign up button located on the signup activity
    public void btn_signup(View view)
    {
        System.out.println("signup activity: signup button has been pressed");
        attemptLogin();
    }

    //Cancel button located on the signup activity
    public void btn_cancel(View view)
    {
        Intent intent = new Intent(this, Login.class);
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
}
