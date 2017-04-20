package com.github.pathway_comparison_project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.*;

import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.io.FastaWriterHelper;

/**
 * Main program
 * 
 * @see Query
 * @see EnzymeFinder
 * 
 */

public class PathwayComparisonProject {

	public static void main(String[] args) throws IOException {
		Hashtable<String, String> corresp1 = new Hashtable<>(); // correspondances
																// ids bigg (GI)
																// /
																// ids ncbi
																// bacterie1
		Hashtable<String, String> corresp2 = new Hashtable<>(); // correspondances
																// ids bigg (GI)
																// /
																// ids ncbi
																// bacterie2
		ArrayList<String> fileNames1 = new ArrayList<String>();
		ArrayList<String> fileNames2 = new ArrayList<String>();
		String sbml1 = null;
		String sbml2 = null;
		String multifasta1 = null;
		String multifasta2 = null;
		String orthologFile = null;

		System.out.println("Possedez vous deja les fichiers multifasta ? (O/N) ");
		String ans = string_input();
		if (ans.equals("O")) {
			System.out.println("Chemin du fichier multifasta de reference : ");
			multifasta1 = string_input();
			System.out.println("Chemin du deuxieme fichier multifasta : ");
			multifasta2 = string_input();
		} else if (ans.equals("N")) {

			System.out.println("Chemin du fichier SBML de référence : ");
			sbml1 = string_input();
			System.out.println("Nom de l'organisme de référence : ");
			String organism1 = string_input();
			System.out.println("Chemin du second fichier SBML : ");
			sbml2 = string_input();
			System.out.println("Nom du second organisme : ");
			String organism2 = string_input();

			ArrayList<String> biggIdsList1 = findEnzymes(sbml1);

			File tmp = new File("./src/main/resources/Tmp");
			tmp.mkdir();

			long startTime1 = System.currentTimeMillis();
			fileNames1 = enzymesQuery(organism1, biggIdsList1, corresp1, "src/main/resources/Tmp/results1");
			long estimatedTime1 = System.currentTimeMillis() - startTime1;
			System.out.println("Temps (ms) : " + estimatedTime1);

			String outputName1 = "src/main/resources/FastaFiles/" + organism1.replaceAll(" ", "_") + ".fasta";
			multifasta1 = makeMultifasta(fileNames1, outputName1);

			ArrayList<String> biggIdsList2 = findEnzymes(sbml2);

			long startTime2 = System.currentTimeMillis();
			fileNames2 = enzymesQuery(organism2, biggIdsList2, corresp2, "src/main/resources/Tmp/results2");
			long estimatedTime2 = System.currentTimeMillis() - startTime2;
			System.out.println("Temps (ms) : " + estimatedTime2);

			String outputName2 = "src/main/resources/FastaFiles/" + organism2.replaceAll(" ", "_") + ".fasta";
			multifasta2 = makeMultifasta(fileNames2, outputName2);

			removeDirectory(tmp);

		} else {
			System.out.println("Erreur");
			System.exit(1);
		}

		System.out.println(
				"Voulez-vous utiliser un seuil de divergence et une evalue differentes des valeurs par defaut (divergence = 0.8 et evalue = 1e-5) ? (O/N)");
		String ans2 = string_input();
		if (ans2.equals("O")) {
			System.out.println("Valeurs de divergence et evalue : ");
			String de = string_input();
			long startTime3 = System.currentTimeMillis();
			orthologFile = findOrthologs(multifasta1, multifasta2, de);
			long estimatedTime3 = System.currentTimeMillis() - startTime3;
			System.out.println("Temps (ms) : " + estimatedTime3);
		} else if (ans2.equals("N")) {
			long startTime3 = System.currentTimeMillis();
			orthologFile = findOrthologs(multifasta1, multifasta2);
			long estimatedTime3 = System.currentTimeMillis() - startTime3;
			System.out.println("Temps (ms) : " + estimatedTime3);
		} else {
			System.out.println("Erreur");
			System.exit(1);
		}

		addOrthologyInfo(sbml1, orthologFile, corresp1, "ref_sbml.xml");
		addOrthologyInfo(sbml2, orthologFile, corresp2, "query_sbml.xml");
	}

