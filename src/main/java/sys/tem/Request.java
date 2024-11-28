package sys.tem;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Request {
    private String method;
    private String patch;
    private Map<String, String> param = new HashMap<>();
    private String requestBody;

    public Request(String method, String patch, String requestBody) {
        this.method = method;
        patchAndParam(patch);

        this.requestBody = requestBody;
    }

    public String getMethod() {
        return method;
    }

    private void patchAndParam(String patch) {
        int sep = patch.indexOf("?");
        if (sep < 0) {
            this.patch = patch;
        } else {
            this.patch = patch.substring(0, sep);
            List<NameValuePair> params;
            try {
                params = URLEncodedUtils.parse(new URI(patch), "UTF-8");
                for (NameValuePair param : params) {
                    if (param.getName() != null && param.getValue() != null) {
                        this.param.put(param.getName(), param.getValue());
                    }
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getPatch() {
        return patch;
    }

    public String getRequestBody() {
        return requestBody;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", patch='" + patch + '\'' +
                ", param='" + param + '\'' +
                ", requestBody='" + requestBody + '\'' +
                '}';
    }

    public String getQueryParam(String name) {
        if (param.containsKey(name)) {
            return param.get(name);
        }
        return null;
    }

    public Map getQueryParams() {
        return param;
    }

    public Request(String method, String patch) {
        this.method = method;
        patchAndParam(patch);
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
