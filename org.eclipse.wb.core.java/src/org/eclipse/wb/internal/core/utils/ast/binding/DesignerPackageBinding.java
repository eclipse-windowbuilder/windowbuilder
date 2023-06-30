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
package org.eclipse.wb.internal.core.utils.ast.binding;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;

/**
 * Implementation of {@link IPackageBinding}.
 *
 * We use our implementations of bindings because standard ones reference objects from internal
 * compiler's AST. This is not problem for Eclipse itself, but we parse very often, for every change
 * in editor, so we can end up with a lot of referenced objects.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
final class DesignerPackageBinding implements IPackageBinding {
	private final String m_name;
	private final boolean m_unnamed;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	DesignerPackageBinding(IPackageBinding binding) {
		m_name = binding.getName();
		m_unnamed = binding.isUnnamed();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		throw new IllegalArgumentException();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPackageBinding methods
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public boolean isUnnamed() {
		return m_unnamed;
	}

	@Override
	public String[] getNameComponents() {
		throw new IllegalArgumentException();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IBinding methods
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getKind() {
		throw new IllegalArgumentException();
	}

	@Override
	public int getModifiers() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isDeprecated() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isSynthetic() {
		throw new IllegalArgumentException();
	}

	@Override
	public IJavaElement getJavaElement() {
		throw new IllegalArgumentException();
	}

	@Override
	public String getKey() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isRecovered() {
		throw new IllegalArgumentException();
	}

	@Override
	public org.eclipse.jdt.core.dom.IAnnotationBinding[] getAnnotations() {
		throw new IllegalArgumentException();
	}
}
