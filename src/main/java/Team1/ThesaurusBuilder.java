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
    String term;
    Data(String t, int nDocs, int docIndex) {
        term = t;
        postings = new double[nDocs];
        increment(docIndex);
    }
    void assignIndex(int id) { indexID = id; }
    void increment(int i) { postings[i]++; }
    void calcTFIDF() {
        double docFreq = 0;
        for (int i = 0; i < postings.length; i++) {
            if(postings[i] != 0) docFreq++;
        }
        double N = postings.length;
        double idf = Math.log10(N/docFreq);
        for (int i = 0; i < postings.length; i++) {
            double tf = postings[i];
            postings[i] = tf * idf;
        }
    }
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
                        if(fileContent.length() == 0) continue;
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
    public static double[][] buildTermDocMatrix(ArrayList<Document> docList, boolean normalize) throws Exception {
        HashMap<String, Data> termDocs = new HashMap<>();
        final int numDocs = docList.size(); // number of columns

        // build Map version of a term-doc matrix
        for(int i = 0; i < docList.size(); i++) {
            Document d = docList.get(i);
            ArrayList<String> tokens = analyze(d.get("content"));
            // TODO(Andrew) include name
            for (String t : tokens) {
                Data termData = termDocs.get(t);
                if (termData == null)
                    termDocs.put(t, new Data(t, numDocs, i));
                else
                    termData.increment(i);
            }
        }

        final int numTerms = termDocs.size(); // number of rows
        double[][] A = new double[numTerms][numDocs]; // initialize 2D matrix

        System.out.println("Number of terms: " + numTerms);

        double[] termFreq = termDocs.get("int").postings;
        System.out.println("Weight of 'int' in the first doc?  " + termFreq[0]);
        System.out.println("Weight of 'int' in the sec doc?  " + termFreq[1]);
        System.out.println("Weight of 'int' in the third doc?  " + termFreq[2]);
        System.out.println("Weight of 'int' in the fourth doc?  " + termFreq[3]);
        System.out.println("Weight of 'int' in the fifth doc?  " + termFreq[4]);

        // copy values
        int i = 0;
        for(HashMap.Entry<String, Data> entry : termDocs.entrySet()) {
            Data data = entry.getValue();
            data.assignIndex(i); // assign each term an id (row index)
            if(normalize) data.calcTFIDF(); // compute and assign tf-idf weighting to postings
            A[data.indexID] = data.postings; // copy term's postings to term-doc matrix
            i++;
        }
        // apply length normalization to each document
        if(normalize) {
            for (int j = 0; j < numDocs; j++) {
                double squaredSum = 0;
                for (int k = 0; k < numTerms; k++)
                    squaredSum += A[k][j] * A[k][j];

                double norm = Math.sqrt(squaredSum);
                if (norm == 0)
                    continue;
                for (int k = 0; k < numTerms; k++)
                    A[k][j] /= norm;
            }
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
            double[][] tDM = buildTermDocMatrix(docList, true);

            double[][] coMatrix = MatrixUtilities.multiply(tDM, MatrixUtilities.transpose(tDM));
            MatrixUtilities.setDiagonal(coMatrix, 0);

            System.out.println("Matrix");


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
