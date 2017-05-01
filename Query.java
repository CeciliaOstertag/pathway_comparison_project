package com.github.pathway_comparison_project;

import org.apache.log4j.BasicConfigurator;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;

import com.algosome.eutils.EntrezSearch;
import com.algosome.eutils.EntrezFetch;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;
import com.algosome.eutils.io.*;

/**
 * This class is used to query NCBI Protein DB for enzyme sequences in fasta
 * format
 * 
 * @see PathwayComparisonProject
 * @see EnzymeFinder
 * @see RsdResultsParser
 * @see SbmlAnnotator
 * 
 * @author Peter Bock, Guillamaury Debras, Mercia Ngoma-Komb, Cécilia Ostertag,
 *         Franck Soubes
 */

public class Query {

/*
* Enzyme EntrezSearch id
*/
	protected String idstring = ""; 
	
	/*
* Url used to download the text file
*/
	protected String finalUrl = ""; 
	
	/*
* Enzyme NCBI GI id (called BiggId because the sbml file comes from BiGG database)
*/
	protected String enzymeBiggId; 
	
	/*
* Enzyme NCBI Protein id 
*/
	protected String enzymeNcbiId;
	
	/*
* NCBI database to query
*/
	protected String dataBase; 
	
	/*
* Name to give to the fasta file when downloading
*/
	protected String fastaFile; 
	
	/**
	 * Automated query to NCBI Protein database. Uses a Protein GI id to search
	 * the corresponding sequence, and download the file in fasta format
	 * 
	 * @param enzymeBiggId
	 *            : Protein GI id corresponding to an enzyme
	 **/

	public Query(String enzymeBiggId) {
		this.enzymeBiggId = enzymeBiggId;
		dataBase = EntrezSearch.DB_PROTEIN;
	}

	/**
	 * Copied from jeutils EntrezSearch class to avoid bugs. Parses out an XML
	 * value tag out of line using a regular expression.
	 * 
	 * @param line
	 *            A line of text to parse.
	 * @param tag
	 *            The XML tag identifier.
	 * @return A string representation of the text between the given tags, or an
	 *         empty string if nothing was found.
	 */
	protected String parseTextFromLine(String line, String tag) {
		Pattern pattern = Pattern.compile(tag + ">(.+)</" + tag);
		Matcher matcher = pattern.matcher(line);
		matcher.find();
		try {
			return matcher.group(1);
		} catch (IllegalStateException ise) {
			return "";
		}
	}

	/**
	 * Copied from jeutils EntrezSearch class to avoid bugs. Parses an ID
	 * identifier out of the give line.
	 * 
	 * @param line
	 *            A line of text to parse.
	 * @return A string representation of the ID. This parses any text between
	 *         <Id> and </Id>
	 * @see com.algosome.eutils.io.InputStreamParser
	 */
	protected String parseID(String line) {
		return parseTextFromLine(line, "Id");
	}

	/**
	 * Parses an new hyperlink reference out of the give line, in case the
	 * document was moved to another url.
	 * 
	 * @param line
	 *            A line of text to parse.
	 * @return A string representation of the new url.
	 */
	protected String parseMove(String line, String tag) {
		Pattern pattern = Pattern.compile(tag + ">The document has moved <a href=\"(.+)\">here</a>.</" + tag);
		Matcher matcher = pattern.matcher(line);
		matcher.find();
		try {
			return matcher.group(1);
		} catch (IllegalStateException ise) {
			return "";
		}
	}

	/**
	 * Execution of the query to NCBI Protein databank.
	 * 
	 * @param fastaName
	 *            : Name of the downloaded fasta file
	 **/

	public void execute(String fastaName) throws Exception {

		// BasicConfigurator.configure();

		EntrezSearch search = new EntrezSearch();

		search.setDatabase(dataBase);

		search.setTerm(enzymeBiggId);

		search.setMaxRetrieval(1);

		InputStreamParser myInputStreamParser1 = new InputStreamParser() {

			public void parseFrom(int start) {
			}

			public void parseTo(int end) {
			}

			public void parseInput(InputStream is) throws IOException {

				String url = "";

				BufferedReader line_reader = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while ((line = line_reader.readLine()) != null) {
					if (line.indexOf("<Id>") != -1) {
						String id = parseID(line);
						if (id != null) {
							idstring += id + " ";
						}
					}
					// si l'url a change, on remplace la premiere url par la
					// deuxieme
					else if (line.indexOf("<p>") != -1) {

						url = parseMove(line, "p");
						is = new URL(url).openStream();
						BufferedReader line_reader2 = new BufferedReader(new InputStreamReader(is));
						String line2 = null;
						while ((line2 = line_reader2.readLine()) != null) {
							if (line2.indexOf("<Id>") != -1) {
								String id = parseID(line2);
								if (id != null) {
									idstring += id + " ";
								} 
							}
						}
					}
				}
			}
		};		
		if ((myInputStreamParser1 != null))
			search.doQuery(myInputStreamParser1);
		else
			System.out.println("myInputStreamParser1 NULL!");
		
		if (idstring != "") {
			search.setIds(idstring); // on donne directement les ids trouves par
										// le
			// parseur pour contourner le bug

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new Exception(e);
			}
			try {
				EntrezFetch fetch = new EntrezFetch(search);
				fetch.setRetType("fasta");
				fetch.setRetMode("text");

				InputStreamParser myInputStreamParser2 = new InputStreamParser() {
					public void parseFrom(int start) {
					}

					public void parseTo(int end) {
					}

					public void parseInput(InputStream is) throws IOException {
						BufferedReader br = new BufferedReader(new InputStreamReader(is));
						String line = null;
						while ((line = br.readLine()) != null) {
							if (line.indexOf("<p>") != -1) {
								finalUrl = parseMove(line, "p");
							}
						}
					}
				};

				if ((myInputStreamParser2 != null))
					fetch.doQuery(myInputStreamParser2);
				else
					System.out.println("myInputStreamParser2 NULL!");

				fastaFile = download(finalUrl, fastaName);
				System.out.println(fastaFile);
				readProtFasta(fastaFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Download the file in fasta format
	 * 
	 * @param url
	 *            : Name of the efetch url used to download the file
	 * @param fastaName
	 *            : Name of the downloaded fasta file
	 **/

	public String download(String url, String fastaName) {
		Runtime runtime = Runtime.getRuntime();
		String commande = "wget -O " + fastaName + " " + url;
		try {
			Process p = runtime.exec(commande);
			p.waitFor();
		} catch (IOException e) {
			System.out.println(e);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
		return fastaName;
	}

	public String getFileName() {
		return fastaFile;
	}

	/**
	 * Reads a fasta file containing proteic sequences
	 * 
	 * @param filename
	 *            : name of the fasta file to read from
	 * @param seqList
	 *            : list of sequences found in the file
	 **/

	public void readProtFasta(String filename) {
		LinkedHashMap<String, ProteinSequence> helper;
		try {
			File file = new File(filename);
			helper = FastaReaderHelper.readFastaProteinSequence(file);
			for (Entry<String, ProteinSequence> entry : helper.entrySet()) {
				enzymeNcbiId = entry.getValue().getAccession().toString();
				String[] fields = enzymeNcbiId.split(" ");
				enzymeNcbiId = fields[0];
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the corresponding NCBI id
	 * 
	 * @return enzymeNcbiId : NCBI id
	 **/

	public String getNcbiId() {
		return enzymeNcbiId;
	}

}
