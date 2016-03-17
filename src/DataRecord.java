import java.util.regex.*;

/**
 * One record from the input datafiles
 * @author Nirav
 *
 */
public class DataRecord
{
  private String[] pageTitles;
  private String pageURL;
  private String[] candidateNameList;
  private String actualProductName;
  
  public DataRecord(String dataRecordLine)
  {
    Pattern pattern = Pattern.compile("###PAGE_URL=(.*?)###TITLE_START###(.*?)###TITLE_END######CANDIDATE_NAME_START###(.*?)###CANDIDATE_NAME_END######NAME=(.*?)$");
    Matcher matcher = pattern.matcher(dataRecordLine);
    
    if(matcher.find())
    {
      pageURL = matcher.group(1);
      pageTitles = matcher.group(2).split(";;");
      candidateNameList = matcher.group(3).split(";;");
      actualProductName = matcher.group(4);
      
      if(actualProductName == null || actualProductName.isEmpty())
        System.out.println("Missing product name in : " + pageURL);
    }
    else
      System.out.println("Skipping record : " + dataRecordLine);
  }
  
  public String[] pageTitles()
  {
    return this.pageTitles;
  }
  
  public String pageURL()
  {
    return this.pageURL;
  }
  
  public String[] candidateProductNames()
  {
    return this.candidateNameList;
  }
  
  public String actualProductName()
  {
    return this.actualProductName;
  }
}
