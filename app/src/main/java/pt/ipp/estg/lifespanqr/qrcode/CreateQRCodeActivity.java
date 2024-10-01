package pt.ipp.estg.lifespanqr.qrcode;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import pt.ipp.estg.lifespanqr.R;

public class CreateQRCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qrcode);

        CreateQRFragment createQRFragment = new CreateQRFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameMain, createQRFragment)
                .commit();
    }
}