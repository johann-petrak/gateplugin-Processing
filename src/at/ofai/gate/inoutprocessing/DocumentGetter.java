package at.ofai.gate.inoutprocessing;


// all Document getters provide a way to initialize the concrete
// method of getting one document after the other and implement
// tge method getNext() to retrieve the next document already in
// GATE format. If geNext() returns null, the client should
// check haveNext() to see if this is an indication that no
// more documents are available from that source. If haveNext is
// not null, the document was in error.
// Individual subclasses do have their own setXXX methods to 
// set the parameters to describe the source of the documents.
// Alternately, they can also implement a special constructor
// that takes those parameters.
// If the parameters are set using setXXX the init() method has
// to be called before the getter can be used, if the constructor
// is used, the constructor will call init() automatically.

// TODO: we should change this to implement ClosableIterator<Document>
// which is essentially an Iterator<Document> that needs to be closed.
public abstract class DocumentGetter {
  public abstract gate.Document getNextDocument();
  protected boolean mHaveNext = true;
  protected BatchProcessingOptions mOptions = null;
  public boolean haveNext() {
    return mHaveNext;
  }
  public abstract void init();
  public abstract void close();
  public String getFileName() {
    return null;
  }
  public void setDebug(Boolean onOff) {
    mDebug = onOff;
  }
  private Boolean mDebug = Boolean.FALSE;
}