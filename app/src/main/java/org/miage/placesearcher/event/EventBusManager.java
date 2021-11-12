package org.miage.placesearcher.event;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by alexmorel on 10/01/2018.
 */

public class EventBusManager {

    public static Bus BUS = new Bus(ThreadEnforcer.ANY);
}
