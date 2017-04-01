
import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.core.util.FileUtils;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;

import com.algosome.eutils.EntrezSearch;
import com.algosome.eutils.JeUtils;
import com.algosome.eutils.util.OutputListener;
import com.algosome.eutils.util.OutputListenerAdapter;
import com.algosome.eutils.net.URLConnect;
import com.algosome.eutils.EntrezFetch;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;
import java.lang.StringBuffer;
import com.algosome.eutils.io.*;
import com.algosome.eutils.net.*;


public class Query{

	protected String idstring=""; //id de l'enzyme
	protected String finalUrl=""; //url qui permet de telecharger le fichier 
	protected String enzymeBiggId ; //id bigg de l'enzyme
	protected String enzymeNcbiId;
	protected String organism; 
	protected String sequenceType;
	protected String dataBase ; //NCBI Gene ou Protein
	protected String fastaFile ; //nom du fichier fasta qui sera telecharge
	
	public Query(String enzymeBiggId, String organism, String sequenceType){	
		this.enzymeBiggId= enzymeBiggId;
		this.organism=organism;
		this.sequenceType=sequenceType;
		if (sequenceType.equals("nucleic")){
				dataBase = EntrezSearch.DB_GENE; }
		else if (sequenceType.equals("proteic"))
		{
			dataBase = EntrezSearch.DB_PROTEIN;
		}
		else
		{
			System.out.println("Erreur : ce type de sequence est inconnu : "+sequenceType);
			System.exit(1);
		}
	}
			
	//on remet les parsers pour eviter les bugs
	protected String parseTextFromLine(String line, String tag){
		Pattern pattern = Pattern.compile( tag+">(.+)</"+tag );
		Matcher matcher = pattern.matcher( line );
		matcher.find();
		try{
		return matcher.group(1);
		}catch( IllegalStateException ise ){
			return "";
		}
	}
	
	protected String parseID(String line){
		return parseTextFromLine(line, "Id");
	}
	
	protected String parseMove(String line, String tag){
		Pattern pattern = Pattern.compile( tag+">The document has moved <a href=\"(.+)\">here</a>.</"+tag );
		Matcher matcher = pattern.matcher( line );
		matcher.find();
		try{
		return matcher.group(1);
		}catch( IllegalStateException ise ){
			return "";
		}
	}
	

	public void execute(String fastaName) throws Exception{	
		
		BasicConfigurator.configure();
		
		EntrezSearch search = new EntrezSearch();
		
		search.setDatabase(dataBase);
		
		search.setTerm(enzymeBiggId+" "+organism);
		
		//search.setSearchField("affl");
		
		search.setMaxRetrieval(1);
				
		search.doQuery(new InputStreamParser(){

			
		    public void parseFrom(int start){}

		    public void parseTo(int end){}
		    
		    public void parseInput(InputStream is) throws IOException{
		    	

		    	String url="";

				BufferedReader line_reader = new BufferedReader(new InputStreamReader(is));
				StringBuffer output = new StringBuffer();
				String line = null;
				while ( (line = line_reader.readLine() ) != null )
				{
					if ( line.indexOf("<Id>") != -1 ) {
						
						String id = parseID(line);
						
						if ( id != null ){
							idstring+=id+" ";
							System.out.println("id trouve :"+id);
						}
						
					}
					//si l'url a change, on remplace la premiere url par la deuxieme
					else if ( line.indexOf("<p>") != -1){
						
						url = parseMove(line,"p");
						//System.out.println("L'url a change : "+url);
						is = new URL(url).openStream();
						BufferedReader line_reader2 = new BufferedReader(new InputStreamReader(is));
						StringBuffer	output2 = new StringBuffer();
						String line2 = null;
						while ( (line2 = line_reader2.readLine() ) != null )
						{
							if ( line2.indexOf("<Id>") != -1 ) {
								
								String id = parseID(line2);
								
								if ( id != null ){
									idstring+=id+" ";
									System.out.println("id trouve :"+id);
								}
								
							}
						}
					}
					
					
					
				}

		    }
		    

		});
		
		search.setIds(idstring); //on donne directement les ids trouves par le parseur pour contourner le bug
				
		
		try{
		
		    Thread.sleep(1000);
		
		}catch(InterruptedException e){
		
		    e.printStackTrace();
		
		    throw new Exception(e);
		
		}
		
			EntrezFetch fetch = new EntrezFetch(search);
			fetch.setRetType("fasta"); //format fasta
			fetch.setRetMode("text"); //telechargera le fichier
			fetch.doQuery(new InputStreamParser(){
	
			    public void parseFrom(int start){}
	
			    public void parseTo(int end){}
	
			    public void parseInput(InputStream is) throws IOException{
			    	
			    BufferedReader br = new BufferedReader(new InputStreamReader(is));
	
				   String line = null;
	
				   while ( ( line = br.readLine() ) != null ){
	
					 if ( line.indexOf("<p>") != -1){
							finalUrl = parseMove(line,"p");
						}
	
				   }						
			    }
			});

			//System.out.println(finalUrl);
			
			
			fastaFile=download(finalUrl, fastaName);
			System.out.println("Fichier fasta téléchargé");
			System.out.println(fastaFile);
			if (sequenceType.equals("proteic"))
			{
				readProtFasta(fastaFile);
			}
			else
			{			
				readDNAFasta(fastaFile);
				}

	}
	
		
	public String download(String url, String fastaName){

		Runtime runtime = Runtime.getRuntime();
		String commande = "wget -O "+fastaName+" "+url; 

		try {
			Process p = runtime.exec(commande);
			p.waitFor();
			
		}		
		catch (IOException e) {
			System.out.println(e);
		}
		catch (InterruptedException e) {
			System.out.println(e);
		}
		return fastaName;		
	}
	
	
	public String getFileName(){
		return fastaFile;
	}
	
	public void readProtFasta(String filename){

		LinkedHashMap<String, ProteinSequence> helper;
		try {
			File file = new File(filename);
			helper = FastaReaderHelper.readFastaProteinSequence(file);
			for (  Entry<String, ProteinSequence> entry : helper.entrySet() ) {
				enzymeNcbiId=entry.getValue().getAccession().toString();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void readDNAFasta(String filename){

		LinkedHashMap<String, DNASequence> helper;
		try {
			File file = new File(filename);
			helper = FastaReaderHelper.readFastaDNASequence(file);
			for (  Entry<String, DNASequence> entry : helper.entrySet() ) {
				enzymeNcbiId=entry.getValue().getAccession().toString();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getNcbiId(){
		return enzymeNcbiId;
	}
	

}


