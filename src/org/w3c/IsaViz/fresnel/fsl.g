/*   @author Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 * $Id: fsl.g,v 1.20 2006/10/26 13:58:04 epietrig Exp $
 */

header {
/*   @author Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *
 * $Id: fsl.g,v 1.20 2006/10/26 13:58:04 epietrig Exp $
 */

package org.w3c.IsaViz.fresnel;
}

class FSLParser extends Parser;

options {
	k = 1;
	buildAST = true;
}

imaginaryTokenDefinitions :
	AXIS
	FUNCTIONNAME
	TEXT
	QNAME
	ANYNAME
	ANDOP
	OROP
	;

locationpath :
	step (SLASHOP^ step)*
	;

step :
	(AXIS^)? test (predicate)*
	| abbreviatedstep
	| LITERAL
	| TEXT
	| TEXTLANG
	| TEXTDT
	;

test :
	typetest
	;

typetest :
	ANYNAME
	| MSUBOP^ QNAME
	| QNAME
	;

abbreviatedstep :
	SELFABBR
	;

predicate :
	LSQBR^ predexpr RSQBR!
	;

predexpr :
	orexpr
	;

orexpr :
	andexpr (OROP^ orexpr)?
	;

andexpr :
	compexpr (ANDOP^ andexpr)?
	;

compexpr :
	unaryexpr ((EQUALOP^ | DIFFOP^ | INFOP^ | SUPOP^ | INFEQOP^ | SUPEQOP^) unaryexpr)?
	;

unaryexpr :
	FUNCTIONNAME^ LPAREN! arguments RPAREN!
	| NUMBER
	| locationpath
	;

arguments :
	(unaryexpr (COMMA! unaryexpr)*)?
	;









class FSLLexer extends Lexer;

options {
  	k=2;
  	filter=false;
	charVocabulary='\3'..'\377';
}


NUMBER :
	('-')? (DIGIT)+ ('.' (DIGIT)+)?
	;

LITERAL :
	('"' (LETTER | DIGIT | SP)+ '"'
	| "'" (LETTER | DIGIT | SP)+ "'") (LANG | DATATYPE)?
	;

protected LANG :
	"@" NCNAME
	;

protected DATATYPE :
	"^^" (NCNAME)? ':' NCNAME {$setType(QNAME);}
	;

NAME :
	("in::") => "in::" {$setType(AXIS);}
	| ("out::") => "out::" {$setType(AXIS);}
	| ("self::") => "self::" {$setType(AXIS);}
	| ("and") => "and" {$setType(ANDOP);}
	| ("or") => "or" {$setType(OROP);}
	| ('*') => '*' {$setType(ANYNAME);}
	| ("exp") => "exp" {$setType(FUNCTIONNAME);}
	| ("text()" LANG) => "text()" LANG {$setType(TEXTLANG);}
	| ("text()" DATATYPE) => "text()" DATATYPE {$setType(TEXTDT);}
	| ("text()") => "text()" {$setType(TEXT);}
	| ("count") => "count" {$setType(FUNCTIONNAME);}
	| ("local-name") => "local-name" {$setType(FUNCTIONNAME);}
	| ("namespace-uri") => "namespace-uri" {$setType(FUNCTIONNAME);}
	| ("uri") => "uri" {$setType(FUNCTIONNAME);}
	| ("literal-value") => "literal-value" {$setType(FUNCTIONNAME);}
	| ("literal-dt") => "literal-dt" {$setType(FUNCTIONNAME);}
	| ("string-length") => "string-length" {$setType(FUNCTIONNAME);}
	| ("string") => "string" {$setType(FUNCTIONNAME);}
	| ("starts-with") => "starts-with" {$setType(FUNCTIONNAME);}
	| ("contains") => "contains" {$setType(FUNCTIONNAME);}
	| ("concat") => "concat" {$setType(FUNCTIONNAME);}
	| ("substring-before") => "substring-before" {$setType(FUNCTIONNAME);}
	| ("substring-after") => "substring-after" {$setType(FUNCTIONNAME);}
	| ("substring") => "substring" {$setType(FUNCTIONNAME);}
	| ("boolean") => "boolean" {$setType(FUNCTIONNAME);}
	| ("not") => "not" {$setType(FUNCTIONNAME);}
	| ("true") => "true" {$setType(FUNCTIONNAME);}
	| ("false") => "false" {$setType(FUNCTIONNAME);}
	| ("normalize-space") => "normalize-space" {$setType(FUNCTIONNAME);}
	| ("number") => "number" {$setType(FUNCTIONNAME);}
	| ("blank") => "blank" {$setType(FUNCTIONNAME);}
	| ((NCNAME)? ":*") => (NCNAME)? ":*" {$setType(QNAME);}
	| (NCNAME)? ':' NCNAME {$setType(QNAME);}
	;

protected NCNAME :
	(LETTER | '_') (NCNAMECHAR | '_')*
	;

protected NCNAMECHAR :
	(LETTER | DIGIT)
	;

protected DIGIT :
	('0'..'9')
	;

protected LETTER :
	('a'..'z' | 'A'..'Z')
	;

protected SP :
	' ' | '$' | '@' | '&' | '{' | '}' | '!' | '?' | '%' | '*' | ':' | '_' | '-' | '#' | '+'
	| LSQBR | RSQBR | LPAREN | RPAREN | SLASHOP | COMMA | SELFABBR | SUPOP | INFOP | EQUALOP
	;

MSUBOP :
	'^'
	;

COMMA :
	','
	;

SLASHOP :
	'/'
	;

EQUALOP :
	'='
	;

DIFFOP :
	"!="
	;

INFOP :
	'<'
	;

INFEQOP :
	"<="
	;

SUPOP :
	'>'
	;

SUPEQOP :
	">="
	;

LPAREN :
	 '('
	 ;

RPAREN :
	')'
	;

LSQBR :
	'['
	;

RSQBR :
	']'
	;

SELFABBR :
	'.'
	;

WS :
	( ' ' 
	| '\r' '\n'
	| '\n'
	| '\t'
	)
	{$setType(Token.SKIP);}
	;



