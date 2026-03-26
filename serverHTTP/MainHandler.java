package serverHTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MainHandler implements HttpHandler {
    private static final String WEB_ROOT = "web";
    private static final int BUFFER_SIZE = 8192;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        Path filePath = Paths.get(WEB_ROOT, path);

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            String contentType = determineContentType(path);
            long fileSize = Files.size(filePath);

            exchange.getResponseHeaders().set("Content-type", contentType);
            exchange.sendResponseHeaders(200, fileSize);

            try (InputStream input = Files.newInputStream(filePath);
            OutputStream output = exchange.getResponseBody()) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
        } else {
            byte[] errorMessage = "Error 404: File not found!".getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(404, errorMessage.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorMessage);
            }
        }
    }

    private String determineContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "text/javascript; charset=UTF-8";
        if (path.endsWith(".jpg")) return "image/jpeg";
        return "application/octet-stream";
    }
}