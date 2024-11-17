package cloud.robpod.rpc.server;

import cloud.robpod.rpc.JsonRpcGson;
import cloud.robpod.rpc.JsonRpcRequest;
import cloud.robpod.rpc.JsonRpcResponse;
import cloud.robpod.rpc.client.JsonRpcClient;
import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

public class JsonRpcServer {
    private static final Logger LOGGER = Logger.getLogger(JsonRpcServer.class.getName());

    private JsonRpcDispatcher dispatcher = new JsonRpcDispatcher();

    public Map<String, Object> getTargets() {
        return dispatcher.getTargets();
    }

    private Integer port;

    public JsonRpcServer(Integer port) {
        this.port = port;
    }

    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket server = new ServerSocket(port);
                    server.setReuseAddress(true);

                    while (true) {

                        Socket client = server.accept();
                        LOGGER.info("New connection "+client.getInetAddress().getHostAddress());

                        ClientHandler clientSock = new ClientHandler(client);
                        new Thread(clientSock).start();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class ClientHandler implements Runnable {
        private final Socket clientSocket;
        public ClientHandler(Socket socket){
            this.clientSocket = socket;
        }

        public void run(){
            OutputStream out = null;
            InputStream in = null;
            try {

                // get the output stream of client
                out = clientSocket.getOutputStream();

                // get the input stream of client
                in = clientSocket.getInputStream();

                // read request
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte buffer[] = new byte[1024];
                while(true) {
                    int n = in.read(buffer);
                    System.out.println(n);

                    baos.write(buffer,0,n);
                    if( n < 1024) break;
                }

                // deserialize request
                JsonRpcRequest request = JsonRpcGson.gson.fromJson(baos.toString(), JsonRpcRequest.class);

                // process request
                JsonRpcResponse response = JsonRpcServer.this.dispatcher.dispatch(request);

                // serialize response
                String responseJson = JsonRpcGson.gson.toJson(response);

                // send response
                out.write(responseJson.getBytes(StandardCharsets.UTF_8));
                out.flush();

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }

                    clientSocket.close();

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
