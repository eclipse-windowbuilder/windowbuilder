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
package org.eclipse.wb.tests.designer.core.util;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.GenericTypeError;
import org.eclipse.wb.internal.core.utils.GenericTypeResolver;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Iterator;
import java.util.List;

/**
 * Tests for {@link GenericsUtils}.
 * 
 * @author scheglov_ke
 */
public class GenericsUtilsTest extends DesignerTestCase {
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
  // ISelection
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_first() throws Exception {
    Object firstObject = "123";
    // prepare objects
    List<Object> objects;
    {
      objects = Lists.newArrayList();
      objects.add(firstObject);
      objects.add(555);
    }
    ISelection selection = new StructuredSelection(objects);
    //
    assertSame(firstObject, GenericsUtils.first(selection));
    assertSame(firstObject, GenericsUtils.<String>first(selection));
  }

  public void test_iterable() throws Exception {
    // prepare objects
    List<Object> objects;
    {
      objects = Lists.newArrayList();
      objects.add("123");
      objects.add(555);
    }
    ISelection selection = new StructuredSelection(objects);
    Iterable<Object> iterable = GenericsUtils.iterable(selection);
    // iterate
    {
      // prepare iterator
      Iterator<Object> iterator = iterable.iterator();
      // first element
      assertTrue(iterator.hasNext());
      assertSame(objects.get(0), iterator.next());
      // second element
      assertTrue(iterator.hasNext());
      assertSame(objects.get(1), iterator.next());
      // not more elements
      assertFalse(iterator.hasNext());
    }
    // remove unsupported
    {
      Iterator<Object> iterator = iterable.iterator();
      try {
        iterator.remove();
        fail();
      } catch (UnsupportedOperationException e) {
      }
    }
  }

