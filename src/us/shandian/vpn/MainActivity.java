package us.shandian.vpn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;

import java.util.ArrayList;

import us.shandian.vpn.manager.VpnManager;
import us.shandian.vpn.manager.VpnProfile;
import us.shandian.vpn.util.ProfileLoader;

public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener
{
	private DrawerLayout mDrawer;
	private ActionBarDrawerToggle mToggle;
	private ImageView mFooter;
	private ListView mList;
	private ProfileLoader mLoader;
	private ConfigFragment mFragment;
	private Switch mSwitch;
	
	private ArrayList<String> mArray;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Loader
		mLoader = ProfileLoader.getInstance(this);
		
		// Action bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayUseLogoEnabled(false);
		
		// Drawer
		mDrawer = (DrawerLayout) findViewById(R.id.drawer);
		mToggle = new ActionBarDrawerToggle(this, mDrawer, R.drawable.ic_drawer, 0, 0);
		mDrawer.setDrawerListener(mToggle);
		mDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		
		// Fragment
		mFragment = new ConfigFragment();
		getFragmentManager().beginTransaction().replace(R.id.frame, mFragment).commit();
		
		// List
		mList = (ListView) findViewById(R.id.profiles);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mFooter = (ImageView) inflater.inflate(R.layout.footer, null);
		mList.addFooterView(mFooter);
		mList.setOnItemClickListener(this);
		mFooter.setOnClickListener(this);
		
	}
	
	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mToggle.syncState();
		
		// List
		mArray = reloadList();
		
		if (mArray.size() > 0) {
			mFragment.setProfile(mLoader.getProfile(mArray.get(0)));
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration conf) {
		super.onConfigurationChanged(conf);
		mToggle.onConfigurationChanged(conf);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		mSwitch = (Switch) menu.findItem(R.id.toggle).getActionView();
		mSwitch.setChecked(VpnManager.isVpnRunning());
		mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton v, boolean checked) {
				if (!checked) {
					VpnManager.stopVpn();
				} else {
					VpnProfile p = mFragment.getProfile();
					
					if (p != null && VpnManager.startVpn(p)) {
						mLoader.updateProfile(p);
					}
				}
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			return mToggle.onOptionsItemSelected(item);
		} else if (item.getItemId() == R.id.save) {
			VpnProfile p = mFragment.getProfile();
			
			if (p != null) {
				mLoader.updateProfile(p);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		if (id < mArray.size()) {
			mFragment.setProfile(mLoader.getProfile(mArray.get(position)));
			mDrawer.closeDrawer(Gravity.START);
		}
	}

	@Override
	public void onClick(View v) {
		final EditText text = new EditText(this);
		new AlertDialog.Builder(this)
							.setTitle(R.string.input)
							.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									String name = text.getText().toString();
									mFragment.setProfile(mLoader.createProfile(name));
									mArray = reloadList();
								}
							})
							.setPositiveButton(android.R.string.cancel, null)
							.setView(text)
							.create()
							.show();
	}
	
	private ArrayList<String> reloadList() {
		ArrayList<String> array = mLoader.getProfileList();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list, R.id.list_text, array);
		mList.setAdapter(adapter);
		return array;
	}
}
