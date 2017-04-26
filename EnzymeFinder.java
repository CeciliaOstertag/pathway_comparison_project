package com.github.pathway_comparison_project;

import org.sbml.libsbml.FbcModelPlugin;
import org.sbml.libsbml.GeneProduct;
import org.sbml.libsbml.ListOfGeneProducts;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;

import java.util.ArrayList;

/**
 * This class is used to find a list of all enzymes (fbc:GeneProducts) in a SBML file
 *
 * @see PathwayComparisonProject
 * @see Query
 * @see RsdResultsParser
 * @see SbmlAnnotator
 * 
 * @author Peter Bock, Guillamaury Debras, Mercia Ngoma-Komb, Cécilia Ostertag, Franck Soubes
 */

public class EnzymeFinder {
	
	protected String sbmlFile;
	protected ArrayList<String> enzymeList = new ArrayList<>();

	public EnzymeFinder(String sbmlFile) {
		this.sbmlFile = sbmlFile;
	}

	public ArrayList<String> getEnzymeList() {
		return enzymeList;
	}
	
	/**
	 * Parses the SBML file and gets all occurrences of NCBI Protein GI
	 * 
	 **/

	public void find() {

		SBMLReader reader = new SBMLReader();
		SBMLDocument doc = reader.readSBML(sbmlFile);
		Model model = doc.getModel();
		FbcModelPlugin fbc = (FbcModelPlugin) model.getPlugin("fbc");
		ListOfGeneProducts gpList = fbc.getListOfGeneProducts();

		for (int i = 0; i < gpList.getNumGeneProducts(); i++) {
			GeneProduct gp = gpList.get(i);
			String annotationString = gp.getAnnotationString();
			int indexNcbiGI = annotationString.indexOf("ncbigi/") + 10;
			int indexEnd = annotationString.indexOf("\"", indexNcbiGI);
			if (indexNcbiGI != -1 && indexEnd != -1) {
				String ncbigi = annotationString.substring(indexNcbiGI, indexEnd);
				enzymeList.add(ncbigi);
			} else {
				System.out.println("Le GI n'a pas ete trouve...");
			}

		}
		System.out.println(enzymeList.size() + " enzymes ont ete trouvees");
	}
	
}
