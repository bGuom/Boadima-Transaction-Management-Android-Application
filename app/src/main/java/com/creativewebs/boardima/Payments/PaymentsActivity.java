package com.creativewebs.boardima.Payments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.creativewebs.boardima.MainActivity;
import com.creativewebs.boardima.Members.AccountActivity;
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

public class PaymentsActivity extends AppCompatActivity {
    public static ArrayList<String> PID,PNames,PDate,PTotal,PVal;

    private ListView LVPayments;
    private CustomAdapter adapter;
    public ProgressBar pbr;
    private TextView empt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        this.getSupportActionBar().setTitle("Payments");

        LVPayments = (ListView) findViewById(R.id.list_);
        pbr = (ProgressBar)findViewById(R.id.pbr3);
        empt=(TextView)findViewById(R.id.tvemptylist);
        PID= new ArrayList<String>();
         PNames= new ArrayList<String>();
        PDate= new ArrayList<String>();
        PTotal= new ArrayList<String>();
        PVal= new ArrayList<String>();

    }

    @Override
    public void onResume(){
        super.onResume();
        updatePayments();
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    public void updatePayments(){
        String path = "http://api.howtolk.com/payment_info/";
        if(isNetworkAvailable(this)) {
            new JSONTask().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    public void showPayments(){



        adapter = new CustomAdapter();
        LVPayments.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addmember:
                Intent intent = new Intent(this, NewPaymentActivity.class);
                startActivity(intent);
                return true;
            case R.id.removemember:
                Intent intent2 = new Intent(this, RemovePaymentActivity.class);
                startActivity(intent2);
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

    public void notifyUsers(int id){

        List<String> users = Arrays.asList(PID.get(id).split(","));

        for(String user : users) {
            String msg = PVal.get(id) + "RS Payment for " + PNames.get(id) + " is due on " + PDate.get(id) +".. You have total outstanding of " + PTotal.get(id) +"RS..";
            sendNotification(user,"Payment Reminder!",msg);
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

    public class CustomAdapter extends BaseAdapter {


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
            view = getLayoutInflater().inflate(R.layout.row_payment,null);

            ImageView PaymentLogo = (ImageView)view.findViewById(R.id.ivpay);
            TextView PayName = (TextView)view.findViewById(R.id.tvpayname);
            TextView PayDate = (TextView)view.findViewById(R.id.tvpaydate);
            TextView PayTot = (TextView)view.findViewById(R.id.tvpaytotal);
            TextView PayVal = (TextView)view.findViewById(R.id.tvpayval);
            Button bT = (Button)view.findViewById(R.id.btnoti);

            PaymentLogo.setImageResource(R.drawable.pay);
            PayName.setText(PNames.get(i) );
            PayDate.setText("Due Date: "+PDate.get(i));
            PayTot.setText("Current Outstanding: " +PTotal.get(i));
            PayVal.setText(PVal.get(i)+" RS");

            bT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notifyUsers(i);

                }
            });
            return view;

        }
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        String s;

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pbr.setVisibility(View.GONE);
            if (PNames.size()>0){
                empt.setVisibility(View.GONE);
                showPayments();
            }else {
                empt.setText("No Payments :)");
                empt.setVisibility(View.VISIBLE);
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbr.setVisibility(View.VISIBLE);
            if (PNames.size()>0) {
                PID.clear();
                PNames.clear();
                PDate.clear();
                PTotal.clear();
                PVal.clear();
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
                    List<String> users = Arrays.asList(e.getString("userids").split(","));

                    s = users.get(0);

                    if (users.contains(AccountActivity.UserID)){
                        PID.add(e.getString("userids"));
                        PNames.add(e.getString("name"));
                        PDate.add(e.getString("date"));
                        PTotal.add(e.getString("total"));
                        PVal.add(e.getString("value"));
                    }

                }





                return s;




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

    public class JSONTasknotify extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null){
                s="oops";
            }
            s =s.replaceAll("\"","").replace("[","").replace("]","");

            if (s.equals("result:success")){
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

    public void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }





}
