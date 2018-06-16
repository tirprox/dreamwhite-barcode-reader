package ru.dreamwhite.barcodereader.fragments;

import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import ru.dreamwhite.barcodereader.DataManager;
import ru.dreamwhite.barcodereader.R;
import ru.dreamwhite.barcodereader.adapters.FileNameAdapter;

public class LoadFragment extends Fragment implements
        FileNameAdapter.OnClickListener,
        FileNameAdapter.OnLongClickListener

{

    TextView mFragmentTitle;
    private FileNameAdapter adapter;
    private RecyclerView recyclerView;
    private ImageButton deleteAllButton;

    public static LoadFragment newInstance() {
        LoadFragment fragment = new LoadFragment();
        return fragment;

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_load, container, false);

        mFragmentTitle = view.findViewById(R.id.fragment_load_title);

        ArrayList<File> files = DataManager.instance().getFileList(getActivity());


        recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        deleteAllButton = view.findViewById(R.id.delete_all);
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle("Удалить ВСЕ приемки?");
                b.setPositiveButton("ДА", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton)
                    {

                        String dirPath = DataManager.instance().dir.getPath();
                        DataManager.instance().storage.deleteDirectory(dirPath);
                        DataManager.instance().storage.createDirectory(dirPath);


                        initAdapter();
                        DataManager.instance().reset();
                        DataManager.instance().setDefaultFileName();
                    }
                });
                b.setNegativeButton("НЕТ", null);
                b.create().show();
            }
        });



        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());

        ShapeDrawable line = new ShapeDrawable();
        line.getPaint().setColor(0x0f000000);
        line.getPaint().setStyle(Paint.Style.FILL);
        line.setIntrinsicHeight(1);


        dividerItemDecoration.setDrawable(line);
        recyclerView.addItemDecoration(dividerItemDecoration);

        initAdapter();

        return view;

    }

    private void initAdapter() {
        adapter = new FileNameAdapter(DataManager.instance().getFileList(getActivity()));

        adapter.setOnClickListener(this);
        adapter.setOnLongClickListener(this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onItemClick(int position, File item) {

        Log.d("FileAdapter", item.getName());
        DataManager.instance().loadSupply(item, getActivity());
        getActivity().onBackPressed();
    }

    @Override
    public void onLongItemClick(int position, final File item) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle("Удалить приемку " + item.getName() + "?");
        b.setPositiveButton("ДА", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int whichButton)
            {
                DataManager.instance().storage.deleteFile(item.getPath());
                initAdapter();

                if (DataManager.instance().getFileName().equals(item.getName())) {
                    DataManager.instance().reset();
                    DataManager.instance().setDefaultFileName();
                }
            }
        });
        b.setNegativeButton("НЕТ", null);
        b.create().show();

    }
}
