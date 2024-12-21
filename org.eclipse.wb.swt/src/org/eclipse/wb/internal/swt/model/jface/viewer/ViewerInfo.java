/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.jface.viewer;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.WrapperByMethod;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.jface.WrapperInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.swt.widgets.Composite;

/**
 * Model for any JFace {@link org.eclipse.jface.viewers.Viewer}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swt.model.jface
 */
public class ViewerInfo extends WrapperInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewerInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IWrapperInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected WrapperByMethod createWrapper() {
		return new WrapperByMethod(this, JavaInfoUtils.getParameter(this, "viewer.control.method")) {
			@Override
			protected void configureParameter(ParameterDescription parameter, JavaInfo parameterJavaInfo)
					throws Exception {
				if (parameter.isParent()
						&& parameter.getType() == Composite.class
						&& parameterJavaInfo instanceof CompositeInfo) {
					configureHierarchy(parameterJavaInfo);
				}
			}
		};
	}
}