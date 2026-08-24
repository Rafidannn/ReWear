package com.example.application.service.user;

import com.example.application.model.user.*;
import com.example.application.repository.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final UserSchoolVerificationRepository verificationRepository;
    private final BankAccountRepository bankAccountRepository;
    private final SchoolRepository schoolRepository;

    public UserService(UserRepository userRepository,
                       AddressRepository addressRepository,
                       UserSchoolVerificationRepository verificationRepository,
                       BankAccountRepository bankAccountRepository,
                       SchoolRepository schoolRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.verificationRepository = verificationRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.schoolRepository = schoolRepository;
    }

    public List<School> findAllSchools() {
        return schoolRepository.findAll();
    }

    public List<User> findAllUsers() {
        return userRepository.findAllWithSchool();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByIdWithSchool(Long id) {
        return userRepository.findByIdWithSchool(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByEmailWithSchool(String email) {
        return userRepository.findByEmailWithSchool(email);
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return Optional.empty();
        }
        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (rawPassword.equals(user.getPasswordHash())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<Address> getUserAddresses(User user) {
        return addressRepository.findByUserAndDeletedAtIsNull(user);
    }

    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    public Optional<UserSchoolVerification> getVerification(User user) {
        return verificationRepository.findByUser(user);
    }

    public UserSchoolVerification requestSchoolVerification(UserSchoolVerification verification) {
        verification.setStatus(VerificationStatus.PENDING);
        return verificationRepository.save(verification);
    }

    public List<BankAccount> getUserBankAccounts(User user) {
        return bankAccountRepository.findByUser(user);
    }

    public BankAccount saveBankAccount(BankAccount bankAccount) {
        return bankAccountRepository.save(bankAccount);
    }

    public User registerUser(String fullName, String email, String phone, String password, School school) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email.trim().toLowerCase());
        user.setPhone(phone);
        user.setPasswordHash(password);
        user.setRole(Role.BUYER_SELLER);
        user.setSchool(school);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    public User toggleAccountSuspension(User user) {
        if (user == null) return null;
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        } else {
            user.setAccountStatus(AccountStatus.SUSPENDED);
        }
        return userRepository.save(user);
    }

    public User changeUserRole(User user, Role newRole) {
        if (user == null || newRole == null) return user;
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public UserSchoolVerification submitSchoolVerification(User user, School school, String schoolNumber, String proofUrl) {
        if (user == null) return null;
        UserSchoolVerification v = verificationRepository.findByUser(user).orElseGet(() -> {
            UserSchoolVerification newV = new UserSchoolVerification();
            newV.setUser(user);
            return newV;
        });

        v.setSchool(school != null ? school : user.getSchool());
        v.setSchoolNumber(schoolNumber != null ? schoolNumber.trim() : null);
        if (proofUrl != null && !proofUrl.isBlank()) {
            v.setProofUrl(proofUrl);
        }
        v.setStatus(VerificationStatus.PENDING);
        v.setRejectionReason(null);
        return verificationRepository.save(v);
    }

    public List<UserSchoolVerification> getPendingVerifications() {
        return verificationRepository.findByStatus(VerificationStatus.PENDING);
    }

    public Optional<UserSchoolVerification> getVerificationByUserId(Long userId) {
        if (userId == null) return Optional.empty();
        return verificationRepository.findByUserId(userId);
    }

    public User rejectUserSchoolVerification(User user, String rejectionReason) {
        if (user == null) return null;
        Optional<UserSchoolVerification> verOpt = verificationRepository.findByUser(user);
        if (verOpt.isPresent()) {
            UserSchoolVerification v = verOpt.get();
            v.setStatus(VerificationStatus.REJECTED);
            v.setRejectionReason(rejectionReason != null && !rejectionReason.isBlank() ? rejectionReason : "Dokumen KTA tidak valid / buram.");
            verificationRepository.save(v);
        }
        return user;
    }

    public User verifyUserSchool(User user, School school) {
        if (user == null) return null;
        if (school != null) {
            user.setSchool(school);
        } else if (user.getSchool() == null) {
            List<School> allSchools = findAllSchools();
            if (!allSchools.isEmpty()) {
                user.setSchool(allSchools.get(0));
            }
        }
        // update existing verification if any
        Optional<UserSchoolVerification> verOpt = verificationRepository.findByUser(user);
        if (verOpt.isPresent()) {
            UserSchoolVerification v = verOpt.get();
            v.setStatus(VerificationStatus.APPROVED);
            v.setRejectionReason(null);
            verificationRepository.save(v);
        }
        return userRepository.save(user);
    }
}
