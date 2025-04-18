/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.SwtMethodParameterEvaluator;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * Model for {@link Window}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public class WindowInfo extends AbstractComponentInfo implements IJavaInfoRendering {
	private Shell m_shell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WindowInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		JavaInfoUtils.scheduleSpecialRendering(this);
		fillContextMenu();
	}

	/**
	 * Convenience method for accessing the {@link Window} contained by this
	 * {@link WindowInfo}.
	 */
	public Window getWindow() {
		return (Window) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	private void fillContextMenu() throws Exception {
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void addContextMenu(List<? extends ObjectInfo> objects,
					ObjectInfo object,
					IMenuManager manager) throws Exception {
				if (object == WindowInfo.this) {
					contextMenu_setMinimalSize(manager);
					contextMenu_removeSize(manager);
				}
			}
		});
	}

	/**
	 * Adds "Set minimal size" item.
	 */
	private void contextMenu_setMinimalSize(IMenuManager manager) throws Exception {
		ObjectInfoAction action = new ObjectInfoAction(this) {
			@Override
			protected void runEx() throws Exception {
				if (getMethod_getInitialSize() == null) {
					TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(WindowInfo.this);
					getEditor().addMethodDeclaration(
							"protected org.eclipse.swt.graphics.Point getInitialSize()",
							List.of("return new org.eclipse.swt.graphics.Point(545, 390);"),
							new BodyDeclarationTarget(typeDeclaration, false));
				}
				Dimension preferredSize = getPreferredSize();
				getTopBoundsSupport().setSize(preferredSize.width, preferredSize.height);
			}
		};
		action.setText(ModelMessages.WindowInfo_minSizeActionText);
		manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, action);
	}

	/**
	 * Adds "Remove getInitialSize()" item.
	 */
	private void contextMenu_removeSize(IMenuManager manager) throws Exception {
		ObjectInfoAction action = new ObjectInfoAction(this) {
			@Override
			protected void runEx() throws Exception {
				MethodDeclaration sizeMethod = getMethod_getInitialSize();
				if (sizeMethod != null) {
					getEditor().removeBodyDeclaration(sizeMethod);
				}
			}
		};
		action.setText("Remove getInitialSize()");
		manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, action);
	}

	/**
	 * @return the {@link MethodDeclaration} for "getInitialSize()", may be <code>null</code>.
	 */
	private MethodDeclaration getMethod_getInitialSize() {
		TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(this);
		return AstNodeUtils.getMethodBySignature(typeDeclaration, "getInitialSize()");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IJavaInfoRendering
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void render() throws Exception {
		Window window = getWindow();
		window.create();
		m_shell = window.getShell();
	}

	public Shell getParentShell_interceptor() throws Exception {
		ASTNode node = getCreationSupport().getNode();
		return SwtMethodParameterEvaluator.getDefaultShell(node);
	}

	public Object getInitialSize_validator(Object o) {
		if (o == null) {
			return new org.eclipse.swt.graphics.Point(450, 300);
		}
		return o;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractComponentInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected TopBoundsSupport createTopBoundsSupport() {
		return new WindowTopBoundsSupport(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hierarchy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canBeRoot() {
		return true;
	}

	@Override
	public Shell getComponentObject() {
		return m_shell;
	}

	/**
	 * @return the {@link WindowInfo}'s Shell.
	 */
	Shell getShell() {
		return m_shell;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh_dispose() throws Exception {
		// dispose Dialog
		{
			Object object = getObject();
			if (object != null) {
				ReflectionUtils.invokeMethod(object, "close()");
				m_shell = null;
			}
		}
		// call "super"
		super.refresh_dispose();
	}

	@Override
	protected void refresh_afterCreate() throws Exception {
		// preferred size, should be here, because "super" applies "top bounds"
		setPreferredSize(new Dimension(m_shell.computeSize(SWT.DEFAULT, SWT.DEFAULT)));
		// call "super"
		super.refresh_afterCreate();
	}

	@Override
	protected void refresh_fetch() throws Exception {
		ControlInfo.refresh_fetch(this, new RunnableEx() {
			@Override
			public void run() throws Exception {
				WindowInfo.super.refresh_fetch();
			}
		});
	}
}
