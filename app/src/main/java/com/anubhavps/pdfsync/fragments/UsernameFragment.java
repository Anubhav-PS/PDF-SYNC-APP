package com.anubhavps.pdfsync.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.anubhavps.pdfsync.R;
import com.anubhavps.pdfsync.activities.SignUp;
import com.anubhavps.pdfsync.network.NetworkProcess;
import com.anubhavps.pdfsync.network.iFirebaseAccountSignInResult;
import com.anubhavps.pdfsync.util.InputFormatter;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;


public class UsernameFragment extends Fragment implements View.OnClickListener, iFirebaseAccountSignInResult {


    private TextInputEditText usernameEt;
    private ProgressBar progressBar;

    private MaterialTextView errorTxt;

    private String inputUsername, username;

    private NetworkProcess networkProcess;

    private View rootView;
    private iFirebaseAccountSignInResult firebaseAccountSignInResult;


    public UsernameFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_username, container, false);

        MaterialTextView createAccountTxt = rootView.findViewById(R.id.usernameToSignUpTxt);

        usernameEt = rootView.findViewById(R.id.usernameEt);
        progressBar = rootView.findViewById(R.id.usernameProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        ImageButton proceedBtn = rootView.findViewById(R.id.usernameProceedBtn);
        errorTxt = rootView.findViewById(R.id.usernameErrorTxt);

        usernameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                usernameEt.setError(null);
                errorTxt.setText(null);
                inputUsername = Objects.requireNonNull(usernameEt.getText()).toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        firebaseAccountSignInResult = this;
        networkProcess = new NetworkProcess();

        createAccountTxt.setOnClickListener(this);
        proceedBtn.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.usernameToSignUpTxt) {
            openSignUpPage();
        } else if (id == R.id.usernameProceedBtn) {
            progressBar.setVisibility(View.VISIBLE);
            username = inputUsername;
            goToPasswordFragment();
        }
    }

    private void openSignUpPage() {
        Intent intent = new Intent(getContext(), SignUp.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        requireActivity().finish();
    }

    private void goToPasswordFragment() {

        if (username == null) {
            errorTxt.setText("username is required!");
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (username.isEmpty()) {
            errorTxt.setText("username cannot be empty");
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (!InputFormatter.isValidUsername(username)) {
            errorTxt.setText("Username must be 8 to 20 characters wide.No special characters allowed except '_'");
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        networkProcess.getUserMailId(username, firebaseAccountSignInResult);

    }

    private void proceedToPassword(String result, Fragment passwordFragment) {
        // Create a Bundle and put the data you want to pass
        Bundle bundle = new Bundle();
        bundle.putString("MAIL_ID", result);
        passwordFragment.setArguments(bundle);

        // Get the fragment manager and start a new transaction
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right);


        // Replace the current fragment with the new fragment
        fragmentTransaction.replace(R.id.loginFrameLayout, passwordFragment);

        // Add the transaction to the back stack
        fragmentTransaction.addToBackStack(null);

        progressBar.setVisibility(View.INVISIBLE);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    @Override
    public void onEmailVerified(String user_UID) {

    }

    @Override
    public void onEmailNotVerified() {

    }

    @Override
    public void onResetPasswordSent() {

    }

    @Override
    public void onUserMailIdFetched(String userMailId) {
        PasswordFragment passwordFragment = new PasswordFragment();
        proceedToPassword(userMailId, passwordFragment);
    }

    @Override
    public void onUsernameNotRegistered() {
        progressBar.setVisibility(View.INVISIBLE);
        callSnackBar("Account with provided username does not exists");
    }

    @Override
    public void onBadResponse() {
        progressBar.setVisibility(View.INVISIBLE);
        callSnackBar("There was some error.Please try again later");
    }

    @Override
    public void onErrorReported(Exception e) {
        callSnackBar(e.getLocalizedMessage());
    }

    @Override
    public void onSuccess(Task<Void> task) {

    }

    @Override
    public void onSuccess(String message) {

    }

    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(requireContext(), rootView.findViewById(R.id.usernamePage), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.olive_200));
        snackbar.show();
    }

}