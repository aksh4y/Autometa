package com.akshaysadarangani.autometa;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class SudoPlace implements Parcelable {
    LatLng latLng;
    String name;

    public SudoPlace(LatLng latLng, String name) {
        this.latLng = latLng;
        this.name = name;
    }

    public LatLng getLatLng() {

        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected SudoPlace(Parcel in) {
        this.name = in.readString();
        this.latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(this.latLng, flags);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SudoPlace createFromParcel(Parcel in) {
            return new SudoPlace(in);
        }

        public SudoPlace[] newArray(int size) {
            return new SudoPlace[size];
        }
    };
}
