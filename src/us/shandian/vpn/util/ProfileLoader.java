package us.shandian.vpn.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashMap;

import us.shandian.vpn.manager.VpnProfile;

public class ProfileLoader {
	
	private static ProfileLoader sInstance;
	
	private Context mContext;
	private SharedPreferences mEntryPref;
	private String mDefault;
	private HashMap<String, VpnProfile> mEntries = new HashMap<String, VpnProfile>();
	
	public static ProfileLoader getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ProfileLoader(context);
		}
		
		return sInstance;
	}
	
	private ProfileLoader(Context context) {
		mContext = context;
		mEntryPref = context.getSharedPreferences("vpns", Context.MODE_WORLD_READABLE);
		mDefault = mEntryPref.getString("def", "");
		load();
	}
	
	private void load() {
		String[] names = mEntryPref.getString("vpns", "").split("\\|");
		
		for (String name : names) {
			if (TextUtils.isEmpty(name)) continue;
			
			// Load all profiles
			SharedPreferences pref = mContext.getSharedPreferences(name, Context.MODE_WORLD_READABLE);
			VpnProfile p = new VpnProfile();
			
			// TODO: Add more avaliable configs
			p.name = name;
			p.server = pref.getString("server", "");
			p.username = pref.getString("username", "");
			p.password = pref.getString("password", "");
			p.mppe = pref.getBoolean("mppe", true);
			
			// Put to map
			mEntries.put(name, p);
		}
	}
	
	private void save() {
		
		StringBuilder s = new StringBuilder();
		
		for (HashMap.Entry<String, VpnProfile> e : mEntries.entrySet()) {
			String name = e.getKey();
			VpnProfile p = e.getValue();
			
			// Add to vpns
			s.append(name).append("|");
			
			// Save to profiles
			// TODO: Add more avaliable configs
			SharedPreferences pref = mContext.getSharedPreferences(name, Context.MODE_WORLD_READABLE);
			SharedPreferences.Editor edit = pref.edit();
			edit.putString("server", p.server);
			edit.putString("username", p.username);
			edit.putString("password", p.password);
			edit.putBoolean("mppe", p.mppe);
			edit.commit();
		}
		
		mEntryPref.edit().putString("vpns", s.toString()).putString("def", mDefault).commit();
	}
	
	public VpnProfile createProfile(String name) {
		VpnProfile p = new VpnProfile();
		p.name = name;
		mEntries.put(name, p);
		save();
		return p;
	}
	
	public VpnProfile updateProfile(VpnProfile p) {
		mEntries.put(p.name, p);
		save();
		return p;
	}
	
	public VpnProfile getProfile(String name) {
		return mEntries.get(name);
	}
	
	public ArrayList<String> getProfileList() {
		ArrayList<String> list = new ArrayList<String>();
		
		for (HashMap.Entry<String, VpnProfile> e : mEntries.entrySet()) {
			list.add(e.getKey());
		}
		
		return list;
	}
	
	public VpnProfile getDefault() {
		return getProfile(mDefault);
	}
	
	public void setDefault(VpnProfile p) {
		mDefault = p.name;
		save();
	}
	
}
