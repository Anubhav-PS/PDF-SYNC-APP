package com.anubhavps.pdfsync.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.anubhavps.pdfsync.R;

import com.anubhavps.pdfsync.activities.Comments;
import com.anubhavps.pdfsync.activities.SharePDF;
import com.anubhavps.pdfsync.adapters.ViewMyPDFAdapter;
import com.anubhavps.pdfsync.interfaces.iOnOpenActivity;
import com.anubhavps.pdfsync.interfaces.iOnPdfPressed;
import com.anubhavps.pdfsync.models.PDF;
import com.anubhavps.pdfsync.network.NetworkProcess;

import com.anubhavps.pdfsync.interfaces.network.iFirebaseRecycleBinResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseStarredResult;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.Query;


public class HomeFragment extends Fragment implements iOnPdfPressed, View.OnClickListener, iFirebaseStarredResult, iFirebaseRecycleBinResult {

    private View rootView;
    private MaterialToolbar toolBar;

    private NetworkProcess networkProcess;

    private iOnOpenActivity onOpenActivity;

    private FloatingActionButton floatingActionButton;

    private RecyclerView recyclerView;

    private ViewMyPDFAdapter viewMyPDFAdapter;

    private MaterialTextView starredTxt;

    private iOnPdfPressed pdfPressed;

    private Dialog dialog;

    private PDF pdfFile;

    private iFirebaseStarredResult firebaseStarredResult;
    private iFirebaseRecycleBinResult firebaseRecycleBinResult;

    private String searchText;

    public HomeFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        toolBar = rootView.findViewById(R.id.homeSearchBar);
        floatingActionButton = rootView.findViewById(R.id.homeUploadFile);

