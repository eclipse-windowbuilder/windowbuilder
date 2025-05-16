/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.model.layout.absolute;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Class which contributes actions for components ordering in same parent.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.model.layout.absolute
 */
public final class OrderingSupport {
	private final List<? extends IAbstractComponentInfo> m_components;
	private final IAbstractComponentInfo m_component;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public OrderingSupport(List<? extends IAbstractComponentInfo> components,
			IAbstractComponentInfo component) {
		m_components = components;
		m_component = component;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void contributeActions(IMenuManager manager) {
		IMenuManager orderMenuManager = new MenuManager(ModelMessages.OrderingSupport_orderManager);
		manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, orderMenuManager);
		// add separate actions
		boolean isFirst = m_components.indexOf(m_component) == 0;
		boolean isLast = m_components.indexOf(m_component) == m_components.size() - 1;
		orderMenuManager.add(new OrderAction(ModelMessages.OrderingSupport_bringToFront,
				CoreImages.LAYOUT_ABSOLUTE_BRING_TO_FRONT, !isFirst) {
			@Override
			protected void runEx() throws Exception {
				GlobalState.getOrderProcessor().move(m_component, getFirstSibling());
			}
		});
		orderMenuManager.add(new OrderAction(ModelMessages.OrderingSupport_sendToBack,
				CoreImages.LAYOUT_ABSOLUTE_SEND_TO_BACK, !isLast) {
			@Override
			protected void runEx() throws Exception {
				GlobalState.getOrderProcessor().move(m_component, null);
			}
		});
		orderMenuManager.add(new OrderAction(ModelMessages.OrderingSupport_bringForward,
				CoreImages.LAYOUT_ABSOLUTE_BRING_FORWARD, !isFirst) {
			@Override
			protected void runEx() throws Exception {
				GlobalState.getOrderProcessor().move(m_component, getPreviousSibling());
			}
		});
		orderMenuManager.add(new OrderAction(ModelMessages.OrderingSupport_sendBackward,
				CoreImages.LAYOUT_ABSOLUTE_SEND_BACKWARD, !isLast) {
			@Override
			protected void runEx() throws Exception {
				GlobalState.getOrderProcessor().move(m_component, getNextSibling());
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the first component.
	 */
	private IAbstractComponentInfo getFirstSibling() {
		return m_components.get(0);
	}

	/**
	 * @return the component directly before {@link #m_component}, or <code>null</code> if
	 *         {@link #m_component} is first component.
	 */
	private IAbstractComponentInfo getPreviousSibling() {
		int index = m_components.indexOf(m_component);
		return index != 0 ? m_components.get(index - 1) : null;
	}

	/**
	 * @return the component directly after {@link #m_component}, or <code>null</code> if
	 *         {@link #m_component} is last or last but one component.
	 */
	private IAbstractComponentInfo getNextSibling() {
		int index = m_components.indexOf(m_component);
		return index + 2 < m_components.size() ? m_components.get(index + 2) : null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// OrderAction
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Abstract super class for ordering actions.
	 */
	private abstract class OrderAction extends ObjectInfoAction {
		public OrderAction(String text, ImageDescriptor image, boolean enabled) {
			super(m_component.getUnderlyingModel());
			setText(text);
			setImageDescriptor(image);
			setEnabled(enabled);
		}
	}
}
