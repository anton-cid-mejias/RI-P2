package es.udc.fic.mri_searcher;

import java.io.IOException;

public class SearcherUI {
    public static void main(String[] args) throws IOException {

	if ((args.length == 0) || (args.length > 0
		&& ("-h".equals(args[0]) || "-help".equals(args[0])))) {
	    print_usage_and_exit();
	}

	String indexin = null;
	int cut;
	int top;
	String[] ints;
	int int1 = 0;
	int int2 = 0;
	int counter = 0;
	String[] fieldsproc = new String[2];
	String[] fieldsvisual = new String[5];
	
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
		    if (args[i+2].contains("-")){
			ints = args[i+2].split("-");
			int1 = Integer.parseInt(ints[0]);
			int2 = Integer.parseInt(ints[1]);
		    }else{
			int1 = Integer.parseInt(args[i+2]);
		    }
		    i++;
		}
	    } else if ("-fieldsproc".equals(args[i])){
		while (!args[i+1].contains("-")){
		    fieldsproc[counter] = args[i+1];
		    i++;
		}
		counter = 0;
	    } else if ("-fieldsvisual".equals(args[i])){
		while (!args[i+1].contains("-")){
		    fieldsvisual[counter] = args[i+1];
		    i++;
		}
		counter = 0;
	    }
	}
    }

    private static void print_usage_and_exit() {
	String usage = "Usage: Indexing options [-openmode openmode] [-index pathname] [-coll pathname] "
		+ "[-indexingmodel default| jm lambda | dir mu] \n";
	System.out.println(usage);
	System.exit(0);
    }
}
