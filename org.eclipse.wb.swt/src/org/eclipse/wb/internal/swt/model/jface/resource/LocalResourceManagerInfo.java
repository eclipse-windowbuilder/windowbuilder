/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.jface.resource;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.resource.LocalResourceManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation {@link JavaInfo} for {@link LocalResourceManager}.
 */
public class LocalResourceManagerInfo extends ResourceManagerInfo {
	public LocalResourceManagerInfo(AstEditor editor, ComponentDescription description, CreationSupport creationSupport)
			throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// New resource manager utils
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of {@link LocalResourceManagerInfo} and adds it to the
	 * {@link ManagerContainerInfo}, directly attached to the root.
	 *
	 * @return the newly created {@link LocalResourceManagerInfo}.
	 */
	/* package */ static LocalResourceManagerInfo createNew(JavaInfo root) throws Exception {
		Assert.isTrue(root.isRoot());
		AstEditor editor = root.getEditor();
		LocalResourceManagerInfo resourceManager = (LocalResourceManagerInfo) JavaInfoUtils.createJavaInfo( //
				editor, //
				LocalResourceManager.class, //
				new ConstructorCreationSupport());
		createSourceEntry(root, resourceManager);
		return resourceManager;
	}

	/**
	 * <p>
	 * Ensures that the given {@link LocalResourceManagerInfo} has presence in
	 * source code. The model object info will be attached to the
	 * {@link ManagerContainerInfo} of the root info object. The resource manager is
	 * stored in an instance variable and initialized in the constructor.
	 * </p>
	 * Example:
	 *
	 * <pre>
	 * public class Test extends Shell {
	 * 	private LocalResourceManager localResourceManager;
	 *
	 * 	public Test() {
	 * 		localResourceManager = new LocalResourceManager(JFaceResources.getResources(), this);
	 * 	}
	 * }
	 * </pre>
	 *
	 * @param root            the {@link JavaInfo} of the root object.
	 * @param resourceManager the {@link LocalResourceManagerInfo} to ensure.
	 */
	private static void createSourceEntry(final JavaInfo root, final LocalResourceManagerInfo resourceManager) throws Exception {
		Assert.isTrue(root.isRoot());
		final AstEditor editor = root.getEditor();
		final VariableSupport rootVariableSupport = root.getVariableSupport();
		final StatementTarget rootTarget = rootVariableSupport.getStatementTarget();

		// prepare "createResourceManager()" method
		String methodSignature = "createResourceManager()";
		String methodHeader = "private void createResourceManager()";
		String methodInvocation = "createResourceManager()";
		// ensure "createResourceManager()" exists
		TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(root);
		MethodDeclaration managerMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
		if (managerMethod == null) {
			MethodDeclaration rootMethod = editor.getEnclosingMethod(rootTarget.getPosition());
			managerMethod = editor.addMethodDeclaration(
					methodHeader,
					Collections.emptyList(),
					new BodyDeclarationTarget(rootMethod, false));
			root.addExpressionStatement(rootTarget, methodInvocation);
		}

		final StatementTarget managerTarget = new StatementTarget(managerMethod, true);
		final CreationSupport creationSupport = new CreationSupport() {
			@Override
			public boolean isJavaInfo(ASTNode node) {
				return false;
			}

			@Override
			public ASTNode getNode() {
				return null;
			}

			@Override
			public String add_getSource(NodeTarget target) throws Exception {
				return "new org.eclipse.jface.resource.LocalResourceManager(org.eclipse.jface.resource.JFaceResources.getResources(),%parent%)";
			}

			@Override
			public void add_setSourceExpression(Expression expression) throws Exception {
				ClassInstanceCreation creation = (ClassInstanceCreation) expression;
				resourceManager.setCreationSupport(new ConstructorCreationSupport(creation));
				resourceManager.bindToExpression(creation);
			}
		};
		resourceManager.setCreationSupport(creationSupport);
		// do add new resource manager
		JavaInfoUtils.add(resourceManager, //
				new FieldUniqueVariableSupport(resourceManager), //
				PureFlatStatementGenerator.INSTANCE, //
				AssociationObjects.empty(), //
				root, //
				null, //
				managerTarget);
		root.removeChild(resourceManager);
		// resource manager must've been initialized first
		root.getDescription().setDefaultMethodOrder(new MethodOrderAfterResourceManager());
		ManagerContainerInfo.get(root).addChild(resourceManager);
	}

