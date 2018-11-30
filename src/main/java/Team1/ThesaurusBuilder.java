package Team1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Class to represent the data of a term. Simplifies building term-doc matrix from Map.
 */
class Data {
    double[] postings;
    int indexID;
    Data(int nDocs) {
        postings = new double[nDocs];
    }
    void assignIndex(int id) { indexID = id; }
    void increment(int i) { postings[i]++; }
}

public class ThesaurusBuilder {
    final static String[] valid_extensions = {".java", ".c", ".h", ".py"};
    /**
     * Build a list of Documents from a directory.
     * @param folder
     * @return All the docs in a repo
     * @throws Exception
     */
    public static void buildDocs(File folder, ArrayList<Document> docList, String rootName) throws Exception {
        for (File entry : folder.listFiles()) {
            if (entry.isDirectory()) {
                buildDocs(entry, docList, rootName);
            } else {
                for (String ext : valid_extensions) {
                    if (entry.getName().endsWith(ext)) {
                        String name = "a" + folder.getPath().substring(rootName.length()).replace('\\','/') + "/" + entry.getName();
//                        System.out.println(name);
                        String fileContent = new String(Files.readAllBytes(entry.toPath()));
                        Document doc = new Document();
                        doc.add(new StringField("name", name, Field.Store.YES));
                        doc.add(new TextField("content", fileContent, Field.Store.YES));
                        docList.add(doc);
                    }
                }
            }
        }
    }

    /**
     * Analyze given text and strip out terms into list.
     * @param text
     * @return
     * @throws IOException
     */
    public static ArrayList<String> analyze(String text) throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        ArrayList<String> result = new ArrayList<String>();
        TokenStream tokenStream = analyzer.tokenStream("name", text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while(tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        return result;
    }

    /**
     * Build term-doc matrix from a list of documents.
     * @param docList
     * @return
     */
    public static double[][] buildTermDocMatrix(ArrayList<Document> docList) throws Exception {
        HashMap<String, Data> termDocs = new HashMap<>();
        final int numDocs = docList.size(); // number of columns

        // build Map version of a term-doc matrix
        for(int i = 0; i < docList.size(); i++) {
            Document d = docList.get(i);
            ArrayList<String> tokens = analyze(d.get("content"));

            for (String t : tokens) {
                Data termData = termDocs.get(t);
                if (termData == null)
                    termDocs.put(t, new Data(numDocs));
                else
                    termData.increment(i);
            }
        }

        final int numTerms = termDocs.size(); // number of rowsd
        double[][] A = new double[numTerms][numDocs]; // initialize 2D matrix

        // copy values
        int i = 0;
        for(HashMap.Entry<String, Data> entry : termDocs.entrySet()) {
            Data data = entry.getValue();
            data.assignIndex(i);
            A[data.indexID] = data.postings;
            i++;
        }
        return A;
    }

    public static void main(String[] args) {

        final String repoName = "sway-master";

        try {
            // Recursively build our list of docs
            ArrayList<Document> docList = new ArrayList<>();
            buildDocs(new File(repoName), docList, repoName);
            System.out.println("Number of docs: " + docList.size());

            // With all the documents, we build term-document matrix
            // Each document has an index (or columnID)
            // Each term has an index (or rowID)
            // Matrix A dimensions = #tokens X #docs
            double[][] tDM = buildTermDocMatrix(docList);
            System.out.println("How many times does 'int' occur in the first doc?  " + tDM[2617][0]);

            double[][] coMatrix = MatrixUtilities.multiply(tDM, MatrixUtilities.transpose(tDM));


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
