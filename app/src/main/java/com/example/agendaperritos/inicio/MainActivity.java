package com.example.agendaperritos.inicio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agendaperritos.db.DbHelper;
import com.example.agendaperritos.diseños.LineItemDecoration;
import com.example.agendaperritos.NotificationUtils;
import com.example.agendaperritos.R;
import com.example.agendaperritos.actividades.EliminadoDatos;
import com.example.agendaperritos.actividades.NuevaCita;
import com.example.agendaperritos.actividades.nuevoCliente;
import com.example.agendaperritos.adaptadores.ListaContactoAdapter;
import com.example.agendaperritos.db.DbContactos;
import com.example.agendaperritos.entidades.Contactos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
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
    String rutaArchivoCSV = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/agenda.csv";
    DbContactos dbContactos = new DbContactos(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        
        // CARGAR SIN COMENTAR Y SOBRE LA MISMA COMENTAR Y VOLVER A CARGAR
        //cargarDatos();
        //cargarDatosListos();

        if (!verificarArchivoCargado(getApplicationContext())) {
            // Si los datos aún no se han cargado, carga los datos

            cargarDatos();
            cargarDatosListos();
            
            // Marca la bandera indicando que los datos han sido cargados
            marcarDatosComoCargados(getApplicationContext());
        }

        listaContactos = findViewById(R.id.listaContactos);

        listaContactos.setLayoutManager(new LinearLayoutManager(this));

        LineItemDecoration lineDecoration = new LineItemDecoration(this);
        listaContactos.addItemDecoration(lineDecoration);

        totalCostTextView = findViewById(R.id.totalCostTextView); // Enlaza con el TextView del total

        try {
            listaArrayContactos = dbContactos.mostrarContactos(); // Mostrar todos los contactos por defecto
            todosLosContactos = dbContactos.mostrarContactos();
        }catch (Exception e){
            Log.e("Error", "Error al mostrar contactos: " + Log.getStackTraceString(e));
            Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
        }

        Collections.reverse(listaArrayContactos);

        adapter = new ListaContactoAdapter(listaArrayContactos);
        listaContactos.setAdapter(adapter);

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

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
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

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menuNuevoCliente:
                nuevoRegistroCliente();
                return true;
            case R.id.menuNuevaCita:
                nuevoRegistro();
                return true;
            case R.id.menuDatos:
                compartirDatos();
                return true;
            case R.id.menuPromocion:
                compartirPromociones();
                return true;
            case R.id.menuSalir:
                salir();
                return true;
            case R.id.menuGaleriaImagenes:
                //galeriaImagenes();
                return true;
            case R.id.menuEliminarDatosGaleria:
                eliminaGaleria();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void eliminaGaleria() {
        Intent intent = new Intent(this, EliminadoDatos.class);
        startActivity(intent);
    }

    private void salir() {
        cerrarAplicacion();
    }

    private void nuevoRegistro(){
        Intent intent = new Intent(this, NuevaCita.class);
        startActivity(intent);
    }

    private void nuevoRegistroCliente(){
        Intent intent = new Intent(this, nuevoCliente.class);
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
        todosLosContactos = dbContactos.mostrarCitasPromo();

        // Obtener el número de teléfono del contacto actual
        HashMap<String, Integer> frecuencanumRegistro = new HashMap<>();

        // Iterar sobre todos los contactos
        for (Contactos contacto : todosLosContactos) {
            // Obtener el número de teléfono del contacto actual
            String numeroRegistroMascota = contacto.getRegistro();

            // Incrementar la frecuencia del número en el diccionario
            Integer frecuenciaActual = frecuencanumRegistro.get(numeroRegistroMascota);
            frecuenciaActual = (frecuenciaActual == null) ? 0 : frecuenciaActual;
            frecuencanumRegistro.put(numeroRegistroMascota, frecuenciaActual + 1);
        }

        // Identificar números con frecuencia mayor o igual a 5
        for (Map.Entry<String, Integer> entry : frecuencanumRegistro.entrySet()) {
            String numeroTelefono = entry.getKey();
            int frecuencia = entry.getValue();

            Contactos contacto = dbContactos.verContacto(Integer.parseInt(numeroTelefono));
            String nuevonumero = contacto.getTelefono();
            Toast.makeText(this, nuevonumero, Toast.LENGTH_LONG).show();

            if (frecuencia >= 2 && Integer.parseInt(nuevonumero) > 0) {
                String mensajeDescuento;
                if (frecuencia >= 1 && frecuencia < 4) {
                    mensajeDescuento = "obten un 25% de descuento en la 4° visita.";
                } else if (frecuencia >= 4 && frecuencia < 8) {
                    mensajeDescuento = "obten un 50% de descuento en la 8° visita.";
                } else {
                    mensajeDescuento = "obten completamente GRATIS, la numero 12°";
                }
                String lineaHorizontal = " "; // Puedes ajustar la longitud según tus necesidades

                String mensaje = "PROMOCIONES A CLIENTES CONSTANTES" +
                        "\n" + lineaHorizontal +
                        "\nHola, Te esperamos en ★COSMO & WANDA★ perruqueria." +
                        "\nPor tus " + frecuencia + " visitas, " + mensajeDescuento +
                        "\n" + lineaHorizontal +
                        "\n\uD83D\uDC36¡Hazte parte de nuestra familia!✂️";
                try {
                    // Codificar el mensaje para asegurar la correcta transmisión en la URI
                    mensaje = URLEncoder.encode(mensaje, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                // Crear un intent para abrir WhatsApp y compartir el mensaje
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_VIEW);
                String uri = "https://api.whatsapp.com/send/?phone=56"+ nuevonumero + "&text=" + mensaje;
                sendIntent.setData(Uri.parse(uri));
                startActivity(sendIntent);
            }
        }
    }

    public void exportarCSV(ArrayList<Contactos> todosLosContactos, String rutaArchivo) {

        try {
            FileWriter fw = new FileWriter(rutaArchivo);
            BufferedWriter bw = new BufferedWriter(fw);

            // Escribir encabezados
            bw.write("id,nombre,mascota,direccion,telefono,fecha,hora,costo");
            bw.newLine();

            // Escribir datos
            for (Contactos contacto : todosLosContactos) {
                bw.write(contacto.getId() + ","
                        + contacto.getNombre() + ","
                        + contacto.getMascota() + ","
                        + contacto.getDireccion() + ","
                        + contacto.getTelefono() + ","
                        + contacto.getFecha() + ","
                        + contacto.getHora() + ","
                        + contacto.getCosto());
                bw.newLine();
            }

            bw.close();
            fw.close();
            Toast.makeText(this, "Exportacion exitosa", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error"+ e, Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarDatos() {
        DbContactos dbContactos = new DbContactos(MainActivity.this);
        SQLiteDatabase db = dbContactos.getWritableDatabase();

        // Inserta tus datos en la base de datos
        db.beginTransaction();
        try {
            // Aquí realiza tus inserciones, por ejemplo:
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('1','Adela Gonzalez','Pepita','Guanabara 2378','982641442');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('2','Alejandra Mendoza','Shaggy','Baron De Juras Reales 3507','966349111');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('3','Ana Castillo','Behily','Isla De Esepcion','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('4','Andrea','Estrella','Roma 2537','972324423');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('5','Antonia ','Bruno','Carlos Spano','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('6','Antonia ','Canela ','Carlos Spano','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('7','Carla Soto','Coffie','Costa Rica 3569','939492193');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('8','Carmen Vargas','Peluchin','Olivo Pasajefinlandia','944020274');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('9','Claudia Riveros','Bambino','Me','933553321');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('10','Daniel','Oso','Costa Rica','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('11','Daniela Pavez','Martina ','Parral2406','979556099');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('12','Daniela Pavez','Morita','Parral 2406','979556099');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('13','Daniela Pavez','Toto','Parral 2406','979556099');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('14','Daniela Urzua','Boby','Monterrey','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('15','Elizabet','Canela','Reina Maria2164','978883767');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('16','Elizabet Gonzales','Canela','Reina Maria 2163','978883767');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('17','Elizabet Rodriguez','Bulla','Costa Rica','968057992');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('18','Elizabet Rodriguez','Flor','Costa Rica','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('19','Ester Lopez','Hany','Monterrey 2686','976464903');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('20','Fabiana','Lucky','Monterrey 2801','935712810');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('21','Flavia','Benito','Costa Rica','958468112');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('22','Francis','Sami','Viena 2698','946353786');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('23','Franco','Nico','Costa Rica','935697182');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('24','Gabriel','Lady','Delfos 2525','940090467');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('25','Gabriela','Estrella','Roma','974065950');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('26','Gabriela','Kein','Roma','974065950');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('27','Harry Pavez','Sofia','Monterrey','920018119');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('28','Hermana Sr Maribel Taller','Blanca','Roma ','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('29','Hermana Sr Maribel Taller','Roco','Roma','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('30','Ivon Bravo','Pluton','Monterrey 2179','994962191');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('31','Javier ','Inti','Viena ','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('32','Jonathan Valenzuela','Chash','Monseñor Muller 3682','954072262');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('33','Jorge Diaz Areas','Toffe','La Primavera 3321','957172815');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('34','Karen Concha','Perla ','Pasaje Pedro Games 2025','942672693');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('35','Karen Gutierres','Lucas','Mar Del Sur 3542','920120351');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('36','Karin Gutierres','Kira','Mar Del Sur 2542','920120851');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('37','Katerine Vasquez','Slinky','Mar Del Sur 3575','974877022');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('38','Katia Cartagena','Boby','Monterrey 2801','991805503');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('39','Katia Cartagena','Dexter','Monterrey 2801','991805503');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('40','Laly','Toby','Viena 2434','946398225');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('41','Lili','Toby','Viena 2434','946398225');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('42','Liset','Akira','Viena 2168','979651789');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('43','Luis Vargas ','Niño','Av La Paz','966145449');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('44','Malena','Roco','Carlos Spano Con Baron','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('45','Manuel','Pelusa','Roma 2555','962798236');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('46','Marcia Cortes','Relmu','Viena 2561','993228239');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('47','Marco','Soraya','Vivaceta','933648416');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('48','Marcos Bustamante','Lilo','Calle','998397620');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('49','Margarita Alegria','Kandy','Ventura Laureda ','969009798');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('50','Margarita Dias','Princesa','Monterrey 2874','997389558');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('51','Maria','Lilo','Mar De Las Antillas3634','959007246');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('52','Maria Ignacia','Jacinta','Andres Marambio 3779','975216542');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('53','Maria Jose','Roki','Costa Rica','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('54','Maribel Taller','Lucero','Roma 2181','959115311');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('55','Marlen','Lolo','Cañere','933701010');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('56','Marlen','Tadeo','Cañete 2050 Independencia','933701010');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('57','Mauro Valenzuela','Deicy','Roma 2727','947841854');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('58','Nancy Ahumada','Simon','Primavera 3325','998608527');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('59','Nancy Haumada','Oso','Primavera 3325','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('60','Patricio Valencia','Perlita','14 De La Fama 2698','930516414');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('61','Pia','Bruna','Monterrey 1801','984058635');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('62','S','Mick','Ventura Laureda','998397620');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('63','Sandra ','Peluzs','Renca','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('64','Sara','Bruno','Monter','982760452');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('65','Sebastian ','Helios','Monterrey','964216239');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('66','Sofia','Princesa','Carlos Spano Vecina Del Frente Antonia','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('67','Solange','Chica','Mar De Las Antillas 3562','935220389');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('68','Solange','Federico','Andres Marambio','962400518');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('69','Teraldine','Beto','Puntiagudo 5351','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('70','Tia De Rodri ','Tadeo','Costa Rica','978258368');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('71','Vecina','Keila','Carlos Spano','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('72','Vecina Antonia','Flor','Carlos Spano','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('73','Vecina Betzabet','Pelusa','Monterrey','978170781');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('74','Vecina Del Frente','Keila','Carlos Spano','982912209');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('75','Vecina Del Frente Antonia','Bruno','Carlos Spano','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('76','Vecina Del Furgon','S','Costa Rica','966349111');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('77','Vecina Paty','Mily','Carlos Spano','981276398');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('78','Vecina Paty','Moly','Carlos Spano','981276398');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('79','Viviana ','Mia ','Monterrey','0');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('80','Olga','Mate','Monterrey 2191','945120846');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('81','Cecilia Cardenas','Bruna','Monterrey 1801','984058635');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('82','Mama De Señora De Carniceria','Canela ','Alberto Gonzales Dorsal','227347950');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('83','Rosa Muños','Boby','Viena 2681','941990969');");
            db.execSQL("INSERT INTO t_contactos (registro, nombre, mascota, direccion, telefono) VALUES ('84','Marcos Figueroa','Noa','Vivaceta','933648416');");




            // ... continua con los demás datos
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void cargarDatosListos() {
        DbContactos dbContactos = new DbContactos(MainActivity.this);
        SQLiteDatabase db = dbContactos.getWritableDatabase();

        // Inserta tus datos en la base de datos
        db.beginTransaction();
        try {
            // Aquí realiza tus inserciones, por ejemplo:
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('65','Sebastian ','Helios','01/09/2023','13:29','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('36','Karin Gutierres','Kira','01/09/2023','16:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('16','Elizabet Gonzales','Canela','01/09/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('14','Daniela Urzua','Boby','01/11/2023','17:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('21','Flavia','Benito','01/12/2023','14:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('24','Gabriel','Lady','02/08/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('71','Vecina','Keila','02/09/2023','16:30','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('4','Andrea','Estrella','03/08/2023','17:00','30000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('69','Teraldine','Beto','03/08/2023','17:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('54','Maribel Taller','Lucero','03/12/2023','10:02','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('47','Marco','Soraya','03/12/2023','12:00','30000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('51','Maria','Lilo','04/08/2023','17:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('5','Antonia ','Bruno','04/08/2023','10:007','7000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('13','Daniela Pavez','Toto','04/10/2023','14:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('20','Fabiana','Lucky','04/12/2023','15:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('34','Karen Concha','Perla ','05/10/2023','15:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('57','Mauro Valenzuela','Deicy','05/12/2023','14:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('72','Vecina Antonia','Flor','05/12/2023','17:00','25000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('11','Daniela Pavez','Martina ','06/10/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('62','S','Mick','06/12/2023','12:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('76','Vecina Del Furgon','S','06/12/2023','15:01','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('48','Marcos Bustamante','Lilo','07/08/2023','15:30','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('23','Franco','Nico','07/12/2023','17:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('17','Elizabet Rodriguez','Bulla','08/11/2023','14:02','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('18','Elizabet Rodriguez','Flor','08/11/2023','16:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('44','Malena','Roco','09/08/2023','17:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('41','Lili','Toby','09/12/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('63','Sandra ','Peluzs','10/09/2023','16:00','13000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('78','Vecina Paty','Moly','10/10/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('77','Vecina Paty','Mily','10/10/2023','12:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('68','Solange','Federico','10/11/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('82','Mama De Señora De Carniceria','Canela ','11/08/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('1','Adela Gonzalez','Pepita','11/09/2023','16:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('8','Carmen Vargas','Peluchin','11/10/2023','16:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('38','Katia Cartagena','Boby','12/10/2023','10:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('39','Katia Cartagena','Dexter','12/10/2023','12:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('15','Elizabet','Canela','12/12/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('52','Maria Ignacia','Jacinta','12/12/2023','16:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('27','Harry Pavez','Sofia','13/09/2023','16:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('83','Rosa Muños','Boby','13/10/2023','16:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('19','Ester Lopez','Hany','13/11/2023','14:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('38','Katia Cartagena','Boby','13/12/2023','10:01','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('39','Katia Cartagena','Dexter','13/12/2023','12:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('66','Sofia','Princesa','14/08/2023','10:00','25000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('75','Vecina Del Frente Antonia','Bruno','14/09/2023','10:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('22','Francis','Sami','14/09/2023','16:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('46','Marcia Cortes','Relmu','14/11/2023','16:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('80','Olga','Mate','14/12/2023','10:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('6','Antonia ','Canela ','15/08/2023','17:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('33','Jorge Diaz Areas','Toffe','15/11/2023','10:00','13000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('64','Sara','Bruno','15/11/2023','16:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('60','Patricio Valencia','Perlita','15/12/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('70','Tia De Rodri ','Tadeo','16/09/2023','13:29','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('73','Vecina Betzabet','Pelusa','16/09/2023','10:00','13000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('54','Maribel Taller','Lucero','16/09/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('81','Cecilia Cardenas','Bruna','16/12/2023','10:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('67','Solange','Chica','18/10/2023','15:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('32','Jonathan Valenzuela','Chash','18/11/2023','09:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('56','Marlen','Tadeo','19/10/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('7','Carla Soto','Coffie','19/10/2023','17:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('28','Hermana Sr Maribel Taller','Blanca','20/09/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('55','Marlen','Lolo','20/10/2023','14:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('40','Laly','Toby','21/08/2023','17:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('29','Hermana Sr Maribel Taller','Roco','21/09/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('37','Katerine Vasquez','Slinky','21/11/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('52','Maria Ignacia','Jacinta','22/08/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('53','Maria Jose','Roki','22/10/2023','10:00','30000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('61','Pia','Bruna','23/08/2023','17:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('58','Nancy Ahumada','Simon','23/10/2023','14:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('59','Nancy Haumada','Oso','23/10/2023','16:15','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('2','Alejandra Mendoza','Shaggy','24/08/2023','17:30','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('52','Maria Ignacia','Jacinta','24/10/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('35','Karen Gutierres','Lucas','25/08/2023','10:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('26','Gabriela','Kein','25/11/2023','10:01','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('25','Gabriela','Estrella','25/11/2023','12:01','30000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('49','Margarita Alegria','Kandy','26/09/2023','14:00','20000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('74','Vecina Del Frente','Keila','26/10/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('45','Manuel','Pelusa','26/10/2023','16:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('84','Marcos Figueroa','Noa','26/11/2023','10:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('79','Viviana ','Mia ','27/09/2023','11:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('50','Margarita Dias','Princesa','27/10/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('30','Ivon Bravo','Pluton','27/10/2023','14:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('3','Ana Castillo','Behily','28/08/2023','18:01','30000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('42','Liset','Akira','28/09/2023','10:00','25000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('31','Javier ','Inti','28/09/2023','16:00','20000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('12','Daniela Pavez','Morita','28/10/2023','10:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('10','Daniel','Oso','29/09/2023','17:00','20000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('9','Claudia Riveros','Bambino','29/11/2023','16:00','18000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('24','Gabriel','Lady','30/11/2023','15:00','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('60','Patricio Valencia','Perlita','31/08/2023','13:29','15000');");
            db.execSQL("INSERT INTO t_citas (registro, nombre, mascota, fecha, hora, costo) VALUES ('43','Luis Vargas ','Niño','31/08/2023','16:00','15000');");



            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private boolean verificarArchivoCargado(Context context) {

        boolean datosCargados = false;
        File archivo = new File(context.getFilesDir(),"archivo.txt");  // Reemplaza con la ruta correcta

        if (archivo.exists()) {
            // Si el archivo existe, lee su contenido y determina si los datos están cargados
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea;
                if ((linea = br.readLine()) != null) {
                    datosCargados = Boolean.parseBoolean(linea.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Si el archivo no existe, créalo y establece la bandera en false
            try (FileWriter fw = new FileWriter(archivo)) {
                fw.write("false");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return datosCargados;
    }

    private void marcarDatosComoCargados(Context context) {
        File archivo = new File(context.getFilesDir(),"archivo.txt");  // Reemplaza con la ruta correcta

        // Marca la bandera indicando que los datos han sido cargados
        try (FileWriter fw = new FileWriter(archivo)) {
            fw.write("true");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cerrarAplicacion() {
        Intent intent = new Intent(this, Logeo_usuario.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("EXIT", false)) {
            finish();
        }
    }
}
