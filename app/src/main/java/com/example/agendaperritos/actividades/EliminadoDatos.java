package com.example.agendaperritos.actividades;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.agendaperritos.inicio.MainActivity;
import com.example.agendaperritos.R;
import com.example.agendaperritos.db.DbImagenes;

public class EliminadoDatos extends AppCompatActivity {
    EditText etRegistroEliminar;
    Button btnEliminarRegistro;
    DbImagenes imagenes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminado_datos);

        btnEliminarRegistro = findViewById(R.id.btnEliminarRegistro);
        etRegistroEliminar = findViewById(R.id.etRegistroEliminar);

        imagenes = new DbImagenes(this);

        btnEliminarRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etRegistroEliminar!=null){
                    String registroElimiado = etRegistroEliminar.getText().toString();
                    eliminarRegistro(Integer.parseInt(registroElimiado));
                }else{
                    Toast.makeText(EliminadoDatos.this, "Ingrese datos",Toast.LENGTH_LONG).show();
                    etRegistroEliminar.setFocusable(true);
                }
            }
        });

    }

    private void eliminarRegistro(int id) {
        boolean correcto = imagenes.eliminarDatoGaleria(id);
        if (correcto){
            Toast.makeText(this, "Datos Eliminados con exito",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(EliminadoDatos.this, MainActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this, "Problemas al intentar elimjnar datos",Toast.LENGTH_LONG).show();
        }
    }
}