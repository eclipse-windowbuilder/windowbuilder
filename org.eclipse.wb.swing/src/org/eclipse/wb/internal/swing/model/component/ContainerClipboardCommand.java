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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;

/**
 * {@link ClipboardCommand} for adding {@link ComponentInfo} to {@link ContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.model
 */
public abstract class ContainerClipboardCommand<T> extends ClipboardCommand {
	private static final long serialVersionUID = 0L;
	private final JavaInfoMemento m_memento;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ContainerClipboardCommand(ComponentInfo component) throws Exception {
		m_memento = JavaInfoMemento.createMemento(component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@SuppressWarnings("unchecked")
	public final void execute(JavaInfo javaInfo) throws Exception {
		T container = (T) javaInfo;
		ComponentInfo component = (ComponentInfo) m_memento.create(javaInfo);
		add(container, component);
		m_memento.apply();
	}

	/**
	 * Adds given {@link ComponentInfo} to {@link ContainerInfo} using container specific way.
	 */
	protected abstract void add(T container, ComponentInfo component) throws Exception;
}
