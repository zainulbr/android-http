package com.example.risyat.skrpsi;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.risyat.skrpsi.model.App;
import com.example.risyat.skrpsi.model.Data;
import com.google.firebase.messaging.RemoteMessage;

import java.net.URI;
import java.sql.SQLOutput;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * Created by filipp on 5/23/2016.
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    SharedPreferences.Editor spe;
    SharedPreferences sp;
    protected MainActivity activityMainContex = App.getContext();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        spe = sp.edit();
        Log.d(TAG, "onMessageReceived: ");
        String x = "";
        Map data = new Map() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @Override
            public Object get(Object key) {
                return null;
            }

            @Override
            public Object put(Object key, Object value) {
                return null;
            }

            @Override
            public Object remove(Object key) {
                return null;
            }

            @Override
            public void putAll(@NonNull Map m) {

            }

            @Override
            public void clear() {

            }

            @NonNull
            @Override
            public Set keySet() {
                return null;
            }

            @NonNull
            @Override
            public Collection values() {
                return null;
            }

            @NonNull
            @Override
            public Set<Entry> entrySet() {
                return null;
            }
        };
        try {
            data = remoteMessage.getData();
        } catch (Exception e) {
            Log.e(TAG, "onMessageReceived: ", e);
        }
        if (Integer.valueOf(data.get("status").toString()) == 1) {
            setValueSensor(data, "");
            System.out.println(sp.getBoolean(Data.notif, false));
            if (sp.getBoolean(Data.notif, false)) {
                showNotification(data);
            }
        } else {
            setValueSensor(data, "still scure");
        }
        setTextSensor(data);
    }

    private void setValueSensor(Map messageData, String value) {
        spe.putString(Data.SensorsText[Integer.valueOf(messageData.get("sensor").toString())], value.equals("") ? messageData.get("message").toString() : "Sensor " + (Integer.valueOf(messageData.get("sensor").toString()) + 1) + "Undetected");
        spe.commit();
    }

    private void setTextSensor(final Map messageData) {
        if (Data.SensorsValue[Integer.valueOf(messageData.get("sensor").toString())] != Integer.valueOf(messageData.get("status").toString())) {
            activityMainContex.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String t = sp.getString(Data.SensorsText[Integer.valueOf(messageData.get("sensor").toString())],"Sensor Unknown");
                    activityMainContex.Sensors[Integer.valueOf(messageData.get("sensor").toString())].setText(t);
                    if (messageData.get("status").toString().equals("1")){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            activityMainContex.Sensors[Integer.valueOf(messageData.get("sensor").toString())].setCompoundDrawableTintList(ColorStateList.valueOf(Color.RED));

                        }
                        activityMainContex. Sensors[Integer.valueOf(messageData.get("sensor").toString())].setTextColor(Color.RED);
                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            activityMainContex.Sensors[Integer.valueOf(messageData.get("sensor").toString())].setCompoundDrawableTintList(ColorStateList.valueOf(Color.WHITE));
                        }
                        activityMainContex.Sensors[Integer.valueOf(messageData.get("sensor").toString())].setTextColor(Color.WHITE);
                    }
                }
            });
            Data.SensorsValue[Integer.valueOf(messageData.get("sensor").toString())] = Integer.valueOf(messageData.get("status").toString());
        }

    }


    private void showNotification(Map messageData) {
        Log.d(TAG, "showNotification: ");
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri uriRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(messageData.get("title").toString())
                .setContentText(messageData.get("message").toString())
                .setSubText(messageData.get("subtitle").toString())
                .setSound(messageData.containsKey("sound") ? uriRingtone : null)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0, builder.build());
    }


}
