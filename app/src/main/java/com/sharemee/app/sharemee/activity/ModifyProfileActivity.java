package com.sharemee.app.sharemee.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

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

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    // url to update product
    private static final String url_modify = "http://sharemee.com/webservice/model/signin.php";

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

        btnPicture=(Button)findViewById(R.id.add_user_picture_button);
        btnModify=(Button)findViewById(R.id.modify_user_profile_button);
        userPicture=(ImageView)findViewById(R.id.add_user_picture);
        btnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        userName =(AutoCompleteTextView)findViewById(R.id.modify_user_profile_name);
        userMail =(AutoCompleteTextView)findViewById(R.id.modify_user_profile_email);
        userSurname =(AutoCompleteTextView)findViewById(R.id.modify_user_profile_name);


        // save button click event
        btnModify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update product
                String nameUser = userName.getText().toString();
                String mailUser = userMail.getText().toString();
                String surnameUser = userSurname.getText().toString();

                if ((!nameUser.isEmpty())&&(!mailUser.isEmpty())&&(!mailUser.isEmpty())) {
                    new ModifyProfile().execute();
                }
                else {
                    Context context = getApplicationContext();
                    CharSequence text = "Le nom ou la description de l'objet ne sont pas renseignés";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

            }});}


    private void selectImage() {

        final CharSequence[] options = { "Prendre une Photo", "Choisir depuis Gallery","Annuler" };

        AlertDialog.Builder builder = new AlertDialog.Builder(ModifyProfileActivity.this);
        builder.setTitle("Ajouter une Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Prendre une Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                else if (options[item].equals("Choisir depuis Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                }
                else if (options[item].equals("Annuler")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /********après validation de lajout de l'image ********/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);

                    userPicture.setImageBitmap(bitmap);

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    //TODO rajouter id de l'user si besoin pour eviter les doublons
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        //outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {

                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                // BitmapFactory.Options options = new BitmapFactory.Options();
                //options.outHeight=260;
                // options.outWidth=260;
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                Log.w("path of imgfrom gall", picturePath + "");
                userPicture.setImageBitmap(thumbnail);
            }
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
            pDialog.setMessage("Ajout d'un objet...");
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
            String mailUser = userMail.getText().toString();
            String surnameUser = userSurname.getText().toString();

            // String catObject= objCategory.getText().toString();


            Log.d("nameUser", nameUser);
            Log.d("surnameUser", surnameUser);


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("nameUser", nameUser));
            params.add(new BasicNameValuePair("surnameUser", surnameUser));
            params.add(new BasicNameValuePair("mailUser", mailUser));
            //params.add(new BasicNameValuePair("catObject", catObject));


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
        }
    }


}



