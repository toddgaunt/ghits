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

public class Indexer {
    final static String[] valid_extensions = {".java", ".c", ".h", ".py"};
    
    /**
     * Prints to stdout the proper program usage and then exits.
     */
    public static void usage()
    {
        System.out.println("usage: ghits-index <DIRECTORY-TO-BE-INDEXED>");
        System.exit(-1);
    }
	
	/**
     * Recursively add files to the index.
     * @param folder
     */
    public static void indexProject(File folder, IndexWriter indexWriter) throws Exception {
    	for (File entry : folder.listFiles()) {
            if (entry.isDirectory()) {
                indexProject(entry, indexWriter);
            } else {
            	final String name = folder.getPath() + "/" + entry.getName();
            	for (String ext : valid_extensions) {
            		if (name.endsWith(ext)) {
            			System.out.println(name);
                        String fileContent = new String(Files.readAllBytes(entry.toPath()));
                        Document doc = new Document();
                        doc.add(new StringField("name", name, Field.Store.YES));
                        doc.add(new TextField("content", fileContent, Field.Store.YES));
                        indexWriter.addDocument(doc);
            		}
            	}   
            }
        }
    }
    
    public static void main(String[] args) {
            	if (args.length < 1)
            		usage();
            	try {
	            	final File repoRootFolder = new File(args[0]);
	
	    			/* Setup the indexer */
	                Directory indexDir = FSDirectory.open(new File("index").toPath());
	                IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
	                IndexWriter indexWriter = new IndexWriter(indexDir, config);
	
	    			/* Add the repository's files to the index */
	                System.out.println("Files indexed:");
	                indexProject(repoRootFolder, indexWriter);
	                indexWriter.close();
            	} catch (Exception e) {
        			e.printStackTrace();
        		}
	}
}
