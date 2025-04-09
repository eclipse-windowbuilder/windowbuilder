/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.utils.ast.binding;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Implementation of {@link IVariableBinding}.
 *
 * We use our implementations of bindings because standard ones reference objects from internal
 * compiler's AST. This is not problem for Eclipse itself, but we parse very often, for every change
 * in editor, so we can end up with a lot of referenced objects.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
final class DesignerVariableBinding implements IVariableBinding {
	private final String m_name;
	private final ITypeBinding m_declaringClass;
	private final ITypeBinding m_type;
	private final boolean m_field;
	private final int m_modifiers;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	DesignerVariableBinding(BindingContext context, IVariableBinding binding) {
		m_name = binding.getName();
		m_type = context.get(binding.getType(), true);
		m_declaringClass = context.get(binding.getDeclaringClass());
		m_field = binding.isField();
		m_modifiers = binding.getModifiers();
	}

	DesignerVariableBinding(BindingContext context,
			String name,
			ITypeBinding declaringClass,
			ITypeBinding type,
			boolean field,
			int modifiers) {
		m_name = name;
		m_declaringClass = context.get(declaringClass);
		m_type = context.get(type, true);
		m_field = field;
		m_modifiers = modifiers;
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
	// IVariableBinding
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getConstantValue() {
		throw new IllegalArgumentException();
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		return m_declaringClass;
	}

	@Override
	public IMethodBinding getDeclaringMethod() {
		throw new IllegalArgumentException();
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public ITypeBinding getType() {
		return m_type;
	}

	@Override
	public IVariableBinding getVariableDeclaration() {
		throw new IllegalArgumentException();
	}

	@Override
	public int getVariableId() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isEnumConstant() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isField() {
		return m_field;
	}

	@Override
	public boolean isParameter() {
		throw new IllegalArgumentException();
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
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
	public int getKind() {
		throw new IllegalArgumentException();
	}

	@Override
	public int getModifiers() {
		return m_modifiers;
	}

	@Override
	public boolean isDeprecated() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isSynthetic() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isRecovered() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isEffectivelyFinal() {
		throw new IllegalArgumentException();
	}
}
