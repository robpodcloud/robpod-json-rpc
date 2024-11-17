package cloud.robpod.rpc;

import java.util.ArrayList;
import java.util.List;

public class JsonRpcRequest {
    private String target;
    private String method;
    private List<JsonRpcParam> params = new ArrayList<>();

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<JsonRpcParam> getParams() {
        return params;
    }
}
