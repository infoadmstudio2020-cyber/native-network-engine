package com.roni.library.network;

public class NativeNetworkEngineResult<T> {
    private final boolean success;
    private final T data;
    private final String errorMessage;
    private final int errorCode;

    private NativeNetworkEngineResult(boolean success, T data, String errorMessage, int errorCode) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static <T> NativeNetworkEngineResult<T> success(T data) {
        return new NativeNetworkEngineResult<>(true, data, null, 0);
    }

    public static <T> NativeNetworkEngineResult<T> failure(int errorCode, String errorMessage) {
        return new NativeNetworkEngineResult<>(false, null, errorMessage, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "NativeNetworkEngineResult{" +
                "success=" + success +
                ", data=" + data +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorCode=" + errorCode +
                '}';
    }
}
