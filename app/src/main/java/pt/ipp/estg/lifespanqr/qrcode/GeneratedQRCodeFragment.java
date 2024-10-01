package pt.ipp.estg.lifespanqr.qrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import pt.ipp.estg.lifespanqr.R;


public class GeneratedQRCodeFragment extends Fragment {

    private Bitmap qrcode;
    private ImageView myImage;
    private Button btnSave;

    public GeneratedQRCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_generated_qrcode, container, false);
        btnSave = v.findViewById(R.id.btnSave);

        qrcode = (Bitmap) getArguments().get("qrcode");

        myImage = (ImageView) v.findViewById(R.id.imageView);
        myImage.setImageBitmap(qrcode);


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToGallery();
                Toast.makeText(v.getContext(), "QRCode saved", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    private void saveImageToGallery(){
        MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), qrcode, "QR_" + getArguments().getString("name"), null);
    }
}