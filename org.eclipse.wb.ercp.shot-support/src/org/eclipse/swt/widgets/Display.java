/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.InternalGCData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.ibm.ugl.UGLErrorHandler;
import com.ibm.ugl.eswt.OS;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Instances of this class are responsible for managing the connection between SWT and the
 * underlying operating system. Their most important function is to implement the SWT event loop in
 * terms of the platform event model. They also provide various methods for accessing information
 * about the operating system, and have overall control over the operating system resources which
 * SWT allocates.
 * <p>
 * Applications which are built with SWT will <em>almost always</em> require only a single display.
 * In particular, some platforms which SWT supports will not allow more than one <em>active</em>
 * display. In other words, some platforms do not support creating a new display if one already
 * exists that has not been sent the <code>dispose()</code> message.
 * <p>
 * In SWT, the thread which creates a <code>Display</code> instance is distinguished as the
 * <em>user-interface thread</em> for that display.
 * </p>
 * The user-interface thread for a particular display has the following special attributes:
 * <ul>
 * <li>
 * The event loop for that display must be run from the thread.</li>
 * <li>
 * Some SWT API methods (notably, most of the public methods in <code>Widget</code> and its
 * subclasses), may only be called from the thread. (To support multi-threaded user-interface
 * applications, class <code>Display</code> provides inter-thread communication methods which allow
 * threads other than the user-interface thread to request that it perform operations on their
 * behalf.)</li>
 * <li>
 * The thread is not allowed to construct other <code>Display</code>s until that display has been
 * disposed. (Note that, this is in addition to the restriction mentioned above concerning platform
 * support for multiple displays. Thus, the only way to have multiple simultaneously active
 * displays, even on platforms which support it, is to have multiple threads.)</li>
 * </ul>
 * Enforcing these attributes allows SWT to be implemented directly on the underlying operating
 * system's event model. This has numerous benefits including smaller footprint, better use of
 * resources, safer memory management, clearer program logic, better performance, and fewer overall
 * operating system threads required. The down side however, is that care must be taken (only) when
 * constructing multi-threaded applications to use the inter-thread communication mechanisms which
 * this class provides when required. </p>
 * <p>
 * All SWT API methods which may only be called from the user-interface thread are distinguished in
 * their documentation by indicating that they throw the "<code>ERROR_THREAD_INVALID_ACCESS</code>"
 * SWT exception.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Close, Dispose</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see #syncExec
 * @see #asyncExec
 * @see #wake
 * @see #readAndDispatch
 * @see #sleep
 * @see #dispose
 */
public class Display extends Device {
  Shell activeShell;
  boolean systemColorsCreated = false;
  /* Displays. */
  static Display Default;
  static Display[] Displays = new Display[4];
  /* Package name */
  static final String PACKAGE_NAME;
  static {
    String name = Display.class.getName();
    int index = name.lastIndexOf('.');
    PACKAGE_NAME = name.substring(0, index + 1);
    com.ibm.ugl.p3ml.OS.uglErrorHandler = new ESWTErrorHandler();
  }
  static final int KeyCodeMap[][] = {
      {OS.KEYCODE_BACKSPACE, SWT.BS},
      {OS.KEYCODE_TAB, SWT.TAB},
      {OS.KEYCODE_ENTER, SWT.CR},
      {OS.KEYCODE_ESCAPE, SWT.ESC},
      {OS.KEYCODE_PAUSE, SWT.PAUSE},
      {OS.KEYCODE_HOME, SWT.HOME},
      {OS.KEYCODE_END, SWT.END},
      {OS.KEYCODE_PAGE_UP, SWT.PAGE_UP},
      {OS.KEYCODE_PAGE_DOWN, SWT.PAGE_DOWN},
      {OS.KEYCODE_INSERT, SWT.INSERT},
      {OS.KEYCODE_DELETE, SWT.DEL},
      {OS.KEYCODE_ARROW_LEFT, SWT.ARROW_LEFT},
      {OS.KEYCODE_ARROW_RIGHT, SWT.ARROW_RIGHT},
      {OS.KEYCODE_ARROW_UP, SWT.ARROW_UP},
      {OS.KEYCODE_ARROW_DOWN, SWT.ARROW_DOWN},
      {OS.KEYCODE_SHIFT, SWT.SHIFT},
      {OS.KEYCODE_ALT, SWT.ALT},
      {OS.KEYCODE_CONTROL, SWT.CTRL},
      {OS.KEYCODE_LOCK_CAPS, SWT.CAPS_LOCK},
      {OS.KEYCODE_LOCK_NUM, SWT.NUM_LOCK},
      {OS.KEYCODE_LOCK_SCROLL, SWT.SCROLL_LOCK},
      {OS.KEYCODE_F1, SWT.F1},
      {OS.KEYCODE_F2, SWT.F2},
      {OS.KEYCODE_F3, SWT.F3},
      {OS.KEYCODE_F4, SWT.F4},
      {OS.KEYCODE_F5, SWT.F5},
      {OS.KEYCODE_F6, SWT.F6},
      {OS.KEYCODE_F7, SWT.F7},
      {OS.KEYCODE_F8, SWT.F8},
      {OS.KEYCODE_F9, SWT.F9},
      {OS.KEYCODE_F10, SWT.F10},
      {OS.KEYCODE_F11, SWT.F11},
      {OS.KEYCODE_F12, SWT.F12},
      {OS.KEYCODE_F13, SWT.F13},
      {OS.KEYCODE_F14, SWT.F14},
      {OS.KEYCODE_F15, SWT.F15},
      {OS.KEYCODE_KEYPAD_MULTIPLY, SWT.KEYPAD_MULTIPLY},
      {OS.KEYCODE_KEYPAD_ADD, SWT.KEYPAD_ADD},
      {OS.KEYCODE_KEYPAD_SUBTRACT, SWT.KEYPAD_SUBTRACT},
      {OS.KEYCODE_KEYPAD_DECIMAL, SWT.KEYPAD_DECIMAL},
      {OS.KEYCODE_KEYPAD_DIVIDE, SWT.KEYPAD_DIVIDE},
      {OS.KEYCODE_KEYPAD_0, SWT.KEYPAD_0},
      {OS.KEYCODE_KEYPAD_1, SWT.KEYPAD_1},
      {OS.KEYCODE_KEYPAD_2, SWT.KEYPAD_2},
      {OS.KEYCODE_KEYPAD_3, SWT.KEYPAD_3},
      {OS.KEYCODE_KEYPAD_4, SWT.KEYPAD_4},
      {OS.KEYCODE_KEYPAD_5, SWT.KEYPAD_5},
      {OS.KEYCODE_KEYPAD_6, SWT.KEYPAD_6},
      {OS.KEYCODE_KEYPAD_7, SWT.KEYPAD_7},
      {OS.KEYCODE_KEYPAD_8, SWT.KEYPAD_8},
      {OS.KEYCODE_KEYPAD_9, SWT.KEYPAD_9},
      {OS.KEYCODE_KEYPAD_ENTER, SWT.KEYPAD_CR},
      {OS.KEYCODE_KEYPAD_ENTER, SWT.KEYPAD_EQUAL},};
  /* Sync/Async Widget Communication */
  Synchronizer synchronizer = new Synchronizer(this);
  Thread thread;
  /* Timer Runnables */
  Runnable[] timers;
  /* Display Shutdown */
  Runnable[] disposeList;
  /* Event handling */
  Event[] eventQueue;
  EventTable eventTable, filterTable;
  /* Display Data */
  Object data;
  String[] keys;
  Object[] values;
  /* System palette */
  int defaultPalette;
  /* Initial Guesses for Shell Trimmings. */
  int borderTrimWidth = 4, borderTrimHeight = 4;
  int titleTrimWidth = 4, titleTrimHeight = 24;
  Color COLOR_INFO_FOREGROUND, COLOR_INFO_BACKGROUND, COLOR_TITLE_FOREGROUND,
      COLOR_TITLE_BACKGROUND;
  Color COLOR_TITLE_BACKGROUND_GRADIENT, COLOR_TITLE_INACTIVE_FOREGROUND,
      COLOR_TITLE_INACTIVE_BACKGROUND;
  Color COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT, COLOR_WIDGET_DARK_SHADOW,
      COLOR_WIDGET_NORMAL_SHADOW;
  Color COLOR_WIDGET_LIGHT_SHADOW, COLOR_WIDGET_HIGHLIGHT_SHADOW, COLOR_WIDGET_BACKGROUND;
  Color COLOR_WIDGET_FOREGROUND, COLOR_WIDGET_BORDER, COLOR_LIST_FOREGROUND, COLOR_LIST_BACKGROUND;
  Color COLOR_LIST_SELECTION, COLOR_LIST_SELECTION_TEXT;
  private Control focusControl = null;
  private Hashtable shells = null;
  /**
   * TEMPORARY CODE. Install the runnable that gets the current display. This code will be removed
   * in the future.
   */
  static {
    Internal_DeviceFinder = new Runnable() {
      public void run() {
        Device device = getCurrent();
        if (device == null) {
          device = getDefault();
        }
        setDevice(device);
      }
    };
  }

