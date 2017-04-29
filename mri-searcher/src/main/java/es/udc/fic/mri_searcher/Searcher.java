package es.udc.fic.mri_searcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {

    //
    public static void run(String index, SimilarityAndColl simColl, int int1,
	    int int2, int cut, int top, String[] fieldsProcs,
	    String[] fieldsVisual) throws IOException, ParseException {

	Directory indir = FSDirectory.open(Paths.get(index));
	DirectoryReader reader = DirectoryReader.open(indir);
	IndexSearcher searcher = new IndexSearcher(reader);
	searcher.setSimilarity(simColl.getSimilarity());

	Path path = Paths.get(simColl.getColl(), "cran.qry");
	InputStream stream = Files.newInputStream(path);
	List<List<String>> queries = CranQueryParser
		.parseString(new StringBuffer(toString(stream)));

	path = Paths.get(simColl.getColl(), "cranqrel");
	stream = Files.newInputStream(path);
	List<QueryNumberRelevanceDoc> queriesRelevance = CranRelParser
		.parseString(new StringBuffer(toString(stream)));

	int lastQuery = Integer
		.parseInt(queries.get(queries.size() - 1).get(0));
	if (int1 == 0) {
	    int1 = 1;
	    int2 = lastQuery;
	} else if (int2 == 0) {
	    int2 = int1;
	} else {
	    if (int1 > lastQuery || int2 > lastQuery) {
		System.out.println("Last query number is " + lastQuery
			+ " use a different range of queries");
		System.exit(1);
	    }
	}

	// Parses T or W or both
	MultiFieldQueryParser parser = new MultiFieldQueryParser(fieldsProcs,
		new StandardAnalyzer());

	Iterator<List<String>> itr = queries.iterator();
	List<String> actualQuery = itr.next();
	int actualQueryIndex = Integer.parseInt(actualQuery.get(0));
	Query query = null;
	TopDocs topDocs = null;
	int hits = 0;
	List<ScoreDoc> scoreDocs = null;
	for (int i = int1; i < int2; i++) {
	    if (i == actualQueryIndex) {
		query = parser.parse(actualQuery.get(1));
		topDocs = searcher.search(query, top);
		hits = topDocs.totalHits;
		scoreDocs = Arrays.asList(topDocs.scoreDocs);
		
		
		//Llamar a función de Antón
		/*Imprimir con una función:
		 * query, el top n de documentos, y para cada documento se
		 * visualizarán todos los campos indicados en el argumento
		 * fields (opción fieldsvisual), el score del documento y una
		 * marca que diga si es relevante según los juicios de
		 * relevancia, y las métricas individuales para cada query.
		 * Finalmente se mostrarán las métricas promediadas
		 */

		actualQuery = itr.next();
		actualQueryIndex = Integer.parseInt(actualQuery.get(0));
	    }
	}

    }

    private static String toString(InputStream stream) throws IOException {
	ByteArrayOutputStream result = new ByteArrayOutputStream();
	byte[] buffer = new byte[1024];
	int length;
	while ((length = stream.read(buffer)) != -1) {
	    result.write(buffer, 0, length);
	}
	return result.toString("UTF-8");
    }
}
