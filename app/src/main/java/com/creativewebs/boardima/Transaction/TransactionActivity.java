package com.creativewebs.boardima.Transaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class TransactionActivity extends AppCompatActivity {
    public static ArrayList<String> TransID,FromIDs,ToIDs,Values,Dates,Reasons,FReq,TReq;


    public ArrayList<String> IDs,Fnames,Snames;
    private ListView LVTransactions;
    private CustomAdapter adapter;
    private String path;

    private TextView Res,Giv;

    private float totalIn, totalOut;
    public ProgressBar pbr;
    private TextView empt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translist);
        this.getSupportActionBar().setTitle("Transactions");
        LVTransactions = (ListView) findViewById(R.id.translist);
        pbr = (ProgressBar)findViewById(R.id.pbr4);
        empt= (TextView) findViewById(R.id.tvempty);

        IDs = new ArrayList<String>();
        Fnames= new ArrayList<String>();
        Snames= new ArrayList<String>();

        FromIDs = new ArrayList<String>();
        ToIDs = new ArrayList<String>();
        Values = new ArrayList<String>();
        Dates = new ArrayList<String>();
        Reasons = new ArrayList<String>();
        TransID = new ArrayList<String>();
        FReq = new ArrayList<String>();
        TReq = new ArrayList<String>();

        Res = (TextView)findViewById(R.id.tvresived);
        Giv = (TextView) findViewById(R.id.tvgave);




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


    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trans_menu, menu);
        return true;
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
    public void updateTransactions(){
        if (AccountActivity.UserID.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Title");

// Set up the input
            final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    path = "http://api.howtolk.com/my_transactions?id=" +  input.getText().toString();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
        else{
                path = "http://api.howtolk.com/my_transactions?id=" + AccountActivity.UserID;
            }
        if(isNetworkAvailable(this)) {
            new JSONTask().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    public void showTransactions(){

        totalIn=0;
        totalOut=0;
        String MI =AccountActivity.UserID;

        for(int i=0; i<TransID.size();i++){
            String TI = ToIDs.get(i);
            String FI = FromIDs.get(i);


            if (TI.equals(MI)){
                totalIn+=Float.parseFloat(Values.get(i));
            }
            if (FI.equals(MI)){
                totalOut+=Float.parseFloat(Values.get(i));
            }
        }
        Res.setText("Total Received "+totalIn+"Rs");
        Giv.setText("Total Given "+totalOut+"Rs");



        adapter = new CustomAdapter();
        LVTransactions.setAdapter(adapter);
    }

    public void closeTransaction(String TransactionID){
        String path = "http://api.howtolk.com/close_transaction/?transid="+TransactionID+"&reqid="+AccountActivity.UserID;
        if(isNetworkAvailable(this)) {
            new JSONTaskClose().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(this, NewTransactionActivity.class);
                startActivity(intent);
                return true;

            case R.id.filter:
                //Intent intent2 = new Intent(this, FilterActivity.class);
                //startActivity(intent2);
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
            return TransID.size();
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
            view = getLayoutInflater().inflate(R.layout.row_transaction,null);

            ImageView Logo = (ImageView)view.findViewById(R.id.ivtrans);
            TextView Name = (TextView)view.findViewById(R.id.tvpayname);
            TextView Date = (TextView)view.findViewById(R.id.tvpaydate);
            TextView Reason = (TextView)view.findViewById(R.id.tvpaytotal);
            TextView Value = (TextView)view.findViewById(R.id.tvval);


            bT = (Button)view.findViewById(R.id.btremov);





                String FromID = FromIDs.get(i);
                String ToID = ToIDs.get(i);
                String UserName = "";




                if (FromID.equals(AccountActivity.UserID)) {
                    Logo.setImageResource(R.drawable.minus);
                    if (IDs.indexOf(ToID)!=-1) {
                        UserName = Fnames.get(IDs.indexOf(ToID));
                    }
                    Value.setTextColor(Color.RED);
                }
                if (ToID.equals(AccountActivity.UserID)) {
                    Logo.setImageResource(R.drawable.plus);
                    if (IDs.indexOf(ToID)!=-1) {
                        UserName = Fnames.get(IDs.indexOf(FromID));
                    }
                    Value.setTextColor(Color.GREEN);
                }

                Name.setText(UserName);
                Value.setText(Values.get(i) + " Rs");
                Date.setText(Dates.get(i));
                Reason.setText(Reasons.get(i));

                if ((FReq.get(i).equals("1")) || (TReq.get(i).equals("1"))) {
                    bT.setBackground(ContextCompat.getDrawable(TransactionActivity.this, R.drawable.shadowred));
                    bT.setText("SETTLE THIS TRANSACTION");
                } else {
                    bT.setBackground(ContextCompat.getDrawable(TransactionActivity.this, R.drawable.shadow));
                    bT.setText("CLOSE THIS TRANSACTION");
                }


                bT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bT.setBackground(ContextCompat.getDrawable(TransactionActivity.this, R.drawable.shadowred));
                        bT.setText("SETTLE THIS TRANSACTION");
                        closeTransaction(TransID.get(i));
                    }
                });


            return view;

        }
    }

    public class JSONTaskMem extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (IDs.size()>0){
                empt.setVisibility(View.GONE);
                updateTransactions();
            }else{
                empt.setVisibility(View.VISIBLE);
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

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pbr.setVisibility(View.GONE);
            if (FromIDs.size()>0){
                empt.setVisibility(View.GONE);
                showTransactions();
            }else{
                empt.setVisibility(View.VISIBLE);
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            empt.setVisibility(View.GONE);
            pbr.setVisibility(View.VISIBLE);
            if (FromIDs.size()>0) {
                FromIDs.clear();
                ToIDs.clear();
                Values.clear();
                Dates.clear();
                Reasons.clear();
                TransID.clear();
                FReq.clear();
                TReq.clear();

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
                    FromIDs.add(e.getString("fromid"));
                    ToIDs.add(e.getString("toid"));
                    Values.add(e.getString("value"));
                    Dates.add(e.getString("date"));
                    Reasons.add(e.getString("reason"));
                    TransID.add(e.getString("transid"));
                    FReq.add(e.getString("fromreq"));
                    TReq.add(e.getString("toreq"));




                }



                return finalJson;




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

    public class JSONTaskClose extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showMsg(s);

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
