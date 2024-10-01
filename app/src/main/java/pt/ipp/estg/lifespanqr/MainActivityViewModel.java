package pt.ipp.estg.lifespanqr;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class MainActivityViewModel extends AndroidViewModel {

    private MutableLiveData<Bitmap> profilePic;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        this.profilePic = new MutableLiveData<>();
    }

    public MutableLiveData<Bitmap> getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Bitmap profilePic) {
        this.profilePic.setValue(profilePic);
    }
}
