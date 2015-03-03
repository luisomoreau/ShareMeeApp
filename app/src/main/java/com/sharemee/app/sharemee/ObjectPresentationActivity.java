package com.sharemee.app.sharemee;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
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

    private static String url_object_image = "http://sharemee.com/webservice/images/no-image.jpg";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_OBJECT = "object";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        Context context = this.getApplicationContext();
        new LoadObjectDetails(this).execute();

        new DownloadImageTask((ImageView) findViewById(R.id.imageViewObjectPresenation))
                .execute(url_object_image);

    }

    class LoadObjectDetails extends AsyncTask<String, String, JSONObject>{
        private ObjectPresentationActivity longOperationContext = null;

        public LoadObjectDetails(ObjectPresentationActivity context) {
            longOperationContext = context;

        }

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

                    int success;

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
                            Log.d("First product object from Json Array", object.toString());

                            //Object with idObject found
                            //Text View


                            return object;

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject object) {

            final JSONObject object1 = object;
            Log.d("object 1", object1.toString());


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Context context = getBaseContext();


            try {
                Log.d("object name :", object1.getString(TAG_NAME_OBJECT));
                /*
                objectName = (TextView) longOperationContext.findViewById(R.id.objectNameMYO);
                objectDesc = (TextView) longOperationContext.findViewById(R.id.objectPresentationItemDescription);
                objectCategory = (TextView) longOperationContext.findViewById(R.id.objectPresentationItemCategory);
                objectUsername = (TextView) longOperationContext.findViewById(R.id.objectPresentationItemUsername);
                objectCity = (TextView) longOperationContext.findViewById(R.id.objectPresentationItemCityName);
                objectDistance = (TextView) longOperationContext.findViewById(R.id.objectPresentationItemDistance);
                */
                objectName.setText(object1.getString(TAG_NAME_OBJECT));
                objectDesc.setText(object1.getString(TAG_DESC_OBJECT));
                objectCategory.setText(object1.getString(TAG_NAME_CATEGORY));
                objectUsername.setText(object1.getString(TAG_NAME_USER));
                objectCity.setText(object1.getString(TAG_NAME_CITY));
                objectDistance.setText(object1.getString(TAG_LAT_OBJECT));

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
