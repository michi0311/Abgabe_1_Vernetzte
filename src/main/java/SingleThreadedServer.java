import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/****************************
 * Created by Michael Marolt *
 *****************************/

public class SingleThreadedServer implements Runnable{
    private int port;
    private String baseDirectory;
    private ServerSocket serverSocket;
    private boolean isStopped;
    private String defaultFile = "index.html";

    public SingleThreadedServer (int port, String baseDirectory) {
        this.port = port;
        this.baseDirectory = baseDirectory;
        isStopped = false;
    }


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server running on port " + serverSocket.getLocalPort());
        } catch (IOException e) {
            throw new RuntimeException("Can't open port 80", e);
        }

        while (!isStopped) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Error accepting client Connection \n" + e);
            }

            try {
                InputStream input = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line = reader.readLine();
                System.out.println(line + clientSocket.getPort());
                String fileLocation = line.split(" ")[1];

                fileLocation += fileLocation.equals("/") ? defaultFile : "";

                File file = new File(baseDirectory + fileLocation);

                FileInputStream fileIn = null;
                byte[] fileData = new byte[(int) file.length()];

                try {
                    fileIn = new FileInputStream(file);
                    fileIn.read(fileData);
                } finally {
                    if (fileIn != null) {
                        fileIn.close();
                    }
                }

                OutputStream outputStream = clientSocket.getOutputStream();
                outputStream.write(fileData,0,fileData.length);
                outputStream.flush();
                clientSocket.close();

            } catch (Exception e) {

            }
        }
    }
}
