package com.example.agendaperritos;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agendaperritos.adaptadores.ListaContactoAdapter;
import com.example.agendaperritos.db.DbContactos;
import com.example.agendaperritos.db.DbHelper;
import com.example.agendaperritos.entidades.Contactos;

import java.security.MessageDigest;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RecyclerView listaContactos;
    ArrayList<Contactos> listaArrayContactos;
    ArrayList<Contactos> todosLosContactos;
    ListaContactoAdapter adapter;
    TextView totalCostTextView; // Agrega esta línea para referenciar el TextView del total



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listaContactos = findViewById(R.id.listaContactos);
        listaContactos.setLayoutManager(new LinearLayoutManager(this));
        listaContactos.addItemDecoration(new DividerItemDecoration(this)); // Agregar el ItemDecoration

        totalCostTextView = findViewById(R.id.totalCostTextView); // Enlaza con el TextView del total

        DbContactos dbContactos = new DbContactos(MainActivity.this);
        listaArrayContactos = dbContactos.mostrarContactos(); // Mostrar todos los contactos por defecto
        todosLosContactos = dbContactos.mostrarContactos();

        Collections.reverse(listaArrayContactos);

        adapter = new ListaContactoAdapter(listaArrayContactos);
        listaContactos.setAdapter(adapter);


        //listaContactos.setLayoutManager(new LinearLayoutManager(this));

        cargarSpinnerMeses();
        int mesActual = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int añoActual = Calendar.getInstance().get(Calendar.YEAR);

        // Establecer la selección por defecto en los Spinners del mes y el año
        Spinner monthSpinner = findViewById(R.id.monthSpinner);
        Spinner yearSpinner = findViewById(R.id.yearSpinner);
        monthSpinner.setSelection(mesActual - 1); // Restar 1 porque el índice en el Spinner empieza desde 0
        yearSpinner.setSelection(obtenerPosicionAñoActual(añoActual)); // Obtener la posición del año actual en la lista de años disponibles

        filtrarContactosPorMes(mesActual, añoActual);

        // Obtener la fecha y hora actual del dispositivo
        Calendar calendarioActual = Calendar.getInstance();
        long tiempoActual = calendarioActual.getTimeInMillis();

        // Inicializar variables para almacenar la fecha y hora más próxima
        long tiempoAgendaMasProxima = Long.MAX_VALUE;
        Contactos agendaMasProxima = null;


        // Iterar a través de la lista de contactos y comparar las fechas y horas
        for (Contactos contacto : todosLosContactos) {
            String fechaContacto = contacto.getFecha();
            String horaContacto = contacto.getHora();

            // Convertir la fecha y hora del contacto en milisegundos para comparar con el tiempo actual
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String fechaHoraString = fechaContacto + " " + horaContacto;
            try {
                Date fechaHoraContacto = sdf.parse(fechaHoraString);
                long tiempoContacto = fechaHoraContacto.getTime();

                // Comparar con el tiempo actual y actualizar la agenda más próxima si es necesario
                if (tiempoContacto >= tiempoActual && tiempoContacto < tiempoAgendaMasProxima) {
                    tiempoAgendaMasProxima = tiempoContacto;
                    agendaMasProxima = contacto;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (agendaMasProxima != null) {
            // Calcula la diferencia entre la hora actual y la hora de la cita más próxima en milisegundos
            long tiempoRestante = tiempoAgendaMasProxima - tiempoActual;

            Log.d("TiempoRestante", "Tiempo restante en milisegundos: " + tiempoRestante);

            // Si la diferencia es menor o igual a 30 minutos (en milisegundos), muestra la notificación
            if (tiempoRestante <= 30 * 60 * 1000) {
                long minutosRestantes = tiempoRestante / (60 * 1000); // Calcula los minutos restantes
                String mensaje = "Cita próxima en " + minutosRestantes + " minutos: " + agendaMasProxima.getFecha() + " " + agendaMasProxima.getHora();
                NotificationUtils.showNotification(MainActivity.this, "Cita próxima", mensaje);
            }
        }

        // Asignar color a la agenda más próxima (por ejemplo, cambiar el fondo de la vista)
        if (agendaMasProxima != null) {
            // Aquí puedes cambiar el color de fondo de la vista correspondiente a la agenda más próxima
            int posicionAgendaMasProxima = listaArrayContactos.indexOf(agendaMasProxima);
            adapter.setPosicionAgendaMasProxima(posicionAgendaMasProxima);
        }

    }

    private int obtenerPosicionAñoActual(int añoActual) {
        Spinner yearSpinner = findViewById(R.id.yearSpinner);
        ArrayAdapter<Integer> adapter = (ArrayAdapter<Integer>) yearSpinner.getAdapter();
        return adapter.getPosition(añoActual);
    }


    private void cargarSpinnerMeses() {
        Spinner monthSpinner = findViewById(R.id.monthSpinner);
        Spinner yearSpinner = findViewById(R.id.yearSpinner);

        // Obtener los nombres de los meses en un array
        String[] nombresMeses = new DateFormatSymbols(Locale.getDefault()).getMonths();

        // Crear un ArrayAdapter para el Spinner y establecerlo
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_layout, nombresMeses);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // Cargar el Spinner con los años disponibles (puedes obtener estos años de tu base de datos)
        ArrayList<Integer> listaAnios = new ArrayList<>();
        listaAnios.add(2023); // Por ejemplo, agrega aquí los años disponibles en tu base de datos
        listaAnios.add(2024);
        listaAnios.add(2025);
        listaAnios.add(2026);

        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_layout, listaAnios);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Establecer un listener para el Spinner para filtrar los contactos cuando se selecciona un mes
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int mesSeleccionado = position + 1;
                int añoSeleccionado = (int) yearSpinner.getSelectedItem(); // Usa el año seleccionado del yearSpinner
                filtrarContactosPorMes(mesSeleccionado, añoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada cuando no se selecciona ningún mes
            }
        });
        // Establecer un listener para el Spinner del año para filtrar los contactos cuando se selecciona un año
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Obtener el mes seleccionado (posición + 1 porque los meses en Calendar van de 0 a 11)
                int mesSeleccionado = monthSpinner.getSelectedItemPosition() + 1;
                int añoSeleccionado = (int) parent.getItemAtPosition(position);
                filtrarContactosPorMes(mesSeleccionado, añoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada cuando no se selecciona ningún año
            }
        });
    }


    private void filtrarContactosPorMes(int mesSeleccionado, int añoSeleccionado) {
        // Limpiar la lista de contactos antes de llenarla con los contactos del mes seleccionado
        listaArrayContactos.clear();

        // Obtener todos los contactos de la base de datos
        DbContactos dbContactos = new DbContactos(MainActivity.this);
        //ArrayList<Contactos> todosLosContactos = dbContactos.mostrarContactos();

        double totalCost = 0.0;
        // Filtrar los contactos por el mes seleccionado
        for (Contactos contacto : todosLosContactos) {
            String fechaContacto = contacto.getFecha();
            if (fechaContacto != null && !fechaContacto.isEmpty()) {
                String[] partesFecha = fechaContacto.split("/");
                if (partesFecha.length == 3) {
                    int mesContacto = Integer.parseInt(partesFecha[1]);
                    int añoContacto = Integer.parseInt(partesFecha[2]);
                    if (mesContacto == mesSeleccionado && añoContacto == añoSeleccionado) {
                        listaArrayContactos.add(contacto);
                        // Sumar el costo del contacto al total
                        double costoContacto = Double.parseDouble(contacto.getCosto());
                        totalCost += costoContacto;
                    }
                }

                // Formatear el total como un número entero
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                String formattedTotal = decimalFormat.format(totalCost);

                // Actualizar el TextView del total con el valor de la suma formateado como un número entero
                totalCostTextView.setText("Total Cost: $" + formattedTotal);


                adapter.notifyDataSetChanged();
            }
        }

        // Notificar al adaptador que los datos han cambiado
        //listaContactos.getAdapter().notifyDataSetChanged();
    }



    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menuNuevo:
                nuevoRegistro();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void nuevoRegistro(){
        Intent intent = new Intent(this, NuevoActivity.class);
        startActivity(intent);
    }
}