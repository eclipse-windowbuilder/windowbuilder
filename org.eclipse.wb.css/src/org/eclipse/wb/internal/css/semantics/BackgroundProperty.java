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
package org.eclipse.wb.internal.css.semantics;

/**
 * Composite property for background.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public class BackgroundProperty extends AbstractSemanticsComposite {
  public final SimpleValue color = mapSimpleProperty(this, "background-color");
  public final SimpleValue image = mapSimpleProperty(this, "background-image");
  public final SimpleValue repeat = mapSimpleProperty(this, "background-repeat");
  public final SimpleValue attachment = mapSimpleProperty(this, "background-attachment");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BackgroundProperty(AbstractSemanticsComposite composite) {
    super(composite);
  }
}
