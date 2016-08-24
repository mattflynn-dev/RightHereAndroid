package org.mattflynn.righthere;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ConfirmPage extends Activity implements OnClickListener{
	
	private String sameURL;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_page);
        Intent intent = getIntent();
        String sameURL = intent.getExtras().getString("returl");
        Button btsame = (Button) findViewById(R.id.sendsame);
        btsame.setOnClickListener(this);
        Button btnnew = (Button) findViewById(R.id.backbegin);
		btnnew.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.backbegin:
			Intent backhome = new Intent(this, MainActivity.class);
			startActivity(backhome);
			break;
			
		case R.id.sendsame:
			Intent resend = new Intent(this, ContactMessage.class);
			resend.putExtra("SEND_URL", sameURL);
			startActivity(resend);
			break;
		}

	}

}
