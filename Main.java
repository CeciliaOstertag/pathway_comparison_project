import java.io.BufferedReader;
import java.io.File; 
import java.io.FileInputStream; 
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.*;

import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.ProteinSequence; 
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;  
import org.biojava.nbio.core.sequence.io.FastaWriterHelper;

/**
 * Main program
 * @see Request
 */

public class Main {	


	public static void main(String[] args) 
	{
		Hashtable corresp1 = new Hashtable(); //correspondances ids bigg / ids ncbi bacterie1
		Hashtable corresp2 = new Hashtable(); //correspondances ids bigg / ids ncbi bacterie2
		ArrayList<String> fileNames1 = new ArrayList<String>();
		ArrayList<String> fileNames2 = new ArrayList<String>();
		String sequenceType = null;
		String sbml1;
		String sbml2;
		String multifasta1 = null;
		String multifasta2 = null;
		String orthologFile;
		
		System.out.println("Possedez vous deja les fichiers multifasta ? (Y/N) ");
		String ans = string_input();
		if (ans.equals("Y"))
		{
			System.out.println("Chemin du fichier multifasta de référence : ");
			multifasta1 = string_input();
			System.out.println("Chemin du deuxieme fichier multifasta : ");
			multifasta2 = string_input();
			
			System.out.println("Type de séquences (nucleic/proteic) : ");
			sequenceType = string_input();
		}
		else if (ans.equals("N"))
		{
			
			System.out.println("Chemin du fichier SBML de référence : ");
			sbml1 = string_input();
			System.out.println("Nom de l'organisme de référence : ");
			String organism1 = string_input();
			System.out.println("Chemin du second fichier SBML : ");
			sbml2 = string_input();
			System.out.println("Nom du second organisme : ");
			String organism2 = string_input();
			
			System.out.println("Type de séquences (nucleic/proteic) : ");
			sequenceType = string_input();
			
			ArrayList biggIdsList1 = findEnzymes(sbml1);
			
			fileNames1 = enzymesQuery(organism1, biggIdsList1, sequenceType, corresp1,"Tmp/results1");

			String outputName1 = "FastaFiles/"+organism1.replaceAll(" ", "_")+".fasta"; 
			multifasta1=makeMultifasta(fileNames1,sequenceType,outputName1); 
			
			ArrayList biggIdsList2 = findEnzymes(sbml2);
			
			fileNames2 = enzymesQuery(organism2, biggIdsList2, sequenceType, corresp2,"Tmp/results1");

			String outputName2 = "FastaFiles/"+organism2.replaceAll(" ", "_")+".fasta"; 
			multifasta2=makeMultifasta(fileNames2,sequenceType,outputName2); 
		}
		else
		{
			System.out.println("Erreur");
			System.exit(1);
		}
		//System.out.println("Valeur de la e-value pour la recherche d'orthologues : ");
		//String eValue = string_input();
		

		orthologFile=findOrthologs(multifasta1,multifasta2,sequenceType);
		
		//Fusion fusion = new Fusion("","",orthologFile,corresp1, corresp2);
		//fusion.findNcbiIds();
	}
	
	/**
     * String user input
     * @return string (String)
     **/
	public static String string_input()
    {
	
	try{
	    BufferedReader buff = new BufferedReader (new InputStreamReader(System.in)); 
	    String string=buff.readLine();
	    return string;
	}
	catch(IOException e) {System.out.println(e);
	    return null;
	}
    }
	
	public static ArrayList findEnzymes(String sbmlFile)
	{
		EnzymeFinder finder = new EnzymeFinder(sbmlFile);
    	finder.find();
    	ArrayList enzymeList = finder.getEnzymeList();
    	return enzymeList;
	}
	
