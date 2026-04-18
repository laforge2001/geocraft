/*
 * Copyright (C) ConocoPhillips 2026 All Rights Reserved.
 */
package org.geocraft.ui.plot.util;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;


/**
 * Shared cache of SWT Color and Font resources for the plot UI.
 * <p>
 * Resources are created once per unique {@link RGB} / {@link FontData} key and
 * reused across every paint. Callers must NOT dispose the returned handles —
 * the cache releases them when the owning {@link Display} is disposed.
 * <p>
 * This replaces the pattern of <code>new Color(device, rgb)</code> / <code>new
 * Font(null, ...)</code> inside paint listeners and per-instance constructors,
 * which was leaking native handles on every paint and crashing the JVM on
 * macOS when the leaked backing store was touched from Cocoa.
 */
public final class PlotResources {

  private static final Map<RGB, Color> COLORS = new ConcurrentHashMap<>();

  private static final Map<FontData, Font> FONTS = new ConcurrentHashMap<>();

  private static boolean _disposeHookInstalled;

  private PlotResources() {
    // static only
  }

  public static Color getColor(final RGB rgb) {
    if (rgb == null) {
      return null;
    }
    Display display = display();
    Color cached = COLORS.get(rgb);
    if (cached != null && !cached.isDisposed()) {
      return cached;
    }
    Color fresh = new Color(display, rgb);
    Color existing = COLORS.putIfAbsent(rgb, fresh);
    if (existing != null && !existing.isDisposed()) {
      fresh.dispose();
      return existing;
    }
    if (existing != null) {
      COLORS.put(rgb, fresh);
    }
    return fresh;
  }

  public static Font getFont(final FontData data) {
    if (data == null) {
      return null;
    }
    Display display = display();
    Font cached = FONTS.get(data);
    if (cached != null && !cached.isDisposed()) {
      return cached;
    }
    Font fresh = new Font(display, data);
    Font existing = FONTS.putIfAbsent(data, fresh);
    if (existing != null && !existing.isDisposed()) {
      fresh.dispose();
      return existing;
    }
    if (existing != null) {
      FONTS.put(data, fresh);
    }
    return fresh;
  }

  public static Font getFont(final FontData[] data) {
    if (data == null || data.length == 0) {
      return null;
    }
    return getFont(data[0]);
  }

  public static Font getFont(final String name, final int height, final int style) {
    return getFont(new FontData(name, height, style));
  }

  public static Font getDefaultPlotFont() {
    return getFont("SansSerif", 8, SWT.NORMAL);
  }

  private static Display display() {
    Display display = Display.getCurrent();
    if (display == null) {
      display = Display.getDefault();
    }
    installDisposeHook(display);
    return display;
  }

  private static synchronized void installDisposeHook(final Display display) {
    if (_disposeHookInstalled || display == null || display.isDisposed()) {
      return;
    }
    _disposeHookInstalled = true;
    display.disposeExec(new Runnable() {

      @Override
      public void run() {
        for (Color c : COLORS.values()) {
          if (c != null && !c.isDisposed()) {
            c.dispose();
          }
        }
        COLORS.clear();
        for (Font f : FONTS.values()) {
          if (f != null && !f.isDisposed()) {
            f.dispose();
          }
        }
        FONTS.clear();
        _disposeHookInstalled = false;
      }
    });
  }
}