        recyclerView = rootView.findViewById(R.id.homeRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        pdfPressed = this;
        firebaseStarredResult = this;
        firebaseRecycleBinResult = this;

        MenuItem searchMenuItem = toolBar.getMenu().getItem(0);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setQueryHint("Search PDF in Cloud");
        //searchView.setPadding(0, 4, 16, 4);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query.trim();
                if (!searchText.isEmpty()) processFindPdf(searchText);
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
                processQuery();
                return true;
            }
        });


        floatingActionButton.setOnClickListener(v -> getPDF());

        networkProcess = new NetworkProcess();


        return rootView;
    }

    private void processFindPdf(String searchText) {
        String orderBy = "name";
        Query.Direction direction = Query.Direction.DESCENDING;
        Query query = networkProcess.getAllPdfsQuery(orderBy, direction, false, false, searchText);
        FirestoreRecyclerOptions<PDF> options = networkProcess.downloadPdfs(query);
        viewMyPDFAdapter = new ViewMyPDFAdapter(options, getContext(), HomeFragment.this, pdfPressed);
        viewMyPDFAdapter.startListening();
        recyclerView.setAdapter(viewMyPDFAdapter);

    }

    private void processQuery() {
        String orderBy = "name";
        Query.Direction direction = Query.Direction.DESCENDING;
        Query query = networkProcess.getAllPdfsQuery(orderBy, direction, false, false);
        FirestoreRecyclerOptions<PDF> options = networkProcess.downloadPdfs(query);
        viewMyPDFAdapter = new ViewMyPDFAdapter(options, getContext(), HomeFragment.this, pdfPressed);
        viewMyPDFAdapter.startListening();
        recyclerView.setAdapter(viewMyPDFAdapter);
    }

    //this function will get the pdf from the storage
    private void getPDF() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        onOpenActivity.onOpenGalleryIntent(intent);
    }


    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(requireContext(), rootView.findViewById(R.id.homePage), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.olive_200));
        snackbar.show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        try {
            this.onOpenActivity = (iOnOpenActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity + "is not implementing iOpenActivity");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (this.onOpenActivity != null) {
            this.onOpenActivity = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        processQuery();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (viewMyPDFAdapter != null) viewMyPDFAdapter.stopListening();
    }

    @Override
    public void onPdfCardPressed(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "application/pdf");
        startActivity(intent);
    }

    @Override
    public void onPdfDetailsPressed(PDF pdf) {
        dialog = new Dialog(getContext());

        dialog.setContentView(R.layout.dialog_pdf_details);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        MaterialTextView filenameTxt = dialog.findViewById(R.id.dialogPdfDetailFilenameTxt);
        MaterialTextView commentTxt = dialog.findViewById(R.id.dialogPdfDetailCommentTxt);
        MaterialTextView shareTxt = dialog.findViewById(R.id.dialogPdfDetailShareTxt);
        MaterialTextView manageAccessTxt = dialog.findViewById(R.id.dialogPdfDetailManageAccessTxt);
        MaterialTextView renameTxt = dialog.findViewById(R.id.dialogPdfDetailRenameTxt);
        starredTxt = dialog.findViewById(R.id.dialogPdfDetailStarredTxt);
        MaterialTextView removeTxt = dialog.findViewById(R.id.dialogPdfDetailRemoveTxt);

        this.pdfFile = pdf;

        filenameTxt.setText(pdf.getFilename());

        if (pdf.isStarred()) {
            starredTxt.setText("Remove from starred");
        } else {
            starredTxt.setText("Add To starred");
        }

        commentTxt.setOnClickListener(this);
        starredTxt.setOnClickListener(this);
        removeTxt.setOnClickListener(this);
        shareTxt.setOnClickListener(this);


        dialog.show();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dialogPdfDetailStarredTxt) {
            dialog.dismiss();
            if (pdfFile.isStarred()) {
                networkProcess.removeFromStarred(pdfFile.getDocumentId(), firebaseStarredResult);
            } else {
                networkProcess.addToStarred(pdfFile.getDocumentId(), firebaseStarredResult);
            }
        } else if (id == R.id.dialogPdfDetailRemoveTxt) {
            dialog.dismiss();
            if (pdfFile.isRecycleBin()) {
                networkProcess.removeFromRecycleBin(pdfFile.getDocumentId(), firebaseRecycleBinResult);
            } else {
                networkProcess.addToRecycleBin(pdfFile.getDocumentId(), firebaseRecycleBinResult);
            }
        } else if (id == R.id.dialogPdfDetailCommentTxt) {
            dialog.dismiss();
            Intent intent = new Intent(getContext(), Comments.class);
            intent.putExtra("COMMENT_ID", pdfFile.getCommentsId());
            onOpenActivity.onOpenCommentActivity(intent);
        } else if (id == R.id.dialogPdfDetailShareTxt) {
            dialog.dismiss();
            Intent intent = new Intent(getContext(), SharePDF.class);
            intent.putExtra("DOCUMENT_ID", pdfFile.getDocumentId());
            intent.putExtra("NAME",pdfFile.getName());
            intent.putExtra("FILENAME",pdfFile.getFilename());
            intent.putExtra("SIZE",pdfFile.getSize());
            intent.putExtra("UPLOADED_ON",pdfFile.getUploadedOn());
            intent.putExtra("URL",pdfFile.getUrl());
            onOpenActivity.onOpenSharePdfActivity(intent);
        }
    }

    @Override
    public void onErrorReported(Exception e) {
        callSnackBar(e.getLocalizedMessage());
    }

    @Override
    public void onSuccess(Task<Void> task) {

    }

    @Override
    public void onAddedToStarred() {
        callToast("Added to starred");
    }

    @Override
    public void onRemovedFromStarred() {
        callToast("Removed to starred");
    }

    @Override
    public void onSuccess(String message) {

    }

    @Override
    public void onAddedToRecycleBin() {
        callToast("PDF moved to recycle bin");
    }

    @Override
    public void onRemovedFromRecycleBin() {
        callToast("PDF has been restored");
    }

    private void callToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}