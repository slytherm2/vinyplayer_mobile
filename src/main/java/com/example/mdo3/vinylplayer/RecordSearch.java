package com.example.mdo3.vinylplayer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.util.ArrayList;
import org.json.*;

import javax.net.ssl.HttpsURLConnection;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class RecordSearch extends AppCompatActivity {
    private static final int COOKIE_FLAG = 1; //using cookie information
    private static final int USERINFO_FLAG = 2; //using user information
    private static final int HTTP_TIMEOUT = 10000; //10 seconds
    private static final int THREAD_TIMEOUT = 2000;

    boolean errorFlag = false;
    private String sessionId = null;
    private String userId = null;
    private static SharedPreferences preferences;
    private boolean validCookies;

    ArrayList<String> records;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_search);

        // dynamically add action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        validCookies = false;

        // get record listview
        ListView recordResults = (ListView) findViewById(R.id.record_results);
        records = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, records);
        recordResults.setAdapter(adapter);



        // get userId & sessionId from Main Screen
        Bundle extras = getIntent().getExtras();
        if(extras == null) { return; }
        userId = extras.getString("userId");
        sessionId = extras.getString("sessionId");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.record_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        handleIntent(getIntent());
        return true;
    }

    public void addRecords(JSONArray records)
    {
        for(int i=0; i<records.length(); i++) {
            try {
                this.addRecord(records.getJSONObject(i));
            } catch (JSONException e) {
                return;
            }
        }
        return;
    }

    private void addRecord(JSONObject record)
    {
        try{
            String artist = record.getString("artist");
            String album = record.getString("album");
        }
        catch (JSONException e) { return; }

        this.records.add("");
        this.adapter.notifyDataSetChanged();
        return;
    }

    // Get the intent, verify the action and get the query
    private void handleIntent(Intent intent)
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {

            String query = intent.getStringExtra(SearchManager.QUERY);
            String queries[] = query.split("-");
            String artist = queries[0];
            String album = queries[1];
            Log.d("onNewIntent", "handleIntent called");
            //createHttpRequest(artist, album, true);
            SearchTask searchTask = new SearchTask(query, new AsyncResponse(){
                @Override
                void processFinish(String output)
                {
                    return;
                }
            });
            searchTask.execute((Void) null);
        }
    }

    //Flag = 1 : create http request with cookie information
    //flag = 2 : create hhtp request with user name and password
    private HttpURLConnection createHttpRequest(String discogsQuery, boolean hasCookies)
    {
        try
        {
            URL url = new URL(getResources().getString(R.string.http_url_test_search_jose));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");
            connection.setRequestProperty( "Content-Length", String.valueOf(discogsQuery.length()));
            connection.setRequestProperty("Cookie", sessionId+";"+userId);
            return connection;
        }
        catch(MalformedURLException error)
        {
            Log.d("createHttpRequest","Malformed Problem: " + error);
            return null;
        }
        catch(SocketTimeoutException error)
        {
            Log.d("createHttpRequest","Socket Problem: " + error);
            errorFlag = true;
            return null;
        }
        catch (IOException error)
        {
            Log.d("createHttpRequest","IO Problem: " + error);
            return null;
        }
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


    public class SearchTask extends AsyncTask<Void, Void, String>
    {
        private final String artist;
        private final String album;
        private final String query;
        private Context context;

        public interface AsyncResponse { void processFinish(String output);}
        public AsyncResponse delegate = null;

        private SearchTask(String query, AsyncResponse delegate)
        {
            this.delegate = delegate;
            this.query = query;
            String queries[] = query.split("-");
            this.artist = queries[0];
            this.album = queries[1];
        }

        @Override
        protected String doInBackground(Void... voids) {
            boolean urlResponse = false;
            HttpURLConnection connection = null;

            try
            {
                String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
                String discogsQuery = String.format("artist=%s&album=%s",
                        URLEncoder.encode(artist, charset),
                        URLEncoder.encode(album, charset));
                // if user has cookies, login with cookies (contains sessionId & userId)
                // if not, then do something else
                if(hasCookies())
                {
                    connection = createHttpRequest(discogsQuery, true);
                    validCookies = true;
                }
                else
                {
                    connection = createHttpRequest(discogsQuery, false);
                    validCookies = false;
                }

                if(connection == null )
                {
                    Log.d("doInBackground", "connection is null");
                    return null;
                }

                OutputStream output = null;
                InputStream reader = null;

                output = new BufferedOutputStream(connection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
                writer.write(discogsQuery);
                writer.flush();
                writer.close();

                connection.connect();
                Thread.sleep(THREAD_TIMEOUT);

                int responseCode = connection.getResponseCode();
                String responseMessge = connection.getResponseMessage();
                Log.d("createHttpRequest", String.valueOf(responseCode));
                Log.d("createHttpRequest", responseMessge);
                switch(responseCode)
                {
                    case HttpURLConnection.HTTP_OK:
                        reader = new BufferedInputStream(connection.getInputStream());
                        BufferedReader in = null;
                        in = new BufferedReader(new InputStreamReader(reader));
                        StringBuilder body = new StringBuilder();
                        String inputLine;
                        while((inputLine = in.readLine()) != null)
                        {
                            body.append(inputLine);
                        }
                        in.close();
                        Log.d("createHttpRequest", body.toString());
                        return body.toString();
                    default:
                        Log.d("createHttpRequest", "Didn't get HTTP_OK response");
                }
                connection.disconnect();
                return null;
            }
            catch(IOException e)
            {
                System.out.println("IOException : " + e);
                return null;
            }
            catch(InterruptedException e)
            {
                System.out.println("Interrupted Exception : " + e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String aString) {
            delegate.processFinish(aString);
        }
    }

}



