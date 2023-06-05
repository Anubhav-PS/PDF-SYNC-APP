package com.anubhavps.pdfsync.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhavps.pdfsync.R;
import com.anubhavps.pdfsync.fragments.HomeFragment;
import com.anubhavps.pdfsync.interfaces.iOnPdfPressed;
import com.anubhavps.pdfsync.models.Comment;
import com.anubhavps.pdfsync.models.PDF;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;


public class CommentsAdapter extends FirestoreRecyclerAdapter<Comment, CommentsAdapter.CommentsHolder> {


    private final Context context;

    public CommentsAdapter(@NonNull FirestoreRecyclerOptions<Comment> options, Context context) {
        super(options);
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull CommentsHolder holder, int position, @NonNull Comment model) {

        holder.usernameTxt.setText(model.getBy());
        holder.messageTxt.setText(model.getComment());

        // Get the current date and time
        Date currentDate = new Date();

        // Get the timestamp you want to format
        Date timestamp = model.getTime().toDate();

        // Create the date formatter with the desired format pattern
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM HH:mm yyyy");

        // Format the timestamp based on the conditions
        String formattedTimestamp;
        if (isWithinWeek(currentDate, timestamp)) {
            formattedTimestamp = new SimpleDateFormat("HH:mm").format(timestamp);
        } else if (isSameYear(currentDate, timestamp)) {
            formattedTimestamp = new SimpleDateFormat("dd MMM HH:mm").format(timestamp);
        } else {
            formattedTimestamp = formatter.format(timestamp);
        }

        holder.timeTxt.setText(formattedTimestamp);
    }

    // Check if two dates have the same year
    private static boolean isSameYear(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    // Check if a date is within the past week
    private static boolean isWithinWeek(Date currentDate, Date date) {
        long currentTimeMillis = currentDate.getTime();
        long pastWeekMillis = currentTimeMillis - (7 * 24 * 60 * 60 * 1000);
        return date.getTime() >= pastWeekMillis;
    }

    @NonNull
    @Override
    public CommentsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_comments, parent, false);
        return new CommentsHolder(rootView);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
    }

    @Override
    public void updateOptions(@NonNull FirestoreRecyclerOptions<Comment> options) {
        super.updateOptions(options);
    }

    static class CommentsHolder extends RecyclerView.ViewHolder {
        MaterialTextView usernameTxt, timeTxt, messageTxt;

        public CommentsHolder(@NonNull View itemView) {
            super(itemView);
            usernameTxt = itemView.findViewById(R.id.cellCommentsUsername);
            timeTxt = itemView.findViewById(R.id.cellCommentsUploadedOn);
            messageTxt = itemView.findViewById(R.id.cellCommentsMessage);
        }
    }
}
