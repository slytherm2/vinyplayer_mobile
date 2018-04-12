package com.example.mdo3.vinylplayer;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.math3.geometry.euclidean.twod.Line;

import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class MainScreen extends AppCompatActivity
{

    private String vinylConnected = null;
    private String vinylNotConnected = null;

    private String newTitle = null;
    private Intent intent = null;
    private ListView listview = null;
    private ArrayAdapter adapter = null;
    private String intentClass = null;
    private DrawerLayout mDrawerLayout;

    private boolean DEBUG = true;
    private int REQUEST_ENABLE_BT = 1;
    private int ENABLE_CAMERA = 2;

    private String sessionID = null;
    private String userID = null;
    private String email = null;

    private static Button btn = null;
    private SharedPreferences preferences;

    private ListView catalogListView;
    private ArrayList<Record> recordList;
    private ArrayList<String> lv;
    private String[] recordSet;

    final private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        intent = getIntent();   //get the intent of the previous activity
        vinylConnected = getResources().getString(R.string.label_con);
        vinylNotConnected =  getResources().getString(R.string.label_not_con);
        BluetoothLESingleton leSingleton = BluetoothLESingleton.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Resources rsrc = this.getResources();
        email = preferences.getString(rsrc.getString(R.string.label_email), null);
        sessionID = preferences.getString(getResources().getString(R.string.session_id),"");
        userID = preferences.getString(getResources().getString(R.string.user_id),"");
        catalogListView = (ListView) findViewById(R.id.main_albumList);
        recordList = new ArrayList<>();

        //update title to reflect user "welcome ...username"
        TextView title = (TextView) findViewById(R.id.main_title);
        if(newTitle != null)
            title.setText(newTitle);
        else
            title.setText("User " + getString(R.string.label_Welcome) );  //or use generic title
        btn = findViewById(R.id.main_stateBTN);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

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

        //automatically enable bluetooth if available
        Thread t1 = new Thread(new Runnable()
        {
            public void run()
            {
                startBT();
            }
        });
        t1.start();

        //sets up the listview with items from the array
        //TODO: connect to database and pull information relating to the specific user
       String str = preferences.getString(email + this.getResources().getString(R.string.local_catalog),
               null);

       if(str != null)
           recordList = splitInformation(str);

       //Album : Artist
        lv = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        for(int i = 0; i < recordList.size(); i++)
        {
            Record rc1 = recordList.get(i);
            String album = rc1.getAlbum();
            String artist = rc1.getArtist();
            String uri = rc1.getFilePath();
            lv.add(album + "\n" + artist);
            values.add(uri);
            System.out.println("DEBUG: " + album + "\n" + artist);
        }


        //When an item on the list gets clicked on, do some action
        //TODO: modify click to bring to music player
        //todo: include picture, album, artist
        listview = (ListView) findViewById(R.id.main_albumList);
        adapter = new ArrayAdapter(this, R.layout.mainscreen_listview, R.id.Itemname, lv);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id)
            {
                String yourData = lv.get(position);
                System.out.println("DEBUG: " + yourData);
                System.out.println("DEBUG: " + position + 1);

            }
        });

    }

    public void startBT(View view)
    {
        Toast.makeText(this, R.string.launchingBT_msg, Toast.LENGTH_SHORT).show();
        Intent bt_intent = new Intent(this, LowEnergyBlueTooth.class);
        startActivityForResult(bt_intent, REQUEST_ENABLE_BT);
    }

    private void startBT()
    {
        Intent bt_intent = new Intent(this, LowEnergyBlueTooth.class);
        startActivityForResult(bt_intent, REQUEST_ENABLE_BT);
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
                btn = findViewById(R.id.main_stateBTN);
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                btn.setText(vinylConnected);

            } else if (resultCode == Activity.RESULT_CANCELED)
            {
                btn = findViewById(R.id.main_stateBTN);
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                btn.setText(vinylNotConnected);
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

                //Bitmap compression into jpg
                //Bitmap compression into png doesn't use quality level
                /*
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/vinyl_images");
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-" + n + ".jpg";
                File file = new File(myDir, fname);

                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    image.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

               */
               // return directory.getAbsolutePath();

                System.out.println("DEBUG: image saved");
                Intent intent = new Intent(this, MainScreen.class);
                startActivity(intent);
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
        System.out.println("DEBUG: requesting permission");
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
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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

    private ArrayList<Record> splitInformation(String list)
    {
        ArrayList<Record> recordList = new ArrayList<>();
        ArrayList<Song> songList = new ArrayList<>();

        if (list == null)
            return null;
        else if(list.isEmpty())
            return null;

        //List is CSV, the start of each album is marked by STOPNULL
        String[] temp = list.split(",");
        Record record;
        Song song;
        String artist;
        String album;
        String uri;
        String rpm;
        String status;
        int songPos = 1;
        int counter = 0;

        //List : album name, artist name, uri, rotation speed, song, duration
        //Record(String artist, String album, ArrayList<Song> tracklist, String rpm, String filePath)
        //Song(String title, int position, String duration)
        for(int i = counter; i < temp.length - 4; i=counter)
        {
            album = temp[counter];
            artist = temp[++counter];
            uri = temp[++counter];
            rpm = temp[++counter];

            while(counter < temp.length - 4)
            {
                song = new Song(temp[++counter], String.valueOf(songPos), temp[++counter]);
                songList.add(song);
                songPos++;
                if (temp[counter+1].equals(this.getResources().getString(R.string.stop_flag)))
                {
                    counter++;
                    counter++;
                    break;
                }
            }
            record = new Record(artist, album, songList, rpm, uri);
            recordList.add(record);
        }
        return recordList;
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
}
