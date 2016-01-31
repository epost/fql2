package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class Patrick28ExampleOlog extends Example {
	
	@Override
	public Language lang() {
		return Language.FPQL;
	}


	@Override
	public String getName() {
	return "Olog";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "S = schema {"
			+ "\n	nodes \"arginine\", \"an electrically-charged side chain\", \"a side chain\", "
			+ "\n		 \"an amino acid found in dairy\", \"an amino acid\", \"an amine group\","
			+ "\n		 \"a carboxylic acid\";"
			+ "\n	edges \"is \" : \"arginine\" -> \"an amino acid found in dairy\","
			+ "\n	      \"is\" : \"an amino acid found in dairy\" -> \"an amino acid\","
			+ "\n	      \"is  \" : \"arginine\" -> \"an amino acid\","
			+ "\n	      \"has\" : \"arginine\" -> \"an electrically-charged side chain\","
			+ "\n	      \"has   \" : \"an amino acid\" -> \"a side chain\","
			+ "\n	      \"is   \" : \"an electrically-charged side chain\" -> \"a side chain\","
			+ "\n	      \"has \" : \"an amino acid\" -> \"a carboxylic acid\","
			+ "\n	      \"has  \" : \"an amino acid\" -> \"an amine group\";"
			+ "\n	equations"
			+ "\n	\"is \".\"is\" = \"is  \", \"has\".\"is   \" = \"is  \".\"has   \";"
			+ "\n}"
			+ "\n";





}
