package sys.tem;

import java.io.IOException;

public class Main {
    private static final int PORT = 9999;
    private static final int NUM_TREADS = 64;

    public static void main(String[] args) throws IOException {
        Server server = new Server(NUM_TREADS);
        server.start(PORT);
    }
}

