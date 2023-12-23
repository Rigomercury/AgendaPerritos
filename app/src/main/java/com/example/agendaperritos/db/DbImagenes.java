package com.example.agendaperritos.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;

import androidx.annotation.Nullable;

import com.example.agendaperritos.entidades.Contactos;
import com.example.agendaperritos.entidades.Imagenes;

import java.util.ArrayList;

public class DbImagenes extends DbHelper{
    Context context;

    public DbImagenes(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public ArrayList<Imagenes> mostrarImagenes(String id){

        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        ArrayList<Imagenes> listaImagenes = new ArrayList<>();
        Imagenes imagenes = null;
        Cursor cursorImagenes = null;

        cursorImagenes = db.rawQuery("SELECT * FROM " + TABLA_IMAGENES + " WHERE registro = " + id, null);


        if(cursorImagenes.moveToFirst()){
            do{
                imagenes = new Imagenes();
                imagenes.setId(cursorImagenes.getInt(0));
                imagenes.setRegistro(cursorImagenes.getString(1));
                imagenes.setImagen(cursorImagenes.getBlob(2));
                listaImagenes.add(imagenes);

            }while (cursorImagenes.moveToNext());
        }

        cursorImagenes.close();

        return listaImagenes;
    }

    public ArrayList<Imagenes> mostrarImagenesParaDialog(int id){

        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        ArrayList<Imagenes> listaImagenes = new ArrayList<>();
        Imagenes imagenes = null;
        Cursor cursorImagenes = null;

        cursorImagenes = db.rawQuery("SELECT * FROM " + TABLA_IMAGENES + " WHERE id = " + id, null);


        if(cursorImagenes.moveToFirst()){
            do{
                imagenes = new Imagenes();
                imagenes.setId(cursorImagenes.getInt(0));
                imagenes.setRegistro(cursorImagenes.getString(1));
                imagenes.setImagen(cursorImagenes.getBlob(2));
                listaImagenes.add(imagenes);

            }while (cursorImagenes.moveToNext());
        }

        cursorImagenes.close();

        return listaImagenes;
    }

    public long insertaGaleria(String registro, byte[] imagen) {
        long id = 0;

        try {
            DbHelper dbhelper = new DbHelper(context);
            SQLiteDatabase db = dbhelper.getWritableDatabase();

            ContentValues value = new ContentValues();
            value.put("registro", registro);
            value.put("imagen", imagen);

            id = db.insert(TABLA_IMAGENES, null, value);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return id;
    }

    public boolean eliminarImagenDialog(int id){

        boolean correcto;

        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        try{
            db.execSQL("DELETE FROM " + TABLA_IMAGENES + " WHERE id = '" + id+ "'");
            correcto = true;
        }catch (Exception ex){
            ex.toString();
            correcto = false;
        }finally {
            db.close();
        }
        return correcto;
    }

    public boolean eliminarDatoGaleria(int id){

        boolean correcto;

        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        try{
            db.execSQL("DELETE FROM " + TABLA_IMAGENES + " WHERE registro = '" + id+ "'");
            correcto = true;
        }catch (Exception ex){
            ex.toString();
            correcto = false;
        }finally {
            db.close();
        }
        return correcto;
    }
}