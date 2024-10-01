package pt.ipp.estg.lifespanqr.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;

import pt.ipp.estg.lifespanqr.R;

public final class Camera {
    private static androidx.camera.core.Camera xCamera;
    private static LifecycleOwner lifecycleOwner;
    private static Button torchToggle;
    private static PreviewView cameraView;
    private static View mView;
    private static Context context;
    private static OnCaptureListener photoCaptureListener;
    private static ImageCapture imageCapture;

    public interface OnCaptureListener {
        void onPhotoCapture(Bitmap photo);
    }

    public Camera(Context context, LifecycleOwner lifecycleOwner,
                   View view, Integer previewViewLayoutId, Integer torchButtonLayoutId){

        mView = view;
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        cameraView = view.findViewById(previewViewLayoutId);
        torchToggle = view.findViewById(torchButtonLayoutId);
    }

    public void startCamera(){
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .build();

                preview.setSurfaceProvider(cameraView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetRotation(mView.getDisplay().getRotation())
                        .build();

                cameraProvider.unbindAll();
                xCamera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture, preview);

                setTorchOnClickListener();
            } catch (Exception e) {
                Log.e("CAMERA", "Failed to bind use case ", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void setTorchOnClickListener() {
        torchToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(xCamera.getCameraInfo().getTorchState().getValue() == 0){
                    xCamera.getCameraControl().enableTorch(true);
                    torchToggle.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_flash_on_24));
                } else {
                    xCamera.getCameraControl().enableTorch(false);
                    torchToggle.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_flash_off_24));
                }
            }
        });
    }

    public void setOnCaptureListener(OnCaptureListener listener){
        photoCaptureListener = listener;
    }

    public static void takePicture() {
        if (imageCapture != null) {
            imageCapture.takePicture(ContextCompat.getMainExecutor(context), new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    Bitmap photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                    photoCaptureListener.onPhotoCapture(photo);
                    image.close();
                }
            });
        }
    }
}
