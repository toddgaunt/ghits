package Team1;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.json.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

class Args {
    String mapping_path, out_path, sim;
    boolean debug;
    public Args(String mapPath, String outPath, boolean debugFlag, String similarity) {
        mapping_path = mapPath;
        out_path = outPath;
        debug = debugFlag;
        sim = similarity;
    }
}

public class App
{
	public static Analyzer analyzer = new StandardAnalyzer();
	
    /**
     * Prints to stdout the proper program usage and then exits.
     */
    public static void usage()
    {
        System.out.println("usage: ghits [-d] [-m <mapping_path>] [-o <out_path>] [-s <similarity variant>]");
        System.out.println("    -d    Set the debug flag to enable extra print-outs to stdout");
        System.out.println("    -m    Specify the json file that provides a query expansion mapping");
        System.out.println("    -o    Specify the path to the output file the program writes json rankings to");
        System.out.println("    -s    Specify the similarity variant (e.g. ''lnn.bnn''");
        System.exit(-1);
    }

    /**
     * Parses a given query and uses it to run a search on an index.
     * @paramj query String to be parsed.
     * @return String with our compiled search results.
     * @throws Exception
     */
    public static JSONObject getQueryRFF(String query, Similarity sim) throws Exception {
        File index = new File("index");
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(index.toPath())));

        if(sim != null)
            indexSearcher.setSimilarity(sim);

        QueryParser parser = new QueryParser("content", new StandardAnalyzer());
        TopDocs results;
        ScoreDoc[] hits;

        // Search for the top five files
        results = indexSearcher.search(parser.parse(query), 5);
        hits = results.scoreDocs;
        JSONObject jsonObj = new JSONObject();
        for(ScoreDoc hit: hits){
            Document doc = indexSearcher.doc(hit.doc);
            jsonObj.put(doc.get("name"), hit.score);
        }
        return jsonObj;
    }
    
    public static String normalize_query(String query) {
    	String normalized = Normalizer.normalize(query, Form.NFD);
    	return normalized.replaceAll("[^A-Za-z0-9_\\- ]", "");
    }
    
    public static Args parse_args(String[] args) {
    	// Default arguments
    	String outPath = "ghits_output.json";
    	String mapPath = null, similarity = null;
    	boolean debug = false;

        char[][] argv = new char[args.length][0];
        for (int i = 0; i < argv.length; ++i)
            argv[i] = args[i].toCharArray();

    	int ptr;
    	String opt_arg;
    	for (int i = 0; i < argv.length; ++i) {
			ptr = 0;
    		if ('-' == argv[i][ptr]) {
				ptr += 1;
				while (ptr != argv[i].length) {
					ptr += 1;
                    if (argv[i].length == ptr) {
                    		if (i + 1 < argv.length) {
                    			opt_arg = new String(argv[i + 1]);
                    		} else {
                    			opt_arg = null;
                    		}
                    } else {
                            opt_arg = new String(argv[ptr]);
                    }
                    switch (argv[i][ptr - 1]) {
                    case 'h':
                            usage();
                            break;
                    case 'o':
                    		if (null == opt_arg) usage();
                            outPath = opt_arg;
                            break;
                    case 'm':
                    		if (null == opt_arg) usage();
                            mapPath = opt_arg;
                    		break;
                    case 's':
                    		if (null == opt_arg) usage();
                            similarity = opt_arg;
                    		break;
                    case 'd':
                    		debug = true;
                    		break;
                    default:
                            usage();
                    }
				}
			} else {
				//TODO(todd): required arguments go here
			}
		}
    	return new Args(mapPath, outPath, debug, similarity);
    }
    
    public static JSONObject read_mappings(String mappings_path) throws Exception {
    	InputStream is = new FileInputStream(mappings_path);
        if (is == null)
            throw new NullPointerException("Cannot find  file " + mappings_path);
        JSONObject map = new JSONObject(new JSONTokener(is));
        return map;
    }
    
    /**
     * Analyze given text and tokenize it into a list of terms.
     * @param text
     * @return
     * @throws IOException
     */
    public static ArrayList<String> tokenize(String text) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        TokenStream tokenStream = analyzer.tokenStream("name", text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while(tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        tokenStream.close();
        return result;
    }

    /**
     * Expand query using a mapping json.
     * @param mapping
     * @param queryTerms
     * @return
     * @throws Exception
     */
    public static String expandQuery(JSONObject mapping, List<String> queryTerms) throws Exception {
    	String expandedQuery = "";
        for(String term : queryTerms) {
        	Object obj;
        	try {
        		obj = mapping.get(term);
        	} catch (JSONException e) {
        		// if term not in map, just add back to query with no transformation.
        		expandedQuery += term + ' ';
                continue;
        	}

            JSONArray synonyms = (JSONArray) obj;
            expandedQuery += term + ' ';
            for (int i = 0; i < synonyms.length(); i++)
                expandedQuery += synonyms.get(i).toString() + ' ';
        }

        return expandedQuery;
    }

    /**
     * Prompt user for a non-empty query.
     * @return
     */
    public static String queryPrompt() {
        Scanner reader = new Scanner(System.in);
        String query = "";
        while(query.isEmpty()) {
            System.out.print("Enter a query >> ");
            System.out.flush();
            query = reader.nextLine();
        }
        return query;
    }

    /**
     * Return instance of specified similarity.
     * @param sim String
     * @return Similarity
     * @throws Exception
     */
    public static Similarity getSimilarity(String sim) throws Exception {
        if(sim == null) return null;
        switch (sim) {
            case "lnc.ltn":
                return TFIDF.LncLtnSim();
            case "bnn.bnn":
                return TFIDF.BnnBnnSim();
            case "anc.apc":
                return TFIDF.AnnApnSim();
            case "U-L":
                return LM.U_L();
            case "U-JM":
                return LM.U_JM();
            case "U-DS":
                return LM.U_DS();
            default:
                throw new Exception("Invalid similarity");
        }
    }

    /**
     * Main.
     * @param argsv String[]
     */
    public static void main(String[] argsv)
    {
        try {
        	// Parse arguments and flags
            Args args = parse_args(argsv);
			// Read in the query expansion mappings file if given
			JSONObject mapping = null;
			if (args.mapping_path != null) {
				try {
					mapping = read_mappings(args.mapping_path);
				} catch (Exception e) {
					mapping = null;
					System.out.println("Unable to open mappings file, terminating");
					System.exit(-1);
				}
			}
			
			JSONObject resultsObj = new JSONObject();
			String query = queryPrompt();
			
			// Begin the main interactive loop to ask the user for queries to search on
            while (!query.equals("q") && !query.equals("Q")) {
                String originalQuery = query;
                query = normalize_query(query);
                if (args.debug)
                    System.out.println("Original Query: " + query);
                // If a mappings file was provided, use it to expand the query
                if (mapping != null) {
                	query = expandQuery(mapping, tokenize(query));
                	if (args.debug)
                		System.out.println("Expanded Query: " + query);
                
                }

                Similarity similarity = getSimilarity(args.sim);
                
                // Run rankings retrieval, store as json, and output results to user
                resultsObj.put(originalQuery, getQueryRFF(query, similarity));
                System.out.println(resultsObj.toString(4));
               
                query = queryPrompt();
            }
            // Write the results to a file so they may be evaluated later
            FileWriter file = new FileWriter(args.out_path);
            file.write(resultsObj.toString(4));
            file.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
