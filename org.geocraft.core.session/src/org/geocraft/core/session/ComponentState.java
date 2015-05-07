/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.session;


import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.common.util.GeoCraftVersion;


/** 
 * XML generators of component state. A component can be
 * the repository, an algorithm, or a veiwer. The XML session
 * state file to write to was opened by the SessionManager
 * which contains the method to write generated XML.
 */
public class ComponentState {

  static String _indent = "";

  static void incrIndent() {
    _indent += "  ";
  }

  static void decrIndent() {
    _indent = _indent.substring(2);
  }

  /**
   * Write the XML Prologue
   */
  static public void writePrologue() {
    SessionManager.getInstance().writeXML("<?xml version=\"1.0\" ?>");
    //no DTD
    //no style sheet
  }

  /**
   * Write the <geocraft> root element
   */
  static public void openRootElement() {
    String version = GeoCraftVersion.getCurrentVersion();
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();

    //Window attributes
    int width, height, x, y;
    width = activeWindow.getShell().getSize().x;
    height = activeWindow.getShell().getSize().y;
    x = activeWindow.getShell().getLocation().x;
    y = activeWindow.getShell().getLocation().y;

    SessionManager.getInstance().writeXML(
        "<geocraft version=\"" + version + "\" width=\"" + width + "\" height=\"" + height + "\" x=\"" + x + "\" y=\""
            + y + "\">");
    incrIndent();
  }

  /**
   * Write the </geocraft> root element
   */
  static public void closeRootElement() {
    SessionManager.getInstance().writeXML("</geocraft>");
    decrIndent();
  }

  /**
   * Write the <repository> element.
   */
  static public void openRepositoryElement() {
    SessionManager.getInstance().writeXML(_indent + "<repository>");
    incrIndent();
  }

  /**
   * Write the </repository> element.
   */
  static public void closeRepositoryElement() {
    decrIndent();
    SessionManager.getInstance().writeXML(_indent + "</repository>");
  }

  /**
   * Write the <entities> element.
   */
  static public void openEntitiesElement() {
    SessionManager.getInstance().writeXML(_indent + "<entities>");
    incrIndent();
  }

  /**
   * Write the </entities> element.
   */
  static public void closeEntitiesElement() {
    decrIndent();
    SessionManager.getInstance().writeXML(_indent + "</entities>");
  }

  /**
   * Write the <entity> element.
   * @param varName var name for the entity
   * @param uniqueId Unique ID for the entity
   */
  static public void openEntityElement(String varName, String uniqueId) {
    SessionManager.getInstance().writeXML(
        _indent + "<entity varName=\"" + varName + "\" uniqueId=\"" + uniqueId + "\">");
    incrIndent();
  }

  /**
   * Write the </entity> element.
   */
  static public void closeEntityElement() {
    decrIndent();
    SessionManager.getInstance().writeXML(_indent + "</entity>");
  }

  /**
   * Write the <property> element.
   */
  static public void entityPropertyElement(String key, String value) {
    SessionManager.getInstance().writeXML(_indent + "<property key=\"" + key + "\" value=\"" + value + "\"/>");
  }

  /**
   * Write the <algorithms> element.
   */
  static public void openAlgorithmsElement() {
    SessionManager.getInstance().writeXML(_indent + "<algorithms>");
    incrIndent();
  }

  /**
   * Write the </algorithms> element.
   */
  static public void closeAlgorithmsElement() {
    decrIndent();
    SessionManager.getInstance().writeXML(_indent + "</algorithms>");
  }

  /**
   * Write the <algorithm> element.
   * @param name Name of the algorithm
   * @param klass Qualified class name of the algorithm
   * @param windowID ID of the workbench window containing the algorithm
   * @param uniqueId Unique ID for the algorithm
   * @param batch true if saving batch state, false if saving session state
   */
  static public void openAlgorithmElement(String name, String klass, String windowID, int uniqueId, boolean batch) {
    String action = batch ? "\"execute\"" : "\"load\"";
    SessionManager.getInstance().writeXML(
        _indent + "<algorithm name=\"" + name + "\" class=\"" + klass + "\" windowID=\"" + windowID + "\" uniqueID=\""
            + uniqueId + "\" action=" + action + ">");
    incrIndent();
  }

  /**
   * Write the </algorithm> element.
   */
  static public void closeAlgorithmElement() {
    decrIndent();
    SessionManager.getInstance().writeXML(_indent + "</algorithm>");
  }

  /**
   * Write the algorithm <parameter/> element.
   */
  static public void algorithmParameterElement(String key, String value) {
    SessionManager.getInstance().writeXML(_indent + "<parameter key=\"" + key + "\" value=\"" + value + "\"/>");
  }

