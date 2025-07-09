package healthcare.healthcare_spring.dto.request;

public class CalculateRequest {
    private int day;
    private int month;
    private int year;
    private int totalStep;
    private String name;

    public int getTotalStep() {
        return totalStep;
    }
    public String getName() {
        return name;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}
