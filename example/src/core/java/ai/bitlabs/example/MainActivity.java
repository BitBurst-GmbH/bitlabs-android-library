package ai.bitlabs.example;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import ai.bitlabs.sdk.BitLabs;
import ai.bitlabs.sdk.data.model.Survey;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Example";

    private final BitLabs bitLabs = BitLabs.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bitLabs.init(this, "46d31e1e-315a-4b52-b0de-eca6062163af", "USER_ID");

        // optionally add custom tags to your users
        Map<String, Object> tags = new HashMap<>();
        tags.put("my_tag", "new_user");
        bitLabs.setTags(tags);

        bitLabs.addTag("is_premium", true);

        // Get client-side callbacks to reward the user (We highly recommend using server-to-server callbacks!)
        bitLabs.setOnRewardListener(payout -> Log.i(TAG, "Reward payout: " + payout));

        findViewById(R.id.btn_check_surveys).setOnClickListener(view -> bitLabs.checkSurveys(
                hasSurveys -> Log.i(TAG, hasSurveys ? "Found Surveys": "No Surveys"),
                e -> Log.e(TAG, "CheckSurveysErr: " + e.getMessage(), e.getCause()))
        );

        findViewById(R.id.btn_get_surveys).setOnClickListener(view -> bitLabs.getSurveys(
                surveys -> {
                    for (Survey survey :
                            surveys) {
                        Log.i(TAG, "Survey " + survey.getId() + " in Category " + survey.getDetails().getCategory().getName());
                    }
                },
                exception -> Log.e(TAG, "GetSurveysErr: " + exception.getMessage(), exception.getCause()))
        );

        findViewById(R.id.btn_launch_offerwall).setOnClickListener(view -> bitLabs.launchOfferWall(this));
    }
}
