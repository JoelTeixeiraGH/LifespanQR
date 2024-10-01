package pt.ipp.estg.lifespanqr.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import pt.ipp.estg.lifespanqr.R;
import pt.ipp.estg.lifespanqr.ingredient.Ingredient;
import pt.ipp.estg.lifespanqr.ingredient.IngredientListViewModel;
import pt.ipp.estg.lifespanqr.qrcode.QRCode;

public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";
    private static final int REQUEST_CAMERA = 52;
    private PreviewView cameraView;
    private Camera camera;
    private Context mContext;
    private Button torchToggle;

    public CameraFragment(){}

    public CameraFragment(int contentLayoutId) {
        super(contentLayoutId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraView = view.findViewById(R.id.cameraView);
        torchToggle = view.findViewById(R.id.torchToggle);
        camera = new Camera(mContext, this, view, R.id.cameraView, R.id.torchToggle);

        if (!checkSelfPermissions()) {
            requestPermissions();
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        camera.setOnCaptureListener(photo -> {
            Ingredient newIngredient;
            String rawValue = QRCode.getRawValue(getContext(), photo);

            if(rawValue == null){
                Toast.makeText(getActivity(), "No QR Code detected", Toast.LENGTH_SHORT).show();
            } else {
                if((newIngredient = Ingredient.parse((rawValue))) == null){
                    Toast.makeText(getActivity(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
                } else {
                    showPostCaptureDialog(getParentFragment(), newIngredient);
                }
            }
        });
        camera.startCamera();
    }


    private boolean checkSelfPermissions(){
        return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions(){
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startCamera();
        }
    }

    public void showPostCaptureDialog(Fragment fragment, Ingredient newIngredient){
        PostScanDialogFragment.DialogListener listener = new PostScanDialogFragment.DialogListener() {
            @Override
            public void onPositiveClick() {
                ViewModelProviders.of(getActivity()).get(IngredientListViewModel.class)
                        .add(newIngredient, true);
            }

            @Override
            public void onNegativeClick() {
                ViewModelProviders.of(getActivity()).get(IngredientListViewModel.class)
                        .add(newIngredient, false);
            }
        };

        PostScanDialogFragment dialog = new PostScanDialogFragment(listener, newIngredient);

        dialog.show(getActivity().getSupportFragmentManager(), null);
    }

}