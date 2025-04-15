/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.utils.reflect;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Helper for setting properties for {@link ClassLoader}.
 * <p>
 * http://java.dzone.com/articles/classloaderlocal-how-avoid
 *
 * @author Jevgeni Kabanov
 * @author scheglov_ke
 * @coverage core.util
 */
public class ClassLoaderLocalMap {
	private static final Object NULL_OBJECT = new Object();
	private static final Map<Object, Map<Object, Object>> globalMap = Collections.synchronizedMap(new WeakHashMap<>());

	////////////////////////////////////////////////////////////////////////////
	//
	// Map
	//
	////////////////////////////////////////////////////////////////////////////
	public static boolean containsKey(ClassLoader cl, Object key) {
		synchronized (NULL_OBJECT) {
			return getLocalMap(cl).containsKey(key);
		}
	}

	public static void put(ClassLoader cl, Object key, Object value) {
		synchronized (NULL_OBJECT) {
			getLocalMap(cl).put(key, value);
		}
	}

	public static Object get(ClassLoader cl, Object key) {
		synchronized (NULL_OBJECT) {
			return getLocalMap(cl).get(key);
		}
	}

	private static Map<Object, Object> getLocalMap(ClassLoader key) {
		Object gkey = key;
		if (gkey == null) {
			gkey = NULL_OBJECT;
		}
		return globalMap.computeIfAbsent(gkey, ignore -> new WeakHashMap<>());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClassLoaderInitializer
	//
	////////////////////////////////////////////////////////////////////////////
	public static class ClassLoaderLocalMapManager implements IClassLoaderInitializer {
		@Override
		public void initialize(ClassLoader classLoader) {
			// no-op
		}

		@Override
		public void deinitialize(ClassLoader classLoader) {
			globalMap.clear();
		}
	}
}
