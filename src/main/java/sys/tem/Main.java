package sys.tem;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    private static final int PORT = 9999;
    private static final int NUM_TREADS = 64;

    public static void main(String[] args) {
        final var server = new Server(NUM_TREADS);
        // код инициализации сервера (из вашего предыдущего ДЗ)

        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
                try {
                    final var REPLACE_CLASSIC_FILE = "classic.html";
                    final var filePath = Path.of(server.getSiteFilePatch(), REPLACE_CLASSIC_FILE);
                    final var template = Files.readString(filePath);
                    final var mimeType = Files.probeContentType(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    server.titleWithСontent(responseStream, mimeType, content);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            // TODO: handlers code
            try {
                server.headlineWithoutContent(responseStream, "400", "bad request");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.listen(PORT);
    }
}

