package com.sharemee.app.sharemee.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.ConnectionConfig;
import com.sharemee.app.sharemee.util.JSONParser;
import com.sharemee.app.sharemee.util.MyLocationListener;
import com.sharemee.app.sharemee.util.PrefUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MyObjectsActivity extends BaseActivity {

    //Store user_ID variable declarations
    String idUser;
    public static String PREFS_USER_ID = "user_ID" ;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> objectsList;

    private String baseURL = new ConnectionConfig().getBaseURL();

    // url to get all objects list
    private String url_user_objects = baseURL+ "webservice/model/get_user_objects.php";
    //private static String url_user_objects = "http://10.0.2.2/sharemee/webservice/model/get_user_objects.php";


    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_OBJECTS = "objects";
    private static final String TAG_ID_OBJECT = "idObject";
    private static final String TAG_NAME_OBJECT = "nameObject";
    private static final String TAG_IMAGE_PATH_OBJECT = "imagePath1Object";
    private static final String TAG_NAME_CATEGORY = "nameCategory";
    private static final String TAG_NAME_CITY = "nameCity";
    private static final String TAG_LONG_OBJECT = "longObject";
    private static final String TAG_LAT_OBJECT = "latObject";
    private Double latitudePhone;
    private Double longitudePhone;

    // objects JSONArray
    JSONArray objects = null;

    private ListView lv;
    private CardView btnAddObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_my_object, null,false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        //parameter
        String savedUserId = PrefUtils.getFromPrefs(MyObjectsActivity.this, PREFS_USER_ID, "0");
        Log.d("savedUserId",savedUserId);
        idUser = savedUserId;

        // Hashmap for ListView
        objectsList = new ArrayList<HashMap<String, String>>();

        // Loading objects in Background Thread
        new LoadAllProducts().execute();

        // Get listview
        lv = (ListView) findViewById(R.id.list_view_activity_results);

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
                        MySingleObjectActivity.class);
                // sending idObject to next activity
                in.putExtra(TAG_ID_OBJECT, idObject);
                Log.d("Id objet récupéré : ", idObject );

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });
        btnAddObj = (CardView) findViewById(R.id.add_object_buttonProf);
        btnAddObj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddObjectActivity.class);
                startActivity(intent);
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
        private ListView lv;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MyObjectsActivity.this);
            pDialog.setMessage("Chargement en cours...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();


        }

        /**
         * getting All objects from url
         * */
        protected String doInBackground(String... args) {

            Looper.prepare();


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idUser", idUser));

            //check params
            Log.d("params :", params.toString());

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_user_objects, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            LocationListener mlocListener = new MyLocationListener();
            Location location = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            mlocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);

            if (!isDeviceSupportLocation()) {
                latitudePhone=0.0;
                longitudePhone=0.0;
            }else{
                latitudePhone=location.getLatitude();
                longitudePhone=location.getLongitude();}

            Log.d("lattitudePhone :", latitudePhone.toString());
            Log.d("longitudePhone :", longitudePhone.toString());


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
                        //String nameCity = c.getString(TAG_NAME_CITY);
                        String longObjectSt = c.getString(TAG_LONG_OBJECT);
                        String latObjectSt = c.getString(TAG_LAT_OBJECT);


                        String dist="";
                        if(longObjectSt!="null"&&latObjectSt!="null"){
                            Double longObject = Double.parseDouble(longObjectSt);
                            Double latObject = Double.parseDouble(latObjectSt);

                            if (isDeviceSupportLocation()) {
                            String distanceCalcule =MyLocationListener.calculerDistance(latitudePhone, longitudePhone, latObject, longObject);
                            dist= String.valueOf(distanceCalcule)+" km";}}


                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_ID_OBJECT, idObject);
                        map.put(TAG_NAME_OBJECT, nameObject);
                        map.put(TAG_NAME_CATEGORY, nameCategory);
                        map.put(TAG_NAME_CITY, dist);

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

            lv = (ListView) findViewById(R.id.list_view_activity_results);

            // dismiss the dialog after getting all objects
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            MyObjectsActivity.this, objectsList,
                            R.layout.search_item, new String[] { TAG_ID_OBJECT, TAG_NAME_OBJECT, TAG_NAME_CATEGORY, TAG_NAME_CITY},
                            new int[] { R.id.idObjectSearch, R.id.objectNameSearch, R.id.objectCategorieSearch, R.id.objectDistanceSearch });
                    // updating listview
                    lv.setAdapter(adapter);
                }
            });

        }

        private boolean isDeviceSupportLocation() {
            if (getApplicationContext().getPackageManager().hasSystemFeature(
                    LOCATION_SERVICE)) {
                // this device has a camera
                return true;
            } else {
                // no camera on this device
                return false;
            }
        }



    }
}
