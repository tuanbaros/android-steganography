package tuannt.simple.steganography;

import android.graphics.Bitmap;

public class DecodeRequest {
    private final Bitmap bitmap;
    private final String key;

    public DecodeRequest(Bitmap bitmap, String key) {
        this.bitmap = bitmap;
        this.key = key;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getKey() {
        return key;
    }
}
