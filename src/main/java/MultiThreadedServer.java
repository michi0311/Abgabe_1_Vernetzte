import StatusPages.StatusHelper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;


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
            }
            catch ( IOException ioe )
            {
                System.out.println( "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }



            String method = pre.getProperty("method");
            String uri = pre.getProperty("uri");

            System.out.println(header.toString());
            System.out.println(pre.toString());
            System.out.println(parms.toString());







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
                    outputStream = clientSocket.getOutputStream();
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
                    } else if (header.getProperty("content-type").equals("multipart/form-data")) {

                    }

                    outputStream.write(("HTTP/1.0 200 OK\r\n" + "\r\n").getBytes());
                    outputStream.write((htmlOut[0] + htmlEnd).getBytes());
                    //Files.copy(file.toPath(), outputStream);
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


    /**
     * Decodes the percent encoding scheme. <br/>
     * For example: "an+example%20string" -> "an example string"
     */
    private String decodePercent( String str ) throws InterruptedException
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
            System.out.println( "BAD REQUEST: Bad percent-encoding." );
            return null;
        }
    }


    private void decodeParms( String parms, Properties p )
            throws InterruptedException
    {
        if ( parms == null )
            return;

        StringTokenizer st = new StringTokenizer( parms, "&" );
        while ( st.hasMoreTokens())
        {
            String e = st.nextToken();
            int sep = e.indexOf( '=' );
            if ( sep >= 0 )
                p.put( decodePercent( e.substring( 0, sep )).trim(),
                        decodePercent( e.substring( sep+1 )));
        }
    }





}


