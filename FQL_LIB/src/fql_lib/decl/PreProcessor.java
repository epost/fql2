package fql_lib.decl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.decl.CatExp.CatExpVisitor;
import fql_lib.decl.CatExp.Cod;
import fql_lib.decl.CatExp.Const;
import fql_lib.decl.CatExp.Dom;
import fql_lib.decl.CatExp.Exp;
import fql_lib.decl.CatExp.Kleisli;
import fql_lib.decl.CatExp.Named;
import fql_lib.decl.CatExp.One;
import fql_lib.decl.CatExp.Plus;
import fql_lib.decl.CatExp.Times;
import fql_lib.decl.CatExp.Union;
import fql_lib.decl.CatExp.Var;
import fql_lib.decl.CatExp.Zero;

public class PreProcessor implements CatExpVisitor<CatExp, FQLProgram> {

	@Override
	public CatExp visit(FQLProgram env, Zero e) {
		return e;
	}

	@Override
	public CatExp visit(FQLProgram env, One e) {
		return e;
	}

	@Override
	public CatExp visit(FQLProgram env, Plus e) {
		return new Plus(e.a.accept(env, this), e.b.accept(env, this));
	}

	@Override
	public CatExp visit(FQLProgram env, Times e) {
		return new Times(e.a.accept(env, this), e.b.accept(env, this));
	}

	@Override
	public CatExp visit(FQLProgram env, Exp e) {
		return new Exp(e.a.accept(env, this), e.b.accept(env, this));

	}

	@Override
	public CatExp visit(FQLProgram env, Var e) {
		return e;
	}

	@Override
	public CatExp visit(FQLProgram env, Const e) {
		return e;
	}

	@Override
	public CatExp visit(FQLProgram env, Dom e) {
		return e;
	}

	@Override
	public CatExp visit(FQLProgram env, Cod e) {
		return e;
	}

	@Override
	public CatExp visit(FQLProgram env, Named e) {
		return e;
	}

	@Override
	public CatExp visit(FQLProgram env, Kleisli e) {
		return e;
	}

	@Override
	public CatExp visit(FQLProgram env, Union e) {
		CatExp l = e.l.accept(env, this);
		CatExp r = e.r.accept(env, this);
		CatExp lx= CatOps.resolve(env, l);
		CatExp rx= CatOps.resolve(env, r);
		if (!(lx instanceof Const)) {
			throw new RuntimeException("Not a const: " + lx);
		}
		if (!(rx instanceof Const)) {
			throw new RuntimeException("Not a const: " + rx);
		}
		Const ly = (Const) lx;
		Const ry = (Const) rx;
		Set<Pair<Pair<String, List<String>>, Pair<String, List<String>>>> eqs = new HashSet<>();
		Set<Triple<String, String, String>> arrows = new HashSet<>();
		Set<String> nodes = new HashSet<>();
		nodes.addAll(ly.nodes);
		nodes.addAll(ry.nodes);
		arrows.addAll(ly.arrows);
		arrows.addAll(ry.arrows);
		eqs.addAll(ly.eqs);
		eqs.addAll(ry.eqs);
		return new Const(nodes, arrows, eqs);
	}

}
