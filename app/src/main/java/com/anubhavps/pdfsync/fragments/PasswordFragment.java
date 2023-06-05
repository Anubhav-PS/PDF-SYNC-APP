package com.anubhavps.pdfsync.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.anubhavps.pdfsync.R;
import com.anubhavps.pdfsync.activities.DashBoard;
import com.anubhavps.pdfsync.database.LocalDatabase;
import com.anubhavps.pdfsync.interfaces.database.iLocalDatabaseResult;
import com.anubhavps.pdfsync.models.User;
import com.anubhavps.pdfsync.network.NetworkProcess;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseAccountSignInResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseAccountSignUpResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseDocumentDownloadResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseFCMTokenResult;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;


public class PasswordFragment extends Fragment implements View.OnClickListener, iFirebaseAccountSignInResult, iFirebaseDocumentDownloadResult, iFirebaseAccountSignUpResult, iLocalDatabaseResult, iFirebaseFCMTokenResult {

    View rootView;
    private TextInputEditText passwordEt;
    private ProgressBar progressBar;
    private MaterialTextView errorTxt, resetTxt;
    private MaterialButton loginBtn;

    private String mailID;
    private String password;

    private NetworkProcess networkProcess;


    private iFirebaseAccountSignInResult firebaseAccountSignInResult;
    private iFirebaseDocumentDownloadResult firebaseDocumentDownloadResult;
    private iFirebaseAccountSignUpResult firebaseAccountSignUpResult;

    private iFirebaseFCMTokenResult firebaseFCMTokenResult;

    private iLocalDatabaseResult localDatabaseResult;

