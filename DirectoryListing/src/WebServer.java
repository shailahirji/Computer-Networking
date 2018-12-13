import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class WebServer {
    // this is the port the web server listens on
    private static final int PORT_NUMBER = 8080;

    // main entry point for the application
    public static void main(String args[]) {
        try {
            // open socket
            ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);

            // start listener thread
            Thread listener = new Thread(new SocketListener(serverSocket));
            listener.start();

            // message explaining how to connect
            System.out.println("To connect to this server via a web browser, try \"http://127.0.0.1:8080/{url to retrieve}\"");

            // wait until finished
            System.out.println("Press enter to shutdown the web server...");
            Console cons = System.console();
            String enterString = cons.readLine();

            // kill listener thread
            listener.interrupt();

            // close the socket
            serverSocket.close();
        } catch (Exception e) {
            System.err.println("WebServer::main - " + e.toString());
        }
    }
}

class SocketListener implements Runnable {
    private ServerSocket serverSocket;

    public SocketListener(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    // this thread listens for connections, launches a seperate socket connection
    //  thread to interact with them
    public void run() {
        while (!this.serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread connection = new Thread(new SocketConnection(clientSocket));
                connection.start();
                Thread.yield();
            } catch (IOException e) {
                if (!this.serverSocket.isClosed()) {
                    System.err.println("SocketListener1::run - " + e.toString());
                }
            }
        }
    }
}

class SocketConnection implements Runnable {
    private final String HTTP_LINE_BREAK = "\r\n";

    private Socket clientSocket;

    public SocketConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    // one of these threads is spawned and used to talk to each connection
    public void run() {
        try {
            BufferedReader request = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            PrintWriter response = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.handleConnection(request, response);
        } catch (IOException e) {
            System.err.println("SocketConnection1::run - " + e.toString());
        }
    }

    // TODO: implement your HTTP protocol for this server within this method
    private void handleConnection(BufferedReader request, PrintWriter response) {
        try {
            /*
             *
             * TODO: implement your web server here
             * 
             * Tasks:
             * ------
             * 1.) Figure out how to parse the HTTP request header
             * 2.) Figure out how to open files
             * 3.) Figure out how to create an HTTP response header
             * 4.) Figure out how to return resources (i.e. files)
             * 
             */

            /*
            1. parse the http request header
             */
            String [] request_parsing=parse_request(request);
            String method=request_parsing[0];
            String file_name=request_parsing[1];


            List<Path> subPathList = get_directories();

            /*
            2. How to open files
            3. createResponse
            4. return resources
             */
            sendResponse(method,file_name, subPathList, response, request);

            this.clientSocket.close();

        } catch (IOException e) {
            System.err.println("SocketConnection1::handleConnection: " + e.toString());
        }
    }


