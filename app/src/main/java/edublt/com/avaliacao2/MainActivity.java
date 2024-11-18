package edublt.com.avaliacao2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnMapConfig = findViewById(R.id.btnMapConfig);
        Button btnRecordTrail = findViewById(R.id.btnRecordTrail);
        Button btnViewTrail = findViewById(R.id.btnViewTrail);

        btnMapConfig.setOnClickListener(v ->
                startActivity(new Intent(this, MapConfigActivity.class)));

        btnRecordTrail.setOnClickListener(v ->
                startActivity(new Intent(this, TrailRecordActivity.class)));

        btnViewTrail.setOnClickListener(v ->
                startActivity(new Intent(this, TrailViewActivity.class)));
    }
}