package com.sharemee.app.sharemee;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ObjectPresentationActivity extends ListActivity {

    String idObject;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> objectDetailList;

    // url to get all objects list
    //private static String url_product_details = "http://sharemee.com/webservice/model/get_object_info.php";
    private static String url_product_details = "http://localhost/sharemee/webservice/model/get_object_info.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_OBJECTS = "objects";
    private static final String TAG_ID_OBJECT = "idObject";
    private static final String TAG_NAME_OBJECT = "nameObject";
    private static final String TAG_DESC_OBJECT = "descObject";
    private static final String TAG_LAT_OBJECT = "latObject";
    private static final String TAG_LONG_OBJECT = "longObject";
    private static final String TAG_YEAR_OBJECT = "yearObject";
    private static final String TAG_DATE_OBJECT = "addedDateTimeObject";
    private static final String TAG_ID_USER = "idUser";
    private static final String TAG_NAME_USER = "nameUser";
    private static final String TAG_SURNAME_USER = "surnameUser";
    private static final String TAG_ID_CATEGORY = "idCategory";
    private static final String TAG_NAME_CATEGORY = "nameCategory";
    private static final String TAG_ID_CITY = "idCity";
    private static final String TAG_NAME_CITY = "nameCity";
    private static final String TAG_ZIPCODE_CITY = "zipcodeCity";



    // objects JSONArray
    JSONArray objects = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_presentation);

        // Hashmap for ListView
        objectDetailList = new ArrayList<HashMap<String, String>>();

        // getting product details from intent
        Intent i = getIntent();

        // getting product id (pid) from intent
        idObject = i.getStringExtra(TAG_ID_OBJECT);

        // Loading objects in Background Thread
        new LoadProductDetails().execute();

        // Get listview
        ListView lv = getListView();

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
    class LoadProductDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ObjectPresentationActivity.this);
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
            params.add(new BasicNameValuePair("idObject", idObject));
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_product_details, "GET", params);

            // Check your log cat for JSON response
            Log.d("Product Details: ", json.toString());

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
                        objectDetailList.add(map);
                    }
                }/* else {
                    // no objects found
                    // Launch Add New product Activity
                    Intent i = new Intent(getApplicationContext(),
                            NewProductActivity.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }*/
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
                            ObjectPresentationActivity.this, objectDetailList,
                            R.layout.search_item, new String[] { TAG_ID_OBJECT, TAG_NAME_OBJECT},
                            new int[] { R.id.objectPresentationItemLabel, R.id.objectPresentationItemDescription});
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}
