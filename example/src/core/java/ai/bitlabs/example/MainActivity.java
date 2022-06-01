package ai.bitlabs.example;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import ai.bitlabs.sdk.BitLabs;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Example";

    BitLabs bitLabs = BitLabs.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bitLabs.init(this, "YOUR_APP_TOKEN", "USER_ID");

        bitLabs.checkSurveys(hasSurveys -> Log.i(TAG, hasSurveys != null ? hasSurveys.toString() : "NULL -  Check BitLabs Logs"));

        // optionally add custom tags to your users
        Map<String, Object> tags = new HashMap<>();
        tags.put("my_tag", "new_user");
        bitLabs.setTags(tags);

        bitLabs.addTag("is_premium", true);

        // Get client-side callbacks to reward the user (We highly recommend using server-to-server callbacks!)
        bitLabs.setOnRewardListener(payout -> Log.i("BitLabs", "Reward payout: " + payout));

        bitLabs.getSurveys(surveys -> {
            if (surveys == null)
                Log.i(TAG, "NULL -  Check BitLabs Logs");
            else {
                Log.i(TAG, "Surveys: " + surveys);
                if (!surveys.isEmpty()) surveys.get(0).open(this);
            }
        });

        findViewById(R.id.open).setOnClickListener(view -> bitLabs.launchOfferWall(this));
    }
}
