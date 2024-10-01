package pt.ipp.estg.lifespanqr.qrcode;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import net.glxn.qrgen.android.QRCode;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import pt.ipp.estg.lifespanqr.R;
import pt.ipp.estg.lifespanqr.ingredient.Ingredient;
import pt.ipp.estg.lifespanqr.ingredient.IngredientListViewModel;


public class CreateQRFragment extends Fragment{

    private static final int REQUEST_WRITE_CALENDAR = 500;
    private Button btnGenerate;
    private String name, brand, date;
    private EditText editTextProductName,editTextProductBrand,editTextProductDate;
    private DatePickerDialog dpd;
    private CheckBox checkBox_addProduct, checkBox_addToCalendar;
    private int day, month, year;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth auth;
    private final ContentValues event = new ContentValues();
    private int calendarId = 1;

    public CreateQRFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_create_qr, container, false);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Products");

        editTextProductDate = (EditText) v.findViewById(R.id.editTextProductDate);
        editTextProductName = (EditText) v.findViewById(R.id.editTextProductName);
        editTextProductBrand = (EditText) v.findViewById(R.id.editTextProductBrand);
        btnGenerate = (Button) v.findViewById(R.id.btnGenerate);
        checkBox_addProduct = (CheckBox) v.findViewById(R.id.checkBox);
        checkBox_addToCalendar = (CheckBox) v.findViewById(R.id.addWithEvent_checkBox);
        checkBox_addToCalendar.setEnabled(false);

        checkBox_addProduct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBox_addToCalendar.setEnabled(isChecked);
                if(!isChecked){
                    checkBox_addToCalendar.setChecked(false);
                }
            }
        });

        //Date Picker + Edit Text
        editTextProductDate.setOnClickListener(v1 -> {

            dpd = new DatePickerDialog(
                    v1.getContext(),
                    (view, _year, _month, _day) -> {
                        day = _day;
                        month = _month+1;
                        year = _year;
                        editTextProductDate.setText(day + "/" + month + "/" + year);
                    },
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dpd.show();

        });

        btnGenerate.setOnClickListener(v12 -> {
            name = editTextProductName.getText().toString();
            brand = editTextProductBrand.getText().toString();
            date = Ingredient.buildDateString(day, month, year);

            if (TextUtils.isEmpty(name.trim()) || TextUtils.isEmpty(brand.trim()) || TextUtils.isEmpty(date.trim())) {
                Toast.makeText(getActivity(), "The fields cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                Bitmap qrcode = QRCode.from(name + "//" + brand + "//" + date).withSize(750, 750).withCharset("UTF-8").bitmap();

                GeneratedQRCodeFragment generatedQRCodeFragment = new GeneratedQRCodeFragment();

                if (checkBox_addProduct.isChecked()) {
                    Ingredient newIngredient = new Ingredient(name, brand, date);
                    ViewModelProviders.of(getActivity()).get(IngredientListViewModel.class).add(newIngredient, checkBox_addToCalendar.isChecked());
                }

                Bundle args = new Bundle();
                args.putParcelable("qrcode", qrcode);
                args.putString("name", name);

                generatedQRCodeFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameMain, generatedQRCodeFragment)
                        .commit();
            }
        });

        return v;
    }
}