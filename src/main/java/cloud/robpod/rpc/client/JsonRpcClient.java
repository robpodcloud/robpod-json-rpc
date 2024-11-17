package cloud.robpod.rpc.client;

import cloud.robpod.rpc.JsonRpcGson;
import cloud.robpod.rpc.JsonRpcParam;
import cloud.robpod.rpc.JsonRpcRequest;
import cloud.robpod.rpc.JsonRpcResponse;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;


public class JsonRpcClient<B> {
    private static final Logger LOGGER = Logger.getLogger(JsonRpcClient.class.getName());
    private final String target;
    private String host;
    private Integer port;
    private Class<B> service;


    public JsonRpcClient(Class<B> service, String target) {
        this.service = service;
        this.target = target;
    }

    public B get() {
        return (B) Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class[] { service },
                new BroadcastInvocationHandler());
    }

    public class BroadcastInvocationHandler implements InvocationHandler{

        private JsonRpcRequest getRequest(Method method, Object[] args){
            JsonRpcRequest request = new JsonRpcRequest();
            for(Object arg:args){
                JsonRpcParam param = new JsonRpcParam();
                param.setValue(JsonRpcGson.gson.toJson(arg));
                param.setValueClass(arg.getClass().getName());
                request.getParams().add(param);
            }

            request.setTarget(target);
            request.setMethod(method.getName());

            return request;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // build request
            JsonRpcRequest request = getRequest(method, args);
            String jsonRequest = JsonRpcGson.gson.toJson(request);

            LOGGER.info("sending request: "+jsonRequest);

            // send request
            Socket clientSocket = new Socket(host, port);
            clientSocket.getOutputStream().write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            clientSocket.getOutputStream().flush();

            // get response
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buffer[] = new byte[1024];
            for(int s; (s= clientSocket.getInputStream().read(buffer)) != -1; ){
                baos.write(buffer, 0, s);
            }

            String jsonResponse = baos.toString();
            LOGGER.info("received response: "+jsonResponse);


            JsonRpcResponse response = JsonRpcGson.gson.fromJson(jsonResponse, JsonRpcResponse.class);
            clientSocket.close();

            // deserialize result
            if(response.getError()){
                throw new Exception(response.getErrorMessage());
            }

            if(response.getResult().getValue() != null){
                return JsonRpcGson.gson.fromJson(response.getResult().getValue(), Class.forName(response.getResult().getValueClass()));
            }else{
                return null;
            }
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
