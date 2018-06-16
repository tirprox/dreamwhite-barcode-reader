package ru.dreamwhite.barcodereader.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ru.dreamwhite.barcodereader.R;
import ru.dreamwhite.barcodereader.model.moysklad.mswrappers.BaseEntity;

public class SpinnerAdapter<T extends BaseEntity> extends ArrayAdapter {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final ArrayList<T> items;
    private final int mResource;

    public SpinnerAdapter(@NonNull Context context, @LayoutRes int resource,
                          @NonNull ArrayList<T> objects) {
        super(context, resource, 0, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        items = (ArrayList<T>) objects;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        final View view = mInflater.inflate(mResource, parent, false);

        TextView itemTextView = (TextView) view.findViewById(R.id.spinner_item);

        BaseEntity item = items.get(position);
        itemTextView.setText(item.name());

        return view;
    }
}

