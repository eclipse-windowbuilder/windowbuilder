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
package org.eclipse.wb.internal.core.databinding.parser;

import org.eclipse.wb.internal.core.parser.ParseFactoryNoModel;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * {@link ParseFactoryNoModel} for data bindings support.
 *
 * @author sablin_aa
 * @coverage bindings.parser
 */
public final class ParseFactoryNoModelDatabindings extends ParseFactoryNoModel {
	////////////////////////////////////////////////////////////////////////////
	//
	// ParseFactory_noModel
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean noModel(ASTNode node) {
		MethodDeclaration enclosingMethod = AstNodeUtils.getEnclosingMethod(node);
		if (enclosingMethod != null) {
			return "initDataBindings".equals(enclosingMethod.getName().toString());
		}
		return false;
	}
}
