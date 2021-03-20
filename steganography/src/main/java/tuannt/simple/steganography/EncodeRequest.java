package tuannt.simple.steganography;

import android.graphics.Bitmap;

public class EncodeRequest {
    private final Bitmap bitmap;
    private final String message;
    private final String key;

    public EncodeRequest(Bitmap bitmap, String message, String key) {
        this.bitmap = bitmap;
        this.message = message;
        this.key = key;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getMessage() {
        return message;
    }

    public String getKey() {
        return key;
    }
}
