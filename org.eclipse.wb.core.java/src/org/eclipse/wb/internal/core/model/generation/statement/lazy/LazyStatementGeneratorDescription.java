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
package org.eclipse.wb.internal.core.model.generation.statement.lazy;

import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link StatementGeneratorDescription} for {@link LazyStatementGenerator}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public final class LazyStatementGeneratorDescription extends StatementGeneratorDescription {
  public static final StatementGeneratorDescription INSTANCE =
      new LazyStatementGeneratorDescription();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private LazyStatementGeneratorDescription() {
    super("org.eclipse.wb.core.model.statement.lazy",
        "Lazy",
        "each component in separate getXXX() method");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StatementGeneratorDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public StatementGenerator get() {
    return LazyStatementGenerator.INSTANCE;
  }

  @Override
  public GenerationPropertiesComposite createPropertiesComposite(Composite parent,
      DataBindManager bindManager,
      IPreferenceStore store) {
    return new PropertiesComposite(parent, bindManager, store);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties composite
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class PropertiesComposite extends GenerationPropertiesComposite {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public PropertiesComposite(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
    }
  }
}
