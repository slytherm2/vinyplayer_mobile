package com.example.mdo3.vinylplayer;

import com.example.mdo3.vinylplayer.AsyncTaskFactory;
import com.example.mdo3.vinylplayer.asyncTask.SearchTask;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import org.json.*;

public class RecordSearch extends AppCompatActivity {
    private String sessionId = null;
    private String userId = null;
    private boolean isExecutingTask = false;
    private static SharedPreferences preferences;

    ArrayList<Record> records;
    private RecyclerView.Adapter adapter;
    AsyncTaskFactory factory;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_search);

        // dynamically add action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        factory = new AsyncTaskFactory();
        this.userId = preferences.getString(getResources().getString(R.string.session_id),"");
        this.sessionId = preferences.getString(getResources().getString(R.string.user_id),"");
        this.records = new ArrayList<Record>();

        RecyclerView recordResults = (RecyclerView) findViewById(R.id.record_results);
        recordResults.setHasFixedSize(true); // maybe take this out if things aren't working
        recordResults.setLayoutManager(new LinearLayoutManager(this));

        // specify adapter
        adapter = new RecordAdapter(this, records);
        recordResults.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.record_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        handleIntent(getIntent());
        return true;
    }

    public void addRecords(JSONArray records)
    {
        for (int i = 0; i < records.length(); i++)
        {
            try
            {
                this.addRecord(records.getJSONObject(i));
            }
            catch (JSONException e)
            {

                continue;
            }
        }
        return;
    }

    private void addRecord(JSONObject record)
    {
        try
        {
            String title[] = record.getString("title").split("-");
            String artist = title[0].trim();
            String album = title[1].trim();
            String url = record.getString("thumb");
            String year = record.getString("year");
            String id = record.getString("id");
//            String artist = record.getString("artist");
//            String album = record.getString("album");
//            String url = record.getString("url");
//            String albumId = record.getString("albumId");
//            String year = record.getString("year");

//            ArrayList<Song> tracklist = new ArrayList<Song>();
//            JSONArray tracklist_JSON = record.getJSONArray("tracklist");
//            for(int i = 0; i < tracklist_JSON.length(); i++)
//            {
//                // Duration duration = null;
//                String title = tracklist_JSON.getJSONObject(i).getString("title");
//                String duration = tracklist_JSON.getJSONObject(i).getString("duration");
//                Song song = new Song(title, duration);
//                tracklist.add(song);
//            }

            //Order important :{artist, album, year, url, albumId}
//            String[] params = {artist, album, year, url, id};
//            Record newRecord = new Record(tracklist, params);

            Record newRecord = new Record(artist, album, url, year, id);

            this.records.add(newRecord);
            this.adapter.notifyDataSetChanged();
            return;
        } catch (JSONException e) {
            return;
        }
    }

    // Get the intent, verify the action and get the query
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            if(this.userId == null || this.sessionId == null  || isExecutingTask)
            {
                Log.d("RecordSearch", "User not logged in or task is already being executed");
                return;
            }

            isExecutingTask = true;
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("RecordSearch", "handleIntent called");

            // SearchTask task = (SearchTask) factory.generateAsyncTask("Search", query,

            //         getResources().getString(R.string.http_url_test_search), this.userId, this.sessionId);
            
            SearchTask task = (SearchTask) factory.generateAsyncTask("Search", 
                                                                     query,
                                                                     getResources().getString(R.string.http_url_test_search_jose),
                                                                     this.userId, 
                                                                     this.sessionId);

            try
            {
                String result = task.execute().get();
                JSONArray records = null;
                if(result != null)
                    records = new JSONArray(result);
                if(records != null)
                    addRecords(records);
                isExecutingTask = false;
            }
            catch (InterruptedException e)
            {
                Log.d("RecordSearch" , "InterruptedException on handleIntent: " + e.toString());
            }
            catch (ExecutionException e) {
                Log.d("RecordSearch" , "ExecutionException on handleIntent: " + e.toString());
            } catch (JSONException e) {
                Log.d("RecordSearch" , "JSONException on handleIntent: " + e.toString());
            }

        }
    }
}





