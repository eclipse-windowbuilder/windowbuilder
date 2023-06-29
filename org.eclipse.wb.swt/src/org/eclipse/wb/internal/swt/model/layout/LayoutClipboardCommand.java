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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.internal.core.model.clipboard.ComponentClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * Command for pasting {@link ControlInfo} on {@link CompositeInfo} using layout specific way.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public abstract class LayoutClipboardCommand<L extends LayoutInfo>
extends
ComponentClipboardCommand<CompositeInfo> {
	private static final long serialVersionUID = 0L;
	private final JavaInfoMemento m_controlMemento;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutClipboardCommand(ControlInfo control) throws Exception {
		m_controlMemento = JavaInfoMemento.createMemento(control);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@SuppressWarnings("unchecked")
	public final void execute(CompositeInfo composite) throws Exception {
		ControlInfo control = (ControlInfo) m_controlMemento.create(composite);
		L layout = (L) composite.getLayout();
		add(layout, control);
		m_controlMemento.apply();
	}

	/**
	 * Adds given {@link ControlInfo} to {@link CompositeInfo} using layout specific way.
	 */
	protected abstract void add(L layout, ControlInfo control) throws Exception;
}
