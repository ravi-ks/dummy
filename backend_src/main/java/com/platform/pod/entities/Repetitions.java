package com.platform.pod.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.platform.pod.dto.Repeat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@NoArgsConstructor
@Data
public class Repetitions{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private int repetition_id;

    @Column(nullable = false)
    private boolean sun = false;

    @Column(nullable = false)
    private boolean mon = false;

    @Column(nullable = false)
    private boolean tue = false;
    @Column(nullable = false)
    private boolean wed = false;
    @Column(nullable = false)
    private boolean thu = false;
    @Column(nullable = false)
    private boolean fri = false;
    @Column(nullable = false)
    private boolean sat = false;

    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Date endsOn;

    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Date startsAt;

    private int frequency_gap;

    @OneToOne(cascade = CascadeType.ALL,fetch=FetchType.LAZY)
    private Tasks task;

    public Repetitions(int repetition_id, boolean sun, boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean sat, Date endsOn, Date startsAt, int frequency_gap, Tasks task) {
        this.repetition_id = repetition_id;
        this.sun = sun;
        this.mon = mon;
        this.tue = tue;
        this.wed = wed;
        this.thu = thu;
        this.fri = fri;
        this.sat = sat;
        this.endsOn = endsOn;
        this.startsAt = startsAt;
        this.frequency_gap = frequency_gap;
        this.task = task;
    }

    public void setAllDays() {
        this.sun = true;
        this.mon = true;
        this.tue = true;
        this.wed = true;
        this.thu = true;
        this.fri = true;
        this.sat = true;
    }

    public void setWorkingDays() {
        this.mon = true;
        this.tue = true;
        this.wed = true;
        this.thu = true;
        this.fri = true;
    }

    public void setSomeDays(List<RepetitionType> daysToSet) {
        this.sun = this.mon = this.tue = this.wed = this.thu = this.fri = this.sat = false;
        if (daysToSet.contains(RepetitionType.sun))
            this.sun = true;
        if (daysToSet.contains(RepetitionType.mon))
            this.mon = true;
        if (daysToSet.contains(RepetitionType.tue))
            this.tue = true;
        if (daysToSet.contains(RepetitionType.wed))
            this.wed = true;
        if (daysToSet.contains(RepetitionType.thu))
            this.thu = true;
        if (daysToSet.contains(RepetitionType.fri))
            this.fri = true;
        if (daysToSet.contains(RepetitionType.sat))
            this.sat = true;
    }

    public Repeat toRepeat(){
        return  new Repeat(
                this.sun,
                this.mon,
                this.tue,
                this.wed,
                this.thu,
                this.fri,
                this.sat
        );
    }
}
