/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

/**
 * A token represents an identifier or a reserved word or a number.
 *
 * @author marias
 */
public class ElviraToken {

	// Attributes
	private String identifierString;

	private ReservedWord reservedWord;

	private int integerValue;

	private double doubleValue;

	private boolean booleanValue;

	private String stringValue1;

	private String stringValue2;

	private String[] stringListValue;

	private double[] doublesTableValue;

	private boolean isReservedWord;

	private boolean isIdentifier;

	private boolean isInteger;

	private boolean isDouble;

	private boolean isBoolean;

	private TokenType tokenType;

	// Constructors

	/**
	 * Integer value constructor.
	 *
	 * @param tokenType    <code>TokenType</code>
	 * @param integerValue <code>int</code>
	 */
	public ElviraToken(TokenType tokenType, int integerValue) {
		this.tokenType = tokenType;
		this.integerValue = integerValue;
		this.isInteger = true;
	}

	/**
	 * Double value constructor.
	 *
	 * @param tokenType   <code>TokenType</code>
	 * @param doubleValue <code>double</code>
	 */
	public ElviraToken(TokenType tokenType, double doubleValue) {
		this.tokenType = tokenType;
		this.doubleValue = doubleValue;
		this.isDouble = true;
	}

	/**
	 * Identifier constructor.
	 *
	 * @param tokenType        <code>TokenType</code>
	 * @param identifierString <code>String</code>
	 * tokenType == IDENTIFIER during the whole method execution
	 */
	public ElviraToken(TokenType tokenType, String identifierString) {
		this.tokenType = tokenType;
		this.identifierString = identifierString;
		this.isIdentifier = true;
	}

	/**
	 * Reserved word constructor.
	 *
	 * @param tokenType    <code>TokenType</code>
	 * @param reservedWord <code>ReservedWord</code>
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord) {
		this.tokenType = tokenType;
		this.reservedWord = reservedWord;
		this.isReservedWord = true;
	}

	/**
	 * Reserved word constructor.
	 *
	 * @param tokenType    <code>TokenType</code>
	 * @param reservedWord <code>ReservedWord</code>
	 * @param integerValue <code>int</code>
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, int integerValue) {
		this(tokenType, reservedWord);
		this.integerValue = integerValue;
		this.isInteger = true;
	}

	/**
	 * Reserved word constructor.
	 *
	 * @param tokenType    <code>TokenType</code>
	 * @param reservedWord <code>ReservedWord</code>
	 * @param doubleValue  <code>double</code>
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, double doubleValue) {
		this(tokenType, reservedWord);
		this.doubleValue = doubleValue;
		this.isDouble = true;
	}

	/**
	 * Boolean value constructor.
	 *
	 * @param tokenType    <code>TokenType</code>
	 * @param booleanValue <code>boolean</code>
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, boolean booleanValue) {
		this(tokenType, reservedWord);
		this.booleanValue = booleanValue;
		this.isBoolean = true;
	}

	/**
	 * Reserved word constructor.
	 *
	 * @param tokenType    <code>TokenType</code>
	 * @param reservedWord <code>ReservedWord</code>
	 * @param stringValue1 <code>String</code>
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, String stringValue1) {
		this(tokenType, reservedWord);
		this.stringValue1 = stringValue1;
	}

	/**
	 * Reserved word constructor.
	 *
	 * @param tokenType    <code>TokenType</code>
	 * @param reservedWord <code>ReservedWord</code>
	 * @param stringValue1 <code>String</code>
	 * @param stringValue2 <code>String</code>
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, String stringValue1, String stringValue2) {
		this(tokenType, reservedWord);
		this.stringValue1 = stringValue1;
		this.stringValue2 = stringValue2;
	}

	/**
	 * Reserved word constructor for words with a list of strings.
	 *
	 * @param tokenType       <code>TokenType</code>
	 * @param reservedWord    <code>ReservedWord</code>
	 * @param stringListValue <code>String[]</code>
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, String[] stringListValue) {
		this(tokenType, reservedWord);
		this.stringListValue = stringListValue;
	}

	/**
	 * Reserved word constructor for tables of doubles.
	 *
	 * @param tokenType    <code>TokenType</code>
	 * @param reservedWord <code>ReservedWord</code>
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, double[] table) {
		this(tokenType, reservedWord);
		this.doublesTableValue = table;
	}

	// Methods

	/**
	 * @return Reserved word of this token. <code>ReservedWord</code>
	 */
	public ReservedWord getReservedWord() {
		return reservedWord;
	}

