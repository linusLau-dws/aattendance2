package hk.com.dataworld.iattendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;

import static hk.com.dataworld.iattendance.Constants.PREF_HAS_SUPERVISOR_RIGHT;

public class SelectionActivity extends BaseActivity {
    private SharedPreferences mSharedPreferences;
    private boolean mHasSuRights = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mHasSuRights = mSharedPreferences.getBoolean(PREF_HAS_SUPERVISOR_RIGHT, false);

        Button registerLocation =  findViewById(R.id.btnLocationRegistration);
        registerLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent attendanceIntent = new Intent(SelectionActivity.this, LocationRegistrationActivity.class);
                startActivity(attendanceIntent);
            }
        });


        Button registerTimecard =  findViewById(R.id.btnTimecardRegistration);
        registerTimecard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent attendanceIntent = new Intent(SelectionActivity.this, TimeCardRegistrationActivity.class);
                startActivity(attendanceIntent);
            }
        });

        Button punch =  findViewById(R.id.btnPunch);
        punch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent attendanceIntent = new Intent(SelectionActivity.this, PunchActivity.class);
                startActivity(attendanceIntent);
            }
        });

        Button supervisorMode =  findViewById(R.id.btnSupervisorMode);
        supervisorMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent attendanceIntent = new Intent(SelectionActivity.this, SupervisorActivity.class);
                startActivity(attendanceIntent);
            }
        });

        if (mHasSuRights) {
            registerLocation.setVisibility(View.GONE);
            registerTimecard.setVisibility(View.GONE);
            supervisorMode.setVisibility(View.GONE);
        }
    }
}
