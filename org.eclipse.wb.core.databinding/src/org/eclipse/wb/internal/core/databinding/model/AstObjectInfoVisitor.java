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
package org.eclipse.wb.internal.core.databinding.model;

/**
 * Visitor for visiting {@link AstObjectInfo} hierarchy.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public abstract class AstObjectInfoVisitor {
  /**
   * This method is invoked to visit given {@link AstObjectInfo}.
   */
  public abstract void visit(AstObjectInfo object) throws Exception;
}