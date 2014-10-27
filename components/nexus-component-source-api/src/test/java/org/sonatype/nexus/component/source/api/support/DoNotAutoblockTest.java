package org.sonatype.nexus.component.source.api.support;

import junit.framework.TestCase;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DoNotAutoblockTest
{
  @Test
  public void testIsAutoBlockEnabled() throws Exception {
    final AutoBlockStrategy autoblock = new DoNotAutoblock();

    assertThat(autoblock.isAutoBlocked(), is(equalTo(false)));
    assertThat(autoblock.isAutoBlockEnabled(), is(equalTo(false)));
  }

}