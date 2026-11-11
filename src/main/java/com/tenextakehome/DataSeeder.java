package com.tenextakehome;

import com.tenextakehome.model.Employee;
import com.tenextakehome.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(EmployeeRepository employeeRepository) {
        return args -> {
            if (employeeRepository.count() == 0) {
                employeeRepository.save(new Employee("John", "Doe", "john.doe@tenex.com", "Engineering", new BigDecimal("95000.00")));
                employeeRepository.save(new Employee("Jane", "Smith", "jane.smith@tenex.com", "Product", new BigDecimal("88000.00")));
                employeeRepository.save(new Employee("Mike", "Johnson", "mike.johnson@tenex.com", "Operations", new BigDecimal("79000.00")));
            }
        };
    }
}