	/**
	 * @return isInteger. <code>boolean</code>
	 */
	public boolean isInteger() {
		return isInteger;
	}

	/**
	 * @return isDouble. <code>boolean</code>
	 */
	public boolean isDouble() {
		return isDouble;
	}

	/**
	 * @return isReservedWord. <code>boolean</code>
	 */
	public boolean isReservedWord() {
		return isReservedWord;
	}

	/**
	 * @return isIdentifier. <code>boolean</code>
	 */
	public boolean isIdentifier() {
		return isIdentifier;
	}

	/**
	 * @return isBoolean. <code>boolean</code>
	 */
	public boolean isBoolean() {
		return isBoolean;
	}

	/**
	 * @return integerValue. <code>int</code>
	 */
	public int getIntegerValue() {
		return integerValue;
	}

	/**
	 * @return doubleValue. <code>double</code>
	 */
	public double getDoubleValue() {
		return doubleValue;
	}

	/**
	 * @return stringValue. <code>String</code>
	 */
	public String getStringValue1() {
		return stringValue1;
	}

	/**
	 * @return stringValue. <code>String</code>
	 */
	public String getStringValue2() {
		return stringValue2;
	}

	/**
	 * @return identifierString. <code>String</code>
	 */
	public String getIdentifierString() {
		return identifierString;
	}

	/**
	 * @return booleanValue. <code>boolean</code>
	 */
	public boolean getBooleanValue() {
		return booleanValue;
	}

	/**
	 * @return stringListValue. <code>String[]</code>
	 */
	public String[] getStringListValue() {
		return stringListValue;
	}

	/**
	 * @return doublesTableValue. <code>double[]</code>
	 */
	public double[] getDoublesTableValue() {
		return doublesTableValue;
	}

	/**
	 * @return tokenType. <code>Enumerate TokenType</code>
	 */
	public TokenType getTokenType() {
		return tokenType;
	}

	/**
	 * @param token <code>ElviraToken</code>
	 * @return boolean
	 */
	public boolean sameToken(ElviraToken token) {
		boolean equalStringList;
		equalStringList = (
				(token.stringListValue == stringListValue) && (stringListValue == null)
		);
		equalStringList = !equalStringList && (token.stringListValue == null);

		return false;
	}

	public String toString() {
		String string = new String();
		string = string + "Token type: " + tokenType + "\n";
		if (isReservedWord) {
			string = string + "Reserved word: " + reservedWord;
			if (stringValue1 != null) {
				string = string + "(" + stringValue1;
				if (stringValue2 != null) {
					string = string + "," + stringValue2 + ")";
				} else {
					string = string + ")\n";
				}
			} else {
				string = string + "\n";
			}
		} else if (isIdentifier) {
			string = string + "Identifier: " + identifierString;
		}
		if (isInteger) {
			string = string + "Value = " + integerValue;
		}
		if (isDouble) {
			string = string + "Value = " + doubleValue;
		}
		if (isBoolean) {
			string = string + "Value = " + booleanValue;
		}
		if (stringListValue != null) {
			string = string + "Value = ";
			for (String stringValue : stringListValue) {
				string = string + " " + stringValue;
			}
		}
		if (doublesTableValue != null) {
			string = string + "Value = ";
			for (int i = 0; i < doublesTableValue.length; i++) {
				string = string + doublesTableValue[i];
			}
		}
		return string;
	}

}