	/**
	 * Removes a non empty directory
	 * 
	 * @param dir
	 *            : directory name
	 **/
	public static void removeDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null && files.length > 0) {
				for (File aFile : files) {
					removeDirectory(aFile);
				}
			}
			dir.delete();
		} else {
			dir.delete();
		}
	}

	/**
	 * String user input
	 * 
	 * @return string (String)
	 **/
	public static String string_input() {

		try {
			BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
			String string = buff.readLine();
			return string;
		} catch (IOException e) {
			System.out.println(e);
			return null;
		}
	}

	public static ArrayList<String> findEnzymes(String sbmlFile) {
		EnzymeFinder finder = new EnzymeFinder(sbmlFile);
		finder.find();
		ArrayList<String> enzymeList = finder.getEnzymeList();
		return enzymeList;
	}

	/**
	 * Reads a fasta file containing proteic sequences
	 * 
	 * @param filename
	 *            : name of the fasta file to read from
	 * @param seqList
	 *            : list of sequences found in the file
	 **/
	public static void readProtFasta(String filename, ArrayList<ProteinSequence> seqList) {

		LinkedHashMap<String, ProteinSequence> helper;
		try {
			File file = new File(filename);
			helper = FastaReaderHelper.readFastaProteinSequence(file);
			for (Entry<String, ProteinSequence> entry : helper.entrySet()) {
				seqList.add(entry.getValue());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes a fasta file containing proteic sequences
	 * 
	 * @param filename
	 *            : name of the output fasta file
	 * @param seqs
	 *            : list of sequences that will be written
	 **/
	public static void writeProtFasta(String filename, ArrayList<ProteinSequence> seqs) {

		try {
			File file = new File(filename);
			FastaWriterHelper.writeProteinSequence(file, seqs);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Makes a multifasta file from a list of fasta files
	 * 
	 * @param fileNames
	 *            : list of fasta files names
	 * @param outputName
	 *            : name of the resulting multifasta file
	 * @return outputName : name of the resulting multifasta file
	 **/
	public static String makeMultifasta(ArrayList<String> fileNames, String outputName) {
		ArrayList<ProteinSequence> seqList = new ArrayList<>();
		for (int i = 0; i < fileNames.size(); i++) {
			String name = fileNames.get(i);
			try {
				readProtFasta(name, seqList);
			} catch (Exception e) {
				System.out.println(e);
				System.out.println(name);
			}
		}
		writeProtFasta(outputName, seqList);

		System.out.println("Le fichier multifasta a été créé : " + outputName);
		return outputName;

	}

	/**
	 * Uses RSD algorithm to find ortholog sequences between two sets of proteic
	 * sequences in fasta format
	 * 
	 * @param multifasta1
	 *            : name of first multifasta file
	 * @param multifasta2
	 *            : name of first multifasta file
	 * @return orthologFile : name of the resulting text file containing the
	 *         list of orthologs
	 **/
	public static String findOrthologs(String multifasta1, String multifasta2) {

		String orthologFile = "./orthologs.txt";
		Runtime runtime = Runtime.getRuntime();
		String commande = "rsd_search -q " + multifasta1 + " --subject-genome=" + multifasta2 + " -o " + orthologFile;
		System.out.println("Recherche d'orthologues en cours...");
		try {
			Process p = runtime.exec(commande);
			p.waitFor();
			System.out.println("Recherche d'orthologues terminéee, voir fichier " + orthologFile);
		} catch (IOException e) {
			System.out.println(e);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
		return orthologFile;

	}

	/**
	 * Uses RSD algorithm to find ortholog sequences between two sets of proteic
	 * sequences in fasta format, with a non default evalue
	 * 
	 * @param multifasta1
	 *            : name of first multifasta file
	 * @param multifasta2
	 *            : name of first multifasta file
	 * @param de
	 *            : value of divergence and evalue thresholds
	 * @return orthologFile : name of the resulting text file containing the
	 *         list of orthologs
	 **/
	public static String findOrthologs(String multifasta1, String multifasta2, String de) {

		String orthologFile = "orthologs.txt";
		Runtime runtime = Runtime.getRuntime();
		String commande = "rsd_search -q " + multifasta1 + " --subject-genome=" + multifasta2 + " -o " + "./"
				+ orthologFile + " --de " + de;
		try {
			Process p = runtime.exec(commande);
			BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = "";

			while ((line = output.readLine()) != null) {
				System.out.println(line);
			}

			while ((line = error.readLine()) != null) {
				System.out.println(line);
			}

			p.waitFor();
			System.out.println("Fin de l'execution de l'algorithme RSD. Voir fichier " + orthologFile
					+ " si le programme s'est execute correctement");
		} catch (IOException e) {
			System.out.println(e);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
		return orthologFile;
	}

	/**
	 * Makes query at NCBI databank to retrive fasta files associated with NCBI
	 * Protein GI corresponding to enzymes
	 * 
	 * @param biggIdsList
	 *            : list of BiGG ids (Protein GI) of one bacteria
	 * @param corresp
	 *            : correspondance between requested BiGG ids and found NCBI ids
	 * @param fastaName
	 *            : used to define the names of downloaded fasta files
	 * @return fileNames : list of downloaded fasta files' names
	 * @throws IOException
	 **/
	public static ArrayList<String> enzymesQuery(String organism, ArrayList<String> biggIdsList,
			Hashtable<String, String> corresp, String fastaName) throws IOException {
		ArrayList<String> fileNames = new ArrayList<String>();
		BufferedWriter bf = new BufferedWriter(new FileWriter(organism + "_correspondances.txt", true));
		for (int i = 0; i < biggIdsList.size(); i++) {
			String newFastaName = fastaName + "_" + i + ".fasta";
			String enzymeBiggId = biggIdsList.get(i);
			Query query = new Query(enzymeBiggId);
			try {
				query.execute(newFastaName);
				String enzymeNcbiId = query.getNcbiId();
				System.out.println(enzymeNcbiId);
				String fileName = query.getFileName();
				fileNames.add(fileName);
				corresp.put(enzymeBiggId, enzymeNcbiId);
				bf.write(enzymeBiggId + "\t" + enzymeNcbiId + "\n");
			} catch (StringIndexOutOfBoundsException e){
				e.printStackTrace();
			}
			catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				System.exit(1);
			}
		}
		bf.flush();
		bf.close();
		System.out.println("Toutes les requetes ont été effectuees");
		System.out.println("Les correspondances se trouvent dans le fichier " + organism + "_correspondances.txt");
		return fileNames;
	}

	public static void addOrthologyInfo(String sbmlFile, String orthologFile, Hashtable<String, String> corresp, String outputName) {

		RsdResultsParser parser = new RsdResultsParser(orthologFile);
		parser.findOrthologList();
		ArrayList<String> lref = parser.getOrthtologListRef();
		ArrayList<String> lquery = parser.getOrthtologListQuery();

		SbmlAnnotator annotator = new SbmlAnnotator(sbmlFile, lref, lquery, corresp);
		annotator.annotateName(outputName);

		System.out.println("Fichier SBML annote");
	}
}
