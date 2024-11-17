import cloud.robpod.rpc.client.JsonRpcClient;

public class TestClient {

    public static void main(String[] args) {
        JsonRpcClient<TestInterface> client = new JsonRpcClient<TestInterface>(TestInterface.class, "test");
        client.setHost("127.0.0.1");
        client.setPort(8879);
        int t = client.get().sum("a", new String[]{"b", "c"});

        System.out.println(t);
    }
}
