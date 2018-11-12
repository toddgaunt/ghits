package Team1;

import java.io.File;
import java.nio.file.Files;

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
        System.out.println("usage: make run MODE=<index|query> REPO=path/to/root/of/repo");
        System.exit(-1);
    }

    /**
     * Parses a given query and uses it to run a search on an index.
     * @param is IndexSearcher. Used with the query to search our built index.
     * @param query String to be parsed.
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

    /**
     * Recursively add files to the index.
     * @param folder
     */
    public static void indexProject(File folder, IndexWriter indexWriter) throws Exception {
    	final String[] valid_extensions = {".java", ".c", ".h", ".py"};
    	for (File entry : folder.listFiles()) {
            if (entry.isDirectory()) {
                indexProject(entry, indexWriter);
            } else {
            	final String name = entry.getName();
            	for (String ext : valid_extensions) {
            		if (name.endsWith(ext)) {
            			System.out.println(entry.getName());
                        String fileContent = new String(Files.readAllBytes(entry.toPath()));
                        Document doc = new Document();
                        doc.add(new StringField("name", entry.getName(), Field.Store.YES));
                        doc.add(new TextField("content", fileContent, Field.Store.YES));
                        indexWriter.addDocument(doc);
            		}
            	}   
            }
        }
    }

    /**
     * Main.
     * @param args String[]. "PathToRepoRoot"
     */
    public static void main(String[] args)
    {
        try {
            if(args.length < 1)
            	usage();
            
            if (args[0].equals("index")) {
            	if (args.length < 2)
            		usage();
            	final File repoRootFolder = new File(args[1]);

    			/* Setup the indexer */
                Directory indexDir = FSDirectory.open(new File("index").toPath());
                IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
                IndexWriter indexWriter = new IndexWriter(indexDir, config);

    			/* Add the repository's files to the index */
                System.out.println("Files indexed:");
                indexProject(repoRootFolder, indexWriter);
                indexWriter.close();
            } else if (args[0].equals("query")){
            	String query = "";
            	for (int i = 1; i < args.length; i += 1) {
            		query += " " + args[i];
            	}
            	System.out.println("Query: " + query);
            	/* Use the index */
    			File index = new File("index");
                IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(index.toPath())));
                String results = getQueryRFF(indexSearcher, query);
                System.out.println('\n' + results);
            } else {
            	usage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
