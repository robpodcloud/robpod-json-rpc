import cloud.robpod.rpc.server.JsonRpcServer;

public class TestServer implements TestInterface {

    public Integer sum(String b,String ...a){
        return 5;
    }

    public static void main(String[] args) {
        JsonRpcServer server = new JsonRpcServer(8879);
        server.getTargets().put("test", new TestServer());
        server.start();
    }
}
