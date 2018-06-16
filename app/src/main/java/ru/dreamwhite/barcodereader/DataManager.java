package ru.dreamwhite.barcodereader;

import android.content.Context;
import android.util.Log;

import com.snatik.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.dreamwhite.barcodereader.model.moysklad.Item;
import ru.dreamwhite.barcodereader.model.moysklad.Supply;
import ru.dreamwhite.barcodereader.model.moysklad.SupplyFileData;
import ru.dreamwhite.barcodereader.model.moysklad.mswrappers.Store;
import ru.dreamwhite.barcodereader.model.moysklad.mswrappers.Supplier;

public class DataManager {
    private static DataManager instance;
    public Item defaultItem = new Item ("Неизвестный товар", "Неизвестный штрихкод");

    ExecutorService executor = Executors.newSingleThreadExecutor();
    private DataManager() {}

    public Storage storage;

    private HashMap<String,OnModelChangedListener> modelChangedListeners = new HashMap<>();
    private HashMap<String,OnFileNameChangedListener> loadedListeners = new HashMap<>();

    public static DataManager instance() {
        if (instance==null) {

            instance = new DataManager();
            instance.defaultItem.isDefault=true;
            instance.setDefaultSuppliers();
        }
        return instance;
    }

/*----------------------------------------------------------------*/

    private ArrayList<String> codes = new ArrayList<>();
    private HashMap<String,Item> assortment = new HashMap<>();
    private HashMap<String,Item> mSupplyMap = new HashMap<>();

    private Supply mCurrentSupply = new Supply();

    private ArrayList<Supplier> mSuppliers = new ArrayList<>();
    private ArrayList<Store> mStores = new ArrayList<>();




    private void setDefaultSuppliers() {
        mSuppliers = new ArrayList<>();
        Supplier first = new Supplier("7eb096b6-3678-11e8-9ff4-34e8002f9d0e");
        first.name("Ильин А.Д.");

        Supplier second = new Supplier("1aabe121-337e-11e8-9109-f8fc000bf466");
        second.name("ООО \"Валенсия+\"");

        Supplier third = new Supplier("15325c49-a4f5-11e7-6b01-4b1d00052b2e");
        third.name("ООО \"Ксения\"");


        addSupplier(first);
        addSupplier(second);
        addSupplier(third);
    }


    public void addSupplier(Supplier supplier) {
        mSuppliers.add(supplier);
    }

    public ArrayList<Supplier> getSuppliers() {
        return mSuppliers;
    }


    public void addStore(Store store) {
        mStores.add(store);
    }

    public ArrayList<Store> getStores() {
        return mStores;
    }

    public Supply getCurrentSupply() {

        JSONArray data = new JSONArray();

        ArrayList<Item> items = new ArrayList<>(mSupplyMap.values());
        try {
            for (int i = 0; i<items.size(); i++) {
                data.put(items.get(i).toJSONObject());
            }
        } catch (JSONException e) {}

        mCurrentSupply.setItems(data);
        return mCurrentSupply;
    }

    public void setSupply(Supply supply) {
        mCurrentSupply = supply;

    }

    public void deleteItem(String barcode) {
        instance.mSupplyMap.remove(barcode);
    }


    public void addCode(String code, boolean isSave) {
        Item item = mSupplyMap.get(code);
        if (item!=null) {
            item.count++;
        }
        else {
            mSupplyMap.put(code, assortment.get(code));
        }

        codes.add(code);

        if (isSave) {
            saveSupply();
        }
    }

    public void addCode(Item item) {

        mSupplyMap.put(item.barcode, item);
        codes.add(item.barcode);
    }



    public ArrayList<String> getCodes() {
        return instance().codes;
    }
    public HashMap<String, Item> getSupplyMap() {
        return instance().mSupplyMap;
    }


    public void addAssortmentItem(String barcode, Item item) {


        assortment.put(barcode,item);
    }

    public Item getItemForBarcode(String barcode) {
        Item result = assortment.get(barcode);
        if (result!=null) {
            return result;
        }
        else return defaultItem;
    }

    public HashMap getAssortment() {
        return assortment;
    }


    private String currentFileName = "";

    public File dir, contextDir;

