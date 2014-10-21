package fql_lib.examples;

public class Patrick8Example extends Example {

	@Override
	public boolean isPatrick() {
		return true;
	}
	
	@Override
	public String getName() {
		return "P All Syntax";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "string : type"
			+ "\nbill : string"
			+ "\nreverse : string -> string"
			+ "\neq1 : reverse.reverse = string"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	nodes N;"
			+ "\n	edges f : N -> N, att : N -> string;"
			+ "\n	equations f.f = f;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables v:N;"
			+ "\n	equations v.att = bill.reverse;"
			+ "\n} : S"
			+ "\n"
			+ "\nF = mapping {"
			+ "\n	nodes N -> N;"
			+ "\n	edges f -> f.f, att -> att;"
			+ "\n} : S -> S"
			+ "\n"
			+ "\nh = homomorphism {"
			+ "\n	variables v -> v;"
			+ "\n} : I -> I"
			+ "\n"
			+ "\nw1 = pi F I"
			+ "\nw2 = sigma F I"
			+ "\nw3 = delta F I"
			+ "\nw4 = relationalize I"
			+ "\nu1 = pi F h"
			+ "\nu2 = sigma F h"
			+ "\nu3 = delta F I"
			+ "\n//u4 = relationalize h ? "
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
			+ "\n";




}
