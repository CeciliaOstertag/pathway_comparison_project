package com.github.pathway_comparison_project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class RsdResultsParser {

	private String orthologFile;
	private ArrayList<String> orthologListRef = new ArrayList<>();
	private ArrayList<String> orthologListQuery = new ArrayList<>();

	public RsdResultsParser(String orthologFile) {
		this.orthologFile = orthologFile;
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<String> getOrthtologListRef() {
		return orthologListRef;
	}

	public ArrayList<String> getOrthtologListQuery() {
		return orthologListQuery;
	}

}
