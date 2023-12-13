package com.example.agendaperritos;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.agendaperritos.db.DbContactos;
import com.example.agendaperritos.entidades.Contactos;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NuevaCita extends AppCompatActivity {

    EditText txtRegistro, txtNombre, txtFecha, txtHora, txtCosto,txtMascota;
    Spinner spRegistros;
    Button btnGuardar, btnCompartir;
    Contactos contacto;
    int ids=0;
    DbContactos dbContactos = new DbContactos(NuevaCita.this);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_cita);

        spRegistros = findViewById(R.id.spRegistros);
        txtRegistro = findViewById(R.id.txtRegistro);
        txtNombre = findViewById(R.id.txtNombre);
        txtNombre.setInputType(InputType.TYPE_NULL);
        txtMascota= findViewById(R.id.txtMascota);
        txtMascota.setInputType(InputType.TYPE_NULL);
        txtFecha= findViewById(R.id.txtFecha);
        txtHora = findViewById(R.id.txtHora);
        txtCosto= findViewById(R.id.txtCosto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCompartir = findViewById(R.id.btncompartir);

        List<String> registros = dbContactos.obtenerRegistros();

        Collections.sort(registros);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, registros);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRegistros.setAdapter(adapter);

        spRegistros.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Obtiene el elemento seleccionado en el Spinner
                String selectedItem = (String) adapterView.getItemAtPosition(i);

                // Extrae el número de registro de la cadena seleccionada
                String[] partes = selectedItem.split(" - |N:");
                if (partes.length == 4) {
                    String nombreDueño = partes[0];
                    String nombreMascota = partes[1];
                    String numeroRegistro = partes[3];

                    // Actualiza el TxtRegistro con el número de registro seleccionado
                    txtRegistro.setText(numeroRegistro);
                    txtNombre.setText(nombreDueño);
                    txtMascota.setText(nombreMascota);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Maneja la situación cuando no se ha seleccionado nada
            }
        });


        txtRegistro.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    String id = txtRegistro.getText().toString();
                    contacto = dbContactos.verContactoNombre(id);

                    String nombreDueño = contacto.getNombre();
                    String nombreMascota = contacto.getMascota();
                    txtNombre.setText(nombreDueño);
                    txtMascota.setText(nombreMascota);
                }
            }
        });

        btnCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzaComparte();
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String registro = String.valueOf(txtRegistro.getText());
                String nombre = convertirAPropio(String.valueOf(txtNombre.getText()));
                String mascota = convertirAPropio(String.valueOf(txtMascota.getText()));
                String fecha = txtFecha.getText().toString();
                String hora = txtHora.getText().toString();
                String costo = txtCosto.getText().toString();
                if(nombre.isEmpty() || mascota.isEmpty()||fecha.isEmpty()||hora.isEmpty()||costo.isEmpty()){
                    Toast.makeText(NuevaCita.this, "Complete todos los datos", Toast.LENGTH_SHORT).show();
                }else{
                    DbContactos dbContactos = new DbContactos(NuevaCita.this);
                    long id = dbContactos.insertaCita(registro,nombre,mascota,fecha,hora,costo);
                    if (id>0){
                        Toast.makeText(NuevaCita.this, "Registro Guardado con Exito", Toast.LENGTH_LONG).show();
                        ids = (int) id;
                        limpiar();
                    }else{
                        Toast.makeText(NuevaCita.this, "Error al Ingresar Contacto", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void limpiar(){
        txtRegistro.setText("");
        txtNombre.setText("");
        txtMascota.setText("");
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

    private String obtenerRegistroDesdeNombreMascota(String nombreDueño, String nombreMascota) {
        // Implementa lógica para obtener el número de registro correspondiente
        // Puedes consultar la base de datos o utilizar otro método según tu estructura de datos
        // ...

        return "NúmeroDeRegistro"; // Reemplaza con el número de registro real
    }
}