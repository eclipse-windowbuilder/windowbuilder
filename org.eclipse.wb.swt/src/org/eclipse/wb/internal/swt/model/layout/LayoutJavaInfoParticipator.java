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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.internal.core.model.util.surround.SurroundSupport;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link IJavaInfoInitializationParticipator} that performs name-based binding of
 * {@link LayoutInfo} and related artifacts.
 * <p>
 * For example it binds {@link LayoutInfo} with its {@link SurroundSupport}, that should be in same
 * package and have <code>"_SurroundSupport"</code> suffix.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public class LayoutJavaInfoParticipator implements IJavaInfoInitializationParticipator {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new LayoutJavaInfoParticipator();

	private LayoutJavaInfoParticipator() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IJavaInfoInitializationParticipator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(final JavaInfo javaInfo) throws Exception {
		if (javaInfo instanceof final LayoutInfo layout) {
			// perform bindings...
			if (layout.getParent() != null) {
				// ...right now, if Layout already bound to its container
				performBindings(layout);
			} else {
				// ...or later, when Layout will be bound to its container
				layout.addBroadcastListener(new ObjectInfoChildAddAfter() {
					@Override
					public void invoke(ObjectInfo parent, ObjectInfo child) throws Exception {
						if (child == layout) {
							layout.removeBroadcastListener(this);
							performBindings(layout);
						}
					}
				});
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bindings
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Performs artifacts bindings after building components tree.
	 */
	private void performBindings(LayoutInfo layout) throws Exception {
		// bind SurroundSupport
		run(layout, new IBindingProcessor() {
			@Override
			public boolean run(LayoutInfo layout,
					ClassLoader classLoader,
					Class<?> layoutClass,
					String layoutName) throws Exception {
				String surroundClassName = layoutName + "SurroundSupport";
				Class<?> surroundClass = classLoader.loadClass(surroundClassName);
				ReflectionUtils.getConstructor(surroundClass, layoutClass).newInstance(layout);
				return true;
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * The processor to use with {@link LayoutJavaInfoParticipator#run(LayoutInfo, IBindingProcessor)}
	 * .
	 */
	private interface IBindingProcessor {
		boolean run(LayoutInfo layout, ClassLoader classLoader, Class<?> layoutClass, String layoutName)
				throws Exception;
	}

	/**
	 * Uses {@link IBindingProcessor} to attempt to perform binding by class of {@link LayoutInfo} and
	 * any of its super classes. Trick is that sometimes artifact is bound not to the exact class of
	 * {@link LayoutInfo}, but to some of its super classes, so we should check them too.
	 */
	private static void run(final LayoutInfo layout, final IBindingProcessor processor) {
		Class<?> layoutClass = layout.getClass();
		for (; layoutClass != null; layoutClass = layoutClass.getSuperclass()) {
			final Class<?> finalLayoutClass = layoutClass;
			boolean success = ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
				@Override
				public Boolean runObject() throws Exception {
					run(layout, processor, finalLayoutClass);
					return true;
				}
			}, false);
			if (success) {
				return;
			}
		}
	}

	private static void run(LayoutInfo layout, IBindingProcessor processor, Class<?> layoutClass)
			throws Exception {
		// prepare name of Layout without "Info" suffix
		final String layoutName;
		{
			String layoutClassName = layoutClass.getName();
			layoutName = StringUtils.removeEnd(layoutClassName, "Info");
		}
		// try to bind
		ClassLoader classLoader = ReflectionUtils.getClassLoader(layoutClass);
		processor.run(layout, classLoader, layoutClass, layoutName);
	}
}
