package ai.bitlabs.example;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import ai.bitlabs.sdk.BitLabsSDK;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BitLabsSDK.Companion.init(MainActivity.this, "6c7083df-b97e-4d29-9d90-798fd088bc08", "YOUR_USER_ID");

        Map<String, Object> tags = new HashMap<>();
        tags.put("my_tag", "new_user");
        tags.put("is_premium", true);
        BitLabsSDK.Companion.setTags(tags);

        findViewById(R.id.open).setOnClickListener(view -> {
            if (BitLabsSDK.Companion.surveyAvailable())
                BitLabsSDK.Companion.show(view.getContext());
        });
    }
}
