package com.example.agendaperritos.actividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.agendaperritos.inicio.MainActivity;
import com.example.agendaperritos.R;
import com.example.agendaperritos.db.DbContactos;
import com.example.agendaperritos.db.DbImagenes;
import com.example.agendaperritos.entidades.Contactos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class nuevoCliente extends AppCompatActivity {

    EditText txtRegistro, txtNombre, txtTelefono,txtMascota,txtDireccion;
    Button btnGuardar, btnCompartir, btnModificar,btnEliminar;
    ImageButton btnRotarImagen, btnRotarImagen2;
    ImageView imgPerrito1, imgPerrito2;

    Contactos contacto;
    DbContactos dbContactos = new DbContactos(nuevoCliente.this);

    boolean correcto = false;
    int ids=0;
    private Bitmap selectedImageBitmap;
    private Bitmap selectedImageBitmap2;

    int imageSelected = 0;
    int calidadImagen = 60;
    String numDialog;

    private static final int CODIGO_SELECCIONAR_IMAGEN = 1;
    private static final int CODIGO_TOMAR_FOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_cliente);

        inicializateVariables();



        int siguienteRegistro = dbContactos.obtenerSiguienteRegistro();
        txtRegistro.setText(String.valueOf(siguienteRegistro));


        txtRegistro.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                String regis = txtRegistro.getText().toString();

                contacto = dbContactos.verContacto(Integer.parseInt(regis));
                if(contacto!=null){
                    String nombre = contacto.getNombre();
                    String mascota = contacto.getMascota();
                    String direccion = contacto.getDireccion();
                    String telefono = contacto.getTelefono();

                    // Obtén el BLOB de la imagen y conviértelo a Bitmap
                    byte[] imagenEnBytes1 = contacto.getImagen();
                    byte[] imagenEnBytes2 = contacto.getImagen2();

                    if (imagenEnBytes1 != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imagenEnBytes1, 0, imagenEnBytes1.length);
                        imgPerrito1.setImageBitmap(bitmap);
                        selectedImageBitmap = bitmap;
                    }
                    if(imagenEnBytes2 != null){
                        Bitmap bitmap2 = BitmapFactory.decodeByteArray(imagenEnBytes2, 0, imagenEnBytes2.length);
                        imgPerrito2.setImageBitmap(bitmap2);
                        selectedImageBitmap2 = bitmap2;
                    }
                    txtNombre.setText(nombre);
                    txtMascota.setText(mascota);
                    txtDireccion.setText(direccion);
                    txtTelefono.setText(telefono);
                    btnGuardar.setEnabled(false);
                    btnModificar.setEnabled(true);
                }else{
                    txtNombre.setText("");
                    txtMascota.setText("");
                    txtDireccion.setText("");
                    txtTelefono.setText("");
                    btnModificar.setEnabled(false);
                    btnGuardar.setEnabled(true);
                }
            }

        });


        //BOTONES PARA MODIFICAR CLIENTE
        btnCompartir.setOnClickListener(v -> lanzaComparte());
        btnGuardar.setOnClickListener(v -> {
            String registro = String.valueOf((txtRegistro.getText()));
            String nombre = convertirAPropio(String.valueOf(txtNombre.getText()));
            String mascota = convertirAPropio(String.valueOf(txtMascota.getText()));
            String direccion = convertirAPropio(String.valueOf(txtDireccion.getText()));
            String telefono = txtTelefono.getText().toString();

            byte[] imagen1, imagen2;

            if (selectedImageBitmap!=null) {
                Bitmap scaledBitmap = scaleBitmap(selectedImageBitmap, 800, 800); // Puedes ajustar el tamaño según tus necesidades
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, calidadImagen, stream);
                imagen1 = stream.toByteArray();
            }else{
                Drawable drawable = getResources().getDrawable(R.drawable.ic_logo_cyw);
                Bitmap defaultBitmap = ((BitmapDrawable) drawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                defaultBitmap.compress(Bitmap.CompressFormat.PNG, calidadImagen, stream);
                imagen1 = stream.toByteArray();
            }

            if(selectedImageBitmap2!=null){
                Bitmap scaledBitmap2 = scaleBitmap(selectedImageBitmap2, 800, 800); // Puedes ajustar el tamaño según tus necesidades
                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                scaledBitmap2.compress(Bitmap.CompressFormat.PNG, calidadImagen, stream2);
                imagen2 = stream2.toByteArray();
            }else{
                Drawable drawable = getResources().getDrawable(R.drawable.ic_logo_cyw);
                Bitmap defaultBitmap = ((BitmapDrawable) drawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                defaultBitmap.compress(Bitmap.CompressFormat.PNG, calidadImagen, stream);
                imagen2 = stream.toByteArray();
            }


            if(nombre.isEmpty() || mascota.isEmpty() ||direccion.isEmpty() ||telefono.isEmpty()) {
                Toast.makeText(nuevoCliente.this, "Complete todos los datos", Toast.LENGTH_SHORT).show();
            } else if (telefono.length()!=9) {
                Toast.makeText(nuevoCliente.this, "Introduzca los 9 digitos", Toast.LENGTH_SHORT).show();
            }
            else{
                long id = dbContactos.insertaContactoConImagen(registro, nombre,mascota,direccion,telefono, imagen1, imagen2);
                if (id>0){
                    Toast.makeText(nuevoCliente.this, "Registro Guardado con Exito", Toast.LENGTH_LONG).show();
                    ids = (int) id;
                    limpiar();
                    Intent intent = new Intent(nuevoCliente.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(nuevoCliente.this, "Error al Ingresar Contacto", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] imagen1, imagen2;

                if (selectedImageBitmap != null) {
                    // Escalar la imagen antes de guardarla para reducir su tamaño
                    Bitmap scaledBitmap = scaleBitmap(selectedImageBitmap, 800, 800); // Puedes ajustar el tamaño según tus necesidades
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    scaledBitmap.compress(Bitmap.CompressFormat.PNG, calidadImagen, stream);
                    imagen1 = stream.toByteArray();
                }else {
                    // Si no hay imagen seleccionada, utiliza la imagen predeterminada del recurso drawable
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_logo_cyw);
                    Bitmap defaultBitmap = ((BitmapDrawable) drawable).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    defaultBitmap.compress(Bitmap.CompressFormat.PNG, calidadImagen, stream);
                    imagen1 = stream.toByteArray();
                }
                if(selectedImageBitmap2 != null){
                    Bitmap scaledBitmap2 = scaleBitmap(selectedImageBitmap2, 800, 800); // Puedes ajustar el tamaño según tus necesidades
                    ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                    scaledBitmap2.compress(Bitmap.CompressFormat.PNG, calidadImagen, stream2);
                    imagen2 = stream2.toByteArray();
                }else{
                    // Si no hay imagen seleccionada, utiliza la imagen predeterminada del recurso drawable
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_logo_cyw);
                    Bitmap defaultBitmap = ((BitmapDrawable) drawable).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    defaultBitmap.compress(Bitmap.CompressFormat.PNG, calidadImagen, stream);
                    imagen2 = stream.toByteArray();
                }

                if(!txtRegistro.getText().toString().equals("") &&!txtNombre.getText().toString().equals("") &&!txtMascota.getText().toString().equals("") &&!txtDireccion.getText().toString().equals("") && !txtTelefono.getText().toString().equals("")){
                    correcto = dbContactos.editarContactoConImagen(txtRegistro.getText().toString(),txtNombre.getText().toString(),txtMascota.getText().toString(),txtDireccion.getText().toString(), imagen1,imagen2 ,txtTelefono.getText().toString());

                    if(correcto){
                        Toast.makeText(nuevoCliente.this, "Registro Modificado", Toast.LENGTH_LONG).show();
                        verMain();
                    }else{
                        Toast.makeText(nuevoCliente.this, "Registro Fallo", Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(nuevoCliente.this, "Llene los campos", Toast.LENGTH_LONG).show();
                }

            }
        });
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean eliminacionExitosa = dbContactos.eliminarContacto(txtRegistro.getText().toString());
                if (eliminacionExitosa) {
                    Toast.makeText(nuevoCliente.this, "Registro eliminado correctamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(nuevoCliente.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(nuevoCliente.this, "Error al eliminar el registro", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //BOTONES PARA MODIFICAR IMAGEN
        btnRotarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedImageBitmap != null) {
                    // Definir la matriz de rotación
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90); // Puedes ajustar el ángulo de rotación según tus necesidades

                    // Aplicar la rotación a la imagen
                    selectedImageBitmap = Bitmap.createBitmap(selectedImageBitmap, 0, 0, selectedImageBitmap.getWidth(), selectedImageBitmap.getHeight(), matrix, true);

                    // Actualizar la imagen en tu vista
                    // Asegúrate de que rvGaleriaImagenes es la vista correcta que contiene la imagen
                    ImageView imageView = imgPerrito1.findViewById(R.id.imgPerrito1);
                    if (imageView != null) {
                        imageView.setImageBitmap(selectedImageBitmap);
                    }
                }
            }
        });
        btnRotarImagen2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedImageBitmap2 != null) {
                    // Definir la matriz de rotación
                    Matrix matrix2 = new Matrix();
                    matrix2.postRotate(90); // Puedes ajustar el ángulo de rotación según tus necesidades

                    // Aplicar la rotación a la imagen
                    selectedImageBitmap2 = Bitmap.createBitmap(selectedImageBitmap2, 0, 0, selectedImageBitmap2.getWidth(), selectedImageBitmap2.getHeight(), matrix2, true);

                    // Actualizar la imagen en tu vista
                    // Asegúrate de que rvGaleriaImagenes es la vista correcta que contiene la imagen
                    ImageView imageView2 = imgPerrito2.findViewById(R.id.imgPerrito2);
                    if (imageView2 != null) {
                        imageView2.setImageBitmap(selectedImageBitmap2);
                    }
                }
            }
        });

        imgPerrito1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numDialog = "1";
                showAlertDialog(numDialog);
            }
        });
        imgPerrito2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numDialog = "2";
                showAlertDialog(numDialog);
            }
        });
    }

    private void obtenerImagenDesdeCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CODIGO_TOMAR_FOTO);
        }
    }
    private void obtenerImagenDesdeGaleria() {
        Intent galeriaIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galeriaIntent, CODIGO_SELECCIONAR_IMAGEN);
    }

    private void mostrarImagen(Bitmap bitmap) {
        imgPerrito1.setImageBitmap(bitmap);
    }
    private Bitmap obtenerBitmapDesdeImagenUri(Uri imagenUri) {
        try {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagenUri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mostrarImagen2(Bitmap bitmap2) {
        imgPerrito2.setImageBitmap(bitmap2);
    }
    private Bitmap obtenerBitmapDesdeImagenUri2(Uri imagenUri) {
        try {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagenUri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CODIGO_SELECCIONAR_IMAGEN && data != null) {

                if (imageSelected == 1) {
                    // Acciones para la imagen 1
                    Uri imagenUri = data.getData();
                    Bitmap bitmap = obtenerBitmapDesdeImagenUri(imagenUri);
                    if (bitmap != null) {
                        selectedImageBitmap = bitmap;
                        mostrarImagen(bitmap);
                    } else {
                        Toast.makeText(this, "Error al obtener el bitmap de la imagen seleccionada", Toast.LENGTH_SHORT).show();
                    }
                } else if (imageSelected == 2) {
                    // Acciones para la imagen 2
                    Uri imagenUri2 = data.getData();
                    Bitmap bitmap2 = obtenerBitmapDesdeImagenUri2(imagenUri2);
                    if (bitmap2 != null) {
                        selectedImageBitmap2 = bitmap2;
                        mostrarImagen2(bitmap2);
                    } else {
                        Toast.makeText(this, "Error al obtener el bitmap de la imagen seleccionada", Toast.LENGTH_SHORT).show();
                    }
                }

            } else if (requestCode == CODIGO_TOMAR_FOTO && data != null) {

                if (imageSelected == 1) {
                    // Acciones para la imagen 1
                    Bundle extras = data.getExtras();
                    Bitmap bitmap = (Bitmap) extras.get("data");
                    if (bitmap != null) {
                        selectedImageBitmap = bitmap;
                        mostrarImagen(bitmap);
                    } else {
                        Toast.makeText(this, "Error al obtener el bitmap de la foto tomada", Toast.LENGTH_SHORT).show();
                    }
                } else if (imageSelected == 2) {
                    // Acciones para la imagen 2
                    Bundle extras2 = data.getExtras();
                    Bitmap bitmap2 = (Bitmap) extras2.get("data");
                    if (bitmap2 != null) {
                        selectedImageBitmap2 = bitmap2;
                        mostrarImagen2(bitmap2);
                    } else {
                        Toast.makeText(this, "Error al obtener el bitmap de la foto tomada", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            Toast.makeText(this, "Se canceló la operación", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_TOMAR_FOTO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, ahora puedes lanzar la intención de la cámara
                obtenerImagenDesdeCamara();
            } else {
                // Permiso denegado, muestra un mensaje o toma acciones apropiadas
                Toast.makeText(this, "Se necesita el permiso de la cámara para tomar fotos", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void inicializateVariables() {
        txtRegistro = findViewById(R.id.txtRegistro);
        txtNombre = findViewById(R.id.txtNombre);
        txtMascota= findViewById(R.id.txtMascota);
        txtDireccion= findViewById(R.id.txtDireccion);
        txtTelefono= findViewById(R.id.txtTelefono);

        //PARA ACCIONES DE CLIENTE
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnCompartir = findViewById(R.id.btncompartir);
        btnModificar =findViewById(R.id.btnModificar);

        //PARA TOMAR IMAGEN
        btnRotarImagen = findViewById(R.id.btnRotarImagen);
        imgPerrito1 = findViewById(R.id.imgPerrito1);
        btnRotarImagen2 = findViewById(R.id.btnRotarImagen2);
        imgPerrito2 = findViewById(R.id.imgPerrito2);
    }
    private void verMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null) {
            return null; // Manejar caso de un Bitmap nulo
        }

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        // Calcular las nuevas dimensiones manteniendo la proporción
        float aspectRatio = (float) originalWidth / originalHeight;
        int newWidth = maxWidth;
        int newHeight = Math.round(maxWidth / aspectRatio);

        // Si la altura calculada es mayor que maxHeight, ajustar las dimensiones
        if (newHeight > maxHeight) {
            newHeight = maxHeight;
            newWidth = Math.round(maxHeight * aspectRatio);
        }

        // Escalar el bitmap
        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

        // Recortar el bitmap al tamaño deseado
        int x = 0;
        int y = 0;

        // Ajustar las coordenadas de recorte si el ancho o alto son mayores que los valores máximos
        if (newWidth > maxWidth) {
            x = (newWidth - maxWidth) / 2;
        }
        if (newHeight > maxHeight) {
            y = (newHeight - maxHeight) / 2;
        }

        // Asegurarse de que las coordenadas de recorte no excedan las dimensiones del bitmap
        x = Math.max(0, x);
        y = Math.max(0, y);
        int cropWidth = Math.min(maxWidth, newWidth);
        int cropHeight = Math.min(maxHeight, newHeight);

        // Crear el nuevo bitmap recortado
        return Bitmap.createBitmap(bitmap, x, y, cropWidth, cropHeight);
    }
    private void limpiar(){
        txtRegistro.setText("");
        txtNombre.setText("");
        txtMascota.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        imgPerrito1.setImageResource(R.drawable.ic_logo_cyw);
        imgPerrito2.setImageResource(R.drawable.ic_logo_cyw);// Establece la imagen en blanco
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

    private void showAlertDialog(String numDialog) { //ACA PODRIA RECIBIR EL ID DESDE EL LIASARTAIMAGENESADAPTER Y MOSTRARLA

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_muestra, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        Button closeButton, deletButton, cameraButton, fileButton;

        closeButton = view.findViewById(R.id.Btn_cerrar_imagen);
        deletButton = view.findViewById(R.id.Btn_eliminar_imagen);
        cameraButton = view.findViewById(R.id.Btn_camara_imagen);
        fileButton = view.findViewById(R.id.Btn_archivo_imagen);

        AlertDialog dialog = builder.create();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        deletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(nuevoCliente.this);
                confirmDialogBuilder.setTitle("Confirmar Eliminación");
                confirmDialogBuilder.setMessage("¿Estás seguro de que quieres eliminar esta imagen?");

                confirmDialogBuilder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Si el usuario hace clic en "Sí", elimina la imagen
                        int idRegistro = contacto.getId();
                        DbContactos dbContactos = new DbContactos(nuevoCliente.this);
                        String imgDelete = null;
                        if (numDialog=="1"){
                            imgDelete = "imagen";
                        }else if(numDialog == "2"){
                            imgDelete = "imagen2";
                        }

                        correcto = dbContactos.eliminarContactoConId(String.valueOf(idRegistro), imgDelete);
                        Toast.makeText(nuevoCliente.this, "Se elimino perfectamente", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        verMain();
                    }
                });

                confirmDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        salirActivity();
                    }
                });
                // Mostrar el cuadro de diálogo de confirmación
                AlertDialog confirmDialog = confirmDialogBuilder.create();
                confirmDialog.show();
            }
        });
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSelected =Integer.parseInt(numDialog);
                obtenerImagenDesdeCamara();
                dialog.dismiss();
            }
        });
        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSelected = Integer.parseInt(numDialog);
                obtenerImagenDesdeGaleria();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void salirActivity() {
        Intent intent = new Intent(nuevoCliente.this, nuevoCliente.class);
        startActivity(intent);

    }
}