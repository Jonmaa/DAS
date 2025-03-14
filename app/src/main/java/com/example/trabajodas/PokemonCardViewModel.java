package com.example.trabajodas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.trabajodas.PokemonCard;
import java.util.ArrayList;
import java.util.List;

public class PokemonCardViewModel extends ViewModel {
    private final MutableLiveData<List<PokemonCard>> pokemonCardList = new MutableLiveData<>(new ArrayList<>());

    // Obtiene la lista de cartas
    public LiveData<List<PokemonCard>> getPokemonCardList() {
        return pokemonCardList;
    }

    // Añade una carta a la lista de cartas
    public void addCard(PokemonCard card) {
        List<PokemonCard> currentList = pokemonCardList.getValue();
        currentList.add(card);
        pokemonCardList.setValue(currentList);
    }

    // Setea la lista de cartas
    public void setPokemonCardList(List<PokemonCard> cards) {
        pokemonCardList.setValue(cards);
    }
}