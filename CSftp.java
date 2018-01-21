import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;

public class CSftp {

  public static BufferedReader controlReader = null;
  public static BufferedWriter controlWriter = null;
  public static BufferedReader dataReader = null;

  public static Socket controlSocket = null;
  public static Socket dataSocket = null;

  public enum ConnectionType {
    DATACONNECTION, CONTROLCONNECTION
  }

  /*
    Closes established connections
  */

  public static void closeSockets() throws IOException {
    if (controlSocket != null) {
      controlSocket.close();
    }
    if (dataSocket != null) {
      dataSocket.close();
    }
  }

  // Print array with prefix
  public static void printArray(String[] arr, String prefix) {
    for(int i = 0; i < arr.length; i++) {
      String line = arr[i];
      System.out.println(prefix + line);
    }
  }

  /*
    This method sends commands to server and returns the response with proper prints.
  */
  public static String[] communicate(ConnectionType readFrom, String message) throws IOException{
    System.out.println("--> " + message);
    write(message);
    String[] controlConnectionResponse = read(ConnectionType.CONTROLCONNECTION);

    printArray(controlConnectionResponse, "<-- ");

    if (readFrom == ConnectionType.DATACONNECTION) {
      String[] dataConnectionResponse = read(readFrom);
      printArray(dataConnectionResponse, "");

      // read control connection once again to get final message
      controlConnectionResponse = read(ConnectionType.CONTROLCONNECTION);
      printArray(controlConnectionResponse, "<-- ");
    }

    return controlConnectionResponse;
  }

  // PASSIVE Mode
  public static void passiveMode() throws IOException {
    String message = "PASV ";
    String response = communicate(ConnectionType.CONTROLCONNECTION, message)[0];
    int s = response.indexOf("(") + 1;
    int f = response.indexOf(")");
    String IPWithPort = response.substring(s, f);
    String[] octatList = IPWithPort.split(",");
    String IPAddress = "";
    int port = 0;
    for (int i = 0; i < octatList.length; i++) {
      if (i < 3) {
        IPAddress += octatList[i] + ".";
      } else if (i == 3) {
        IPAddress += octatList[i];
      } else if (i == 4) {
        port += 256 * Integer.parseInt(octatList[i]);
      } else if (i == 5) {
        port += Integer.parseInt(octatList[i]);
      }
    }
    dataConnection(IPAddress, port);
  }


  //  Client commands
  public static void user(String username) throws IOException {
    String message = "USER " + username;
    communicate(ConnectionType.CONTROLCONNECTION, message);
  }

  public static void pw(String password) throws IOException {
    String message = "PASS " + password;
    communicate(ConnectionType.CONTROLCONNECTION, message);
  }

  public static void quit() throws IOException {
    String message = "QUIT";
    communicate(ConnectionType.CONTROLCONNECTION, message);
    closeSockets();
    System.exit(0);
  }

  public static void features() throws IOException {
    String message = "FEAT";
    // TO DO: Fix stream bug
    communicate(ConnectionType.CONTROLCONNECTION, message);
  }

  public static void dir() throws IOException {
    passiveMode();
    String message = "LIST";
    communicate(ConnectionType.DATACONNECTION, message);
  }

  public static void cd(String directory) throws IOException {
    String message = "CWD " + directory;
    communicate(ConnectionType.CONTROLCONNECTION, message);
  }

  public static void get(String remote) throws IOException {
    passiveMode();
    String message = "RETR " + remote;
    // TO DO save the file
    communicate(ConnectionType.DATACONNECTION, message);

  }


  /*
    This method reads from FTP server and exits if there is a I/O Error
  */

