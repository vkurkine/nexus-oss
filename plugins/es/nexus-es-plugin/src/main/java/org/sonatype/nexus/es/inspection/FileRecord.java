package org.sonatype.nexus.es.inspection;

import java.util.Date;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: cstamas
 * Date: 07/11/13
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class FileRecord
{
  private final String repositoryId;

  private final String path;

  private final String name;

  private final Date created;

  private final Date modified;

  private final long length;

  private final String mimeType;

  private final String creator;

  // nested
  private final Hash[] hashes;

  // nested
  private final Map<String, String> attributes;

  public static class Hash
  {
    private final String alg;

    private final String bytesHex;
  }
}
