package org.geocraft.io.remote;


public class RemoteServiceProvider {

  private static IRemoteDataService _service;

  public void setRemoteDataService(IRemoteDataService service) {
    _service = service;
  }

  public void unsetRemoteDataService() {
    _service = null;
  }

  public static IRemoteDataService getRemoteDataService() {
    return _service;
  }
}
