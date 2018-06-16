package ru.dreamwhite.barcodereader.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.snatik.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.dreamwhite.barcodereader.DataManager;
import ru.dreamwhite.barcodereader.MainActivity;
import ru.dreamwhite.barcodereader.R;
import ru.dreamwhite.barcodereader.adapters.SpinnerAdapter;
import ru.dreamwhite.barcodereader.model.moysklad.Supply;

public class SendFragment extends Fragment {

    OkHttpClient client = new OkHttpClient();

    TextView mStoreSpinnerTitle;
    TextView mAgentSpinnerTitle;
    AppCompatSpinner mStoreSpinner;
    AppCompatSpinner mAgentSpinner;
    Button mSaveButton;
    Button mSendButton;

    Handler handler = new Handler();

    Runnable onSuccess = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "Все готово! Вы прекрасны!", Toast.LENGTH_SHORT).show();
        }
    };

    Runnable onFailure = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "Что-то пошло не так! Данные не отправлены.", Toast.LENGTH_SHORT).show();
        }
    };

    public static SendFragment newInstance() {
        SendFragment fragment = new SendFragment();
        return fragment;

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client.dispatcher().setMaxRequests(4);

    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_send, container, false);

        mStoreSpinnerTitle = view.findViewById(R.id.store_spinner_title);
        mAgentSpinnerTitle = view.findViewById(R.id.agent_spinner_title);

        mStoreSpinner = view.findViewById(R.id.store_spinner);
        mAgentSpinner = view.findViewById(R.id.agent_spinner);

        mSaveButton = view.findViewById(R.id.save_button);
        mSendButton = view.findViewById(R.id.send_button);

        SpinnerAdapter storeAdapter = new SpinnerAdapter(getActivity(), R.layout.spinner_item, DataManager.instance().getStores());
        SpinnerAdapter agentAdapter = new SpinnerAdapter(getActivity(), R.layout.spinner_item, DataManager.instance().getSuppliers());

        mStoreSpinner.setAdapter(storeAdapter);
        mAgentSpinner.setAdapter(agentAdapter);

        mStoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String storeId = DataManager.instance().getStores().get(position).id();

                DataManager.instance().getCurrentSupply().getAttrs().store(storeId);
                //Log.d("SpinnerAdapter", "Store id: " + storeId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mAgentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String agentId = DataManager.instance().getSuppliers().get(position).id();

                DataManager.instance().getCurrentSupply().getAttrs().supplier(agentId);
                //Log.d("SpinnerAdapter", "Store id: " + agentId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postSupplyToMS();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSupply();
            }
        });

        return view;

    }

    private void postItemsToSupply(String supplyHref, JSONArray chunk) {

        RequestBody body = RequestBody.create(MainActivity.JSON, chunk.toString());

        Request request = new Request.Builder()
                .url(supplyHref + "/positions")
                .post(body)
                .header("Content-Type", "application/json")
                .header("Authorization", MainActivity.CREDENTIALS)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                /*try {
                    JSONArray respJSON = new JSONArray(resp);
                    Log.d("SendResponse", resp);
                } catch (JSONException e) {e.printStackTrace();}*/

                Log.d("SendResponse", resp);

                synchronized (this) {
                    chunkCounter++;
                }

                if (chunkCounter>=chunkSize) {
                    handler.post(onSuccess);
                }
            }
        });

    }


    int chunkSize = 0;
    volatile int chunkCounter = 0;

    private void postSupplyToMS() {
        Log.d("MainActivity", "Posting Supply to MS");

        final Supply supply = DataManager.instance().getCurrentSupply();

        RequestBody body = RequestBody.create(MainActivity.JSON, supply.toJSON());
        Request request = new Request.Builder()
                .url("https://online.moysklad.ru/api/remap/1.1/entity/supply")
                .post(body)
                .header("Content-Type", "application/json")
                .header("Authorization", MainActivity.CREDENTIALS)
                .build();

        if (supply.getItems().length()>100) {

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    handler.post(onFailure);

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String resp = response.body().string();


                    Log.d("SendResponse", resp);

                    try {
                        JSONObject r = new JSONObject(resp);
                        JSONObject meta = r.getJSONObject("meta");
                        String href = meta.getString("href");

                        ArrayList<JSONArray> chunks = supply.getPagedItems();
                        chunkSize = chunks.size();
                        chunkCounter = 0;
                      for (int i=0; i<chunks.size(); i++) {

//                        for (int i=0; i<1; i++) {
                            postItemsToSupply(href, chunks.get(i));
                        }
                    } catch (JSONException e) {e.printStackTrace();}

                }
            });
        }

        else {

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    handler.post(onFailure);


                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("SendResponse", response.body().string());
                    handler.post(onSuccess);

                }
            });
        }
    }



    public void saveSupply() {
        DataManager.instance().saveSupply();
        Toast.makeText(getActivity(), "Приемка сохранена", Toast.LENGTH_LONG).show();
    }

}
