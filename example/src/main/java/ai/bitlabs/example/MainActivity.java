package ai.bitlabs.example;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import ai.bitlabs.sdk.BitLabs;
import ai.bitlabs.sdk.BitLabsSDK;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "EXAMPLE";

    BitLabs bitLabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BitLabsSDK.Companion.init(MainActivity.this, "YOUR_TOKEN", "YOUR_USER_ID");

        bitLabs = new BitLabs("YOUR_TOKEN", "Test-User");

        bitLabs.hasSurveys(hasSurveys -> {
            Log.i(TAG, hasSurveys.toString());
            return null;
        });

        // optionally add custom tags to your users
        Map<String, Object> tags = new HashMap<>();
        tags.put("my_tag", "new_user");
        tags.put("is_premium", true);
        bitLabs.setTags(tags);
//        BitLabsSDK.Companion.setTags(tags);

        // Get client-side callbacks to reward the user (We highly recommend using server-to-server callbacks!)
        BitLabsSDK.Companion.onReward(payout -> Log.e("BitLabs", "BitLabs payout of: " + payout));

        findViewById(R.id.open).setOnClickListener(view -> bitLabs.launchOfferWall(this));
    }
}
