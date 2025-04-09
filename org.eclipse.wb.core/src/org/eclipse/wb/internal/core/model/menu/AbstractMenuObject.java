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
package org.eclipse.wb.internal.core.model.menu;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.draw2d.EventListenerList;

/**
 * Abstract implementation of {@link IMenuObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.menu
 */
public abstract class AbstractMenuObject implements IMenuObjectInfo {
	protected final ObjectInfo m_component;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractMenuObject(ObjectInfo component) {
		m_component = component;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Model
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getToolkitModel() {
		return m_component;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Listener
	//
	////////////////////////////////////////////////////////////////////////////
	private final EventListenerList m_eventTable = new EventListenerList();

	@Override
	public final void addListener(IMenuObjectListener listener) {
		m_eventTable.addListener(IMenuObjectListener.class, listener);
	}

	@Override
	public final void removeListener(IMenuObjectListener listener) {
		m_eventTable.removeListener(IMenuObjectListener.class, listener);
	}

	/**
	 * Notifies {@link IMenuObjectListener#refresh()}'s.
	 */
	protected final void fireRefreshListeners() {
		for (IMenuObjectListener listener : m_eventTable.getListenersIterable(IMenuObjectListener.class)) {
			listener.refresh();
		}
	}

	/**
	 * Notifies {@link IMenuObjectListener#deleting(Object)}.
	 */
	protected final void fireDeleteListeners(Object toolkitModel) {
		for (IMenuObjectListener listener : m_eventTable.getListenersIterable(IMenuObjectListener.class)) {
			listener.deleting(toolkitModel);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void executeEdit(RunnableEx runnable) {
		ExecutionUtils.run(m_component, runnable);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final boolean canMove() {
		return GlobalState.getValidatorHelper().canReorder(m_component);
	}

	@Override
	public final boolean canReparent() {
		return GlobalState.getValidatorHelper().canReparent(m_component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this {@link JavaMenuMenuObject} is root of menu hierarchy.
	 */
	protected final boolean isRoot() {
		return MenuObjectInfoUtils.getMenuInfo(m_component.getParent()) == null;
	}

	/**
	 * @return <code>true</code> if given <code>child</code> belong our menu hierarchy.
	 */
	protected final boolean isRootFor(ObjectInfo child) {
		return isRoot() && child != null && m_component.isItOrParentOf(child);
	}
}
