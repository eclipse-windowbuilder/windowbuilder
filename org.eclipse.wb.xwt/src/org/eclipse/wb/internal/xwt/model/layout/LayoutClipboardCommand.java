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
package org.eclipse.wb.internal.xwt.model.layout;

import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

/**
 * Abstract command for pasting {@link ControlInfo} on {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public abstract class LayoutClipboardCommand<L extends LayoutInfo>
extends
CompositeClipboardCommand {
	private static final long serialVersionUID = 0L;
	private final XmlObjectMemento m_controlMemento;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutClipboardCommand(ControlInfo control) throws Exception {
		m_controlMemento = XmlObjectMemento.createMemento(control);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ClipboardCommand
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@SuppressWarnings("unchecked")
	protected void execute(CompositeInfo composite) throws Exception {
		ControlInfo control = (ControlInfo) m_controlMemento.create(composite);
		L layout = (L) composite.getLayout();
		add(layout, control);
		m_controlMemento.apply();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LayoutClipboardCommand
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds given {@link ControlInfo} to {@link CompositeInfo} using layout specific way.
	 */
	protected abstract void add(L layout, ControlInfo control) throws Exception;
}
