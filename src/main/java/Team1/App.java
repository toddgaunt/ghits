package Team1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Scanner;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

class Args {
	String mapping_path;
	String out_path;
}

public class App
{


    /**
     * Prints to stdout the proper program usage and then exits.
     */
    public static void usage()
    {
        System.out.println("usage: ghits <index REPOSTRING | query QUERYFILE>");
        System.exit(-1);
    }

    /**
     * Parses a given query and uses it to run a search on an index.
     * @param is IndexSearcher. Used with the query to search our built index.
     * @paramj query String to be parsed.
     * @return String with our compiled search results.
     * @throws Exception
     */
    public static String getQueryRFF(IndexSearcher is, String query) throws Exception {
        int rank = 1;
        String ret = "";

        QueryParser parser = new QueryParser("content", new StandardAnalyzer());
        TopDocs results;
        ScoreDoc[] hits;

        // Search for the top five files
        results = is.search(parser.parse(query), 5);
        hits = results.scoreDocs;
        for (ScoreDoc hit: hits) {
            Document doc = is.doc(hit.doc);
            ret += rank;
            ret += " " + doc.get("name");
            ret += " " + hit.score;
            ret += "\n";
            rank += 1;
        }
        return ret;
    }

   
    
    static String readFile(String path) throws IOException 
    {
    	byte[] encoded = Files.readAllBytes(Paths.get(path));
    	return new String(encoded);
    }
    
    public static String normalize_query(String query)
    {
    	String normalized = Normalizer.normalize(query, Form.NFD);
    	return normalized.replaceAll("[^A-Za-z0-9_\\- ]", "");
    }
    
    public static Args parse_args(char[][] argv) {
    	Args args = new Args();
    	int ptr;
    	String opt_arg = "";
    	for (int i = 0; i < argv.length; ++i) {
			ptr = 0;
    		if ('-' == argv[i][ptr]) {
				ptr += 1;
				while ('\0' != argv[i][ptr]) {
					ptr += 1;
                    if ('\0' == argv[i][ptr]) {
                    		if (i + 1 >= argv.length)
                    			usage();
                            opt_arg = new String(argv[i + 1]);
                    } else {
                            opt_arg = new String(argv[ptr]);
                    }
                    switch (argv[i][ptr - 1]) {
                    case 'h':
                            usage();
                            break;
                    case 'o':
                            args.out_path = opt_arg;
                            break;
                    case 'm':
                    		args.mapping_path = opt_arg;
                    		break;
                    default:
                            usage();
                    }
				}
			} else {
				//TODO(todd): required arguments go here
			}
		}
    	return args;
    }

    /**
     * Main.
     * @param argsv String[]. "PathToRepoRoot"
     */
    public static void main(String[] argsv)
    {
        try {
			char[][] argv = new char[argsv.length][0];
			for (int i = 0; i < argv.length; ++i ) {
				argv[i] = argsv[i].toCharArray();
			}
			Args args = parse_args(argv);
			
			Scanner reader = new Scanner(System.in);
			File index = new File("index");
			IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(index.toPath())));
			while (true) {
				System.out.print("Enter a query >> ");
				String query = reader.nextLine();
				if (query.equals("q") || query.equals("Q"))
					break;
				System.out.println("Query: " + query);
				String normalized_query = normalize_query(query);
				System.out.println("Normalized Query: " + normalized_query);
				/* Use the index */
				
				String results = getQueryRFF(indexSearcher, normalized_query);
				System.out.println('\n' + results);
			}
			reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
