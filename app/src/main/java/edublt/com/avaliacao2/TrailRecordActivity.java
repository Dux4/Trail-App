package edublt.com.avaliacao2;

import android.Manifest;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.UUID;

public class TrailRecordActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseHelper dbHelper;
    private String currentTrailId;
    private Chronometer chronometer;
    private TextView speedText;
    private TextView distanceText;
    private float totalDistance = 0;
    private Location lastLocation;
    private SharedPreferences prefs;
    private PolylineOptions polylineOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_record);

        prefs = getSharedPreferences("MapSettings", MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this);
        currentTrailId = UUID.randomUUID().toString();

        chronometer = findViewById(R.id.chronometer);
        speedText = findViewById(R.id.speedText);
        distanceText = findViewById(R.id.distanceText);

        polylineOptions = new PolylineOptions().width(5).color(ContextCompat.getColor(this, android.R.color.holo_red_dark));

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this::onMapReady);

        // Request location permissions if needed
        if (checkLocationPermission()) {
            setupLocationUpdates();
        }

        chronometer.start();
    }

    private void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Apply saved settings
        boolean isSatellite = prefs.getBoolean("isSatellite", false);
        boolean isCourseUp = prefs.getBoolean("isCourseUp", false);

        // Configure map type
        mMap.setMapType(isSatellite ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);

        // Configure navigation mode
        if (isCourseUp) {
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        } else {
            mMap.getUiSettings().setRotateGesturesEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
        }

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void setupLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                updateLocationInfo(location);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateLocationInfo(Location location) {
        if (location == null) return;

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Update map
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
            polylineOptions.add(currentLatLng);
            mMap.addPolyline(polylineOptions);

            // Rotate the camera to match course if "Course Up" is enabled
            if (prefs.getBoolean("isCourseUp", false) && location.hasBearing()) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new com.google.android.gms.maps.model.CameraPosition.Builder()
                                .target(currentLatLng)
                                .zoom(17)
                                .bearing(location.getBearing())
                                .build()));
            }
        }

        // Update distance and speed
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);
            totalDistance += distance;
            distanceText.setText(String.format("Distância: %.2f km", totalDistance / 1000));
            speedText.setText(String.format("Velocidade: %.1f km/h", location.getSpeed() * 3.6));
        }

        // Save location to database
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TRAIL_ID, currentTrailId);
        values.put(DatabaseHelper.COLUMN_LATITUDE, location.getLatitude());
        values.put(DatabaseHelper.COLUMN_LONGITUDE, location.getLongitude());
        values.put(DatabaseHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());

        dbHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_TRAILS, null, values);

        lastLocation = location;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        chronometer.stop();
        dbHelper.close();
    }
}
