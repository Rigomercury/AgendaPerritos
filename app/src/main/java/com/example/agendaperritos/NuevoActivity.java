package com.example.agendaperritos;

import androidx.appcompat.app.AppCompatActivity;

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

public class NuevoActivity extends AppCompatActivity {

    EditText txtNombre, txtTelefono, txtFecha, txtHora, txtCosto,txtMascota,txtDireccion;
    Button btnGuardar, btnCompartir;
    Contactos contacto;
    int ids=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo);

        txtNombre = findViewById(R.id.txtNombre);
        txtMascota= findViewById(R.id.txtMascota);
        txtDireccion= findViewById(R.id.txtDireccion);
        txtTelefono= findViewById(R.id.txtTelefono);
        txtFecha= findViewById(R.id.txtFecha);
        txtHora = findViewById(R.id.txtHora);
        txtCosto= findViewById(R.id.txtCosto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCompartir = findViewById(R.id.btncompartir);

        btnCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzaComparte();
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = txtNombre.getText().toString();
                String mascota = txtMascota.getText().toString();
                String direccion = txtDireccion.getText().toString();
                String telefono = txtTelefono.getText().toString();
                String fecha = txtFecha.getText().toString();
                String hora = txtHora.getText().toString();
                String costo = txtCosto.getText().toString();
                if(nombre.isEmpty() || mascota.isEmpty() ||direccion.isEmpty() ||telefono.isEmpty()||fecha.isEmpty()||hora.isEmpty()||costo.isEmpty()){
                    Toast.makeText(NuevoActivity.this, "Complete todos los datos", Toast.LENGTH_SHORT).show();
                }else{
                    DbContactos dbContactos = new DbContactos(NuevoActivity.this);
                    long id = dbContactos.insertaContacto(nombre,mascota,direccion,telefono,fecha,hora,costo);
                    if (id>0){
                        Toast.makeText(NuevoActivity.this, "Registro Guardado con Exito", Toast.LENGTH_LONG).show();
                        ids = (int) id;
                        limpiar();
                    }else{
                        Toast.makeText(NuevoActivity.this, "Error al Ingresar Contacto", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void limpiar(){
        txtNombre.setText("");
        txtMascota.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtFecha.setText("");
        txtHora.setText("");
        txtCosto.setText("");
    }
    public void showDatePickerDialog(View v) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            txtFecha.setText(selectedDate);
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    public void showTimePickerDialog(View v) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            txtHora.setText(selectedTime);
        }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }
    public void lanzaComparte(){
        Intent intent = new Intent(this, VerActivity.class);
        intent.putExtra("ID", ids);
        startActivity(intent);
    }
}