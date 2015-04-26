package com.sharemee.app.sharemee.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharemee.app.sharemee.util.JSONParser;
import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.DownloadImageTask;
import com.sharemee.app.sharemee.util.MyLocationListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ObjectPresentationActivity extends BaseActivity {

    String idObject;

    TextView objectName;
    TextView objectDesc;
    TextView objectCategory;
    TextView objectUsername;
    TextView objectCity;
    TextView objectDistance;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // url to get all objects list
    private static String url_object_detail = "http://sharemee.com/webservice/model/get_object_details.php";
    //private static String url_object_detail = "http://10.0.2.2/sharemee/webservice/model/get_object_details.php";

    private static String url_object_image = "http://sharemee.com/webservice/images/";
    //private static String url_object_image = "http://10.0.2.2/sharemee/webservice/images/no-image.jpg";

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

    private TextView contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_object_presentation, null,false);
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
        objectCity = (TextView) findViewById(R.id.objectPresentationItemCityName);
        objectDistance = (TextView) findViewById(R.id.objectPresentationItemDistance);

        // Loading objects in Background Thread
        new LoadObjectDetails().execute();

        contact = (TextView) findViewById(R.id.contactObject);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactActivity.class);
                intent.putExtra(TAG_ID_OBJECT, idObject);
                startActivity(intent);
            }
        });

    }


    class LoadObjectDetails extends AsyncTask<String, String, JSONObject>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ObjectPresentationActivity.this);
            pDialog.setMessage("Recherche de l'objet...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            //Looper.prepare();
            int success;
            Context myContext;
            myContext=getApplicationContext();
            LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            LocationListener mlocListener = new MyLocationListener();
            Location location = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (!MyLocationListener.isDeviceSupportLocation(myContext)) {
                latitudePhone=0.0;
                longitudePhone=0.0;
            }else{
                mlocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
                latitudePhone=location.getLatitude();
                longitudePhone=location.getLongitude();}

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
                    Context context = getBaseContext();


                    try {
                        Log.d("object name :", object1.getString(TAG_NAME_OBJECT));

                        String longObjectSt = object1.getString(TAG_LONG_OBJECT);
                        String latObjectSt = object1.getString(TAG_LAT_OBJECT);
                        //Calcul de la distance
                        String dist="";
                        if(longObjectSt!="null"&&latObjectSt!="null"){
                            Double longObject = Double.parseDouble(longObjectSt);
                            Double latObject = Double.parseDouble(latObjectSt);
                            if (MyLocationListener.isDeviceSupportLocation(getApplicationContext())) {
                                String distanceCalcule =MyLocationListener.calculerDistance(latitudePhone, longitudePhone, latObject, longObject);
                                dist= String.valueOf(distanceCalcule)+" km";}}


                        //The cardviews are set
                        objectName.setText(object1.getString(TAG_NAME_OBJECT));
                        objectDesc.setText(object1.getString(TAG_DESC_OBJECT));
                        objectCategory.setText(object1.getString(TAG_NAME_CATEGORY));
                        objectUsername.setText(object1.getString(TAG_NAME_USER));
                        //objectCity.setText(object1.getString(TAG_NAME_CITY));
                        objectDistance.setText(dist);

                        //Construct full image url to get the image
                        String full_image_url_1 = url_object_image + object1.getString(TAG_IMAGE_PATH_1_OBJECT)+".jpg";
                        Log.d("image path 1", full_image_url_1);


                        //The DownloadImageTask is called to get the image on the server
                        if (!object1.getString(TAG_IMAGE_PATH_1_OBJECT).equals("null")) {
                            new DownloadImageTask((ImageView) findViewById(R.id.imageViewObjectPresenation1))
                                    .execute(full_image_url_1);
                        }
                /*
                if (object1.getString(TAG_IMAGE_PATH_2_OBJECT)!=null) {
                    new DownloadImageTask((ImageView) findViewById(R.id.imageViewObjectPresenation2))
                            .execute(full_image_url_2);
                }
*/
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            });
            // dismiss the dialog once got all details
            pDialog.dismiss();
        }



    }
}
