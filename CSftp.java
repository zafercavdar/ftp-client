package a1_g0o1b_y2m1b;

public class Main {

	public static void main(String[] args) {
		String host = args[0];
		int port = 21;
		if (args.length > 1) {
			port = Integer.parseInt(args[1]);
		}
		System.out.println("Trying connect to host " + host + " at port: " + port);
	}
}
