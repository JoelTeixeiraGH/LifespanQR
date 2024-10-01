package pt.ipp.estg.lifespanqr.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pt.ipp.estg.lifespanqr.ingredient.Ingredient;

@Dao
public interface IngredientDao {

    @Insert
    void insert(Ingredient ingredient);

    @Update
    void update(Ingredient ingredient);

    @Delete
    void delete(Ingredient ingredient);

    @Query("DELETE FROM Ingredient")
    void deleteAllIngredients();

    @Query("SELECT * FROM Ingredient")
    LiveData<List<Ingredient>> getAll();
}
