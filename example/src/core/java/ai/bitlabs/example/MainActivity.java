package ai.bitlabs.example;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import ai.bitlabs.sdk.BitLabs;
import ai.bitlabs.sdk.offerwall.Offerwall;
import ai.bitlabs.sdk.data.model.bitlabs.Survey;
import ai.bitlabs.sdk.data.model.bitlabs.WidgetType;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Example";

    private final BitLabs bitLabs = BitLabs.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String UID = "oblivatevariegata";
        bitLabs.init(this, BuildConfig.APP_TOKEN, UID);
        BitLabs.API.init(BuildConfig.APP_TOKEN, UID);

        // bitLabs.setDebugMode(true);

        Offerwall offerwall = BitLabs.OFFERWALL.create(BuildConfig.APP_TOKEN, UID);

        // optionally add custom tags to your users
        Map<String, Object> tags = new HashMap<>();
        tags.put("my_tag", "new_user");
        offerwall.getTags().putAll(tags);

        offerwall.getTags().put("is_premium", true);

        // Get client-side callbacks to reward the user (We highly recommend using server-to-server callbacks!)
        offerwall.setOnSurveyRewardListener(surveyReward -> Log.i(TAG, "Survey Reward: " + surveyReward));

        offerwall.setOnOfferwallClosedListener(totalSurveyReward -> Log.i(TAG, "Offerwall closed. Total survey reward: " + totalSurveyReward));

        findViewById(R.id.btn_check_surveys).setOnClickListener(view -> BitLabs.API.checkSurveys(
                hasSurveys -> Log.i(TAG, hasSurveys ? "Found Surveys" : "No Surveys"),
                e -> Log.e(TAG, "CheckSurveysErr: " + e.getMessage(), e.getCause())
        ));

        findViewById(R.id.btn_get_surveys).setOnClickListener(view -> BitLabs.API.getSurveys(
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
                bitLabs.showSurvey(this, R.id.container_survey_widget, WidgetType.SIMPLE)
        );

        findViewById(R.id.btn_launch_offerwall).setOnClickListener(view -> offerwall.launch(this));

        findViewById(R.id.btn_show_leaderboard).setOnClickListener(view ->
                bitLabs.showLeaderboard(this, R.id.container_leaderboard)
        );
    }
}
