package com.example.agendaperritos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NOMBRE = "agenda.db";
    public static final String TABLE_CONTACTOS = "t_contactos";
    public static final String TABLE_CITAS = "t_citas";

    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_CONTACTOS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "registro TEXT NOT NULL,"+
                "nombre TEXT NOT NULL," +
                "mascota TEXT NOT NULL," +
                "direccion TEXT NOT NULL," +
                "telefono TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_CITAS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "registro TEXT NOT NULL,"+
                "nombre TEXT NOT NULL," +
                "mascota TEXT NOT NULL," +
                "fecha TEXT NOT NULL," +
                "hora TEXT NOT NULL," +
                "costo TEXT NOT NULL )" );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE " + TABLE_CONTACTOS);
        //db.execSQL("DROP TABLE " + TABLE_CITAS);
        onCreate(db);
    }

}
