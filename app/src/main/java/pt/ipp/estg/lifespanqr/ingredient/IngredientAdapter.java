package pt.ipp.estg.lifespanqr.ingredient;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import pt.ipp.estg.lifespanqr.R;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private TAG tag;
    private Context mContext;
    private List<Ingredient> ingredients;
    private CheckboxListener checkboxListener;
    private ItemClickListener itemClickListener;

    public IngredientAdapter(CheckboxListener checkboxListener, ItemClickListener itemClickListener, TAG tag) {
        this.checkboxListener = checkboxListener;
        this.itemClickListener = itemClickListener;
        ingredients = new ArrayList<>();
        this.tag = tag;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        this.mContext = parent.getContext();

        return new IngredientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int i) {

        Ingredient ingredient = ingredients.get(i);

        LocalDate expiryDate = LocalDate.parse(ingredient.getExpiry(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String expiryDateStr = ingredient.getExpiry();

        holder.v_name.setText(ingredient.getName());
        holder.v_brand.setText(ingredient.getBrand());

        if (tag == TAG.UNCHECKED) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            int pref_maxExpiringSoon = sharedPreferences.getInt("pref_expiring_soon", 15);

            LocalDate currentDate = LocalDate.now();
            long daysLeft = ChronoUnit.DAYS.between(currentDate, expiryDate);

            if (daysLeft < pref_maxExpiringSoon) {
                if (daysLeft == 0) {
                    expiryDateStr = mContext.getResources().getString(R.string.today);
                    holder.v_expiryDate.setTextColor(ContextCompat.getColor(mContext, R.color.error));
                } else if (daysLeft == 1) {
                    expiryDateStr = mContext.getResources().getString(R.string.tomorrow);
                    holder.v_expiryDate.setTextColor(ContextCompat.getColor(mContext, R.color.warning));
                } else {
                    expiryDateStr = daysLeft + " days left";
                }
            }
        } else {
            holder.v_name.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            holder.v_expiryDate.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            holder.v_brand.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            if (ingredient.isExpired()) {
                holder.v_checkbox.setEnabled(false);
                holder.v_checkbox.setChecked(true);
            } else {
                holder.v_checkbox.setChecked(ingredient.isConsumed());
            }
        }

        holder.v_expiryDate.setText(expiryDateStr);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        filterIngredients();
        notifyDataSetChanged();
    }

    public class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView v_name, v_brand, v_expiryDate;
        CheckBox v_checkbox;
        LinearLayout v_this;

        public IngredientViewHolder(@NonNull View v) {
            super(v);
            v_name = v.findViewById(R.id.recycler_view_ingredient_name);
            v_brand = v.findViewById(R.id.recycler_view_ingredient_brand);
            v_expiryDate = v.findViewById(R.id.recycler_view_ingredient_expiry);
            v_checkbox = v.findViewById(R.id.recycler_view_ingredient_checkbox);
            v_this = v.findViewById(R.id.list_item);

            v_checkbox.setOnClickListener(v1 -> {
                CheckBox c = (CheckBox) v1;
                checkboxListener.onCheckboxClick(ingredients.get(getAdapterPosition()));
                c.setChecked(!c.isChecked());
            });

            v.setOnClickListener(v1 -> itemClickListener.onItemClick(ingredients.get(getAdapterPosition())));
        }
    }

    public enum TAG {
        UNCHECKED, CHECKED
    }

    public void filterIngredients(){
        if(tag == IngredientAdapter.TAG.CHECKED) {
            ingredients = ingredients.stream().filter(i -> i.isConsumed() || i.isExpired())
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.sort(ingredients, Collections.reverseOrder());
        }
        else {
            ingredients = ingredients.stream().filter(i -> {
                System.out.println(i.getName() + ", "+ i.isConsumed() + ", " + i.isExpired());
                return !i.isConsumed() && !i.isExpired();})
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.sort(ingredients);
        }
    }

    public interface CheckboxListener {
        void onCheckboxClick(Ingredient ingredient);
    }

    public interface ItemClickListener{
        void onItemClick(Ingredient ingredient);
    }
}
