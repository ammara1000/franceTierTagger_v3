package nerd.amara;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Http {
    private static final Logger LOGGER = LoggerFactory.getLogger("FranceTiersHttp");
    private static final String USER_AGENT = "FranceTiersTagger/1.0.0";

    private static final ExecutorService HTTP_EXECUTOR = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "francetiers-http");
        t.setDaemon(true);
        return t;
    });

    private static final HttpClient client = HttpClient.newBuilder()
            .executor(HTTP_EXECUTOR)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final Gson gson = new Gson();

    public enum Status {
        OK,
        NOT_FOUND,
        SERVER_ERROR,
        NETWORK_ERROR,
        PARSE_ERROR
    }

    public static class HttpResult<T> {
        public final Status status;
        public final T data;

        private HttpResult(Status status, T data) {
            this.status = status;
            this.data = data;
        }

        static <T> HttpResult<T> ok(T data) {
            return new HttpResult<>(Status.OK, data);
        }

        static <T> HttpResult<T> error(Status status) {
            return new HttpResult<>(status, null);
        }
    }

    public static <T> HttpResult<T> getJson(String url, Class<T> responseType) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                try {
                    return HttpResult.ok(gson.fromJson(response.body(), responseType));
                } catch (JsonSyntaxException e) {
                    LOGGER.warn("Réponse JSON invalide sur URL : {}", url, e);
                    return HttpResult.error(Status.PARSE_ERROR);
                }
            } else if (response.statusCode() == 404) {
                return HttpResult.error(Status.NOT_FOUND);
            } else if (response.statusCode() >= 500) {
                LOGGER.warn("Erreur serveur {} sur URL : {}", response.statusCode(), url);
                return HttpResult.error(Status.SERVER_ERROR);
            } else {
                LOGGER.warn("HTTP erreur {} sur URL : {}", response.statusCode(), url);
                return HttpResult.error(Status.SERVER_ERROR);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Requête interrompue sur URL {}", url);
            return HttpResult.error(Status.NETWORK_ERROR);
        } catch (Exception e) {
            LOGGER.warn("Erreur réseau sur URL {} : {}", url, e.getMessage());
            return HttpResult.error(Status.NETWORK_ERROR);
        }
    }

    public static byte[] getBytes(String url, Duration timeout) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .timeout(timeout)
                .GET()
                .build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 200) {
            return response.body();
        }
        throw new IOException("HTTP " + response.statusCode());
    }
}
