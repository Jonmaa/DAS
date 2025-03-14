package com.example.trabajodas;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PokemonCardAdapter extends RecyclerView.Adapter<PokemonCardAdapter.ViewHolder> {
    private List<PokemonCard> pokemonCardList;
    private DatabaseHelper databaseHelper;

    public PokemonCardAdapter(List<PokemonCard> pokemonCardList, DatabaseHelper databaseHelper) {
        this.pokemonCardList = pokemonCardList;
        this.databaseHelper = databaseHelper;
    }

    // Crear la vista de la carta
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PokemonCard card = pokemonCardList.get(position);
        holder.nameTextView.setText(card.getName());
        holder.priceTextView.setText(String.valueOf(card.getPrice()));

        // Cargar imagen
        if (card.getImagePath() != null && !card.getImagePath().isEmpty()) {
            File imgFile = new File(card.getImagePath());
            if (imgFile.exists()) {
                holder.pokemonImageView.setImageURI(Uri.fromFile(imgFile));
            } else {
                holder.pokemonImageView.setImageResource(R.drawable.default_image); // Imagen por defecto si el archivo no existe
            }
        } else {
            holder.pokemonImageView.setImageResource(R.drawable.default_image); // Imagen por defecto si no hay ruta
        }

        // Click largo para eliminar la carta
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle(v.getContext().getString(R.string.delete_card))
                    .setMessage(v.getContext().getString(R.string.are_you_sure))
                    .setPositiveButton(v.getContext().getString(R.string.yes), (dialog, which) -> {
                        databaseHelper.deleteCard(card.getName());
                        pokemonCardList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

        // Click para editar la carta
        holder.itemView.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_edit_card, null);
            EditText editName = dialogView.findViewById(R.id.editName);
            EditText editPrice = dialogView.findViewById(R.id.editPrice);
            editName.setText(card.getName());
            editPrice.setText(String.valueOf(card.getPrice()));

            new AlertDialog.Builder(v.getContext())
                    .setTitle(v.getContext().getString(R.string.edit_card))
                    .setView(dialogView)
                    .setPositiveButton(v.getContext().getString(R.string.save_button), (dialog, which) -> {
                        String newName = editName.getText().toString();
                        double newPrice = Double.parseDouble(editPrice.getText().toString());
                        String imagePath = card.getImagePath();
                        databaseHelper.updateCard(card.getName(), newName, newPrice, imagePath);
                        card.setName(newName);
                        card.setPrice(newPrice);
                        notifyItemChanged(position);
                    })
                    .setNegativeButton(v.getContext().getString(R.string.cancel), null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return pokemonCardList.size();
    }

    // Clase para manejar los elementos de la vista
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView;
        ImageView pokemonImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            pokemonImageView = itemView.findViewById(R.id.imageView2);
        }
    }
}