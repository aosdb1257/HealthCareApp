package healthcare.healthcare_spring.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.data.relational.core.sql.In;

@Entity
public class User {
    @Id
    private String name;
    private Integer height;
    private Integer weight;
    private Integer goalWeight;
    private Integer date;
    private Integer goalWalk;
    protected User() {}

    public User(String name, int height, int weight, int goalWeight, int date, int goalWalk) {
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.goalWeight = goalWeight;
        this.date = date;
        this.goalWalk = goalWalk;
    }

    public String getName() {
        return name;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getWeight() {
        return weight;
    }

    public Integer getGoalWeight() {
        return goalWeight;
    }

    public Integer getDate() {
        return date;
    }

    public Integer getGoalWalk() {
        return goalWalk;
    }
}
