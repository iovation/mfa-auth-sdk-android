-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

#suggested for libraries by the proguard manual
# ref: https://www.guardsquare.com/en/proguard/manual/examples#library
-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod

-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** wtf(...);
}

-flattenpackagehierarchy com.launchkey.android.authenticator.sdk.ui.internal

-dontoptimize