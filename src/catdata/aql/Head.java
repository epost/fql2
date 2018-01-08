package catdata.aql;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import catdata.Util;

public class Head<Ty, En, Sym, Fk, Att, Gen, Sk> implements Comparable<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> {

	//these are necessary for KB
	/*
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		Head<?,?,?,?,?,?,?> other = (Head<?,?,?,?,?,?,?>) o;
		
		if (att != null) {
			return att.equals(other.att);
		} else if (fk != null) {
			return fk.equals(other.fk);
		} else if (gen != null) {
			return gen.equals(other.gen);
		} else if (sk != null) {
			return sk.equals(other.sk);
		} else if ()
		else if (obj != null) {
			return obj.equals(other.obj) && ty.equals(other.ty); //not quite - ignore for now bug what if ty null TODO aql
		} else if (sym != null) {
			return sym.equals(other.sym);
		} 
		
		throw new RuntimeException("Anomaly: please report");
	}*/
		
	
	
	@Override
	 public int compareTo(Head<Ty, En, Sym, Fk, Att, Gen, Sk> other) {
	return	Util.anomaly();
//			return toString().compareTo(other.toString()); //essentially an arbitrary precedence on ground symbols
	   }
	
/*	int code = -1;
	@Override
	public int hashCode() {
		if (code != -1) {
			return code;
		}
		code = new HashCodeBuilder()
				.append(ty)
				.append(sym)
				.append(fk)
				.append(att)
				.append(gen)
				.append(sk)
				.append(obj)
				.toHashCode();
		return code;
	} */
	/*
	@Override
	public int compareTo(Head<Ty, En, Sym, Fk, Att, Gen, Sk> o) {
		return new CompareToBuilder()
				.append(ty, o.ty)
				.append(sym, o.sym)
				.append(fk, o.fk)
				.append(att, o.att)
				.append(gen, o.gen)
				.append(sk, o.sk)
				.append(obj, o.obj)
				.toComparison();

	}

	
*/ /*
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		Head<Ty, En, Sym, Fk, Att, Gen, Sk> o = (Head<Ty, En, Sym, Fk, Att, Gen, Sk>) obj;
		return new EqualsBuilder()
				.append(ty, o.ty)
				.append(sym, o.sym)
				.append(fk, o.fk)
				.append(att, o.att)
				.append(gen, o.gen)
				.append(sk, o.sk)
				.append(obj, o.obj).isEquals();
	}
*/
	
		/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((att == null) ? 0 : att.hashCode());
		result = prime * result + ((fk == null) ? 0 : fk.hashCode());
		result = prime * result + ((gen == null) ? 0 : gen.hashCode());
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		result = prime * result + ((sk == null) ? 0 : sk.hashCode());
		result = prime * result + ((sym == null) ? 0 : sym.hashCode());
		result = prime * result + ((ty == null) ? 0 : ty.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Head))
			return false;
		Head other = (Head) obj;
		if (att == null) {
			if (other.att != null)
				return false;
		} else if (!att.equals(other.att))
			return false;
		if (fk == null) {
			if (other.fk != null)
				return false;
		} else if (!fk.equals(other.fk))
			return false;
		if (gen == null) {
			if (other.gen != null)
				return false;
		} else if (!gen.equals(other.gen))
			return false;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		if (sk == null) {
			if (other.sk != null)
				return false;
		} else if (!sk.equals(other.sk))
			return false;
		if (sym == null) {
			if (other.sym != null)
				return false;
		} else if (!sym.equals(other.sym))
			return false;
		if (ty == null) {
			if (other.ty != null)
				return false;
		} else if (!ty.equals(other.ty))
			return false;
		return true;
	}*/

	public final Sym sym;
	public final Fk fk; 
	public final Att att;
	public final Gen gen;
	public final Sk sk;
	public final Object obj;
	public final Ty ty;
	
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Head<Ty, En, Sym, Fk, Att, Gen, Sk> Sym(Sym sym) {
		if (sym == null) {
			throw new RuntimeException("Anomaly, please report");
		}
		return new Head<>(sym, null, null, null, null, null, null);
	}
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Head<Ty, En, Sym, Fk, Att, Gen, Sk> Fk(Fk fk) {
		if (fk == null) {
			throw new RuntimeException("Anomaly, please report");
		}
		return new Head<>(null, fk, null, null, null, null, null);
	}
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Head<Ty, En, Sym, Fk, Att, Gen, Sk> Att(Att att) {
		if (att == null) {
			throw new RuntimeException("Anomaly, please report");
		}
		return new Head<>(null, null, att, null, null, null, null);
	}
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Head<Ty, En, Sym, Fk, Att, Gen, Sk> Gen(Gen gen) {
		if (gen == null) {
			throw new RuntimeException("Anomaly, please report");
		}
		return new Head<>(null, null, null, gen, null, null, null);
	}
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Head<Ty, En, Sym, Fk, Att, Gen, Sk> Sk(Sk sk) {
		if (sk == null) {
			throw new RuntimeException("Anomaly, please report");
		}
		return new Head<>(null, null, null, null, sk, null, null);
	}
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Head<Ty, En, Sym, Fk, Att, Gen, Sk> Obj(Object obj, Ty ty) {
		if (obj == null || ty == null) {
			throw new RuntimeException("Anomaly, please report");
		}
		return new Head<>(null, null, null, null, null, obj, ty);
	}
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Head<Ty, En, Sym, Fk, Att, Gen, Sk> ObjAqlInternal(Object obj) {
		if (obj == null) {
			throw new RuntimeException("Anomaly, please report");
		}
		return new Head<>(null, null, null, null, null, obj, null);
	}
	
	private Head(Sym sym, Fk fk, Att att, Gen gen, Sk sk, Object obj, Ty ty) {
		this.sym = sym;
		this.fk = fk;
		this.att = att;
		this.gen = gen;
		this.sk = sk;
		this.obj = obj;
		this.ty = ty;
	}

	public Head(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
        sym = term.sym;
        fk = term.fk;
        att = term.att;
        gen = term.gen;
        sk = term.sk;
        obj = term.obj;
        ty = term.ty;
		if (term.var != null) {
			throw new RuntimeException("Variable " + term.var + " cannot be a symbol");
		} else if (sym == null && fk == null && att == null && gen == null && sk == null && obj == null) {
			throw new RuntimeException("Anomaly, please report");
		}
	}
