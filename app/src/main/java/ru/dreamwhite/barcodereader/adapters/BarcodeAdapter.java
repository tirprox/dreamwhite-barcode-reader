package ru.dreamwhite.barcodereader.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import ru.dreamwhite.barcodereader.DataManager;
import ru.dreamwhite.barcodereader.R;
import ru.dreamwhite.barcodereader.model.moysklad.Item;

public class BarcodeAdapter extends RecyclerView.Adapter<BarcodeAdapter.ViewHolder> {

    private ArrayList<Item> mItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView mTitle, mSubtitle, mCount;

        public ViewHolder(FrameLayout layout) {
            super(layout);
            mTitle = (TextView) layout.findViewById(R.id.recycler_view_title);
            mSubtitle = (TextView) layout.findViewById(R.id.recycler_view_subtitle);
            mCount = (TextView) layout.findViewById(R.id.recycler_view_count);
        }
    }

    public BarcodeAdapter(HashMap<String,Item> map) {
        mItems = new ArrayList<Item>(map.values());
    }

    @Override
    public BarcodeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        FrameLayout layout = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item, parent, false);



        ViewHolder vh = new ViewHolder(layout);

        return vh;
    }


    private Item getSupplyItemOrDefault(int position) {
        Item item = mItems.get(position);
        if (item==null) {
            item = DataManager.instance().defaultItem;
        }
        return item;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final Item item = getSupplyItemOrDefault(position);

            holder.mTitle.setText(item.name);
            holder.mSubtitle.setText(item.barcode);
            holder.mCount.setText(item.getCount());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemClick(position, item);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    private OnItemClicked onClick;

    //make interface like this
    public interface OnItemClicked {
        void onItemClick(int position, Item item);
    }

    public void setOnClick(OnItemClicked onClick)
    {
        this.onClick=onClick;
    }


}