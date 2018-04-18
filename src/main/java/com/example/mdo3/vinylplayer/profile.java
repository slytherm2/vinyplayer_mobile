package com.example.mdo3.vinylplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class profile extends AppCompatActivity
{
    private Toolbar mTopToolbar;

    private TextView inputEmail = null;
    private TextView name = null;
    private TextView phone = null;
    private TextView address = null;
    private TextView city = null;
    private TextView state = null;

    private EditText ename = null;
    private EditText ephone = null;
    private EditText eaddress = null;
    private EditText ecity = null;
    private EditText estate = null;

    private String mInputEmail = null;
    private String mName = null;
    private String mPhone = null;
    private String mAddress = null;
    private String mCity = null;
    private String mState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //adding tool bar with back arrow to go back to activity
        //it goes to the activity listed in the android manifest
        mTopToolbar = (Toolbar) findViewById(R.id.profile_tool);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Get reference to text view boxes on profile activity
        inputEmail = (TextView) findViewById(R.id.profile_email);
        name = (TextView) findViewById(R.id.profile_name);
        phone = (TextView) findViewById(R.id.profile_phone);
        address = (TextView) findViewById(R.id.profile_address);
        city = (TextView) findViewById(R.id.profile_city);
        state = (TextView) findViewById(R.id.profile_state);

        //Get reference to text view boxes on profile activity
        ename = (EditText) findViewById(R.id.profile_eName);
        ephone = (EditText) findViewById(R.id.profile_ePhone);
        eaddress = (EditText) findViewById(R.id.profile_eAddress);
        ecity = (EditText) findViewById(R.id.profile_eCity);
        estate = (EditText) findViewById(R.id.profile_eState);

        //Get information saved in the xml file in default location
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mInputEmail = preferences.getString("Email",null).trim();
        if(mInputEmail != null && !mInputEmail.isEmpty())
        {
            mName = preferences.getString(mInputEmail + "Name", null);
            mPhone = preferences.getString(mInputEmail + "Phone", null);
            mAddress = preferences.getString(mInputEmail + "Address", null);
            mCity = preferences.getString(mInputEmail + "City", null);
            mState = preferences.getString(mInputEmail + "State", null);
        }

        //Set the text view boxes to information from xml
        if(mInputEmail != null && !mInputEmail.isEmpty())
            inputEmail.setText(mInputEmail);
        if(mName != null && !mName.isEmpty())
            name.setText(mName);
        if(mPhone != null && !mPhone.isEmpty())
            phone.setText(mPhone);
        if(mAddress != null && !mAddress.isEmpty())
            address.setText(mAddress);
        if(mCity != null && !mCity.isEmpty())
            city.setText(mCity);
        if(mState != null && !mState.isEmpty())
            state.setText(mState);
    }

    //Callback method for the back arrow on the tool bar
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    public void switchBtn(View view)
    {
        ViewSwitcher tempSwitcher = (ViewSwitcher) findViewById(R.id.name_switcher);
        EditText v = (EditText) tempSwitcher.getNextView();
        if(mName != null)
            v.setText(mName);
        else
            v.setHint(this.getResources().getString(R.string.label_name));
        tempSwitcher.showNext(); //or switcher.showPrevious();

        tempSwitcher = (ViewSwitcher) findViewById(R.id.phone_switcher);
        v = (EditText) tempSwitcher.getNextView();
        if(mPhone != null)
            v.setText(mPhone);
        else
            v.setHint(this.getResources().getString(R.string.label_phone));
        tempSwitcher.showNext();

        tempSwitcher = (ViewSwitcher) findViewById(R.id.address_switcher);
        v = (EditText) tempSwitcher.getNextView();
        if(mAddress != null)
            v.setText(mAddress);
        else
            v.setHint(this.getResources().getString(R.string.label_address));
        tempSwitcher.showNext();

        tempSwitcher = (ViewSwitcher) findViewById(R.id.button_switcher);
        tempSwitcher.showNext();

        tempSwitcher = (ViewSwitcher) findViewById(R.id.city_switcher);
        v = (EditText) tempSwitcher.getNextView();
        if(mCity != null)
            v.setText(mCity);
        else
            v.setHint(this.getResources().getString(R.string.label_city));
        tempSwitcher.showNext();

        tempSwitcher = (ViewSwitcher) findViewById(R.id.state_switcher);
        v = (EditText) tempSwitcher.getNextView();
        if(mState != null)
            v.setText(mState);
        else
            v.setHint(this.getResources().getString(R.string.label_state));
        tempSwitcher.showNext();
    }

    public void cancelBtn(View view)
    {
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }

    public void submitBtn(View view)
    {
        saveInformation();
        Intent intent = new Intent(this, profile.class);
        startActivity(intent);
    }

    private void saveInformation()
    {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        Resources rsrc = this.getResources();

        if(eaddress != null || !eaddress.getText().toString().isEmpty())
        editor.putString(mInputEmail + rsrc.getString(R.string.signup_address),
                eaddress.getText().toString());

        if(ename != null || !ename.getText().toString().isEmpty())
        editor.putString(mInputEmail + rsrc.getString(R.string.signup_name),
                ename.getText().toString());

        if(ecity != null || !ecity.getText().toString().isEmpty())
        editor.putString(mInputEmail + rsrc.getString(R.string.signup_city),
                ecity.getText().toString());

        if(estate != null || !estate.getText().toString().isEmpty())
        editor.putString(mInputEmail + rsrc.getString(R.string.signup_state),
                estate.getText().toString());

        if(ephone != null || !ephone.getText().toString().isEmpty())
        editor.putString(mInputEmail + rsrc.getString(R.string.signup_phone),
                ephone.getText().toString());
        editor.commit();

        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }
}
