/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.dreamwhite.barcodereader;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.dreamwhite.barcodereader.adapters.BarcodeAdapter;
import ru.dreamwhite.barcodereader.fragments.EditItemFragment;
import ru.dreamwhite.barcodereader.fragments.LoadFragment;
import ru.dreamwhite.barcodereader.fragments.OnFragmentEditListener;
import ru.dreamwhite.barcodereader.fragments.OnItemAmountChangedListener;
import ru.dreamwhite.barcodereader.fragments.SendFragment;
import ru.dreamwhite.barcodereader.fragments.SettingsFragment;
import ru.dreamwhite.barcodereader.model.moysklad.Item;
import ru.dreamwhite.barcodereader.model.moysklad.mswrappers.Store;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        BarcodeAdapter.OnItemClicked,
        OnFragmentEditListener,
        OnItemAmountChangedListener,
        OnModelChangedListener,
        DataManager.OnFileNameChangedListener, SharedPreferences.OnSharedPreferenceChangeListener

{

    public static final String CREDENTIALS = Credentials.basic(Auth.LOGIN, Auth.PASSWORD);
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";
    final MainActivity activity = this;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    BarcodeAdapter adapter;
    OkHttpClient client = new OkHttpClient();
    DataManager instance = DataManager.instance(); // Hold our data!!
    Handler handler = new Handler();
    Runnable refresh = new Runnable() {
        @Override
        public void run() {
            adapter = new BarcodeAdapter(DataManager.instance().getSupplyMap());
            adapter.setOnClick(activity);
            recyclerView.setAdapter(adapter);
            Log.d("RefreshRunnable", "Adapter Refreshed!");
        }
    };
    private Button readBarcode;
    private ProgressBar spinner;
    private TextView header;
    private TextView message;
    Runnable onAssortmentLoaded = new Runnable() {
        @Override
        public void run() {
            readBarcode.setEnabled(true);
            spinner.setVisibility(View.GONE);
            header.setVisibility(View.VISIBLE);

            message.setVisibility(View.GONE);

           // DataManager.instance().mockSupply();

        }
    };
    Runnable onFailed = new Runnable() {
        @Override
        public void run() {
            //readBarcode.setEnabled(true);
            spinner.setVisibility(View.GONE);
            message.setText("Не удалось загрузить ассортимент");
            //header.setVisibility(View.VISIBLE);
            //message.setVisibility(View.GONE);

        }
    };
    private TextView fileName;
    private Runnable onSupplyLoaded = new Runnable() {
        @Override
        public void run() {
            fileName.setText(DataManager.instance().getFileName());
        }
    };

    private void getAssortment() throws IOException {

        if (DataManager.instance().getAssortment().isEmpty()) {
            Request request = new Request.Builder()
                    .url("https://dreamwhite.ru/wp-content/plugins/import-export/import-products/short-report.json")
                    .build();


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    handler.post(onFailed);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    ResponseBody responseBody = response.body();
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
                    long startTime = System.currentTimeMillis();

                    try {
                        JSONArray root = new JSONArray(responseBody.string());
                        final int size = root.length();

                        for (int i = 0; i < size; i++) {
                            JSONObject variant = root.getJSONObject(i);
                            String id = variant.getString("id");
                            String name = variant.getString("name");
                            String barcode = variant.getString("barcode");

                            Item item = new Item(name, barcode);
                            item.id = id;
                            item.href = "https://online.moysklad.ru/api/remap/1.1/entity/variant/" + id;

                            DataManager.instance().addAssortmentItem(barcode, item);
                        }

                    } catch (JSONException e) {
                    }
                    ;
                    long time = (System.currentTimeMillis() - startTime);
                    Log.d("Parsing", "Took " + String.valueOf(time));

                    handler.post(onAssortmentLoaded);
                }
            });

        } else {
            handler.post(onAssortmentLoaded);
        }

    }

    public void getStoresFromMS() {

        if (DataManager.instance().getStores().size() == 0) {
            Request request = new Request.Builder()
                    .url("https://online.moysklad.ru/api/remap/1.1/entity/store")
                    .header("Content-Type", "application/json")
                    .header("Authorization", CREDENTIALS)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    ResponseBody responseBody = response.body();
                    try {
                        JSONObject json = new JSONObject(responseBody.string());

                        JSONArray rows = json.getJSONArray("rows");
                        for (int i = 0; i < rows.length(); i++) {
                            JSONObject row = rows.getJSONObject(i);
                            Store store = new Store(row.getString("id"));
                            store.name(row.getString("name"));

                            Log.d("Stores", "id: " + store.id());
                            Log.d("Stores", "name: " + store.name());

                            DataManager.instance().addStore(store);
                        }


                    } catch (JSONException e) {
                    }

                }
            });
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataManager.instance().addOnModelChangedListener("MainActivity", this);
        DataManager.instance().addOnFileNameChangedListener("MainActivity", this);

        DataManager.instance().reset(); //setting filename to now

        DataManager.instance().contextDir = getFilesDir();

        File supplyDir = new File(getFilesDir(), "supplies");
        DataManager.instance().dir = supplyDir;

        if(!supplyDir.exists()) {
            DataManager.instance().storage.createDirectory(supplyDir.getPath());
        }

        DataManager.instance().setDefaultFileName();



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        spinner = findViewById(R.id.spinner);
        header = findViewById(R.id.header);
        message = findViewById(R.id.message);
        fileName = findViewById(R.id.supply_file_name);

        fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadFragment();
            }
        });
        fileName.setText(DataManager.instance().getFileName());


        recyclerView = findViewById(R.id.recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());

        ShapeDrawable line = new ShapeDrawable();
        line.getPaint().setColor(0x0f000000);
        line.getPaint().setStyle(Paint.Style.FILL);
        line.setIntrinsicHeight(1);


        dividerItemDecoration.setDrawable(line);
        recyclerView.addItemDecoration(dividerItemDecoration);

        refreshAdapter();

        readBarcode = findViewById(R.id.read_barcode);

        readBarcode.setEnabled(false);
        readBarcode.setOnClickListener(this);

        try {
            getAssortment();
            getStoresFromMS();

        } catch (IOException e) {
        }
        ;

        Button send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showSendFragment();
                //postSupplyToMS();
            }
        });


        ImageButton load = findViewById(R.id.load_supply);
        ImageButton save = findViewById(R.id.save_supply);
        ImageButton newSupply = findViewById(R.id.new_supply);
        ImageButton settings = findViewById(R.id.settings);

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadFragment();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager.instance().saveSupply();
                Toast.makeText(MainActivity.this, "Приемка сохранена", Toast.LENGTH_LONG).show();
            }
        });

        newSupply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("Создать новую приемку?");
                b.setPositiveButton("ДА", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        DataManager.instance().reset();
                        DataManager.instance().setDefaultFileName();
                        DataManager.instance().notifyOnChangedListeners();
                    }
                });
                b.setNegativeButton("НЕТ", null);
                b.create().show();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsFragment();
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        boolean isRotate = prefs.getBoolean("pref_rotate", true);
        setOrientation(isRotate);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

      public void showSettingsFragment() {
        SettingsFragment fragment = SettingsFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_out_right, R.anim.slide_out_right);

        transaction.replace(R.id.fragment_stub, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showLoadFragment() {
        LoadFragment fragment = LoadFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_out_right, R.anim.slide_out_right);

        transaction.replace(R.id.fragment_stub, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    public void showSendFragment() {

        if (DataManager.instance().hasItems()) {
            SendFragment fragment = SendFragment.newInstance();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_left);


            transaction.replace(R.id.fragment_stub, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Toast.makeText(this, "Нечего отправлять", Toast.LENGTH_SHORT).show();
        }

    }

    public void refreshAdapter() {
        handler.post(refresh);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        refreshAdapter();

    }

    @Override
    public void onItemClick(int position, Item item) {

        EditItemFragment fragment = EditItemFragment.newInstance(item);

        fragment.setEditListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_out, R.anim.fade_out);

        transaction.replace(R.id.fragment_stub, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        Log.d("RecyclerView", item.name + " " + item.barcode + " " + item.count);
    }

    @Override
    public void onEdit() {

        refreshAdapter();
    }

    @Override
    public void itemAmountChanged(String barcode, int newAmount) {
        DataManager.instance().getItemForBarcode(barcode).count = newAmount;
        refreshAdapter();

    }

    @Override
    public void onChanged() {
        refreshAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

       /* if (id == R.id.show_load_fragment) {
            showLoadFragment();
            //Toast.makeText(MainActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
            return true;
        }*/
        /*else if (id == R.id.save_action) {
            DataManager.instance().saveSupply();
            Toast.makeText(MainActivity.this, "Приемка сохранена", Toast.LENGTH_LONG).show();
            return true;
        }
        else if (id == R.id.new_action) {

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Создать новую приемку?");
            b.setPositiveButton("ДА", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    DataManager.instance().reset();
                    DataManager.instance().setDefaultFileName();
                    DataManager.instance().notifyOnChangedListeners();
                }
            });
            b.setNegativeButton("НЕТ", null);
            b.create().show();

        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFileNameChanged() {
        handler.post(onSupplyLoaded);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_rotate")) {
            boolean isRotate = sharedPreferences.getBoolean(key, true);
            setOrientation(isRotate);

        }

    }

    public void setOrientation(boolean isRotate) {
        if (isRotate) {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
        else {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
