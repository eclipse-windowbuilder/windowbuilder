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
package org.eclipse.wb.internal.core.editor.palette.model.entry;

import org.eclipse.core.runtime.IConfigurationElement;

import org.xml.sax.Attributes;

import java.util.Map;

/**
 * Factory for {@link AttributesProvider} implementations.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class AttributesProviders {
	public static AttributesProvider get(final IConfigurationElement element) {
		return new AttributesProvider() {
			@Override
			public String getAttribute(String name) {
				return element.getAttribute(name);
			}
		};
	}

	public static AttributesProvider get(final Attributes attributes) {
		return new AttributesProvider() {
			@Override
			public String getAttribute(String name) {
				return attributes.getValue(name);
			}
		};
	}

	public static AttributesProvider get(final Map<String, String> attributes) {
		return new AttributesProvider() {
			@Override
			public String getAttribute(String name) {
				return attributes.get(name);
			}
		};
	}
}
