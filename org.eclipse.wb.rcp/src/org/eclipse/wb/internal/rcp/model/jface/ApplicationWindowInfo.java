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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.broadcast.GenericPropertyGetValue;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;

import java.util.List;

/**
 * Model for {@link ApplicationWindow}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage rcp.model.jface
 */
public final class ApplicationWindowInfo extends WindowInfo {
	private final ApplicationWindowInfo m_this = this;
	private JavaInfo m_statusLineManager;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ApplicationWindowInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		addBroadcastListener(new GenericPropertyGetValue() {
			@Override
			public void invoke(GenericPropertyImpl property, Object[] value) throws Exception {
				// return status manager's 'message' property as value of ApplicationWindow.status property.
				if (isStatusManagerMessageProperty(property)) {
					value[0] = m_this.getPropertyByTitle("status").getValue();
				}
			}
		});
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void bindComponents(List<JavaInfo> components) throws Exception {
				// bind IContributionManager's to ApplicationWindow
				for (JavaInfo component : components) {
					if (component.getParent() == null
							&& ReflectionUtils.isSuccessorOf(
									component.getDescription().getComponentClass(),
									"org.eclipse.jface.action.IContributionManager")
							&& isReturnedFromMethod(component)) {
						addChild(component);
						component.setAssociation(new EmptyAssociation());
						if (isStatusLineManager(component)) {
							m_statusLineManager = component;
						}
					}
				}
			}

			private boolean isReturnedFromMethod(JavaInfo javaInfo) {
				for (ASTNode node : javaInfo.getRelatedNodes()) {
					if (node.getLocationInParent() == ReturnStatement.EXPRESSION_PROPERTY) {
						return true;
					}
				}
				return false;
			}

			@Override
			public void setPropertyExpression(GenericPropertyImpl property,
					String[] source,
					Object[] value,
					boolean[] shouldSet) throws Exception {
				// set status manager's 'message' property as value of ApplicationWindow.status property.
				if (isStatusManagerMessageProperty(property)) {
					m_this.getPropertyByTitle("status").setValue(value[0]);
					shouldSet[0] = false;
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// StatusLineManager
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean isStatusManagerMessageProperty(GenericPropertyImpl property) {
		JavaInfo statusLineManager = getStatusLineManager();
		if (statusLineManager != null && statusLineManager == property.getJavaInfo()) {
			return "message".equals(property.getTitle());
		}
		return false;
	}

	private JavaInfo getStatusLineManager() {
		return m_statusLineManager;
	}

	private boolean isStatusLineManager(JavaInfo javaInfo) {
		return StatusLineManager.class.isAssignableFrom(javaInfo.getDescription().getComponentClass());
	}
}
