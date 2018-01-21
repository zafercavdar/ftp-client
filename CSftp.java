import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.io.IOException;

public class CSftp {

  public static BufferedReader ftpReader = null;
  public static BufferedWriter ftpWriter = null;
  public static Socket socket = null;


  /*
    This method sends commands to server and returns the response with proper prints.
  */
  public static void communicate(String message) throws IOException{
    System.out.println("--> " + message);
    write(message);
    String response = read();
    System.out.println("<-- " + response);
  }


  //  Client commands
  public static void user(String username) throws IOException {
    String message = "USER " + username;
    communicate(message);
  }

  public static void pw(String password) throws IOException {
    String message = "PASS " + password;
    communicate(message);
  }

  public static void quit() throws IOException {
    String message = "QUIT";
    communicate(message);
    socket.close();
    System.exit(0);
  }

  public static void get(String remote) {
    String message = "PASV " + remote;
    String message2 = "RETR " + remote;

  }

  public static void features() {
    String message = "FEAT";

  }

  public static void cd(String directory) {
    String message = "CWD " + directory;

  }

  public static void dir() {
    String message = "PASV ";
    String message2 = "LIST ";

  }


  /*
    This method reads from FTP server and exits if there is a I/O Error
  */

  public static String read() throws IOException {
    try {
      String response = ftpReader.readLine();
      return response;
    }catch (IOException e) {
      System.out.println("0xFFFD Control connection I/O error, closing control connection");
      socket.close();
      System.exit(1);
      return null;
    }
  }


  /*
    This method writes to FTP server and exits if there is a I/O Error
  */

  public static void write(String message) throws IOException {
      try {
        ftpWriter.write(message + "\n");
        ftpWriter.flush();
      }catch (IOException e) {
        System.out.println("0xFFFD Control connection I/O error, closing control connection");
        socket.close();
        System.exit(1);
      }
  }


  /*
    This method creates Socket connection, ftpReader, ftpWriter and checks if the connection is successful or not.
  */

  public static boolean connect(String server, int port) {
    try {
      socket = new Socket(server, port);
      ftpReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      ftpWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      String response = read();
      System.out.println(response);
      return response.substring(0,3).equals("220");
    } catch (Exception e) {
      System.out.println("0xFFFC Control connection to " + server + " on port " + port + " failed to open.");
      System.exit(1);
      return false;
    }
  }


  /*
    Parse arguments, get user inputs and call corresponding method.
  */
	public static void main(String[] args) {
    if (args.length ==  0) {
      System.err.println("Insufficient command line arguments. Exiting ...");
      System.exit(1);
    } else {
      String address = args[0];
      int port = 21;
      if (args.length > 1) {
        port = Integer.parseInt(args[1]);
      }
      System.out.println("Trying to connect to server " + address + " at port " + port);
      try {
        boolean connected = connect(address, port);
        if (connected) {
          System.out.println("Successfully connected.");
        } else {
          System.exit(1);
        }

        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
          System.out.print("csftp> ");

          String input = null;
          try {
              input = consoleReader.readLine();
          } catch (IOException e) {
            System.out.println("0xFFFE Input error while reading commands, terminating.");
            break;
          }

          String[] userArgs = input.split("\\s{1,}");
          if (userArgs.length == 0) {
            System.err.println("0x001 Invalid command.");
          } else {
            if (userArgs.length > 2) {
              System.err.println("0x002 Incorrect number of arguments.");
            } else {
              String command = userArgs[0];
              String param = null;
              boolean hasParam = (userArgs.length == 2);
              if (hasParam) {
                param = userArgs[1];
              }
              if (command.equals("quit") || command.equals("features") || command.equals("dir")) {
                if (hasParam) {
                  System.err.println("0x002 Incorrect number of arguments.");
                  continue;
                }
              }
              if (command.equals("user") || command.equals("pw") ||
                      command.equals("get") || command.equals("cd")) {
                if (!hasParam) {
                  System.err.println("0x002 Incorrect number of arguments.");
                  continue;
                }
              }

              switch(command) {
                case "quit":  quit(); break;
                case "features": features(); break;
                case "dir": dir(); break;
                case "user": user(param); break;
                case "pw": pw(param); break;
                case "get": get(param); break;
                case "cd": cd(param); break;
                default: System.err.println("0x001 Invalid command."); break;
              }

              if (command.equals("quit")) {
                break;
              }
            }
          }
        }
        consoleReader.close();

      } catch (Exception e) {
        e.printStackTrace(System.out);
      }
    }
	}
}
