/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.resource.LocalResourceManager;

import java.util.Collections;

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
		String methodName = "createResourceManager";
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
		//
		MethodDescription methodDescription = new MethodDescription((Class<?>) null);
		methodDescription.setOrder(MethodOrder.parse("first"));
		methodDescription.setName(methodName);
		methodDescription.postProcess();
		// do add new resource manager
		JavaInfoUtils.add(resourceManager, //
				new FieldUniqueVariableSupport(resourceManager), //
				PureFlatStatementGenerator.INSTANCE, //
				AssociationObjects.empty(), //
				root, //
				null, //
				managerTarget);
		root.removeChild(resourceManager);
		root.getDescription().addMethod(methodDescription);
		ManagerContainerInfo.get(root).addChild(resourceManager);
	}
}
