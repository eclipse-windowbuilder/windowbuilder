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
package org.eclipse.wb.internal.rcp.model.jface.viewers;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.WrapperByMethod;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.jface.WrapperInfo;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;

/**
 * Model for any JFace {@link org.eclipse.jface.viewers.ViewerColumn}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage rcp.model.jface.viewers
 */
public class ViewerColumnInfo extends WrapperInfo {
	private ViewerInfo m_viewerInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewerColumnInfo(AstEditor editor,
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
		return new WrapperByMethod(this, JavaInfoUtils.getParameter(this, "ViewerColumn.method")) {
			@Override
			protected CreationSupport newWrappedCreationSupport() throws Exception {
				return new ViewerColumnCreationSupport(ViewerColumnInfo.this);
			}

			@Override
			protected void configureParameter(ParameterDescription parameter, JavaInfo parameterJavaInfo)
					throws Exception {
				if (parameterJavaInfo instanceof ViewerInfo) {
					m_viewerInfo = (ViewerInfo) parameterJavaInfo;
				}
				if (parameter.isParent()
						&& (parameter.getType() == getWrappedType() || parameterJavaInfo instanceof ViewerInfo)
						&& m_viewerInfo != null) {
					configureHierarchy(m_viewerInfo);
				}
			}

			@Override
			protected CreationSupport newControlCreationSupport() {
				return new ViewerColumnWidgetCreationSupport(ViewerColumnInfo.this);
			}

			@Override
			protected Association newControlAssociation() {
				return new ViewerColumnWidgetAssociation(ViewerColumnInfo.this);
			}

			@Override
			protected void configureHierarchy(JavaInfo parent, JavaInfo control) throws Exception {
				softAddChild(parent.getParent(), control);
				softAddChild(control, ViewerColumnInfo.this);
			}
		};
	}
}
