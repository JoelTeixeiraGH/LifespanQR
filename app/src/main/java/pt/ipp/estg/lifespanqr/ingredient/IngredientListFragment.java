package pt.ipp.estg.lifespanqr.ingredient;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import pt.ipp.estg.lifespanqr.R;
import pt.ipp.estg.lifespanqr.qrcode.CreateQRCodeActivity;
import pt.ipp.estg.lifespanqr.qrcode.QRCode;

public class IngredientListFragment extends Fragment {
    private RecyclerView rv_list;
    private IngredientAdapter adapter;
    private FloatingActionButton fab;
    private IngredientAdapter.TAG mTag;
    private IngredientListViewModel ingredientViewModel;
    private IngredientAdapter.CheckboxListener checkboxListener;
    private IngredientAdapter.ItemClickListener itemClickListener;


    public IngredientListFragment(){}

    public IngredientListFragment(int contentLayoutId, IngredientAdapter.TAG tag) {
        super(contentLayoutId);
        this.mTag = tag;
        checkboxListener = ingredient -> {
            ingredient.setConsumed(!ingredient.isConsumed());
            ingredientViewModel.update(ingredient);
        };

        itemClickListener = this::showDetailsDialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv_list = view.findViewById(R.id.ingredient_rv);
        fab = view.findViewById(R.id.fab);

        adapter = new IngredientAdapter(checkboxListener, itemClickListener, mTag);
        rv_list.setAdapter(adapter);
        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));

        ingredientViewModel = ViewModelProviders.of(this).get(IngredientListViewModel.class);

        ingredientViewModel.getObservable()
                .observe(getViewLifecycleOwner(), this::updateRecyclerView);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateQRCodeActivity.class);
            startActivity(intent);
        });
    }

    private void updateRecyclerView(List<Ingredient> ingredients){
        adapter.setIngredients(ingredients);
        if(adapter.getItemCount() == 0){
            getView().findViewById(R.id.empty_list).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.ingredient_rv).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.empty_list).setVisibility(View.GONE);
            getView().findViewById(R.id.ingredient_rv).setVisibility(View.VISIBLE);
        }
    }

    private void showDetailsDialog(Ingredient ingredient) {
        View v = getLayoutInflater().inflate(R.layout.item_details_dialog, null);
        ImageView iv_qrcode = v.findViewById(R.id.item_details_dialog_qrcode);
        TextView tv_name = v.findViewById(R.id.item_details_dialog_name);
        TextView tv_expiry = v.findViewById(R.id.item_details_dialog_expiry);
        TextView tv_brand = v.findViewById(R.id.item_details_dialog_brand);

        Bitmap qrcode = QRCode.from(ingredient.toQRString())
                .withSize(350, 350)
                .withCharset("UTF-8")
                .bitmap();

        iv_qrcode.setImageBitmap(qrcode);
        tv_name.setText(ingredient.getName());
        tv_expiry.setText(ingredient.getExpiry());
        tv_brand.setText(ingredient.getBrand());

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(getContext())
                .setView(v)
                .setPositiveButton("delete", (dialog, which) ->
                        ingredientViewModel.delete(ingredient))
                .setNeutralButton("cancel", (dialog, which) ->
                        dialog.dismiss())
                .setNegativeButton("save QR code", (dialog, which) -> {
                    MediaStore.Images.Media
                            .insertImage(getActivity().getContentResolver(),
                                    qrcode,
                                    "QR_" + ingredient.getName(),
                                    null);
                    Toast.makeText(getActivity(), "QR Code saved", Toast.LENGTH_SHORT).show();
                })
                .create();

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
    }
}
