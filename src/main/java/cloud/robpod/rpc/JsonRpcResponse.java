package cloud.robpod.rpc;

public class JsonRpcResponse {
    private Boolean error = false;
    private String errorMessage;
    private JsonRpcParam result;

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JsonRpcParam getResult() {
        return result;
    }

    public void setResult(JsonRpcParam result) {
        this.result = result;
    }
}