    private static final DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    public void saveSupply() {
        executor.execute(new Runnable() {
            @Override
            public void run() {


                //File dir = new File(context.getFilesDir(), "supplies");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir, currentFileName);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                JSONArray data = new JSONArray();
                ArrayList<Item> items = new ArrayList<>(mSupplyMap.values());
                final int size = items.size();

                for (int i = 0; i<size; i++) {
                    data.put(items.get(i).encode());
                }


                String fileContents = data.toString();

                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public ArrayList<File> getFileList(Context context) {

        //File dir = new File(context.getFilesDir(), "supplies");

        ArrayList<File> inFiles = new ArrayList<>();
        Queue<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(dir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else if (file.getName().endsWith(".json")) {
                inFiles.add(file);
            }
        }
        return inFiles;

    }


    public SupplyFileData parseSupplyFile(File file) {

        String text = loadFile(file);

        int overallCount = 0;
        int size = 0;

        try {
            JSONArray array = new JSONArray(text);
            size = array.length();
            for (int i=0; i<size; i++) {
                JSONObject item = array.getJSONObject(i);
                overallCount += item.getInt("quantity");
            }

        }
        catch (JSONException e) {}

        String title = file.getName().replace(".json", "");

        SupplyFileData data = new SupplyFileData(title, size, overallCount);

        return data;

    }


    public String loadFile(File file) {
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }

    public void loadSupply(File file, Context context){
        reset();

        String text = loadFile(file);

        try {
            JSONArray items = new JSONArray(text);
            final int size = items.length();
            for (int i = 0; i<size; i++) {
                Item item = Item.decode(items.getJSONObject(i));
                Log.d("Inside Load Function", item.name + " " + item.count);
                addCode(item);
                //addCode(item.barcode, false);
            }

        } catch (JSONException e) { e.printStackTrace();}

        setFileName(file.getName());

        notifyOnChangedListeners();
        notifyFileNameChangedListeners();

    }

    public void mockSupply(){
        reset();


        ArrayList<Item> list = new ArrayList<>(DataManager.instance().getAssortment().values());

        int size = list.size();

        int limit = 210;

        for (int i = 0; i<limit && i <size; i++) {
            Item item = list.get(i);
            DataManager.instance().addCode(item);
        }

        setFileName("mock.json");

        notifyOnChangedListeners();
        notifyFileNameChangedListeners();

    }


    public void addOnModelChangedListener(String component, OnModelChangedListener listener) {
        modelChangedListeners.put(component, listener);
    }
    public void removeOnModelChangedListener(String component) {
        modelChangedListeners.remove(component);
    }

    public void notifyOnChangedListeners() {
        ArrayList<OnModelChangedListener> listenerArrayList  = new ArrayList<>(modelChangedListeners.values());

        final int size = listenerArrayList.size();
        for (int i=0; i<size; i++) {
            listenerArrayList.get(i).onChanged();
        }

    }

    public void addOnFileNameChangedListener(String component, OnFileNameChangedListener listener) {
        loadedListeners.put(component, listener);
    }
    public void removeOnFileNameChangedListener(String component) {
        loadedListeners.remove(component);
    }

    public void notifyFileNameChangedListeners() {
        ArrayList<OnFileNameChangedListener> listenerArrayList  = new ArrayList<>(loadedListeners.values());

        final int size = listenerArrayList.size();
        for (int i=0; i<size; i++) {
            listenerArrayList.get(i).onFileNameChanged();
        }
    }

    public void setDefaultFileName() {
        Date currentTime = Calendar.getInstance().getTime();
        currentFileName = df.format(currentTime) + ".json";
        notifyFileNameChangedListeners();
    }

    public void setFileName(String fileName) {
        currentFileName =  fileName;
        notifyFileNameChangedListeners();
    }

    public String getFileName() {
       return currentFileName;
    }

    public void reset() {

        mSupplyMap = new HashMap<>();
        mCurrentSupply.setItems(new JSONArray());
        codes = new ArrayList<>();
        setDefaultSuppliers();

        notifyOnChangedListeners();

    }

    public boolean hasItems() {
        return !mSupplyMap.isEmpty();
    }

    public interface OnFileNameChangedListener {
        void onFileNameChanged();
    }

}
