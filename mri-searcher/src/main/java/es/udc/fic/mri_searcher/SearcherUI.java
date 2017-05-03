package es.udc.fic.mri_searcher;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;

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
	
	//rf
	int rf1Tq = -1;
	int rf1Td = -1;
	int rfNdr = -1;
	//prf
	int nd = -1;
	int nw = -1;
	
	boolean explain = false;
	

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
		while ( (i+1 < args.length) && (!args[i + 1].contains("-"))) {
		    if (args[i + 1].equals("T") || args[i + 1].equals("W")) {
			fieldsproc[counter] = args[i + 1];
			i++;
		    } else {
			System.out.println(
				"The lista-campos in -fieldsproc item must be: T or W");
			print_usage_and_exit();
		    }
		}
		counter = 0;
	    } else if ("-fieldsvisual".equals(args[i])) {
		fieldsvisual = new String[5];
		while ( (i+1 < args.length) && (!args[i + 1].contains("-"))) {
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
		counter = 0;
	    } else if ("-rf1".equals(args[i])){
		rf1Tq = Integer.parseInt(args[i+1]);
		rf1Td = Integer.parseInt(args[i+2]);
		rfNdr = Integer.parseInt(args[i+3]);
		i = i+3;
		
	    } else if ("-rf2".equals(args[i])){
		rfNdr = Integer.parseInt(args[i+1]);
		i++;
	    } else if ("-prfjm".equals(args[i])){
		nd =  Integer.parseInt(args[i+1]);
		nw =  Integer.parseInt(args[i+2]);
		i = i+2;
	    } else if ("-prfdir".equals(args[i])){
		nd =  Integer.parseInt(args[i+1]);
		nw =  Integer.parseInt(args[i+2]);
		i = i+2;
	    } else if ("-explain".equals(args[i])){
		explain = true;
	    }
	}

	//OJO, hay que completar esto para las nuevas opciones
	if ((indexin == null) || (cut < 0) || (top < 0) || (fieldsproc == null)
		|| (fieldsvisual == null)) {
	    print_usage_and_exit();
	}

	SimilarityAndColl simColl = IndexingModelWriter
		.readIndexingModel(indexin, indexingModel);
	//OJO, esto habrá que cambiar en varios dependiendo de lo que entre
	Searcher.run(indexin, simColl, int1, int2, cut, top, fieldsproc,
		fieldsvisual);
    }

    private static void print_usage_and_exit() {
	String usage = "Usage: Searching options [-search default|jm|dir] [-indexin pathname] [-cut n] "
		+ "[-top n] [-queries all|int1|int1-int2] [fieldsproc lista-campos]"
		+ " [fieldsvisual lista-campos] [-rf1 tq td ndr] [-rf2 ndr] "
		+ "[-prfjm nd nw] [-prfdir nd nw] [-explain]\n";
	System.out.println(usage);
	System.exit(0);
    }
}
