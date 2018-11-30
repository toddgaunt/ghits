package Team1;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.File;
import java.nio.file.Files;
import java.util.Vector;

public class ThesaurusBuilder {
    final static String[] valid_extensions = {".java", ".c", ".h", ".py"};
    /**
     * Build a list of Documents from a directory.
     * @param folder
     * @return All the docs in a repo
     * @throws Exception
     */
    public static void buildDocs(File folder, Vector<Document> docList, String rootName) throws Exception {
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



    public static void main(String[] args) {

        ThesaurusBuilder thesaurusBuilder = new ThesaurusBuilder();

        Vector<Document> docList = new Vector<Document>();
        try {
            buildDocs(new File("sway-master"), docList, "sway-master");
            System.out.println(docList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
