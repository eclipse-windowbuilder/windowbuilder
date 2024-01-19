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
package org.eclipse.wb.internal.swing.databinding.model.generic;

import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.binding.DesignerTypeBinding;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.databinding.parser.DatabindingParser;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang3.ArrayUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * Helper class for provide all generic information.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.generic
 */
public class GenericUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Generic: AST
	//
	////////////////////////////////////////////////////////////////////////////
	public static IGenericType[] getReturnTypeArguments(AstEditor editor,
			MethodInvocation invocation,
			int count) throws Exception {
		ITypeBinding[] typeArguments = AstNodeUtils.getMethodBinding(invocation).getTypeArguments();
		return getObjectTypes(editor, typeArguments, count);
	}

	public static IGenericType[] getObjectTypes(AstEditor editor, ITypeBinding[] bindings, int count)
			throws Exception {
		IGenericType[] types = new IGenericType[count];
		if (bindings.length == types.length) {
			ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
			for (int i = 0; i < types.length; i++) {
				types[i] = getObjectType(classLoader, bindings[i]);
			}
		} else {
			Arrays.fill(types, ClassGenericType.OBJECT_CLASS);
		}
		return types;
	}

	public static IGenericType getCreationType(AstEditor editor, ClassInstanceCreation creation)
			throws Exception {
		ITypeBinding binding = AstNodeUtils.getTypeBinding(creation);
		return getObjectType(editor, binding);
	}

	public static IGenericType getObjectType(AstEditor editor, ITypeBinding binding) throws Exception {
		ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
		return getObjectType(classLoader, binding);
	}

	private static IGenericType getObjectType(ClassLoader classLoader, ITypeBinding binding)
			throws Exception {
		Class<?> rawType =
				ReflectionUtils.getClassByName(
						classLoader,
						AstNodeUtils.getFullyQualifiedName(binding, true));
		String typeName = null;
		if (binding instanceof DesignerTypeBinding) {
			// none
		} else if (binding.isWildcardType() && binding.getBound() == null) {
			return ClassGenericType.WILDCARD;
		} else {
			typeName = resolveTypeName(binding);
			if (binding.isParameterizedType()) {
				GenericTypeContainer genericType = new GenericTypeContainer(rawType);
				for (ITypeBinding subBinding : binding.getTypeArguments()) {
					genericType.getSubTypes().add(getObjectType(classLoader, subBinding));
				}
				return genericType;
			}
			if (binding.isArray() && rawType.isArray() && !rawType.getComponentType().isPrimitive()) {
				GenericTypeContainer genericType =
						new GenericTypeContainer(rawType, binding.getDimensions());
				genericType.getSubTypes().add(getObjectType(classLoader, binding.getElementType()));
				return genericType;
			}
		}
		return new ClassGenericType(rawType, typeName, null);
	}

	private static String resolveTypeName(ITypeBinding binding) throws Exception {
		if (binding.isArray()) {
			StringBuffer fullName = new StringBuffer();
			for (int i = 0; i < binding.getDimensions(); i++) {
				fullName.append("[]");
			}
			fullName.insert(0, resolveTypeName(binding.getElementType()));
			return fullName.toString();
		}
		if (binding.isWildcardType()) {
			ITypeBinding boundType = binding.getBound();
			if (boundType == null) {
				return "?";
			}
			String fullName = binding.isUpperbound() ? "? extends " : "? super ";
			return fullName + resolveTypeName(boundType);
		}
		if (binding.isTypeVariable()) {
			return binding.getName();
		}
		String className = AstNodeUtils.getFullyQualifiedName(binding, false);
		if (binding.isParameterizedType()) {
			StringBuffer fullName = new StringBuffer();
			fullName.append(className);
			fullName.append("<");
			ITypeBinding[] types = binding.getTypeArguments();
			for (int i = 0; i < types.length; i++) {
				if (i > 0) {
					fullName.append(", ");
				}
				fullName.append(resolveTypeName(types[i]));
			}
			fullName.append(">");
			return fullName.toString();
		}
		return convertPrimitiveType(className);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Generic: Java Runtime
	//
	////////////////////////////////////////////////////////////////////////////
	public static IGenericType getObjectType(TypeVariable<?> superTypeParameter,
			Type superTypeParameterClass,
			PropertyDescriptor descriptor) {
		Method readMethod = descriptor.getReadMethod();
		Class<?> rawType = descriptor.getPropertyType();
		if (readMethod == null) {
			return new ClassGenericType(rawType, null, null);
		}
		Type type = readMethod.getGenericReturnType();
		if (type instanceof Class<?> || type instanceof TypeVariable<?>) {
			return new ClassGenericType(rawType, null, null);
		}
		if (type instanceof ParameterizedType) {
			GenericTypeContainer genericType = new GenericTypeContainer(rawType);
			ParameterizedType parameterizedType = (ParameterizedType) type;
			//
			if (superTypeParameter != null
					&& parameterizedType.getActualTypeArguments().length == 1
					&& superTypeParameter == parameterizedType.getActualTypeArguments()[0]) {
				genericType.getSubTypes().add(resolveType(superTypeParameterClass));
				return genericType;
			}
			for (Type subType : parameterizedType.getActualTypeArguments()) {
				genericType.getSubTypes().add(resolveType(subType));
			}
			return genericType;
		}
		if (type instanceof GenericArrayType) {
			int dimension = 0;
			Type elementType = null;
			GenericArrayType arrayType = (GenericArrayType) type;
			while (true) {
				dimension++;
				elementType = arrayType.getGenericComponentType();
				if (elementType instanceof GenericArrayType) {
					arrayType = (GenericArrayType) elementType;
					continue;
				}
				break;
			}
			GenericTypeContainer genericType = new GenericTypeContainer(rawType, dimension);
			genericType.getSubTypes().add(resolveType(elementType));
			return genericType;
		}
		Assert.fail(MessageFormat.format("Undefine type: {0} {1}", readMethod, rawType));
		return null;
	}

	private static IGenericType resolveType(Type type) {
		if (type instanceof Class<?>) {
			return new ClassGenericType((Class<?>) type, null, null);
		}
		if (type instanceof WildcardType wildcardType) {
			if (ArrayUtils.isEmpty(wildcardType.getUpperBounds())
					&& ArrayUtils.isEmpty(wildcardType.getLowerBounds())) {
				return ClassGenericType.WILDCARD;
			}
		}
		if (type instanceof ParameterizedType
				|| type instanceof GenericArrayType
				|| type instanceof WildcardType) {
			return new ClassGenericType(null, resolveTypeName(type), "???");
		}
		if (type instanceof TypeVariable<?>) {
			return ClassGenericType.WILDCARD;
		}
		Assert.fail(MessageFormat.format("Undefine type: {0}", type));
		return null;
	}

	private static String resolveTypeName(Type type) {
		if (type instanceof Class<?> rawType) {
			return convertPrimitiveType(ReflectionUtils.getFullyQualifiedName(rawType, false));
		}
		if (type instanceof ParameterizedType parameterizedType) {
			Class<?> rawType = (Class<?>) parameterizedType.getRawType();
			StringBuffer fullName = new StringBuffer();
			fullName.append(CoreUtils.getClassName(rawType));
			fullName.append("<");
			Type[] types = parameterizedType.getActualTypeArguments();
			for (int i = 0; i < types.length; i++) {
				if (i > 0) {
					fullName.append(", ");
				}
				fullName.append(resolveTypeName(types[i]));
			}
			fullName.append(">");
			return fullName.toString();
		}
		if (type instanceof GenericArrayType) {
			StringBuffer fullName = new StringBuffer();
			Type elementType = null;
			GenericArrayType arrayType = (GenericArrayType) type;
			while (true) {
				fullName.append("[]");
				elementType = arrayType.getGenericComponentType();
				if (elementType instanceof GenericArrayType) {
					arrayType = (GenericArrayType) elementType;
					continue;
				}
				break;
			}
			fullName.insert(0, resolveTypeName(elementType));
			return fullName.toString();
		}
		if (type instanceof WildcardType wildcardType) {
			Type[] upperBounds = wildcardType.getUpperBounds();
			Type[] lowerBounds = wildcardType.getLowerBounds();
			if (!ArrayUtils.isEmpty(upperBounds)) {
				Type upperBound = upperBounds[0];
				boolean isWildcard =
						upperBound instanceof Class<?>
				&& ((Class<?>) upperBound).getName().equals("java.lang.Object");
				if (!isWildcard) {
					return "? extends " + resolveTypeName(upperBound);
				}
			} else if (!ArrayUtils.isEmpty(lowerBounds)) {
				return "? super " + resolveTypeName(lowerBounds[0]);
			}
			return "?";
		}
		if (type instanceof TypeVariable<?>) {
			return "?";
		}
		Assert.fail("Undefine type: " + type);
		return null;
	}

	public static String convertPrimitiveType(String className) {
		if ("boolean".equals(className)) {
			return "java.lang.Boolean";
		} else if ("char".equals(className)) {
			return "java.lang.Character";
		} else if ("byte".equals(className)) {
			return "java.lang.Byte";
		} else if ("short".equals(className)) {
			return "java.lang.Short";
		} else if ("int".equals(className)) {
			return "java.lang.Integer";
		} else if ("long".equals(className)) {
			return "java.lang.Long";
		} else if ("float".equals(className)) {
			return "java.lang.Float";
		} else if ("double".equals(className)) {
			return "java.lang.Double";
		}
		return className;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// String
	//
	////////////////////////////////////////////////////////////////////////////
	public static String getTypesSource(IGenericType... types) {
		StringBuffer line = new StringBuffer();
		line.append("<");
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				line.append(", ");
			}
			line.append(types[i].getFullTypeName());
		}
		line.append(">");
		return line.toString();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Assert
	//
	////////////////////////////////////////////////////////////////////////////
	public static void assertEquals(IGenericType expected, IGenericType actual) {
		if (DatabindingParser.useGenerics
				&& !expected.getFullTypeName().equals(actual.getFullTypeName())) {
			Class<?> actualClass = actual.getRawType();
			Class<?> expectedClass = expected.getRawType();
			if (actualClass != null
					&& expectedClass != null
					&& actualClass.isAssignableFrom(expectedClass)) {
				return;
			}
			//
			Assert.fail(MessageFormat.format(
					"Generic: {0} expected, but {1} found",
					expected.getFullTypeName(),
					actual.getFullTypeName()));
		}
	}
}