package com.anubhavps.pdfsync.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;

import com.anubhavps.pdfsync.R;
import com.anubhavps.pdfsync.adapters.ViewMyPDFAdapter;
import com.anubhavps.pdfsync.interfaces.iOnPdfPressed;
import com.anubhavps.pdfsync.models.PDF;
import com.anubhavps.pdfsync.network.NetworkProcess;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseRecycleBinResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseResult;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.Query;

public class RecycleBin extends AppCompatActivity implements View.OnClickListener, iOnPdfPressed, iFirebaseRecycleBinResult, iFirebaseResult {

    private ImageButton backArrowBtn;

    private NetworkProcess networkProcess;

    private RecyclerView recyclerView;

    private ViewMyPDFAdapter viewMyPDFAdapter;

    private iOnPdfPressed onPdfPressed;

    private Dialog dialog;

    private PDF pdfFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        backArrowBtn = findViewById(R.id.recycleBinBackBtn);
        backArrowBtn.setOnClickListener(this);

        recyclerView = findViewById(R.id.recycleBinRecycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(RecycleBin.this));


        onPdfPressed = this;

        networkProcess = new NetworkProcess();

    }

    private void processQuery() {
        String orderBy = "name";
        Query.Direction direction = Query.Direction.DESCENDING;
        Query query = networkProcess.getAllRemovedPdfsQuery(orderBy, direction);
        FirestoreRecyclerOptions<PDF> options = networkProcess.downloadPdfs(query);
        viewMyPDFAdapter = new ViewMyPDFAdapter(options, RecycleBin.this, null, this);
        viewMyPDFAdapter.startListening();
        recyclerView.setAdapter(viewMyPDFAdapter);
    }

    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(RecycleBin.this, findViewById(R.id.recycleBinPage), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(RecycleBin.this, R.color.olive_200));
        snackbar.show();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.recycleBinBackBtn) {
            Intent intent = new Intent();
            setResult(503, intent);
            finish();
        } else if (id == R.id.dialogRecycleBinRestoreTxt) {
            dialog.dismiss();
            networkProcess.removeFromRecycleBin(pdfFile.getDocumentId(), this);
        }else if (id==R.id.dialogRecycleBinDeletePermanentTxt){
            dialog.dismiss();
            networkProcess.deletePermanentlyPdf(pdfFile.getDocumentId(), this);
        }
    }

    @Override
    public void onPdfCardPressed(String url) {

    }

    @Override
    public void onPdfDetailsPressed(PDF pdf) {
        dialog = new Dialog(RecycleBin.this);

        dialog.setContentView(R.layout.dialog_recycle_bin_pdf_details);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        MaterialTextView restoreTxt = dialog.findViewById(R.id.dialogRecycleBinRestoreTxt);
        MaterialTextView deletePermanentTxt = dialog.findViewById(R.id.dialogRecycleBinDeletePermanentTxt);

        this.pdfFile = pdf;

        restoreTxt.setOnClickListener(this);
        deletePermanentTxt.setOnClickListener(this);

        dialog.show();
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
    public void onAddedToRecycleBin() {

    }

    @Override
    public void onRemovedFromRecycleBin() {
        callSnackBar("PDF has been Restored");
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
        callSnackBar(message);
    }
}