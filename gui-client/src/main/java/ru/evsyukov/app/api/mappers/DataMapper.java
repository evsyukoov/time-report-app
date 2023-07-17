package ru.evsyukov.app.api.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;
import ru.evsyukov.app.api.dto.Department;
import ru.evsyukov.app.api.dto.RestEmployee;
import ru.evsyukov.app.api.dto.RestProject;
import ru.evsyukov.app.data.entity.Employee;
import ru.evsyukov.app.data.entity.Project;

@Component
@Mapper(
        componentModel = "spring")
public interface DataMapper {

    Employee restToDataEmployee(RestEmployee employee);

    Project restToDataProject(RestProject project);

    @Mappings({
            @Mapping(target = "name", source = "department"),
            @Mapping(target = "shortName", source = "departmentShort")
    })
    Department employeeToDepartment(Employee employee);

}
