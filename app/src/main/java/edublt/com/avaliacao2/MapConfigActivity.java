package edublt.com.avaliacao2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MapConfigActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_config);

        prefs = getSharedPreferences("MapSettings", MODE_PRIVATE);

        RadioGroup mapTypeGroup = findViewById(R.id.mapTypeGroup);
        RadioGroup navigationModeGroup = findViewById(R.id.navigationModeGroup);
        Button saveButton = findViewById(R.id.saveButton);

        // Load saved preferences
        boolean isSatellite = prefs.getBoolean("isSatellite", false);
        boolean isCourseUp = prefs.getBoolean("isCourseUp", false);

        ((RadioButton) findViewById(isSatellite ? R.id.satelliteType : R.id.normalType)).setChecked(true);
        ((RadioButton) findViewById(isCourseUp ? R.id.courseUp : R.id.northUp)).setChecked(true);

        // Save preferences on button click
        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isSatellite", mapTypeGroup.getCheckedRadioButtonId() == R.id.satelliteType);
            editor.putBoolean("isCourseUp", navigationModeGroup.getCheckedRadioButtonId() == R.id.courseUp);
            editor.apply();
            Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
        });
    }
}
