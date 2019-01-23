package com.creativewebs.boardima.Members;

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

public class MembersActivity extends AppCompatActivity {
    public static ArrayList<String> IDs,Fnames,Snames,Bdays,Phonenos,Homephones,Addresses,Passwords;

    private ListView LVMembers;
    private CustomAdapter adapter;
    private TextView empt;
    public ProgressBar pbr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        this.getSupportActionBar().setTitle("Members");

        LVMembers = (ListView) findViewById(R.id.list_);
        empt=(TextView)findViewById(R.id.tvemptylist);
        pbr = (ProgressBar)findViewById(R.id.pbr3);
        IDs = new ArrayList<String>();
        Fnames= new ArrayList<String>();
        Snames= new ArrayList<String>();
        Bdays= new ArrayList<String>();
        Phonenos= new ArrayList<String>();
        Homephones= new ArrayList<String>();
        Addresses= new ArrayList<String>();
        Passwords= new ArrayList<String>();
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
        inflater.inflate(R.menu.user_menu, menu);
        return true;
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
                Intent intent = new Intent(this, NewMemberActivity.class);
                startActivity(intent);
                return true;
            case R.id.removemember:
                Intent intent2 = new Intent(this, DeleteMembersActivity.class);
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

    public class CustomAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return IDs.size();
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
            view = getLayoutInflater().inflate(R.layout.row_user,null);

            ImageView UserLogo = (ImageView)view.findViewById(R.id.ivphoto);
            TextView UserName = (TextView)view.findViewById(R.id.tvpayname);
            TextView UserContact = (TextView)view.findViewById(R.id.tvpaydate);
            TextView UserBDay = (TextView)view.findViewById(R.id.tvpaytotal);
            Button bT = (Button)view.findViewById(R.id.btinfo);

            UserLogo.setImageResource(R.drawable.user);
            UserName.setText(Fnames.get(i) + " " + Snames.get(i) );
            UserContact.setText("Mobile: "+Phonenos.get(i));
            UserBDay.setText("Birth Day: "+Bdays.get(i));

            bT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UpdateMemberActivity.ID=i;
                    showInfo();
                }
            });
            return view;

        }
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pbr.setVisibility(View.GONE);
            if (IDs.size()>0){
                empt.setVisibility(View.GONE);
                showMembers();
            }else {
                empt.setText("No Members :(");
                empt.setVisibility(View.VISIBLE);
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbr.setVisibility(View.VISIBLE);
            if (IDs.size()>0) {
                IDs.clear();
                Fnames.clear();
                Snames.clear();
                Bdays.clear();
                Phonenos.clear();
                Homephones.clear();
                Addresses.clear();
                Passwords.clear();
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
                    Bdays.add(e.getString("bday"));
                    Phonenos.add(e.getString("phoneno"));
                    Homephones.add(e.getString("homephone"));
                    Addresses.add(e.getString("address"));
                    Passwords.add(e.getString("password"));
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

    public void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }





}
