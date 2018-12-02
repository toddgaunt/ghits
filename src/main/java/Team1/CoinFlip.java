package Team1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;

class GithubPullRequest
{
	String query;
	String[] file_path;
	
	GithubPullRequest(int size) {
		file_path = new String[size];
	}
}

public class CoinFlip
{
	public static Map<String, List<String>> train(GithubPullRequest[] pr, DirectoryReader dr) throws Exception {
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		Set<String> query_language = new HashSet<String>();
		Set<String> corpus_language = new HashSet<String>();
		for (GithubPullRequest p : pr) {
			for (String s : App.tokenize(p.query)) {
				query_language.add(normalize(s));
			}
		}
		for (int i = 0; i < dr.numDocs(); ++i) {
			for (String s : App.tokenize(dr.document(i).get("content"))) {
				corpus_language.add(normalize(s));
			}
		}
		// Convert the corpus lanugage to an array to apply a random access mapping
		String[] corpus_language_random_access = corpus_language.toArray(new String[corpus_language.size()]);
		Random rand = new Random(System.currentTimeMillis());
		// Randomly assign query_language terms to corpus_language terms
		for (String word : query_language) {
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(corpus_language_random_access[rand.nextInt(corpus_language_random_access.length)]);
			map.put(word, tmp);
		}
		//System.out.println(query_language);
		//System.out.println(corpus_language);
		//System.out.println(map);
		return map;
	}

	public String transform(String query) {
		return "";
	}
	
	public static void write_mapping(JSONObject mapping, String file_path) throws Exception {
		FileWriter file = new FileWriter(file_path);
        file.write(mapping.toString());
        file.flush();
	}
	
	public static String normalize(String query)
	{
	    	String normalized = Normalizer.normalize(query, Form.NFD);
	    	return normalized.replaceAll("[^A-Za-z0-9_\\-+ ]", "");
	}
	
	public static GithubPullRequest[] read_training_set(String file_path) throws Exception {
		JSONParser parser = new JSONParser();
        JSONObject jsonFile = (JSONObject) parser.parse(new FileReader(file_path));
		ArrayList<GithubPullRequest> pr = new ArrayList<GithubPullRequest>();

        for (Object i : jsonFile.entrySet()) {
        	Map.Entry<String, Object> issueAndCode = (Map.Entry) i;
            Set<String> filesOfIssue = ((HashMap<String, Integer>) issueAndCode.getValue()).keySet();
            String[] file_paths = filesOfIssue.toArray(new String[filesOfIssue.size()]);
            
            GithubPullRequest tmp = new GithubPullRequest(filesOfIssue.size());
            tmp.query = issueAndCode.getKey();
            tmp.file_path = file_paths;
        	//System.out.println(tmp.query);
        	//System.out.println("------");
        	//System.out.println(filesOfIssue);
        	pr.add(tmp);
        }
		return (GithubPullRequest[]) pr.toArray(new GithubPullRequest[pr.size()]);
	}
	
	public static void main(String[] args) {
		try {
			File index = new File("index");
			DirectoryReader dr = DirectoryReader.open(FSDirectory.open(index.toPath()));
			GithubPullRequest[] pr = read_training_set("train.json");
			Map<String, List<String>> map = train(pr, dr);
			JSONObject mapping = new JSONObject(map);
			write_mapping(mapping, "coinflip_mapping.json");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}