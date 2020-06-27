package com.nammaexpo.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nammaexpo.expection.ExpoException;
import com.nammaexpo.models.enums.MessageCode;
import com.nammaexpo.models.layout.Layout;
import com.nammaexpo.payload.response.MessageResponse;
import com.nammaexpo.persistance.dao.ExhibitionDetailsRepository;
import com.nammaexpo.persistance.dao.PageRepository;
import com.nammaexpo.persistance.dao.UserRepository;
import com.nammaexpo.persistance.model.ExhibitionDetailsEntity;
import com.nammaexpo.persistance.model.PageEntity;
import com.nammaexpo.persistance.model.UserEntity;
import com.nammaexpo.utils.SerDe;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.function.Supplier;

@Api(tags = "Exhibition Page Controller")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class PageController {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExhibitionDetailsRepository exhibitionDetailsRepository;

    @PostMapping("/pages")
    @PreAuthorize("hasAuthority('EXHIBITOR')")
    public MessageResponse createPage(
            @NotNull @RequestBody Layout layout,
            @RequestHeader(value = "Authorization") String authorization
    ) throws JsonProcessingException {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Supplier<ExpoException> expoExceptionSupplier = () -> ExpoException.error(
                MessageCode.TRANSACTION_NOT_FOUND);

        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(expoExceptionSupplier
                );

        ExhibitionDetailsEntity exhibitionDetailsEntity = exhibitionDetailsRepository
                .findByExhibitorId(userEntity.getId())
                .orElseThrow(expoExceptionSupplier);

        pageRepository.save(PageEntity.builder()
                .isActive(true)
                .content(SerDe.mapper().writeValueAsBytes(layout))
                .exhibitionDetails(exhibitionDetailsEntity)
                .createdBy(exhibitionDetailsEntity.getExhibitor().getId())
                .build());

        return MessageResponse.builder()
                .messageCode(MessageCode.PAGE_CREATED)
                .build();
    }

    @GetMapping("/pages")
    @PreAuthorize("hasAuthority('EXHIBITOR')")
    public Layout getPage(
            @RequestHeader(value = "Authorization") String authorization
    ) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Supplier<ExpoException> expoExceptionSupplier = () -> ExpoException.error(MessageCode.TRANSACTION_NOT_FOUND);

        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(expoExceptionSupplier
                );

        return exhibitionDetailsRepository.findByExhibitorId(userEntity.getId())
                .map(ExhibitionDetailsEntity::getPageDetails)
                .orElseThrow(expoExceptionSupplier);
    }

    @PutMapping("/pages")
    @PreAuthorize("hasAuthority('EXHIBITOR')")
    public MessageResponse updatePage(
            @NotNull @RequestBody Layout layout,
            @RequestHeader(value = "Authorization") String authorization
    ) throws JsonProcessingException {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Supplier<ExpoException> expoExceptionSupplier = () -> ExpoException.error(MessageCode.TRANSACTION_NOT_FOUND);

        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(expoExceptionSupplier
                );

        ExhibitionDetailsEntity exhibitionDetailsEntity = exhibitionDetailsRepository
                .findByExhibitorId(userEntity.getId())
                .orElseThrow(expoExceptionSupplier);

        PageEntity pageEntity = exhibitionDetailsEntity.getPage();

        pageEntity.setContent(SerDe.mapper().writeValueAsBytes(layout));

        pageRepository.save(pageEntity);

        return MessageResponse.builder()
                .messageCode(MessageCode.PAGE_UPDATED)
                .build();
    }
}
