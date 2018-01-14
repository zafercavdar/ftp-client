import org.apache.commons.net.ftp.FTPClient;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CSftp {

  public static FTPClient ftpClient = null;

  public static void user(String username) {

  }

  public static void pw(String password) {

  }

  public static void quit() {

  }

  public static void get(String remote) {

  }

  public static void features() {

  }

  public static void cd(String directory) {

  }

  public static void dir() {

  }

	public static void main(String[] args) {
    if (args.length ==  0) {
      System.err.println("Insufficient command line arguments. Exiting ...");
      System.exit(0);
    } else {
      String address = args[0];
      int port = 21;
      if (args.length > 1) {
        port = Integer.parseInt(args[1]);
      }
      System.out.println("Trying connect to address " + address + " at port: " + port);
      try {
        //ftpClient = new FTPClient();
        //ftpClient.connect(address, port);
        System.out.println("Successfully connected.");

        BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
          System.out.print("csftp> ");
          String input = rd.readLine();
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
        rd.close();

      } catch (Exception e) {
        e.printStackTrace(System.out);
      }
    }
	}
}
