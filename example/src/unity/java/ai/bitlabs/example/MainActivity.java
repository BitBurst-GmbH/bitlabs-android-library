package ai.bitlabs.example;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ai.bitlabs.sdk.BitLabs;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Example";

    BitLabs bitLabs = BitLabs.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bitLabs.init("YOUR-APP-TOKEN", "USER-ID");

        findViewById(R.id.open).setOnClickListener(view -> bitLabs.launchOfferWall(this));
    }
}
