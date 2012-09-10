// $ANTLR 2.7.5 (20050128): "fsl.g" -> "FSLLexer.java"$

/*   @author Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005.
 *
 * $Id: FSLLexer.java,v 1.16 2006/10/26 13:58:04 epietrig Exp $
 */

package org.w3c.IsaViz.fresnel;

import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

public class FSLLexer extends antlr.CharScanner implements FSLParserTokenTypes, TokenStream
 {
public FSLLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public FSLLexer(Reader in) {
	this(new CharBuffer(in));
}
public FSLLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public FSLLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = true;
	setCaseSensitive(true);
	literals = new Hashtable();
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '-':  case '0':  case '1':  case '2':
				case '3':  case '4':  case '5':  case '6':
				case '7':  case '8':  case '9':
				{
					mNUMBER(true);
					theRetToken=_returnToken;
					break;
				}
				case '"':  case '\'':
				{
					mLITERAL(true);
					theRetToken=_returnToken;
					break;
				}
				case '*':  case ':':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case 'G':  case 'H':  case 'I':  case 'J':
				case 'K':  case 'L':  case 'M':  case 'N':
				case 'O':  case 'P':  case 'Q':  case 'R':
				case 'S':  case 'T':  case 'U':  case 'V':
				case 'W':  case 'X':  case 'Y':  case 'Z':
				case '_':  case 'a':  case 'b':  case 'c':
				case 'd':  case 'e':  case 'f':  case 'g':
				case 'h':  case 'i':  case 'j':  case 'k':
				case 'l':  case 'm':  case 'n':  case 'o':
				case 'p':  case 'q':  case 'r':  case 's':
				case 't':  case 'u':  case 'v':  case 'w':
				case 'x':  case 'y':  case 'z':
				{
					mNAME(true);
					theRetToken=_returnToken;
					break;
				}
				case '[':
				{
					mLSQBR(true);
					theRetToken=_returnToken;
					break;
				}
				case ']':
				{
					mRSQBR(true);
					theRetToken=_returnToken;
					break;
				}
				case '(':
				{
					mLPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case ')':
				{
					mRPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case '/':
				{
					mSLASHOP(true);
					theRetToken=_returnToken;
					break;
				}
				case ',':
				{
					mCOMMA(true);
					theRetToken=_returnToken;
					break;
				}
				case '.':
				{
					mSELFABBR(true);
					theRetToken=_returnToken;
					break;
				}
				case '=':
				{
					mEQUALOP(true);
					theRetToken=_returnToken;
					break;
				}
				case '^':
				{
					mMSUBOP(true);
					theRetToken=_returnToken;
					break;
				}
				case '!':
				{
					mDIFFOP(true);
					theRetToken=_returnToken;
					break;
				}
				case '\t':  case '\n':  case '\r':  case ' ':
				{
					mWS(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if ((LA(1)=='<') && (LA(2)=='=')) {
						mINFEQOP(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='=')) {
						mSUPEQOP(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (true)) {
						mSUPOP(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (true)) {
						mINFOP(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_ttype = testLiteralsTable(_ttype);
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mNUMBER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NUMBER;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '-':
		{
			match('-');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		int _cnt29=0;
		_loop29:
		do {
			if (((LA(1) >= '0' && LA(1) <= '9'))) {
				mDIGIT(false);
			}
			else {
				if ( _cnt29>=1 ) { break _loop29; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt29++;
		} while (true);
		}
		{
		if ((LA(1)=='.')) {
			match('.');
			{
			int _cnt32=0;
			_loop32:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					mDIGIT(false);
				}
				else {
					if ( _cnt32>=1 ) { break _loop32; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt32++;
			} while (true);
			}
		}
		else {
		}
		
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIGIT;
		int _saveIndex;
		
		{
		matchRange('0','9');
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LITERAL;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '"':
		{
			match('"');
			{
			int _cnt36=0;
			_loop36:
			do {
				switch ( LA(1)) {
				case 'A':  case 'B':  case 'C':  case 'D':
				case 'E':  case 'F':  case 'G':  case 'H':
				case 'I':  case 'J':  case 'K':  case 'L':
				case 'M':  case 'N':  case 'O':  case 'P':
				case 'Q':  case 'R':  case 'S':  case 'T':
				case 'U':  case 'V':  case 'W':  case 'X':
				case 'Y':  case 'Z':  case 'a':  case 'b':
				case 'c':  case 'd':  case 'e':  case 'f':
				case 'g':  case 'h':  case 'i':  case 'j':
				case 'k':  case 'l':  case 'm':  case 'n':
				case 'o':  case 'p':  case 'q':  case 'r':
				case 's':  case 't':  case 'u':  case 'v':
				case 'w':  case 'x':  case 'y':  case 'z':
				{
					mLETTER(false);
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					mDIGIT(false);
					break;
				}
				case ' ':  case '!':  case '#':  case '$':
				case '%':  case '&':  case '(':  case ')':
				case '*':  case '+':  case ',':  case '-':
				case '.':  case '/':  case ':':  case '<':
				case '=':  case '>':  case '?':  case '@':
				case '[':  case ']':  case '_':  case '{':
				case '}':
				{
					mSP(false);
					break;
				}
				default:
				{
					if ( _cnt36>=1 ) { break _loop36; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				_cnt36++;
			} while (true);
			}
			match('"');
			break;
		}
		case '\'':
		{
			match("'");
			{
			int _cnt38=0;
			_loop38:
			do {
				switch ( LA(1)) {
				case 'A':  case 'B':  case 'C':  case 'D':
				case 'E':  case 'F':  case 'G':  case 'H':
				case 'I':  case 'J':  case 'K':  case 'L':
				case 'M':  case 'N':  case 'O':  case 'P':
				case 'Q':  case 'R':  case 'S':  case 'T':
				case 'U':  case 'V':  case 'W':  case 'X':
				case 'Y':  case 'Z':  case 'a':  case 'b':
				case 'c':  case 'd':  case 'e':  case 'f':
				case 'g':  case 'h':  case 'i':  case 'j':
				case 'k':  case 'l':  case 'm':  case 'n':
				case 'o':  case 'p':  case 'q':  case 'r':
				case 's':  case 't':  case 'u':  case 'v':
				case 'w':  case 'x':  case 'y':  case 'z':
				{
					mLETTER(false);
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					mDIGIT(false);
					break;
				}
				case ' ':  case '!':  case '#':  case '$':
				case '%':  case '&':  case '(':  case ')':
				case '*':  case '+':  case ',':  case '-':
				case '.':  case '/':  case ':':  case '<':
				case '=':  case '>':  case '?':  case '@':
				case '[':  case ']':  case '_':  case '{':
				case '}':
				{
					mSP(false);
					break;
				}
				default:
				{
					if ( _cnt38>=1 ) { break _loop38; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				_cnt38++;
			} while (true);
			}
			match("'");
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		switch ( LA(1)) {
		case '@':
		{
			mLANG(false);
			break;
		}
		case '^':
		{
			mDATATYPE(false);
			break;
		}
		default:
			{
			}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mLETTER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LETTER;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			matchRange('a','z');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			matchRange('A','Z');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mSP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SP;
		int _saveIndex;
		
		switch ( LA(1)) {
		case ' ':
		{
			match(' ');
			break;
		}
		case '$':
		{
			match('$');
			break;
		}
		case '@':
		{
			match('@');
			break;
		}
		case '&':
		{
			match('&');
			break;
		}
		case '{':
		{
			match('{');
			break;
		}
		case '}':
		{
			match('}');
			break;
		}
		case '!':
		{
			match('!');
			break;
		}
		case '?':
		{
			match('?');
			break;
		}
		case '%':
		{
			match('%');
			break;
		}
		case '*':
		{
			match('*');
			break;
		}
		case ':':
		{
			match(':');
			break;
		}
		case '_':
		{
			match('_');
			break;
		}
		case '-':
		{
			match('-');
			break;
		}
		case '#':
		{
			match('#');
			break;
		}
		case '+':
		{
			match('+');
			break;
		}
		case '[':
		{
			mLSQBR(false);
			break;
		}
		case ']':
		{
			mRSQBR(false);
			break;
		}
		case '(':
		{
			mLPAREN(false);
			break;
		}
		case ')':
		{
			mRPAREN(false);
			break;
		}
		case '/':
		{
			mSLASHOP(false);
			break;
		}
		case ',':
		{
			mCOMMA(false);
			break;
		}
		case '.':
		{
			mSELFABBR(false);
			break;
		}
		case '>':
		{
			mSUPOP(false);
			break;
		}
		case '<':
		{
			mINFOP(false);
			break;
		}
		case '=':
		{
			mEQUALOP(false);
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mLANG(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LANG;
		int _saveIndex;
		
		match("@");
		mNCNAME(false);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDATATYPE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DATATYPE;
		int _saveIndex;
		
		match("^^");
		{
		switch ( LA(1)) {
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':  case '_':  case 'a':
		case 'b':  case 'c':  case 'd':  case 'e':
		case 'f':  case 'g':  case 'h':  case 'i':
		case 'j':  case 'k':  case 'l':  case 'm':
		case 'n':  case 'o':  case 'p':  case 'q':
		case 'r':  case 's':  case 't':  case 'u':
		case 'v':  case 'w':  case 'x':  case 'y':
		case 'z':
		{
			mNCNAME(false);
			break;
		}
		case ':':
		{
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		match(':');
		mNCNAME(false);
		if ( inputState.guessing==0 ) {
			_ttype = QNAME;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mNCNAME(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NCNAME;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':  case 'a':  case 'b':
		case 'c':  case 'd':  case 'e':  case 'f':
		case 'g':  case 'h':  case 'i':  case 'j':
		case 'k':  case 'l':  case 'm':  case 'n':
		case 'o':  case 'p':  case 'q':  case 'r':
		case 's':  case 't':  case 'u':  case 'v':
		case 'w':  case 'x':  case 'y':  case 'z':
		{
			mLETTER(false);
			break;
		}
		case '_':
		{
			match('_');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		_loop114:
		do {
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case 'A':  case 'B':
			case 'C':  case 'D':  case 'E':  case 'F':
			case 'G':  case 'H':  case 'I':  case 'J':
			case 'K':  case 'L':  case 'M':  case 'N':
			case 'O':  case 'P':  case 'Q':  case 'R':
			case 'S':  case 'T':  case 'U':  case 'V':
			case 'W':  case 'X':  case 'Y':  case 'Z':
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':  case 'g':  case 'h':
			case 'i':  case 'j':  case 'k':  case 'l':
			case 'm':  case 'n':  case 'o':  case 'p':
			case 'q':  case 'r':  case 's':  case 't':
			case 'u':  case 'v':  case 'w':  case 'x':
			case 'y':  case 'z':
			{
				mNCNAMECHAR(false);
				break;
			}
			case '_':
			{
				match('_');
				break;
			}
			default:
			{
				break _loop114;
			}
			}
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNAME(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NAME;
		int _saveIndex;
		
		boolean synPredMatched45 = false;
		if (((LA(1)=='i') && (LA(2)=='n'))) {
			int _m45 = mark();
			synPredMatched45 = true;
			inputState.guessing++;
			try {
				{
				match("in::");
				}
			}
			catch (RecognitionException pe) {
				synPredMatched45 = false;
			}
			rewind(_m45);
			inputState.guessing--;
		}
		if ( synPredMatched45 ) {
			match("in::");
			if ( inputState.guessing==0 ) {
				_ttype = AXIS;
			}
		}
		else {
			boolean synPredMatched47 = false;
			if (((LA(1)=='o') && (LA(2)=='u'))) {
				int _m47 = mark();
				synPredMatched47 = true;
				inputState.guessing++;
				try {
					{
					match("out::");
					}
				}
				catch (RecognitionException pe) {
					synPredMatched47 = false;
				}
				rewind(_m47);
				inputState.guessing--;
			}
			if ( synPredMatched47 ) {
				match("out::");
				if ( inputState.guessing==0 ) {
					_ttype = AXIS;
				}
			}
			else {
				boolean synPredMatched49 = false;
				if (((LA(1)=='s') && (LA(2)=='e'))) {
					int _m49 = mark();
					synPredMatched49 = true;
					inputState.guessing++;
					try {
						{
						match("self::");
						}
					}
					catch (RecognitionException pe) {
						synPredMatched49 = false;
					}
					rewind(_m49);
					inputState.guessing--;
				}
				if ( synPredMatched49 ) {
					match("self::");
					if ( inputState.guessing==0 ) {
						_ttype = AXIS;
					}
				}
				else {
					boolean synPredMatched51 = false;
					if (((LA(1)=='a') && (LA(2)=='n'))) {
						int _m51 = mark();
						synPredMatched51 = true;
						inputState.guessing++;
						try {
							{
							match("and");
							}
						}
						catch (RecognitionException pe) {
							synPredMatched51 = false;
						}
						rewind(_m51);
						inputState.guessing--;
					}
					if ( synPredMatched51 ) {
						match("and");
						if ( inputState.guessing==0 ) {
							_ttype = ANDOP;
						}
					}
					else {
						boolean synPredMatched53 = false;
						if (((LA(1)=='o') && (LA(2)=='r'))) {
							int _m53 = mark();
							synPredMatched53 = true;
							inputState.guessing++;
							try {
								{
								match("or");
								}
							}
							catch (RecognitionException pe) {
								synPredMatched53 = false;
							}
							rewind(_m53);
							inputState.guessing--;
						}
						if ( synPredMatched53 ) {
							match("or");
							if ( inputState.guessing==0 ) {
								_ttype = OROP;
							}
						}
						else {
							boolean synPredMatched57 = false;
							if (((LA(1)=='e') && (LA(2)=='x'))) {
								int _m57 = mark();
								synPredMatched57 = true;
								inputState.guessing++;
								try {
									{
									match("exp");
									}
								}
								catch (RecognitionException pe) {
									synPredMatched57 = false;
								}
								rewind(_m57);
								inputState.guessing--;
							}
							if ( synPredMatched57 ) {
								match("exp");
								if ( inputState.guessing==0 ) {
									_ttype = FUNCTIONNAME;
								}
							}
							else {
								boolean synPredMatched59 = false;
								if (((LA(1)=='t') && (LA(2)=='e'))) {
									int _m59 = mark();
									synPredMatched59 = true;
									inputState.guessing++;
									try {
										{
										match("text()");
										mLANG(false);
										}
									}
									catch (RecognitionException pe) {
										synPredMatched59 = false;
									}
									rewind(_m59);
									inputState.guessing--;
								}
								if ( synPredMatched59 ) {
									match("text()");
									mLANG(false);
									if ( inputState.guessing==0 ) {
										_ttype = TEXTLANG;
									}
								}
								else {
									boolean synPredMatched61 = false;
									if (((LA(1)=='t') && (LA(2)=='e'))) {
										int _m61 = mark();
										synPredMatched61 = true;
										inputState.guessing++;
										try {
											{
											match("text()");
											mDATATYPE(false);
											}
										}
										catch (RecognitionException pe) {
											synPredMatched61 = false;
										}
										rewind(_m61);
										inputState.guessing--;
									}
									if ( synPredMatched61 ) {
										match("text()");
										mDATATYPE(false);
										if ( inputState.guessing==0 ) {
											_ttype = TEXTDT;
										}
									}
									else {
										boolean synPredMatched63 = false;
										if (((LA(1)=='t') && (LA(2)=='e'))) {
											int _m63 = mark();
											synPredMatched63 = true;
											inputState.guessing++;
											try {
												{
												match("text()");
												}
											}
											catch (RecognitionException pe) {
												synPredMatched63 = false;
											}
											rewind(_m63);
											inputState.guessing--;
										}
										if ( synPredMatched63 ) {
											match("text()");
											if ( inputState.guessing==0 ) {
												_ttype = TEXT;
											}
										}
										else {
											boolean synPredMatched65 = false;
											if (((LA(1)=='c') && (LA(2)=='o'))) {
												int _m65 = mark();
												synPredMatched65 = true;
												inputState.guessing++;
												try {
													{
													match("count");
													}
												}
												catch (RecognitionException pe) {
													synPredMatched65 = false;
												}
												rewind(_m65);
												inputState.guessing--;
											}
											if ( synPredMatched65 ) {
												match("count");
												if ( inputState.guessing==0 ) {
													_ttype = FUNCTIONNAME;
												}
											}
											else {
												boolean synPredMatched67 = false;
												if (((LA(1)=='l') && (LA(2)=='o'))) {
													int _m67 = mark();
													synPredMatched67 = true;
													inputState.guessing++;
													try {
														{
														match("local-name");
														}
													}
													catch (RecognitionException pe) {
														synPredMatched67 = false;
													}
													rewind(_m67);
													inputState.guessing--;
												}
												if ( synPredMatched67 ) {
													match("local-name");
													if ( inputState.guessing==0 ) {
														_ttype = FUNCTIONNAME;
													}
												}
												else {
													boolean synPredMatched69 = false;
													if (((LA(1)=='n') && (LA(2)=='a'))) {
														int _m69 = mark();
														synPredMatched69 = true;
														inputState.guessing++;
														try {
															{
															match("namespace-uri");
															}
														}
														catch (RecognitionException pe) {
															synPredMatched69 = false;
														}
														rewind(_m69);
														inputState.guessing--;
													}
													if ( synPredMatched69 ) {
														match("namespace-uri");
														if ( inputState.guessing==0 ) {
															_ttype = FUNCTIONNAME;
														}
													}
													else {
														boolean synPredMatched71 = false;
														if (((LA(1)=='u') && (LA(2)=='r'))) {
															int _m71 = mark();
															synPredMatched71 = true;
															inputState.guessing++;
															try {
																{
																match("uri");
																}
															}
															catch (RecognitionException pe) {
																synPredMatched71 = false;
															}
															rewind(_m71);
															inputState.guessing--;
														}
														if ( synPredMatched71 ) {
															match("uri");
															if ( inputState.guessing==0 ) {
																_ttype = FUNCTIONNAME;
															}
														}
														else {
															boolean synPredMatched73 = false;
															if (((LA(1)=='l') && (LA(2)=='i'))) {
																int _m73 = mark();
																synPredMatched73 = true;
																inputState.guessing++;
																try {
																	{
																	match("literal-value");
																	}
																}
																catch (RecognitionException pe) {
																	synPredMatched73 = false;
																}
																rewind(_m73);
																inputState.guessing--;
															}
															if ( synPredMatched73 ) {
																match("literal-value");
																if ( inputState.guessing==0 ) {
																	_ttype = FUNCTIONNAME;
																}
															}
															else {
																boolean synPredMatched75 = false;
																if (((LA(1)=='l') && (LA(2)=='i'))) {
																	int _m75 = mark();
																	synPredMatched75 = true;
																	inputState.guessing++;
																	try {
																		{
																		match("literal-dt");
																		}
																	}
																	catch (RecognitionException pe) {
																		synPredMatched75 = false;
																	}
																	rewind(_m75);
																	inputState.guessing--;
																}
																if ( synPredMatched75 ) {
																	match("literal-dt");
																	if ( inputState.guessing==0 ) {
																		_ttype = FUNCTIONNAME;
																	}
																}
																else {
																	boolean synPredMatched77 = false;
																	if (((LA(1)=='s') && (LA(2)=='t'))) {
																		int _m77 = mark();
																		synPredMatched77 = true;
																		inputState.guessing++;
																		try {
																			{
																			match("string-length");
																			}
																		}
																		catch (RecognitionException pe) {
																			synPredMatched77 = false;
																		}
																		rewind(_m77);
																		inputState.guessing--;
																	}
																	if ( synPredMatched77 ) {
																		match("string-length");
																		if ( inputState.guessing==0 ) {
																			_ttype = FUNCTIONNAME;
																		}
																	}
																	else {
																		boolean synPredMatched79 = false;
																		if (((LA(1)=='s') && (LA(2)=='t'))) {
																			int _m79 = mark();
																			synPredMatched79 = true;
																			inputState.guessing++;
																			try {
																				{
																				match("string");
																				}
																			}
																			catch (RecognitionException pe) {
																				synPredMatched79 = false;
																			}
																			rewind(_m79);
																			inputState.guessing--;
																		}
																		if ( synPredMatched79 ) {
																			match("string");
																			if ( inputState.guessing==0 ) {
																				_ttype = FUNCTIONNAME;
																			}
																		}
																		else {
																			boolean synPredMatched81 = false;
																			if (((LA(1)=='s') && (LA(2)=='t'))) {
																				int _m81 = mark();
																				synPredMatched81 = true;
																				inputState.guessing++;
																				try {
																					{
																					match("starts-with");
																					}
																				}
																				catch (RecognitionException pe) {
																					synPredMatched81 = false;
																				}
																				rewind(_m81);
																				inputState.guessing--;
																			}
																			if ( synPredMatched81 ) {
																				match("starts-with");
																				if ( inputState.guessing==0 ) {
																					_ttype = FUNCTIONNAME;
																				}
																			}
																			else {
																				boolean synPredMatched83 = false;
																				if (((LA(1)=='c') && (LA(2)=='o'))) {
																					int _m83 = mark();
																					synPredMatched83 = true;
																					inputState.guessing++;
																					try {
																						{
																						match("contains");
																						}
																					}
																					catch (RecognitionException pe) {
																						synPredMatched83 = false;
																					}
																					rewind(_m83);
																					inputState.guessing--;
																				}
																				if ( synPredMatched83 ) {
																					match("contains");
																					if ( inputState.guessing==0 ) {
																						_ttype = FUNCTIONNAME;
																					}
																				}
																				else {
																					boolean synPredMatched85 = false;
																					if (((LA(1)=='c') && (LA(2)=='o'))) {
																						int _m85 = mark();
																						synPredMatched85 = true;
																						inputState.guessing++;
																						try {
																							{
																							match("concat");
																							}
																						}
																						catch (RecognitionException pe) {
																							synPredMatched85 = false;
																						}
																						rewind(_m85);
																						inputState.guessing--;
																					}
																					if ( synPredMatched85 ) {
																						match("concat");
																						if ( inputState.guessing==0 ) {
																							_ttype = FUNCTIONNAME;
																						}
																					}
																					else {
																						boolean synPredMatched87 = false;
																						if (((LA(1)=='s') && (LA(2)=='u'))) {
																							int _m87 = mark();
																							synPredMatched87 = true;
																							inputState.guessing++;
																							try {
																								{
																								match("substring-before");
																								}
																							}
																							catch (RecognitionException pe) {
																								synPredMatched87 = false;
																							}
																							rewind(_m87);
																							inputState.guessing--;
																						}
																						if ( synPredMatched87 ) {
																							match("substring-before");
																							if ( inputState.guessing==0 ) {
																								_ttype = FUNCTIONNAME;
																							}
																						}
																						else {
																							boolean synPredMatched89 = false;
																							if (((LA(1)=='s') && (LA(2)=='u'))) {
																								int _m89 = mark();
																								synPredMatched89 = true;
																								inputState.guessing++;
																								try {
																									{
																									match("substring-after");
																									}
																								}
																								catch (RecognitionException pe) {
																									synPredMatched89 = false;
																								}
																								rewind(_m89);
																								inputState.guessing--;
																							}
																							if ( synPredMatched89 ) {
																								match("substring-after");
																								if ( inputState.guessing==0 ) {
																									_ttype = FUNCTIONNAME;
																								}
																							}
																							else {
																								boolean synPredMatched91 = false;
																								if (((LA(1)=='s') && (LA(2)=='u'))) {
																									int _m91 = mark();
																									synPredMatched91 = true;
																									inputState.guessing++;
																									try {
																										{
																										match("substring");
																										}
																									}
																									catch (RecognitionException pe) {
																										synPredMatched91 = false;
																									}
																									rewind(_m91);
																									inputState.guessing--;
																								}
																								if ( synPredMatched91 ) {
																									match("substring");
																									if ( inputState.guessing==0 ) {
																										_ttype = FUNCTIONNAME;
																									}
																								}
																								else {
																									boolean synPredMatched93 = false;
																									if (((LA(1)=='b') && (LA(2)=='o'))) {
																										int _m93 = mark();
																										synPredMatched93 = true;
																										inputState.guessing++;
																										try {
																											{
																											match("boolean");
																											}
																										}
																										catch (RecognitionException pe) {
																											synPredMatched93 = false;
																										}
																										rewind(_m93);
																										inputState.guessing--;
																									}
																									if ( synPredMatched93 ) {
																										match("boolean");
																										if ( inputState.guessing==0 ) {
																											_ttype = FUNCTIONNAME;
																										}
																									}
																									else {
																										boolean synPredMatched95 = false;
																										if (((LA(1)=='n') && (LA(2)=='o'))) {
																											int _m95 = mark();
																											synPredMatched95 = true;
																											inputState.guessing++;
																											try {
																												{
																												match("not");
																												}
																											}
																											catch (RecognitionException pe) {
																												synPredMatched95 = false;
																											}
																											rewind(_m95);
																											inputState.guessing--;
																										}
																										if ( synPredMatched95 ) {
																											match("not");
																											if ( inputState.guessing==0 ) {
																												_ttype = FUNCTIONNAME;
																											}
																										}
																										else {
																											boolean synPredMatched97 = false;
																											if (((LA(1)=='t') && (LA(2)=='r'))) {
																												int _m97 = mark();
																												synPredMatched97 = true;
																												inputState.guessing++;
																												try {
																													{
																													match("true");
																													}
																												}
																												catch (RecognitionException pe) {
																													synPredMatched97 = false;
																												}
																												rewind(_m97);
																												inputState.guessing--;
																											}
																											if ( synPredMatched97 ) {
																												match("true");
																												if ( inputState.guessing==0 ) {
																													_ttype = FUNCTIONNAME;
																												}
																											}
																											else {
																												boolean synPredMatched99 = false;
																												if (((LA(1)=='f') && (LA(2)=='a'))) {
																													int _m99 = mark();
																													synPredMatched99 = true;
																													inputState.guessing++;
																													try {
																														{
																														match("false");
																														}
																													}
																													catch (RecognitionException pe) {
																														synPredMatched99 = false;
																													}
																													rewind(_m99);
																													inputState.guessing--;
																												}
																												if ( synPredMatched99 ) {
																													match("false");
																													if ( inputState.guessing==0 ) {
																														_ttype = FUNCTIONNAME;
																													}
																												}
																												else {
																													boolean synPredMatched101 = false;
																													if (((LA(1)=='n') && (LA(2)=='o'))) {
																														int _m101 = mark();
																														synPredMatched101 = true;
																														inputState.guessing++;
																														try {
																															{
																															match("normalize-space");
																															}
																														}
																														catch (RecognitionException pe) {
																															synPredMatched101 = false;
																														}
																														rewind(_m101);
																														inputState.guessing--;
																													}
																													if ( synPredMatched101 ) {
																														match("normalize-space");
																														if ( inputState.guessing==0 ) {
																															_ttype = FUNCTIONNAME;
																														}
																													}
																													else {
																														boolean synPredMatched103 = false;
																														if (((LA(1)=='n') && (LA(2)=='u'))) {
																															int _m103 = mark();
																															synPredMatched103 = true;
																															inputState.guessing++;
																															try {
																																{
																																match("number");
																																}
																															}
																															catch (RecognitionException pe) {
																																synPredMatched103 = false;
																															}
																															rewind(_m103);
																															inputState.guessing--;
																														}
																														if ( synPredMatched103 ) {
																															match("number");
																															if ( inputState.guessing==0 ) {
																																_ttype = FUNCTIONNAME;
																															}
																														}
																														else {
																															boolean synPredMatched105 = false;
																															if (((LA(1)=='b') && (LA(2)=='l'))) {
																																int _m105 = mark();
																																synPredMatched105 = true;
																																inputState.guessing++;
																																try {
																																	{
																																	match("blank");
																																	}
																																}
																																catch (RecognitionException pe) {
																																	synPredMatched105 = false;
																																}
																																rewind(_m105);
																																inputState.guessing--;
																															}
																															if ( synPredMatched105 ) {
																																match("blank");
																																if ( inputState.guessing==0 ) {
																																	_ttype = FUNCTIONNAME;
																																}
																															}
																															else {
																																boolean synPredMatched108 = false;
																																if (((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2))))) {
																																	int _m108 = mark();
																																	synPredMatched108 = true;
																																	inputState.guessing++;
																																	try {
																																		{
																																		{
																																		switch ( LA(1)) {
																																		case 'A':  case 'B':  case 'C':  case 'D':
																																		case 'E':  case 'F':  case 'G':  case 'H':
																																		case 'I':  case 'J':  case 'K':  case 'L':
																																		case 'M':  case 'N':  case 'O':  case 'P':
																																		case 'Q':  case 'R':  case 'S':  case 'T':
																																		case 'U':  case 'V':  case 'W':  case 'X':
																																		case 'Y':  case 'Z':  case '_':  case 'a':
																																		case 'b':  case 'c':  case 'd':  case 'e':
																																		case 'f':  case 'g':  case 'h':  case 'i':
																																		case 'j':  case 'k':  case 'l':  case 'm':
																																		case 'n':  case 'o':  case 'p':  case 'q':
																																		case 'r':  case 's':  case 't':  case 'u':
																																		case 'v':  case 'w':  case 'x':  case 'y':
																																		case 'z':
																																		{
																																			mNCNAME(false);
																																			break;
																																		}
																																		case ':':
																																		{
																																			break;
																																		}
																																		default:
																																		{
																																			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
																																		}
																																		}
																																		}
																																		match(":*");
																																		}
																																	}
																																	catch (RecognitionException pe) {
																																		synPredMatched108 = false;
																																	}
																																	rewind(_m108);
																																	inputState.guessing--;
																																}
																																if ( synPredMatched108 ) {
																																	{
																																	switch ( LA(1)) {
																																	case 'A':  case 'B':  case 'C':  case 'D':
																																	case 'E':  case 'F':  case 'G':  case 'H':
																																	case 'I':  case 'J':  case 'K':  case 'L':
																																	case 'M':  case 'N':  case 'O':  case 'P':
																																	case 'Q':  case 'R':  case 'S':  case 'T':
																																	case 'U':  case 'V':  case 'W':  case 'X':
																																	case 'Y':  case 'Z':  case '_':  case 'a':
																																	case 'b':  case 'c':  case 'd':  case 'e':
																																	case 'f':  case 'g':  case 'h':  case 'i':
																																	case 'j':  case 'k':  case 'l':  case 'm':
																																	case 'n':  case 'o':  case 'p':  case 'q':
																																	case 'r':  case 's':  case 't':  case 'u':
																																	case 'v':  case 'w':  case 'x':  case 'y':
																																	case 'z':
																																	{
																																		mNCNAME(false);
																																		break;
																																	}
																																	case ':':
																																	{
																																		break;
																																	}
																																	default:
																																	{
																																		throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
																																	}
																																	}
																																	}
																																	match(":*");
																																	if ( inputState.guessing==0 ) {
																																		_ttype = QNAME;
																																	}
																																}
																																else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
																																	{
																																	switch ( LA(1)) {
																																	case 'A':  case 'B':  case 'C':  case 'D':
																																	case 'E':  case 'F':  case 'G':  case 'H':
																																	case 'I':  case 'J':  case 'K':  case 'L':
																																	case 'M':  case 'N':  case 'O':  case 'P':
																																	case 'Q':  case 'R':  case 'S':  case 'T':
																																	case 'U':  case 'V':  case 'W':  case 'X':
																																	case 'Y':  case 'Z':  case '_':  case 'a':
																																	case 'b':  case 'c':  case 'd':  case 'e':
																																	case 'f':  case 'g':  case 'h':  case 'i':
																																	case 'j':  case 'k':  case 'l':  case 'm':
																																	case 'n':  case 'o':  case 'p':  case 'q':
																																	case 'r':  case 's':  case 't':  case 'u':
																																	case 'v':  case 'w':  case 'x':  case 'y':
																																	case 'z':
																																	{
																																		mNCNAME(false);
																																		break;
																																	}
																																	case ':':
																																	{
																																		break;
																																	}
																																	default:
																																	{
																																		throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
																																	}
																																	}
																																	}
																																	match(':');
																																	mNCNAME(false);
																																	if ( inputState.guessing==0 ) {
																																		_ttype = QNAME;
																																	}
																																}
																																else {
																																	boolean synPredMatched55 = false;
																																	if (((LA(1)=='*'))) {
																																		int _m55 = mark();
																																		synPredMatched55 = true;
																																		inputState.guessing++;
																																		try {
																																			{
																																			match('*');
																																			}
																																		}
																																		catch (RecognitionException pe) {
																																			synPredMatched55 = false;
																																		}
																																		rewind(_m55);
																																		inputState.guessing--;
																																	}
																																	if ( synPredMatched55 ) {
																																		match('*');
																																		if ( inputState.guessing==0 ) {
																																			_ttype = ANYNAME;
																																		}
																																	}
																																	else {
																																		throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
																																	}
																																	}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}
																																	if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
																																		_token = makeToken(_ttype);
																																		_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
																																	}
																																	_returnToken = _token;
																																}
																																
	protected final void mNCNAMECHAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NCNAMECHAR;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':  case 'a':  case 'b':
		case 'c':  case 'd':  case 'e':  case 'f':
		case 'g':  case 'h':  case 'i':  case 'j':
		case 'k':  case 'l':  case 'm':  case 'n':
		case 'o':  case 'p':  case 'q':  case 'r':
		case 's':  case 't':  case 'u':  case 'v':
		case 'w':  case 'x':  case 'y':  case 'z':
		{
			mLETTER(false);
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			mDIGIT(false);
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLSQBR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LSQBR;
		int _saveIndex;
		
		match('[');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRSQBR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RSQBR;
		int _saveIndex;
		
		match(']');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LPAREN;
		int _saveIndex;
		
		match('(');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RPAREN;
		int _saveIndex;
		
		match(')');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSLASHOP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SLASHOP;
		int _saveIndex;
		
		match('/');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMA;
		int _saveIndex;
		
		match(',');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSELFABBR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SELFABBR;
		int _saveIndex;
		
		match('.');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSUPOP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SUPOP;
		int _saveIndex;
		
		match('>');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mINFOP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = INFOP;
		int _saveIndex;
		
		match('<');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mEQUALOP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EQUALOP;
		int _saveIndex;
		
		match('=');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMSUBOP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MSUBOP;
		int _saveIndex;
		
		match('^');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDIFFOP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIFFOP;
		int _saveIndex;
		
		match("!=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mINFEQOP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = INFEQOP;
		int _saveIndex;
		
		match("<=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSUPEQOP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SUPEQOP;
		int _saveIndex;
		
		match(">=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case ' ':
		{
			match(' ');
			break;
		}
		case '\r':
		{
			match('\r');
			match('\n');
			break;
		}
		case '\n':
		{
			match('\n');
			break;
		}
		case '\t':
		{
			match('\t');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			_ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 288230376151711744L, 576460745995190270L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 576183675373223936L, 576460745995190270L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 576179277326712832L, 576460745995190270L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	
	}
