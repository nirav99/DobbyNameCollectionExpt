import java.util.*;

public class CosineSimilarity
{
  private NameWithPOSTags candidateName;
  private NameWithPOSTags title;
  
  private HashMap<String, Integer> vocabulary;
  private HashMap<String, Double> posTagWeight;
  
  public static boolean DEBUG_MODE = false;
  
  public CosineSimilarity(NameWithPOSTags title, NameWithPOSTags candidateName, HashMap<String, Double> posTagWeight)
  {
    this.posTagWeight = posTagWeight;
    this.candidateName = candidateName;
    this.title = title;
  }
  
  public double getSimilarity()
  {
    long startTime = 0;
    
    buildVocabulary();
    double[] titleVector = getVector(title.posTaggedName);
    double[] nameVector = getVector(candidateName.posTaggedName);
    
    double numerator = 0;
    
    for(int i = 0; i < titleVector.length; i++)
      numerator = numerator + ((titleVector[i]) * (nameVector[i]));
    
    double denominator = getNormOfVector(titleVector) * getNormOfVector(nameVector);
    double similarity = (denominator > 0) ? 1.0 * numerator / denominator : 0;
    
    long endTime = 0;
    
 //   System.out.println("Cosine Similarity : " + similarity);
 //   System.out.format("Processing time : %.4f sec\n", 1.0 * (endTime - startTime) / 1000.0);
    
    if(DEBUG_MODE)
    {
      System.out.println("Name Vector :");
      printVector(nameVector);
      System.out.println("Title Vector :");
      printVector(titleVector);
    }
    return similarity;
  }
  
  private void buildVocabulary()
  {
    vocabulary = new HashMap<String, Integer>();
    addNameToVocabulary(this.title.posTaggedName);
    addNameToVocabulary(this.candidateName.posTaggedName);
  }
  
  private void addNameToVocabulary(String content)
  {
    String[] words = content.split("\\s+");
    String formattedWord;
    Integer value;
    int index = vocabulary.size();
    
    for(String word : words)
    {
      // Remove POS tag from the word and make it lowercase
      formattedWord = word.replaceFirst("_[A-Z]+$", "").toLowerCase();
      
      value = vocabulary.get(formattedWord);
      
      if(value == null)
        vocabulary.put(formattedWord, index++);
    }
  }
  
  private double[] getVector(String content)
  {
    String[] words = content.split("\\s+");
    
    Integer index;
    String formattedWord;
    
    double[] vector = new double[vocabulary.size()];
    double weight;
    
    double multiplier = 1.0;
    
    for(String word : words)
    {
      // Remove POS tag from the word and make it lowercase
      formattedWord = word.replaceFirst("_[A-Z]+$", "").toLowerCase();
      index = vocabulary.get(formattedWord);
      
      if(DEBUG_MODE) System.out.println("Word = " + word + " formatted word = " + formattedWord + " Index = " + index);
      weight = getWeightForWord(word);
      vector[index] = weight * multiplier;

      /*
      multiplier = multiplier - 0.05;
      if(multiplier <= 0)
        multiplier = 0.1;
      */
    }
    
    return vector;
  }
  
  private double getWeightForWord(String word)
  {
    int lastIndexOfUnderscore = word.lastIndexOf("_");
    
    if(lastIndexOfUnderscore >= 0)
    {
      String posTag = word.substring(lastIndexOfUnderscore + 1);
      Double weight = posTagWeight.get(posTag);
      
      return (weight != null) ? weight : 0.7;
    }
    else
      return 0;
  }
  
  private void printVector(double[] vector)
  {
    for(int i = 0; i < vector.length; i++)
      System.out.print(vector[i] + " ");
    System.out.println();
  }
  
  private double getNormOfVector(double input[])
  {
    double norm = 0;
    
    for(int i = 0; i < input.length; i++)
      norm = norm + input[i] * input[i];
    
    return Math.sqrt(norm);
  }
  
  public static void main(String[] args)
  {
    try
    {
      String name = "Champion_NNP Men_NNP 's_POS Powertrain_NNP Tank_NNP Top_NNP";
      String title = "Champion_NNP Men_NNP 's_POS Powertrain_NNP Tank_NNP Top_NNP at_IN Amazon_NNP Men_NNP 's_POS Clothing_NNP store_NN";
      
      NameWithPOSTags posName = new NameWithPOSTags("Champion Men's Powertrain Tank Top", name);
      NameWithPOSTags posTitle = new NameWithPOSTags("Champion Men's Powertrain Tank Top at Amazon Men's Clothing store", title);
      
      HashMap<String, Double> posTagWeightMap = new HashMap<String, Double>();
      posTagWeightMap.put("NNP", 1.0);
      posTagWeightMap.put("CC", 0.1);
      posTagWeightMap.put("IN", 0.1);
      posTagWeightMap.put("TO", 0.1);
      posTagWeightMap.put("SYM", 0.1);
      posTagWeightMap.put("DT", 0.1);
      
      CosineSimilarity cosSim = new CosineSimilarity(posTitle, posName, posTagWeightMap);
      CosineSimilarity.DEBUG_MODE = true;
      System.out.println("Similarity = " + cosSim.getSimilarity());
    }
    catch(Exception e)
    {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }
}
