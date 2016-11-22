package catdata.fql;

import catdata.fql.examples.AutoIsoExample;
import catdata.fql.examples.CategoryExample;
import catdata.fql.examples.CharExample;
import catdata.fql.examples.CoProdExample;
import catdata.fql.examples.CyclicExample;
import catdata.fql.examples.DeltaExample;
import catdata.fql.examples.DihedralExample;
import catdata.fql.examples.EDsToFqlExample;
import catdata.fql.examples.EmployeesExample;
import catdata.fql.examples.EmptyExample;
import catdata.fql.examples.EnumsExample;
import catdata.fql.examples.ExponentialExample;
import catdata.fql.examples.Exponentials2Example;
import catdata.fql.examples.ExternalExample;
import catdata.fql.examples.FnCompExample;
import catdata.fql.examples.FullSigmaExample;
import catdata.fql.examples.FullSigmaExample2;
import catdata.fql.examples.GeneExample;
import catdata.fql.examples.GraphExample;
import catdata.fql.examples.ImageExample;
import catdata.fql.examples.IsoExample;
import catdata.fql.examples.MatchExample;
import catdata.fql.examples.MatchExample2;
import catdata.fql.examples.NistContExample;
import catdata.fql.examples.NistExample;
import catdata.fql.examples.PeopleExample;
import catdata.fql.examples.ProductExample;
import catdata.fql.examples.PropExample;
import catdata.fql.examples.RelationalizerExample;
import catdata.fql.examples.SqlToFqlExample;
import catdata.fql.examples.SubSchemaExample;
import catdata.fql.examples.SurjectiveExample;
import catdata.fql.examples.TransformExample;
import catdata.fql.examples.Transforms2Example;
import catdata.fql.examples.TriangleExample;
import catdata.fql.examples.TypedCompositionExample;
import catdata.fql.examples.TypedEmployeesExample;
import catdata.fql.examples.TypedFoilExample;
import catdata.fql.examples.TypedPiExample;
import catdata.fql.examples.TypedSigmaExample;
import catdata.fql.examples.TypedWeirdExample;
import catdata.fql.examples.WrittenExample;
import catdata.ide.Example;


/**
 * 
 * @author ryan
 * 
 */
public class FqlExamples {

	public static Example exp = new ExponentialExample();
	public static Example graph = new GraphExample();
	public static Example plusSig = new CoProdExample();
	public static Example full = new FullSigmaExample();
	public static Example full2 = new FullSigmaExample2();
	public static Example external = new ExternalExample();
	public static Example people = new PeopleExample();
	public static Example typedFoil = new TypedFoilExample();
	public static Example typedPi = new TypedPiExample();
	public static Example typedWeird = new TypedWeirdExample();
	public static Example sql2fql = new SqlToFqlExample();
	public static Example empty = new EmptyExample();
	public static Example typeddelta = new DeltaExample();
	public static Example tcomp = new TypedCompositionExample();
	public static Example iso = new IsoExample();
	public static Example triangle = new TriangleExample();
	public static Example cyclicgroup = new CyclicExample();
	public static Example employess = new EmployeesExample(); 
	public static Example dihedral = new DihedralExample();
	public static Example typedemployees = new TypedEmployeesExample();
	public static Example typedsigma = new TypedSigmaExample();
	public static Example relative = new RelationalizerExample();
	public static Example products = new ProductExample();
	public static Example enums = new EnumsExample();
	// public static Example functor = new FunctorExample();
	public static Example transform = new TransformExample();
	public static Example transform2 = new Transforms2Example();
	public static Example written = new WrittenExample();
//	public static Example monad = new MonadExample();
	public static Example gene = new GeneExample();
	public static Example sub = new SubSchemaExample();
	public static Example match = new MatchExample();
	public static Example match2 = new MatchExample2();
	public static Example surj = new SurjectiveExample();
	public static Example cat = new CategoryExample();
	public static Example eds2fql = new EDsToFqlExample();
	public static Example fncomp = new FnCompExample();
	public static Example auto = new AutoIsoExample();
	public static Example exp2 = new Exponentials2Example();
	public static Example prop = new PropExample();
	public static Example chare = new CharExample();
	public static Example im = new ImageExample();
	public static Example nist = new NistExample();
	public static Example nist2 = new NistContExample();
	
}
