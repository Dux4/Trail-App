package edublt.com.avaliacao2;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_view);

        dbHelper = new DatabaseHelper(this);
        infoText = findViewById(R.id.trailInfoText);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this::onMapReady);
        }
    }

    private void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
                DatabaseHelper.COLUMN_TIMESTAMP + " ASC" // Ordem cronológica
        );

        if (cursor == null || !cursor.moveToFirst()) {
            infoText.setText("Nenhuma trilha encontrada.");
            return;
        }

        ArrayList<LatLng> points = new ArrayList<>();
        long startTime = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP));
        long endTime = startTime;
        float totalDistance = 0f;
        Location lastPoint = null;
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        do {
            double lat = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE));
            double lng = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LONGITUDE));
            long timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP));

            LatLng point = new LatLng(lat, lng);
            points.add(point);
            boundsBuilder.include(point);

            // Calcula a distância entre pontos consecutivos
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

            endTime = timestamp;
        } while (cursor.moveToNext());
        cursor.close();

        // Verifica se há pontos suficientes para exibir no mapa
        if (!points.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(points)
                    .width(5)
                    .color(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            mMap.addPolyline(polylineOptions);

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        }

        // Calcula a duração e a velocidade média
        long durationMillis = endTime - startTime;
        float durationHours = durationMillis / (3600f * 1000); // Converte para horas
        float averageSpeed = (durationHours > 0) ? (totalDistance / 1000f) / durationHours : 0;

        // Logs para depuração
        Log.d("TrailViewActivity", "Duração em milissegundos: " + durationMillis);
        Log.d("TrailViewActivity", "Duração em horas: " + durationHours);
        Log.d("TrailViewActivity", "Distância total (km): " + (totalDistance / 1000f));
        Log.d("TrailViewActivity", "Velocidade média (km/h): " + averageSpeed);

        // Formata os dados e exibe
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        long durationSeconds = durationMillis / 1000;
        String info = String.format(Locale.getDefault(),
                "Início: %s\nDuração: %02d:%02d:%02d\nDistância: %.2f km\nVelocidade Média: %.2f km/h",
                sdf.format(startTime),
                durationSeconds / 3600, (durationSeconds % 3600) / 60, (durationSeconds % 60),
                totalDistance / 1000f, averageSpeed);
        infoText.setText(info);
    }
}
