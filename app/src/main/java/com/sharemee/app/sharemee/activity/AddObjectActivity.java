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
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.*;
import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.ConnectionConfig;
import com.sharemee.app.sharemee.util.JSONParser;
import com.sharemee.app.sharemee.util.MyLocationListener;
import com.sharemee.app.sharemee.util.PrefUtils;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class AddObjectActivity extends BaseActivity {


    AutoCompleteTextView objName;
    AutoCompleteTextView objDescription;
    ImageView objImage;
    CardView btnAddObject;
    Button btnImage;
    Spinner objCategory;
    Spinner spinner;
    String idUser;

    final int CROP_RESULT = 3;
    final int CAMERA_CAPTURE = 1;
    final int SELECT_GALLERY = 2;
    private Uri picUri;
    Bitmap thePic;


    private Double longitudeObject;
    private Double latitudeObject;
    public static String PREFS_USER_ID = "user_ID";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";


    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    private String baseURL = new ConnectionConfig().getBaseURL();

    // url to update product
    private String url_add_object = baseURL + "webservice/model/add_object.php";
    //private static final String url_add_object = "http://192.168.1.34/ShareMeeWeb/webservice/model/add_object.php";
    //private static final String url_add_object = "http://10.0.2.2/sharemee/webservice/model/add_object.php";

    private String url_upload_image = baseURL + "webservice/model/upload_picture.php";

    //Upload Image
    ProgressDialog prgDialog;
    String encodedString;
    RequestParams params = new RequestParams();
    String imgPath, fileName;
    Bitmap bitmap;
    private static int RESULT_LOAD_IMG = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_add_object, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        // now you can do all your other stuffs

        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);


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


        /****Récupération image et bouton ****/

        btnImage = (Button) findViewById(R.id.add_picture_button);
        objImage = (ImageView) findViewById(R.id.add_picture_image);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        objName = (AutoCompleteTextView) findViewById(R.id.add_object_name);
        objDescription = (AutoCompleteTextView) findViewById(R.id.add_object_description);
        objImage = (ImageView) findViewById(R.id.add_picture_image);
        objCategory = (Spinner) findViewById(R.id.spinner_category);

        btnAddObject = (CardView) findViewById(R.id.add_object_button);


        // save button click event
        btnAddObject.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update product
                String nameObj = objName.getText().toString();
                String descObj = objDescription.getText().toString();
                //String catObj= " t";

                if ((!nameObj.isEmpty()) && (!descObj.isEmpty())) {
                    new AddObject().execute();
                    encodeImagetoString();
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Le nom ou la description de l'objet ne sont pas renseignés";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

                encodeImagetoString();

            }
        });
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

        AlertDialog.Builder builder = new AlertDialog.Builder(AddObjectActivity.this);
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


    /**
     * Background Async Task to  Save product Details
     */
    class AddObject extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddObjectActivity.this);
            pDialog.setMessage("Ajout d'un objet...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Saving product
         */
        protected String doInBackground(String... args) {


//TODO rajouter envoi de l'image à la base de données
            // getting updated data from EditTexts

            String savedUserId = PrefUtils.getFromPrefs(AddObjectActivity.this, PREFS_USER_ID, "0");
            Log.d("savedUserId", savedUserId);
            idUser = savedUserId;
            String nameObject = objName.getText().toString();
            String descObject = objDescription.getText().toString();
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
            JSONObject json = jsonParser.makeHttpRequest(url_add_object,
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
            encodeImagetoString();
        }
    }


    // When Upload button is clicked
    public void uploadImage(View v) {
        // When Image is selected from Gallery
        if (imgPath != null && !imgPath.isEmpty()) {
            prgDialog.setMessage("Converting Image to Binary Data");
            prgDialog.show();
            // Convert image to String using Base64
            encodeImagetoString();
            // When Image is not selected from Gallery
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "You must select image from gallery before you try to upload",
                    Toast.LENGTH_LONG).show();
        }
    }

    // AsyncTask - To convert Image to String
    public void encodeImagetoString() {
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {

            }

            ;

            @Override
            protected String doInBackground(Void... params) {
                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                bitmap = thePic;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                prgDialog.setMessage("Calling Upload");
                // Put converted Image string into Async Http Post param
                params.put("image", encodedString);
                // Trigger Image upload
                triggerImageUpload();
            }
        }.execute(null, null, null);
    }

    public void triggerImageUpload() {
        makeHTTPCall();
    }

    // http://192.168.2.4:9000/imgupload/upload_image.php
    // http://192.168.2.4:9999/ImageUploadWebApp/uploadimg.jsp
    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {
        prgDialog.setMessage("Envois de la photo");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(url_upload_image,
                params, new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        Toast.makeText(getApplicationContext(), "Image envoyée",
                                Toast.LENGTH_LONG).show();
                    }

                    // When the response returned by REST has Http
                    // response code other than '200' such as '404',
                    // '500' or '403' etc
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(),
                                    "Requested resource not found",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong at server end",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error Occured \n Most Common Error: \n1. Device not connected to Internet\n2. Web App is not deployed in App server\n3. App server is not running\n HTTP Status code : "
                                            + statusCode, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }


                });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }


}
