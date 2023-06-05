package com.anubhavps.pdfsync.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhavps.pdfsync.R;
import com.anubhavps.pdfsync.activities.RecycleBin;
import com.anubhavps.pdfsync.fragments.HomeFragment;
import com.anubhavps.pdfsync.fragments.RestrictedFragment;
import com.anubhavps.pdfsync.fragments.StarredFragment;
import com.anubhavps.pdfsync.interfaces.iOnPdfPressed;
import com.anubhavps.pdfsync.models.PDF;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Date;


public class ViewMyPDFAdapter extends FirestoreRecyclerAdapter<PDF, ViewMyPDFAdapter.MyPDFHolder> {


    private final Context context;
    private final Fragment fragment;

    private iOnPdfPressed pdfPressed;

    public ViewMyPDFAdapter(@NonNull FirestoreRecyclerOptions<PDF> options, Context context, Fragment fragment, iOnPdfPressed pdfPressed) {
        super(options);
        this.context = context;
        this.fragment = fragment;
        this.pdfPressed = pdfPressed;
    }


    @Override
    protected void onBindViewHolder(@NonNull MyPDFHolder holder, int position, @NonNull PDF model) {

        holder.fileNameTxt.setText(model.getFilename());
        holder.nameTxt.setText(model.getName());
        Date date = new Date(model.getUploadedOn());
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm");
        String formattedDate = formatter.format(date);
        holder.dateTxt.setText(formattedDate);

        double megabytes = (double) model.getSize() / (1024 * 1024);
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String size = decimalFormat.format(megabytes) + " MB";
        holder.sizeTxt.setText(size);


        if (fragment instanceof HomeFragment || fragment instanceof RestrictedFragment || fragment instanceof StarredFragment) {
            holder.nameTxt.setTextColor(this.context.getResources().getColor(R.color.pdf_list_cell));
        } else {
            if (fragment == null) {
                holder.nameTxt.setTextColor(this.context.getResources().getColor(R.color.pdf_list_cell));
            }else{
                holder.nameTxt.setTextColor(this.context.getResources().getColor(R.color.white));
            }
        }

        if (fragment == null) {
            holder.more.setVisibility(View.INVISIBLE);
            holder.cardView.setOnClickListener(v -> pdfPressed.onPdfDetailsPressed(model));
        } else {
            holder.cardView.setOnClickListener(v -> pdfPressed.onPdfCardPressed(model.getUrl()));
            holder.more.setOnClickListener(v -> pdfPressed.onPdfDetailsPressed(model));
        }

    }


    @NonNull
    @Override
    public MyPDFHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_pdf_list, parent, false);
        return new MyPDFHolder(rootView);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
    }

    @Override
    public void updateOptions(@NonNull FirestoreRecyclerOptions<PDF> options) {
        super.updateOptions(options);
    }

    static class MyPDFHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        MaterialTextView fileNameTxt, nameTxt, sizeTxt, dateTxt;

        ImageView more;


        public MyPDFHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cellPdfListCardView);
            fileNameTxt = itemView.findViewById(R.id.cellPdfListFileNameTxt);
            nameTxt = itemView.findViewById(R.id.cellPdfListUploadedBy);
            sizeTxt = itemView.findViewById(R.id.cellPdfListSizeTxt);
            dateTxt = itemView.findViewById(R.id.cellPdfListUploadedOn);
            more = itemView.findViewById(R.id.cellPdfListMore);
        }
    }
}
