package org.eclipse.wb.internal.rcp.nebula.titledseparator;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.editor.complex.InstanceObjectPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

/**
 * Model for {@link TitledSeparator}.
 */
public class TitledSeparatorInfo extends CompositeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TitledSeparatorInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // listener for setting property to default
    InstanceObjectPropertyEditor.installListenerForProperty(this);
  }
}
