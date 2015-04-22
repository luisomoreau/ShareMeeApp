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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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

public class AddObjectActivity extends BaseActivity {

    AutoCompleteTextView objName;
    AutoCompleteTextView objDescription;
    ImageView objImage;
    CardView btnAddObject;
    Button btnImage;
    Spinner objCategory;
    Spinner spinner;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";


    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // url to update product
    private static final String url_signin = "http://sharemee.com/webservice/model/signin.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_add_object, null,false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        // now you can do all your other stuffs


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

        btnImage=(Button)findViewById(R.id.add_picture_button);
        objImage=(ImageView)findViewById(R.id.add_picture_image);
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
                String catObj= " t";

                if ((!nameObj.isEmpty())&&(!descObj.isEmpty())&&(!catObj.isEmpty())) {
                    new AddObject().execute();
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

                AlertDialog.Builder builder = new AlertDialog.Builder(AddObjectActivity.this);
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

                    objImage.setImageBitmap(bitmap);

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
                Log.w("path of imgfrom gall", picturePath+"");
                objImage.setImageBitmap(thumbnail);
            }
        }
    }





    /**
     * Background Async Task to  Save product Details
     * */
    class AddObject extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
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
         * */
        protected String doInBackground(String... args) {



            // getting updated data from EditTexts

            String nameObject = objName.getText().toString();
            String descObject = objDescription.getText().toString();
           // String catObject= objCategory.getText().toString();


            Log.d("nameUser", nameObject);
            Log.d("surnameUser", descObject);


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("nameObject", nameObject));
            params.add(new BasicNameValuePair("descObject", descObject));
            //params.add(new BasicNameValuePair("catObject", catObject));


            Log.d("params", params.toString());

            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_signin,
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
