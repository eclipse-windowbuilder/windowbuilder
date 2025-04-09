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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import org.eclipse.jface.action.IMenuManager;

/**
 * Implementation of single listener {@link Property}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
public abstract class AbstractListenerProperty extends AbstractEventProperty {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractListenerProperty(JavaInfo javaInfo, String title, PropertyEditor propertyEditor) {
		super(javaInfo, title, propertyEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes listener for this event.
	 */
	protected abstract void removeListener() throws Exception;

	/**
	 * Contributes actions into context menu.
	 */
	protected abstract void addListenerActions(IMenuManager manager, IMenuManager implementMenuManager)
			throws Exception;
}
