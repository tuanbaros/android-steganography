# android_steganography

`android_steganography` is a simple module for fully transparent hiding any string within an image. This technique is known as LSB (Least Significant Bit) [steganography](https://en.wikipedia.org/wiki/steganography) 

## Require
* API 21 or higher
* Use Android X

## Usage

### Dependency
Add to ``root/build.gradle``

```groovy
repositories {
    jcenter()
    maven { url 'https://jitpack.io' }
  }
```

Include the library in your ``app/build.gradle``

```groovy
dependencies{
    compile 'com.github.tuanbaros:android-steganography:master-SNAPSHOT'
}
```

### Initial
```java
import tuannt.simple.steganography.Stegy;

Stegy.init("your context");
```

### How to encode message into an image?
- Create an `EncodeRequest`
```java
EncodeRequest encodeRequest = new EncodeRequest(bitmap, "your message", "your key");
```

- Encode and callback
```java
Stegy.encodeStringIntoImage(MainActivity.this, encodeRequest, new StegyCallback<Bitmap>() {
    @Override
    public void onSuccess(Bitmap data) {
        // todo
    }

    @Override
    public void onError(String error) {
        // todo
    }
});

```

### How to decode message from an image?
- Create a `DecodeRequest`
```java
DecodeRequest decodeRequest = new DecodeRequest(bitmap, "your key");
```

- Decode and callback
```java
Stegy.decodeStringFromImage(MainActivity.this, decodeRequest, new StegyCallback<String>() {
    @Override
    public void onSuccess(String data) {
        // todo
    }

    @Override
    public void onError(String error) {
        // todo
    }
});
```

## References
- https://en.wikipedia.org/wiki/Steganography
- https://securitydaily.net/gioi-thieu-ky-thuat-giau-tin-trong-anh-steganography-phan-2/