    private List<Path> get_directories() {
        List<Path> subPathList = new ArrayList<>();//possible paths accessible from webroot

        //collect all reachable files from root
        Path path = Paths.get("webroot");
        try (Stream<Path> subPaths = Files.walk(path)) {
            subPathList = subPaths.filter(Files::isRegularFile).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return subPathList;
    }

    private String[] parse_request(BufferedReader request){
        String message = this.readHTTPHeader(request);
        //get only header
        String []header=message.split("\r\n");
        //0th index contains the header

        Pattern pattern= Pattern.compile("(.*)\\s(.*)\\s.*");
        Matcher match=pattern.matcher(header[0]);
        String file_name="";
        String method="";

        if(match.find()){
            method=match.group(1);
            file_name=match.group(2);
        }

        String [] result= {method,file_name};
        return result;
    }

    /*
    This method prepares a response message based on the requested file, method of request and whether or not request source exists
     */
    private void sendResponse(String method,String file_name, List<Path> pathList, PrintWriter response, BufferedReader request) throws IOException {

        Path default_path = Paths.get("webroot" + file_name + "/index.html");//if index.html file is available, respond with that, default
        Path subfolder_path = Paths.get("webroot" + file_name + "/.DS_Store");//for if there is a subfolder
        Path file_path=Paths.get("webroot"+file_name);//for if theres a simple txt file. Used for current and subfolder

            /*
            Case 1: default html exists
             */
        if (pathList.contains(default_path)) {

            response.println(responseWithSource(default_path,method).toString());//will return a string builder with response content

            response.close();
            request.close();

        }
        /*
        Case 2: when accessing home directory with only /
        */
        else if (file_name.equals("/")) {


            response.print(responseNoSource().toString());
            System.out.println(responseNoSource().toString());

            String name = null;
            StringBuilder html_directory = new StringBuilder();
            for (Path x : pathList) {
                String dir = x.toString();
                Pattern pattern_folder = Pattern.compile(".*(/.*)/.DS_Store");//only print main folders not their contents
                Matcher match_folder = pattern_folder.matcher(dir);
                if (match_folder.find()) {
                    name = match_folder.group(1);
                    html_directory.append("<a href=\"" + name + "\">" + name.substring(1) + "</a><br>");
                }

            }
            if(method.equals("GET")) {
                response.println(html_directory.toString());
                System.out.println(html_directory.toString());
            }
            response.close();
            request.close();

        }
            /*
            Case 3: accessing sub folder
             */
        else if (pathList.contains(subfolder_path)) {//just subdirectory list it out


            response.print(responseNoSource().toString());
            System.out.println(responseNoSource().toString());

            String name = null;
            StringBuilder html_directory = new StringBuilder();
            for (Path x : pathList) {
                String dir = x.toString();
                Pattern pattern = Pattern.compile("webroot" + file_name + "(/.*)");//only print main files not their contents
                Matcher match = pattern.matcher(dir);
                if (match.find()) {
                    name = match.group(1);
                    if (!name.equals("/.DS_Store"))
                        html_directory.append("<a href=\" " + file_name + name + "\">" + name.substring(1) + "</a><br>");
                }
            }

            if(method.equals("GET")) {
                response.println(html_directory.toString());
                System.out.println(html_directory.toString());
            }
            response.close();
            request.close();

        }/*
        case 4: accessing file inside subfolder
        */
        else if(pathList.contains(file_path)){//for trying to access a inside subfolder

            response.println(responseWithSource(file_path,method).toString());
            response.close();
            request.close();

        }   /*
            case 4: file not found
            */
        else{
            //if doesnt exist at all
            StringBuilder response_builder = new StringBuilder(50);
            response_builder.append("HTTP/1.1 ").append("404 file not found").append(HTTP_LINE_BREAK);
            response_builder.append("Connection: Closed" + HTTP_LINE_BREAK+HTTP_LINE_BREAK);

            System.out.println(response_builder.toString());

            response.print(response_builder.toString());
            response.println("<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>404 Page Not Found</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <h1>Page "+file_name +" not Found</h1>\n" +
                    "<img src=\"https://www.noupe.com/wp-content/uploads/2015/02/46_creative_404_signum-error.jpg\" width=100% height=100%>\n"+
                    "</body>\n" +
                    "</html>");

            response.close();
            request.close();
        }

    }

    private String readHTTPHeader(BufferedReader reader)
    {
        String message = "";
        String line = "";
        while ((line != null) && (!line.equals(this.HTTP_LINE_BREAK)))
        {
            line = this.readHTTPHeaderLine(reader);
            message += line;
        }
        return message;
    }

    private String readHTTPHeaderLine(BufferedReader reader) {
        String line = "";
        try {
            line = reader.readLine() + this.HTTP_LINE_BREAK;
        } catch (IOException e) {
            System.err.println("SocketConnection1::readHTTPHeaderLine: " + e.toString());
            line = "";
        }
        return line;
    }

    /*
    This method prepares a response for when a source document link a html file or txt file exists for the request
     */
    private StringBuilder responseWithSource(Path path_to_doc, String method) throws IOException {
        String response_content = path_to_doc.toString();
        //date formating used for last modified and response date
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        //get file content into byte array
        File file = new File(response_content);
        byte[] fileContent = Files.readAllBytes(file.toPath());

        //content as a string
        String str = new String(fileContent, "UTF-8");
        URLConnection connection = file.toURI().toURL().openConnection();
        String mimeType = connection.getContentType();
        //prepare response header

        StringBuilder response_builder = new StringBuilder(50 + fileContent.length);
        response_builder.append("HTTP/1.1 ").append("202 OK").append(HTTP_LINE_BREAK);
        response_builder.append("Date: " + dateFormat.format(date) + " GMT" + HTTP_LINE_BREAK);
        response_builder.append("Server: " + InetAddress.getLocalHost().getHostName() + HTTP_LINE_BREAK);
        response_builder.append("Last-Modified: " + dateFormat.format(file.lastModified()) + " GMT" + HTTP_LINE_BREAK);
        response_builder.append("Content-length: " + fileContent.length + HTTP_LINE_BREAK);
        response_builder.append("Content-Type: " + mimeType + HTTP_LINE_BREAK);
        response_builder.append("Connection: Closed" + HTTP_LINE_BREAK+HTTP_LINE_BREAK);

        if(method.equals("GET")){
            response_builder.append(str);//add the body
//          response.print(response_builder.toString());
            System.out.println(response_builder.toString());

            return response_builder;
        }else {
            //response.print(response_builder.toString());
            System.out.println(response_builder.toString());
            return response_builder;
        }
    }

    /*
    This method prepares a response for when a source document doesnt exist, e.g. contents o
     */
    private StringBuilder responseNoSource(){
        StringBuilder response_builder = new StringBuilder(50);
        response_builder.append("HTTP/1.1 ").append("202 OK").append(HTTP_LINE_BREAK);
        response_builder.append("Content-Type:text/html " + HTTP_LINE_BREAK);
        response_builder.append("Connection: Closed" + HTTP_LINE_BREAK + HTTP_LINE_BREAK);
        return response_builder;
    }
}
