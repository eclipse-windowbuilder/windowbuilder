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
package org.eclipse.wb.tests.designer.core.util.ast;

import org.eclipse.wb.internal.core.utils.ast.Gatherer;
import org.eclipse.wb.internal.core.utils.ast.ListGatherer;
import org.eclipse.wb.internal.core.utils.ast.SetGatherer;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author scheglov_ke
 */
public class GathererTest extends AbstractJavaTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Project creation
	//
	////////////////////////////////////////////////////////////////////////////
	@BeforeClass
	public static void setUpClass() throws Exception {
		do_projectCreate();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ListGatherer() throws Exception {
		TypeDeclaration typeDeclaration =
				createTypeDeclaration_Test(
						"public class Test {",
						"  String a = 'aaa';",
						"  String b = 'bbb';",
						"}");
		Gatherer<StringLiteral> gatherer = new ListGatherer<StringLiteral>() {
			@Override
			public void endVisit(StringLiteral node) {
				addResult(node);
			}
		};
		typeDeclaration.accept(gatherer);
		// check gatherer
		assertTrue(gatherer.hasResults());
		// check List result
		List<StringLiteral> resultList = gatherer.getResultList();
		assertEquals(2, resultList.size());
		assertEquals("aaa", resultList.get(0).getLiteralValue());
		assertEquals("bbb", resultList.get(1).getLiteralValue());
		// check Set result
		Set<StringLiteral> resultSet = gatherer.getResultSet();
		assertEquals(2, resultSet.size());
		resultSet.containsAll(resultList);
	}

	@Test
	public void test_SetGatherer() throws Exception {
		TypeDeclaration typeDeclaration =
				createTypeDeclaration_Test(
						"public class Test {",
						"  String a = 'aaa';",
						"  String b = 'bbb';",
						"}");
		Gatherer<StringLiteral> gatherer = new SetGatherer<StringLiteral>() {
			@Override
			public void endVisit(StringLiteral node) {
				addResult(node);
			}
		};
		typeDeclaration.accept(gatherer);
		// check gatherer
		assertTrue(gatherer.hasResults());
		assertNull(gatherer.getUniqueResult());
		// check Set result
		Set<StringLiteral> resultSet = gatherer.getResultSet();
		assertEquals(2, resultSet.size());
		assertTrue(hasStringLiteral(resultSet, "aaa"));
		assertTrue(hasStringLiteral(resultSet, "bbb"));
		// check List result
		List<StringLiteral> resultList = gatherer.getResultList();
		assertEquals(2, resultList.size());
		resultList.containsAll(resultSet);
	}

	@Test
	public void test_unique() throws Exception {
		TypeDeclaration typeDeclaration =
				createTypeDeclaration_Test(
						"// filler filler filler",
						"public class Test {",
						"  String a = 'aaa';",
						"}");
		Gatherer<StringLiteral> gatherer = new SetGatherer<StringLiteral>() {
			@Override
			public void endVisit(StringLiteral node) {
				addResult(node);
			}
		};
		typeDeclaration.accept(gatherer);
		// check gatherer
		assertTrue(gatherer.hasResults());
		//
		StringLiteral stringLiteral = gatherer.getUniqueResult();
		assertNotNull(stringLiteral);
		assertEquals("aaa", stringLiteral.getLiteralValue());
	}

	@Test
	public void test_VariableDeclaration() throws Exception {
		TypeDeclaration typeDeclaration =
				createTypeDeclaration_Test(
						"public class Test {",
						"  String a = 'aaa';",
						"  String b = 'bbb';",
						"}");
		Gatherer<VariableDeclarationFragment> gatherer =
				new ListGatherer<VariableDeclarationFragment>() {
			@Override
			public void endVisit(VariableDeclarationFragment node) {
				addResult(node);
			}
		};
		typeDeclaration.accept(gatherer);
		// check gatherer
		assertTrue(gatherer.hasResults());
		// check array of VariableDeclaration's
		VariableDeclaration[] declarations = gatherer.toArray(VariableDeclarationFragment.class);
		assertEquals(2, declarations.length);
		assertEquals("a", declarations[0].getName().getIdentifier());
		assertEquals("b", declarations[1].getName().getIdentifier());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given {@link Collection} has {@link StringLiteral} with given
	 *         value.
	 */
	private static boolean hasStringLiteral(Collection<StringLiteral> collection, String literalValue) {
		for (StringLiteral stringLiteral : collection) {
			if (stringLiteral.getLiteralValue().equals(literalValue)) {
				return true;
			}
		}
		// not found
		return false;
	}
}
