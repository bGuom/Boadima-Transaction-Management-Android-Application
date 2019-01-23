package com.creativewebs.boardima.Transaction;

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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.creativewebs.boardima.ItemData;
import com.creativewebs.boardima.Members.AccountActivity;
import com.creativewebs.boardima.R;
import com.creativewebs.boardima.SpinnerAdapter;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Buddhi on 4/18/2018.
 */

public class NewTransactionActivity extends AppCompatActivity {

    public ArrayList<String> IDs,Fnames,Snames;
    public  ArrayList<ItemData> list;
    private Spinner sp,sp2;
    private EditText val,date,reason;
    private TextView err;
    public ProgressBar pbr;
    private Button bT;

    private String fromid,toid,transval,transdate,transreason;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtransaction);
        this.getSupportActionBar().setTitle("New Transaction");

        IDs = new ArrayList<String>();
        Fnames= new ArrayList<String>();
        Snames= new ArrayList<String>();


        sp=(Spinner)findViewById(R.id.spinner);
        sp2=(Spinner)findViewById(R.id.spinner2);

        val = (EditText)findViewById(R.id.ettransval);
        date = (EditText)findViewById(R.id.ettransdate);
        reason = (EditText)findViewById(R.id.ettransreason);

        err = (TextView) findViewById(R.id.tvtranserr);

        bT = (Button) findViewById(R.id.bnTrans);
        pbr = (ProgressBar)findViewById(R.id.pbr5);

        bT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromid= IDs.get((int) sp.getSelectedItemId());
                toid = IDs.get((int) sp2.getSelectedItemId());
                transval=val.getText().toString();
                transdate=date.getText().toString();
                transreason=reason.getText().toString();
                err.setText("");

                if (transval.equals("")){
                    err.setText("Transaction Amount can not be empty");
                }else if((IDs.get((int) sp.getSelectedItemId()).equals(AccountActivity.UserID)) || (IDs.get((int) sp2.getSelectedItemId()).equals(AccountActivity.UserID))){
                    if((IDs.get((int) sp.getSelectedItemId()).equals(AccountActivity.UserID)) && (IDs.get((int) sp2.getSelectedItemId()).equals(AccountActivity.UserID))){
                        err.setText("This is an invalid transaction");
                    }else {

                        saveTransaction();
                    }
                }else{
                    err.setText("You can not save transaction for someone else");
                }
            }
        });




    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(this, TransactionActivity.class);
        startActivity(i);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateMembers();
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);

        date.setText(formattedDate);
    }

    public void updateMembers(){
        String path = "http://api.howtolk.com/all_user_info/";
        if(isNetworkAvailable(this)) {
            new JSONTask().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    public void saveTransaction(){
        String path = "http://api.howtolk.com/new_transaction/?fromid="+fromid+"&toid="+toid+"&value="+transval+"&date="+transdate+"&reason="+transreason;
        if(isNetworkAvailable(this)) {
            new JSONTasknewTrans().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    public void showMembers(){

        list=new ArrayList<>();

        for(int r=0;r<IDs.size();r++){
            list.add(new ItemData(Fnames.get(r),R.drawable.user));

        }


        SpinnerAdapter adapter=new SpinnerAdapter(this,
                R.layout.spinner_layout,R.id.txt,list);
        sp.setAdapter(adapter);
        sp2.setAdapter(adapter);

        sp.setSelection(IDs.indexOf(AccountActivity.UserID));
        sp2.setSelection(IDs.indexOf(AccountActivity.UserID));
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

    public void goBack(){
        Intent intent = new Intent(this, TransactionActivity.class);
        startActivity(intent);
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (IDs.size()>0){
                showMembers();
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (IDs.size()>0) {
                IDs.clear();
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
                    IDs.add(e.getString("id"));
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

    public class JSONTasknewTrans extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            bT.setVisibility(View.VISIBLE);
            pbr.setVisibility(View.GONE);
            if (s.length()==18){
                showMsg("New Transaction saved successfully");
                goBack();
            }
            else{
                showMsg(s);
                showMsg("Failed. Please try again");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbr.setVisibility(View.VISIBLE);
            bT.setVisibility(View.GONE);

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