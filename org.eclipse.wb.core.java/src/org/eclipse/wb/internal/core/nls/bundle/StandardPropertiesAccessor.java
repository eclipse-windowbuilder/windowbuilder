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
package org.eclipse.wb.internal.core.nls.bundle;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Accessor that uses standard {@link Properties}.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public class StandardPropertiesAccessor implements IPropertiesAccessor {
	public static final StandardPropertiesAccessor INSTANCE = new StandardPropertiesAccessor();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private StandardPropertiesAccessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPropertiesAccessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, String> load(InputStream is, String charset) throws Exception {
		Properties properties = new Properties();
		load0(properties, is, charset);
		return (Map) properties;
	}

	@Override
	public void save(OutputStream os, String charset, Map<String, String> map, String comments)
			throws Exception {
		SortedProperties sorted = new SortedProperties();
		sorted.putAll(map);
		store0(sorted, os, charset, comments);
	}

	private static void load0(Properties properties, InputStream is, String charset) throws Exception {
		if (charset.equals("UTF-8")) {
			Method loadMethod = Properties.class.getMethod("load", Reader.class);
			Reader reader = new InputStreamReader(is, charset);
			loadMethod.invoke(properties, reader);
		} else {
			properties.load(is);
		}
	}

	private static void store0(Properties properties, OutputStream os, String charset, String comments)
			throws Exception {
		if (charset.equals("UTF-8")) {
			Method loadMethod = Properties.class.getMethod("store", Writer.class, String.class);
			Writer reader = new OutputStreamWriter(os, charset);
			loadMethod.invoke(properties, reader, comments);
		} else {
			properties.store(os, comments);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Stored properties
	//
	////////////////////////////////////////////////////////////////////////////
	private static class SortedProperties extends Properties {
		private static final long serialVersionUID = 0L;

		class IteratorWrapper implements Enumeration<Object> {
			Iterator<String> iterator;

			public IteratorWrapper(Iterator<String> iterator) {
				this.iterator = iterator;
			}

			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public String nextElement() {
				return iterator.next();
			}
		}

		@Override
		public synchronized Enumeration<Object> keys() {
			TreeSet<String> set = new TreeSet<>();
			for (Enumeration<Object> e = super.keys(); e.hasMoreElements();) {
				set.add((String) e.nextElement());
			}
			return new IteratorWrapper(set.iterator());
		}
	}
}
