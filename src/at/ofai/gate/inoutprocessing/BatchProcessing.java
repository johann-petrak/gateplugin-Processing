package at.ofai.gate.inoutprocessing;

//import java.util.Enumeration;
//import java.util.Vector;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ExecutionException;
import gate.util.GateException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import uk.co.flamingpenguin.jewel.cli.*;
import org.apache.log4j.PropertyConfigurator;

// TODO: 
// Make the typical way of using the class as an object, not 
// static.
// properly set up logging for default
// Use logger for debug and info messages (any messages)
// By default output info and higher to stdout
// allow config of the logger by cmdline
// Change the Getter to work more like an iterator.
// Add DB Getter
// Add Putter: store file in dir, store in DB etc.

public class BatchProcessing {

  static String optionSaveAs;
  static String optionSaveTo;
  static List<String> otherArgs;
  static String optionListFrom;
  static String optionInDir;
  static String optionOutDir;
  static String optionController;
  static boolean optionDebug;
  static BatchProcessingOptions options = null;

    public static void main( String[] args ) throws MalformedURLException, GateException {

      try {
         options =
                 CliFactory.parseArguments(BatchProcessingOptions.class, args);

      } catch(ArgumentValidationException e) {
        System.err.println("Error parsing options: "+e);
        System.exit(1);
      }

      optionSaveAs = options.getSaveAs();
      optionSaveTo = options.getSaveTo();
      otherArgs = options.getArgs();
      if(otherArgs == null) {
        otherArgs = new LinkedList<String>();
      }
      optionListFrom = options.getListFrom();
      optionInDir = options.getInDir();
      optionOutDir = options.getOutDir();
      optionController = options.getController();
      optionDebug = options.isDebug();

      if(options.isLog4jConfig()) {
        System.err.println("Setting log4j config to "+options.getLog4jConfig());
        PropertyConfigurator.configure(options.getLog4jConfig());
      }

      // Initialize gate - let it guess its home or pass the homedir
      // using -Dgate.home="homedir"
      // We must do it here already because processing some parameters needs
      // to initialize Gate resources
      try {
        gate.Gate.init();
      } catch (gate.util.GateException e) {
        System.err.println("Could not initialize gate: "+e);
        System.exit(2);
      }

      if(optionListFrom.equals("stdin")) {
        mDocumentGetter = new DocumentGetterFileNamesFromStdin(options);
        ((DocumentGetterFileNamesFromStdin)mDocumentGetter).setIndir(optionInDir);
        runUsingGetters(mDocumentGetter);
      } else if(optionListFrom.equals("file")) {
        if(otherArgs.size() != 1) {
          System.err.println("Need exactly one filename to process filenames from file! (have "+otherArgs.size()+")\n");
          System.exit(2);
        }        
        mDocumentGetter = new DocumentGetterFileNamesFromFile(options,otherArgs.get(0));
        ((DocumentGetterFileNamesFromFile)mDocumentGetter).setIndir(optionInDir);
        runUsingGetters(mDocumentGetter);
      } else if(optionListFrom.equals("dircorpus")) {
        //TODO: get the dircorpus plugin, create a dircorpus from the
        // in/out directory specification and run the pipeline on that
        // corpus

        // if we have the pluginsDir option is set, look for the DirectoryCorpus
        // plugin there, if not found try the plugins dir in gate home
        File pluginsDir;
        if(options.isPluginsDir() && !options.getPluginsDir().equals("")) {
          pluginsDir = new File(options.getPluginsDir());
          Gate.getCreoleRegister().
            registerDirectories(new File(pluginsDir,"DirectoryCorpus").
              toURI().toURL());
        } else {
          Gate.getCreoleRegister().registerDirectories(
            new File(Gate.getPluginsHome(), "DirectoryCorpus").toURI().toURL());
        }

        FeatureMap fm = Factory.newFeatureMap();
        URL dirurl = new File(options.getInDir()).toURI().toURL();
        fm.put("directoryURL", dirurl); // set input directory URL
        if(options.isOutDir()) {
          URL outdirurl = new File(options.getOutDir()).toURI().toURL();
          fm.put("outDirectoryURL", outdirurl); // set output directory URL, if outdir specified
        }
        Corpus corpus = (Corpus) Factory.createResource("at.ofai.gate.directorycorpus.DirectoryCorpus",fm);
        runUsingCorpus(corpus);
      } else {
        if(otherArgs.size() != 0) {
          System.err.println("No additional parameter needed for files from dir, will take --indir dir\n");
          System.exit(2);
        }
        mDocumentGetter = new DocumentGetterFileNamesFromDir(options,optionInDir);
        runUsingGetters(mDocumentGetter);
      }
    }
    
    
    static BlockingQueue<PipelineRunner> runners = null;
    public static void runUsingGetters (DocumentGetter getter) {
      int optionNrThreads = Integer.parseInt(options.getNrThreads());
      if(mDocumentGetter == null) {
        System.err.println("Could not initialize the file getter for filefrom "+optionListFrom+"\n");
        System.exit(2);
      }
      runners = new ArrayBlockingQueue<PipelineRunner>(optionNrThreads);
      PipelineRunner runner = null;
      for(int i = 0; i<optionNrThreads; i++) {
        try {
          runner = new PipelineRunner(options, i);
          runners.add(runner);
        } catch (Exception ex) {
          System.err.println("Could not create a pipeline runner: "+ex);
          System.exit(1);
        }
      }
      final ExecutorService pool;
      pool = Executors.newFixedThreadPool(optionNrThreads);

      Document d = null;
      try {
      while(true) {
        d = mDocumentGetter.getNextDocument();
        if(d == null) {
          if (!mDocumentGetter.haveNext()) {
            // no more documents, shutdown the pool
            shutdownAndAwaitTermination(pool);
            break;
          }
          System.err.println("Did not get a document!\n");
        } else {
          // submit the document to a runner and execute it
          runner = getRunner();
          runner.setDocument(d, mDocumentGetter.getFileName());
          pool.execute(runner);
        }
      }
      } catch (Exception ex) {
        System.err.println("Exception during execution "+ex);
        ex.printStackTrace(System.err);
      } finally {
        shutdownAndAwaitTermination(pool);
      }
      
  }

