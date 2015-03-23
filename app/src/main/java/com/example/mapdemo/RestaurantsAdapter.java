package com.example.mapdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by avkadam on 3/22/15.
 */
public class RestaurantsAdapter extends ArrayAdapter {

    public RestaurantsAdapter(Context context, List objects) {
        super(context, R.layout.item_restaurant, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tvName;
        TextView tvAddress;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_restaurant, parent, false);
        }
        Restaurant restaurant = (Restaurant) getItem(position);

        tvName = (TextView) convertView.findViewById(R.id.tvName);
        tvName.setText(restaurant.getName());

        tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
        tvAddress.setText(restaurant.getAddress());

        return convertView;
    }
}
