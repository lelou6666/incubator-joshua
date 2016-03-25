package joshua.decoder.phrase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import joshua.corpus.Vocabulary;
import joshua.decoder.JoshuaConfiguration;
import joshua.decoder.ff.FeatureFunction;
<<<<<<< HEAD
import joshua.decoder.ff.tm.BasicRuleCollection;
import joshua.decoder.ff.tm.BilingualRule;
=======
>>>>>>> refs/remotes/apache/master
import joshua.decoder.ff.tm.Grammar;
import joshua.decoder.ff.tm.Rule;
import joshua.decoder.ff.tm.RuleCollection;
import joshua.decoder.ff.tm.Trie;
<<<<<<< HEAD
import joshua.util.io.LineReader;
=======
import joshua.decoder.ff.tm.hash_based.MemoryBasedBatchGrammar;
import joshua.decoder.ff.tm.packed.PackedGrammar;
>>>>>>> refs/remotes/apache/master

/**
 * Represents a phrase table, and is implemented as a wrapper around either a {@link PackedGrammar}
 * or a {@link MemoryBasedBatchGrammar}.
 * 
 * TODO: this should all be implemented as a two-level trie (source trie and target trie).
 */
<<<<<<< HEAD

public class PhraseTable implements Grammar {
=======
public class PhraseTable implements Grammar {
  
  private JoshuaConfiguration config;
  private Grammar backend;
>>>>>>> refs/remotes/apache/master
  
  private String grammarFile;
  private int owner;
  private JoshuaConfiguration config;  
  private HashMap<PhraseWrapper, RuleCollection> entries;
  private int numRules;
  private List<FeatureFunction> features;
  private int maxSourceLength;

  /**
   * Chain to the super with a number of defaults. For example, we only use a single nonterminal,
   * and there is no span limit.
   * 
   * @param grammarFile
   * @param owner
   * @param config
   * @throws IOException
   */
<<<<<<< HEAD
  public PhraseTable(String grammarFile, String owner, JoshuaConfiguration config, List<FeatureFunction> features) throws IOException {
    this.config = config;
    this.owner = Vocabulary.id(owner);
    this.grammarFile = grammarFile;
    this.features = features;
    this.maxSourceLength = 0;
    Vocabulary.id("[X]");
    
    this.entries = new HashMap<PhraseWrapper, RuleCollection>();

    loadPhraseTable();
  }
  
  public PhraseTable(String owner, JoshuaConfiguration config, List<FeatureFunction> features) {
    this.config = config;
    this.owner = Vocabulary.id(owner);
    this.features = features;
    this.maxSourceLength = 0;
    
    this.entries = new HashMap<PhraseWrapper, RuleCollection>();
  }
  
  private void loadPhraseTable() throws IOException {
    
    String prevSourceSide = null;
    List<String> rules = new ArrayList<String>(); 
    int[] french = null;
    
    for (String line: new LineReader(this.grammarFile)) {
      int sourceEnd = line.indexOf(" ||| ");
      String source = line.substring(0, sourceEnd);
      String rest = line.substring(sourceEnd + 5);

      rules.add(rest);
      
      if (prevSourceSide == null || ! source.equals(prevSourceSide)) {

        // New source side, store accumulated rules
        if (prevSourceSide != null) {
          System.err.println(String.format("loadPhraseTable: %s -> %d rules", Vocabulary.getWords(french), rules.size()));
          entries.put(new PhraseWrapper(french), new LazyRuleCollection(owner, 1, french, rules));
          rules = new ArrayList<String>();
        }
        
        String[] foreignWords = source.split("\\s+");
        french = new int[foreignWords.length];
        for (int i = 0; i < foreignWords.length; i++)
          french[i] = Vocabulary.id(foreignWords[i]);

        maxSourceLength = Math.max(french.length, getMaxSourcePhraseLength());
        
        prevSourceSide = source;
      }
    }
    
    if (french != null) {
      entries.put(new PhraseWrapper(french), new LazyRuleCollection(owner, 1, french, rules));
      System.err.println(String.format("loadPhraseTable: %s -> %d rules", Vocabulary.getWords(french), rules.size()));
    }
  }

=======
  public PhraseTable(String grammarFile, String owner, String type, JoshuaConfiguration config, int maxSource) 
      throws IOException {
    this.config = config;
    int spanLimit = 0;
    
    if (grammarFile != null && new File(grammarFile).isDirectory()) {
      this.backend = new PackedGrammar(grammarFile, spanLimit, owner, type, config);
      if (this.backend.getMaxSourcePhraseLength() == -1) {
        System.err.println("FATAL: Using a packed grammar for a phrase table backend requires that you");
        System.err.println("       packed the grammar with Joshua 6.0.2 or greater");
        System.exit(-1);
      }

    } else {
      this.backend = new MemoryBasedBatchGrammar(type, grammarFile, owner, "[X]", spanLimit, config);
    }
  }
  
