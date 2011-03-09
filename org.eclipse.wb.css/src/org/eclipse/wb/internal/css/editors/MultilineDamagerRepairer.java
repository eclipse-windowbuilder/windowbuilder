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
package org.eclipse.wb.internal.css.editors;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

/**
 * {@link DefaultDamagerRepairer} which damages and repairs full partition.
 * 
 * @author scheglov_ke
 * @coverage CSS.editor
 */
public class MultilineDamagerRepairer extends DefaultDamagerRepairer {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultilineDamagerRepairer(ITokenScanner scanner) {
    super(scanner);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPresentationDamager
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IRegion getDamageRegion(ITypedRegion partition,
      DocumentEvent e,
      boolean documentPartitioningChanged) {
    return partition;
  }
}