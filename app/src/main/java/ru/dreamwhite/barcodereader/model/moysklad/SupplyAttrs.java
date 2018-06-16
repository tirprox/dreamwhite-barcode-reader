package ru.dreamwhite.barcodereader.model.moysklad;

import ru.dreamwhite.barcodereader.model.moysklad.mswrappers.Group;
import ru.dreamwhite.barcodereader.model.moysklad.mswrappers.Organization;
import ru.dreamwhite.barcodereader.model.moysklad.mswrappers.Store;
import ru.dreamwhite.barcodereader.model.moysklad.mswrappers.Supplier;

public class SupplyAttrs {

    private Supplier supplier;
    private Organization organization;
    private Store store;
    private Group group;

    public SupplyAttrs() {

    }

    public SupplyAttrs(String supplierId, String orgId, String storeId, String groupId) {
        supplier = new Supplier(supplierId);
        organization= new Organization(orgId);
        store = new Store(storeId);
        group = new Group(groupId);
    }

    public SupplyAttrs supplier(String id) {
        supplier = new Supplier(id);
        return this;
    }

    public Supplier supplier() {
        return supplier;
    }

    public SupplyAttrs organization(String id) {
        organization = new Organization(id);
        return this;
    }

    public Organization organization() {
        return organization;
    }

    public SupplyAttrs store(String id) {
        store = new Store(id);
        return this;
    }

    public Store store() {
        return store;
    }

    public SupplyAttrs group(String id) {
        group = new Group(id);
        return this;
    }

    public Group group() {
        return group;
    }


}
