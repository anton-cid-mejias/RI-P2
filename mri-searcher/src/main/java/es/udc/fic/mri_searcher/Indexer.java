package es.udc.fic.mri_searcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

    public static void run(OpenMode openmode, String index, String coll,
	    Similarity similarity) throws IOException {
	try {
	    System.out.println("Indexing to directory '" + index + "'...");
	    
	    Directory dir = FSDirectory.open(Paths.get(index));
	    Analyzer analyzer = new StandardAnalyzer();
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

	    iwc.setOpenMode(openmode);
	    iwc.setSimilarity(similarity);

	    IndexWriter writer = new IndexWriter(dir, iwc);

	    
	    indexDocs(writer, Paths.get(coll));
	    writer.close();
	} catch (IOException e) {
	    System.out.println(" caught a " + e.getClass() + "\n with message: "
		    + e.getMessage());
	}
    }

    private static void indexDocs(final IndexWriter writer, Path path)
	    throws IOException {
	if (Files.isDirectory(path)) {
	    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
		@Override
		public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) throws IOException {
		    try {
			if (checkCranCollection(file)) {
			    indexDoc(writer, file,
				    attrs.lastModifiedTime().toMillis());
			    return FileVisitResult.TERMINATE;
			}
		    } catch (IOException ignore) {
			// don't index files that can't be read.
		    }
		    return FileVisitResult.CONTINUE;
		}
	    });
	} else {
	    if (checkCranCollection(path)) {
		indexDoc(writer, path,
			Files.getLastModifiedTime(path).toMillis());
	    }
	}
    }

    private static void indexDoc(IndexWriter writer, Path file,
	    long lastModified) throws IOException {
	
	try (InputStream stream = Files.newInputStream(file)) {
	    List<List<String>> allCran = CranParser
		    .parseString(new StringBuffer(toString(stream)));

	    for (List<String> cran : allCran) {
		Document doc = new Document();
		int counterT = 0;
		int counterW = 0;
		
		// I
		Field I = new StringField("I", cran.get(0),
			Field.Store.YES);
		doc.add(I);

		// T
		Field T = new TextField("T", cran.get(1),
			Field.Store.YES);
		doc.add(T);
		
		//Token counter of T
		TokenStream tokens = writer.getAnalyzer().tokenStream("T", T.stringValue());
		tokens.reset();
		while (tokens.incrementToken()){
		    counterT++;
		}
		tokens.close();
		Field TT = new StringField("TTokens", String.valueOf(counterT),
			Field.Store.YES);
		doc.add(TT);
		
		// A
		Field A = new TextField("A", cran.get(2),
			Field.Store.YES);
		doc.add(A);

		// B
		Field B = new TextField("B", cran.get(3), Field.Store.YES);
		doc.add(B);

		// W
		Field W = new TextField("W", cran.get(4),
			Field.Store.YES);
		doc.add(W);
		
		//Token counter of W
		tokens = writer.getAnalyzer().tokenStream("W", W.stringValue());
		tokens.reset();
		while (tokens.incrementToken()){
		    counterW++;
		}
		tokens.close();
		Field TW = new StringField("WTokens", String.valueOf(counterW),
			Field.Store.YES);
		doc.add(TW);
		
		writer.addDocument(doc);

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

    private static boolean checkCranCollection(Path file) {
	String fileName = file.toString();
	int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
	fileName= fileName.substring(p + 1);
	return "cran.all.1400".equals(fileName);
    }

}
