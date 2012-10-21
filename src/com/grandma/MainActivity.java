package com.grandma;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/*
 * The Main Activity
 */
public class MainActivity extends Activity {
    static private Context appContext;
    private final ArrayList<Friend> friends = new ArrayList<Friend>();
    private final ArrayList<GrandmaMenuItem> grandmaMenuItems = new ArrayList<GrandmaMenuItem>();
    private FriendsArrayAdapter friendsArrayAdapter;
    private GrandmaMenuItemArrayAdapter grandmaMenuItemArrayAdapter;
    private ListView listView;
    private DBHelper dbHelper;
	

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = this;
        setContentView(R.layout.main);

        // Setup the ListView Adapter that is loaded when selecting "Sync" menu
        listView = (ListView) findViewById(R.id.friendsview);
        friendsArrayAdapter = new FriendsArrayAdapter(this, R.layout.rowlayout, friends);
        listView.setAdapter(friendsArrayAdapter);

        
        // Set the ListView Adapter that is loaded when selecting "Sync" menu
        listView = (ListView)findViewById(R.id.friendsview);
        grandmaMenuItemArrayAdapter = new GrandmaMenuItemArrayAdapter(this, R.layout.rowlayout, grandmaMenuItems);
        listView.setAdapter(grandmaMenuItemArrayAdapter);        
        
        // If auto sync preference is true, dispatch a sync task
        boolean isAutoDelete = prefsGetAutoDelete();
        if (isAutoDelete) {
            // Clear all data including settings
            String fname = prefsGetFilename();
            String pname = prefsGetPictureName();
            ExternalStorage.deleteExternalStoragePublicFile(this.getPackageName(), fname);
            ExternalStorage.deleteExternalStoragePublicFile(this.getPackageName(),	fname);
            ExternalStorage.deleteInternalStoragePrivate(appContext, fname);
            ExternalStorage.deleteInternalStoragePrivate(appContext, pname);            
            dbHelper.clearAll();
        }

