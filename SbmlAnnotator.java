package com.github.pathway_comparison_project;

import java.util.ArrayList;
import java.util.Hashtable;

import org.sbml.libsbml.FbcModelPlugin;
import org.sbml.libsbml.GeneProduct;
import org.sbml.libsbml.ListOfGeneProducts;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;

/**
 * This class is used to add enzyme orthology information to a given sbml file.
 * The information is added in the attribute "name" of each fbc:GeneProduct
 * element of the model. The annotation syntax is as follows : - for orthologs :
 * <NCBI Id of enzyme from reference genome>/<NCBI Id of enzyme from subject
 * genome> - for non-orthologs : <NCBI Id of enzyme from current genome>
 * 
 * @see Query
 * @see EnzymeFinder
 * @see RsdResultsParser
 * @see PathwayComparisonProject
 * 
 * @author Peter Bock, Guillamaury Debras, Mercia Ngoma-Komb, Cécilia Ostertag,
 *         Franck Soubes
 */
public class SbmlAnnotator {

	protected String sbmlFile;
	protected ArrayList<String> orthologListRef = new ArrayList<>();
	protected ArrayList<String> orthologListQuery = new ArrayList<>();
	protected Hashtable<String, String> corresp = new Hashtable<>();

	public SbmlAnnotator(String sbmlFile, ArrayList<String> orthologListRef, ArrayList<String> orthologListQuery,
			Hashtable<String, String> corresp) {
		this.sbmlFile = sbmlFile;
		this.orthologListRef = orthologListRef;
		this.orthologListQuery = orthologListQuery;
		this.corresp = corresp;
	}

	/**
	 * Parses the sbml file and add annotation to each fbc:GeneProduct, then
	 * saves these informations in a copy of the original sbml file
	 * 
	 **/

	public void annotateName(String outputName) {

		SBMLReader reader = new SBMLReader();
		SBMLWriter writer = new SBMLWriter();
		SBMLDocument doc = reader.readSBML(sbmlFile);
		Model model = doc.getModel();
		FbcModelPlugin fbc = (FbcModelPlugin) model.getPlugin("fbc");
		ListOfGeneProducts gpList = fbc.getListOfGeneProducts();

		int nb_ortho = 0;
		int nb_other = 0;
		for (int i = 0; i < gpList.getNumGeneProducts(); i++) {
			GeneProduct gp = gpList.get(i);
			String annotationString = gp.getAnnotationString();
			int indexNcbiGI = annotationString.indexOf("ncbigi/") + 10;
			int indexEnd = annotationString.indexOf("\"", indexNcbiGI);
			if (indexNcbiGI != -1 && indexEnd != -1) {
				String ncbigi = annotationString.substring(indexNcbiGI, indexEnd);
				String ncbiId = corresp.get(ncbigi);
				if (orthologListRef.contains(ncbiId)) {
					nb_ortho++;
					int index = orthologListRef.indexOf(ncbiId);
					gp.setName(orthologListRef.get(index) + "/" + orthologListQuery.get(index));
				} else if (orthologListQuery.contains(ncbiId)) {
					nb_ortho++;
					int index = orthologListQuery.indexOf(ncbiId);
					gp.setName(orthologListRef.get(index) + "/" + orthologListQuery.get(index));
				} else {
					nb_other++;
					gp.setName(ncbiId);
				}
			} else {
				System.out.println("Le GI n'a pas ete trouve...");
			}
		}
		model.setName(outputName);
		System.out.println("Nombre d'enzymes orthologues : " + nb_ortho + "\nReste : " + nb_other);
		writer.writeSBML(doc, outputName);
	}

}
