# TruValidate Multifactor Authentication SDK for Android

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/lacaprjc/Multifactor-Authentication-Mobile-Authenticator-UI-SDK?label=latest%20release)

  * [Introduction](introduction)
  * [Running the Latest Release](#latestrelease)
  * [Running as a Submodule](#submodule)
  * [Configuration](#configuration)
  * [Project Notes](#projectnotes)
  * [Links](#links)

# <a name="introduction"></a>Introduction

This is the open-source repository of the TruValidate Multifactor Authentication Mobile Authenticator **UI** SDK for Android. This module serves as a UI wrapper for the
TruValidate Multifactor Authentication Mobile Authenticator **Core** SDK for Android. Any and all configuration of the **Core** SDK must be done through the **Core** SDK. The
primary purpose of the **UI** SDK is to provide a bare bones UI implementation of the Core SDK. Because the Core SDK requires, at a minimum, that implementors
set an SDK key allowing users to link their device, this means the UI SDK will provide little use without properly configuring the Core SDK first.

Developer documentation for using the Core and UI Authenticator SDKs is found [here](https://docs.launchkey.com/authenticator-sdk/ui/integrate-authenticator-sdk.html).

# <a name="latestrelease"></a>Running the Latest Release

To run the latest release of the TruValidate Multifactor Authentication Mobile Authenticator UI SDK add the following repository and dependency to your
project-level `build.gradle` and your app-level `build.gradle` respectively:
```gradle
// project-level build.gradle
repositories {
   maven {
       url "https://github.com/iovation/launchkey-android-authenticator-sdk/raw/master/lk-auth-sdk"
   }
}

// app-level build.gradle
dependencies {
    // (UI) Auth SDK
    implementation 'com.launchkey.android.authenticator.sdk:lk-auth-sdk:<version>'
}
```

# <a name="submodule"></a>Running as a Submodule

To import this module into your Android project and run it as a submodule add the following lines to your
project-level `settings.gradle` and your app-level `build.gradle`:
```gradle
// project-level settings.gradle
include ':lk-auth-sdk-ui', ':app'
project(':lk-auth-sdk-ui').projectDir = file('<local_path_to_repo>/app')

// app-level build.gradle
dependencies {
    // (UI) Auth SDK
    implementation project(':lk-auth-sdk-ui')
}
```

# <a name="configuration"></a>Configuration

If the UI SDK is not configured it will assume a default configuration. We highly recommend implementors configure the SDK. This is most
easily done in the `Application::onCreate()` method of your application:
```xml
<!-- AndroidManifest.xml -->
<application
    android:name=".MyApplication"
    ...
    >
    ...
</application>
```
```java
// MyApplication.java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AuthenticatorUIConfig.Builder uiConfigBuilder = new AuthenticatorUIConfig.Builder()
                // Determine whether AuthMethod changes should be allowed from the SecurityFragment prior to the user's device being linked
                .allowSecurityChangesWhenUnlinked(booleanValue)
                // Set theme to be used in UI SDK via theme id
                .theme(R.style.DemoAppTheme)
                // Or set theme to be used by programmatic configuration
                .theme(new AuthenticatorTheme.Builder(this).build());
        AuthenticatorUIManager uiManager = AuthenticatorUIManager.getInstance();
        uiManager.initialize(uiConfigBuilder.build(this));
    }
}
```
```kotlin
// MyApplication.kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val uiConfigBuilder: AuthenticatorUIConfig.Builder = AuthenticatorUIConfig.Builder()
                // Determine whether AuthMethod changes should be allowed from the SecurityFragment prior to the user's device being linked
                .allowSecurityChangesWhenUnlinked(booleanValue)
                // Set theme to be used in UI SDK via theme id
                .theme(R.style.DemoAppTheme)
                // Or set theme to be used by programmatic configuration
                .theme(AuthenticatorTheme.Builder(this).build())
        val uiManager = AuthenticatorUIManager.instance
        uiManager.initialize(uiConfigBuilder.build(this))
    }
}
```

#  <a name="projectnotes"></a>Project Notes
* You may notice that our team references the Core SDK in this project as follows:
  ```groovy
  dependencies {
      // ...
      implementation findProject(':lk-auth-sdk-core') ?: 'com.launchkey.android.authenticator.sdk:lk-auth-sdk-core:<version>'
  }
  ```
  This is because our team develops the open source UI SDK with the closed source Core SDK submodule which is not available publicly.
  For our own convenience, we use the Maven artifact as a fallback for implementors allowing ourselves and implementors to freely
  develop on this shared project.
* You may notice that this project has an `internal` package with many classes in it. If you are building from source, you may use
  (and even contribute) to any of the classes in this package, but keep in mind that they are subject to change. If you are not
  building from source or lack the time to deal with frequent changes to these classes, **do not use them because your build will break
  from version to version.** As of right now, all classes located outside of the `internal` package are not subject to change and have
  updates corresponding to the semantic versioning of the UI SDK.

#  <a name="links"></a>Links

  Check our [documentation](https://docs.launchkey.com/authenticator-sdk/before-you-begin.html) for setting up
  a Multifactor Authentication Service and the official [Android Development website](https://d.android.com)
  for everything else regarding Android.

## FAQ's

Browse FAQ's or submit a question to the TruValidate Multifactor Authentication support team for both
technical and non-technical issues. Visit the TruValidate Multifactor Authentication Support website [here](https://www.iovation.com/contact).
