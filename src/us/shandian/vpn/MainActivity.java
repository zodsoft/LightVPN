package us.shandian.vpn;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import us.shandian.vpn.R;
import us.shandian.vpn.manager.VpnManager;
import us.shandian.vpn.manager.VpnProfile;

public class MainActivity extends Activity implements View.OnClickListener
{
	private Button mButton;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mButton = (Button) findViewById(R.id.connect);
		mButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

	}
}
