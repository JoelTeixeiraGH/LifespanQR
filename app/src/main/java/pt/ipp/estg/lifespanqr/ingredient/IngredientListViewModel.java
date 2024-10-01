package pt.ipp.estg.lifespanqr.ingredient;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import pt.ipp.estg.lifespanqr.IngredientCalendar;
import pt.ipp.estg.lifespanqr.database.IngredientRepository;

public class IngredientListViewModel extends AndroidViewModel {
    private IngredientRepository repository;
    private LiveData<List<Ingredient>> ingredientObservable;
    private Context mContext;


    public IngredientListViewModel(@NonNull Application application) {
        super(application);
        repository = IngredientRepository.getInstance(application);
        ingredientObservable = repository.getObservable();
        mContext = application.getBaseContext();
    }


    public void add(Ingredient ingredient, boolean addToCalendar){
        repository.add(ingredient, addToCalendar);
        Toast.makeText(mContext, "Ingredient added", Toast.LENGTH_SHORT).show();
    }

    public void update(Ingredient ingredient){
        if(ingredient.isConsumed()){
            IngredientCalendar.getInstance().deleteEvent(ingredient);
        }
        repository.update(ingredient);
    }

    public void delete(Ingredient ingredient){
        repository.delete(ingredient);
        Toast.makeText(mContext, "Ingredient deleted", Toast.LENGTH_SHORT).show();
    }

    public void deleteAll(){
        repository.deleteAll();
        Toast.makeText(mContext, "Ingredients deleted", Toast.LENGTH_SHORT).show();
    }

    public LiveData<List<Ingredient>> getObservable(){
        return ingredientObservable;
    }
}