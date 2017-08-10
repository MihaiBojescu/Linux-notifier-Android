package dev.mihaibojescu.linuxnotifier.IO;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import dev.mihaibojescu.linuxnotifier.R;

/**
 * Created by michael on 11.07.2017.
 */

public class IOClass
{

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
        if (instance == null)
            instance = new IOClass();

        return instance;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public void writeToFile(String fileName, JSONObject input)
    {
        FileOutputStream fileOutputStream;

        try
        {
            fileOutputStream = context.openFileOutput("devices.json", Context.MODE_PRIVATE);
            fileOutputStream.write(input.toString().getBytes());
            fileOutputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public JSONObject readFile(String fileName)
    {
        JSONObject result;
        FileInputStream fileInputStream;
        StringBuilder stringBuilder;
        byte[] buffer;

        try
        {
            fileInputStream = context.openFileInput("devices.json");
            stringBuilder = new StringBuilder();
            buffer = new byte[1024];

            while (fileInputStream.read(buffer) != -1)
                stringBuilder.append(new String(buffer));

            result = new JSONObject(stringBuilder.toString());
            return result;
        }
        catch (JSONException | IOException e)
        {
            e.printStackTrace();
        }
        return new JSONObject();
    }
}
