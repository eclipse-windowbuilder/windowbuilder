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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateIcon;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;

import java.lang.reflect.Method;

/**
 * Implementation of {@link CreationSupport} for {@link ControlInfo} exposed from
 * {@link FieldEditorInfo} using <code>getXXXControl(Composite)</code>.
 * <p>
 * For example {@link StringFieldEditor#getTextControl(org.eclipse.swt.widgets.Composite)}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class FieldEditorSubComponentCreationSupport extends CreationSupport
implements
IImplicitCreationSupport {
	private final FieldEditorInfo m_fieldEditor;
	private final Method m_getMethod;
	private final String m_getMethodSignature;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FieldEditorSubComponentCreationSupport(FieldEditorInfo fieldEditor, Method getMethod) {
		m_fieldEditor = fieldEditor;
		m_getMethod = getMethod;
		m_getMethodSignature = ReflectionUtils.getMethodSignature(m_getMethod);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "method: " + m_getMethod.getName();
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
		m_fieldEditor.addBroadcastListener(new JavaInfoSetObjectAfter() {
			@Override
			public void invoke(JavaInfo target, Object o) throws Exception {
				if (target == m_fieldEditor) {
					Object object = getObject();
					m_javaInfo.setObject(object);
				}
			}
		});
		// set initial Object
		m_javaInfo.setObject(getObject());
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
		if (node instanceof MethodInvocation invocation) {
			return invocation.arguments().size() == 1
					&& invocation.getName().getIdentifier().equals(m_getMethod.getName())
					&& m_fieldEditor.isRepresentedBy(invocation.getExpression());
		}
		return false;
	}

	@Override
	public ASTNode getNode() {
		return m_fieldEditor.getCreationSupport().getNode();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Special access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Object} for sub-component exposed by this
	 *         {@link FieldEditorSubComponentCreationSupport}.
	 */
	Object getObject() throws Exception {
		Object fieldEditorObject = m_fieldEditor.getObject();
		Expression parentExpression =
				FieldEditorSubComponentVariableSupport.getParentExpression(m_fieldEditor);
		Object parentObject = JavaInfoEvaluationHelper.getValue(parentExpression);
		Object object = m_getMethod.invoke(fieldEditorObject, parentObject);
		return object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Components with {@link FieldEditorSubComponentCreationSupport} can be "deleted", but for them
	 * this means that they delete their children and related nodes, but keep themselves in parent.
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
		final String getMethodSignature = m_getMethodSignature;
		return new IClipboardImplicitCreationSupport() {
			private static final long serialVersionUID = 0L;

			@Override
			public JavaInfo find(JavaInfo host) throws Exception {
				for (JavaInfo child : host.getChildrenJava()) {
					if (child.getCreationSupport() instanceof FieldEditorSubComponentCreationSupport) {
						FieldEditorSubComponentCreationSupport exposedCreation =
								(FieldEditorSubComponentCreationSupport) child.getCreationSupport();
						if (exposedCreation.m_getMethodSignature.equals(getMethodSignature)) {
							return child;
						}
					}
				}
				return null;
			}
		};
	}
}
