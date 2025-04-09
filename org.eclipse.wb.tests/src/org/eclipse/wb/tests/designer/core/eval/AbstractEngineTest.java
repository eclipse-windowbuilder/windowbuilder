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
package org.eclipse.wb.tests.designer.core.eval;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author scheglov_ke
 */
public abstract class AbstractEngineTest extends AbstractJavaTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Evaluates "return" expression of method with given signature.
	 */
	protected final Object evaluateSingleMethod(TypeDeclaration typeDeclaration,
			String methodSignature) throws Exception {
		return evaluateSingleMethod(typeDeclaration, methodSignature, methodSignature);
	}

	/**
	 * Evaluates "return" expression of method with given signature.
	 */
	protected final Object evaluateSingleMethod(TypeDeclaration typeDeclaration,
			String entryMethodSignature,
			String returnMethodSignature) throws Exception {
		MethodDeclaration entryMethod =
				AstNodeUtils.getMethodBySignature(typeDeclaration, entryMethodSignature);
		MethodDeclaration returnMethod =
				AstNodeUtils.getMethodBySignature(typeDeclaration, returnMethodSignature);
		List<Statement> statements = DomGenerics.statements(returnMethod.getBody());
		ReturnStatement returnStatement = (ReturnStatement) statements.get(statements.size() - 1);
		Expression expressionToEvaluate = returnStatement.getExpression();
		return evaluateExpression(entryMethod, expressionToEvaluate);
	}

	protected final Object evaluateExpression(MethodDeclaration entryPoint,
			final Expression expressionToEvaluate) throws Exception {
		ClassLoader projectClassLoader =
				CodeUtils.getProjectClassLoader(m_lastEditor.getModelUnit().getJavaProject());
		ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(entryPoint);
		//
		final boolean[] expressionEvaluated = new boolean[1];
		EvaluationContext context = new EvaluationContext(projectClassLoader, flowDescription) {
			@Override
			public void evaluationSuccessful(Expression expression, Object value) throws Exception {
				if (expression == expressionToEvaluate) {
					expressionEvaluated[0] = true;
				}
			}
		};
		Object evaluationResult = AstEvaluationEngine.evaluate(context, expressionToEvaluate);
		assertTrue(MessageFormat.format(
				"Expression ''{0}'' was not notified in evaluationSuccessful()",
				expressionToEvaluate), expressionEvaluated[0]);
		return evaluationResult;
	}

	/**
	 * Creates {@link ICompilationUnit} for resource in "/resources/eval" and given name.
	 */
	protected final TypeDeclaration createResourceType(String name) throws Exception {
		return createResourceType(name, "Test.java");
	}

	/**
	 * Creates {@link ICompilationUnit} for resource in "/resources/eval" and given name.
	 */
	protected final TypeDeclaration createResourceType(String resourceName, String unitName)
			throws Exception {
		String path = "core/eval/" + resourceName + ".java";
		String code = readResourceFileContent(path);
		CompilationUnit compilationUnit = createASTCompilationUnit("test", unitName, code);
		assertEquals(1, compilationUnit.types().size());
		return (TypeDeclaration) compilationUnit.types().get(0);
	}

	/**
	 * Evaluates given expression.
	 */
	protected final Object evaluateExpression(String expression, String returnType) throws Exception {
		return evaluateExpression(expression, returnType, false);
	}

	/**
	 * Evaluates given expression.
	 */
	protected final Object evaluateExpression(String expression, String returnType, boolean waitBuild)
			throws Exception {
		CompilationUnit compilationUnit =
				createASTCompilationUnit(
						"test",
						"Test.java",
						getSource("// filler filler filler", "package test;", "class Test {", "  "
								+ returnType
								+ " foo() {", "      return " + expression + ";", "  }", "}"));
		TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
		if (waitBuild) {
			waitForAutoBuild();
		}
		return evaluateSingleMethod(typeDeclaration, "foo()");
	}
}
