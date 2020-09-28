package io.github.wysohn.tradegui.inject.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.github.wysohn.tradegui.inject.annotaton.OfferWaitTime;

public class WaitingTimeModule extends AbstractModule {
    @Provides
    @OfferWaitTime
    long waitTime() {
        return 60 * 1000L;
    }
}
