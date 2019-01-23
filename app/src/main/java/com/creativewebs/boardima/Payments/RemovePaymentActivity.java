package com.creativewebs.boardima.Payments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.creativewebs.boardima.MainActivity;
import com.creativewebs.boardima.Members.AccountActivity;
import com.creativewebs.boardima.R;
import com.creativewebs.boardima.Transaction.TransactionActivity;
import com.creativewebs.boardima.Members.UpdateMemberActivity;

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

public class RemovePaymentActivity extends Activity {
    public static ArrayList<String> PID,PNames,PDate,PTotal,PVal,PHostid;
    private ListView LVMembers;
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listdialog);

        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        PID= new ArrayList<String>();
        PNames= new ArrayList<String>();
        PDate= new ArrayList<String>();
        PTotal= new ArrayList<String>();
        PVal= new ArrayList<String>();
        PHostid= new ArrayList<String>();


        LVMembers = (ListView) findViewById(R.id.list_dialog);

        Button done = (Button)findViewById(R.id.bdone);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onResume(){
        super.onResume();


        updateMembers();

    }


    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    public void updateMembers(){
        String path = "http://api.howtolk.com/payment_info/";
        if(isNetworkAvailable(this)) {
            new JSONTask().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    public void showMembers(){

        adapter = new CustomAdapter();
        LVMembers.setAdapter(adapter);
    }

    public void showInfo(){
        Intent intent = new Intent(this, UpdateMemberActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addmember:
                Intent intent = new Intent(this, TransactionActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
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

    public class CustomAdapter extends BaseAdapter {

        private Button bT;


        @Override
        public int getCount() {
            return PNames.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override

        public View getView(final int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.row_adduser,null);

            ImageView UserLogo = (ImageView)view.findViewById(R.id.ivaddmem);
            TextView UserName = (TextView)view.findViewById(R.id.tvmemname);
            TextView UserID = (TextView)view.findViewById(R.id.tvmemid);
            bT = (Button)view.findViewById(R.id.btaddpayer);



            bT.setText("Remove");
            UserLogo.setImageResource(R.drawable.pay);
            UserName.setText(PNames.get(i));
            UserID.setText("Amount: " + PVal.get(i));

            bT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removePayment(PID.get(i));

                }
            });


            return view;

        }
    }

    public void goBack(){
        Intent intent = new Intent(this, PaymentsActivity.class);
        startActivity(intent);
    }

    public void removePayment(String PayID){

        String hostID = PHostid.get(PID.indexOf(PayID));

        if (hostID.equals(AccountActivity.UserID)){

            String path = "http://api.howtolk.com/remove_payment?id="+PayID;
            if(isNetworkAvailable(this)) {
                new JSONTaskRemovePayment().execute(path);
            }
            else{
                showMsg("Please check your internet connection");
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }




        }else{
            showMsg("Payment can only be removed by its creator");
        }

    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (PID.size()>0){

                showMembers();
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (PID.size()>0) {
                PID.clear();
                PNames.clear();
                PDate.clear();
                PVal.clear();
                PTotal.clear();
                PHostid.clear();

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
                    PID.add(e.getString("id"));
                    PNames.add(e.getString("name"));
                    PDate.add(e.getString("date"));
                    PVal.add(e.getString("value"));
                    PTotal.add(e.getString("total"));
                    PHostid.add(e.getString("hostid"));

                }



                return "";




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

    public class JSONTaskRemovePayment extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.length()==18){
                showMsg("Payment removed successfully");
                goBack();
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

    public void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }





}
