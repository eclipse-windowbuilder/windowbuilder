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
package org.eclipse.wb.internal.xwt.model.property.event;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.event.EventsPropertyUtils;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.event.AbstractListenerProperty;
import org.eclipse.wb.internal.xwt.editor.XwtPairResourceProvider;
import org.eclipse.wb.internal.xwt.model.util.NameSupport;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;

/**
 * {@link Property} for single XML event.
 *
 * @author scheglov_ke
 * @coverage XWT.model.property
 */
public final class XwtListenerProperty extends AbstractListenerProperty {
	private final String m_name;
	private final String m_attribute;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XwtListenerProperty(XmlObjectInfo object, String name) {
		super(object, name, new XwtListenerPropertyEditor(name));
		m_name = name;
		m_attribute = name + "Event";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Property
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isModified() throws Exception {
		return m_object.getAttribute(m_attribute) != null;
	}

	@Override
	public void setValue(Object value) throws Exception {
		if (value == UNKNOWN_VALUE) {
			if (MessageDialog.openConfirm(
					DesignerPlugin.getShell(),
					"Confirm",
					"Do you really want delete event '" + m_name + "' and its method?")) {
				removeListener();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void removeListener() throws Exception {
		deleteMethodDeclaration();
		m_object.removeAttribute(m_attribute);
		ExecutionUtils.refresh(m_object);
	}

	/**
	 * Deletes corresponding {@link MethodDeclaration} in Java, if exists.
	 */
	private void deleteMethodDeclaration() throws Exception {
		prepareJavaFile();
		if (m_javaFile == null) {
			return;
		}
		// update Java AST
		try {
			prepareAST();
			MethodDeclaration method = getMethodDeclaration0(false);
			if (method != null) {
				m_editor.removeBodyDeclaration(method);
				saveAST();
			}
		} finally {
			clearAST();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addListenerActions(IMenuManager manager, IMenuManager implementMenuManager)
			throws Exception {
		IAction[] actions = createListenerMethodActions();
		// append existing stub action
		if (actions[0] != null) {
			manager.appendToGroup(IContextMenuConstants.GROUP_EVENTS, actions[0]);
		}
		// append existing or new method action
		implementMenuManager.add(actions[0] != null ? actions[0] : actions[1]);
	}

	/**
	 * For given {@link ListenerMethodProperty} creates two {@link Action}'s:
	 *
	 * [0] - for existing stub method, may be <code>null</code>;<br>
	 * [1] - for creating new stub method.
	 */
	private IAction[] createListenerMethodActions() {
		IAction[] actions = new IAction[2];
		// try to find existing stub method
		{
			String methodName = m_object.getAttribute(m_attribute);
			if (methodName != null) {
				actions[0] = new ObjectInfoAction(m_object) {
					@Override
					protected void runEx() throws Exception {
						openListener();
					}
				};
				actions[0].setText(m_name + " -> " + methodName);
				actions[0].setImageDescriptor(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR);
			}
		}
		// in any case prepare action for creating new stub method
		{
			actions[1] = new ObjectInfoAction(m_object) {
				@Override
				protected void runEx() throws Exception {
					openListener();
				}
			};
			actions[1].setText(m_name);
			actions[1].setImageDescriptor(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR);
		}
		//
		return actions;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handler
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void openListener() throws Exception {
		MethodDeclaration method = getMethodDeclaration();
		if (method != null) {
			openMethod_inEditor(method);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AST
	//
	////////////////////////////////////////////////////////////////////////////
	private AstEditor m_editor;
	private TypeDeclaration m_typeDeclaration;

	/**
	 * Prepares {@link #m_editor} for {@link #m_javaFile}.
	 */
	private void prepareAST() throws Exception {
		ICompilationUnit unit = JavaCore.createCompilationUnitFrom(m_javaFile);
		m_editor = new AstEditor(unit);
		m_typeDeclaration = DomGenerics.types(m_editor.getAstUnit()).get(0);
	}

	/**
	 * Saves changes performed in {@link #m_editor}.
	 */
	private void saveAST() throws Exception {
		m_editor.saveChanges(false);
	}

	/**
	 * Clears {@link #m_editor} after finishing AST operations.
	 */
	private void clearAST() {
		m_editor = null;
		m_typeDeclaration = null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private IFile m_javaFile;

	private MethodDeclaration getMethodDeclaration() throws Exception {
		prepareJavaFile();
		if (m_javaFile == null) {
			return null;
		}
		// find in Java AST
		try {
			prepareAST();
			return getMethodDeclaration0(true);
		} finally {
			clearAST();
		}
	}

	private void prepareJavaFile() {
		IFile xwtFile = m_object.getContext().getFile();
		m_javaFile = XwtPairResourceProvider.INSTANCE.getPair(xwtFile);
	}

	/**
	 * @return the existing or new {@link MethodDeclaration} for this event.
	 */
	private MethodDeclaration getMethodDeclaration0(boolean addNew) throws Exception {
		// try to find existing
		{
			String methodName = m_object.getAttribute(m_attribute);
			if (methodName != null) {
				return AstNodeUtils.getMethodByName(m_typeDeclaration, methodName);
			}
		}
		// if only existing
		if (!addNew) {
			return null;
		}
		// prepare method name
		String methodName;
		{
			String baseName = "on";
			{
				String objectName = NameSupport.getName(m_object);
				if (objectName != null) {
					baseName += StringUtils.capitalize(objectName);
				}
			}
			baseName += m_name;
			methodName = m_editor.getUniqueMethodName(baseName);
		}
		// add new method
		MethodDeclaration method =
				m_editor.addMethodDeclaration(
						"public void " + methodName + "(org.eclipse.swt.widgets.Event event)",
						Collections.emptyList(),
						new BodyDeclarationTarget(m_typeDeclaration, false));
		saveAST();
		// set method in XML
		m_object.setAttribute(m_attribute, methodName);
		ExecutionUtils.refresh(m_object);
		// done
		return method;
	}

	/**
	 * Opens source of given Java {@link IFile} at position that corresponds {@link MethodDeclaration}
	 * .
	 */
	private void openMethod_inEditor(MethodDeclaration method) throws Exception {
		IEditorPart javaEditor = IDE.openEditor(DesignerPlugin.getActivePage(), m_javaFile);
		if (javaEditor instanceof ITextEditor) {
			((ITextEditor) javaEditor).selectAndReveal(method.getStartPosition(), 0);
		}
	}
}
