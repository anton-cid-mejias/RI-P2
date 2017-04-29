package es.udc.fic.mri_searcher;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.Similarity;

import es.udc.fic.util.IndexingModelWriter;

public class SearcherUI {
    public static void main(String[] args) throws IOException, ParseException {

	if ((args.length == 0) || (args.length > 0
		&& ("-h".equals(args[0]) || "-help".equals(args[0])))) {
	    print_usage_and_exit();
	}

	String indexin = null;
	int cut = -1;
	int top = -1;
	String[] ints;
	int int1 = 0;
	int int2 = 0;
	int counter = 0;
	String[] fieldsproc = null;
	String[] fieldsvisual = null;

	// indexingModel : Null = default, true = jm lambda, false = dir mu
	Boolean indexingModel = null;

	for (int i = 0; i < args.length; i++) {
	    if ("-indexin".equals(args[i])) {
		indexin = args[i + 1];
		i++;
	    } else if ("-cut".equals(args[i])) {
		cut = Integer.parseInt(args[i + 1]);
		i++;
	    } else if ("-top".equals(args[i])) {
		top = Integer.parseInt(args[i + 1]);
		i++;
	    } else if ("-search".equals(args[i])) {
		if ("jm".equals(args[i + 1])) {
		    indexingModel = true;
		    i++;
		} else if ("dir".equals(args[i + 1])) {
		    indexingModel = false;
		    i++;
		} else {
		    i++;
		}
	    } else if ("-queries".equals(args[i])) {
		if ("all".equals(args[i + 1])) {
		    i++;
		} else {
		    if (args[i + 2].contains("-")) {
			ints = args[i + 2].split("-");
			int1 = Integer.parseInt(ints[0]);
			int2 = Integer.parseInt(ints[1]);
			if (int1 < 0) {
			    System.out.println(
				    "The first integer in -queries must be"
					    + " greater than 0");
			    print_usage_and_exit();
			}
			if (int1 > int2) {
			    System.out.println(
				    "The first integer in -queries must be"
					    + " greater than the second");
			    print_usage_and_exit();
			}

		    } else {
			int1 = Integer.parseInt(args[i + 2]);
			if (int1 < 0) {
			    System.out.println(
				    "The first integer in -queries must be"
					    + " greater than 0");
			    print_usage_and_exit();
			}
		    }
		    i++;
		}
	    } else if ("-fieldsproc".equals(args[i])) {
		fieldsproc = new String[2];
		while (!args[i + 1].contains("-")) {
		    if (args[i + 1].equals("T") || args[i + 1].equals("W")) {
			fieldsproc[counter] = args[i + 1];
			i++;
		    } else {
			System.out.println(
				"The lista-campos in -fieldsproc item must be: T or W");
			print_usage_and_exit();
		    }
		}
		i++;
		counter = 0;
	    } else if ("-fieldsvisual".equals(args[i])) {
		fieldsvisual = new String[5];
		while (!args[i + 1].contains("-")) {
		    if (args[i + 1].equals("T") || args[i + 1].equals("I")
			    || args[i + 1].equals("W")
			    || args[i + 1].equals("B")
			    || args[i + 1].equals("A")) {
			fieldsproc[counter] = args[i + 1];
			i++;
		    } else {
			System.out.println(
				"The lista-campos item in -fieldsvisual must be: I,T,B,A or W");
			print_usage_and_exit();
		    }
		}
		i++;
		counter = 0;
	    }
	}

	if ((indexin == null) || (cut > 0) || (top > 0) || (fieldsproc == null)
		|| (fieldsvisual == null)) {
	    print_usage_and_exit();
	}

	SimilarityAndColl simColl = IndexingModelWriter
		.readIndexingModel(indexin, indexingModel);
	Searcher.run(indexin, simColl, int1, int2, cut, top, fieldsproc,
		fieldsvisual);
    }

    private static void print_usage_and_exit() {
	String usage = "Usage: Searching options [-search default|jm|dir] [-indexin pathname] [-cut n] "
		+ "[-top n] [-queries all|int1|int1-int2] [fieldsproc lista-campos]"
		+ " [fieldsvisual lista-campos] \n";
	System.out.println(usage);
	System.exit(0);
    }
}
