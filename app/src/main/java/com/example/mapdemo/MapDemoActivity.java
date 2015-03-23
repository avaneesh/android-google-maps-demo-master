package com.example.mapdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


// maps API key: AIzaSyDfQovu3zQ9MN1dyeSOiGKsvQBLNhi8vmg
public class MapDemoActivity extends FragmentActivity implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationListener,
        GoogleMap.OnMapLongClickListener {

	private SupportMapFragment mapFragment;
	private GoogleMap map;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	private long UPDATE_INTERVAL = 60000;  /* 60 secs */
	private long FASTEST_INTERVAL = 5000; /* 5 secs */

    private EditText etSearch;
    private ListView lvResults;
    private ArrayList<Restaurant> listRestaurants;
    private RestaurantsAdapter aRestaurants;

//    private final String GOOGLE_API_KEY="AIzaSyDfQovu3zQ9MN1dyeSOiGKsvQBLNhi8vmg";
//    private final String GOOGLE_API_KEY = "AIzaSyDtz2n8SizCnw1jw5A8TTlLDYHqKpEvi9I"; // android key - doesnt work
    private final String GOOGLE_API_KEY = "AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE"; // server key - works

	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_demo_activity);

		mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
		if (mapFragment != null) {
			mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
		} else {
			Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
		}

        etSearch = (EditText) findViewById(R.id.etSearch);
        lvResults = (ListView) findViewById(R.id.lvResults);

        listRestaurants = new ArrayList();
        aRestaurants = new RestaurantsAdapter(this, listRestaurants);
        lvResults.setAdapter(aRestaurants);
	}

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            map.setMyLocationEnabled(true);
            map.setOnMapLongClickListener(this);
            map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater()));

            // Now that map has loaded, let's get our location!
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            connectClient();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void connectClient() {
        // Connect the client.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /*
     * Called when the Activity becomes visible.
    */
    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
    }

    /*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
		super.onStop();
	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				mGoogleApiClient.connect();
				break;
			}

		}
	}

	private boolean isGooglePlayServicesAvailable() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			return true;
		} else {
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST);

			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getSupportFragmentManager(), "Location Updates");
			}

			return false;
		}
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle dataBundle) {
		// Display the connection status
		Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (location != null) {
			Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
			map.animateCamera(cameraUpdate);
            startLocationUpdates();
        } else {
			Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
		}
	}

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    /*
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			Toast.makeText(getApplicationContext(),
					"Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
		}
	}

    @Override
    public void onMapLongClick(LatLng latLng) {
        Toast.makeText(this, "Long Press", Toast.LENGTH_LONG).show();

        showAlertDialogForPoint(latLng);
    }

    private void showAlertDialogForPoint(final LatLng point) {
        // inflate message_item.xml view
        View messageView = LayoutInflater.from(MapDemoActivity.this).
                inflate(R.layout.messages_item, null);
        // Create alert dialog builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set message_item.xml to AlertDialog builder
        alertDialogBuilder.setView(messageView);

        // Create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // Configure dialog button (OK)
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Define color of marker icon
                        BitmapDescriptor defaultMarker =
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                        // Extract content from alert dialog
                        String title = ((EditText) alertDialog.findViewById(R.id.etTitle)).
                                getText().toString();
                        String snippet = ((EditText) alertDialog.findViewById(R.id.etSnippet)).
                                getText().toString();
                        // Creates and adds marker to the map
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(point)
                                .title(title)
                                .snippet(snippet)
                                .icon(defaultMarker));

                        // Animate marker using drop effect
                        // --> Call the dropPinEffect method here
                        dropPinEffect(marker);
                    }
                });

        // Configure dialog button (Cancel)
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
                });

        // Display the dialog
        alertDialog.show();
    }

    // Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
//        long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            long start = SystemClock.uptimeMillis();
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
//                    start = SystemClock.uptimeMillis();
//                    handler.postDelayed(this, 15);

                }
            }
        });
    }


    class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater mInflater;

        public CustomWindowAdapter(LayoutInflater i){
            mInflater = i;
        }

        // This defines the contents within the info window based on the marker
        @Override
        public View getInfoContents(Marker marker) {
            // Getting view from the layout file
            View v = mInflater.inflate(R.layout.custom_info_window, null);
            // Populate fields
            TextView title = (TextView) v.findViewById(R.id.tv_info_window_title);
            title.setText(marker.getTitle());

            TextView description = (TextView) v.findViewById(R.id.tv_info_window_description);
            // Return info window contents
            String html_desc = Html.fromHtml(marker.getSnippet()).toString();
            description.setText(html_desc);
            return v;
        }

        // This changes the frame of the info window; returning null uses the default frame.
        // This is just the border and arrow surrounding the contents specified above
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }
    }

    public void onSearchClick(View v){
        String searchQ = etSearch.getText().toString();
        if (searchQ.isEmpty()) {
            searchQ = "restaurants";
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        String currLongitude = Double.toString(location.getLongitude());
        String currLatitue = Double.toString(location.getLatitude());
        String locationQ = currLatitue+","+currLongitude;
        Toast.makeText(this, "Searching "+searchQ+" near: "+locationQ, Toast.LENGTH_SHORT).show();

//          String places_search_q="https://maps.googleapis.com/maps/api/place/search/json?location="+locationQ+"&sensor=true&key="+GOOGLE_API_KEY+
//                "&keyword="+searchQ+"&types=food&rankby=distance"; // WORKS
//
          String places_search_q="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+locationQ+
                "&key="+GOOGLE_API_KEY+
                "&keyword="+searchQ+"&rankby=distance"; // WORKS


        doSearch(places_search_q);
    }

    private void doSearch(String places_search_q) {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(this, places_search_q, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e("RESP", response.toString());
                try {
                    if (response.getString("status").equals("OK")) {
                        handleSearchResp(response);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Error: "+response.getString("error_message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Failed!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSearchResp(JSONObject response){
        try {
            aRestaurants.clear();
            aRestaurants.addAll(Restaurant.fromJSONArray(response.getJSONArray("results")));
            getPlaceDetails();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // Retrieve address from Place ID and update UI
    private ArrayList<Restaurant> getPlaceDetails() {
        ArrayList listRestaurants = new ArrayList();
        String detailsQ = "https://maps.googleapis.com/maps/api/place/details/json?key="+GOOGLE_API_KEY+"&placeid=";
        for (int i = 0; i < aRestaurants.getCount(); i++) {
            Restaurant restaurant = (Restaurant) aRestaurants.getItem(i);
            String currPlaceId = restaurant.getPlaces_id();
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(this, detailsQ+currPlaceId, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    for (int i = 0; i < aRestaurants.getCount(); i++) {
                        Restaurant.updateFromJSON(response, (Restaurant) aRestaurants.getItem(i));
                    }
                    aRestaurants.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(getApplicationContext(), "Could not get details!!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        return listRestaurants;
    }
}
