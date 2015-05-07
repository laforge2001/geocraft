package org.geocraft.core.shell;


import java.util.List;


public interface ICommandExecutor {

  public void executeCommand(String command);

  /**
   * This method is for testing purposes only...
   * @param command
   * @param returnValueName
   * @return
   */
  public String executeCommand(String command, String returnValueName);

  // execute a list of shell commands
  public void executeShellCommands(List<String> commands);

  // Get shell commands that have been saved
  public List<String> getShellCommands();

  // remove a saved line or set of saved lines
  public List<String> removeSavedLines(final int[] indexList);

  // Remove all of the saved lines
  public void removeAllSavedLines();
}
