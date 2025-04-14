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
package org.eclipse.wb.internal.core.utils.reflect;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Helper for setting properties for {@link ClassLoader}.
 *
 * @author Jevgeni Kabanov
 * @author scheglov_ke
 * @coverage core.util
 */
public class ClassLoaderLocalMap {
	private static final Map<ClassLoader, Map<Object, Object>> globalMap = Collections
			.synchronizedMap(new WeakHashMap<>());

	////////////////////////////////////////////////////////////////////////////
	//
	// Map
	//
	////////////////////////////////////////////////////////////////////////////
	public static boolean containsKey(ClassLoader cl, Object key) {
		if (cl == null) {
			cl = ClassLoaderLocalMap.class.getClassLoader();
		}
		// synchronizing over ClassLoader is usually safest
		synchronized (cl) {
			if (!globalMap.containsKey(cl)) {
				return false;
			}
			return getLocalMap(cl).containsKey(key);
		}
	}

	public static void put(ClassLoader cl, Object key, Object value) {
		if (cl == null) {
			cl = ClassLoaderLocalMap.class.getClassLoader();
		}
		// synchronizing over ClassLoader is usually safest
		synchronized (cl) {
			getLocalMap(cl).put(key, value);
		}
	}

	public static Object get(ClassLoader cl, Object key) {
		if (cl == null) {
			return globalMap.get(key);
		}
		// synchronizing over ClassLoader is usually safest
		synchronized (cl) {
			return getLocalMap(cl).get(key);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////

	private static Map<Object, Object> getLocalMap(ClassLoader cl) {
		return globalMap.computeIfAbsent(cl, ignore -> new WeakHashMap<>());
	}
}
