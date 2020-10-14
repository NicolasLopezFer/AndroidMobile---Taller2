package com.example.taller2;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_CODE = 11;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private int contador = 0;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightSensorListener;
    private Geocoder geocoder;
    private EditText direccion;
    private Marker locationMarker;
    private Marker searchMarker;
    private Activity me;
    public static final double lowerLeftLatitude = 1.396967;
    public static final double lowerLeftLongitude= -78.903968;
    public static final double upperRightLatitude= 11.983639;
    public static final double upperRigthLongitude= -71.869905;
    private static final int RADIUS_OF_EARTH_KM = 6371;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        direccion = findViewById(R.id.direccion);

        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        geocoder = new Geocoder(getBaseContext());

        me = this;


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PermissionUtil.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION,"", LOCATION_CODE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(lightSensorListener,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(lightSensorListener);
    }

    private void setMarker(LocationResult locationResult) {
        Location location = locationResult.getLastLocation();
        LatLng myLocation = new LatLng(location.getLatitude(),location.getLongitude());
        locationMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title("Tú"));
        if(contador == 0){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            contador+=1;
        }else{
            locationMarker.remove();
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
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    setMarker(locationResult);
                }
            };
            fusedLocationProviderClient.requestLocationUpdates(createLocationRequest(),locationCallback, Looper.myLooper());
        }

        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (mMap != null) {
                    if (sensorEvent.values[0] < 5000) {
                        Log.i("MAPS", "DARK MAP " + sensorEvent.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.dark_style));
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + sensorEvent.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.retro_style));
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        direccion.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEND){
                    String addressString = direccion.getText().toString();
                    direccion.setText("");
                    hideSoftKeyboard(me);
                    if(!addressString.isEmpty()){
                        try{
                            List<Address> addresses = geocoder.getFromLocationName(addressString,2,lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRigthLongitude);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address addressResult = addresses.get(0);
                                LatLng position = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                                if (mMap != null) {
                                    if(searchMarker != null) {
                                        searchMarker.remove();
                                    }
                                    searchMarker=mMap.addMarker(new MarkerOptions().position(position).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                                    Double latitude1 = searchMarker.getPosition().latitude;
                                    Double latitude2 = locationMarker.getPosition().latitude;
                                    Double longitude1 = searchMarker.getPosition().longitude;
                                    Double longitude2 = locationMarker.getPosition().longitude;
                                    Toast.makeText(MapsActivity.this, "La distancia es: " + distance(latitude1,longitude1,latitude2,longitude2)+" Km", Toast.LENGTH_SHORT).show();
                                    //getRoute(latitude2,longitude2,latitude1,longitude1);
                                }
                            } else {Toast.makeText(MapsActivity.this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();}
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                    }else {
                        Toast.makeText(MapsActivity.this, "La dirección esta vacía", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(searchMarker!=null){
                    searchMarker.remove();
                }
                searchMarker=mMap.addMarker(new MarkerOptions().position(latLng).title(geoCoderSearchLatLang(latLng)));
                Double latitude1 = searchMarker.getPosition().latitude;
                Double latitude2 = locationMarker.getPosition().latitude;
                Double longitude1 = searchMarker.getPosition().longitude;
                Double longitude2 = locationMarker.getPosition().longitude;
                Toast.makeText(MapsActivity.this, "La distancia es: " + distance(latitude1,longitude1,latitude2,longitude2)+" Km", Toast.LENGTH_SHORT).show();

                //getRoute(latitude2,longitude2,latitude1,longitude1);

            }
        });

    }

    private void getRoute(Double latitude1,Double longitude1,Double latitude2,Double longitude2) {

        String origen = latitude1.toString()+","+longitude1.toString();
        String destino = latitude2.toString()+","+longitude2.toString();
        List<LatLng> path = new ArrayList();


        //Execute Directions API request
        GeoApiContext context = new GeoApiContext();
        context.setApiKey("AIzaSyBqRcTbIZik18YabgfWg8G-EoTRV-zaUsE");

        DirectionsApiRequest req = DirectionsApi.getDirections(context, origen,destino);
        try{
            DirectionsResult res = req.await();
            Log.i("DIRECCION",res.toString());

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            Log.i("DIRECCION",e.getMessage());
        }

        //Draw the polyline
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
            mMap.addPolyline(opts);
        }
    }

    private String geoCoderSearchLatLang(LatLng latLng) {
        String finalName="";
        try {
            Address name = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
            finalName = name.getAddressLine(0);
        }catch (IOException e){
            e.printStackTrace();
        }
        return finalName;
    }

    protected LocationRequest createLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); //tasa de refresco en milisegundos
        locationRequest.setFastestInterval(5000); //máxima tasa de refresco
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = RADIUS_OF_EARTH_KM * c;
        return Math.round(result*100.0)/100.0;
    }




}