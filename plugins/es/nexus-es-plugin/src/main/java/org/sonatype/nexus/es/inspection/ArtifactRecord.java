package org.sonatype.nexus.es.inspection;

/**
 * Created with IntelliJ IDEA.
 * User: cstamas
 * Date: 07/11/13
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class ArtifactRecord
{
  // parent FileRecord

  private final String groupId;

  private final String artifactId;

  private final String version;

  private final String baseVersion;

  private final String classifier;

  private final String extension;

  private final String packaging;

  // nested
  private final Dependency[] dependencies;

  public static class Dependency
  {
    // parent ArtifactRecord
    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String type;

    private final String scope;

    private final boolean optional;

    private final int distance; // 0 declared in POM, all >0 transitive
  }
}
