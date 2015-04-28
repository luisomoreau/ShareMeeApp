package com.sharemee.app.sharemee.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.ConnectionConfig;
import com.sharemee.app.sharemee.util.DownloadImageTask;
import com.sharemee.app.sharemee.util.JSONParser;
import com.sharemee.app.sharemee.util.MyLocationListener;
import com.sharemee.app.sharemee.util.PrefUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marin on 28/04/2015.
 */
public class ModifyObjectActivity extends BaseActivity {
//TODO rajouter une checkbox si on veut modifier la position de l'objet

    String idObject;
    Button btnPicture;
    Button btnModify;
    ImageView objectPicture;
    TextView objectName;
    TextView objectDesc;
    Spinner objCategory;
    Spinner spinner;

    private Double longitudeObject;
    private Double latitudeObject;

    String idUser;
    public static String PREFS_USER_NAME = "user_name";

    // JSON Node names
    private static final String TAG_USER = "user";
    private static final String TAG_OBJECT = "object";
    private static final String TAG_ID_OBJECT = "idObject";
    private static final String TAG_NAME_OBJECT = "nameObject";
    private static final String TAG_DESC_OBJECT = "descObject";
    public static String PREFS_USER_ID = "user_ID";
    private static final String TAG_IMAGE_PATH_1_OBJECT = "imagePath1Object";

    final int CROP_RESULT = 3;
    final int CAMERA_CAPTURE = 1;
    final int SELECT_GALLERY = 2;
    private Uri picUri;
    Bitmap thePic;
    String imgPath, fileName;
    Bitmap bitmap;
    private static int RESULT_LOAD_IMG = 0;
    RequestParams params = new RequestParams();

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    private String baseURL = new ConnectionConfig().getBaseURL();
    // url to update product
    private String url_modify_object = baseURL + "webservice/model/modify_object.php";
    //URL to get image
    private String url_object_image = baseURL + "webservice/images/";
    private String url_object_detail = baseURL+"webservice/model/get_object_details.php";

    // Progress Dialog
    private ProgressDialog pDialog;
    // JSON Node names
    private static final String TAG_SUCCESS = "success";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_modify_object, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        // now you can do all your other stuffs


        // getting product details from intent
        Intent i = getIntent();

        // getting product id (pid) from intent
        idObject = i.getStringExtra(TAG_ID_OBJECT);
        // Loading objects in Background Thread
        new LoadObjectDetails().execute();

        /* ************************** Création du Spinner pour categories******************/

        //Récupération du Spinner déclaré dans le fichier main.xml de res/layout
        spinner = (Spinner) findViewById(R.id.spinner_category);
        //Création d'une liste d'élément à mettre dans le Spinner(pour l'exemple)
        List categoryList = new ArrayList();
        categoryList.add("Bricolage");
        categoryList.add("Cuisine");
        categoryList.add("Livre");
        categoryList.add("Jardinage");
        categoryList.add("Menage");

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

        btnPicture = (Button) findViewById(R.id.add_object_picture_button);
        btnModify = (Button) findViewById(R.id.modify_object_button);
        objectPicture = (ImageView) findViewById(R.id.add_object_picture);
        btnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        objectName = (AutoCompleteTextView) findViewById(R.id.modify_object_name);
        objectDesc = (AutoCompleteTextView) findViewById(R.id.modify_object_desc);


        // save button click event
        btnModify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update product
                String nameObject = objectName.getText().toString();
                String descObject = objectDesc.getText().toString();

