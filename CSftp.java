import org.apache.commons.net.ftp.FTPClient;

public class CSftp {

	public static void main(String[] args) {
    if (args.length ==  0) {
      System.out.println("Insufficient command line arguments. Exiting ...");
      System.exit(0);
    } else {
      String host = args[0];
      int port = 21;
      if (args.length > 1) {
        port = Integer.parseInt(args[1]);
      }
      System.out.println("Trying connect to host " + host + " at port: " + port);
      FTPClient ftpClient = new FTPClient();
      try {
        ftpClient.connect(host, port);
      } catch (Exception e) {
        e.printStackTrace(System.out);
      }
      //System.out.println(connectionResponse);
    }
	}
}
