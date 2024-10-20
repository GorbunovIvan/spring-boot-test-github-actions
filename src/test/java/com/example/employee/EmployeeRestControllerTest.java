package com.example.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
// You may use either '@WebMvcTest' or '@AutoConfigureMockMvc'
// @WebMvcTest - loads only controller and its dependencies
@AutoConfigureMockMvc // - loads full context
@Transactional
class EmployeeRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EmployeeService employeeService;

    private List<Employee> employees;

    @BeforeEach
    public void init() {
        employees = new ArrayList<>(List.of(
                employeeService.create(new Employee("test one", 11)),
                employeeService.create(new Employee("test two", 22)),
                employeeService.create(new Employee("test three", 33))
        ));
    }

    @Test
    void testFindAll() throws Exception {

        var response = mvc.perform(MockMvcRequestBuilders.get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        String jsonReceived = new String(response.getContentAsByteArray());
        List<Employee> employeesReceived = new ObjectMapper().readValue(jsonReceived, new TypeReference<>(){});

        assertEquals(employees, employeesReceived);
    }

    @Test
    void testFindById() throws Exception {

        var employee = employees.get(1);

        var response = mvc.perform(MockMvcRequestBuilders.get("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        String jsonReceived = new String(response.getContentAsByteArray());
        var employeeReceived = new ObjectMapper().readValue(jsonReceived, Employee.class);

        assertEquals(employee, employeeReceived);

        mvc.perform(MockMvcRequestBuilders.get("/api/employees/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {

        var employee = new Employee("employee for posting", 99);

        var response = mvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(employee)))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse();

        String jsonReceived = new String(response.getContentAsByteArray());
        var employeeReceived = new ObjectMapper().readValue(jsonReceived, Employee.class);

        assertEquals(employee.getName(), employeeReceived.getName());
        assertEquals(employee.getAge(), employeeReceived.getAge());

        assertEquals(employeeReceived, employeeService.findById(employeeReceived.getId()));
    }

    @Test
    void testUpdate() throws Exception {

        var employee = employees.get(1);

        var response = mvc.perform(MockMvcRequestBuilders.put("/api/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(employee)))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse();

        String jsonReceived = new String(response.getContentAsByteArray());
        var employeeReceived = new ObjectMapper().readValue(jsonReceived, Employee.class);

        assertEquals(employee, employeeReceived);

        mvc.perform(MockMvcRequestBuilders.put("/api/employees/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(employee)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteById() throws Exception {

        var employeeForDeletion = employees.get(1);

        mvc.perform(MockMvcRequestBuilders.delete("/api/employees/{id}", employeeForDeletion.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var employeesAfterDeletion = employeeService.findAll();

        assertEquals(employees.size()-1, employeesAfterDeletion.size());

        assertTrue(employeesAfterDeletion.stream()
                .noneMatch(e -> e.getName().equals(employeeForDeletion.getName())
                        && e.getAge().equals(employeeForDeletion.getAge())));
    }
}