package pt.ipp.estg.lifespanqr.database;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.ipp.estg.lifespanqr.ingredient.Ingredient;

public abstract class Firebase{

    private static MutableLiveData<List<Ingredient>> ingredientObservable = new MutableLiveData<>();


    public static void addIngredient(Ingredient ingredient){
        ingredient.key = getIngredientReference().push().getKey();
        getIngredientReference().child(ingredient.key).setValue(ingredient);
    }

    protected static void setChildAdded(Ingredient ingredient){
        List<Ingredient> newList = ingredientObservable.getValue();
        if(newList == null){
            newList = new ArrayList<>();
        }
        newList.add(ingredient);
        ingredientObservable.setValue(newList);
    }

    public static void updateIngredient(Ingredient ingredient) {
        getIngredientReference().child(ingredient.key).setValue(ingredient);
    }

    protected static void setChildChanged(Ingredient ingredient){
        List<Ingredient> updatedList = ingredientObservable.getValue();
        updatedList = updatedList.stream()
                .map(ingredient1 -> ingredient1.getKey().equals(ingredient.getKey()) ? ingredient : ingredient1)
                .collect(Collectors.toList());
        ingredientObservable.setValue(updatedList);
    }

    public static void deleteIngredient(Ingredient ingredient){
        getIngredientReference().child(ingredient.key).removeValue();
    }

    public static void setChildRemoved(Ingredient ingredient){
        List<Ingredient> updatedList = ingredientObservable.getValue();
        for(int i = 0; i < updatedList.size(); i++){
            if(updatedList.get(i).getKey().equals(ingredient.getKey())){
                updatedList.remove(i);
            }
        }
        ingredientObservable.setValue(updatedList);
    }

    public static void deleteAll(){
        getIngredientReference().removeValue();
    }

    public static MutableLiveData<List<Ingredient>> getObservable(){
        return ingredientObservable;
    }

    public static DatabaseReference getUserReference(){
        return FirebaseDatabase.getInstance().getReference(getUid());
    }

    private static String getUid(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static void setList(List<Ingredient> list){
        ingredientObservable.setValue(list);
    }

    public static DatabaseReference getIngredientReference(){
        return getUserReference().child("ingredients");
    }

    public static DatabaseReference getTrashReference(){
        return getUserReference().child("trash");
    }

    public static void deleteLocal(){
        ingredientObservable.getValue().clear();
    }

    public static void mergeWithRoom(List<Ingredient> roomIngredients){
        if(roomIngredients != null) {
            roomIngredients.forEach(ingredient -> {
                Firebase.addIngredient(ingredient);
            });
        }
    }
}