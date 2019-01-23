package com.creativewebs.boardima.Members;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.creativewebs.boardima.LoginActivity;
import com.creativewebs.boardima.MainActivity;
import com.creativewebs.boardima.R;
import com.creativewebs.boardima.Services.SendNotificationActivity;

/**
 * Created by Buddhi on 3/13/2018.
 */

public class AccountActivity extends AppCompatActivity{

    public static String UserID=null;

    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        this.getSupportActionBar().setTitle("My Account");

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        Button bLog = (Button) findViewById(R.id.bLogout);
        Button bmsg = (Button) findViewById(R.id.bTmsg);





        bLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginPrefsEditor.putBoolean("saveLogin", false);
                loginPrefsEditor.putString("username", "");
                loginPrefsEditor.putString("password", "");
                loginPrefsEditor.commit();

                Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        bmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AccountActivity.this, SendNotificationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });



    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
