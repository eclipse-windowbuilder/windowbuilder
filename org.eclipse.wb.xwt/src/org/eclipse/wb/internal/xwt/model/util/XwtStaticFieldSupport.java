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
package org.eclipse.wb.internal.xwt.model.util;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.editor.StaticFieldPropertyEditorGetExpression;

import org.eclipse.swt.SWT;

/**
 * Implementation of {@link StaticFieldPropertyEditorGetExpression} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.model
 */
public final class XwtStaticFieldSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XwtStaticFieldSupport(XmlObjectInfo rootObject) {
		rootObject.addBroadcastListener(new StaticFieldPropertyEditorGetExpression() {
			@Override
			public void invoke(Class<?> clazz, String field, String[] expression) throws Exception {
				if (clazz == SWT.class) {
					expression[0] = field;
				} else {
					expression[0] = "(" + ReflectionUtils.getCanonicalName(clazz) + ")." + field;
				}
			}
		});
	}
}
