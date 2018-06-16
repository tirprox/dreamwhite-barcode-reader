package ru.dreamwhite.barcodereader.model.moysklad;

import org.json.JSONException;
import org.json.JSONObject;

public class Item {

    public String name, barcode;

    public String id, href;

    public boolean isDefault = false;

    public int count = 1;

    public Item(String name, String barcode) {
        this.name = name;
        this.barcode = barcode;

    }



    public JSONObject encode() {
        JSONObject object = new JSONObject();
        try {
            object.put("quantity", count);
            object.put("name", name);
            object.put("barcode", barcode);
            object.put("id", id);
            object.put("href", href);
        } catch (JSONException e) {}

        return object;
    }

    public static Item decode(JSONObject object) {
        try {
            Item item = new Item(object.getString("name"), object.getString("barcode"));
            item.count = object.getInt("quantity");
            item.id = object.getString("id");
            item.href = object.getString("href");
            return item;

        } catch (JSONException e) {}

        return null;
    }


    public String getCount() {
        return String.valueOf(count);
    }

    public JSONObject toJSONObject() throws JSONException{
        JSONObject object = new JSONObject();
        object.put("quantity", count);

        JSONObject assortment = new JSONObject();
        JSONObject meta = new JSONObject();

        meta.put("href", href);
        meta.put("metadataHref", "https://online.moysklad.ru/api/remap/1.1/entity/variant/metadata");
        meta.put("type", "variant");
        meta.put("mediaType", "application/json");

        assortment.put("meta", meta);
        object.put("assortment", assortment);


        return object;
    }

}
