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
package org.eclipse.wb.internal.swt.model.jface.resource;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation {@link JavaInfo} for {@link org.eclipse.jface.resource.ResourceRegistry}.
 *
 * @author lobas_av
 * @coverage swt.model.jface
 */
public class ResourceRegistryInfo extends JavaInfo {
	private final List<KeyFieldInfo> m_fields;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ResourceRegistryInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// Color/FontRegistry need initialized display
		{
			Class<?> displayClass =
					EditorState.get(editor).getEditorLoader().loadClass("org.eclipse.swt.widgets.Display");
			ReflectionUtils.invokeMethod(displayClass, "getDefault()");
		}
		// prepare key's information
		m_fields = getKeyFields(getDescription().getComponentClass());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public final List<KeyFieldInfo> getKeyFields() {
		return m_fields;
	}

	/**
	 * Extract all <code>public static</code> field's with type {@link String} for given {@link Class}
	 * .
	 */
	private static List<KeyFieldInfo> getKeyFields(Class<?> registryClass) throws Exception {
		List<KeyFieldInfo> fields = Lists.newArrayList();
		// extract all fields
		for (Field field : registryClass.getFields()) {
			if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
				Class<?> declaringClass = field.getDeclaringClass();
				String keyName = field.getName();
				String keyValue = (String) field.get(null);
				fields.add(new KeyFieldInfo(declaringClass, keyName, keyValue));
			}
		}
		// sort all fields
		Collections.sort(fields, new Comparator<KeyFieldInfo>() {
			@Override
			public int compare(KeyFieldInfo info1, KeyFieldInfo info2) {
				return info1.keyName.compareTo(info2.keyName);
			}
		});
		//
		return fields;
	}
}