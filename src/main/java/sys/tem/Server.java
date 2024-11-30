package sys.tem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private String siteFilePatch = "./public";
    private Map<Request, Handler> requestResponse = new HashMap<>();
    private String homeRes = "/index.html";
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
        final var allowedMethods = List.of(GET, POST);
        try (
                socket;
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            final var limit = 4096;
            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);

            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                return;
            }
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                return;
            }
            final var method = requestLine[0];
            if (!allowedMethods.contains(method)) {
                badRequest(out);
                return;
            }
            final var path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
                return;
            }

            // ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                return;
            }
            // отматываем на начало буфера
            in.reset();
            // пропускаем requestLine
            in.skip(headersStart);


            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));


            Request request;

            if (!method.equals(GET)) {
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);

                    final var body = new String(bodyBytes);
                    request = new Request(method, path, body);
                } else {
                    request = new Request(method, path);
                }

            } else {
                request = new Request(method, path);
            }

            //===consoleDemo
            System.out.println("headers " + headers);
            System.out.println("-getQueryParams()   " + request.getQueryParams());
            System.out.println("-getQueryParam(\"value\")   " + request.getQueryParam("value"));
            System.out.println("-request.getPostParams()   " + request.getPostParams());
            System.out.println("-getPostParam(\"value\")   " + request.getPostParam("value"));
            System.out.println("-request   " + request);
            //===endConsoleDemo

            if (requestResponse.containsKey(request)) {
                requestResponse.get(request).handle(request, out);
                return;
            }
            if (Objects.equals(request.getPatch(), "/")) {
                request.setPatch(homeRes);
            }
            if (!validPaths.contains(request.getPatch())) {
                headlineWithoutContent(out, "404", "Not Found");
                return;
            }

            theHeaderWithTheFile(out, request.getPatch());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String patch, Handler handler) {
        Request request = new Request(method, patch);
        requestResponse.put(request, handler);
    }

    public void headlineWithoutContent(BufferedOutputStream responseStream, String responseCode, String
            responseStatus) throws IOException {
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

    public void titleWithСontent(BufferedOutputStream responseStream, String mimeType, byte[] content) throws
            IOException {
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

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}
