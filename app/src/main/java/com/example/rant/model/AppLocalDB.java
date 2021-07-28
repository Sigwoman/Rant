package com.example.rant.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.Room;

import com.example.rant.MyApplication;

@Database(entities = {Post.class}, version = 3, exportSchema = false)
abstract class AppLocalDbRepository extends RoomDatabase {
    public abstract PostDAO postDAO();
}

public class AppLocalDB {
    final static public AppLocalDbRepository db = Room.databaseBuilder(
            MyApplication.getAppContext(),
            AppLocalDbRepository.class,
            "dbFileName.db"
        ).fallbackToDestructiveMigration().build();
}