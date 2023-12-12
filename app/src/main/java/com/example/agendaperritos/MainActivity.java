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
import android.net.Uri;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
            SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String fechaHoraString = fechaContacto + " " + horaContacto;
            try {
                Date fechaHoraContacto = sd.parse(fechaHoraString);
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
            case R.id.menuDatos:
                compartirDatos();
                return true;
            case R.id.menuPromocion:
                compartirPromociones();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void nuevoRegistro(){
        Intent intent = new Intent(this, NuevoActivity.class);
        startActivity(intent);
    }

    private void compartirDatos(){
        String numeroTelefono = "+56991831880";
        String lineaHorizontal = " "; // Puedes ajustar la longitud según tus necesidades

        // Texto que deseas compartir
        String mensaje = "DATOS SOLICITADOS PARA TOMAR CITAS POR MAIL:" +
                "\n" + lineaHorizontal +
                "\nNombre Dueño:"+
                "\nNombre Mascota:"+
                "\nDireccion:"+
                "\nnumero de Telefono:"+
                "\nServicio requerido:"+
                "\n" + lineaHorizontal +
                "\nFecha y Hora de preferencia, apenas tengamos agenda te devolveremos el mensaje";

        // Crear un intent para abrir WhatsApp y compartir el mensaje
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_VIEW);
        String uri = "whatsapp://send?phone=" + numeroTelefono + "&text=" + mensaje;
        sendIntent.setData(Uri.parse(uri));
        startActivity(sendIntent);
    }

    private void compartirPromociones() {
        DbContactos dbContactos = new DbContactos(MainActivity.this);
        todosLosContactos = dbContactos.mostrarContactos();

        // Obtener el número de teléfono del contacto actual
        HashMap<String, Integer> frecuenciaNumeros = new HashMap<>();
        String nombreMascota = " ";

        // Iterar sobre todos los contactos
        for (Contactos contacto : todosLosContactos) {
            // Obtener el número de teléfono del contacto actual
            String numeroTelefono = contacto.getTelefono();
            nombreMascota = contacto.getMascota();

            // Incrementar la frecuencia del número en el diccionario
            Integer frecuenciaActual = frecuenciaNumeros.get(numeroTelefono);
            frecuenciaActual = (frecuenciaActual == null) ? 0 : frecuenciaActual;
            frecuenciaNumeros.put(numeroTelefono, frecuenciaActual + 1);

        }

        // Imprimir la frecuencia de los números de teléfono
        for (Map.Entry<String, Integer> entry : frecuenciaNumeros.entrySet()) {

            String numeroTelefono = entry.getKey();
            int frecuencia = entry.getValue();
            String mensajeDescuento;

            if (frecuencia >= 1 && frecuencia < 4) {
                mensajeDescuento = "Cuando cumpla 4 visitas, tendra un descuento de un 25%";
            } else if (frecuencia >= 4 && frecuencia < 8) {
                mensajeDescuento = "Cuando cumpla 8 visitas, tendra un descuento de un 50%";
            } else {
                mensajeDescuento = "Su visita numero 12 sera completamente GRATUITA!!!";
            }

            String lineaHorizontal = " "; // Puedes ajustar la longitud según tus necesidades

            String mensaje = "PROMOCIONES A CLIENTES CONSTANTES" +
                    "\n" + lineaHorizontal +
                    "\nPara el contacto " + numeroTelefono + " le tenemos en perruqueria ★COSMO & WANDA★, una promoción a su medida" +
                    "\nTiene: " + frecuencia + " visita(s) con nosotros" +
                    "\n" + mensajeDescuento +
                    "\n" + lineaHorizontal +
                    "\nNo dejes de venir y ser parte de la familia ★COSMO & WANDA★";

            try {
                // Codificar el mensaje para asegurar la correcta transmisión en la URI
                mensaje = URLEncoder.encode(mensaje, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Crear un intent para abrir WhatsApp y compartir el mensaje
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_VIEW);
            String uri = "whatsapp://send?phone=" + numeroTelefono + "&text=" + mensaje;
            sendIntent.setData(Uri.parse(uri));
            startActivity(sendIntent);
        }
    }
}