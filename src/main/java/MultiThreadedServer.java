import StatusPages.StatusHelper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Date;
import java.util.StringTokenizer;


public class MultiThreadedServer implements Runnable {
    private int port;
    private String baseDirectory;
    private ServerSocket serverSocket;
    private boolean isStopped;

    public MultiThreadedServer (int port, String baseDirectory) {
        this.port = port;
        this.baseDirectory = baseDirectory;
        isStopped = false;
    }


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server running on port " + port);
        } catch (IOException e) {
            throw new RuntimeException("Can't open port 80", e);
        }

        while (!isStopped) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Error accepting client Connection \n" + e);
            }
            new Thread(
                    new WorkerRunnable(clientSocket, baseDirectory)
            ).start();
        }

    }
}


class WorkerRunnable implements Runnable {
    private Socket clientSocket;
    private String defaultFile = "index.html";
    private String baseDirectory;
    private BufferedReader reader;
    private OutputStream outputStream;

    public WorkerRunnable(Socket clientSocket, String baseDirectory) {
        this.clientSocket = clientSocket;
        this.baseDirectory = baseDirectory;
    }


    @Override
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            StringTokenizer tokenizer = new StringTokenizer(line);
            String method = tokenizer.nextToken();
            String uri = tokenizer.nextToken();
            String version = tokenizer.nextToken();

            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());


            System.out.println(new Date().toString() + " " + method + " " + uri + " " + version + " " + clientSocket.getPort());



            switch (method) {
                case "GET":
                    uri += uri.charAt(uri.length()-1)=='/' ? defaultFile : "";

                    File file = new File(baseDirectory + uri);


                    outputStream = clientSocket.getOutputStream();

                    String responseHeader;
                    if (file.exists()) {
                        //TODO Content Type and Content Length
                        responseHeader = StatusHelper.statusHelper("200");
                        responseHeader += "\r\n";


                    } else {
                        responseHeader = StatusHelper.statusHelper("404");
                        responseHeader += "\r\n";

                        file = new File("src/main/java/StatusPages/404.html");
                        System.out.println(file.getAbsolutePath());
                    }

                    outputStream.write(responseHeader.getBytes());
                    Files.copy(file.toPath(), outputStream);
                    outputStream.flush();
                break;
                case "POST":






                break;
                default:
                    System.out.println("Method unknown: " + method);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
                reader.close();
                clientSocket.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}


