package com.example.currentplacedetailsonmap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by Ovhagen on 2017-05-12.
 */

public class AntiScrollMapView extends MapView {
    public AntiScrollMapView(Context context) {
        super(context);
    }

    public AntiScrollMapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AntiScrollMapView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public AntiScrollMapView(Context context, GoogleMapOptions googleMapOptions) {
        super(context, googleMapOptions);
    }

    @Override
    public void getMapAsync(OnMapReadyCallback onMapReadyCallback) {
        super.getMapAsync(onMapReadyCallback);
    }

    //Puts the MapView event over the scroll view
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        int action = ev.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;

        }
        return super.dispatchTouchEvent(ev);
    }

}
