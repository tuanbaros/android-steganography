package tuannt.simple.steganography;

public interface StegyCallback<T> {
    void onSuccess(T data);
    void onError(String error);
}
