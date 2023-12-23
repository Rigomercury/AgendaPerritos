package com.example.agendaperritos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 13;
    private static final String DATABASE_NOMBRE = "agenda.db";
    public static final String TABLE_CONTACTOS = "t_contactos";
    public static final String TABLE_CITAS = "t_citas";
    public static final String TABLA_IMAGENES = "t_imagenes";

    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DbHelper", "onCreate: Creando tablas");

        db.execSQL("CREATE TABLE " + TABLE_CONTACTOS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "registro TEXT NOT NULL,"+
                "nombre TEXT NOT NULL," +
                "mascota TEXT NOT NULL," +
                "direccion TEXT NOT NULL," +
                "imagen BLOB," +
                "telefono TEXT NOT NULL," +
                "imagen2 BLOB)");

        db.execSQL("CREATE TABLE " + TABLE_CITAS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "registro TEXT NOT NULL,"+
                "nombre TEXT NOT NULL," +
                "mascota TEXT NOT NULL," +
                "fecha TEXT NOT NULL," +
                "hora TEXT NOT NULL," +
                "costo TEXT NOT NULL )" );

        db.execSQL("CREATE TABLE " + TABLA_IMAGENES + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "registro TEXT NOT NULL,"+
                "imagen BLOB )" );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Log.d("DbHelper", "onUpgrade: Actualizando base de datos");
        //db.execSQL("ALTER TABLE " + TABLE_CONTACTOS + " ADD COLUMN imagen2 BLOB");

        Log.d("DbHelper", "onUpgrade: Actualizando base de datos");
        db.endTransaction();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_IMAGENES);

        onCreate(db);

    }
}
