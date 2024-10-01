package pt.ipp.estg.lifespanqr.database;

import android.os.AsyncTask;

import com.google.firebase.auth.FirebaseAuth;

import pt.ipp.estg.lifespanqr.ingredient.Ingredient;

public class RepositoryAsyncTasks {
    public static class Add extends AsyncTask<Ingredient, Void, Void> {
        private IngredientDao ingredientDao;

        public Add(IngredientDao dao){
            ingredientDao = dao;
        }

        @Override
        protected Void doInBackground(Ingredient... ingredients) {
            if(FirebaseAuth.getInstance().getCurrentUser() == null) {
                ingredientDao.insert(ingredients[0]);
            }
            else {
                Firebase.addIngredient(ingredients[0]);
            }
            return null;
        }
    }

    public static class Update extends AsyncTask<Ingredient, Void, Void> {
        private IngredientDao ingredientDao;

        public Update(IngredientDao dao){
            ingredientDao = dao;
        }


        @Override
        protected Void doInBackground(Ingredient... ingredients) {
            if(FirebaseAuth.getInstance().getCurrentUser() == null) {
                ingredientDao.update(ingredients[0]);
            }
            else {
                Firebase.updateIngredient(ingredients[0]);
            }
            return null;
        }
    }

    public static class Delete extends AsyncTask<Ingredient, Void, Void> {
        private IngredientDao ingredientDao;

        public Delete(IngredientDao dao){
            ingredientDao = dao;
        }

        @Override
        protected Void doInBackground(Ingredient... ingredients) {
            if(FirebaseAuth.getInstance().getCurrentUser() == null) {
                ingredientDao.delete(ingredients[0]);
            }
            else {
                Firebase.deleteIngredient(ingredients[0]);
            }
            return null;
        }
    }

    public static class DeleteAll extends AsyncTask<Void, Void, Void> {
        private IngredientDao ingredientDao;

        public DeleteAll(IngredientDao dao){
            ingredientDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(FirebaseAuth.getInstance().getCurrentUser() == null) {
                ingredientDao.deleteAllIngredients();
            }
            else {
                Firebase.deleteAll();
            }
            return null;
        }
    }

    public static class DeleteRoom extends AsyncTask<Void, Void, Void> {
        private IngredientDao ingredientDao;

        public DeleteRoom(IngredientDao dao){
            ingredientDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ingredientDao.deleteAllIngredients();
            return null;
        }
    }
}
