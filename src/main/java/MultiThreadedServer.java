import Helpers.MimeTypes;
import Helpers.StatusHelper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;


//todo content-type
//todo content-length

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
            /*
            String line = reader.readLine();
            StringTokenizer tokenizer = new StringTokenizer(line);
            String method = tokenizer.nextToken();
            String uri = tokenizer.nextToken();
            String version = tokenizer.nextToken();

            System.out.println(new Date().toString() + " " + method + " " + uri + " " + version + " " + clientSocket.getPort());
/*
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());*/

            Properties pre = new Properties();
            Properties parms = new Properties();
            Properties header = new Properties();
            Properties files = new Properties();

            try {
                // Read the request line
                String inLine = reader.readLine();
                if (inLine == null) return;
                StringTokenizer st = new StringTokenizer( inLine );
                if ( !st.hasMoreTokens())
                    System.out.println("BAD REQUEST: Syntax error. Usage: GET /example/file.html" );

                String method = st.nextToken();
                pre.put("method", method);

                if ( !st.hasMoreTokens())
                    System.out.println( "BAD REQUEST: Missing URI. Usage: GET /example/file.html" );

                String uri = st.nextToken();

                // Decode parameters from the URI
                int qmi = uri.indexOf( '?' );
                if ( qmi >= 0 )
                {
                    decodeParms( uri.substring( qmi+1 ), parms );
                    uri = decodePercent( uri.substring( 0, qmi ));
                }
                else uri = decodePercent(uri);

                // If there's another token, it's protocol version,
                // followed by HTTP headers. Ignore version but parse headers.
                // NOTE: this now forces header names lowercase since they are
                // case insensitive and vary by client.
                if ( st.hasMoreTokens())
                {
                    String line = reader.readLine();
                    while ( line != null && line.trim().length() > 0 )
                    {
                        int p = line.indexOf( ':' );
                        if ( p >= 0 )
                            header.put( line.substring(0,p).trim().toLowerCase(), line.substring(p+1).trim());
                        line = reader.readLine();
                    }
                }

                pre.put("uri", uri);

                pre.put("version", st.nextToken());
            }
            catch ( IOException ioe )
            {
                System.out.println( "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }



            String method = pre.getProperty("method");
            String uri = pre.getProperty("uri");
            String version = pre.getProperty("version");

            System.out.println(new Date().toString() + " " + method + " " + uri + " " + version + " " + clientSocket.getPort());

            System.out.println(header.toString());
            System.out.println(pre.toString());
            System.out.println(parms.toString());



            switch (method) {
                case "GET":
                    uri += uri.charAt(uri.length()-1)=='/' ? defaultFile : "";

                    File file = new File(baseDirectory + uri);


                    String responseHeader;
                    if (file.exists()) {
                        responseHeader = StatusHelper.statusHelper("200");
                    } else {
                        responseHeader = StatusHelper.statusHelper("404");
                        file = new File("src/main/java/Helpers/404.html");
                    }

                    String responseContentType = "Content-Type:" + MimeTypes.mimeTypesHelper(file.getName().split("\\.")[1]) + "\r\n";


                    SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
                    date.setTimeZone(TimeZone.getTimeZone("GMT"));

                    outputStream = clientSocket.getOutputStream();
                    outputStream.write(responseHeader.getBytes());
                    outputStream.write(responseContentType.getBytes());
                    outputStream.write(("Content-Length: " + file.length() + "\r\n").getBytes());
                    outputStream.write(("Date: " + date.format(new Date()) + "\r\n").getBytes());
                    outputStream.write("Server: NicerServer/0.1\r\n".getBytes());
                    outputStream.write("\r\n".getBytes());
                    Files.copy(file.toPath(), outputStream);
                    outputStream.flush();
                break;
                case "POST":
                    final String[] htmlOut = {"<!DOCTYPE html>\n<html>\n<head>\n<title>Example</title>\n</head>\n<body>\n"};
                    String htmlEnd = "</body>\n</html>";


                    if (parms.size() > 0) {
                        parms.forEach((key, value) -> {
                            htmlOut[0] += ("<p> Received form variable with name <b>" + key.toString() + "</b> and value <b>" + value.toString() + "</b>.</p>\n");
                        });
                    }

                    if (header.getProperty("content-type").equals("application/x-www-form-urlencoded")) {
                        StringBuilder payload = new StringBuilder();
                        while(reader.ready()){
                            payload.append((char) reader.read());
                        }

                        String[] payloadArray = payload.toString().split("&");
                        for (String s: payloadArray) {
                            String[] query = s.split("=");
                            htmlOut[0] += ("<p> Received form variable with name <b>" + query[0] + "</b> and value <b>" + query[1] + "</b>.</p>\n");
                        }
                    } else if (header.getProperty("content-type").split(";")[0].equals("multipart/form-data")) {
                        System.out.println("nice");
                        StringBuilder payload = new StringBuilder();
                        while(reader.ready()){
                            payload.append((char) reader.read());
                        }

                        String boundary = "--" + header.getProperty("content-type").split("boundary")[1].replace("=","");

                        String[] payloadArray = payload.toString().split(boundary);
                        for (String s: payloadArray) {
                            if (s.length() > 4) {
                                //System.out.println(s);
                                String[] st = s.split("name=\"")[1].split("\"");
                                String name = st[0];
                                String val =st[1].replaceAll("\\s+","");
                                htmlOut[0] += ("<p> Received form variable with name <b>" + name + "</b> and value <b>" + val + "</b>.</p>\n");
                            }
                        }


                    }

                    htmlOut[0] += htmlEnd;


                    outputStream = clientSocket.getOutputStream();
                    outputStream.write(("HTTP/1.0 200 OK\r\n" + "\r\n").getBytes());
                    outputStream.write((htmlOut[0]).getBytes());
                    outputStream.flush();



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


    private String decodePercent( String str )
    {
        try
        {
            StringBuffer sb = new StringBuffer();
            for( int i=0; i<str.length(); i++ )
            {
                char c = str.charAt( i );
                switch ( c )
                {
                    case '+':
                        sb.append( ' ' );
                        break;
                    case '%':
                        sb.append((char)Integer.parseInt( str.substring(i+1,i+3), 16 ));
                        i += 2;
                        break;
                    default:
                        sb.append( c );
                        break;
                }
            }
            return sb.toString();
        }
        catch( Exception e )
        {
            return null;
        }
    }


    private void decodeParms( String params, Properties p ) {
        if ( params == null ) return;

        StringTokenizer st = new StringTokenizer( params, "&" );
        while ( st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf( '=' );
            if ( sep >= 0 )
                p.put( decodePercent( e.substring( 0, sep )).trim(),
                        decodePercent( e.substring( sep+1 )));
        }
    }
}


