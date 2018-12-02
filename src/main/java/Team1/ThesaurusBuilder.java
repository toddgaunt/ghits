package Team1;

import org.apache.commons.io.FilenameUtils;
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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ThesaurusBuilder {
    public static final String[] valid_extensions = new String[] {"java", "c", "h", "py"};
    public static final Set<String> extentions = new HashSet<>(Arrays.asList(valid_extensions));
    public static double threshold = 0.05;
    private static String repoName = "sway-master";
    private static String trainJson = "bin/train.json"; // path to pull requests (issue paired with files)
    private static int numOfSynonyms = 3;
    private static boolean normalizeFlag = true;
    private static Analyzer analyzer = App.analyzer;
    private static TermData[] issueTermArray;
    private static TermData[] codeTermArray;

    public static void main(String[] args) {
        if(args.length == 1) // usage: thesaurus <train.json>
            trainJson = args[0];

        try {
            double[][] coMatrix = buildCoMatrix();
            System.out.println("Co-occurrence Matrix: " + coMatrix.length + " X " + coMatrix[0].length);

            mapSynonyms(coMatrix);
//            issueTermArray[0].printSynonyms();
//            issueTermArray[1].printSynonyms();
            generateThesaurus("thesaurus.json", false);
            generateThesaurus("thesaurus-withweights.json", true);
            System.out.println("thesaurus.json was built successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Build the cooccurrence matrix.
     * @return double[][] cooccurrence matrix
     * @throws Exception
     */
    private static double[][] buildCoMatrix() throws Exception {
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
        HashMap<String, TermData> issueTerms = new HashMap<>();
        HashMap<String, TermData> codeTerms = new HashMap<>();
        double[][] issueTDM = buildTermDocMatrix(issueCodeDocs, issueTerms, true);
        double[][] codeTDM = buildTermDocMatrix(issueCodeDocs, codeTerms, false);
        return MatrixUtilities.multiply(issueTDM, MatrixUtilities.transpose(codeTDM));
    }

    /**
     * Build a list of Documents from a directory.
     * @param folder
     * @return All the docs in a repo
     * @throws Exception
     */
    private static void buildDocs(File folder, HashMap<String,Document> docList, String rootName) throws Exception {
        for (File entry : folder.listFiles()) {
            String filename = entry.getName();
            if (entry.isDirectory())
                buildDocs(entry, docList, rootName);
            else if (extentions.contains(FilenameUtils.getExtension(filename))) {
                String path = folder.getPath().substring(rootName.length()).replace('\\', '/') + "/";
                String name = "a" + path + filename;
                String fileContent = new String(Files.readAllBytes(entry.toPath()));
                if (fileContent.length() == 0) continue;
                Document doc = new Document();
                doc.add(new StringField("name", name, Field.Store.YES));
                doc.add(new TextField("content", fileContent, Field.Store.YES));
                docList.put(name, doc);
            }
        }
    }

    /**
     * Build documents from issue+code.
     * @param docList Collection of code documents from repository.
     * @param issueCodeDocs List to be populated with issue+code Documents.
     * @param trainJsonFile Path to json file with ground truth data for issue -> files.
     * @throws Exception
     */
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

        System.out.println("Number of issue+code Documents: " + issueCodeDocs.size());
    }

    /**
     * Analyze given text and strip out terms into list.
     * @param text
     * @return
     * @throws IOException
     */
    public static ArrayList<String> analyze(String text) throws IOException {
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
     * Build term-doc matrix from a list of documents.
     * @param docList
     * @param termDataMap
     * @param termsFromIssue
     * @return
     * @throws Exception
     */
    private static double[][] buildTermDocMatrix(ArrayList<Document> docList, HashMap<String, TermData> termDataMap,
                                                 boolean termsFromIssue) throws Exception {
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
                TermData termData = termDataMap.get(t);
                if (termData == null)
                    termDataMap.put(t, new TermData(t, numDocs, i));
                else
                    termData.increment(i);
            }
        }

        final int numTerms = termDataMap.size(); // number of rows
        double[][] A = new double[numTerms][numDocs]; // initialize 2D matrix

        System.out.println("Number of terms: " + numTerms);
        if(termsFromIssue) issueTermArray = new TermData[numTerms];
        else codeTermArray = new TermData[numTerms];
        int i = 0;
        for(HashMap.Entry<String, TermData> entry : termDataMap.entrySet()) {
            TermData data = entry.getValue();
            data.assignIndex(i); // assign each term an id (row index)
            if(normalizeFlag) data.calcTFIDF(); // compute and assign tf-idf weighting to postings
            if(termsFromIssue) issueTermArray[i] = data;
            else codeTermArray[i] = data;
            A[data.indexID] = data.postings; // copy term's postings to term-doc matrix
            i++;
        }

        // apply length normalization to each document
        if(normalizeFlag) {
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

    /**
     * Map synonyms using coMatrix.
     * @param coMatrix
     */
    private static void mapSynonyms(double[][] coMatrix) {
        int numIssueTerms = coMatrix.length;
        int numCodeTerms = coMatrix[0].length;

        for (int i = 0; i < numIssueTerms; i++) {
            PriorityQueue<TermWeight> termWeights = new PriorityQueue<>();
            ArrayList<TermWeight> syns = issueTermArray[i].synonyms;

            for (int j = 0; j < numCodeTerms; j++)
                termWeights.add(new TermWeight(codeTermArray[j].term, coMatrix[i][j]));

            for (TermWeight tw = termWeights.poll(); tw.val > threshold && syns.size() < 3; tw = termWeights.poll())
                syns.add(tw);
        }
    }

    /**
     * Generate a json thesaurus file from issueTermArray (w1 -> issue+code -> w2).
     * @param filename
     * @param includeWeights boolean Include weights for each synonym to view in json file.
     * @throws Exception
     */
    private static void generateThesaurus(String filename, boolean includeWeights) throws Exception{
        JSONObject map = new JSONObject();

        for (int i = 0; i < issueTermArray.length; i++) {
            TermData td = issueTermArray[i];
            if(td.synonyms.isEmpty()) continue;

            JSONArray list = new JSONArray();
            for (TermWeight tw : td.synonyms) {
                if(includeWeights) {
                    JSONObject synonym = new JSONObject();
                    synonym.put(tw.term, tw.val);
                    list.add(synonym);
                }
                else
                    list.add(tw.term);
            }
            map.put(td.term, list);
        }

        FileWriter file = new FileWriter(filename);
        file.write(map.toJSONString());
        file.flush();
    }
}

/**
 * Class to represent the data of a term. Simplifies building term-doc matrix from Map.
 */
class TermData {
    double[] postings;
    ArrayList<TermWeight> synonyms;
    int indexID;
    String term;
    TermData(String t, int nDocs, int docIndex) {
        term = t;
        postings = new double[nDocs];
        synonyms = new ArrayList<>();
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
    void printSynonyms() {
        String str = "Synonyms for " + term + '\n';
        for (int i = 0; i < synonyms.size(); i++)
            str += synonyms.get(i).term + " : " + synonyms.get(i).val + '\n';
        System.out.println(str);
    }
}

/**
 * Class to represent a term with its weight.
 */
class TermWeight implements Comparable<TermWeight> {
    double val;
    String term;
    public TermWeight(String t, double v) {
        term = t;
        val = v;
    }
    @Override
    public int compareTo(TermWeight other) {
        if(val > other.val)
            return -1;
        else if(val < other.val)
            return 1;
        return 0;
    }
}