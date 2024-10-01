package pt.ipp.estg.lifespanqr.authentication;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseUser;

import pt.ipp.estg.lifespanqr.MainActivity;
import pt.ipp.estg.lifespanqr.R;

public class ProfileDialogFragment extends DialogFragment{
    private FirebaseUser user;
    private Bitmap userPic;
    private TextView tv_username, tv_email;
    private ImageView iv_userPic;
    private Button btn_signOut;
    private OnSignOutListener onSignOutListener;

    public interface OnSignOutListener {
        void onSignOutClick();
    }

    public ProfileDialogFragment(FirebaseUser user, Bitmap userPic, OnSignOutListener onSignOutListener) {
        super();
        this.user = user;
        this.userPic = userPic;
        this.onSignOutListener = onSignOutListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.profile_dialog, null);

        iv_userPic = view.findViewById(R.id.profile_dialog_user_icon);
        tv_username = view.findViewById(R.id.profile_dialog_username);
        tv_email = view.findViewById(R.id.profile_dialog_email);
        btn_signOut = view.findViewById(R.id.profile_dialog_signout);

        iv_userPic.setImageBitmap(userPic);
        tv_username.setText(user.getDisplayName());
        tv_email.setText(user.getEmail());
        btn_signOut.setOnClickListener(v -> {
            onSignOutListener.onSignOutClick();
            getDialog().dismiss();
        });

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return view;
    }
}
