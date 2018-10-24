package Team1;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
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
    public static void usage()
    {
        System.out.println("usage: make run REPO=path/to/root/of/repo");
        System.exit(-1);
    }

    public static String getQueryRFF(IndexSearcher is, String pageID, String query) throws Exception {
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
            ret += pageID;
            ret += " Q0";
            ret += " " + doc.get("id");
            ret += " " + rank;
            ret += " " + hit.score;
            ret += " team1-default";
            ret += "\n";
            rank += 1;
        }
        return ret;
    }

    public static void main(String[] args)
    {
        try {
            String dataFile = args[0];

			/* Setup the indexer */
            Directory indexDir = FSDirectory.open(new File("index").toPath());
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
            IndexWriter iwriter = new IndexWriter(indexDir, config);

			/* Add the repository's files to the index */
            // TODO add files to index

            iwriter.close();

			/* Use the index */
            IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File("index").toPath())));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
