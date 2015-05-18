package com.sharemee.app.sharemee.activity;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.ConnectionConfig;
import com.sharemee.app.sharemee.util.JSONParser;
import com.sharemee.app.sharemee.util.MyLocationListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchableActivity extends BaseActivity {

    Spinner spinner;
    private AutoCompleteTextView search_field;
    private Spinner search_category;
    private String search_text;
    private String category;
    private ImageView doSearch;

    /* Variable for listViewResults*/


    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> objectsList;

    private String baseURL = new ConnectionConfig().getBaseURL();

    // url to get all objects list
    private String url_search_objects = baseURL + "webservice/model/result_search.php";
    //private static String url_all_objects = "http://10.0.2.2/sharemee/webservice/model/get_all_objects.php";

    private String url_object_image = baseURL + "webservice/images/";

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_search, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);



                /* ************************** Création du Spinner pour categories******************/

        //Récupération du Spinner déclaré dans le fichier main.xml de res/layout
        spinner = (Spinner) findViewById(R.id.spinner_category_search);
        //Création d'une liste d'élément à mettre dans le Spinner(pour l'exemple)
        List categoryList = new ArrayList();
        categoryList.add("Toutes catégories");
        categoryList.add("Livres");
        categoryList.add("Jardinage");
        categoryList.add("Bricolage");
        categoryList.add("Cuisine");
        categoryList.add("Ménage");
        categoryList.add("Sport");
        categoryList.add("Autres");

 /*Le Spinner a besoin d'un adapter pour sa presentation alors on lui passe le context(this) et
                un fichier de presentation par défaut( android.R.layout.simple_spinner_item)
 Avec la liste des elements (exemple) */
        ArrayAdapter adapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                categoryList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       /* On definit une présentation du spinner quand il est déroulé         (android.R.layout.simple_spinner_dropdown_item) */
        //Enfin on passe l'adapter au Spinner et c'est tout
        spinner.setAdapter(adapter);

        doSearch = (ImageView) findViewById(R.id.do_search_but);
        search_field = (AutoCompleteTextView) findViewById(R.id.my_search_field);
        search_category = (Spinner) findViewById(R.id.spinner_category_search);

        // save button click event
        doSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update product
                search_text = search_field.getText().toString();
                category = search_category.getSelectedItem().toString();

                // Hashmap for ListView
                objectsList = new ArrayList<HashMap<String, String>>();

                // Loading objects in Background Thread
                new doMySearch().execute();

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
                                ObjectPresentationActivity.class);
                        // sending idObject to next activity
                        in.putExtra(TAG_ID_OBJECT, idObject);
                        Log.d("Id objet récupéré : ", idObject);

                        // starting new activity and expecting some response back
                        startActivityForResult(in, 100);
                    }
                });

            }

        });

    }

    class doMySearch extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        private ListView lv;
        private String full_image_url_1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SearchableActivity.this);
            pDialog.setMessage("Chargement en cours...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All objects from url
         */
        protected String doInBackground(String... args) {

            //Looper.prepare();
            Context myContext;
            myContext = getApplicationContext();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("search_field", search_text));
            params.add(new BasicNameValuePair("category", category));

            Log.d("params", params.toString());
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_search_objects, "POST", params);

            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            //Requesting location data
            LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            LocationListener mlocListener = new MyLocationListener();
            Location location = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (!MyLocationListener.isDeviceSupportLocation(myContext)) {

                latitudePhone = 0.0;
                longitudePhone = 0.0;
            } else {
                //mlocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
                latitudePhone = location.getLatitude();
                longitudePhone = location.getLongitude();
            }

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
                        Log.d("objects", c.toString());

                        // Storing each json item in variable
                        String idObject = c.getString(TAG_ID_OBJECT);
                        String nameObject = c.getString(TAG_NAME_OBJECT);
                        String nameCategory = c.getString(TAG_NAME_CATEGORY);
                        //String nameCity = c.getString(TAG_NAME_CITY);
                        String longObjectSt = c.getString(TAG_LONG_OBJECT);
                        String latObjectSt = c.getString(TAG_LAT_OBJECT);
                        String imagePath1Object = c.getString(TAG_IMAGE_PATH_OBJECT);


                        //Distance
                        String dist = "";
                        if (longObjectSt != "null" && latObjectSt != "null") {
                            Double longObject = Double.parseDouble(longObjectSt);
                            Double latObject = Double.parseDouble(latObjectSt);
                            if (MyLocationListener.isDeviceSupportLocation(myContext)) {
                                String distanceCalcule = MyLocationListener.calculerDistance(latitudePhone, longitudePhone, latObject, longObject);
                                dist = String.valueOf(distanceCalcule) + " km";
                            }
                        }

                        //Image
                        //Construct full image url to get the image
                        full_image_url_1 = url_object_image + imagePath1Object + ".jpg";

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
         * *
         */
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
                            SearchableActivity.this, objectsList,
                            R.layout.search_item, new String[]{TAG_ID_OBJECT, TAG_NAME_OBJECT, TAG_NAME_CATEGORY, TAG_NAME_CITY},
                            new int[]{R.id.idObjectSearch, R.id.objectNameSearch, R.id.objectCategorieSearch, R.id.objectDistanceSearch,});
                    // updating listview
                    lv.setAdapter(adapter);
                }
            });

        }


    }
}

