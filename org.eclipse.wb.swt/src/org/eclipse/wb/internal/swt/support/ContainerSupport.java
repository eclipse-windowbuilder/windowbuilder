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

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub class for using SWT {@link org.eclipse.swt.widgets.Composite}'s in another
 * {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class ContainerSupport extends AbstractSupport {
	private static final Map<Image, Object> SWT_TO_TOOLKIT_IMAGES = new HashMap<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Style
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given {@link Composite} has {@link SWT#RIGHT_TO_LEFT} style.
	 */
	public static boolean isRTL(Object composite) {
		if (composite == null) {
			return false;
		}
		return ControlSupport.isStyle(composite, SWT.RIGHT_TO_LEFT);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Shell
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link org.eclipse.swt.widgets.Shell} {@link Class} loaded from active editor
	 *         {@link ClassLoader}.
	 */
	public static Class<?> getShellClass() {
		return loadClass("org.eclipse.swt.widgets.Shell");
	}

	/**
	 * @return <code>true</code> if given object is {@link org.eclipse.swt.widgets.Shell}.
	 */
	public static boolean isShell(Object o) {
		return getShellClass().isAssignableFrom(o.getClass());
	}

	/**
	 * @return <code>true</code> if given {@link Class} is {@link org.eclipse.swt.widgets.Shell}.
	 */
	public static boolean isShell(Class<?> clazz) {
		return getShellClass().isAssignableFrom(clazz);
	}

	/**
	 * Create new instance of SWT {@link org.eclipse.swt.widgets.Shell}: <code>new Shell()</code>.
	 */
	public static Object createShell() throws Exception {
		return getShellClass().newInstance();
	}

	/**
	 * Invoke method <code>shell.setText()</code> for given shell.
	 */
	public static void setShellText(Object shell, String text) throws Exception {
		ReflectionUtils.invokeMethod(shell, "setText(java.lang.String)", text);
	}

	/**
	 * Sets the new FillLayout instance to the given composite.
	 */
	public static void setFillLayout(Object composite) throws Exception {
		Class<?> layoutClass = loadClass("org.eclipse.swt.layout.FillLayout");
		Object layout = layoutClass.newInstance();
		ReflectionUtils.invokeMethod(composite, "setLayout(org.eclipse.swt.widgets.Layout)", layout);
	}

	/**
	 * Invoke method <code>shell.setImage()</code> for given shell.
	 */
	public static void setShellImage(Object shell, Image swtImage) throws Exception {
		Object image = SWT_TO_TOOLKIT_IMAGES.get(swtImage);
		if (image == null) {
			image = ToolkitSupport.createToolkitImage(swtImage);
			SWT_TO_TOOLKIT_IMAGES.put(swtImage, image);
		}
		ReflectionUtils.invokeMethod(shell, "setImage(org.eclipse.swt.graphics.Image)", image);
	}

	/**
	 * Show shell and hide it during closing (don't disposed).
	 */
	public static void showShell(Object shell) throws Exception {
		ToolkitSupport.showShell(shell);
	}

	/**
	 * Invokes Shell.close() method using reflection.
	 */
	public static void closeShell(Object shell) {
		ReflectionUtils.invokeMethodEx(shell, "close()");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Composite
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link org.eclipse.swt.widgets.Composite} {@link Class} loaded from active editor
	 *         {@link ClassLoader}.
	 */
	public static Class<?> getCompositeClass() {
		return loadClass("org.eclipse.swt.widgets.Composite");
	}

	/**
	 * @return <code>true</code> if given {@link Class} is successor of
	 *         {@link org.eclipse.swt.widgets.Composite}.
	 */
	public static boolean isCompositeClass(Class<?> clazz) {
		return ReflectionUtils.isSuccessorOf(clazz, "org.eclipse.swt.widgets.Composite");
	}

	/**
	 * @return <code>true</code> if given object is {@link org.eclipse.swt.widgets.Composite}.
	 */
	public static boolean isComposite(Object o) {
		return isCompositeClass(o.getClass());
	}

	/**
	 * Creates new instance of SWT {@link org.eclipse.swt.widgets.Composite}.
	 */
	public static Object createComposite(Object parent, int style) throws Exception {
		Constructor<?> constructor =
				ReflectionUtils.getConstructorBySignature(
						getCompositeClass(),
						"<init>(org.eclipse.swt.widgets.Composite,int)");
		return constructor.newInstance(parent, style);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Invoke method <code>Composite.getChildren()</code> for given composite.
	 */
	public static Object[] getChildren(Object composite) {
		Object[] children = (Object[]) ReflectionUtils.invokeMethodEx(composite, "getChildren()");
		return children != null ? children : ArrayUtils.EMPTY_OBJECT_ARRAY;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layout
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link org.eclipse.swt.widgets.Layout} {@link Class} loaded from active editor
	 *         {@link ClassLoader}.
	 */
	public static Class<?> getLayoutClass() {
		return loadClass("org.eclipse.swt.widgets.Layout");
	}

	/**
	 * Invoke method <code>Composite.getLayout()</code> for given composite.
	 */
	public static Object getLayout(Object composite) throws Exception {
		return ReflectionUtils.invokeMethodEx(composite, "getLayout()");
	}

	/**
	 * Invoke method <code>Composite.setLayout(layout)</code> for given composite.
	 */
	public static void setLayout(Object composite, Object layout) throws Exception {
		ReflectionUtils.invokeMethod(composite, "setLayout(org.eclipse.swt.widgets.Layout)", layout);
	}

	/**
	 * Invoke method <code>Composite.layout()</code> for given composite.
	 */
	public static void layout(Object composite) throws Exception {
		ReflectionUtils.invokeMethod(composite, "layout()");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Client area
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Invoke method <code>Composite.computeTrim()</code> for given composite.
	 */
	public static Rectangle computeTrim(Object composite, int x, int y, int width, int height)
			throws Exception {
		Object rectangle =
				ReflectionUtils.invokeMethod(composite, "computeTrim(int,int,int,int)", x, y, width, height);
		return RectangleSupport.getRectangle(rectangle);
	}

	/**
	 * Invoke method <code>Composite.getClientArea()</code> for given composite.
	 */
	public static Rectangle getClientArea(final Object composite) {
		return ExecutionUtils.runObject(new RunnableObjectEx<Rectangle>() {
			@Override
			public Rectangle runObject() throws Exception {
				Object rectangle = ReflectionUtils.invokeMethod(composite, "getClientArea()");
				return RectangleSupport.getRectangle(rectangle);
			}
		});
	}
}