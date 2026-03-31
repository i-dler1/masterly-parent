package com.masterly.web.client;

import com.masterly.web.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "core-service", url = "${core.service.url}")
public interface CoreServiceClient {

    @GetMapping("/api/clients")
    Page<ClientDto> getClients(@RequestParam("masterId") Long masterId);

    @GetMapping("/api/clients/paginated")
    Page<ClientDto> getClientsPaginated(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("sortDir") String sortDir,
            @RequestParam("masterId") Long masterId
    );

    @PostMapping("/api/clients")
    ClientDto createClient(@RequestParam("masterId") Long masterId, @RequestBody ClientDto clientDto);

    @GetMapping("/api/services/paginated")
    Page<ServiceDto> getServicesPaginated(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("sortDir") String sortDir,
            @RequestParam("masterId") Long masterId
    );

    @GetMapping("/api/materials/paginated")
    Page<MaterialDto> getMaterialsPaginated(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("sortDir") String sortDir,
            @RequestParam("masterId") Long masterId
    );

    @GetMapping("/api/appointments/paginated")
    Page<AppointmentDto> getAppointmentsPaginated(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("sortDir") String sortDir,
            @RequestParam("masterId") Long masterId
    );

    @GetMapping("/api/appointments/calendar")
    List<AppointmentDto> getAppointmentsByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("masterId") Long masterId
    );

    @GetMapping("/api/clients/{id}")
    ClientDto getClient(@PathVariable Long id, @RequestParam Long masterId);

    @PutMapping("/api/clients/{id}")
    ClientDto updateClient(@PathVariable Long id,
                           @RequestParam("masterId") Long masterId,
                           @RequestBody ClientDto clientDto);

    @DeleteMapping("/api/clients/{id}")
    void deleteClient(@PathVariable Long id,
                      @RequestParam("masterId") Long masterId);

    @GetMapping("/api/services/{id}")
    ServiceDto getService(@PathVariable Long id,
                          @RequestParam("masterId") Long masterId);

    @GetMapping("/api/services")
    List<ServiceDto> getServices(@RequestParam("masterId") Long masterId);

    @PostMapping("/api/services")
    ServiceDto createService(@RequestParam("masterId") Long masterId,
                             @RequestBody ServiceDto serviceDto);

    @PutMapping("/api/services/{id}")
    ServiceDto updateService(@PathVariable Long id,
                             @RequestParam("masterId") Long masterId,
                             @RequestBody ServiceDto serviceDto);

    @DeleteMapping("/api/services/{id}")
    void deleteService(@PathVariable Long id);

    @GetMapping("/api/materials/{id}")
    MaterialDto getMaterial(@PathVariable Long id,
                            @RequestParam("masterId") Long masterId);

    @PostMapping("/api/materials")
    MaterialDto createMaterial(@RequestParam("masterId") Long masterId,
                               @RequestBody MaterialDto materialDto);

    @PutMapping("/api/materials/{id}")
    MaterialDto updateMaterial(@PathVariable Long id,
                               @RequestParam("masterId") Long masterId,
                               @RequestBody MaterialDto materialDto);

    @DeleteMapping("/api/materials/{id}")
    void deleteMaterial(@PathVariable Long id);

    @GetMapping("/api/appointments/{id}")
    AppointmentDto getAppointment(@PathVariable Long id);

    @PostMapping("/api/appointments")
    AppointmentDto createAppointment(@RequestBody AppointmentCreateDto createDto);

    @PostMapping("/api/appointments/{id}/status")
    AppointmentDto updateAppointmentStatus(@PathVariable Long id, @RequestParam("status") String status);

    @DeleteMapping("/api/appointments/{id}")
    void deleteAppointment(@PathVariable Long id);

    @GetMapping("/api/clients/all")
    List<ClientDto> getAllClients(@RequestParam("masterId") Long masterId);

    @GetMapping("/api/services/all")
    List<ServiceDto> getAllServices(@RequestParam("masterId") Long masterId);

    @PutMapping("/api/appointments/{id}")
    AppointmentDto updateAppointment(@PathVariable Long id, @RequestBody AppointmentCreateDto createDto);

    @GetMapping("/api/masters/profile/{id}")
    MasterDto getMasterProfile(@PathVariable Long id);

    @PutMapping("/api/masters/profile/{id}")
    MasterDto updateMasterProfile(@PathVariable Long id, @RequestBody MasterUpdateDto updateDto);

    @GetMapping("/api/clients/by-email")
    ClientDto getClientByEmail(@RequestParam("email") String email);

    @GetMapping("/api/appointments/by-client/{clientId}")
    List<AppointmentDto> getAppointmentsByClientId(@PathVariable Long clientId);

    @GetMapping("/api/masters")
    List<MasterDto> getAllMasters();

    @GetMapping("/api/masters/{id}")
    MasterDto getMasterById(@PathVariable Long id);

    @GetMapping("/api/services/by-master/{masterId}")
    List<ServiceDto> getServicesByMasterId(@PathVariable Long masterId);

    @GetMapping("/api/appointments/check-availability")
    boolean checkAvailability(
            @RequestParam("masterId") Long masterId,
            @RequestParam("date") String date,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime
    );

// ==================== СЛОТЫ ВРЕМЕНИ ====================

    @GetMapping("/api/availability/slots")
    List<AvailabilitySlotDto> getFreeSlots(
            @RequestParam("masterId") Long masterId,
            @RequestParam(value = "serviceId", required = false) Long serviceId,
            @RequestParam("date") String date
    );

    @PostMapping("/api/availability/slots")
    AvailabilitySlotDto createSlot(@RequestBody AvailabilitySlotDto slotDto);

    @DeleteMapping("/api/availability/slots/{slotId}")
    void deleteSlot(@PathVariable Long slotId);
}
