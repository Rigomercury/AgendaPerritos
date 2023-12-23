package com.example.agendaperritos.actividades;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agendaperritos.R;
import com.example.agendaperritos.db.DbContactos;
import com.example.agendaperritos.entidades.Contactos;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Locale;

public class EditarActivity extends AppCompatActivity {

    EditText txtRegistro, txtNombre, txtTelefono, txtFecha, txtHora,txtCosto,txtMascota,txtDireccion;;
    Button btnGuardar;
    FloatingActionButton fabEditar, fabEliminar;
    boolean correcto = false;
    Contactos contacto;
    int id = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edita);

        txtRegistro = findViewById(R.id.txtRegistro);
        txtNombre = findViewById(R.id.txtNombre);
        txtTelefono= findViewById(R.id.txtTelefono);
        txtFecha= findViewById(R.id.txtFecha);
        txtHora= findViewById(R.id.txtHora);
        txtMascota= findViewById(R.id.txtMascota);
        txtDireccion= findViewById(R.id.txtDireccion);
        txtCosto =  findViewById(R.id.txtCosto);
        fabEditar= findViewById(R.id.fabEditar);
        fabEditar.setVisibility(View.INVISIBLE);
        fabEliminar= findViewById(R.id.fabEliminar);
        fabEliminar.setVisibility(View.INVISIBLE);
        btnGuardar = findViewById(R.id.btnGuardar);

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();

            if(extras == null){
                id = Integer.parseInt(null);
            }else{
                id = extras.getInt("ID");
            }
        }else{
            id = (int) savedInstanceState.getSerializable("ID");


        }

        DbContactos dbContactos = new DbContactos(EditarActivity.this);
        contacto = dbContactos.verContactoCitasNombres(id);

        if(contacto != null){
            txtRegistro.setText(contacto.getRegistro());
            txtNombre.setText(contacto.getNombre());
            txtMascota.setText(contacto.getMascota());
            txtFecha.setText(contacto.getFecha());
            txtHora.setText(contacto.getHora());
            txtCosto.setText(contacto.getCosto());

            String idR = txtRegistro.getText().toString();
            Contactos contacto2 = dbContactos.verContacto(Integer.parseInt(idR));
            txtDireccion.setText(contacto2.getDireccion());
            txtTelefono.setText(contacto2.getTelefono());


        }

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!txtNombre.getText().toString().equals("") &&!txtMascota.getText().toString().equals("") && !txtFecha.getText().toString().equals("") &&  !txtHora.getText().toString().equals("")){
                    correcto = dbContactos.editarCita(id,txtNombre.getText().toString(),txtMascota.getText().toString(),txtFecha.getText().toString(),txtHora.getText().toString(),txtCosto.getText().toString());

                    if(correcto){
                        Toast.makeText(EditarActivity.this, "Registro Modificado", Toast.LENGTH_LONG).show();
                        verRegistro();
                    }else{
                        Toast.makeText(EditarActivity.this, "Registro Fallo", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(EditarActivity.this, "Llene los campos", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public void verRegistro(){
        Intent intent = new Intent(this, VerActivity.class);
        intent.putExtra("ID",id);
        startActivity(intent);
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
}
