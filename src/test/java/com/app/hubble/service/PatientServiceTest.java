package com.app.hubble.service;

import com.app.hubble.dto.response.PatientResponse;
import com.app.hubble.entity.Patient;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

//    @Mock
//    private PatientRepository patientRepository;
//
//    @Mock
//    private PatientMapper patientMapper;
//
//    @InjectMocks
//    private PatientService patientService;
//
//    @Test
//    void findPatientById() {
//        UUID id = UUID.randomUUID();
//        Patient patient = new Patient();
//        patient.setId(id);
//
//        PatientResponse patientResponse = new PatientResponse();
//        when(patientRepository.findById(id))
//                .thenReturn(Mono.just(patient));
//
//        when(patientMapper.toDto(patient))
//                .thenReturn(patientResponse);
//
//        StepVerifier.create(patientService.findPatientById(id))
//                .expectNext(patientResponse)
//                .verifyComplete();
//    }
//
//    @Test
//    void errorFindPatientById() {
//        UUID id = UUID.randomUUID();
//
//        when(patientRepository.findById(id))
//                .thenReturn(Mono.empty());
//
//        StepVerifier.create(patientService.findPatientById(id))
//                .expectError(NotFoundException.class)
//                .verify();
//    }
}