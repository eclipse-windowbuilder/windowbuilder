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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.rcp.databinding.model.SimpleClassObjectInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Abstract model for all <code>JFace</code> viewer observable content provider.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class ObservableCollectionContentProviderInfo extends SimpleClassObjectInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ObservableCollectionContentProviderInfo(String className) {
		super(className);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			IModelResolver resolver,
			IDatabindingsProvider provider) throws Exception {
		if ("getKnownElements".equals(invocation.getName().getIdentifier())) {
			return new KnownElementsObservableInfo(this);
		}
		return null;
	}
}