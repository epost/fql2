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
	public static Example patrick5 = new Patrick5ExampleDelta();
	public static Example patrick6 = new Patrick6Example();
	public static Example patrick7 = new Patrick7Example();
	public static Example patrick8 = new Patrick8ExampleAllSyntax();
	public static Example patrick9 = new Patrick9Example();
	public static Example patrick10=new Patrick10Example();
	public static Example patrick11=new Patrick11Example();
	public static Example patrick12=new Patrick12ExampleCoprod();
	public static Example patrick13=new Patrick13ExampleProd();
//	public static Example patrick14=new Patrick14ExampleFlower1();
	public static Example patrick15=new Patrick15ExampleNist();
	public static Example patrick16=new Patrick16ExampleFlower2();
	public static Example patrick17=new Patrick17Example();
	public static Example patrick18=new Patrick18Example();
	public static Example patrick19=new Patrick19ExampleNist2();
	public static Example patrick20=new Patrick20ExampleUber();
//	public static Example patrick21=new Patrick21Example();
//	public static Example patrick22=new Patrick22Example();
	public static Example patrick23=new Patrick23Example();
	public static Example patrick24=new Patrick24ExampleBadUber();
	public static Example patrick25=new Patrick25ExampleCoApply();
	public static Example patrick26=new Patrick26ExampleCata();
	public static Example patrick27= new Patrick27ExampleIntegration();
	public static Example patrick28 = new Patrick28ExampleOlog();
	
	public static Example[] examples = new Example[] {patrick28, patrick27, patrick26, patrick25, patrick24, patrick23, /*patrick22, patrick21,*/ patrick20, patrick19, patrick18, patrick17, patrick16, patrick15, /* patrick14, */ patrick13, patrick12, patrick11, patrick10, patrick9, patrick8, patrick, patrick2, patrick3, patrick4, patrick5, patrick6, patrick7, all, vik,  /*nist2, */ nistExample, propExample, expExample, piExample, sigmaExample, deltaExample, integrationExample, coproductsExample, productsExample, employeeExample, setExample, catExample, monadExample };
	
	public static Example[] key_examples = examples; //new Example[] { deltaExample, integrationExample, coproductsExample, productsExample, employeeExample, setExample, catExample, monadExample };

	public static final String helpString = ""; //TODO

}

