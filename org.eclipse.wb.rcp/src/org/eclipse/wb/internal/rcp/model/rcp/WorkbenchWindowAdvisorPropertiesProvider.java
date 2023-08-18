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
package org.eclipse.wb.internal.rcp.model.rcp;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.GenericPropertyGetValueEx;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.ListGatherer;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import java.text.MessageFormat;
import java.util.List;

/**
 * Provider for properties of {@link IWorkbenchWindowConfigurer} in
 * {@link WorkbenchWindowAdvisor#preWindowOpen()}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
final class WorkbenchWindowAdvisorPropertiesProvider {
	private final ActionBarAdvisorInfo m_actionBar;
	private boolean m_initialized;
	private AstEditor m_windowEditor;
	private TypeDeclaration m_windowType;
	private final List<Property> m_properties = Lists.newArrayList();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WorkbenchWindowAdvisorPropertiesProvider(ActionBarAdvisorInfo actionBar) {
		m_actionBar = actionBar;
		addPropertyValueBroadcast();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Broadcasts
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * We don't visit {@link Expression}'s of {@link WorkbenchWindowAdvisor} properties, so have to
	 * use special way to provide their values.
	 */
	private void addPropertyValueBroadcast() {
		m_actionBar.addBroadcastListener(new GenericPropertyGetValueEx() {
			@Override
			public void invoke(GenericPropertyImpl property, Expression expression, Object[] value)
					throws Exception {
				if (m_properties.contains(property)) {
					value[0] = getValue(expression);
				}
			}

			private Object getValue(Expression expression) {
				if (expression instanceof BooleanLiteral) {
					return ((BooleanLiteral) expression).booleanValue();
				}
				if (expression instanceof StringLiteral) {
					return ((StringLiteral) expression).getLiteralValue();
				}
				return Property.UNKNOWN_VALUE;
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	List<Property> getProperties() throws Exception {
		if (!m_initialized) {
			m_initialized = true;
			initializeWindowAST();
			if (m_windowEditor != null) {
				createProperties();
			}
		}
		// return prepared properties
		return m_properties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Fills {@link #m_properties} field.
	 */
	private void createProperties() throws Exception {
		ComponentDescription windowDescription =
				ComponentDescriptionHelper.getDescription(
						m_actionBar.getEditor(),
						"org.eclipse.ui.application.IWorkbenchWindowConfigurer");
		for (GenericPropertyDescription description : windowDescription.getProperties()) {
			m_properties.add(createProperty(description));
		}
	}

	private Property createProperty(GenericPropertyDescription description) throws Exception {
		ExpressionAccessor expressionAccessor = createExpressionAccessor(description);
		return new GenericPropertyImpl(m_actionBar,
				description.getTitle(),
				new ExpressionAccessor[]{expressionAccessor},
				description.getDefaultValue(),
				description.getConverter(),
				description.getEditor());
	}

	private ExpressionAccessor createExpressionAccessor(GenericPropertyDescription description) {
		SetterAccessor setterAccessor = (SetterAccessor) description.getAccessorsList().get(0);
		String methodName = setterAccessor.getSetter().getName();
		return new Window_ExpressionAccessor(methodName);
	}

	/**
	 * Initializes {@link #m_windowEditor} and related.
	 */
	private void initializeWindowAST() throws Exception {
		ICompilationUnit windowUnit = get_WorkbenchWindowAdvisor_unit();
		if (windowUnit != null) {
			m_windowEditor = new AstEditor(windowUnit);
			String windowTypeName = CodeUtils.findPrimaryType(windowUnit).getElementName();
			m_windowType = AstNodeUtils.getTypeByName(m_windowEditor.getAstUnit(), windowTypeName);
		}
	}

	/**
	 * @return the {@link ICompilationUnit} with {@link WorkbenchWindowAdvisor} that references this
	 *         {@link ActionBarAdvisor}.
	 */
	private ICompilationUnit get_WorkbenchWindowAdvisor_unit() throws Exception {
		String actionAdvisorName =
				AstNodeUtils.getFullyQualifiedName(JavaInfoUtils.getTypeDeclaration(m_actionBar), false);
		IPackageFragment pkg = (IPackageFragment) m_actionBar.getEditor().getModelUnit().getParent();
		for (ICompilationUnit unit : pkg.getCompilationUnits()) {
			if (is_WorkbenchWindowAdvisor_thatReferences_ActionBarAdvisor(unit, actionAdvisorName)) {
				return unit;
			}
		}
		return null;
	}

	/**
	 * @return <code>true</code> if given {@link ICompilationUnit} has {@link WorkbenchWindowAdvisor}
	 *         that references {@link ActionBarAdvisor} with given name.
	 */
	private static boolean is_WorkbenchWindowAdvisor_thatReferences_ActionBarAdvisor(ICompilationUnit unit,
			String actionAdvisorName) throws Exception {
		IType type = CodeUtils.findPrimaryType(unit);
		if (CodeUtils.isSuccessorOf(type, "org.eclipse.ui.application.WorkbenchWindowAdvisor")) {
			CompilationUnit astUnit = CodeUtils.parseCompilationUnit(unit);
			TypeDeclaration astType = AstNodeUtils.getTypeByName(astUnit, type.getElementName());
			MethodDeclaration createActionBarAdvisor =
					AstNodeUtils.getMethodBySignature(
							astType,
							"createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)");
			if (createActionBarAdvisor != null) {
				List<Statement> statements = DomGenerics.statements(createActionBarAdvisor.getBody());
				for (Statement statement : statements) {
					if (statement instanceof ReturnStatement returnStatement) {
						if (returnStatement.getExpression() instanceof ClassInstanceCreation) {
							ClassInstanceCreation creation =
									(ClassInstanceCreation) returnStatement.getExpression();
							String creationTypeName = AstNodeUtils.getFullyQualifiedName(creation, false);
							return creationTypeName.equals(actionAdvisorName);
						}
					}
				}
			}
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionAccessor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link ExpressionAccessor} for {@link IWorkbenchWindowConfigurer} in
	 * {@link WorkbenchWindowAdvisor#preWindowOpen()}.
	 *
	 * @author scheglov_ke
	 */
	private final class Window_ExpressionAccessor extends ExpressionAccessor {
		private final String m_methodName;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public Window_ExpressionAccessor(String methodName) {
			m_methodName = methodName;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// ExpressionAccessor
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Expression getExpression(JavaInfo javaInfo) throws Exception {
			MethodInvocation invocation = getInvocation();
			if (invocation != null) {
				return DomGenerics.arguments(invocation).get(0);
			}
			return null;
		}

		@Override
		public boolean setExpression(JavaInfo javaInfo, String source) throws Exception {
			// update existing invocation
			MethodInvocation invocation = getInvocation();
			if (invocation != null) {
				if (source == null) {
					m_windowEditor.removeEnclosingStatement(invocation);
				} else {
					Expression expression = DomGenerics.arguments(invocation).get(0);
					m_windowEditor.replaceExpression(expression, source);
				}
			} else if (source != null) {
				MethodDeclaration method = ensureTargetMethod();
				VariableDeclaration configurerDeclaration = getConfigurerDeclaration(method);
				if (configurerDeclaration != null) {
					String statementSource =
							MessageFormat.format(
									"{0}.{1}({2});",
									configurerDeclaration.getName().getIdentifier(),
									m_methodName,
									source);
					m_windowEditor.addStatement(statementSource, new StatementTarget(method, false));
				}
			}
			// success
			m_windowEditor.commitChanges();
			return true;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Utils
		//
		////////////////////////////////////////////////////////////////////////////
		private MethodInvocation getInvocation() {
			MethodDeclaration method = getTargetMethod();
			if (method != null) {
				ListGatherer<MethodInvocation> gatherer = new ListGatherer<>() {
					@Override
					public boolean visit(MethodInvocation invocation) {
						if (invocation.getName().getIdentifier().equals(m_methodName)) {
							addResult(invocation);
						}
						return false;
					}
				};
				method.accept(gatherer);
				return gatherer.getUniqueResult();
			}
			return null;
		}

		/**
		 * @return the {@link VariableDeclaration} for variable with type
		 *         {@link IWorkbenchWindowConfigurer}.
		 */
		private VariableDeclaration getConfigurerDeclaration(MethodDeclaration method) {
			ListGatherer<VariableDeclaration> gatherer = new ListGatherer<>() {
				@Override
				public void endVisit(VariableDeclarationStatement node) {
					ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(node.getType());
					if (AstNodeUtils.isSuccessorOf(
							typeBinding,
							"org.eclipse.ui.application.IWorkbenchWindowConfigurer")) {
						VariableDeclarationFragment declaration = DomGenerics.fragments(node).get(0);
						addResult(declaration);
					}
				}
			};
			method.accept(gatherer);
			return gatherer.getUniqueResult();
		}

		private MethodDeclaration ensureTargetMethod() throws Exception {
			MethodDeclaration method = getTargetMethod();
			if (method == null) {
				method =
						m_windowEditor.addMethodDeclaration(
								"public void preWindowOpen()",
								Lists.newArrayList("org.eclipse.ui.application.IWorkbenchWindowConfigurer configurer = getWindowConfigurer();"),
								new BodyDeclarationTarget(m_windowType, false));
			}
			return method;
		}

		private MethodDeclaration getTargetMethod() {
			return AstNodeUtils.getMethodBySignature(m_windowType, "preWindowOpen()");
		}
	}
}
