package com.anubhavps.pdfsync.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.anubhavps.pdfsync.R;
import com.anubhavps.pdfsync.network.NetworkProcess;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseAccountSignUpResult;
import com.anubhavps.pdfsync.util.InputFormatter;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;

import java.util.Objects;

public class SignUp extends AppCompatActivity implements iFirebaseAccountSignUpResult, View.OnClickListener {

    private NetworkProcess networkProcess;
    private String userID;

    // input field views
    private TextInputEditText usernameEt, mailEt, passwordEt;
    private ProgressBar progressBar;
    // input field string values

    private String inputUsername;
    private String inputMail;
    private String inputPassword;

    private String username;

    private boolean validUsername = false;
    private boolean validMail = false;
    private boolean validPassword = false;

    private iFirebaseAccountSignUpResult firebaseAccountSignUpResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameEt = findViewById(R.id.signUpUsernameEt);
        mailEt = findViewById(R.id.signUpEmailEt);
        passwordEt = findViewById(R.id.signUpPasswordEt);
        progressBar = findViewById(R.id.signUpProgressBar);

        MaterialButton createAccount = findViewById(R.id.signUpBtn);
        MaterialTextView toLogin = findViewById(R.id.signUpToLoginPageTxt);


        // name change listeners
        usernameEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (Objects.requireNonNull(usernameEt.getText()).toString().trim().isEmpty()) {
                    usernameEt.setError("Name is required");
                }
            }
        });

        usernameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                usernameEt.setError(null);
                inputUsername = Objects.requireNonNull(usernameEt.getText()).toString();
                validUsername = InputFormatter.isValidUsername(inputUsername);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // mail id change listeners
        mailEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (Objects.requireNonNull(mailEt.getText()).toString().trim().isEmpty()) {
                    mailEt.setError("email id is required !");
                }
            }
        });

        mailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mailEt.setError(null);
                inputMail = Objects.requireNonNull(mailEt.getText()).toString();
                if (!InputFormatter.isValidMailId(inputMail)) {
                    mailEt.setError("invalid email ID");
                    validMail = false;
                } else {
                    validMail = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // password change listeners
        passwordEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (Objects.requireNonNull(passwordEt.getText()).toString().trim().isEmpty()) {
                    passwordEt.setError("Minimum 10 characters");
                }
            }
        });

        passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordEt.setError(null);
                inputPassword = Objects.requireNonNull(passwordEt.getText()).toString().trim();
                if (inputPassword.isEmpty()) {
                    passwordEt.setError("Password is required !");
                    validPassword = false;
                } else validPassword = inputPassword.length() >= 10;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        firebaseAccountSignUpResult = this;
        networkProcess = new NetworkProcess();

        // button click listeners
        createAccount.setOnClickListener(this);
        toLogin.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.signUpBtn) {
            validation();
        } else if (id == R.id.signUpToLoginPageTxt) {
            openLoginPage();
        }
    }

    @Override
    public void onAccountCreated(Task<AuthResult> task) {
        userID = Objects.requireNonNull(task.getResult().getUser()).getUid();
        networkProcess.sendVerificationMail(firebaseAccountSignUpResult);
    }

    @Override
    public void onVerificationMailSent() {
        networkProcess.createUsernameRecord(username, userID, firebaseAccountSignUpResult);

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(SignUp.this);
        materialAlertDialogBuilder.setTitle("Verification Mail Sent");
        materialAlertDialogBuilder.setMessage("Verify the link sent to your mail");
        materialAlertDialogBuilder.setPositiveButton("Ok", (dialog, which) -> {
            //send verification mail
        });
        materialAlertDialogBuilder.setCancelable(false);
        materialAlertDialogBuilder.show();
    }

    @Override
    public void onUsernameAvailable() {
        networkProcess.createAccount(inputMail, inputPassword, firebaseAccountSignUpResult);
    }

    @Override
    public void onUsernameTaken() {
        progressBar.setVisibility(View.INVISIBLE);
        callSnackBar("Username already exists!");
    }

    @Override
    public void onSuccess(Task<Void> task) {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSuccess(String message) {
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void openLoginPage() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        finish();
    }

    private void validation() {

        if (inputUsername == null || inputMail == null || inputPassword == null) {
            callSnackBar("Fill all the fields to proceed !");
            return;
        }

        if (!validUsername) {
            usernameEt.setError("Username must have minimum 8 characters.Only lowercase letters.No special characters allowed except '_' ");
            return;
        }

        if (!validMail) {
            mailEt.setError("email id is required");
            return;
        }

        if (!validPassword) {
            passwordEt.setError("Minimum 10 characters");
            return;
        }

        username = inputUsername;
        progressBar.setVisibility(View.VISIBLE);
        networkProcess.isUserNameAvailable(username, firebaseAccountSignUpResult);
    }

    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(this, findViewById(R.id.signUpPage), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.olive_200));
        snackbar.show();
    }

    @Override
    public void onErrorReported(Exception e) {
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.firebaseAccountSignUpResult != null) {
            this.firebaseAccountSignUpResult = null;
        }
    }
}