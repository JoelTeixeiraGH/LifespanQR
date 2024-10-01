package pt.ipp.estg.lifespanqr.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public abstract class QRCode extends net.glxn.qrgen.android.QRCode {

    protected QRCode(String text) {
        super(text);
    }

    public static String getRawValue(Context context, Bitmap bitmap){
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        SparseArray<Barcode> barcode = barcodeDetector.detect(frame);

        if(barcode.size() > 0)
            return barcode.valueAt(0).rawValue;
        return null;
    }
}
