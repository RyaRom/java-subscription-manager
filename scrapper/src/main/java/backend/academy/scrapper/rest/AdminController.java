package backend.academy.scrapper.rest;

import backend.academy.scrapper.service.UpdatePollingJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/scrapper/api")
@RequiredArgsConstructor
public class AdminController {
    private final UpdatePollingJob updatePollingJob;

    @PostMapping("/test/update")
    public void fetchUpdate() {
        updatePollingJob.update();
    }
}
