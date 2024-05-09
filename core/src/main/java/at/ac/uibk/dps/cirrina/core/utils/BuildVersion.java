package at.ac.uibk.dps.cirrina.core.utils;

public class BuildVersion {

  public static String getBuildVersion() {
    return BuildVersion.class.getPackage().getImplementationVersion();
  }

}