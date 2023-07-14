package ru.evsyukov.app.api.mappers;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.evsyukov.app.api.dto.RestEmployee;
import ru.evsyukov.app.api.dto.RestProject;
import ru.evsyukov.app.data.entity.Employee;
import ru.evsyukov.app.data.entity.Project;

@Component
@Mapper(
        componentModel = "spring")
public interface DtoToDataMapper {

    Employee restToDataEmployee(RestEmployee employee);

    Project restToDataProject(RestProject project);



}
