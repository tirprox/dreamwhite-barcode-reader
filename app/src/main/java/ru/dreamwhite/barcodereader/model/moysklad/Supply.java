package ru.dreamwhite.barcodereader.model.moysklad;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Supply {

    private static final String
            ORG_DEFAULT_ID = "8f3fb0c0-e00e-11e6-7a69-9711001f668a",
            AGENT_DEFAULT_ID = "15325c49-a4f5-11e7-6b01-4b1d00052b2e", // counterparty
            STORE_DEFAULT_ID = "baedb9ed-de2a-11e6-7a34-5acf00087a3f",
            GROUP_DEFAULT_ID = "59c74466-a4ef-11e7-7a69-8f5500021289";

    private String name, description;

    public SupplyAttrs getAttrs() {
        return attrs;
    }

    public void setAttrs(SupplyAttrs attrs) {
        this.attrs = attrs;
    }

    private SupplyAttrs attrs;


    private JSONObject organisation, agent, store;

    private JSONObject supply;

    private JSONArray items;


    public Supply() {
        try { setDefaults(); }
        catch (JSONException e) {}

    }

    public ArrayList<JSONArray> getPagedItems() {
        ArrayList<JSONArray> result = new ArrayList<>();
        final int size = items.length();
        final int limit = size+100;

        for (int i=100; i<limit; i+=100) {
            int counter = i - 100;
            JSONArray chunk = new JSONArray();
            while (counter < i && counter < size) {
                try {
                    JSONObject temp = items.getJSONObject(counter);
                    chunk.put(temp);
                    counter++;
                }
                catch (JSONException e) {e.printStackTrace();}
            }
            result.add(chunk);
        }

      return result;
    }


    public String toJSON() {
        supply = new JSONObject();
        try {
            supply.put("organization", attrs.organization().toJSONObject());
            supply.put("agent", attrs.supplier().toJSONObject());
            supply.put("store", attrs.store().toJSONObject());
            supply.put("group", attrs.group().toJSONObject());
            supply.put("applicable", false);

            if(items.length()<=100) {
                supply.put("positions", items);
            }

        } catch (JSONException e) {}


        return supply.toString();
    }

    private void setDefaults() throws JSONException {
        items = new JSONArray();
        attrs = new SupplyAttrs(AGENT_DEFAULT_ID, ORG_DEFAULT_ID, STORE_DEFAULT_ID, GROUP_DEFAULT_ID);

        name = "DEFAULT NAME";
        description = "DEFAULT DESCRIPTION";

        organisation = attrs.organization().toJSONObject();
        agent = attrs.supplier().toJSONObject();
        store = attrs.store().toJSONObject();
    }

    public void addPosition(JSONObject item) {
        items.put(item);
    }

    public void setItems(JSONArray items) {
        this.items = items;
    }

    public JSONArray getItems() {
        return items;
    }


}
