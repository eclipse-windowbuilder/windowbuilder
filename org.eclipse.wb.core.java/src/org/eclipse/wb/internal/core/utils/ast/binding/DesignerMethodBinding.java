/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.osgi.util.NLS;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Implementation of {@link IMethodBinding}.
 *
 * We use our implementations of bindings because standard ones reference objects from internal
 * compiler's AST. This is not problem for Eclipse itself, but we parse very often, for every change
 * in editor, so we can end up with a lot of referenced objects.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public final class DesignerMethodBinding implements IMethodBinding {
	private final String m_name;
	private final int m_modifiers;
	private final boolean m_constructor;
	private final boolean m_compactConstructor;
	private final boolean m_canonicalConstructor;
	private final boolean m_syntheticRecordMethod;
	private final boolean m_varargs;
	private final ITypeBinding m_declaringClass;
	private final ITypeBinding m_returnType;
	private final String m_key;
	private ITypeBinding[] m_parameterTypes;
	private String[] m_parameterNames;
	private ITypeBinding[] m_exceptionTypes;
	private DesignerMethodBinding m_methodDeclaration;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	DesignerMethodBinding(BindingContext context, IMethodBinding binding) {
		m_key = binding.getKey();
		m_name = binding.getName();
		m_modifiers = binding.getModifiers();
		m_constructor = binding.isConstructor();
		m_compactConstructor = binding.isCompactConstructor();
		m_canonicalConstructor = binding.isCanonicalConstructor();
		m_syntheticRecordMethod = binding.isSyntheticRecordMethod();
		m_varargs = binding.isVarargs();
		m_declaringClass = context.get(binding.getDeclaringClass());
		m_returnType = context.get(binding.getReturnType());
		m_parameterNames = binding.getParameterNames();
		{
			ITypeBinding[] parameterTypes = binding.getParameterTypes();
			m_parameterTypes = new ITypeBinding[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				ITypeBinding parameterType = parameterTypes[i];
				m_parameterTypes[i] = context.get(parameterType);
			}
		}
		{
			ITypeBinding[] exceptionTypes = binding.getExceptionTypes();
			m_exceptionTypes = new ITypeBinding[exceptionTypes.length];
			for (int i = 0; i < exceptionTypes.length; i++) {
				ITypeBinding exceptionType = exceptionTypes[i];
				m_exceptionTypes[i] = context.get(exceptionType);
			}
		}
		{
			IMethodBinding methodDeclaration = binding.getMethodDeclaration();
			if (methodDeclaration == binding) {
				m_methodDeclaration = this;
			} else {
				m_methodDeclaration = context.get(methodDeclaration);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes parameter type with given index.
	 */
	public void removeParameterType(int index) {
		m_parameterTypes = ArrayUtils.remove(m_parameterTypes, index);
		// When using a JDK 8, JDT is unable to calculate the parameter names, leading
		// to a mismatch with the number of parameter types
		if (index < m_parameterNames.length) {
			m_parameterNames = ArrayUtils.remove(m_parameterNames, index);
		} else {
			String message = NLS.bind(Messages.DesignerMethodBinding_unknownArgumentNames, m_key);
			DesignerPlugin.log(Status.warning(message));
		}
		if (m_methodDeclaration != this) {
			m_methodDeclaration.removeParameterType(index);
		}
	}

	/**
	 * Adds new {@link ITypeBinding} into throws exceptions.
	 */
	public void addExceptionType(ITypeBinding newException) {
		m_exceptionTypes = ArrayUtils.add(m_exceptionTypes, newException);
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
	// IMethodBinding
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ITypeBinding getDeclaringClass() {
		return m_declaringClass;
	}

	@Override
	public Object getDefaultValue() {
		throw new IllegalArgumentException();
	}

	@Override
	public ITypeBinding[] getExceptionTypes() {
		return m_exceptionTypes;
	}

	@Override
	public IMethodBinding getMethodDeclaration() {
		return m_methodDeclaration;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public IAnnotationBinding[] getParameterAnnotations(int paramIndex) {
		throw new IllegalArgumentException();
	}

	@Override
	public ITypeBinding[] getParameterTypes() {
		return m_parameterTypes;
	}

	@Override
	public String[] getParameterNames() {
		return m_parameterNames;
	}

	@Override
	public ITypeBinding getReturnType() {
		return m_returnType;
	}

	@Override
	public ITypeBinding getDeclaredReceiverType() {
		throw new IllegalArgumentException();
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		throw new IllegalArgumentException();
	}

	@Override
	public ITypeBinding[] getTypeParameters() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isAnnotationMember() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isConstructor() {
		return m_constructor;
	}

	@Override
	public boolean isDefaultConstructor() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isGenericMethod() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isParameterizedMethod() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isRawMethod() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isSubsignature(IMethodBinding otherMethod) {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isVarargs() {
		return m_varargs;
	}

	@Override
	public boolean overrides(IMethodBinding method) {
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
		return m_key;
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
	public IBinding getDeclaringMember() {
		throw new IllegalArgumentException();
	}

	@Override
	public IVariableBinding[] getSyntheticOuterLocals() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isCompactConstructor() {
		return m_compactConstructor;
	}

	@Override
	public boolean isCanonicalConstructor() {
		return m_canonicalConstructor;
	}

	@Override
	public boolean isSyntheticRecordMethod() {
		return m_syntheticRecordMethod;
	}
}
