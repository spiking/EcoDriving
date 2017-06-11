package com.example.currentplacedetailsonmap.models;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Atlas on 2017-05-04.
 */

public class LatLngSerializedObject implements java.io.Serializable {

    private transient com.google.android.gms.maps.model.LatLng mLocation;
    public LatLngSerializedObject(LatLng mLocation) {
        this.mLocation = mLocation;
    }
    public LatLng getLatLng() {
        return mLocation;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(mLocation.latitude);
        out.writeDouble(mLocation.longitude);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        mLocation = new LatLng(in.readDouble(), in.readDouble());
    }
}
