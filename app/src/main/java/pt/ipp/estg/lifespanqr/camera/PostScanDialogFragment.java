package pt.ipp.estg.lifespanqr.camera;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import pt.ipp.estg.lifespanqr.ingredient.Ingredient;

public class PostScanDialogFragment extends DialogFragment {

    private Ingredient ingredient;
    private DialogListener listener;

    public PostScanDialogFragment(DialogListener listener, Ingredient ingredient){
        this.listener = listener;
        this.ingredient = ingredient;
    }

    private void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        return new MaterialAlertDialogBuilder(getActivity())
                .setTitle("New Ingredient")
                .setMessage(String.format("Name: %s\nBrand: %s\nExpiry date: %s",
                        ingredient.getName(), ingredient.getBrand(), ingredient.getExpiry()))
                .setPositiveButton("Add (with event)", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onPositiveClick();
                    }
                })
                .setNegativeButton("Add (no event)", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onNegativeClick();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

    }

    public interface DialogListener{
        public void onPositiveClick();
        public void onNegativeClick();
    }
}
