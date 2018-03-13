package com.example.mdo3.vinylplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.net.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    public static final String LOGIN_USER = "John Doe";
    private ArrayList<String> cookieJar;
    private boolean validCookies;
    private String sessionId = null;
    private String userId = null;
    private static SharedPreferences preferences;
    private static Map<String, List<String>> headerFields;

    private static final int COOKIE_FLAG = 1; //using cookie information
    private static final int USERINFO_FLAG = 2; //using user information

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.log_email);
        mPasswordView = (EditText) findViewById(R.id.log_passwd);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        cookieJar = new ArrayList<>();
        validCookies = false;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /*
    Attempts to sign in the user with the credentials
     */
    private void attemptLogin()
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
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
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password, this);
            mAuthTask.execute((Void) null);
        }
    }


    private boolean isEmailValid(String email)
    {
        boolean endingValid = false;
        boolean hasSymbol = false;

        endingValid = CheckDomains(email);
        hasSymbol = email.contains("@");

        //TODO: Uncomment
        //return (endingValid && hasSymbol);
        return true;
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

        //TODO: uncomment
        //return (hasCap && hasNum && hasSize);
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
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
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
    {

        private final String mEmail;
        private final String mPassword;
        private Context context;

        private UserLoginTask(String email, String password, Context context)
        {
            mEmail = email;
            mPassword = password;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            boolean urlResponse = false;
            HttpURLConnection urlConnection = null;

            try {

                //if user has cookies, login with cookies (contains session Id and user Id)
                //if not, login with username and pass (create cookie from server)
                if(hasCookies())
                {
                    urlConnection = createHttpRequest(mEmail, mPassword, COOKIE_FLAG);
                    validCookies = true;
                }
                else
                {
                    urlConnection = createHttpRequest(mEmail, mPassword, USERINFO_FLAG);
                    validCookies = false;
                }

                if(urlConnection != null)
                {
                    urlConnection.connect();
                    Thread.sleep(2000);
                }
                else
                {
                    return false;
                }

                System.out.println("DEBUG: POST code " + urlConnection.getResponseCode());
                System.out.println(urlConnection.getResponseCode() == urlConnection.HTTP_OK);

                //If user sucessfully log in with cookie, no further action required
                //if user sucessfully log in with username and password, save the cookie information from server
                //if user failed to login with cookie, login with username and pass, save cookie info
                //if user failed to login with username and pass, return to main screen
                if (urlConnection.getResponseCode() == urlConnection.HTTP_OK)
                {
                    /*
                    success: save cookies information
                    success: if it already exist, don't do anything
                     */
                    urlResponse = true;
                    if(!validCookies)
                    {
                        System.out.println("DEBUG: Successful login w/ username and password");
                        headerFields = urlConnection.getHeaderFields();
                        saveCookieInfo(headerFields);
                    }
                    System.out.println("DEBUG: Successful login w/ cookie");
                }
                else
                {
                    urlResponse = false;
                    if(validCookies)
                    {
                        System.out.println("DEBUG: Attempting second try with username and password");
                        urlConnection = createHttpRequest(mEmail,mPassword,USERINFO_FLAG);
                        urlConnection.connect();
                        Thread.sleep(2000);

                        if(urlConnection.getResponseCode() == urlConnection.HTTP_OK)
                        {
                            System.out.println("DEBUG: Successful login 2nd attempt, saving cookie information");
                            urlResponse = true;
                           headerFields = urlConnection.getHeaderFields();
                            saveCookieInfo(headerFields);
                        }
                        else
                        {
                            System.out.println("DEBUG: unable to login ");
                            urlResponse = false;
                        }
                    }
                }
                urlConnection.disconnect();
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
        protected void onPostExecute(final Boolean success)
        {
            mAuthTask = null;
            showProgress(false);

            if (success)
            {
                //Login was successful, bring user to home screen
                finish();
                Intent intent = new Intent(context, MainScreen.class);
                intent.putExtra(LOGIN_USER, mEmailView.getText().toString());
                startActivity(intent);
            }
            else
            {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /** sign in button on the log in page */
    public void signIn(View view) throws InterruptedException
    {
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

    //check for previously saved cookies from the application
    private boolean hasCookies()
    {
        System.out.println("DEBUG: Checking XML");
        sessionId = preferences.getString(getResources().getString(R.string.session_id),null);
        userId = preferences.getString(getResources().getString(R.string.user_id), null);

        if(sessionId != null && userId != null)
        {
            System.out.println("DEBUG: cookies are available");
            return true;
        }
        System.out.println("DEBUG: no cookies available");
        return false;
    }

    //Flag = 1 : create http request with cookie information
    //flag = 2 : create hhtp request with user name and password
    private HttpURLConnection createHttpRequest(String mEmail, String mPassword, int flag)
    {
        try
        {
            //todo: change from local to server
            URL url = new URL(getResources().getString(R.string.http_url_test_login));
            //HttpsURLConnection urlConnection =  (HttpsURLConnection) url.openConnection();
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");

            //Creating http request with cookie
            if(flag == 1)
            {
                StringBuilder str = new StringBuilder();
                str.append(sessionId);
                str.append(";");
                str.append(userId);
                urlConnection.setRequestProperty("Cookie", str.toString());
            }
            //creating http request with username and password
            else if(flag == 2)
            {
                String postParams = null;
                StringBuilder str = new StringBuilder();
                str.append(URLEncoder.encode("email", "UTF-8"));
                str.append("=");
                str.append(URLEncoder.encode(mEmail, "UTF-8"));
                str.append("&");
                str.append(URLEncoder.encode("password", "UTF-8"));
                str.append("=");
                str.append(URLEncoder.encode(mPassword, "UTF-8"));
                postParams = str.toString();

                urlConnection.setRequestProperty("Content-length", String.valueOf(postParams.length()));
                OutputStream outputPost = new BufferedOutputStream((urlConnection.getOutputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));
                writer.write(postParams);
                writer.flush();
                writer.close();
                outputPost.close();
            }
            return urlConnection;
        }
        catch(MalformedURLException error)
        {
            System.err.println("Malformed Problem: " + error);
            return null;
        }
        catch(SocketTimeoutException error)
        {
            System.err.println("Socket Problem: " + error);
            return null;
        }
        catch (IOException error)
        {
            System.err.println("IO Problem: " + error);
            return null;
        }
        catch(Exception e)
        {
            System.err.print("General Problem: " + e);
            return null;
        }
    }

    //Cookie information is stored in....
    //Emulator : View > Tool Window > Device File Explorer
    //Emulator :data is stored inside data > data > com.example.mdo3.vinylplayer > shared_prefs >
    //Emulator: com.example.mdo3.vinylplayer _preferences.xml
    //On physical device...
    //TBD
    private void saveCookieInfo( Map<String, List<String>> headerFields)
    {
        SharedPreferences.Editor editor = preferences.edit();
        List<String> cookieHeaders = headerFields.get(getResources().getString(R.string.cooke_header));

        if (cookieHeaders != null)
        {
            for (String cHeader : cookieHeaders)
            {
                cookieJar.add(cHeader.substring(0, cHeader.indexOf(";")));
            }
        }
        editor.putString(getResources().getString(R.string.session_id), cookieJar.get(0));
        editor.putString(getResources().getString(R.string.user_id), cookieJar.get(1));
        editor.commit();
        System.out.println("DEBUG: Successfully saved Cookie Information");
    }
}
