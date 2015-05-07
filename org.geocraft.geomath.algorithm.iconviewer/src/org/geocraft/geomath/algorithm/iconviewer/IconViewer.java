package org.geocraft.geomath.algorithm.iconviewer;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.internal.iconviewer.IconViewerActivator;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.form2.IModelForm;
import org.osgi.framework.Bundle;


public class IconViewer extends StandaloneAlgorithm {

  /** The icon file names list. */
  private final List<String> _fileNames = new ArrayList<String>();

  /** The icons list. */
  private final List<Image> _icons = new ArrayList<Image>();

  /** The panel to put icons. */
  private Composite _panel;

  public IconViewer() {
    // Empty
  }

  @Override
  public void buildView(IModelForm modelForm) {
    _panel = modelForm.getToolkit().createComposite(modelForm.getComposite());
    _panel.setLayout(new GridLayout(10, false));
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {
    loadImages(logger, monitor);

    Display.getDefault().syncExec(new Runnable() {

      public void run() {

        for (int i = 0; i < _icons.size(); i++) {
          Image icon = _icons.get(i);
          Rectangle bounds = icon.getBounds();
          if (bounds.width < 50 && bounds.height < 50) {
            Label label = new Label(_panel, SWT.NONE);
            label.setImage(_icons.get(i));
            label.setToolTipText(_fileNames.get(i) + " " + bounds.width + " by " + bounds.height);
          }
        }
        _panel.pack();
        // we have used them and don't need them anymore
        _icons.clear();

      }
    });
  }

  @Override
  public void propertyChanged(String key) {
    // Empty
  }

  @Override
  public void validate(IValidation results) {
    // Empty
  }

  /**
   * Load the images.
   */
  private void loadImages(final ILogger logger, final IProgressMonitor monitor) {
    String classpath = System.getProperty("java.class.path");
    String[] pathItems = classpath.split(File.pathSeparator);
    List<String> paths = new ArrayList<String>(Arrays.asList(pathItems));
    Bundle[] bundles = IconViewerActivator.getBundleContext().getBundles();
    monitor.beginTask("Icon Viewer", bundles.length * 3);
    monitor.subTask("Locate bundles");
    for (Bundle bundle : bundles) {
      URL x = FileLocator.find(bundle, new Path(""), null);
      try {
        URL url = FileLocator.toFileURL(x);
        paths.add(url.getFile());
      } catch (IOException e) {
        logger.warn("Could not find bundle at " + x, e);
      }
      monitor.worked(1);
    }
    monitor.subTask("Load Images");
    for (int i = 0; i < paths.size() && !monitor.isCanceled(); i++) {
      String name = paths.get(i);
      if (!name.contains(".jar")) {
        processDir(new File(name), logger);
      } else {
        processJar(name, logger);
      }
      monitor.worked(2);
    }
  }

  /**
   * Load the images in a jar file.
   * 
   * @param jar the jar file
   */
  private void processJar(final String jar, final ILogger logger) {
    try {
      JarInputStream jarIn = new JarInputStream(new FileInputStream(jar));
      JarEntry entry;
      while ((entry = jarIn.getNextJarEntry()) != null) {
        if (isImage(entry.toString())) {
          if (!_fileNames.contains(entry.toString())) {
            _fileNames.add(entry.toString());
            URL url = ImageRegistryUtil.createURL(entry.toString());
            _icons.add(ImageDescriptor.createFromURL(url).createImage());
          }
        }
      }
    } catch (IOException e) {
      logger.warn(e.getMessage(), e);
    }
  }

  /**
   * Load the images in a directory.
   * 
   * @param dir the directory
   */
  private void processDir(final File dir, final ILogger logger) {
    if (dir.isDirectory()) {
      File[] files = dir.listFiles();
      for (File file : files) {
        if (file.isDirectory()) {
          processDir(file, logger);
        } else if (isImage(file.getAbsolutePath())) {
          try {
            if (!_fileNames.contains(file.getAbsolutePath())) {
              _fileNames.add(file.getAbsolutePath());
              URL url = file.toURI().toURL();// find(Platform.getBundle(CommonUI.PLUGIN_ID), new Path(path)., null);
              _icons.add(ImageDescriptor.createFromURL(url).createImage());
            }
          } catch (IOException ex) {
            logger.warn(ex.getMessage(), ex);
          }
        }
      }
    }
  }

  /**
   * Check if a file name is an image file.
   * 
   * @param name the file name
   * @return <code>true</code> if the file name is of an image file, </code>false</code> otherwise
   */
  private boolean isImage(final String name) {
    return name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".jpg") || name.endsWith(".ico")
        || name.endsWith(".bmp");
  }

  /**
   * Release the resources.
   */
  public void shutdown() {
    //    _panel.removeAll();
    _icons.clear();
    _fileNames.clear();
    System.gc();
  }

  @Override
  public boolean createCopy() {
    return false;
  }

}
