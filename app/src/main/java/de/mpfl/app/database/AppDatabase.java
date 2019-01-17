package de.mpfl.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.mpfl.app.utils.DateTimeFormat;

public final class AppDatabase extends SQLiteOpenHelper {
    // static instance for singleton pattern
    private static AppDatabase staticInstance;

    private final static String DATABASE_NAME = "appdata.db3";  // internal database name
    private final static int DATABASE_VERSION = 12;             // current app version  (contains only favorites)

    // table names
    // DO NOT CHANGE THIS FIELDS
    private final static String TABLE_FAVORITES = "favorites";

    // favorites table
    // DO NOT CHANGE THIS FIELDS
    private final static String KEY_FAVORITES_ID = "favorite_id";
    private final static String KEY_FAVORITES_TRIP_ID = "trip_id";
    private final static String KEY_FAVORITES_TRIP_TYPE = "trip_type";
    private final static String KEY_FAVORITES_TRIP_NAME = "trip_name";
    private final static String KEY_FAVORITES_TRIP_DESC = "trip_desc";
    private final static String KEY_FAVORITES_TRIP_DATE = "trip_date";
    private final static String KEY_FAVORITES_TRIP_TIME = "trip_time";

    public static synchronized AppDatabase getInstance(Context context) {
        if(staticInstance == null) {
            staticInstance = new AppDatabase(context);
        }

        return staticInstance;
    }

    private AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create favorites table
        String createFavoritesTable = String.format(
                "CREATE TABLE IF NOT EXISTS `%s` ( " +
                        "`%s` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                        "`%s` TEXT NOT NULL, " +
                        "`%s` TEXT NOT NULL, " +
                        "`%s` TEXT NOT NULL, " +
                        "`%s` TEXT NOT NULL, " +
                        "`%s` TEXT NOT NULL, " +
                        "`%s` TEXT NOT NULL )",
                TABLE_FAVORITES, KEY_FAVORITES_ID, KEY_FAVORITES_TRIP_ID, KEY_FAVORITES_TRIP_TYPE, KEY_FAVORITES_TRIP_NAME, KEY_FAVORITES_TRIP_DESC, KEY_FAVORITES_TRIP_DATE, KEY_FAVORITES_TRIP_TIME);

        db.execSQL(createFavoritesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // simply clear and recreate the database...
        String clearFavoritesTable = String.format("DELETE FROM %s", TABLE_FAVORITES);
        db.execSQL(clearFavoritesTable);

        this.onCreate(db);
    }

    public void addFavorite(Favorite favorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        String selectFavoritesQuery = String.format("SELECT * FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = '%s'", TABLE_FAVORITES, KEY_FAVORITES_TRIP_ID, favorite.getTripId(), KEY_FAVORITES_TRIP_DATE, favorite.getTripDate(), KEY_FAVORITES_TRIP_TIME, favorite.getTripTime());

        try {
            Cursor cursor = db.rawQuery(selectFavoritesQuery, null);
            if(cursor != null && cursor.moveToFirst()) {
                // we have this favorite already in our database
                return;
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_FAVORITES_TRIP_ID, favorite.getTripId());
            contentValues.put(KEY_FAVORITES_TRIP_TYPE, favorite.getTripType());
            contentValues.put(KEY_FAVORITES_TRIP_NAME, favorite.getTripName());
            contentValues.put(KEY_FAVORITES_TRIP_DESC, favorite.getTripDesc());
            contentValues.put(KEY_FAVORITES_TRIP_DATE, favorite.getTripDate());
            contentValues.put(KEY_FAVORITES_TRIP_TIME, favorite.getTripTime());

            db.insertOrThrow(TABLE_FAVORITES, null, contentValues);
            db.setTransactionSuccessful();
        } catch(Exception ignored) {
        } finally {
            db.endTransaction();
        }
    }

    public void deleteFavorite(Favorite favorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            String deleteFavoriteCommand = String.format("DELETE FROM %s WHERE %s = %s", TABLE_FAVORITES, KEY_FAVORITES_ID, String.valueOf(favorite.getId()));
            db.execSQL(deleteFavoriteCommand);
            db.setTransactionSuccessful();
        } catch (Exception ex) {
        } finally {
            db.endTransaction();
        }
    }

    public List<Favorite> getAllFavorites() {
        this.cleanFavorites();

        List<Favorite> favoritesList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectFavoritesQuery = String.format("SELECT * FROM %s ORDER BY trip_date, trip_time", TABLE_FAVORITES);
        Cursor cursor = db.rawQuery(selectFavoritesQuery, null);

        try {
            if(cursor.moveToFirst()) {
                do {
                    Favorite favorite = new Favorite();
                    favorite.setId(cursor.getInt(cursor.getColumnIndex(KEY_FAVORITES_ID)));
                    favorite.setTripId(cursor.getString(cursor.getColumnIndex(KEY_FAVORITES_TRIP_ID)));
                    favorite.setTripType(cursor.getString(cursor.getColumnIndex(KEY_FAVORITES_TRIP_TYPE)));
                    favorite.setTripName(cursor.getString(cursor.getColumnIndex(KEY_FAVORITES_TRIP_NAME)));
                    favorite.setTripDesc(cursor.getString(cursor.getColumnIndex(KEY_FAVORITES_TRIP_DESC)));
                    favorite.setTripDate(cursor.getString(cursor.getColumnIndex(KEY_FAVORITES_TRIP_DATE)));
                    favorite.setTripTime(cursor.getString(cursor.getColumnIndex(KEY_FAVORITES_TRIP_TIME)));

                    favoritesList.add(favorite);
                } while(cursor.moveToNext());
            }
        } catch(Exception ignored) {
        } finally {
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return favoritesList;
    }

    private void cleanFavorites() {
        SQLiteDatabase db = this.getWritableDatabase();

        String cleanFavoritesCommand = String.format("DELETE FROM %s WHERE %s < %s", TABLE_FAVORITES, KEY_FAVORITES_TRIP_DATE, DateTimeFormat.from(new Date()).to(DateTimeFormat.YYYYMMDD));
        db.execSQL(cleanFavoritesCommand);
    }
}
