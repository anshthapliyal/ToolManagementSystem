package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.UnreturnedToolWorkerDTO;
import com.coditas.tool.management.system.dto.user.*;
import com.coditas.tool.management.system.entity.*;
import com.coditas.tool.management.system.repository.*;
import com.coditas.tool.management.system.security.JwtHelper;
import com.coditas.tool.management.system.service.S3Service;
import com.coditas.tool.management.system.specification.UserSpecification;
import com.coditas.tool.management.system.exception.UserEmailAlreadyExistsException;
import com.coditas.tool.management.system.exception.UserNotFoundException;
import com.coditas.tool.management.system.service.EmailService;
import com.coditas.tool.management.system.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final FacilityRepository facilityRepository;
    private final S3Service s3Service;
    private final WorkplaceRepository workplaceRepository;
    private final WorkstationRepository workstationRepository;
    private final JwtHelper jwtHelper;
    private final ToolCribRepository toolCribRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, RoleRepository roleRepository,
                           EmailService emailService, FacilityRepository facilityRepository, S3Service s3Service,
                           WorkplaceRepository workplaceRepository, WorkstationRepository workstationRepository,
                           JwtHelper jwtHelper, ToolCribRepository toolCribRepository) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
        this.facilityRepository = facilityRepository;
        this.s3Service = s3Service;
        this.workplaceRepository = workplaceRepository;
        this.workstationRepository = workstationRepository;
        this.jwtHelper = jwtHelper;
        this.toolCribRepository = toolCribRepository;
    }

    @Override
    public SuccessResponse uploadProfilePhoto(MultipartFile profilePhoto, String name,
                                              String email, String password) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        //Update name if given
        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }

        //Update email if provided
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email.trim());
        }

        //Update password if given
        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters long.");
            }
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            user.setPassword(passwordEncoder.encode(password));
        }

        Map<String, String> response = new HashMap<>();

        //Upload profile photo if given
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            String key = "profiles/" + (email != null ? email : user.getEmail()) + "/" + profilePhoto.getOriginalFilename();
            response = s3Service.uploadPhoto(profilePhoto, key);
            user.setProfileImageUrl(response.get("Link"));
        }

        userRepository.save(user);

        //Send confirmation success email
        String subject = "Greetings User " + user.getName() + " ! Here are your Updated Details.";
        StringBuilder bodyBuilder = new StringBuilder("Your updated details:\n");
        if (name != null) bodyBuilder.append("Name: ").append(user.getName()).append("\n");
        if (email != null) bodyBuilder.append("Email: ").append(user.getEmail()).append("\n");
        bodyBuilder.append("Thank You.");
        emailService.sendEmail(user.getEmail(), subject, bodyBuilder.toString());

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Profile updated successfully, and confirmation email sent!")
                .data(response.isEmpty() ? null : response)
                .time(LocalDateTime.now())
                .build();
    }


    @Override
    public SuccessResponse createFacilityManager(UserDTO dto){
        User user = modelMapper.map(dto, User.class);

        String pass = dto.getName().trim().toLowerCase();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(pass));

        //Checking is the user Email already exists in the db
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserEmailAlreadyExistsException("Email already exists! Login or use a different email");
        }

        //Fetches and maps the role from the set of roles
        Role facilityManagerRole = roleRepository.findByRole("ROLE_FACILITYMANAGER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        //if found
        user.setRoles(List.of(facilityManagerRole));

        userRepository.save(user);

        //confirmatio mail
        String subject = "Welcome Aboard Facility Manager "+ user.getName()+ " ! Here are your credentials.";
        String body = "Email: " + user.getEmail() + "\nPassword: " + pass + "\nPlease" +
                " sign in and reset your password. Thank You.";
        emailService.sendEmail(user.getEmail(), subject, body);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Facility Manager added successfully.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse updateFacilityManager(long id, MgrUpdateReqDTO req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No facility manager found with provided details."));
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        userRepository.save(user);
        String subject = "Greetings Facility Manager "+ user.getName()+ " ! Here are your Updated Details.";
        String body = "Name: " + user.getName() + "\nEmail: "+ user.getEmail()+ "\nThank You.";
        emailService.sendEmail(user.getEmail(), subject, body);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Successfully updated.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse deleteFacilityManager(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No facility manager found with provided details."));
        user.setActive(false);
        userRepository.save(user);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Successfully deleted the Facility Manager.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse getAvailableFacilityManagers(String search, List<String> fields) {
        List<User> facilityManagers = userRepository.findByRoleName("ROLE_FACILITYMANAGER");

        List<String> collected = facilityManagers.stream()
                .filter(user -> facilityRepository.findByFacilityManager(user).isEmpty())
                .filter(user -> {
                    if (search == null || fields == null || fields.isEmpty()) return true;

                    String keyword = search.toLowerCase();
                    boolean matches = false;

                    if (fields.contains("name") && user.getName() != null) {
                        matches |= user.getName().toLowerCase().contains(keyword);
                    }
                    if (fields.contains("email") && user.getEmail() != null) {
                        matches |= user.getEmail().toLowerCase().contains(keyword);
                    }

                    return matches;
                })
                .map(User::getEmail)
                .collect(Collectors.toList());

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Here are the available facility managers.")
                .data(collected)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public Page<UserListDTO> getFacilityManagers(int page, int size, String search, List<String> fields) {
        Pageable pageable = PageRequest.of(page, size);

        //Dynamic Query
        Specification<User> spec = Specification
                .where(UserSpecification.hasRole("ROLE_FACILITYMANAGER"))
                .and(UserSpecification.isActive())
                .and(UserSpecification.searchByFields(search, fields));

        Page<User> usersPage = userRepository.findAll(spec, Pageable.unpaged());
        //mapping for DTO
        List<UserListDTO> fullDtoList = usersPage.getContent().stream()
                .map(user -> {
                    UserListDTO dto = modelMapper.map(user, UserListDTO.class);
                    Facility facility = facilityRepository.findByFacilityManager(user).orElse(null);
                    if (facility != null) {
                        dto.setFacilityName(facility.getName());
                        dto.setFacilityAddress(facility.getAddress());
                    }
                    return dto;
                })
                .toList();

        List<UserListDTO> filteredList = fullDtoList.stream()
                .filter(dto -> {
                    if (fields != null && fields.contains("facilityName") && search != null) {
                        return dto.getFacilityName() != null &&
                                dto.getFacilityName().toLowerCase().contains(search.toLowerCase());
                    }
                    return true;
                })
                .toList();

        //Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredList.size());
        List<UserListDTO> pagedList = start > filteredList.size() ? List.of() : filteredList.subList(start, end);

        //returning correct size
        return new PageImpl<>(pagedList, pageable, filteredList.size());
    }


    //WorkplaceManagerEndPoints
    @Override
    public SuccessResponse createWorkplaceManager(UserDTO dto) {
        User user = modelMapper.map(dto, User.class);

        String pass = dto.getName().trim().toLowerCase();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(pass));

        //Checks is the user Email already exists in db
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserEmailAlreadyExistsException("Email already exists! Login or use a different email.");
        }

        Role workplaceManagerRole = roleRepository.findByRole("ROLE_WORKPLACEMANAGER")
                .orElseThrow(() -> new RuntimeException("Role not found."));

        user.setRoles(List.of(workplaceManagerRole));

        userRepository.save(user);

        //success mail
        String subject = "Welcome Aboard Workplace Manager "+ user.getName()+ " ! Here are your credentials.";
        String body = "Email: " + user.getEmail() + "\nPassword: " + pass + "\nPlease" +
                " sign in and reset your password. Thank You.";
        emailService.sendEmail(user.getEmail(), subject, body);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Facility Manager added successfully.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse updateWorkplaceManager(long id, MgrUpdateReqDTO req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No Workplace manager found with provided details."));
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        userRepository.save(user);
        String subject = "Greetings Facility Manager "+ user.getName()+ " ! Here are your Updated Details.";
        String body = "Name: " + user.getName() + "\nEmail: "+ user.getEmail()+ "\nThank You.";
        emailService.sendEmail(user.getEmail(), subject, body);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Successfully updated.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse deleteWorkplaceManager(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No workplace manager found with provided details."));
        user.setActive(false);
        userRepository.save(user);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Successfully deleted the workplace Manager.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public Page<WrkMngrListDTO> getWorkplaceManagers(int page, int size, String search, List<String> fields) {
        Pageable pageable = PageRequest.of(page, size);

        //Filtering
        Specification<User> spec = Specification
                .where(UserSpecification.hasRole("ROLE_WORKPLACEMANAGER"))
                .and(UserSpecification.isActive())
                .and(UserSpecification.searchByFields(search, fields));

        Page<User> usersPage = userRepository.findAll(spec, Pageable.unpaged()); // unpaged since filtering happens after mapping

        // Map Users to DTOs and fetch workplace/facility info
        List<WrkMngrListDTO> fullDtoList = usersPage.getContent().stream()
                .map(user -> {
                    WrkMngrListDTO dto = modelMapper.map(user, WrkMngrListDTO.class);
                    Workplace workplace = workplaceRepository.findByWorkplaceManager(user).orElse(null);
                    if (workplace != null) {
                        dto.setWorkplaceName(workplace.getName());
                        dto.setFacilityName(workplace.getFacility().getName());
                    }
                    return dto;
                })
                .toList();

        //Filter by workplaceName if given
        List<WrkMngrListDTO> filteredList = fullDtoList.stream()
                .filter(dto -> {
                    if (fields != null && fields.contains("workplaceName") && search != null) {
                        return dto.getWorkplaceName() != null &&
                                dto.getWorkplaceName().toLowerCase().contains(search.toLowerCase());
                    }
                    return true;
                })
                .toList();

        //manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredList.size());

        List<WrkMngrListDTO> pagedList = start > filteredList.size() ? List.of() : filteredList.subList(start, end);

        return new PageImpl<>(pagedList, pageable, filteredList.size());
    }


    @Override
    public SuccessResponse getAvailableWorkplaceManagers(String search, List<String> fields) {
        List<User> workplaceManagers = userRepository.findByRoleName("ROLE_WORKPLACEMANAGER");

        List<String> collected = workplaceManagers.stream()
                .filter(user -> workplaceRepository.findByWorkplaceManager(user).isEmpty())
                .filter(user -> {
                    if (search == null || fields == null || fields.isEmpty()) return true;

                    String keyword = search.toLowerCase();
                    boolean matches = false;

                    if (fields.contains("name") && user.getName() != null) {
                        matches |= user.getName().toLowerCase().contains(keyword);
                    }
                    if (fields.contains("email") && user.getEmail() != null) {
                        matches |= user.getEmail().toLowerCase().contains(keyword);
                    }

                    return matches;
                })
                .map(User::getEmail)
                .collect(Collectors.toList());

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Here are the available workplace managers.")
                .data(collected)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse createWorker(UserDTO dto) {
        User user = modelMapper.map(dto, User.class);

        String pass = dto.getName().trim().toLowerCase();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(pass));

        //db checking
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserEmailAlreadyExistsException("Email already exists! Login or use a different email.");
        }

        Role workerRole = roleRepository.findByRole("ROLE_WORKER")
                .orElseThrow(() -> new RuntimeException("Role not found."));

        user.setRoles(List.of(workerRole));

        userRepository.save(user);

        String subject = "Welcome Aboard Worker "+ user.getName()+ " ! Here are your credentials.";
        String body = "Email: " + user.getEmail() + "\nPassword: " + pass + "\nPlease" +
                " sign in and reset your password. Thank You.";
        emailService.sendEmail(user.getEmail(), subject, body);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Worker added successfully.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse updateWorker(long id, MgrUpdateReqDTO req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No Worker found with provided details."));
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        userRepository.save(user);
        String subject = "Greetings Worker "+ user.getName()+ " ! Here are your Updated Details.";
        String body = "Name: " + user.getName() + "\nEmail: "+ user.getEmail()+ "\nThank You.";
        emailService.sendEmail(user.getEmail(), subject, body);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Worker Successfully updated.")
                .time(LocalDateTime.now())
                .build();    }

    @Override
    public SuccessResponse deleteWorker(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No Worker found with provided details."));
        user.setActive(false);
        userRepository.save(user);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Successfully deleted the Facility Manager.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public Page<WorkerListDTO> getWorkers(int page, int size, String search, List<String> fields) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasRole("ROLE_WORKER"))
                .and(UserSpecification.isActive())
                .and(UserSpecification.searchByFields(search, fields)); // name & email only

        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAll(spec, pageable);

        //mapping
        List<WorkerListDTO> dtos = usersPage.getContent().stream()
                .map(user -> {
                    WorkerListDTO dto = new WorkerListDTO();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());

                    workstationRepository.findByWorker(user).ifPresent(ws -> {
                        dto.setWorkstationCode(ws.getStationCode());
                        if (ws.getWorkplace() != null) {
                            dto.setWorkplaceName(ws.getWorkplace().getName());
                            if (ws.getWorkplace().getFacility() != null) {
                                dto.setFacilityName(ws.getWorkplace().getFacility().getName());
                            }
                        }
                    });

                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, usersPage.getTotalElements());
    }



    @Override
    public SuccessResponse getAvailableWorkers(String search, List<String> fields) {
        List<User> workers = userRepository.findByRoleName("ROLE_WORKER");

        List<String> collected = workers.stream()
                .filter(worker -> workstationRepository.findByWorker(worker).isEmpty())
                .filter(worker -> {
                    if (search == null || fields == null || fields.isEmpty()) return true;

                    String keyword = search.toLowerCase();
                    boolean matches = false;

                    if (fields.contains("name") && worker.getName() != null) {
                        matches |= worker.getName().toLowerCase().contains(keyword);
                    }
                    if (fields.contains("email") && worker.getEmail() != null) {
                        matches |= worker.getEmail().toLowerCase().contains(keyword);
                    }

                    return matches;
                })
                .map(User::getEmail)
                .collect(Collectors.toList());

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Here are the available workers.")
                .data(collected)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse getWorkerDetails(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found."));
        UnreturnedToolWorkerDTO dto = UnreturnedToolWorkerDTO.builder()
                .workerName(user.getName())
                .workerEmail(user.getEmail())
                .workerImageUrl(user.getProfileImageUrl())
                .build();

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Here are the worker details")
                .data(dto)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public Map<String, Object> getUserHierarchyInfo(String token) {
        String email = jwtHelper.getUsernameFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = user.getRoles().get(0).getRole();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("role", role);

        switch (role) {
            case "ROLE_OWNER" -> {
                response.put("name", user.getName());
                response.put("email", user.getEmail());
            }
            case "ROLE_FACILITYMANAGER" -> {
                Facility facility = facilityRepository.findByFacilityManager(user)
                        .orElseThrow(() -> new RuntimeException("Facility not found"));
                response.put("facilityName", facility.getName());
                response.put("workplaces", facility.getWorkplaces().stream().map(Workplace::getName).toList());
                response.put("name", user.getName());
                response.put("email", user.getEmail());
            }
            case "ROLE_WORKPLACEMANAGER" -> {
                Workplace workplace = workplaceRepository.findByWorkplaceManager(user)
                        .orElseThrow(() -> new RuntimeException("Workplace not found"));
                ToolCrib toolCrib = toolCribRepository.findByWorkplace(workplace)
                        .orElseThrow(() -> new RuntimeException("ToolCrib not found"));
                response.put("facilityName", workplace.getFacility().getName());
                response.put("workplaceName", workplace.getName());
                response.put("toolCribName", toolCrib.getName());
                response.put("name", user.getName());
                response.put("email", user.getEmail());
            }
            case "ROLE_TOOLCRIBMANAGER" -> {
                ToolCrib toolCrib = toolCribRepository.findByToolCribManagersContaining(user)
                        .orElseThrow(() -> new RuntimeException("ToolCrib not found"));
                Workplace wp = toolCrib.getWorkplace();
                response.put("facilityName", wp.getFacility().getName());
                response.put("workplaceName", wp.getName());
                response.put("toolCribName", toolCrib.getName());
                response.put("name", user.getName());
                response.put("email", user.getEmail());
            }
            case "ROLE_WORKER" -> {
                Workstation ws = workstationRepository.findByWorker(user)
                        .orElseThrow(() -> new RuntimeException("Workstation not found"));
                Workplace wp = ws.getWorkplace();
                ToolCrib toolCrib = toolCribRepository.findByWorkplace(wp)
                        .orElseThrow(() -> new RuntimeException("ToolCrib not found"));
                response.put("facilityName", wp.getFacility().getName());
                response.put("workplaceName", wp.getName());
                response.put("toolCribName", toolCrib.getName());
                response.put("workstationCode", ws.getStationCode());
                response.put("name", user.getName());
                response.put("email", user.getEmail());
            }
            default -> throw new RuntimeException("Invalid role");
        }

        return response;
    }


    @Override
    public Page<UserHierarchyDTO> getAllUserHierarchies(int page, int size, String role, String email) {
        Pageable fetchLargerPage = PageRequest.of(page, size * 3); // over-fetch
        Specification<User> spec = Specification.where(null);

        if (role != null && !role.isBlank()) {
            spec = spec.and((root, query, cb)
                    -> cb.equal(root.join("roles").get("role"), role));
        }

        if (email != null && !email.isBlank()) {
            spec = spec.and((root, query, cb)
                    -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }

        Page<User> users = userRepository.findAll(spec, fetchLargerPage);
        List<UserHierarchyDTO> filteredResult = new ArrayList<>();

        for (User user : users.getContent()) {
            String userRole = user.getRoles().get(0).getRole();
            UserHierarchyDTO.UserHierarchyDTOBuilder builder = UserHierarchyDTO.builder()
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(userRole);

            switch (userRole) {
                case "ROLE_OWNER" -> filteredResult.add(builder.build());

                case "ROLE_FACILITYMANAGER" -> facilityRepository.findByFacilityManager(user).ifPresent(facility -> {
                    builder.facilityName(facility.getName());
                    builder.workplaces(facility.getWorkplaces().stream().map(Workplace::getName).toList());
                    filteredResult.add(builder.build());
                });

                case "ROLE_WORKPLACEMANAGER" -> workplaceRepository.findByWorkplaceManager(user)
                        .ifPresent(wp -> toolCribRepository.findByWorkplace(wp)
                                .ifPresent(toolCrib -> {
                    builder.facilityName(wp.getFacility().getName())
                            .workplaceName(wp.getName())
                            .toolCribName(toolCrib.getName());
                    filteredResult.add(builder.build());
                }));

                case "ROLE_TOOLCRIBMANAGER" -> toolCribRepository.findByToolCribManagersContaining(user).ifPresent(toolCrib -> {
                    Workplace wp = toolCrib.getWorkplace();
                    builder.facilityName(wp.getFacility().getName())
                            .workplaceName(wp.getName())
                            .toolCribName(toolCrib.getName());
                    filteredResult.add(builder.build());
                });

                case "ROLE_WORKER" -> workstationRepository.findByWorker(user).ifPresent(ws -> {
                    Workplace wp = ws.getWorkplace();
                    toolCribRepository.findByWorkplace(wp).ifPresent(toolCrib -> {
                        builder.facilityName(wp.getFacility().getName())
                                .workplaceName(wp.getName())
                                .toolCribName(toolCrib.getName())
                                .workstationCode(ws.getStationCode());
                        filteredResult.add(builder.build());
                    });
                });
            }

            if (filteredResult.size() >= size) break; //i am doing filtering post pagination -> that is why
            //manual pagination must happen
        }

        int total = (int) users.getTotalElements(); //total users
        return new PageImpl<>(filteredResult.subList(0, Math.min(size, filteredResult.size())),
                PageRequest.of(page, size), total);
    }
}
