package tuannt.simple.steganography;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import io.flutter.FlutterInjector;
import io.flutter.Log;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.loader.FlutterLoader;
import io.flutter.plugin.common.MethodChannel;

public class Stegy {
    private static MethodChannel methodChannel;
    private final static String SUCCESS = "success";
    private final static String ERROR = "error";

    public static void init(Context context) {
        FlutterEngine flutterEngine = new FlutterEngine(context);
        FlutterLoader flutterLoader = FlutterInjector.instance().flutterLoader();

        //Start executing Dart code to pre-warm the FlutterEngine.
        flutterEngine.getDartExecutor().executeDartEntrypoint(
                new DartExecutor.DartEntrypoint(flutterLoader.findAppBundlePath(), "main")
        );
        methodChannel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), "samples.flutter.dev/battery");
    }

    public static void encodeStringIntoImage(Activity activity, EncodeRequest request, StegyCallback<Bitmap> callback) {
        methodChannel.setMethodCallHandler((call, result) -> {
            if (SUCCESS.equals(call.method)) {
                byte[] bytes = (byte[]) call.arguments;
                Log.i(">>>>",  "" + bytes[0]);
                Bitmap imgToSave = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                callback.onSuccess(imgToSave);
            } else if (ERROR.equals(call.method)) {
                callback.onError((String) call.arguments);
            }
        });
        new Thread(() -> {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            request.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            Log.i(">>>>pixels",  "" + request.getBitmap().getWidth() * request.getBitmap().getHeight());
            byte[] messages = request.getMessage().getBytes(StandardCharsets.US_ASCII);
            Log.i(">>>>", "" + messages[0]);
            HashMap map = new HashMap();
            map.put("data", bytes);
            map.put("message", request.getMessage());
            map.put("key", request.getKey());
            activity.runOnUiThread(() -> methodChannel.invokeMethod("encode", map));
        }).start();
    }

    public static void decodeStringFromImage(Activity activity, DecodeRequest request, StegyCallback<String> callback) {
        methodChannel.setMethodCallHandler((call, result) -> {
            if (SUCCESS.equals(call.method)) {
                String message = (String) call.arguments;
                callback.onSuccess(message);
            } else if (ERROR.equals(call.method)) {
                callback.onError((String) call.arguments);
            }
        });
        new Thread(() -> {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            request.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            HashMap map = new HashMap();
            map.put("data", bytes);
            map.put("key", request.getKey());
            activity.runOnUiThread(() -> methodChannel.invokeMethod("decode", map));
        }).start();
    }

    public static void encode(Activity activity, EncodeRequest request, StegyCallback<Bitmap> callback) {
        new Thread(() -> {
            int width = request.getBitmap().getWidth();
            int height = request.getBitmap().getHeight();
            int density = request.getBitmap().getDensity();

            // get pixels
            int[] pixels = new int[width * height];
            request.getBitmap().getPixels(pixels, 0, width, 0, 0, width, height);

            // array store rgb of each pixel
            byte[] pixelRGBs = new byte[pixels.length * 3];
            int[] shifts = new int[] {16, 8, 0};
            int rgbIndex = 0;
            for (int pixel : pixels) {
                for (int shift : shifts) {
                    pixelRGBs[rgbIndex++] = (byte) ((pixel >> shift) & 255);
                }
            }

            // convert messsage to byte
            String padMessage = request.getMessage() + "=";
            // message bytes
            byte[] messages = padMessage.getBytes(StandardCharsets.US_ASCII);
            // message 0, 1 array
            byte[] newBytes = new byte[messages.length * 8];
            for (int i = 0; i < messages.length; i++) {
                int msgByte = messages[i];
                for (int j = 0; j < 8; j++) {
                    int lastBit = msgByte & 1;
                    newBytes[i * 8 + (8 - j - 1)] = (byte) lastBit;
                    msgByte = msgByte >> 1;
                }
            }

            // hide image
            byte[] encodedImg = pixelRGBs;
            int lastBitMask = 254;
            for (int i = 0; i < pixelRGBs.length; i++) {
                if (i < newBytes.length) {
                    encodedImg[i] = (byte) ((pixelRGBs[i] & lastBitMask) | newBytes[i]);
                } else {
                    // cac bit cuoi ko lien quan thi dua ve 0
                    encodedImg[i] = (byte) (pixelRGBs[i] & lastBitMask);
                }
            }

            Bitmap encodedBitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            encodedBitmap.setDensity(density);

//            int index = 0;
            int rgbIndex2 = 0;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int alpha = 255;
                    int r = (encodedImg[rgbIndex2] & 255);
                    int g = (encodedImg[rgbIndex2 + 1] & 255);
                    int b = (encodedImg[rgbIndex2 + 2] & 255);
                    encodedBitmap.setPixel(i, j, Color.argb(alpha, r, g, b));
//                    if (index == pixels.length - 1) {
//                        break;
//                    }
//                    index = index + 1;
                    rgbIndex2 = rgbIndex2 + 3;
                }
            }

            activity.runOnUiThread(() -> callback.onSuccess(encodedBitmap));
        }).start();
    }

    public static void decode(Activity activity, DecodeRequest request, StegyCallback<String> callback) {
        new Thread(() -> {
            int width = request.getBitmap().getWidth();
            int height = request.getBitmap().getHeight();
            int density = request.getBitmap().getDensity();
            int[] pixels = new int[width * height];
            request.getBitmap().getPixels(pixels, 0, width, 0, 0, width, height);


            byte[] pixelRGBs = new byte[pixels.length * 3];
            int[] shifts = new int[] {16, 8, 0};
            int rgbIndex = 0;
            for (int pixel : pixels) {
                for (int shift : shifts) {
                    pixelRGBs[rgbIndex++] = (byte) ((pixel >> shift) & 255);
                }
            }

            byte[] lastBits = new byte[pixelRGBs.length];
            for (int i = 0; i < pixelRGBs.length; i++) {
                lastBits[i] = (byte) (pixelRGBs[i] & 1);
            }

            int pad = 8 - lastBits.length % 8;
            byte[] padBits = new byte[lastBits.length + pad];
            for (int i = 0; i < padBits.length; i++) {
                if (i < lastBits.length) {
                    padBits[i] = lastBits[i];
                } else {
                    padBits[i] = 0;
                }
            }
            int byteCnt = padBits.length / 8;
            byte[] newBytes = new byte[byteCnt];
            int index = 0;
//            int indexByte  = 0;
            for (int i = 0; i < byteCnt; i++) {
                int asemble = 0;
                int indexByte = 0;
                byte[] bitsOfByte = new byte[8];
                for(int j = 0; j < 8; j++) {
                    asemble = (asemble << 1) | padBits[index + j];
                }
                newBytes[i] = (byte) asemble;
                index  = index + 8;
            }

            // remove het ky tu 0 con thua
            int lastNonZeroIdx = newBytes.length - 1;
            while (newBytes[lastNonZeroIdx] == 0) {
                --lastNonZeroIdx;
            }

            byte[] sanitized = new byte[lastNonZeroIdx];
            for (int i = 0; i < lastNonZeroIdx; i++) {
                sanitized[i] = newBytes[i];
            }
            String msg = new String(sanitized, StandardCharsets.US_ASCII);

            activity.runOnUiThread(() -> callback.onSuccess(msg));
        }).start();
    }

    private static int encodeOnePixel(int pixel, int byteMessage) {
        return ((pixel & 255) & 254) | byteMessage;
    }

    private static int byteArrayToInt(byte[] b, int offset) {

        int value = 0x00000000;

        for (int i = 0; i < 3; i++) {
            int shift = (3 - 1 - i) * 8;
            value |= (b[i + offset] & 0x000000FF) << shift;
        }

        value = value & 0x00FFFFFF;

        return value;
    }

    public static int[] byteArrayToIntArray(byte[] b) {

        android.util.Log.v("Size byte array", b.length + "");

        int size = b.length / 3;

        android.util.Log.v("Size Int array", size + "");

        System.runFinalization();
        //Garbage collection
        System.gc();

        android.util.Log.v("FreeMemory", Runtime.getRuntime().freeMemory() + "");
        int[] result = new int[size];
        int offset = 0;
        int index = 0;

        while (offset < b.length) {
            result[index++] = byteArrayToInt(b, offset);
            offset = offset + 3;
        }

        return result;
    }
}
