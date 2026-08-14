package nerd.amara;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Http {
    private static final HttpClient client = HttpClient.newBuilder()
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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                try {
                    return HttpResult.ok(gson.fromJson(response.body(), responseType));
                } catch (JsonSyntaxException e) {
                    System.err.println("Réponse JSON invalide sur URL : " + url);
                    return HttpResult.error(Status.PARSE_ERROR);
                }
            } else if (response.statusCode() == 404) {
                return HttpResult.error(Status.NOT_FOUND);
            } else if (response.statusCode() >= 500) {
                System.err.println("Erreur serveur " + response.statusCode() + " sur URL : " + url);
                return HttpResult.error(Status.SERVER_ERROR);
            } else {
                System.err.println("HTTP erreur " + response.statusCode() + " sur URL : " + url);
                return HttpResult.error(Status.SERVER_ERROR);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Erreur réseau sur URL " + url + " : " + e.getMessage());
            return HttpResult.error(Status.NETWORK_ERROR);
        }
    }
}