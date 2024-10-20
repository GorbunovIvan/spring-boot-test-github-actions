package com.example.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class EmployeeServiceIntegrationTest {

    // you can use either '@Autowired + @MockBean' or '@InjectMocks + @Mock'

//    @InjectMocks
    @Autowired
    private EmployeeService employeeService;

//    @Mock
    @MockBean
    private EmployeeRepository employeeRepository;

    private List<Employee> employees;

    @BeforeEach
    public void setUp() {
        employees = List.of(
                new Employee(1L, "test employee", 99),
                new Employee(2L, "another test employee", 22),
                new Employee(3L, "one more test employee", 44)
        );
    }

    @Test
    void testFindAll() {
        when(employeeRepository.findAll()).thenReturn(employees);
        employeeService.findAll();
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {

        var employee = employees.get(0);
        long id = employee.getId();
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        employeeService.findById(id);
        verify(employeeRepository, times(1)).findById(id);
    }

    @Test
    void testCreate() {

        var employee = employees.get(0);
        when(employeeRepository.save(employee)).thenReturn(employee);

        employeeService.create(employee);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void testUpdate() {

        var employee = employees.get(0);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeRepository.existsById(employee.getId())).thenReturn(true);
        when(employeeRepository.existsById(-1L)).thenReturn(false);

        employeeService.update(employee.getId(), employee);
        verify(employeeRepository, times(1)).existsById(employee.getId());
        verify(employeeRepository, times(1)).save(employee);

        employeeService.update(-1L, employees.get(1));
        verify(employeeRepository, times(1)).existsById(-1L);
        verify(employeeRepository, never()).save(employees.get(1));
    }

    @Test
    void testDeleteById() {
        employeeService.deleteById(1L);
        verify(employeeRepository, times(1)).deleteById(anyLong());
    }
}