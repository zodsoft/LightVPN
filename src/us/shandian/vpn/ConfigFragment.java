package us.shandian.vpn;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import us.shandian.vpn.manager.VpnProfile;

public class ConfigFragment extends Fragment {
	private EditText mServer;
	private EditText mUserName;
	private EditText mPassword;
	private EditText mDns1;
	private EditText mDns2;
	private CheckBox mMppe;
	
	// Current
	private VpnProfile mProfile;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.config, null);
		
		// Get views
		mServer = (EditText) v.findViewById(R.id.server);
		mUserName = (EditText) v.findViewById(R.id.username);
		mPassword = (EditText) v.findViewById(R.id.password);
		mDns1 = (EditText) v.findViewById(R.id.dns1);
		mDns2 = (EditText) v.findViewById(R.id.dns2);
		mMppe = (CheckBox) v.findViewById(R.id.mppe);
		
		// Default set to disabled
		mServer.setEnabled(false);
		mUserName.setEnabled(false);
		mPassword.setEnabled(false);
		mDns1.setEnabled(false);
		mDns2.setEnabled(false);
		mMppe.setEnabled(false);
		
		return v;
	}
	
	public void removeProfile() {
		mProfile = null;
		
		// Default set to disabled
		mServer.setEnabled(false);
		mUserName.setEnabled(false);
		mPassword.setEnabled(false);
		mDns1.setEnabled(false);
		mDns2.setEnabled(false);
		mMppe.setEnabled(false);
		
		// Clean up
		mServer.setText("");
		mUserName.setText("");
		mPassword.setText("");
		mDns1.setText("");
		mDns2.setText("");
		mMppe.setChecked(false);
		
		getActivity().getActionBar().setSubtitle(null);
	}
	
	public void setProfile(VpnProfile p) {
		mProfile = p;
		
		// All enabled
		mServer.setEnabled(true);
		mUserName.setEnabled(true);
		mPassword.setEnabled(true);
		mDns1.setEnabled(true);
		mDns2.setEnabled(true);
		mMppe.setEnabled(true);
		
		// Set value
		mServer.setText(p.server);
		mUserName.setText(p.username);
		mPassword.setText(p.password);
		mDns1.setText(p.dns1);
		mDns2.setText(p.dns2);
		mMppe.setChecked(p.mppe);
		
		// ActionBar
		getActivity().getActionBar().setSubtitle(p.name);
	}
	
	public VpnProfile getProfile() {
		if (mProfile == null) return null;
		
		mProfile.server = mServer.getText().toString();
		mProfile.username = mUserName.getText().toString();
		mProfile.password = mPassword.getText().toString();
		mProfile.dns1 = mDns1.getText().toString();
		mProfile.dns2 = mDns2.getText().toString();
		mProfile.mppe = mMppe.isChecked();
		
		return mProfile;
	}

}
