package com.creativewebs.boardima.Members;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.creativewebs.boardima.LoginActivity;
import com.creativewebs.boardima.Members.AccountActivity;
import com.creativewebs.boardima.Members.MembersActivity;
import com.creativewebs.boardima.MyFirebaseMessagingService;
import com.creativewebs.boardima.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NewMemberActivity extends AppCompatActivity {


    private EditText ETfname,ETsname,ETbday,ETphone,EThome,ETadd,ETpass;
    private TextView TVerrfname,TVerrpassword;
    private String fname="",sname="",bday="",phone="",phonehome="",address="",password="";
    private Button BTsubmit;
    public ProgressBar pbr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newuser);
        this.getSupportActionBar().hide();

        ETfname = findViewById(R.id.etfname);
        ETsname = findViewById(R.id.etsname);
        ETbday = findViewById(R.id.etbday);
        ETphone = findViewById(R.id.etphone);
        EThome = findViewById(R.id.etphonehome);
        ETadd = findViewById(R.id.etaddress);
        ETpass = findViewById(R.id.etpassword);
        TVerrfname = findViewById(R.id.tvfnameerr);
        TVerrpassword = findViewById(R.id.tvpassworderr);
        BTsubmit = findViewById(R.id.bReg);
        pbr = (ProgressBar)findViewById(R.id.pbr2);



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
                ETfname.setBackgroundResource(R.drawable.edittext);
                ETfname.setBackgroundResource(R.drawable.edittext);
                ETsname.setBackgroundResource(R.drawable.edittext);
                ETbday.setBackgroundResource(R.drawable.edittext);
                ETphone.setBackgroundResource(R.drawable.edittext);
                EThome.setBackgroundResource(R.drawable.edittext);
                ETadd.setBackgroundResource(R.drawable.edittext);
                ETpass.setBackgroundResource(R.drawable.edittext);
                validate();
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        TVerrfname.setText("");
        TVerrpassword.setText("");
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
        TVerrfname.setText("");
        TVerrpassword.setText("");
        if (fname.equals("")){
            TVerrfname.setText("First Name is required field");
            ETfname.setBackgroundResource(R.drawable.edittextred);
            Valid=false;
        }
        if (phone.equals("")){
            TVerrfname.setText("Phone number is required field");
            ETphone.setBackgroundResource(R.drawable.edittextred);
            Valid=false;
        }
        if (password.equals("")){
            TVerrpassword.setText("PassWord is required field");
            ETpass.setBackgroundResource(R.drawable.edittextred);
            Valid=false;
        }
        if (Valid){
            createUser();
        }

    }



    public void goBack(){

        if(AccountActivity.UserID==null){
            showMsg("Please Login to your Account");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, MembersActivity.class);
            startActivity(intent);
        }


    }

    public void createUser(){
        String regId = MyFirebaseMessagingService.getToken(this);
        String path = "http://api.howtolk.com/new/?password="+password+"&fname="+fname+"&sname="+sname+"&bday="+bday+"&phoneno="+phone+"&homephone="+phonehome+"&address="+address+ "&fcmid="+regId;
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
            pbr.setVisibility(View.GONE);
            BTsubmit.setVisibility(View.VISIBLE);
            if(s == null){
                s="";
            }
            s =s.replaceAll("\"","").replace("[","").replace("]","");

            if (s.equals("result:success")){
                showMsg("New Member added successfully");
                goBack();
            }
            else{
                showMsg("Failed. Please try again");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            BTsubmit.setVisibility(View.GONE);
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

}
