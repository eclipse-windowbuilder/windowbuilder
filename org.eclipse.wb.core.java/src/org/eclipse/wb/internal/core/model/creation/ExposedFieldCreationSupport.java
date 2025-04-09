/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateIcon;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardImplicitCreationSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;

import java.lang.reflect.Field;

/**
 * Implementation of {@link CreationSupport} for object exposed using field.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class ExposedFieldCreationSupport extends CreationSupport
implements
IImplicitCreationSupport,
IExposedCreationSupport {
	private final JavaInfo m_hostJavaInfo;
	private final Field m_field;
	private final String m_fieldName;
	private final boolean m_direct;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ExposedFieldCreationSupport(JavaInfo hostJavaInfo, Field field, boolean direct) {
		m_hostJavaInfo = hostJavaInfo;
		m_field = field;
		m_fieldName = m_field.getName();
		m_direct = direct;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "field: " + m_field.getType().getName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setJavaInfo(JavaInfo javaInfo) throws Exception {
		super.setJavaInfo(javaInfo);
		// evaluation
		m_hostJavaInfo.addBroadcastListener(new JavaInfoSetObjectAfter() {
			@Override
			public void invoke(JavaInfo target, Object o) throws Exception {
				if (target == m_hostJavaInfo) {
					if (m_javaInfo.getObject() == null) {
						Object object = m_field.get(o);
						if (object != null) {
							m_javaInfo.setObject(object);
						}
					}
				}
			}
		});
		// icon decorator
		m_javaInfo.addBroadcastListener(new ObjectInfoPresentationDecorateIcon() {
			@Override
			public void invoke(ObjectInfo object, ImageDescriptor[] icon) throws Exception {
				if (object == m_javaInfo) {
					ImageDescriptor decorator = DesignerPlugin.getImageDescriptor("exposed/decorator.gif");
					icon[0] = new DecorationOverlayIcon(icon[0], decorator, IDecoration.BOTTOM_RIGHT);
				}
			}
		});
	}

	@Override
	public boolean isJavaInfo(ASTNode node) {
		if (node instanceof SimpleName simpleName) {
			return isNameOfField(simpleName)
					&& !isFieldHidden(simpleName)
					&& m_hostJavaInfo.isRepresentedBy(null);
		}
		if (node instanceof QualifiedName qualifiedName) {
			Name qualifier = qualifiedName.getQualifier();
			SimpleName simpleName = qualifiedName.getName();
			return isNameOfField(simpleName) && m_hostJavaInfo.isRepresentedBy(qualifier);
		}
		return false;
	}

	@Override
	public ASTNode getNode() {
		return m_hostJavaInfo.getCreationSupport().getNode();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean isNameOfField(SimpleName simpleName) {
		return simpleName.getIdentifier().equals(m_fieldName);
	}

	private boolean isFieldHidden(SimpleName simpleName) {
		if (AstNodeUtils.isVariable(simpleName)) {
			ExecutionFlowDescription flowDescription =
					JavaInfoUtils.getState(m_hostJavaInfo).getFlowDescription();
			return ExecutionFlowUtils.getLastAssignment(flowDescription, simpleName) != null;
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Special access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link JavaInfo} that exposes this {@link JavaInfo}.
	 */
	@Override
	public JavaInfo getHostJavaInfo() {
		return m_hostJavaInfo;
	}

	/**
	 * @return the {@link Field} used to expose this {@link JavaInfo}.
	 */
	public Field getField() {
		return m_field;
	}

	/**
	 * @return <code>true</code> if this {@link JavaInfo} is direct child of host {@link JavaInfo}.
	 */
	@Override
	public boolean isDirect() {
		return m_direct;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Components with {@link ExposedFieldCreationSupport} can be "deleted", but for them this means
	 * that they delete their children and related nodes, but keep themselves in parent.
	 */
	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void delete() throws Exception {
		JavaInfoUtils.deleteJavaInfo(m_javaInfo, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardImplicitCreationSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IClipboardImplicitCreationSupport getImplicitClipboard() {
		final String fieldName = m_fieldName;
		return new IClipboardImplicitCreationSupport() {
			private static final long serialVersionUID = 0L;

			@Override
			public JavaInfo find(JavaInfo host) throws Exception {
				for (JavaInfo child : host.getChildrenJava()) {
					if (child.getCreationSupport() instanceof ExposedFieldCreationSupport) {
						ExposedFieldCreationSupport exposedCreation =
								(ExposedFieldCreationSupport) child.getCreationSupport();
						if (exposedCreation.m_fieldName.equals(fieldName)) {
							return child;
						}
					}
				}
				return null;
			}
		};
	}
}
