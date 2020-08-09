package dynasty.software.the.stylishly.repo;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.repo.room.StylishlyDatabase;

/**
 * Author : Aduraline.
 */

public class RepositoryManager {

    private static RepositoryManager manager;
    private StylishlyDatabase database;
    private static final String DATABASE_NAME = "stylishly.db";
    private static SharedPreferences mSharedPreferences;

    private RepositoryManager() {

        Context context = StylishlyApplication.getApplication().getApplicationContext();
        database = Room.databaseBuilder(context, StylishlyDatabase.class, DATABASE_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static synchronized RepositoryManager manager() {

        if (manager == null)
            manager = new RepositoryManager();

        return manager;
    }

    public SharedPreferences preferences() {
        return mSharedPreferences;
    }
    public StylishlyDatabase database() {
        return database;
    }
}