  /**
   * Write the <viewerPart> element.
   * @param klass Viewer part's class
   * @param partID Viewer part's ID
   * @param windowID ID of workbench window containing viewer part
   * @param perspectiveID ID of the perspective in the workbench window containing the viewer part
   * @param uniqueId Unique ID for the viewer part
   */
  static public void openViewerPartElement(String klass, String partId, String windowID, String perspectiveID,
      int uniqueId) {
    SessionManager.getInstance().writeXML(
        _indent + "<viewerPart class=\"" + klass + "\" partID=\"" + partId + "\" windowID=\"" + windowID
            + "\" perspectiveID=\"" + perspectiveID + "\" uniqueID=\"" + uniqueId + "\">");
    incrIndent();
  }

  /**
   * Write the </viewerPart> element.
   */
  static public void closeViewerPartElement() {
    decrIndent();
    SessionManager.getInstance().writeXML(_indent + "</viewerPart>");
  }

  /**
   * Generate the <viewer> element.
   * @param title Title of the viewer
   * @param partClass Qualified class name of the viewer part
   * @return XML for the <viewer> element.
   */
  static public String openViewerElement(String title, String klass) {
    String s = _indent + "<viewer title=\"" + title + "\" class=\"" + klass + "\">";
    incrIndent();
    return s;
  }

  /**
   * Generate the </viewer> element.
   */
  static public String closeViewerElement() {
    decrIndent();
    return _indent + "</viewer>";
  }

  /** Generate the <viewerLayer/> element */
  static public String viewerLayerElement(String layerName, boolean checked) {
    return _indent + "<viewerLayer layerName=\"" + layerName + "\" checked=\"" + (checked ? "true" : "false") + "\"/>";
  }

  /**
   * Generate the <viewerProperty/> element.
   */
  static public String viewerParameterElement(String key, String value) {
    return _indent + "<viewerProperty key=\"" + key + "\" value=\"" + value + "\"/>";
  }

  /**
   * Generate the <renderer> element.
   * @param klass Qualified class name of the renderer
   * @param uniqueId Unique ID of the renderer
   * @return <renderer> XML for the <renderer> element
   */
  static public String openRendererElement(String klass, int uniqueId) {
    String s = _indent + "<renderer class=\"" + klass + "\" uniqueID=\"" + uniqueId + "\">";
    incrIndent();
    return s;
  }

  /**
   * Generate the </renderer> element.
   */
  static public String closeRendererElement() {
    decrIndent();
    return _indent + "</renderer>";
  }

  /**
   * Generate the renderer's <rendererProperty/> element.
   */
  static public String rendererPropertyElement(String key, String value) {
    return _indent + "<rendererProperty key=\"" + key + "\" value=\"" + value + "\"/>";
  }

  /**
   * Generate the renderer's <entityRendered/> element.
   * @param varName var name for the entity rendered
   * @param uniqueId Unique ID for the entity
   * @return XML for the <entityRendered> element
   */
  static public String renderedEntityElement(String uniqueId) {
    return _indent + "<entityRendered uniqueId=\"" + uniqueId + "\"/>";
  }

  /**
   * Generate the renderer's <modelProperty/> element.
   */
  static public String rendererModelPropertyElement(String key, String value) {
    return _indent + "<modelProperty key=\"" + key + "\" value=\"" + value + "\"/>";
  }

  /**
   * Generate the <perspective> element
   * @param id The ID of the perspective
   * @param name The name of the perspective
   * @param klass The class of the perspective
   * @param windowID ID of the workbench window containing the perspective
   * @param isEditorAreaVisible true if perspective has a visible editor; otherwise, false
   */
  static public void openPerspectiveElement(String id, String name, String klass, String windowID,
      boolean isEditorAreaVisible) {
    String isVisible = isEditorAreaVisible ? "true" : "false";
    String s = _indent + "<perspective id=\"" + id + "\" name=\"" + name + "\" class=\"" + klass + "\" windowID=\""
        + windowID + "\" isEditorAreaVisible=\"" + isVisible + "\">";
    incrIndent();
    SessionManager.getInstance().writeXML(s);
  }

  /**
   * Generate the </perspective> element
   */
  static public void closePerspectiveElement() {
    decrIndent();
    SessionManager.getInstance().writeXML(_indent + "</perspective>");
  }

  /**
   * Generate the <preferencePage> element
   * @param name The name of the preference page
   * @param klass The class of the preference page
   */
  static public void openPreferencePageElement(String name, String klass) {
    String s = _indent + "<preferencePage name=\"" + name + "\" class=\"" + klass + "\">";
    incrIndent();
    SessionManager.getInstance().writeXML(s);
  }

  /**
   * Generate the </preferencePage> element
   */
  static public void closePreferencePageElement() {
    decrIndent();
    SessionManager.getInstance().writeXML(_indent + "</preferencePage>");
  }

  /**
   * Write the <preference/> element.
   */
  static public void preferenceElement(String key, String value) {
    SessionManager.getInstance().writeXML(_indent + "<preference key=\"" + key + "\" value=\"" + value + "\"/>");
  }

  /**
   * Write the <plotWindow/> element.
   * @param windowID ID of the workbench window containing the viewers
   */
  static public void plotWindowElement(String windowID) {
    SessionManager.getInstance().writeXML(_indent + "<plotWindow windowID=\"" + windowID + "\"/>");
  }
}
