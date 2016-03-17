import java.util.*;
import java.util.regex.*;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Normalize and POS tags the title and the names and then send off the name matchers.
 * @author Nirav
 *
 */
public class Processor
{
  private MaxentTagger englishTagger;
  private MaxentTagger germanTagger;
  
  private HashMap<String, Double> enPOSTagWeightMap;
  private HashMap<String, Double> dePOSTagWeightMap;
  
  public Processor()
  {
    englishTagger = new MaxentTagger("tagmodel/english-left3words-distsim.tagger");
    germanTagger = new MaxentTagger("tagmodel/german-fast.tagger"); 
    
    buildPosTaggerWeightMaps();
  }
  
  private void buildPosTaggerWeightMaps()
  {
    enPOSTagWeightMap = new HashMap<String, Double>();
    enPOSTagWeightMap.put("NNP", 1.0);
    enPOSTagWeightMap.put("CC", 0.1);
    enPOSTagWeightMap.put("IN", 0.1);
    enPOSTagWeightMap.put("TO", 0.1);
    enPOSTagWeightMap.put("SYM", 0.1);
    enPOSTagWeightMap.put("DT", 0.1);
    
    dePOSTagWeightMap = new HashMap<String, Double>();
    dePOSTagWeightMap.put("NE", 1.0);
    dePOSTagWeightMap.put("ART", 0.1);
    dePOSTagWeightMap.put("PTKZU", 0.1);
    dePOSTagWeightMap.put("ITJ", 0.1);
    dePOSTagWeightMap.put("APPR", 0.1);
    dePOSTagWeightMap.put("APPRART", 0.1);
    dePOSTagWeightMap.put("APPRART", 0.1);
  }
  
  public String processRecord(DataRecord dataRecord)
  {
    String pageDomain = getDomain(dataRecord.pageURL());
    ArrayList<String> normalizedTitles = getNormalizedTitles(dataRecord, pageDomain);
    
    ArrayList<NameWithPOSTags> posTaggedTitles = new ArrayList<NameWithPOSTags>(normalizedTitles.size());
    String posTaggedContent;
    
    MaxentTagger tagger = pageDomain.toLowerCase().endsWith(".de") || pageDomain.toLowerCase().endsWith(".at") || pageDomain.toLowerCase().endsWith(".ch") ?
                          germanTagger : englishTagger;
    
    HashMap<String, Double> posTagWeightMap = pageDomain.toLowerCase().endsWith(".de") || pageDomain.toLowerCase().endsWith(".at") || pageDomain.toLowerCase().endsWith(".ch") ?
                                              dePOSTagWeightMap : enPOSTagWeightMap;
    
    for(String title : normalizedTitles)
    {
      posTaggedContent = tagger.tagString(title).replaceAll("(?i)(?<=[A-Za-z])'s_POS", " ");
      posTaggedTitles.add(new NameWithPOSTags(title, posTaggedContent));
    }

    ArrayList<String> normalizedNames = getNormalizedNames(dataRecord);
    ArrayList<NameWithPOSTags> posTaggedNames = new ArrayList<NameWithPOSTags>(normalizedNames.size());
    
    for(String name : normalizedNames)
    {
      posTaggedContent = tagger.tagString(name).replaceAll("(?i)(?<=[A-Za-z])'s_POS", " ");;
      posTaggedNames.add(new NameWithPOSTags(name, posTaggedContent));
    }
    
    double similarity = 0;
    double maxSimilarity = 0;
    int maxNameIndex = -1;    
    
    for(int i = 0; i < posTaggedNames.size(); i++)
    {
      similarity = 0;
      
      for(int j = 0; j < posTaggedTitles.size(); j++)
      {
        CosineSimilarity cosineSimilarity = new CosineSimilarity(posTaggedTitles.get(j), posTaggedNames.get(i), posTagWeightMap);
        similarity = cosineSimilarity.getSimilarity();
        
        if(similarity >= 0.6 && similarity > maxSimilarity)
        {
          maxSimilarity = similarity;
          maxNameIndex = i;
        }
      }
    }
        
    if(maxNameIndex >= 0)
      return dataRecord.candidateProductNames()[maxNameIndex];
    else
      return null;
  }
  
  private ArrayList<String> getNormalizedTitles(DataRecord dataRecord, String pageDomain)
  {
    ArrayList<String> normalizedTitles = new ArrayList<String>();
    
    String pageDomainWithoutWWW = pageDomain.replaceFirst("(?i)www\\d?\\.", "");
    String[] domainComponents = domainComponents(pageDomain);
    
    String formattedName = null;
    
    for(String title : dataRecord.pageTitles())
    {
      formattedName = removeDomainFromContent(title, pageDomain, pageDomainWithoutWWW, domainComponents);
      normalizedTitles.add(normalizeName(formattedName, false));
    }
    
    return normalizedTitles;
  }
  
  private ArrayList<String> getNormalizedNames(DataRecord dataRecord)
  {
    ArrayList<String> normalizedNames = new ArrayList<String>();
    
    for(String candidateName : dataRecord.candidateProductNames())
    {
      normalizedNames.add(normalizeName(candidateName, false));
    }
    
    return normalizedNames;
  }
  
  private String normalizeName(String name, boolean changeToLowercase)
  {
    if(name == null)
      return null;
    
    String normalizedName = (changeToLowercase) ? name.toLowerCase() : name;
    return normalizedName.replaceAll("<[^>]*?>", " ").replaceAll("[\\(\\)\\.,\\!;:]+", " ").replaceAll("(?i)(['`]s)(?=(\\W|$))", "").replaceAll("\\s+", " ").replaceAll("&amp;", "&").replaceAll("^\\W+", "").replaceAll("\\W+$", "");
  }
  
  private String removeDomainFromContent(String content, String pageDomain, String domainWithoutWWW, String[] domainComponents)
  {
    String newContent = content.replaceAll(pageDomain, " ").replaceAll(domainWithoutWWW, " ");
    
    StringBuilder regex = new StringBuilder("(");
    
    for(String domainComp : domainComponents)
      regex.append(domainComp).append("|");
    regex.setLength(regex.length() - 1);
    regex.append(")");
    
    String patternContent = "(?<=(^|\\b))" + regex.toString() + "(?=(^|\\b))";
//    System.out.println(patternContent);
    Pattern p = Pattern.compile(patternContent, Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(newContent);
    
    return m.replaceAll(" ").replaceAll("\\s+", " ");
  }
  
  private String getDomain(String pageURL)
  {
    if(pageURL != null)
    {
      String domain = pageURL.replaceFirst("(?i)https?://", "").replaceFirst("(:\\d+)?/.*$", "");
      return domain.toLowerCase();
    }
    return null;
  }
  
  private String[] domainComponents(String domain)
  {
    return (domain != null) ? domain.split("\\.") : null;
  }
}

class NameWithPOSTags
{
  String name;
  String posTaggedName;
  
  NameWithPOSTags(String name, String posTaggedName)
  {
    this.name = name;
    this.posTaggedName = posTaggedName;
  }
}