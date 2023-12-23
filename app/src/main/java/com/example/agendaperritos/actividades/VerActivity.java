package com.example.agendaperritos.actividades;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import com.example.agendaperritos.inicio.MainActivity;
import com.example.agendaperritos.R;
import com.example.agendaperritos.db.DbContactos;
import com.example.agendaperritos.entidades.Contactos;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Locale;

public class VerActivity extends AppCompatActivity {

    TextView txtRegistro, txtFecha, txtHora, txtNombre, txtTelefono, txtCosto, txtMascota, txtDireccion;
    FloatingActionButton fabEditar, fabEliminar, fabCompartir;

    Contactos contacto;;
    int id = 0;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver);

        txtRegistro = findViewById(R.id.txtRegistro);
        txtNombre = findViewById(R.id.txtNombre);
        txtMascota = findViewById(R.id.txtMascota);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtTelefono = findViewById(R.id.txtTelefono);
        txtFecha = findViewById(R.id.txtFecha);
        txtHora = findViewById(R.id.txtHora);
        txtCosto = findViewById(R.id.txtCosto);
        fabEditar = findViewById(R.id.fabEditar);
        fabEliminar = findViewById(R.id.fabEliminar);
        fabCompartir = findViewById(R.id.fabCompartir);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                id = Integer.parseInt(null);
            } else {
                id = extras.getInt("ID");
            }
        } else {
            id = (int) savedInstanceState.getSerializable("ID");
        }

        DbContactos dbContactos = new DbContactos(VerActivity.this);
        contacto = dbContactos.verContactoCitasNombres(id);

        if (contacto != null) {
            txtRegistro.setText(contacto.getRegistro());
            txtNombre.setText(contacto.getNombre());
            txtMascota.setText(contacto.getMascota());
            txtFecha.setText(contacto.getFecha());
            txtHora.setText(contacto.getHora());
            txtCosto.setText("$ " + contacto.getCosto());

            txtRegistro.setInputType(InputType.TYPE_NULL);
            txtNombre.setInputType(InputType.TYPE_NULL);
            txtMascota.setInputType(InputType.TYPE_NULL);
            txtFecha.setInputType(InputType.TYPE_NULL);
            txtHora.setInputType(InputType.TYPE_NULL);
            txtCosto.setInputType(InputType.TYPE_NULL);

            String idR = txtRegistro.getText().toString();
            DbContactos dbContactoDic = new DbContactos( VerActivity.this);

            Contactos contactor = dbContactoDic.verContacto(Integer.parseInt(idR));

            txtDireccion.setText(contactor.getDireccion());
            txtTelefono.setText(contactor.getTelefono());
            txtDireccion.setInputType(InputType.TYPE_NULL);
            txtTelefono.setInputType(InputType.TYPE_NULL);

            fabEditar = findViewById(R.id.fabEditar);
            fabEliminar = findViewById(R.id.fabEliminar);

        }
        fabEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerActivity.this, EditarActivity.class);
                intent.putExtra("ID", id);
                startActivity(intent);
            }
        });

        fabEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VerActivity.this);
                builder.setMessage("¿Desea Elimnar Cita?")
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (dbContactos.eliminarCita(id)) {
                                    lista();
                                }
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });

        fabCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Número de teléfono al que deseas enviar el mensaje
                String numeroTelefono = txtTelefono.getText().toString();
                String lineaHorizontal = " "; // Puedes ajustar la longitud según tus necesidades

                // Texto que deseas compartir
                String mensaje = "¡Hola! Confirmamos tu cita, en Perruquería ★'COSMO Y WANDA'★" +
                        "\n" + lineaHorizontal +
                        "\nDia: " + txtFecha.getText().toString() +
                        "\nHora: " + txtHora.getText().toString() +
                        "\n" + lineaHorizontal +
                        "\nPuedes cancelar en efectivo o Transferencia a:" +
                        "\nNombre: Maribel Salgado" +
                        "\nRut: 16800320-K" +
                        "\nBanco Estado, Cta Rut" +
                        "\n" + lineaHorizontal +
                        "\nTambien puedes reservar a: pelu.cosmoywanda@gmail.com" +
                        "\nSiguenos en Instagram: https://www.instagram.com/peluqueria.canina.cosmoywanda/"+
                        "\n" + lineaHorizontal +
                        "\nGracias por preferirnos.";

                if (numeroTelefono.isEmpty()) {
                    // Crear un intent para abrir WhatsApp y compartir el mensaje
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, mensaje);
                    //sendIntent.putExtra("sms_body", mensaje);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                } else {
                    // Crear un intent para abrir WhatsApp y compartir el mensaje
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_VIEW);
                    String uri = "https://api.whatsapp.com/send/?phone=56" + numeroTelefono + "&text=" + mensaje;
                    sendIntent.setData(Uri.parse(uri));
                    startActivity(sendIntent);

                }
            }
        });
    }

    private void lista() {
        Intent intent = new Intent(this, MainActivity.class);
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