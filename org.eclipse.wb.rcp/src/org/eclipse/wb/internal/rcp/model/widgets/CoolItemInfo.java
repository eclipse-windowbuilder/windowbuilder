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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
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
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.CoolItem;

import java.util.Collections;
import java.util.List;

/**
 * Model for {@link CoolItem}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class CoolItemInfo extends ItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CoolItemInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		addBroadcastListener(new JavaEventListener() {
			private ControlInfo m_ourControl;

			@Override
			public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				// remove setControl() invocation when move ControlInfo in/from our CoolBar
				if (oldParent == getParent() && child instanceof ControlInfo && getControl() == child) {
					removeMethodInvocations("setControl(org.eclipse.swt.widgets.Control)");
				}
				// when WE are moved, remove possible setControl() invocation
				if (child == CoolItemInfo.this) {
					m_ourControl = getControl();
					removeMethodInvocations("setControl(org.eclipse.swt.widgets.Control)");
				}
			}

			@Override
			public void variable_addStatementsToMove(JavaInfo parent, List<JavaInfo> children)
					throws Exception {
				if (parent == CoolItemInfo.this) {
					if (m_ourControl != null) {
						children.add(m_ourControl);
					}
				}
			}

			@Override
			public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				// if WE were moved, move our ControlInfo
				if (child == CoolItemInfo.this) {
					if (m_ourControl != null) {
						command_ADD(m_ourControl);
					}
				}
			}
		});
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
			return GenericsUtils.singletonList(control);
		}

		@Override
		public List<ObjectInfo> getChildrenGraphical() throws Exception {
			return getChildrenTree();
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return list with single {@link #getControl()} or empty.
	 */
	public final List<ObjectInfo> getSimpleContainerChildren() {
		ObjectInfo control = getControl();
		if (control != null) {
			return List.of(control);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public CoolItem getWidget() {
		return (CoolItem) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		{
			Rectangle bounds = new Rectangle(getWidget().getBounds());
			setModelBounds(bounds);
		}
		super.refresh_fetch();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setControl() support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ControlInfo} set using
	 *         {@link CoolItem#setControl(org.eclipse.swt.widgets.Control)}, may be <code>null</code>.
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
	 * Adds new {@link ControlInfo} to {@link CoolBarInfo} and associates with given
	 * {@link CoolItemInfo} set using {@link CoolItem#setControl(org.eclipse.swt.widgets.Control)}.
	 */
	public void command_CREATE(ControlInfo control) throws Exception {
		// add to CoolBar
		{
			StatementTarget target = JavaInfoUtils.getTarget(this, control, null);
			JavaInfoUtils.addTarget(control, null, getParentJava(), target);
		}
		// add setControl() invocation
		addSetControlInvocation(control);
	}

	/**
	 * Adds existing {@link ControlInfo} to {@link CoolBarInfo} and associates with given
	 * {@link CoolItemInfo} set using {@link CoolItem#setControl(org.eclipse.swt.widgets.Control)}.
	 */
	public void command_ADD(final ControlInfo control) throws Exception {
		// move to CoolBar, but code inside of CoolItem
		{
			final CoolBarInfo coolBar = (CoolBarInfo) getParentJava();
			final StatementTarget target = JavaInfoUtils.getTarget(this, control, null);
			IMoveTargetProvider targetProvider = new IMoveTargetProvider() {
				@Override
				public void add() throws Exception {
					coolBar.addChild(control, getNextJavaInfo());
				}

				@Override
				public void move() throws Exception {
					coolBar.moveChild(control, getNextJavaInfo());
				}

				@Override
				public StatementTarget getTarget() throws Exception {
					return target;
				}

				private JavaInfo getNextJavaInfo() {
					return GenericsUtils.getNextOrNull(coolBar.getChildrenJava(), CoolItemInfo.this);
				}
			};
			JavaInfoUtils.moveProvider(control, null, coolBar, targetProvider);
		}
		// add setControl() invocation
		addSetControlInvocation(control);
	}

	/**
	 * Adds {@link CoolItem#setControl(org.eclipse.swt.widgets.Control)} invocation.
	 */
	private void addSetControlInvocation(ControlInfo control) throws Exception {
		String source = TemplateUtils.format("{0}.setControl({1})", this, control);
		Expression expression = control.addExpressionStatement(source);
		addRelatedNodes(expression);
	}
}