  public PhraseTable(String owner, JoshuaConfiguration config) {
    this.config = config;
    
    this.backend = new MemoryBasedBatchGrammar(owner, config);
  }
      
>>>>>>> refs/remotes/apache/master
  /**
   * Returns the longest source phrase read. For {@link MemoryBasedBatchGrammar}s, we subtract 1
   * since the grammar includes the nonterminal. For {@link PackedGrammar}s, the value was either
   * in the packed config file (Joshua 6.0.2+) or was passed in via the TM config line.
   * 
   * @return
   */
  public int getMaxSourcePhraseLength() {
<<<<<<< HEAD
    return maxSourceLength;
=======
    if (backend instanceof MemoryBasedBatchGrammar)
      return this.backend.getMaxSourcePhraseLength() - 1;
    else
      return this.backend.getMaxSourcePhraseLength();
>>>>>>> refs/remotes/apache/master
  }

  /**
   * Collect the set of target-side phrases associated with a source phrase.
   * 
   * @param sourceWords the sequence of source words
   * @return the rules
   */
<<<<<<< HEAD
  public List<Rule> getPhrases(int[] sourceWords) {
    RuleCollection rules = entries.get(new PhraseWrapper(sourceWords));
    if (rules != null) {
//      System.err.println(String.format("PhraseTable::getPhrases(%s) = %d of them", Vocabulary.getWords(sourceWords),
//          rules.getRules().size()));
      return rules.getSortedRules(features);
=======
  public RuleCollection getPhrases(int[] sourceWords) {
    if (sourceWords.length != 0) {
      Trie pointer = getTrieRoot();
      if (! (backend instanceof PackedGrammar))
        pointer = pointer.match(Vocabulary.id("[X]"));
      int i = 0;
      while (pointer != null && i < sourceWords.length)
        pointer = pointer.match(sourceWords[i++]);

      if (pointer != null && pointer.hasRules()) {
        return pointer.getRuleCollection();
      }
>>>>>>> refs/remotes/apache/master
    }
    return null;
  }

  /**
   * Adds a rule to the grammar. Only supported when the backend is a MemoryBasedBatchGrammar.
   * 
   * @param rule the rule to add
   */
  public void addRule(Rule rule) {
    ((MemoryBasedBatchGrammar)backend).addRule(rule);
  }
  
  public void addEOSRule() {
    int[] french = { Vocabulary.id("[X]"), Vocabulary.id("</s>") };
    
    maxSourceLength = Math.max(getMaxSourcePhraseLength(), 1);

    RuleCollection rules = new BasicRuleCollection(1, french);
    rules.getRules().add(Hypothesis.END_RULE);
    entries.put(new PhraseWrapper(new int[] { Vocabulary.id("</s>") }), rules); 
    
//    List<String> rules = new ArrayList<String>();
//    rules.add("[X,1] </s> ||| 0");
//    entries.put(new PhraseWrapper(new int[] { Vocabulary.id("</s>") }), new LazyRuleCollection(owner, 1, french, rules));
  }
  
  @Override
  public void addOOVRules(int sourceWord, List<FeatureFunction> features) {
    // TODO: _OOV shouldn't be outright added, since the word might not be OOV for the LM (but now almost
    // certainly is)
<<<<<<< HEAD
    int[] french = { Vocabulary.id("[X]"), sourceWord };
    
    String targetWord = (config.mark_oovs 
        ? Vocabulary.word(sourceWord) + "_OOV"
        : Vocabulary.word(sourceWord));

    int[] english = { -1, Vocabulary.id(targetWord) };
    final byte[] align = { 0, 0 };
    
    maxSourceLength = Math.max(getMaxSourcePhraseLength(), 1);
    
    BilingualRule oovRule = new BilingualRule(Vocabulary.id("[X]"), french, english, "", 1, align);
    oovRule.setOwner(owner);
    oovRule.estimateRuleCost(features);
    
//    List<String> rules = new ArrayList<String>();
//    rules.add(String.format("[X,1] %s ||| -1 ||| 0-0 1-1", targetWord));
//  entries.put(new PhraseWrapper(new int[] { sourceWord }), new LazyRuleCollection(owner, 1, french, rules));
    
    RuleCollection rules = new BasicRuleCollection(1, french);
    rules.getRules().add(oovRule);
    entries.put(new PhraseWrapper(new int[] { sourceWord }), rules); 
  }

  /**
   * The phrase table doesn't use a trie.
   */
  @Override
  public Trie getTrieRoot() {
    return null;
  }

  /**
   * We don't pre-sort grammars!
   */
  @Override
  public void sortGrammar(List<FeatureFunction> models) {
  }

  /**
   * We never pre-sort grammars! Why would you?
   */
  @Override
  public boolean isSorted() {
    return false;
  }

  @Override
  public boolean hasRuleForSpan(int startIndex, int endIndex, int pathLength) {
    // No limit on maximum phrase length
=======
    int targetWord = config.mark_oovs
        ? Vocabulary.id(Vocabulary.word(sourceWord) + "_OOV")
        : sourceWord;   

    int nt_i = Vocabulary.id("[X]");
    Rule oovRule = new Rule(nt_i, new int[] { nt_i, sourceWord },
        new int[] { -1, targetWord }, "", 1, null);
    addRule(oovRule);
    oovRule.estimateRuleCost(featureFunctions);
        
//    String ruleString = String.format("[X] ||| [X,1] %s ||| [X,1] %s", 
//        Vocabulary.word(sourceWord), Vocabulary.word(targetWord));
//    BilingualRule oovRule = new HieroFormatReader().parseLine(ruleString);
//    oovRule.setOwner(Vocabulary.id("oov"));
//    addRule(oovRule);
//    oovRule.estimateRuleCost(featureFunctions);
  }

  @Override
  public Trie getTrieRoot() {
    return backend.getTrieRoot();
  }

  @Override
  public void sortGrammar(List<FeatureFunction> models) {
    backend.sortGrammar(models);    
  }

  @Override
  public boolean isSorted() {
    return backend.isSorted();
  }

  /**
   * This should never be called. 
   */
  @Override
  public boolean hasRuleForSpan(int startIndex, int endIndex, int pathLength) {
>>>>>>> refs/remotes/apache/master
    return true;
  }

  @Override
  public int getNumRules() {
<<<<<<< HEAD
    return numRules;
=======
    return backend.getNumRules();
>>>>>>> refs/remotes/apache/master
  }

  @Override
  public Rule constructManualRule(int lhs, int[] sourceWords, int[] targetWords, float[] scores,
<<<<<<< HEAD
      int aritity) {
    return null;
=======
      int arity) {
    return backend.constructManualRule(lhs,  sourceWords, targetWords, scores, arity);
>>>>>>> refs/remotes/apache/master
  }

  @Override
  public void writeGrammarOnDisk(String file) {
<<<<<<< HEAD
=======
    backend.writeGrammarOnDisk(file);
>>>>>>> refs/remotes/apache/master
  }

  @Override
  public boolean isRegexpGrammar() {
<<<<<<< HEAD
    return false;
  }
  
  /**
   * A simple wrapper around an int[] used for hashing
   */
  private class PhraseWrapper {
    public int[] words;

    /**
     * Initial from the source side of the rule. Delete the nonterminal that will be there, since
     * later indexing will not have it.
     * 
     * @param source the source phrase, e.g., [-1, 17, 91283]
     */
    public PhraseWrapper(int[] source) {
      this.words = Arrays.copyOfRange(source, 0, source.length);
    }
    
    @Override
    public int hashCode() {
      return Arrays.hashCode(words);
    }
    
    @Override
    public boolean equals(Object other) {
      if (other instanceof PhraseWrapper) {
        PhraseWrapper that = (PhraseWrapper) other;
        if (words.length == that.words.length) {
          for (int i = 0; i < words.length; i++)
            if (words[i] != that.words[i])
              return false;
          return true;
        }
      }
      return false;
    }
=======
    return backend.isRegexpGrammar();
  }

  @Override
  public int getOwner() {
    return backend.getOwner();
  }

  @Override
  public int getNumDenseFeatures() {
    return backend.getNumDenseFeatures();
>>>>>>> refs/remotes/apache/master
  }
}
