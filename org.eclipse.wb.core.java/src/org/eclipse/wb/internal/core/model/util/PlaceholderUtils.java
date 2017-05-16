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
package org.eclipse.wb.internal.core.model.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

/**
 * Utils for "placeholders" - components created instead of real ones, when exception happens.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class PlaceholderUtils {
  private static String KEY_EXCEPTIONS = PlaceholderUtils.class.getName() + ".exceptions";
  private static String KEY_PLACEHOLDER = PlaceholderUtils.class.getName() + ".placeholder";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private PlaceholderUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access: JavaInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link JavaInfo} has placeholder object.
   */
  public static boolean isPlaceholder(JavaInfo javaInfo) {
    ASTNode node = javaInfo.getCreationSupport().getNode();
    return isPlaceholder(node);
  }

  /**
   * @return exceptions associated with given {@link JavaInfo}.
   */
  public static List<Throwable> getExceptions(JavaInfo javaInfo) {
    ASTNode node = javaInfo.getCreationSupport().getNode();
    return getExceptions(node);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exceptions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return exceptions associated with given {@link ASTNode}, may be empty, but not
   *         <code>null</code>.
   */
  public static List<Throwable> getExceptions(ASTNode node) {
    List<Throwable> exceptions = getExceptions0(node);
    return exceptions != null ? exceptions : ImmutableList.<Throwable>of();
  }

  /**
   * Removes information associated with given {@link ASTNode}.
   */
  public static void clear(ASTNode node) {
    node.setProperty(KEY_EXCEPTIONS, null);
    node.setProperty(KEY_PLACEHOLDER, null);
  }

  /**
   * Adds new exception associated with given {@link ASTNode}.
   */
  public static void addException(ASTNode node, Throwable e) {
    // prepare List for exceptions
    List<Throwable> exceptions = getExceptions0(node);
    if (exceptions == null) {
      exceptions = Lists.newArrayList();
      node.setProperty(KEY_EXCEPTIONS, exceptions);
    }
    // add new exception
    exceptions.add(e);
  }

  /**
   * @return exceptions associated with given {@link ASTNode}, may be <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  private static List<Throwable> getExceptions0(ASTNode node) {
    return (List<Throwable>) node.getProperty(KEY_EXCEPTIONS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Placeholder
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link ASTNode} was evaluated as placeholder.
   */
  public static boolean isPlaceholder(ASTNode node) {
    return node != null && node.getProperty(KEY_PLACEHOLDER) == Boolean.TRUE;
  }

  /**
   * Marks that given {@link ASTNode} was evaluated as placeholder.
   */
  public static void markPlaceholder(ASTNode node) {
    node.setProperty(KEY_PLACEHOLDER, Boolean.TRUE);
  }
}
