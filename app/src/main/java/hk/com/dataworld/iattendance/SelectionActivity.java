package hk.com.dataworld.iattendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

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
                Intent attendanceIntent = new Intent(SelectionActivity.this, BluetoothNewActivity.class);
                startActivity(attendanceIntent);
            }
        });

        Button supervisorMode =  findViewById(R.id.btnSupervisorMode);
        supervisorMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent attendanceIntent = new Intent(SelectionActivity.this, BluetoothNewActivity.class);
                startActivity(attendanceIntent);
            }
        });
    }
}
