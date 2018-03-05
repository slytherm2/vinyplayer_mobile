package com.example.mdo3.vinylplayer;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainScreen extends AppCompatActivity {

    private String vinylConnected = null;
    private String vinylNotConnected = null;

    private String newTitle = null;
    private Intent intent = null;
    private Intent cameraIntent = null;
    private Button btn = null;
    private ListView listview = null;
    private DatabaseTask dbt = null;
    private ArrayList<String> list = null;
    private ArrayAdapter adapter = null;
    private String userID = null;
    private String intentClass = null;
    private DrawerLayout mDrawerLayout;

    private boolean DEBUG = true;
    private int REQUEST_ENABLE_BT = 1;
    private int ENABLE_CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        intent = getIntent();   //get the intent of the previous activity
        vinylConnected = getResources().getString(R.string.label_con);
        vinylNotConnected =  getResources().getString(R.string.label_not_con);

        //get the user email from the previous activity (login/signup)
        String user = null;
        if (intent != null)
        {
            if (Login.LOGIN_USER != null)
            {
                user = intent.getStringExtra(Login.LOGIN_USER);
                intentClass = "Login";
            }
            if (SignUp.SIGNUP_USER != null)
            {
                user = intent.getStringExtra(SignUp.SIGNUP_USER);
                intentClass = "SignUp";
            }
        }

        //creates a new title with "welcome" and "user email before the @"
        newTitle = null;
        if (user != null)
        {
            String tempStr = user.substring(0, user.indexOf('@'));
            newTitle = tempStr + " " + getString(R.string.label_Welcome);
        }

        //update title to reflect user "welcome ...username"
        TextView title = (TextView) findViewById(R.id.main_title);
        if(newTitle != null)
            title.setText(newTitle);
        else
            title.setText("User " + getString(R.string.label_Welcome) );  //or use generic title
        btn = findViewById(R.id.main_stateBTN);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));


       /*
        //sets up the listview with items from the array
        //TODO: connect to database and pull information relating to the specific user
        String[] strValues = {"Martin", "Jonathan", "Lucy", "Cece", "Bob", "Linda", "Jonny", "Jim","Carol", "John"};
        dbt = new DatabaseTask(this);
        dbt.execute((Void) null);


        listview = (ListView) findViewById(R.id.main_albumList);
        list = new ArrayList<String>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        //When an item on the list gets clicked on, do some action
        //TODO: modify click to bring to music player
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                String yourData = list.get(position);
                System.out.println(yourData);
            }
        });
        */

       //when we press the button on the tool bar menu
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        //the tool bar that includes the hamburger menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener(){
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );

        //camera action button
        FloatingActionButton fab = findViewById(R.id.main_camera);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Snackbar.make(view, getResources().getString(R.string.launching_camera), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

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
        });
    }

    public class DatabaseTask extends AsyncTask<Void, Void, Boolean>
    {

        private Context context;

        private DatabaseTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean urlResponse = false;
            String postParams = null;
            try {

                System.out.println("Action: starting callout catalog");
                URL url = new URL("https://vinyl-player-server.herokuapp.com/catalog");
                HttpsURLConnection urlConnection =  (HttpsURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                //TODO: remove urlConnection.setRequestProperty("Content-length", String.valueOf(postParams.length()));
                urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");

                /*
                OutputStream outputPost = new BufferedOutputStream((urlConnection.getOutputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));
                writer.write(postParams);
                writer.flush();
                writer.close();
                outputPost.close();
                */


                //setup cookie manager
                urlConnection.connect();
                Thread.sleep(2000);

                if (urlConnection.getResponseCode() == urlConnection.HTTP_OK)
                    urlResponse = true;
                else
                    urlResponse = false;

                String json_response = "";
                String text = "";
                InputStreamReader in = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(in);
                while ((text = br.readLine()) != null) {
                    json_response += text;
                }

                System.out.println("Response Code " + urlConnection.getResponseCode());
                System.out.println("Response Message  " + json_response);

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
            dbt = null;

            if (success)
            {
                finish();
            }
            else
            {
            }
        }

        @Override
        protected void onCancelled() {
            dbt = null;
        }
    }

    private String createPost(ArrayList<String> requests)
    {
        String finalString = null;
        StringBuilder str = new StringBuilder();

        try {
            int count = 0;
            for (String temp : requests) {
                str.append(URLEncoder.encode(temp, "UTF-8"));
                if (count%2 == 0)
                {
                    str.append("=");
                }
                if(count%2 == 1)
                {
                    str.append("&");
                }
                count++;
            }

        } catch (IOException e)
        {
            System.err.println(e);
        }

        finalString = str.toString();
        if (finalString.endsWith("&"))
        {
            int value = finalString.length();
            char c = finalString.charAt(value);
            finalString.replace(String.valueOf(c),"");
        }
        return finalString;
    }

    public void startBT(View view)
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
                return;
            }
        }

        //request code 2 = Camera
        if (requestCode == 2)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                //get image from camera
                //TODO: send image to image analysis application for discovery
                Bitmap image = (Bitmap) data.getExtras().get("data");

            } else if (resultCode == Activity.RESULT_CANCELED)
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ENABLE_CAMERA)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                callCamera();
            }
            else
            {
                return;
            }
        }
    }

    private void callCamera()
    {
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(cameraIntent, ENABLE_CAMERA);
        }
    }
}
