package com.example.agendaperritos.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaperritos.R;
import com.example.agendaperritos.VerActivity;
import com.example.agendaperritos.entidades.Contactos;

import java.util.ArrayList;

public class ListaContactoAdapter extends RecyclerView.Adapter<ListaContactoAdapter.ContactoViewHolder> {
    ArrayList<Contactos> listaContactos;
    private int posicionAgendaMasProxima = -1;

    public ListaContactoAdapter(ArrayList<Contactos>listaContactos){
        this.listaContactos = listaContactos;

    }

    public void setPosicionAgendaMasProxima(int posicion) {
        posicionAgendaMasProxima = posicion;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ContactoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_item_contacto, null, false);
        return new ContactoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactoViewHolder holder, int position) {
        //Contactos contacto = listaContactos.get(position);
        holder.itemView.setBackgroundResource(R.drawable.item_background);

        holder.tvMascota.setText(listaContactos.get(position).getMascota());
        holder.tvNombre.setText("Dueño: " + listaContactos.get(position).getNombre());
        holder.tvFecha.setText(listaContactos.get(position).getFecha());
        holder.tvHora.setText(listaContactos.get(position).getHora());
        holder.tvCosto.setText("$ " + listaContactos.get(position).getCosto());

        if (position == posicionAgendaMasProxima) {
            // Cambiar el color de fondo del elemento si coincide con la agenda más próxima
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAgendaProxima));
        } else {
            // Restaurar el color de fondo predeterminado de los demás elementos
            holder.itemView.setBackgroundResource(R.drawable.item_background);
        }
    }
    @Override
    public int getItemCount() {
        return listaContactos.size();
    }

    public class ContactoViewHolder extends RecyclerView.ViewHolder {
        TextView tvMascota,tvNombre,tvFecha, tvHora, tvCosto;

        public ContactoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMascota= itemView.findViewById(R.id.tvMascota);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvFecha= itemView.findViewById(R.id.tvFecha);
            tvHora= itemView.findViewById(R.id.tvHora);
            tvCosto= itemView.findViewById(R.id.tvCosto);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = itemView.getContext();
                    Intent intent = new Intent(context, VerActivity.class);
                    intent.putExtra("ID", listaContactos.get(getAdapterPosition()).getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}
