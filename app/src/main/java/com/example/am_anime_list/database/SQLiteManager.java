package com.example.am_anime_list.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.am_anime_list.model.Anime;
import com.example.am_anime_list.model.Status;

import java.util.ArrayList;
import java.util.List;

public class SQLiteManager extends SQLiteOpenHelper {
    private static SQLiteManager sqLiteManager;
    private static final String DATABASE_NAME = "anime_database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "animelist";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_IMAGE_URL = "image_url";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_NUM_EPISODES = "num_episodes";
    private static final String COLUMN_MEAN = "mean";
    private static final String COLUMN_PROGRESS = "progress";

    public SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static SQLiteManager instanceOfDatabase(Context context) {
        if (sqLiteManager == null)
            sqLiteManager = new SQLiteManager(context);
        return sqLiteManager;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_IMAGE_URL + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_STATUS + " TEXT, " +
                COLUMN_NUM_EPISODES + " TEXT, " +
                COLUMN_MEAN + " TEXT, " +
                COLUMN_PROGRESS + " INTEGER)";
        sqLiteDatabase.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean checkID(int id) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        try (Cursor result = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = " + id,
                null)) {
            return result.getCount() == 1;
        }
    }

    public void addAnimeToDatabase(Anime anime) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, anime.getId());
        contentValues.put(COLUMN_IMAGE_URL, anime.getImageUrl());
        contentValues.put(COLUMN_TITLE, anime.getTitle());
        contentValues.put(COLUMN_STATUS, Status.watching.getString());
        contentValues.put(COLUMN_NUM_EPISODES, anime.getNumEpisodes());
        contentValues.put(COLUMN_MEAN, "0");
        contentValues.put(COLUMN_PROGRESS, 0);
        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
    }

    public List<Anime> getAnimeListArray(String title, int limit, int offset)
    {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        List<Anime> animeList = new ArrayList<>();
        try (Cursor result = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + TABLE_NAME +
                        " WHERE " + COLUMN_TITLE + " LIKE " + "'%" + title + "%'" +
                        " ORDER BY " + COLUMN_STATUS + " DESC "+
                        " LIMIT " + limit + " OFFSET " + offset,
                null))
        {
            if(result.getCount() != 0)
            {
                while (result.moveToNext())
                {
                    Anime anime = new Anime();
                    anime.setId(result.getInt(0));
                    anime.setImageUrl(result.getString(1));
                    anime.setTitle(result.getString(2));
                    anime.setStatus(Status.fromString(result.getString(3)));
                    anime.setNumEpisodes(result.getString(4));
                    anime.setMean(result.getString(5));
                    anime.setProgress(result.getInt(6));
                    animeList.add(anime);
                }
            }
        }
        return animeList;
    }

    public void updateAnime(Anime anime) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, anime.getId());
        contentValues.put(COLUMN_IMAGE_URL, anime.getImageUrl());
        contentValues.put(COLUMN_TITLE, anime.getTitle());
        contentValues.put(COLUMN_STATUS, anime.getStatus().getString());
        contentValues.put(COLUMN_NUM_EPISODES, anime.getNumEpisodes());
        contentValues.put(COLUMN_MEAN, anime.getMean());
        contentValues.put(COLUMN_PROGRESS, anime.getProgress());
        sqLiteDatabase.update(TABLE_NAME, contentValues, COLUMN_ID + " =? ",
                new String[]{String.valueOf(anime.getId())});
    }

    public void deleteAnime(int id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_NAME, COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)});
    }
}
