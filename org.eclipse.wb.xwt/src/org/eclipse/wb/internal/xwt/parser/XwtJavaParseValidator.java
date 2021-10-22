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
package org.eclipse.wb.internal.xwt.parser;

import org.eclipse.wb.internal.core.parser.IParseValidator;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.xwt.IExceptionConstants;

/**
 * {@link IParseValidator} to prevent opening XWT Java class.
 *
 * @author scheglov_ke
 * @coverage XWT.parser
 */
public class XwtJavaParseValidator implements IParseValidator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IParseValidator INSTANCE = new XwtJavaParseValidator();

  private XwtJavaParseValidator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseValidator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void validate(AstEditor editor) throws Exception {
    if (editor.getSource().contains("XWT.load")) {
      throw new DesignerException(IExceptionConstants.DONT_OPEN_JAVA,
          editor.getModelUnit().getElementName());
    }
  }
}
