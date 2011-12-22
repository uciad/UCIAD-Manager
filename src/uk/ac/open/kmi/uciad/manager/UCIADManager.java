package uk.ac.open.kmi.uciad.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.open.kmi.uciad.repository.manager.UCIADRepositoryManager;
import uk.ac.open.kmi.uciad.util.DataCompressor;
import uk.ac.open.kmi.uciad.util.DateUtils;
import uk.ac.open.kmi.uciad.util.HTTPUtils;

public class UCIADManager {

    private static DateFormat dateFormatForParsedZipFile = new SimpleDateFormat("dd-MMM-yyyy");
    
    public static void main(String args[]) {
        //Arguments expected serverName(e.g. web06) 12-May-2011 -- 18-May-2011
        //OR
        //Arguments expected serverName(e.g. web06) 12-May-2011 13-May-2011 14-May-2011
        //String args [] ={"web06"};        
        try {
            
            if (args.length > 1) {
                assignProperties(args[0]);
                if (args.length == 4 && args[2].equals("--")) {
                    if ((args[1].contains("/") || !args[1].contains("-")) || (args[3].contains("/") || !args[3].contains("-"))) {
                        System.out.println("Please input dates in dd-MMM-yyyy format only.");
                        System.exit(2);
                    }
                    List<Date> dates = DateUtils.getListOfDatesBetween(args[1], args[3], "dd-MMM-yyyy");
                    Iterator datesItr = dates.iterator();
                    while (datesItr.hasNext())
                    {
                        Date date = (Date)datesItr.next();
                        updateRepository(dateFormatForParsedZipFile.format(date));  
                    }
                } else {
                    for (String date : args) {
                        if (!date.contains("web"))
                        {
                            if (date.contains("/") || !date.contains("-")) {
                                System.out.println("Please input dates in dd-MMM-yyyy format only.");
                                System.exit(2);
                            }
                            updateRepository(date);
                        }                        
                    }
                }
            } else if(args.length > 0){
                assignProperties(args[0]);
                String date = dateFormatForParsedZipFile.format(DateUtils.getYesterdaysDate());                
                //removes contexts older than 7 days from the main inferenced repository.
                //System.out.println("Context to be removed: "+System.getProperty("context") + "_" + dateFormatForParsedZipFile.format(DateUtils.getDate7DaysAgo()));
                UCIADRepositoryManager.remove(System.getProperty("context") + "_" + dateFormatForParsedZipFile.format(DateUtils.getDate7DaysAgo()));
                updateRepository(date);
                
            }
            
        }catch (Exception ex) {
            Logger.getLogger(UCIADManager.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private static void assignProperties(String serverName) {
        // Reading properties
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("conf/uciadManager.conf." + serverName));
            for (Object key : properties.keySet().toArray()) {
                System.setProperty((String) key, properties.getProperty((String) key));
            }
        } catch (FileNotFoundException e) {            
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateRepository(String date) {
        try {
            String zipFileName = "parsedLog_" + date + ".zip";
            System.out.println(System.getProperty("parsedLogURL") + "/" + zipFileName);
            HTTPUtils.downloadZipFile((System.getProperty("parsedLogURL") + "/" + zipFileName), zipFileName);
            UCIADRepositoryManager.clearDestDir("data/unZip/");
            DataCompressor.unZip("data/" + zipFileName);
            File sourceDir = new File("data/unZip/");
            File[] listOfFiles = sourceDir.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) 
            {   
                File file = new File("data/unZip/" + listOfFiles[i].getName());
                if (file.exists())
                {   
                    Date dateInQuestion = (Date) dateFormatForParsedZipFile.parse(date);
                    if (!DateUtils.olderOrEqualToDate7DaysAgo(dateInQuestion))
                    {
                         //adds to the main inferenced (upto 7 days) repository
                        UCIADRepositoryManager.add(file.getPath(), System.getProperty("context") + "_" + date, "7dayRepID");
                        System.out.println("UCIAD done");   
                    }                    
                    //adds to the main non-inferenced repository
                    UCIADRepositoryManager.add(file.getPath(), System.getProperty("context") + "_" + (date.split("-"))[1] + "-" + (date.split("-"))[2], "repID");                    
                    System.out.println("UCIADAll done");
                }
            }
           
        } catch (Exception ex) {
            Logger.getLogger(UCIADManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
}
