package com.example.risyat.skrpsi.model;

/**
 * Created by Zainulbr on 15/10/2017.
 */

import android.app.Application;

import android.content.Context;

import com.example.risyat.skrpsi.MainActivity;


/**
 * Created by Zainulbr on 18/09/2017.
 */

public class App extends Application {
    private static Context mContext;

    public static MainActivity getContext() {
        return (MainActivity)App.mContext;
    }

    public static void setContext(Context mContext) {
        App.mContext = mContext;
    }

}
