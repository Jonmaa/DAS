package com.example.trabajodas;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.List;

public class AddCardFragment extends Fragment {

    private ImageView imageView;
    private String imagePath;
    private DatabaseHelper databaseHelper;
    private PokemonCardViewModel viewModel;
    private PokemonCardAdapter adapter;

    // ActivityResultLauncher para seleccionar una imagen de la galería
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    imagePath = getPathFromUri(selectedImageUri);
                    imageView.setImageURI(selectedImageUri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_card, container, false);

        databaseHelper = new DatabaseHelper(getActivity());
        viewModel = new ViewModelProvider(requireActivity()).get(PokemonCardViewModel.class);

        // Observar la lista de cartas en el ViewModel
        viewModel.getPokemonCardList().observe(getViewLifecycleOwner(), cards -> {
            if (cards.isEmpty()) {
                // Si la lista está vacía después de cambiar de idioma, recargar desde la base de datos
                viewModel.setPokemonCardList(databaseHelper.getAllCards());
            }
            adapter = new PokemonCardAdapter(cards, databaseHelper);
        });

        EditText editName = view.findViewById(R.id.editName);
        EditText editPrice = view.findViewById(R.id.editPrice);
        Button saveButton = view.findViewById(R.id.saveButton);
        Button selectImageButton = view.findViewById(R.id.selectImageButton);
        imageView = view.findViewById(R.id.imageView);

        // Añadir una carta
        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString();
            double price = Double.parseDouble(editPrice.getText().toString());

            if (name.isEmpty() || price <= 0) {
                Toast.makeText(getActivity(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            // Add card to the database
            PokemonCard card = new PokemonCard(name, price, imagePath);
            databaseHelper.insertCard(name, price, imagePath, getActivity());


            // Update the ViewModel
            viewModel.addCard(card);

            // Send notification
            sendNotification(name);

        });

        // Seleccionar una imagen de la galería
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        return view;
    }

    // Obtener la ruta de la imagen seleccionada
    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

    // Enviar una notificación
    private void sendNotification(String cardName) {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "card_channel";
        String channelName = "Card Notifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), channelId)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("New Card Added")
                .setContentText("Card: " + cardName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }

    // Actualizar los textos al cambiar de idioma
    public void updateTexts() {
        EditText editName = getView().findViewById(R.id.editName);
        EditText editPrice = getView().findViewById(R.id.editPrice);
        Button saveButton = getView().findViewById(R.id.saveButton);
        Button selectImageButton = getView().findViewById(R.id.selectImageButton);

        editName.setHint(getString(R.string.name_hint));
        editPrice.setHint(getString(R.string.price_hint));
        saveButton.setText(getString(R.string.save_button));
        selectImageButton.setText(getString(R.string.select_picture));
    }
}