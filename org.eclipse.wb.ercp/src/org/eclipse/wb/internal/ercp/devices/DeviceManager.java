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
package org.eclipse.wb.internal.ercp.devices;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.ercp.devices.command.CategoryAddCommand;
import org.eclipse.wb.internal.ercp.devices.command.CategoryMoveCommand;
import org.eclipse.wb.internal.ercp.devices.command.CategoryNameCommand;
import org.eclipse.wb.internal.ercp.devices.command.CategoryRemoveCommand;
import org.eclipse.wb.internal.ercp.devices.command.Command;
import org.eclipse.wb.internal.ercp.devices.command.DeviceAddCommand;
import org.eclipse.wb.internal.ercp.devices.command.DeviceEditCommand;
import org.eclipse.wb.internal.ercp.devices.command.DeviceMoveCommand;
import org.eclipse.wb.internal.ercp.devices.command.DeviceRemoveCommand;
import org.eclipse.wb.internal.ercp.devices.command.ElementVisibilityCommand;
import org.eclipse.wb.internal.ercp.devices.model.CategoryInfo;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.eclipse.core.runtime.IConfigurationElement;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Manager for accessing {@link DeviceInfo}'s.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class DeviceManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private DeviceManager() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String POINT_DEVICES = "org.eclipse.wb.ercp.devices";
  private static List<CategoryInfo> m_caterogies;

  /**
   * Specifies that devices configuration should be reloaded.
   */
  public static void forceReload() {
    m_caterogies = null;
  }

  /**
   * Removes all applied {@link Command}'s.
   */
  public static void resetToDefaults() {
    m_commands.clear();
    commands_write();
    forceReload();
  }

  /**
   * @return the {@link List} of {@link CategoryInfo}'s existing in configuration.
   */
  public static List<CategoryInfo> getCategories() {
    if (m_caterogies == null) {
      m_caterogies = Lists.newArrayList();
      try {
        // load from plugins
        List<IConfigurationElement> categoryElements =
            ExternalFactoriesHelper.getElements(POINT_DEVICES, "category");
        for (IConfigurationElement categoryElement : categoryElements) {
          CategoryInfo category = new CategoryInfo(categoryElement);
          // add this category
          m_caterogies.add(category);
          // add devices
          for (IConfigurationElement deviceElement : categoryElement.getChildren("device")) {
            category.addDevice(new DeviceInfo(deviceElement));
          }
        }
        // apply commands
        commands_apply();
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    }
    return m_caterogies;
  }

  /**
   * @return the {@link CategoryInfo} with given id, or <code>null</code> if no such
   *         {@link CategoryInfo} found.
   */
  public static CategoryInfo getCategory(String id) {
    for (CategoryInfo category : getCategories()) {
      if (category.getId().equals(id)) {
        return category;
      }
    }
    // no category found
    return null;
  }

  /**
   * @return the {@link DeviceInfo} with given id, or <code>null</code> if no such
   *         {@link DeviceInfo} found.
   */
  public static DeviceInfo getDevice(String id) {
    for (CategoryInfo category : getCategories()) {
      for (DeviceInfo device : category.getDevices()) {
        if (device.getId().equals(id)) {
          return device;
        }
      }
    }
    // no device found
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final String P_DEFAULT_DEVICE_ID = "devices.defaultId";

  /**
   * @return the default {@link DeviceInfo} selected by user.
   */
  public DeviceInfo getDefault() {
    //String defaultId = Activator.getDefault().getPreferenceStore().getString(P_DEFAULT_DEVICE_ID);
    //if (defaultId ==)
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final List<Class<? extends Command>> m_commandClasses = Lists.newArrayList();
  static {
    m_commandClasses.add(ElementVisibilityCommand.class);
    m_commandClasses.add(CategoryAddCommand.class);
    m_commandClasses.add(CategoryNameCommand.class);
    m_commandClasses.add(CategoryMoveCommand.class);
    m_commandClasses.add(CategoryRemoveCommand.class);
    m_commandClasses.add(DeviceAddCommand.class);
    m_commandClasses.add(DeviceEditCommand.class);
    m_commandClasses.add(DeviceMoveCommand.class);
    m_commandClasses.add(DeviceRemoveCommand.class);
  }
  private static Map<String, Class<? extends Command>> m_idToCommandClass;
  private static List<Command> m_commands;

  /**
   * Applies commands for modifying palette.
   */
  private static void commands_apply() {
    try {
      // prepare mapping: id -> command class
      if (m_idToCommandClass == null) {
        m_idToCommandClass = Maps.newTreeMap();
        for (Class<? extends Command> commandClass : m_commandClasses) {
          String id = (String) commandClass.getField("ID").get(null);
          m_idToCommandClass.put(id, commandClass);
        }
      }
      // read commands
      m_commands = Lists.newArrayList();
      File commandsFile = commands_getFile();
      if (commandsFile.exists()) {
        FileInputStream inputStream = new FileInputStream(commandsFile);
        try {
          SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
          parser.parse(inputStream, new DefaultHandler() {
            @Override
            public void startElement(String uri,
                String localName,
                String name,
                Attributes attributes) {
              try {
                // prepare command class
                Class<? extends Command> commandClass;
                {
                  commandClass = m_idToCommandClass.get(name);
                  if (commandClass == null) {
                    return;
                  }
                }
                // create command
                Command command;
                {
                  Constructor<? extends Command> constructor =
                      commandClass.getConstructor(new Class[]{Attributes.class});
                  command = constructor.newInstance(new Object[]{attributes});
                }
                // add command
                commands_addExecute(command);
              } catch (Throwable e) {
              }
            }
          });
        } finally {
          inputStream.close();
        }
      }
    } catch (Throwable e) {
    }
  }

  /**
   * Adds given {@link Command} to the list (and executes it).
   */
  private static void commands_addExecute(Command command) {
    try {
      command.execute();
      commands_add(command);
    } catch (Throwable e) {
    }
  }

  /**
   * Adds given {@link Command} to the list and writes commands.
   */
  public static void commands_add(Command command) {
    command.addToCommandList(m_commands);
  }

  /**
   * Stores current {@link Command}'s {@link List}.
   */
  public static void commands_write() {
    try {
      File commandsFile = commands_getFile();
      PrintWriter writer = new PrintWriter(new FileOutputStream(commandsFile));
      try {
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<commands>");
        // write separate commands
        for (Command command : m_commands) {
          writer.println(command.toString());
        }
        // close
        writer.println("</commands>");
      } finally {
        writer.close();
      }
    } catch (Throwable e) {
    }
  }

  /**
   * @return the {@link File} with {@link Command}'s.
   */
  private static File commands_getFile() {
    File stateDirectory = Activator.getDefault().getStateLocation().toFile();
    stateDirectory.mkdirs();
    return new File(stateDirectory, "devices.commands");
  }
}
