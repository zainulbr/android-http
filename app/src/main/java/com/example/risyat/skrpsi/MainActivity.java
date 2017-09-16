package com.example.risyat.skrpsi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
//import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

//import org.apache.http.*;
import org.json.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Time;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends Activity implements View.OnClickListener {

    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";
    // declare buttons and text inputs
    private ImageView Off,On;
    private EditText editTextIPAddress, editTextPortNumber;
    private TextView[] Sensors;
    private Handler handler = new Handler();
    private Runnable r;
    private  Thread thread;
    // shared preferences objects used to save the IP address and port so that the user doesn't have to
    // type them next time he/she opens the app.
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // assign buttons
        Off = (ImageView)findViewById(R.id.imageView);
        On = (ImageView)findViewById(R.id.imageView2);

        // assign text inputs
        editTextIPAddress = (EditText)findViewById(R.id.editTextIPAddress);
        editTextPortNumber = (EditText)findViewById(R.id.editTextPortNumber);
        Sensors = new TextView[4];
        Sensors[0] = (TextView)findViewById(R.id.textView);
        Sensors[1] = (TextView)findViewById(R.id.textView3);
        Sensors[2] = (TextView)findViewById(R.id.textView4);
        Sensors[3] = (TextView)findViewById(R.id.textView9);

        // set button listener (this class)
        On.setOnClickListener(this);
        Off.setOnClickListener(this);

        // get the IP address and port number from the last time the user used the app,
        // put an empty string "" is this is the first time.
        editTextIPAddress.setText(sharedPreferences.getString(PREF_IP,""));
        editTextPortNumber.setText(sharedPreferences.getString(PREF_PORT,""));
    }


    @Override
    public void onClick(final View view) {

        // get the pin number
        String parameterValue = "";
        // get the ip address
        final String ipAddress = editTextIPAddress.getText().toString().trim();
        // get the port number
        final String portNumber = editTextPortNumber.getText().toString().trim();


        // save the IP address and port for the next time the app is used
        editor.putString(PREF_IP,ipAddress); // set the ip address value to save
        editor.putString(PREF_PORT,portNumber); // set the port number to save
        editor.commit(); // save the IP and PORT

        // get the pin number from the button that was clicked
        if(view.getId()==On.getId())
        {
            parameterValue = "11";

            if (thread != null && thread.isAlive()){
                System.out.println("threed alive");
                Toast.makeText(getBaseContext(),"Application is running",Toast.LENGTH_LONG).show();
                return;
            }
//            On.setVisibility(GONE);
//            Off.setVisibility(VISIBLE);
        }
        else if(view.getId()==Off.getId())
        {
            parameterValue = "12";
            if (thread != null  && !thread.isAlive()){
                Toast.makeText(getBaseContext(),"application off", Toast.LENGTH_LONG).show();
            }
//            On.setVisibility(VISIBLE);
//            Off.setVisibility(GONE);
        }

        // execute HTTP request''
        if(ipAddress.length()>0 && portNumber.length()>0 && (parameterValue == "11" || parameterValue.equals("11")) ) {
            final String finalParameterValue = parameterValue;
//            r = new Runnable() {
//                public void run() {
//                    Log.e(TAG, "run: ");
//                    new HttpRequestAsyncTask(
//                            view.getContext(), finalParameterValue, ipAddress, portNumber, "pin"
//                    ).execute();
//                    Log.e(TAG, "run: outpost");
//                    handler.postDelayed(this, 1000);
//                }
//            };
//            handler.postDelayed(r, 2000);

            thread = new Thread() {
                @Override
                public void run() {
                    try {
                        while(true) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new HttpRequestAsyncTask(
                                            view.getContext(), finalParameterValue, ipAddress, portNumber, "pin"
                                    ).execute();
                                    try {
                                        sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "run: ",e );
                                    }
                                    handler.post(this);

                                }
                            });
                            System.out.println("start");
                            sleep(5000);
                            System.out.println("finish");
                            Log.e(TAG, "run: oke" );
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "run: ",e );
                    }
                }
            };
            thread.start();
        }
        if (parameterValue == "12" || parameterValue.equals("12")){
            System.out.println("test");
            if (thread.isAlive()){
                thread.stop();
                thread.destroy();
                Log.e(TAG, "onClick: stop");
            }
            handler.removeCallbacks(r);
            Log.e(TAG, "onClick: ");
        }
    }

    /**
     * Description: Send an HTTP Get request to a specified ip address and port.
     * Also send a parameter "parameterName" with the value of "parameterValue".
     * @param parameterValue the pin number to toggle
     * @param ipAddress the ip address to send the request to
     * @param portNumber the port number of the ip address
     * @param parameterName
     * @return The ip address' reply text, or an ERROR message is it fails to receive one
     */
    public String sendRequest(String parameterValue, String ipAddress, String portNumber, String parameterName) {
        String serverResponse = "ERROR";

        try {

            HttpClient httpclient = new DefaultHttpClient(); // create an HTTP client
            // define the URL e.g. http://myIpaddress:myport/?pin=13 (to toggle pin 13 for example)
            String url = "http://risyatskripsi1.000webhostapp.com/index.php/welcome/latest";
            URI website = new URI(url);
            HttpGet getRequest = new HttpGet(); // create an HTTP GET object
            getRequest.setURI(website); // set the URL of the GET request
            HttpResponse response = httpclient.execute(getRequest); // execute the request
            // get the ip address server's reply
            InputStream content = null;
            content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    content
            ));
            serverResponse = in.readLine();
            // Close the connection
            content.close();
        } catch (ClientProtocolException e) {
            // HTTP error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            // IO error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // URL syntax error
            serverResponse = e.getMessage();
            e.printStackTrace();
        }
        // return the server's reply/response text
        return serverResponse;
    }


    /**
     * An AsyncTask is needed to execute HTTP requests in the background so that they do not
     * block the user interface.
     */
    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {

        // declare variables needed
        private String requestReply,ipAddress, portNumber;
        private Context context;
        private AlertDialog alertDialog;
        private String parameter;
        private String parameterValue;

        /**
         * Description: The asyncTask class constructor. Assigns the values used in its other methods.
         * @param context the application context, needed to create the dialog
         * @param parameterValue the pin number to toggle
         * @param ipAddress the ip address to send the request to
         * @param portNumber the port number of the ip address
         */
        public HttpRequestAsyncTask(Context context, String parameterValue, String ipAddress, String portNumber, String parameter)
        {
            this.context = context;
            alertDialog = new AlertDialog.Builder(this.context)
                    .setTitle("HTTP Response From IP Address:")
                    .setCancelable(true)
                    .create();

            this.ipAddress = ipAddress;
            this.parameterValue = parameterValue;
            this.portNumber = portNumber;
            this.parameter = parameter;
        }

        /**
         * Name: doInBackground
         * Description: Sends the request to the ip address
         * @param voids
         * @return
         */
        @Override
        protected Void doInBackground(Void... voids) {
//            alertDialog.setMessage("Data sent, waiting for reply from server...");
//            if(!alertDialog.isShowing())
//            {
//                alertDialog.show();
//            }
            requestReply = sendRequest(parameterValue,ipAddress,portNumber, parameter);
            System.out.println(requestReply);
            return null;
        }

        /**
         * Name: onPostExecute
         * Description: This function is executed after the HTTP request returns from the ip address.
         * The function sets the dialog's message with the reply text from the server and display the dialog
         * if it's not displayed already (in case it was closed by accident);
         * @param aVoid void parameter
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                JSONArray arr = new JSONArray(requestReply);
                for (int i = 0; i < arr.length(); i++) {
                    int id = arr.getJSONObject(i).getInt("id");
                    int status = arr.getJSONObject(i).getInt("status");
                    String sStatus = status == 1 ? "Detected" : "Not Detected";
                    Sensors[id].setText(sStatus);
                }
            }
            catch(JSONException e){

            }

//            alertDialog.setMessage(requestReply);
//            if(!alertDialog.isShowing())
//            {
//                alertDialog.show(); // show dialog
//            }
        }

        /**
         * Name: onPreExecute
         * Description: This function is executed before the HTTP request is sent to ip address.
         * The function will set the dialog's message and display the dialog.
         */
        @Override
        protected void onPreExecute() {
//            alertDialog.setMessage("Sending data to server, please wait...");
//            if(!alertDialog.isShowing())
//            {
//                alertDialog.show();
//            }
        }

    }
}
