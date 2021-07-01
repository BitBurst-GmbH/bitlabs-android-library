package ai.bitlabs.example;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import ai.bitlabs.sdk.BitLabsSDK;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BitLabsSDK.Companion.init(MainActivity.this, "YOUR_TOKEN", "YOUR_USER_ID");

        // optionally add custom tags to your users
        Map<String, Object> tags = new HashMap<>();
        tags.put("my_tag", "new_user");
        tags.put("is_premium", true);
        BitLabsSDK.Companion.setTags(tags);

        findViewById(R.id.open).setOnClickListener(view ->
                BitLabsSDK.Companion.hasSurveys(
                        // NOTE: the offerwall can be shown without checking for surveys first
                        response -> BitLabsSDK.Companion.show(this),
                        error -> Log.e("BitLabs", error.toString())
                )
        );
    }
}