  static class ESWTErrorHandler implements UGLErrorHandler {
    /* (non-Javadoc)
     * @see com.ibm.ugl.UGLErrorHandler#respond(java.lang.String, int, java.lang.String)
     */
    public void respond(String uglFunction, int errorCode, String errorMessage) {
      if (uglFunction.startsWith("Image_")) {
        if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_IO) {
          SWT.error(SWT.ERROR_IO);
        } else if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_OUT_OF_MEMORY) {
          SWT.error(SWT.ERROR_NO_HANDLES);
        } else if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_UNSUPPORTED_DEPTH) {
          SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
        } else if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_INVALID_IMAGE) {
          SWT.error(SWT.ERROR_INVALID_IMAGE);
        } else if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_UNSUPPORTED_FORMAT) {
          SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
        }
      } else if (uglFunction.startsWith("Font_")) {
        if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_OUT_OF_MEMORY) {
          SWT.error(SWT.ERROR_NO_HANDLES);
        }
      } else if (uglFunction.startsWith("Graphics_")) {
        if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_OUT_OF_MEMORY) {
          SWT.error(SWT.ERROR_NO_HANDLES);
        }
      } else if (uglFunction.equals("MenuBar_New")
          || uglFunction.equals("PopupMenu_New")
          || uglFunction.equals("DropDownMenu_New")) {
        if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_OUT_OF_MEMORY) {
          SWT.error(SWT.ERROR_NO_HANDLES);
        }
      }
      if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_OUT_OF_MEMORY) {
        // pass on native out of memory situations, if we can
        throw new OutOfMemoryError();
      }
      if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_NOT_IMPLEMENTED) {
        SWT.error(SWT.ERROR_NOT_IMPLEMENTED);
      }
      if (errorCode == com.ibm.ugl.p3ml.OS.ERROR_OTHER) {
        throw new RuntimeException("Unexpected native exception: " + errorMessage);
      }
    }
  }

  /**
   * TEMPORARY CODE.
   */
  static void setDevice(Device device) {
    Internal_CurrentDevice = device;
  }

  /**
   * Constructs a new instance of this class.
   * <p>
   * Note: The resulting display is marked as the <em>current</em> display. If this is the first
   * display which has been constructed since the application started, it is also marked as the
   * <em>default</em> display.
   * </p>
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if called from a thread that already created an
   *              existing display</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   *              </ul>
   * 
   * @see #getCurrent
   * @see #getDefault
   * @see Widget#checkSubclass
   * @see Shell
   */
  public Display() {
    super();
  }

  /**
   * Adds the listener to the collection of listeners who will be notifed when an event of the given
   * type occurs anywhere in this display. When the event does occur, the listener is notified by
   * sending it the <code>handleEvent()</code> message.
   * 
   * <p>
   * Setting the type of an event to <code>SWT.None</code> from within the
   * <code>handleEvent()</code> method can be used to change the event type and stop subsequent Java
   * listeners from running. Because event filters run before other listeners, event filters can
   * both block other listeners and set arbitrary fields within an event. For this reason, event
   * filters are both powerful and dangerous. They should generally be avoided for performance,
   * debugging and code maintenance reasons.
   * </p>
   * 
   * @param eventType
   *          the type of event to listen for
   * @param listener
   *          the listener which should be notified when the event occurs
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see Listener
   * @see #removeFilter
   * @see #removeListener
   * 
   * @since 3.0
   */
  public void addFilter(int eventType, Listener listener) {
    checkDevice();
    if (listener == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    if (filterTable == null) {
      filterTable = new EventTable();
    }
    filterTable.hook(eventType, listener);
  }

  /**
   * Adds the listener to the collection of listeners who will be notifed when an event of the given
   * type occurs. When the event does occur in the display, the listener is notified by sending it
   * the <code>handleEvent()</code> message.
   * 
   * @param eventType
   *          the type of event to listen for
   * @param listener
   *          the listener which should be notified when the event occurs
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see Listener
   * @see #removeListener
   * 
   * @since 2.0
   */
  public void addListener(int eventType, Listener listener) {
    checkDevice();
    if (listener == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    if (eventTable == null) {
      eventTable = new EventTable();
    }
    eventTable.hook(eventType, listener);
  }

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the user-interface thread
   * at the next reasonable opportunity. The caller of this method continues to run in parallel, and
   * is not notified when the runnable has completed. Specifying <code>null</code> as the runnable
   * simply wakes the user-interface thread when run.
   * <p>
   * Note that at the time the runnable is invoked, widgets that have the receiver as their display
   * may have been disposed. Therefore, it is necessary to check for this case inside the runnable
   * before accessing the widget.
   * </p>
   * 
   * @param runnable
   *          code to run on the user-interface thread or <code>null</code>
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see #syncExec
   */
  public void asyncExec(Runnable runnable) {
    if (isDisposed()) {
      SWT.error(SWT.ERROR_DEVICE_DISPOSED);
    }
    synchronizer.asyncExec(runnable);
  }

  /**
   * Causes the system hardware to emit a short sound (if it supports this capability).
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public void beep() {
    checkDevice();
    OS.Display_Beep(internal_handle);
  }

  @Override
  protected void checkDevice() {
    if (thread == null) {
      error(SWT.ERROR_WIDGET_DISPOSED);
    }
    if (thread != Thread.currentThread()) {
      error(SWT.ERROR_THREAD_INVALID_ACCESS);
    }
    if (isDisposed()) {
      error(SWT.ERROR_DEVICE_DISPOSED);
    }
  }

  void checkSubclass() {
    if (!Display.isValidClass(getClass())) {
      SWT.error(SWT.ERROR_INVALID_SUBCLASS);
    }
  }

  /**
   * Requests that the connection between SWT and the underlying operating system be closed.
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see Device#dispose
   * 
   * @since 2.0
   */
  public void close() {
    checkDevice();
    Event event = new Event();
    sendEvent(SWT.Close, event);
    if (event.doit) {
      dispose();
    }
  }

  /**
   * Creates the device in the operating system. If the device does not have a handle, this method
   * may do nothing depending on the device.
   * <p>
   * This method is called before <code>init</code>.
   * </p>
   * <p>
   * Subclasses are supposed to reimplement this method and not call the <code>super</code>
   * implementation.
   * </p>
   * 
   * @param data
   *          the DeviceData which describes the receiver
   * 
   * @see #init
   */
  @Override
  protected void internal_create() {
    checkSubclass();
    checkDisplay(thread = Thread.currentThread());
    register(this);
    if (Default == null) {
      Default = this;
    }
    internal_handle = com.ibm.ugl.p3ml.OS.Display_New();
  }

  @Override
  protected void destroy() {
    if (this == Default) {
      Default = null;
    }
    deregister(this);
    super.destroy();
  }

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the user-interface thread
   * just before the receiver is disposed. Specifying a <code>null</code> runnable is ignored.
   * 
   * @param runnable
   *          code to run at dispose time.
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public void disposeExec(Runnable runnable) {
    checkDevice();
    if (disposeList == null) {
      disposeList = new Runnable[4];
    }
    for (int i = 0; i < disposeList.length; i++) {
      if (disposeList[i] == null) {
        disposeList[i] = runnable;
        return;
      }
    }
    Runnable[] newDisposeList = new Runnable[disposeList.length + 4];
    System.arraycopy(disposeList, 0, newDisposeList, 0, disposeList.length);
    newDisposeList[disposeList.length] = runnable;
    disposeList = newDisposeList;
  }

  /**
   * Does whatever display specific cleanup is required, and then uses the code in
   * <code>SWTError.error</code> to handle the error.
   * 
   * @param code
   *          the descriptive error code
   * 
   * @see SWTError#error
   */
  void error(int code) {
    SWT.error(code);
  }

  static synchronized void checkDisplay(Thread thread) {
    for (int i = 0; i < Displays.length; i++) {
      if (Displays[i] != null && Displays[i].thread == thread) {
        SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
      }
    }
  }

  static int convertToUglKeyCode(int swtKeyCode) {
    for (int i = 0; i < KeyCodeMap.length; i++) {
      if (KeyCodeMap[i][1] == swtKeyCode) {
        return KeyCodeMap[i][0];
      }
    }
    return swtKeyCode;
  }

  static int convertToSwtKeyCode(int uglKeyCode) {
    /*
     * Bug 108406 - eSWT should always return keycode
     * of lower-case char. UGL always returns keycode
     * of upper-case char.
     */
    if (uglKeyCode >= 65 && uglKeyCode <= 90) {
      return uglKeyCode + 32;
    }
    for (int i = 0; i < KeyCodeMap.length; i++) {
      if (KeyCodeMap[i][0] == uglKeyCode) {
        return KeyCodeMap[i][1];
      }
    }
    return 0;
  }

  static synchronized void deregister(Display display) {
    for (int i = 0; i < Displays.length; i++) {
      if (display == Displays[i]) {
        Displays[i] = null;
      }
    }
  }

  /**
   * Returns the display which the given thread is the user-interface thread for, or null if the
   * given thread is not a user-interface thread for any display.
   * 
   * @param thread
   *          the user-interface thread
   * @return the display for the given thread
   */
  public static synchronized Display findDisplay(Thread thread) {
    for (int i = 0; i < Displays.length; i++) {
      Display display = Displays[i];
      if (display != null && display.thread == thread) {
        return display;
      }
    }
    return null;
  }

  static synchronized boolean noDisplaysExist() {
    for (int i = 0; i < Displays.length; i++) {
      if (Displays[i] != null) {
        return false;
      }
    }
    return true;
  }

  boolean filterEvent(Event event) {
    if (filterTable != null) {
      filterTable.sendEvent(event);
    }
    return false;
  }

  boolean filters(int eventType) {
    if (filterTable == null) {
      return false;
    }
    return filterTable.hooks(eventType);
  }

  /**
   * Returns the currently active <code>Shell</code>, or null if no shell belonging to the currently
   * running application is active.
   * 
   * @return the active shell or null
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public Shell getActiveShell() {
    checkDevice();
    return activeShell;
  }

  /**
   * Returns a rectangle which describes the area of the receiver which is capable of displaying
   * data.
   * 
   * @return the client area
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see #getBounds
   */
  @Override
  public Rectangle getClientArea() {
    checkDevice();
    Rectangle bounds = getBounds();
    int[] insetArray = OS.Display_GetInsets(internal_handle);
    return new Rectangle(bounds.x + insetArray[com.ibm.ugl.p3ml.OS.INDEX_LEFT],
        bounds.y + insetArray[com.ibm.ugl.p3ml.OS.INDEX_TOP],
        bounds.width
            - (insetArray[com.ibm.ugl.p3ml.OS.INDEX_RIGHT] + insetArray[com.ibm.ugl.p3ml.OS.INDEX_LEFT]),
        bounds.height
            - (insetArray[com.ibm.ugl.p3ml.OS.INDEX_TOP] + insetArray[com.ibm.ugl.p3ml.OS.INDEX_BOTTOM]));
  }

  /**
   * Returns the display which the currently running thread is the user-interface thread for, or
   * null if the currently running thread is not a user-interface thread for any display.
   * 
   * @return the current display
   */
  public static synchronized Display getCurrent() {
    return findDisplay(Thread.currentThread());
  }

  /**
   * Returns the application defined property of the receiver with the specified name, or null if it
   * has not been set.
   * <p>
   * Applications may have associated arbitrary objects with the receiver in this fashion. If the
   * objects stored in the properties need to be notified when the display is disposed of, it is the
   * application's responsibility to provide a <code>disposeExec()</code> handler which does so.
   * </p>
   * 
   * @param key
   *          the name of the property
   * @return the value of the property or null if it has not been set
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the key is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see #setData(String, Object)
   * @see #disposeExec(Runnable)
   */
  public Object getData(String key) {
    checkDevice();
    if (key == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    if (keys == null) {
      return null;
    }
    for (int i = 0; i < keys.length; i++) {
      if (keys[i].equals(key)) {
        return values[i];
      }
    }
    return null;
  }

  /**
   * Returns the application defined, display specific data associated with the receiver, or null if
   * it has not been set. The <em>display specific data</em> is a single, unnamed field that is
   * stored with every display.
   * <p>
   * Applications may put arbitrary objects in this field. If the object stored in the display
   * specific data needs to be notified when the display is disposed of, it is the application's
   * responsibility to provide a <code>disposeExec()</code> handler which does so.
   * </p>
   * 
   * @return the display specific data
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see #setData(Object)
   * @see #disposeExec(Runnable)
   */
  public Object getData() {
    checkDevice();
    return data;
  }

  /**
   * Returns the default display. One is created (making the thread that invokes this method its
   * user-interface thread) if it did not already exist.
   * 
   * @return the default display
   */
  public static synchronized Display getDefault() {
    if (Default == null) {
      Default = new Display();
    }
    return Default;
  }

  static synchronized void register(Display display) {
    for (int i = 0; i < Displays.length; i++) {
      if (Displays[i] == null) {
        Displays[i] = display;
        return;
      }
    }
    Display[] newDisplays = new Display[Displays.length + 4];
    System.arraycopy(Displays, 0, newDisplays, 0, Displays.length);
    newDisplays[Displays.length] = display;
    Displays = newDisplays;
  }

  /**
   * Returns the button dismissal alignment, one of <code>LEFT</code> or <code>RIGHT</code>. The
   * button dismissal alignment is the ordering that should be used when positioning the default
   * dismissal button for a dialog. For example, in a dialog that contains an OK and CANCEL button,
   * on platforms where the button dismissal alignment is <code>LEFT</code>, the button ordering
   * should be OK/CANCEL. When button dismissal alignment is <code>RIGHT</code>, the button ordering
   * should be CANCEL/OK.
   * 
   * @return the button dismissal order
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @since 2.1
   */
  public int getDismissalAlignment() {
    checkDevice();
    return SWT.LEFT;
  }

  /**
   * Returns the longest duration, in milliseconds, between two mouse button clicks that will be
   * considered a <em>double click</em> by the underlying operating system.
   * 
   * @return the double click time
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public int getDoubleClickTime() {
    checkDevice();
    return OS.Display_GetDoubleClickTime(internal_handle);
  }

  /**
   * Returns the control which currently has keyboard focus, or null if keyboard events are not
   * currently going to any of the controls built by the currently running application.
   * 
   * @return the control under the cursor
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public Control getFocusControl() {
    checkDevice();
    return focusControl;
  }

  /*
   * Track the currently focused Control in Java to avoid
   * the WidgetTable lookup.
   */
  void setFocusControl(Control control) {
    focusControl = control;
  }

  /**
   * Returns the maximum allowed depth of icons on this display, in bits per pixel. On some
   * platforms, this may be different than the actual depth of the display.
   * 
   * @return the maximum icon depth
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see Device#getDepth
   */
  public int getIconDepth() {
    return getDepth();
  }

  /**
   * Returns a (possibly empty) array containing all shells which have not been disposed and have
   * the receiver as their display.
   * 
   * @return the receiver's shells
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public Shell[] getShells() {
    checkDevice();
    if (shells == null || shells.size() == 0) {
      return new Shell[0];
    }
    // count the number of shells so we can create the array
    int count = 0;
    Enumeration elementEnum = shells.elements();
    while (elementEnum.hasMoreElements()) {
      Shell shell = (Shell) elementEnum.nextElement();
      if (!shell.isDisposed() && this == shell.getDisplay()) {
        count++;
      }
    }
    // create and fill the array to return
    int index = 0;
    Shell[] result = new Shell[count];
    elementEnum = shells.elements();
    while (elementEnum.hasMoreElements()) {
      Shell shell = (Shell) elementEnum.nextElement();
      if (!shell.isDisposed() && this == shell.getDisplay()) {
        result[index++] = shell;
      }
    }
    return result;
  }

  void addShell(Shell shell) {
    // replaces WidgetTable functionality
    if (shells == null) {
      shells = new Hashtable();
    }
    shells.put(shell, shell);
  }

  void removeShell(Shell shell) {
    // replaces WidgetTable functionality
    shells.remove(shell);
  }

  /**
   * Returns the thread that has invoked <code>syncExec</code> or null if no such runnable is
   * currently being invoked by the user-interface thread.
   * <p>
   * Note: If a runnable invoked by asyncExec is currently running, this method will return null.
   * </p>
   * 
   * @return the receiver's sync-interface thread
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public Thread getSyncThread() {
    if (isDisposed()) {
      SWT.error(SWT.ERROR_DEVICE_DISPOSED);
    }
    return synchronizer.syncThread;
  }

  /**
   * Returns the matching standard color for the given constant, which should be one of the color
   * constants specified in class <code>SWT</code>. Any value other than one of the SWT color
   * constants which is passed in will result in the color black. This color should not be free'd
   * because it was allocated by the system, not the application.
   * 
   * @param id
   *          the color constant
   * @return the matching color
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see SWT
   */
  @Override
  public Color getSystemColor(int id) {
    checkDevice();
    if (systemColorsCreated == false) {
      initSystemColors();
    }
    switch (id) {
      case SWT.COLOR_TITLE_FOREGROUND :
        return COLOR_TITLE_FOREGROUND;
      case SWT.COLOR_TITLE_BACKGROUND :
        return COLOR_TITLE_BACKGROUND;
      case SWT.COLOR_TITLE_BACKGROUND_GRADIENT :
        return COLOR_TITLE_BACKGROUND_GRADIENT;
      case SWT.COLOR_TITLE_INACTIVE_FOREGROUND :
        return COLOR_TITLE_INACTIVE_FOREGROUND;
      case SWT.COLOR_TITLE_INACTIVE_BACKGROUND :
        return COLOR_TITLE_INACTIVE_BACKGROUND;
      case SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT :
        return COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT;
      case SWT.COLOR_WIDGET_DARK_SHADOW :
        return COLOR_WIDGET_DARK_SHADOW;
      case SWT.COLOR_WIDGET_NORMAL_SHADOW :
        return COLOR_WIDGET_NORMAL_SHADOW;
      case SWT.COLOR_WIDGET_LIGHT_SHADOW :
        return COLOR_WIDGET_LIGHT_SHADOW;
      case SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW :
        return COLOR_WIDGET_HIGHLIGHT_SHADOW;
      case SWT.COLOR_WIDGET_BACKGROUND :
        return COLOR_WIDGET_BACKGROUND;
      case SWT.COLOR_WIDGET_FOREGROUND :
        return COLOR_WIDGET_FOREGROUND;
      case SWT.COLOR_WIDGET_BORDER :
        return COLOR_WIDGET_BORDER;
      case SWT.COLOR_LIST_FOREGROUND :
        return COLOR_LIST_FOREGROUND;
      case SWT.COLOR_LIST_BACKGROUND :
        return COLOR_LIST_BACKGROUND;
      case SWT.COLOR_LIST_SELECTION :
        return COLOR_LIST_SELECTION;
      case SWT.COLOR_LIST_SELECTION_TEXT :
        return COLOR_LIST_SELECTION_TEXT;
      default :
        return super.getSystemColor(id);
    }
  }

  /**
   * Returns the user-interface thread for the receiver.
   * 
   * @return the receiver's user-interface thread
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public Thread getThread() {
    if (isDisposed()) {
      SWT.error(SWT.ERROR_DEVICE_DISPOSED);
    }
    return thread;
  }

  @Override
  protected void init() {
    super.init();
    initCallbacks();
  }

  void initCallbacks() {
    com.ibm.ugl.p3ml.OS.Display_InitCallbacks(internal_handle, this);
    // XXX modify
    //OS.Display_RegisterCallback(internal_handle,
    //	OS.CALLBACK_BUTTON,
    //	"org/eclipse/swt/widgets/Button",
    //	"buttonCallback");
    //OS.Display_RegisterCallback(internal_handle,
    //	OS.CALLBACK_LIST,
    //	"org/eclipse/swt/widgets/List",
    //	"listCallback");
    com.ibm.ugl.p3ml.OS.Display_RegisterCallback(
        internal_handle,
        com.ibm.ugl.p3ml.OS.CALLBACK_SHELL,
        "org/eclipse/swt/widgets/Shell",
        "shellCallback");
    //OS.Display_RegisterCallback(internal_handle,
    //	OS.CALLBACK_SCROLLBAR,
    //	"org/eclipse/swt/widgets/Widget",
    //	"scrollbarCallback");
    //OS.Display_RegisterCallback(internal_handle,
    //	OS.CALLBACK_CHOICE,
    //	"org/eclipse/swt/widgets/Combo",
    //	"choiceCallback");
    //OS.Display_RegisterCallback(internal_handle,
    //	OS.CALLBACK_COMBO,
    //	"org/eclipse/swt/widgets/Combo",
    //	"comboCallback");
    //OS.Display_RegisterCallback(internal_handle,
    //	OS.CALLBACK_CONTROL_KEY,
    //	"org/eclipse/swt/widgets/Control",
    //	"keyCallback");
    //OS.Display_RegisterCallback(internal_handle,
    //	OS.CALLBACK_TEXT_MODIFY,
    //	"org/eclipse/swt/widgets/Text",
    //	"textModifyCallback");
    com.ibm.ugl.p3ml.OS.Display_RegisterCallback(
        internal_handle,
        OS.CALLBACK_HAS_MENU,
        "org/eclipse/swt/widgets/Control",
        "hasMenuCallback");
    //OS.Display_RegisterCallback(internal_handle,
    //	OS.CALLBACK_CONTROL_POINTER,
    //	"org/eclipse/swt/widgets/Control",
    //	"pointerCallback");
    com.ibm.ugl.p3ml.OS.Display_RegisterCallback(
        internal_handle,
        com.ibm.ugl.p3ml.OS.CALLBACK_MENU_SELECTION,
        "org/eclipse/swt/widgets/MenuItem",
        "selectionCallback");
    com.ibm.ugl.p3ml.OS.Display_RegisterCallback(
        internal_handle,
        com.ibm.ugl.p3ml.OS.CALLBACK_FOCUS,
        "org/eclipse/swt/widgets/Control",
        "focusCallback");
    com.ibm.ugl.p3ml.OS.Display_RegisterCallback(
        internal_handle,
        com.ibm.ugl.p3ml.OS.CALLBACK_PAINT,
        "org/eclipse/swt/widgets/Control",
        "paintCallback");
    com.ibm.ugl.p3ml.OS.Display_RegisterCallback(
        internal_handle,
        com.ibm.ugl.p3ml.OS.CALLBACK_SHELL_RESIZE,
        "org/eclipse/swt/widgets/Shell",
        "resizeCallback");
    com.ibm.ugl.p3ml.OS.Display_RegisterCallback(
        internal_handle,
        com.ibm.ugl.p3ml.OS.CALLBACK_SHELL_MOVE,
        "org/eclipse/swt/widgets/Shell",
        "moveCallback");
    com.ibm.ugl.p3ml.OS.Display_RegisterCallback(
        internal_handle,
        com.ibm.ugl.p3ml.OS.CALLBACK_DISPLAY_TIMER,
        "org/eclipse/swt/widgets/Display",
        "timerCallback");
    //OS.Display_RegisterCallback(internal_handle,
    //	OS.CALLBACK_TEXT_VERIFY,
    //	"org/eclipse/swt/widgets/Control",
    //	"textVerifyCallback");
    com.ibm.ugl.p3ml.OS.Display_RegisterCallback(
        internal_handle,
        OS.CALLBACK_MENU,
        "org/eclipse/swt/widgets/Menu",
        "menuCallback");
  }

  void initSystemColors() {
    // TODO Add more color ids to OS 
    COLOR_INFO_FOREGROUND =
        Color.internal_newFromHandle(
            this,
            OS.Display_GetSystemColor(internal_handle, com.ibm.ugl.p3ml.OS.COLOR_INFO_FOREGROUND));
    COLOR_INFO_BACKGROUND =
        Color.internal_newFromHandle(
            this,
            OS.Display_GetSystemColor(internal_handle, com.ibm.ugl.p3ml.OS.COLOR_INFO_BACKGROUND));
    COLOR_TITLE_FOREGROUND =
        Color.internal_newFromHandle(
            this,
            OS.Display_GetSystemColor(internal_handle, com.ibm.ugl.p3ml.OS.COLOR_TITLE_FOREGROUND));
    COLOR_TITLE_BACKGROUND =
        Color.internal_newFromHandle(
            this,
            OS.Display_GetSystemColor(internal_handle, com.ibm.ugl.p3ml.OS.COLOR_TITLE_BACKGROUND));
    COLOR_TITLE_BACKGROUND_GRADIENT = COLOR_TITLE_BACKGROUND;
    COLOR_TITLE_INACTIVE_FOREGROUND =
        Color.internal_newFromHandle(this, OS.Display_GetSystemColor(
            internal_handle,
            com.ibm.ugl.p3ml.OS.COLOR_INACTIVE_CAPTION_FOREGROUND));
    COLOR_TITLE_INACTIVE_BACKGROUND =
        Color.internal_newFromHandle(this, OS.Display_GetSystemColor(
            internal_handle,
            com.ibm.ugl.p3ml.OS.COLOR_INACTIVE_CAPTION_BACKGROUND));
    COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT = COLOR_TITLE_INACTIVE_BACKGROUND;
    COLOR_WIDGET_DARK_SHADOW =
        Color.internal_newFromHandle(this, OS.Display_GetSystemColor(
            internal_handle,
            com.ibm.ugl.p3ml.OS.COLOR_CONTROL_SHADOW_DARK));
    COLOR_WIDGET_NORMAL_SHADOW =
        Color.internal_newFromHandle(this, OS.Display_GetSystemColor(
            internal_handle,
            com.ibm.ugl.p3ml.OS.COLOR_CONTROL_SHADOW_NORMAL));
    COLOR_WIDGET_LIGHT_SHADOW = COLOR_WIDGET_NORMAL_SHADOW;
    COLOR_WIDGET_HIGHLIGHT_SHADOW =
        Color.internal_newFromHandle(this, OS.Display_GetSystemColor(
            internal_handle,
            com.ibm.ugl.p3ml.OS.COLOR_CONTROL_HIGHLIGHT_NORMAL));
    COLOR_WIDGET_BACKGROUND =
        Color.internal_newFromHandle(
            this,
            OS.Display_GetSystemColor(internal_handle, com.ibm.ugl.p3ml.OS.COLOR_CONTROL_BACKGROUND));
    COLOR_WIDGET_FOREGROUND =
        Color.internal_newFromHandle(
            this,
            OS.Display_GetSystemColor(internal_handle, com.ibm.ugl.p3ml.OS.COLOR_CONTROL_FOREGROUND));
    COLOR_WIDGET_BORDER = COLOR_WIDGET_FOREGROUND;
    COLOR_LIST_FOREGROUND = COLOR_WIDGET_FOREGROUND;
    COLOR_LIST_BACKGROUND = COLOR_WIDGET_BACKGROUND;
    COLOR_LIST_SELECTION =
        Color.internal_newFromHandle(this, OS.Display_GetSystemColor(
            internal_handle,
            com.ibm.ugl.p3ml.OS.COLOR_TEXT_HIGHLIGHT_BACKGROUND));
    COLOR_LIST_SELECTION_TEXT =
        Color.internal_newFromHandle(this, OS.Display_GetSystemColor(
            internal_handle,
            com.ibm.ugl.p3ml.OS.COLOR_TEXT_HIGHLIGHT_FOREGROUND));
    systemColorsCreated = true;
  }

  public void internal_copyArea(int imageHandle, int x, int y, int width, int height) {
    OS.Device_CopyArea(internal_handle, imageHandle, x, y, width, height);
  }

  /**
   * Invokes platform specific functionality to dispose a GC handle.
   * <p>
   * <b>IMPORTANT:</b> This method is <em>not</em> part of the public API for <code>Display</code>.
   * It is marked public only so that it can be shared within the packages provided by SWT. It is
   * not available on all platforms, and should never be called from application code.
   * </p>
   * 
   * @param handle
   *          the platform specific GC handle
   * @param data
   *          the platform specific GC data
   * 
   */
  @Override
  public void internal_dispose_GC(int gc, InternalGCData data) {
    // do nothing. GCs disposed in GC.dispose
  }

  public Rectangle internal_getBounds() {
    return getBounds();
  }

  public int internal_getDepth() {
    return OS.Device_GetDepth(internal_handle);
  }

  /**
   * Invokes platform specific functionality to allocate a new GC handle.
   * <p>
   * <b>IMPORTANT:</b> This method is <em>not</em> part of the public API for <code>Display</code>.
   * It is marked public only so that it can be shared within the packages provided by SWT. It is
   * not available on all platforms, and should never be called from application code.
   * </p>
   * 
   * @param data
   *          the platform specific GC data
   * @return the platform specific GC handle
   * 
   * @private
   */
  @Override
  public int internal_new_GC(InternalGCData data) {
    checkDevice();
    int graphics = OS.Device_NewGraphics(internal_handle);
    data.device = this;
    data.font = getSystemFont();
    return graphics;
  }

  static boolean isValidClass(Class clazz) {
    String name = clazz.getName();
    int index = name.lastIndexOf('.');
    // is standard swt widget
    if (name.substring(0, index + 1).equals(PACKAGE_NAME)) {
      return true;
    }
    // allow mobile extensions
    if (name.substring(0, index + 1).equals("org.eclipse.ercp.swt.mobile.")) {
      return true;
    }
    return false;
  }

  boolean isValidThread() {
    return thread == Thread.currentThread();
  }

  /**
   * Maps a point from one coordinate system to another. When the control is null, coordinates are
   * mapped to the display.
   * <p>
   * NOTE: On right-to-left platforms where the coordinate systems are mirrored, special care needs
   * to be taken when mapping coordinates from one control to another to ensure the result is
   * correctly mirrored.
   * 
   * Mapping a point that is the origin of a rectangle and then adding the width and height is not
   * equivalent to mapping the rectangle. When one control is mirrored and the other is not, adding
   * the width and height to a point that was mapped causes the rectangle to extend in the wrong
   * direction. Mapping the entire rectangle instead of just one point causes both the origin and
   * the corner of the rectangle to be mapped.
   * </p>
   * 
   * @param from
   *          the source <code>Control</code> or <code>null</code>
   * @param to
   *          the destination <code>Control</code> or <code>null</code>
   * @param point
   *          to be mapped
   * @return point with mapped coordinates
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been
   *              disposed</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @since 2.1.2
   */
  public Point map(Control from, Control to, Point point) {
    checkDevice();
    if (point == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    return map(from, to, point.x, point.y);
  }

  /**
   * Maps a point from one coordinate system to another. When the control is null, coordinates are
   * mapped to the display.
   * <p>
   * NOTE: On right-to-left platforms where the coordinate systems are mirrored, special care needs
   * to be taken when mapping coordinates from one control to another to ensure the result is
   * correctly mirrored.
   * 
   * Mapping a point that is the origin of a rectangle and then adding the width and height is not
   * equivalent to mapping the rectangle. When one control is mirrored and the other is not, adding
   * the width and height to a point that was mapped causes the rectangle to extend in the wrong
   * direction. Mapping the entire rectangle instead of just one point causes both the origin and
   * the corner of the rectangle to be mapped.
   * </p>
   * 
   * @param from
   *          the source <code>Control</code> or <code>null</code>
   * @param to
   *          the destination <code>Control</code> or <code>null</code>
   * @param x
   *          coordinates to be mapped
   * @param y
   *          coordinates to be mapped
   * @return point with mapped coordinates
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been
   *              disposed</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @since 2.1.2
   */
  public Point map(Control from, Control to, int x, int y) {
    checkDevice();
    if (from != null && from.isDisposed()) {
      SWT.error(SWT.ERROR_INVALID_ARGUMENT);
    }
    if (to != null && to.isDisposed()) {
      SWT.error(SWT.ERROR_INVALID_ARGUMENT);
    }
    Point point = new Point(x, y);
    if (to != null && from != null) {
      point = from.toDisplay(point);
      point = to.toControl(point);
    } else if (to != null) {
      point = to.toControl(point);
    } else if (from != null) {
      point = from.toDisplay(point);
    }
    return point;
  }

  /**
   * Maps a point from one coordinate system to another. When the control is null, coordinates are
   * mapped to the display.
   * <p>
   * NOTE: On right-to-left platforms where the coordinate systems are mirrored, special care needs
   * to be taken when mapping coordinates from one control to another to ensure the result is
   * correctly mirrored.
   * 
   * Mapping a point that is the origin of a rectangle and then adding the width and height is not
   * equivalent to mapping the rectangle. When one control is mirrored and the other is not, adding
   * the width and height to a point that was mapped causes the rectangle to extend in the wrong
   * direction. Mapping the entire rectangle instead of just one point causes both the origin and
   * the corner of the rectangle to be mapped.
   * </p>
   * 
   * @param from
   *          the source <code>Control</code> or <code>null</code>
   * @param to
   *          the destination <code>Control</code> or <code>null</code>
   * @param rectangle
   *          to be mapped
   * @return rectangle with mapped coordinates
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been
   *              disposed</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @since 2.1.2
   */
  public Rectangle map(Control from, Control to, Rectangle rectangle) {
    checkDevice();
    if (rectangle == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    return map(from, to, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
  }

  /**
   * Maps a point from one coordinate system to another. When the control is null, coordinates are
   * mapped to the display.
   * <p>
   * NOTE: On right-to-left platforms where the coordinate systems are mirrored, special care needs
   * to be taken when mapping coordinates from one control to another to ensure the result is
   * correctly mirrored.
   * 
   * Mapping a point that is the origin of a rectangle and then adding the width and height is not
   * equivalent to mapping the rectangle. When one control is mirrored and the other is not, adding
   * the width and height to a point that was mapped causes the rectangle to extend in the wrong
   * direction. Mapping the entire rectangle instead of just one point causes both the origin and
   * the corner of the rectangle to be mapped.
   * </p>
   * 
   * @param from
   *          the source <code>Control</code> or <code>null</code>
   * @param to
   *          the destination <code>Control</code> or <code>null</code>
   * @param x
   *          coordinates to be mapped
   * @param y
   *          coordinates to be mapped
   * @param width
   *          coordinates to be mapped
   * @param height
   *          coordinates to be mapped
   * @return rectangle with mapped coordinates
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been
   *              disposed</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @since 2.1.2
   */
  public Rectangle map(Control from, Control to, int x, int y, int width, int height) {
    checkDevice();
    if (from != null && from.isDisposed()) {
      SWT.error(SWT.ERROR_INVALID_ARGUMENT);
    }
    if (to != null && to.isDisposed()) {
      SWT.error(SWT.ERROR_INVALID_ARGUMENT);
    }
    // TODO: need to take right alignment into account for rectangle.x
    Rectangle rectangle = new Rectangle(x, y, width, height);
    if (to != null && from != null) {
      Point topLeft = from.toDisplay(x, y);
      topLeft = to.toControl(topLeft);
      rectangle.x = topLeft.x;
      rectangle.y = topLeft.y;
    } else if (to != null) {
      Point topLeft = to.toControl(x, y);
      rectangle.x = topLeft.x;
      rectangle.y = topLeft.y;
    } else if (from != null) {
      Point topLeft = from.toDisplay(x, y);
      rectangle.x = topLeft.x;
      rectangle.y = topLeft.y;
    }
    return rectangle;
  }

  /**
   * Generate a low level system event.
   * 
   * <code>post</code> is used to generate low level keyboard and mouse events. The intent is to
   * enable automated UI testing by simulating the input from the user. Most SWT applications should
   * never need to call this method.
   * <p>
   * Note that this operation can fail when the operating system fails to generate the event for any
   * reason. For example, this can happen when there is no such key or mouse button or when the
   * system event queue is full.
   * </p>
   * <p>
   * <b>Event Types:</b>
   * <p>
   * KeyDown, KeyUp
   * <p>
   * The following fields in the <code>Event</code> apply:
   * <ul>
   * <li>(in) type KeyDown or KeyUp</li>
   * <p>
   * Either one of:
   * <li>(in) character a character that corresponds to a keyboard key</li>
   * <li>(in) keyCode the key code of the key that was typed, as defined by the key code constants
   * in class <code>SWT</code></li>
   * </ul>
   * <p>
   * MouseDown, MouseUp
   * </p>
   * <p>
   * The following fields in the <code>Event</code> apply:
   * <ul>
   * <li>(in) type MouseDown or MouseUp
   * <li>(in) button the button that is pressed or released
   * </ul>
   * <p>
   * MouseMove
   * </p>
   * <p>
   * The following fields in the <code>Event</code> apply:
   * <ul>
   * <li>(in) type MouseMove
   * <li>(in) x the x coordinate to move the mouse pointer to in screen coordinates
   * <li>(in) y the y coordinate to move the mouse pointer to in screen coordinates
   * </ul>
   * </dl>
   * 
   * @param event
   *          the event to be generated
   * 
   * @return true if the event was generated or false otherwise
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the event is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @since 3.0
   * 
   */
  public boolean post(Event event) {
    if (isDisposed()) {
      SWT.error(SWT.ERROR_DEVICE_DISPOSED);
    }
    if (event == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    int type = event.type;
    switch (type) {
      case SWT.KeyDown :
      case SWT.KeyUp : {
        char character = event.character;
        int uglKeyCode = convertToUglKeyCode(event.keyCode);
        if (character > 0 && uglKeyCode == event.keyCode) {
          uglKeyCode = character;
        }
        // SWT treats DEL as a character and UGL
        // would prefer the DEL as a keycode.  Without this, the
        // mapping in the UGL will not work.
        switch (character) {
          case SWT.DEL :
            uglKeyCode = OS.KEYCODE_DELETE;
            character = 0;
            break;
          case SWT.BS :
            uglKeyCode = OS.KEYCODE_BACKSPACE;
            character = 0;
            break;
          case SWT.CR :
            uglKeyCode = OS.KEYCODE_ENTER;
            character = 0;
            break;
          case SWT.ESC :
            uglKeyCode = OS.KEYCODE_ESCAPE;
            character = 0;
            break;
          case SWT.TAB :
            uglKeyCode = OS.KEYCODE_TAB;
            character = 0;
            break;
          case SWT.LF :
            uglKeyCode = OS.KEYCODE_ENTER;
            character = 0;
            break;
        }
        if (uglKeyCode < 0) {
          return false; // Don't allow invalid keycodes to the native layer
        }
        int keyEventType = type == SWT.KeyUp ? OS.EVENT_KEY_UP : OS.EVENT_KEY_DOWN;
        return OS.Display_PostKeyEvent(internal_handle, keyEventType, uglKeyCode, character);
      }
      case SWT.MouseDown :
      case SWT.MouseUp : {
        int button;
        switch (event.button) {
          case 1 :
            button = OS.POINTER_MASK_BUTTON1;
            break;
          case 2 :
            button = OS.POINTER_MASK_BUTTON2;
            break;
          case 3 :
            button = OS.POINTER_MASK_BUTTON3;
            break;
          default :
            return false;
        }
        int mouseEventType = type == SWT.MouseDown ? OS.EVENT_POINTER_DOWN : OS.EVENT_POINTER_UP;
        return OS.Display_PostPointerEvent(internal_handle, mouseEventType, button);
      }
      case SWT.MouseMove : {
        int x = event.x;
        int y = event.y;
        return OS.Display_PostPointerMoveEvent(internal_handle, x, y);
      }
    }
    return false;
  }

  void postEvent(Event event) {
    /**
     * Place the event at the end of the event queue. This code is always called in the Display's
     * thread so it must be re-entrant but does not need to be synchronized.
     */
    if (eventQueue == null) {
      eventQueue = new Event[4];
    }
    int index = 0;
    int length = eventQueue.length;
    while (index < length) {
      if (eventQueue[index] == null) {
        break;
      }
      index++;
    }
    if (index == length) {
      Event[] newQueue = new Event[length + 4];
      System.arraycopy(eventQueue, 0, newQueue, 0, length);
      eventQueue = newQueue;
    }
    eventQueue[index] = event;
  }

  /**
   * Reads an event from the operating system's event queue, dispatches it appropriately, and
   * returns <code>true</code> if there is potentially more work to do, or <code>false</code> if the
   * caller can sleep until another event is placed on the event queue.
   * <p>
   * In addition to checking the system event queue, this method also checks if any inter-thread
   * messages (created by <code>syncExec()</code> or <code>asyncExec()</code>) are waiting to be
   * processed, and if so handles them before returning.
   * </p>
   * 
   * @return <code>false</code> if the caller can sleep upon return from this method
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_FAILED_EXEC - if an exception occurred while running an inter-thread
   *              message</li>
   *              </ul>
   * 
   * @see #sleep
   * @see #wake
   */
  public boolean readAndDispatch() {
    checkDevice();
    if (OS.Display_ReadAndDispatch(internal_handle)) {
      runDeferredEvents();
      return true;
    } else {
      return runAsyncMessages();
    }
  }

  protected void release() {
    sendEvent(SWT.Dispose, new Event());
    // release Shells
    if (shells != null && shells.size() > 0) {
      Enumeration elementEnum = shells.elements();
      while (elementEnum.hasMoreElements()) {
        Shell shell = (Shell) elementEnum.nextElement();
        if (!shell.isDisposed()) {
          if (this == shell.getDisplay()) {
            shell.dispose();
          }
        }
      }
    }
    while (readAndDispatch()) {
    }
    runDeferredEvents();
    runAsyncMessages();
    /* Run dispose list */
    if (disposeList != null) {
      for (int i = 0; i < disposeList.length; i++) {
        if (disposeList[i] != null) {
          disposeList[i].run();
        }
      }
    }
    disposeList = null;
    if (systemColorsCreated) {
      releaseSystemColors();
    }
    /* Release synchronizer */
    synchronizer.releaseSynchronizer();
    synchronizer = null;
    releaseDisplay();
    super.release();
  }

  void releaseDisplay() {
    /* Release references */
    thread = null;
    data = null;
    keys = null;
    values = null;
  }

  void releaseSystemColors() {
    COLOR_INFO_FOREGROUND.dispose();
    COLOR_INFO_BACKGROUND.dispose();
    COLOR_TITLE_FOREGROUND.dispose();
    COLOR_TITLE_BACKGROUND.dispose();
    COLOR_TITLE_BACKGROUND_GRADIENT.dispose();
    COLOR_TITLE_INACTIVE_FOREGROUND.dispose();
    COLOR_TITLE_INACTIVE_BACKGROUND.dispose();
    COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT.dispose();
    COLOR_WIDGET_DARK_SHADOW.dispose();
    COLOR_WIDGET_NORMAL_SHADOW.dispose();
    COLOR_WIDGET_LIGHT_SHADOW.dispose();
    COLOR_WIDGET_HIGHLIGHT_SHADOW.dispose();
    COLOR_WIDGET_BACKGROUND.dispose();
    COLOR_WIDGET_FOREGROUND.dispose();
    COLOR_WIDGET_BORDER.dispose();
    COLOR_LIST_FOREGROUND.dispose();
    COLOR_LIST_BACKGROUND.dispose();
    COLOR_LIST_SELECTION.dispose();
    COLOR_LIST_SELECTION_TEXT.dispose();
  }

  /**
   * Removes the listener from the collection of listeners who will be notifed when an event of the
   * given type occurs anywhere in this display.
   * 
   * @param eventType
   *          the type of event to listen for
   * @param listener
   *          the listener which should no longer be notified when the event occurs
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              </ul>
   * 
   * @see Listener
   * @see #addFilter
   * @see #addListener
   * 
   * @since 3.0
   */
  public void removeFilter(int eventType, Listener listener) {
    checkDevice();
    if (listener == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    if (filterTable == null) {
      return;
    }
    filterTable.unhook(eventType, listener);
    if (filterTable.size() == 0) {
      filterTable = null;
    }
  }

  /**
   * Removes the listener from the collection of listeners who will be notifed when an event of the
   * given type occurs.
   * 
   * @param eventType
   *          the type of event to listen for
   * @param listener
   *          the listener which should no longer be notified when the event occurs
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see Listener
   * @see #addListener
   * 
   * @since 2.0
   */
  public void removeListener(int eventType, Listener listener) {
    checkDevice();
    if (listener == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    if (eventTable == null) {
      return;
    }
    eventTable.unhook(eventType, listener);
  }

  boolean runAsyncMessages() {
    return synchronizer.runAsyncMessages();
  }

  boolean runDeferredEvents() {
    /*
     * Run deferred events.  This code is always
     * called  in the Display's thread so it must
     * be re-enterant but need not be synchronized.
     */
    while (eventQueue != null) {
      /* Take an event off the queue */
      Event event = eventQueue[0];
      if (event == null) {
        break;
      }
      int length = eventQueue.length;
      System.arraycopy(eventQueue, 1, eventQueue, 0, --length);
      eventQueue[length] = null;
      /* Run the event */
      Widget widget = event.widget;
      if (widget != null && !widget.isDisposed()) {
        Widget item = event.item;
        if (item == null || !item.isDisposed()) {
          widget.notifyListeners(event.type, event);
        }
      }
      /*
       * At this point, the event queue could
       * be null due to a recursive invokation
       * when running the event.
       */
    }
    /* Clear the queue */
    eventQueue = null;
    return true;
  }

  int getLastEventTime() {
    return (int) System.currentTimeMillis() & 0x7FFFFFFF;
  }

  void sendEvent(int eventType, Event event) {
    if (eventTable == null && filterTable == null) {
      return;
    }
    if (event == null) {
      event = new Event();
    }
    event.display = this;
    event.type = eventType;
    if (event.time == 0) {
      event.time = getLastEventTime();
    }
    if (!filterEvent(event)) {
      if (eventTable != null) {
        eventTable.sendEvent(event);
      }
    }
  }

  /**
   * On platforms which support it, sets the application name to be the argument. On Motif, for
   * example, this can be used to set the name used for resource lookup.
   * 
   * @param name
   *          the new app name
   */
  public static void setAppName(String name) {
    OS.Display_SetAppName(name);
  }

  /**
   * Sets the application defined property of the receiver with the specified name to the given
   * argument.
   * <p>
   * Applications may have associated arbitrary objects with the receiver in this fashion. If the
   * objects stored in the properties need to be notified when the display is disposed of, it is the
   * application's responsibility provide a <code>disposeExec()</code> handler which does so.
   * </p>
   * 
   * @param key
   *          the name of the property
   * @param value
   *          the new value for the property
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the key is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see #getData(String)
   * @see #disposeExec(Runnable)
   */
  public void setData(String key, Object value) {
    checkDevice();
    if (key == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    /* Remove the key/value pair */
    if (value == null) {
      if (keys == null) {
        return;
      }
      int index = 0;
      while (index < keys.length && !keys[index].equals(key)) {
        index++;
      }
      if (index == keys.length) {
        return;
      }
      if (keys.length == 1) {
        keys = null;
        values = null;
      } else {
        String[] newKeys = new String[keys.length - 1];
        Object[] newValues = new Object[values.length - 1];
        System.arraycopy(keys, 0, newKeys, 0, index);
        System.arraycopy(keys, index + 1, newKeys, index, newKeys.length - index);
        System.arraycopy(values, 0, newValues, 0, index);
        System.arraycopy(values, index + 1, newValues, index, newValues.length - index);
        keys = newKeys;
        values = newValues;
      }
      return;
    }
    /* Add the key/value pair */
    if (keys == null) {
      keys = new String[]{key};
      values = new Object[]{value};
      return;
    }
    for (int i = 0; i < keys.length; i++) {
      if (keys[i].equals(key)) {
        values[i] = value;
        return;
      }
    }
    String[] newKeys = new String[keys.length + 1];
    Object[] newValues = new Object[values.length + 1];
    System.arraycopy(keys, 0, newKeys, 0, keys.length);
    System.arraycopy(values, 0, newValues, 0, values.length);
    newKeys[keys.length] = key;
    newValues[values.length] = value;
    keys = newKeys;
    values = newValues;
  }

  /**
   * Sets the application defined, display specific data associated with the receiver, to the
   * argument. The <em>display specific data</em> is a single, unnamed field that is stored with
   * every display.
   * <p>
   * Applications may put arbitrary objects in this field. If the object stored in the display
   * specific data needs to be notified when the display is disposed of, it is the application's
   * responsibility provide a <code>disposeExec()</code> handler which does so.
   * </p>
   * 
   * @param data
   *          the new display specific data
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see #getData()
   * @see #disposeExec(Runnable)
   */
  public void setData(Object data) {
    checkDevice();
    this.data = data;
  }

  /**
   * Sets the synchronizer used by the display to be the argument, which can not be null.
   * 
   * @param synchronizer
   *          the new synchronizer for the display (must not be null)
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the synchronizer is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_FAILED_EXEC - if an exception occurred while running an inter-thread
   *              message</li>
   *              </ul>
   */
  public void setSynchronizer(Synchronizer synchronizer) {
    checkDevice();
    if (synchronizer == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    if (this.synchronizer != null) {
      this.synchronizer.runAsyncMessages();
    }
    this.synchronizer = synchronizer;
  }

  /**
   * Causes the user-interface thread to <em>sleep</em> (that is, to be put in a state where it does
   * not consume CPU cycles) until an event is received or it is otherwise awakened.
   * 
   * @return <code>true</code> if an event requiring dispatching was placed on the queue.
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see #wake
   */
  public boolean sleep() {
    checkDevice();
    OS.Display_Sleep(internal_handle);
    runDeferredEvents();
    return true;
  }

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the user-interface thread
   * at the next reasonable opportunity. The thread which calls this method is suspended until the
   * runnable completes. Specifying <code>null</code> as the runnable simply wakes the
   * user-interface thread.
   * <p>
   * Note that at the time the runnable is invoked, widgets that have the receiver as their display
   * may have been disposed. Therefore, it is necessary to check for this case inside the runnable
   * before accessing the widget.
   * </p>
   * 
   * @param runnable
   *          code to run on the user-interface thread or <code>null</code>
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_FAILED_EXEC - if an exception occured when executing the runnable</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see #asyncExec
   */
  public void syncExec(Runnable runnable) {
    if (isDisposed()) {
      SWT.error(SWT.ERROR_DEVICE_DISPOSED);
    }
    synchronizer.syncExec(runnable);
  }

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the user-interface thread
   * after the specified number of milliseconds have elapsed. If milliseconds is less than zero, the
   * runnable is not executed.
   * <p>
   * Note that at the time the runnable is invoked, widgets that have the receiver as their display
   * may have been disposed. Therefore, it is necessary to check for this case inside the runnable
   * before accessing the widget.
   * </p>
   * 
   * @param milliseconds
   *          the delay before running the runnable
   * @param runnable
   *          code to run on the user-interface thread
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the runnable is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see #asyncExec
   */
  public void timerExec(int milliseconds, Runnable runnable) {
    checkDevice();
    if (runnable == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    if (milliseconds < 0) {
      return;
    }
    if (timers == null) {
      timers = new Runnable[4];
    }
    int index = 0;
    while (index < timers.length) {
      if (timers[index] == null) {
        break;
      }
      index++;
    }
    if (index == timers.length) {
      Runnable[] newTimers = new Runnable[timers.length + 4];
      System.arraycopy(timers, 0, newTimers, 0, timers.length);
      timers = newTimers;
    }
    timers[index] = runnable;
    OS.Display_StartTimer(internal_handle, milliseconds, index);
  }

  void timerCallback(int id) {
    if (id < 0 || id >= timers.length) {
      return;
    }
    Runnable r = timers[id];
    if (r != null) {
      syncExec(r);
    }
    timers[id] = null;
  }

  /**
   * Forces all outstanding paint requests for the display to be processed before this method
   * returns.
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see Control#update()
   */
  public void update() {
    checkDevice();
    if (shells != null) {
      Enumeration elementEnum = shells.elements();
      while (elementEnum.hasMoreElements()) {
        Shell shell = (Shell) elementEnum.nextElement();
        if (!shell.isDisposed() && this == shell.getDisplay()) {
          shell.update(true);
        }
      }
    }
  }

  /**
   * If the receiver's user-interface thread was <code>sleep</code>ing, causes it to be awakened and
   * start running again. Note that this method may be called from any thread.
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * 
   * @see #sleep
   */
  public void wake() {
    if (isDisposed()) {
      SWT.error(SWT.ERROR_DEVICE_DISPOSED);
    }
    wakeThread();
  }

  void wakeThread() {
    OS.Display_Wake(internal_handle);
  }

  public Rectangle internal_getDefaultClipping() {
    return getBounds();
  }
}