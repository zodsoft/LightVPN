package us.shandian.vpn.util;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.StringBuilder;

public class RunCommand {
	public static Process run(String command) throws IOException {
		ProcessBuilder builder = new ProcessBuilder("su");
		Process p = builder.start();
		DataOutputStream dos = new DataOutputStream(p.getOutputStream());

		dos.writeBytes(command + "\n");
		dos.flush();
		dos.writeBytes("exit\n");
		dos.flush();
		return p;
	}
	
	public static String readInput(Process proc) throws IOException {
		DataInputStream dis = new DataInputStream(proc.getInputStream());
		
		StringBuilder s = new StringBuilder();
		String str;
		while ((str = dis.readLine()) != null) {
			s.append(str).append("\n");
		}
		
		return s.toString();
	}
	
	public static String readError(Process proc) throws IOException {
		DataInputStream dis = new DataInputStream(proc.getErrorStream());
		
		StringBuilder s = new StringBuilder();
		String str;
		while ((str = dis.readLine()) != null) {
			s.append(str).append("\n");
		}
		
		return s.toString();
	}
}