        dbHelper = new DBHelper(this);

    } // onCreate()

    ///////////////////////////////////////////////////////////////////

    /**
     * Cleanup
     */
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    ///////////////////////////////////////////////////////////////////

    /**
     * Invoked at the time to create the menu
     * @param the menu to create
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    /**
     * Invoked when a menu item has been selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String fname=null, pname=null;
        switch (item.getItemId()) {

            // Case: Bring up the Preferences Screen
            case R.id.menu_prefs: // Preferences
                // Launch the Preference Activity
                Intent i = new Intent(this, AppPreferenceActivity.class);
                startActivity(i);
                break;

            // Case: Load from Assets
            case R.id.menu_loadfromassets:
                // Get the Friend's list
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            // Get list from the assets directory.
                            // In real life this would be fetch from the web.
                            String fname = prefsGetFilename();
                        	byte[] buffer = getAsset(fname);
                            if (fname != null && fname.length() > 0) {
                                if (fname.equals("menu.txt")) {
        							// Parse the JSON file
                                	String menuList = new String(buffer);
                                	menuList = EncodingUtils.getString(menuList.getBytes(), "utf-8");
                                	final JSONObject json = new JSONObject(menuList);         
                                	JSONArray jsonArrayOutside = json.getJSONArray("data");                                	
                                	int l = jsonArrayOutside.length();
                                	for (int i1 = 0; i1 < l; i1++) {
        								JSONObject jsonObject = jsonArrayOutside.getJSONObject(i1);
        								Iterator keyIter = jsonObject.keys();
        								while(keyIter.hasNext()) {
        									String key = (String) keyIter.next();
        									JSONArray jsonArrayInside = jsonObject.getJSONArray(key);
        									int lengthInside = jsonArrayInside.length();
        									for(int i2 = 0; i2 < lengthInside; i2++) {
        										JSONObject o = jsonArrayInside.getJSONObject(i2);
        										String name = o.getString("name");
        										String price = o.getString("price");        										
        										GrandmaMenuItem gmi = new GrandmaMenuItem();
        										gmi.price = price;
        										gmi.name = name;
        										grandmaMenuItems.add(gmi);
        									}
        								}
        							}
        						}

                                // Only the original owner thread can touch its views
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        grandmaMenuItemArrayAdapter = new GrandmaMenuItemArrayAdapter(MainActivity.this, R.layout.rowlayout, grandmaMenuItems);
                                        listView.setAdapter(grandmaMenuItemArrayAdapter);
                                        grandmaMenuItemArrayAdapter.notifyDataSetChanged();

                                        // Get image from assets. In real life this would go to the web
                                        String pname = prefsGetPictureName();
                                        if (pname != null && pname.length() > 0) {
                                            byte[] buffer = getAsset(pname);
                                            Bitmap bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                                            ImageView imageView = (ImageView) findViewById(R.id.avatarview);
                                            imageView.setImageBitmap(bm);
                                        }
                                    }
                                });
                            } else {
                                Log.d(appContext.getString(R.string.app_name)+".get friends", " count: " + fname);
                            }

                        } catch (Exception e) {
                            Log.d(appContext.getString(R.string.app_name), "Exception: " + e);
                        }
                    }
                }.start();
                break;

            // Case: Export Internal
            case R.id.menu_exportinternal:
                /////////////////////////////////////////
                // Export to Internal and External memory
                /////////////////////////////////////////
                byte[] buffer;
                fname = prefsGetFilename();
                buffer = getAsset(fname);
                
                //////writeInternalStoragePrivate(fname, buffer);
                ExternalStorage.writeInternalStoragePrivate(appContext, fname, buffer);             
                //
                pname = prefsGetPictureName();
                buffer = getAsset(pname);
                //////writeInternalStoragePrivate(pname, buffer);
                ExternalStorage.writeInternalStoragePrivate(appContext, pname, buffer);
                break;


            // Case: Export Internal
            case R.id.menu_exportexternal:
                /////////////////////////////////////////
                // Export to Internal and External memory
                /////////////////////////////////////////
                byte[] buffer2;
                fname = prefsGetFilename();
                buffer2 = getAsset(fname);
                
                //////writeToExternalStoragePublic(fname, buffer2);
                ExternalStorage.writeToExternalStoragePublic(this.getPackageName(), fname, buffer2);

                //
                pname = prefsGetPictureName();
                buffer2 = getAsset(pname);
                //////writeToExternalStoragePublic(pname, buffer2);
                ExternalStorage.writeToExternalStoragePublic(this.getPackageName(), pname, buffer2);
                break;

            // Case: Export to DB
            case R.id.menu_exportdb:
                try {
                    fname = prefsGetFilename();                    
                    if (fname != null && fname.length() > 0) {
                        buffer = getAsset(fname);                    	
                       	// Parse the JSON file
                       	String friendslist = new String(buffer);
                       	final JSONObject json = new JSONObject(friendslist);
                       	JSONArray d = json.getJSONArray("data");
                       	int l = d.length();
                       	for (int i2=0; i2<l; i2++) {
                        		JSONObject o = d.getJSONObject(i2);
                        		String n = o.getString("name");
                        		String id = o.getString("id");
                        		dbHelper.insert(id, n);
                        }
                        // Only the original owner thread can touch its views
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                friendsArrayAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        Log.d(appContext.getString(R.string.app_name)+".get friends", " count: " + fname);
                    }
                } catch (Exception e) {
                    Log.d(appContext.getString(R.string.app_name), "Exception: " + e);
                }
                break;

            /* Case: Load from Internal Store */
            case R.id.menu_loadfrominternalstore:
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            // load picture from internal memory
                            String fname = prefsGetFilename();
                            if (fname != null && fname.length() > 0) {
                                //////byte[] buffer = readInternalStoragePrivate(fname);
                                byte[] buffer = ExternalStorage.readInternalStoragePrivate(appContext, fname);
                                // Parse the JSON file
                                String friendslist = new String(buffer);
                                final JSONObject json = new JSONObject(friendslist);
                                JSONArray d = json.getJSONArray("data");
                                int l = d.length();
                                for (int i=0; i<l; i++) {
                                    JSONObject o = d.getJSONObject(i);
                                    String n = o.getString("name");
                                    String id = o.getString("id");
                                    Friend f = new Friend();
                                    f.id = id;
                                    f.name = n;
                                    friends.add(f);
                                }
                                // Only the original owner thread can touch its views
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        friendsArrayAdapter.notifyDataSetChanged();
                                        // Get image from assets. In real life this would go to the web
                                        String pname = prefsGetPictureName();
                                        if (pname != null && pname.length() > 0) {
                                            //////byte[] buffer = readInternalStoragePrivate(pname);
                                            byte[] buffer = ExternalStorage.readInternalStoragePrivate(appContext, pname);
                                            Bitmap bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                                            ImageView imageView = (ImageView) findViewById(R.id.avatarview);
                                            imageView.setImageBitmap(bm);
                                        }
                                    }
                                });
                            } else {
                                Log.d(appContext.getString(R.string.app_name)+".get friends", " count: " + fname);
                            }

                        } catch (Exception e) {
                            Log.d(appContext.getString(R.string.app_name), "Exception: " + e);
                        }
                    }
                }.start();
                break;

            /* Case: Load From DB */
            case R.id.menu_loadfromdb:
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            // load picture from DB
                            final ArrayList<Friend> dbFriends = dbHelper.listSelectAll();
                            if (dbFriends != null) {
                                // Only the original owner thread can touch its views
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        friendsArrayAdapter = new FriendsArrayAdapter(MainActivity.this, R.layout.rowlayout, dbFriends);
                                        listView.setAdapter(friendsArrayAdapter);
                                        friendsArrayAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.d(appContext.getString(R.string.app_name), "Exception: " + e);
                        }
                    }
                }.start();
                break;

            /* Case: Clear Internal Store */
            case R.id.menu_clearinternalstore:
                fname = prefsGetFilename();
                pname = prefsGetPictureName();
                //////deleteInternalStoragePrivate(fname);
                ExternalStorage.deleteInternalStoragePrivate(appContext, fname);
                //////deleteInternalStoragePrivate(pname);
                ExternalStorage.deleteInternalStoragePrivate(appContext, pname);

                // Only the original owner thread can touch its views
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        // Notify the attached View that the underlying data has been changed
                        //  and it should refresh itself.
                        if (listView != null) {
                            friends.clear();
                        }
                        friendsArrayAdapter.notifyDataSetChanged();
                    }
                });
                break;

            /* Case: Clear External Store */
            case R.id.menu_clearexternalstore:
                fname = prefsGetFilename();
                pname = prefsGetPictureName();
                //////deleteExternalStoragePublicFile(fname);
                ExternalStorage.deleteExternalStoragePublicFile(this.getPackageName(), fname);
                //////deleteExternalStoragePublicFile(pname);
                ExternalStorage.deleteExternalStoragePublicFile(this.getPackageName(), pname);
                //

                // Only the original owner thread can touch its views
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        // Notify the attached View that the underlying data has been changed
                        //  and it should refresh itself.
                        if (listView != null) {
                            friends.clear();
                        }
                        friendsArrayAdapter.notifyDataSetChanged();
                    }
                });
                break;


            /* Case: Clear DB */
            case R.id.menu_cleardb:
                dbHelper.clearAll();
                break;

            default:
                break;

        }
        return true;
    }

   
    ///////////////////////////////////////////////////////////////////

    private void parseJSONFile() {
    	try {
    		String fname = prefsGetFilename();
    		byte[] buffer = getAsset(fname);
    		if(fname != null && fname.length() > 0) {
    			String menuList = new String(buffer);
    			final JSONObject json = new JSONObject(menuList);         
    			JSONArray jsonArrayOutside = json.getJSONArray("data");                                	
    			int l = jsonArrayOutside.length();
    			for (int i1 = 0; i1 < l; i1++) {
    				JSONObject jsonObject = jsonArrayOutside.getJSONObject(i1);
    				Iterator keyIter = jsonObject.keys();
    				while(keyIter.hasNext()) {
    					String key = (String) keyIter.next();
    					JSONArray jsonArrayInside = jsonObject.getJSONArray(key);
    					int lengthInside = jsonArrayInside.length();
						for(int i2 = 0; i2 < lengthInside; i2++) {
							JSONObject o = jsonArrayInside.getJSONObject(i2);
							String name = o.getString("name");
							String price = o.getString("price");        										
							GrandmaMenuItem gmi = new GrandmaMenuItem();
							gmi.price = price;
							gmi.name = name;
							grandmaMenuItems.add(gmi);
						}	
    				}
    			}
    		}
    	} catch (Exception e) {
    		// TODO: handle exception
    		Log.d(appContext.getString(R.string.app_name), "Exception: " + e);
    	}    	
    }
    
    /**
     * Get contents of named asset
     * @param name the name of the asset
     */
    private byte[] getAsset(String name) {
        byte[] buffer = null;
        try {
            AssetManager mngr = getAssets();
            InputStream is = mngr.open(name);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            buffer = new byte[1024*3];
            is.read(buffer);
            bo.write(buffer);
            bo.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    ///////////////////////////////////////////////////////////////////

    /**
     * ListView Friends ArrayAdapter
     */
    public class FriendsArrayAdapter extends ArrayAdapter<Friend> {
        private final Activity context;
        private final ArrayList<Friend> friends;
        private int resourceId;

        /**
         * Constructor
         * @param context the application content
         * @param resourceId the ID of the resource/view
         * @param friends the bound ArrayList
         */
        public FriendsArrayAdapter(
                Activity context,
                int resourceId,
                ArrayList<Friend> friends) {
            super(context, resourceId, friends);
            this.context = context;
            this.friends = friends;
            this.resourceId = resourceId;
        }

        /**
         * Updates the view
         * @param position the ArrayList position to update
         * @param convertView the view to update/inflate if needed
         * @param parent the groups parent view
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = vi.inflate(resourceId, null);
            }
            Friend f = friends.get(position);
            TextView rowTxt = (TextView) rowView.findViewById(R.id.rowtext_top);
            rowTxt.setText(f.name);
            rowTxt = (TextView) rowView.findViewById(R.id.rowtext_bottom);
            rowTxt.setText(f.id);
            return rowView;
        }

    }

    /////////////////////////////////////////////////////////////
    // The following methods show how to use the SharedPrefences
    /////////////////////////////////////////////////////////////

    /**
     * Retrieves the Auto delete preference
     * @return the value of auto delete
     */
    public boolean prefsGetAutoDelete() {
        boolean v = false;
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = appContext.getString(R.string.prefs_autodelete_key);
        try {
            v = sprefs.getBoolean(key, false);
        } catch (ClassCastException e) {
            // if exception, do nothing; that is return default value of false.
        }
        return v;
    }

    /**
     * Sets the auto delete preference
     * @param v the value to set
     */
    public void  prefsSetAutoDelete(boolean v) {
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        Editor e = sprefs.edit();
        String key = appContext.getString(R.string.prefs_autodelete_key);
        e.putBoolean(key, v);
        e.commit();
    }

    /**
     * Retrieves the the name of the friend's list asset
     * @return the value of the asset
     */
    public String prefsGetFilename() {
        String v = null;
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = appContext.getString(R.string.prefs_assetname_friendslist_key);
        try {
            v = sprefs.getString(key, null);
        } catch (ClassCastException e) {
            // if exception, do nothing; that is return default value of false.
        }
        return v;
    }

    /**
     * Sets the friends list asset name preference
     * @param v the value to set
     */
    public void  prefsSetFilename(String v) {
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        Editor e = sprefs.edit();
        String key = appContext.getString(R.string.prefs_assetname_friendslist_key);
        e.putString(key, v);
        e.commit();
    }

    /**
     * Retrieves the name of the picture asset/file
     * @return the value of the asset
     */
    public String prefsGetPictureName() {
        String v = null;
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = appContext.getString(R.string.prefs_assetname_picture_key);
        try {
            v = sprefs.getString(key, "");
        } catch (ClassCastException e) {
            // if exception, do nothing; that is return default value of false.
        }
        return v;
    }

    /**
     * Sets the picture asset name preference
     * @param v the value to set
     */
    public void  prefsSetPictureName(String v) {
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        Editor e = sprefs.edit();
        String key = appContext.getString(R.string.prefs_assetname_picture_key);
        e.putString(key, v);
        e.commit();
    }

}
