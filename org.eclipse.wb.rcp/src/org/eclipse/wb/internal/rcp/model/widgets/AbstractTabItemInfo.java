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
package org.eclipse.wb.internal.rcp.model.widgets;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils.IMoveTargetProvider;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.TabItem;

import java.util.List;

/**
 * Model for {@link TabItem} or {@link CTabItem}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public abstract class AbstractTabItemInfo extends ItemInfo {
	private final AbstractTabItemInfo m_this = this;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractTabItemInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void selecting(ObjectInfo object, boolean[] refreshFlag) throws Exception {
				ControlInfo ourControl = getControl();
				if (ourControl != null) {
					if (object == ourControl || ourControl.isParentOf(object)) {
						if (getFolder().m_selectedItem != m_this) {
							doSelect();
							refreshFlag[0] = true;
						}
					}
				}
			}
		});
		addBroadcastListener(new ObjectInfoDelete() {
			@Override
			public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
				if (child == m_this) {
					ControlInfo ourControl = getControl();
					if (ourControl != null) {
						ourControl.delete();
					}
				}
			}
		});
		addBroadcastListener(new JavaEventListener() {
			private ControlInfo m_ourControl;

			@Override
			public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				// remove setControl() invocation when move ControlInfo in/from our TabFolder
				if (oldParent == getParent() && child instanceof ControlInfo && getControl() == child) {
					removeMethodInvocations("setControl(org.eclipse.swt.widgets.Control)");
				}
				// when WE are moved, remove possible setControl() invocation
				if (child == m_this) {
					m_ourControl = getControl();
					removeMethodInvocations("setControl(org.eclipse.swt.widgets.Control)");
				}
			}

			@Override
			public void variable_addStatementsToMove(JavaInfo parent, List<JavaInfo> children)
					throws Exception {
				if (parent == m_this) {
					if (m_ourControl != null) {
						children.add(m_ourControl);
					}
				}
			}

			@Override
			public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				// if WE were moved, move our ControlInfo
				if (child == m_this) {
					if (m_ourControl != null) {
						command_ADD(m_ourControl);
					}
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the parent {@link AbstractTabFolderInfo}.
	 */
	public AbstractTabFolderInfo getFolder() {
		return (AbstractTabFolderInfo) getParent();
	}

	/**
	 * Shows this {@link AbstractTabItemInfo} in its parent {@link AbstractTabFolderInfo}.
	 */
	public void doSelect() {
		if (getFolder().m_selectedItem != this) {
			getFolder().m_selectedItem = this;
			ExecutionUtils.refresh(this);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public List<ObjectInfo> getChildrenTree() throws Exception {
			ControlInfo control = getControl();
			if (control != null) {
				return ImmutableList.<ObjectInfo>of(control);
			} else {
				return ImmutableList.of();
			}
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setControl() support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ControlInfo} set using
	 *         {@link TabItem#setControl(org.eclipse.swt.widgets.Control)}, may be <code>null</code>.
	 */
	public ControlInfo getControl() {
		MethodInvocation invocation =
				getMethodInvocation("setControl(org.eclipse.swt.widgets.Control)");
		if (invocation != null) {
			Expression controlExpression = DomGenerics.arguments(invocation).get(0);
			return (ControlInfo) getParentJava().getChildRepresentedBy(controlExpression);
		}
		return null;
	}

	/**
	 * Adds new {@link ControlInfo} to {@link AbstractTabFolderInfo} and associates with given
	 * {@link AbstractTabItemInfo} set using
	 * {@link TabItem#setControl(org.eclipse.swt.widgets.Control)}.
	 */
	public void command_CREATE(ControlInfo control) throws Exception {
		// add to TabFolder
		{
			StatementTarget target = JavaInfoUtils.getTarget(this, control, null);
			JavaInfoUtils.addTarget(control, null, getParentJava(), target);
		}
		// add setControl() invocation
		addInvocation_setControl(control);
		doSelect();
	}

	/**
	 * Adds existing {@link ControlInfo} to {@link AbstractTabFolderInfo} and associates with given
	 * {@link AbstractTabItemInfo} set using
	 * {@link TabItem#setControl(org.eclipse.swt.widgets.Control)}.
	 */
	public void command_ADD(final ControlInfo control) throws Exception {
		// move to TabFolder, but code inside of TabItem
		{
			final AbstractTabFolderInfo tabFolder = getFolder();
			final StatementTarget target = JavaInfoUtils.getTarget(this, control, null);
			IMoveTargetProvider targetProvider = new IMoveTargetProvider() {
				@Override
				public void add() throws Exception {
					tabFolder.addChild(control, getNextJavaInfo());
				}

				@Override
				public void move() throws Exception {
					tabFolder.moveChild(control, getNextJavaInfo());
				}

				@Override
				public StatementTarget getTarget() throws Exception {
					return target;
				}

				private JavaInfo getNextJavaInfo() {
					return GenericsUtils.getNextOrNull(tabFolder.getChildrenJava(), m_this);
				}
			};
			JavaInfoUtils.moveProvider(control, null, tabFolder, targetProvider);
		}
		// associate with this item
		addInvocation_setControl(control);
		doSelect();
	}

	/**
	 * Adds {@link TabItem#setControl(org.eclipse.swt.widgets.Control)} invocation.
	 */
	private void addInvocation_setControl(ControlInfo control) throws Exception {
		String source = TemplateUtils.format("{0}.setControl({1})", this, control);
		Expression expression = control.addExpressionStatement(source);
		addRelatedNodes(expression);
	}
}
