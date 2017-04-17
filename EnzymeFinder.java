/**
 * @file    echoSBML.java
 * @brief   Extract the label of every GeneProduct (enzyme) in the SBML file.
 * @author  Peter Bock
 *
 *
 */

import org.sbml.libsbml.*;
import java.util.ArrayList;


public class EnzymeFinder
{
	protected String sbmlFile;
	protected ArrayList enzymeList = new ArrayList();

	
	public EnzymeFinder(String sbmlFile)
	{
		this.sbmlFile=sbmlFile;
	}
	
	public ArrayList getEnzymeList()
	{
		return enzymeList;
	}
  public void find()
  {

    SBMLReader reader     = new SBMLReader();

    SBMLDocument doc = reader.readSBML(sbmlFile);

    Model model = doc.getModel();
    FbcModelPlugin fbc = (FbcModelPlugin) model.getPlugin("fbc");
    // Apparently all I needed was to cast the SBasePlugin to
    //  FbcModelPlugin...
    // The developers really did help a lot with their message.
    ListOfGeneProducts gpList = fbc.getListOfGeneProducts();
    // Now we (finally) have the list of GeneProducts



    for (int i = 0;i < gpList.getNumGeneProducts(); i++) {
        GeneProduct gp = gpList.get(i);
        // Get a geneproduct from the list
        //System.out.println(gp.getLabel()); // code de verification pour voir que les labels sont bien printee.

        //enzymeList.add(gp.getLabel()); // Adds Label element of gp to enzymeList.
        // label element coresponds to BiGG ID.

        // Is there a way to get the identifier.org ids directly ?
        // is there a more direct way of
        // transforming ListOfGenePrducts to an ArrayList ?
        // Haven't found one yet...
        
        String annotationString = gp.getAnnotationString();

        int indexNcbiGI = annotationString.indexOf("ncbigi/") + 10;
        int indexEnd = annotationString.indexOf("\"",indexNcbiGI);
        if(indexNcbiGI != -1 && indexEnd != -1){
          String ncbigi = annotationString.substring(indexNcbiGI,indexEnd); // trouve l'id GI
          enzymeList.add(ncbigi);
          System.out.println(ncbigi);
        }
        else
        {
          System.out.println("No Annotation data...");
        }


    }
    System.out.println(enzymeList.size()+" enzymes ont ete trouvees");
  }
  // Code de verification. L'on devrait
  // techniquement aussi verifier la presence du plugin fbc,
  // mais je suis pas encore sur comment.
  static
  {
    try
    {
      System.loadLibrary("sbmlj");
      // For extra safety, check that the jar file is in the classpath.
      Class.forName("org.sbml.libsbml.libsbml");
    }
    catch (UnsatisfiedLinkError e)
    {
      System.err.println("Error encountered while attempting to load libSBML:");
      System.err.println("Please check the value of your "
                         + (System.getProperty("os.name").startsWith("Mac OS")
                            ? "DYLD_LIBRARY_PATH" : "LD_LIBRARY_PATH") +
                         " environment variable and/or your" +
                         " 'java.library.path' system property (depending on" +
                         " which one you are using) to make sure it list the" +
                         " directories needed to find the " +
                         System.mapLibraryName("sbmlj") + " library file and" +
                         " libraries it depends upon (e.g., the XML parser).");
      System.exit(1);
    }
    catch (ClassNotFoundException e)
    {
      System.err.println("Error: unable to load the file 'libsbmlj.jar'." +
                         " It is likely that your -classpath command line " +
                         " setting or your CLASSPATH environment variable " +
                         " do not include the file 'libsbmlj.jar'.");
      e.printStackTrace();

      System.exit(1);
    }
    catch (SecurityException e)
    {
      System.err.println("Error encountered while attempting to load libSBML:");
      e.printStackTrace();
      System.err.println("Could not load the libSBML library files due to a"+
                         " security exception.\n");
      System.exit(1);
    }
  }
}

