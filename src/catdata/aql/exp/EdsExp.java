package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Constraints;
import catdata.aql.Kind;

public abstract class EdsExp<Ty, En, Sym, Fk, Att> extends Exp<Constraints<Ty, En, Sym, Fk, Att>> {

	
	@Override
	public Kind kind() {
		return Kind.CONSTRAINTS;
	}

	public abstract SchExp<Ty, En, Sym, Fk, Att> type(AqlTyping G);

		

	/////////////////////////////////////////////////////////////////////////////////////////



	/////////////////////////////////////////////////////////////////////////////////////////

	public static final class EdsExpVar extends EdsExp<Object, Object, Object, Object, Object> {

		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		
		public final String var;

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.CONSTRAINTS));
		}

		public EdsExpVar(String var) {
			Util.assertNotNull(var);
			this.var = var;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Constraints<Object, Object, Object, Object, Object> eval(AqlEnv env) {
			return env.defs.eds.get(var);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (var.hashCode());
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
			EdsExpVar other = (EdsExpVar) obj;
			return var.equals(other.var);
		}

		@Override
		public String toString() {
			return var;
		}

		

		@SuppressWarnings("unchecked")
		@Override
		public SchExp<Object, Object, Object, Object, Object> type(AqlTyping G) {
			if (!G.defs.eds.containsKey(var)) {
				throw new RuntimeException("Not constraints: " + var);
			}
			return (SchExp<Object, Object, Object, Object, Object>) ((Object) G.defs.eds.get(var));
		}
	}
}
