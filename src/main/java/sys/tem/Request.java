package sys.tem;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class Request {
    private String method;
    private String patch;
    private Map<String, ArrayList<String>> getParam = new HashMap<>();
    private Map<String, ArrayList<String>> postParam = new HashMap<>();
    private String requestBody;

    public Request(String method, String patch, String requestBody) {
        this.method = method;
        patchAndParam(patch);
        this.requestBody = requestBody;
        this.postParam = params(requestBody);
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
            this.getParam = params(patch.substring(sep + 1));
        }
    }

    private Map params(String stParam) {
        List<NameValuePair> params;
        Map<String, ArrayList<String>> resParam = new HashMap<>();
        try {
            params = URLEncodedUtils.parse(new URI("?" + stParam), "UTF-8");
            for (NameValuePair param : params) {
                if (param.getName() != null && param.getValue() != null) {
                    if (resParam.containsKey(param.getName())) {
                        resParam.get(param.getName()).add(param.getValue());
                    } else {
                        resParam.put(param.getName(), new ArrayList<>());
                        resParam.get(param.getName()).add(param.getValue());
                    }
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return resParam;
    }

    public String getPatch() {
        return patch;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", patch='" + patch + '\'' +
                ", getParam=" + getParam +
                ", postParam=" + postParam +
                ", requestBody='" + requestBody + '\'' +
                '}';
    }

    public List getQueryParam(String name) {
        if (getParam.containsKey(name)) {
            return getParam.get(name);
        }
        return null;
    }

    public Map getQueryParams() {
        return getParam;
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

    public Map getPostParams() {
        return postParam;
    }

    public List getPostParam(String name) {
        if (postParam.containsKey(name)) {
            return postParam.get(name);
        }
        return null;
    }


}
