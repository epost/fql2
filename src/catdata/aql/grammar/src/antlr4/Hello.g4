parser grammar Hello;
//import HelloLex ;
tokens {
  HELLO, ID
}
r : HELLO ID;
