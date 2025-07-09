package healthcare.healthcare_spring.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZoneId;

@Entity
public class Calculate {
    @Id
    private Integer day;
    private Integer totalStep;
    private Integer year;
    private Integer month;

    private String name;
    protected Calculate() {}

    public Calculate(Integer day, Integer totalStep,Integer month, Integer year, String name) {
        this.totalStep = totalStep;
        this.name = name;
    }

    @PrePersist
    public void prePersist() {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        this.year = now.getYear();
        this.month = now.getMonthValue();
        this.day = now.getDayOfMonth();
    }

    public Integer getDay() {
        return day;
    }

    public Integer getTotalStep() {
        return totalStep;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public String getName() {
        return name;
    }
}
