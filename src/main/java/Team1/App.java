package Team1;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

class Args {
    String mapping_path;
    String out_path;
    boolean debug;
    public Args(String mapPath, String outPath, boolean debugFlag) {
        mapping_path = mapPath;
        out_path = outPath;
        debug = debugFlag;
    }
}

public class App
{
    /**
     * Prints to stdout the proper program usage and then exits.
     */
    public static void usage()
    {
        System.out.println("usage: ghits [-d] [-m <mapping_path>] [-o <out_path>] ");
        System.out.println("    -d    Set the debug flag to enable extra print-outs to stdout");
        System.out.println("    -m    Specify the json file that provides a query expansion mapping");
        System.out.println("    -o    Specify the path to the output file the program writes json rankings to");
        System.exit(-1);
    }

    /**
     * Parses a given query and uses it to run a search on an index.
     * @paramj query String to be parsed.
     * @return String with our compiled search results.
     * @throws Exception
     */
    public static JSONObject getQueryRFF(String query) throws Exception {
        File index = new File("index");
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(index.toPath())));

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
    	String outPath = "", mapPath = "";
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
    	return new Args(mapPath, outPath, debug);
    }

    /**
     * Expand query using a mapping json.
     * @param args
     * @param query
     * @return
     * @throws Exception
     */
    public static String queryExpansion(Args args, String query, boolean flip) throws Exception {
        InputStream is = new FileInputStream(args.mapping_path);
        if (is == null)
            throw new NullPointerException("Cannot find  file " + args.mapping_path);
        JSONObject map = new JSONObject(new JSONTokener(is));

        ArrayList<String> termsInQuery = ThesaurusBuilder.analyze(query);

        String expandedQuery = "";
        for(String term : termsInQuery) {
            Object obj = map.get(term);

            if(obj == null) { // if term not in map, just add back to query and skip
                expandedQuery += term + ' ';
                continue;
            }

            JSONArray synonyms = (JSONArray) obj;
            System.out.println(synonyms.toString());
            if(!flip)
                expandedQuery += term + ' ';
            for (int i = 0; i < synonyms.length(); i++)
                expandedQuery += synonyms.get(i).toString() + ' ';
        }

        System.out.println("Expanded query: " + expandedQuery);
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
        return normalize_query(query);
    }

    /**
     * Main.
     * @param argsv String[]
     */
    public static void main(String[] argsv)
    {
        boolean customRun = argsv.length > 1;
        try {
            Args args;
            if(customRun)
                args = parse_args(argsv);
			else // default run
                args = new Args("thesaurus.json", "", true);

			String query = queryPrompt();
            while (!query.equals("q")) {
                if (args.debug)
                    System.out.println("Original Query: " + query);

                // Query expansion
                query = queryExpansion(args, query, false);
                if (args.debug)
                    System.out.println("Expanded Query: " + query);

                // Run retrieval and store in json
                JSONObject resultsObj = new JSONObject();
                resultsObj.put(query, getQueryRFF(query));
                System.out.println(resultsObj.toString(4));

                // Write json to file
                FileWriter file = new FileWriter("output.json");
                file.write(resultsObj.toString(4));
                file.flush();
                query = queryPrompt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
