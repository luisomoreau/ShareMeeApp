package com.sharemee.app.sharemee.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//TODO check empty fields
public class SigninActivity extends Activity {

    AutoCompleteTextView txtName;
    AutoCompleteTextView txtSurname;
    AutoCompleteTextView txtEmail;
    AutoCompleteTextView txtPassword;
    AutoCompleteTextView txtReenterpassword;

    Button btnSignIn;

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
        setContentView(R.layout.activity_signin);
        // don’t set any content view here, since its already set in BaseActivity
        //FrameLayout frameLayout = (FrameLayout)findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        //View activityView = layoutInflater.inflate(R.layout.activity_signin, null,false);
        // add the custom layout of this activity to frame layout.
        //frameLayout.addView(activityView);
        // now you can do all your other stuffs

        txtName = (AutoCompleteTextView) findViewById(R.id.signin_name);
        txtSurname = (AutoCompleteTextView) findViewById(R.id.signin_surname);
        txtEmail = (AutoCompleteTextView) findViewById(R.id.signin_email);
        txtPassword = (AutoCompleteTextView) findViewById(R.id.signin_password);
        txtReenterpassword = (AutoCompleteTextView) findViewById(R.id.signin_reenter_password);

        btnSignIn = (Button) findViewById(R.id.sign_in_button);


        // save button click event
        btnSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update product
                String password = txtPassword.getText().toString();
                String password2 = txtReenterpassword.getText().toString();
                if((txtName.getText().toString().isEmpty())||(txtSurname.getText().toString().isEmpty())||(txtEmail.getText().toString().isEmpty())||(txtPassword.getText().toString().isEmpty())||(txtReenterpassword.getText().toString().isEmpty())){
                    Context context = getApplicationContext();
                    CharSequence text = "Veuillez renseigner tous les champs";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                else {
                    if (password.equals(password2)) {
                        new Signin().execute();
                    }else {
                        Context context = getApplicationContext();
                        CharSequence text = "Les mots de passes ne correspondent pas";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }
            }

        });
    }


    /**
     * Background Async Task to  Save product Details
     * */
    class Signin extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SigninActivity.this);
            pDialog.setMessage("Création du profil ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Saving product
         * */
        protected String doInBackground(String... args) {

            // getting updated data from EditTexts

            String nameUser = txtName.getText().toString();
            String surnameUser = txtSurname.getText().toString();
            String mailUser = txtEmail.getText().toString();
            String passwordUser = txtPassword.getText().toString();

            Log.d("nameUser", nameUser);
            Log.d("surnameUser", surnameUser);
            Log.d("mailUser", mailUser);
            Log.d("passwordUser", passwordUser);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("nameUser", nameUser));
            params.add(new BasicNameValuePair("surnameUser", surnameUser));
            params.add(new BasicNameValuePair("mailUser", mailUser));
            params.add(new BasicNameValuePair("passwordUser", passwordUser));

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
