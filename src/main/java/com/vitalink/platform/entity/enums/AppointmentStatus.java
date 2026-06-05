package com.vitalink.platform.entity.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum AppointmentStatus {
    SCHEDULED {
        @Override
        public Set<AppointmentStatus> allowedTransitions() {
            return EnumSet.of(CONFIRMED, CANCELLED, NO_SHOW);
        }
    },
    CONFIRMED {
        @Override
        public Set<AppointmentStatus> allowedTransitions() {
            return EnumSet.of(COMPLETED, CANCELLED, NO_SHOW);
        }
    },
    COMPLETED {
        @Override
        public Set<AppointmentStatus> allowedTransitions() {
            return Collections.emptySet();
        }
    },
    CANCELLED {
        @Override
        public Set<AppointmentStatus> allowedTransitions() {
            return Collections.emptySet();
        }
    },
    NO_SHOW {
        @Override
        public Set<AppointmentStatus> allowedTransitions() {
            return Collections.emptySet();
        }
    };

    public abstract Set<AppointmentStatus> allowedTransitions();

    public boolean canTransitionTo(AppointmentStatus target) {
        return allowedTransitions().contains(target);
    }

    public boolean blocksSchedule() {
        return this == SCHEDULED || this == CONFIRMED;
    }
}
