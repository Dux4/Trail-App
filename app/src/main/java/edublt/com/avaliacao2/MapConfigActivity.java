package edublt.com.avaliacao2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
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

        mapTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isSatellite", checkedId == R.id.satelliteType);
            editor.apply();
        });

        navigationModeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isCourseUp", checkedId == R.id.courseUp);
            editor.apply();
        });
    }
}