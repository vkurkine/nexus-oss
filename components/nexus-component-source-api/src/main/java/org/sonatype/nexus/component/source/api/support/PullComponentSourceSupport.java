package org.sonatype.nexus.component.source.api.support;

import java.io.IOException;

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.ComponentRequest;
import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public abstract class PullComponentSourceSupport
    extends ComponentSupport
    implements PullComponentSource
{
  private final ComponentSourceId id;

  private boolean enabled = true;

  private final AutoBlockStrategy autoBlockStrategy;

  protected PullComponentSourceSupport(final ComponentSourceId id, final AutoBlockStrategy autoBlockStrategy) {
    this.id = checkNotNull(id);
    this.autoBlockStrategy = checkNotNull(autoBlockStrategy);
  }

  @Override
  public ComponentSourceId getId() {
    return id;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(final boolean enabled) {
    boolean statusChanging = this.enabled != enabled;
    this.enabled = enabled;

    if (statusChanging) {
      // TODO: Dispatch an event?
    }
  }

  @Override
  public final <T extends Component> Iterable<ComponentEnvelope<T>> fetchComponents(final ComponentRequest<T> request)
      throws IOException
  {
    if (isAutoBlocked()) {
      return Lists.newArrayList();
    }
    try {
      return doFetchComponents(request);
    }
    catch (Exception e) {
      autoBlockStrategy.processException(e);
      return Lists.newArrayList();
    }
  }

  protected abstract <T extends Component> Iterable<ComponentEnvelope<T>> doFetchComponents(
      final ComponentRequest<T> request) throws Exception;

  @Override
  public boolean isAutoBlocked() {
    return autoBlockStrategy.isAutoBlocked();
  }

  @Override
  public boolean isAutoBlockEnabled() {
    return autoBlockStrategy.isAutoBlockEnabled();
  }
}
