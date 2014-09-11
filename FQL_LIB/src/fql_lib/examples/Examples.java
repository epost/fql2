package fql_lib.examples;

public class Examples {

	public static Example setExample = new SetsExample();
	public static Example catExample = new CatExample();
	public static Example employeeExample = new EmployeeExample();
	public static Example monadExample = new MonadExample();
	public static Example productsExample = new ProductsExample();	
	public static Example coproductsExample = new CoProductExample();
	public static Example integrationExample = new IntegrationExample();
	public static Example deltaExample = new DeltaExample();
	public static Example sigmaExample = new SigmaExample();
	public static Example piExample = new PiExample();
	public static Example expExample = new ExpExample();
	public static Example propExample = new PropExample();
	public static Example nistExample = new NistExampl();
	//public static Example nist2 = new Nist2(); 
	public static Example vik = new VikExample();
	public static Example all = new AllSyntax();
	public static Example patrick = new PatrickExample();
	public static Example patrick2 = new Patrick2Example();
	public static Example patrick3 = new Patrick3Example();
	public static Example patrick4 = new Patrick4Example();
	
	public static Example[] examples = new Example[] {patrick, patrick2, patrick3, patrick4, all, vik,  /*nist2, */ nistExample, propExample, expExample, piExample, sigmaExample, deltaExample, integrationExample, coproductsExample, productsExample, employeeExample, setExample, catExample, monadExample };
	
	public static Example[] key_examples = examples; //new Example[] { deltaExample, integrationExample, coproductsExample, productsExample, employeeExample, setExample, catExample, monadExample };

	public static final String helpString = ""; //TODO

}

