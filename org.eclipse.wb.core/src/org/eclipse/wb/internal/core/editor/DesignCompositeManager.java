/*******************************************************************************
 * Copyright (c) 2021, 2021 DSA Daten- und Systemtechnik GmbH. (https://www.dsa.de)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel du Preez   - initial implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor;

/**
 * This class is used to hide/show the Windowbuilder toolbar. By default it is set to true to hide
 * the Windowbuilder toolbars from external sources. Create this object and pass it to the
 * DesignerEditor constructor.
 *
 * example: DesignCompsoiteManager designCompositeManager = new DesignCompositeManager(false);
 * DesignerEditor designEditor = new DesignerEditor(designCompositeManager)
 *
 * @author Marcel du Preez
 */
public class DesignCompositeManager {
  //by default the WindowBuilder toolbar should be visible
  private boolean includeWBToolbar = true;

  /**
   * Default constructor.
   *
   * Using this constructor will set the Windowbuilder toolbar by default
   *
   */
  public DesignCompositeManager() {
  }

  /**
   * Constructor
   *
   * Sets the Windowbuilder toolbar to hidden/visible depending on the parameter includeWBtoolbar
   *
   * @param includeWBtoolbar
   *          - true if WB toolbar should be visible - false if the WB toolbar should be hidden
   */
  public DesignCompositeManager(boolean includeWBtoolbar) {
    includeWBToolbar = includeWBtoolbar;
  }

  public boolean getIncludeWindowBuilderToolbar() {
    return includeWBToolbar;
  }

  public void setIncludeWindowBuilderToolbar(boolean include) {
    includeWBToolbar = include;
  }
}
