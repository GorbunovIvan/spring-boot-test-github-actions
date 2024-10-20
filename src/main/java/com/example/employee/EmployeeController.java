package com.example.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("newEmployee", new Employee());
        return "employees/employees";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable long id, Model model) {
        model.addAttribute("employee", employeeService.findById(id));
        return "employees/employee";
    }

    @PostMapping
    public String create(@ModelAttribute Employee employee) {
        employeeService.create(employee);
        return "redirect:/employees";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable long id, Model model) {
        model.addAttribute("employee", employeeService.findById(id));
        return "employees/edit";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable long id, @ModelAttribute Employee employee) {
        employeeService.update(id, employee);
        return "redirect:/employees/" + id;
    }

    @DeleteMapping("/{id}")
    public String deleteById(@PathVariable long id) {
        employeeService.deleteById(id);
        return "redirect:/employees";
    }
}
