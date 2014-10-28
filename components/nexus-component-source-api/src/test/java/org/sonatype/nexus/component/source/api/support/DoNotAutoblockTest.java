package org.sonatype.nexus.component.source.api.support;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DoNotAutoblockTest
{
  @Test
  public void testIsAutoBlockEnabled() throws Exception {
    final AutoBlockStrategy autoblock = new DoNotAutoBlock();

    assertThat(autoblock.getAutoBlockState(),is(equalTo(AutoBlockState.NOT_BLOCKED)));
    assertThat(autoblock.isAutoBlockEnabled(), is(equalTo(false)));
  }

}