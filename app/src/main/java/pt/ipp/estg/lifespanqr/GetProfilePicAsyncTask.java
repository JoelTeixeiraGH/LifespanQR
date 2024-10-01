package pt.ipp.estg.lifespanqr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.Menu;

import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;
import java.net.URL;

public class GetProfilePicAsyncTask extends AsyncTask<Void, Void, Bitmap> {
    private Menu menu;
    private FirebaseUser user;
    private ProfilePicListener listener;

    public interface ProfilePicListener {
        void onUserPhotoFetched(Bitmap bitmap);
    }

    GetProfilePicAsyncTask(Menu menu, FirebaseUser user, ProfilePicListener listener){
        this.menu = menu;
        this.user = user;
        this.listener = listener;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        try {
            InputStream is = (InputStream) new URL(user.getPhotoUrl().toString()).getContent();
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            System.out.println("ERR " + e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        listener.onUserPhotoFetched(bitmap);
    }
}