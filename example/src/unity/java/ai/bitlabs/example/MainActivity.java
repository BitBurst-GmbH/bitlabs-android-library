package ai.bitlabs.example;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ai.bitlabs.sdk.BitLabs;

public class MainActivity extends AppCompatActivity {

    final BitLabs bitLabs = BitLabs.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bitLabs.init(this, "YOUR_APP_TOKEN", "USER_ID");

        findViewById(R.id.btn_launch_offerwall).setOnClickListener(view -> bitLabs.launchOfferWall(this));
    }
}
