package com.creativewebs.boardima.Payments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.creativewebs.boardima.Members.AccountActivity;
import com.creativewebs.boardima.Members.AddMembersActivity;
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

/**
 * Created by Buddhi on 4/3/2018.
 */

public class NewPaymentActivity extends AppCompatActivity {
    public static ArrayList<String> IDs,Fnames,Snames;
    private EditText ETname, ETdate, ETvalue, ETtotal;
    private TextView TVp1err,TVp2err,TVp3err,TVp4err;
    private String name="",date="",value="",total="", userids="";

    public static ArrayList<String> PaymentMemberIDs;
    private ListView LVMembers;
    private CustomAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpayment);
        this.getSupportActionBar().setTitle("New Payment");
        PaymentMemberIDs = new ArrayList<String>();
        LVMembers = (ListView) findViewById(R.id.list_payer);
        IDs = new ArrayList<String>();
        Fnames= new ArrayList<String>();
        Snames= new ArrayList<String>();

        ETname = findViewById(R.id.etpname);
        ETdate = findViewById(R.id.etpdate);
        ETvalue = findViewById(R.id.etpval);
        ETtotal = findViewById(R.id.etptotal);

        TVp1err = findViewById(R.id.tvp1eerr);
        TVp2err = findViewById(R.id.tvp2err);
        TVp3err = findViewById(R.id.tvp3err);
        TVp4err = findViewById(R.id.tvp4err);


        Button addmem = (Button)findViewById(R.id.baddmem);
        Button bTPay = (Button) findViewById(R.id.bcreatepay);

        addmem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(NewPaymentActivity.this, AddMembersActivity.class);
                startActivity(i);
            }
        });

        bTPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name=ETname.getText().toString();
                date=ETdate.getText().toString();
                value=ETvalue.getText().toString();
                total=ETtotal.getText().toString();


                userids= android.text.TextUtils.join(",", PaymentMemberIDs);


                validate();
            }
        });
    }

    public void validate(){
        boolean Valid=true;
        TVp1err.setText("");
        TVp2err.setText("");
        TVp3err.setText("");
        TVp4err.setText("");
        if (name.equals("")){
            TVp1err.setText("Payment Name is required field");
            Valid=false;
        }
        if (value.equals("")){
            TVp2err.setText("Amount is required field");
            Valid=false;
        }
        if (date.equals("")){
            TVp3err.setText("Effective day is required field");
            Valid=false;
        }
        if (total.equals("")){
            total="0";
        }
        if (PaymentMemberIDs.size()==0){
            showMsg("At least one member is required");
            Valid=false;
        }
        if (Valid){
            createPayment();
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        updateMembers();
    }

    public class CustomAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return PaymentMemberIDs.size();
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
            Button bT = (Button)view.findViewById(R.id.btaddpayer);


            ListAdapter listadp = LVMembers.getAdapter();

            bT.setText("REMOVE");
            UserLogo.setImageResource(R.drawable.user);
            UserName.setText(Fnames.get(IDs.indexOf(PaymentMemberIDs.get(i))) + " " + Snames.get(IDs.indexOf(PaymentMemberIDs.get(i))));
            UserID.setText("User ID: " + PaymentMemberIDs.get(i));


            bT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        PaymentMemberIDs.remove(i);
                        showMembers();
                    }catch (IndexOutOfBoundsException e){

                    }
                }
            });




            return view;

        }
    }

    public void createPayment(){

        String path = "http://api.howtolk.com/new_payment?name="+name+"&date="+date+"&value="+value+"&total="+total+"&userids="+userids+"&hostid="+AccountActivity.UserID;
        if(isNetworkAvailable(this)) {
            new JSONTaskCreatePayment().execute(path);
        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }



    public void showMembers(){

        if (PaymentMemberIDs.size()>0) {


            adapter = new CustomAdapter();
            LVMembers.setAdapter(adapter);

            ListAdapter listadp = LVMembers.getAdapter();
            if (listadp != null) {
                int totalHeight = 0;
                for (int i = 0; i < listadp.getCount(); i++) {
                    View listItem = listadp.getView(i, null, LVMembers);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }
                ViewGroup.LayoutParams params = LVMembers.getLayoutParams();
                params.height = totalHeight + (LVMembers.getDividerHeight() * (listadp.getCount() - 1));
                LVMembers.setLayoutParams(params);
                LVMembers.requestLayout();
            }
        }
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

    public void goBack(){
        Intent intent = new Intent(this, PaymentsActivity.class);
        startActivity(intent);
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

    public class JSONTaskCreatePayment extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.length()==18){
                showMsg("New Payment added successfully");
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
