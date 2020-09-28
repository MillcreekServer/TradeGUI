package io.github.wysohn.tradegui.inject.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.github.wysohn.rapidframework3.interfaces.plugin.ITaskSupervisor;
import io.github.wysohn.rapidframework3.utils.OfferScheduler;
import io.github.wysohn.tradegui.inject.annotaton.OfferWaitTime;

public class OfferSchedulerModule extends AbstractModule {
    @Provides
    OfferScheduler offerScheduler(ITaskSupervisor task, @OfferWaitTime long watingTime) {
        return new OfferScheduler(task, watingTime);
    }
}