/*
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((att == null) ? 0 : att.hashCode());
		result = prime * result + ((fk == null) ? 0 : fk.hashCode());
		result = prime * result + ((gen == null) ? 0 : gen.hashCode());
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		result = prime * result + ((sk == null) ? 0 : sk.hashCode());
		result = prime * result + ((sym == null) ? 0 : sym.hashCode());
		result = prime * result + ((ty == null) ? 0 : ty.hashCode());
		return result;
	}

	

		/*
		if (att == null) {
			if (other.att != null)
				return false;
		} else if (!att.equals(other.att))
			return false;
		if (fk == null) {
			if (other.fk != null)
				return false;
		} else if (!fk.equals(other.fk))
			return false;
		if (gen == null) {
			if (other.gen != null)
				return false;
		} else if (!gen.equals(other.gen))
			return false;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		if (sk == null) {
			if (other.sk != null)
				return false;
		} else if (!sk.equals(other.sk))
			return false;
		if (sym == null) {
			if (other.sym != null)
				return false;
		} else if (!sym.equals(other.sym))
			return false;
		if (ty == null) {
			if (other.ty != null)
				return false;
		} else if (!ty.equals(other.ty))
			return false;
		return true; */
//}
	
		
	@Override
	public String toString() {
		if (att != null) {
			return att.toString();
		} else if (fk != null) {
			return fk.toString();
		} else if (sym != null) {
			return sym.toString();
		} else if (gen != null) {
			return gen.toString();
		} else if (sk != null) {
			return sk.toString();
		} else if (obj != null) {
			return obj + "@" + ty;
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	
	///////////////////////
	
	//works 
	
	/* @Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	} */

	/*	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj); //includes super
	} */
	
	
	int code = -1;
		public int hashCode() {
		if (code != -1) {
				return code;
			}
			code = new HashCodeBuilder()
					.append(true) //there's a bug in HashCodeBuilder first append in null TODO aql rqport
					.append(ty)
					.append(sym)
					.append(fk)
					.append(att)
					.append(gen)
					.append(sk)
					.append(obj)
					.toHashCode();
			return code;
		}
	
		
		@Override
		public boolean equals(Object x) { //name collision on obj
			if (x == null) {
				return false;
			}
			if (x == this) {
				return true;
			}
			if (x.getClass() != getClass()) {
				return false;
			}
			Head<Ty, En, Sym, Fk, Att, Gen, Sk> o = (Head<Ty, En, Sym, Fk, Att, Gen, Sk>) x;
			if (ty != null) {
				return (ty.equals(o.ty) && obj.equals(o.obj));
			}
			if (sym != null) {
				return sym.equals(o.sym);
			}
			if (fk != null) {
				return fk.equals(o.fk);
			}
			if (att != null) {
				return att.equals(o.att);
			}
			if (gen != null) {
				return gen.equals(o.gen);
			}
			if (sk != null) {
				return sk.equals(o.sk);
			}
			return Util.anomaly();
			
			/*
			return new EqualsBuilder()
					.append(true, true) //just in case
					.append(ty, o.ty)
					.append(sym, o.sym)
					.append(fk, o.fk)
					.append(att, o.att)
					.append(gen, o.gen)
					.append(sk, o.sk)
					.append(obj, o.obj).isEquals(); */
		} 
}
