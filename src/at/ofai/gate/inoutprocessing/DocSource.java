package at.ofai.gate.inoutprocessing;

import gate.Document;
import gate.LanguageResource;
import gate.Resource;
import gate.creole.AbstractLanguageResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.util.ClosableIterator;
import gate.util.MethodNotImplementedException;

/**
 * A document source is a language resource that models some way to get
 * one GATE document after another. It acts like an iterator and neither the
 * number of documents that will be delivered nor their order is known in
 * advance in the general case. Specific implementations may make the number
 * of total documents available and may make guarantees about document order.
 * 
 * @author Johann Petrak
 */
public abstract class DocSource
  extends AbstractLanguageResource
  implements ClosableIterator<Document>
{
  /**
   * Return the next document if there is one, i.e. after hasNext() returned
   * true. This can return null after hasNext() returned true if there was
   * an error converting the original source to a GATE document and the
   * parameter failOnConversionError is set to false.
   * <p>
   * A document source must follow the following usage protocol:
   * <ul>
   * <li>set the LR parameters using the setter methods</li>
   * <li>call init: this will return the resource in a state that can be
   * openend and used</li>
   * <li>call open: this will initialize the iterator for processing</li>
   * <li>iteratively check for more documents with hasNext(), if there are more
   * documents available, get the next one with the next() method.</li>
   * <li>free resources by calling the close() method</li>
   * </ul>
   *
   * @return
   */
  public abstract Document next();
  public abstract boolean hasNext();
  public abstract Resource init()  throws ResourceInstantiationException;
  //public abstract String originalDocumentID();
  public abstract void open();
  public abstract void close();
  /**
   * This can be called after open to get the total number of documents
   * if it is known. If the total number of documents is not known or cannot
   * be known for this source, a number less than zero is returned.
   * 
   * @return the total number of documents available or a number less than
   * zero if that number is not known.
   */
  public int getTotalNumberOfDocuments() { return -1; }
  public void remove() {
    throw new MethodNotImplementedException("Remove not implemented for DocSource");
  }
  @CreoleParameter(
    comment="Directory that should contain the random index data",
    defaultValue = "true")
  public void setFailOnConversionError(Boolean yesno) {
    failOnConversionError = yesno;
  }
  public Boolean getFailOnConversionError() {
    return failOnConversionError;
  }
  Boolean failOnConversionError = true;
}