package com.example.agendaperritos.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.agendaperritos.entidades.Contactos;

import java.util.ArrayList;
import java.util.List;

public class DbContactos extends DbHelper{

    Context context;

    public DbContactos(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public long insertaCita(String registro,String nombre, String mascota, String fecha, String hora, String costo){

        long id = 0;

        try{
            DbHelper dbhelper = new DbHelper(context);
            SQLiteDatabase db = dbhelper.getWritableDatabase();

            ContentValues value = new ContentValues();
            value.put("registro", registro);
            value.put("nombre",nombre);
            value.put("mascota", mascota);
            value.put("fecha", fecha);
            value.put("hora", hora);
            value.put("costo", costo);

             id = db.insert(TABLE_CITAS, null, value);

        }catch(Exception ex){
            ex.toString();
        }
        return id;
    }

    public ArrayList<Contactos> mostrarContactos(){

        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        ArrayList<Contactos> listaContactos = new ArrayList<>();
        Contactos contacto = null;
        Cursor cursorContactos = null;

        cursorContactos = db.rawQuery("SELECT * FROM " + TABLE_CITAS + " ORDER BY fecha ASC, hora ASC" , null);

        // Obtener el mes actual del dispositivo
        Calendar calendar = Calendar.getInstance();
        int mesActual = calendar.get(Calendar.MONTH) + 1; // +1 porque los meses en Calendar van de 0 a 11


        if(cursorContactos.moveToFirst()){
            do{
                contacto = new Contactos();
                contacto.setId(cursorContactos.getInt(0));
                contacto.setRegistro(cursorContactos.getString(1));
                contacto.setNombre(cursorContactos.getString(2));
                contacto.setMascota(cursorContactos.getString(3));
                //contacto.setDireccion(cursorContactos.getString(3));
                //contacto.setTelefono(cursorContactos.getString(4));
                contacto.setFecha(cursorContactos.getString(4));
                contacto.setHora(cursorContactos.getString(5));
                contacto.setCosto(cursorContactos.getString(6));
                listaContactos.add(contacto);

            }while (cursorContactos.moveToNext());
        }

        cursorContactos.close();

        return listaContactos;
    }

    public Contactos verContacto(int id){

        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        Contactos contacto = null;
        Cursor cursorContactos;

        cursorContactos = db.rawQuery("SELECT * FROM " + TABLE_CONTACTOS + " WHERE id = " + id + " LIMIT 1 ", null);

        if(cursorContactos.moveToFirst()){
            contacto = new Contactos();
            contacto.setId(cursorContactos.getInt(0));
            contacto.setRegistro(cursorContactos.getString(1));
            contacto.setNombre(cursorContactos.getString(2));
            contacto.setMascota(cursorContactos.getString(3));
            contacto.setDireccion(cursorContactos.getString(4));
            contacto.setTelefono(cursorContactos.getString(5));
        }

        cursorContactos.close();

        return contacto;
    }

    public boolean editarContacto(int id, String nombre,String mascota,String direccion, String telefono, String fecha, String hora, String costo ){

        boolean correcto = false;

        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        try{
            db.execSQL("UPDATE " + TABLE_CONTACTOS + " SET nombre = '" + nombre +"', mascota = '" + mascota +"',direccion = '" + direccion +"',telefono = '" + telefono +"', fecha = '" + fecha +"',hora = '" +
                    hora +"', costo = '" + costo +"' WHERE id = '" + id + "'");
            correcto = true;
        }catch (Exception ex){
            ex.toString();
            correcto = false;
        }finally {
            db.close();
        }
        return correcto;
    }

    public boolean eliminarContacto(int id){

        boolean correcto = false;

        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        try{
            db.execSQL("DELETE FROM " + TABLE_CONTACTOS + " WHERE id = '" + id+ "'");
            correcto = true;
        }catch (Exception ex){
            ex.toString();
            correcto = false;
        }finally {
            db.close();
        }
        return correcto;
    }

    public long insertaContacto(String registro, String nombre, String mascota, String direccion, String telefono) {

        long id = 0;

        try{
            DbHelper dbhelper = new DbHelper(context);
            SQLiteDatabase db = dbhelper.getWritableDatabase();

            ContentValues value = new ContentValues();
            value.put("registro",registro);
            value.put("nombre",nombre);
            value.put("mascota", mascota);
            value.put("direccion", direccion);
            value.put("telefono", telefono);

            id = db.insert(TABLE_CONTACTOS, null, value);

        }catch(Exception ex){
            ex.toString();
        }
        return id;
    }

    public Contactos verContactoNombre(String id){

        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        Contactos contacto = null;
        Cursor cursorContactos;

        cursorContactos = db.rawQuery("SELECT * FROM " + TABLE_CONTACTOS + " WHERE registro = " + id + " LIMIT 1 ", null);

        if(cursorContactos.moveToFirst()){
            contacto = new Contactos();
            contacto.setId(cursorContactos.getInt(0));
            contacto.setRegistro(cursorContactos.getString(1));
            contacto.setNombre(cursorContactos.getString(2));
            contacto.setMascota(cursorContactos.getString(3));
            contacto.setDireccion(cursorContactos.getString(4));
            contacto.setTelefono(cursorContactos.getString(5));
        }

        cursorContactos.close();

        return contacto;
    }

    public Contactos verContactoCitasNombres(int id){

        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        Contactos contacto = null;
        Cursor cursorContactos;

        cursorContactos = db.rawQuery("SELECT * FROM " + TABLE_CITAS + " WHERE id = " + id + " LIMIT 1 ", null);

        if(cursorContactos.moveToFirst()){
            contacto = new Contactos();
            contacto.setRegistro(cursorContactos.getString(1));
            contacto.setNombre(cursorContactos.getString(2));
            contacto.setMascota(cursorContactos.getString(3));
            contacto.setFecha(cursorContactos.getString(4));
            contacto.setHora(cursorContactos.getString(5));
            contacto.setCosto(cursorContactos.getString(6));
        }

        cursorContactos.close();

        return contacto;
    }

    public int obtenerSiguienteRegistro() {
        DbHelper dbhelper = new DbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        int siguienteRegistro = 1;

        try {
            Cursor cursor = db.rawQuery("SELECT MAX(CAST(registro AS INTEGER)) + 1 FROM " + TABLE_CONTACTOS, null);
            if (cursor != null && cursor.moveToFirst()) {
                siguienteRegistro = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return siguienteRegistro;
    }

    public List<String> obtenerRegistros() {

        List<String> registros = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columnas = {"registro","nombre", "mascota", "direccion"};
        Cursor cursor = db.query(TABLE_CONTACTOS, columnas, null, null, null, null, null);

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String registro = cursor.getString(cursor.getColumnIndex("registro"));
            @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex("nombre"));
            @SuppressLint("Range") String mascota = cursor.getString(cursor.getColumnIndex("mascota"));
            @SuppressLint("Range") String direccion = cursor.getString(cursor.getColumnIndex("direccion"));
            registros.add(mascota + " - " + nombre + " - " + direccion +" N:"+registro);
        }

        cursor.close();
        db.close();

        return registros;
    }
}
