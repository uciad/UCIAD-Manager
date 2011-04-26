/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.uciad.util;

/**
 *
 * @author se3535
 */
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPUtils {

    public static void main (String args[]) {
        try {
            System.out.println(getFrom("http://data.open.ac.uk/oro/21488"));
        } catch (Exception ex) {
            Logger.getLogger(HTTPUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static String postTo(String URL, String query) {
        StringBuffer res = new StringBuffer("");
        try {
            URL url = new URL(URL);
            URLConnection postUrlConnection = url.openConnection();
            postUrlConnection.setDoOutput(true);
            postUrlConnection.setDoInput(true);
            postUrlConnection.setUseCaches(false);
            postUrlConnection.setAllowUserInteraction(false);
            postUrlConnection.setRequestProperty("Accept",
                    "application/xml");

            DataOutputStream dos = new DataOutputStream(postUrlConnection.getOutputStream());
            dos.writeBytes(query);
            dos.close();

            InputStream postInputStream = postUrlConnection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(postInputStream));
           // String str = null;
            char[] buff = new char[1024 * 100];
            int n;
            int i = 0;
            do {
                n = in.read(buff);
                if (n != -1) {
                    res.append(new String(buff, 0, n));
                }
                i += n;
                System.out.print((i / 1024) + "K(" + n + ") ");
            // res.append(str);
            } while (n != -1);
            // while ((str = in.readLine()) != null) {
            // System.out.println(str);
            // res += str+"\n";
            // }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res.toString();
    }

    public static String getFrom(String URL) throws Exception {
        StringBuffer res = new StringBuffer("");
        try {
            URL url = new URL(URL);
            URLConnection postUrlConnection = url.openConnection();
            postUrlConnection.setDoOutput(true);
            postUrlConnection.setUseCaches(false);
            postUrlConnection.setRequestProperty("Accept","application/rdf+xml");

            InputStream postInputStream = postUrlConnection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(postInputStream));
            
            char[] buff = new char[1024 * 100];
            int n;
            int i = 0;
            do {
                n = in.read(buff);
                if (n != -1) {
                    res.append(new String(buff, 0, n));
                }
                i += n;
            } while (n != -1);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res.toString();
    }

    // This approach is slightly different from the above method because it reads and writes zip files which
    // does not contain lines so doesn't have to check for new line characters etc which causes zip file to corrupt as well.
    
    @SuppressWarnings("finally")
	public static boolean downloadZipFile(String URL, String indexNumber, String indexArchivePath) throws Exception {
        boolean success = false;
        try {
            URL url = new URL(URL+"/"+indexNumber+"-index.zip");
            URLConnection postUrlConnection = url.openConnection();
            postUrlConnection.setDoOutput(true);
            postUrlConnection.setUseCaches(false);
            postUrlConnection.setRequestProperty("content-type","binary/data");

            InputStream in = postUrlConnection.getInputStream();
            FileOutputStream fout = new FileOutputStream(indexArchivePath+"/"+indexNumber+"-index.zip");
            
            byte[] buff = new byte[8192]; // 1024 * 8
            int n;
            while ((n = in.read(buff))>0) {
                      fout.write(buff, 0, n);
            }
            in.close();
            fout.close();
            success = true;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        } finally {
            return success;
        }
    }
    
    
	public static boolean downloadZipFile(String URLStr, String zipFileName) throws Exception {
        boolean success = false;
        try {
            URL url = new URL(URLStr);
            URLConnection postUrlConnection = url.openConnection();
            postUrlConnection.setDoOutput(true);
            postUrlConnection.setUseCaches(false);
            postUrlConnection.setRequestProperty("content-type","binary/data");

            InputStream in = postUrlConnection.getInputStream();
            FileOutputStream fout = new FileOutputStream("data/"+zipFileName);
            
            byte[] buff = new byte[8192]; // 1024 * 8
            int n;
            while ((n = in.read(buff))>0) {
                      fout.write(buff, 0, n);
            }
            in.close();
            fout.close();
            success = true;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        } finally {
            return success;
        }
    }
}

