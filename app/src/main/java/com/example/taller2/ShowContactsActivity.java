package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ListView;

public class ShowContactsActivity extends AppCompatActivity {

	ListView listContacts;
	private static final int CONTACT_PERMISSION = 22;
	String[] projection;
	Cursor cursor;
	ContactsAdapter contactsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_contacts);

		//inflate
		listContacts = findViewById(R.id.listContacts);

		projection = new String[]{ContactsContract.Profile._ID, ContactsContract.Profile.DISPLAY_NAME_PRIMARY};
		contactsAdapter = new ContactsAdapter(this,null,0);
		listContacts.setAdapter(contactsAdapter);

		PermissionUtil.requestPermission(this, Manifest.permission.READ_CONTACTS,"Para leer contactos",CONTACT_PERMISSION);
		pintarContactos();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode){
			case CONTACT_PERMISSION:
				pintarContactos();
				return;

		}
	}

	private void pintarContactos(){
		if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

			cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);

			contactsAdapter.changeCursor(cursor);
		}
	}
}