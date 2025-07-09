package healthcare.healthcare_spring.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CalculateJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    public CalculateJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveCalculate(int temperature, int bpm) {
        String sql = "Insert INTO calculate(temperature, bpm) VALUES (?,?)";
        jdbcTemplate.update(sql, temperature, bpm);
    }
}
