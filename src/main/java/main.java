/****************************
 * Created by Michael Marolt *
 *****************************/

public class main {
    public static void main(String[] args) {
        int port;
        String baseDirectory;


        if (args.length > 0) {
            port = 80;
            baseDirectory = null;
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-r")) {
                    baseDirectory = main.class.getProtectionDomain().getCodeSource().getLocation().getPath() + args[i + 1];
                    System.out.println("Directory: " + baseDirectory);
                } else if (args[i].equals("-a")) {
                    baseDirectory = args[i + 1];
                    System.out.println("Directory: " + baseDirectory);
                } else if (args[i].equals("-p")) {
                    port = Integer.parseInt(args[i + 1]);
                    System.out.println("Port: " + port);
                } else if (args[i].equals("-h")) {
                    System.out.println("Usage:  java main -a \"/Users/michaelmarolt/Desktop/Studium/5. Semester/Vernetzte Systeme/Abgabe_1/documentRoot\" -t single -p 80");
                    System.out.println();
                    System.out.println("-a: specifies absolute File path");
                    System.out.println("-r: specifies relative File path");
                    System.out.println("-p: specifies port Number");
                    System.out.println("-h: Help");
                    System.out.println();

                    System.exit(0);
                }
            }

            if (baseDirectory == null) {
                System.out.println("No Directory given");
                System.exit(0);
            }
        } else {
            port = 80;
            baseDirectory = "/Users/michaelmarolt/Desktop/Studium/5. Semester/Vernetzte Systeme/Abgabe_1/documentRoot";
        }


        System.out.println("Server Type: Multi Threaded");
        MultiThreadedServer server = new MultiThreadedServer(port, baseDirectory);
        server.run();

    }
}