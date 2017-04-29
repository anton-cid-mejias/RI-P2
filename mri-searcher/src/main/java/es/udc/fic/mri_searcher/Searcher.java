package es.udc.fic.mri_searcher;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {

    //
    public void run(String index, Similarity similarity,
	    String queries, int int1, int int2, String cut, String top,
	    List<String> fieldsProcs, List<String> fieldsVisual) throws IOException {
	
	Directory indir = FSDirectory.open(Paths.get(index));
	DirectoryReader reader = DirectoryReader.open(indir);
	IndexSearcher searcher = new IndexSearcher(reader);
	searcher.setSimilarity(similarity);
	
	
	
	//Parses T or W or both
	MultiFieldQueryParser parser = new MultiFieldQueryParser(
		    (String[]) fieldsProcs.toArray(), new StandardAnalyzer());
	
    }
}
