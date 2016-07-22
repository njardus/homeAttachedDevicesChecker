/**
 * 
 * homeAttachedDevicesChecker is a small app that will poll the appropriate 
 * page on the DGN2200 router and return a list of the attached device names.
 * 
 */
 package homeattacheddeviceschecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Base64;

/**
 *
 * @author NardusG
 */
public class HomeAttachedDevicesChecker {
    
    /* MAGIC NUMBERS */
    private static final long SLEEPTIME = 0;
    
    /* VARIABLES */
    private static String username;
    private static String password;
    private static String ipAddress;
    private static deviceTableObj deviceTable;

    /**
     * @param args -u username for the router
     *  -p password for the router
     * [-ip optional IP address of the router, with port]
     */
    public static void main(String[] args) {
        parseArgs(args);
        
        String url = "http://"
                + ipAddress
                + "/DEV_device.htm"
               ;
        
        String URLSource = getURL(url);
        
        if (!"".equals(URLSource))
        {
            parseURL(URLSource);
        }
        
        for (int i = 0; i < deviceTable.len; i++)
        {
            System.out.println("Device number:     " + deviceTable.number[i]);
            System.out.println("Device name:       " + deviceTable.name[i]);
            System.out.println("Device IP address: " + deviceTable.ip[i]);
        }
    }
    
    private static deviceTableObj parseURL(String URLSource)
    {
        int tableIndex = URLSource.indexOf("<span class=\"thead\">1</span></td>") + 20;
        int tableIndexEnd = URLSource.indexOf("</table>");

        String table = URLSource.substring(tableIndex, tableIndexEnd);
        String[] tableLines = table.split("<span class=\"thead\">");
        String[] deviceNumber = new String[tableLines.length];
        int[] indexNum1 = new int[tableLines.length];
        int[] indexNum2 = new int[tableLines.length];
        String[] deviceIP = new String[tableLines.length];
        int[] indexIP1 = new int[tableLines.length];
        int[] indexIP2 = new int[tableLines.length];
        String[] deviceName = new String[tableLines.length];
        int[] indexName1 = new int[tableLines.length];
        int[] indexName2 = new int[tableLines.length];
        
        deviceTable = new deviceTableObj(tableLines.length);
        
        for (int i = 0; i < tableLines.length; i++)
        {
            indexNum1[i] = 0;
            indexNum2[i] = tableLines[i].indexOf("</span>");
            
            indexIP1[i] = tableLines[i].indexOf("<span class=\"ttext\">") + 20;
            indexIP2[i] = tableLines[i].indexOf("</span>", indexIP1[i]);
            
            indexName1[i] = tableLines[i].indexOf("<span class=\"ttext\">", indexIP2[i]) + 20;
            indexName2[i] = tableLines[i].indexOf("</span>", indexName1[i]);
            
            deviceTable.number[i] = tableLines[i].substring(indexNum1[i] , indexNum2[i]) + "\n";
            deviceTable.ip[i] = tableLines[i].substring(indexIP1[i] , indexIP2[i]) + "\n";
            deviceTable.name[i] = tableLines[i].substring(indexName1[i] , indexName2[i]) + "\n";
        }
        return deviceTable;
    }

    private static String getURL(String searchString) 
    {
//        System.out.println(searchString);
        
        String authorization = "";
        String encoded = "";
        
        String returnString = null;
        try {
            URL url;
//            System.out.println("Create URL:" + searchString);
            url = new URL( searchString );
//            System.out.println("URL defined");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (username != null && password != null) {
                authorization = username + ":" + password;
//                System.out.println("Auth: " +  authorization);
            }

            if (authorization != null) {
                byte[] authBytes = authorization.getBytes(StandardCharsets.UTF_8);
                encoded = Base64.getEncoder().encodeToString(authBytes);
            }

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + encoded);
            connection.setRequestProperty("User-Agent", "HomeAttachedDevicesChecker-java");
//            System.out.println("Connection declared and properties set");

            int responseCode;
            responseCode = connection.getResponseCode();
            
            try {
                Thread.sleep(SLEEPTIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(HomeAttachedDevicesChecker.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            responseCode = connection.getResponseCode();
//            System.out.println("Response code returned: " + responseCode);
            if (responseCode == 200)
            {
                // GET request successful
//                System.out.println("Response code was 200");
                StringBuffer response;
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()))) {
                    String inputLine;
                    response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                //System.out.println("getURL returns: " + response.toString());
                returnString = response.toString();
            }
            else
            {
                System.out.println("ERROR: Response code was not 200");
                
                returnString = "";
                // GET request failed
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(HomeAttachedDevicesChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HomeAttachedDevicesChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnString;
    }
    
    private static void parseArgs(String[] args) {
        if (args.length == 0)
        {
            username = "";
            password = "";
            ipAddress = "192.168.0.1";
        }
        else if (args.length == 2)
        {
            switch (args[0]) 
            {
                case "-u":
                    username = args[1];
                    password = "";
                    ipAddress = "192.168.0.1";
                    break;
                case "-p":
                    username = "";
                    password = args[1];
                    ipAddress = "192.168.0.1";
                    break;
                case "-ip":
                    username = "";
                    password = "";
                    ipAddress = args[1];
                    break;
            }     
        }
        else if (args.length == 4)
        {
            if ("-u".equals(args[0]) && "-p".equals(args[2]))
            {
                username = args[1];
                password = args[3];
                ipAddress = "192.168.0.1";
            }
            else if ("-u".equals(args[0]) && "-ip".equals(args[2]))
            {
                username = args[1];
                password = "";
                ipAddress = args[3];
            }
            else if ("-p".equals(args[0]) && "-u".equals(args[2]))
            {
                username = args[3];
                password = args[1];
                ipAddress = "192.168.0.1";
            }
            else if ("-p".equals(args[0]) && "-ip".equals(args[2]))
            {
                username = "";
                password = args[1];
                ipAddress = args[3];
            }
            else if ("-ip".equals(args[0]) && "-u".equals(args[2]))
            {
                username = args[3];
                password = "";
                ipAddress = args[1];                
            }
            else if ("-ip".equals(args[0]) && "-p".equals(args[2]))
            {
                username = "";
                password = args[3];
                ipAddress = args[1];
            }
        }
        else if (args.length == 6)
        {
            for (int i = 0; i < args.length; i++)
            {
                if ("-u".equals(args[i]))
                {
                    username = args[i+1];
                }
                if ("-p".equals(args[i]))
                {
                    password = args[i+1];
                }
                if ("-ip".equals(args[i]))
                {
                    ipAddress = args[i+1];
                }
            }
        }
    }
}
    

