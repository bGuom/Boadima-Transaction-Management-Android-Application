package com.creativewebs.boardima.Services;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.creativewebs.boardima.MainActivity;
import com.creativewebs.boardima.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Buddhi on 4/21/2018.
 */

public class SendNotificationActivity extends AppCompatActivity {

    private String title, msg;
    EditText ETtitle,ETmsg ;
    ArrayList<String> Users, UIDs , Fnames,Snames;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendnotification);

        ETtitle = (EditText) findViewById(R.id.ettitle);
        ETmsg = (EditText) findViewById(R.id.etmsg);
        Button Bt = (Button) findViewById(R.id.bsend);
        Users = new ArrayList<String>();
        UIDs = new ArrayList<String>();
        Fnames= new ArrayList<String>();
        Snames= new ArrayList<String>();



        Bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = ETtitle.getText().toString();
                msg = ETmsg.getText().toString();


                // Build an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SendNotificationActivity.this);

                // String array for alert dialog multi choice items

                String[] UserArray = new String[Fnames.size()];
                UserArray = Fnames.toArray(UserArray);

                // Boolean array for initial selected items
                final boolean[] checkedUsers = new boolean[UIDs.size()];

                // Convert the color array to list
                final List<String> colorsList = Arrays.asList(UserArray);

                builder.setMultiChoiceItems(UserArray, checkedUsers, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {


                        Users.add(UIDs.get(which));



                    }
                });

                // Specify the dialog is not cancelable
                builder.setCancelable(false);

                // Set a title for alert dialog
                builder.setTitle("Select users to send message");

                // Set the positive/yes button click listener
                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        send();
                        // Do something when click positive button

                    }
                });



                // Set the neutral/cancel button click listener
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // Do something when click the neutral button
                    }
                });

                AlertDialog dialog = builder.create();
                // Display the alert dialog on interface
                dialog.show();
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
        updateMembers();

    }
    public void updateMembers(){
        String path = "http://api.howtolk.com/all_user_info/";
        if(isNetworkAvailable(this)) {
            new JSONTaskMem().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    public void send(){


        for(String user : Users) {
            sendNotification(user,title,msg);
        }

    }

    public void sendNotification(String id,String title, String msg){

        String path = "http://api.howtolk.com/send_fcm/?toid="+id+"&title="+title+"&message="+msg;
        if(isNetworkAvailable(this)) {
            new JSONTasknotify().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }



    public class JSONTaskMem extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (UIDs.size()>0) {
                UIDs.clear();
                Fnames.clear();
                Snames.clear();

            }

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

                JSONArray json = new JSONArray(finalJson);

                for(int r=0;r<json.length();r++){
                    JSONObject e = json.getJSONObject(r);
                    UIDs.add(e.getString("id"));
                    Fnames.add(e.getString("fname"));
                    Snames.add(e.getString("sname"));

                }



                return Fnames.get(0);




            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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

    public void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public class JSONTasknotify extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.length()==18){
                showMsg("Members notified successfully");

            }
            else{

                showMsg("Failed. Please try again");
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
