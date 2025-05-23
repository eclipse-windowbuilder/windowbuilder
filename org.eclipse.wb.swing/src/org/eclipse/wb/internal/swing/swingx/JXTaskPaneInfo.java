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
package org.eclipse.wb.internal.swing.swingx;

import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.JPanelInfo;

import java.awt.Component;
import java.util.List;

/**
 * Model for <code>org.jdesktop.swingx.JXTaskPane</code>.
 *
 * @author sablin_aa
 * @coverage swingx.model
 */
public final class JXTaskPaneInfo extends JPanelInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JXTaskPaneInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link ComponentInfo} using {@link ActionInfo}.
	 *
	 * @return the created {@link ComponentInfo}.
	 */
	public ComponentInfo command_CREATE(ActionInfo action, ComponentInfo nextComponent)
			throws Exception {
		// ensure that ActionInfo is already added
		if (action.getParent() == null) {
			ActionContainerInfo.add(getRootJava(), action);
		}
		// prepare CreationSupport
		CreationSupport creationSupport;
		{
			String source = TemplateUtils.format("add({0})", action);
			creationSupport = new ImplicitFactoryCreationSupport("add(javax.swing.Action)", source);
		}
		// create Component
		ComponentInfo newComponent =
				(ComponentInfo) JavaInfoUtils.createJavaInfo(getEditor(), Component.class, creationSupport);
		JavaInfoUtils.add(newComponent, AssociationObjects.invocationVoid(), this, nextComponent);
		getBroadcastObject().select(List.of(newComponent));
		return newComponent;
	}
}
