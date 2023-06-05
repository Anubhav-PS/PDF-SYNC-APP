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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.anubhavps.pdfsync.interfaces.network.iFirebaseStarredResult;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.Query;

public class RestrictedFragment extends Fragment implements iOnPdfPressed, iFirebaseStarredResult, View.OnClickListener {


    private View rootView;

    private NetworkProcess networkProcess;

    private iOnOpenActivity onOpenActivity;

    private RecyclerView recyclerView;

    private ViewMyPDFAdapter viewMyPDFAdapter;

    private iOnPdfPressed pdfPressed;

    private Dialog dialog;

    private MaterialTextView starredTxt;

    private PDF pdfFile;


    public RestrictedFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_restricted, container, false);

        networkProcess = new NetworkProcess();

        recyclerView = rootView.findViewById(R.id.restrictedPdfRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        pdfPressed = this;


        return rootView;

    }


    private void processQuery() {
        String orderBy = "name";
        Query.Direction direction = Query.Direction.DESCENDING;
        Query query = networkProcess.getAllRestrictedPdfQuery(orderBy, direction);
        FirestoreRecyclerOptions<PDF> options = networkProcess.downloadPdfs(query);
        viewMyPDFAdapter = new ViewMyPDFAdapter(options, getContext(), RestrictedFragment.this, pdfPressed);
        viewMyPDFAdapter.startListening();
        recyclerView.setAdapter(viewMyPDFAdapter);
    }


    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(requireContext(), rootView.findViewById(R.id.restrictedPage), message, Snackbar.LENGTH_LONG);
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

        dialog.setContentView(R.layout.dialog_restricted_pdf_details);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        MaterialTextView filenameTxt = dialog.findViewById(R.id.dialogRestrictedPdfDetailFilenameTxt);
        MaterialTextView commentTxt = dialog.findViewById(R.id.dialogRestrictedPdfDetailCommentTxt);
        MaterialTextView shareTxt = dialog.findViewById(R.id.dialogRestrictedPdfDetailShareTxt);
        MaterialTextView manageAccessTxt = dialog.findViewById(R.id.dialogRestrictedPdfDetailManageAccessTxt);
        MaterialTextView makePdfPrivateTxt = dialog.findViewById(R.id.dialogRestrictedPdfDetailPrivateTxt);
        MaterialTextView renameTxt = dialog.findViewById(R.id.dialogRestrictedPdfDetailRenameTxt);
        starredTxt = dialog.findViewById(R.id.dialogRestrictedPdfDetailStarredTxt);
        MaterialTextView removeTxt = dialog.findViewById(R.id.dialogRestrictedPdfDetailRemoveTxt);

        this.pdfFile = pdf;

        if (pdf.isStarred()) {
            starredTxt.setText("Remove from starred");
        } else {
            starredTxt.setText("Add To starred");
        }

        filenameTxt.setText(pdf.getFilename());

        shareTxt.setOnClickListener(this);
        commentTxt.setOnClickListener(this);
        makePdfPrivateTxt.setOnClickListener(this);
        starredTxt.setOnClickListener(this);
        removeTxt.setOnClickListener(this);


        dialog.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dialogRestrictedPdfDetailStarredTxt) {
            dialog.dismiss();
            if (pdfFile.isStarred()) {
                networkProcess.removeFromStarred(pdfFile.getDocumentId(), this);
            } else {
                networkProcess.addToStarred(pdfFile.getDocumentId(), this);
            }
        } else if (id == R.id.dialogRestrictedPdfDetailRemoveTxt) {
            dialog.dismiss();
            //remove the access
            //then add to recycle bin
        } else if (id == R.id.dialogRestrictedPdfDetailCommentTxt) {
            dialog.dismiss();
            Intent intent = new Intent(getContext(), Comments.class);
            intent.putExtra("COMMENT_ID", pdfFile.getCommentsId());
            onOpenActivity.onOpenCommentActivity(intent);
        } else if (id == R.id.dialogRestrictedPdfDetailShareTxt) {
            dialog.dismiss();
            Intent intent = new Intent(getContext(), SharePDF.class);
            intent.putExtra("DOCUMENT_ID", pdfFile.getDocumentId());
            intent.putExtra("NAME", pdfFile.getName());
            intent.putExtra("FILENAME", pdfFile.getFilename());
            intent.putExtra("SIZE", pdfFile.getSize());
            intent.putExtra("UPLOADED_ON", pdfFile.getUploadedOn());
            intent.putExtra("URL", pdfFile.getUrl());
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
    public void onSuccess(String message) {

    }

    @Override
    public void onAddedToStarred() {
        callToast("Added to starred");
    }

    @Override
    public void onRemovedFromStarred() {
        callToast("Removed to starred");
    }

    private void callToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}