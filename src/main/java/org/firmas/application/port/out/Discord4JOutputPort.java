package org.firmas.application.port.out;

import io.quarkus.runtime.StartupEvent;

public interface Discord4JOutputPort {

    void onStart(StartupEvent ev);

}
