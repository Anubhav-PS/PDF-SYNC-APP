package com.anubhavps.pdfsync.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.anubhavps.pdfsync.R;
import com.anubhavps.pdfsync.database.LocalDatabase;
import com.anubhavps.pdfsync.models.User;
import com.anubhavps.pdfsync.network.NetworkProcess;
import com.anubhavps.pdfsync.network.iFirebaseAuthSession;



public class SplashScreen extends AppCompatActivity implements iFirebaseAuthSession {

    private iFirebaseAuthSession firebaseAuthSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

    /*    FirebaseApp.initializeApp(SplashScreen.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());
*/
        firebaseAuthSession = this;

        NetworkProcess networkProcess = new NetworkProcess();
        networkProcess.isUserSignedIn(firebaseAuthSession);



    }


    @Override
    public void authVerifiedAndAlive(boolean proceed) {

        LocalDatabase localDatabase = new LocalDatabase(SplashScreen.this);

        final Intent intent;
        if (proceed) {
            User user = User.getInstance();
            user = localDatabase.getUser();
            intent = new Intent(SplashScreen.this, DashBoard.class);
        } else {
            localDatabase.deleteUser();
            intent = new Intent(SplashScreen.this, Login.class);
        }

        int SPLASH_SCREEN = 1200;
        new Handler().postDelayed(() -> {
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            finish();
        }, SPLASH_SCREEN);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthSession != null) {
            firebaseAuthSession = null;
        }
    }
}