package com.anubhavps.pdfsync.interfaces.network;


import android.net.Uri;

import com.anubhavps.pdfsync.models.Comment;
import com.anubhavps.pdfsync.models.PDF;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import java.util.Map;

public interface iFirebaseService {

    void createAccount(String mailId, String password, iFirebaseAccountSignUpResult firebaseAccountSignUpResult);

    void sendVerificationMail(iFirebaseAccountSignUpResult firebaseAccountSignUpResult);

    void loginUser(String mailId, String password, iFirebaseAccountSignInResult firebaseAccountSignInResult);

    void createUserRecord(Map<String, Object> user, String userID, iFirebaseAccountSignUpResult firebaseAccountSignUpResult);

    void downloadUserRecord(String user_UID, iFirebaseDocumentDownloadResult firebaseDocumentDownloadResult);

    void resetPassword(String mailId, iFirebaseAccountSignInResult firebaseAccountSignInResult);

    void logoutUser();

    void generateFCMToken(iFirebaseFCMTokenResult firebaseFCMTokenResult);

    void uploadFCMToken(String fcmToken, String user_UID, iFirebaseFCMTokenResult firebaseFCMTokenResult);

    void isUserSignedIn(iFirebaseAuthSession firebaseAuthSession);

    void isUserNameAvailable(String username, iFirebaseAccountSignUpResult firebaseAccountSignUpResult);

    void getUserMailId(String username, iFirebaseAccountSignInResult firebaseAccountSignInResult);

    void getUserId(String username);

    void createUsernameRecord(String username, String user_UID, iFirebaseAccountSignUpResult firebaseAccountSignUpResult);

    void uploadPdfToStorage(String filename, Uri uri, iFirebaseStorageResult firebaseStorageResult);

    void uploadPdfToDatabase(PDF pdf, iFirebaseDataUploadResult firebaseDataUploadResult);

    void pdfExists(String filename, iFirebaseDataUploadResult firebaseDataUploadResult);

    FirestoreRecyclerOptions<PDF> downloadPdfs(Query query);

    Query getAllPdfsQuery(String orderBy, Query.Direction direction, boolean recycleBin,boolean starred);

    void addToStarred(String docId,iFirebaseStarredResult firebaseStarredResult);

    void removeFromStarred(String docId,iFirebaseStarredResult firebaseStarredResult);

    void addToRecycleBin(String docId,iFirebaseRecycleBinResult firebaseRecycleBinResult);

    void removeFromRecycleBin(String docId,iFirebaseRecycleBinResult firebaseRecycleBinResult);

    void addComment(Comment comment,String commentId,iOnFirebaseCommentResult firebaseCommentResult);

    Query getAllCommentsQuery(String commentId, Query.Direction direction);

    FirestoreRecyclerOptions<Comment> downloadAllComments(Query query);

    void getUserDetails(String username,iFirebaseQueryUserDetailResult firebaseQueryUserDetailResult);

    void sharePdfWithUser(String inviteeName,String inviteeUsername,String inviteeUser_UID,String inviteeMailId,String inviteMessage,PDF pdf,iFirebaseSharePdfResult firebaseSharePdfResult);

    Query getAllSharedWithMePdfQuery(String orderBy,Query.Direction direction);
    Query getAllRestrictedPdfQuery(String orderBy,Query.Direction direction);
    Query getAllRemovedPdfsQuery(String orderBy,Query.Direction direction);

    void deletePermanentlyPdf(String documentId,iFirebaseResult firebaseResult);




}
