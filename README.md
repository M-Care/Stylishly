# Stylishly
Stylishly is an android photosharing social networking app for Fashionistas. The basic concept is to allow anyone anywhere in the world to share their style among a community of fashion enthusiasts and get their outfits rated.
There are 8 different versions of Stylishly published on the google playstore since April 2017. This version however is the 4th version (Version 4.0) of the Stylishly app. 
Some of the features of this app include : Social login (using Facebook &amp; Twitter), photosharing, bookmarking (to store images locally for future use), image appreciation system (like/love images), comments, sharing /forwarding contents to various other social media platforms, Follower/Following, Search based on posts, users &amp; hashtags, friend suggestion system, instant messaging (text &amp; image sharing supported in chat), etc. 
Get started building your dream social networking app with Stylishly 4.0.


# How to set it up
After registering a Firebase account, replace the google-services.json file inside your /Stylishly/app with the new google-services.json file downloaded from your firebase account.

Also input all values as required in the /Stylishly/app/src/main/res/values/strings.xml file. They include <b>facebook_app_id, fb_login_protocol_scheme, back_4_app_application_id and back_4_app_client_key .</b>

In your StylishlyApplication.java file, go to the method private void initInstallation(){...} check for the line <b>parseInstallation.put("GCMSenderId", "INPUT_YOUR_GCM_SENDER_ID");</b> and replace INPUT_YOUR_GCM_SENDER_ID with your GCM/FCM SENDERID.

In your AndroidManifest.xml file, goto <b>android:name="com.parse.push.gcm_sender_id" android:value="id:INPUT_YOUR_GCM_SENDER_ID"</b> and change INPUT_YOUR_GCM_SENDER_ID to your GCM/FCM SENDERID.

If experiencing any difficulties setting up this source code, do endeavour to reach out for assistance : aduraline19065@gmail.com

# Contributing to the opensource community
We encourage contributions to the opensource community. You're welcome to assist us in making this code better.
            
            
# Libraries & Dependencies used
    //ButterKnife

    implementation 'com.jakewharton:butterknife:8.5.1'
    
    annotationProcessor 'com.jakewharton:butterknife-compiler:8.5.1'

    //Utilities
    
    implementation 'de.hdodenhof:circleimageview:2.1.0'
    
    implementation 'uk.co.chrisjenx:calligraphy:2.3.0'
    
    implementation 'net.steamcrafted:materialiconlib:1.1.4'
    
    implementation 'com.pnikosis:materialish-progress:1.7'
    
    implementation 'com.github.bumptech.glide:glide:4.0.0'
    
    implementation 'com.miguelcatalan:materialsearchview:1.4.0'
    
    implementation 'com.github.vivchar:ViewPagerIndicator:1.1.0'
    
    implementation 'com.github.esafirm.android-image-picker:imagepicker:1.12.0'
    
    implementation 'com.rengwuxian.materialedittext:library:2.1.4'
    
    implementation 'com.zxy.android:tiny:0.1.0'
    
    implementation 'io.fotoapparat.fotoapparat:library:2.2.0'
    
    implementation 'com.hendraanggrian:socialview-core:0.17'
    
    implementation 'com.hendraanggrian:socialview-commons:0.17'
    
    implementation 'me.kaelaela:verticalviewpager:1.0.0@aar'

    //RxJava
    implementation 'io.reactivex.rxjava2:rxandroid:2.0.1'
    implementation 'io.reactivex.rxjava2:rxjava:2.1.7'

    //Room
    
    implementation 'android.arch.persistence.room:runtime:1.0.0'
    
    annotationProcessor 'android.arch.persistence.room:compiler:1.0.0'

    //Gson
    
    implementation 'com.google.code.gson:gson:2.8.2'

    //TimeAgo
    
    implementation 'com.github.marlonlom:timeago:3.0.1'

    //Parceller
    
    implementation 'org.parceler:parceler-api:1.1.9'
    
    annotationProcessor 'org.parceler:parceler:1.1.9'

    //parse
    
    implementation 'com.parse:parse-android:1.13.0'
    
    implementation 'com.github.tgio:parse-livequery:1.0.3'
    
    implementation 'com.parse:parsetwitterutils-android:1.10.6'
    
    implementation 'com.facebook.android:facebook-android-sdk:4.9.0'
    
    implementation 'com.parse:parsefacebookutils-v4-android:1.10.3@aar'

    //BottomNav
    
    implementation 'com.aurelhubert:ahbottomnavigation:2.1.0'

    //FCM

    implementation 'com.google.firebase:firebase-core:10.2.1'
    
    implementation 'com.google.firebase:firebase-messaging:10.2.1'
    
    implementation 'com.google.firebase:firebase-crash:10.2.1'
    


