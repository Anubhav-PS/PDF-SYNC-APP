package com.anubhavps.pdfsync.activities;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.anubhavps.pdfsync.R;
import com.anubhavps.pdfsync.database.LocalDatabase;
import com.anubhavps.pdfsync.fragments.HomeFragment;
import com.anubhavps.pdfsync.fragments.PasswordFragment;
import com.anubhavps.pdfsync.fragments.RestrictedFragment;
import com.anubhavps.pdfsync.fragments.SharedWithMeFragment;
import com.anubhavps.pdfsync.fragments.StarredFragment;
import com.anubhavps.pdfsync.interfaces.iOnOpenActivity;
import com.anubhavps.pdfsync.models.PDF;
import com.anubhavps.pdfsync.models.User;
import com.anubhavps.pdfsync.network.NetworkProcess;
import com.anubhavps.pdfsync.network.iFirebaseAccountSignUpResult;
import com.anubhavps.pdfsync.network.iFirebaseDataUploadResult;
import com.anubhavps.pdfsync.network.iFirebaseStorageResult;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class DashBoard extends AppCompatActivity implements iOnOpenActivity, iFirebaseStorageResult, iFirebaseDataUploadResult, View.OnClickListener {


    private MaterialCardView userAvatar;
    private NetworkProcess networkProcess;

    private LocalDatabase localDatabase;

    private iFirebaseStorageResult firebaseStorageResult;
    private iFirebaseDataUploadResult firebaseDataUploadResult;

    private MaterialTextView progressStatus;

    private ProgressBar progressBar;

    private Dialog dialog;

    private String status;

    private String fileName;
    private Uri uri;

    private BottomNavigationView bottomNavigationView;

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Intent data = result.getData();

        if (data != null) {
            Uri uri = data.getData();
            this.uploadFile(uri);
        }

        if (result.getResultCode() == 501) { //call from home fragment to comment activity
            bottomNavigationView.setSelectedItemId(R.id.nav_menu_home);
        } else if (result.getResultCode() == 502) { //call from home fragment to share pdf activity
            bottomNavigationView.setSelectedItemId(R.id.nav_menu_home);
        } else if (result.getResultCode() == 503){  //call from recycle bin to dashboard activity
            bottomNavigationView.setSelectedItemId(R.id.nav_menu_home);
        }

    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        if (savedInstanceState == null) {
            HomeFragment homeFragment = new HomeFragment();
            makeTransaction(homeFragment);
        }

        firebaseStorageResult = this;
        firebaseDataUploadResult = this;

        networkProcess = new NetworkProcess();
        localDatabase = new LocalDatabase(this);

        userAvatar = findViewById(R.id.dashboardAvatarCardView);
        bottomNavigationView = findViewById(R.id.dashBoardBottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_menu_home);

        bottomNavigationViewSetup();

        userAvatar.setOnClickListener(this);


    }


    // bottom navigation setup process
    private void bottomNavigationViewSetup() {

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = new HomeFragment();
            if (id == R.id.nav_menu_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_menu_starred) {
                fragment = new StarredFragment();
            } else if (id == R.id.nav_menu_restricted) {
                fragment = new RestrictedFragment();
            } else if (id == R.id.nav_menu_shared_with_me) {
                fragment = new SharedWithMeFragment();
            }
            makeTransaction(fragment);
            return true;
        });

        bottomNavigationView.setOnItemReselectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = new HomeFragment();
            if (id == R.id.nav_menu_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_menu_starred) {
                fragment = new StarredFragment();
            } else if (id == R.id.nav_menu_restricted) {
                fragment = new RestrictedFragment();
            } else if (id == R.id.nav_menu_shared_with_me) {
                fragment = new SharedWithMeFragment();
            }
            makeTransaction(fragment);
        });

    }

    private void openManageAccountDialog() {

        dialog = new Dialog(DashBoard.this);
        dialog.setContentView(R.layout.dialog_manage_account);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
        dialog.setCanceledOnTouchOutside(true);

        MaterialTextView studentNameTxt, studentMailIdTxt, editProfileTxt, recycleBinTxt, shareAppTxt, aboutTheAppTxt, sendFeedbackTxt, versionTxt;
        MaterialButton signOutTxt = dialog.findViewById(R.id.dialogAccountSignOutBtn);
        ImageView closeBtn = dialog.findViewById(R.id.dialogAccountCloseBtn);

        studentNameTxt = dialog.findViewById(R.id.dialogAccountUsernameTxt);
        studentMailIdTxt = dialog.findViewById(R.id.dialogAccountUserMailIdTxt);

        editProfileTxt = dialog.findViewById(R.id.dialogAccountEditProfileTxt);
        recycleBinTxt = dialog.findViewById(R.id.dialogAccountRecycleBinTxt);

        shareAppTxt = dialog.findViewById(R.id.dialogAccountShareAppTxt);
        aboutTheAppTxt = dialog.findViewById(R.id.dialogAccountAboutAppTxt);
        sendFeedbackTxt = dialog.findViewById(R.id.dialogAccountSendFeedbackTxt);
        versionTxt = dialog.findViewById(R.id.dialogAccountAppVersionTxt);


        studentNameTxt.setText(User.getInstance().getUsername());
        studentMailIdTxt.setText(User.getInstance().getMailId());

        String versionName = "version " + getVersionCode();
        versionTxt.setText(versionName);

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        signOutTxt.setOnClickListener(this);

        editProfileTxt.setOnClickListener(this);
        recycleBinTxt.setOnClickListener(this);
        shareAppTxt.setOnClickListener(this);
        aboutTheAppTxt.setOnClickListener(this);
        sendFeedbackTxt.setOnClickListener(this);

        dialog.show();
    }


    public void makeTransaction(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.dashBoardFragmentContainer, fragment);
        fragmentTransaction.commit();
    }

    public String getVersionCode() {
        String v = "";
        try {
            v = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            return v;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(DashBoard.this, "Failed to fetch Version Detail", Toast.LENGTH_SHORT).show();
        }
        return v;
    }

    private String extractFileName(ContentResolver resolver, Uri uri) {
        Cursor cursor = resolver.query(uri, null, null, null, null);
        assert cursor != null;
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        String name = cursor.getString(nameIndex);
        cursor.close();
        return name;
    }


    @Override
    public void onOpenGalleryIntent(Intent intent) {
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onOpenCommentActivity(Intent intent) {
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onOpenSharePdfActivity(Intent intent) {
        activityResultLauncher.launch(intent);
    }

    public void openDialog() {
        dialog = new Dialog(DashBoard.this);
        dialog.setContentView(R.layout.dialog_upload_pdf_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        progressStatus = dialog.findViewById(R.id.dialogUploadFileStatusTxt);
        progressBar = dialog.findViewById(R.id.dialogUploadFileProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        String status = "Uploading....";
        progressStatus.setText(status);

        dialog.show();
    }

    private void uploadFile(Uri data) {
        String file = extractFileName(getContentResolver(), data);
        this.fileName = file;
        this.uri = data;
        networkProcess.pdfExists(file, firebaseDataUploadResult);
    }

    @Override
    public void onPdfUploadFailed(Exception e) {
        callSnackBar(e.getLocalizedMessage());
    }

    @Override
    public void onPdfDownloadUrlFailed(Exception e) {
        callSnackBar(e.getLocalizedMessage());
    }

    @Override
    public void uploadingPdfProgress(double progress) {
        status = "Uploading... " + (int) progress + " %";
        progressStatus.setText(status);
    }

    @Override
    public void onPdfSuccessfullyUploadedToStorage(PDF pdf) {
        networkProcess.uploadPdfToDatabase(pdf, firebaseDataUploadResult);
    }

    @Override
    public void onDataUploaded() {
        status = "Uploaded... 100 %";
        progressStatus.setText(status);
        dialog.dismiss();
        callSnackBar("Uploaded SuccessFully");
    }

    @Override
    public void onDataUploadFailed(Exception e) {
        callSnackBar(e.getLocalizedMessage());
    }

    @Override
    public void onPdfExists(boolean exists) {
        if (exists) {
            //file exists
            callSnackBar("File With Same Name Already Exists!");
        } else {
            openDialog();
            networkProcess.uploadPdfToStorage(fileName, uri, firebaseStorageResult);
        }
    }

    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(DashBoard.this, findViewById(R.id.dashBoardPage), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(DashBoard.this, R.color.olive_200));
        snackbar.show();
    }

    private void processSignOut() {
        networkProcess.logoutUser();
        localDatabase.deleteUser();
        Toast.makeText(DashBoard.this, "Logging out, see you soon !", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(DashBoard.this, Login.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        finish();
    }

    private void openShareDialog() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "PDF Scan");
        String shareMessage = "\nHey dude, check out this app for pdf management\n\n";
        shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + this.getPackageName() + "\n";
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(intent, "Select One"));
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dashboardAvatarCardView) {
            openManageAccountDialog();
        } else if (id == R.id.dialogAccountSignOutBtn) {
            if (dialog != null) dialog.dismiss();
            processSignOut();
        } else if (id == R.id.dialogAccountShareAppTxt) {
            openShareDialog();
        } else if (id == R.id.dialogAccountRecycleBinTxt) {
            openRecycleBin();
        }
    }

    private void openRecycleBin() {

        Intent intent = new Intent(DashBoard.this, RecycleBin.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

    }
}