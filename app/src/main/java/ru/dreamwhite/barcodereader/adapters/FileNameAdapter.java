package ru.dreamwhite.barcodereader.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ru.dreamwhite.barcodereader.DataManager;
import ru.dreamwhite.barcodereader.R;
import ru.dreamwhite.barcodereader.model.moysklad.SupplyFileData;

public class FileNameAdapter extends RecyclerView.Adapter<FileNameAdapter.ViewHolder> {

    private ArrayList<File> mItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView mTitle, mSubtitle, mCount;

        public ViewHolder(FrameLayout layout) {
            super(layout);
            mTitle = (TextView) layout.findViewById(R.id.recycler_view_title);
            mSubtitle = (TextView) layout.findViewById(R.id.recycler_view_subtitle);
            mCount = (TextView) layout.findViewById(R.id.recycler_view_count);
        }


    }

    public class SortFileName implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            return f1.getName().compareTo(f2.getName())*-1;
        }
    }

    public FileNameAdapter(ArrayList<File> items) {
        mItems = items;
        Collections.sort(mItems, new SortFileName());
    }

    @Override
    public FileNameAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

        FrameLayout layout = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item, parent, false);

        ViewHolder vh = new ViewHolder(layout);

        return vh;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final File item = mItems.get(position);

        SupplyFileData data = DataManager.instance().parseSupplyFile(item);

        holder.mTitle.setText(data.title);
        holder.mSubtitle.setText("Уникальных позиций: " + data.uniquePositions);
        holder.mCount.setText(String.valueOf(data.overallPositions));



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(position, item);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                longClickListener.onLongItemClick(position, item);
                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    private OnClickListener clickListener;
    private OnLongClickListener longClickListener;


    //make interface like this
    public interface OnClickListener {
        void onItemClick(int position, File item);
    }

    public interface OnLongClickListener {
        void onLongItemClick(int position, File item);
    }

    public void setOnClickListener(OnClickListener onClick)
    {
        this.clickListener =onClick;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener)
    {
        this.longClickListener = onLongClickListener;
    }



}