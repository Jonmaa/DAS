package com.example.trabajodas;

import android.content.DialogInterface;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    private RecyclerView recyclerView;
    private PokemonCardAdapter adapter;
    private List<PokemonCard> pokemonCardList;
    private Button addButton;
    private DatabaseHelper databaseHelper;
    private PokemonCardViewModel viewModel;

    // Para lanzar la actividad de añadir carta
    private final ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String name = result.getData().getStringExtra("pokemonName");
                    double price = result.getData().getDoubleExtra("pokemonPrice", 0.0);
                    String imagePath = result.getData().getStringExtra("pokemonImage");

                    // Guardar en la base de datos
                    databaseHelper.insertCard(name, price, imagePath, this);

                    // Recargar la lista
                    pokemonCardList.clear();
                    pokemonCardList.addAll(databaseHelper.getAllCards());
                    adapter.notifyDataSetChanged();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Cargar idioma guardado en SharedPreferences
        loadLocale();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Cargar el fragmento de añadir carta en modo horizontal
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainer, new AddCardFragment());
            fragmentTransaction.commit();
        }


        // Solicitar permisos de notificación y lectura de imágenes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.READ_MEDIA_IMAGES},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        databaseHelper = new DatabaseHelper(this); // Inicializar DB

        viewModel = new ViewModelProvider(this).get(PokemonCardViewModel.class);

        // Cargar cartas desde la base de datos
        List<PokemonCard> initialCards = databaseHelper.getAllCards();
        viewModel.setPokemonCardList(initialCards);
        viewModel.getPokemonCardList().observe(this, updatedCards -> {
            pokemonCardList.clear();
            pokemonCardList.addAll(updatedCards);
            adapter.notifyDataSetChanged();
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addButton = findViewById(R.id.addButton);

        // Ocultar botón de añadir en modo horizontal
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            addButton.setVisibility(View.GONE);
        } else {
            addButton.setVisibility(View.VISIBLE);
        }
        // Lanzar actividad de añadir carta al hacer clic en el botón
        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddCardActivity.class);
            startActivityIntent.launch(intent);
        });

        // Cargar cartas desde la base de datos
        pokemonCardList = databaseHelper.getAllCards();
        adapter = new PokemonCardAdapter(pokemonCardList, databaseHelper);
        recyclerView.setAdapter(adapter);

        Button changeLanguageButton = findViewById(R.id.changeLanguageButton);
        // Cambiar idioma al hacer clic en el botón
        changeLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });

    }

    // Mostrar un diálogo de selección de idioma
    private void showLanguageDialog() {
        final String[] languages = {"English", "Español"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_language))
                .setItems(languages, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                setLocale("en");
                                break;
                            case 1:
                                setLocale("es");
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    // Cambiar el idioma de la aplicación
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        // Guardar idioma en SharedPreferences
        getSharedPreferences("Settings", MODE_PRIVATE)
                .edit()
                .putString("My_Lang", lang)
                .apply();

        updateTexts();
        // Actualizar el texto del fragmento de añadir carta si está visible
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddCardFragment fragment = (AddCardFragment) fragmentManager.findFragmentById(R.id.fragmentContainer);
        if (fragment != null) {
            fragment.updateTexts();
        }
    }

    // Cargar el idioma guardado en SharedPreferences
    private void loadLocale() {
        String lang = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("My_Lang", "en"); // Predeterminado: inglés
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    // Actualizar los textos de la actividad
    private void updateTexts() {
        // Actualizar el título de la actividad
        setTitle(getString(R.string.app_name));

        // Actualizar el texto del botón de añadir
        addButton.setText(getString(R.string.add_button));

        // Actualizar el texto del botón de cambiar idioma
        Button changeLanguageButton = findViewById(R.id.changeLanguageButton);
        changeLanguageButton.setText(getString(R.string.change_language));

    }
}
