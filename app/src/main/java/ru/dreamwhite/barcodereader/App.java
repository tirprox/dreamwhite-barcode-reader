package ru.dreamwhite.barcodereader;

import android.app.Application;

import com.snatik.storage.Storage;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataManager.instance().storage = new Storage(getApplicationContext());
    }


}
