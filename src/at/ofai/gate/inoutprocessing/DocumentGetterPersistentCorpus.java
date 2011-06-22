package at.ofai.gate.inoutprocessing;


public class DocumentGetterPersistentCorpus extends DocumentGetter {
  public DocumentGetterPersistentCorpus(String dataStore, String corpusName) {
    setDataStore(dataStore);
    setCorpusName(corpusName);
    init();
  }
  public void setDataStore(String dataStoreName) {
    System.err.println("Set data store to: "+dataStoreName+"\n");
    mDataStoreName = dataStoreName;
    
    try {
      mDataStoreURL = new java.net.URL(mDataStoreName);
    } catch (java.net.MalformedURLException e) {
      System.err.println("Wrong URL format for '"+dataStoreName+"' :"+e);
      // TODO: rethrow or own exception for runtime errors here!
    }
      
  }
  public void setCorpusName(String corpusName) {
    System.err.println("Set corpus name to: "+corpusName+"\n");
    mCorpusName = corpusName;
  }
  gate.Corpus mCorpus = null;
  String mDataStoreName = null;
  String mCorpusName = null;
  java.net.URL mDataStoreURL = null;
  gate.DataStore mDataStore = null;
  int mCurIdx = 0;
  
  public void init () {
    // open the corpus
    // If the datastore is already open, we should NOT reopen it!!
    // (ther should be some register to find out)
    // Also, if the same corpus is already open, just use it! 
    if(mDataStoreURL == null) {
      System.err.println("No datastore URL: datastore name missing or no init()\n");
    }
    try {
      System.err.println("Opening datastore ... \n");
      mDataStore = 
        gate.Factory.openDataStore("gate.persist.SerialDataStore", mDataStoreName);
      java.util.List ids = mDataStore.getLrIds("gate.corpora.SerialCorpusImpl");
      System.err.println("Finding corpus ids, size="+ids.size()+" content="+ids+" ... \n");
      for(int i = 0; i < ids.size(); i++) {
        System.err.println("Got ID: "+ids.get(i)+"\n");
        gate.corpora.SerialCorpusImpl c = null;
        if(mCorpusName.equals(mDataStore.getLrName(ids.get(i)))) {
          try {
            /*  HOW NOT TO DO IT!!!
            c = 
              (gate.corpora.SerialCorpusImpl)mDataStore.getLr("gate.corpora.SerialCorpusImpl", ids.get(i));
              */
            gate.FeatureMap params = gate.Factory.newFeatureMap();
            params.put(gate.DataStore.DATASTORE_FEATURE_NAME, mDataStore);
            params.put(gate.DataStore.LR_ID_FEATURE_NAME, ids.get(i));
            gate.FeatureMap features = gate.Factory.newFeatureMap();
            c = (gate.corpora.SerialCorpusImpl) gate.Factory.createResource("gate.corpora.SerialCorpusImpl", params, features,
                                                     "tmp");
          } catch (gate.creole.ResourceInstantiationException e) {
            System.err.println("Could not get corpus: "+e);
          }
          mCorpus = c;
        }
      }
      if(mCorpus == null) {
        System.err.println("Corpus not found in datastore!!\n");
        System.exit(2);
      }
    } catch (gate.persist.PersistenceException e) {
      System.err.println("Could not open Datastore ("+mDataStoreName+"): "+e);
    }
  }
  
  public gate.Document getNextDocument() {
    gate.Document d = null;
    if(mCorpus == null) {
      mHaveNext = false;
      return null;
    }
    if(mCurIdx < mCorpus.size()) {
      d = (gate.Document)mCorpus.get(mCurIdx);
      mCurIdx = mCurIdx +1;
    } else {
      mHaveNext = false;
      return null;
    }
    return d;
  }

  public void close() {
    
  }

}