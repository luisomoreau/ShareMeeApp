package com.sharemee.app.sharemee;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ResultSearchActivity extends ListActivity {


    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> objectsList;

    // url to get all objects list
    private static String url_all_objects = "http://sharemee.com/webservice/model/get_all_objects.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_OBJECTS = "objects";
    private static final String TAG_ID_OBJECT = "idObject";
    private static final String TAG_NAME_OBJECT = "nameObject";
    private static final String TAG_NAME_CATEGORY = "nameCategory";
    private static final String TAG_NAME_CITY = "nameCity";

    // objects JSONArray
    JSONArray objects = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_search);

        // Hashmap for ListView
        objectsList = new ArrayList<HashMap<String, String>>();

        // Loading objects in Background Thread
        new LoadAllProducts().execute();

        // Get listview
        ListView lv = getListView();

        // on seleting single product
        // launching  Object Presentation Screen
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String idObject = ((TextView) view.findViewById(R.id.idObjectSearch)).getText()
                        .toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        ObjectPresentationActivity.class);
                // sending idObject to next activity
                in.putExtra(TAG_ID_OBJECT, idObject);
                Log.d("Id de l'objet récupéré : ", idObject );

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });

    }

    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ResultSearchActivity.this);
            pDialog.setMessage("Chargement en cours...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All objects from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_objects, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // objects found
                    // Getting Array of Products
                    objects = json.getJSONArray(TAG_OBJECTS);

                    // looping through All Products
                    for (int i = 0; i < objects.length(); i++) {
                        JSONObject c = objects.getJSONObject(i);

                        // Storing each json item in variable
                        String idObject = c.getString(TAG_ID_OBJECT);
                        String nameObject = c.getString(TAG_NAME_OBJECT);
                        String nameCategory = c.getString(TAG_NAME_CATEGORY);
                        String nameCity = c.getString(TAG_NAME_CITY);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_ID_OBJECT, idObject);
                        map.put(TAG_NAME_OBJECT, nameObject);
                        map.put(TAG_NAME_CATEGORY, nameCategory);
                        map.put(TAG_NAME_CITY, nameCity);

                        // adding HashList to ArrayList
                        objectsList.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all objects
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            ResultSearchActivity.this, objectsList,
                            R.layout.search_item, new String[] { TAG_ID_OBJECT, TAG_NAME_OBJECT, TAG_NAME_CATEGORY, TAG_NAME_CITY},
                            new int[] { R.id.idObjectSearch, R.id.objectNameSearch, R.id.objectCategorieSearch, R.id.objectDistanceSearch });
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}