  public static String[] read(ConnectionType connectionType) throws IOException {
    BufferedReader reader = null;
    String errorMessage = "";

    switch(connectionType) {
      case DATACONNECTION:
        reader = dataReader;
        errorMessage = "0x3A7 Data transfer connection I/O error, closing data connection.";
        break;
      case CONTROLCONNECTION:
        reader = controlReader;
        errorMessage = "0xFFFD Control connection I/O error, closing control connection.";
        break;
      default:
        break;
    }

    try {
      // Store each line in an array list
      ArrayList<String> response = new ArrayList<String>();
      while(true) {
        String line = null;
        line = reader.readLine();
        response.add(line);
        if (!reader.ready()) {
          break;
        }
      }

      // Create a string array and transfer arraylist's content to string array
      int listLength = response.size();
      String[] result = new String[listLength];
      int index = 0;
      for (String line : response) {
        result[index] = line;
        index++;
      }
      return result;
    }catch (IOException e) {
      System.out.println(errorMessage);
      closeSockets();
      System.exit(1);
      return null;
    }
  }


  /*
    This method writes to FTP server and exits if there is a I/O Error
  */

  public static void write(String message) throws IOException {
      try {
        controlWriter.write(message + "\n");
        controlWriter.flush();
      }catch (IOException e) {
        System.out.println("0xFFFD Control connection I/O error, closing control connection");
        closeSockets();
        System.exit(1);
      }
  }


  /*
    This method creates Socket connection, controlReader, controlWriter and checks if the connection is successful or not.
  */

  public static boolean controlConnection(String server, int port) {
    try {
      controlSocket = new Socket(server, port);
      controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
      controlWriter = new BufferedWriter(new OutputStreamWriter(controlSocket.getOutputStream()));
      String response = read(ConnectionType.CONTROLCONNECTION)[0];
      System.out.println(response);
      return response.substring(0,3).equals("220");
    } catch (Exception e) {
      System.out.println("0xFFFC Control connection to " + server + " on port " + port + " failed to open.");
      System.exit(1);
      return false;
    }
  }

  /*
    This method creates Socket connection, dataReader, dataWriter and checks if the connection is successful or not.
  */

  public static void dataConnection(String server, int port) {
    try {
      dataSocket = new Socket(server, port);
      dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
    } catch (Exception e) {
      System.out.println("0x3A2 Data transfer connection " + server + " on port " + port + " failed to open.");
    }
  }


  /*
    Parse arguments, get user inputs and call corresponding method.
  */

	public static void main(String[] args) {
    // If no console arguments, exit
    if (args.length ==  0) {
      System.err.println("Insufficient command line arguments. Exiting ...");
      System.exit(1);
    } else {
      // First arg will be server address
      // Port is optional, if not specified, take it as 21
      String address = args[0];
      int port = 21;
      if (args.length > 1) {
        port = Integer.parseInt(args[1]);
      }

      System.out.println("Trying to connect to server " + address + " at port " + port);
      try {
        // If any control connection error occurs of receives message other than 220, exit
        boolean connected = controlConnection(address, port);
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
            System.exit(1);
          }

          // Parse user provided command
          String[] splittedInput = input.split("\\s{1,}");

          if (splittedInput.length == 0) {
            System.err.println("0x001 Invalid command.");
          } else {
            if (splittedInput.length > 2) {
              // We should have 1 command and may have at max 1 argument.
              System.err.println("0x002 Incorrect number of arguments.");
            } else {
              // splittedInput.length equals to 1 or 2
              String command = splittedInput[0];
              String param = null;
              boolean hasParam = (splittedInput.length == 2);
              if (hasParam) {
                param = splittedInput[1];
              }
              if (command.equals("quit") || command.equals("features") || command.equals("dir")) {
                if (hasParam) {
                  // These commands shouldn't have a parameter
                  System.err.println("0x002 Incorrect number of arguments.");
                  continue;
                }
              }
              if (command.equals("user") || command.equals("pw") ||
                      command.equals("get") || command.equals("cd")) {
                if (!hasParam) {
                  // These commands should have 1 parameter
                  System.err.println("0x002 Incorrect number of arguments.");
                  continue;
                }
              }

              // Call methods related with commands
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
