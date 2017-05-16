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
package org.eclipse.wb.internal.core.utils.xml;

/**
 * A visitor for {@link DocumentElement} model.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public class DocumentModelVisitor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // DocumentNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean visit(DocumentElement element) {
    return true;
  }

  public void endVisit(DocumentElement element) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Other
  //
  ////////////////////////////////////////////////////////////////////////////
  public void visit(DocumentAttribute attribute) {
  }

  public void visit(DocumentTextNode node) {
  }
}
