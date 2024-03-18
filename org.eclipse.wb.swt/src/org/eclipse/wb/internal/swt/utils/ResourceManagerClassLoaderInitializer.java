/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.utils;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.IClassLoaderInitializer;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.SwtInvocationEvaluatorInterceptor;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

/**
 * Implementation of {@link IClassLoaderInitializer} for initializing RCP
 * <code>ResourceManager</code>.
 *
 * @author scheglov_ke
 * @coverage swt.utils
 */
public final class ResourceManagerClassLoaderInitializer implements IClassLoaderInitializer {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IClassLoaderInitializer INSTANCE =
			new ResourceManagerClassLoaderInitializer();

	private ResourceManagerClassLoaderInitializer() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClassLoaderInitializer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void initialize(final ClassLoader classLoader) {
		ExecutionUtils.runIgnore(new RunnableEx() {
			@Override
			public void run() throws Exception {
				Class<?> managerClass = classLoader.loadClass("org.eclipse.wb.swt.ResourceManager");
				Class<?> providerClass =
						classLoader.loadClass("org.eclipse.wb.swt.ResourceManager$PluginResourceProvider");
				initialize_ResourceManager(classLoader, managerClass, providerClass);
			}
		});
	}

	@Override
	public void deinitialize(ClassLoader classLoader) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Initializes already loaded <code>ResourceManager</code> class.
	 */
	private void initialize_ResourceManager(ClassLoader classLoader,
			Class<?> managerClass,
			Class<?> providerClass) throws Exception {
		Object provider = createProvider(classLoader, providerClass);
		ReflectionUtils.setField(managerClass, "m_designTimePluginResourceProvider", provider);
	}

	/**
	 * @return the implementation of <code>PluginResourceProvider</code>.
	 */
	private Object createProvider(ClassLoader classLoader, Class<?> providerClass) {
		try {
			return new ByteBuddy() //
					.subclass(providerClass) //
					.method(ElementMatchers.named("getEntry")) //
					.intercept(InvocationHandlerAdapter.of((Object proxy, Method method, Object[] args) -> {
						String symbolicName = (String) args[0];
						String fullPath = (String) args[1];
						return SwtInvocationEvaluatorInterceptor.getEntry(symbolicName, fullPath);
					})) //
					.make() //
					.load(classLoader) //
					.getLoaded() //
					.getConstructor() //
					.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new DesignerException(ICoreExceptionConstants.EVAL_BYTEBUDDY, e);
		}
	}
}
