package Team1;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
	public void train(GithubPullRequest[] pr) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < pr.length; ++i) {
		}
	}

	public String transform(String query) {
		return "";
	}
	
	public static void write_mapping(String file_path) {
	
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
        	System.out.println(tmp.query);
        	System.out.println("------");
        	System.out.println(filesOfIssue);
        	pr.add(tmp);
        }
		return (GithubPullRequest[]) pr.toArray(new GithubPullRequest[pr.size()]);
	}
	
	public static void main(String[] args) {
		try {
			GithubPullRequest[] pr = read_training_set("train.json");
			//write_mapping("coinflip_mapping.json");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}