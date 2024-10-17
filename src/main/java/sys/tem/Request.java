package sys.tem;

import java.util.Objects;

public class Request {
    private String method;
    private String patch;
    private String requestBody;

    public Request(String method, String patch, String requestBody) {
        this.method = method;
        this.patch = patch;
        this.requestBody = requestBody;
    }

    public String getMethod() {
        return method;
    }

    public String getPatch() {
        return patch;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public Request(String method, String patch) {
        this.method = method;
        this.patch = patch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(method, request.method) && Objects.equals(patch, request.patch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, patch);
    }
}
