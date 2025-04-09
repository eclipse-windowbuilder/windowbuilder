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
package org.eclipse.wb.internal.core.model.clipboard;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;

import java.util.Map;
import java.util.TreeMap;

/**
 * Command for applying properties of {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
public final class PropertiesClipboardCommand extends ClipboardCommand {
	private static final long serialVersionUID = 0L;
	private final Map<String, String> m_propertyTitleToSource = new TreeMap<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertiesClipboardCommand(JavaInfo javaInfo) throws Exception {
		for (Property property : javaInfo.getProperties()) {
			if (property instanceof GenericPropertyImpl genericProperty
					&& !property.getCategory().isSystem()
					&& property.isModified()) {
				String clipboardSource = genericProperty.getClipboardSource();
				if (clipboardSource != null) {
					m_propertyTitleToSource.put(property.getTitle(), clipboardSource);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void execute(JavaInfo javaInfo) throws Exception {
		for (Property property : javaInfo.getProperties()) {
			if (property instanceof GenericPropertyImpl genericProperty) {
				String clipboardSource = m_propertyTitleToSource.get(property.getTitle());
				if (clipboardSource != null) {
					genericProperty.setExpression(clipboardSource, Property.UNKNOWN_VALUE);
				}
			}
		}
	}
}
