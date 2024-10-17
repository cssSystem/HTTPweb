package sys.tem;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private String siteFilePatch = "./public";
    private Map<Request, Handler> requestResponse = new HashMap<>();
    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    ExecutorService threadPool;
    int portNumber;

    public Server(int numberOfThreads) {
        this.threadPool = Executors.newFixedThreadPool(numberOfThreads);
    }

    public void listen(int portNumber) {
        this.portNumber = portNumber;
        try (final var serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                final var socket = serverSocket.accept();
                threadPool.submit(() -> connection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSiteFilePatch() {
        return siteFilePatch;
    }

    private void connection(Socket socket) {
        try (
                socket;
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }
            final var method = parts[0];
            final var path = parts[1];
            Request request = new Request(method, path);
            if (requestResponse.containsKey(request)) {
                requestResponse.get(request).handle(request, out);
                return;
            }
            if (!validPaths.contains(path)) {
                headlineWithoutContent(out, "404", "Not Found");
                return;
            }

            theHeaderWithTheFile(out, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String patch, Handler handler) {
        Request request = new Request(method, patch);
        requestResponse.put(request, handler);
    }

    public void headlineWithoutContent(BufferedOutputStream responseStream, String responseCode, String responseStatus) throws IOException {
        responseStream.write((
                "HTTP/1.1 " + responseCode + " " + responseStatus + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        responseStream.flush();
    }

    public void theHeaderWithTheFile(BufferedOutputStream responseStream, String filePatch) throws IOException {

        final var filePath = Path.of(siteFilePatch, filePatch);
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);
        System.out.println(mimeType);
        responseStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, responseStream);
        responseStream.flush();
    }

    public void titleWithСontent(BufferedOutputStream responseStream, String mimeType, byte[] content) throws IOException {
        responseStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        responseStream.write(content);
        responseStream.flush();
    }
}
