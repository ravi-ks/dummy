package com.platform.pod.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.Column;

public enum RepetitionType {
    Daily,
    WorkingDays,
    sun,
    mon,
    tue,
    wed,
    thu,
    fri,
    sat
}
