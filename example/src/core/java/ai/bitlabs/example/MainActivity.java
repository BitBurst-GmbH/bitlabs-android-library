package ai.bitlabs.example;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import ai.bitlabs.sdk.BitLabs;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Example";

    private final BitLabs bitLabs = BitLabs.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bitLabs.init(this, "YOUR_APP_TOKEN", "USER_ID");

        // optionally add custom tags to your users
        Map<String, Object> tags = new HashMap<>();
        tags.put("my_tag", "new_user");
        bitLabs.setTags(tags);

        bitLabs.addTag("is_premium", true);

        // Get client-side callbacks to reward the user (We highly recommend using server-to-server callbacks!)
        bitLabs.setOnRewardListener(payout -> Log.i("BitLabs", "Reward payout: " + payout));

        findViewById(R.id.btn_check_surveys).setOnClickListener(view -> bitLabs.checkSurveys(hasSurveys ->
                Log.i(TAG, hasSurveys != null
                        ? hasSurveys.toString()
                        : "Couldn't check for surveys -  Check BitLabs Logs"))
        );

        findViewById(R.id.btn_get_surveys).setOnClickListener(view -> bitLabs.getSurveys(surveys -> {
            if (surveys == null)
                Log.i(TAG, "Couldn't get surveys -  Check BitLabs Logs");
            else {
                Log.i(TAG, "Surveys: " + surveys);
                if (!surveys.isEmpty()) surveys.get(0).open(this);
            }
        }));

        findViewById(R.id.btn_launch_offerwall).setOnClickListener(view -> bitLabs.launchOfferWall(this));
    }
}
