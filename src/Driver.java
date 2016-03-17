import java.io.*;
import java.util.*;

public class Driver
{
  private ArrayList<DataRecord> dataRecordList;
  
  private int totalRecords;
  private int totalCorrectlyIdentifiedNames;
  private Processor processor;
  
  public Driver(File inputFile) throws IOException
  {
    dataRecordList = new ArrayList<DataRecord>();
    processor = new Processor();
    readInputDataFile(inputFile);
  }
  
  private void readInputDataFile(File inputFile) throws IOException
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
    
    String line = null;
    
    while((line = reader.readLine()) != null)
    {
      dataRecordList.add(new DataRecord(line));
    }
    reader.close();
    
    System.out.println("Correctly read " + dataRecordList.size() + " records");
  }
  
  public void processRecords()
  {
    String productName;
    String actualProductName;
    
    for(DataRecord dataRecord : dataRecordList)
    {
      productName = processor.processRecord(dataRecord);
      actualProductName = dataRecord.actualProductName();
      
     System.out.println("Actual product name : " + actualProductName);
     System.out.println("Found by program    : " + productName);
     System.out.println("========================");
    }
  }
  
  public static void main(String[] args)
  {
    try
    {
      File inputFile = new File("C:\\Users\\Nirav\\workspace\\DobbyNameCollectionExpt\\datafiles\\Title_CandidateName_log.txt");
      Driver driver = new Driver(inputFile);
      driver.processRecords();
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
}
