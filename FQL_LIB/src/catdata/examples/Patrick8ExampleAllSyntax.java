package catdata.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class Patrick8ExampleAllSyntax extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}

	
	@Override
	public String getName() {
		return "All Syntax";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "//Don't forget to enable 'adom tables' in the options menu to display instances on infinite schemas"
			+ "\nstring : type"
			+ "\nbill : string" 
			+ "\ngeorge harry : string"
			+ "\nreverse : string -> string"
			+ "\neq1 : reverse.reverse = string"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	nodes N;"
			+ "\n	edges f : N -> N, att : N -> string;"
			+ "\n	equations f.f = f;"
			+ "\n}"
			+ "\nSS = schema {"
			+ "\nnodes N;"
			+ "\nedges f : N -> string;"
			+ "\nequations \"!_N\".bill = f;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables v:N, u:string;"
			+ "\n	equations v.att = bill.reverse;"
			+ "\n} : S"
			+ "\n"
			+ "\nII = instance {"
			+ "\n	variables v:N, vx:N, vy yz:N;"
			+ "\n	equations;"
			+ "\n} : S"
			+ "\n"
			+ "\n//asserts that an instance is saturated"
			+ "\nJ = INSTANCE {"
			+ "\n	variables;"
			+ "\n	equations;"
			+ "\n} : S"
			+ "\n"
			+ "\nF = mapping {"
			+ "\n	nodes N -> N;"
			+ "\n	edges f -> f.f, att -> att;"
			+ "\n} : S -> S"
			+ "\nF1 = id S"
			+ "\nF2 = (F1 ; F1)"
			+ "\n"
			+ "\nh = homomorphism {"
			+ "\n	variables v -> v, u -> v.att;"
			+ "\n} : I -> I"
			+ "\nh1 = id I"
			+ "\nh2 = (h1 ; h1)"
			+ "\nh3 = pushout h h"
			+ "\n"
			+ "\n//conjunctive queries (does not relationalize)"
			+ "\nq1 = flower {"
			+ "\n	select n1.att as col1, n2.att as col2;"
			+ "\n	from N as n1, N as n2;"
			+ "\n	where n1.f.att = n2.att;"
			+ "\n} I //(q1:var) = flower ... will bind var to the result schema"
			+ "\n"
			+ "\n//allows propositional logic (relationalizes)"
			+ "\nq2 = FLOWER {"
			+ "\n	select n1.att as col1, n2.att as col2;"
			+ "\n	from N as n1, N as n2;"
			+ "\n	where ((true and n1.f.att = n2.att) or false);"
			+ "\n} I"
			+ "\n"
			+ "\nuber = polynomial {"
			+ "\n	 q = {for n:N; where n.att=n.att; attributes att=n.att; edges f = {n=n.f} : q;} : N"
			+ "\n} : S -> S"
			+ "\nuber2 = apply uber I"
			+ "\nuber3 = apply uber h"
			+ "\nuber4 = coapply uber uber2"
			+ "\n"
			+ "\nw1 = pi F I"
			+ "\nw2 = sigma F I"
			+ "\nw3 = delta F I"
			+ "\nw4 = relationalize I"
			+ "\nu1 = pi F h"
			+ "\nu2 = sigma F h"
			+ "\nu3 = delta F h"
			+ "\nu4 = relationalize h"
			+ "\n"
			+ "\nx1 = return sigma delta F I"
			+ "\nx2 = coreturn sigma delta F I"
			+ "\nx3 = return delta pi F I"
			+ "\nx4 = coreturn delta pi F I"
			+ "\n"
			+ "\nI0 = (I * I)"
			+ "\nI1 = fst I I"
			+ "\nI2 = snd I I"
			+ "\nI3 = pair I1 I2"
			+ "\nI4 = (I + I)"
			+ "\nI5 = inl I I"
			+ "\nI6 = inr I I"
			+ "\nI7 = case I5 I6"
			+ "\nI8 = void S"
			+ "\nI9 = ff I"
			+ "\nI10 = unit S"
			+ "\nI11= tt I"
			+ "\n"
	+ "\n//illustrates warning for non functors"
	+ "\nSch1 = schema {"
	+ "\n	nodes n;"
	+ "\n	edges f:n-> n;"
	+ "\n	equations f.f = n;"
	+ "\n}"
	+ "\n"
	+ "\nSch2 = schema {"
	+ "\n	nodes n;"
	+ "\n	edges f:n-> n;"
	+ "\n	equations;"
	+ "\n}"
	+ "\n";
//	+ "\nNonFun = mapping {"
//	+ "\n	nodes n -> n;"
//	+ "\n	edges f -> f;"
//	+ "\n} : Sch1 -> Sch2"
//	+ "\n";




}
