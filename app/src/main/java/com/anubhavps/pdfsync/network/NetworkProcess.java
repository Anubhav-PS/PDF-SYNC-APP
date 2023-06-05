package com.anubhavps.pdfsync.network;


import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.anubhavps.pdfsync.interfaces.network.iFirebaseAccountSignInResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseAccountSignUpResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseAuthSession;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseDataUploadResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseDocumentDownloadResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseFCMTokenResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseQueryUserDetailResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseRecycleBinResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseService;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseSharePdfResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseStarredResult;
import com.anubhavps.pdfsync.interfaces.network.iFirebaseStorageResult;
import com.anubhavps.pdfsync.interfaces.network.iOnFirebaseCommentResult;
import com.anubhavps.pdfsync.models.Comment;
import com.anubhavps.pdfsync.models.PDF;
import com.anubhavps.pdfsync.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkProcess implements iFirebaseService {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFunctions firebaseFunctions;
    private StorageReference storageReference;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public NetworkProcess() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void createAccount(String mailId, String password, iFirebaseAccountSignUpResult firebaseAccountSignUpResult) {
        this.firebaseAuth.createUserWithEmailAndPassword(mailId, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseAccountSignUpResult.onAccountCreated(task);
                    } else {
                        firebaseAccountSignUpResult.onErrorReported(task.getException());
                    }
                });
    }

    @Override
    public void sendVerificationMail(iFirebaseAccountSignUpResult firebaseAccountSignUpResult) {
        Objects.requireNonNull(this.firebaseAuth.getCurrentUser()).sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseAccountSignUpResult.onVerificationMailSent();
            } else {
                firebaseAccountSignUpResult.onErrorReported(task.getException());
            }
        });
    }

    @Override
    public void loginUser(String mailId, String password, iFirebaseAccountSignInResult firebaseAccountSignInResult) {
        this.firebaseAuth
                .signInWithEmailAndPassword(mailId, password.trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified()) {
                            //email is verified
                            //proceed with verifying the mail
                            this.firebaseUser = this.firebaseAuth.getCurrentUser();
                            assert this.firebaseUser != null;

                            final String user_UID = this.firebaseUser.getUid();

                            firebaseAccountSignInResult.onEmailVerified(user_UID);

                        } else {
                            // prompt to verify mail

                            firebaseAccountSignInResult.onEmailNotVerified();
                        }
                    } else {
                        //error
                        firebaseAccountSignInResult.onErrorReported(task.getException());
                    }
                });
    }

    @Override
    public void createUserRecord(Map<String, Object> user, String userID, iFirebaseAccountSignUpResult firebaseAccountSignUpResult) {
        //USERS
        db.collection("USERS").document(userID).set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseAccountSignUpResult.onSuccess("User Profile Updated");
            } else {
                firebaseAccountSignUpResult.onErrorReported(task.getException());
            }
        });

    }

    public FirebaseUser getCurrentUser() {
        return this.firebaseAuth.getCurrentUser();
    }

    @Override
    public void downloadUserRecord(String user_UID, iFirebaseDocumentDownloadResult firebaseDocumentDownloadResult) {
        db.collection("USERS").document(user_UID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    //user record exist
                    //proceed further
                    firebaseDocumentDownloadResult.onDocumentDownloaded(task);
                } else {
                    //user record doesn't exisit
                    firebaseDocumentDownloadResult.onDocumentNotExist();
                }
            } else {
                //there was an error
                firebaseDocumentDownloadResult.onDocumentDownloadFailed(task.getException());
            }
        });
    }

    @Override
    public void resetPassword(String mailId, iFirebaseAccountSignInResult firebaseAccountSignInResult) {
        this.firebaseAuth.sendPasswordResetEmail(mailId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //reset password mail is sent
                        //proceed further
                        firebaseAccountSignInResult.onResetPasswordSent();
                    } else {
                        //there was an error
                        firebaseAccountSignInResult.onErrorReported(task.getException());
                    }
                });
    }

    @Override
    public void logoutUser() {
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void generateFCMToken(iFirebaseFCMTokenResult firebaseFCMTokenResult) {
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        //fcm token generation failed
                        //log out user
                        firebaseFCMTokenResult.fcmTokenFailed(task.getException());
                    } else {
                        //fcm token generated
                        //proceed further
                        firebaseFCMTokenResult.fcmTokenGenerated(task);
                    }
                });
    }

    @Override
    public void uploadFCMToken(String fcmToken, String user_UID, iFirebaseFCMTokenResult firebaseFCMTokenResult) {
        Map<String, Object> token = new HashMap<>();
        token.put("fcmToken", fcmToken);
        token.put("lastUpdated", new Timestamp(new Date()));

        db.collection("FCM").document(user_UID).set(token).addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                //fcm token couldn't be uploaded
                //log out user
                firebaseFCMTokenResult.fcmTokenFailed(task.getException());
            } else {
                //fcm token uploaded
                //proceed further
                firebaseFCMTokenResult.fcmTokenUploaded();
            }

        });
    }

    @Override
    public void isUserSignedIn(iFirebaseAuthSession firebaseAuthSession) {

        this.firebaseUser = this.firebaseAuth.getCurrentUser();

        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            //user is signed in
            //proceed to homepage
            firebaseAuthSession.authVerifiedAndAlive(true);
        } else {
            //user is not signed in and not verified user
            //proceed to login page
            this.firebaseAuth.signOut();
            firebaseAuthSession.authVerifiedAndAlive(false);
        }

    }

    @Override
    public void isUserNameAvailable(String username, iFirebaseAccountSignUpResult firebaseAccountSignUpResult) {


        db.collection("USERNAMES").document(username.trim()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    firebaseAccountSignUpResult.onUsernameTaken();
                } else {
                    firebaseAccountSignUpResult.onUsernameAvailable();
                }
            } else {
                firebaseAccountSignUpResult.onErrorReported(task.getException());
            }
        });


    }

    public void usernameAuthExist(String username, iFirebaseAccountSignUpResult firebaseAccountSignUpResult) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);

        firebaseFunctions = FirebaseFunctions.getInstance();

        // Call the Firebase Callable Function
        firebaseFunctions
                .getHttpsCallable("isUsernameAvailable")
                .call(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HttpsCallableResult result = task.getResult();
                        // Handle the result from the Callable Function
                        // You can access the result data using result.getData()
                        Map<String, Object> response = (Map<String, Object>) result.getData();
                        assert response != null;
                        boolean exists = (boolean) response.get("username");

                        if (exists) {
                            //username is already taken
                            firebaseAccountSignUpResult.onUsernameTaken();
                        } else {
                            //username is available
                            firebaseAccountSignUpResult.onUsernameAvailable();
                        }

                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseFunctionsException) {
                            //error reported
                            System.out.println("->>>>>>>>>>> error reported");
                            firebaseAccountSignUpResult.onErrorReported(exception);
                        }
                    }
                });

    }

    @Override
    public void getUserMailId(String username, iFirebaseAccountSignInResult firebaseAccountSignInResult) {


        //https://us-central1-pdf-sync-project.cloudfunctions.net/getMailId

        final String url = "https://us-central1-pdf-sync-project.cloudfunctions.net/getMailId";

        OkHttpClient client = new OkHttpClient();

        String urlWithParams = url + "?username=" + username;

        Request request = new Request.Builder()
                .url(urlWithParams)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("CloudFunctionCaller", "Failed to call Cloud Function", e);
                firebaseAccountSignInResult.onErrorReported(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String result = response.body().string();
                response.close();
                if (response.code() != 210) {
                    firebaseAccountSignInResult.onBadResponse();
                } else {
                    System.out.println("->>>>>>>>>>>>>>>>> value " + result);
                    if (result.equalsIgnoreCase("null")) {
                        firebaseAccountSignInResult.onUsernameNotRegistered();
                    } else {
                        System.out.println("--------->Yay -- " + result);
                        firebaseAccountSignInResult.onUserMailIdFetched(result);
                    }
                }
            }
        });

       /* Map<String, Object> data = new HashMap<>();
        data.put("username", username);

        firebaseFunctions = FirebaseFunctions.getInstance();

        // Call the Firebase Callable Function
        firebaseFunctions
                .getHttpsCallable("getUserMailId")
                .call(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HttpsCallableResult result = task.getResult();
                        // Handle the result from the Callable Function
                        // You can access the result data using result.getData()
                        Map<String, Object> response = (Map<String, Object>) result.getData();
                        assert response != null;
                        String mailId = (String) response.get("mailId");

                        if (mailId == null) {
                            //no account corresponding to this username
                            firebaseAccountSignInResult.onUsernameNotRegistered();
                        } else {
                            //username present and registered
                            firebaseAccountSignInResult.onUserMailIdFetched(mailId);
                        }

                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseFunctionsException) {
                            //error reported
                            firebaseAccountSignInResult.onErrorReported(exception);

                        }
                    }
                });*/
    }

    @Override
    public void getUserId(String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);

        firebaseFunctions = FirebaseFunctions.getInstance();

        // Call the Firebase Callable Function
        firebaseFunctions
                .getHttpsCallable("getUserId")
                .call(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HttpsCallableResult result = task.getResult();
                        // Handle the result from the Callable Function
                        // You can access the result data using result.getData()
                        Map<String, Object> response = (Map<String, Object>) result.getData();
                        assert response != null;
                        String user_uid = (String) response.get("user_UID");

                        if (user_uid == null) {
                            //no account corresponding to this username

                        } else {
                            //username present and registered

                        }

                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseFunctionsException firebaseFunctionsException) {
                            FirebaseFunctionsException.Code code = firebaseFunctionsException.getCode();
                            String message = firebaseFunctionsException.getMessage();

                            //error reported
                        }
                    }
                });
    }

    @Override
    public void createUsernameRecord(String username, String user_UID, iFirebaseAccountSignUpResult firebaseAccountSignUpResult) {
        //USERS
        Map<String, Object> data = new HashMap<>();
        data.put("user_UID", user_UID);

        db.collection("USERNAMES").document(username).set(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseAccountSignUpResult.onSuccess("User Profile Updated");
            } else {
                firebaseAccountSignUpResult.onErrorReported(task.getException());
            }
        });

    }

    @Override
    public void uploadPdfToStorage(String filename, Uri uri, iFirebaseStorageResult firebaseStorageResult) {
        //function to upload the data to fire store if the user has selected an image
        //upload with image
        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference uploadPath = storageReference
                .child("PDF_FILES")
                .child(User.getInstance().getUser_UID())
                .child(filename);

        //upload of pdf failed
        //report error
        uploadPath.putFile(uri).addOnFailureListener(firebaseStorageResult::onPdfUploadFailed).addOnSuccessListener(taskSnapshot -> {
            //pdf uploaded successfully
            //get the meta data
            StorageMetadata metadata = taskSnapshot.getMetadata();
            assert metadata != null;
            long size = metadata.getSizeBytes();
            long creationTimeMillis = metadata.getCreationTimeMillis();

            uploadPath.getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uploadUrl = task.getResult().toString();
                    //successfully downloaded the url
                    //upload the pdf details to database
                    PDF pdf = new PDF(filename, size, uploadUrl, creationTimeMillis);
                    firebaseStorageResult.onPdfSuccessfullyUploadedToStorage(pdf);
                } else {
                    //unable to download url
                    //report download url failed
                    firebaseStorageResult.onPdfDownloadUrlFailed(task.getException());
                }
            });

        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            //(int) progress + "% Uploading...")
            //show upload percentage
            firebaseStorageResult.uploadingPdfProgress(progress);
        });

    }

    @Override
    public void uploadPdfToDatabase(PDF pdf, iFirebaseDataUploadResult firebaseDataUploadResult) {
        DocumentReference documentReference = this.db.collection("PDF").document(User.getInstance().getUser_UID()).collection("FILES").document();
        String documentId = documentReference.getId();
        pdf.setDocumentId(documentId);
        pdf.setCommentsId(documentId);
        documentReference.set(pdf).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseDataUploadResult.onDataUploaded();
            } else {
                firebaseDataUploadResult.onDataUploadFailed(task.getException());
            }
        });
    }

    @Override
    public void pdfExists(String filename, iFirebaseDataUploadResult firebaseDataUploadResult) {
        this.db.collection("PDF")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .whereEqualTo("filename", filename)
                .get().addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) {
                        //doesn't exists
                        firebaseDataUploadResult.onPdfExists(false);
                    } else {
                        //it exists
                        firebaseDataUploadResult.onPdfExists(true);
                    }
                });
    }

    @Override
    public FirestoreRecyclerOptions<PDF> downloadPdfs(Query query) {
        return new FirestoreRecyclerOptions.Builder<PDF>().setQuery(query, PDF.class).build();
    }

    @Override
    public Query getAllPdfsQuery(String orderBy, Query.Direction direction, boolean recycleBin, boolean starred) {
        return this.db.collection("PDF")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .orderBy(orderBy, direction)
                .whereEqualTo("recycleBin", recycleBin)
                .whereEqualTo("starred", starred);
    }

    public Query getAllPdfsQuery(String orderBy, Query.Direction direction, boolean recycleBin, boolean starred, String filename) {
        return this.db.collection("PDF")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .orderBy(orderBy, direction)
                .whereEqualTo("recycleBin", recycleBin)
                .whereEqualTo("filename", filename);
    }

    @Override
    public void addToStarred(String docId, iFirebaseStarredResult firebaseStarredResult) {
        Map<String, Object> data = new HashMap<>();
        data.put("starred", true);
        this.db.collection("PDF")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .document(docId)
                .update(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseStarredResult.onAddedToStarred();
                    } else {
                        firebaseStarredResult.onErrorReported(task.getException());
                    }
                });
    }

    @Override
    public void removeFromStarred(String docId, iFirebaseStarredResult firebaseStarredResult) {
        Map<String, Object> data = new HashMap<>();
        data.put("starred", false);
        this.db.collection("PDF")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .document(docId)
                .update(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseStarredResult.onRemovedFromStarred();
                    } else {
                        firebaseStarredResult.onErrorReported(task.getException());
                    }
                });
    }

    @Override
    public void addToRecycleBin(String docId, iFirebaseRecycleBinResult firebaseRecycleBinResult) {
        Map<String, Object> data = new HashMap<>();
        data.put("recycleBin", true);
        this.db.collection("PDF")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .document(docId)
                .update(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseRecycleBinResult.onAddedToRecycleBin();
                    } else {
                        firebaseRecycleBinResult.onErrorReported(task.getException());
                    }
                });
    }

    @Override
    public void removeFromRecycleBin(String docId, iFirebaseRecycleBinResult firebaseRecycleBinResult) {
        Map<String, Object> data = new HashMap<>();
        data.put("recycleBin", false);
        this.db.collection("PDF")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .document(docId)
                .update(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseRecycleBinResult.onRemovedFromRecycleBin();
                    } else {
                        firebaseRecycleBinResult.onErrorReported(task.getException());
                    }
                });
    }

    @Override
    public void addComment(Comment comment, String commentId, iOnFirebaseCommentResult firebaseCommentResult) {
        DocumentReference documentReference = this.db.collection("COMMENTS")
                .document(commentId)
                .collection("CONTENTS")
                .document();
        String documentId = documentReference.getId();
        comment.setDocumentId(documentId);
        comment.setTime(new Timestamp(new Date()));
        documentReference.set(comment).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                firebaseCommentResult.onCommentFailed();
            }
        });

    }

    @Override
    public Query getAllCommentsQuery(String commentId, Query.Direction direction) {

        return this.db.collection("COMMENTS")
                .document(commentId)
                .collection("CONTENTS")
                .orderBy("time", direction);


    }

    @Override
    public FirestoreRecyclerOptions<Comment> downloadAllComments(Query query) {
        return new FirestoreRecyclerOptions.Builder<Comment>().setQuery(query, Comment.class).build();
    }

    @Override
    public void getUserDetails(String username, iFirebaseQueryUserDetailResult firebaseQueryUserDetailResult) {

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);

        firebaseFunctions = FirebaseFunctions.getInstance();
        // Call the Firebase Callable Function
        firebaseFunctions
                .getHttpsCallable("getUserDetails")
                .call(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HttpsCallableResult result = task.getResult();
                        // Handle the result from the Callable Function
                        // You can access the result data using result.getData()
                        Map<String, Object> response = (Map<String, Object>) result.getData();
                        assert response != null;
                        boolean status = (boolean) response.get("status");

                        if (status) {
                            //no account corresponding to this username
                            String name = (String) response.get("name");
                            String user_UID = (String) response.get("user_UID");
                            String mailId = (String) response.get("mailId");
                            firebaseQueryUserDetailResult.onUserDetailSearchFound(name, mailId, user_UID, username);
                        } else {
                            //username present and registered
                            firebaseQueryUserDetailResult.onUserDoesNotExists();
                        }

                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseFunctionsException) {
                            //error reported
                            firebaseQueryUserDetailResult.onSearchFailed(exception);
                        }
                    }
                });


    }

    @Override
    public void sharePdfWithUser(String inviteeName, String inviteeUsername, String inviteeUser_UID, String inviteeMailId, String inviteMessage, PDF pdf, iFirebaseSharePdfResult firebaseSharePdfResult) {

        Map<String, Object> data = new HashMap<>();
        data.put("user_UID", inviteeUser_UID);
        data.put("name", inviteeName);
        data.put("username", inviteeUsername);
        data.put("mailId", inviteeMailId);
        data.put("message", inviteMessage);

        data.put("senderName", pdf.getName());
        data.put("filename",pdf.getFilename());
        data.put("documentId",pdf.getDocumentId());
        data.put("size",pdf.getSize());
        data.put("uploadedOn",pdf.getUploadedOn());
        data.put("url",pdf.getUrl());


        System.out.println("HELLO SHARE WITH USERS");
        firebaseFunctions = FirebaseFunctions.getInstance();
        // Call the Firebase Callable Function
        firebaseFunctions
                .getHttpsCallable("shareWithUser")
                .call(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HttpsCallableResult result = task.getResult();
                        // Handle the result from the Callable Function
                        // You can access the result data using result.getData()
                        Map<String, Object> response = (Map<String, Object>) result.getData();

                        assert response != null;
                        boolean status = (boolean) response.get("status");

                        if (status) {
                            //process successful
                            String message = (String) response.get("response");
                            firebaseSharePdfResult.onPdfSharedSuccessful(message);
                        } else {
                            //there was an error
                            String message = (String) response.get("response");
                            firebaseSharePdfResult.onPdfShareFailed(message);
                        }

                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseFunctionsException) {
                            //error reported
                            firebaseSharePdfResult.onErrorReported(exception);
                        }
                    }
                });

    }

    @Override
    public Query getAllSharedWithMePdfQuery(String orderBy, Query.Direction direction) {
        return this.db.collection("SHARED_WITH_ME")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .orderBy(orderBy, direction);
    }

    @Override
    public Query getAllRestrictedPdfQuery(String orderBy, Query.Direction direction) {
        return this.db.collection("PDF")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .orderBy(orderBy, direction)
                .whereEqualTo("recycleBin", false)
                .whereEqualTo("sharing", "RESTRICTED");
    }

    @Override
    public Query getAllRemovedPdfsQuery(String orderBy, Query.Direction direction) {
        return this.db.collection("PDF")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .orderBy(orderBy, direction)
                .whereEqualTo("recycleBin", true);
    }

    @Override
    public void deletePermanentlyPdf(String documentId, iFirebaseResult firebaseResult) {
         this.db.collection("PDF")
                .document(User.getInstance().getUser_UID())
                .collection("FILES")
                .document(documentId)
                .delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        firebaseResult.onSuccess("PDF deleted permanently");
                    }else{
                        firebaseResult.onErrorReported(task.getException());
                    }
                });
    }
}

