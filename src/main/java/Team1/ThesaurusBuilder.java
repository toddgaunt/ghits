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
                        System.out.println(name);
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

    public static ArrayList<String> buildTerms(ArrayList<Document> docList) {
        HashSet<String> set = new HashSet<String>();

        for(Document d : docList) {
            try {
                set.addAll(analyze(d.get("content")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<String>(set);
    }


    public static void main(String[] args) {

        ThesaurusBuilder thesaurusBuilder = new ThesaurusBuilder();

        ArrayList<Document> docList = new ArrayList<Document>();
        ArrayList<String> termList = new ArrayList<String>();

        try {
            // Recursively build our list of docs
            buildDocs(new File("sway-master"), docList, "sway-master");
            System.out.println("Number of docs: " + docList.size());

            // Analyze each doc to strip out terms into list
            termList = buildTerms(docList);
            System.out.println("Number of terms: " + termList.size());

            // With all the documents, we build term-document matrix
            // Each document has an index (or columnID)
            // Each term has an index (or rowID)
            // Matrix A = #docs X #tokens


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
