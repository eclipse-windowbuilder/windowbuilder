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
package org.eclipse.wb.internal.rcp.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.context.DataBindingContextInfo;

import java.util.List;

/**
 * This abstract model for all bindings: binding over <code>DataBindingContext.bindXXX()</code> and
 * binding input for JFace viewers.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public abstract class AbstractBindingInfo extends AstObjectInfo implements IBindingInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is invoked as last step of parsing.
	 */
	public void postParse() throws Exception {
		create();
	}

	public void create() throws Exception {
		((BindableInfo) getTarget()).createBinding(this);
		((BindableInfo) getTargetProperty()).createBinding(this);
		((BindableInfo) getModel()).createBinding(this);
		((BindableInfo) getModelProperty()).createBinding(this);
	}

	public void delete() throws Exception {
		((BindableInfo) getTarget()).deleteBinding(this);
		((BindableInfo) getTargetProperty()).deleteBinding(this);
		((BindableInfo) getModel()).deleteBinding(this);
		((BindableInfo) getModelProperty()).deleteBinding(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Definition
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return source code for find definition into source code.
	 */
	public abstract String getDefinitionSource(DatabindingsProvider provider) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create {@link IUiContentProvider} content providers for edit this model.
	 */
	public abstract void createContentProviders(List<IUiContentProvider> providers,
			IPageListener listener,
			DatabindingsProvider provider) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Add source code association with this model.
	 */
	public abstract void addSourceCode(DataBindingContextInfo context,
			List<String> lines,
			CodeGenerationSupport generationSupport) throws Exception;
}