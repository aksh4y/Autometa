package com.akshaysadarangani.autometa;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import static android.app.Activity.RESULT_OK;

public class DialogActivity extends AppCompatDialogFragment {

    Button location_choose, location_pick;
    TextView place_lbl;
    SudoPlace position = null;
    DialogActivityListener listener;
    final int PLACE_PICKER_REQUEST = 1;
    private static final int MAP_ACTIVITY_REQUEST_CODE = 0;
    String userID, userName;
    int radius;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        assert getActivity() != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.location_picker, null);

        SetActivity setActivity = (SetActivity) getActivity();
        userID = setActivity.userID;
        userName = setActivity.userName;
        radius = setActivity.radius;

        builder.setView(view)
                .setTitle("Pick Location")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(position != null) {
                            SudoPlace plc = new SudoPlace(position.getLatLng(), position.getName());
                            listener.setLocation(plc);
                        }
                    }
                });

        location_choose = view.findViewById(R.id.location_cat);
        location_pick = view.findViewById(R.id.location_pick);
        place_lbl = view.findViewById(R.id.place_lbl);

        location_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        location_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getActivity(), MapsActivity.class);
                myIntent.putExtra("uid", userID);
                myIntent.putExtra("radius", radius);
                DialogActivity.this.startActivityForResult(myIntent, MAP_ACTIVITY_REQUEST_CODE);
            }
        });

        return  builder.create();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DialogActivityListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DialogActivityListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                assert getContext() != null;
                Place place = PlacePicker.getPlace(getContext(), data);
                position = new SudoPlace(place.getLatLng(), place.getName().toString());
                place_lbl.setText(position.name);
            }
        }
        if (requestCode == MAP_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                position = data.getParcelableExtra("place");
                place_lbl.setText(position.name);
            }
        }
    }

    public interface DialogActivityListener {
        void setLocation(SudoPlace loc);
    }
}