	/**
     * Reads a fasta file containing proteic sequences
     * @param filename : name of the fasta file to read from
     * @param seqList : list of sequences found in the file
     **/
	public static void readProtFasta(String filename,ArrayList<ProteinSequence> seqList){

		LinkedHashMap<String, ProteinSequence> helper;
		try {
			File file = new File(filename);
			helper = FastaReaderHelper.readFastaProteinSequence(file);
			for (  Entry<String, ProteinSequence> entry : helper.entrySet() ) {
				seqList.add(entry.getValue());
			}
			

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
     * Writes a fasta file containing proteic sequences
     * @param filename : name of the output fasta file 
     * @param seqs : list of sequences that will be written
     **/
	public static void writeProtFasta(String filename, ArrayList<ProteinSequence> seqs){

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
     * Reads a fasta file containing nucleic sequences
     * @param filename : name of the fasta file to read from
     * @param seqList : list of sequences found in the file
     **/
	public static void readDNAFasta(String filename, ArrayList<DNASequence> seqList){

		LinkedHashMap<String, DNASequence> helper;
		try {
			File file = new File(filename);
			
			helper = FastaReaderHelper.readFastaDNASequence(file);
			
			
			for (Entry<String, DNASequence> entry : helper.entrySet() ) {

				seqList.add(entry.getValue());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
     * Writes a fasta file containing nucleic sequences
     * @param filename : name of the output fasta file 
     * @param seqs : list of sequences that will be written
     **/
	public static void writeDNAFasta(String filename, ArrayList<DNASequence> seqs){

		try {
			File file = new File(filename);
			FastaWriterHelper.writeNucleotideSequence(file, seqs);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
     * Makes a multifasta file from a list of fasta files
     * @param fileNames : list of fasta files names
     * @param type : type of sequence (nucleic or proteic)
     * @param outputName : name of the resulting multifasta file
     * @return outputName : name of the resulting multifasta file
     **/
	public static String makeMultifasta(ArrayList<String> fileNames, String type, String outputName)
	{
		ArrayList seqList = new ArrayList();
		for (int i=0; i<fileNames.size();i++)
		{ 
			String name = fileNames.get(i);
			if (type.equals("proteic"))
			{
				readProtFasta(name,seqList);
			}
			else
			{			
				readDNAFasta(name,seqList);
				}
		}
		
		if (type.equals("proteic")) 
		{
			writeProtFasta(outputName, seqList);
		}
		else
		{
			writeDNAFasta(outputName, seqList);
		}
		
		System.out.println("Le fichier multifasta a été créé : "+outputName);
		return outputName;

	}

	/**
     * Uses RSD algorithm to find ortholog sequences between two sets of sequences
     * @param multifasta1 : name of first multifasta file
     * @param multifasta2 : name of first multifasta file
     * @param type : type of sequence (nucleic or proteic)
     * @return orthologFile : name of the resulting text file containing the list of orthologs
     **/
	public static String findOrthologs(String multifasta1,String multifasta2, String type){
		
		// TODO modifier pour prendre en compte la e-value et sa valeur par defaut

		String orthologFile = "orthologs3.txt";
		Runtime runtime = Runtime.getRuntime();
		String commande = "rsd_search -q "+multifasta1+" --subject-genome="+multifasta2+" -o "+orthologFile;
		try {
			Process p = runtime.exec(commande);
			p.waitFor();
			System.out.println("Recherche d'orthologues terminéee, voir fichier "+orthologFile);
		}		
		catch (IOException e) {
			System.out.println(e);
		}
		catch (InterruptedException e) {
			System.out.println(e);
		}
		return orthologFile;
		
	}
	
	/**
     * Makes request at NCBI databank to retrive fasta files associated with BiGG ids corresponding to enzymes
     * @param biggIdsList : list of BiGG ids of one bacteria
     * @param sequenceType : type of sequence (nucleic or proteic)
     * @param corresp : correspondance between requested BiGG ids and found NCBI ids
     * @param fastaName : used to define the names of downloaded fasta files
     * @return fileNames : list of downloaded fasta files' names
     **/
	public static ArrayList<String> enzymesQuery(String organism, ArrayList biggIdsList, String sequenceType, Hashtable corresp,String fastaName) {
		
		ArrayList<String> fileNames = new ArrayList<String>();
		for (int i=0; i<biggIdsList.size();i++)
		{
			String newFastaName = fastaName+"_"+i+".fasta";
			String enzymeBiggId = (String)biggIdsList.get(i);
			Query query = new Query(enzymeBiggId,organism,sequenceType);
			try {
				query.execute(newFastaName);
			String enzymeNcbiId = query.getNcbiId();
			System.out.println(enzymeNcbiId);
			String fileName = query.getFileName();
			fileNames.add(fileName);
			corresp.put(enzymeBiggId, enzymeNcbiId);
			
			}
			catch (Exception e)
			{
				System.out.println(e);
				return null;
			}
			
		}
		System.out.println("Toutes les requêtes ont été effectuées");
		return fileNames;
	}
}
