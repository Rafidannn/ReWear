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
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
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
}
