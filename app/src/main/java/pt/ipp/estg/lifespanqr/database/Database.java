package pt.ipp.estg.lifespanqr.database;

import android.content.Context;
import androidx.room.*;
import java.time.LocalDate;
import pt.ipp.estg.lifespanqr.ingredient.Ingredient;

@androidx.room.Database(entities = {Ingredient.class}, version = 1)
@androidx.room.TypeConverters({Database.TypeConverters.class})
public abstract class Database extends RoomDatabase {

    private static Database instance;

    public abstract IngredientDao ingredientDao();

    public static synchronized Database getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    Database.class, "lifespantag")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public static class TypeConverters {

        @androidx.room.TypeConverter
        public static String dateToString (LocalDate date){
            return date.toString();
        }

        @androidx.room.TypeConverter
        public static LocalDate stringToDate(String date){
            return LocalDate.parse(date);
        }
    }
}