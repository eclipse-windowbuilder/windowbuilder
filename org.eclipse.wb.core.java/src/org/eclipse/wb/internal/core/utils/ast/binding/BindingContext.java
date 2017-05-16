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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.Map;

/**
 * Container, point of access for copies of {@link IBinding}'s.
 *
 * We use our implementations of bindings because standard ones reference objects from internal
 * compiler's AST. This is not problem for Eclipse itself, but we parse very often, for every change
 * in editor, so we can end up with a lot of referenced objects.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public final class BindingContext {
  private final Map<String, DesignerTypeBinding> m_typeBindings = Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the existing (shared by fully qualified name) or new instance of
   *         {@link DesignerTypeBinding} for given {@link ITypeBinding}.
   */
  public ITypeBinding get(ITypeBinding binding) {
    return get(binding, false);
  }

  public ITypeBinding get(ITypeBinding binding, boolean withGenerics) {
    if (binding == null) {
      return null;
    }
    // OK, not-null binding
    String fullyQualifiedName = AstNodeUtils.getFullyQualifiedName(binding, false, withGenerics);
    if (binding.isGenericType()) {
      fullyQualifiedName += "_wbpGeneric";
    }
    if (binding.isAnonymous()) {
      fullyQualifiedName += "_wbpAnonymous_" + binding.getKey();
    }
    // try to get existing copy
    DesignerTypeBinding designerBinding = null;
    if (!binding.isTypeVariable()) {
      designerBinding = m_typeBindings.get(fullyQualifiedName);
    }
    // if no existing, create new
    if (designerBinding == null) {
      designerBinding = new DesignerTypeBinding(this, fullyQualifiedName, binding);
    }
    // done
    return designerBinding;
  }

  /**
   * @return the new instance of {@link DesignerTypeBinding} for given {@link ITypeBinding}.
   */
  public DesignerTypeBinding getCopy(ITypeBinding binding) {
    return new DesignerTypeBinding(this, null, binding);
  }

  /**
   * @return the new instance of {@link DesignerMethodBinding} for given {@link IMethodBinding}.
   */
  public DesignerMethodBinding get(IMethodBinding binding) {
    return new DesignerMethodBinding(this, binding);
  }

  /**
   * @return the new instance of {@link DesignerVariableBinding} for given {@link IVariableBinding}.
   */
  public IVariableBinding get(IVariableBinding binding) {
    return new DesignerVariableBinding(this, binding);
  }

  /**
   * @return the new instance of {@link DesignerVariableBinding}.
   */
  public IVariableBinding get(String name,
      ITypeBinding declaringClass,
      ITypeBinding type,
      boolean field,
      int modifiers) {
    return new DesignerVariableBinding(this, name, declaringClass, type, field, modifiers);
  }

  /**
   * @return the new instance of {@link DesignerPackageBinding} for given {@link IPackageBinding}.
   */
  public IPackageBinding get(IPackageBinding binding) {
    return new DesignerPackageBinding(binding);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal registration
  //
  ////////////////////////////////////////////////////////////////////////////
  void register(String fullyQualifiedName, DesignerTypeBinding designerBinding) {
    m_typeBindings.put(fullyQualifiedName, designerBinding);
  }
}
