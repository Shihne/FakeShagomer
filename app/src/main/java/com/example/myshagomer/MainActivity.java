package com.example.myshagomer;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;
    Marker marker;
    Polyline polyline;
    List<LatLng> latLngList;
    TextView coords, way, result;
    EditText editText;
    Button button;
    Location loc;
    LocationManager manager;
    double lat = 0.0;
    double lon = 0.0;
    double newLat, newLon;
    double distance = 0.0;
    double allWay = 0.0;
    double step = 0.71;
    int steps = 0;
    private static final int REQUEST_GPS_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coords = (TextView) findViewById(R.id.coords);
        way = (TextView) findViewById(R.id.distance);
        result = (TextView) findViewById(R.id.result);
        button = (Button) findViewById(R.id.start);
        editText = (EditText) findViewById(R.id.height);

        button.setText("Начать");
        button.setBackgroundColor(Color.GREEN);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        final LatLng latLng = new LatLng(59.0, 29.0);
        marker = map.addMarker(new MarkerOptions().position(latLng));
        //map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.setMinZoomPreference(14.0f);

        int permissionCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionFine != PackageManager.PERMISSION_GRANTED
                && permissionCoarse != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_GPS_CODE);
            return;
        } else {

            button.setOnClickListener(
                    new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View v) {
                            if (button.getText() == "Начать") {
                                if (editText.getText().toString().length() != 0) {
                                    double height = Double.parseDouble(editText.getText().toString());
                                    step = height / 400 + 0.37;
                                }
                                button.setText("Закончить");
                                button.setBackgroundColor(Color.RED);
                                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    Activity#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for Activity#requestPermissions for more details.
                                    return;
                                }
                                Location lc = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                assert lc != null;
                                lat = lc.getLatitude();
                                lon = lc.getLongitude();
                                latLngList = new LinkedList<>();
                                latLngList.add(0, new LatLng(lat, lon));
                                polyline = map.addPolyline(new PolylineOptions().add(new LatLng(lat, lon)));

                                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 0, listener);
                            } else {
                                button.setText("Начать");
                                button.setBackgroundColor(Color.GREEN);
                                manager.removeUpdates(listener);
                            }
                        }
                    }
            );

        }
    }
    public LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            loc = location;
            newLat = loc.getLatitude();
            newLon = loc.getLongitude();
            LatLng ll = new LatLng(newLat, newLon);
            coords.setText("Координаты: " + newLat + ", " + newLon);
            marker.setPosition(ll);
            latLngList.add(latLngList.size(), ll);
            polyline.setPoints(latLngList);
            map.moveCamera(CameraUpdateFactory.newLatLng(ll));
            distance += latlngDistance(lat, lon, newLat, newLon);

            way.setText("Расстояние: " + allWay);
            if (distance > step) {
                steps += (int)(distance / step);
                allWay = allWay + distance;
                distance = distance - (int)(distance / step);
            }
            result.setText("Всего шагов: " + steps);
            lat = newLat;
            lon = newLon;

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public double latlngDistance(double lat1, double lon1, double lat2, double lon2) {
        int r = 6372795;
        lat1 *= Math.PI / 180;
        lat2 *= Math.PI / 180;
        lon1 *= Math.PI / 180;
        lon2 *= Math.PI / 180;
        double cl1 = Math.cos(lat1);
        double cl2 = Math.cos(lat2);
        double sl1 = Math.sin(lat1);
        double sl2 = Math.sin(lat2);
        double delta = lon2 - lon1;
        double cdelta = Math.cos(delta);
        double sdelta = Math.sin(delta);
        double y = Math.sqrt(Math.pow(cl2 * sdelta, 2) + Math.pow(cl1 * sl2 - sl1 * cl2 * cdelta, 2));
        double x = sl1 * sl2 + cl1 * cl2 * cdelta;
        double ad = Math.atan2(y, x);
        double dist = ad * r;
        return dist;
    }
}
