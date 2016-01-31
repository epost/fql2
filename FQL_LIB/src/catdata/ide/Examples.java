package catdata.ide;

import java.util.Vector;

import catdata.examples.AllSyntax;
import catdata.examples.CatExample;
import catdata.examples.CoProductExample;
import catdata.examples.DeltaExample;
import catdata.examples.EmployeeExample;
import catdata.examples.ExpExample;
import catdata.examples.IntegrationExample;
import catdata.examples.MonadExample;
import catdata.examples.NistExampl;
import catdata.examples.OplDeltaExample;
import catdata.examples.OplEmployeesExample;
import catdata.examples.OplFlowerExamle;
import catdata.examples.OplGroupExample;
import catdata.examples.OplGroupingExample;
import catdata.examples.OplHornExample;
import catdata.examples.OplJSExample;
import catdata.examples.OplMLExample;
import catdata.examples.OplMod4Example;
import catdata.examples.OplNested2Example;
import catdata.examples.OplNestedExample;
import catdata.examples.OplPicsExample;
import catdata.examples.OplSKExample;
import catdata.examples.OplSigmaExample;
import catdata.examples.OplStackExample;
import catdata.examples.OplTyTest2Example;
import catdata.examples.OplTyTestExample;
import catdata.examples.OplTypedEmployeesExample;
import catdata.examples.OplUnfailingExample;
import catdata.examples.Patrick10Example;
import catdata.examples.Patrick11Example;
import catdata.examples.Patrick12ExampleCoprod;
import catdata.examples.Patrick13ExampleProd;
import catdata.examples.Patrick15ExampleNist;
import catdata.examples.Patrick16ExampleFlower2;
import catdata.examples.Patrick17Example;
import catdata.examples.Patrick18Example;
import catdata.examples.Patrick19ExampleNist2;
import catdata.examples.Patrick20ExampleUber;
import catdata.examples.Patrick23Example;
import catdata.examples.Patrick24ExampleBadUber;
import catdata.examples.Patrick25ExampleCoApply;
import catdata.examples.Patrick26ExampleCata;
import catdata.examples.Patrick27ExampleIntegration;
import catdata.examples.Patrick28ExampleOlog;
import catdata.examples.Patrick29ExampleEpiMono;
import catdata.examples.Patrick2Example;
import catdata.examples.Patrick30ExampleSoed1;
import catdata.examples.Patrick3Example;
import catdata.examples.Patrick4Example;
import catdata.examples.Patrick5ExampleDelta;
import catdata.examples.Patrick6Example;
import catdata.examples.Patrick7Example;
import catdata.examples.Patrick8ExampleAllSyntax;
import catdata.examples.Patrick9Example;
import catdata.examples.PatrickExample;
import catdata.examples.PiExample;
import catdata.examples.ProductsExample;
import catdata.examples.PropExample;
import catdata.examples.SetsExample;
import catdata.examples.SigmaExample;
import catdata.examples.VikExample;

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
	public static Example patrick29 = new Patrick29ExampleEpiMono();
	public static Example patrick30 = new Patrick30ExampleSoed1();
	
	public static Example mod4 = new OplMod4Example();
	public static Example js = new OplJSExample();
	public static Example sk = new OplSKExample();
	public static Example oplDelta = new OplDeltaExample();
	public static Example oplStack = new OplStackExample();
	public static Example oplGroup = new OplGroupExample();
	public static Example oplEmployees = new OplEmployeesExample();
	public static Example oplSigma = new OplSigmaExample();
	public static Example oplUnfailing = new OplUnfailingExample();
	//public static Example oplTyped = new OplTypedExample();
	public static Example oplFlower = new OplFlowerExamle();
	public static Example oplTypedEmployees = new OplTypedEmployeesExample();
	public static Example oplPics = new OplPicsExample();
	public static Example oplTyTest = new OplTyTestExample();
	public static Example oplTyTest2 = new OplTyTest2Example();
	public static Example oplNested = new OplNestedExample();
	public static Example oplNested2 = new OplNested2Example();
	public static Example oplML = new OplMLExample();
	public static Example oplGrouping = new OplGroupingExample();
	public static Example oplHorn = new OplHornExample();
	
	public static Example[] examples = new Example[] {oplHorn, oplGrouping, oplML, oplNested2, oplNested, oplTyTest2, /*oplTyTest,*/ oplPics, oplTypedEmployees, oplFlower, /*oplTyped,*/ oplUnfailing, oplSigma, oplEmployees, oplGroup, patrick30, oplStack, oplDelta, sk, js, mod4, patrick29, patrick28, patrick27, patrick26, patrick25, patrick24, patrick23, /*patrick22, patrick21,*/ patrick20, patrick19, patrick18, patrick17, patrick16, patrick15, /* patrick14, */ patrick13, patrick12, patrick11, patrick10, patrick9, patrick8, patrick, patrick2, patrick3, patrick4, patrick5, patrick6, patrick7, all, vik,  /*nist2, */ nistExample, propExample, expExample, piExample, sigmaExample, deltaExample, integrationExample, coproductsExample, productsExample, employeeExample, setExample, catExample, monadExample };
		
	//public static Example[] key_examples = examples; //new Example[] { deltaExample, integrationExample, coproductsExample, productsExample, employeeExample, setExample, catExample, monadExample };

	public static final String helpString = ""; //TODO

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static Vector filterBy(String string) {
		Vector ret = new Vector();
		for (Example e : examples) {
			if (e.lang().toString().equals(string) || string.equals("All")) {
				ret.add(e);
			}
		}
		return ret;
	}
}

