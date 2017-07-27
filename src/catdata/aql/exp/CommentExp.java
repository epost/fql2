package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import catdata.Pair;
import catdata.aql.Comment;
import catdata.aql.Kind;

public class CommentExp extends Exp<Comment> {

	public final String s;
	public final boolean isM;
	
	@Override
	public Map<String, String> options() {
		return Collections.emptyMap();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isM ? 1231 : 1237);
		result = prime * result + ((s == null) ? 0 : s.hashCode());
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
		CommentExp other = (CommentExp) obj;
		if (isM != other.isM)
			return false;
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (!s.equals(other.s))
			return false;
		return true;
	}

	public CommentExp(String s, boolean isM) {
		this.s = s;
		this.isM = isM;
	}

	

	@Override
	public Kind kind() {
		return Kind.COMMENT;
	}

	@Override
	public Comment eval(AqlEnv env) {
		return new Comment(s, isM);
	}

	@Override
	public String toString() {
		return s;
	}


	@Override
	public Collection<Pair<String, Kind>> deps() {
		return Collections.emptyList();
	}

}
