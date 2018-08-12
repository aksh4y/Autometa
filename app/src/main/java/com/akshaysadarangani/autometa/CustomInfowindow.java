package com.akshaysadarangani.autometa;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfowindow implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfowindow(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.custom_infowindow, null);
        TextView title= view.findViewById(R.id.content);
        TextView tip = view.findViewById(R.id.staticTip);
        title.setText(marker.getTitle());
        if(marker.getTitle().equals("SEARCH HERE"))
            tip.setText("");
        else
            tip.setText(R.string.press_and_hold_this_window_to_set_up_a_geofence);
        return view;
    }
}