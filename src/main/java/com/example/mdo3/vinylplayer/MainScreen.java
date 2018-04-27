package com.example.mdo3.vinylplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdo3.vinylplayer.asyncTask.ImageAnalysisTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class MainScreen extends AppCompatActivity
{
    //TODO: improve UI
    //ToDO: rotate images
    //Todo: high res pictures
    //TODO: loading animations
    //TODO: get catalog information from DB
    //TODO: create update on menu slide
    //TODO: connect to database and pull information relating to the specific user

    private String vinylConnected = null;
    private String vinylNotConnected = null;

    private String newTitle = null;
    private Intent intent = null;
    private ListView listview = null;
    private DrawerLayout mDrawerLayout;

    private boolean DEBUG = true;
    private int REQUEST_ENABLE_BT = 1;
    private int ENABLE_CAMERA = 2;

    private String sessionID = null;
    private String userID = null;
    private String email = null;

    public static Button btn = null;
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private ArrayList<Record> recordList;
    private ArrayList<Record> fullRecordList;
    private CatalogRecordAdapter catRecAdapter;

    public static final int SPLITURL = 1;
    public static  final int SPLITPATH = 2;

    private int HOME = 3;
    private int ANTISKIP = 4;

    private BluetoothLESingleton leSingleton;
    private LowEnergyBlueTooth btle;
    public static int picture = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        intent = getIntent();   //get the intent of the previous activity
        vinylConnected = getResources().getString(R.string.label_con);
        vinylNotConnected =  getResources().getString(R.string.label_not_con);
        leSingleton = BluetoothLESingleton.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Resources rsrc = this.getResources();
        email = preferences.getString(rsrc.getString(R.string.label_email), null);
        sessionID = preferences.getString(getResources().getString(R.string.session_id),"");
        userID = preferences.getString(getResources().getString(R.string.user_id),"");
        recordList = new ArrayList<>();

        //update title to reflect user "welcome ...username"
        TextView title = (TextView) findViewById(R.id.main_title);
        newTitle = preferences.getString(email + this.getResources().getString(R.string.label_name), null);
        if(newTitle != null)
            title.setText(newTitle);
        else
            title.setText("User " + getString(R.string.label_Welcome) );  //or use generic title

        //Bluetooth button
        btn = findViewById(R.id.main_stateBTN);
        if(leSingleton.getConnStatus())
        {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            btn.setText(this.getResources().getString(R.string.label_con));
            btn.setEnabled(false);
        }
        else
        {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            btn.setText(this.getResources().getString(R.string.label_not_con));
            btn.setEnabled(true);
        }

       //Deals with the items inside the navigation drawer aka hamburger menu
        //best to use fragments when working with the navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener()
                {
                    public boolean onNavigationItemSelected(MenuItem menuItem)
                    {
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        menuItem.setChecked(false);
                        System.out.println("DEBUG: " + menuItem.toString() + " has been pressed");
                        launchMenuActivity(menuItem);
                        return true;
                    }
                });

        //changing the nav_main_screen.xml username and email
        View headerLayout = navigationView.getHeaderView(0);
        TextView tempTextView = (TextView) headerLayout.findViewById(R.id.nav_user_name);
        String temp = preferences.getString(email + rsrc.getString(R.string.label_name), null);
        if(temp != null)
            tempTextView.setText(temp);
        else
            tempTextView.setText("User");

        tempTextView = (TextView) headerLayout.findViewById(R.id.nav_email);
        tempTextView.setText(email);

        //deals with the nav menu bar or hamburger menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener()
                {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset)
                    {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView)
                    {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView)
                    {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState)
                    {
                        // Respond when the drawer motion state changes
                    }
                }
        );

        //camera action button
        //see if user granted permission before launching camera
        FloatingActionButton fab = findViewById(R.id.main_camera);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Snackbar.make(view, getResources().getString(R.string.launching_camera), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                checkCamPerms();
            }
        });

        /*//automatically enable bluetooth if available
        Thread t1 = new Thread(new Runnable()
        {
            public void run()
            {
                startBT();
            }
        });
        t1.start();*/

        //sets up the listview with items from the array
       String str = preferences.getString(email + this.getResources().getString(R.string.local_catalog),
               null);
       if(str != null)
           fullRecordList = Utils.splitInformation(this, str, SPLITPATH);

       str = preferences.getString(email + this.getResources().getString(R.string.search_catalog), null);
       if(str != null)
       {
           recordList = Utils.splitInformation(this, str, SPLITURL);
           if(fullRecordList == null)
               fullRecordList = new ArrayList<>();
           for(int i = 0; i < recordList.size(); i++)
           {
               fullRecordList.add(recordList.get(i));
           }
       }

        //When an item on the list gets clicked on, do some action
        listview = (ListView) findViewById(R.id.main_albumList);
       if(fullRecordList != null)
            catRecAdapter = new CatalogRecordAdapter(this, fullRecordList);
        listview.setAdapter(catRecAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id)
            {
                startMusicPlayer(fullRecordList.get(position));
            }
        });
    }

    public void startBT(View view)
    {
        Toast.makeText(this, R.string.launchingBT_msg, Toast.LENGTH_SHORT).show();
        startBT();
    }

    private void startBT()
    {
        Intent bt_intent = new Intent(this, LowEnergyBlueTooth.class);
        startActivityForResult(bt_intent, REQUEST_ENABLE_BT);
    }

    private void startMusicPlayer(Record record)
    {
        final String recordKey = this.getResources().getString(R.string.record);
        Intent intent = new Intent(this, MusicPlayer.class);
        intent.putExtra(recordKey, record);
        startActivity(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (DEBUG)
            System.out.println("DEBUG: MainScreen onActivityResult()\n");

        //request code 1 = Bluetooth
        if (requestCode == 1)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                Toast.makeText(this,
                        getResources().getString(R.string.bt_connected),
                        Toast.LENGTH_SHORT).show();
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                btn.setText(vinylConnected);
                btn.setEnabled(false);
            }
            else if (resultCode == Activity.RESULT_CANCELED)
            {
                Toast.makeText(this, getResources().getString(R.string.bt_conn_failed), Toast.LENGTH_SHORT).show();
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                btn.setText(vinylNotConnected);
                btn.setEnabled(true);
            }
        }

        //request code 2 = Camera
        if (requestCode == 2)
        {
            //This saves the image to default directory
            //Data > Data > Com.example.mdo3.vinylplayer > app_imageDir
            System.out.println("DEBUG : inside camera ");
            if (resultCode == Activity.RESULT_OK)
            {
                //TODO: send image to image analysis application for discovery
                System.out.println("DEBUG : Camera Result good ");

                Bitmap image = (Bitmap) data.getExtras().get("data");
                MediaStore.Images.Media.insertImage(getContentResolver(),
                        image,
                        UUID.randomUUID().toString(),
                        "vinyl_Image");


                //send data to the Heroku server for image analysis
                /*String url = getResources().getString(R.string.http_test_url_analyzeimage);
                AsyncTaskFactory factory = new AsyncTaskFactory();
                ImageAnalysisTask task = (ImageAnalysisTask) factory.generateAsyncTask("ImageAnalysis",
                        String.valueOf(picture),
                        url,
                        this.userID,
                        this.sessionID);*/


                try
                {
                   /* String artist = record.getString("artist");
                    String album = record.getString("album");
                    String url = record.getString("url");
                    String albumId = record.getString("albumId");
                    String year = record.getString("year");

                    ArrayList<Song> tracklist = new ArrayList<Song>();
                    JSONArray tracklist_JSON = record.getJSONArray("tracklist");
                    String title = null;
                    String duration = null;
                    String outputStr = (String) task.execute(image).get();
                    System.out.println("DEBUG: " + outputStr);

                    JSONArray records = null;
                    if(outputStr != null)
                        records = new JSONArray(outputStr);
                    if(records != null)
                        //Toast.makeText(this, records.getString("artist")), 1000);
                        addRecords(records);
                    //picture = (picture + 1)%2;*/

                    JSONObject json = new JSONObject();
                    json.put("artist", "Martin");
                    json.put("album", "martin's album");
                    json.put("url", "");
                    json.put("albumId", "12345");
                    json.put("year", "2020");
                    JSONArray jsonA = new JSONArray();
                    JSONObject song = new JSONObject();
                    song.put("title", "Song Title 1");
                    song.put("duration", "1:11");
                    jsonA.put(song);
                    JSONObject newSong = new JSONObject();
                    newSong.put("title", "song Title 2 ");
                    newSong.put("duration", "2:22");
                    jsonA.put(newSong);
                    json.put("tracklist",jsonA);

                    this.addRecord(json);
                }
                catch(Exception e)
                {

                }
            }
            else if (resultCode == Activity.RESULT_CANCELED)
            {
                return;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //used to ask user for permission to use camera
    //if granted, it will bring the user to the camera
    //if not, the user cannot use the camera feature
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        System.out.println("DEBUG: Main Screen requesting permission");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ENABLE_CAMERA)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                callCamera();
            }
            else
            {
                System.out.println("DEBUG: return");
                return;
            }
        }
    }

    private void launchMenuActivity(MenuItem item)
    {
        //referenced to the id located on teh main_screen_drawer_view.xml
        int id = item.getItemId();
        intent = new Intent(this, MainScreen.class);

        //profile page
        if(id == R.id.nav_profile)
            intent = new Intent(this,profile.class);
        //testing page
        else if(id == R.id.nav_test)
            intent = new Intent(this,testing.class);
        //Activity for manually adding songs
        else if(id == R.id.nav_manual_add)
            intent = new Intent(this,manual_add.class);
        //Launching camera, to search record based on camera
        else if(id == R.id.nav_camera_search)
        {
            View view = findViewById(android.R.id.content);
            Snackbar.make(view, getResources().getString(R.string.launching_camera), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            checkCamPerms();
            return;  //must call return to close out of menu bar and launch camera application
        }
        //search records by input
        else if(id == R.id.nav_search_records)
        {
            intent = new Intent(MainScreen.this, RecordSearch.class);
        }
        //connect to bluetooth device
        else if(id == R.id.nav_add_device)
        {
            intent = new Intent(this, LowEnergyBlueTooth.class);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
        else if(id == R.id.anti_skip)
        {
            sendData(ANTISKIP);
        }
        else if(id == R.id.reset_tonearm)
        {
            sendData(HOME);
        }
        else if(id == R.id.nav_log_out)
        {
            System.out.println("DEBUG: Logging user out now...");

            AsyncTaskFactory factory = new AsyncTaskFactory();
            AsyncTask logOutTask = factory.generateAsyncTask("Logout");
            try
            {
                if(sessionID != null && !sessionID.isEmpty() && userID != null && !userID.isEmpty())
                {
                    String httpURL = this.getResources().getString(R.string.https_url_logout);
                    if(httpURL != null)
                    {
                        //clear cookie from database
                        String[] params = {userID, sessionID, httpURL};
                        Boolean result = (Boolean) logOutTask.execute(params).get();

                        if(result)
                            intent = new Intent(this, Login.class);
                    }
                }
            }
            catch (InterruptedException e)
            {
                Log.d("Exception", e.getMessage());
            }
            catch(ExecutionException e)
            {
                Log.d("Exception", e.getMessage());
            }
        }
       startActivity(intent);
    }

    public static Button getButton()
    {
        return btn;
    }

/*    public static void setButton(Boolean result)
    {
        if(result) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            btn.setText(ApplicationContext.getInstance().getResources().getString(R.string.label_con));
            btn.setEnabled(false);
        }
        else
        {

            btn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            btn.setText(ApplicationContext.getInstance().getResources().getString(R.string.label_not_con));
            btn.setEnabled(true);
        }
    }*/

    private void checkCamPerms()
    {
        //Manifest requires camera use, user must give permission to use camera.
        //-1 = no camera permission
        //0 = camera permission granted
        int cameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    ENABLE_CAMERA);
        }
        else
        {
            System.out.println("DEBUG: Calling Camera");
            callCamera();
        }
    }

    private void callCamera()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null)
            {
                startActivityForResult(cameraIntent, ENABLE_CAMERA);
            }
        }
        else
        {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null)
            {
                startActivityForResult(cameraIntent, ENABLE_CAMERA);
            }
        }
    }

    private LinearLayout addSong(String filepath, String song)
    {
        //Layout params 1 = width, param 2 = height
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        //create a horizontal linear layout
        LinearLayout hLL = new LinearLayout(this);
        hLL.setOrientation(LinearLayout.HORIZONTAL);
        hLL.setLayoutParams(lParams);

        //create an image view for album picture
        ImageView image = new ImageView(this);
        image.setImageURI(Uri.parse(filepath));
        hLL.addView(image);

        //Corresponding album name and artist name
        TextView catalog = new TextView(this);
        catalog.setLayoutParams(lParams);
        catalog.setEms(10);
        catalog.setText(song);
        hLL.addView(catalog);

        //add the horizontal linear layout to the vertical linear layout
        return hLL;
    }

    private void sendData(int command)
    {
        byte[] data = String.valueOf(command).getBytes();

        //Sending command to embedded hardware
        btle.send(leSingleton.getGattService(),
                leSingleton.getSERVICE_UUID(),
                leSingleton.getGatt(),
                data);
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
                return;
            }
        }
        return;
    }

    private void addRecord(JSONObject record)
    {
        try
        {
            String artist = record.getString("artist");
            String album = record.getString("album");
            String url = record.getString("url");
            String albumId = record.getString("albumId");
            String year = record.getString("year");

            ArrayList<Song> tracklist = new ArrayList<Song>();
            JSONArray tracklist_JSON = record.getJSONArray("tracklist");
            String title = null;
            String duration = null;
            for(int i = 0; i < tracklist_JSON.length(); i++)
            {
                // Duration duration = null;
                title = tracklist_JSON.getJSONObject(i).getString("title");
                duration = tracklist_JSON.getJSONObject(i).getString("duration");
//                String duration_parsed[] = duration_String.split(":"); // song duration is in format minutes:seconds
//
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    duration = Duration.ofMinutes(Integer.parseInt(duration_parsed[0]));
//                    duration = duration.plusSeconds(Integer.parseInt(duration_parsed[1]));
//                }

                Song song = new Song(title, String.valueOf(i+1), duration);
                tracklist.add(song);
            }

            //Toast.makeText(this, artist, Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, album, Toast.LENGTH_SHORT).show();

            //Order important :{artist, album, year, url, albumId}
            String[] params = {artist, album, year, url, albumId};
            Record newRecord = new Record(tracklist, params);

            //Record newRecord = new Record(artist, album, tracklist, url);

            Intent intent = new Intent(this, RecordInfo.class);
            intent.putExtra("record", newRecord);
            startActivity(intent);

        } catch (JSONException e) {
            return;
        }
    }
}
