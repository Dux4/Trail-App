// TrailViewActivity.java
package edublt.com.avaliacao2;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TrailViewActivity extends AppCompatActivity {
    private GoogleMap mMap;
    private DatabaseHelper dbHelper;
    private TextView infoText;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_view);

        prefs = getSharedPreferences("MapSettings", MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this);
        infoText = findViewById(R.id.trailInfoText);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this::onMapReady);
    }

    private void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Apply saved settings
        boolean isSatellite = prefs.getBoolean("isSatellite", false);
        boolean isCourseUp = prefs.getBoolean("isCourseUp", false);

        mMap.setMapType(isSatellite ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setRotateGesturesEnabled(!isCourseUp);

        loadTrailData();
    }

    @SuppressLint("Range")
    private void loadTrailData() {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_TRAILS,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_TIMESTAMP + " DESC"
        );

        ArrayList<LatLng> points = new ArrayList<>();
        long startTime = 0;
        long endTime = 0;
        float totalDistance = 0;
        Location lastPoint = null;
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        if (cursor.moveToFirst()) {
            startTime = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP));
            do {
                double lat = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE));
                double lng = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LONGITUDE));
                LatLng point = new LatLng(lat, lng);
                points.add(point);
                boundsBuilder.include(point);

                if (lastPoint != null) {
                    Location currentPoint = new Location("");
                    currentPoint.setLatitude(lat);
                    currentPoint.setLongitude(lng);
                    totalDistance += lastPoint.distanceTo(currentPoint);
                }

                Location currentLocation = new Location("");
                currentLocation.setLatitude(lat);
                currentLocation.setLongitude(lng);
                lastPoint = currentLocation;

            } while (cursor.moveToNext());
            endTime = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP));
        }
        cursor.close();

        // Draw trail on map
        if (!points.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(points)
                    .width(5)
                    .color(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            mMap.addPolyline(polylineOptions);

            // Zoom to show entire trail
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        }

        // Update info text
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        long duration = endTime - startTime;
        float averageSpeed = (totalDistance / 1000f) / (duration / 3600000f); // km/h

        String info = String.format(Locale.getDefault(),
                "Início: %s\nDuração: %02d:%02d:%02d\nDistância: %.2f km\nVelocidade Média: %.2f km/h",
                sdf.format(startTime), duration / 3600000, (duration % 3600000) / 60000, (duration % 60000) / 1000,
                totalDistance / 1000f, averageSpeed);
        infoText.setText(info);
    }
}
