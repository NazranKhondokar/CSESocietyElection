package info.androidhive.loginandregistration.activity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import info.androidhive.loginandregistration.app.AppConfig;
import info.androidhive.loginandregistration.R;
import info.androidhive.loginandregistration.helper.ServiceHandler;

public class VoteActivity extends ListActivity {

    private Button btnShowCandidate;
    private ArrayList<HashMap<String, String>> candidatesList;
    ProgressDialog pDialog;

    private static final String TAG_CANDIDATE_LIST = "seat_candidates";
    private static final String TAG_NAME = "name";
    private static final String TAG_REG = "reg";
    private static final String TAG_SEAT_NAME = "seat_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        btnShowCandidate = (Button) findViewById(R.id.btnShowCandidate);
        final Spinner spinner = (Spinner) findViewById(R.id.seats_spinner_vote);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.seats_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(arrayAdapter);

        candidatesList = new ArrayList<HashMap<String, String>>();

        btnShowCandidate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                candidatesList.clear();
                String newCategory = spinner.getSelectedItem().toString();
                Log.e("Spinner: ", "> " + newCategory);
                new AddNewCategory().execute(newCategory);


            }
        });


    }

    private class GetCategories extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(VoteActivity.this);
            pDialog.setMessage("Fetching Candidates..");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ServiceHandler jsonParser = new ServiceHandler();
            String json = jsonParser.makeServiceCall(AppConfig.URL_find_candidates, ServiceHandler.GET);

            Log.e("Response: ", "> " + json);

            if (json != null) {
                try {

                    JSONObject jsonObj = new JSONObject(json.substring(json.indexOf("{\"")));

                    JSONArray candidates = jsonObj.getJSONArray(TAG_CANDIDATE_LIST);

                    // looping through All candidates
                    for (int i = 0; i < candidates.length(); i++) {
                        JSONObject c = candidates.getJSONObject(i);

                        // Storing each json item in variable
                        String name = c.getString(TAG_NAME);
                        String reg = c.getString(TAG_REG);
                        String seat_name = c.getString(TAG_SEAT_NAME);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_NAME, name);
                        map.put(TAG_REG, reg);
                        map.put(TAG_SEAT_NAME, seat_name);

                        // adding HashList to ArrayList
                        candidatesList.add(map);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.e("JSON Data", "Didn't receive any data from server!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            VoteActivity.this, candidatesList,
                            R.layout.list_item, new String[]{
                            TAG_NAME,
                            TAG_REG,
                            TAG_SEAT_NAME},
                            new int[]{
                                    R.id.nameCandidate,
                                    R.id.regCandidate,
                                    R.id.seatNameCandidate});
                    // updating listview
                    setListAdapter(adapter);

                }
            });
        }

    }


    private class AddNewCategory extends AsyncTask<String, Void, Void> {

        boolean isNewCategoryCreated = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(VoteActivity.this);
            pDialog.setMessage("Loading Candidates..");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... arg) {

            String newCategory = arg[0];

            // Preparing post params
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("seat_name", newCategory));

            ServiceHandler serviceClient = new ServiceHandler();

            String json = serviceClient.makeServiceCall(AppConfig.URL_SELECTED_SEAT,
                    ServiceHandler.POST, params);

            Log.e("Create Response: ", "> " + json);

            if (json != null) {
                try {
                    JSONObject jsonObj = new JSONObject(json);
                    boolean error = jsonObj.getBoolean("error");
                    // checking for error node in json
                    if (!error) {
                        // new seat_name created successfully
                        isNewCategoryCreated = true;
                    } else {
                        Log.e("Create Category Error: ", "> " + jsonObj.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.e("JSON Data", "Didn't receive any data from server!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            if (isNewCategoryCreated) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // fetching all candidates
                        new GetCategories().execute();
                    }
                });
            }
        }
    }
}

