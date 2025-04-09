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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.custom.ScrolledComposite;

/**
 * Model for {@link ScrolledComposite}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class ScrolledCompositeInfo extends CompositeInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ScrolledCompositeInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// when move ControlInfo out: remove "setContent()" and "setMinSize()" invocations
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				if (child instanceof ControlInfo && oldParent == ScrolledCompositeInfo.this) {
					removeMethodInvocations("setContent(org.eclipse.swt.widgets.Control)");
					removeMethodInvocations("setMinSize(org.eclipse.swt.graphics.Point)");
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
	 * @return the {@link ControlInfo} set using given
	 *         {@link ScrolledComposite#setContent(org.eclipse.swt.widgets.Control)}.
	 */
	public ControlInfo getContent() {
		MethodInvocation invocation =
				getMethodInvocation("setContent(org.eclipse.swt.widgets.Control)");
		if (invocation != null) {
			return (ControlInfo) getChildRepresentedBy(DomGenerics.arguments(invocation).get(0));
		} else {
			return null;
		}
	}

	/**
	 * @return <code>true</code> if this {@link ScrolledCompositeInfo} has child {@link ControlInfo}
	 *         and corresponding {@link ScrolledComposite#setContent(org.eclipse.swt.widgets.Control)}
	 *         .
	 */
	public boolean hasRequired_setContent() {
		return getChildrenControls().isEmpty()
				|| getMethodInvocation("setContent(org.eclipse.swt.widgets.Control)") != null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link ControlInfo} and associates it with this {@link ScrolledCompositeInfo}.
	 */
	public void command_CREATE(ControlInfo control) throws Exception {
		JavaInfoUtils.add(control, null, this, null);
		attach(control);
	}

	/**
	 * Adds existing {@link ControlInfo} and associates it with this {@link ScrolledCompositeInfo}.
	 */
	public void command_ADD(ControlInfo control) throws Exception {
		JavaInfoUtils.move(control, null, this, null);
		attach(control);
	}

	/**
	 * Attaches just added/moved {@link ControlInfo} to this {@link ScrolledCompositeInfo}.
	 */
	private void attach(ControlInfo control) throws Exception {
		// configure "setMinSize()"
		{
			String arguments =
					TemplateUtils.format("{0}.computeSize({1}, {1})", control, "org.eclipse.swt.SWT.DEFAULT");
			MethodInvocation invocation =
					addMethodInvocation("setMinSize(org.eclipse.swt.graphics.Point)", arguments);
			control.addRelatedNodes(invocation);
		}
		// associate using "setContent()"
		{
			MethodInvocation invocation =
					addMethodInvocation(
							"setContent(org.eclipse.swt.widgets.Control)",
							TemplateUtils.getExpression(control));
			control.addRelatedNodes(invocation);
		}
	}
}
