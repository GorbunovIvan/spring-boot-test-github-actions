package com.example.employee;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
// You may use either '@WebMvcTest' or '@AutoConfigureMockMvc'
// @WebMvcTest - loads only controller and its dependencies
@AutoConfigureMockMvc // - loads full context
@Transactional
class EmployeeControllerTest {

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

        var response = mvc.perform(MockMvcRequestBuilders.get("/employees")
                    .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/employees"))
                .andReturn()
                .getResponse();

        String htmlReceived = new String(response.getContentAsByteArray());

        for (var employee : employees) {
            assertTrue(htmlReceived.contains(employee.getName()));
        }
    }

    @Test
    void testFindById() throws Exception {

        var employee = employees.get(1);

        var response = mvc.perform(MockMvcRequestBuilders.get("/employees/{id}", employee.getId())
                    .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/employee"))
                .andReturn()
                .getResponse();

        String htmlReceived = new String(response.getContentAsByteArray());

        assertTrue(htmlReceived.contains(employee.getName()));
        assertTrue(htmlReceived.contains(String.valueOf(employee.getAge())));
    }

    @Test
    void testCreate() throws Exception {

        var employee = new Employee("employee for posting", 99);

        mvc.perform(MockMvcRequestBuilders.post("/employees")
                    .contentType(MediaType.TEXT_HTML)
                    .param("name", employee.getName())
                    .param("age", String.valueOf(employee.getAge())))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/employees"));

        assertTrue(employeeService.findAll().stream()
                .anyMatch(e -> e.getName().equals(employee.getName())
                            && e.getAge().equals(employee.getAge())));
    }

    @Test
    void testEditForm() throws Exception {

        var employee = employees.get(1);

        var response = mvc.perform(MockMvcRequestBuilders.get("/employees/{id}/edit", employee.getId())
                    .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/edit"))
                .andReturn()
                .getResponse();

        String htmlReceived = new String(response.getContentAsByteArray());

        assertTrue(htmlReceived.contains(employee.getName()));
        assertTrue(htmlReceived.contains(String.valueOf(employee.getAge())));
    }

    @Test
    void testUpdate() throws Exception {

        var employee = new Employee(employees.get(1).getId(), employees.get(1).getName(), employees.get(1).getAge());

        mvc.perform(MockMvcRequestBuilders.put("/employees/{id}", employee.getId())
                    .contentType(MediaType.TEXT_HTML)
                    .param("name", employee.getName() + " updated")
                    .param("age", String.valueOf((employee.getAge() + 1) * 2)))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/employees/" + employee.getId()));

        var employeeFromBase = employeeService.findById(employee.getId());

        assertEquals(employeeFromBase.getName(), employee.getName() + " updated");
        assertEquals(employeeFromBase.getAge(), (employee.getAge() + 1) * 2);
    }

    @Test
    void testDeleteById() throws Exception {

        var employeeForDeletion = employees.get(1);

        mvc.perform(MockMvcRequestBuilders.delete("/employees/{id}", employeeForDeletion.getId())
                    .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/employees"));

        var employeesAfterDeletion = employeeService.findAll();

        assertEquals(employees.size()-1, employeesAfterDeletion.size());

        assertTrue(employeesAfterDeletion.stream()
                .noneMatch(e -> e.getName().equals(employeeForDeletion.getName())
                                && e.getAge().equals(employeeForDeletion.getAge())));
    }
}