	/**
	 * <p>
	 * Special method order that ensures all method invocations in the current
	 * widget are executed <b>after</b> the resource manager has been initialized.
	 * </p>
	 * <p>
	 * If the manager is created outside the constructor (i.e. in a separate
	 * method), then new statements are added after the corresponding method
	 * invocation inside the constructor.
	 * </p>
	 *
	 * Example:
	 *
	 * <pre>
	 * public class Test extends Shell {
	 * 	private LocalResourceManager localResourceManager;
	 *
	 * 	public Test() {
	 * 		init();
	 * 		// ---- Method calls are added after this comment
	 * 		setBackground(localResourceManager.create(ColorDescriptor.createFrom(new RGB(1,1,1)));
	 * 	}
	 *
	 *  public void init() {
	 * 		localResourceManager = new LocalResourceManager(JFaceResources.getResources(), this);
	 *  }
	 * }
	 * </pre>
	 */
	/* package */ static final class MethodOrderAfterResourceManager extends MethodOrder {
		@Override
		public boolean canReference(JavaInfo javaInfo) {
			return true;
		}

		@Override
		protected StatementTarget getSpecificTarget(JavaInfo javaInfo, String newSignature) throws Exception {
			ResourceManagerInfo targetInfo = null;
			MethodOrder defaultMethodOrder = MethodOrder.parse("afterCreation");
			for (ManagerContainerInfo child : javaInfo.getChildren(ManagerContainerInfo.class)) {
				for (ResourceManagerInfo grandChild : child.getChildren(ResourceManagerInfo.class)) {
					targetInfo = grandChild;
				}
			}
			// If this method order is used in a widget without resource manager.
			if (targetInfo == null) {
				return defaultMethodOrder.getTarget(javaInfo, newSignature);
			}
			// If the resource manager is created directly inside the constructor. The new
			// statement is added directly after the manager creation.
			StatementTarget targetStatement = targetInfo.getVariableSupport().getStatementTarget();
			MethodDeclaration targetDeclaration = AstNodeUtils.getEnclosingMethod(targetStatement.getStatement());
			if (targetDeclaration.isConstructor()) {
				return new StatementTarget(targetStatement.getStatement(), false);
			}
			// If the resource manager is created in a separate method, go through all
			// method invocations and find the one that creates the manager instance.
			AstEditor editor = javaInfo.getEditor();
			VariableSupport rootVariableSupport = javaInfo.getRootJava().getVariableSupport();
			StatementTarget rootStatement = rootVariableSupport.getStatementTarget();
			MethodDeclaration rootMethod = editor.getEnclosingMethod(rootStatement.getPosition());
			MethodInvocation targetInvocation = null;
			for (Statement statement : DomGenerics.statements(rootMethod)) {
				if (statement instanceof ExpressionStatement expressionStatement
						&& expressionStatement.getExpression() instanceof MethodInvocation methodInvocation
						&& isConstructorInvocation(methodInvocation, targetDeclaration)) {
					targetInvocation = methodInvocation;
					break;
				}
			}
			// If the resource manager is not created in the constructor
			if (targetInvocation == null) {
				return defaultMethodOrder.getTarget(javaInfo, newSignature);
			}
			return new StatementTarget(targetInvocation, false);
		}

		private boolean isConstructorInvocation(MethodInvocation rootInvocation, MethodDeclaration targetDeclaration) {
			AtomicBoolean result = new AtomicBoolean(false);
			MethodDeclaration rootDeclaration = AstNodeUtils.getLocalMethodDeclaration(rootInvocation);
			if (rootDeclaration != null) {
				rootDeclaration.accept(new ASTVisitor() {
					@Override
					public boolean visit(MethodInvocation methodInvocation) {
						MethodDeclaration methodDeclaration = AstNodeUtils.getLocalMethodDeclaration(rootInvocation);
						if (targetDeclaration.equals(methodDeclaration)) {
							result.set(true);
						} else if (methodDeclaration != null) {
							result.set(isConstructorInvocation(methodInvocation, targetDeclaration));
						}
						// Stop early if we already found a match
						return !result.get();
					}
				});
			}
			return result.get();
		}
	}
}
