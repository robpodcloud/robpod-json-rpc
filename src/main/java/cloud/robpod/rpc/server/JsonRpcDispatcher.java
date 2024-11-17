package cloud.robpod.rpc.server;

import cloud.robpod.rpc.JsonRpcParam;
import cloud.robpod.rpc.JsonRpcRequest;
import cloud.robpod.rpc.JsonRpcResponse;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;

public class JsonRpcDispatcher {
    private Map<String, Object> targets = new Hashtable<String, Object>();

    private Gson gson = new Gson();

    public JsonRpcResponse dispatch(JsonRpcRequest request){
        try{
            // discover method
            Object target = targets.get(request.getTarget());
            Method method = getMethod(target, request);

            // invoke method
            Object[] params = getParams(request);
            Object result = method.invoke(target, params);

            // response
            return getResult(result);

        }catch (Exception e){
            e.printStackTrace();

            // error response
            JsonRpcResponse response = new JsonRpcResponse();
            response.setError(true);
            response.setErrorMessage(e.getMessage());

            return response;

        }
    }

    private Class[] getParamsClasses(JsonRpcRequest request){
        return request.getParams().stream().map((c)->{
            try {
                return Class.forName(c.getValueClass());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).toArray(Class[]::new);
    }

    private Class getParamClass(JsonRpcParam param){
        try {
            return Class.forName(param.getValueClass());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Method getMethod(Object target, JsonRpcRequest request) throws NoSuchMethodException {
        Class[] paramClasses = getParamsClasses(request);
        return target.getClass().getMethod(request.getMethod(), paramClasses);
    }

    private Object[] getParams(JsonRpcRequest request){
        return request.getParams().stream().map((p)->{
            return gson.fromJson(p.getValue(), getParamClass(p));
        }).toArray(Object[]::new);
    }

    private JsonRpcResponse getResult(Object obj){
        JsonRpcParam result = new JsonRpcParam();

        if(obj != null){
            result.setValue(gson.toJson(obj));
            result.setValueClass(obj.getClass().getName());
        }

        JsonRpcResponse response = new JsonRpcResponse();
        response.setResult(result);

        return response;
    }

    public Map<String, Object> getTargets() {
        return targets;
    }
}
