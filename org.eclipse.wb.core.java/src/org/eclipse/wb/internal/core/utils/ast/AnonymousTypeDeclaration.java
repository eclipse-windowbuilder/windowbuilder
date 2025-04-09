/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.core.utils.ast;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Support for using {@link AnonymousClassDeclaration} as {@link TypeDeclaration}.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 * @deprecated Don't use a custom JDT type and work on the {@link AnonymousClassDeclaration directly.
 */
@Deprecated
public class AnonymousTypeDeclaration {
	private static final String KEY = "AnonymousTypeDeclaration";
	private static Class<?> m_class;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private AnonymousTypeDeclaration() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public static boolean is(TypeDeclaration node) {
		return node != null && node.getClass().getName().endsWith("AnonymousTypeDeclaration2");
	}

	public static TypeDeclaration get(ASTNode node) {
		return (TypeDeclaration) node.getProperty(KEY);
	}

	public static TypeDeclaration create(AnonymousClassDeclaration acd) {
		ensureClass();
		try {
			Constructor<?> constructor = m_class.getConstructor(AnonymousClassDeclaration.class);
			return (TypeDeclaration) constructor.newInstance(acd);
		} catch (Throwable e) {
			throw ReflectionUtils.propagate(e);
		}
	}

	private static void ensureClass() {
		try {
			if (m_class == null) {
				Method defineMethod =
						ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{
								String.class,
								byte[].class,
								int.class,
								int.class});
				defineMethod.setAccessible(true);
				InputStream stream =
						AnonymousTypeDeclaration.class.getResourceAsStream("AnonymousTypeDeclaration2.clazz");
				byte[] bytes = IOUtils2.readBytes(stream);
				m_class =
						(Class<?>) defineMethod.invoke(TypeDeclaration.class.getClassLoader(), new Object[]{
								"org.eclipse.jdt.core.dom.AnonymousTypeDeclaration2",
								bytes,
								0,
								bytes.length});
			}
		} catch (Throwable e) {
			throw ReflectionUtils.propagate(e);
		}
	}
}
