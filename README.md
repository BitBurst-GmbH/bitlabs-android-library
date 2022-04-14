## BitLabs Android SDK v2

[![](https://jitpack.io/v/BitBurst-GmbH/bitlabs-android-library.svg)](https://jitpack.io/#BitBurst-GmbH/bitlabs-android-library)

Official Android SDK for BitBurst's BitLabs. We have an example app that can be used for
reference on how to integrate this SDK. Take a look at `/example` for more details.

Read the complete documentation here: https://bitlabs.ai/integrations/android-sdk

### Dependencies

Add the following above the __dependencies__ section of your app level build.gradle file:
``` Groovy
repositories {
    maven {
        url 'https://jitpack.io'
    }
}
```

Then add the dependency in the same file:
``` Groovy
dependencies {
    // other dependencies
    
    implementation 'com.github.BitBurst-GmbH.bitlabs-android-library:core:2.1.3'

    // other dependencies
}
```

### Usage

Refer to the full guide here: [Android SDK v2](https://developer.bitlabs.ai/docs/android-sdk-v2#initialising-the-sdk)
