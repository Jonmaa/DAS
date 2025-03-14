package com.example.trabajodas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trabajodas.PokemonCard;

import java.util.ArrayList;
import java.util.List;

public class PokemonCardViewModel extends ViewModel {
    private final MutableLiveData<List<PokemonCard>> pokemonCardList = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<PokemonCard>> getPokemonCardList() {
        return pokemonCardList;
    }

    public void addCard(PokemonCard card) {
        List<PokemonCard> currentList = pokemonCardList.getValue();
        currentList.add(card);
        pokemonCardList.setValue(currentList);
    }

    public void setPokemonCardList(List<PokemonCard> cards) {
        pokemonCardList.setValue(cards);
    }
}