package catdata.aql;

public class Lineage<Ty,En,Sym,Fk,Att,Gen,Sk> {

	public final Integer i;
	public final Term<Ty,En,Sym,Fk,Att,Gen,Sk> t;
	
	public <Ty,En,Sym,Fk,Att,Gen,Sk> Lineage<Ty,En,Sym,Fk,Att,Gen,Sk> convert() {
		return (Lineage<Ty, En, Sym, Fk, Att, Gen, Sk>) this;
	}

	public Lineage(Integer i, Term<Ty,En,Sym,Fk,Att,Gen,Sk> t) {
		this.i = i;
		this.t = t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((i == null) ? 0 : i.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Lineage other = (Lineage) obj;
		if (i == null) {
			if (other.i != null)
				return false;
		} else if (!i.equals(other.i))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + t + " " + i + "]";
	}

}