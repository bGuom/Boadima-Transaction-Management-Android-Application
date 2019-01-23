package com.creativewebs.boardima.Members;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.creativewebs.boardima.MainActivity;
import com.creativewebs.boardima.Members.MembersActivity;
import com.creativewebs.boardima.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateMemberActivity extends AppCompatActivity {

    public static int ID;
    private EditText ETfname,ETsname,ETbday,ETphone,EThome,ETadd,ETpass;
    private TextView TVerrfname,TVerrpassword;
    private String fname="",sname="",bday="",phone="",phonehome="",address="",password="";
    private Button BTsubmit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateuser);
        this.getSupportActionBar().setTitle("Update Member");
        ETfname = findViewById(R.id.etfnameup);
        ETsname = findViewById(R.id.etsnameup);
        ETbday = findViewById(R.id.etbdayup);
        ETphone = findViewById(R.id.etphoneup);
        EThome = findViewById(R.id.etphonehomeup);
        ETadd = findViewById(R.id.etaddressup);
        ETpass = findViewById(R.id.etpasswordup);
        TVerrpassword = findViewById(R.id.tvpassworderrup);
        BTsubmit = findViewById(R.id.bUp);



        BTsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fname=ETfname.getText().toString();
                sname=ETsname.getText().toString();
                bday=ETbday.getText().toString();
                phone=ETphone.getText().toString();
                phonehome=EThome.getText().toString();
                address=ETadd.getText().toString();
                password=ETpass.getText().toString();
                validate();
            }
        });

    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onStart(){
        super.onStart();
        ETfname.setText(MembersActivity.Fnames.get(ID));
        ETsname.setText(MembersActivity.Snames.get(ID));
        ETbday.setText(MembersActivity.Bdays.get(ID));
        ETphone.setText(MembersActivity.Phonenos.get(ID));
        EThome.setText(MembersActivity.Homephones.get(ID));
        ETadd.setText(MembersActivity.Addresses.get(ID));
        TVerrpassword.setText("Type the user password to update details");
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

    public void validate(){
        boolean Valid=true;

        TVerrpassword.setText("");

        if (password.equals("")){
            TVerrpassword.setText("PassWord is required field");
            Valid=false;
        }
        if (Valid){
            updateUser();
        }

    }

    public void goBack(){
        Intent intent = new Intent(this, MembersActivity.class);
        startActivity(intent);
    }

    public void updateUser(){
        String path = "http://api.howtolk.com/update/?id="+MembersActivity.IDs.get(ID)+"&password="+password+"&fname="+fname+"&sname="+sname+"&bday="+bday+"&phoneno="+phone+"&homephone="+phonehome+"&address="+address;
        if(isNetworkAvailable(this)) {
            new JSONTask().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }

    }

    public void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }


    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.length()==18){
                showMsg("Account Updated successfully");
                goBack();
            }
            else{
                showMsg("Failed. Check your Password");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


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

}
