package org.eclipse.wb.internal.core.xml.editor;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.structure.property.IPropertiesToolBarContributor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;

import java.util.List;

/**
 * {@link IPropertiesToolBarContributor} for XML.
 *
 * @author scheglov_ke
 * @coverage XML.editor
 */
public final class XmlPropertiesToolBarContributor implements IPropertiesToolBarContributor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IPropertiesToolBarContributor INSTANCE =
      new XmlPropertiesToolBarContributor();

  private XmlPropertiesToolBarContributor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPropertiesToolBarContributor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void contributeToolBar(IToolBarManager manager, final List<ObjectInfo> objects)
      throws Exception {
    addGotoDefinitionAction(manager, objects);
  }

  private void addGotoDefinitionAction(IToolBarManager manager, List<ObjectInfo> objects) {
    if (objects.size() == 1 && objects.get(0) instanceof XmlObjectInfo) {
      final XmlObjectInfo javaInfo = (XmlObjectInfo) objects.get(0);
      IAction gotoDefinitionAction = new Action() {
        @Override
        public void run() {
          int position = javaInfo.getElement().getOffset();
          IDesignPageSite site = IDesignPageSite.Helper.getSite(javaInfo);
          site.openSourcePosition(position);
        }
      };
      gotoDefinitionAction.setImageDescriptor(DesignerPlugin.getImageDescriptor("structure/goto_definition.gif"));
      gotoDefinitionAction.setToolTipText(Messages.ComponentsPropertiesPage_goDefinition);
      manager.appendToGroup(GROUP_EDIT, gotoDefinitionAction);
    }
  }
}