  public void test_iterableSelection() throws Exception {
    // prepare objects
    List<Object> objects;
    {
      objects = Lists.newArrayList();
      objects.add("123");
      objects.add(555);
    }
    // prepare selection provider
    ISelectionProvider selectionProvider;
    {
      final ISelection selection = new StructuredSelection(objects);
      selectionProvider = new ISelectionProvider() {
        public ISelection getSelection() {
          return selection;
        }

        public void setSelection(ISelection _selection) {
        }

        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        }

        public void addSelectionChangedListener(ISelectionChangedListener listener) {
        }
      };
    }
    // get iterable
    Iterable<Object> iterable = GenericsUtils.iterableSelection(selectionProvider);
    // iterate
    {
      // prepare iterator
      Iterator<Object> iterator = iterable.iterator();
      // first element
      assertTrue(iterator.hasNext());
      assertSame(objects.get(0), iterator.next());
      // second element
      assertTrue(iterator.hasNext());
      assertSame(objects.get(1), iterator.next());
      // not more elements
      assertFalse(iterator.hasNext());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arrays
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GenericsUtils#get(Class, Object...)}.
   */
  public void test_get() throws Exception {
    Object[] objects = {"0", 1, 2.2};
    assertEquals("0", GenericsUtils.get(String.class, objects));
    assertEquals(new Integer(1), GenericsUtils.get(Integer.class, objects));
    assertEquals(new Double(2.2), GenericsUtils.get(Double.class, objects));
    assertEquals(null, GenericsUtils.get(Float.class, objects));
  }

  /**
   * Test for {@link GenericsUtils#get(Class, Object...)}.
   */
  public void test_get_fromList() throws Exception {
    List<?> objects = ImmutableList.<Object>of("0", 1, 2.2);
    assertEquals("0", GenericsUtils.get(String.class, objects));
    assertEquals(new Integer(1), GenericsUtils.get(Integer.class, objects));
    assertEquals(new Double(2.2), GenericsUtils.get(Double.class, objects));
    assertEquals(null, GenericsUtils.get(Float.class, objects));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Collections
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_select() throws Exception {
    // prepare objects
    List<Object> objects;
    {
      objects = Lists.newArrayList();
      objects.add("111");
      objects.add("222");
      objects.add(333);
      objects.add(444);
    }
    // select String
    {
      List<String> stringList = GenericsUtils.select(objects, String.class);
      assertEquals(2, stringList.size());
      assertEquals("111", stringList.get(0));
      assertEquals("222", stringList.get(1));
    }
    // select Integer
    {
      List<Integer> integerList = GenericsUtils.select(objects, Integer.class);
      assertEquals(2, integerList.size());
      assertEquals(Integer.valueOf(333), integerList.get(0));
      assertEquals(Integer.valueOf(444), integerList.get(1));
    }
  }

  public void test_cast() throws Exception {
    // prepare objects
    List<Object> objects;
    {
      objects = Lists.newArrayList();
      objects.add("111");
      objects.add("222");
    }
    // get String
    {
      List<String> stringList = GenericsUtils.<String>cast(objects);
      assertThat(stringList).containsExactly("111", "222");
    }
  }

  /**
   * Test for {@link GenericsUtils#asList(Object[], Object)}.<br>
   * With not-<code>null</code> elements.
   */
  public void test_asList_1() throws Exception {
    List<String> list = GenericsUtils.asList(new String[]{"111", "222"}, "333");
    assertEquals(3, list.size());
    assertSame("111", list.get(0));
    assertSame("222", list.get(1));
    assertSame("333", list.get(2));
  }

  /**
   * Test for {@link GenericsUtils#asList(Object[], Object)}.<br>
   * With <code>null</code> elements.
   */
  public void test_asList_2() throws Exception {
    List<String> list = GenericsUtils.asList(null, "111");
    assertEquals(1, list.size());
    assertSame("111", list.get(0));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // singletonList()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GenericsUtils#singletonList(Object)}.
   */
  public void test_singletonList_1() throws Exception {
    assertThat(GenericsUtils.singletonList(null)).isEmpty();
  }

  /**
   * Test for {@link GenericsUtils#singletonList(Object)}.
   */
  public void test_singletonList_2() throws Exception {
    String element = "theElement";
    assertThat(GenericsUtils.singletonList(element)).containsExactly(element);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getPrevOrNull()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GenericsUtils#getPrevOrNull(List, int)}.
   */
  public void test_getPrevOrNull_index() throws Exception {
    List<String> elements = ImmutableList.of("000", "111", "222");
    assertSame(null, GenericsUtils.getPrevOrNull(elements, 0));
    assertSame("000", GenericsUtils.getPrevOrNull(elements, 1));
    assertSame("111", GenericsUtils.getPrevOrNull(elements, 2));
  }

  /**
   * Test for {@link GenericsUtils#getPrevOrNull(List, Object)}.
   */
  public void test_getPrevOrNull_element() throws Exception {
    List<String> elements = ImmutableList.of("000", "111", "222");
    // use element
    assertSame("000", GenericsUtils.getPrevOrNull(elements, "111"));
    assertSame("111", GenericsUtils.getPrevOrNull(elements, "222"));
    assertNull(GenericsUtils.getPrevOrNull(elements, "000"));
    assertNull(GenericsUtils.getPrevOrNull(elements, "no such element"));
  }

  /**
   * Test for {@link GenericsUtils#getPrevOrLast(List, Object)}.
   */
  public void test_getPrevOrLast_element() throws Exception {
    List<String> elements = ImmutableList.of("000", "111", "222");
    // no elements
    assertNull(GenericsUtils.getPrevOrLast(ImmutableList.of(), "no matter"));
    // use element
    assertSame("111", GenericsUtils.getPrevOrLast(elements, "222"));
    assertSame("000", GenericsUtils.getPrevOrLast(elements, "111"));
    assertSame("222", GenericsUtils.getPrevOrLast(elements, "000"));
    assertNull(GenericsUtils.getPrevOrNull(elements, "no such element"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // next/last elements
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GenericsUtils#getNextOrNull(List, int)}.
   */
  public void test_getNextOrNull_index() throws Exception {
    List<String> elements = ImmutableList.of("000", "111", "222");
    // use index
    assertSame("111", GenericsUtils.getNextOrNull(elements, 0));
    assertSame("222", GenericsUtils.getNextOrNull(elements, 1));
    assertNull(GenericsUtils.getNextOrNull(elements, 2));
  }

  /**
   * Test for {@link GenericsUtils#getNextOrNull(List, Object)}.
   */
  public void test_getNextOrNull_element() throws Exception {
    List<String> elements = ImmutableList.of("000", "111", "222");
    // use element
    assertSame("111", GenericsUtils.getNextOrNull(elements, "000"));
    assertSame("222", GenericsUtils.getNextOrNull(elements, "111"));
    assertNull(GenericsUtils.getNextOrNull(elements, "222"));
    assertNull(GenericsUtils.getNextOrNull(elements, "no such element"));
  }

  /**
   * Test for {@link GenericsUtils#getNextOrFirst(List, Object)}.
   */
  public void test_getNextOrFirst_element() throws Exception {
    List<String> elements = ImmutableList.of("000", "111", "222");
    // no elements
    assertNull(GenericsUtils.getNextOrFirst(ImmutableList.of(), "no matter"));
    // use element
    assertSame("111", GenericsUtils.getNextOrFirst(elements, "000"));
    assertSame("222", GenericsUtils.getNextOrFirst(elements, "111"));
    assertSame("000", GenericsUtils.getNextOrFirst(elements, "222"));
    assertNull(GenericsUtils.getNextOrNull(elements, "no such element"));
  }

  /**
   * Test for {@link GenericsUtils#getFirstOrNull(List)}.
   */
  public void test_getFirstOrNull() throws Exception {
    assertNull(GenericsUtils.getFirstOrNull(ImmutableList.of()));
    assertSame("000", GenericsUtils.getFirstOrNull(ImmutableList.of("000", "111", "222")));
  }

  /**
   * Test for {@link GenericsUtils#getLastOrNull(List)}.
   */
  public void test_getLastOrNull() throws Exception {
    assertNull(GenericsUtils.getLastOrNull(ImmutableList.of()));
    assertSame("222", GenericsUtils.getLastOrNull(ImmutableList.of("000", "111", "222")));
  }

  /**
   * Test for {@link GenericsUtils#getLast(List)}.
   */
  public void test_getLast() throws Exception {
    try {
      assertNull(GenericsUtils.getLast(ImmutableList.of()));
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
    assertSame("222", GenericsUtils.getLast(ImmutableList.of("000", "111", "222")));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // areAdjacent()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GenericsUtils#areAdjacent(List, List)}.
   */
  public void test_areAdjacent() throws Exception {
    assertTrue(GenericsUtils.areAdjacent(ImmutableList.of(), ImmutableList.of()));
    assertTrue(GenericsUtils.areAdjacent(ImmutableList.of("a"), ImmutableList.of("a")));
    assertTrue(GenericsUtils.areAdjacent(ImmutableList.of("a", "b", "c"), ImmutableList.of("a")));
    assertTrue(GenericsUtils.areAdjacent(
        ImmutableList.of("a", "b", "c"),
        ImmutableList.of("a", "b")));
    assertTrue(GenericsUtils.areAdjacent(
        ImmutableList.of("a", "b", "c"),
        ImmutableList.of("b", "c")));
    assertFalse(GenericsUtils.areAdjacent(
        ImmutableList.of("a", "b", "c"),
        ImmutableList.of("a", "c")));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enum
  //
  ////////////////////////////////////////////////////////////////////////////
  private static enum MyEnum {
    A, B, C
  }

  /**
   * Test for {@link GenericsUtils#getEnumStrings(Enum...)}.
   */
  public void test_getEnumStrings() throws Exception {
    assertThat(GenericsUtils.getEnumStrings(MyEnum.A, MyEnum.B)).isEqualTo(new String[]{"A", "B"});
  }

  /**
   * Test for {@link GenericsUtils#getEnumValue(String, Enum...)}.
   */
  public void test_getEnumValue() throws Exception {
    assertSame(MyEnum.B, GenericsUtils.getEnumValue("B", MyEnum.A, MyEnum.B, MyEnum.C));
    assertSame(null, GenericsUtils.getEnumValue("B", MyEnum.A));
  }

  /**
   * Test for {@link GenericsUtils#getEnumValues(Class, String...)}.
   */
  public void test_getEnumValues_byString() throws Exception {
    // good
    {
      MyEnum[] expectedValues = new MyEnum[]{MyEnum.A, MyEnum.B};
      MyEnum[] actualValues = GenericsUtils.getEnumValues(MyEnum.class, "A", "B");
      assertThat(actualValues).isEqualTo(expectedValues);
    }
    // bad
    try {
      GenericsUtils.getEnumValues(MyEnum.class, "noSuchElement");
      fail();
    } catch (Throwable e) {
    }
  }

  /**
   * Test for {@link GenericsUtils#getEnumValues(Class, Predicate)}.
   */
  public void test_getEnumValues_filter() throws Exception {
    MyEnum[] expectedValues = new MyEnum[]{MyEnum.B, MyEnum.C};
    MyEnum[] actualValues = GenericsUtils.getEnumValues(MyEnum.class, new Predicate<MyEnum>() {
      public boolean apply(MyEnum t) {
        return t == MyEnum.B || t == MyEnum.C;
      }
    });
    assertThat(actualValues).isEqualTo(expectedValues);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generics and names
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getTypeName_badTypeToResolve() throws Exception {
    try {
      GenericsUtils.getTypeName(GenericTypeResolver.EMPTY, null);
      fail();
    } catch (GenericTypeError e) {
    }
  }

  public void test_getTypeName_noSuchTypeVariable() throws Exception {
    class Foo<A> {
    }
    try {
      TypeVariable<?> typeVariable = Foo.class.getTypeParameters()[0];
      GenericsUtils.getTypeName(GenericTypeResolver.EMPTY, typeVariable);
      fail();
    } catch (GenericTypeError e) {
    }
  }

  public void test_getTypeName_fixed_hasVariable() throws Exception {
    class Foo<A> {
    }
    GenericTypeResolver resolver = GenericTypeResolver.fixed("A", String.class);
    TypeVariable<?> typeVariable = Foo.class.getTypeParameters()[0];
    String actualParameterType = GenericsUtils.getTypeName(resolver, typeVariable);
    assertEquals("java.lang.String", actualParameterType);
  }

  public void test_getTypeName_fixed_noSuchVariable() throws Exception {
    class Foo<B> {
    }
    GenericTypeResolver resolver = GenericTypeResolver.fixed("A", String.class);
    TypeVariable<?> typeVariable = Foo.class.getTypeParameters()[0];
    try {
      GenericsUtils.getTypeName(resolver, typeVariable);
      fail();
    } catch (GenericTypeError e) {
    }
  }

  public void test_getTypeName_Class() throws Exception {
    String actualParameterType = GenericsUtils.getTypeName(GenericTypeResolver.EMPTY, String.class);
    assertEquals("java.lang.String", actualParameterType);
  }

  public void test_getTypeName_innerClass() throws Exception {
    String actualParameterType =
        GenericsUtils.getTypeName(GenericTypeResolver.EMPTY, java.util.Map.Entry.class);
    assertEquals("java.util.Map.Entry", actualParameterType);
  }

  public void test_getTypeName_TypeVariable() throws Exception {
    @SuppressWarnings("unused")
    class Listener<E> {
      void handle(E event) {
      }
    }
    @SuppressWarnings("unused")
    class Widget {
      public void addListener(Listener<String> listener) {
      }
    }
    //
    Type parameterTypeToResolve;
    {
      Method method_handle = ReflectionUtils.getMethod(Listener.class, "handle", Object.class);
      assertNotNull(method_handle);
      parameterTypeToResolve = method_handle.getGenericParameterTypes()[0];
      assertEquals("E", parameterTypeToResolve.toString());
    }
    // prepare resolver
    GenericTypeResolver resolver;
    {
      Method method_addListener =
          ReflectionUtils.getMethod(Widget.class, "addListener", Listener.class);
      resolver =
          GenericTypeResolver.argumentOfMethod(GenericTypeResolver.EMPTY, method_addListener, 0);
    }
    // validate
    String actualParameterType = GenericsUtils.getTypeName(resolver, parameterTypeToResolve);
    assertEquals("java.lang.String", actualParameterType);
  }

  public void test_getTypeName_TypeVariable_deepResolving() throws Exception {
    @SuppressWarnings("unused")
    class Event<E1, E2> {
      E1 value1;
      E2 value2;
    }
    @SuppressWarnings("unused")
    class Listener<L1, L2> {
      void handle(Event<L1, L2> event) {
      }
    }
    @SuppressWarnings("unused")
    class Widget {
      public void addListener(Listener<String, Integer> listener) {
      }
    }
    //
    Type parameterTypeToResolve;
    {
      Method method_handle = ReflectionUtils.getMethod(Listener.class, "handle", Event.class);
      assertNotNull(method_handle);
      parameterTypeToResolve = method_handle.getGenericParameterTypes()[0];
      assertEquals(Event.class.getName() + "<L1, L2>", parameterTypeToResolve.toString());
    }
    // prepare resolver
    GenericTypeResolver resolver;
    {
      Method method_addListener =
          ReflectionUtils.getMethod(Widget.class, "addListener", Listener.class);
      resolver =
          GenericTypeResolver.argumentOfMethod(GenericTypeResolver.EMPTY, method_addListener, 0);
    }
    // validate
    String actualParameterType = GenericsUtils.getTypeName(resolver, parameterTypeToResolve);
    assertEquals(
        Event.class.getName() + "<java.lang.String, java.lang.Integer>",
        actualParameterType);
  }

  /**
   * Value of type variable specified in one of the superclasses.
   */
  public void test_getTypeName_TypeVariable_actualInSuperclass() throws Exception {
    class Event<T> {
    }
    @SuppressWarnings("unused")
    class Listener<L> {
      void handle(Event<L> event) {
      }
    }
    @SuppressWarnings("unused")
    class GenericWidget<V> {
      public void addListener(Listener<V> listener) {
      }
    }
    class ConcreteWidget extends GenericWidget<String> {
    }
    class ConcreteWidget2 extends ConcreteWidget {
    }
    //
    Type parameterTypeToResolve;
    {
      Method method_handle = ReflectionUtils.getMethod(Listener.class, "handle", Event.class);
      assertNotNull(method_handle);
      parameterTypeToResolve = method_handle.getGenericParameterTypes()[0];
      assertEquals(Event.class.getName() + "<L>", parameterTypeToResolve.toString());
    }
    // prepare resolver
    GenericTypeResolver resolver;
    {
      Method method_addListener =
          ReflectionUtils.getMethod(ConcreteWidget2.class, "addListener", Listener.class);
      GenericTypeResolver superClassResolver =
          GenericTypeResolver.superClass(
              GenericTypeResolver.EMPTY,
              ConcreteWidget2.class,
              GenericWidget.class);
      resolver = GenericTypeResolver.argumentOfMethod(superClassResolver, method_addListener, 0);
    }
    // validate
    String actualParameterType = GenericsUtils.getTypeName(resolver, parameterTypeToResolve);
    assertEquals(Event.class.getName() + "<java.lang.String>", actualParameterType);
  }

  public void test_getTypeName_TypeVariable_askParent() throws Exception {
    class Event<E> {
    }
    @SuppressWarnings("unused")
    class Listener<L> {
      void handle(Event<L> event) {
      }
    }
    @SuppressWarnings("unused")
    class GenericWidget<D> {
      public void addListener(Listener<D> listener) {
      }
    }
    class ActualWidget extends GenericWidget<String> {
    }
    // prepare resolver
    Type parameterTypeToResolve;
    GenericTypeResolver resolver;
    {
      Method method_addListener =
          ReflectionUtils.getMethod(GenericWidget.class, "addListener", Listener.class);
      parameterTypeToResolve = method_addListener.getGenericParameterTypes()[0];
      GenericTypeResolver superClassResolver =
          GenericTypeResolver.superClass(
              GenericTypeResolver.EMPTY,
              ActualWidget.class,
              GenericWidget.class);
      resolver = GenericTypeResolver.argumentOfMethod(superClassResolver, method_addListener, 0);
    }
    // validate
    String actualParameterType = GenericsUtils.getTypeName(resolver, parameterTypeToResolve);
    assertEquals(Listener.class.getName() + "<java.lang.String>", actualParameterType);
  }

  interface Inter<E> {
    void setValue(E value);
  }

  /**
   * Generic type used in Inter and declared as type argument in using Super.
   */
  public void test_getTypeName_resolveFromSuperInterface() throws Exception {
    abstract class Super<T> implements Inter<T> {
    }
    abstract class Sub extends Super<String> {
    }
    //
    Type typeToResolve;
    {
      Method method = Inter.class.getDeclaredMethod("setValue", Object.class);
      typeToResolve = method.getGenericParameterTypes()[0];
      assertEquals("E", typeToResolve.toString());
    }
    // prepare resolver
    GenericTypeResolver resolver =
        GenericTypeResolver.superClass(GenericTypeResolver.EMPTY, Sub.class, Inter.class);
    // validate
    String actualParameterType = GenericsUtils.getTypeName(resolver, typeToResolve);
    assertEquals("java.lang.String", actualParameterType);
  }

  /**
   * Generic type used in Super and declared as type argument in using Super.
   */
  public void test_getTypeName_resolveFromSuperClass() throws Exception {
    @SuppressWarnings("unused")
    class Super<T> {
      void setValue(T value) {
      }
    }
    class Sub extends Super<String> {
    }
    //
    Type typeToResolve;
    {
      Method method = Super.class.getDeclaredMethod("setValue", Object.class);
      typeToResolve = method.getGenericParameterTypes()[0];
      assertEquals("T", typeToResolve.toString());
    }
    // prepare resolver
    GenericTypeResolver resolver =
        GenericTypeResolver.superClass(GenericTypeResolver.EMPTY, Sub.class, Super.class);
    // validate
    String actualParameterType = GenericsUtils.getTypeName(resolver, typeToResolve);
    assertEquals("java.lang.String", actualParameterType);
  }

  public void test_getTypeName_TypeVariable_WildcardType() throws Exception {
    @SuppressWarnings("unused")
    class Listener<E> {
      void handle(E event) {
      }
    }
    @SuppressWarnings("unused")
    class Widget {
      public void addListener(Listener<? extends String> listener) {
      }
    }
    //
    Type parameterTypeToResolve;
    {
      Method method_handle = ReflectionUtils.getMethod(Listener.class, "handle", Object.class);
      assertNotNull(method_handle);
      parameterTypeToResolve = method_handle.getGenericParameterTypes()[0];
    }
    // prepare resolver
    GenericTypeResolver resolver;
    {
      Method method_addListener =
          ReflectionUtils.getMethod(Widget.class, "addListener", Listener.class);
      resolver =
          GenericTypeResolver.argumentOfMethod(GenericTypeResolver.EMPTY, method_addListener, 0);
    }
    // validate
    String actualParameterType = GenericsUtils.getTypeName(resolver, parameterTypeToResolve);
    assertEquals("java.lang.String", actualParameterType);
  }

  public void test_getTypeName_TypeVariable_withEnclosingTypeArguments() throws Exception {
    @SuppressWarnings("unused")
    class Listener<E> {
      void handle(E event) {
      }
    }
    @SuppressWarnings("unused")
    class Widget<D> {
      public void addListener(Listener<D> listener) {
      }
    }
    //
    Type parameterTypeToResolve;
    {
      Method method_handle = ReflectionUtils.getMethod(Listener.class, "handle", Object.class);
      assertNotNull(method_handle);
      parameterTypeToResolve = method_handle.getGenericParameterTypes()[0];
    }
    // prepare resolver
    Method method_addListener =
        ReflectionUtils.getMethod(Widget.class, "addListener", Listener.class);
    // validate: String
    {
      GenericTypeResolver parentResolver = GenericTypeResolver.fixed("D", String.class);
      GenericTypeResolver resolver =
          GenericTypeResolver.argumentOfMethod(parentResolver, method_addListener, 0);
      String actualParameterType = GenericsUtils.getTypeName(resolver, parameterTypeToResolve);
      assertEquals("java.lang.String", actualParameterType);
    }
    // validate: Integer
    {
      GenericTypeResolver parentResolver = GenericTypeResolver.fixed("D", Integer.class);
      GenericTypeResolver resolver =
          GenericTypeResolver.argumentOfMethod(parentResolver, method_addListener, 0);
      String actualParameterType = GenericsUtils.getTypeName(resolver, parameterTypeToResolve);
      assertEquals("java.lang.Integer", actualParameterType);
    }
  }

  public void test_getTypeName_TypeVariable_withEnclosingTypeArguments2() throws Exception {
    @SuppressWarnings("unused")
    class Listener<E> {
      void handle(E event) {
      }
    }
    @SuppressWarnings("unused")
    class Widget<D> {
      public void addListener(Listener<D> listener) {
      }
    }
    class Widget_1<X1> extends Widget<X1> {
    }
    class Widget_2<X2> extends Widget_1<X2> {
    }
    //
    Type parameterTypeToResolve;
    {
      Method method_handle = ReflectionUtils.getMethod(Listener.class, "handle", Object.class);
      assertNotNull(method_handle);
      parameterTypeToResolve = method_handle.getGenericParameterTypes()[0];
    }
    // prepare resolver
    Method method_addListener =
        ReflectionUtils.getMethod(Widget.class, "addListener", Listener.class);
    // validate
    GenericTypeResolver parentResolver = GenericTypeResolver.fixed("X2", String.class);
    GenericTypeResolver parentResolver2 =
        GenericTypeResolver.superClass(parentResolver, Widget_2.class, Widget.class);
    GenericTypeResolver resolver =
        GenericTypeResolver.argumentOfMethod(parentResolver2, method_addListener, 0);
    String actualParameterType = GenericsUtils.getTypeName(resolver, parameterTypeToResolve);
    assertEquals("java.lang.String", actualParameterType);
  }
}
