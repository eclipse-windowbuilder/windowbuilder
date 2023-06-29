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
package org.eclipse.wb.internal.rcp.parser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.parser.IParseFactorySimpleModelCic;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * {@link IParseFactorySimpleModelCic} for RCP.
 *
 * @author scheglov_ke
 * @coverage rcp.parser
 */
public class ParseFactorySimpleModelCic implements IParseFactorySimpleModelCic {
	////////////////////////////////////////////////////////////////////////////
	//
	// IParseFactory_simpleModel_CIC
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean accept(AstEditor editor, ClassInstanceCreation creation, ITypeBinding typeBinding)
			throws Exception {
		EditorState state = EditorState.get(editor);
		if (state.getEditorLoader() == null) {
			return false;
		}
		// if possible to handle
		String typeName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
		return isModel(typeName, typeBinding) || noModel(typeName, typeBinding);
	}

	@Override
	public JavaInfo create(AstEditor editor, ClassInstanceCreation creation, ITypeBinding typeBinding)
			throws Exception {
		String typeName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
		if (noModel(typeName, typeBinding)) {
			return null;
		}
		return JavaInfoUtils.createJavaInfo(editor, typeName, new ConstructorCreationSupport(creation));
	}

	private static boolean isModel(String typeName, ITypeBinding binding) {
		if (typeName.startsWith("org.eclipse.swt.")) {
			return typeName.equals("org.eclipse.swt.layout.GridData")
					|| typeName.equals("org.eclipse.swt.layout.FormData")
					|| typeName.equals("org.eclipse.swt.layout.FormAttachment")
					|| AstNodeUtils.isSuccessorOf(
							binding,
							"org.eclipse.swt.widgets.Widget",
							"org.eclipse.swt.widgets.Layout");
		}
		if (typeName.startsWith("org.eclipse.jface.layout.")) {
			return AstNodeUtils.isSuccessorOf(binding, "org.eclipse.swt.widgets.Layout");
		}
		if (typeName.startsWith("org.eclipse.jface.viewers.")) {
			return AstNodeUtils.isSuccessorOf(binding, "org.eclipse.jface.viewers.ColumnLayoutData");
		}
		return false;
	}

	private static boolean noModel(String typeName, ITypeBinding binding) {
		if (typeName.startsWith("org.eclipse.swt.graphics.")) {
			return true;
		}
		return false;
	}
}
