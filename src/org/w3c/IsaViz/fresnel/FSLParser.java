// $ANTLR 2.7.5 (20050128): "fsl.g" -> "FSLParser.java"$

/*   @author Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *
 * $Id: FSLParser.java,v 1.16 2006/10/26 13:58:04 epietrig Exp $
 */

package org.w3c.IsaViz.fresnel;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class FSLParser extends antlr.LLkParser       implements FSLParserTokenTypes
 {

protected FSLParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public FSLParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected FSLParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public FSLParser(TokenStream lexer) {
  this(lexer,1);
}

public FSLParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void imaginaryTokenDefinitions() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST imaginaryTokenDefinitions_AST = null;
		
		try {      // for error handling
			AST tmp1_AST = null;
			tmp1_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp1_AST);
			match(AXIS);
			AST tmp2_AST = null;
			tmp2_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp2_AST);
			match(FUNCTIONNAME);
			AST tmp3_AST = null;
			tmp3_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp3_AST);
			match(TEXT);
			AST tmp4_AST = null;
			tmp4_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp4_AST);
			match(QNAME);
			AST tmp5_AST = null;
			tmp5_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp5_AST);
			match(ANYNAME);
			AST tmp6_AST = null;
			tmp6_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp6_AST);
			match(ANDOP);
			AST tmp7_AST = null;
			tmp7_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp7_AST);
			match(OROP);
			imaginaryTokenDefinitions_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		returnAST = imaginaryTokenDefinitions_AST;
	}
	
	public final void locationpath() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST locationpath_AST = null;
		
		try {      // for error handling
			step();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop4:
			do {
				if ((LA(1)==SLASHOP)) {
					AST tmp8_AST = null;
					tmp8_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp8_AST);
					match(SLASHOP);
					step();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop4;
				}
				
			} while (true);
			}
			locationpath_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
		returnAST = locationpath_AST;
	}
	
	public final void step() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST step_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case AXIS:
			case QNAME:
			case ANYNAME:
			case MSUBOP:
			{
				{
				switch ( LA(1)) {
				case AXIS:
				{
					AST tmp9_AST = null;
					tmp9_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp9_AST);
					match(AXIS);
					break;
				}
				case QNAME:
				case ANYNAME:
				case MSUBOP:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				test();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop8:
				do {
					if ((LA(1)==LSQBR)) {
						predicate();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop8;
					}
					
				} while (true);
				}
				step_AST = (AST)currentAST.root;
				break;
			}
			case SELFABBR:
			{
				abbreviatedstep();
				astFactory.addASTChild(currentAST, returnAST);
				step_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL:
			{
				AST tmp10_AST = null;
				tmp10_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp10_AST);
				match(LITERAL);
				step_AST = (AST)currentAST.root;
				break;
			}
			case TEXT:
			{
				AST tmp11_AST = null;
				tmp11_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp11_AST);
				match(TEXT);
				step_AST = (AST)currentAST.root;
				break;
			}
			case TEXTLANG:
			{
				AST tmp12_AST = null;
				tmp12_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp12_AST);
				match(TEXTLANG);
				step_AST = (AST)currentAST.root;
				break;
			}
			case TEXTDT:
			{
				AST tmp13_AST = null;
				tmp13_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp13_AST);
				match(TEXTDT);
				step_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		returnAST = step_AST;
	}
	
	public final void test() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST test_AST = null;
		
		try {      // for error handling
			typetest();
			astFactory.addASTChild(currentAST, returnAST);
			test_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		returnAST = test_AST;
	}
	
	public final void predicate() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST predicate_AST = null;
		
		try {      // for error handling
			AST tmp14_AST = null;
			tmp14_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp14_AST);
			match(LSQBR);
			predexpr();
			astFactory.addASTChild(currentAST, returnAST);
			match(RSQBR);
			predicate_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		returnAST = predicate_AST;
	}
	
	public final void abbreviatedstep() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST abbreviatedstep_AST = null;
		
		try {      // for error handling
			AST tmp16_AST = null;
			tmp16_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp16_AST);
			match(SELFABBR);
			abbreviatedstep_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		returnAST = abbreviatedstep_AST;
	}
	
	public final void typetest() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typetest_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ANYNAME:
			{
				AST tmp17_AST = null;
				tmp17_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp17_AST);
				match(ANYNAME);
				typetest_AST = (AST)currentAST.root;
				break;
			}
			case MSUBOP:
			{
				AST tmp18_AST = null;
				tmp18_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp18_AST);
				match(MSUBOP);
				AST tmp19_AST = null;
				tmp19_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp19_AST);
				match(QNAME);
				typetest_AST = (AST)currentAST.root;
				break;
			}
			case QNAME:
			{
				AST tmp20_AST = null;
				tmp20_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp20_AST);
				match(QNAME);
				typetest_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		returnAST = typetest_AST;
	}
	
	public final void predexpr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST predexpr_AST = null;
		
		try {      // for error handling
			orexpr();
			astFactory.addASTChild(currentAST, returnAST);
			predexpr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
		returnAST = predexpr_AST;
	}
	
	public final void orexpr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST orexpr_AST = null;
		
		try {      // for error handling
			andexpr();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case OROP:
			{
				AST tmp21_AST = null;
				tmp21_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp21_AST);
				match(OROP);
				orexpr();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RSQBR:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			orexpr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
		returnAST = orexpr_AST;
	}
	
	public final void andexpr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST andexpr_AST = null;
		
		try {      // for error handling
			compexpr();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case ANDOP:
			{
				AST tmp22_AST = null;
				tmp22_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp22_AST);
				match(ANDOP);
				andexpr();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case OROP:
			case RSQBR:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			andexpr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
		returnAST = andexpr_AST;
	}
	
	public final void compexpr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST compexpr_AST = null;
		
		try {      // for error handling
			unaryexpr();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case EQUALOP:
			case DIFFOP:
			case INFOP:
			case SUPOP:
			case INFEQOP:
			case SUPEQOP:
			{
				{
				switch ( LA(1)) {
				case EQUALOP:
				{
					AST tmp23_AST = null;
					tmp23_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp23_AST);
					match(EQUALOP);
					break;
				}
				case DIFFOP:
				{
					AST tmp24_AST = null;
					tmp24_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp24_AST);
					match(DIFFOP);
					break;
				}
				case INFOP:
				{
					AST tmp25_AST = null;
					tmp25_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp25_AST);
					match(INFOP);
					break;
				}
				case SUPOP:
				{
					AST tmp26_AST = null;
					tmp26_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp26_AST);
					match(SUPOP);
					break;
				}
				case INFEQOP:
				{
					AST tmp27_AST = null;
					tmp27_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp27_AST);
					match(INFEQOP);
					break;
				}
				case SUPEQOP:
				{
					AST tmp28_AST = null;
					tmp28_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp28_AST);
					match(SUPEQOP);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				unaryexpr();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case ANDOP:
			case OROP:
			case RSQBR:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			compexpr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_6);
		}
		returnAST = compexpr_AST;
	}
	
	public final void unaryexpr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unaryexpr_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case FUNCTIONNAME:
			{
				AST tmp29_AST = null;
				tmp29_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp29_AST);
				match(FUNCTIONNAME);
				match(LPAREN);
				arguments();
				astFactory.addASTChild(currentAST, returnAST);
				match(RPAREN);
				unaryexpr_AST = (AST)currentAST.root;
				break;
			}
			case NUMBER:
			{
				AST tmp32_AST = null;
				tmp32_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp32_AST);
				match(NUMBER);
				unaryexpr_AST = (AST)currentAST.root;
				break;
			}
			case AXIS:
			case TEXT:
			case QNAME:
			case ANYNAME:
			case LITERAL:
			case TEXTLANG:
			case TEXTDT:
			case MSUBOP:
			case SELFABBR:
			{
				locationpath();
				astFactory.addASTChild(currentAST, returnAST);
				unaryexpr_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
		returnAST = unaryexpr_AST;
	}
	
	public final void arguments() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arguments_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case AXIS:
			case FUNCTIONNAME:
			case TEXT:
			case QNAME:
			case ANYNAME:
			case LITERAL:
			case TEXTLANG:
			case TEXTDT:
			case MSUBOP:
			case SELFABBR:
			case NUMBER:
			{
				unaryexpr();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop25:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						unaryexpr();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop25;
					}
					
				} while (true);
				}
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			arguments_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_7);
		}
		returnAST = arguments_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"AXIS",
		"FUNCTIONNAME",
		"TEXT",
		"QNAME",
		"ANYNAME",
		"ANDOP",
		"OROP",
		"SLASHOP",
		"LITERAL",
		"TEXTLANG",
		"TEXTDT",
		"MSUBOP",
		"SELFABBR",
		"LSQBR",
		"RSQBR",
		"EQUALOP",
		"DIFFOP",
		"INFOP",
		"SUPOP",
		"INFEQOP",
		"SUPEQOP",
		"LPAREN",
		"RPAREN",
		"NUMBER",
		"COMMA",
		"LANG",
		"DATATYPE",
		"NAME",
		"NCNAME",
		"NCNAMECHAR",
		"DIGIT",
		"LETTER",
		"SP",
		"WS"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 368838144L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 368840192L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 368971264L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 262144L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 263168L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 263680L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 67108864L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	
	}
