package com.example.mapdemo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by avkadam on 3/22/15.
 */
public class Restaurant {
        private String name;
        private String address; //formatted_address
        private String places_id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

    public String getPlaces_id() {
        return places_id;
    }

    public void setPlaces_id(String places_id) {
        this.places_id = places_id;
    }

    public static Restaurant fromJSON(JSONObject jsonObject) {
            Restaurant restaurant = new Restaurant();
            try {
                if (jsonObject.getString("status").equals("OK")) {
                    restaurant.name = jsonObject.getJSONObject("result").getString("name");
                    restaurant.places_id = jsonObject.getJSONObject("result").getString("place_id");
                    restaurant.address = jsonObject.getJSONObject("result").getString("formatted_address");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return restaurant;
    }

    public static void updateFromJSON(JSONObject jsonObject, Restaurant restaurant) {
        try {
            if (jsonObject.getString("status").equals("OK")) {
                if (restaurant.places_id.equals(jsonObject.getJSONObject("result").getString("place_id"))) {
                    restaurant.address = jsonObject.getJSONObject("result").getString("formatted_address");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Restaurant fromJSONLocal(JSONObject jsonObject) {
        Restaurant restaurant = new Restaurant();
        try {
            restaurant.name = jsonObject.getString("name");
            restaurant.places_id = jsonObject.getString("place_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return restaurant;
    }

        public static ArrayList<Restaurant> fromJSONArray(JSONArray jsonArray) {
            ArrayList listRestaurants = new ArrayList();
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject restaurantJSON = jsonArray.getJSONObject(i);
                    listRestaurants.add(fromJSONLocal(restaurantJSON));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return listRestaurants;
        }

}
