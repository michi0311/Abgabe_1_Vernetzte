import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/****************************
 * Created by Michael Marolt *
 *****************************/

public class main {
    public static void main(String[] args) {
        int port;
        String baseDirectory;
        boolean isSingle = true;


        if (args.length > 0) {
            port = 80;
            baseDirectory = "/Users/michaelmarolt/Desktop/Studium/5. Semester/Vernetzte Systeme/Abgabe_1/documentRoot";
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-r")) {
                    baseDirectory = main.class.getProtectionDomain().getCodeSource().getLocation().getPath() + args[i + 1];
                    System.out.println("Directory: " + baseDirectory);
                } else if (args[i].equals("-a")) {
                    baseDirectory = args[i + 1];
                    System.out.println("Directory: " + baseDirectory);
                } else if (args[i].equals("-t")) {
                    isSingle = args[i+1].equals("single");
                } else if (args[i].equals("-p")) {
                    port = Integer.parseInt(args[i + 1]);
                    System.out.println("Port: " + port);
                } else if (args[i].equals("-h")) {
                    System.out.println("Usage:  java main -a \"/Users/michaelmarolt/Desktop/Studium/5. Semester/Vernetzte Systeme/Abgabe_1/documentRoot\" -t single -p 80");
                    System.out.println();
                    System.out.println("-a: specifies absolute File path");
                    System.out.println("-r: specifies relative File path");
                    System.out.println("-p: specifies port Number");
                    System.out.println("-t: specifies connection Type (multi,single)");
                    System.out.println("-h: Help");
                    System.out.println();

                    System.exit(0);
                }
            }
        } else {
            port = 80;
            baseDirectory = "/Users/michaelmarolt/Desktop/Studium/5. Semester/Vernetzte Systeme/Abgabe_1/documentRoot";
        }



        if (isSingle) {
            System.out.println("Server Type: Single Threaded");
            SingleThreadedServer server = new SingleThreadedServer(port, baseDirectory);
            server.run();
        } else {
            System.out.println("Server Type: Multi Threaded");
            MultiThreadedServer server = new MultiThreadedServer(port, baseDirectory);
            server.run();
        }
    }
}

class SingleThreadedServer implements Runnable {
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


class MultiThreadedServer implements Runnable {
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

    public WorkerRunnable(Socket clientSocket, String baseDirectory) {
        this.clientSocket = clientSocket;
        this.baseDirectory = baseDirectory;
    }


    @Override
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            System.out.println(new Date().toString() + " " + line + " " + clientSocket.getPort());
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

