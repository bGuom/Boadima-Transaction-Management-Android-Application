package com.creativewebs.boardima;

/**
 * Created by Buddhi on 1/16/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.creativewebs.boardima.Members.AccountActivity;
import com.creativewebs.boardima.Members.NewMemberActivity;
import com.creativewebs.boardima.Services.GetRequestClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText uName;
    private EditText pWord;
    private CheckBox stay;
    private TextView sUP;

    private String phone="",pass="";

    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    public ProgressBar pbr;
    private Button bLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.getSupportActionBar().hide();

        bLog = (Button) findViewById(R.id.bLogin);
        uName = (EditText) findViewById(R.id.editText_phoneno);
        pWord = (EditText) findViewById(R.id.editText_password);
        stay = (CheckBox) findViewById(R.id.check);
        sUP = (TextView) findViewById(R.id.signup);
        pbr = (ProgressBar)findViewById(R.id.pbr);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            uName.setText(loginPreferences.getString("username", ""));
            pWord.setText(loginPreferences.getString("password", ""));
            phone = (loginPreferences.getString("username", ""));
            pass = (loginPreferences.getString("username", ""));
            stay.setChecked(true);
            login();
        }

        sUP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, NewMemberActivity.class);
                startActivity(i);
            }
        });


        bLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                phone=uName.getText().toString();
                pass=pWord.getText().toString();

                if (phone.equals("") || pass.equals("")){
                    showMsg("Please re check your inputs");
                }else{
                    login();
                }
            }
        });



    }



    public boolean isNetworkAvailable(Context ctx)
    {
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()&& cm.getActiveNetworkInfo().isAvailable()&& cm.getActiveNetworkInfo().isConnected())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void updateFCM(){
        String regId = MyFirebaseMessagingService.getToken(this);
        String path = "http://api.howtolk.com/update_fcmid?id="+ AccountActivity.UserID+ "&fcmid="+regId;
        new GetRequestClass().execute(path);
        finishLogin();
    }

    public void finishLogin(){




        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(uName.getWindowToken(), 0);

        if (stay.isChecked()) {
            loginPrefsEditor.putBoolean("saveLogin", true);
            loginPrefsEditor.putString("username", phone);
            loginPrefsEditor.putString("password", pass);
            loginPrefsEditor.commit();
        } else {
            loginPrefsEditor.clear();
            loginPrefsEditor.commit();
        }




        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }

    public void login(){
        String path = "http://api.howtolk.com/auth/?phoneno="+phone+"&password="+pass;
        if(isNetworkAvailable(this)) {
            new JSONTask().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }


    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pbr.setVisibility(View.GONE);
            bLog.setVisibility(View.VISIBLE);

            if (s==null){
                s="";
            }

            s =s.replaceAll("\"","").replace("[","").replace("]","");

            //showMsg(s);



            if (s.equals("error")){
                showMsg("Wrong phone no Password combination");
            }
            else if (s.equals("")) {
                showMsg("Failed. Please try again");
            }else if (Integer.parseInt(s)>0){
                showMsg("Logged in successful");
                AccountActivity.UserID= s;
                updateFCM();
            } else {
                showMsg("Error occurred");
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bLog.setVisibility(View.GONE);
            pbr.setVisibility(View.VISIBLE);
        }

        @Override
        public String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();







                return finalJson;




            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }


    public void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }




}
