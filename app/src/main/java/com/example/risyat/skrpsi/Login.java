package com.example.risyat.skrpsi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOError;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import dmax.dialog.SpotsDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by Zainulbr on 15/10/2017.
 */

public class Login extends Activity{
    private SpotsDialog ProgressDlg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProgressDlg = new SpotsDialog(this);
        setContentView(R.layout.login_activity);
        Button btnLogin = (Button) findViewById(R.id.btnlogin);
        final EditText edtTextUsernmae = (EditText) findViewById(R.id.edt_text_username);
        final EditText edtTextPassword = (EditText) findViewById(R.id.edt_text_password);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtTextPassword.getText().equals("") || edtTextUsernmae.getText().equals("")) {
                    Toast.makeText(getApplicationContext(), "Required complate field",Toast.LENGTH_LONG).show();
                    return;
                }
                ProgressDlg.show();
                loginaction("risyat@gmail.com", "123456789");
            }
        });
    }

    private void loginaction(String useremail, String password) {
        if (FirebaseAuth.getInstance().getCurrentUser()!= null) {
            ProgressDlg.dismiss();
            return;
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(useremail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Logger.getLogger(MainActivity.class.getName()).log(Level.ALL, "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Logger.getLogger(MainActivity.class.getName()).log(Level.ALL, "signInWithEmail", task.getException());
                            Toast.makeText(Login.this, "Internal Server Error", Toast.LENGTH_SHORT).show();
                            ProgressDlg.dismiss();
                            return;
                        }
                        Log.e(TAG, "onComplete: " + FirebaseInstanceId.getInstance().getToken());
                        ProgressDlg.dismiss();
                        redirect(getApplicationContext());
                    }
                });
    }

    private boolean cekStatus(){
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                redirect(getApplicationContext());
                return true;
            }
        } catch (NullPointerException e){
            Log.e(TAG, "cekStatus: ",e );
        }
        return false;
    }

    private void redirect(Context c){
        Intent intent = new Intent(c, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        registerToken(FirebaseInstanceId.getInstance().getToken());
        startActivity(intent);
        finish();
    }

    private void registerToken(String token) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://risyatskripsi1.000webhostapp.com/index.php/user/update/"+token+"/0")
                .get()
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: ",e );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    Log.d(TAG, "onResponse: Success");
                }else {
                    Log.e(TAG, "onResponse: error" + response);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cekStatus()) {
            return;
        }
    }
}
