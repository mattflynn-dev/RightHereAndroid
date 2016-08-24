package org.mattflynn.righthere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;

public class ContactMessage extends Activity implements OnItemClickListener{

	private ArrayList<Map<String, String>> mPeopleList;

    private SimpleAdapter mAdapter;
    private AutoCompleteTextView mTxtPhoneNo;
    
    private String contentMsg;
    private String phoneNumber;
    private String smsURL;

/** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_message);
        
        Intent intent = getIntent(); 
        smsURL = intent.getExtras().getString("SEND_URL");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        mPeopleList = new ArrayList<Map<String, String>>();
        PopulatePeopleList();
        mTxtPhoneNo = (AutoCompleteTextView) findViewById(R.id.mmWhoNo);

        mAdapter = new SimpleAdapter(this, mPeopleList, R.layout.custcontview ,new String[] { "Name", "Phone" , "Type" }, new int[] { R.id.ccontName, R.id.ccontNo, R.id.ccontType });      

        mTxtPhoneNo.setAdapter(mAdapter);
        mTxtPhoneNo.setOnItemClickListener(new OnItemClickListener(){
        	@Override
        	public void onItemClick(AdapterView<?> av, View v, int index, long arg) {
        	    Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);
        	    Iterator<String> myVeryOwnIterator = map.keySet().iterator();
        	          while(myVeryOwnIterator.hasNext()) {
        	            String key=(String)myVeryOwnIterator.next();
        	            String value=(String)map.get(key);
        	            mTxtPhoneNo.setText(value);
        	            phoneNumber = value.toString();
        	        }               
        	    }
        	});
        
        Button sendSMS = (Button) findViewById(R.id.send);

        sendSMS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        EditText message = (EditText)findViewById(R.id.message);
		        String messageString = message.getText().toString();
		        contentMsg = messageString + " " + smsURL; 
				smsSender(phoneNumber, contentMsg);		
					
			}
		});
        }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == android.R.id.home) {
    		finish();
    	}
    	return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
	public void PopulatePeopleList()
    {

    mPeopleList.clear();

    Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

    while (people.moveToNext())
    {
    String contactName = people.getString(people.getColumnIndex(
    ContactsContract.Contacts.DISPLAY_NAME));

    String contactId = people.getString(people.getColumnIndex(
    ContactsContract.Contacts._ID));
    String hasPhone = people.getString(people.getColumnIndex(
    ContactsContract.Contacts.HAS_PHONE_NUMBER));

    if ((Integer.parseInt(hasPhone) > 0))
    {

    // You know have the number so now query it like this
    Cursor phones = getContentResolver().query(
    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
    null,
    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,
    null, null);
    while (phones.moveToNext()) {

    //store numbers and display a dialog letting the user select which.
    String phoneNumber = phones.getString(
    phones.getColumnIndex(
    ContactsContract.CommonDataKinds.Phone.NUMBER));
    
    
    String numberType = phones.getString(phones.getColumnIndex(
    ContactsContract.CommonDataKinds.Phone.TYPE));
    

    Map<String, String> NamePhoneType = new HashMap<String, String>();

    NamePhoneType.put("Name", contactName);
    NamePhoneType.put("Phone", phoneNumber);

    
    if(numberType.equals("0"))
    NamePhoneType.put("Type", "Work");
    else
    if(numberType.equals("1"))
    NamePhoneType.put("Type", "Home");
    else if(numberType.equals("2"))
    NamePhoneType.put("Type",  "Mobile");
    else
    NamePhoneType.put("Type", "Other");
    

    //Then add this map to the list.
    mPeopleList.add(NamePhoneType);
    }
    phones.close();
    }
    }
    people.close();

    startManagingCursor(people);
    }
    
    private void smsSender(String number, String message) {
     /* FOR TESTING  Toast.makeText(this, phoneNumber, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, contentMsg, Toast.LENGTH_LONG).show(); */
    	SmsManager sms = SmsManager.getDefault();
    	sms.sendTextMessage(number, null, message, null, null);
    	
        Intent intentNext = new Intent(this, ConfirmPage.class);
        intentNext.putExtra("returl", smsURL);
        startActivity(intentNext);
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}
    

}
