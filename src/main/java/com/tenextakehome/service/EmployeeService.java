package com.tenextakehome.service;

import com.tenextakehome.dto.CreateEmployeeRequest;
import com.tenextakehome.dto.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponse> getAllEmployees();

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeeResponse updateEmployee(Long id, CreateEmployeeRequest request);

    void deleteEmployee(Long id);
}
