package pt.ipp.estg.lifespanqr.database;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.List;

import pt.ipp.estg.lifespanqr.IngredientCalendar;
import pt.ipp.estg.lifespanqr.ingredient.Ingredient;


public class IngredientRepository {
    private static IngredientDao ingredientDao;
    private static LiveData<List<Ingredient>> roomObservable;
    private static MutableLiveData<List<Ingredient>> firebaseObservable;
    private static MediatorLiveData<List<Ingredient>> mediatorObservable;
    private static IngredientRepository instance;
    private static ChildEventListener firebaseChildEventListener;
    private static Observer<List<Ingredient>> mediatorObserver;

    private IngredientRepository(Application application){
        Database database = Database.getInstance(application);
        ingredientDao = database.ingredientDao();
        mediatorObservable = new MediatorLiveData<>();
        roomObservable = ingredientDao.getAll();
        firebaseChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Ingredient ingredient = snapshot.getValue(Ingredient.class);
                if (!ingredient.isConsumed())
                    IngredientCalendar.addEvent(ingredient);
                Firebase.setChildAdded(ingredient);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Ingredient ingredient = snapshot.getValue(Ingredient.class);
                Firebase.setChildChanged(ingredient);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Ingredient ingredient = snapshot.getValue(Ingredient.class);
                Firebase.setChildRemoved(ingredient);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // no-op
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // no-op
            }
        };
        mediatorObserver = new Observer<List<Ingredient>>() {
            @Override
            public void onChanged(List<Ingredient> ingredients) {
                mediatorObservable.setValue(ingredients);
            }
        };
    }

    public static IngredientRepository getInstance(Application application){
        if (instance == null){
            instance = new IngredientRepository(application);
        }
        return instance;
    }

    public MediatorLiveData<List<Ingredient>> getObservable(){
        return mediatorObservable;
    }

    public void add(Ingredient ingredient, boolean addToCalendar){
        if(addToCalendar) {
            ingredient = IngredientCalendar.addEvent(ingredient);
        }
        new RepositoryAsyncTasks.Add(ingredientDao).execute(ingredient);
    }

    public void update(Ingredient ingredient){
        if (ingredient.isConsumed()){
            IngredientCalendar.deleteEvent(ingredient);
        } else {
            ingredient = IngredientCalendar.addEvent(ingredient);
        }
        new RepositoryAsyncTasks.Update(ingredientDao).execute(ingredient);
    }

    public void delete(Ingredient ingredient){
        IngredientCalendar.deleteEvent(ingredient);
        new RepositoryAsyncTasks.Delete(ingredientDao).execute(ingredient);
    }

    public void deleteAll(){
        for (Ingredient ingredient : mediatorObservable.getValue()) {
            IngredientCalendar.deleteEvent(ingredient);
        }
        new RepositoryAsyncTasks.DeleteAll(ingredientDao).execute();
    }

    public static void useFirebase(){
        Firebase.getIngredientReference().addChildEventListener(firebaseChildEventListener);
        Log.i("IngredientRepository", "Firebase listener attached");

        mediatorObservable.removeSource(roomObservable);
        Log.i("IngredientRepository", "Room removed as source");

        Firebase.mergeWithRoom(roomObservable.getValue());
        firebaseObservable = Firebase.getObservable();
        Log.i("IngredientRepository", "Room merged with Firebase");

        mediatorObservable.addSource(firebaseObservable, mediatorObserver);
        Log.i("IngredientRepository", "Firebase added as source");

        new RepositoryAsyncTasks.DeleteRoom(ingredientDao).execute();
    }

    public static void useRoom(){
        removeFirebaseChildEventListener();

        mediatorObservable.removeSource(firebaseObservable);
        Log.i("IngredientRepository", "Firebase removed as source");

        if(firebaseObservable != null && firebaseObservable.getValue() != null) {
            firebaseObservable.getValue().clear();
            Log.i("IngredientRepository", "Firebase local data removed");
        }

        if(mediatorObservable.getValue() != null) {
            List<Ingredient> ingredientList = mediatorObservable.getValue();

            if (ingredientList != null && !ingredientList.isEmpty()) {
                for (Ingredient ingredient : ingredientList) {
                    IngredientCalendar.deleteEvent(ingredient);
                }
            }
            Log.i("IngredientRepository", "Firebase ingredient events removed");
        }

        mediatorObservable.addSource(roomObservable, mediatorObserver);
        Log.i("IngredientRepository", "Room added as source");
    }

    public static void removeFirebaseChildEventListener(){
        try {
            Firebase.getIngredientReference().removeEventListener(firebaseChildEventListener);
            Log.i("IngredientRepository", "Firebase listener detached");
        } catch (Exception e){
            Log.e("IngredientRepository", "Failed to detach firebase listener");
        }
    }
}
