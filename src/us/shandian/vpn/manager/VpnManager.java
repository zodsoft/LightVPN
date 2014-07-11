package us.shandian.vpn.manager;

import android.text.TextUtils;

import java.lang.StringBuilder;

import us.shandian.vpn.util.RunCommand;

public class VpnManager
{
	private static final String PPP_UNIT = "100";
	private static final String PPP_INTERFACE = "ppp" + PPP_UNIT;
	private static final int MAX_WAIT_TIME = 15; // seconds
	
	// Start connection to a PPTP server
	public static boolean startVpn(VpnProfile p) {
		// Check
		if (TextUtils.isEmpty(p.server) || TextUtils.isEmpty(p.username) ||
			TextUtils.isEmpty(p.password)) {
			
			return false;
		}
		
		// Iface
		String iface = getDefaultIface();
		
		// Arguments to mtpd
		String[] args = new String[]{iface, "pptp", p.server, "1723", "name", p.username,
					"password", p.password, "linkname", "vpn", "refuse-eap", "nodefaultroute",
					"idle", "1800", "mtu", "1400", "mru", "1400", (p.mppe ? "+mppe" : "nomppe"),
					"unit", PPP_UNIT};
		
		// Start
		startMtpd(args);
		
		// Wait for mtpd
		if (!blockUntilStarted()) {
			return false;
		}
		
		// Set up ip route
		setupRoute();
		
		// Set up dns
		setupDns();
		
		return true;
	}
	
	public static void stopVpn() {
		// Kill all vpn stuff
		StringBuilder s = new StringBuilder();
		s.append("pkill mtpd\n")
		 .append("pkill pppd\n")
		 .append("ip ro flush dev ").append(PPP_INTERFACE).append("\n")
		 .append("iptables -t nat -F\n")
		 .append("iptables -t nat -X\n")
		 .append("iptables -t nat -Z");
		
		try {
			RunCommand.run(s.toString()).waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isVpnRunning() {
		try {
			Process p = RunCommand.run("pgrep mtpd");
			p.waitFor();
			if (!TextUtils.isEmpty(RunCommand.readInput(p).replace("\n", "").trim())) {
				return true;
			}
		} catch (Exception e) {
			
		}
		
		return false;
	}
	
	private static String getDefaultIface() {
		String routes;
		
		try {
			Process p = RunCommand.run("ip ro");
			p.waitFor();
			routes = RunCommand.readInput(p);
		} catch (Exception e) {
			routes = null;
		}
		
		if (routes != null) {
			for (String route : routes.split("\n")) {
				if (route.startsWith("default")) {
					String iface = null;
					boolean last = false;
					for (String ele : route.split(" ")) {
						if (last) {
							iface = ele;
							break;
						} else if (ele.equals("dev")) {
							last = true;
						}
					}
					
					if (iface != null) {
						return iface;
					} else {
						break;
					}
				}
			}
		}
		
		// Can't load default interface? That's not possible.
		return "eth0";
	}
	
	private static void startMtpd(String[] args) {
		StringBuilder s = new StringBuilder();
		s.append("mtpd");
		
		// Add args
		for (String arg : args) {
			s.append(" ").append(arg);
		}
		
		// Run
		try {
			RunCommand.run(s.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean blockUntilStarted() {
		int n = MAX_WAIT_TIME * 2;
		
		for (int i = 0; i < n; i++) {
			try {
				Process p = RunCommand.run("ip ro");
				p.waitFor();
				String out = RunCommand.readInput(p);
				
				if (out.contains(PPP_INTERFACE)) {
					return true;
				} else {
					Thread.sleep(500);
				}
			} catch (Exception e) {
				break;
			}
		}
		
		return false;
	}
	
	private static void setupRoute() {
		StringBuilder s = new StringBuilder();
		s.append("ip ro add 0.0.0.0/1 dev ").append(PPP_INTERFACE).append("\n")
		 .append("ip ro add 128.0.0.0/1 dev ").append(PPP_INTERFACE).append("\n")
		 .append("ip ru add from all table 200 \n")
		 .append("ip ro add default dev ").append(PPP_INTERFACE).append(" table 200");
		
		// Run
		try {
			Process p = RunCommand.run(s.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void setupDns() {
		// For now, I haven't got any idea of how to get the DNS returned by pppd
		// So we just use 8.8.8.8 and 8.8.4.4
		
		String dns1 = null, dns2 = null;
		
		try {
			Process p = RunCommand.run("getprop net.dns1");
			p.waitFor();
			dns1 = RunCommand.readInput(p).replace("\n", "").trim();
			p = RunCommand.run("getprop net.dns2");
			p.waitFor();
			dns2 = RunCommand.readInput(p).replace("\n", "").trim();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		if (TextUtils.isEmpty(dns1) || TextUtils.isEmpty(dns2)) {
			return;
		}
		
		StringBuilder s = new StringBuilder();
		s.append("iptables -t nat -A OUTPUT -d ").append(dns1).append("/32 -o ")
			.append(PPP_INTERFACE).append(" -p udp -m udp --dport 53 -j DNAT --to-destination 8.8.8.8:53\n")
		 .append("iptables -t nat -A OUTPUT -d ").append(dns2).append("/32 -o ")
			.append(PPP_INTERFACE).append(" -p udp -m udp --dport 53 -j DNAT --to-destination 8.8.4.4:53");
		
		try {
			RunCommand.run(s.toString()).waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
}
