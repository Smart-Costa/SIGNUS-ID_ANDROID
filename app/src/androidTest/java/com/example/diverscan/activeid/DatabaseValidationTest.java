package com.example.diverscan.activeid;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.diverscan.activeid.sqlite.LoginDBHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Validates critical database tables exist to prevent runtime crashes.
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseValidationTest {

    @Test
    public void validateRolHHTableExists() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        // Initialize the helper
        LoginDBHelper dbHelper = new LoginDBHelper(appContext);
        
        // Trigger database opening (this will run onUpgrade/onCreate logic)
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        assertTrue("Database should be open", db.isOpen());

        // Check if table RolHH exists
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='RolHH'", null);
        boolean tableExists = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                tableExists = true;
            }
            cursor.close();
        }

        assertTrue("Table 'RolHH' must exist in the database to prevent crashes", tableExists);
        
        // Optional: Validate columns exist by attempting a dummy query
        try {
            Cursor c = db.rawQuery("SELECT Description FROM RolHH WHERE 1=0", null);
            if (c != null) c.close();
        } catch (Exception e) {
            fail("Querying RolHH failed, likely missing columns: " + e.getMessage());
        }

        db.close();
    }
}
