package com.example.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @SpyBean
    private EmployeeRepository employeeRepository;

    private List<Employee> employees;

    @BeforeEach
    public void initEach() {

        employees = List.of(
                employeeService.create(new Employee("Maxim", 22)),
                employeeService.create(new Employee("Denis", 33)),
                employeeService.create(new Employee("Anna", 44))
        );

        Mockito.reset(employeeRepository);
    }

    @Test
    void testFindAll() {
        assertEquals(employees, employeeService.findAll());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {

        for (var employee : employees) {
            assertEquals(employee, employeeService.findById(employee.getId()));
            verify(employeeRepository, times(1)).findById(employee.getId());
        }

        verify(employeeRepository, times(employees.size())).findById(anyLong());

        assertNull(employeeService.findById(-1L));
    }

    @Test
    void testCreate() {

        var employee = new Employee("test employee", 99);
        var employeeCreated = employeeService.create(employee);

        assertEquals(employee.getName(), employeeCreated.getName());
        assertEquals(employee.getAge(), employeeCreated.getAge());

        verify(employeeRepository, times(1)).save(employee);

        assertEquals(employeeCreated, employeeService.findById(employeeCreated.getId()));
    }

    @Test
    void testUpdate() {

        for (var employee : employees) {

            employee.setName(employee.getName() + " test");
            employee.setAge((employee.getAge() + 1) * 2);

            assertEquals(employee, employeeService.update(employee.getId(), employee));

            verify(employeeRepository, times(1)).existsById(employee.getId());
            verify(employeeRepository, times(1)).save(employee);
        }

        verify(employeeRepository, times(employees.size())).save(any(Employee.class));

        var employee = new Employee("wrong user", 88);
        employeeService.update(-1L, employee);
        verify(employeeRepository, times(1)).existsById(-1L);
        verify(employeeRepository, never()).save(employee);
    }

    @Test
    void testDeleteById() {

        assertFalse(employeeService.findAll().isEmpty());

        for (var employee : employees) {
            employeeService.deleteById(employee.getId());
            verify(employeeRepository, times(1)).deleteById(employee.getId());
        }

        verify(employeeRepository, times(employees.size())).deleteById(anyLong());

        assertTrue(employeeService.findAll().isEmpty());
    }
}