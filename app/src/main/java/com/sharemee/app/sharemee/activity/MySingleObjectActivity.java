package com.sharemee.app.sharemee.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.ConnectionConfig;
import com.sharemee.app.sharemee.util.DownloadImageTask;
import com.sharemee.app.sharemee.util.JSONParser;
import com.sharemee.app.sharemee.util.MyLocationListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
/**
 *
 *thsi activioty shows one object of the user with different function at the end
 **/

public class MySingleObjectActivity extends BaseActivity {

    String idObject;

    TextView objectName;
    TextView objectDesc;
    TextView objectCategory;
    TextView objectUsername;
    TextView objectDistance;
    TextView delete;
    TextView modify;


    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    private String baseURL = new ConnectionConfig().getBaseURL();

    // url to get all objects list
    private String url_object_detail = baseURL + "webservice/model/get_object_details.php";
    private String url_object_delete = baseURL + "webservice/model/delete_object.php";
    private String url_object_image = baseURL + "webservice/images/";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_OBJECT = "object";
    private static final String TAG_ID_OBJECT = "idObject";
    private static final String TAG_NAME_OBJECT = "nameObject";
    private static final String TAG_DESC_OBJECT = "descObject";
    private static final String TAG_LAT_OBJECT = "latObject";
    private static final String TAG_LONG_OBJECT = "longObject";
    private static final String TAG_IMAGE_PATH_1_OBJECT = "imagePath1Object";
    private static final String TAG_ID_USER = "idUser";
    private static final String TAG_NAME_USER = "nameUser";
    private static final String TAG_SURNAME_USER = "surnameUser";
    private static final String TAG_ID_CATEGORY = "idCategory";
    private static final String TAG_NAME_CATEGORY = "nameCategory";

    private Double latitudePhone;
    private Double longitudePhone;

//creating the activity and setting listener
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_my_single_object, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        // getting product details from intent
        Intent i = getIntent();

        // getting product id (pid) from intent
        idObject = i.getStringExtra(TAG_ID_OBJECT);

        objectName = (TextView) findViewById(R.id.objectPresentationItemName);
        objectDesc = (TextView) findViewById(R.id.objectPresentationItemDescription);
        objectCategory = (TextView) findViewById(R.id.objectPresentationItemCategory);
        objectUsername = (TextView) findViewById(R.id.objectPresentationItemUsername);
        objectDistance = (TextView) findViewById(R.id.objectPresentationItemDistance);

        modify = (TextView) findViewById(R.id.modifyObject);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), ModifyObjectActivity.class);
                intent.putExtra(TAG_ID_OBJECT, idObject);
                startActivity(intent);
            }
        });

        delete = (TextView) findViewById(R.id.deleteObject);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteObject();
            }
        });
        // Loading objects in Background Thread
        new LoadObjectDetails().execute();

    }

    //Asynctask to load objects informations when the activity starts
    class LoadObjectDetails extends AsyncTask<String, String, JSONObject> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MySingleObjectActivity.this);
            pDialog.setMessage("Recherche de l'objet...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            //Looper.prepare();
            Context myContext;
            myContext = getApplicationContext();
            int success;
            LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            LocationListener mlocListener = new MyLocationListener();
            Location location = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (!MyLocationListener.isDeviceSupportLocation(myContext)) {
                latitudePhone = 0.0;
                longitudePhone = 0.0;
            } else {
                //Location has been asked in previous activity
                latitudePhone = location.getLatitude();
                longitudePhone = location.getLongitude();
            }

            Log.d("lattitudePhone :", latitudePhone.toString());
            Log.d("longitudePhone :", longitudePhone.toString());

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("idObject", idObject));

                //check params
                Log.d("params :", params.toString());

                // getting product details by making HTTP request
                // Note that product details url will use GET request
                JSONObject json = jsonParser.makeHttpRequest(
                        url_object_detail, "GET", params);

                // check your log for json response
                Log.d("Single Product Details", json.toString());

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {

                    JSONArray productObj = json
                            .getJSONArray(TAG_OBJECT); // JSON Array

                    // get first product object from JSON Array
                    JSONObject object = productObj.getJSONObject(0);
                    //check object variable
                    //Log.d("First product object from Json Array", object.toString());

                    return object;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final JSONObject object) {
            //get the parameter
            final JSONObject object1 = object;

            //Here is where the UI is called, thus the following code will appear in the User Interface Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Context myContext = getBaseContext();
                    try {

                        String longObjectSt = object1.getString(TAG_LONG_OBJECT);
                        String latObjectSt = object1.getString(TAG_LAT_OBJECT);
                        //Calcul de la distance
                        String dist = "";
                        if (longObjectSt != "null" && latObjectSt != "null") {
                            Double longObject = Double.parseDouble(longObjectSt);
                            Double latObject = Double.parseDouble(latObjectSt);
                            if (MyLocationListener.isDeviceSupportLocation(myContext)) {
                                String distanceCalcule = MyLocationListener.calculerDistance(latitudePhone, longitudePhone, latObject, longObject);
                                dist = String.valueOf(distanceCalcule) + " km";
                            }
                        }

                        Log.d("object name :", object1.getString(TAG_NAME_OBJECT));
                        //The cardviews are set
                        objectName.setText(object1.getString(TAG_NAME_OBJECT));
                        objectDesc.setText(object1.getString(TAG_DESC_OBJECT));
                        objectCategory.setText(object1.getString(TAG_NAME_CATEGORY));
                        objectUsername.setText(object1.getString(TAG_NAME_USER));
                        //objectCity.setText(object1.getString(TAG_NAME_CITY));
                        objectDistance.setText(dist);

                        //Construct full image url to get the image
                        String full_image_url_1 = url_object_image + object1.getString(TAG_IMAGE_PATH_1_OBJECT) + ".jpg";
                        Log.d("image path 1", full_image_url_1);

                        //The DownloadImageTask is called to get the image on the server
                        if (!object1.getString(TAG_IMAGE_PATH_1_OBJECT).equals("null")) {
                            new DownloadImageTask((ImageView) findViewById(R.id.imageViewObjectPresenation1))
                                    .execute(full_image_url_1);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            // dismiss the dialog once got all details
            pDialog.dismiss();
        }


    }

    //Asynctask to delete the object
    class DeleteObject extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MySingleObjectActivity.this);
            pDialog.setMessage("Suppression de l'objet ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Saving product
         */
        protected String doInBackground(String... args) {

            // getting updated data from EditTexts
            Intent i1 = getIntent();
            String idObject = i1.getStringExtra(TAG_ID_OBJECT);
            Log.d("IDobjet qu'on supp : ", idObject);


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("idObject", idObject));

            Log.d("params", params.toString());

            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_object_delete,
                    "POST", params);

            Log.d("json", json.toString());

            // check json success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about product update
                    setResult(100, i);
                    finish();
                } else {
                    // failed to update product
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
            // dismiss the dialog once product uupdated
            pDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), MyObjectsActivity.class);
            startActivity(intent);
        }
    }

//function to confirm that the user wants to delete the object
    private void confirmDeleteObject() {

        final CharSequence[] options = {"OUI", "NON"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MySingleObjectActivity.this);
        builder.setTitle("Etes vous sur de supprimer cet Objet ?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("OUI")) {
                    new DeleteObject().execute();
                } else if (options[item].equals("NON")) {

                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
}
