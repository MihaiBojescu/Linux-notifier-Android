package dev.mihaibojescu.linuxnotifier.DeviceHandlers;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by michael on 7/29/17.
 */

public class Request
{
    public static class reasons
    {
        public static final String REQUEST_INFO = "request information";
        public static final String AUTHENTIFICATE= "authentificate";
        public static final String NOTIFICATION = "notification";
    }

    public static JSONObject createRequest(String reason)
    {
        try
        {
            JSONObject request = new JSONObject();
            request.put("reason", reason);
            return request;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}