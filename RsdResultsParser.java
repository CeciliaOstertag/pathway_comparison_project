package com.github.pathway_comparison_project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is used to parse the output file containing the results of the RSD algorithm used to find orthologs between two sets of sequences
 * 
 * @see Query
 * @see EnzymeFinder
 * @see PathwayComparisonProject
 * @see SbmlAnnotator
 * 
 * @author Peter Bock, Guillamaury Debras, Mercia Ngoma-Komb, Cécilia Ostertag, Franck Soubes
 */

public class RsdResultsParser {

	private String orthologFile;
	private ArrayList<String> orthologListRef = new ArrayList<>();
	private ArrayList<String> orthologListQuery = new ArrayList<>();

	public RsdResultsParser(String orthologFile) {
		this.orthologFile = orthologFile;
	}

	/**
	 * Parses the pairs of orthologs written in the orthology results file, and
	 * saves them in two ArrayList : one for the reference genome, and the other
	 * for the subject genome
	 * 
	 **/
	public void findOrthologList() {
		try {
			BufferedReader buff = new BufferedReader(new FileReader(orthologFile));
			String line = null;
			while ((line = buff.readLine()) != null) {
				String[] fields = line.split("\t");
				if (fields.length > 3) {
					orthologListRef.add(fields[1]);
					orthologListQuery.add(fields[2]);
				}
			}
			buff.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the list of ortholog proteins from the reference genome
	 * 
	 * @return orthologListRef : list of ortholog proteins from the reference genome
	 * 
	 **/

	public ArrayList<String> getOrthtologListRef() {
		return orthologListRef;
	}

	/**
	 * Returns the list of ortholog proteins from the subject genome
	 * 
	 * @return orthologListQuery : list of ortholog proteins from the subjecte genome
	 * 
	 **/
	public ArrayList<String> getOrthtologListQuery() {
		return orthologListQuery;
	}

}
