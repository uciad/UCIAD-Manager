package uk.ac.open.kmi.uciad.manager;

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

    public static void main(String args[]) {
        //String args[] = {"02/Apr/2011", "--", "27/Mar/2011"};
        try {
            // Reading properties
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream("conf/uciadManager.conf"));
                for (Object key : properties.keySet().toArray()) {
                    System.setProperty((String) key, properties.getProperty((String) key));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            DateFormat dateFormatForParsedZipFile = new SimpleDateFormat("dd-MMM-yyyy");
            if (args.length > 0) {
                if (args.length == 3 && args[1].equals("--")) {
                    if ((args[0].contains("/") || !args[0].contains("-")) || (args[2].contains("/") || !args[2].contains("-"))) {
                        System.out.println("Please input dates in dd-MMM-yyyy format only.");
                        System.exit(2);
                    }
                    List<Date> dates = DateUtils.getListOfDatesBetween(args[0], args[2], "dd-MMM-yyyy");
                    Iterator datesItr = dates.iterator();
                    while (datesItr.hasNext())
                    {
                        Date date = (Date)datesItr.next();
                        updateRepository(dateFormatForParsedZipFile.format(date));                        
                    }
                } else {
                    for (String date : args) {
                        if (date.contains("/") || !date.contains("-")) {
                            System.out.println("Please input dates in dd-MMM-yyyy format only.");
                            System.exit(2);
                        }
                        updateRepository(date);
                    }
                }
            } else {
                String date = dateFormatForParsedZipFile.format(DateUtils.getYesterdaysDate());                
                updateRepository(date);
                //removes contexts older than 7 days from the main inferenced repository.
                UCIADRepositoryManager.remove(System.getProperty("context") + "_" + dateFormatForParsedZipFile.format(DateUtils.getDate7DaysAgo()));
            }

//        } catch (ParseException ex) {
//            System.out.println("Please input dates in dd-MMM-yyyy format only.");
//        }
        }catch (Exception ex) {
            Logger.getLogger(UCIADManager.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private static void updateRepository(String date) {
        try {
            String zipFileName = "parsedLog_" + date + ".zip";
            System.out.println(System.getProperty("parsedLogURL") + "/" + zipFileName);
            HTTPUtils.downloadZipFile((System.getProperty("parsedLogURL") + "/" + zipFileName), zipFileName);
            DataCompressor.unZip("data/" + zipFileName);
            //adds to the main inferenced (upto 7 days) repository
            UCIADRepositoryManager.add("data/unZip/parsedLog.rdf", System.getProperty("context") + "_" + date, "7dayRepId");            
            //adds to the main non-inferenced repository
            UCIADRepositoryManager.add("data/unZip/parsedLog.rdf", System.getProperty("context") + "_" + (date.split("-"))[1] + "-" + (date.split("-"))[2], "repID");        
        } catch (Exception ex) {
            Logger.getLogger(UCIADManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
