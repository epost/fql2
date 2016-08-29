package catdata.aql;

public interface NF<Ty,En,Sym,Fk,Att,Gen,Sk> {

	public Term<Ty,En,Sym,Fk,Att,Gen,Sk> nf(Term<Ty,En,Sym,Fk,Att,Gen,Sk> term);
	
}