                if ((!nameObject.isEmpty()) && (!descObject.isEmpty()) ) {
                    confirmModifyObject();
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Le nom ou la description de l'objet ne sont pas renseignés";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

            }
        });
    }


    private void selectImage() {

        // Check if there is a camera.
        Context context = getApplicationContext();
        PackageManager packageManager = context.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
            Toast.makeText(getApplicationContext(), "Vous n'avez pas de caméra sur votre appareil.", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        final CharSequence[] options = {"Prendre une Photo", "Choisir depuis Gallery", "Annuler"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ModifyObjectActivity.this);
        builder.setTitle("Ajouter une Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Prendre une Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    RESULT_LOAD_IMG = 1;
                    startActivityForResult(intent, CAMERA_CAPTURE);

                } else if (options[item].equals("Choisir depuis Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    RESULT_LOAD_IMG = 2;
                    startActivityForResult(intent, CAMERA_CAPTURE);

                } else if (options[item].equals("Annuler")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * *****après validation de lajout de l'image *******
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("resultCode", String.valueOf(resultCode));
        Log.d("requestCode", String.valueOf(requestCode));
        Log.d("result_ok", String.valueOf(RESULT_OK));
        Log.d("crop_result", String.valueOf(CROP_RESULT));
        Log.d("camera_capture", String.valueOf(CAMERA_CAPTURE));
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CAPTURE) {
                picUri = data.getData();
                performCrop(picUri);
            } else if (requestCode == CROP_RESULT) {
                //get the returned data
                Bundle extras = data.getExtras();
                //get the cropped bitmap
                thePic = extras.getParcelable("data");
                ImageView imgView = (ImageView) findViewById(R.id.add_picture_image);

                imgView.setImageBitmap(thePic);


                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(picUri,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                Log.d("imgPath", imgPath);
                cursor.close();

                //ImageView imgView = (ImageView) findViewById(R.id.add_picture_image);
                // Set the Image in ImageView

                //imgView.setImageBitmap(BitmapFactory.decodeFile(imgPath));
                // Get the Image's file name
                String fileNameSegments[] = imgPath.split("/");
                fileName = fileNameSegments[fileNameSegments.length - 1];
                Log.d("fileName", fileName);
                // Put file name in Async Http Post Param which will used in Php web app
                params.put("filename", fileName);


            }
        }
    }

    private void performCrop(Uri selectedImage) {
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(selectedImage, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 300);
            cropIntent.putExtra("outputY", 300);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, CROP_RESULT);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    class ModifyObject extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ModifyObjectActivity.this);
            pDialog.setMessage("Modification de l'objet ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Saving product
         */
        protected String doInBackground(String... args) {

            // getting updated data from EditTexts

            String savedUserId = PrefUtils.getFromPrefs(ModifyObjectActivity.this, PREFS_USER_ID, "0");
            Log.d("savedUserId", savedUserId);
            idUser = savedUserId;
            String nameObject = objectName.getText().toString();
            String descObject = objectDesc.getText().toString();
            String catObject = objCategory.getSelectedItem().toString();
            String idCategory = "";
            if (catObject.compareTo("Bricolage") == 0) {
                idCategory = "1";
            } else if (catObject.compareTo("Cuisine") == 0) {
                idCategory = "2";
            } else if (catObject.compareTo("Livre") == 0) {
                idCategory = "3";
            } else if (catObject.compareTo("Jardinage") == 0) {
                idCategory = "4";
            } else if (catObject.compareTo("Menage") == 0) {
                idCategory = "5";
            }
            Context myContext;
            myContext = getApplicationContext();
            LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            LocationListener mlocListener = new MyLocationListener();
            Location location = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (!MyLocationListener.isDeviceSupportLocation(myContext)) {
                latitudeObject = 0.01;
                longitudeObject = 0.01;
            } else {
                mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
                latitudeObject = location.getLatitude();
                longitudeObject = location.getLongitude();
            }


            Log.d("nameObject : ", nameObject);
            Log.d("descObject : ", descObject);
            Log.d("catObject : ", idCategory);
            Log.d("imagePath1Object : ", fileName);
            Log.d("latObject : ", latitudeObject.toString());
            Log.d("longObject : ", longitudeObject.toString());


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("nameObject", nameObject));
            params.add(new BasicNameValuePair("descObject", descObject));
            params.add(new BasicNameValuePair("latObject", latitudeObject.toString()));
            params.add(new BasicNameValuePair("longObject", longitudeObject.toString()));
            params.add(new BasicNameValuePair("smUser_idUser", idUser));
            params.add(new BasicNameValuePair("smCategory_idCategory", idCategory));
            if (fileName != null) {
                fileName = fileName.substring(0, fileName.length() - 4);
                params.add(new BasicNameValuePair("imagePath1Object", fileName.toString()));
            }


            Log.d("params", params.toString());

            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_modify_object,
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
            Intent in = new Intent(getApplicationContext(),
                    MySingleObjectActivity.class);
            // sending idObject to next activity
            in.putExtra(TAG_ID_OBJECT, idObject);
            Log.d("Id objet récupéré : ", idObject );

            // starting new activity and expecting some response back
            startActivityForResult(in, 100);
        }


    }

    class LoadObjectDetails extends AsyncTask<String, String, JSONObject>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ModifyObjectActivity.this);
            pDialog.setMessage("Recherche de l'objet...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            //Looper.prepare();
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
                        //The cardviews are set
                        objectName.setText(object1.getString(TAG_NAME_OBJECT));
                        objectDesc.setText(object1.getString(TAG_DESC_OBJECT));


                        //Construct full image url to get the image
                        String full_image_url_1 = url_object_image + object1.getString(TAG_IMAGE_PATH_1_OBJECT)+".jpg";
                        Log.d("image path 1", full_image_url_1);

                        //The DownloadImageTask is called to get the image on the server
                        if (!object1.getString(TAG_IMAGE_PATH_1_OBJECT).equals("null")) {
                            new DownloadImageTask((ImageView) findViewById(R.id.imageViewObjectPresenation1))
                                    .execute(full_image_url_1);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            });
            // dismiss the dialog once got all details
            pDialog.dismiss();
        }


    }
    public void confirmModifyObject() {

        final CharSequence[] options = {"OUI", "NON"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ModifyObjectActivity.this);
        builder.setTitle("Etes vous sur de vouloir modifier cet objet ?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("OUI")) {
                    new ModifyObject().execute();
                } else if (options[item].equals("NON")) {

                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(550, 300);
    }
}
