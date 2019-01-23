package com.creativewebs.boardima.Members;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.creativewebs.boardima.LoginActivity;
import com.creativewebs.boardima.Members.AccountActivity;
import com.creativewebs.boardima.R;
import com.creativewebs.boardima.Services.GetRequestClass;

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

public class DeleteMembersActivity extends Activity {
    public static ArrayList<String> IDs,Fnames,Snames,Passwords;

    private ListView LVMembers;
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listdialog);

        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);



        LVMembers = (ListView) findViewById(R.id.list_dialog);
        IDs = new ArrayList<String>();
        Fnames= new ArrayList<String>();
        Snames= new ArrayList<String>();
        Passwords= new ArrayList<String>();
        Button done = (Button)findViewById(R.id.bdone);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }



    @Override
    public void onResume(){
        super.onResume();


        updateMembers();

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
        private String MemID;
        private Button bT;


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
            view = getLayoutInflater().inflate(R.layout.row_adduser,null);

            ImageView UserLogo = (ImageView)view.findViewById(R.id.ivaddmem);
            TextView UserName = (TextView)view.findViewById(R.id.tvmemname);
            TextView UserID = (TextView)view.findViewById(R.id.tvmemid);
            bT = (Button)view.findViewById(R.id.btaddpayer);

            MemID = IDs.get(i);

            bT.setText(" Delete ");
            UserLogo.setImageResource(R.drawable.user);
            UserName.setText(Fnames.get(i) + " " + Snames.get(i));
            UserID.setText("User ID: " + MemID);

            bT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DeleteMember(i);

                }
            });


            return view;

        }
    }

    public void DeleteMember(final int id){

        if(IDs.get(id).equals(AccountActivity.UserID) || AccountActivity.UserID.equals("1")){
            String path = "http://api.howtolk.com/delete_member/?id="+IDs.get(id) +"&password=" +  Passwords.get(id);
            new GetRequestClass().execute(path);
            showMsg("Deleted");
            if(IDs.get(id).equals(AccountActivity.UserID)) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }else{
            showMsg("You can't delete other members");
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
