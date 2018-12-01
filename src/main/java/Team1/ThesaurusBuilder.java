package Team1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
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
    final public static String[] valid_extensions = {".java", ".c", ".h", ".py"};
    /**
     * Build a list of Documents from a directory.
     * @param folder
     * @return All the docs in a repo
     * @throws Exception
     */
    private static void buildDocs(File folder, HashMap<String,Document> docList, String rootName) throws Exception {
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
                        docList.put(name, doc);
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
     * @param normalize
     * @return
     */
    private static double[][] buildTermDocMatrix(ArrayList<Document> docList, boolean normalize,
                                                 boolean termsFromIssue) throws Exception {
        HashMap<String, Data> termDocs = new HashMap<>();
        final int numDocs = docList.size(); // number of columns

        // build Map version of a term-doc matrix
        for(int i = 0; i < docList.size(); i++) {
            Document d = docList.get(i);
            ArrayList<String> tokens = new ArrayList();

            if(termsFromIssue)
                tokens = analyze(d.get("issue"));
            else
                tokens = analyze(d.get("code"));

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

    private static void buildIssueCodeDocs(HashMap<String,Document> docList,
                                           ArrayList<Document> issueCodeDocs, String trainJsonFile) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject jsonFile = (JSONObject) parser.parse(new FileReader(trainJsonFile));

        // For each issue concatenate all code
        for(Object o : jsonFile.entrySet()) {
            Map.Entry<String, Object> issueAndCode = (Map.Entry) o;
            Set<String> filesOfIssue = ((HashMap<String, Integer>) issueAndCode.getValue()).keySet();

            String code = "";
            for(String filename : filesOfIssue) {
                Document file =  docList.get(filename);
                if(file != null) // Append code if this file is in our docList
                    code += file.get("content") + '\n';
            }

            // Only create and add doc if we had code
            if(code.length() != 0) {
                Document issueCodeDoc = new Document();
                issueCodeDoc.add(new TextField("issue", issueAndCode.getKey(), Field.Store.YES));
                issueCodeDoc.add(new TextField("code", code, Field.Store.YES));
                issueCodeDocs.add(issueCodeDoc);
            }
        }

        System.out.println(issueCodeDocs.size());

    }

    public static void main(String[] args) {

        final String repoName = "sway-master";
        final String trainJson = "bin/train.json"; // path to pull requests (issue paired with files)

        try {
            // Recursively build our list of docs from repo
            HashMap<String,Document> docList = new HashMap<>();
            buildDocs(new File(repoName), docList, repoName);
            System.out.println("Number of docs: " + docList.size());

            ArrayList<Document> issueCodeDocs = new ArrayList<>();
            buildIssueCodeDocs(docList, issueCodeDocs, trainJson);

            // With all the documents, we build term-document matrix
            // Each document has an index (or columnID)
            // Each term has an index (or rowID)
            // Matrix A dimensions = #tokens X #docs
            double[][] issueTDM = buildTermDocMatrix(issueCodeDocs, false, true);
            double[][] codeTDM = buildTermDocMatrix(issueCodeDocs, false, false);

            double[][] coMatrix = MatrixUtilities.multiply(issueTDM, MatrixUtilities.transpose(codeTDM));
//            MatrixUtilities.setDiagonal(coMatrix, 0);

            System.out.println("Co-occurrence Matrix: " + coMatrix.length + " X " + coMatrix[0].length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
