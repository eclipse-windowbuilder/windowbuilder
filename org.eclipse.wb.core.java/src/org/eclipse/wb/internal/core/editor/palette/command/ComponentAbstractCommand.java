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
package org.eclipse.wb.internal.core.editor.palette.command;

import org.eclipse.wb.core.editor.palette.model.AbstractElementInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;

import org.xml.sax.Attributes;

/**
 * Abstract {@link Command} for {@link ComponentEntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public abstract class ComponentAbstractCommand extends AbstractElementCommand {
	protected final String m_className;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentAbstractCommand(String id,
			String name,
			String description,
			boolean visible,
			String className) {
		super(id, name, description, visible);
		m_className = className;
	}

	public ComponentAbstractCommand(Attributes attributes) {
		super(attributes);
		m_className = attributes.getValue("class");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected final void updateElement(AbstractElementInfo element) {
		super.updateElement(element);
		ComponentEntryInfo component = (ComponentEntryInfo) element;
		component.setComponentClassName(m_className);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addAttributes() {
		super.addAttributes();
		addAttribute("class", m_className);
	}
}
