package com.example.trabajodas;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddCardActivity extends AppCompatActivity {

    private EditText nameEditText, priceEditText;
    private Button saveButton, selectImageButton;
    private ImageView cardImageView;
    private String cardImagePath;

    // Lanzador para seleccionar imagen de la galería
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    cardImagePath = getPathFromUri(selectedImageUri);
                    cardImageView.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        nameEditText = findViewById(R.id.nameEditText);
        priceEditText = findViewById(R.id.priceEditText);
        saveButton = findViewById(R.id.saveButton);
        selectImageButton = findViewById(R.id.selectImageButton);
        cardImageView = findViewById(R.id.imageView);

        // Seleccionar imagen de la galería
        selectImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Guardar la carta
        saveButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString();
            double price = Double.parseDouble(priceEditText.getText().toString());

            if (name.isEmpty() || price <= 0) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            // Devolver los datos a la actividad principal
            Intent intent = new Intent();
            intent.putExtra("pokemonName", name);
            intent.putExtra("pokemonPrice", price);
            intent.putExtra("pokemonImage", cardImagePath);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    // Obtener la ruta de la imagen seleccionada
    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }
}