    public PasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_password, container, false);

        // Retrieve the data passed from the previous fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            mailID = bundle.getString("MAIL_ID");
        }


        passwordEt = rootView.findViewById(R.id.passwordEt);
        errorTxt = rootView.findViewById(R.id.passwordErrorTxt);
        resetTxt = rootView.findViewById(R.id.passwordResetTxt);
        loginBtn = rootView.findViewById(R.id.passwordLoginBtn);
        progressBar = rootView.findViewById(R.id.passwordProgressBar);

        passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                errorTxt.setText("");
                password = Objects.requireNonNull(passwordEt.getText()).toString().trim();
                if (password.isEmpty()) {
                    errorTxt.setText("Password is required !");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        networkProcess = new NetworkProcess();

        firebaseAccountSignInResult = this;
        firebaseDocumentDownloadResult = this;
        firebaseAccountSignUpResult = this;
        firebaseFCMTokenResult = this;
        localDatabaseResult = this;

        loginBtn.setOnClickListener(this);
        resetTxt.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.passwordLoginBtn) {
            loginUser();
        } else if (id == R.id.passwordResetTxt) {
            processForgotPassword();
        }
    }

    private void loginUser() {

        if (password == null) {
            errorTxt.setText("Password is required");
            return;
        }

        if (password.length() < 10) {
            errorTxt.setText("Minimum 10 Character");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        networkProcess.loginUser(mailID, password, firebaseAccountSignInResult);

    }


    // display on unverified account login
    private void promptVerifyMail() {
        progressBar.setVisibility(View.INVISIBLE);
        //Display an alert dialog for  not verifying the mail ID
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireContext());
        materialAlertDialogBuilder.setTitle("User Not Verified");
        materialAlertDialogBuilder.setMessage("Verify the link sent to your mail during registration");
        materialAlertDialogBuilder.setPositiveButton("Resend Verification Link", (dialog, which) -> {
            //send verification mail
            networkProcess.sendVerificationMail(firebaseAccountSignUpResult);
        });
        materialAlertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> {

        });
        materialAlertDialogBuilder.setCancelable(false);
        materialAlertDialogBuilder.show();
    }

    private void processForgotPassword() {
        progressBar.setVisibility(View.VISIBLE);
        networkProcess.resetPassword(mailID, firebaseAccountSignInResult);
    }


    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(requireContext(), rootView.findViewById(R.id.passwordPage), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.olive_200));
        snackbar.show();
    }

    @Override
    public void onEmailVerified(String user_UID) {
        networkProcess.downloadUserRecord(user_UID, firebaseDocumentDownloadResult);
    }

    @Override
    public void onEmailNotVerified() {
        progressBar.setVisibility(View.GONE);
        promptVerifyMail();
    }

    @Override
    public void onResetPasswordSent() {
        progressBar.setVisibility(View.INVISIBLE);
        String message = "Link to reset password has been sent to your mail ID";
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Password Reset Link Sent")
                .setMessage(message).show();
    }

    @Override
    public void onUserMailIdFetched(String userMailId) {

    }

    @Override
    public void onUsernameNotRegistered() {

    }

    @Override
    public void onErrorReported(Exception e) {
        progressBar.setVisibility(View.GONE);
        errorTxt.setText(e.getLocalizedMessage());
    }

    @Override
    public void onSuccess(Task<Void> task) {

    }

    @Override
    public void onSuccess(String message) {

    }

    @Override
    public void onDocumentDownloaded(Task<DocumentSnapshot> task) {
        User user = User.getInstance();

        DocumentSnapshot documentSnapshot = task.getResult();

        final String user_UID = Objects.requireNonNull(documentSnapshot.get("user_UID")).toString();
        final String username = Objects.requireNonNull(documentSnapshot.get("username")).toString();
        final String name = Objects.requireNonNull(documentSnapshot.get("name")).toString();
        final String mailId = Objects.requireNonNull(documentSnapshot.get("mailId")).toString();
        final String contactNumber = Objects.requireNonNull(documentSnapshot.get("contactNumber")).toString();
        final String fcmToken = Objects.requireNonNull(documentSnapshot.get("fcmToken")).toString();
        final long avatar = (long) Objects.requireNonNull(documentSnapshot.get("avatar"));

        user.setUser_UID(user_UID);
        user.setUsername(username);
        user.setName(name);
        user.setMailId(mailId);
        user.setContactNumber(contactNumber);
        user.setFcmToken(fcmToken);
        user.setAvatar(avatar);
        progressBar.setVisibility(View.INVISIBLE);

        networkProcess.generateFCMToken(firebaseFCMTokenResult);
    }

    @Override
    public void onDocumentDownloadFailed(Exception e) {
        progressBar.setVisibility(View.INVISIBLE);
        callSnackBar(e.getLocalizedMessage());
    }

    @Override
    public void onDocumentNotExist() {
        progressBar.setVisibility(View.GONE);
        errorTxt.setText("User Record Not Found!");
    }

    @Override
    public void onAccountCreated(Task<AuthResult> task) {

    }

    @Override
    public void onVerificationMailSent() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Verification Link has been sent")
                .setMessage("Please check your mail for verification, and then login again")
                .setPositiveButton("ok", null)
                .setCancelable(false)
                .show();
    }

    @Override
    public void onUsernameAvailable() {

    }

    @Override
    public void onUsernameTaken() {

    }

    @Override
    public void onBadResponse() {

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void userAddedToDB() {
        //upload FCM token
        networkProcess.uploadFCMToken(User.getInstance().getFcmToken(), User.getInstance().getUser_UID(), firebaseFCMTokenResult);
    }

    @Override
    public void userUpdatedInDB() {

    }

    @Override
    public void fcmTokenGenerated(Task<String> task) {
        String fcmToken = task.getResult();
        User.getInstance().setFcmToken(fcmToken);
        LocalDatabase localDatabase = new LocalDatabase(getContext(),localDatabaseResult);
        localDatabase.addUser(User.getInstance());
    }

    @Override
    public void fcmTokenUploaded() {
        Intent intent = new Intent(getContext(), DashBoard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        requireActivity().finish();
    }

    @Override
    public void fcmTokenFailed(Exception e) {
        progressBar.setVisibility(View.INVISIBLE);
        String message = e.getLocalizedMessage() + ".\n Try logging in again after sometime.";
        callSnackBar(message);
        networkProcess.logoutUser();
    }
}