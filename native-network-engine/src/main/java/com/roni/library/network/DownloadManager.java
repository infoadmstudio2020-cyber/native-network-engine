package com.roni.library.network;

import com.roni.library.network.http.HttpClientEngine;
import com.roni.library.network.http.HttpMethod;
import com.roni.library.network.http.HttpRequest;
import com.roni.library.network.http.HttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DownloadManager {
    public interface DownloadListener {
        void onProgress(long bytesRead, long totalBytes);
        void onSuccess(File file);
        void onFailure(Throwable t);
    }

    private final HttpClientEngine clientEngine;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public DownloadManager(HttpClientEngine clientEngine) {
        this.clientEngine = clientEngine != null ? clientEngine : new HttpClientEngine();
    }

    public DownloadManager() {
        this(new HttpClientEngine());
    }

    public Future<?> download(String url, File destination, DownloadListener listener) {
        return executor.submit(() -> {
            try {
                HttpRequest req = HttpRequest.builder()
                        .setUrl(url)
                        .setMethod(HttpMethod.GET)
                        .build();
                HttpResponse resp = clientEngine.execute(req);
                if (!resp.isSuccessful()) {
                    throw new RuntimeException("Download failed with HTTP " + resp.getStatusCode());
                }

                byte[] data = resp.getBody().getBytes(StandardCharsets.UTF_8);
                if (destination.getParentFile() != null) {
                    destination.getParentFile().mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(destination)) {
                    fos.write(data);
                    fos.flush();
                }
                if (listener != null) {
                    listener.onProgress(data.length, data.length);
                    listener.onSuccess(destination);
                }
            } catch (Throwable t) {
                if (listener != null) {
                    listener.onFailure(t);
                }
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
