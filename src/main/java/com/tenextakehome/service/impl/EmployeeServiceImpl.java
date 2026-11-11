package com.tenextakehome.service.impl;

import com.tenextakehome.dto.CreateEmployeeRequest;
import com.tenextakehome.dto.EmployeeResponse;
import com.tenextakehome.exception.ResourceNotFoundException;
import com.tenextakehome.model.Employee;
import com.tenextakehome.repository.EmployeeRepository;
import com.tenextakehome.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToResponse(employee);
    }

    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setSalary(request.getSalary());

        Employee savedEmployee = employeeRepository.save(employee);
        return mapToResponse(savedEmployee);
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, CreateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setSalary(request.getSalary());

        Employee updatedEmployee = employeeRepository.save(employee);
        return mapToResponse(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employeeRepository.delete(employee);
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getSalary(),
                employee.getCreatedAt()
        );
    }
}
