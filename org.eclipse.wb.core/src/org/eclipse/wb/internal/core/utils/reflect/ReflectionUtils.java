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
package org.eclipse.wb.internal.core.utils.reflect;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Contains different Java reflection utilities.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public class ReflectionUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ReflectionUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Java 8 uses "MethodRef" that uses {@link SoftReference} to a {@link Method}. But this means
   * that when it looses a reference to a <em>protected</em> {@link Method}, it cannot restore it. I
   * wish we had done it differently, not by using {@link PropertyDescriptor}.
   */
  public static Method getWriteMethod(PropertyDescriptor propertyDescriptor) {
    try {
      return propertyDescriptor.getWriteMethod();
    } catch (Throwable e) {
      return null;
    }
  }

  /**
   * Java 8 uses "MethodRef" that uses {@link SoftReference} to a {@link Method}. But this means
   * that when it looses a reference to a <em>protected</em> {@link Method}, it cannot restore it. I
   * wish we had done it differently, not by using {@link PropertyDescriptor}.
   */
  public static Method getReadMethod(PropertyDescriptor propertyDescriptor) {
    try {
      return propertyDescriptor.getReadMethod();
    } catch (Throwable e) {
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ClassLoader} that was used to load given {@link Class}. Always return not
   *         <code>null</code> value, even if {@link Class} was loaded using bootstrap
   *         {@link ClassLoader}.
   */
  public static ClassLoader getClassLoader(Class<?> clazz) {
    ClassLoader classLoader = clazz.getClassLoader();
    if (classLoader == null) {
      classLoader = ClassLoader.getSystemClassLoader();
    }
    return classLoader;
  }

  /**
   * @return the {@link Class} with given name - primitive or {@link Object} (including arrays).
   */
  public static Class<?> getClassByName(ClassLoader classLoader, String className) throws Exception {
    Assert.isNotNull(className);
    // check for primitive type
    if ("boolean".equals(className)) {
      return boolean.class;
    } else if ("byte".equals(className)) {
      return byte.class;
    } else if ("char".equals(className)) {
      return char.class;
    } else if ("short".equals(className)) {
      return short.class;
    } else if ("int".equals(className)) {
      return int.class;
    } else if ("long".equals(className)) {
      return long.class;
    } else if ("float".equals(className)) {
      return float.class;
    } else if ("double".equals(className)) {
      return double.class;
    }
    // check for array
    if (className.endsWith("[]")) {
      int dimensions = StringUtils.countMatches(className, "[]");
      String componentClassName = StringUtils.substringBefore(className, "[]");
      Class<?> componentClass = getClassByName(classLoader, componentClassName);
      return Array.newInstance(componentClass, new int[dimensions]).getClass();
    }
    // OK, load this class as object
    return classLoader.loadClass(className);
  }

  /**
   * @return <code>true</code> if given {@link ClassLoader} has class with given name.
   */
  public static boolean hasClass(ClassLoader classLoader, String name) {
    try {
      classLoader.loadClass(name);
      return true;
    } catch (Throwable e) {
      return false;
    }
  }

  /**
   * @return the {@link Object} default value for class with given {@code className}.
   */
  public static Object getDefaultValue(String className) {
    // primitive
    if ("boolean".equals(className)) {
      return false;
    } else if ("byte".equals(className)) {
      return (byte) 0;
    } else if ("char".equals(className)) {
      return (char) 0;
    } else if ("short".equals(className)) {
      return (short) 0;
    } else if ("int".equals(className)) {
      return 0;
    } else if ("long".equals(className)) {
      return 0L;
    } else if ("float".equals(className)) {
      return 0.0f;
    } else if ("double".equals(className)) {
      return 0.0;
    }
    // Object
    return null;
  }

  /**
   * @return the {@link Object} default value for class - false/zero for primitives,
   *         <code>"dynamic"</code> for {@link String} and empty collections.
   */
  public static Object getDefaultValue(Class<?> clazz) {
    if (clazz.isPrimitive()) {
      return getDefaultValue(clazz.getName());
    }
    // String
    if (isSuccessorOf(clazz, "java.lang.String")) {
      return "<dynamic>";
    }
    // collections
    if (isSuccessorOf(clazz, "java.util.List")) {
      return Lists.newArrayList();
    }
    if (isSuccessorOf(clazz, "java.util.Set")) {
      return Sets.newHashSet();
    }
    if (isSuccessorOf(clazz, "java.util.Map")) {
      return Maps.newHashMap();
    }
    // Object
    return null;
  }

  /**
   * @param clazz
   *          the {@link Class} to check.
   * @param requiredClass
   *          the name of {@link Class} that should be extended, or name of interface that should be
   *          implemented.
   *
   * @return <code>true</code> if given {@link Class} extends <code>requiredClass</code> or
   *         implements interface with this name.
   */
  public static boolean isSuccessorOf(Class<?> clazz, String requiredClass) {
    if (clazz == null) {
      return false;
    }
    String clazzName = clazz.getName();
    // check cache
    {
      IsSuccessorResult result = isSuccessorOf_checkCache(clazz, requiredClass);
      if (result == IsSuccessorResult.TRUE) {
        return true;
      }
      if (result == IsSuccessorResult.FALSE) {
        return false;
      }
    }
    // check this Class
    if (clazzName.equals(requiredClass)) {
      isSuccessorOf_addCache(clazz, requiredClass, IsSuccessorResult.TRUE);
      return true;
    }
    // check super-Class
    if (isSuccessorOf(clazz.getSuperclass(), requiredClass)) {
      isSuccessorOf_addCache(clazz, requiredClass, IsSuccessorResult.TRUE);
      return true;
    }
    // check interfaces
    for (Class<?> interfaceClass : clazz.getInterfaces()) {
      if (isSuccessorOf(interfaceClass, requiredClass)) {
        isSuccessorOf_addCache(clazz, requiredClass, IsSuccessorResult.TRUE);
        return true;
      }
    }
    // no, not required Class
    isSuccessorOf_addCache(clazz, requiredClass, IsSuccessorResult.FALSE);
    return false;
  }

  private enum IsSuccessorResult {
    UNKNOWN, FALSE, TRUE
  }

  private static final Map<String, WeakHashMap<Class<?>, IsSuccessorResult>> m_isSuccessorOfCache =
      Maps.newHashMap();

  private static void isSuccessorOf_addCache(Class<?> clazz,
      String requiredClass,
      IsSuccessorResult result) {
    WeakHashMap<Class<?>, IsSuccessorResult> classes = m_isSuccessorOfCache.get(requiredClass);
    if (classes == null) {
      classes = new WeakHashMap<Class<?>, IsSuccessorResult>();
      m_isSuccessorOfCache.put(requiredClass, classes);
    }
    classes.put(clazz, result);
  }

  private static IsSuccessorResult isSuccessorOf_checkCache(Class<?> clazz, String requiredClass) {
    WeakHashMap<Class<?>, IsSuccessorResult> classes = m_isSuccessorOfCache.get(requiredClass);
    if (classes != null) {
      return classes.get(clazz);
    }
    return IsSuccessorResult.UNKNOWN;
  }

  /**
   * @return <code>true</code> if candidate can be used as target {@link Class}.
   */
  public static boolean isAssignableFrom(Class<?> targetClass, Object candidate) {
    // primitive
    if (targetClass.isPrimitive()) {
      if (targetClass == byte.class) {
        return candidate instanceof Byte;
      }
      if (targetClass == char.class) {
        return candidate instanceof Character;
      }
      if (targetClass == short.class) {
        return candidate instanceof Short;
      }
      if (targetClass == int.class) {
        return candidate instanceof Integer;
      }
      if (targetClass == long.class) {
        return candidate instanceof Long;
      }
      if (targetClass == float.class) {
        return candidate instanceof Float;
      }
      if (targetClass == double.class) {
        return candidate instanceof Double;
      }
    }
    // object
    if (candidate == null) {
      return true;
    }
    return targetClass.isInstance(candidate);
  }

  /**
   * @return <code>true</code> if candidate can be used as target {@link Class}.
   */
  public static boolean isSuccessorOf(Object candidate, String requiredClass) {
    if (candidate == null) {
      return false;
    }
    Class<?> candidateClass = candidate.getClass();
    if (!requiredClass.contains(".")) {
      Field fieldTYPE = getFieldByName(candidateClass, "TYPE");
      if (fieldTYPE != null) {
        Class<?> primitiveType = (Class<?>) getFieldObject(candidateClass, "TYPE");
        if (ObjectUtils.equals(primitiveType.getName(), requiredClass)) {
          return true;
        }
      }
      return false;
    } else {
      return isSuccessorOf(candidateClass, requiredClass);
    }
  }

  /**
   * @return the result of {@link Class#isMemberClass()} and ignore {@link NoClassDefFoundError}.
   */
  public static boolean isMemberClass(final Class<?> clazz) {
    Assert.isNotNull(clazz);
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        return clazz.isMemberClass();
      }
    }, false);
  }

  /**
   * @return the list of all supertypes: superclasses and interfaces. Class itself and its
   *         interfaces first, then superclass.
   */
  public static List<Class<?>> getSuperHierarchy(Class<?> clazz) throws Exception {
    List<Class<?>> types = Lists.newArrayList();
    types.add(clazz);
    // check super Class
    Class<?> superclass = clazz.getSuperclass();
    if (superclass != null) {
      // interfaces
      for (Class<?> interfaceClass : clazz.getInterfaces()) {
        types.addAll(getSuperHierarchy(interfaceClass));
      }
      // super Class
      types.addAll(getSuperHierarchy(superclass));
    }
    // done
    return types;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Specific
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if two arrays have same number of classes and all "specific" classes
   *         are {@link #isMoreSpecific(Class, Class)} than "base".
   */
  public static boolean isMoreSpecific(Class<?> base[], Class<?> specific[]) {
    if (base.length != specific.length) {
      return false;
    }
    for (int i = 0; i < base.length; i++) {
      if (!isMoreSpecific(base[i], specific[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return <code>true</code> if "specific" is "base" or subclass of "base".
   */
  public static boolean isMoreSpecific(Class<?> base, Class<?> specific) {
    return base.isAssignableFrom(specific);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enchanced classes support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link Class} that is "enchanced", i.e. is generated by
   *         "CGLib", or some other library.
   */
  public static boolean isEnchancedClass(Class<?> clazz) {
    return clazz.getName().indexOf('$') != -1;
  }

  /**
   * @return the super-Class of given {@link Class} that is "normal", i.e. is not generated by
   *         "CGLib", or some other library. For now we just check that {@link Class} name has no
   *         <code>'$'</code> character.
   */
  public static Class<?> getNormalClass(Class<?> clazz) {
    while (isEnchancedClass(clazz)) {
      clazz = clazz.getSuperclass();
    }
    return clazz;
  }

  /**
   * @return the {@link String} presentation of given {@link Method}. If this {@link Method} is
   *         declared in some "enchanced" {@link Class}, and there was same {@link Method} in
   *         "normal" {@link Class}, then <code>toString</code> of "normal" {@link Method} returned.
   */
  public static String toString(Method method) {
    String methodSignature = getMethodSignature(method);
    // try to find in "normal" Class method with same signature
    while (isEnchancedClass(method.getDeclaringClass())) {
      Class<?> superClass = method.getDeclaringClass().getSuperclass();
      Method superMethod = getMethodBySignature(superClass, methodSignature);
      if (superMethod == null) {
        break;
      }
      method = superMethod;
    }
    // OK, use toString()
    return method.toString();
  }

  /**
   * @return the string presentation {@link Constructor} that uses short class names.
   */
  public static String getShortConstructorString(Constructor<?> constructor) {
    if (constructor == null) {
      return "<null-constructor>";
    }
    StringBuilder buffer = new StringBuilder();
    buffer.append(getShortName(constructor.getDeclaringClass()));
    buffer.append('(');
    // append parameters
    {
      Class<?>[] parameters = constructor.getParameterTypes();
      for (int i = 0; i < parameters.length; i++) {
        Class<?> parameterType = parameters[i];
        if (i != 0) {
          buffer.append(',');
        }
        buffer.append(getShortName(parameterType));
      }
    }
    // close
    buffer.append(')');
    return buffer.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Signature
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param runtime
   *          is <code>true</code> if we need name for class loading, <code>false</code> if we need
   *          name for source generation.
   *
   * @return the fully qualified name of given {@link Type}.
   */
  public static String getFullyQualifiedName(Type type, boolean runtime) {
    Assert.isNotNull(type);
    // Class
    if (type instanceof Class<?>) {
      Class<?> clazz = (Class<?>) type;
      // array
      if (clazz.isArray()) {
        return getFullyQualifiedName(clazz.getComponentType(), runtime) + "[]";
      }
      // object
      String name = clazz.getName();
      if (!runtime) {
        name = name.replace('$', '.');
      }
      return name;
    }
    // GenericArrayType
    if (type instanceof GenericArrayType) {
      GenericArrayType genericArrayType = (GenericArrayType) type;
      return getFullyQualifiedName(genericArrayType.getGenericComponentType(), runtime) + "[]";
    }
    // ParameterizedType
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      // raw type
      StringBuilder sb = new StringBuilder();
      sb.append(getFullyQualifiedName(rawType, runtime));
      // type arguments
      sb.append("<");
      boolean firstTypeArgument = true;
      for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
        if (!firstTypeArgument) {
          sb.append(",");
        }
        firstTypeArgument = false;
        sb.append(getFullyQualifiedName(typeArgument, runtime));
      }
      sb.append(">");
      // done
      return sb.toString();
    }
    // WildcardType
    if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;
      return "? extends " + getFullyQualifiedName(wildcardType.getUpperBounds()[0], runtime);
    }
    // TypeVariable
    TypeVariable<?> typeVariable = (TypeVariable<?>) type;
    return typeVariable.getName();
  }

  /**
   * Appends fully qualified names of given parameter types (appends also <code>"()"</code>).
   */
  private static void appendParameterTypes(StringBuilder buffer, Type[] parameterTypes) {
    buffer.append('(');
    boolean firstParameter = true;
    for (Type parameterType : parameterTypes) {
      if (firstParameter) {
        firstParameter = false;
      } else {
        buffer.append(',');
      }
      buffer.append(getFullyQualifiedName(parameterType, false));
    }
    buffer.append(')');
  }

  /**
   * Same as {@link Class#getCanonicalName()}.
   * <p>
   * GWT has problems with canonical name of inner class, so we need such workaround.
   * http://code.google.com/p/google-web-toolkit/issues/detail?id=4346
   */
  public static String getCanonicalName(Class<?> clazz) {
    return getFullyQualifiedName(clazz, false);
  }

  /**
   * Returns the short name of {@link Class}, or same name for simple type name.
   *
   * <pre>
	 * getShortName("javax.swing.JPanel")  = "JPanel"
	 * getShortName("boolean")             = "boolean"
	 * </pre>
   *
   * @return the short name of given {@link Class}.
   */
  public static String getShortName(Class<?> clazz) {
    String className = getFullyQualifiedName(clazz, false);
    // member Class
    if (clazz.isMemberClass()) {
      Class<?> topClass = getTopLevelClass(clazz);
      String topName = topClass.getName();
      String topPackage = StringUtils.substringBeforeLast(topName, ".") + ".";
      return className.substring(topPackage.length());
    }
    // normal top level Class, may be array
    if (className.indexOf('.') != -1) {
      return StringUtils.substringAfterLast(className, ".");
    }
    // primitive or default package
    return className;
  }

  private static Class<?> getTopLevelClass(Class<?> clazz) {
    while (clazz.isMemberClass()) {
      clazz = clazz.getEnclosingClass();
    }
    return clazz;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modifiers
  //
  ////////////////////////////////////////////////////////////////////////////
  public static boolean isPublic(Constructor<?> constructor) {
    return Modifier.isPublic(constructor.getModifiers());
  }

  public static boolean isProtected(Constructor<?> constructor) {
    return Modifier.isProtected(constructor.getModifiers());
  }

  public static boolean isPrivate(Constructor<?> constructor) {
    return Modifier.isPrivate(constructor.getModifiers());
  }

  public static boolean isPackagePrivate(Constructor<?> constructor) {
    return !isPublic(constructor) && !isProtected(constructor) && !isPrivate(constructor);
  }

  public static boolean isPublic(Method method) {
    return Modifier.isPublic(method.getModifiers());
  }

  public static boolean isProtected(Method method) {
    return Modifier.isProtected(method.getModifiers());
  }

  public static boolean isPrivate(Method method) {
    return Modifier.isPrivate(method.getModifiers());
  }

  public static boolean isPackagePrivate(Method method) {
    return !isPublic(method) && !isProtected(method) && !isPrivate(method);
  }

  public static boolean isAbstract(Method method) {
    return Modifier.isAbstract(method.getModifiers());
  }

  public static boolean isPublic(Field field) {
    return Modifier.isPublic(field.getModifiers());
  }

  public static boolean isProtected(Field field) {
    return Modifier.isProtected(field.getModifiers());
  }

  public static boolean isPrivate(Field field) {
    return Modifier.isPrivate(field.getModifiers());
  }

  public static boolean isPackagePrivate(Field field) {
    return !isPublic(field) && !isProtected(field) && !isPrivate(field);
  }

  public static boolean isStatic(Field field) {
    return Modifier.isStatic(field.getModifiers());
  }

  public static boolean isAbstract(Class<?> clazz) {
    return Modifier.isAbstract(clazz.getModifiers());
  }

  public static boolean isPublic(Class<?> clazz) {
    return Modifier.isPublic(clazz.getModifiers());
  }

  public static boolean isProtected(Class<?> clazz) {
    return Modifier.isProtected(clazz.getModifiers());
  }

  public static boolean isPrivate(Class<?> clazz) {
    return Modifier.isPrivate(clazz.getModifiers());
  }

  public static boolean isPackagePrivate(Class<?> clazz) {
    return !isPublic(clazz) && !isProtected(clazz) && !isPrivate(clazz);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Method
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return all declared {@link Method}'s, including protected and private.
   */
  public static Map<String, Method> getMethods(Class<?> clazz) {
    Map<String, Method> methods = Maps.newHashMap();
    // process classes
    for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
      for (Method method : c.getDeclaredMethods()) {
        String signature = getMethodSignature(method);
        if (!methods.containsKey(signature)) {
          method.setAccessible(true);
          methods.put(signature, method);
        }
      }
    }
    // process interfaces
    for (Class<?> interfaceClass : clazz.getInterfaces()) {
      for (Method method : interfaceClass.getDeclaredMethods()) {
        String signature = getMethodSignature(method);
        if (!methods.containsKey(signature)) {
          method.setAccessible(true);
          methods.put(signature, method);
        }
      }
    }
    // done
    return methods;
  }

  /**
   * @return signature for given {@link Method}. This signature is not same signature as in JVM or
   *         JDT, just some string that unique identifies method in its {@link Class}.
   */
  public static String getMethodSignature(Method method) {
    Assert.isNotNull(method);
    return getMethodSignature(method.getName(), method.getParameterTypes());
  }

  /**
   * Returns the signature of {@link Method} with given combination of name and parameter types.
   * This signature is not same signature as in JVM or JDT, just some string that unique identifies
   * method in its {@link Class}.
   *
   * @param name
   *          the name of {@link Method}.
   * @param parameterTypes
   *          the types of {@link Method} parameters.
   *
   * @return signature of {@link Method}.
   */
  public static String getMethodSignature(String name, Type... parameterTypes) {
    Assert.isNotNull(name);
    Assert.isNotNull(parameterTypes);
    //
    StringBuilder buffer = new StringBuilder();
    buffer.append(name);
    appendParameterTypes(buffer, parameterTypes);
    return buffer.toString();
  }

  /**
   * @return generic signature for given {@link Method}. This signature is not same signature as in
   *         JVM or JDT, just some string that unique identifies constructor in its {@link Class}.
   *         It uses {@link Method#getGenericParameterTypes()}, so for each {@link TypeVariable} its
   *         name will be used in signature.
   */
  public static String getMethodGenericSignature(Method method) {
    Assert.isNotNull(method);
    return getMethodSignature(method.getName(), method.getGenericParameterTypes());
  }

  /**
   * Note: you should not use this method, using {@link #getMethodBySignature(Class, String)}.
   *
   * @return the {@link Method} for given name, or <code>null</code> if no such method found.
   */
  public static Method getMethodByName(Class<?> clazz, String name) {
    Assert.isNotNull(clazz);
    Assert.isNotNull(name);
    for (Method method : getMethods(clazz).values()) {
      if (method.getName().equals(name)) {
        return method;
      }
    }
    return null;
  }

  private static final ClassMap<Map<String, Method>> m_getMethodBySignature = ClassMap.create();

  /**
   * Returns the {@link Method} defined in {@link Class}. This method can have any visibility, i.e.
   * we can find even protected/private methods. Can return <code>null</code> if no method with
   * given signature found.
   *
   * @param clazz
   *          the {@link Class} to get method from it, or its superclass.
   * @param signature
   *          the signature of method in same format as {@link #getMethodSignature(Method)}.
   *
   * @return the {@link Method} for given signature, or <code>null</code> if no such method found.
   */
  public static Method getMethodBySignature(Class<?> clazz, String signature) {
    Assert.isNotNull(clazz);
    Assert.isNotNull(signature);
    // prepare cache
    Map<String, Method> cache = m_getMethodBySignature.get(clazz);
    if (cache == null) {
      cache = getMethods(clazz);
      m_getMethodBySignature.put(clazz, cache);
    }
    // use cache
    return cache.get(signature);
  }

  /**
   * @param signature
   *          the generic signature of {@link Method} in same format as
   *          {@link #getMethodGenericSignature(Method)}.
   *
   * @return the {@link Method} for given signature. This constructor can have any visibility, i.e.
   *         we can find even protected/private constructors.
   */
  public static Method getMethodByGenericSignature(Class<?> clazz, String signature) {
    Assert.isNotNull(clazz);
    Assert.isNotNull(signature);
    // check public methods
    for (Method method : clazz.getMethods()) {
      if (getMethodGenericSignature(method).equals(signature)) {
        return method;
      }
    }
    // not found
    return null;
  }

  /**
   * Returns the {@link Method} defined in {@link Class}. This method can have any visibility, i.e.
   * we can find even protected/private methods. Can return <code>null</code> if no method with
   * given combination of name/parameters found.
   *
   * @param clazz
   *          the {@link Class} to get method from it, or its superclass.
   * @param name
   *          the name of method.
   * @param parameterTypes
   *          the array of parameter types.
   *
   * @return the {@link Method} for given name/parameters, or <code>null</code> if no such method
   *         found.
   */
  public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
    Assert.isNotNull(clazz);
    Assert.isNotNull(name);
    Assert.isNotNull(parameterTypes);
    String signature = getMethodSignature(name, parameterTypes);
    return getMethodBySignature(clazz, signature);
  }

  /**
   * @return <code>true</code> if given {@link Method} is declared in {@link Class} with given name
   *         or one of its superclasses.
   */
  public static boolean isAlreadyDeclaredIn(Method method, Class<?> targetClass) {
    Class<?> declaringClass = method.getDeclaringClass();
    return declaringClass.isAssignableFrom(targetClass);
  }

  /**
   * @return <code>true</code> if two {@link Method}s have same name, but parameter types of
   *         "specific" are {@link #isMoreSpecific(Class[], Class[])} than "base".
   */
  public static boolean isMoreSpecific(Method base, Method specific) {
    return base.getName().equals(specific.getName())
        && isMoreSpecific(base.getParameterTypes(), specific.getParameterTypes());
  }

  /**
   * @return the {@link Method} which is {@link #isMoreSpecific(Method, Method)} than all others.
   */
  public static Method getMostSpecific(List<Method> methods) {
    Method mostSpecific = null;
    for (Method method : methods) {
      if (mostSpecific == null || isMoreSpecific(mostSpecific, method)) {
        mostSpecific = method;
      }
    }
    return mostSpecific;
  }

  /**
   * @return the {@link Object} result of invoking method with given signature.
   */
  public static Object invokeMethodEx(Object object, String signature, Object... arguments) {
    try {
      return invokeMethod(object, signature, arguments);
    } catch (Throwable e) {
      throw propagate(e);
    }
  }

  /**
   * @return the {@link Object} result of invoking method with given signature.
   */
  public static Object invokeMethod(Object object, String signature, Object... arguments)
      throws Exception {
    Assert.isNotNull(object);
    Assert.isNotNull(arguments);
    // prepare class/object
    Class<?> refClass = getRefClass(object);
    Object refObject = getRefObject(object);
    // prepare method
    Method method = getMethodBySignature(refClass, signature);
    Assert.isNotNull(method, "Can not find method " + signature + " in " + refClass);
    // do invoke
    try {
      return method.invoke(refObject, arguments);
    } catch (InvocationTargetException e) {
      throw propagate(e.getCause());
    }
  }

  /**
   * Invokes method without parameters.
   *
   * @return the {@link Object} result of invoking method.
   */
  public static Object invokeMethod2(Object object, String name) throws Exception {
    return invokeMethod(object, name + "()");
  }

  /**
   * Invokes method with single parameter.
   *
   * @return the {@link Object} result of invoking method.
   */
  public static Object invokeMethod2(Object object,
      String name,
      Class<?> parameterType_1,
      Object argument_1) throws Exception {
    Class<?>[] types = new Class<?>[]{parameterType_1};
    Object[] values = new Object[]{argument_1};
    return invokeMethod2(object, name, types, values);
  }

  /**
   * Invokes method with two parameters.
   *
   * @return the {@link Object} result of invoking method.
   */
  public static Object invokeMethod2(Object object,
      String name,
      Class<?> parameterType_1,
      Class<?> parameterType_2,
      Object argument_1,
      Object argument_2) throws Exception {
    Class<?>[] types = new Class<?>[]{parameterType_1, parameterType_2};
    Object[] values = new Object[]{argument_1, argument_2};
    return invokeMethod2(object, name, types, values);
  }

  /**
   * Invokes method with three parameters.
   *
   * @return the {@link Object} result of invoking method.
   */
  public static Object invokeMethod2(Object object,
      String name,
      Class<?> parameterType_1,
      Class<?> parameterType_2,
      Class<?> parameterType_3,
      Object argument_1,
      Object argument_2,
      Object argument_3) throws Exception {
    Class<?>[] types = new Class<?>[]{parameterType_1, parameterType_2, parameterType_3};
    Object[] values = new Object[]{argument_1, argument_2, argument_3};
    return invokeMethod2(object, name, types, values);
  }

  /**
   * Invokes method with four parameters.
   *
   * @return the {@link Object} result of invoking method.
   */
  public static Object invokeMethod2(Object object,
      String name,
      Class<?> parameterType_1,
      Class<?> parameterType_2,
      Class<?> parameterType_3,
      Class<?> parameterType_4,
      Object argument_1,
      Object argument_2,
      Object argument_3,
      Object argument_4) throws Exception {
    Class<?>[] types =
        new Class<?>[]{parameterType_1, parameterType_2, parameterType_3, parameterType_4};
    Object[] values = new Object[]{argument_1, argument_2, argument_3, argument_4};
    return invokeMethod2(object, name, types, values);
  }

  /**
   * Invokes method by name and parameter types.
   *
   * @param object
   *          the object to call, may be {@link Class} for invoking static method.
   * @param name
   *          the name of method.
   * @param parameterTypes
   *          the types of parameters.
   * @param arguments
   *          the values of argument for invocation.
   *
   * @return the {@link Object} result of invoking method.
   */
  public static Object invokeMethod2(Object object,
      String name,
      Class<?>[] parameterTypes,
      Object[] arguments) throws Exception {
    Assert.equals(parameterTypes.length, arguments.length);
    String signature = getMethodSignature(name, parameterTypes);
    return invokeMethod(object, signature, arguments);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return signature for given {@link Constructor}. This signature is not same signature as in JVM
   *         or JDT, just some string that unique identifies constructor in its {@link Class}.
   */
  public static String getConstructorSignature(Constructor<?> constructor) {
    Assert.isNotNull(constructor);
    return getConstructorSignature(constructor.getParameterTypes());
  }

  /**
   * @return generic signature for given {@link Constructor}. This signature is not same signature
   *         as in JVM or JDT, just some string that unique identifies constructor in its
   *         {@link Class}. It uses {@link Constructor#getGenericParameterTypes()}, so for each
   *         {@link TypeVariable} its name will be used in signature.
   */
  public static String getConstructorGenericSignature(Constructor<?> constructor) {
    Assert.isNotNull(constructor);
    return getConstructorSignature(constructor.getGenericParameterTypes());
  }

  /**
   * Returns the signature of {@link Constructor} with given parameter types. This signature is not
   * same signature as in JVM or JDT, just some string that unique identifies constructor in its
   * {@link Class}.
   *
   * @param parameterTypes
   *          the types of {@link Constructor} parameters.
   *
   * @return signature of {@link Constructor}.
   */
  public static String getConstructorSignature(Type... parameterTypes) {
    Assert.isNotNull(parameterTypes);
    //
    StringBuilder buffer = new StringBuilder();
    buffer.append("<init>");
    appendParameterTypes(buffer, parameterTypes);
    return buffer.toString();
  }

  /**
   * @param signature
   *          the signature of {@link Constructor} in same format as
   *          {@link #getConstructorSignature(Constructor)} .
   *
   * @return the {@link Constructor} for given signature. This constructor can have any visibility,
   *         i.e. we can find even protected/private constructors.
   */
  @SuppressWarnings("unchecked")
  public static <T> Constructor<T> getConstructorBySignature(Class<T> clazz, String signature) {
    Assert.isNotNull(clazz);
    Assert.isNotNull(signature);
    // check all declared constructors
    for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
      if (getConstructorSignature(constructor).equals(signature)) {
        constructor.setAccessible(true);
        return (Constructor<T>) constructor;
      }
    }
    // not found
    return null;
  }

  /**
   * @param signature
   *          the generic signature of {@link Constructor} in same format as
   *          {@link #getConstructorGenericSignature(Constructor)}.
   *
   * @return the {@link Constructor} for given signature. This constructor can have any visibility,
   *         i.e. we can find even protected/private constructors.
   */
  @SuppressWarnings("unchecked")
  public static <T> Constructor<T> getConstructorByGenericSignature(Class<T> clazz, String signature) {
    Assert.isNotNull(clazz);
    Assert.isNotNull(signature);
    // check all declared constructors
    for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
      if (getConstructorGenericSignature(constructor).equals(signature)) {
        constructor.setAccessible(true);
        return (Constructor<T>) constructor;
      }
    }
    // not found
    return null;
  }

  /**
   * @param parameterTypes
   *          the array of parameter types.
   *
   * @return the {@link Constructor} for given signature. This constructor can have any visibility,
   *         i.e. we can find even protected/private constructors.
   */
  public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
    Assert.isNotNull(clazz);
    Assert.isNotNull(parameterTypes);
    String signature = getConstructorSignature(parameterTypes);
    return getConstructorBySignature(clazz, signature);
  }

  /**
   * @return the {@link Constructor} which can be invoked with given arguments, may be
   *         <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  public static <T> Constructor<T> getConstructorForArguments(Class<T> clazz, Object... arguments) {
    Assert.isNotNull(clazz);
    Assert.isNotNull(arguments);
    for (Constructor<?> constructor : clazz.getConstructors()) {
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      // check compatibility of each parameter and argument
      boolean canUse = true;
      if (parameterTypes.length == arguments.length) {
        for (int i = 0; i < parameterTypes.length; i++) {
          Class<?> parameterType = parameterTypes[i];
          Object argument = arguments[i];
          if (!isAssignableFrom(parameterType, argument)) {
            canUse = false;
            break;
          }
        }
        // use if possible
        if (canUse) {
          return (Constructor<T>) constructor;
        }
      }
    }
    // no compatible constructor
    return null;
  }

  /**
   * @return <code>true</code> if two given {@link Constructor}-s represent same constructor.
   */
  public static boolean equals(Constructor<?> constructor_1, Constructor<?> constructor_2) {
    if (constructor_1 == constructor_2) {
      return true;
    }
    return constructor_1.getDeclaringClass() == constructor_2.getDeclaringClass()
        && ObjectUtils.equals(
            getConstructorSignature(constructor_1),
            getConstructorSignature(constructor_2));
  }

  /**
   * @return the {@link Constructor} with minimal number of parameters.
   */
  public static Constructor<?> getShortestConstructor(Class<?> clazz) {
    Constructor<?> shortest = null;
    int minCount = Integer.MAX_VALUE;
    for (Constructor<?> constructor : clazz.getConstructors()) {
      int thisCount = constructor.getParameterTypes().length;
      if (minCount > thisCount) {
        shortest = constructor;
        minCount = thisCount;
      }
    }
    return shortest;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Field
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return all declared {@link Field}'s, including protected and private.
   */
  public static List<Field> getFields(Class<?> clazz) {
    List<Field> fields = Lists.newArrayList();
    while (clazz != null) {
      // add all declared field
      for (Field field : clazz.getDeclaredFields()) {
        field.setAccessible(true);
        fields.add(field);
      }
      // process superclass
      clazz = clazz.getSuperclass();
    }
    return fields;
  }

  /**
   * @return the {@link Field} of given class with given name or <code>null</code> if no such
   *         {@link Field} found.
   */
  public static Field getFieldByName(Class<?> clazz, String name) {
    Assert.isNotNull(clazz);
    Assert.isNotNull(name);
    // check fields of given class and its super classes
    while (clazz != null) {
      // check all declared field
      Field[] declaredFields = clazz.getDeclaredFields();
      for (Field field : declaredFields) {
        if (field.getName().equals(name)) {
          field.setAccessible(true);
          return field;
        }
      }
      // check interfaces
      {
        Class<?>[] interfaceClasses = clazz.getInterfaces();
        for (Class<?> interfaceClass : interfaceClasses) {
          Field field = getFieldByName(interfaceClass, name);
          if (field != null) {
            return field;
          }
        }
      }
      // check superclass
      clazz = clazz.getSuperclass();
    }
    // not found
    return null;
  }

  /**
   * @return the {@link Object} value of field with given name.
   */
  public static Object getFieldObject(final Object object, final String name) {
    Assert.isNotNull(object);
    Assert.isNotNull(name);
    return ExecutionUtils.runObject(new RunnableObjectEx<Object>() {
      public Object runObject() throws Exception {
        Class<?> refClass = getRefClass(object);
        Object refObject = getRefObject(object);
        Field field = getFieldByName(refClass, name);
        if (field == null) {
          throw new IllegalArgumentException("Unable to find '" + name + "' in " + refClass);
        }
        return field.get(refObject);
      }
    });
  }

  /**
   * @return the {@link String} value of field with given name.
   */
  public static String getFieldString(Object object, String name) {
    return (String) getFieldObject(object, name);
  }

  /**
   * @return the <code>int</code> value of field with given name.
   */
  public static int getFieldInt(Object object, String name) {
    return (Integer) getFieldObject(object, name);
  }

  /**
   * @return the <code>short</code> value of field with given name.
   */
  public static short getFieldShort(Object object, String name) {
    return (Short) getFieldObject(object, name);
  }

  /**
   * @return the <code>long</code> value of field with given name.
   */
  public static long getFieldLong(Object object, String name) {
    return (Long) getFieldObject(object, name);
  }

  /**
   * @return the <code>float</code> value of field with given name.
   */
  public static float getFieldFloat(Object object, String name) {
    return (Float) getFieldObject(object, name);
  }

  /**
   * @return the <code>boolean</code> value of field with given name.
   */
  public static boolean getFieldBoolean(Object object, String name) {
    return (Boolean) getFieldObject(object, name);
  }

  /**
   * Sets {@link Object} value of field with given name.
   */
  public static void setField(Object object, String name, Object value) {
    try {
      Assert.isNotNull(object);
      Class<?> refClass = getRefClass(object);
      Object refObject = getRefObject(object);
      getFieldByName(refClass, name).set(refObject, value);
    } catch (Throwable e) {
      throw propagate(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Class} of given {@link Object} or casted object, if it is {@link Class}
   *         itself.
   */
  private static Class<?> getRefClass(Object object) {
    return object instanceof Class<?> ? (Class<?>) object : object.getClass();
  }

  /**
   * @return the {@link Object} that should be used as argument for {@link Field#get(Object)} and
   *         {@link Method#invoke(Object, Object[])}.
   */
  private static Object getRefObject(Object object) {
    return object instanceof Class<?> ? null : object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Throwable propagation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper class used in {@link #propagate(Throwable)}.
   */
  private static class ExceptionThrower {
    private static Throwable throwable;

    private ExceptionThrower() throws Throwable {
      if (System.getProperty("wbp.ReflectionUtils.propagate().InstantiationException") != null) {
        throw new InstantiationException();
      }
      if (System.getProperty("wbp.ReflectionUtils.propagate().IllegalAccessException") != null) {
        throw new IllegalAccessException();
      }
      throw throwable;
    }

    public static synchronized void spit(Throwable t) {
      if (System.getProperty("wbp.ReflectionUtils.propagate().dontThrow") == null) {
        ExceptionThrower.throwable = t;
        try {
          ExceptionThrower.class.newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } finally {
          ExceptionThrower.throwable = null;
        }
      }
    }
  }

  /**
   * Propagates {@code throwable} as-is without any wrapping. This is trick.
   *
   * @return nothing will ever be returned; this return type is only for your convenience, to use
   *         this method in "throw" statement.
   */
  public static RuntimeException propagate(Throwable throwable) {
    if (System.getProperty("wbp.ReflectionUtils.propagate().forceReturn") == null) {
      ExceptionThrower.spit(throwable);
    }
    return null;
  }

  /**
   * @return the {@link Exception} that is given {@link Throwable} or wraps it.
   */
  public static Exception getExceptionToThrow(Throwable e) {
    if (e instanceof Exception) {
      return (Exception) e;
    }
    return new Exception(e);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyDescriptor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link BeanInfo} of given component {@link Class}.
   */
  public static BeanInfo getBeanInfo(Class<?> clazz) throws IntrospectionException {
    // standard components don't have BeanInfo's
    {
      String className = clazz.getName();
      if (className.startsWith("java.lang.")
          || className.startsWith("java.awt.")
          || className.startsWith("javax.swing.")
          || className.startsWith("org.eclipse.swt")
          || className.startsWith("org.eclipse.jface")
          || className.startsWith("org.eclipse.ui.forms")) {
        return null;
      }
    }
    // OK, get BeanInfo (may be not trivial)
    Introspector.flushCaches();
    String[] standard_beanInfoSearchPath = Introspector.getBeanInfoSearchPath();
    try {
      Introspector.setBeanInfoSearchPath(new String[]{});
      return Introspector.getBeanInfo(clazz);
    } finally {
      Introspector.flushCaches();
      Introspector.setBeanInfoSearchPath(standard_beanInfoSearchPath);
    }
  }

  private static ClassMap<List<PropertyDescriptor>> m_propertyDescriptorsCache = ClassMap.create();

  /**
   * Flushes cache for specified {@link Class}.
   */
  public static void flushPropertyDescriptorsCache(Class<?> clazz) {
    m_propertyDescriptorsCache.remove(clazz);
  }

  /**
   * @return the {@link PropertyDescriptor}'s for given {@link Class}.
   */
  public static List<PropertyDescriptor> getPropertyDescriptors(BeanInfo beanInfo,
      Class<?> componentClass) throws Exception {
    // check cache
    {
      List<PropertyDescriptor> descriptors = m_propertyDescriptorsCache.get(componentClass);
      if (descriptors != null) {
        return descriptors;
      }
    }
    // prepare descriptions
    List<PropertyDescriptor> descriptors = Lists.newArrayList();
    // if there is BeanInfo, try to use it
    if (beanInfo != null) {
      Collections.addAll(descriptors, beanInfo.getPropertyDescriptors());
      // remove indexed properties
      for (Iterator<PropertyDescriptor> I = descriptors.iterator(); I.hasNext();) {
        PropertyDescriptor descriptor = I.next();
        if (descriptor instanceof IndexedPropertyDescriptor) {
          I.remove();
        }
      }
    }
    // prepare getters/setters
    Map<String, Method> propertyToGetter = Maps.newTreeMap();
    Map<String, Method> propertyToSetter = Maps.newTreeMap();
    // append existing getters/setters
    for (PropertyDescriptor propertyDescriptor : descriptors) {
      Method readMethod = getReadMethod(propertyDescriptor);
      Method writeMethod = getWriteMethod(propertyDescriptor);
      if (readMethod != null) {
        String propertyName = getQualifiedPropertyName(readMethod);
        propertyToGetter.put(propertyName, readMethod);
        propertyDescriptor.setName(propertyName);
      }
      if (writeMethod != null) {
        String propertyName = getQualifiedPropertyName(writeMethod);
        propertyToSetter.put(propertyName, writeMethod);
        propertyDescriptor.setName(propertyName);
      }
    }
    // append missing methods (most probably protected)
    Set<String> newPropertyNames = Sets.newTreeSet();
    appendPropertyComponents(componentClass, newPropertyNames, propertyToGetter, propertyToSetter);
    // create PropertyDescriptor's for new getters/setters
    for (String propertyName : newPropertyNames) {
      addPropertyDescriptor(descriptors, propertyName, propertyToGetter, propertyToSetter);
    }
    useSimplePropertyNamesWherePossible(descriptors);
    makeMethodsAccessible(descriptors);
    // OK, final result
    m_propertyDescriptorsCache.put(componentClass, descriptors);
    return descriptors;
  }

  private static void useSimplePropertyNamesWherePossible(List<PropertyDescriptor> descriptors) {
    // prepare map: simple name -> qualified names
    Multimap<String, String> simplePropertyNames = HashMultimap.create();
    for (PropertyDescriptor propertyDescriptor : descriptors) {
      String qualifiedPropertyName = propertyDescriptor.getName();
      String simplePropertyName = getSimplePropertyName(qualifiedPropertyName);
      simplePropertyNames.put(simplePropertyName, qualifiedPropertyName);
    }
    // if simple name is unique, use it
    for (PropertyDescriptor propertyDescriptor : descriptors) {
      String qualifiedPropertyName = propertyDescriptor.getName();
      String simplePropertyName = getSimplePropertyName(qualifiedPropertyName);
      if (simplePropertyNames.get(simplePropertyName).size() == 1) {
        propertyDescriptor.setName(simplePropertyName);
      }
    }
  }

  private static void makeMethodsAccessible(List<PropertyDescriptor> descriptors) {
    for (PropertyDescriptor propertyDescriptor : descriptors) {
      Method getMethod = getReadMethod(propertyDescriptor);
      Method setMethod = getWriteMethod(propertyDescriptor);
      if (getMethod != null) {
        getMethod.setAccessible(true);
      }
      if (setMethod != null) {
        setMethod.setAccessible(true);
      }
    }
  }

  private static void addPropertyDescriptor(List<PropertyDescriptor> descriptors,
      String qualifiedPropertyName,
      Map<String, Method> propertyToGetter,
      Map<String, Method> propertyToSetter) throws Exception {
    if (qualifiedPropertyName.startsWith("(")) {
      return;
    }
    // prepare methods
    Method getMethod = propertyToGetter.get(qualifiedPropertyName);
    Method setMethod = propertyToSetter.get(qualifiedPropertyName);
    if (!isValidForJavaIBM(getMethod) || !isValidForJavaIBM(setMethod)) {
      return;
    }
    if (getMethod != null && getMethod.getReturnType() == Void.TYPE) {
      return;
    }
    // add property
    descriptors.add(new PropertyDescriptor(qualifiedPropertyName, getMethod, setMethod));
  }

  /**
   * IMB Java does not allow to create {@link PropertyDescriptor} for non-public methods. See (Case
   * 37683).
   */
  private static boolean isValidForJavaIBM(Method method) {
    if (method == null) {
      return true;
    }
    return !EnvironmentUtils.isJavaIBM() || isPublic(method);
  }

  /**
   * Appends components for {@link PropertyDescriptor}'s of given {@link Class}, its super class and
   * implemented interfaces.
   */
  private static void appendPropertyComponents(Class<?> currentClass,
      Set<String> newPropertyNames,
      Map<String, Method> propertyToGetter,
      Map<String, Method> propertyToSetter) {
    for (Method method : currentClass.getDeclaredMethods()) {
      int methodModifiers = method.getModifiers();
      boolean isPublic = Modifier.isPublic(methodModifiers);
      boolean isProtected = Modifier.isProtected(methodModifiers);
      boolean isStatic = Modifier.isStatic(methodModifiers);
      if (method.isBridge()) {
        continue;
      }
      if (!isStatic && (isPublic || isProtected)) {
        method.setAccessible(true);
        String methodName = method.getName();
        if (methodName.startsWith("set") && method.getParameterTypes().length == 1) {
          String propertyName = getQualifiedPropertyName(method);
          if (!propertyToSetter.containsKey(propertyName)) {
            newPropertyNames.add(propertyName);
            propertyToSetter.put(propertyName, method);
          }
        }
        if (method.getParameterTypes().length == 0) {
          if (methodName.startsWith("get")) {
            String propertyName = getQualifiedPropertyName(method);
            if (!propertyToGetter.containsKey(propertyName)) {
              newPropertyNames.add(propertyName);
              propertyToGetter.put(propertyName, method);
            }
          }
          if (methodName.startsWith("is")) {
            String propertyName = getQualifiedPropertyName(method);
            if (!propertyToGetter.containsKey(propertyName)) {
              newPropertyNames.add(propertyName);
              propertyToGetter.put(propertyName, method);
            }
          }
        }
      }
    }
    // process interfaces
    for (Class<?> interfaceClass : currentClass.getInterfaces()) {
      appendPropertyComponents(interfaceClass, newPropertyNames, propertyToGetter, propertyToSetter);
    }
    // process super Class
    if (currentClass.getSuperclass() != null) {
      appendPropertyComponents(
          currentClass.getSuperclass(),
          newPropertyNames,
          propertyToGetter,
          propertyToSetter);
    }
  }

  private static String getQualifiedPropertyName(Method method) {
    // strip Method name to property name
    String propertyName;
    {
      propertyName = method.getName();
      if (propertyName.startsWith("is")) {
        propertyName = propertyName.substring(2);
      } else if (propertyName.startsWith("get")) {
        propertyName = propertyName.substring(3);
      } else if (propertyName.startsWith("set")) {
        propertyName = propertyName.substring(3);
      }
      propertyName = Introspector.decapitalize(propertyName);
    }
    // include also type
    String types;
    {
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length == 0) {
        types = "(" + getFullyQualifiedName(method.getReturnType(), false) + ")";
      } else {
        StringBuilder buffer = new StringBuilder();
        appendParameterTypes(buffer, parameterTypes);
        types = buffer.toString();
      }
    }
    // return qualified property name
    return propertyName + types;
  }

  private static String getSimplePropertyName(String qualifiedPropertyName) {
    return StringUtils.substringBefore(qualifiedPropertyName, "(");
  }
}
