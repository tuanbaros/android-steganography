package tuannt.simple.steganography;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import io.flutter.FlutterInjector;
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
}
