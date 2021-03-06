package com.example.costellom3761.mymapsapp;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Network;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;



import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private boolean isGPSenabled;
    private boolean isNetWorkEnabled;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private Location myLocation;
    private static final int MY_LOC_ZOOM_FACTOR = 17;
    private ArrayList<Circle> circles;
    EditText editSearch;
    String searchThis;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        editSearch = (EditText)findViewById(R.id.editText_search);



    }

    public void switchView(View v) {
        if (mMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (mMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng birthPlace = new LatLng(32.885126, -117.225515);
        mMap.addMarker(new MarkerOptions().position(birthPlace).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(birthPlace));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed Permission Check 1");
            Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed Permission Check 2");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);

        }


        //mMap.setMyLocationEnabled(true);
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //list of all markers made by GPS/Network for tracking
            circles = new ArrayList<Circle>();

            //getGPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled) Log.d("MyMaps", "getLocation: GPS is enabled");

            //get network status
            isNetWorkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetWorkEnabled) Log.d("MyMaps", "getLocation: NETWORK is enabled");

            if (!isGPSenabled && !isNetWorkEnabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled!");
            } else {
                this.canGetLocation = true;

                if (isNetWorkEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    Log.d("MyMaps", "getLocation: NetworkLoc update request successful.");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT).show();

                }
                if (isGPSenabled) {
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                    PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);
                    Log.d("MyMaps", "getLocation: GPS update request successful");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception E) {
            Log.d("MyMaps", "Caught an exception in my getLocation method");
            E.printStackTrace();
        }
    }


    android.location.LocationListener locationListenerGPS = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // output in log.d and Toast messages that GPS is enabled and working
            Log.d("MyMaps", "GPS is enabled and working");
            Toast.makeText(getApplicationContext(), "GPS is enabled and working", Toast.LENGTH_SHORT).show();


            //drop a marker on map - create a method called dropMarker
            dropMarkerGPS(LocationManager.GPS_PROVIDER);
            Log.d("MyMaps", "Marker dropped at location by GPS");

            //Remove the network location updates. Hint see LocationManager for update removal method.
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // output in log.d and Toast messages that GPS is enabled and working
            Log.d("MyMaps", "GPS is enabled and working");
            Toast.makeText(getApplicationContext(), "GPS is enabled and working", Toast.LENGTH_SHORT).show();

            //setup a switch statement to check the status input parameter
            //case LocationProvider.AVAILABLE --> output message to Log.d and Toast
            //case LocationProvider.OUT_OF_SERVICE --> request updates from NETWORK_PROVIDER
            //case LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER
            //case default --> request updates from NETWORK_PROVIDER
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "GPS is available");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "GPS not available permanent");
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "GPS temporarily unavailable");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                default:
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // output in log.d and Toast messages that network is enabled and working
            Log.d("MyMaps", "Network is enabled and working");
            Toast.makeText(getApplicationContext(), "Network is enabled and working", Toast.LENGTH_SHORT).show();

            //drop a marker on map - create a method called dropMarker
            dropMarkerNetwork(LocationManager.NETWORK_PROVIDER);
            //REPLACE WITH NETWORK_PROVIDER
            Log.d("MyMaps", "Marker dropped at location by Network");

            //Relaunch the network provider request (requestLocationUpdates (NETWORK_PROVIDER))
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // output message in Log.d and Toast
            Log.d("MyMaps", "Network is enabled and working");
            Toast.makeText(getApplicationContext(), "Network is enabled and working", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void dropMarkerNetwork(String provider) {

        LatLng userLocation = null;

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            myLocation = locationManager.getLastKnownLocation(provider);
        }
        if (myLocation == null) {
            //display a message via log.d and/or toast
            Log.d("MyMaps", "myLocation is null");
        } else {
            //get the user location
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            //display a message with the lat/long

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            //drop the actual marker on the map
            //if using circles, reference Android Circle class
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(userLocation)
                    .radius(4)
                    .strokeColor(Color.GREEN)
                    .strokeWidth(2)
                    .fillColor(Color.GREEN));
            circles.add(circle);



            mMap.animateCamera(update);

            Toast.makeText(this, "Latitude: " + myLocation.getLatitude() + "\nLongitude: " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            Log.d("MyMaps", "Latitude: " + myLocation.getLatitude() + "\nLongitude: " + myLocation.getLongitude());
        }
    }

    public void dropMarkerGPS(String provider) {

        LatLng userLocation = null;

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            myLocation = locationManager.getLastKnownLocation(provider);
        }
        if (myLocation == null) {
            //display a message via log.d and/or toast
            Log.d("MyMaps", "myLocation is null");
        } else {
            //get the user location
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            //display a message with the lat/long

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            //drop the actual marker on the map
            //if using circles, reference Android Circle class
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(userLocation)
                    .radius(4)
                    .strokeColor(Color.RED)
                    .strokeWidth(2)
                    .fillColor(Color.RED));
            circles.add(circle);


            mMap.animateCamera(update);

            Toast.makeText(this, "Latitude: " + myLocation.getLatitude() + "\nLongitude: " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            Log.d("MyMaps", "Latitude: " + myLocation.getLatitude() + "\nLongitude: " + myLocation.getLongitude());
        }
    }

    public void track(View v) {
        if (canGetLocation == true) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGPS);
            canGetLocation = false;
        }
        else
            getLocation();
    }

    public void clear(View v) {
        for (Circle c : circles) {
            c.remove();

        }
        circles.clear();
    }

    public void search(View v) {
        //searches address based on editSearch using a Geocoder.
        Geocoder geo = new Geocoder(this);
        searchThis = editSearch.getEditableText().toString();
        StringBuffer buffer = new StringBuffer();

        List<Address> places = new ArrayList<Address>();
        try {
            //results in null pointer exception with lat/long boundaries.
            if(myLocation != null && searchThis.length()>0) {
               // places = geo.getFromLocationName(searchThis,
                      //  5, myLocation.getLatitude()-0.07234315595, myLocation.getLongitude()-(getChangeLongitude(myLocation.getLatitude(),5)),
                      //  myLocation.getLatitude()+0.07234315595, myLocation.getLongitude()+(getChangeLongitude(myLocation.getLatitude(),5)));

                places = geo.getFromLocationName(searchThis, 5, myLocation.getLatitude()-.0125, myLocation.getLongitude()-.015,
                        myLocation.getLatitude()+.0125, myLocation.getLongitude()+.015);

                Log.d("MyMaps", "No exception caught :)");
            }
            else if (myLocation == null){
                Log.d("MyMaps", "your location is null :(");
            }
            else if (searchThis.length()==0){
                Log.d("MyMaps", "you didn't enter a search");
                showMessage("No Search Results Available", "No keyword entered...");
            }
        } catch (IOException e) {
            Log.d("MyMaps", "Exception caught in search method");
            e.printStackTrace();
        }
        if (places.size()>0) {
            for (Address a : places) {
                buffer.append(a.getAddressLine(0) + "\n" + a.getAddressLine(1)
                        + "\n" + a.getAddressLine(2)
                        + "\n" + a.getAddressLine(3)
                        + "\n\n");
                LatLng thePlace = new LatLng(a.getLatitude(),a.getLongitude());

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(thePlace)
                        .radius(20)
                        .strokeColor(Color.MAGENTA)
                        .strokeWidth(2)
                        .fillColor(Color.MAGENTA));

                circles.add(circle);

            }
            showMessage("Search Results", buffer.toString());
            Log.d("MyMaps", "Results shown");
        }
        else if (places.size()==0 && searchThis.length()>0){
            showMessage("No Search Results Available", "Nothing in 5 Mile Radius");
            Log.d("MyMaps", "No Results shown");

        }



    }

    private void showMessage(String title, String message) {
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true); //cancel using back button
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    private double getChangeLongitude(double latitude, int miles) {
        double degreesRadians = (Math.PI/180);
        double radiansDegrees = (180/Math.PI);
        double r = 3690*(Math.cos(latitude * (degreesRadians)));
        return ((miles/r)*radiansDegrees);
    }

}
