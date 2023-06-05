package com.anubhavps.pdfsync.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.anubhavps.pdfsync.enums.DatabaseOperationStatus;
import com.anubhavps.pdfsync.models.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LocalDatabase extends SQLiteOpenHelper implements iLocalDatabaseService {

    private static final String USER_TABLE = "USER_TABLE";

    private static final String USER_UID = "USER_UID";
    private static final String MAIL_ID = "MAIL_ID";
    private static final String USER_NAME = "USER_NAME";


    private static final String NAME = "NAME";

    private static final String CONTACT_NUMBER = "CONTACT_NUMBER";
    private static final String FCM_TOKEN = "FCM_TOKEN";
    private static final String AVATAR = "AVATAR";

    private static final int DB_Version = 2;
    private final static String name = "PDF_SYNC_DB";
    private static ExecutorService executors;
    private final int N = 2;

    private iLocalDatabaseResult localDatabaseResult;

    public LocalDatabase(@Nullable Context context) {
        super(context, name, null, DB_Version);
    }

    public LocalDatabase(@Nullable Context context, iLocalDatabaseResult localDatabaseResult) {
        super(context, name, null, DB_Version);
        this.localDatabaseResult = localDatabaseResult;
        executors = Executors.newFixedThreadPool(N);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        createUserTable(db);
    }

    private void createUserTable(SQLiteDatabase db) {
        final String createTableStatement
                = "CREATE TABLE " + USER_TABLE +
                " (" +
                USER_UID + " TEXT PRIMARY KEY , " +
                MAIL_ID + " TEXT , " +
                USER_NAME + " TEXT , " +
                NAME + " TEXT , " +
                CONTACT_NUMBER + " TEXT , " +
                FCM_TOKEN + " TEXT , " +
                AVATAR + " INT )";
        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        onCreate(db);
    }

    public static ExecutorService getExecutors() {
        return executors;
    }

    public static void stopExecutors() {
        executors.shutdown();
    }

    public Runnable runParallelUserInsertion(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        return () -> {
            ContentValues cv = new ContentValues();

            cv.put(USER_UID, user.getUser_UID());
            cv.put(MAIL_ID, user.getMailId());
            cv.put(USER_NAME, user.getUsername());
            cv.put(NAME,user.getName());
            cv.put(CONTACT_NUMBER, user.getContactNumber());
            cv.put(FCM_TOKEN, user.getFcmToken());
            cv.put(AVATAR, user.getAvatar());

            long result = db.insert(USER_TABLE, null, cv);
            db.close();
        };
    }

    private Runnable runParallelUserUpdate(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        return () -> {
            ContentValues cv = new ContentValues();

            cv.put(NAME, user.getName());
            cv.put(CONTACT_NUMBER, user.getContactNumber());
            cv.put(FCM_TOKEN, user.getFcmToken());
            cv.put(AVATAR, user.getAvatar());
            cv.put(FCM_TOKEN, user.getFcmToken());

            db.update(USER_TABLE, cv, "USER_UID = ?", new String[]{user.getUser_UID()});
            db.close();
        };
    }

    private Runnable hasTaskCompleted(Future<String> result, DatabaseOperationStatus notifyStatus) {
        return () -> {
            while (!result.isDone()) {

            }
            if (notifyStatus == DatabaseOperationStatus.USER_UPDATED) {
                localDatabaseResult.userUpdatedInDB();
            } else if (notifyStatus == DatabaseOperationStatus.USER_ADDED) {
                localDatabaseResult.userAddedToDB();
            }
        };
    }

    @Override
    public void addUser(User user) {
        Future<String> result = executors.submit(runParallelUserInsertion(user), "Done");
        executors.submit(hasTaskCompleted(result, DatabaseOperationStatus.USER_ADDED));
    }

    @Override
    public User getUser() {
        User user = User.getInstance();
        final String query = "SELECT * FROM " + USER_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            user.setUser_UID(cursor.getString(0));
            user.setMailId(cursor.getString(1));
            user.setUsername(cursor.getString(2));
            user.setName(cursor.getString(3));
            user.setContactNumber(cursor.getString(4));
            user.setFcmToken(cursor.getString(5));
            user.setAvatar(cursor.getInt(6));
        }

        cursor.close();
        db.close();
        return user;
    }

    @Override
    public void updateUser(User user) {
        Future<String> result = executors.submit(runParallelUserUpdate(user), "Done");
        executors.submit(hasTaskCompleted(result, DatabaseOperationStatus.USER_UPDATED));
    }

    @Override
    public boolean deleteUser() {
        final String query = "delete from " + USER_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return true;
        }
        cursor.close();
        db.close();
        return false;
    }
}
