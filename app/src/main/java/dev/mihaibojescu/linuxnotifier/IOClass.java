package dev.mihaibojescu.linuxnotifier;

import android.content.Context;
import android.os.Environment;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by michael on 11.07.2017.
 */

public class IOClass {
    private static IOClass instance = null;
    private File currentFile;
    private File folder;
    private Context context;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;

    private IOClass()
    {

    }

    public static IOClass getInstance()
    {
        if(instance == null)
            instance = new IOClass();

        return instance;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public void openFile(String filename)
    {
        folder = new File(Environment.getExternalStorageDirectory() + "/" + context.getPackageName());
        if(!folder.exists())
            folder.mkdirs();

        currentFile = new File(folder, "/" + filename);
        if(!currentFile.exists())
            try
            {
                currentFile.createNewFile();
            }
            catch(IOException e)
            {
                Log.e("error!", e.toString());
            }
    }

    public void writeToFile(JSONObject input)
    {
        BufferedWriter writer;
        try
        {
            writer = new BufferedWriter(new FileWriter(currentFile, false));
            writer.write(input.toString());
            writer.close();
        }
        catch(IOException io)
        {
            Log.e("Input/Output error:", io.toString());
        }
    }

    public JSONObject readFile()
    {
        JSONObject result;
        BufferedReader reader;
        StringBuilder builder;
        String line;

        try
        {
            reader = new BufferedReader(new FileReader(currentFile));
            builder = new StringBuilder();

            while((line = reader.readLine()) != null)
                builder.append(line);

            reader.close();
            result = new JSONObject(builder.toString());
        }
        catch(IOException io)
        {
            io.printStackTrace();
            return null;
        }
        catch(JSONException json)
        {
            json.printStackTrace();
            return null;
        }

        Log.d("json obj", reader.toString());
        return result;
    }
}
