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
package org.eclipse.wb.tests.designer.core.util.reflect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import net.sf.cglib.asm.Opcodes;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.transform.ClassEmitterTransformer;
import net.sf.cglib.transform.ClassTransformer;
import net.sf.cglib.transform.TransformingClassGenerator;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.SystemUtils;

import java.awt.Component;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.LineNumberReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author scheglov_ke
 */
public class ReflectionUtilsTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#getClassLoader(Class)}.
   */
  public void test_getClassLoader() throws Exception {
    // "normal" class
    {
      Class<?> clazz = getClass();
      assertSame(clazz.getClassLoader(), ReflectionUtils.getClassLoader(clazz));
    }
    // "system" class
    {
      Class<?> clazz = String.class;
      assertNull(clazz.getClassLoader());
      assertSame(ClassLoader.getSystemClassLoader(), ReflectionUtils.getClassLoader(clazz));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFullyQualifiedName
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getFullyQualifiedName_primitive_void() throws Exception {
    check_getFullyQualifiedName("void", "void", void.class);
  }

  public void test_getFullyQualifiedName_primitive_boolean() throws Exception {
    check_getFullyQualifiedName("boolean", "boolean", boolean.class);
  }

  public void test_getFullyQualifiedName_primitive_byte() throws Exception {
    check_getFullyQualifiedName("byte", "byte", byte.class);
  }

  public void test_getFullyQualifiedName_primitive_char() throws Exception {
    check_getFullyQualifiedName("char", "char", char.class);
  }

  public void test_getFullyQualifiedName_primitive_short() throws Exception {
    check_getFullyQualifiedName("short", "short", short.class);
  }

  public void test_getFullyQualifiedName_primitive_int() throws Exception {
    check_getFullyQualifiedName("int", "int", int.class);
  }

  public void test_getFullyQualifiedName_primitive_long() throws Exception {
    check_getFullyQualifiedName("long", "long", long.class);
  }

  public void test_getFullyQualifiedName_primitive_float() throws Exception {
    check_getFullyQualifiedName("float", "float", float.class);
  }

  public void test_getFullyQualifiedName_primitive_double() throws Exception {
    check_getFullyQualifiedName("double", "double", double.class);
  }

  public void test_getFullyQualifiedName_String() throws Exception {
    check_getFullyQualifiedName("java.lang.String", "java.lang.String", String.class);
  }

  public void test_getFullyQualifiedName_inner() throws Exception {
    check_getFullyQualifiedName("java.util.Map.Entry", "java.util.Map$Entry", Map.Entry.class);
  }

  public void test_getFullyQualifiedName_array_primitive() throws Exception {
    check_getFullyQualifiedName("int[]", "int[]", int[].class);
  }

  public void test_getFullyQualifiedName_array_primitive2() throws Exception {
    check_getFullyQualifiedName("int[][]", "int[][]", int[][].class);
  }

  public void test_getFullyQualifiedName_array_String() throws Exception {
    check_getFullyQualifiedName("java.lang.String[]", "java.lang.String[]", String[].class);
  }

  public void test_getFullyQualifiedName_array_String2() throws Exception {
    check_getFullyQualifiedName("java.lang.String[][]", "java.lang.String[][]", String[][].class);
  }

  public void test_getFullyQualifiedName_TypeVariable() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      <T> void foo(T values) {
      }
    }
    Method method = Foo.class.getDeclaredMethods()[0];
    String expected = "T";
    check_getFullyQualifiedName(expected, expected, method.getGenericParameterTypes()[0]);
  }

  public void test_getFullyQualifiedName_GenericArrayType() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      <T> void foo(T[] values) {
      }
    }
    Method method = Foo.class.getDeclaredMethods()[0];
    String expected = "T[]";
    check_getFullyQualifiedName(expected, expected, method.getGenericParameterTypes()[0]);
  }

  public void test_getFullyQualifiedName_ParameterizedType() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      <K, V> void foo(Map<K, V> values) {
      }
    }
    Method method = Foo.class.getDeclaredMethods()[0];
    String expected = "java.util.Map<K,V>";
    check_getFullyQualifiedName(expected, expected, method.getGenericParameterTypes()[0]);
  }

  public void test_getFullyQualifiedName_WildcardType() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      <T> void foo(List<? extends T> values) {
      }
    }
    Method method = Foo.class.getDeclaredMethods()[0];
    String expected = "java.util.List<? extends T>";
    check_getFullyQualifiedName(expected, expected, method.getGenericParameterTypes()[0]);
  }

  private void check_getFullyQualifiedName(String expectedSource, String expectedRuntime, Type clazz)
      throws Exception {
    assertEquals(expectedSource, ReflectionUtils.getFullyQualifiedName(clazz, false));
    assertEquals(expectedRuntime, ReflectionUtils.getFullyQualifiedName(clazz, true));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getCanonicalName()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#getCanonicalName(Class)}.
   */
  public void test_getCanonicalName() throws Exception {
    assertEquals("java.lang.String", ReflectionUtils.getCanonicalName(String.class));
    assertEquals("java.util.Map.Entry", ReflectionUtils.getCanonicalName(Map.Entry.class));
    assertEquals("java.lang.String[]", ReflectionUtils.getCanonicalName(String[].class));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getShortName()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#getShortName(Class)}.
   */
  public void test_getShortName() throws Exception {
    assertEquals("int", ReflectionUtils.getShortName(int.class));
    assertEquals("String", ReflectionUtils.getShortName(String.class));
    assertEquals("String[]", ReflectionUtils.getShortName(String[].class));
    assertEquals("Map.Entry", ReflectionUtils.getShortName(Map.Entry.class));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modifiers
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_isX_Constructor() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      public Foo() {
      }

      protected Foo(int v) {
      }

      private Foo(boolean v) {
      }

      Foo(long v) {
      }
    }
    Constructor<?>[] declaredConstructors = Foo.class.getDeclaredConstructors();
    // public
    {
      boolean seenPublic = false;
      for (Constructor<?> constructor : declaredConstructors) {
        if (ReflectionUtils.isPublic(constructor)) {
          seenPublic = true;
          assertTrue(ReflectionUtils.isPublic(constructor));
          assertFalse(ReflectionUtils.isProtected(constructor));
          assertFalse(ReflectionUtils.isPrivate(constructor));
          assertFalse(ReflectionUtils.isPackagePrivate(constructor));
        }
      }
      assertTrue(seenPublic);
    }
    // protected
    {
      boolean seenProtected = false;
      for (Constructor<?> constructor : declaredConstructors) {
        if (ReflectionUtils.isProtected(constructor)) {
          seenProtected = true;
          assertFalse(ReflectionUtils.isPublic(constructor));
          assertTrue(ReflectionUtils.isProtected(constructor));
          assertFalse(ReflectionUtils.isPrivate(constructor));
          assertFalse(ReflectionUtils.isPackagePrivate(constructor));
        }
      }
      assertTrue(seenProtected);
    }
    // private
    {
      boolean seenPrivate = false;
      for (Constructor<?> constructor : declaredConstructors) {
        if (ReflectionUtils.isPrivate(constructor)) {
          seenPrivate = true;
          assertFalse(ReflectionUtils.isPublic(constructor));
          assertFalse(ReflectionUtils.isProtected(constructor));
          assertTrue(ReflectionUtils.isPrivate(constructor));
          assertFalse(ReflectionUtils.isPackagePrivate(constructor));
        }
      }
      assertTrue(seenPrivate);
    }
    // package private
    {
      boolean seenPackagePrivate = false;
      for (Constructor<?> constructor : declaredConstructors) {
        if (ReflectionUtils.isPackagePrivate(constructor)) {
          seenPackagePrivate = true;
          assertFalse(ReflectionUtils.isPublic(constructor));
          assertFalse(ReflectionUtils.isProtected(constructor));
          assertFalse(ReflectionUtils.isPrivate(constructor));
          assertTrue(ReflectionUtils.isPackagePrivate(constructor));
        }
      }
      assertTrue(seenPackagePrivate);
    }
  }

  public void test_isX_Method() throws Exception {
    @SuppressWarnings("unused")
    abstract class Foo {
      public void a() {
      }

      protected void b() {
      }

      private void c() {
      }

      void d() {
      }

      abstract void e();
    }
    // public
    {
      Method method = ReflectionUtils.getMethodBySignature(Foo.class, "a()");
      assertTrue(ReflectionUtils.isPublic(method));
      assertFalse(ReflectionUtils.isProtected(method));
      assertFalse(ReflectionUtils.isPrivate(method));
      assertFalse(ReflectionUtils.isPackagePrivate(method));
    }
    // protected
    {
      Method method = ReflectionUtils.getMethodBySignature(Foo.class, "b()");
      assertFalse(ReflectionUtils.isPublic(method));
      assertTrue(ReflectionUtils.isProtected(method));
      assertFalse(ReflectionUtils.isPrivate(method));
      assertFalse(ReflectionUtils.isPackagePrivate(method));
    }
    // private
    {
      Method method = ReflectionUtils.getMethodBySignature(Foo.class, "c()");
      assertFalse(ReflectionUtils.isPublic(method));
      assertFalse(ReflectionUtils.isProtected(method));
      assertTrue(ReflectionUtils.isPrivate(method));
      assertFalse(ReflectionUtils.isPackagePrivate(method));
    }
    // package private
    {
      Method method = ReflectionUtils.getMethodBySignature(Foo.class, "d()");
      assertFalse(ReflectionUtils.isPublic(method));
      assertFalse(ReflectionUtils.isProtected(method));
      assertFalse(ReflectionUtils.isPrivate(method));
      assertTrue(ReflectionUtils.isPackagePrivate(method));
    }
    // abstract
    {
      {
        Method method = ReflectionUtils.getMethodBySignature(Foo.class, "a()");
        assertFalse(ReflectionUtils.isAbstract(method));
      }
      {
        Method method = ReflectionUtils.getMethodBySignature(Foo.class, "e()");
        assertTrue(ReflectionUtils.isAbstract(method));
      }
    }
  }

  public void test_isX_Field() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      public int a;
      protected int b;
      private int c;
      int d;
    }
    // public
    {
      Field field = ReflectionUtils.getFieldByName(Foo.class, "a");
      assertTrue(ReflectionUtils.isPublic(field));
      assertFalse(ReflectionUtils.isProtected(field));
      assertFalse(ReflectionUtils.isPrivate(field));
      assertFalse(ReflectionUtils.isPackagePrivate(field));
    }
    // protected
    {
      Field field = ReflectionUtils.getFieldByName(Foo.class, "b");
      assertFalse(ReflectionUtils.isPublic(field));
      assertTrue(ReflectionUtils.isProtected(field));
      assertFalse(ReflectionUtils.isPrivate(field));
      assertFalse(ReflectionUtils.isPackagePrivate(field));
    }
    // private
    {
      Field field = ReflectionUtils.getFieldByName(Foo.class, "c");
      assertFalse(ReflectionUtils.isPublic(field));
      assertFalse(ReflectionUtils.isProtected(field));
      assertTrue(ReflectionUtils.isPrivate(field));
      assertFalse(ReflectionUtils.isPackagePrivate(field));
    }
    // package private
    {
      Field field = ReflectionUtils.getFieldByName(Foo.class, "d");
      assertFalse(ReflectionUtils.isPublic(field));
      assertFalse(ReflectionUtils.isProtected(field));
      assertFalse(ReflectionUtils.isPrivate(field));
      assertTrue(ReflectionUtils.isPackagePrivate(field));
    }
  }

  private class Class_private {
  }
  protected class Class_protected {
  }
  public class Class_public {
  }
  class Class_packagePrivate {
  }

  public void test_isX_Class() throws Exception {
    // public
    {
      Class<?> clazz = Class_public.class;
      assertTrue(ReflectionUtils.isPublic(clazz));
      assertFalse(ReflectionUtils.isProtected(clazz));
      assertFalse(ReflectionUtils.isPrivate(clazz));
      assertFalse(ReflectionUtils.isPackagePrivate(clazz));
    }
    // protected
    {
      Class<?> clazz = Class_protected.class;
      assertFalse(ReflectionUtils.isPublic(clazz));
      assertTrue(ReflectionUtils.isProtected(clazz));
      assertFalse(ReflectionUtils.isPrivate(clazz));
      assertFalse(ReflectionUtils.isPackagePrivate(clazz));
    }
    // private
    {
      Class<?> clazz = Class_private.class;
      assertFalse(ReflectionUtils.isPublic(clazz));
      assertFalse(ReflectionUtils.isProtected(clazz));
      assertTrue(ReflectionUtils.isPrivate(clazz));
      assertFalse(ReflectionUtils.isPackagePrivate(clazz));
    }
    // package private
    {
      Class<?> clazz = Class_packagePrivate.class;
      assertFalse(ReflectionUtils.isPublic(clazz));
      assertFalse(ReflectionUtils.isProtected(clazz));
      assertFalse(ReflectionUtils.isPrivate(clazz));
      assertTrue(ReflectionUtils.isPackagePrivate(clazz));
    }
  }

  /**
   * Test for {@link ReflectionUtils#isAbstract(Class)}.
   */
  public void test_isAbstract_Class() throws Exception {
    assertFalse(ReflectionUtils.isAbstract(Object.class));
    assertTrue(ReflectionUtils.isAbstract(JComponent.class));
  }

  /**
   * Test for {@link ReflectionUtils#isStatic(Field)}.
   */
  public void test_isStatic_Field() throws Exception {
    // static
    {
      Field field = ReflectionUtils.getFieldByName(Integer.class, "MIN_VALUE");
      assertTrue(ReflectionUtils.isStatic(field));
    }
    // not static
    {
      Field field = ReflectionUtils.getFieldByName(java.awt.Dimension.class, "width");
      assertFalse(ReflectionUtils.isStatic(field));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethods()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#getMethods(Class)}.
   */
  public void test_getMethods() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      public void a() {
      }

      protected void b() {
      }

      private void c() {
      }

      void d() {
      }
    }
    @SuppressWarnings("unused")
    class Bar extends Foo {
      public void e() {
      }
    }
    Map<String, Method> methods = ReflectionUtils.getMethods(Bar.class);
    assertThat(methods.values()).contains(
        Foo.class.getDeclaredMethod("a"),
        Foo.class.getDeclaredMethod("b"),
        Foo.class.getDeclaredMethod("c"),
        Foo.class.getDeclaredMethod("d"),
        Bar.class.getDeclaredMethod("e"));
  }

  /**
   * Test for {@link ReflectionUtils#getMethods(Class)}.
   * <p>
   * Only last implementation of each method should be returned.
   */
  public void test_getMethods_forInterface() throws Exception {
    abstract class Foo implements Collection<Object> {
    }
    Map<String, Method> methods = ReflectionUtils.getMethods(Foo.class);
    assertThat(methods.values()).contains(Collection.class.getDeclaredMethod("size"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodByName
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethodByName_public() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      public void a() {
      }
    }
    assertSame(null, ReflectionUtils.getMethodByName(Foo.class, "noSuchMethod"));
    {
      Method actual = ReflectionUtils.getMethodByName(Foo.class, "a");
      assertNotNull(actual);
      assertEquals("a()", ReflectionUtils.getMethodSignature(actual));
    }
  }

  public void test_getMethodByName_private() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      private void a() {
      }
    }
    {
      Method actual = ReflectionUtils.getMethodByName(Foo.class, "a");
      assertNotNull(actual);
      assertEquals("a()", ReflectionUtils.getMethodSignature(actual));
    }
  }

  public void test_getMethodByName_useOneOfThem() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      private void a(boolean value) {
      }

      private void a(int value) {
      }
    }
    // we can not be sure which variant will be returned
    {
      Method actual = ReflectionUtils.getMethodByName(Foo.class, "a");
      assertNotNull(actual);
      assertEquals("a", actual.getName());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodBySignature
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethodBySignature_public() throws Exception {
    assertNotNull(ReflectionUtils.getMethodBySignature(Object.class, "hashCode()"));
  }

  public void test_getMethodBySignature_notFound() throws Exception {
    assertNull(ReflectionUtils.getMethodBySignature(Object.class, "hashCode2()"));
  }

  public void test_getMethodBySignature_private_direct() throws Exception {
    assertNotNull(ReflectionUtils.getMethodBySignature(ArrayList.class, "fastRemove(int)"));
  }

  public void test_getMethodBySignature_private_super() throws Exception {
    assertNotNull(ReflectionUtils.getMethodBySignature(ArrayList.class, "removeRange(int,int)"));
  }

  public void test_getMethodBySignature_private_super2() throws Exception {
    assertNotNull(ReflectionUtils.getMethodBySignature(LineNumberReader.class, "fill()"));
  }

  interface MyCollection extends Collection<Object> {
  }

  public void test_getMethodBySignature_superInterface() throws Exception {
    assertNotNull(ReflectionUtils.getMethodBySignature(MyCollection.class, "size()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodByGenericSignature()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethodByGenericSignature_notFound() throws Exception {
    assertNull(ReflectionUtils.getMethodByGenericSignature(Arrays.class, "noSuchMethod()"));
  }

  public void test_getMethodByGenericSignature_array() throws Exception {
    assertNotNull(ReflectionUtils.getMethodByGenericSignature(Arrays.class, "asList(T[])"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethod() - by types
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethod_public() throws Exception {
    assertNotNull(ReflectionUtils.getMethod(Object.class, "hashCode"));
  }

  public void test_getMethod_private_direct() throws Exception {
    assertNotNull(ReflectionUtils.getMethod(ArrayList.class, "fastRemove", int.class));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Specific
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#isMoreSpecific(Class, Class)}.
   */
  public void test_isMoreSpecific_Class() throws Exception {
    assertTrue(ReflectionUtils.isMoreSpecific(List.class, ArrayList.class));
    assertFalse(ReflectionUtils.isMoreSpecific(ArrayList.class, List.class));
    assertFalse(ReflectionUtils.isMoreSpecific(List.class, String.class));
  }

  /**
   * Test for {@link ReflectionUtils#isMoreSpecific(Class[], Class[])}.
   */
  public void test_isMoreSpecific_ClassArray() throws Exception {
    {
      Class<?>[] base = new Class<?>[]{List.class};
      Class<?>[] specific = new Class<?>[]{ArrayList.class};
      assertTrue(ReflectionUtils.isMoreSpecific(base, specific));
    }
    {
      Class<?>[] base = new Class<?>[]{ArrayList.class};
      Class<?>[] specific = new Class<?>[]{List.class};
      assertFalse(ReflectionUtils.isMoreSpecific(base, specific));
    }
    {
      Class<?>[] base = new Class<?>[]{List.class, Object.class};
      Class<?>[] specific = new Class<?>[]{ArrayList.class, String.class};
      assertTrue(ReflectionUtils.isMoreSpecific(base, specific));
    }
    {
      Class<?>[] base = new Class<?>[]{List.class, Object.class};
      Class<?>[] specific = new Class<?>[]{ArrayList.class};
      assertFalse(ReflectionUtils.isMoreSpecific(base, specific));
    }
    {
      Class<?>[] base = new Class<?>[]{List.class, Object.class};
      Class<?>[] specific = new Class<?>[]{ArrayList.class, Object.class};
      assertTrue(ReflectionUtils.isMoreSpecific(base, specific));
    }
    {
      Class<?>[] base = new Class<?>[]{List.class, String.class};
      Class<?>[] specific = new Class<?>[]{ArrayList.class, Object.class};
      assertFalse(ReflectionUtils.isMoreSpecific(base, specific));
    }
  }

  /**
   * Test for {@link ReflectionUtils#isMoreSpecific(Method, Method)}.
   */
  public void test_isMoreSpecific() throws Exception {
    @SuppressWarnings("unused")
    class A {
      void foo() {
      }

      void foo(Object a) {
      }

      void foo(String a) {
      }

      void bar(String a) {
      }
    }
    {
      Method base = ReflectionUtils.getMethodBySignature(A.class, "foo()");
      Method specific = ReflectionUtils.getMethodBySignature(A.class, "foo(java.lang.String)");
      assertFalse(ReflectionUtils.isMoreSpecific(base, specific));
    }
    {
      Method base = ReflectionUtils.getMethodBySignature(A.class, "foo(java.lang.Object)");
      Method specific = ReflectionUtils.getMethodBySignature(A.class, "foo(java.lang.String)");
      assertTrue(ReflectionUtils.isMoreSpecific(base, specific));
    }
    {
      Method base = ReflectionUtils.getMethodBySignature(A.class, "foo(java.lang.Object)");
      Method specific = ReflectionUtils.getMethodBySignature(A.class, "bar(java.lang.String)");
      assertFalse(ReflectionUtils.isMoreSpecific(base, specific));
    }
  }

  /**
   * Test for {@link ReflectionUtils#getMostSpecific(List)}.
   */
  public void test_getMostSpecific() throws Exception {
    @SuppressWarnings("unused")
    class A {
      void foo(Object a) {
      }

      void foo(String a) {
      }

      void bar(String a) {
      }
    }
    {
      Method base = ReflectionUtils.getMethodBySignature(A.class, "foo(java.lang.Object)");
      Method specific = ReflectionUtils.getMethodBySignature(A.class, "foo(java.lang.String)");
      Method bar = ReflectionUtils.getMethodBySignature(A.class, "bar(java.lang.String)");
      assertSame(specific, ReflectionUtils.getMostSpecific(ImmutableList.of(base, specific, bar)));
    }
    {
      assertSame(null, ReflectionUtils.getMostSpecific(ImmutableList.<Method>of()));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isDeclaredIn()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#isAlreadyDeclaredIn(Method, String)}.
   */
  public void test_isAlreadyDeclaredIn() throws Exception {
    class Foo {
    }
    @SuppressWarnings("unused")
    class Bar extends Foo {
      public void m() {
      }
    }
    class Baz extends Bar {
    }
    {
      Method method = ReflectionUtils.getMethodBySignature(Bar.class, "m()");
      // declared in Bar itself
      assertTrue(ReflectionUtils.isAlreadyDeclaredIn(method, Bar.class));
      // Baz is subclass of Bar, so it has method
      assertTrue(ReflectionUtils.isAlreadyDeclaredIn(method, Baz.class));
      // no, Foo has no method yet
      assertFalse(ReflectionUtils.isAlreadyDeclaredIn(method, Foo.class));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enchanced classes support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#getNormalClass(Class)}.
   */
  public void test_getNormalClass() throws Exception {
    assertSame(ArrayList.class, ReflectionUtils.getNormalClass(ArrayList.class));
    {
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(ArrayList.class);
      enhancer.setCallback(NoOp.INSTANCE);
      Class<?> clazz = enhancer.create().getClass();
      assertThat(clazz.getName()).contains("$");
      assertSame(ArrayList.class, ReflectionUtils.getNormalClass(clazz));
    }
  }

  /**
   * Test for {@link ReflectionUtils#toString(java.lang.reflect.Method)}.
   */
  public void test_toString_forMethod() throws Exception {
    // "normal" Class
    {
      Method method = ReflectionUtils.getMethodBySignature(ArrayList.class, "size()");
      assertEquals("public int java.util.ArrayList.size()", ReflectionUtils.toString(method));
    }
    // "enchanced" Class
    {
      Class<?> clazz;
      {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ArrayList.class);
        enhancer.setStrategy(new DefaultGeneratorStrategy() {
          private final ClassTransformer t = new ClassEmitterTransformer() {
            @Override
            public void begin_class(int version,
                int access,
                String className,
                net.sf.cglib.asm.Type superType,
                net.sf.cglib.asm.Type[] interfaces,
                String sourceFile) {
              super.begin_class(version, access, className, superType, interfaces, sourceFile);
              CodeEmitter emitter =
                  begin_method(Opcodes.ACC_PUBLIC, new Signature("__foo__",
                      net.sf.cglib.asm.Type.VOID_TYPE,
                      new net.sf.cglib.asm.Type[]{}), null);
              emitter.return_value();
              emitter.end_method();
            }
          };

          @Override
          protected ClassGenerator transform(ClassGenerator cg) throws Exception {
            return new TransformingClassGenerator(cg, t);
          }
        });
        enhancer.setCallback(new MethodInterceptor() {
          public Object intercept(Object obj,
              java.lang.reflect.Method method,
              Object[] args,
              MethodProxy proxy) throws Throwable {
            return proxy.invokeSuper(obj, args);
          }
        });
        clazz = enhancer.create().getClass();
      }
      // method "size()" was done by CGLib, but exists in ArrayList, so method from ArrayList returned
      {
        Method method = ReflectionUtils.getMethodBySignature(clazz, "size()");
        assertThat(method.toString()).contains("$");
        assertEquals("public int java.util.ArrayList.size()", ReflectionUtils.toString(method));
      }
      // method "__foo__" was generated only in CGLib, so no other method to return
      {
        Method method = ReflectionUtils.getMethodBySignature(clazz, "__foo__()");
        String usualToString = method.toString();
        assertThat(usualToString).contains("$");
        assertEquals(usualToString, ReflectionUtils.toString(method));
      }
    }
  }

  /**
   * Test for {@link ReflectionUtils#getShortConstructorString(Constructor)}.
   */
  public void test_getShortConstructorString() throws Exception {
    {
      Constructor<?> constructor = null;
      assertEquals("<null-constructor>", ReflectionUtils.getShortConstructorString(constructor));
    }
    {
      Constructor<?> constructor =
          ReflectionUtils.getConstructorBySignature(ArrayList.class, "<init>(int)");
      assertEquals("ArrayList(int)", ReflectionUtils.getShortConstructorString(constructor));
    }
    {
      Constructor<?> constructor =
          ReflectionUtils.getConstructorBySignature(String.class, "<init>(byte[],java.lang.String)");
      assertEquals("String(byte[],String)", ReflectionUtils.getShortConstructorString(constructor));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // invokeMethod2
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_invokeMethod2() throws Exception {
    Object myObject = new Object() {
      {
        method_0();
        method_1(0);
        method_2(0, 1);
        method_3(0, 1, 2);
        method_4(0, 1, 2, 3);
      }

      public int method_0() {
        return 0;
      }

      public int method_1(int a) {
        return 1;
      }

      public int method_2(int a, int b) {
        return 2;
      }

      public int method_3(int a, int b, int c) {
        return 3;
      }

      public int method_4(int a, int b, int c, int d) {
        return 4;
      }
    };
    // use variant with array of parameter types
    {
      Class<?>[] types = new Class<?>[]{int.class, int.class};
      Object[] values = new Object[]{0, 0};
      assertEquals(2, ReflectionUtils.invokeMethod2(myObject, "method_2", types, values));
    }
    // use variants with parameter types (0, 1, 2, 3 of them)
    assertEquals(0, ReflectionUtils.invokeMethod2(myObject, "method_0"));
    assertEquals(1, ReflectionUtils.invokeMethod2(myObject, "method_1", int.class, 0));
    assertEquals(2, ReflectionUtils.invokeMethod2(myObject, "method_2", int.class, int.class, 0, 0));
    assertEquals(3, ReflectionUtils.invokeMethod2(
        myObject,
        "method_3",
        int.class,
        int.class,
        int.class,
        0,
        0,
        0));
    assertEquals(4, ReflectionUtils.invokeMethod2(
        myObject,
        "method_4",
        int.class,
        int.class,
        int.class,
        int.class,
        0,
        0,
        0,
        0));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // invokeMethod
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_invokeMethod() throws Exception {
    assertEquals(0, ReflectionUtils.invokeMethod(Lists.newArrayList(), "size()"));
  }

  public void test_invokeMethod_static() throws Exception {
    assertSame(
        Collections.EMPTY_LIST,
        ReflectionUtils.invokeMethod(Collections.class, "emptyList()"));
  }

  public void test_invokeMethod_notFound() throws Exception {
    try {
      assertEquals(0, ReflectionUtils.invokeMethod(Lists.newArrayList(), "size2()"));
      fail();
    } catch (AssertionFailedException e) {
    }
  }

  /**
   * Test that we extract real {@link Exception} from wrapper {@link InvocationTargetException}.
   */
  public void test_invokeMethod_throw_InvocationTargetException() throws Exception {
    try {
      ReflectionUtils.invokeMethod(Collections.EMPTY_LIST, "add(java.lang.Object)", this);
      fail();
    } catch (UnsupportedOperationException e) {
    }
  }

  /**
   * Test that {@link RuntimeException} is extracted from wrapper {@link InvocationTargetException}
   * and then thrown as is.
   */
  public void test_invokeMethod_throwErrorAsIs() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      void throwException() {
        throw new IllegalStateException();
      }
    }
    try {
      ReflectionUtils.invokeMethod(new Foo(), "throwException()");
      fail();
    } catch (IllegalStateException e) {
    }
  }

  public void test_invokeMethodEx_noException() {
    assertEquals(0, ReflectionUtils.invokeMethodEx(Lists.newArrayList(), "size()"));
  }

  /**
   * Test that @link Exception} is extracted from wrapper {@link InvocationTargetException} and then
   * thrown as is.
   */
  public void test_invokeMethodEx_throwExceptionAsIs() {
    @SuppressWarnings("unused")
    class Foo {
      void throwException() throws Exception {
        throw new Exception("Bar");
      }
    }
    try {
      ReflectionUtils.invokeMethodEx(new Foo(), "throwException()");
      fail();
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(Exception.class);
      assertEquals("Bar", e.getMessage());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getConstructorBySignature()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getConstructorBySignature_1() throws Exception {
    assertNotNull(ReflectionUtils.getConstructorBySignature(ArrayList.class, "<init>()"));
  }

  public void test_getConstructorBySignature_2() throws Exception {
    assertNotNull(ReflectionUtils.getConstructorBySignature(ArrayList.class, "<init>(int)"));
  }

  public void test_getConstructorBySignature_notFound() throws Exception {
    assertNull(ReflectionUtils.getConstructorBySignature(ArrayList.class, "<init>(long)"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getConstructorByGenericSignature()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getConstructorByGenericSignature() throws Exception {
    @SuppressWarnings("unused")
    class Foo<E> {
      public Foo(E e, String s) {
      }
    }
    // match
    {
      Constructor<?> constructor =
          ReflectionUtils.getConstructorByGenericSignature(Foo.class, "<init>(E,java.lang.String)");
      assertNotNull(constructor);
    }
    // Integer != String
    {
      Constructor<?> constructor =
          ReflectionUtils.getConstructorByGenericSignature(Foo.class, "<init>(E,java.lang.Integer)");
      assertNull(constructor);
    }
  }

  public void test_getConstructorByGenericSignature_array() throws Exception {
    @SuppressWarnings("unused")
    class Foo<E> {
      public Foo(E[] e, String s) {
      }
    }
    // match
    {
      Constructor<?> constructor =
          ReflectionUtils.getConstructorByGenericSignature(
              Foo.class,
              "<init>(E[],java.lang.String)");
      assertNotNull(constructor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getConstructor() - by types
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getConstructor_byTypes_noParameters() throws Exception {
    assertNotNull(ReflectionUtils.getConstructor(ArrayList.class));
  }

  public void test_getConstructor_byTypes_withParameters() throws Exception {
    assertNotNull(ReflectionUtils.getConstructor(ArrayList.class, int.class));
  }

  public void test_getConstructor_byTypes_notFound() throws Exception {
    assertNull(ReflectionUtils.getConstructor(ArrayList.class, long.class));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getConstructorForArguments()
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unused")
  private static class Foo_getConstructorForArguments {
    public Foo_getConstructorForArguments(String a, Integer b) {
    }

    public Foo_getConstructorForArguments(int a) {
    }
  }

  /**
   * Test for {@link ReflectionUtils#getConstructorForArguments(Class, Object...)}.
   */
  public void test_getConstructorForArguments() throws Exception {
    // wrong number of arguments
    {
      Constructor<?> constructor =
          ReflectionUtils.getConstructorForArguments(Foo_getConstructorForArguments.class, "a");
      assertNull(constructor);
    }
    // incompatible arguments
    {
      Constructor<?> constructor =
          ReflectionUtils.getConstructorForArguments(Foo_getConstructorForArguments.class, "a", "b");
      assertNull(constructor);
    }
    // compatible arguments
    {
      Constructor<?> constructor =
          ReflectionUtils.getConstructorForArguments(
              Foo_getConstructorForArguments.class,
              "a",
              new Integer(1));
      assertNotNull(constructor);
    }
    // compatible arguments, but parameter type is primitive "int"
    {
      Constructor<?> constructor =
          ReflectionUtils.getConstructorForArguments(
              Foo_getConstructorForArguments.class,
              new Integer(1));
      assertNotNull(constructor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // equals(Constructor, Constructor)
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_equalsConstructor_sameConstructor() throws Exception {
    Constructor<?> constructor = ReflectionUtils.getConstructor(ArrayList.class, int.class);
    assertSame(constructor, constructor);
    assertTrue(ReflectionUtils.equals(constructor, constructor));
  }

  public void test_equalsConstructor_sameClass_sameSignature() throws Exception {
    Constructor<?> constructor_1 = ReflectionUtils.getConstructor(ArrayList.class, int.class);
    Constructor<?> constructor_2 = ReflectionUtils.getConstructor(ArrayList.class, int.class);
    assertNotSame(constructor_1, constructor_2);
    assertTrue(ReflectionUtils.equals(constructor_1, constructor_2));
  }

  public void test_equalsConstructor_differentClass_sameSignature() throws Exception {
    Constructor<?> constructor_1 = ReflectionUtils.getConstructor(Vector.class, int.class);
    Constructor<?> constructor_2 = ReflectionUtils.getConstructor(ArrayList.class, int.class);
    assertFalse(ReflectionUtils.equals(constructor_1, constructor_2));
  }

  public void test_equalsConstructor_sameClass_differentSignature() throws Exception {
    Constructor<?> constructor_1 = ReflectionUtils.getConstructor(ArrayList.class);
    Constructor<?> constructor_2 = ReflectionUtils.getConstructor(ArrayList.class, int.class);
    assertFalse(ReflectionUtils.equals(constructor_1, constructor_2));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getShortestConstructor()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Used it test.
   */
  public static class Class_getShortestConstructor {
    public Class_getShortestConstructor(int a, int b) {
    }

    public Class_getShortestConstructor(int a) {
    }
  }

  /**
   * Test for {@link ReflectionUtils#getShortestConstructor(Class)}.
   */
  public void test_getShortestConstructor() throws Exception {
    Class<Class_getShortestConstructor> clazz = Class_getShortestConstructor.class;
    // check that longer constructor is before shorter
    {
      Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      assertThat(constructors[0].getParameterTypes()).hasSize(2);
      assertThat(constructors[1].getParameterTypes()).hasSize(1);
    }
    // do test
    {
      Constructor<?> constructor = ReflectionUtils.getShortestConstructor(clazz);
      assertThat(constructor.getParameterTypes()).hasSize(1);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFields()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#getFields(Class)}.
   */
  public void test_getFields() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      public int a;
      protected int b;
      private int c;
      int d;
    }
    @SuppressWarnings("unused")
    class Bar extends Foo {
      public int e;
    }
    List<Field> fields = ReflectionUtils.getFields(Bar.class);
    assertThat(fields).contains(
        Foo.class.getDeclaredField("a"),
        Foo.class.getDeclaredField("b"),
        Foo.class.getDeclaredField("c"),
        Foo.class.getDeclaredField("d"),
        Bar.class.getDeclaredField("e"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFieldByName
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getFieldByName_public_static() throws Exception {
    assertNotNull(ReflectionUtils.getFieldByName(Collections.class, "EMPTY_LIST"));
  }

  public void test_getFieldByName_private_super() throws Exception {
    assertNotNull(ReflectionUtils.getFieldByName(ArrayList.class, "modCount"));
  }

  public void test_getFieldByName_fromInterface() throws Exception {
    assertNotNull(ReflectionUtils.getFieldByName(JFrame.class, "HIDE_ON_CLOSE"));
  }

  public void test_getFieldByName_notFound() throws Exception {
    assertNull(ReflectionUtils.getFieldByName(Collections.class, "EMPTY_LIST_NO"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFieldObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getFieldObject() throws Exception {
    assertEquals(0, ReflectionUtils.getFieldObject(Lists.newArrayList(), "size"));
  }

  public void test_getFieldObject_static() throws Exception {
    assertSame(
        Collections.EMPTY_LIST,
        ReflectionUtils.getFieldObject(Collections.class, "EMPTY_LIST"));
  }

  public void test_getFieldObject_notFound() throws Exception {
    try {
      ReflectionUtils.getFieldObject(Object.class, "no-such-field");
      fail();
    } catch (Throwable e) {
      assertInstanceOf(IllegalArgumentException.class, DesignerExceptionUtils.getRootCause(e));
    }
  }

  public void test_getFieldString() throws Exception {
    class Foo {
      String m_value;
    }
    //
    Foo foo = new Foo();
    foo.m_value = "some value";
    assertEquals("some value", foo.m_value);
    //
    String fieldString = ReflectionUtils.getFieldString(foo, "m_value");
    assertEquals("some value", fieldString);
  }

  public void test_getFieldShort() throws Exception {
    class A {
      short m_value = (short) 123;
    }
    A foo = new A();
    assertEquals(foo.m_value, ReflectionUtils.getFieldShort(foo, "m_value"));
  }

  public void test_getFieldInt() throws Exception {
    assertEquals(0, ReflectionUtils.getFieldInt(Lists.newArrayList(), "size"));
  }

  public void test_getFieldLong() throws Exception {
    class A {
      long field = 555;
    }
    A a = new A();
    assertEquals(a.field, ReflectionUtils.getFieldLong(a, "field"));
  }

  public void test_getFieldFloat() throws Exception {
    assertEquals(
        Component.LEFT_ALIGNMENT,
        ReflectionUtils.getFieldFloat(Component.class, "LEFT_ALIGNMENT"),
        0.001);
  }

  public void test_getFieldBoolean() throws Exception {
    assertTrue(ReflectionUtils.getFieldBoolean(new JButton(), "paintBorder"));
  }

  public void test_setField() throws Exception {
    class Foo {
      String field;
    }
    Foo foo = new Foo();
    // no value initially
    assertSame(null, foo.field);
    // set value
    String s = "string";
    ReflectionUtils.setField(foo, "field", s);
    assertSame(s, foo.field);
  }

  public void test_setField_exception() throws Exception {
    @SuppressWarnings("unused")
    class Foo {
      String field;
    }
    Foo foo = new Foo();
    // try to set Object, fails
    try {
      Object invalidValue = new Object();
      ReflectionUtils.setField(foo, "field", invalidValue);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exception
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#propagate(Throwable)}.
   */
  public void test_propagate() throws Exception {
    // when we throw Exception, it is thrown as is
    {
      Throwable toThrow = new Exception();
      try {
        ReflectionUtils.propagate(toThrow);
      } catch (Throwable e) {
        assertSame(toThrow, e);
      }
    }
    // when we throw Error, it is thrown as is
    {
      Throwable toThrow = new Error();
      try {
        ReflectionUtils.propagate(toThrow);
      } catch (Throwable e) {
        assertSame(toThrow, e);
      }
    }
    // coverage: for return from propagate()
    {
      String key = "wbp.ReflectionUtils.propagate().forceReturn";
      System.setProperty(key, "true");
      try {
        Throwable toThrow = new Exception();
        Throwable result = ReflectionUtils.propagate(toThrow);
        assertSame(null, result);
      } finally {
        System.clearProperty(key);
      }
    }
    // coverage: for InstantiationException
    {
      String key = "wbp.ReflectionUtils.propagate().InstantiationException";
      System.setProperty(key, "true");
      try {
        Throwable toThrow = new Exception();
        Throwable result = ReflectionUtils.propagate(toThrow);
        assertSame(null, result);
      } finally {
        System.clearProperty(key);
      }
    }
    // coverage: for InstantiationException
    {
      String key = "wbp.ReflectionUtils.propagate().IllegalAccessException";
      System.setProperty(key, "true");
      try {
        Throwable toThrow = new Exception();
        Throwable result = ReflectionUtils.propagate(toThrow);
        assertSame(null, result);
      } finally {
        System.clearProperty(key);
      }
    }
  }

  /**
   * Test for {@link ReflectionUtils#getExceptionToThrow(Throwable)}.
   */
  public void test_getExceptionToThrow() throws Exception {
    {
      Throwable e = new Exception();
      Exception toThrow = ReflectionUtils.getExceptionToThrow(e);
      assertSame(e, toThrow);
    }
    {
      Throwable e = new Error();
      Exception toThrow = ReflectionUtils.getExceptionToThrow(e);
      assertNotSame(e, toThrow);
      assertSame(e, toThrow.getCause());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getClassByName
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getClassByName() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    // check primitive classes
    Class<?>[] primitiveClasses =
        {
            boolean.class,
            byte.class,
            char.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class};
    for (Class<?> primitiveClass : primitiveClasses) {
      assertSame(
          primitiveClass,
          ReflectionUtils.getClassByName(classLoader, primitiveClass.getName()));
    }
    // check object
    assertSame(List.class, ReflectionUtils.getClassByName(classLoader, "java.util.List"));
    // check array
    assertSame(int[].class, ReflectionUtils.getClassByName(classLoader, "int[]"));
    assertSame(String[].class, ReflectionUtils.getClassByName(classLoader, "java.lang.String[]"));
    assertSame(boolean[][].class, ReflectionUtils.getClassByName(classLoader, "boolean[][]"));
    assertSame(
        Double[][][].class,
        ReflectionUtils.getClassByName(classLoader, "java.lang.Double[][][]"));
  }

  public void test_hasClass() throws Exception {
    Class<?> thisClass = getClass();
    ClassLoader classLoader = thisClass.getClassLoader();
    // has this Class
    assertTrue(ReflectionUtils.hasClass(classLoader, thisClass.getName()));
    // no invalid class
    assertFalse(ReflectionUtils.hasClass(classLoader, "no.such.Class"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getDefaultValue()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getDefaultValue() throws Exception {
    assertNull(ReflectionUtils.getDefaultValue((String) null));
    assertNull(ReflectionUtils.getDefaultValue(""));
    assertNull(ReflectionUtils.getDefaultValue("java.lang.String"));
    assertNull(ReflectionUtils.getDefaultValue("java.util.ArrayList"));
    assertEquals(false, ReflectionUtils.getDefaultValue("boolean"));
    assertEquals((byte) 0, ReflectionUtils.getDefaultValue("byte"));
    assertEquals((char) 0, ReflectionUtils.getDefaultValue("char"));
    assertEquals((short) 0, ReflectionUtils.getDefaultValue("short"));
    assertEquals(0, ReflectionUtils.getDefaultValue("int"));
    assertEquals(0L, ReflectionUtils.getDefaultValue("long"));
    assertEquals(0.0f, ReflectionUtils.getDefaultValue("float"));
    assertEquals(0.0, ReflectionUtils.getDefaultValue("double"));
  }

  /**
   * Test for {@link ReflectionUtils#getDefaultValue(Class)}.
   */
  public void test_getDefaultValue_byClass() throws Exception {
    // primitives
    assertEquals(false, ReflectionUtils.getDefaultValue(boolean.class));
    assertEquals((byte) 0, ReflectionUtils.getDefaultValue(byte.class));
    assertEquals((char) 0, ReflectionUtils.getDefaultValue(char.class));
    assertEquals((short) 0, ReflectionUtils.getDefaultValue(short.class));
    assertEquals(0, ReflectionUtils.getDefaultValue(int.class));
    assertEquals(0L, ReflectionUtils.getDefaultValue(long.class));
    assertEquals(0.0f, ReflectionUtils.getDefaultValue(float.class));
    assertEquals(0.0, ReflectionUtils.getDefaultValue(double.class));
    // String
    assertEquals("<dynamic>", ReflectionUtils.getDefaultValue(java.lang.String.class));
    // collections
    {
      List<?> o = (List<?>) ReflectionUtils.getDefaultValue(java.util.ArrayList.class);
      assertThat(o).isEmpty();
    }
    {
      Set<?> o = (Set<?>) ReflectionUtils.getDefaultValue(java.util.HashSet.class);
      assertThat(o).isEmpty();
    }
    {
      Map<?, ?> o = (Map<?, ?>) ReflectionUtils.getDefaultValue(java.util.HashMap.class);
      assertThat(o).isEmpty();
    }
    // arbitrary Object
    assertEquals(null, ReflectionUtils.getDefaultValue(System.class));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getPropertyDescriptors()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * For standard Swing component - {@link JButton}.
   */
  public void test_getPropertyDescriptors_standardSwing() throws Exception {
    assertHasProperties(JButton.class, "enabled", "text");
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * For non-standard Swing component.
   */
  public void test_getPropertyDescriptors_nonStandardSwing() throws Exception {
    class MyButton extends JButton {
      private static final long serialVersionUID = 0L;
    }
    assertHasProperties(MyButton.class, "enabled", "text");
  }

  private interface I_tmp_Component {
    void setEnabled(boolean enabled);
  }
  private interface I_tmp_Button extends I_tmp_Component {
    void setText(String text);
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * For interfaces.
   */
  public void test_getPropertyDescriptors_forInterface() throws Exception {
    assertHasProperties(I_tmp_Button.class, "enabled", "text");
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * Different types for getter and setter.
   */
  public void test_getPropertyDescriptors_differentTypesGetterSetter() throws Exception {
    @SuppressWarnings("unused")
    class MyButton extends JButton {
      private static final long serialVersionUID = 0L;

      public int getFoo() {
        return 0;
      }

      public void setFoo(boolean b) {
      }
    }
    // check properties
    Map<String, PropertyDescriptor> propertiesMap = getPropertyDescriptorNames(MyButton.class);
    Set<String> names = propertiesMap.keySet();
    // setFoo() and getFoo() have different types, so different properties
    assertThat(names).contains("foo(boolean)", "foo(int)");
    // but usual JButton properties exist
    assertThat(names).contains("enabled", "text");
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * Two setters with same method name, but different parameter types.
   */
  public void test_getPropertyDescriptors_twoSettersWithSameName() throws Exception {
    @SuppressWarnings("unused")
    class MyButton extends JPanel {
      private static final long serialVersionUID = 0L;

      public void setText(String[] s) {
      }

      public void setText(String s) {
      }
    }
    // check properties
    assertHasProperties(MyButton.class, "text(java.lang.String)", "text(java.lang.String[])");
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * Two setters with same method name, but different parameter types.
   */
  public void test_getPropertyDescriptors_twoSettersWithCommonNamePrefix() throws Exception {
    @SuppressWarnings("unused")
    class MyButton extends JPanel {
      private static final long serialVersionUID = 0L;

      public void setEn(boolean b) {
      }

      public void setEna(boolean b) {
      }
    }
    // check properties
    assertHasProperties(MyButton.class, "en", "ena");
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * Public getter and protected setter.
   */
  public void test_getPropertyDescriptors_publicGetterProtectedSetter() throws Exception {
    @SuppressWarnings("unused")
    class MyButton extends JPanel {
      private static final long serialVersionUID = 0L;

      public String getTitle() {
        return null;
      }

      protected void setTitle(String s) {
      }
    }
    // check properties
    {
      Set<String> names = getPropertyDescriptorNames(MyButton.class).keySet();
      assertThat(names).contains("title");
      assertThat(names).doesNotContain("title(java.lang.String)");
    }
    // both getter and setter should be accessible
    {
      PropertyDescriptor descriptor = getPropertyDescriptorNames(MyButton.class).get("title");
      assertNotNull(descriptor);
      assertNotNull(descriptor.getReadMethod());
      assertNotNull(descriptor.getWriteMethod());
    }
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * Protected getter and public setter.
   */
  public void test_getPropertyDescriptors_protectedGetterPublicSetter() throws Exception {
    @SuppressWarnings("unused")
    class MyButton extends JPanel {
      private static final long serialVersionUID = 0L;

      protected String getTitle() {
        return null;
      }

      public void setTitle(String s) {
      }
    }
    // check properties
    {
      Set<String> names = getPropertyDescriptorNames(MyButton.class).keySet();
      assertThat(names).contains("title");
      assertThat(names).doesNotContain("title(java.lang.String)");
    }
    // both getter and setter should be accessible
    {
      PropertyDescriptor descriptor = getPropertyDescriptorNames(MyButton.class).get("title");
      assertNotNull(descriptor);
      assertNotNull(descriptor.getReadMethod());
      assertNotNull(descriptor.getWriteMethod());
    }
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * Protected methods and IBM (not really) Java.
   */
  public void test_getPropertyDescriptors_protectedMethodsWithIBM() throws Exception {
    @SuppressWarnings("unused")
    class MyButton extends JPanel {
      private static final long serialVersionUID = 0L;

      protected String getA() {
        return null;
      }

      protected void setA(String s) {
      }
    }
    // check properties, not IBM
    {
      Set<String> names = getPropertyDescriptorNames(MyButton.class).keySet();
      assertThat(names).contains("enabled", "a");
    }
    // check properties, as if in IBM
    {
      EnvironmentUtils.setForcedIBM(true);
      ReflectionUtils.flushPropertyDescriptorsCache(MyButton.class);
      try {
        Set<String> names = getPropertyDescriptorNames(MyButton.class).keySet();
        assertThat(names).contains("enabled").doesNotContain("a");
      } finally {
        EnvironmentUtils.setForcedIBM(false);
      }
    }
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * Method with name <code>"get"</code>, without any following property name. Should be ignored.
   */
  public void test_getPropertyDescriptors_pureGetName() throws Exception {
    @SuppressWarnings("unused")
    class MyButton extends JButton {
      private static final long serialVersionUID = 0L;

      public int get() {
        return 0;
      }
    }
    // check properties
    Map<String, PropertyDescriptor> propertiesMap = getPropertyDescriptorNames(MyButton.class);
    Set<String> names = propertiesMap.keySet();
    // no property for "get()"
    assertThat(names).doesNotContain("");
    // but usual JButton properties exist
    assertThat(names).contains("enabled", "text");
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * Getter method that returns <code>void</code>.
   */
  public void test_getPropertyDescriptors_voidGetter() throws Exception {
    @SuppressWarnings("unused")
    class MyButton extends JButton {
      private static final long serialVersionUID = 0L;

      public void getFoo() {
      }
    }
    // check properties
    Map<String, PropertyDescriptor> propertiesMap = getPropertyDescriptorNames(MyButton.class);
    Set<String> names = propertiesMap.keySet();
    // no property for "getFoo()"
    assertThat(names).doesNotContain("foo");
    // but usual JButton properties exist
    assertThat(names).contains("enabled", "text");
  }

  // XXX
  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.
   * <p>
   * When we try to use "bridge" method during {@link PropertyDescriptor} creation, this causes
   * exception under OpenJDK 6 and 7.
   */
  public void test_getPropertyDescriptors_whenBridgeMethod() throws Exception {
    @SuppressWarnings({"unused"})
    class GenericClass<T> {
      public T getFoo() {
        return null;
      }

      public void setFoo(T value) {
      }
    }
    class SpecificClass extends GenericClass<String> {
      @Override
      public String getFoo() {
        return null;
      }
    }
    // prepare PropertyDescriptor-s
    Map<String, PropertyDescriptor> descriptors = getPropertyDescriptorNames(SpecificClass.class);
    // check "foo(java.lang.Object)"
    PropertyDescriptor propertyDescriptor;
    if (SystemUtils.JAVA_VERSION_FLOAT < 1.7f) {
      propertyDescriptor = descriptors.get("foo(java.lang.Object)");
    } else {
      propertyDescriptor = descriptors.get("foo");
    }
    assertNotNull(propertyDescriptor);
    assertSame(Object.class, propertyDescriptor.getPropertyType());
  }

  @SuppressWarnings({"unused", "serial"})
  private static class MyButton_getPropertyDescriptors_ignoreStaticSetters extends JButton {
    public static void setFoo(int value) {
    }
  }

  /**
   * Test for {@link ReflectionUtils#getPropertyDescriptors(BeanInfo, Class)}.<br>
   * Ignore static "set" methods.
   */
  public void test_getPropertyDescriptors_ignoreStaticSetters() throws Exception {
    // check properties
    Map<String, PropertyDescriptor> propertiesMap =
        getPropertyDescriptorNames(MyButton_getPropertyDescriptors_ignoreStaticSetters.class);
    Set<String> names = propertiesMap.keySet();
    // no property for "setFoo()"
    assertThat(names).doesNotContain("foo");
    // but usual JButton properties exist
    assertThat(names).contains("enabled", "text");
  }

  /**
   * Asserts that given {@link Class} has {@link PropertyDescriptor}'s with given names.
   */
  private static void assertHasProperties(Class<?> clazz, String... expectedNames) throws Exception {
    List<PropertyDescriptor> descriptors = getPropertyDescriptors(clazz);
    // prepare names/setters of all PropertyDescriptor's
    List<String> propertyNames = Lists.newArrayList();
    List<Method> propertySetters = Lists.newArrayList();
    for (PropertyDescriptor descriptor : descriptors) {
      propertyNames.add(descriptor.getName());
      if (descriptor.getWriteMethod() != null) {
        propertySetters.add(descriptor.getWriteMethod());
      }
    }
    // no duplicates, please
    assertThat(propertyNames).doesNotHaveDuplicates();
    assertThat(propertySetters).doesNotHaveDuplicates();
    // assert expected names
    assertThat(propertyNames).contains(expectedNames);
  }

  /**
   * @return the {@link Map} of names for all {@link PropertyDescriptor}'s of given {@link Class}.
   */
  private static Map<String, PropertyDescriptor> getPropertyDescriptorNames(Class<?> clazz)
      throws Exception {
    List<PropertyDescriptor> descriptors = getPropertyDescriptors(clazz);
    return getPropertyDescriptorNames(descriptors);
  }

  /**
   * @return the {@link Map} of names for all {@link PropertyDescriptor}'s of given {@link Class}.
   */
  private static Map<String, PropertyDescriptor> getPropertyDescriptorNames(List<PropertyDescriptor> descriptors)
      throws Exception {
    Map<String, PropertyDescriptor> propertiesMap = Maps.newTreeMap();
    for (PropertyDescriptor propertyDescriptor : descriptors) {
      propertiesMap.put(propertyDescriptor.getName(), propertyDescriptor);
    }
    return propertiesMap;
  }

  private static List<PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) throws Exception {
    BeanInfo beanInfo = ReflectionUtils.getBeanInfo(clazz);
    return ReflectionUtils.getPropertyDescriptors(beanInfo, clazz);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class-related
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ReflectionUtils#isSuccessorOf(Class, String)}.
   */
  public void test_isSuccessorOf() throws Exception {
    assertTrue(ReflectionUtils.isSuccessorOf(List.class, "java.util.List"));
    assertTrue(ReflectionUtils.isSuccessorOf(List.class, "java.util.Collection"));
    assertTrue(ReflectionUtils.isSuccessorOf(ArrayList.class, "java.util.List"));
    assertFalse(ReflectionUtils.isSuccessorOf(Map.class, "java.util.Collection"));
    assertFalse(ReflectionUtils.isSuccessorOf(List.class, "no.such.Class"));
  }

  /**
   * Test for {@link ReflectionUtils#isAssignableFrom(Class, Object)}.
   */
  public void test_isAssignableFrom() throws Exception {
    assertTrue(ReflectionUtils.isAssignableFrom(Object.class, new Object()));
    assertTrue(ReflectionUtils.isAssignableFrom(Object.class, "string"));
    assertTrue(ReflectionUtils.isAssignableFrom(String.class, "string"));
    assertFalse(ReflectionUtils.isAssignableFrom(Integer.class, "string"));
    assertFalse(ReflectionUtils.isAssignableFrom(String.class, new Object()));
    // 'null'
    assertTrue(ReflectionUtils.isAssignableFrom(String.class, null));
    assertTrue(ReflectionUtils.isAssignableFrom(Integer.class, null));
    assertFalse(ReflectionUtils.isAssignableFrom(int.class, null));
    // primitives
    assertFalse(ReflectionUtils.isAssignableFrom(int.class, "string"));
    assertTrue(ReflectionUtils.isAssignableFrom(byte.class, Byte.valueOf((byte) 0)));
    assertTrue(ReflectionUtils.isAssignableFrom(char.class, Character.valueOf('0')));
    assertTrue(ReflectionUtils.isAssignableFrom(short.class, Short.valueOf((short) 0)));
    assertTrue(ReflectionUtils.isAssignableFrom(int.class, Integer.valueOf(0)));
    assertTrue(ReflectionUtils.isAssignableFrom(long.class, Long.valueOf(0)));
    assertTrue(ReflectionUtils.isAssignableFrom(float.class, Float.valueOf(0.0f)));
    assertTrue(ReflectionUtils.isAssignableFrom(double.class, Double.valueOf(0.0)));
  }

  /**
   * Test for {@link ReflectionUtils#isSuccessorOf(Object, String)}.
   */
  public void test_isSuccessorOf_Object_String() throws Exception {
    assertFalse(ReflectionUtils.isSuccessorOf((Object) null, "java.lang.Object"));
    // primitives: true
    assertTrue(ReflectionUtils.isSuccessorOf((byte) 0, "byte"));
    assertTrue(ReflectionUtils.isSuccessorOf('0', "char"));
    assertTrue(ReflectionUtils.isSuccessorOf(0, "int"));
    assertTrue(ReflectionUtils.isSuccessorOf((short) 0, "short"));
    assertTrue(ReflectionUtils.isSuccessorOf((long) 0, "long"));
    assertTrue(ReflectionUtils.isSuccessorOf(0.0f, "float"));
    assertTrue(ReflectionUtils.isSuccessorOf(0.0d, "double"));
    // primitives: false
    assertFalse(ReflectionUtils.isSuccessorOf((byte) 0, "int"));
    //
    assertTrue(ReflectionUtils.isSuccessorOf(new Object(), "java.lang.Object"));
    assertFalse(ReflectionUtils.isSuccessorOf(new Object(), "java.lang.String"));
    //
    assertTrue(ReflectionUtils.isSuccessorOf(new String(), "java.lang.Object"));
    assertTrue(ReflectionUtils.isSuccessorOf(new String(), "java.lang.String"));
    //
    assertTrue(ReflectionUtils.isSuccessorOf(Lists.newArrayList(), "java.util.List"));
  }

  /**
   * Test for {@link ReflectionUtils#isMemberClass(Class)}.
   */
  public void test_isMemberClass() throws Exception {
    assertFalse(ReflectionUtils.isMemberClass(Map.class));
    assertTrue(ReflectionUtils.isMemberClass(Map.Entry.class));
    // no check for NoClassDefFoundError
  }

  /**
   * Test for {@link ReflectionUtils#getSuperHierarchy(Class)}.
   */
  public void test_getAllSupertypes() throws Exception {
    abstract class A implements List<String> {
    }
    abstract class B extends A implements Comparable<String> {
    }
    // Object
    {
      List<Class<?>> types = ReflectionUtils.getSuperHierarchy(Object.class);
      assertThat(types).containsExactly(Object.class);
    }
    // A
    {
      List<Class<?>> types = ReflectionUtils.getSuperHierarchy(A.class);
      assertThat(types).containsExactly(A.class, List.class, Object.class);
    }
    // B
    {
      List<Class<?>> types = ReflectionUtils.getSuperHierarchy(B.class);
      assertThat(types).containsExactly(
          B.class,
          Comparable.class,
          A.class,
          List.class,
          Object.class);
    }
  }
}
