package com.example.mdo3.vinylplayer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.net.URI;
import java.util.ArrayList;

public class MainScreen extends AppCompatActivity {

    private String newTitle = null;
    private Intent intent = null;
    private Button btn = null;
    private ListView listview = null;
    private String[] strValues = {"Martin", "Jonathan", "Lucy", "Cece", "Bob", "Linda", "Jonny", "Jim","Carol", "John"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //get the intent of the previous activity
        intent = getIntent();

        //get the user email from the previous activity (login/signup)
        String user = null;
        if (intent != null)
        {
            if (Login.LOGIN_USER != null)
                user = intent.getStringExtra(Login.LOGIN_USER);
            if (SignUp.SIGNUP_USER != null)
                user = intent.getStringExtra(SignUp.SIGNUP_USER);
        }

        //creates a new title with "welcome" and "user email before the @"
        newTitle = null;
        if (user != null)
        {
            String tempStr = user.substring(0, user.indexOf('@'));
            newTitle = getString(R.string.label_Welcome) + tempStr;
        }

        //update title to reflect user "welcome ...username"
        TextView title = (TextView) findViewById(R.id.main_title);
        if(newTitle != null)
            title.setText(newTitle);
        else
            title.setText(getString(R.string.label_Welcome) + "User");  //or use generic title

        btn = findViewById(R.id.main_stateBTN);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

        //sets up the listview with items from the array
        //TODO: connect to database and pull information relating to the specific user
        listview = (ListView) findViewById(R.id.main_albumList);
        final ArrayList<String> list = new ArrayList<String>();
        for(String str : strValues)
        {
            list.add(str);
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
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
    }
}
