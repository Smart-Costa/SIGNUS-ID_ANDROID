package com.uk.tsl.rfid.samples.bulkencoding;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import com.uk.tsl.rfid.asciiprotocol.device.ReaderManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class BulkEncodingDataLoader
{
    public static ArrayList<BulkEncodingModel.Data> loadFromUri(Context context, Uri uri) throws IOException
    {
        ArrayList<BulkEncodingModel.Data> dataset = new ArrayList<>();

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream))))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                BulkEncodingModel.Data data = parseDataFrom(line);
                if( data != null )
                {
                    dataset.add(data);
                }
            }
        }

        return dataset;
    }

    private static BulkEncodingModel.Data parseDataFrom(String line)
    {
        BulkEncodingModel.Data data = null;

        String[] parts = line.trim().toUpperCase(Locale.US).split(",", -1);
        // Require Id and EPC but password may be absent
        if( parts.length >= 2 )
        {
            String password = (parts.length >= 3) ? parts[2].trim() : "00000000";
            String userData = (parts.length >= 4) ? parts[3].trim() : "00000000";

            data = new BulkEncodingModel.Data(line, parts[0].trim(), parts[1].trim(), password, userData );
        }

        return data;
    }

}
