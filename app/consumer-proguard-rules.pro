#ioa entry point (and main event callbacks)
-keep public class com.launchkey.android.authenticator.sdk.AuthenticatorManager { public protected *; }
-keep public class com.launchkey.android.authenticator.sdk.AuthenticatorConfig { public protected *; }
# added for AuthenticatorConfig.Builder inner class
-keep public class com.launchkey.android.authenticator.sdk.AuthenticatorConfig$* { public protected *; }
-keep public class com.launchkey.android.authenticator.sdk.DeviceLinkedEventCallback { public protected *; }
-keep public class com.launchkey.android.authenticator.sdk.DeviceUnlinkedEventCallback { public protected *; }
-keep public class com.launchkey.android.authenticator.sdk.DeviceKeyPairGeneratedEventCallback { public protected *; }
-keep public class com.launchkey.android.authenticator.sdk.SimpleOperationCallback { *; }

#ioa ui
-keep public class com.launchkey.android.authenticator.sdk.ui.** { public protected *; }

# OSM and Google Maps
-keepnames class org.osmdroid.**
-keepnames class com.google.android.gms.maps.**

#zbar
-keep class net.sourceforge.** {*;}

#qr scanner
-keep public class com.abhi.barcode.** {*;}
-keep public class com.google.zxing.** {*;}