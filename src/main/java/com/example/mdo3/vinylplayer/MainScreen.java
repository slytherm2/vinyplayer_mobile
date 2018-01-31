package com.example.mdo3.vinylplayer;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MainScreen extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter listAdapter;
    private AdapterView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //get the intent of the previous activity
        Intent intent = getIntent();

        //get the user email from the previous activity (login/signup)
        String user = null;
        if (Login.LOGIN_USER == null)
            user = intent.getStringExtra(SignUp.SIGNUP_USER);
        else
            user = intent.getStringExtra(Login.LOGIN_USER);

        //creates a new title with "welcome" and "user email before the @"
        String tempStr = user.substring(0, user.indexOf('@'));
        String newTitle = getString(R.string.label_Welcome) + tempStr;

        //update title to reflect user "welcome ...user"
        TextView title = (TextView) findViewById(R.id.main_title);
        title.setText(newTitle);

        Button btn = findViewById(R.id.main_stateBTN);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {ContactsContract.Data.DISPLAY_NAME};
        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null,
                fromColumns, toViews, 0);
        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

        // This is the Adapter being used to display the list's data
        SimpleCursorAdapter mAdapter;

        // These are the Contacts rows that we will retrieve
        static final String[] PROJECTION = new String[] {ContactsContract.Data._ID,
                ContactsContract.Data.DISPLAY_NAME};

        // This is the select criteria
        static final String SELECTION = "((" +
                ContactsContract.Data.DISPLAY_NAME + " NOTNULL) AND (" +
                ContactsContract.Data.DISPLAY_NAME + " != '' ))";

        // Called when a new Loader needs to be created
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(this, ContactsContract.Data.CONTENT_URI,
                    PROJECTION, SELECTION, null, null);
        }

    @Override
    // Called when a previously created loader has finished loading
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Swap the new cursor in.  (The framework will take care of closing the
            // old cursor once we return.)
            mAdapter.swapCursor(data);
        }

        // Called when a previously created loader is reset, making the data unavailable
        public void onLoaderReset(Loader<Cursor> loader) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            mAdapter.swapCursor(null);
        }

        public void onListItemClick(ListView l, View v, int position, long id) {
            // Do something when a list item is clicked
        }

    public void setListAdapter(SimpleCursorAdapter listAdapter) {
        this.listAdapter = listAdapter;
    }

    public AdapterView getListView() {
        return listView;
    }
}
