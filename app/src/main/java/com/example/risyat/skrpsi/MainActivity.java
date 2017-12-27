package com.example.risyat.skrpsi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.risyat.skrpsi.model.App;
import com.example.risyat.skrpsi.model.Data;
import com.example.risyat.skrpsi.model.Sensor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import dmax.dialog.SpotsDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonStreamParser;
import com.google.gson.TypeAdapter;

import java.io.IOException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static android.content.ContentValues.TAG;


public class MainActivity extends Activity {

    private SpotsDialog ProgressDlg;

    public TextView[] Sensors;

    private String[] SensorsName;

    SharedPreferences.Editor spe;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_new);
        ProgressDlg = new SpotsDialog(this);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        spe = sp.edit();
        Sensors = new TextView[4];
        Sensors[0] = (TextView) findViewById(R.id.edt_text_sensor1);
        Sensors[1] = (TextView) findViewById(R.id.edt_text_sensor2);
        Sensors[2] = (TextView) findViewById(R.id.edt_text_sensor3);
        Sensors[3] = (TextView) findViewById(R.id.edt_text_sensor4);
        SensorsName = Data.SensorsText;

        Button btnReload = (Button) findViewById(R.id.btnReload);
        final Switch onoffNotif = (Switch) findViewById(R.id.notif_switch);

        try {
            onoffNotif.setChecked(sp.getBoolean(Data.notif,false));
        }catch (NullPointerException e){
            Log.e(TAG, "onCreate: ", e);
        }

        for (TextView sensor : Sensors) {
            sensor.setEnabled(false);
            sensor.setFocusable(false);
        }

        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLatest();
            }
        });


        onoffNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onoffNotif.isChecked()) {
                    spe.putBoolean(Data.notif,true);
                    setToast("Notification Actived");
                }else{
                    spe.putBoolean(Data.notif,false);
                    setToast("Notification Disabled");
                }
                spe.commit();
            }
        });

        App.setContext(this);

    }

    private void getLatest() {
        OkHttpClient okHttpClient = new OkHttpClient();
        ProgressDlg.show();

        Request request = new Request.Builder().url("https://risyatskripsi1.000webhostapp.com/index.php/sensors/latest").get().build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: ",e );
                setToast("Internet connection unstable");
                ProgressDlg.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: " + response);
                if (response.isSuccessful()) {
                    parse(response.body().string());
                }else{
                    setToast("Internal Sever Error");
                }
                ProgressDlg.dismiss();

            }
        });

    }

    private void parse(String response) {
        final JsonElement root = new JsonParser().parse(response);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < Sensors.length; i++) {
                    int id = root.getAsJsonArray().get(i).getAsJsonObject().get("id").getAsInt();
                    String value = root.getAsJsonArray().get(i).getAsJsonObject().get("status").getAsString();
                    String time = root.getAsJsonArray().get(i).getAsJsonObject().get("time").getAsString();
                    String def = value.equals("1") ? "Sensor " + (id + 1)+ " Detected" : "Sensor " + (id + 1)+ " Undetected";
                    spe.putString(Data.SensorsText[id], def);
                    spe.commit();
                    Sensors[id].setText(def + " " + time);
                    if (value.equals("1")){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Sensors[id].setCompoundDrawableTintList(ColorStateList.valueOf(Color.RED));

                        }
                        Sensors[id].setTextColor(Color.RED);
                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Sensors[id].setCompoundDrawableTintList(ColorStateList.valueOf(Color.WHITE));
                        }
                        Sensors[id].setTextColor(Color.WHITE);
                    }
                }
            }
        });
    }

    private void setToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        for (int i = 0; i < Sensors.length; i++) {
//            Sensors[i].setText(Data.SensorStatus[i]);
//        }
//    }

        @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0; i < Sensors.length; i++) {
            Sensors[i].setText(sp.getString(Data.SensorsText[i],Data.SensorStatusDefault[i]));
            System.out.println(sp.getString(Data.SensorsText[i],Data.SensorStatusDefault[i]));
        }
    }

}
