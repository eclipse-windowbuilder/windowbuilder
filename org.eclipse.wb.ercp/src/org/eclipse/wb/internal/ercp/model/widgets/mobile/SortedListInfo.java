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
package org.eclipse.wb.internal.ercp.model.widgets.mobile;

import org.eclipse.wb.core.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.ScrollableInfo;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Model for eSWT {@link org.eclipse.ercp.swt.mobile.SortedList}.
 * 
 * @author scheglov_ke
 * @coverage ercp.model.widgets.mobile
 */
public final class SortedListInfo extends ScrollableInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SortedListInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    // "setFilter" can be set only if "filterStyle" is FILTER, but there are no way to check this
    addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        if (property == getPropertyByTitle("hasFilter")) {
          if (getCreationSupport() instanceof ConstructorCreationSupport) {
            ClassInstanceCreation creation =
                ((ConstructorCreationSupport) getCreationSupport()).getCreation();
            if (creation.arguments().size() == 3) {
              Expression filterStyleArgument = (Expression) creation.arguments().get(2);
              if (Integer.valueOf(1).equals(JavaInfoEvaluationHelper.getValue(filterStyleArgument))) {
                return;
              }
            }
          }
          shouldSetValue[0] = false;
        }
      }
    });
  }
}