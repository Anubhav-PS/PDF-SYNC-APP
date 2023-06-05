package com.anubhavps.pdfsync.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.anubhavps.pdfsync.R;
import com.anubhavps.pdfsync.adapters.CommentsAdapter;
import com.anubhavps.pdfsync.adapters.ViewMyPDFAdapter;
import com.anubhavps.pdfsync.fragments.HomeFragment;
import com.anubhavps.pdfsync.models.Comment;
import com.anubhavps.pdfsync.models.PDF;
import com.anubhavps.pdfsync.models.User;
import com.anubhavps.pdfsync.network.NetworkProcess;
import com.anubhavps.pdfsync.network.iOnFirebaseCommentResult;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.api.Usage;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class Comments extends AppCompatActivity implements View.OnClickListener, iOnFirebaseCommentResult {

    private RecyclerView recyclerView;

    private TextInputEditText addCommentsEt;

    private ImageButton sendBtn, closeBtn;

    private CommentsAdapter commentsAdapter;

    private NetworkProcess networkProcess;

    private String inputMessage;

    private String commentId;

    private iOnFirebaseCommentResult firebaseCommentResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);


        commentId = getIntent().getStringExtra("COMMENT_ID");


        addCommentsEt = findViewById(R.id.commentsEt);
        sendBtn = findViewById(R.id.commentsSendBtn);
        closeBtn = findViewById(R.id.commentsCloseBtn);

        recyclerView = findViewById(R.id.commentsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Comments.this));

        networkProcess = new NetworkProcess();


        firebaseCommentResult = this;
        sendBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.commentsSendBtn) {
            uploadComment();
        } else if (id == R.id.commentsCloseBtn) {
            onBackPressed();
        }
    }

    private void uploadComment() {
        if (addCommentsEt.getText()==null){
            return;
        }
        if (addCommentsEt.getText().toString().isEmpty()){
            return;
        }
        inputMessage = Objects.requireNonNull(addCommentsEt.getText()).toString().trim();
        Comment comment = new Comment(inputMessage, User.getInstance().getUsername());
        networkProcess.addComment(comment, commentId, firebaseCommentResult);
        addCommentsEt.setText(null);
    }

    @Override
    public void onCommentFailed() {
        Toast.makeText(this, "Couldn't deliver the comment", Toast.LENGTH_LONG).show();
    }

    private void processQuery(String commentId) {
        Query.Direction direction = Query.Direction.ASCENDING;
        Query query = networkProcess.getAllCommentsQuery(commentId, direction);
        FirestoreRecyclerOptions<Comment> options = networkProcess.downloadAllComments(query);
        commentsAdapter = new CommentsAdapter(options, Comments.this);
        commentsAdapter.startListening();
        recyclerView.setAdapter(commentsAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        processQuery(commentId);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (firebaseCommentResult != null) {
            firebaseCommentResult = null;
        }

        if (commentsAdapter != null) commentsAdapter.stopListening();

    }
}