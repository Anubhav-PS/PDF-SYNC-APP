package com.anubhavps.pdfsync.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anubhavps.pdfsync.R;
import com.anubhavps.pdfsync.models.PDF;
import com.anubhavps.pdfsync.models.User;
import com.anubhavps.pdfsync.network.NetworkProcess;
import com.anubhavps.pdfsync.network.iFirebaseQueryUserDetailResult;
import com.anubhavps.pdfsync.network.iFirebaseSharePdfResult;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;

import java.sql.Date;

public class SharePDF extends AppCompatActivity implements iFirebaseQueryUserDetailResult, View.OnClickListener, iFirebaseSharePdfResult {

    private ImageButton backArrowBtn;
    private MaterialButton grantAccessBtn;
    private ProgressBar searchProgressBar;
    private MaterialToolbar toolbar;
    private MaterialCardView searchResultCard;
    private TextInputEditText addMessageEt;
    private MaterialTextView searchNameTxt, searchMailIdTxt;
    private MaterialTextView shareWithNameTxt;

    private MaterialTextView progressStatus;

    private ProgressBar progressBar;

    private Dialog dialog;

    private String searchText;

    private String name, mailId, user_UID, username;

    private String shareName, shareMailId, shareUser_UID, shareUsername, message = "";

    private PDF pdf;

    private NetworkProcess networkProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_pdf);


        String documentID = getIntent().getStringExtra("DOCUMENT_ID");
        String name = getIntent().getStringExtra("NAME");
        String filename = getIntent().getStringExtra("FILENAME");
        long size = getIntent().getLongExtra("SIZE", 1);
        long uploaded_on = getIntent().getLongExtra("UPLOADED_ON", 1);
        String url = getIntent().getStringExtra("URL");

        pdf = new PDF(name, filename, size, url, uploaded_on, documentID);


        backArrowBtn = findViewById(R.id.sharePdfBackBtn);
        grantAccessBtn = findViewById(R.id.sharePdfAddUserBtn);
        searchProgressBar = findViewById(R.id.sharePdfSearchProgressBar);
        toolbar = findViewById(R.id.sharePdfAddUserSearchView);
        searchResultCard = findViewById(R.id.sharePdfSearchResultCard);
        addMessageEt = findViewById(R.id.sharePdfAddMessageEt);
        searchNameTxt = findViewById(R.id.sharePdfSearchResultNameTxt);
        searchMailIdTxt = findViewById(R.id.sharePdfSearchResultMailIdTxt);
        shareWithNameTxt = findViewById(R.id.sharePdfShareWithNameTxt);


        queryInit();

        MenuItem searchMenuItem = toolbar.getMenu().getItem(0);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setQueryHint("Search user by their username");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query.trim();
                if (!searchText.isEmpty()) processFindUser(searchText);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }


        });

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchProgressBar.setVisibility(View.INVISIBLE);
                searchResultCard.setVisibility(View.INVISIBLE);
                return true;
            }
        });

        networkProcess = new NetworkProcess();
        searchResultCard.setOnClickListener(this);
        backArrowBtn.setOnClickListener(this);
        grantAccessBtn.setOnClickListener(this);


    }

    private void processFindUser(String searchText) {
        if (searchText.equalsIgnoreCase(User.getInstance().getUsername())) {
            callToast("Action not possible");
            return;
        }
        searchProgressBar.setVisibility(View.VISIBLE);
        networkProcess.getUserDetails(searchText, this);
    }

    private void queryInit() {
        searchProgressBar.setVisibility(View.INVISIBLE);
        searchResultCard.setVisibility(View.INVISIBLE);
        shareWithNameTxt.setText("User Not Selected!");
        grantAccessBtn.setEnabled(false);
    }

    @Override
    public void onUserDetailSearchFound(String name, String mail_ID, String user_UID, String username) {
        this.name = name;
        this.mailId = mail_ID;
        this.user_UID = user_UID;
        this.username = username;
        searchProgressBar.setVisibility(View.INVISIBLE);
        searchResultCard.setVisibility(View.VISIBLE);
        searchNameTxt.setText(name);
        searchMailIdTxt.setText(mail_ID);
    }


    @Override
    public void onUserDoesNotExists() {
        searchProgressBar.setVisibility(View.INVISIBLE);
        callToast("Username does not exists");
    }

    @Override
    public void onSearchFailed(Exception e) {
        searchProgressBar.setVisibility(View.INVISIBLE);
        callToast(e.getLocalizedMessage());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sharePdfBackBtn) {
            onBackPressed();
        } else if (id == R.id.sharePdfAddUserBtn) {
            sharePdf();
        } else if (id == R.id.sharePdfSearchResultCard) {
            shareWithNameTxt.setText(name);
            grantAccessBtn.setEnabled(true);
            this.shareName = name;
            this.shareMailId = mailId;
            this.shareUsername = username;
            this.shareUser_UID = user_UID;

        }
    }

    private void sharePdf() {
        if (addMessageEt.getText() == null || addMessageEt.getText().toString().isEmpty()) {
            message = "";
        }

        networkProcess.sharePdfWithUser(shareName, shareUsername, shareUser_UID, shareMailId, message, pdf, this);

        dialog = new Dialog(SharePDF.this);
        dialog.setContentView(R.layout.dialog_upload_pdf_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        progressStatus = dialog.findViewById(R.id.dialogUploadFileStatusTxt);
        progressBar = dialog.findViewById(R.id.dialogUploadFileProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        String status = "Sharing....";
        progressStatus.setText(status);
        dialog.show();
    }

    private void callToast(String message) {
        Toast.makeText(SharePDF.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPdfSharedSuccessful(String message) {
        callToast(message);
        String status = "Shared with " + this.username;
        progressStatus.setText(status);
        progressBar.setVisibility(View.INVISIBLE);
        dialog.dismiss();
        this.shareName = null;
        this.shareMailId = null;
        this.shareUsername = null;
        this.shareUser_UID = null;
        queryInit();
    }

    @Override
    public void onPdfShareFailed(String message) {
        dialog.dismiss();
        callToast(message);
    }

    @Override
    public void onErrorReported(Exception e) {
        dialog.dismiss();
        callToast(e.getLocalizedMessage());
    }
}