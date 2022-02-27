package api.service.impl;

import api.dto.FiltersDto;
import api.repository.ReportDayRepository;
import api.service.DocGeneratorService;
import hibernate.entities.ReportDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocGeneratorServiceImpl implements DocGeneratorService {

    private ReportDayRepository daysRepository;


    @Autowired
    public DocGeneratorServiceImpl(ReportDayRepository daysRepository) {
        this.daysRepository = daysRepository;
    }

    @Override
    public void generateXml(FiltersDto dto) {
        List<ReportDay> days;
        if (dto.getName() == null) {
            days = daysRepository
                    .findReportDayByDateAfterAndDateBefore(dto.getDateStart(), dto.getDateEnd());
        } else {
            days = daysRepository.findReportDayByDateAfterAndDateBeforeAndEmployeeName(
                    dto.getDateStart(), dto.getDateEnd(), dto.getName());
        }
        return;
    }
}
