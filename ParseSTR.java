import java.util.*; 
import java.io.*;
import java.io.Console;

/**
 * Converts a .csv file of haplotypes downloaded
 * from Family Tree DNA project pages to a format
 * that can be used for data analysis by doing the
 * following:
 *  - asks the user which .csv in the current
 *    directory that they'd like to convert
 *  - processes the file to split separate
 *    multi-site DYS markers into discrete values
 *  - saves the group assignment for each record in a
 *    new column called "assignment" and then deletes
 *    the group header rows
 *  - outputs the the converted data to file named by
 *    appending "_converted" to the original file name
 *    like so:
 *         z251_raw_data.csv --> z251_raw_data_converted.csv
 * 
 *  
 * @author Cai Stuart-Maver
 * @version 2015-12-09
 **/

public class ParseSNP
{
    /**
     * Reads from a user-provided .csv file, converts the data
     * to a format suitale for data analysis, and writes the new
     * data to a new .csv file
     *
     * @param args not used
     */
    
    public static void main(String[] args)
    {
        
        System.out.println("  _____                     _____ _   _ _____  \n" +
                           " |  __ \\                   / ____| \\ | |  __ \\ \n" +
                           " | |__) |_ _ _ __ ___  ___| (___ |  \\| | |__) |\n" +
                           " |  ___/ _` | '__/ __|/ _ \\\\___ \\| . ` |  ___/ \n" +
                           " | |  | (_| | |  \\__ \\  __/____) | |\\  | |     \n" +
                           " |_|   \\__,_|_|  |___/\\___|_____/|_| \\_|_|     \n");
            
        
        
        System.out.println("0=============================================0\n" +
                           "|  Welcome to the PareseSNP csv parsing tool  |\n" +
                           "|                                             |\n" +
                           "|  Ver: 0.7      Author: Cai Stuart-Maver     |\n" +
                           "|                        cm19@humboldt.edu    |\n" +
                           "0=============================================0\n");
        
        System.out.println("\n\n" +
                           "This application converts a .csv containing STR data\n" +
                           "and SNP group assignments downloaded from Family Tree\n" +
                           "DNA to a format suitable for data analysis.\n\n");
        
        File csvToParse = chooseCSV();
        
        try
        {
            Scanner inFile = new Scanner(csvToParse);
            
            String inFileName = csvToParse.getName();
            String outFileName = inFileName.substring(0, inFileName.indexOf('.'));
            outFileName = outFileName + "_converted.csv";
            
            
            PrintWriter outFile = new PrintWriter(outFileName);
            
            int lineNum = 0;
            String outputStr;
            String groupHeader = "";
            String[] splitStr;
            
            while(inFile.hasNext())
            {
                // Treat the first line differently, it's the header row
                if (lineNum == 0)
                {
                    outputStr = inFile.nextLine();
                    
                    //add the new assignment column
                    outputStr = "Assignment, " + outputStr;
                    
                    //split multi-site marker headers into unique columns
                    outputStr = outputStr.replaceFirst("DYS385", "DYS385a, DYS385b");
                    outputStr = outputStr.replace("DYS459", "DYS459a, DYS459b");
                    outputStr = outputStr.replace("DYS464", "DYS464a, DYS464b, DYS464c, DYS464d");
                    outputStr = outputStr.replace("YCAII", "YCAIIa, YCAIIb");
                    outputStr = outputStr.replace("CDY", "CDYa, CDYb");
                    outputStr = outputStr.replace("DYF395S1", "DYF395S1a, DYF395S1b");
                    outputStr = outputStr.replace("DYS413", "DYS413a, DYS413b");
                    
                    //write header row to the file
                    outFile.println(outputStr);
                }
                // the rest of the lines are group headers and records
                else
                {
                    outputStr = inFile.nextLine();
                    
                    /*
                     * if this line contains quotes, it may have commas
                     * embedded in the quotes which will throw off the
                     * split(",") method below, so replace them with ";"
                     */
                    if(outputStr.contains("\""))
                    {
                        
                        String tempStr = outputStr.substring(outputStr.indexOf('"'),
                                                             outputStr.lastIndexOf('"'));
                        String replacementStr = tempStr.replace(",", ";");
                        outputStr = outputStr.replace(tempStr, replacementStr);
                        
                        // uncomment the lines below for debugging
                        /*
                        System.out.println(tempStr);
                        System.out.println(replacementStr);
                        System.out.println(outputStr);
                        */
                    }
                    
                    splitStr = outputStr.split(",");
                    
                    //split the multiple DYS values into single data elements
                    
                    outputStr = "";
                    
                    if(splitStr.length > 1) //group header rows have a length of one, skip those
                    {
                        outputStr = groupHeader + ",";
                        
                        for(int i = 0; i < splitStr.length; i++)
                        {
                            if(splitStr[i].indexOf("-") != -1 && i > 4) //only look for '-' in DYS cells
                            {
                                /* for DYS464, most kits have 4 2-digit values
                                 * searated by "-", so 11 chars. A very few kits
                                 * have 6 values, and in those cases, we discard
                                 * the 5th and 6th values
                                 */
                                if(splitStr[i].length() > 11)
                                {
                                    splitStr[i] = splitStr[i].substring(0,12);
                                }
                                
                                splitStr[i] = splitStr[i].replaceAll("-", ",");
                            }
                            
                            outputStr = outputStr + splitStr[i] + ",";
                        }
                        
                        outFile.println(outputStr);
                    }
                    else
                    {
                        /* when a group header is found, the next lines will
                         * have the group header saved to their 'assignment'
                         * column. Group headers are not otherwise written to
                         * the file
                         */
                        groupHeader = splitStr[0];
                    }
                }
                
                lineNum++;
            }
            
            inFile.close();
            outFile.close();
            
            System.out.println("\n\n" + inFileName + " has been parsed;\n" +
                               "the converted file name is " + outFileName + 
                               "\nGoodbye!");
        }
        catch (Exception e)
        {
        }
          
    } // end main method
    
    /**
     * searches the current working directory for .csv files,
     * saves them to an array list, then asks the user to select
     * on of the files to convert
     * 
     * @returns File the .csv in the current directory chosen by the user
     */
    
    private static File chooseCSV()
    {
        String path = System.getProperty("user.dir");
        ArrayList<File> csvFileList = new ArrayList<File>();
        int csvListIncrementor = 0;
        
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        
        System.out.println("Here are the CSV files in the current directory: ");
        
        for (int i = 0; i < listOfFiles.length; i++) 
        {
            if (listOfFiles[i].isFile()) 
            {
                String fileName = listOfFiles[i].getName();
                if (fileName.endsWith(".csv"))
                {
                    csvFileList.add(new File(fileName));
                    System.out.println("File [" + csvListIncrementor + "]: " + listOfFiles[i].getName());
                    csvListIncrementor++;
                }
            } 
        }
        
        System.out.print("Please enter the number of the file you'd like to parse (or -1 to quit): ");
        
        Scanner scanIn = new Scanner(System.in);
        
        int choice = Integer.parseInt(scanIn.nextLine());
        
        if(choice == -1)
        {
            System.out.println("Quit command received; exiting program.");
            System.exit(0);
        }
        else if(choice < 0 || choice > (csvFileList.size()-1))
        {
            System.out.println("Invalid option. Pregram will terminate.");
            System.exit(1);
        }
        
        // comment out line below for debugging
        // System.out.println(choice);
        
        return csvFileList.get(choice);
    }

} // end class ParseSNP
    
