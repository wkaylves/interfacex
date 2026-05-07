package com.kaylves.interfacex.module.http.method;

import lombok.Getter;
import lombok.Setter;

/**
 * @author kaylves
 */
@Setter
@Getter
public class RequestPath {

    String path;

    String method;

    public RequestPath(String path, String method) {
        this.path = path;
        this.method = method;
    }

    public void concat(RequestPath classRequestPath) {
        String classUri = classRequestPath.getPath();
        String methodUri = this.path;

        if (!classUri.startsWith("/")) {
            classUri = "/".concat(classUri);
        }

        if (!classUri.endsWith("/")) {
            classUri = classUri.concat("/");
        }

        if (this.path.startsWith("/")) {
            methodUri = this.path.substring(1);
        }

        this.path = classUri.concat(methodUri);
    }

    @Override
    public String toString() {
        return "RequestPath{" +
                "path='" + path + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