  private static void shutdownAndAwaitTermination(ExecutorService pool) {
    System.err.println("Requesting shutdown");
    pool.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait half an hour for all tasks to complete
      if (!pool.awaitTermination(60 * 30, TimeUnit.SECONDS)) {
        System.err.println("Still not completed, forcing shutdown");
        pool.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!pool.awaitTermination(300, TimeUnit.SECONDS)) {
          System.err.println("Pool did not terminate");
        }
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      System.err.println("Got an interrupt, forcing shutdown");
      pool.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }


    protected static PipelineRunner getRunner()
            throws InterruptedException {
        PipelineRunner runner = runners.take();
        System.err.println("Getting running nr "+runner.getNr()+ " size is now "+runners.size());
        return runner;
    }

    protected static  void putRunner(PipelineRunner runner) throws InterruptedException {
        System.err.println("Putting back running nr "+runner.getNr());
        runners.put(runner);
    }

    public static void runUsingGettersOld (DocumentGetter getter) {
      if(mDocumentGetter == null) {
        System.err.println("Could not initialize the file getter for filefrom "+optionListFrom+"\n");
        System.exit(2);
      }

      // TODO: make this multithreaded .
      // Maybe by doing this: if n threads are specified,

      // load the processing pipeline description from the file
      // specified
      File controllerFile = new File(optionController);
      gate.CorpusController controller = null;
      try {
        controller =
          (gate.CorpusController)gate.util.persistence.PersistenceManager.
          loadObjectFromFile(controllerFile);
      } catch (Exception e) {
        System.err.println("Could not load controller: "+e);
        System.exit(2);
      }
      // process all the documents we get from the source
      // each document will be added to the corpus, run through the
      // pipeline optionally saved and removed from the corpus.
      gate.Document d;
      gate.Corpus corpus = null;
      try {
        corpus = gate.Factory.newCorpus( "tmp001" );
      } catch (Exception e) {
        System.err.println("Could not create a corpus: "+e);
        System.exit(2);
      }
      controller.setCorpus(corpus);
      while(true) {
        d = mDocumentGetter.getNextDocument();
        if(d == null) {
          if (!mDocumentGetter.haveNext()) {
            break;
          }
          System.err.println("Did not get a document!\n");
        } else {
          if(optionDebug) {
            System.err.println("Processing document "+d.getName()+"\n");
          }
          corpus.add(d);
          try {
            controller.execute();
            System.err.println("Document processed!\n");
          } catch (gate.creole.ExecutionException e) {
            System.err.println("Error executing the controller: "+e);
          }

          if(!optionSaveTo.equals("none")) {
            // we save the document
            String toSave = null;
            if(optionSaveAs.equals("string")) {
              System.err.println("Converting output document to string");
              toSave = d.toString();
            } else if(optionSaveAs.equals("xml")) {
              System.err.println("Converting output document to xml");
              toSave = d.toXml();
            } else if(optionSaveAs.equals("xmlorig")) {
              System.err.println("Converting output document to xmlorig");
              toSave = d.toXml(new gate.annotation.AnnotationSetImpl(d));
            }
            if(optionSaveTo.equals("stdout")) {
              try {
                Utils.saveStringToFile(toSave, System.out, options);
              } catch (Exception ex) {
                System.err.println("Exception when writing to stdout: "+ex);
                ex.printStackTrace();
              }
            } else if(optionSaveTo.equals("file")) {
              System.err.println("Save to file");
              // create the full path by combining the outdir(or curdir)
              // with the name of document
              String fileName;
              fileName = mDocumentGetter.getFileName();
              if(fileName == null) {
                fileName = d.getName() + ".xml";
              }
              if(options.isReplExt()) {
                System.err.println("Replacing extension with "+options.getReplExt());
                fileName=fileName.replaceAll("\\.[^.]+$", options.getReplExt());
              }
              try {
                if(options.isCompressOut()) {
                  fileName = fileName + ".gz";
                }
                File outFile = new File(options.getOutDir(),fileName);
                System.err.println("Writing document to file "+outFile.toString());
                FileOutputStream outstream = new FileOutputStream(outFile);
                Utils.saveStringToFile(toSave,outstream,options);
                outstream.close();
                //FileOutputStream fos = new FileOutputStream(outFile);
                //BufferedWriter   out = new BufferedWriter(fos);
              } catch (Exception ex) {
                System.err.println("Exception when writing to file: "+ex);
                ex.printStackTrace();
              }
            }
          }
          corpus.clear();
          if(d != null) {
            gate.Factory.deleteResource(d);
          }
        }
      }

    }

    public static void runUsingCorpus (Corpus corpus) throws ExecutionException {
      // load the processing pipeline description from the file
      // specified
      File controllerFile = new File(optionController);
      gate.CorpusController controller = null;
      try {
        controller =
          (gate.CorpusController)gate.util.persistence.PersistenceManager.
          loadObjectFromFile(controllerFile);
      } catch (Exception e) {
        System.err.println("Could not load controller: "+e);
        System.exit(2);
      }
      controller.setCorpus(corpus);
      controller.execute();
    }


    public static DocumentGetter mDocumentGetter = null; 
}
