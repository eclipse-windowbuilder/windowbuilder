/************************************************************************
 *                                                                      *
 *  DDDD     SSSS    AAA        Daten- und Systemtechnik Aachen GmbH    *
 *  D   D   SS      A   A       Pascalstrasse 28                        *
 *  D   D    SSS    AAAAA       52076 Aachen-Oberforstbach, Germany     *
 *  D   D      SS   A   A       Telefon: +49 (0)2408 / 9492-0           *
 *  DDDD    SSSS    A   A       Telefax: +49 (0)2408 / 9492-92          *
 *                                                                      *
 *                                                                      *
 *  (c) Copyright by DSA - all rights reserved                          *
 *                                                                      *
 ************************************************************************
 *
 * Initial Creation:
 *    Author      marce
 *    Created on  Nov 24, 2021
 *
 ************************************************************************/
package org.eclipse.wb.internal.core.editor;

public class DesignCompositeManager {
  private boolean includeWBToolbar = false;

  public DesignCompositeManager() {
  }

  public boolean includeWindowbuilderToolbar() {
    return includeWBToolbar;
  }

  public void includeWindowbuilderToolbar(boolean include) {
    includeWBToolbar = include;
  }
}
