package StatusPages;

public class StatusHelper {
    public static String statusHelper(String status) {
        String out;
        switch (status) {
            case "200":
                out  = "HTTP/1.0 200 OK\r\n";
                break;
            case "404":
                out = "HTTP/1.0 404 Not Found\r\n";
                break;
            default:
                System.out.println(status + " not found");
                out = "status not found";
        }

        return out;
    }
}
