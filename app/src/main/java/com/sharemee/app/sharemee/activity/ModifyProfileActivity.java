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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.ConnectionConfig;
import com.sharemee.app.sharemee.util.DownloadImageTask;
import com.sharemee.app.sharemee.util.JSONParser;
import com.sharemee.app.sharemee.util.PrefUtils;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class ModifyProfileActivity extends BaseActivity {

    Button btnPicture;
    Button btnModify;
    ImageView userPicture;
    TextView userName;
    TextView userSurname;
    TextView userMail;
    TextView userPassword;
    AutoCompleteTextView txtReenterpassword;

    String idUser;
    public static String PREFS_USER_ID = "user_ID" ;
    public static String PREFS_USER_NAME = "user_name" ;

    // JSON Node names
    private static final String TAG_USER = "user";
    private static final String TAG_ID_USER = "idUser";
    private static final String TAG_NAME_USER = "nameUser";
    private static final String TAG_SURNAME_USER = "surnameUser";
    private static final String TAG_MAIL_USER = "mailUser";
    private static final String TAG_IMAGE_PROFILE_PICTURE = "profilPictureUser";

    final int CROP_RESULT = 3;
    final int CAMERA_CAPTURE = 1;
    final int SELECT_GALLERY = 2;



    //Upload Image

    private String encodedString;
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
    private String url_modify = baseURL+"webservice/model/modify_user.php";
    // url to get user details
    private String url_user_details = baseURL+"webservice/model/get_user_details.php";
    //URL to get image
    private String url_user_image = baseURL+"webservice/images/";

    private String url_upload_image = baseURL + "webservice/model/upload_picture.php";

    // Progress Dialog
    private ProgressDialog pDialog;
    // JSON Node names
    private static final String TAG_SUCCESS = "success";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_modify_profile, null,false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        // now you can do all your other stuffs

        /****Récupération image et bouton ****/
        String savedUserId = PrefUtils.getFromPrefs(ModifyProfileActivity.this, PREFS_USER_ID, "0");
        Log.d("savedUserId",savedUserId);
        idUser = savedUserId;
        // Loading objects in Background Thread
        new LoadUserDetails().execute();

        btnPicture=(Button)findViewById(R.id.add_user_picture_button);
        btnModify=(Button)findViewById(R.id.modify_user_profile_button);
        userPicture=(ImageView)findViewById(R.id.add_user_picture);
        btnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        txtReenterpassword = (AutoCompleteTextView) findViewById(R.id.modify_user_profile_reenter_password);


        userName =(AutoCompleteTextView)findViewById(R.id.modify_user_profile_name);
        userMail =(AutoCompleteTextView)findViewById(R.id.modify_user_profile_email);
        userSurname =(AutoCompleteTextView)findViewById(R.id.modify_user_profile_surname);
        userPassword =(AutoCompleteTextView)findViewById(R.id.modify_user_profile_password);


        // save button click event
        btnModify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update product
                String nameUser = userName.getText().toString();
                String mailUser = userMail.getText().toString();
                String surnameUser = userSurname.getText().toString();
                String password = userPassword.getText().toString();
                String password2 = txtReenterpassword.getText().toString();

                if ((!nameUser.isEmpty())&&(!surnameUser.isEmpty())&&(!mailUser.isEmpty())&&(!password.isEmpty())&&(!password2.isEmpty())) {

                    if (password.equals(password2)) {
                        confirmModifyUser();
                    }else {
                        Context context = getApplicationContext();
                        CharSequence text = "Les mots de passes ne correspondent pas";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }
                else {
                    Context context = getApplicationContext();
                    CharSequence text = "Un ou plusieurs champs ne sont pas renseignés";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

            }});}


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

        AlertDialog.Builder builder = new AlertDialog.Builder(ModifyProfileActivity.this);
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
                ImageView imgView = (ImageView) findViewById(R.id.add_user_picture);

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

    class ModifyProfile extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ModifyProfileActivity.this);
            pDialog.setMessage("Modification du profil ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Saving product
         * */
        protected String doInBackground(String... args) {

            // getting updated data from EditTexts

            String nameUser = userName.getText().toString();
            String surnameUser = userSurname.getText().toString();
            String mailUser = userMail.getText().toString();
            String passwordUser = userPassword.getText().toString();

            Log.d("nameUser", nameUser);
            Log.d("surnameUser", surnameUser);
            Log.d("mailUser", mailUser);
            Log.d("passwordUser", passwordUser);
            Log.d("idUser", idUser);
            Log.d("filePath", fileName);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("idUser", idUser));
            params.add(new BasicNameValuePair("nameUser", nameUser));
            params.add(new BasicNameValuePair("surnameUser", surnameUser));
            params.add(new BasicNameValuePair("mailUser", mailUser));
            params.add(new BasicNameValuePair("passwordUser", passwordUser));
            if (fileName != null) {
                fileName = fileName.substring(0, fileName.length() - 4);
                params.add(new BasicNameValuePair("profilPictureUser", fileName));
            }
            else{
                params.add(new BasicNameValuePair("profilPictureUser", "NULL"));
            }

            Log.d("params", params.toString());

            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_modify,
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
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product uupdated
            pDialog.dismiss();
            encodeImagetoString();

        }


    }
    class LoadUserDetails extends AsyncTask<String, String, JSONObject>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ModifyProfileActivity.this);
            pDialog.setMessage("Chargement du Profil");
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
                params.add(new BasicNameValuePair("idUser", idUser));

                //check params
                Log.d("params :", params.toString());

                // getting product details by making HTTP request
                // Note that product details url will use GET request
                JSONObject json = jsonParser.makeHttpRequest(
                        url_user_details, "GET", params);

                // check your log for json response
                Log.d("Single User Profile", json.toString());

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {

                    JSONArray productObj = json
                            .getJSONArray(TAG_USER); // JSON Array

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
        protected void onPostExecute(final JSONObject user) {
            //get the parameter
            final JSONObject user1 = user;

            //Here is where the UI is called, thus the following code will appear in the User Interface Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Context context = getBaseContext();

                    try {
                        Log.d("user name :", user1.getString(TAG_NAME_USER));
                        //The cardviews are set
                        userName.setText(user1.getString(TAG_NAME_USER));
                        userSurname.setText(user1.getString(TAG_SURNAME_USER));
                        userMail.setText(user1.getString(TAG_MAIL_USER));

                        PrefUtils.saveToPrefs(ModifyProfileActivity.this, PREFS_USER_NAME, user1.getString(TAG_NAME_USER));

                        //Construct full image url to get the image
                        String full_image_url_1 = url_user_image + user1.getString(TAG_IMAGE_PROFILE_PICTURE)+".jpg";
                        Log.d("image path 1", full_image_url_1);

                        //The DownloadImageTask is called to get the image on the server
                        if (!user1.getString(TAG_IMAGE_PROFILE_PICTURE).equals("null")) {
                            new DownloadImageTask((ImageView) findViewById(R.id.add_user_picture))
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

    // AsyncTask - To convert Image to String
    public void encodeImagetoString() {
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {
                Log.d("encodeImageToString", "encodeImage_pre");
            }

            @Override
            protected String doInBackground(Void... params) {
                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                bitmap = thePic;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                Log.d("encode image do in back", "encDoInBack");
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                // prgDialog.setMessage("Calling Upload");
                // Put converted Image string into Async Http Post param
                params.put("image", encodedString);
                Log.d("encode image post", "encImPost");
                // Trigger Image upload
                triggerImageUpload();
            }
        }.execute(null, null, null);
    }

    public void triggerImageUpload() {
        makeHTTPCall();
    }

    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {
        Log.d("makeHttpCall","MakeHttpCall");
        //prgDialog.setMessage("Envois de la photo");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(url_upload_image,
                params, new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        // Hide Progress Dialog
                        //prgDialog.hide();
                        Toast.makeText(getApplicationContext(), "Image envoyée",
                                Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                        startActivity(intent);
                    }

                    // When the response returned by REST has Http
                    // response code other than '200' such as '404',
                    // '500' or '403' etc
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        // Hide Progress Dialog
                        //prgDialog.hide();
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

    public void confirmModifyUser() {

        final CharSequence[] options = { "OUI", "NON"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ModifyProfileActivity.this);
        builder.setTitle("Etes vous sur de vouloir modifier votre profil ?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("OUI"))
                {
                    new ModifyProfile().execute();

                }
                else if (options[item].equals("NON")){

                    dialog.dismiss();

                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        //alertDialog.getWindow().setLayout(550, 300);
    }
}



