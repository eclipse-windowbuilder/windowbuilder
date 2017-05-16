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
package org.eclipse.wb.internal.core.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Helper with various generics related utilities.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public final class GenericsUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GenericsUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ISelection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the first element of given {@link ISelection}.
   */
  @SuppressWarnings("unchecked")
  public static <T> T first(ISelection selection) {
    IStructuredSelection structuredSelection = (IStructuredSelection) selection;
    return (T) structuredSelection.getFirstElement();
  }

  /**
   * @return the generified {@link Iterable}.
   */
  public static <T> Iterable<T> iterable(ISelection selection) {
    final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          Iterator<?> iterator = structuredSelection.iterator();

          public boolean hasNext() {
            return iterator.hasNext();
          }

          @SuppressWarnings("unchecked")
          public T next() {
            return (T) iterator.next();
          }

          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  /**
   * @return the generified {@link Iterable} for selection in given {@link ISelectionProvider}.
   */
  public static <T> Iterable<T> iterableSelection(ISelectionProvider selectionProvider) {
    ISelection selection = selectionProvider.getSelection();
    return iterable(selection);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arrays
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return first element of required type, <code>null</code> if not found.
   */
  @SuppressWarnings("unchecked")
  public static <T> T get(Class<T> clazz, Object... objects) {
    for (Object object : objects) {
      if (isAssignable(clazz, object)) {
        return (T) object;
      }
    }
    return null;
  }

  /**
   * @return first element of required type, <code>null</code> if not found.
   */
  @SuppressWarnings("unchecked")
  public static <T> T get(Class<T> clazz, List<?> objects) {
    for (Object object : objects) {
      if (isAssignable(clazz, object)) {
        return (T) object;
      }
    }
    return null;
  }

  /**
   * @return <code>true</code> object can be casted to given class.
   */
  private static boolean isAssignable(Class<?> clazz, Object object) {
    return object != null && clazz.isAssignableFrom(object.getClass());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Collections
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} with objects with given class (and its sub-classes).
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> select(Collection<? super T> source, Class<T> clazz) {
    List<T> result = Lists.newArrayList();
    for (Object object : source) {
      if (object != null && clazz.isAssignableFrom(object.getClass())) {
        result.add((T) object);
      }
    }
    return result;
  }

  /**
   * @return the {@link List} with objects cast to given type. Note, that this is unsafe operation.
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> cast(Collection<?> source) {
    List<T> result = Lists.newArrayList();
    for (Object object : source) {
      result.add((T) object);
    }
    return result;
  }

  /**
   * Joins array of elements with one more element.
   *
   * @param elements
   *          the elements, may be <code>null</code>.
   * @param element
   *          the one more element to add to the end.
   *
   * @return the {@link List} that contains given elements plus one more element.
   */
  public static <T> List<T> asList(T[] elements, T element) {
    List<T> list = Lists.newArrayList();
    if (elements != null) {
      for (T t : elements) {
        list.add(t);
      }
    }
    list.add(element);
    return list;
  }

  /**
   * @return the {@link List} that contains given element, if it is not <code>null</code>, or empty
   *         {@link List}.
   */
  public static <T, E extends T> List<T> singletonList(E element) {
    if (element == null) {
      return ImmutableList.of();
    }
    return ImmutableList.<T>of(element);
  }

  /**
   * @return the element with index <code>index - 1</code>, or <code>null</code> if given
   *         <code>index</code> is first one.
   */
  public static <T> T getPrevOrNull(List<T> elements, int index) {
    Assert.isLegal(index >= 0 && index < elements.size());
    if (index > 0) {
      return elements.get(index - 1);
    } else {
      return null;
    }
  }

  /**
   * @return the element before the given one, or <code>null</code> if given element is first one or
   *         is not in the {@link List}.
   */
  public static <T> T getPrevOrNull(List<? extends T> elements, T element) {
    int index = elements.indexOf(element);
    if (index == -1) {
      return null;
    } else {
      return getPrevOrNull(elements, index);
    }
  }

  /**
   * Returns:
   * <ul>
   * <li>if given element is not first, then previous element;</li>
   * <li>if given element is first, then last element of list;</li>
   * <li>if given element is not in list, then last element of list;</li>
   * <li>if list is empty, then <code>null</code>.</li>
   */
  public static <T> T getPrevOrLast(List<? extends T> elements, T element) {
    if (elements.isEmpty()) {
      return null;
    }
    T prev = getPrevOrNull(elements, element);
    if (prev != null) {
      return prev;
    }
    return elements.get(elements.size() - 1);
  }

  /**
   * @return the element with index <code>index + 1</code>, or <code>null</code> if given
   *         <code>index</code> is last one.
   */
  public static <T> T getNextOrNull(List<T> elements, int index) {
    Assert.isLegal(index >= 0 && index < elements.size());
    if (index < elements.size() - 1) {
      return elements.get(index + 1);
    } else {
      return null;
    }
  }

  /**
   * @return the element next to the given one, or <code>null</code> if given element is last one or
   *         is not in the {@link List}.
   */
  public static <T> T getNextOrNull(List<? extends T> elements, T element) {
    int index = elements.indexOf(element);
    if (index == -1) {
      return null;
    } else {
      return getNextOrNull(elements, index);
    }
  }

  /**
   * Returns:
   * <ul>
   * <li>if given element is not last, then next element;</li>
   * <li>if given element is last, then first element of list;</li>
   * <li>if given element is not in list, then first element of list;</li>
   * <li>if list is empty, then <code>null</code>.</li>
   */
  public static <T> T getNextOrFirst(List<? extends T> elements, T element) {
    if (elements.isEmpty()) {
      return null;
    }
    T prev = getNextOrNull(elements, element);
    if (prev != null) {
      return prev;
    }
    return elements.get(0);
  }

  /**
   * @return the first element from {@link List} , or <code>null</code> if {@link List} is empty.
   */
  public static <T> T getFirstOrNull(List<T> elements) {
    return elements.isEmpty() ? null : elements.get(0);
  }

  /**
   * @return the last element from {@link List} , or <code>null</code> if {@link List} is empty.
   */
  public static <T> T getLastOrNull(List<T> elements) {
    return elements.isEmpty() ? null : elements.get(elements.size() - 1);
  }

  /**
   * @return the last element from {@link List} , or fails if {@link List} is empty.
   */
  public static <T> T getLast(List<T> elements) {
    return elements.get(elements.size() - 1);
  }

  /**
   * @return <code>true</code> if given elements are adjacent in all elements {@link List}.
   */
  public static <T> boolean areAdjacent(List<T> allElements, List<T> elements) {
    int prevIndex = -1;
    for (T element : elements) {
      if (prevIndex == -1) {
        prevIndex = allElements.indexOf(element);
      } else {
        int index = allElements.indexOf(element);
        if (index != prevIndex + 1) {
          return false;
        }
        prevIndex = index;
      }
    }
    // OK
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enum
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Enum#toString()} array for each {@link Enum} value.
   */
  public static <T extends Enum<?>> String[] getEnumStrings(T... values) {
    String[] fieldNames = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      T value = values[i];
      fieldNames[i] = value.toString();
    }
    return fieldNames;
  }

  /**
   * @return the {@link Enum#toString()} array for each {@link Enum} value.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Enum<?>> T[] getEnumValues(Class<T> enumClass, String... strings) {
    T[] values = (T[]) Array.newInstance(enumClass, strings.length);
    for (int i = 0; i < strings.length; i++) {
      String s = strings[i];
      T value = getEnumValue(s, enumClass.getEnumConstants());
      Assert.isNotNull(value, "No value for %s in %s.", s, enumClass);
      values[i] = value;
    }
    return values;
  }

  /**
   * @return the {@link Enum} value with same {@link Enum#toString()} presentation, may be
   *         <code>null</code> if no such value.
   */
  public static <T extends Enum<?>> T getEnumValue(String s, T... values) {
    for (T value : values) {
      if (value.toString().equals(s)) {
        return value;
      }
    }
    return null;
  }

  /**
   * @return elements of given {@link Enum} that conform {@link Predicate}.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Enum<?>> T[] getEnumValues(Class<T> enumClass, Predicate<T> predicate) {
    List<T> selectedElements = Lists.newArrayList();
    for (T element : enumClass.getEnumConstants()) {
      if (predicate.apply(element)) {
        selectedElements.add(element);
      }
    }
    return selectedElements.toArray((T[]) Array.newInstance(enumClass, selectedElements.size()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generics and names
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the names of (possibly generic) types.
   */
  public static String[] getTypeNames(GenericTypeResolver resolver, Type[] types) {
    String[] names = new String[types.length];
    for (int i = 0; i < types.length; i++) {
      Type type = types[i];
      names[i] = getTypeName(resolver, type);
    }
    return names;
  }

  /**
   * @return the name of (possibly generic) type.
   */
  public static String getTypeName(GenericTypeResolver resolver, Type type) {
    return resolver.resolve(type);
  }
}
