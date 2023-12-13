package com.example.agendaperritos;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.agendaperritos.db.DbContactos;
import com.example.agendaperritos.entidades.Contactos;

import java.util.Calendar;
import java.util.Locale;

public class nuevoCliente extends AppCompatActivity {

    EditText txtRegistro, txtNombre, txtTelefono,txtMascota,txtDireccion;
    Button btnGuardar, btnCompartir;
    Contactos contacto;
    int ids=0;
    DbContactos dbContactos = new DbContactos(nuevoCliente.this);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_cliente);

        txtRegistro = findViewById(R.id.txtRegistro);
        txtNombre = findViewById(R.id.txtNombre);
        txtMascota= findViewById(R.id.txtMascota);
        txtDireccion= findViewById(R.id.txtDireccion);
        txtTelefono= findViewById(R.id.txtTelefono);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCompartir = findViewById(R.id.btncompartir);

        btnCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzaComparte();
            }
        });

        int siguienteRegistro = dbContactos.obtenerSiguienteRegistro();
        txtRegistro.setText(String.valueOf(siguienteRegistro));

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String registro = String.valueOf((txtRegistro.getText()));
                String nombre = convertirAPropio(String.valueOf(txtNombre.getText()));
                String mascota = convertirAPropio(String.valueOf(txtMascota.getText()));
                String direccion = convertirAPropio(String.valueOf(txtDireccion.getText()));
                String telefono = txtTelefono.getText().toString();
                if(nombre.isEmpty() || mascota.isEmpty() ||direccion.isEmpty() ||telefono.isEmpty()){
                    Toast.makeText(nuevoCliente.this, "Complete todos los datos", Toast.LENGTH_SHORT).show();
                }else{
                    long id = dbContactos.insertaContacto(registro, nombre,mascota,direccion,telefono);
                    if (id>0){
                        Toast.makeText(nuevoCliente.this, "Registro Guardado con Exito", Toast.LENGTH_LONG).show();
                        ids = (int) id;
                        limpiar();
                    }else{
                        Toast.makeText(nuevoCliente.this, "Error al Ingresar Contacto", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void limpiar(){
        txtRegistro.setText("");
        txtNombre.setText("");
        txtMascota.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
    }
    public void lanzaComparte(){
        Intent intent = new Intent(this, VerActivity.class);
        intent.putExtra("ID", ids);
        startActivity(intent);
    }
    private String convertirAPropio(String input) {
        String[] palabras = input.split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                String primeraLetra = palabra.substring(0, 1).toUpperCase();
                String restoPalabra = palabra.substring(1).toLowerCase();
                resultado.append(primeraLetra).append(restoPalabra).append(" ");
            }
        }

        return resultado.toString().trim();
    }
}