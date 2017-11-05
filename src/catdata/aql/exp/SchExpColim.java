package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import catdata.Pair;
import catdata.Program;
import catdata.aql.Kind;
import catdata.aql.Schema;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

public class SchExpColim<N> extends SchExp<Ty, En, Sym, Fk, Att> {

		public final ColimSchExp<N> exp;

		public SchExpColim(ColimSchExp<N> exp) {
			this.exp = exp;
		}
		
		//TODO aql schema equality too weak
		@Override
		public SchExp<Ty, En, Sym, Fk, Att> resolve(AqlTyping G, Program<Exp<?>> prog) {
			return this;
		}
		
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((exp == null) ? 0 : exp.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SchExpColim<?> other = (SchExpColim<?>) obj;
			if (exp == null) {
				if (other.exp != null)
					return false;
			} else if (!exp.equals(other.exp))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "getSchema " + exp;
		}

		
		@Override
		public Schema<Ty, En, Sym, Fk, Att> eval(AqlEnv env) {
			return exp.eval(env).schemaStr;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return exp.deps();
		}
		
		
		
		
	}
////////////////////////////////////////////////////////////////////////////////////////////////////