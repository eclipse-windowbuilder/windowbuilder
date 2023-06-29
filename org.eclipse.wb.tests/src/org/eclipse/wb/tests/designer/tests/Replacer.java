/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.tests;

import org.eclipse.wb.internal.core.utils.IOUtils2;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Replacer {
	private static final String TESTS_SRC = "C:\\eclipsePL\\workspace\\org.eclipse.wb.tests\\src";

	public static void main(String[] args) throws Exception {
		File srcFolder = new File(TESTS_SRC);
		visit(srcFolder);
	}

	private static void visit(File file) throws Exception {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				visit(child);
			}
		}
		if (file.isFile()
				&& file.getName().endsWith(".java")
				&& !file.getName().endsWith("Replacer.java")) {
			/*String name = file.getAbsolutePath().substring(TESTS_SRC.length() + 1);
      System.out.println(name);*/
			replaceInFile(file);
		}
	}

	private static void replaceInFile(File file) throws Exception {
		String originalContent = IOUtils2.readString(file);
		//System.out.println(file);
		String s = replaceInString(originalContent);
		if (!originalContent.equals(s)) {
			System.out.println(file);
			//System.out.println(s);
			/*FileOutputStream stream = new FileOutputStream(file);
      stream.write(s.getBytes());
      stream.close();*/
		}
	}

	private static String replaceInString(String s) {
		int invocationLast = 0;
		Pattern patternBegin =
				Pattern.compile("createTypeDeclaration\\(\\s*\"test\",\\s*\"Test\\.java\", getSourceDQ\\(");
		Pattern patternEnd = Pattern.compile("\"\\)\\);");
		while (true) {
			// prepare begin of "invocation"
			int invocationBegin;
			String begin;
			{
				Matcher matcher = patternBegin.matcher(s);
				if (!matcher.find(invocationLast)) {
					break;
				}
				invocationBegin = matcher.start();
				begin = matcher.group();
				//System.out.println(begin);
			}
			//
			//int invocationEnd = s.indexOf("\"}), m_lastEditor);\r\n", invocationBegin);
			int invocationEnd;
			String end;
			{
				Matcher matcher = patternEnd.matcher(s);
				if (!matcher.find(invocationBegin)) {
					System.out.println(s.substring(invocationBegin));
					System.out.println("!!!!!!!!!!!!!");
					System.exit(0);
				}
				invocationEnd = matcher.end();
				invocationEnd -= ");".length();
				end = s.substring(matcher.start(), invocationEnd);
				//System.out.println(end);
			}
			// process single "invocation"
			{
				String invocation = s.substring(invocationBegin, invocationEnd);
				if (invocation.contains(";\r\n")) {
					System.out.println(invocation);
					System.out.println("???????????");
					System.exit(0);
				}
				// begin/end
				invocation = StringUtils.removeStart(invocation, begin);
				invocation = StringUtils.removeEnd(invocation, end);
				//invocation = "createTypeDeclaration_Test0(" + invocation + "\")";
				invocation = "createTypeDeclaration_Test0(" + invocation;
				System.out.println(invocation);
				// replace " with '
				invocation = StringUtils.replace(invocation, "\\\"", "'");
				// apply updated "invocation"
				s = s.substring(0, invocationBegin) + invocation + s.substring(invocationEnd);
			}
			// next
			invocationLast = invocationBegin + 1;
		}
		return s;
	}
	/*private static String replaceInString(String s) {
  	int invocationLast = 0;
  	Pattern patternBegin =
  			Pattern.compile("assertEditor\\(\\s*getExtSource\\(\\s*new String\\[\\]\\{\\s*\"");
  	Pattern patternEnd = Pattern.compile("\"}\\),\\s*m_lastEditor\\);\r\n");
  	while (true) {
  		// prepare begin of "invocation"
  		int invocationBegin;
  		String begin;
  		{
  			Matcher matcher = patternBegin.matcher(s);
  			if (!matcher.find(invocationLast)) {
  				break;
  			}
  			invocationBegin = matcher.start();
  			begin = matcher.group();
  			System.out.println(begin);
  		}
  		//
  		//int invocationEnd = s.indexOf("\"}), m_lastEditor);\r\n", invocationBegin);
  		int invocationEnd;
  		String end;
  		{
  			Matcher matcher = patternEnd.matcher(s);
  			if (!matcher.find(invocationBegin)) {
  				System.out.println(s.substring(invocationBegin));
  				System.out.println("!!!!!!!!!!!!!");
  				System.exit(0);
  			}
  			invocationEnd = matcher.end();
  			invocationEnd -= ");\r\n".length();
  			end = s.substring(matcher.start(), invocationEnd);
  		}
  		System.out.println(end);
  		// process single "invocation"
  		{
  			String invocation = s.substring(invocationBegin, invocationEnd);
  			if (invocation.contains(";\r\n")) {
  				System.out.println(invocation);
  				System.out.println("???????????");
  				System.exit(0);
  			}
  			// begin/end
  			invocation = StringUtils.removeStart(invocation, begin);
  			invocation = StringUtils.removeEnd(invocation, end);
  			invocation = "assertEditor(\"" + invocation + '"';
  			// replace " with '
  			invocation = StringUtils.replace(invocation, "\\\"", "'");
  			// apply updated "invocation"
  			s = s.substring(0, invocationBegin) + invocation + s.substring(invocationEnd);
  		}
  		// next
  		invocationLast = invocationBegin + 1;
  	}
  	return s;
  }*/
	/*private static String replaceInString(String s) {
  	int invocationLast = 0;
  	Pattern pattern =
  		Pattern.compile("ETestUtils\\.parseTestSource\\(" + "\\s*this,\\s*new String\\[\\]\\{");
  	while (true) {
  		// prepare begin/end of "invocation"
  		Matcher matcher = pattern.matcher(s);
  		if (!matcher.find(invocationLast)) {
  			break;
  		}
  		int invocationBegin = matcher.start();
  		String begin = matcher.group();
  		System.out.println(begin);
  		int invocationEnd = s.indexOf("\"});\r\n", invocationBegin);
  		if (invocationEnd == -1) {
  			System.out.println(s.substring(invocationBegin));
  			System.out.println("!!!!!!!!!!!!!");
  			System.exit(0);
  		}
  		invocationEnd += "\"}".length();
  		// process single "invocation"
  		{
  			String invocation = s.substring(invocationBegin, invocationEnd);
  			// begin/end
  			invocation = StringUtils.removeStart(invocation, begin);
  			invocation = StringUtils.removeEnd(invocation, "}");
  			invocation = "parseComposite("// filler filler filler", " + invocation;
  			// replace " with '
  			invocation = StringUtils.replace(invocation, "\\\"", "'");
  			// apply updated "invocation"
  			s = s.substring(0, invocationBegin) + invocation + s.substring(invocationEnd);
  		}
  		// next
  		invocationLast = invocationBegin + 1;
  	}
  	return s;
  }*/
	/*private static String replaceInString(String s) {
  	int invocationLast = 0;
  	while (true) {
  		// prepare begin/end of "invocation"
  		String begin = "assertEditor(";
  		int invocationBegin = s.indexOf(begin, invocationLast);
  		if (invocationBegin == -1) {
  			break;
  		}
  		{
  			invocationBegin += begin.length();
  			char c;
  			while (true) {
  				c = s.charAt(invocationBegin);
  				if (!Character.isWhitespace(c)) {
  					break;
  				}
  				invocationBegin++;
  			}
  			if (c != '"') {
  				break;
  			}
  		}
  		int invocationEnd = s.indexOf("\");\r\n", invocationBegin);
  		if (invocationEnd == -1) {
  			System.out.println(s.substring(invocationBegin));
  			System.out.println("!!!!!!!!!!!!!");
  			System.exit(0);
  		}
  		// process single "invocation"
  		{
  			String invocation = s.substring(invocationBegin, invocationEnd);
  			// replace " with '
  			invocation = StringUtils.replace(invocation, "\\\"", "'");
  			// apply updated "invocation"
  			s = s.substring(0, invocationBegin) + invocation + s.substring(invocationEnd);
  		}
  		// next
  		invocationLast = invocationBegin + 1;
  	}
  	return s;
  }*/
}
