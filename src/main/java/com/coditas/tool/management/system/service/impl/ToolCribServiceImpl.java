package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolCribDetailsDto;
import com.coditas.tool.management.system.dto.user.UserDTO;
import com.coditas.tool.management.system.dto.user.UserListDTO;
import com.coditas.tool.management.system.entity.Role;
import com.coditas.tool.management.system.entity.ToolCrib;
import com.coditas.tool.management.system.entity.User;
import com.coditas.tool.management.system.entity.Workplace;
import com.coditas.tool.management.system.exception.PremiseNotFoundException;
import com.coditas.tool.management.system.exception.UserEmailAlreadyExistsException;
import com.coditas.tool.management.system.exception.UserNotFoundException;
import com.coditas.tool.management.system.repository.RoleRepository;
import com.coditas.tool.management.system.repository.ToolCribRepository;
import com.coditas.tool.management.system.repository.UserRepository;
import com.coditas.tool.management.system.repository.WorkplaceRepository;
import com.coditas.tool.management.system.service.EmailService;
import com.coditas.tool.management.system.service.ToolCribService;
import com.coditas.tool.management.system.specification.ToolCribSpecification;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToolCribServiceImpl implements ToolCribService {
    private final UserRepository userRepository;
    private final WorkplaceRepository workplaceRepository;
    private final ToolCribRepository toolCribRepository;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    @Autowired
    public ToolCribServiceImpl(UserRepository userRepository, WorkplaceRepository workplaceRepository, ToolCribRepository toolCribRepository, ModelMapper modelMapper, RoleRepository roleRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.workplaceRepository = workplaceRepository;
        this.toolCribRepository = toolCribRepository;
        this.modelMapper = modelMapper;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
    }


    @Override
    public SuccessResponse createToolCribManager(UserDTO dto) {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User workplaceManager = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated workplace manager not found"));

        //Find workplace managed by this user
        Workplace workplace = workplaceRepository.findByWorkplaceManager(workplaceManager)
                .orElseThrow(() -> new PremiseNotFoundException("No workplace found for the logged-in manager"));

        //Find ToolCrib of this workplace
        ToolCrib toolCrib = toolCribRepository.findByWorkplace(workplace)
                .orElseThrow(() -> new PremiseNotFoundException("Tool crib not found for the workplace"));

        //Check if email(or User) already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserEmailAlreadyExistsException("Email already exists! Login or use a different email.");
        }

        //set password
        User user = modelMapper.map(dto, User.class);
        String plainPass = dto.getName().trim().toLowerCase();
        user.setPassword(new BCryptPasswordEncoder().encode(plainPass));

        //Assign role
        Role role = roleRepository.findByRole("ROLE_TOOLCRIBMANAGER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(List.of(role));

        userRepository.save(user);

        //Linking to toolcrib
        toolCrib.getToolCribManagers().add(user);
        toolCribRepository.save(toolCrib);

        //Email
        String subject = "Welcome Aboard Tool Crib Manager " + user.getName() + "!";
        String body = "Email: " + user.getEmail() + "\nPassword: " + plainPass
                + "\nPlease log in and change your password.";
        emailService.sendEmail(user.getEmail(), subject, body);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Tool Crib Manager created and linked successfully.")
                .data(dto)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse updateToolCribManager(Long id, UserDTO dto) throws BadRequestException {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User workplaceManager = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated workplace manager not found"));

        Workplace workplace = workplaceRepository.findByWorkplaceManager(workplaceManager)
                .orElseThrow(() -> new PremiseNotFoundException("No workplace found for the logged-in manager"));

        ToolCrib toolCrib = toolCribRepository.findByWorkplace(workplace)
                .orElseThrow(() -> new PremiseNotFoundException("Tool crib not found for the workplace"));

        User toolCribManager = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Tool Crib Manager not found"));

        //Check if user is ToolCrib Manager
        if (!toolCrib.getToolCribManagers().contains(toolCribManager)) {
            throw new BadRequestException("This user is not a Tool Crib Manager in your ToolCrib.");
        }

        //if email is being updated or not and checking for duplicates in db
        if (!toolCribManager.getEmail().equals(dto.getEmail()) &&
                userRepository.existsByEmail(dto.getEmail())) {
            throw new UserEmailAlreadyExistsException("Email already exists! Use a different one.");
        }
        //updation
        toolCribManager.setName(dto.getName());
        toolCribManager.setEmail(dto.getEmail());

        userRepository.save(toolCribManager);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Tool Crib Manager updated successfully.")
                .data(dto)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse deleteToolCribManager(Long id) throws BadRequestException {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User workplaceManager = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated workplace manager not found"));

        Workplace workplace = workplaceRepository.findByWorkplaceManager(workplaceManager)
                .orElseThrow(() -> new PremiseNotFoundException("No workplace found for the logged-in manager"));

        ToolCrib toolCrib = toolCribRepository.findByWorkplace(workplace)
                .orElseThrow(() -> new PremiseNotFoundException("Tool crib not found for the workplace"));

        User toolCribManager = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Tool Crib Manager not found"));

        //Check if the user is ToolCribManager in this ToolCrib
        if (!toolCrib.getToolCribManagers().contains(toolCribManager)) {
            throw new BadRequestException("This user is not a Tool Crib Manager in your ToolCrib.");
        }

        //Remove from ToolCrib manager list(deleting linkage to avoid refrential constrait)
        toolCrib.getToolCribManagers().remove(toolCribManager);
        toolCribRepository.save(toolCrib);

        //Delete the user
        userRepository.delete(toolCribManager);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Tool Crib Manager deleted successfully.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public Page<UserListDTO> getToolCribManagers(int page, int size, String search, List<String> fields) {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User manager = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated workplace manager not found"));

        Workplace workplace = workplaceRepository.findByWorkplaceManager(manager)
                .orElseThrow(() -> new PremiseNotFoundException("No workplace found for the logged-in manager"));

        ToolCrib toolCrib = toolCribRepository.findByWorkplace(workplace)
                .orElseThrow(() -> new PremiseNotFoundException("ToolCrib not found for the workplace"));

        List<User> cribManagers = toolCrib.getToolCribManagers().stream()
                .filter(User::getActive)//getting active toolCribManager
                .collect(Collectors.toList());

        List<UserListDTO> dtoList = cribManagers.stream()
                .map(user -> {
                    UserListDTO dto = new UserListDTO();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    return dto;
                })
                .filter(dto -> {
                    if (search == null || fields == null) return true;

                    String keyword = search.toLowerCase();
                    boolean matches = true;

                    if (fields.contains("name")) {
                        matches &= dto.getName() != null && dto.getName().toLowerCase().contains(keyword);
                    }
                    if (fields.contains("email")) {
                        matches &= dto.getEmail() != null && dto.getEmail().toLowerCase().contains(keyword);
                    }

                    return matches;
                })
                .collect(Collectors.toList());

        //need to manually set number of pages as i am doing filtering at DTO level (not entity level)
        Pageable pageable = PageRequest.of(page, size);
        //returns the index of the first item for the page and converts to Int fron Long
        int start = Math.toIntExact(pageable.getOffset());
        //Out of bounds checking
        int end = Math.min((start + pageable.getPageSize()), dtoList.size());
        List<UserListDTO> paged = dtoList.subList(start, end);

        return new PageImpl<>(paged, pageable, dtoList.size());
    }

    @Override
    public Page<ToolCribDetailsDto> getToolCribsForFacilityManager
            (String search, List<String> fields, int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User facManager = userRepository.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException("Authenticated Facility Manager Not Found !"));

        Long facilityManagerId = facManager.getId();
        Specification<ToolCrib> spec = ToolCribSpecification
                .searchWithFields(search, fields, facilityManagerId);

        return toolCribRepository.findAll(spec, PageRequest.of(page, size))
                .map(tc -> ToolCribDetailsDto.builder()
                        .id(tc.getId())
                        .name(tc.getName())
                        .workplaceId(tc.getWorkplace().getId())
                        .workplaceName(tc.getWorkplace().getName())
                        .managerEmails(tc.getToolCribManagers().stream()
                                .map(User::getEmail)
                                .collect(Collectors.toList()))
                        .build());
    }


}
