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

        bitLabs.init(this, BuildConfig.APP_TOKEN, "diffindocongress");

//        bitLabs.setDebugMode(true);

        // optionally add custom tags to your users
        Map<String, Object> tags = new HashMap<>();
        tags.put("my_tag", "new_user");
        bitLabs.setTags(tags);
        bitLabs.addTag("is_premium", true);

        // Get client-side callbacks to reward the user (We highly recommend using server-to-server callbacks!)
        bitLabs.setOnRewardListener(payout -> Log.i(TAG, "Reward payout: " + payout));

        findViewById(R.id.btn_check_surveys).setOnClickListener(view -> bitLabs.checkSurveys(hasSurveys -> Log.i(TAG, hasSurveys ? "Found Surveys" : "No Surveys"), e -> Log.e(TAG, "CheckSurveysErr: " + e.getMessage(), e.getCause())));

        findViewById(R.id.btn_get_surveys).setOnClickListener(view -> bitLabs.getSurveys(
                surveys -> {
                    for (Survey survey : surveys) {
                        Log.i(TAG, "Survey Id: " + survey.getId() + " in " + survey.getCategory().getName());
                    }
                },
                exception -> Log.e(
                        TAG,
                        "GetSurveys Error: " + exception.getMessage(),
                        exception.getCause()))
        );

        findViewById(R.id.btn_show_survey_widget).setOnClickListener(view ->
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.rl_survey_widgets, bitLabs.getSurveyWidget())
                        .commit()
        );

        findViewById(R.id.btn_launch_offerwall).setOnClickListener(view -> bitLabs.launchOfferWall(this));

        findViewById(R.id.btn_show_leaderboard).setOnClickListener(view ->
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view_tag, bitLabs.getLeaderboardWidget())
                        .commit()
        );
    }
}
