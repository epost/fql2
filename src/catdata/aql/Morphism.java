package catdata.aql;

import catdata.Chc;
import catdata.Pair;

public interface Morphism<Ty,En1,Sym1,Fk1,Att1,Gen1,Sk1,En2,Sym2,Fk2,Att2,Gen2,Sk2> {

	public Collage<Ty,En1,Sym1,Fk1,Att1,Gen1,Sk1> src();
	
	public Collage<Ty,En2,Sym2,Fk2,Att2,Gen2,Sk2> dst();
	
	public Pair<Ctx<Var, Chc<Ty,En2>>, Term<Ty,En2,Sym2,Fk2,Att2,Gen2,Sk2>> 
	 translate(Ctx<Var, Chc<Ty,En1>> ctx, Term<Ty,En1,Sym1,Fk1,Att1,Gen1,Sk1> term);
	
}
