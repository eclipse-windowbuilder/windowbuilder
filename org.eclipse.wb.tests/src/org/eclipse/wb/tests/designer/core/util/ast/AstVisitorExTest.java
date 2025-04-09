/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.util.ast;

import org.eclipse.wb.internal.core.utils.ast.AstVisitorEx;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link AstVisitorEx}.
 *
 * @author scheglov_ke
 */
public class AstVisitorExTest extends AbstractJavaTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_preVisit_ASTNode() throws Exception {
		CompilationUnit compilationUnit =
				createASTCompilationUnit(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"package test;",
								"public class Test {",
								"}"));
		// no exception
		compilationUnit.accept(new AstVisitorEx() {
			@Override
			public void preVisitEx(ASTNode node) throws Exception {
			}
		});
		// exception
		final Exception expected = new Exception("pre");
		try {
			compilationUnit.accept(new AstVisitorEx() {
				@Override
				public void preVisitEx(ASTNode node) throws Exception {
					throw expected;
				}
			});
			fail();
		} catch (Throwable e) {
			assertSame(expected, e);
		}
	}

	@Test
	public void test_postVisit_ASTNode() throws Exception {
		CompilationUnit compilationUnit =
				createASTCompilationUnit(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"package test;",
								"public class Test {",
								"}"));
		// no exception
		compilationUnit.accept(new AstVisitorEx() {
			@Override
			public void postVisitEx(ASTNode node) throws Exception {
			}
		});
		// exception
		final Exception expected = new Exception("post");
		try {
			compilationUnit.accept(new AstVisitorEx() {
				@Override
				public void postVisitEx(ASTNode node) throws Exception {
					throw expected;
				}
			});
			fail();
		} catch (Throwable e) {
			assertSame(expected, e);
		}
	}

	@Test
	public void test_visit_QualifiedName() throws Exception {
		CompilationUnit compilationUnit =
				createASTCompilationUnit(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"package test;",
								"public class Test {",
								"  java.lang.Object o = null;",
								"}"));
		// no exception
		compilationUnit.accept(new AstVisitorEx() {
			@Override
			public boolean visitEx(QualifiedName node) throws Exception {
				return true;
			}
		});
		// exception
		final Exception expected = new Exception("visit");
		try {
			compilationUnit.accept(new AstVisitorEx() {
				@Override
				public boolean visitEx(QualifiedName node) throws Exception {
					throw expected;
				}
			});
			fail();
		} catch (Throwable e) {
			assertSame(expected, e);
		}
	}

	@Test
	public void test_endVisit_QualifiedName() throws Exception {
		CompilationUnit compilationUnit =
				createASTCompilationUnit(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"package test;",
								"public class Test {",
								"  java.lang.Object o = null;",
								"}"));
		// no exception
		final Exception expected = new Exception("endVisit");
		compilationUnit.accept(new AstVisitorEx() {
			@Override
			public void endVisitEx(QualifiedName node) throws Exception {
				super.endVisitEx(node);
			}
		});
		// exception
		try {
			compilationUnit.accept(new AstVisitorEx() {
				@Override
				public void endVisitEx(QualifiedName node) throws Exception {
					throw expected;
				}
			});
			fail();
		} catch (Throwable e) {
			assertSame(expected, e);
		}
	}

	@Test
	public void test_endVisit_MethodInvocation() throws Exception {
		CompilationUnit compilationUnit =
				createASTCompilationUnit(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"package test;",
								"public class Test {",
								"  private Test() {",
								"    System.out.println();",
								"  }",
								"}"));
		// no exception
		compilationUnit.accept(new AstVisitorEx() {
			@Override
			public void endVisitEx(MethodInvocation node) throws Exception {
				super.endVisitEx(node);
			}
		});
		// exception
		final Exception expected = new Exception("endVisit");
		try {
			compilationUnit.accept(new AstVisitorEx() {
				@Override
				public void endVisitEx(MethodInvocation node) throws Exception {
					throw expected;
				}
			});
			fail();
		} catch (Throwable e) {
			assertSame(expected, e);
		}
	}

	@Test
	public void test_endVisit_SuperMethodInvocation() throws Exception {
		CompilationUnit compilationUnit =
				createASTCompilationUnit(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"package test;",
								"public class Test extends javax.swing.JPanel {",
								"  private Test() {",
								"    super.setEnabled(true);",
								"  }",
								"}"));
		// no exception
		compilationUnit.accept(new AstVisitorEx() {
			@Override
			public void endVisitEx(SuperMethodInvocation node) throws Exception {
				super.endVisitEx(node);
			}
		});
		// exception
		final Exception expected = new Exception("endVisit");
		try {
			compilationUnit.accept(new AstVisitorEx() {
				@Override
				public void endVisitEx(SuperMethodInvocation node) throws Exception {
					throw expected;
				}
			});
			fail();
		} catch (Throwable e) {
			assertSame(expected, e);
		}
	}

	@Test
	public void test_endVisit_TypeDeclaration() throws Exception {
		CompilationUnit compilationUnit =
				createASTCompilationUnit(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"package test;",
								"public class Test {",
								"}"));
		// no exception
		compilationUnit.accept(new AstVisitorEx() {
			@Override
			public void endVisitEx(TypeDeclaration node) throws Exception {
				super.endVisitEx(node);
			}
		});
		// exception
		final Exception expected = new Exception("endVisit");
		try {
			compilationUnit.accept(new AstVisitorEx() {
				@Override
				public void endVisitEx(TypeDeclaration node) throws Exception {
					throw expected;
				}
			});
			fail();
		} catch (Throwable e) {
			assertSame(expected, e);
		}
	}

	@Test
	public void test_endVisit_TryStatement() throws Exception {
		CompilationUnit compilationUnit =
				createASTCompilationUnit(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"package test;",
								"public class Test {",
								"  public Test() {",
								"    try {} finally {}",
								"  }",
								"}"));
		// no exception
		compilationUnit.accept(new AstVisitorEx() {
			@Override
			public void endVisitEx(TryStatement node) throws Exception {
				super.endVisitEx(node);
			}
		});
		// exception
		final Exception expected = new Exception("endVisit");
		try {
			compilationUnit.accept(new AstVisitorEx() {
				@Override
				public void endVisitEx(TryStatement node) throws Exception {
					throw expected;
				}
			});
			fail();
		} catch (Throwable e) {
			assertSame(expected, e);
		}
	}
}
