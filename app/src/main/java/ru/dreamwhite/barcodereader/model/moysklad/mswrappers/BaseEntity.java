package ru.dreamwhite.barcodereader.model.moysklad.mswrappers;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseEntity {

    protected static final String BASE_URL = "https://online.moysklad.ru/api/remap/1.1/entity/";
    protected String ENTITY_NAME = "/";


    /*protected static String
            METADATA_HREF = "",
            ORG_METADATA_HREF = "https://online.moysklad.ru/api/remap/1.1/entity/organization/metadata",
            AGENT_METADATA_HREF = "https://online.moysklad.ru/api/remap/1.1/entity/counterparty/metadata", // counterparty
            STORE_METADATA_HREF = "https://online.moysklad.ru/api/remap/1.1/entity/store/metadata",
            GROUP_METADATA_HREF = "https://online.moysklad.ru/api/remap/1.1/entity/group/metadata";*/


    protected String id, href, name;

    private BaseEntity() {

    }

    public BaseEntity(String id) {
        id(id);
    }

    public BaseEntity id(String id) {
        this.id = id;
        this.href = BASE_URL + ENTITY_NAME + "/" + id;
        return this;
    }

    public String id() {
        return id;
    }

    public BaseEntity href(String href) {
        this.href = href;
        return this;
    }

    public String href() {
        return href;
    }

    public BaseEntity name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }


    public JSONObject toJSONObject() throws JSONException {
        JSONObject root = new JSONObject();
        JSONObject meta = new JSONObject();

        String metaHref = "https://online.moysklad.ru/api/remap/1.1/entity/" + ENTITY_NAME + "/metadata";

        meta.put("href", href);
        meta.put("metadataHref", href);
        meta.put("type", ENTITY_NAME);
        meta.put("mediaType", "application/json");

        root.put("meta", meta);
        return root;
    }

}
