package com.example.risyat.skrpsi;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import static android.content.ContentValues.TAG;

/**
 * Created by Zainulbr on 15/10/2017.
 */

public class Splash extends AppCompatActivity {

    int time = 3000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide statusbar
        if (Build.VERSION.SDK_INT >= 16 ){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.splash_activity);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cekStatus()){
                    redirect(getApplicationContext(),Login.class);
                }
            }
        }, time);
    }

    private boolean cekStatus(){
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                redirect(getApplicationContext(),MainActivity.class);
                return false;
            }
        } catch (NullPointerException e){
            Log.e(TAG, "cekStatus: ",e );
        }
        return true;
    }

    private void redirect(Context c,Class cc){
        Intent intent = new Intent(c, cc);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
