package ru.dreamwhite.barcodereader.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import ru.dreamwhite.barcodereader.DataManager;
import ru.dreamwhite.barcodereader.MainActivity;
import ru.dreamwhite.barcodereader.R;
import ru.dreamwhite.barcodereader.model.moysklad.Item;

public class EditItemFragment extends Fragment {

    OnFragmentEditListener editListener;
    OnItemAmountChangedListener amountChangedListener;

    Item item;

    TextView title, subtitle;
    NumberPicker picker;

    Button delete, ok;

    String barcode;

    public static EditItemFragment newInstance(Item item) {
        EditItemFragment fragment = new EditItemFragment();
        Bundle args = new Bundle();
        args.putString("supplyKey", item.barcode);
        fragment.setArguments(args);

        return fragment;

    }

    public void setEditListener(OnFragmentEditListener listener) {
        editListener = listener;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setRetainInstance(true);
    }



    public void init() {

    }

    @Override
    public void onResume() {
        super.onResume();


        item = DataManager.instance().getItemForBarcode(barcode);

        title.setText(item.name);
        subtitle.setText(item.barcode);

        picker.setValue(item.count);

        if (editListener==null) {
            editListener =  (OnFragmentEditListener) getActivity();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_item, container, false);

        barcode = getArguments().getString("supplyKey");
        /*String barcode = getArguments().getString("supplyKey");

        item = DataManager.instance().getItemForBarcode(barcode);*/


        title = view.findViewById(R.id.store_spinner_title);
        subtitle = view.findViewById(R.id.agent_spinner_title);
        picker = view.findViewById(R.id.number_picker);

        picker.setMinValue(1);
        picker.setMaxValue(300);


        delete = view.findViewById(R.id.delete);
        ok = view.findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager.instance().deleteItem(item.barcode);
                editListener.onEdit();

                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });


        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {



                if (amountChangedListener == null)
                {
                    amountChangedListener = (MainActivity) getActivity();
                }

                amountChangedListener.itemAmountChanged(barcode, newVal);

//                Item temp = DataManager.instance().getItemForBarcode(item.barcode);
//                temp.count=newVal;
//                DataManager.instance().deleteItem(item.barcode);
//
//                DataManager.instance().addAssortmentItem(temp.barcode, temp);
//                Log.d("NumberPicker", temp.name + " " + temp.barcode + " " + temp.count);

                //editListener.onEdit();
            }
        });


        return view;



    }


}
