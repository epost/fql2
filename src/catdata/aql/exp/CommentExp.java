package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;

import catdata.Pair;
import catdata.aql.Comment;
import catdata.aql.Kind;

public class CommentExp extends Exp<Comment> {

	public final String s;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (!s.equals(other.s))
			return false;
		return true;
	}

	public CommentExp(String s) {
		this.s = s;
	}

	@Override
	public long timeout() {
		return 0;
	}

	@Override
	public Kind kind() {
		return Kind.COMMENT;
	}

	@Override
	public Comment eval(AqlEnv env) {
		return new Comment(s);
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
