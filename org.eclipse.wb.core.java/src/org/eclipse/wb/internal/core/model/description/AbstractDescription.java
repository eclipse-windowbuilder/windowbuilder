/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.core.runtime.IAdaptable;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract superclass for all description objects.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class AbstractDescription implements IAdaptable {
	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * This description is used for passed {@link JavaInfo}.
	 */
	public static final int STATE_USE = 0;
	/**
	 * {@link JavaInfo} toolkit object was initialized and ready to be used. For example, In GWT-Ext
	 * we can get default values for properties only when we have Element, i.e. bound to parent.
	 */
	public static final int STATE_OBJECT_READY = 1;

	/**
	 * Sends this {@link AbstractDescription} message that given {@link JavaInfo} is now in given
	 * state, so some action can be done. This message will be also transmitted to all children of
	 * this description object.
	 */
	public void visit(JavaInfo javaInfo, int state) throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAdaptable
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tags
	//
	////////////////////////////////////////////////////////////////////////////
	private Map<String, String> m_tags;

	/**
	 * @return the {@link Map} of "tag name" to "tag value". Can return empty {@link Map}, but not
	 *         <code>null</code>.
	 */
	public final Map<String, String> getTags() {
		if (m_tags == null) {
			return Collections.emptyMap();
		}
		return m_tags;
	}

	/**
	 * @return the value of given tag, can return <code>null</code>.
	 */
	public final String getTag(String tag) {
		return getTags().get(tag);
	}

	/**
	 * @return <code>true</code> if this {@link AbstractDescription} has given tag with value
	 *         <code>"true"</code>.
	 */
	public final boolean hasTrueTag(String tag) {
		return "true".equals(getTag(tag));
	}

	/**
	 * Sets the value of given tag.
	 */
	public final void putTag(String tag, String value) {
		if (m_tags == null) {
			m_tags = new TreeMap<>();
		}
		m_tags.put(tag, value);
	}

	/**
	 * Adds tags from given {@link AbstractDescription}.
	 */
	public final void putTags(Map<String, String> tags) {
		if (tags != null && !tags.isEmpty()) {
			if (m_tags == null) {
				m_tags = new TreeMap<>();
			}
			m_tags.putAll(tags);
		}
	}
}
