/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.algorithm;


import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;


public class StandaloneAlgorithmDescription implements IStandaloneAlgorithmDescription {

  /** The configuration element containing the algorithm info. */
  private final IConfigurationElement _configElement;

  public StandaloneAlgorithmDescription(final IConfigurationElement description) {
    _configElement = description;
  }

  public String getName() {
    return _configElement.getAttribute("name");
  }

  public String getHelpId() {
    return _configElement.getAttribute("helpId");
  }

  public boolean isVisible() {
    return Boolean.parseBoolean(_configElement.getAttribute("visible"));
  }

  public String getCategory() {
    return _configElement.getAttribute("category");
  }

  public String getFullPath() {
    return getCategory() + "/" + getName();
  }

  public String getClassName() {
    return _configElement.getAttribute("class");
  }

  public String getToolTip() {
    String tip = "";
    String version = getVersion();
    if (version != null && !version.isEmpty()) {
      tip = "Version: " + getVersion() + "\n";
    }
    tip += _configElement.getAttribute("tooltip");
    return tip;
  }

  public String getVersion() {
    return _configElement.getAttribute("version");
  }

  public StandaloneAlgorithm createAlgorithm() {
    try {
      StandaloneAlgorithm algorithm = (StandaloneAlgorithm) _configElement.createExecutableExtension("class");
      return algorithm;
    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public ImageDescriptor getIcon() {
    String iconName = _configElement.getAttribute("icon");
    if (iconName == null || iconName.length() == 0) {
      return null;
    }
    String bundleName = getBundleName();
    URL url = FileLocator.find(Platform.getBundle(bundleName), new Path(iconName), null);
    if (url != null) {
      return ImageDescriptor.createFromURL(url);
    }
    return null;
  }

  private String getBundleName() {
    return _configElement.getContributor().getName();
  }

  public StandaloneAlgorithmEditorPage createEditorPage(final SharedHeaderFormEditor sharedHeaderFormEditor) {
    StandaloneAlgorithm algorithm = createAlgorithm();
    return new StandaloneAlgorithmEditorPage(sharedHeaderFormEditor, this, algorithm);
  }

  public StandaloneAlgorithmEditorPage createEditorPage(final SharedHeaderFormEditor sharedHeaderFormEditor,
      StandaloneAlgorithm algorithm) {
    return new StandaloneAlgorithmEditorPage(sharedHeaderFormEditor, this, algorithm);
  }

  public String getUsageName() {
    return _configElement.getAttribute("usageName");
  }

  public String dumpDescription() {
    StringBuffer sbuf = new StringBuffer("Standalone Algorithm Description:\n");
    sbuf.append("  name: " + getName() + "\n");
    sbuf.append("  helpId: " + getHelpId() + "\n");
    sbuf.append("  visible: " + isVisible() + "\n");
    sbuf.append("  category: " + getCategory() + "\n");
    sbuf.append("  full path: " + getFullPath() + "\n");
    sbuf.append("  class name: " + getClassName() + "\n");
    sbuf.append("  tool tip: " + getToolTip() + "\n");
    sbuf.append("  version: " + getVersion() + "\n");
    sbuf.append("  bundle name: " + getBundleName() + "\n");
    sbuf.append("  usage name: " + getUsageName() + "\n");

    return sbuf.toString();
  }
}
