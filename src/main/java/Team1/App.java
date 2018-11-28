package Team1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;

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

    /**
     * Main.
     * @param args String[]. "PathToRepoRoot"
     */
    public static void main(String[] args)
    {
        try {
			if (args.length < 2)
				usage();
			String query = readFile(args[1]);
			System.out.println("Query: " + query);
			String normalized_query = normalize_query(query);
			System.out.println("Normalized Query: " + normalized_query);
			/* Use the index */
			File index = new File("index");
			IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(index.toPath())));
			String results = getQueryRFF(indexSearcher, normalized_query);
			System.out.println('\n' + results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
