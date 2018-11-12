package Team1;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;

public class LM {
	public static LMSimilarity U_L(long number_of_terms) {
		return new LMSimilarity() {
			@Override
			public String getName() {
				return "U-L";
			}

			@Override
			protected float score(BasicStats stats, float freq, float docLen) {
				long tf = stats.getTotalTermFreq();
				return (freq + 1) / (tf + number_of_terms);
			}
		};
	}
}