package es.udc.fic.mri_searcher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CranRelParser {
	
    public static List<QueryNumberRelevanceDoc> parseString(StringBuffer fileContent) {

	String text = fileContent.toString();
	String[] lines = text.split("\n");
	String[] numbers;
	QueryNumberRelevanceDoc data;
	int queryNumber;

	List<QueryNumberRelevanceDoc> documents = new LinkedList<>();
	List<Integer> docs = new ArrayList<>();

	numbers = lines[0].split(" ");
	queryNumber = Integer.parseInt(numbers[0]);
	docs.add(Integer.parseInt(numbers[1]));
	for (int i = 1; i < lines.length; ++i) {
	    numbers = lines[0].split(" ");
	    if (Integer.parseInt(numbers[0])==queryNumber){
		docs.add(Integer.parseInt(numbers[1]));
	    }else{
		data = new QueryNumberRelevanceDoc(queryNumber,docs);
		documents.add(data);
		docs.clear();
		queryNumber = Integer.parseInt(numbers[0]);
		docs.add(Integer.parseInt(numbers[1]));
	    }
	}
	return documents;
    }
}