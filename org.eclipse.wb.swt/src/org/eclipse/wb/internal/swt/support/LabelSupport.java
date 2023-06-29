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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.widgets.Label;

import java.lang.reflect.Constructor;

/**
 * Stub class for using SWT {@link org.eclipse.swt.widget.Label}'s in another {@link ClassLoader}.
 *
 * @author scheglov_ke
 * @coverage swt.support
 */
public class LabelSupport extends AbstractSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return new instance of {@link Label}.
	 */
	public static Object newInstance(Object parent) throws Exception {
		Constructor<?> labelConstructor =
				ReflectionUtils.getConstructor(
						getLabelClass(),
						ContainerSupport.getCompositeClass(),
						int.class);
		return labelConstructor.newInstance(parent, SwtSupport.NONE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Invoke method <code>Label.getText()</code> for given label.
	 */
	public static String getText(Object label) throws Exception {
		return (String) ReflectionUtils.invokeMethod(label, "getText()");
	}

	/**
	 * Invoke method <code>Label.getText()</code> for given label.
	 */
	public static void setText(Object label, String text) throws Exception {
		ReflectionUtils.invokeMethod(label, "setText(java.lang.String)", text);
	}

	/**
	 * @return {@link org.eclipse.swt.widgets.Label} {@link Class} loaded from active editor
	 *         {@link ClassLoader}.
	 */
	public static Class<?> getLabelClass() {
		return loadClass("org.eclipse.swt.widgets.Label");
	}
}