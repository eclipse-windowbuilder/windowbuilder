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
package org.eclipse.wb.internal.core.parser;

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * {@link ParseFactoryNoModel} for standard Java.
 *
 * @author scheglov_ke
 * @coverage core.model.parser
 */
public final class ParseFactoryNoModelJava extends ParseFactoryNoModel {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ParseFactory_noModel
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean noModel(ClassInstanceCreation creation, ITypeBinding typeBinding) {
    // classes from standard packages
    String typeName = AstNodeUtils.getFullyQualifiedName(typeBinding, false);
    if (typeName.startsWith("java.lang.") || typeName.startsWith("java.util.")) {
      return true;
    }
    // anonymous listener
    if (creation.getAnonymousClassDeclaration() != null
        && AstNodeUtils.isSuccessorOf(typeBinding, "java.util.EventListener")) {
      boolean isPureInterface = typeBinding.getSuperclass().getSuperclass() == null;
      return isPureInterface;
    }
    // something different
    return false;
  }
}
