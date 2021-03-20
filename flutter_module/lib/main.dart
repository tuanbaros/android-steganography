import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_steganography/decoder.dart';
import 'package:flutter_steganography/encoder.dart';
import 'package:flutter_steganography/requests/requests.dart';

const platform = const MethodChannel('samples.flutter.dev/battery');

void main() {
  print("Init success");
  WidgetsFlutterBinding.ensureInitialized();
  platform.setMethodCallHandler((call) async {
    if (call.method == "encode") {
      Map map = call.arguments;
      encode(map["data"], map["message"], map["key"]);
    } else if (call.method == "decode") {
      Map map = call.arguments;
      decode(map["data"], map["key"]);
    }
  });
}

void encode(Uint8List data, String message, String key) async {
  try {
    EncodeRequest request = EncodeRequest(data, message, key: key);
    Uint8List response = await encodeMessageIntoImageAsync(request);
    platform.invokeMethod("success", response);
  } catch (e) {
    platform.invokeMethod("error", e.toString());
  }
}

void decode(Uint8List data, String key) async {
  try {
    DecodeRequest request = DecodeRequest(data, key: key);
    String response = await decodeMessageFromImageAsync(request);
    platform.invokeMethod("success", response);
  } catch (e) {
    platform.invokeMethod("error", e.toString());
  }
}
