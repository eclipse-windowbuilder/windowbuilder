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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Implementation of {@link ITypeBinding}.
 * <p>
 * We use our implementations of bindings because standard ones reference objects from internal
 * compiler's AST. This is not problem for Eclipse itself, but we parse very often, for every change
 * in editor, so we can end up with a lot of referenced objects.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public final class DesignerTypeBinding implements ITypeBinding {
	private final IPackageBinding m_packageBinding;
	private final String m_name;
	private final String m_key;
	// flags
	private final boolean m_class;
	private final boolean m_primitive;
	private final boolean m_nullType;
	private final boolean m_interface;
	private final boolean m_enum;
	private final boolean m_topLevel;
	private final boolean m_member;
	private final boolean m_nested;
	private final boolean m_local;
	private final boolean m_anonymous;
	private final boolean m_record;
	private final boolean m_intersectionType;
	// generics
	private final boolean m_genericType;
	private final boolean m_parameterizedType;
	private final boolean m_typeVariable;
	private ITypeBinding m_typeDeclaration;
	private ITypeBinding[] m_typeArguments;
	private ITypeBinding[] m_typeParameters;
	private ITypeBinding[] m_typeBounds;
	// modifiers
	private final int m_modifiers;
	private final int m_declaredModifiers;
	// array
	private final boolean m_array;
	private final ITypeBinding m_elementType;
	private final int m_dimensions;
	// elements
	private final ITypeBinding m_declaringClass;
	private final ITypeBinding m_superclass;
	private ITypeBinding[] m_interfaces;
	private final IMethodBinding[] m_declaredMethods;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	DesignerTypeBinding(BindingContext context, String fullyQualifiedName, ITypeBinding binding) {
		if (fullyQualifiedName != null) {
			context.register(fullyQualifiedName, this);
		}
		{
			IPackageBinding jdtPackageBinding = binding.getPackage();
			if (jdtPackageBinding != null) {
				m_packageBinding = new DesignerPackageBinding(jdtPackageBinding);
			} else {
				m_packageBinding = null;
			}
		}
		m_name = binding.getName();
		m_key = binding.getKey();
		// flags
		m_class = binding.isClass();
		m_primitive = binding.isPrimitive();
		m_nullType = binding.isNullType();
		m_interface = binding.isInterface();
		m_enum = binding.isEnum();
		m_topLevel = binding.isTopLevel();
		m_nested = binding.isNested();
		m_member = binding.isMember();
		m_local = binding.isLocal();
		m_anonymous = binding.isAnonymous();
		m_record = binding.isRecord();
		m_intersectionType = binding.isIntersectionType();
		// generics
		m_genericType = binding.isGenericType();
		m_parameterizedType = binding.isParameterizedType();
		m_typeVariable = binding.isTypeVariable();
		{
			ITypeBinding typeDeclaration = binding.getTypeDeclaration();
			if (typeDeclaration == binding) {
				m_typeDeclaration = this;
			} else {
				m_typeDeclaration = context.get(typeDeclaration);
			}
		}
		{
			ITypeBinding[] typeArguments = binding.getTypeArguments();
			m_typeArguments = new ITypeBinding[typeArguments.length];
			for (int i = 0; i < typeArguments.length; i++) {
				ITypeBinding typeArgument = typeArguments[i];
				m_typeArguments[i] = context.get(typeArgument, true);
			}
		}
		{
			ITypeBinding[] typeBounds = binding.getTypeBounds();
			m_typeBounds = new ITypeBinding[typeBounds.length];
			for (int i = 0; i < typeBounds.length; i++) {
				ITypeBinding typeBound = typeBounds[i];
				m_typeBounds[i] = context.get(typeBound);
			}
		}
		{
			ITypeBinding[] typeParameters = binding.getTypeParameters();
			m_typeParameters = new ITypeBinding[typeParameters.length];
			for (int i = 0; i < typeParameters.length; i++) {
				ITypeBinding typeParameter = typeParameters[i];
				m_typeParameters[i] = context.get(typeParameter);
			}
		}
		// modifiers
		m_modifiers = binding.getModifiers();
		m_declaredModifiers = binding.getDeclaredModifiers();
		// array
		m_array = binding.isArray();
		if (binding.getElementType() != null) {
			m_elementType = context.get(binding.getElementType());
		} else {
			m_elementType = null;
		}
		m_dimensions = binding.getDimensions();
		// elements
		m_declaringClass = context.get(binding.getDeclaringClass());
		m_superclass = context.get(binding.getSuperclass(), true);
		{
			ITypeBinding[] jdtInterfaces = binding.getInterfaces();
			m_interfaces = new ITypeBinding[jdtInterfaces.length];
			for (int i = 0; i < jdtInterfaces.length; i++) {
				ITypeBinding jdtInterface = jdtInterfaces[i];
				m_interfaces[i] = context.get(jdtInterface);
			}
		}
		{
			IMethodBinding[] methods = binding.getDeclaredMethods();
			m_declaredMethods = new IMethodBinding[methods.length];
			for (int i = 0; i < methods.length; i++) {
				IMethodBinding method = methods[i];
				m_declaredMethods[i] = context.get(method);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds new {@link ITypeBinding} into implemented interfaces.
	 */
	public void addInterface(ITypeBinding typeBinding) {
		ITypeBinding[] newInterfaces = new ITypeBinding[m_interfaces.length + 1];
		System.arraycopy(m_interfaces, 0, newInterfaces, 0, m_interfaces.length);
		newInterfaces[newInterfaces.length - 1] = typeBinding;
		m_interfaces = newInterfaces;
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
	// ITypeBinding
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getBinaryName() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isPrimitive() {
		return m_primitive;
	}

	@Override
	public boolean isNullType() {
		return m_nullType;
	}

	@Override
	public boolean isArray() {
		return m_array;
	}

	@Override
	public ITypeBinding getElementType() {
		return m_elementType;
	}

	@Override
	public int getDimensions() {
		return m_dimensions;
	}

	@Override
	public boolean isClass() {
		return m_class;
	}

	@Override
	public boolean isInterface() {
		return m_interface;
	}

	@Override
	public boolean isEnum() {
		return m_enum;
	}

	@Override
	public boolean isAnnotation() {
		throw new IllegalArgumentException();
	}

	@Override
	public ITypeBinding[] getTypeParameters() {
		return m_typeParameters;
	}

	@Override
	public boolean isTypeVariable() {
		return m_typeVariable;
	}

	@Override
	public ITypeBinding[] getTypeBounds() {
		return m_typeBounds;
	}

	@Override
	public boolean isParameterizedType() {
		return m_parameterizedType;
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		return m_typeArguments;
	}

	@Override
	public ITypeBinding getErasure() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isRawType() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isWildcardType() {
		throw new IllegalArgumentException();
	}

	@Override
	public ITypeBinding getBound() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isUpperbound() {
		throw new IllegalArgumentException();
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public IPackageBinding getPackage() {
		return m_packageBinding;
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		return m_declaringClass;
	}

	@Override
	public ITypeBinding getSuperclass() {
		return m_superclass;
	}

	@Override
	public ITypeBinding[] getInterfaces() {
		return m_interfaces;
	}

	@Override
	public int getModifiers() {
		return m_modifiers;
	}

	@Override
	@Deprecated
	public int getDeclaredModifiers() {
		return m_declaredModifiers;
	}

	@Override
	public boolean isTopLevel() {
		return m_topLevel;
	}

	@Override
	public boolean isNested() {
		return m_nested;
	}

	@Override
	public boolean isMember() {
		return m_member;
	}

	@Override
	public boolean isLocal() {
		return m_local;
	}

	@Override
	public boolean isAnonymous() {
		return m_anonymous;
	}

	@Override
	public ITypeBinding[] getDeclaredTypes() {
		throw new IllegalArgumentException();
	}

	@Override
	public IVariableBinding[] getDeclaredFields() {
		throw new IllegalArgumentException();
	}

	@Override
	public IMethodBinding[] getDeclaredMethods() {
		return m_declaredMethods;
	}

	@Override
	public boolean isFromSource() {
		throw new IllegalArgumentException();
	}

	@Override
	public String getQualifiedName() {
		throw new IllegalArgumentException();
	}

	@Override
	public int getKind() {
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
		return m_key;
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		throw new IllegalArgumentException();
	}

	//
	// For Eclipse 2.0 support
	//
	/*public String getQualifiedName(ITypeBinding binding) {
   ITypeBinding declaringType;
   IPackageBinding packageBinding;
   String scope;
   if (binding == null) {
   return "";
   }
   if (binding.isPrimitive()) {
   return binding.getName();
   }
   declaringType = binding.getDeclaringClass();
   if (declaringType == null) {
   packageBinding = binding.getPackage();
   if (packageBinding == null) {
   return "";
   }
   scope = packageBinding.getName();
   } else {
   scope = getQualifiedName(declaringType);
   }
   return scope + "." + binding.getName();
   }*/
	//
	// New in Eclipse 3.1
	//
	@Override
	public boolean isAssignmentCompatible(ITypeBinding type) {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isCastCompatible(ITypeBinding type) {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isGenericType() {
		return m_genericType;
	}

	@Override
	public boolean isSubTypeCompatible(ITypeBinding type) {
		throw new IllegalArgumentException();
	}

	@Override
	public ITypeBinding getTypeDeclaration() {
		return m_typeDeclaration;
	}

	@Override
	public IMethodBinding getDeclaringMethod() {
		throw new IllegalArgumentException();
	}

	@Override
	public ITypeBinding getWildcard() {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isCapture() {
		throw new IllegalArgumentException();
	}

	//
	// New in Eclipse 3.2M5
	//
	@Override
	public ITypeBinding getComponentType() {
		throw new IllegalArgumentException();
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		throw new IllegalArgumentException();
	}

	//
	// New in Eclipse 3.3M2
	//
	@Override
	public ITypeBinding createArrayType(int dimension) {
		throw new IllegalArgumentException();
	}

	//
	// New in Eclipse 3.3M6
	//
	@Override
	public boolean isRecovered() {
		throw new IllegalArgumentException();
	}

	//
	// New in Eclipse 3.5M4
	//
	@Override
	public int getRank() {
		throw new IllegalArgumentException();
	}

	@Override
	public ITypeBinding getGenericTypeOfWildcardType() {
		throw new IllegalArgumentException();
	}

	@Override
	public IMethodBinding getFunctionalInterfaceMethod() {
		throw new IllegalArgumentException();
	}

	@Override
	public IAnnotationBinding[] getTypeAnnotations() {
		throw new IllegalArgumentException();
	}

	//
	// New in Eclipse 3.11
	//

	@Override
	public IBinding getDeclaringMember() {
		throw new IllegalArgumentException();
	}

	//
	// New in Eclipse 3.12
	//

	@Override
	public boolean isIntersectionType() {
		return m_intersectionType;
	}

	//
	// New in Eclipse 3.14
	//

	@Override
	public IModuleBinding getModule() {
		throw new IllegalArgumentException();
	}

	//
	// New in Eclipse 3.26
	//

	@Override
	public boolean isRecord() {
		return m_record;
	}
}
