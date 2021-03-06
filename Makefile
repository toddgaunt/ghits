# This is simply a wrapper around mvn to aid muscle memory for make commands

all:
	mvn compile

clean:
	mvn clean
	rm -rf index/

run: all
	mvn exec:java -Dexec.mainClass="Team1.App" -Dexec.args="$(ARGS)"

coinflip: all
	mvn exec:java -Dexec.mainClass="Team1.CoinFlip" -Dexec.args="$(ARGS)"

thesaurus: all
	mvn exec:java -Dexec.mainClass="Team1.ThesaurusBuilder" -Dexec.args="$(ARGS)"

index: all
	rm -rf index/
	mvn exec:java -Dexec.mainClass="Team1.Indexer" -Dexec.args="$(ARGS)"
