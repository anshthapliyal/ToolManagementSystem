package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.user.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    public SuccessResponse createFacilityManager(UserDTO dto);
    public Page<UserListDTO> getFacilityManagers
            (int page, int size, String search, List<String> fields);
    public SuccessResponse updateFacilityManager(long id, MgrUpdateReqDTO req);
    public SuccessResponse deleteFacilityManager(long id);
    //For drop down
    public SuccessResponse getAvailableFacilityManagers(String search, List<String> fields);

    public SuccessResponse createWorkplaceManager(UserDTO dto);


    SuccessResponse uploadProfilePhoto(MultipartFile file, String name,
                                       String newEmail, String password);

    SuccessResponse updateWorkplaceManager(long id, @Valid MgrUpdateReqDTO req);

    SuccessResponse deleteWorkplaceManager(long id);

    Page<WrkMngrListDTO> getWorkplaceManagers(int page, int size, String search, List<String> fields);

    SuccessResponse getAvailableWorkplaceManagers(String search, List<String> fields);

    //Workplace Managers
    SuccessResponse createWorker(@Valid UserDTO dto);

    SuccessResponse updateWorker(long id, @Valid MgrUpdateReqDTO req);

    SuccessResponse deleteWorker(long id);

    Page<WorkerListDTO> getWorkers(int page, int size, String search, List<String> fields);

    SuccessResponse getAvailableWorkers(String search, List<String> fields);

    SuccessResponse getWorkerDetails(long id);

    Map<String, Object> getUserHierarchyInfo(String extractedToken);

    Page<UserHierarchyDTO> getAllUserHierarchies(int page, int size, String role, String email);
}