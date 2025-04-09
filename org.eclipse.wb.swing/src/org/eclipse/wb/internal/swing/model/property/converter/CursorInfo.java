/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.property.converter;

import org.eclipse.wb.internal.core.DesignerPlugin;

import java.awt.Cursor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class containing all default cursors defined by the Java runtime.
 */
public record CursorInfo(int type, String name) {
	private static final class InstanceHolder {
		private static final List<CursorInfo> INSTANCE;
		static {
			List<CursorInfo> instance = new ArrayList<>();
			for (Field field : Cursor.class.getFields()) {
				if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
					try {
						instance.add(new CursorInfo(field.getInt(null), field.getName()));
					} catch (ReflectiveOperationException e) {
						DesignerPlugin.log(e.getMessage(), e);
					}
				}
			}
			INSTANCE = Collections.unmodifiableList(instance);
		}
	}

	/**
	 * Returns an unmodifiable list of all default cursors defined by the Java
	 * runtime.
	 */
	public static List<CursorInfo> getCursors() {
		return InstanceHolder.INSTANCE;
	}
}
