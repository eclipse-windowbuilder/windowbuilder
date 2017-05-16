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

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;

/**
 * Processor for {@link ParseRootContext}, used before parsing.
 *
 * @author scheglov_ke
 * @coverage core.model.parser
 */
public interface IParseContextProcessor {
  void process(AstEditor editor, ExecutionFlowDescription flowDescription, List<JavaInfo> components)
      throws Exception;
}
